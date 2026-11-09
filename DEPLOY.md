# Publicar el sistema

Backend y MySQL en Railway; frontend en Vercel.

## Railway

1. Crear un proyecto desde el repositorio `proyectoFInal` (usa el `Dockerfile`).
2. Agregar una base MySQL al proyecto.
3. Agregar un volumen al backend, montado en `/data`.
4. Cargar las variables del backend:

| Variable | Valor |
|---|---|
| `DB_URL` | `jdbc:mysql://${{MySQL.MYSQLHOST}}:${{MySQL.MYSQLPORT}}/${{MySQL.MYSQLDATABASE}}` |
| `DB_USERNAME` | `${{MySQL.MYSQLUSER}}` |
| `DB_PASSWORD` | `${{MySQL.MYSQLPASSWORD}}` |
| `JWT_SECRET` | Texto aleatorio de al menos 64 caracteres |
| `ADMIN_PASSWORD` | Solo si la base arranca vacía |
| `CLOUDINARY_CLOUD_NAME` | Datos de la cuenta de Cloudinary |
| `CLOUDINARY_API_KEY` | Clave nueva de Cloudinary |
| `CLOUDINARY_API_SECRET` | Secreto nuevo de Cloudinary |
| `ARCA_DIRECTORIO` | `/data/arca` |
| `CORS_ORIGINS` | La dirección que da Vercel, sin barra final |

5. En Networking, generar el dominio público. No activar `ARCA_PERMITIR_PRODUCCION`.

## Vercel

1. Importar el repositorio `proyectoFInalFronted` (toma la configuración de `vercel.json`).
2. Cargar `API_URL` con la dirección pública del backend, sin barra final.
3. Publicar y copiar la dirección en `CORS_ORIGINS` de Railway.

## Datos de demostración

Importar la copia limpia antes del primer inicio de sesión:

```bash
mysql -h HOST -P PUERTO -u USUARIO -p BASE < backups/demo-deploy.sql
```

Host, puerto, usuario y contraseña públicos están en la pestaña Connect de MySQL en Railway.
