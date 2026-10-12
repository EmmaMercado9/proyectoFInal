# Facturación electrónica con ARCA

El sistema emite **Factura C** y **Nota de Crédito C** (emisor monotributista)
conectándose directo a los web services de ARCA:

- **WSAA**: autenticación con certificado digital (ticket válido 12 h).
- **WSFEv1**: pide el CAE de cada comprobante.

Se factura manualmente desde el comprobante de cada venta ("Facturar") y las
notas de crédito también son manuales ("Emitir nota de crédito").

> **Hoy el sistema factura solo en modo prueba (homologación).** Esos
> comprobantes van al servidor de pruebas de ARCA, no tienen validez fiscal
> ni se suman a tu facturación (no cuentan para la categoría del monotributo).
> Para facturar de verdad hace falta, a propósito y en el servidor,
> `ARCA_AMBIENTE=PRODUCCION` **y** `ARCA_PERMITIR_PRODUCCION=true`. Eso no se
> puede cambiar desde el panel.

---

## 1. Configurar desde el panel (ADMIN)

**Configuración → Facturación**:

1. **Datos del negocio**: CUIT, punto de venta, nombre o razón social,
   domicilio, Ingresos Brutos, inicio de actividades. Se pueden cambiar cuando
   haga falta; las facturas ya emitidas conservan los datos con que salieron.
2. **Certificado**:
   - **a)** "Generar pedido de certificado": crea la clave privada (queda en el
     servidor, en `~/.tienda-arca`, nunca se descarga) y el archivo `arca.csr`.
   - **b)** En ARCA, con tu clave fiscal:
     1. Administrador de Relaciones de Clave Fiscal → adherí
        **"WSASS – Autogestión Certificados Homologación"** (solo la primera vez).
     2. WSASS → **Nuevo certificado** → nombre (ej. `tienda`) → pegá el
        contenido de `arca.csr` → guardá el certificado como `.crt`.
     3. WSASS → **Crear autorización a servicio** → ese certificado +
        **wsfe – Facturación Electrónica**.
   - **c)** "Subir el certificado": el sistema verifica que corresponda a la
     clave generada y a tu CUIT, y que no esté vencido.
3. Tildá **"Facturación activada"** y usá **"Probar conexión"**: si está todo
   bien muestra el último número de Factura C autorizado.

En homologación el punto de venta puede ser cualquiera (por ejemplo `1`).

---

## 2. Variables del servidor (infraestructura)

| Variable | Default | Para qué |
|---|---|---|
| `ARCA_AMBIENTE` | `HOMOLOGACION` | `PRODUCCION` para facturar de verdad |
| `ARCA_PERMITIR_PRODUCCION` | `false` | seguro: sin `true`, producción queda bloqueada |
| `ARCA_DIRECTORIO` | `~/.tienda-arca` | clave privada, certificado y ticket (hacer backup, no versionar) |
| `ARCA_CERT_PATH` / `ARCA_KEY_PATH` | — | opcional: usar archivos propios en lugar de los del panel |

> Backup: si se pierde `~/.tienda-arca/arca.key` hay que pedir otro certificado.

---

## 3. Pasar a producción (más adelante, consultalo con tu contador)

1. ARCA → "Administración de puntos de venta y domicilios" → nuevo punto de
   venta **"Factura Electrónica – Monotributo – Web Services"**.
2. Pedir el certificado de producción ("Administración de Certificados
   Digitales") con un CSR nuevo y autorizarlo para "Facturación Electrónica".
3. En el servidor: `ARCA_AMBIENTE=PRODUCCION` y `ARCA_PERMITIR_PRODUCCION=true`.

---

## 4. Reglas que aplica el sistema

- Se factura una vez por venta, por lo cobrado menos lo ya devuelto.
- Consumidor final sin identificar hasta **$10.000.000** (RG 5700/2025); desde
  ese monto pide DNI o CUIT.
- Siempre se informa la **condición frente al IVA del receptor**
  (obligatoria según el manual WSFEv1 v4.8).
- CUIT/CUIL se validan con dígito verificador.
- Nota de crédito: por una devolución hecha después de facturar, por todo el
  saldo (ej. venta cancelada) o por un importe; nunca más que lo facturado.
- En homologación el comprobante dice **"COMPROBANTE DE PRUEBA · SIN VALIDEZ FISCAL"**.
- El QR sigue la especificación de ARCA (RG 4892/2020).

Errores típicos de ARCA:

- *"Certificado no emitido por AC de confianza"*: el `.crt` no es de ARCA o es de otro ambiente.
- *"Computador no autorizado a acceder al servicio"*: falta autorizar el
  certificado para `wsfe` (paso b.3). En WSASS → "Crear autorización a servicio":
  nombre simbólico del DN = el del certificado, CUIT del DN y CUIT representada =
  tu CUIT, servicio = `wsfe - Facturación Electrónica`.
