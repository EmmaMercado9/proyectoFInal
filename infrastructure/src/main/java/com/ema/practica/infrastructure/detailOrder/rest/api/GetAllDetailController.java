package com.ema.practica.infrastructure.detailOrder.rest.api;

import com.ema.practica.core.detailOrder.input.IConsultDetailOrderInput;
import com.ema.practica.core.detailOrder.model.DetailOrder;
import com.ema.practica.infrastructure.detailOrder.rest.model.DetailOrderDTO;
import com.ema.practica.infrastructure.detailOrder.useCases.ConsultDetailUseCaseImp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/getAllDetailsOrder")
@CrossOrigin("*")

public class GetAllDetailController {
    IConsultDetailOrderInput iConsultDetailOrderInput;
    private  final ConsultDetailUseCaseImp consultDetailUseCaseImp;
    @Autowired
    public GetAllDetailController(IConsultDetailOrderInput iConsultDetailOrderInput, ConsultDetailUseCaseImp consultDetailUseCaseImp){
        this.iConsultDetailOrderInput=iConsultDetailOrderInput;
        this.consultDetailUseCaseImp = consultDetailUseCaseImp;
    }

    @GetMapping()

    public ResponseEntity<List<DetailOrderDTO>> GetAllUsers(){
        Collection<DetailOrder> list=iConsultDetailOrderInput.getDetailOrder();

        List<DetailOrderDTO> list1=list.stream().map(DetailOrderDTO::coreToDTO).toList();

        if (!list1.isEmpty()) {
            return ResponseEntity.ok(list1);
        } else {
            return ResponseEntity.noContent().build();
        }
    }

}
