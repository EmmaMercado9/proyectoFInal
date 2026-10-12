package com.ema.practica.infrastructure.order.mapper;

import com.ema.practica.infrastructure.detailOrder.entity.OrderDetailEntity;
import com.ema.practica.infrastructure.detailOrder.rest.model.OrderDetailResponseDTO;
import com.ema.practica.infrastructure.moduloProduct.client.entity.ClientEntity;
import com.ema.practica.infrastructure.moduloProduct.client.rest.model.ClientResumenDTO;
import com.ema.practica.infrastructure.moduloProduct.productVariant.entity.ProductVariantEntity;
import com.ema.practica.infrastructure.order.entity.OrderEntity;
import com.ema.practica.infrastructure.order.rest.model.OrderListadoDTO;
import com.ema.practica.infrastructure.order.rest.model.OrderResponseDTO;
import com.ema.practica.infrastructure.util.user.rest.model.UserResumenDTO;
import com.ema.practica.infrastructure.images.ImageModel;
import com.ema.practica.infrastructure.moduloProduct.atributos.entity.VarianteAtributoEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.Collection;
import java.util.Comparator;

import java.util.List;

// OrderMapper.java
@Component
public class OrderMapper {

    // Días que se aceptan devoluciones o cambios (app.devoluciones.plazo-dias)
    @Value("${app.devoluciones.plazo-dias:3}")
    private int plazoDevolucionDias = 3;

    // Entity → DTO completo (comprobante)
    public OrderResponseDTO toResponseDTO(OrderEntity order) {
        OrderResponseDTO dto = new OrderResponseDTO();
        dto.setIdOrder(order.getIdOrder());
        dto.setTrackingId(order.getTrackingId());
        dto.setOrderDate(order.getOrderDate());
        dto.setCanal(order.getCanal().name());
        dto.setPayment(order.getPayment().name());
        dto.setStatus(order.getStatus().name());
        dto.setSubtotal(order.getSubtotal());
        dto.setDiscount(order.getDiscount());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setAddress(order.getAddress());
        dto.setDescription(order.getDescription());
        dto.setMotivoCancelacion(order.getMotivoCancelacion());
        dto.setFechaCancelacion(order.getFechaCancelacion());
        dto.setCanceladoPor(order.getCanceladoPor());
        if (order.getOrderDate() != null) {
            dto.setFechaLimiteDevolucion(order.getOrderDate().plusDays(plazoDevolucionDias));
        }

        // Usuario — solo los datos necesarios, sin contraseña
        if (order.getUser() != null) {
            UserResumenDTO user = new UserResumenDTO();
            user.setId(order.getUser().getIdUser());
            user.setNombre(order.getUser().getName());
            user.setEmail(order.getUser().getMail());
            dto.setUser(user);
        }
        if (order.getClientEntity() != null) {
            ClientEntity c = order.getClientEntity();
            ClientResumenDTO clienteDTO = new ClientResumenDTO();
            clienteDTO.setIdClient(c.getIdClient());
            clienteDTO.setName(c.getName());
            clienteDTO.setMail(c.getMail());
            clienteDTO.setDni(c.getDni());
            dto.setCliente(clienteDTO);
        }

        // Detalles con datos de variante y producto
        List<OrderDetailResponseDTO> details = order.getDetails().stream()
                .map(this::toDetailDTO)
                .toList();
        dto.setDetails(details);

        return dto;
    }

    private OrderDetailResponseDTO toDetailDTO(OrderDetailEntity detail) {
        OrderDetailResponseDTO dto = new OrderDetailResponseDTO();
        dto.setIdDetail(detail.getIdDetail());
        dto.setQuantity(detail.getQuantity());
        dto.setCantidadDevuelta(detail.getCantidadDevueltaSegura());
        dto.setPriceUnit(detail.getPriceUnit());
        dto.setSubtotal(detail.getSubtotal());

        // Datos de la variante — navega la relación
        ProductVariantEntity variant = detail.getVariant();
        dto.setVariantId(variant.getVariantId());
        dto.setSku(variant.getSku());
        dto.setProductoNombre(variant.getProduct().getNameProduct());
        dto.setTalla(valorAtributo(variant, "tall"));   // "Talla" o "Talle"
        dto.setColor(valorAtributo(variant, "color"));
        String img = primeraImagen(variant.getImageModelSet());
        dto.setImagenUrl(img != null ? img : primeraImagen(variant.getProduct().getImageModelSet()));
        return dto;
    }

    private String valorAtributo(ProductVariantEntity variant, String prefijo) {
        if (variant.getAtributos() == null) return null;
        return variant.getAtributos().stream()
                .filter(a -> a.getAtributo() != null
                        && a.getAtributo().getNombre().toLowerCase().startsWith(prefijo))
                .map(VarianteAtributoEntity::getValor)
                .findFirst()
                .orElse(null);
    }

    // Principal primero; si no hay, cualquiera
    private String primeraImagen(Collection<ImageModel> imagenes) {
        if (imagenes == null || imagenes.isEmpty()) return null;
        return imagenes.stream()
                .sorted(Comparator.comparing(
                        (ImageModel i) -> !Boolean.TRUE.equals(i.getEsPrincipal())))
                .map(ImageModel::getUrl)
                .findFirst()
                .orElse(null);
    }

    // Entity → DTO listado (liviano)
    public OrderListadoDTO toListadoDTO(OrderEntity order) {
        OrderListadoDTO dto = new OrderListadoDTO();
        dto.setIdOrder(order.getIdOrder());
        dto.setTrackingId(order.getTrackingId());
        dto.setOrderDate(order.getOrderDate());
        dto.setCanal(order.getCanal().name());
        dto.setPayment(order.getPayment().name());
        dto.setStatus(order.getStatus().name());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setCantidadItems(order.getDetails().size());
        if (order.getClientEntity() != null) {
            ClientEntity c = order.getClientEntity();
            ClientResumenDTO clienteDTO = new ClientResumenDTO();
            clienteDTO.setIdClient(c.getIdClient());
            clienteDTO.setName(c.getName());
            clienteDTO.setMail(c.getMail());
            clienteDTO.setDni(c.getDni());
            dto.setCliente(clienteDTO);
        }
        return dto;
    }
}