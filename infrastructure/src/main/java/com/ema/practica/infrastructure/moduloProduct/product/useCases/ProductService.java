package com.ema.practica.infrastructure.moduloProduct.product.useCases;

import com.ema.practica.infrastructure.exception.RecursoNoEncontradoException;

import com.ema.practica.infrastructure.exception.VentaInvalidaException;
import com.ema.practica.infrastructure.images.ImageModel;
import com.ema.practica.infrastructure.images.services.ImagenService;
import com.ema.practica.infrastructure.moduloProduct.atributos.crud.AtributoRepository;
import com.ema.practica.infrastructure.moduloProduct.atributos.crud.VarianteAtributoRepository;
import com.ema.practica.infrastructure.moduloProduct.atributos.entity.AtributoEntity;
import com.ema.practica.infrastructure.moduloProduct.atributos.entity.VarianteAtributoEntity;
import com.ema.practica.infrastructure.moduloProduct.atributos.rest.model.AtributoValorDTO;
import com.ema.practica.infrastructure.moduloProduct.product.crud.ProductRepository;
import com.ema.practica.infrastructure.moduloProduct.product.entity.ProductEntity;
import com.ema.practica.infrastructure.moduloProduct.product.rest.mapper.ProductMapper;
import com.ema.practica.infrastructure.moduloProduct.product.rest.model.ProductListadoDTO;
import com.ema.practica.infrastructure.moduloProduct.product.rest.model.ProductRequestDTO;
import com.ema.practica.infrastructure.moduloProduct.product.rest.model.ProductResponseDTO;
import com.ema.practica.infrastructure.moduloProduct.product.rest.model.ProductUpdateDTO;
import com.ema.practica.infrastructure.moduloProduct.productVariant.crud.ProductVariantRepository;
import com.ema.practica.infrastructure.moduloProduct.productVariant.entity.ProductVariantEntity;
import com.ema.practica.infrastructure.moduloProduct.productVariant.rest.model.VarianteRequestDTO;
import com.ema.practica.infrastructure.moduloProduct.productVariant.rest.model.VarianteResponseDTO;
import com.ema.practica.infrastructure.moduloProduct.productVariant.rest.model.VarianteUpdateDTO;
import com.ema.practica.infrastructure.moduloProduct.subcategoria.crud.SubCategoriaRepository;
import com.ema.practica.infrastructure.moduloProduct.subcategoria.entity.SubCategoriaEntity;
import com.ema.practica.infrastructure.util.status.StatusEntity;
import com.ema.practica.infrastructure.detailOrder.crud.OrderDetailRepository;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

