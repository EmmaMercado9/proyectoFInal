package com.ema.practica.infrastructure.importacion.useCases;

import com.ema.practica.infrastructure.exception.VentaInvalidaException;
import com.ema.practica.infrastructure.importacion.crud.FacturaImportadaRepository;
import com.ema.practica.infrastructure.importacion.entity.FacturaImportadaEntity;
import com.ema.practica.infrastructure.importacion.lectores.FacturaLeida;
import com.ema.practica.infrastructure.importacion.lectores.LectorFactura;
import com.ema.practica.infrastructure.importacion.lectores.LineaFactura;
import com.ema.practica.infrastructure.importacion.rest.model.CandidatoProductoDTO;
import com.ema.practica.infrastructure.importacion.rest.model.FacturaImportadaDTO;
import com.ema.practica.infrastructure.importacion.rest.model.ImportacionFacturaDTO;
import com.ema.practica.infrastructure.importacion.rest.model.ModeloImportadoDTO;
import com.ema.practica.infrastructure.importacion.rest.model.ResultadoImportacionDTO;
import com.ema.practica.infrastructure.importacion.rest.model.TalleImportadoDTO;
import com.ema.practica.infrastructure.moduloProduct.atributos.crud.AtributoRepository;
import com.ema.practica.infrastructure.moduloProduct.atributos.entity.AtributoEntity;
import com.ema.practica.infrastructure.moduloProduct.atributos.rest.model.AtributoValorDTO;
import com.ema.practica.infrastructure.moduloProduct.product.crud.ProductRepository;
import com.ema.practica.infrastructure.moduloProduct.product.entity.ProductEntity;
import com.ema.practica.infrastructure.moduloProduct.product.rest.model.ProductRequestDTO;
import com.ema.practica.infrastructure.moduloProduct.product.useCases.ProductService;
import com.ema.practica.infrastructure.moduloProduct.productVariant.crud.ProductVariantRepository;
import com.ema.practica.infrastructure.moduloProduct.productVariant.entity.ProductVariantEntity;
import com.ema.practica.infrastructure.moduloProduct.productVariant.rest.model.VarianteRequestDTO;
import com.ema.practica.infrastructure.util.status.StatusEntity;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Importación de productos desde la factura PDF de un proveedor, en dos pasos:
 * <ol>
 *   <li>{@link #leer}: extrae el texto, elige el lector del proveedor y arma una vista
 *       previa agrupada por modelo, marcando lo que ya existe. No guarda nada.</li>
 *   <li>{@link #confirmar}: con lo que el usuario revisó, crea productos y variantes o
 *       suma stock, todo en una transacción, y registra la factura para no repetirla.</li>
 * </ol>
 */
@Service
@RequiredArgsConstructor
public class ImportacionFacturaService {

    /** Precio sugerido cuando la factura no trae precio de lista: costo + 50 %. */
    static final BigDecimal MARGEN_SUGERIDO = new BigDecimal("1.5");
    static final String ATRIBUTO_TALLE = "Talle";
    static final String SIN_TALLE = "Único";
    /** Desde este parecido de nombre se asocia solo; más abajo queda como sugerencia. */
    static final double PARECIDO_AUTOMATICO = 0.8;
    static final double PARECIDO_SUGERENCIA = 0.4;
    private static final int MAX_CANDIDATOS = 5;
    private static final DateTimeFormatter DMY = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final List<LectorFactura> lectores;     // Spring inyecta todos los @Component LectorFactura
    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
    private final AtributoRepository atributoRepository;
    private final ProductService productService;
    private final FacturaImportadaRepository facturaImportadaRepository;

    // ─── Paso 1: leer ─────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public ImportacionFacturaDTO leer(byte[] pdf) {
        String texto = extraerTexto(pdf);
        LectorFactura lector = lectores.stream()
                .filter(l -> l.reconoce(texto))
                .findFirst()
                .orElseThrow(() -> new VentaInvalidaException("No se reconoce el formato de esta factura."));
        FacturaLeida factura = lector.leer(lector.esGenerico() ? extraerTexto(pdf, true) : texto);
        if (factura.lineas().isEmpty()) {
            throw new VentaInvalidaException("No se encontraron productos en la factura.");
        }
        ImportacionFacturaDTO dto = vistaPrevia(factura);
        dto.setGenerica(lector.esGenerico());
        return dto;
    }

    String extraerTexto(byte[] pdf) {
        return extraerTexto(pdf, false);
    }

    private String extraerTexto(byte[] pdf, boolean ordenVisual) {
        try (PDDocument doc = Loader.loadPDF(pdf)) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(ordenVisual);
            String texto = stripper.getText(doc);
            if (texto.isBlank()) {
                throw new VentaInvalidaException(
                        "El PDF no tiene texto (parece una imagen escaneada); no se puede leer.");
            }
            return texto;
        } catch (IOException e) {
            throw new VentaInvalidaException("El archivo no es un PDF válido.");
        }
    }

    ImportacionFacturaDTO vistaPrevia(FacturaLeida f) {
        ImportacionFacturaDTO dto = new ImportacionFacturaDTO();
        dto.setProveedor(f.proveedor());
        dto.setCuit(f.cuit());
        dto.setNumero(f.numero());
        dto.setFecha(f.fecha());
        dto.setTotal(f.total());
        dto.setSumaLineas(f.sumaLineas());
        dto.setTotalCoincide(f.total() != null
                && f.total().subtract(f.sumaLineas()).abs().compareTo(BigDecimal.ONE) <= 0);
        dto.setUnidades(f.lineas().stream().mapToInt(LineaFactura::cantidad).sum());
        if (f.cuit() != null && f.numero() != null) {
            facturaImportadaRepository.findByCuitAndNumero(f.cuit(), f.numero()).ifPresent(prev ->
                    dto.setYaImportada("Esta factura ya se importó el "
                            + prev.getImportadaEn().format(DMY) + ". Confirmarla duplicaría el stock."));
        }

        // Un modelo por código de artículo, en el orden en que aparecen
        Map<String, List<LineaFactura>> porModelo = new LinkedHashMap<>();
        for (LineaFactura l : f.lineas()) {
            porModelo.computeIfAbsent(l.codigoArticulo(), k -> new ArrayList<>()).add(l);
        }
        List<ProductEntity> catalogo = productRepository.findAll().stream()
                .filter(p -> p.getStatus() != StatusEntity.INACTIVO)
                .toList();
        for (Map.Entry<String, List<LineaFactura>> e : porModelo.entrySet()) {
            dto.getModelos().add(modelo(e.getKey(), e.getValue(), catalogo));
        }
        return dto;
    }

    private ModeloImportadoDTO modelo(String codigo, List<LineaFactura> lineas, List<ProductEntity> catalogo) {
        LineaFactura primera = lineas.get(0);
        ModeloImportadoDTO m = new ModeloImportadoDTO();
        m.setCodigoArticulo(codigo);
        m.setNombre(primera.descripcion());
        m.setMarca(primera.marca());

        Optional<ProductEntity> porCodigo = buscarPorCodigo(codigo, primera.skuVariante());
        List<ProductEntity> parecidos = parecidos(primera.descripcion(), catalogo);
        Optional<ProductEntity> existente = porCodigo.isPresent() ? porCodigo : parecidos.stream()
                .filter(p -> parecido(primera.descripcion(), p.getNameProduct()) >= PARECIDO_AUTOMATICO)
                .findFirst();
        existente.ifPresent(p -> {
            m.setProductoId(p.getIdProduct());
            m.setProductoExistente(p.getNameProduct());
            m.setCoincidencia(porCodigo.isPresent() ? "CODIGO" : "NOMBRE");
        });
        Map<UUID, ProductEntity> candidatos = new LinkedHashMap<>();
        existente.ifPresent(p -> candidatos.put(p.getIdProduct(), p));
        parecidos.forEach(p -> candidatos.putIfAbsent(p.getIdProduct(), p));
        m.setCandidatos(candidatos.values().stream().map(ImportacionFacturaService::candidato).toList());
        m.setPrecioVenta(existente.map(ProductEntity::getPrecioVenta).orElse(precioSugerido(primera)));

        // Un mismo talle puede venir en dos renglones: se suman
        Map<String, TalleImportadoDTO> porTalle = new LinkedHashMap<>();
        for (LineaFactura l : lineas) {
            TalleImportadoDTO t = porTalle.computeIfAbsent(l.talle(), k -> {
                TalleImportadoDTO nuevo = new TalleImportadoDTO();
                nuevo.setTalle(l.talle());
                nuevo.setSku(l.skuVariante());
                nuevo.setCostoUnitario(l.costoUnitario());
                existente.flatMap(p -> resolverVariante(p, l.skuVariante(), l.talle())).ifPresent(v -> {
                    nuevo.setVarianteId(v.getVariantId());
                    nuevo.setStockActual(v.getStock());
                });
                return nuevo;
            });
            t.setCantidad(t.getCantidad() + l.cantidad());
        }
        List<TalleImportadoDTO> talles = new ArrayList<>(porTalle.values());
        talles.sort(Comparator.comparing(ImportacionFacturaService::ordenTalle));
        m.setTalles(talles);
        return m;
    }

    /** Precio de lista del proveedor si lo hay; si no, costo + 50 % redondeado a $100. */
    static BigDecimal precioSugerido(LineaFactura l) {
        if (l.precioLista() != null && l.precioLista().compareTo(l.costoUnitario()) > 0) {
            return l.precioLista().setScale(2, RoundingMode.HALF_UP);
        }
        return l.costoUnitario().multiply(MARGEN_SUGERIDO)
                .divide(BigDecimal.valueOf(100), 0, RoundingMode.CEILING)
                .multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP);
    }

    /** "38.5" → 38.5; "US 8.5" → 8.5; lo que no es número va al final. */
    private static double ordenTalle(TalleImportadoDTO t) {
        try {
            return Double.parseDouble(t.getTalle().replaceAll("[^0-9.]", ""));
        } catch (NumberFormatException e) {
            return Double.MAX_VALUE;
        }
    }

    /** Producto con ese código: SKU o código de barras, propio o de alguna de sus variantes. */
    private Optional<ProductEntity> buscarPorCodigo(String codigo, String skuVariante) {
        return productRepository.findBySku(codigo)
                .or(() -> productRepository.findByCodigoBarra(codigo))
                .or(() -> variantRepository.findFirstBySkuIgnoreCase(skuVariante).map(ProductVariantEntity::getProduct))
                .or(() -> variantRepository.findByCodigoBarra(codigo).map(ProductVariantEntity::getProduct));
    }

    /** Los productos de nombre más parecido, del más al menos parecido. */
    private static List<ProductEntity> parecidos(String descripcion, List<ProductEntity> catalogo) {
        return catalogo.stream()
                .filter(p -> parecido(descripcion, p.getNameProduct()) >= PARECIDO_SUGERENCIA)
                .sorted(Comparator.comparingDouble((ProductEntity p) -> parecido(descripcion, p.getNameProduct())).reversed())
                .limit(MAX_CANDIDATOS)
                .toList();
    }

    /** Palabras en común sobre palabras totales, sin importar orden, acentos ni mayúsculas. */
    static double parecido(String a, String b) {
        Set<String> x = palabras(a), y = palabras(b);
        if (x.isEmpty() || y.isEmpty()) return 0;
        Set<String> comunes = new HashSet<>(x);
        comunes.retainAll(y);
        Set<String> todas = new HashSet<>(x);
        todas.addAll(y);
        double parecido = (double) comunes.size() / todas.size();
        // "Runfalcon" y "Zapatillas Runfalcon": un nombre dentro del otro es una buena sugerencia
        boolean incluido = comunes.size() == Math.min(x.size(), y.size());
        return incluido ? Math.max(parecido, PARECIDO_SUGERENCIA) : parecido;
    }

    private static Set<String> palabras(String s) {
        if (s == null) return Set.of();
        String limpio = Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", " ").trim();
        return limpio.isEmpty() ? Set.of() : new HashSet<>(Arrays.asList(limpio.split(" ")));
    }

    private static CandidatoProductoDTO candidato(ProductEntity p) {
        boolean sinTalles = !Boolean.TRUE.equals(p.getTieneVariantes());
        List<CandidatoProductoDTO.VarianteExistente> variantes = p.getVariants().stream()
                .filter(v -> Boolean.TRUE.equals(v.getEsDefault()) == sinTalles)
                .map(v -> new CandidatoProductoDTO.VarianteExistente(v.getVariantId(), valorTalle(v).orElse(null), v.getStock()))
                .toList();
        return new CandidatoProductoDTO(p.getIdProduct(), p.getNameProduct(), p.getSku(), p.getPrecioVenta(), sinTalles, variantes);
    }

    private static Optional<String> valorTalle(ProductVariantEntity v) {
        return v.getAtributos().stream()
                .filter(a -> a.getAtributo().getNombre().toLowerCase(Locale.ROOT).startsWith("tall"))
                .map(a -> a.getValor())
                .findFirst();
    }

    /**
     * Variante donde cargar ese renglón. Un producto sin talles tiene una sola (la interna);
     * si la factura no indica talle y el producto tiene una única variante, es esa.
     */
    private static Optional<ProductVariantEntity> resolverVariante(ProductEntity p, String sku, String talle) {
        if (!Boolean.TRUE.equals(p.getTieneVariantes())) {
            return p.getVariants().stream().filter(v -> Boolean.TRUE.equals(v.getEsDefault())).findFirst();
        }
        List<ProductVariantEntity> variantes = p.getVariants().stream()
                .filter(v -> !Boolean.TRUE.equals(v.getEsDefault()))
                .toList();
        String buscado = talle.replace(',', '.');
        Optional<ProductVariantEntity> exacta = variantes.stream()
                .filter(v -> sku.equalsIgnoreCase(v.getSku()) || sku.equalsIgnoreCase(v.getCodigoBarra())
                        || valorTalle(v).filter(buscado::equalsIgnoreCase).isPresent())
                .findFirst();
        if (exacta.isEmpty() && SIN_TALLE.equalsIgnoreCase(talle) && variantes.size() == 1) {
            return Optional.of(variantes.get(0));
        }
        return exacta;
    }

    // ─── Historial ──────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<FacturaImportadaDTO> listar() {
        return facturaImportadaRepository.findAllByOrderByImportadaEnDesc().stream()
                .map(FacturaImportadaDTO::de)
                .toList();
    }

    // ─── Paso 2: confirmar ──────────────────────────────────────────────────

    @Transactional
    public ResultadoImportacionDTO confirmar(ImportacionFacturaDTO dto, String usuario) {
        facturaImportadaRepository.findByCuitAndNumero(dto.getCuit(), dto.getNumero()).ifPresent(prev -> {
            throw new VentaInvalidaException("La factura " + dto.getNumero() + " de " + dto.getProveedor()
                    + " ya se importó el " + prev.getImportadaEn().format(DMY) + ".");
        });
        AtributoEntity talle = atributoRepository.findByNombreIgnoreCase(ATRIBUTO_TALLE)
                .orElseThrow(() -> new VentaInvalidaException("Falta el atributo \"Talle\" en el sistema."));

        int productosCreados = 0, variantesCreadas = 0, stockSumado = 0, unidades = 0;
        for (ModeloImportadoDTO m : dto.getModelos()) {
            // En la vista previa se puede corregir un talle: no pueden quedar dos iguales
            Set<String> vistos = new HashSet<>();
            for (TalleImportadoDTO t : m.getTalles()) {
                if (!vistos.add(t.getTalle().trim().toUpperCase())) {
                    throw new VentaInvalidaException("El talle " + t.getTalle() + " está repetido en " + m.getNombre() + ".");
                }
            }
            // El producto que quedó asociado en la vista previa; si no hay, uno con el mismo código
            ProductEntity producto = m.getProductoId() != null
                    ? productRepository.findById(m.getProductoId())
                            .orElseThrow(() -> new VentaInvalidaException("El producto asociado a " + m.getNombre() + " ya no existe."))
                    : buscarPorCodigo(m.getCodigoArticulo(), m.getTalles().get(0).getSku()).orElse(null);
            if (producto == null) {
                producto = crearProducto(m);
                productosCreados++;
            }
            for (TalleImportadoDTO t : m.getTalles()) {
                Optional<ProductVariantEntity> variante = resolverVariante(producto, t.getSku(), t.getTalle());
                if (variante.isPresent()) {
                    productService.sumarStockCompra(variante.get().getVariantId(), t.getCantidad(), t.getCostoUnitario());
                    stockSumado++;
                } else {
                    crearVariante(producto, m, t, talle);
                    variantesCreadas++;
                }
                unidades += t.getCantidad();
            }
        }

        FacturaImportadaEntity registro = new FacturaImportadaEntity();
        registro.setCuit(dto.getCuit());
        registro.setProveedor(dto.getProveedor());
        registro.setNumero(dto.getNumero());
        registro.setFecha(dto.getFecha());
        registro.setTotal(dto.getTotal());
        registro.setUnidades(unidades);
        registro.setUsuario(usuario);
        registro.setImportadaEn(LocalDateTime.now());
        facturaImportadaRepository.save(registro);

        return new ResultadoImportacionDTO(productosCreados, variantesCreadas, stockSumado, unidades);
    }

    private ProductEntity crearProducto(ModeloImportadoDTO m) {
        ProductRequestDTO req = new ProductRequestDTO();
        req.setNameProduct(m.getNombre());
        req.setSku(m.getCodigoArticulo());
        req.setMarca(m.getMarca());
        req.setPrecioVenta(m.getPrecioVenta());
        req.setCosto(m.getTalles().stream().map(TalleImportadoDTO::getCostoUnitario)
                .min(BigDecimal::compareTo).orElse(BigDecimal.ZERO));
        req.setSubCategoriaId(m.getSubCategoriaId());
        req.setTieneVariantes(true);
        try {
            UUID id = productService.crearProducto(req, null).getIdProduct();
            return productRepository.findById(id).orElseThrow();
        } catch (IOException e) {
            throw new VentaInvalidaException("No se pudo crear el producto " + m.getCodigoArticulo());
        }
    }

    private void crearVariante(ProductEntity producto, ModeloImportadoDTO m, TalleImportadoDTO t, AtributoEntity talle) {
        // Asociado a un producto con otro código: el talle nuevo sigue el SKU de ese producto
        String sku = producto.getSku() == null || m.getCodigoArticulo().equalsIgnoreCase(producto.getSku())
                ? t.getSku()
                : producto.getSku() + "-" + t.getTalle().replaceAll("\\s+", "");
        if (variantRepository.existsBySku(sku)) {
            throw new VentaInvalidaException("El SKU " + sku + " ya lo usa otro producto.");
        }
        AtributoValorDTO valor = new AtributoValorDTO();
        valor.setAtributoId(talle.getAtributoId());
        valor.setValor(t.getTalle());

        VarianteRequestDTO req = new VarianteRequestDTO();
        req.setSku(sku);
        req.setPrecioVenta(m.getPrecioVenta());
        req.setCosto(t.getCostoUnitario());
        req.setStock(t.getCantidad());
        req.setStockMinimo(1);
        req.setAtributos(List.of(valor));
        try {
            productService.agregarVariante(producto.getIdProduct(), req, null);
        } catch (IOException e) {
            throw new VentaInvalidaException("No se pudo crear la variante " + sku);
        }
    }
}
