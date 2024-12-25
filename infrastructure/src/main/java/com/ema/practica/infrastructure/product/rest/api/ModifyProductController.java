package com.ema.practica.infrastructure.product.rest.api;
import com.ema.practica.core.product.input.IModifyProductInput;
import com.ema.practica.core.product.model.Product;
import com.ema.practica.infrastructure.product.rest.model.ProductDTO;
import com.ema.practica.infrastructure.product.useCases.ListProductUseCaseImp;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/modifyProduct")
@CrossOrigin("*")
public class ModifyProductController {
    private  final IModifyProductInput iModifyProductInput;
    private final ListProductUseCaseImp listProductUseCaseImp;

    public ModifyProductController(IModifyProductInput iModifyProductInput, ListProductUseCaseImp listProductUseCaseImp){
        this.iModifyProductInput=iModifyProductInput;

        this.listProductUseCaseImp = listProductUseCaseImp;
    }

    @PutMapping
    public ResponseEntity<?> modifyUser(@RequestBody ProductDTO productDTO) throws Exception {
        Product product=productDTO.dtoToCore();
        boolean result = iModifyProductInput.modifyProduct(
                product.getCategory(),
                product.getNameProduct(),
                product.getPriceUnitary(),
                product.getDiscontuedPrice(),
                product.getDescriptionProduct(),
                product.isStatus(),
                product.getColor(),
                product.getSizes(),
                product.getImageMSet(),
                product.getIdProduct());
        return ResponseEntity.ok().body(result);
    }

}
