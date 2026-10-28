# Post-contenido — Unidad 12: Despliegue y CI/CD

## Descripción
Repositorio del laboratorio de la Unidad 12 de Programación Web — Séptimo Semestre.
Contiene la aplicación Spring Boot (CRUD de Categoría y Producto) dockerizada con un
Dockerfile multi-stage y perfiles dev/prod (Parte 1), y su extensión con un pipeline de
integración continua con GitHub Actions y monitoreo con Spring Boot Actuator (Parte 2).

![CI Status](https://github.com/USUARIO/apellido-post1-u12/actions/workflows/ci.yml/badge.svg)

## Estructura del proyecto
```
apellido-post1-u12/
├── .github/workflows/ci.yml      # Pipeline CI (Parte 2)
├── src/main/java/...             # Código: model, repository, controller
├── src/main/resources/
│   ├── application.properties        # Base: puerto y perfil activo
│   ├── application-dev.properties    # Perfil desarrollo (H2)
│   └── application-prod.properties   # Perfil producción (PostgreSQL)
├── src/test/java/...             # Pruebas (contexto + controlador/actuator)
├── Dockerfile                    # Multi-stage (Maven -> JRE Alpine)
├── .dockerignore
├── capturas/                     # Evidencias
└── pom.xml
```

## Parte 1 — Dockerización con Perfiles y Variables de Entorno
El Dockerfile multi-stage compila el proyecto con Maven en una etapa JDK y ejecuta el JAR
resultante en una etapa JRE Alpine con un usuario no root. La configuración sensible se
externaliza en los archivos `.properties` con placeholders `${VAR:default}`: sin variables
de entorno, la aplicación arranca con el perfil `dev` (H2 en memoria); con
`SPRING_PROFILES_ACTIVE=prod` y las variables `DATASOURCE_URL`, `DATASOURCE_USER` y
`DATASOURCE_PASSWORD`, arranca con la configuración de producción (PostgreSQL).

### Variables de entorno
| Variable | Uso | Valor por defecto |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | Perfil activo | `dev` |
| `PORT` | Puerto del servidor | `8080` |
| `DATASOURCE_URL` | URL JDBC | H2 en memoria (dev) / PostgreSQL local (prod) |
| `DATASOURCE_USER` | Usuario de BD | `sa` (dev) / `appuser` (prod) |
| `DATASOURCE_PASSWORD` | Contraseña de BD | vacío |
| `DDL_AUTO` | Estrategia de esquema en prod | `validate` |

## Parte 2 — Pipeline CI/CD con GitHub Actions y Monitoreo con Actuator
El pipeline se activa en cada push o pull request a `main` y tiene dos jobs encadenados con `needs:`:

1. **build-and-test**: configura JDK 21 con caché de Maven, ejecuta `mvn clean verify`
   (compila y corre las pruebas) y publica el reporte de Surefire como artefacto.
2. **docker-build**: construye la imagen con el mismo Dockerfile de la Parte 1, levanta el
   contenedor dentro del runner y hace un *smoke test* (hasta 15 intentos) contra
   `/actuator/health` esperando `UP`. Siempre elimina el contenedor al finalizar. No publica
   la imagen en ningún registro externo.

## Cómo compilar y ejecutar
1. Clonar el repositorio: `git clone https://github.com/USUARIO/apellido-post1-u12.git`
2. Construir la imagen: `docker build -t mi-spring-app:local .`
3. Ejecutar con valores por defecto (perfil dev):
   `docker run -p 8080:8080 mi-spring-app:local`
4. Ejecutar con perfil prod:
   ```
   docker run -p 8080:8080 \
     -e SPRING_PROFILES_ACTIVE=prod \
     -e DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/appdb \
     -e DATASOURCE_USER=appuser \
     -e DATASOURCE_PASSWORD=apppass \
     mi-spring-app:local
   ```
5. Verificar el estado: `curl http://localhost:8080/actuator/health`
6. Probar la API: `curl http://localhost:8080/api/productos`
7. Consultar el pipeline en la pestaña **Actions** del repositorio.

## Despliegue (paso final manual)
La aplicación se desplegó en Railway/Render a partir del mismo Dockerfile, conectando el
repositorio de GitHub y configurando las variables de entorno en el panel de la plataforma
(`SPRING_PROFILES_ACTIVE=prod`, `DATASOURCE_URL`, `DATASOURCE_USER`, `DATASOURCE_PASSWORD`;
`DDL_AUTO=update` solo en el primer arranque con la base de datos vacía).
- URL de la aplicación: https://TU-APP.up.railway.app
- Health: https://TU-APP.up.railway.app/actuator/health

## Decisiones de diseño

### Parte 1
1. **Placeholders con valor por defecto (`${VAR:default}`) en vez de placeholders estrictos.**
   Un placeholder estricto obliga a exportar todas las variables incluso en local y en CI, y
   falla si falta alguna. Con defaults seguros (H2 en memoria) el mismo artefacto arranca sin
   fricción en la máquina local y en el pipeline, y en producción cada valor se sobrescribe.
   Se evita el default solo en credenciales de producción.
2. **`application-dev.properties` y `application-prod.properties` como archivos separados.**
   La separación física hace explícito qué configuración corresponde a cada ambiente y reduce
   el riesgo de editar producción por error; es la convención estándar de Spring Boot.
3. **Imagen final `eclipse-temurin:21-jre-alpine` en vez de distroless.** Reduce el tamaño
   (sin compilador ni Maven) y conserva un shell mínimo para depurar con `docker exec`.
4. **Etapa de compilación con `maven:3.9-eclipse-temurin-21-alpine`.** La imagen JDK base no
   incluye Maven; usar una imagen con Maven evita instalarlo manualmente y mantiene la caché de capas.

### Parte 2
1. **Prueba mínima de contexto además de una prueba de controlador.** Garantiza que el job de
   CI valide algo real: que Spring construye el contexto con el perfil dev y que
   `/actuator/health` responde `UP`.
2. **El job `docker-build` construye y verifica la imagen pero no la publica.** Evita exigir una
   cuenta externa y Secrets solo para aprobar el laboratorio; el smoke test valida que la imagen
   arranca y expone el monitoreo. Railway/Render construyen su propia imagen desde el Dockerfile.
3. **Exposición de Actuator diferenciada por perfil** (todos los endpoints en dev; solo `health`
   e `info` en prod): evita exponer información sensible (beans, variables de entorno) en producción.

## Capturas de pantalla
![Imagen Docker construida localmente](capturas/docker-images.png)
![Contenedor respondiendo UP en local](capturas/actuator-health-local.png)
![GitHub Actions con checks verdes](capturas/github-actions.png)
![Smoke test respondiendo UP en el pipeline](capturas/smoke-test-log.png)
![Panel de Railway o Render con el despliegue activo](capturas/railway-panel.png)
![Endpoint /actuator/health respondiendo en la URL pública](capturas/railway-health.png)
