package com.ema.practica.infrastructure.devolucion.mapper;

import com.ema.practica.infrastructure.devolucion.entity.DevolucionDetalleEntity;
import com.ema.practica.infrastructure.devolucion.entity.DevolucionEntity;
import com.ema.practica.infrastructure.devolucion.rest.model.DevolucionItemResponseDTO;
import com.ema.practica.infrastructure.devolucion.rest.model.DevolucionResponseDTO;
import com.ema.practica.infrastructure.moduloProduct.client.entity.ClientEntity;
import com.ema.practica.infrastructure.moduloProduct.client.rest.model.ClientResumenDTO;
import com.ema.practica.infrastructure.moduloProduct.productVariant.entity.ProductVariantEntity;
import com.ema.practica.infrastructure.order.entity.OrderEntity;
import org.springframework.stereotype.Component;

@Component
public class DevolucionMapper {

    public DevolucionResponseDTO toDTO(DevolucionEntity d) {
        DevolucionResponseDTO dto = new DevolucionResponseDTO();
        dto.setIdDevolucion(d.getIdDevolucion());
        dto.setTipo(d.getTipo().name());
        dto.setFecha(d.getFecha());
        dto.setUsuario(d.getUsuario() != null ? d.getUsuario().getName() : null);
        dto.setMotivo(d.getMotivo());
        dto.setFormaReintegro(d.getFormaReintegro() != null ? d.getFormaReintegro().name() : null);
        dto.setMontoDevuelto(d.getMontoDevuelto());
        dto.setMontoNuevaVenta(d.getMontoNuevaVenta());
        dto.setDiferencia(d.getDiferencia());

        OrderEntity order = d.getOrder();
        dto.setIdOrder(order.getIdOrder());
        dto.setTrackingIdOrden(order.getTrackingId());
        dto.setPaymentOrden(order.getPayment() != null ? order.getPayment().name() : null);
        if (order.getClientEntity() != null) {
            ClientEntity c = order.getClientEntity();
            dto.setCliente(new ClientResumenDTO(c.getIdClient(), c.getName(), c.getMail(), c.getDni()));
        }

        if (d.getOrdenCambio() != null) {
            dto.setIdOrdenCambio(d.getOrdenCambio().getIdOrder());
            dto.setTrackingIdOrdenCambio(d.getOrdenCambio().getTrackingId());
        }

        dto.setItems(d.getItems().stream().map(this::toItemDTO).toList());
        return dto;
    }

    private DevolucionItemResponseDTO toItemDTO(DevolucionDetalleEntity item) {
        DevolucionItemResponseDTO dto = new DevolucionItemResponseDTO();
        ProductVariantEntity v = item.getOrderDetail().getVariant();
        dto.setIdDetail(item.getOrderDetail().getIdDetail());
        dto.setVariantId(v.getVariantId());
        dto.setSku(v.getSku());
        dto.setProductoNombre(v.getProduct().getNameProduct());
        dto.setVariante(v.getDescripcionAtributos());
        dto.setQuantity(item.getQuantity());
        dto.setReingresaStock(item.getReingresaStock());
        dto.setMonto(item.getMonto());
        return dto;
    }
}
