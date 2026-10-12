# Facturación electrónica con ARCA

El sistema emite **Factura C** y **Nota de Crédito C** (emisor monotributista)
conectándose directo a los web services de ARCA:

- **WSAA**: autenticación con certificado digital (ticket válido 12 h, se guarda en `~/.tienda-arca`).
- **WSFEv1**: pide el CAE de cada comprobante.

Se factura manualmente desde el comprobante de cada venta ("Facturar") y las
notas de crédito también son manuales ("Emitir nota de crédito").

> **Hoy el sistema factura solo en modo prueba (homologación).** Esos
> comprobantes van al servidor de pruebas de ARCA, no tienen validez fiscal
> ni se suman a tu facturación (no cuentan para la categoría del monotributo).
> Para facturar de verdad hace falta, a propósito, `ARCA_AMBIENTE=PRODUCCION`
> **y** `ARCA_PERMITIR_PRODUCCION=true`. Si falta cualquiera de las dos, no se
> contacta a los servidores reales.

---

## 1. Qué tenés que hacer en ARCA (con tu clave fiscal nivel 3)

Primero en **homologación** (pruebas, sin validez fiscal), después en **producción**.

### 1.1 Generar la clave privada y el pedido de certificado (CSR)

En la PC (Git Bash trae `openssl`). Reemplazá `TU_CUIT` y `TU NOMBRE`:

```bash
mkdir -p ~/arca && cd ~/arca
openssl genrsa -out arca.key 2048
MSYS_NO_PATHCONV=1 openssl req -new -key arca.key -out arca.csr \
  -subj "/C=AR/O=TU NOMBRE/CN=tienda/serialNumber=CUIT TU_CUIT"
```

> `arca.key` es **secreta**: no la subas al repo ni la compartas. Si se pierde,
> hay que generar otro certificado.

### 1.2 Certificado de homologación

1. Ingresá a ARCA y adherí el servicio **"WSASS – Autogestión Certificados Homologación"**
   (Administrador de Relaciones de Clave Fiscal → Adherir servicio).
2. En WSASS: **Nuevo certificado** → pegá el contenido de `arca.csr` → guardá el
   certificado que devuelve como `~/arca/arca-homo.crt`.
3. En WSASS: **Crear autorización a servicio** → elegí tu certificado y el servicio
   **`wsfe` (Facturación Electrónica)**.

En homologación el punto de venta puede ser cualquiera (por ejemplo `1`).

### 1.3 Producción (cuando todo funcione en pruebas)

1. **Punto de venta**: ARCA → "Administración de puntos de venta y domicilios" →
   agregar uno con sistema **"Factura Electrónica – Monotributo – Web Services"**.
   No uses el mismo número que "Comprobantes en línea".
2. **Certificado**: ARCA → "Administración de Certificados Digitales" → agregar alias →
   subí el CSR (podés generar uno nuevo para producción) → descargá el `.crt`.
3. **Autorizar**: "Administrador de Relaciones de Clave Fiscal" → Nueva relación →
   servicio **"Facturación Electrónica"** → representante: el certificado (alias).

---

## 2. Configurar el backend

Variables de entorno (o en `application-local.properties`, que no se sube a git):

| Variable | Ejemplo | Obligatoria |
|---|---|---|
| `ARCA_ENABLED` | `true` | sí |
| `ARCA_AMBIENTE` | `HOMOLOGACION` o `PRODUCCION` | (default homologación) |
| `ARCA_PERMITIR_PRODUCCION` | `false` | solo `true` para facturar de verdad |
| `ARCA_CUIT` | `20123456789` | sí |
| `ARCA_PUNTO_VENTA` | `1` | sí |
| `ARCA_CERT_PATH` | `C:/Users/vos/arca/arca-homo.crt` | sí |
| `ARCA_KEY_PATH` | `C:/Users/vos/arca/arca.key` | sí |
| `ARCA_RAZON_SOCIAL` | `Juan Pérez` | sí |
| `ARCA_DOMICILIO` | `Av. Siempreviva 742, CABA` | se imprime |
| `ARCA_IIBB` | `20123456789` | se imprime |
| `ARCA_INICIO_ACTIVIDADES` | `01/03/2020` | se imprime |

Reiniciá el backend. En el comprobante de una venta, como ADMIN, usá
**"Probar conexión con ARCA"**: si el certificado está bien muestra el último
número de Factura C autorizado.

También podés probar solo el certificado, sin levantar la app:

```bash
./mvnw -o test -Dtest=WsaaSondaManualTest -Dsurefire.failIfNoSpecifiedTests=false \
  -Dsonda.cert=RUTA/arca-homo.crt -Dsonda.key=RUTA/arca.key
```

Errores típicos de ARCA:

- *"Certificado no emitido por AC de confianza"*: el `.crt` no es de ARCA o es de otro ambiente.
- *"No se encuentra autorizado a operar"* / *"no posee autorización"*: falta autorizar el certificado para `wsfe`.
- *"El CEE ya posee un TA valido"*: se pidió otro ticket con uno vigente; borrá `~/.tienda-arca` solo si cambiaste de certificado.

---

## 3. Reglas que aplica el sistema

- Se factura una vez por venta, por lo cobrado menos lo ya devuelto.
- Consumidor final sin identificar hasta **$10.000.000** (RG 5700/2025); desde ese
  monto pide DNI o CUIT.
- Siempre se informa la **condición frente al IVA del receptor**
  (obligatoria según el manual WSFEv1 v4.8).
- CUIT/CUIL se validan con dígito verificador.
- Nota de crédito: por una devolución hecha después de facturar, por todo el
  saldo (ej. venta cancelada) o por un importe; nunca más que lo facturado.
- En homologación el comprobante dice **"COMPROBANTE DE PRUEBA · SIN VALIDEZ FISCAL"**.
- El QR sigue la especificación de ARCA (RG 4892/2020).
