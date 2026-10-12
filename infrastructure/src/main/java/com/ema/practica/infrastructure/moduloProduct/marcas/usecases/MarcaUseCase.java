package com.ema.practica.infrastructure.moduloProduct.marcas.usecases;

import com.ema.practica.infrastructure.moduloProduct.marcas.crud.MarcaCrudRepo;
import com.ema.practica.infrastructure.moduloProduct.marcas.entity.MarcaEntity;
import com.ema.practica.infrastructure.moduloProduct.marcas.rest.model.MarcaDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class MarcaUseCase {


    private  final MarcaCrudRepo marcaCrudRepo;
    @Autowired
    public MarcaUseCase(MarcaCrudRepo marcaCrudRepo){
        this.marcaCrudRepo=marcaCrudRepo;
    }
    public boolean createMarca(MarcaDto marcaDto){
        if(!marcaCrudRepo.findByNombre(marcaDto.getNombre())){
            MarcaEntity marcaEntity=marcaDto.dtoToEntity();
            marcaCrudRepo.save(marcaEntity);
            return true;
        }
        return false;
    }

    public boolean deleteMarca(Long id){

        if(marcaCrudRepo.existsByMarcaId(id)) {
            marcaCrudRepo.deleteById(id);
            return true;
        }
        else{
            return false;
        }
    }
    public List<MarcaDto> getAll(){

        Iterable<MarcaEntity> marcaEntities=marcaCrudRepo.findAll();
        return StreamSupport.stream(marcaEntities.spliterator(),false).map(MarcaDto::entityToDto).collect(Collectors.toList());
    }

    public boolean modifyMarca(String nombre,Long id){

        if(marcaCrudRepo.existsByMarcaId(id)){
            MarcaEntity marcaEntity=marcaCrudRepo.findByMarcaId(id);
            marcaEntity.setNombre(nombre);
            marcaCrudRepo.save(marcaEntity);
            return true;
        }
        else{
            return false;
        }
    }
}