import static com.ema.practica.infrastructure.util.status.StatusEntity.ACTIVO;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
    private final VarianteAtributoRepository varianteAtributoRepository;
    private final AtributoRepository atributoRepository;
    private final SubCategoriaRepository subCategoriaRepository;
    private final ProductMapper productMapper;
    private final ImagenService imagenService;
    private final OrderDetailRepository orderDetailRepository;


    @Transactional
    public ProductResponseDTO crearProducto(ProductRequestDTO dto,List<MultipartFile> images) throws VentaInvalidaException, IOException {

        if (dto.getSku() != null && productRepository.existsBySku(dto.getSku())) {
            throw new VentaInvalidaException("Ya existe un producto con ese SKU");
        }

        SubCategoriaEntity subCategoria = null;
        if (dto.getSubCategoriaId() != null) {
            subCategoria = subCategoriaRepository.findById(dto.getSubCategoriaId())
                    .orElseThrow();
        }

        ProductEntity product = new ProductEntity();
        product.setNameProduct(dto.getNameProduct());
        product.setCodigoBarra(dto.getCodigoBarra());
        product.setSku(dto.getSku());
        product.setDescription(dto.getDescription());
        product.setMarca(dto.getMarca());
        product.setModelo(dto.getModelo());
        product.setPrecioVenta(dto.getPrecioVenta());
        product.setPrecioPro(dto.getPrecioPro());
        product.setCosto(dto.getCosto());
        product.setMargen(calcularMargen(dto.getPrecioVenta(), dto.getCosto()));
        product.setTieneVariantes(dto.getTieneVariantes());
        product.setSubCategoria(subCategoria);
        product.setStatus(ACTIVO);

        // ── BIFURCACIÓN CLAVE ──────────────────────────────────────────────
        if (!dto.getTieneVariantes()) {
            // Caso SIN variantes: se crea la variante default en el mismo request
            ProductVariantEntity defaultVar = new ProductVariantEntity();
            defaultVar.setProduct(product);
            defaultVar.setSku((dto.getSku() != null ? dto.getSku() : "PROD") + "-DEFAULT");
            defaultVar.setStock(dto.getStockInicial() != null ? dto.getStockInicial() : 0);
            defaultVar.setStockMinimo(dto.getStockMinimo() != null ? dto.getStockMinimo() : 5);
            defaultVar.setEsDefault(true);
            defaultVar.setStatus(ACTIVO);
            product.getVariants().add(defaultVar);
        }
        // Caso CON variantes: no se hace nada más acá.
        // El producto se guarda solo y las variantes llegan en requests separados
        // a agregarVariante(), una por cada talla/color.

        if (images != null && !images.isEmpty()) {

            List<ImageModel> imageList = imagenService.uploadImages(images);
            boolean primer=true;
            for (ImageModel image : imageList) {
                image.setProduct(product);
                image.setEsPrincipal(primer);

                product.getImageModelSet().add(image);
                primer=false;
            }
        }


        ProductEntity guardado = productRepository.save(product);
        return productMapper.toResponseDTO(guardado);
    }

    @Transactional
    public ProductResponseDTO obtenerProducto(UUID productId) {

        ProductEntity producto = productRepository.findById(productId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto", productId));

        return productMapper.toResponseDTO(producto);
    }
    @Transactional
    public VarianteResponseDTO agregarVariante(UUID productId, VarianteRequestDTO dto,List<MultipartFile> files) throws VentaInvalidaException, IOException {

        ProductEntity product = productRepository.findById(productId)
                .orElseThrow();

        if (!product.getTieneVariantes()) {
            throw new VentaInvalidaException("Este producto no acepta variantes adicionales");
        }

        validarCombinacionUnica(product, dto.getAtributos());

        ProductVariantEntity variante = getProductVariantEntity(dto, product);

        for (AtributoValorDTO av : dto.getAtributos()) {
            AtributoEntity atributo = atributoRepository.findById(av.getAtributoId())
                    .orElseThrow();

            VarianteAtributoEntity va =
                    new VarianteAtributoEntity(variante, atributo, av.getValor());

            variante.getAtributos().add(va);
        }
        if (files != null && !files.isEmpty()) {

            List<ImageModel> imageList = imagenService.uploadImages(files);
            boolean primer=true;
            for (ImageModel image : imageList) {
                image.setVariant(variante);
                image.setEsPrincipal(primer);
                variante.getImageModelSet().add(image);
                primer=false;
            }

        }
            ProductVariantEntity guardada = variantRepository.save(variante);

        return productMapper.toVarianteDTO(guardada);
    }

    @Transactional
    public ProductResponseDTO actualizarProducto(UUID id, ProductUpdateDTO dto) {
        ProductEntity product = productRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto", id));

        String sku = blankToNull(dto.getSku());
        String codigoBarra = blankToNull(dto.getCodigoBarra());
        if (sku != null && productRepository.existsBySkuAndIdProductNot(sku, id)) {
            throw new VentaInvalidaException("Ya existe otro producto con ese SKU");
        }
        if (codigoBarra != null && productRepository.existsByCodigoBarraAndIdProductNot(codigoBarra, id)) {
            throw new VentaInvalidaException("Ya existe otro producto con ese código de barras");
        }

        SubCategoriaEntity subCategoria = null;
        if (dto.getSubCategoriaId() != null) {
            subCategoria = subCategoriaRepository.findById(dto.getSubCategoriaId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Subcategoría", dto.getSubCategoriaId()));
        }

        product.setNameProduct(dto.getNameProduct());
        product.setSku(sku);
        product.setCodigoBarra(codigoBarra);
        product.setDescription(dto.getDescription());
        product.setMarca(dto.getMarca());
        product.setModelo(dto.getModelo());
        product.setPrecioVenta(dto.getPrecioVenta());
        product.setPrecioPro(dto.getPrecioPro());
        product.setCosto(dto.getCosto());
        product.setMargen(calcularMargen(dto.getPrecioVenta(), dto.getCosto()));
        product.setSubCategoria(subCategoria);

        // La variante default sigue el SKU del producto
        product.getVariants().stream()
                .filter(ProductVariantEntity::getEsDefault)
                .forEach(v -> v.setSku((sku != null ? sku : "PROD") + "-DEFAULT"));

        return productMapper.toResponseDTO(product);
    }

    @Transactional
    public VarianteResponseDTO actualizarVariante(Long variantId, VarianteUpdateDTO dto) {
        ProductVariantEntity variante = variantRepository.findById(variantId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Variante", variantId));

        if (variante.getEsDefault()) {
            throw new VentaInvalidaException("La variante por defecto se edita desde el producto");
        }
        String codigoBarra = blankToNull(dto.getCodigoBarra());
        if (variantRepository.existsBySkuAndVariantIdNot(dto.getSku(), variantId)) {
            throw new VentaInvalidaException("Ya existe otra variante con ese SKU");
        }
        if (codigoBarra != null && variantRepository.existsByCodigoBarraAndVariantIdNot(codigoBarra, variantId)) {
            throw new VentaInvalidaException("Ya existe otra variante con ese código de barras");
        }

        variante.setSku(dto.getSku());
        variante.setCodigoBarra(codigoBarra);
        variante.setPrecioVenta(dto.getPrecioVenta());
        variante.setPrecioPro(dto.getPrecioPro());
        variante.setCosto(dto.getCosto());
        variante.setMargen(calcularMargen(variante.getPrecioEfectivo(), dto.getCosto()));
        if (dto.getStockMinimo() != null) {
            variante.setStockMinimo(dto.getStockMinimo());
        }
        return productMapper.toVarianteDTO(variante);
    }

    // Un campo vacío del formulario se guarda como null, así no choca con los UNIQUE
    private static String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }

    private static ProductVariantEntity getProductVariantEntity(VarianteRequestDTO dto, ProductEntity product) {
        ProductVariantEntity variante = new ProductVariantEntity();
        variante.setProduct(product);
        variante.setSku(dto.getSku());
        variante.setCodigoBarra(dto.getCodigoBarra());
        variante.setPrecioVenta(dto.getPrecioVenta());
        variante.setPrecioPro(dto.getPrecioPro());
        variante.setCosto(dto.getCosto());
        variante.setStock(dto.getStock());
        variante.setStockMinimo(dto.getStockMinimo() != null ? dto.getStockMinimo() : 5);
        variante.setEsDefault(false);
        variante.setStatus(ACTIVO);
        return variante;
    }
    /**
     * Busca un producto o variante por código de barra.
     * Primero busca en variantes, si no encuentra busca en productos.
     */
    public Map<String, Object> buscarPorCodigoBarra(String codigo) {

        // 1. Buscar en variantes
        Optional<ProductVariantEntity> varianteOpt =
                variantRepository.findByCodigoBarra(codigo);

        if (varianteOpt.isPresent()) {
            ProductVariantEntity variante = varianteOpt.get();
            Map<String, Object> result = new HashMap<>();
            result.put("tipo",     "VARIANTE");
            result.put("variante", productMapper.toVarianteDTO(variante));
            result.put("producto", productMapper.toResponseDTO(variante.getProduct()));
            return result;
        }

        // 2. Buscar en productos
        Optional<ProductEntity> productoOpt =
                productRepository.findByCodigoBarra(codigo);

        if (productoOpt.isPresent()) {
            Map<String, Object> result = new HashMap<>();
            result.put("tipo",     "PRODUCTO");
            result.put("producto", productMapper.toResponseDTO(productoOpt.get()));
            return result;
        }

        // 3. No encontró nada
        return null;
    }

    /**
     * Borrado definitivo. Solo si ninguna variante figura en una venta:
     * el historial (comprobantes, devoluciones) apunta a las variantes.
     */
    @Transactional
    public void eliminarProducto(UUID id) {
        ProductEntity product = productRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto", id));
        if (orderDetailRepository.existsByVariant_Product_IdProduct(id)) {
            throw new VentaInvalidaException("\"" + product.getNameProduct()
                    + "\" tiene ventas registradas: no se puede eliminar. Desactivalo para que no se venda más.");
        }
        List<String> imagenes = new java.util.ArrayList<>();
        product.getImageModelSet().forEach(i -> imagenes.add(i.getImageId()));
        product.getVariants().forEach(v -> v.getImageModelSet().forEach(i -> imagenes.add(i.getImageId())));

        productRepository.delete(product);
        borrarImagenesAlConfirmar(imagenes);
    }

    @Transactional
    public void eliminarVariante(Long variantId) {
        ProductVariantEntity variante = variantRepository.findById(variantId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Variante", variantId));
        ProductEntity product = variante.getProduct();

        if (Boolean.TRUE.equals(variante.getEsDefault())) {
            throw new VentaInvalidaException(
                    "Es la variante única de un producto sin variantes: eliminá el producto.");
        }
        if (orderDetailRepository.existsByVariant_VariantId(variantId)) {
            throw new VentaInvalidaException("La variante " + variante.getSku()
                    + " tiene ventas registradas: no se puede eliminar. Desactivala para que no se venda más.");
        }
        long restantes = product.getVariants().stream()
                .filter(v -> !Boolean.TRUE.equals(v.getEsDefault()))
                .count();
        if (restantes <= 1) {
            throw new VentaInvalidaException(
                    "Es la única variante del producto: eliminá o desactivá el producto.");
        }

        List<String> imagenes = variante.getImageModelSet().stream()
                .map(ImageModel::getImageId).toList();
        product.getVariants().remove(variante); // orphanRemoval borra variante, atributos e imágenes
        borrarImagenesAlConfirmar(imagenes);
    }

    @Transactional
    public VarianteResponseDTO cambiarEstadoVariante(Long variantId, boolean activa) {
        ProductVariantEntity variante = variantRepository.findById(variantId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Variante", variantId));
        if (Boolean.TRUE.equals(variante.getEsDefault())) {
            throw new VentaInvalidaException(
                    "Para un producto sin variantes, activá o desactivá el producto.");
        }
        variante.setStatus(activa ? StatusEntity.ACTIVO : StatusEntity.INACTIVO);
        return productMapper.toVarianteDTO(variante);
    }

    // Cloudinary no participa de la transacción: se borra solo si la base confirmó
    private void borrarImagenesAlConfirmar(List<String> imageIds) {
        if (imageIds.isEmpty()) return;
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    imagenService.borrarDeCloudinary(imageIds);
                }
            });
        } else {
            imagenService.borrarDeCloudinary(imageIds);
        }
    }

    @Transactional
    public void desactivarProducto(UUID id) {
        ProductEntity product = productRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto", id));
        product.setStatus(StatusEntity.INACTIVO);
    }


    @Transactional
    public void reactivar(UUID id) {
        ProductEntity producto = productRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto", id));

        if (producto.getStatus() == StatusEntity.ACTIVO) {
            throw new VentaInvalidaException("El producto ya está activo");
        }

        producto.setStatus(StatusEntity.ACTIVO);
        productRepository.save(producto);
    }


    public Page<ProductListadoDTO> listar(
            String nombre, String status, int page, int size) {

        Pageable pageable = PageRequest.of(page, size);
        String nom = (nombre != null && !nombre.isBlank()) ? nombre : null;
        String sta = (status != null && !status.isBlank()) ? status : null;

        StatusEntity statusEnum = null;
        if (status != null && !status.isBlank()) {
            try {
                statusEnum = StatusEntity.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                statusEnum = null; // Si viene un valor inválido, ignoramos el filtro
            }
        }
        return productRepository
                .listarConFiltros(nom, statusEnum, pageable)
                .map(productMapper::toListadoDTO);
    }


    private Integer calcularMargen(BigDecimal precio, BigDecimal costo) {
        // Margen sobre precio de venta: se divide por precio, no por costo
        if (precio == null || costo == null || precio.signum() == 0) return null;
        return precio.subtract(costo).divide(precio, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100)).intValue();
    }

    private void validarCombinacionUnica(ProductEntity product, List<AtributoValorDTO> nuevos) throws VentaInvalidaException {
        Map<Long, String> nuevaCombo = nuevos.stream()
                .collect(Collectors.toMap(AtributoValorDTO::getAtributoId, AtributoValorDTO::getValor));
        boolean duplicada = product.getVariants().stream()
                .filter(v -> !v.getEsDefault())
                .anyMatch(v -> {
                    Map<Long, String> existente = v.getAtributos().stream()
                            .collect(Collectors.toMap(a -> a.getAtributo().getAtributoId(), VarianteAtributoEntity::getValor));
                    return existente.equals(nuevaCombo);
                });
        if (duplicada) throw new VentaInvalidaException("Ya existe una variante con esa combinación de atributos");
    }

    @Transactional
    public VarianteResponseDTO actualizarStock(Long variantId, Integer nuevoStock) throws VentaInvalidaException {
        if (nuevoStock < 0) {
            throw new VentaInvalidaException("El stock no puede ser negativo");
        }
        ProductVariantEntity variante = variantRepository.findByIdForUpdate(variantId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Variante", variantId));

        variante.setStock(nuevoStock);
        return productMapper.toVarianteDTO(variante);
    }
}


/* if (files != null && files.length > 0) {

            Set<ImageModel> images = Arrays.stream(files)
                    .filter(file -> file != null && !file.isEmpty())
                    .map(file -> {
                        try {
                            ImageModel image = imagenService.uploadImage(file); // Método para subir la imagen
                            image.setProduct(productEntity); // Asegurar la relación
                            return image;
                        } catch (IOException e) {
                            throw new RuntimeException("Error al subir imagen", e);
                        }
                    })
                    .collect(Collectors.toSet());

            productEntity.getImageModelSet().addAll(images);

        }*/