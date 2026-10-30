package com.ema.practica.infrastructure.importacion.useCases;

import com.ema.practica.infrastructure.exception.VentaInvalidaException;
import com.ema.practica.infrastructure.importacion.crud.FacturaImportadaRepository;
import com.ema.practica.infrastructure.importacion.entity.FacturaImportadaEntity;
import com.ema.practica.infrastructure.importacion.lectores.FacturaLeida;
import com.ema.practica.infrastructure.importacion.lectores.LectorFactura;
import com.ema.practica.infrastructure.importacion.lectores.LineaFactura;
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
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
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
                .orElseThrow(() -> new VentaInvalidaException(
                        "No se reconoce el formato de esta factura. Formatos disponibles: "
                                + "Adidas Argentina y Factory Sport (Seven Sport)."));
        FacturaLeida factura = lector.leer(texto);
        if (factura.lineas().isEmpty()) {
            throw new VentaInvalidaException("No se encontraron productos en la factura.");
        }
        return vistaPrevia(factura);
    }

    String extraerTexto(byte[] pdf) {
        try (PDDocument doc = Loader.loadPDF(pdf)) {
            String texto = new PDFTextStripper().getText(doc);
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
        dto.setTotalCoincide(f.total() != null && f.total().compareTo(f.sumaLineas()) == 0);
        dto.setUnidades(f.lineas().stream().mapToInt(LineaFactura::cantidad).sum());
        if (f.numero() != null) {
            facturaImportadaRepository.findByCuitAndNumero(f.cuit(), f.numero()).ifPresent(prev ->
                    dto.setYaImportada("Esta factura ya se importó el "
                            + prev.getImportadaEn().format(DMY) + ". Confirmarla duplicaría el stock."));
        }

        // Un modelo por código de artículo, en el orden en que aparecen
        Map<String, List<LineaFactura>> porModelo = new LinkedHashMap<>();
        for (LineaFactura l : f.lineas()) {
            porModelo.computeIfAbsent(l.codigoArticulo(), k -> new ArrayList<>()).add(l);
        }
        for (Map.Entry<String, List<LineaFactura>> e : porModelo.entrySet()) {
            dto.getModelos().add(modelo(e.getKey(), e.getValue()));
        }
        return dto;
    }

    private ModeloImportadoDTO modelo(String codigo, List<LineaFactura> lineas) {
        LineaFactura primera = lineas.get(0);
        ModeloImportadoDTO m = new ModeloImportadoDTO();
        m.setCodigoArticulo(codigo);
        m.setNombre(primera.descripcion());
        m.setMarca(primera.marca());

        Optional<ProductEntity> existente = productRepository.findBySku(codigo);
        existente.ifPresent(p -> {
            m.setProductoId(p.getIdProduct());
            m.setProductoExistente(p.getNameProduct());
        });
        m.setPrecioVenta(existente.map(ProductEntity::getPrecioVenta).orElse(precioSugerido(primera)));

        // Un mismo talle puede venir en dos renglones: se suman
        Map<String, TalleImportadoDTO> porTalle = new LinkedHashMap<>();
        for (LineaFactura l : lineas) {
            TalleImportadoDTO t = porTalle.computeIfAbsent(l.talle(), k -> {
                TalleImportadoDTO nuevo = new TalleImportadoDTO();
                nuevo.setTalle(l.talle());
                nuevo.setSku(l.skuVariante());
                nuevo.setCostoUnitario(l.costoUnitario());
                existente.flatMap(p -> buscarVariante(p, l.skuVariante(), l.talle())).ifPresent(v -> {
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

    /** Variante del producto con ese SKU o con ese valor de Talle. */
    private static Optional<ProductVariantEntity> buscarVariante(ProductEntity p, String sku, String talle) {
        return p.getVariants().stream()
                .filter(v -> !Boolean.TRUE.equals(v.getEsDefault()))
                .filter(v -> sku.equalsIgnoreCase(v.getSku()) || v.getAtributos().stream().anyMatch(a ->
                        ATRIBUTO_TALLE.equalsIgnoreCase(a.getAtributo().getNombre())
                                && talle.equalsIgnoreCase(a.getValor())))
                .findFirst();
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
            ProductEntity producto = productRepository.findBySku(m.getCodigoArticulo()).orElse(null);
            if (producto == null) {
                producto = crearProducto(m);
                productosCreados++;
            } else if (!Boolean.TRUE.equals(producto.getTieneVariantes())) {
                throw new VentaInvalidaException("El producto " + m.getCodigoArticulo()
                        + " ya existe sin talles; no se le pueden agregar variantes.");
            }
            for (TalleImportadoDTO t : m.getTalles()) {
                Optional<ProductVariantEntity> variante = buscarVariante(producto, t.getSku(), t.getTalle());
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
        if (variantRepository.existsBySku(t.getSku())) {
            throw new VentaInvalidaException("El SKU " + t.getSku() + " ya lo usa otro producto.");
        }
        AtributoValorDTO valor = new AtributoValorDTO();
        valor.setAtributoId(talle.getAtributoId());
        valor.setValor(t.getTalle());

        VarianteRequestDTO req = new VarianteRequestDTO();
        req.setSku(t.getSku());
        req.setPrecioVenta(m.getPrecioVenta());
        req.setCosto(t.getCostoUnitario());
        req.setStock(t.getCantidad());
        req.setStockMinimo(1);
        req.setAtributos(List.of(valor));
        try {
            productService.agregarVariante(producto.getIdProduct(), req, null);
        } catch (IOException e) {
            throw new VentaInvalidaException("No se pudo crear la variante " + t.getSku());
        }
    }
}
