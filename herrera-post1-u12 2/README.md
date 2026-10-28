# Post-contenido — Unidad 12: Despliegue y CI/CD

## Descripción
Repositorio del laboratorio de la Unidad 12 de Programación Web — Séptimo Semestre.
Contiene una API REST Spring Boot (Categoría / Producto) dockerizada con un Dockerfile
multi-stage y perfiles dev/prod (Parte 1), y su extensión con un pipeline de integración
continua con GitHub Actions y monitoreo con Spring Boot Actuator (Parte 2).


## Parte 1 — Dockerización con Perfiles y Variables de Entorno
El Dockerfile multi-stage compila el proyecto con Maven en una etapa JDK y ejecuta el JAR
resultante en una etapa JRE Alpine con un usuario no root. La configuración sensible se
externaliza en `application.properties` con placeholders `${VAR:default}`: sin variables de
entorno la aplicación arranca con el perfil `dev` (H2 en memoria); con
`SPRING_PROFILES_ACTIVE=prod` y las variables `DATASOURCE_URL`, `DATASOURCE_USER`,
`DATASOURCE_PASSWORD` (y `JWT_SECRET` si aplica) arranca con la configuración de producción
(PostgreSQL).

### Variables de entorno
| Variable | Perfil | Valor por defecto | Descripción |
|---|---|---|---|
| `SPRING_PROFILES_ACTIVE` | ambos | `dev` | Perfil activo (`dev` / `prod`) |
| `PORT` | ambos | `8080` | Puerto HTTP (Railway/Render lo inyectan) |
| `DATASOURCE_URL` | ambos | H2 en memoria (dev) / `jdbc:postgresql://localhost:5432/appdb` (prod) | URL JDBC |
| `DATASOURCE_USER` | ambos | `sa` (dev) / `appuser` (prod) | Usuario de BD |
| `DATASOURCE_PASSWORD` | ambos | vacío | Contraseña de BD |
| `DDL_AUTO` | prod | `validate` | Usar `update` solo en el primer despliegue sobre BD vacía |
| `JWT_SECRET` | ambos | clave de desarrollo (dev) / vacío (prod) | Solo si el proyecto usa JWT |

## Parte 2 — Pipeline CI/CD con GitHub Actions y Monitoreo con Actuator
El pipeline (`.github/workflows/ci.yml`) se activa en cada push y pull request a `main`:

1. **Job `build-and-test`**: compila con Maven, ejecuta las pruebas (`mvn clean verify`) y
   publica el reporte de Surefire como artefacto. Usa caché de Maven.
2. **Job `docker-build`** (depende del anterior con `needs:`): construye la imagen con el
   mismo Dockerfile, levanta el contenedor dentro del runner y hace un *smoke test* que
   consulta `/actuator/health` hasta obtener `UP`. Siempre elimina el contenedor al final
   (`if: always()`). No publica la imagen en ningún registro externo.


## Decisiones de diseño

### Parte 1
1. **Placeholders con valor por defecto (`${VAR:default}`) en vez de estrictos**: el mismo
   artefacto arranca sin fricción en local y en CI (H2 y clave de desarrollo), y en producción
   cada valor se sobrescribe con variables reales. No se aplica a credenciales de producción:
   allí la contraseña y el secreto JWT quedan vacíos.
2. **`application-dev.properties` y `application-prod.properties` separados** en vez de un
   solo archivo con bloques condicionales: hace explícito qué configuración corresponde a cada
   ambiente y reduce el riesgo de editar producción por error.
3. **Imagen final `eclipse-temurin` JRE Alpine en vez de distroless**: reduce el tamaño sin
   perder la posibilidad de depurar con `docker exec`. La etapa de compilación usa la imagen
   oficial `maven:3.9-eclipse-temurin-21-alpine` porque las imágenes Temurin no incluyen Maven.

### Parte 2
1. **Prueba mínima de contexto (más pruebas de la API) en vez de omitir las pruebas**: garantiza
   que el job de CI valide algo real antes de construir la imagen.
2. **`docker-build` construye y verifica, pero no publica la imagen**: evita exigir cuentas
   externas y Secrets solo para aprobar el laboratorio; el build real lo hace Railway/Render
   desde el mismo Dockerfile.
3. **Exposición de Actuator diferenciada por perfil** (todo en dev; solo `health` e `info` en
   prod): evita exponer información sensible (beans, variables de entorno) públicamente.
4. **Smoke test con hasta 30 intentos (60 s)**: Spring Boot tarda en arrancar en los runners
   compartidos; un margen mayor evita falsos negativos.

## Cómo compilar y ejecutar
1. Clonar: `git clone https://github.com/luisHerrera05/herrera-post1-u12.git && cd herrera-post1-u12`
2. Construir la imagen: `docker build -t mi-spring-app:local .`
3. Ejecutar con valores por defecto (perfil dev): `docker run -p 8080:8080 mi-spring-app:local`
4. Ejecutar con perfil prod (requiere PostgreSQL accesible):
   ```bash
   docker run -p 8080:8080 \
     -e SPRING_PROFILES_ACTIVE=prod \
     -e DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/appdb \
     -e DATASOURCE_USER=appuser \
     -e DATASOURCE_PASSWORD=apppass \
     -e DDL_AUTO=update \
     -e JWT_SECRET=una-clave-larga-solo-para-produccion \
     mi-spring-app:local
   ```
5. Verificar: `curl http://localhost:8080/actuator/health` → `{"status":"UP"}`
6. Endpoints: `GET/POST /api/categorias`, `GET/POST/PUT/DELETE /api/productos[/{id}]`
7. Pipeline: pestaña **Actions** del repositorio.

