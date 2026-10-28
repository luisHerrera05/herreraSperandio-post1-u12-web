# Guía paso a paso — Post-contenido U12

La carpeta `apellido-post1-u12/` es el repositorio listo. Si ya tienes tu proyecto de
la Unidad 8/9, puedes reemplazar `src/` y `pom.xml` (conserva los cambios de Actuator,
perfiles y pruebas) y ajustar el paquete en `logging.level.com.ejemplo`.

## 0. Antes de empezar
1. Renombra la carpeta con tu apellido: `garcia-post1-u12` (igual el repo en GitHub).
2. En `README.md` reemplaza `USUARIO` y `TU-APP`.
3. Crea el repo VACÍO en GitHub (sin README).

## 1. Verificación local (Checkpoint 1)
```bash
cd tu-apellido-post1-u12
mvn clean package            # corre las pruebas
java -jar target/*.jar
curl localhost:8080/actuator/health      # {"status":"UP",...}
```
(El zip no incluye `mvnw`; si lo quieres: `mvn wrapper:wrapper`.)

## 2. Parte 1 — commits (en este orden)
No agregues `.github/` todavía: el workflow va en la Parte 2.
```bash
git init -b main

git add pom.xml src/main/java .gitignore
git commit -m "feat: proyecto Spring Boot base con CRUD de Categoria y Producto"

git add src/main/resources/application*.properties
git commit -m "feat: externalizar configuracion con placeholders y separar perfiles dev/prod (parte 1)"

git add Dockerfile .dockerignore
git commit -m "feat: Dockerfile multi-stage y .dockerignore (parte 1)"

git add README.md
git commit -m "docs: instrucciones de build local, ejecucion con Docker y decisiones de diseno (parte 1)"

git remote add origin https://github.com/TU_USUARIO/tu-apellido-post1-u12.git
git push -u origin main
```
Para el commit del README de la Parte 1 puedes dejar solo las secciones Descripción,
Parte 1, Cómo ejecutar y Decisiones de la Parte 1, y completarlo al final.

## 3. Verificar el Docker (Checkpoint 2 y final P1)
```bash
docker build -t mi-spring-app:local .
docker images mi-spring-app          # captura -> capturas/docker-images.png (debe ser < 300 MB)

docker run -d --name mi-app-dev -p 8080:8080 mi-spring-app:local
curl localhost:8080/api/productos
curl localhost:8080/actuator/health  # captura -> capturas/actuator-health-local.png
docker logs mi-app-dev | grep -i "profile"    # "The following 1 profile is active: dev"
docker stop mi-app-dev && docker rm mi-app-dev
```
Prueba prod (necesitas Postgres local; si no, levántalo así):
```bash
docker run -d --name pg -e POSTGRES_DB=appdb -e POSTGRES_USER=appuser -e POSTGRES_PASSWORD=apppass -p 5432:5432 postgres:16-alpine
docker run -d --name mi-app-prod -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/appdb \
  -e DATASOURCE_USER=appuser -e DATASOURCE_PASSWORD=apppass \
  -e DDL_AUTO=update \
  mi-spring-app:local
docker logs -f mi-app-prod                   # confirma perfil "prod"
curl localhost:8080/actuator/health          # UP
curl -i localhost:8080/actuator/beans        # 404 en prod
```
Mac con chip M2: Docker Desktop funciona en ARM; las imágenes usadas tienen soporte arm64.

## 4. Parte 2 — commits
Actuator ya está en `pom.xml` y en los perfiles. Para que cada commit refleje trabajo real,
agrégalo en commits separados: ten el `pom.xml` sin la dependencia de Actuator y las
líneas `management.*` fuera de los perfiles en la Parte 1, y súmalos aquí.
```bash
git add pom.xml src/main/resources
git commit -m "feat: agregar Spring Boot Actuator con exposicion diferenciada por perfil (parte 2)"

git add src/test/java
git commit -m "test: agregar pruebas de contexto y de controlador para el pipeline (parte 2)"

git add .github/workflows/ci.yml
git commit -m "ci: agregar pipeline GitHub Actions con build, test y smoke test de Docker (parte 2)"
git push origin main
```
Haz al menos 2 pushes más con cambios reales para tener **3 ejecuciones verdes**.
Capturas: pestaña Actions (`github-actions.png`) y el log del paso "Levantar el contenedor..."
con "La aplicacion respondio UP" (`smoke-test-log.png`).
```bash
git add README.md capturas
git commit -m "docs: badge de estado, decisiones de diseno y README final con resumen de ambas partes (parte 2)"
git push origin main
```

## 5. Despliegue en Railway / Render (paso final)
1. Entra con GitHub → "Deploy from GitHub repo" → elige tu repo (detecta el Dockerfile).
2. Agrega una base PostgreSQL (Railway: "+ New → Database → PostgreSQL").
3. Variables del servicio:
   - `SPRING_PROFILES_ACTIVE=prod`
   - `DATASOURCE_URL=jdbc:postgresql://HOST:PUERTO/BASE` (arma el JDBC con los datos de la BD; la URL `postgresql://` que da la plataforma NO sirve tal cual)
   - `DATASOURCE_USER`, `DATASOURCE_PASSWORD`
   - `DDL_AUTO=update` (solo el primer arranque; luego puedes quitarla)
4. Genera dominio público y abre `/actuator/health` → `{"status":"UP"}`.
5. Capturas: panel con despliegue activo (`railway-panel.png`) y el health en la URL pública (`railway-health.png`).

## 6. Checklist final de entrega
- [ ] Repo `apellido-post1-u12` con mínimo 6 commits descriptivos (3 por parte)
- [ ] 6 capturas en `capturas/` con los nombres del README
- [ ] Badge del README con tu usuario real (se ve verde)
- [ ] 3 ejecuciones verdes en Actions
- [ ] URL pública funcionando
- [ ] Repo público (o compartido con el docente) y URL entregada en Aula virtual UDES
