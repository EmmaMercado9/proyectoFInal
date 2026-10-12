package com.ema.practica.infrastructure.moduloProduct.product.rest.api;

import com.ema.practica.infrastructure.exception.RecursoNoEncontradoException;
import com.ema.practica.infrastructure.exception.VentaInvalidaException;
import com.ema.practica.infrastructure.images.ImageModel;
import com.ema.practica.infrastructure.images.ImageVariantDto;
import com.ema.practica.infrastructure.images.ImagenModelDto;
import com.ema.practica.infrastructure.images.services.ImagenService;
import com.ema.practica.infrastructure.moduloProduct.product.rest.model.ProductListadoDTO;
import com.ema.practica.infrastructure.moduloProduct.product.rest.model.ProductRequestDTO;
import com.ema.practica.infrastructure.moduloProduct.product.rest.model.ProductResponseDTO;
import com.ema.practica.infrastructure.moduloProduct.product.rest.model.ProductUpdateDTO;
import com.ema.practica.infrastructure.moduloProduct.product.useCases.ProductService;
import com.ema.practica.infrastructure.moduloProduct.productVariant.crud.ProductVariantRepository;
import com.ema.practica.infrastructure.moduloProduct.productVariant.entity.ProductVariantEntity;
import com.ema.practica.infrastructure.moduloProduct.productVariant.rest.model.VarianteRequestDTO;
import com.ema.practica.infrastructure.moduloProduct.productVariant.rest.model.VarianteResponseDTO;
import com.ema.practica.infrastructure.moduloProduct.productVariant.rest.model.VarianteUpdateDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final ImagenService  imagenService;
    private final ProductVariantRepository variantRepository;

    // POST /api/productos
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductResponseDTO> crear(
            @RequestPart("datos") @Valid ProductRequestDTO dto,
            @RequestPart(value = "files", required = false) List<MultipartFile> images)
            throws IOException, VentaInvalidaException {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productService.crearProducto(dto, images));
    }


    @GetMapping("/variantes/barcode/{codigo}")
    public ResponseEntity<?> buscarPorCodigoBarra(
            @PathVariable String codigo) {

        Map<String, Object> resultado = productService.buscarPorCodigoBarra(codigo);

        if (resultado == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(resultado);
    }
    // POST /api/productos/{productId}/variantes
    @PostMapping("/{productId}/variantes")
    public ResponseEntity<VarianteResponseDTO> agregarVariante(
            @PathVariable UUID productId,
            @RequestPart(value = "datos") @Valid VarianteRequestDTO datos,
            @RequestPart(value = "files", required = false) List<MultipartFile> files)
            throws VentaInvalidaException, IOException {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productService.agregarVariante(productId, datos, files));
    }

    // GET /api/productos — listado paginado
    @GetMapping
    public ResponseEntity<Page<ProductListadoDTO>> listar(
            @RequestParam(required = false)    String nombre,
            @RequestParam(required = false)    String status,
            @RequestParam(defaultValue = "0")  int    page,
            @RequestParam(defaultValue = "12") int    size) {
        return ResponseEntity.ok(productService.listar(nombre, status, page, size));
    }

    // GET /api/productos/{productId} — detalle completo
    @GetMapping("/{productId}")
    public ResponseEntity<ProductResponseDTO> getById(@PathVariable UUID productId) {
        return ResponseEntity.ok(productService.obtenerProducto(productId));
    }

    // PUT /api/productos/{productId} — editar datos y precios
    @PutMapping("/{productId}")
    public ResponseEntity<ProductResponseDTO> actualizar(
            @PathVariable UUID productId, @RequestBody @Valid ProductUpdateDTO dto) {
        return ResponseEntity.ok(productService.actualizarProducto(productId, dto));
    }

    // PUT /api/productos/variantes/{variantId} — editar SKU, precios, stock mínimo
    @PutMapping("/variantes/{variantId}")
    public ResponseEntity<VarianteResponseDTO> actualizarVariante(
            @PathVariable Long variantId, @RequestBody @Valid VarianteUpdateDTO dto) {
        return ResponseEntity.ok(productService.actualizarVariante(variantId, dto));
    }

    // PATCH /api/productos/variantes/{variantId}/stock
    @PatchMapping("/variantes/{variantId}/stock")
    public ResponseEntity<VarianteResponseDTO> actualizarStock(
            @PathVariable Long variantId,
            @RequestParam Integer cantidad) throws VentaInvalidaException {
        return ResponseEntity.ok(productService.actualizarStock(variantId, cantidad));
    }

    // DELETE /api/productos/{id} — solo si nunca se vendió (si no, 400)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable UUID id) {
        productService.eliminarProducto(id);
        return ResponseEntity.noContent().build();
    }

    // DELETE /api/productos/variantes/{variantId} — solo si nunca se vendió
    @DeleteMapping("/variantes/{variantId}")
    public ResponseEntity<Void> eliminarVariante(@PathVariable Long variantId) {
        productService.eliminarVariante(variantId);
        return ResponseEntity.noContent().build();
    }

    // PATCH /api/productos/variantes/{variantId}/desactivar | /reactivar
    @PatchMapping("/variantes/{variantId}/desactivar")
    public ResponseEntity<VarianteResponseDTO> desactivarVariante(@PathVariable Long variantId) {
        return ResponseEntity.ok(productService.cambiarEstadoVariante(variantId, false));
    }

    @PatchMapping("/variantes/{variantId}/reactivar")
    public ResponseEntity<VarianteResponseDTO> reactivarVariante(@PathVariable Long variantId) {
        return ResponseEntity.ok(productService.cambiarEstadoVariante(variantId, true));
    }

    // PATCH /api/productos/{id}/desactivar
    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<Void> desactivar(@PathVariable UUID id) {
        productService.desactivarProducto(id);
        return ResponseEntity.noContent().build();
    }

    // PATCH /api/productos/{id}/reactivar
    @PatchMapping("/{id}/reactivar")
    public ResponseEntity<Void> reactivar(@PathVariable UUID id) {
        productService.reactivar(id);
        return ResponseEntity.noContent().build();
    }

    // POST /api/productos/variantes/{variantId}/imagenes
    @PostMapping(value = "/variantes/{variantId}/imagenes",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<ImageVariantDto>> subirImagenesVariante(
            @PathVariable Long variantId,
            @RequestParam("files") List<MultipartFile> files) throws IOException {

        ProductVariantEntity variante = variantRepository.findById(variantId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Variante", variantId));

        List<ImageModel> imagenes = imagenService.uploadImagesVariante(files, variante);

        List<ImageVariantDto> response = imagenes.stream()
                .map(img -> new ImageVariantDto(
                        img.getImageId(),
                        img.getName(),
                        img.getUrl(),
                        img.getEsPrincipal()
                ))
                .toList();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}

/*   public ResponseEntity<?> addProduct(@RequestPart("product") ProductDTO productDTO,
                                        @RequestPart("imageFile")MultipartFile[] file) {
        System.out.println("ProductDTO: " + productDTO);

        System.out.println("Files received: " + file.length);
        try {

            System.out.println("imagene en el dto despues "+productDTO.getImageMSet());
            ProductDTO productDTO1=addProductUseCaseImp.createProduct(productDTO,file);
            System.out.println("imagene en el dto despues "+productDTO.getImageMSet());

            return ResponseEntity.status(HttpStatus.CREATED).body(productDTO1);
        }catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

    }
*/
