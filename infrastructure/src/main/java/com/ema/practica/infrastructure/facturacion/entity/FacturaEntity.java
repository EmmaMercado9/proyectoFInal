package com.ema.practica.infrastructure.facturacion.entity;

import com.ema.practica.infrastructure.devolucion.entity.DevolucionEntity;
import com.ema.practica.infrastructure.order.entity.OrderEntity;
import com.ema.practica.infrastructure.util.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Comprobante electrónico autorizado por ARCA (tiene CAE). Solo se guardan
 * los aprobados: un rechazo se informa al usuario y no deja registro.
 */
@Entity
@Table(name = "facturas", uniqueConstraints = @UniqueConstraint(
        name = "uk_factura_numero", columnNames = {"tipo", "punto_venta", "numero", "ambiente"}))
@Getter
@Setter
@NoArgsConstructor
public class FacturaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_factura")
    private Long idFactura;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_order", nullable = false)
    private OrderEntity order;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "tipo", nullable = false, length = 20)
    private TipoComprobante tipo;

    @Column(name = "punto_venta", nullable = false)
    private Integer puntoVenta;

    @Column(name = "numero", nullable = false)
    private Long numero;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Column(name = "importe_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal importeTotal;

    // Receptor: 80 CUIT, 86 CUIL, 96 DNI, 99 consumidor final sin identificar
    @Column(name = "doc_tipo", nullable = false)
    private Integer docTipo;

    @Column(name = "doc_nro", nullable = false)
    private Long docNro;

    @Column(name = "condicion_iva_receptor", nullable = false)
    private Integer condicionIvaReceptorId;

    @Column(name = "cae", nullable = false, length = 20)
    private String cae;

    @Column(name = "cae_vencimiento", nullable = false)
    private LocalDate caeVencimiento;

    // HOMOLOGACION (prueba, sin validez fiscal) o PRODUCCION
    @Column(name = "ambiente", nullable = false, length = 15)
    private String ambiente;

    // Notas de crédito: factura que anulan y, si corresponde, la devolución que la origina
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_factura_asociada")
    private FacturaEntity facturaAsociada;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_devolucion")
    private DevolucionEntity devolucion;

    @Column(name = "motivo")
    private String motivo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_user")
    private UserEntity usuario;

    // Copia de los datos del emisor al momento de emitir: si después cambian
    // (domicilio, razón social...) la factura se sigue imprimiendo como salió
    @Column(name = "emisor_cuit")
    private Long emisorCuit;

    @Column(name = "emisor_razon_social")
    private String emisorRazonSocial;

    @Column(name = "emisor_domicilio")
    private String emisorDomicilio;

    @Column(name = "emisor_iibb", length = 30)
    private String emisorIibb;

    @Column(name = "emisor_inicio_actividades", length = 10)
    private String emisorInicioActividades;

    @Column(name = "emisor_condicion_iva", length = 60)
    private String emisorCondicionIva;

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn = LocalDateTime.now();

    public String getNumeroFormateado() {
        return String.format("%05d-%08d", puntoVenta, numero);
    }
}
