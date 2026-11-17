# Sistema de citas médicas

API REST para administrar pacientes, médicos, especialidades, disponibilidades y citas. Está construida con Java, Spring Boot, Spring Data JPA y PostgreSQL, siguiendo una arquitectura por capas.

## Funcionalidad actual

La API cuenta con operaciones CRUD para médicos, pacientes, especialidades y disponibilidades. Las citas permiten crear, consultar, actualizar, confirmar y cancelar; `DELETE /api/citas/{id}` también cancela y conserva el registro.

Al crear o actualizar una cita se valida que la especialidad esté asignada al médico, que el horario esté dentro de una disponibilidad activa del mismo día y que no se cruce con otra cita pendiente o confirmada de ese médico. La duración debe ser positiva y la cita no puede terminar en otro día. Las solicitudes usan DTOs con Bean Validation; los errores se traducen a respuestas HTTP centralizadas.

## Tecnologías

- Java 25 y Maven (se incluye Maven Wrapper).
- Spring Boot 4.1.1, Spring Web MVC, Spring Data JPA y Bean Validation.
- PostgreSQL 17 en Docker Compose.
- Lombok y MapStruct 1.6.3.

## Iniciar con Docker Compose

Requisitos: Docker Engine y Docker Compose.

```bash
cp .env.example .env
docker compose up --build
```

La API queda en `http://localhost:8080` y PostgreSQL en `localhost:5432`. Compose espera a que la base de datos esté saludable antes de iniciar la aplicación. El script `database/init.sql` crea las tablas y carga datos de prueba al inicializar un volumen PostgreSQL vacío (12 médicos, 12 pacientes, 6 especialidades, 12 disponibilidades y 12 citas); no se vuelve a ejecutar sobre un volumen existente. Para volver a cargar esos datos desde cero, ejecuta `docker compose down -v` y luego `docker compose up --build` (esto elimina los datos guardados en el volumen local).

Los valores de `.env.example` son solo para desarrollo local. `.env` está excluido de Git. Para detener los servicios conservando los datos, ejecuta `docker compose down`. Para borrar también los datos locales y recrear la base vacía, ejecuta `docker compose down -v`.

## Despliegue en Kubernetes

Los manifiestos de `k8s/` despliegan PostgreSQL 17 y la API en el namespace `citas-medicas`. La API usa la imagen local `citas-medicas:local` con `imagePullPolicy: Never`; por ello, la imagen debe estar disponible en el runtime del clúster antes de crear el Deployment. El Service interno de la base de datos se llama `postgres` y la API usa `jdbc:postgresql://postgres:5432/citas_medicas`.

### Requisitos y comprobación del clúster

Se necesitan Docker, `kubectl` configurado para un clúster en ejecución y un runtime accesible desde Kubernetes. Comprueba el contexto antes de continuar:

```bash
kubectl config current-context
kubectl cluster-info
kubectl get nodes -o wide
```

El entorno disponible para este proyecto usa Kind: el contexto detectado es `kind-citas-medicas` y el clúster se llama `citas-medicas`. Antes de desplegar, confirma que ese contexto responde con los comandos anteriores. Los pasos de CI/CD más abajo requieren un runner self-hosted en el mismo host de Kind.

### Construir y cargar la imagen

Desde la raíz del repositorio:

```bash
docker build -t citas-medicas:local .
```

Carga la imagen en los nodos del clúster Kind:

```bash
kind load docker-image citas-medicas:local --name citas-medicas
```

### Crear credenciales y configuración SQL

Los siguientes valores son solo para desarrollo. No guardes credenciales reales en archivos versionados. Crea el namespace primero y luego los recursos auxiliares:

```bash
kubectl apply -f k8s/namespace.yaml
kubectl create secret generic postgres-credentials \
  --namespace citas-medicas \
  --from-literal=username=citas_user \
  --from-literal=password=change_me
kubectl create configmap postgres-init \
  --namespace citas-medicas \
  --from-file=init.sql=database/init.sql
```

El ConfigMap monta el SQL en `/docker-entrypoint-initdb.d/01-init.sql`. La imagen oficial de PostgreSQL ejecuta esos scripts al inicializar un directorio de datos vacío; los reinicios con un PVC ya inicializado no vuelven a ejecutar el script, igual que el volumen de Compose.

### Aplicar y verificar

```bash
kubectl apply -f k8s/
kubectl get pods -n citas-medicas -w
```

Cuando ambos Pods aparezcan como `Running` y `Ready`:

```bash
kubectl get deployments,pods,services,pvc -n citas-medicas
kubectl describe pod -n citas-medicas -l app=postgres
kubectl describe pod -n citas-medicas -l app=citas-medicas
kubectl logs -n citas-medicas deployment/postgres
kubectl logs -n citas-medicas deployment/citas-medicas
```

Comprueba la resolución y disponibilidad del Service PostgreSQL desde la red del clúster:

```bash
kubectl run postgres-check -n citas-medicas --rm -i --restart=Never \
  --image=postgres:17-alpine -- \
  pg_isready -h postgres -p 5432 -U citas_user -d citas_medicas
```

El init container de la aplicación repite `pg_isready` contra `postgres` hasta que la base acepte conexiones. En los logs de Spring Boot, confirma que no aparezcan errores JDBC ni fallos de conexión.

### Acceder a la API

El Service de la API es `ClusterIP`. Mantén el siguiente comando en ejecución y accede desde otra terminal:

```bash
kubectl port-forward -n citas-medicas service/citas-medicas 8080:8080
curl -i http://localhost:8080/api/especialidades
```

### Detener y eliminar

Para detener la aplicación y PostgreSQL y eliminar todos los recursos del namespace (incluido el PVC y sus datos):

```bash
kubectl delete namespace citas-medicas
```

El namespace y sus recursos se pueden volver a crear siguiendo estos pasos. El borrado del PVC elimina los datos persistidos del clúster.

## CI/CD local con ACT

El workflow `.github/workflows/ci.yml` conserva triggers compatibles con GitHub Actions para CI, y ofrece el despliegue únicamente como job manual y local. No configura runners registrados ni intenta conectar GitHub con el clúster Kind de Fedora.

### Requisitos y entorno

Se necesita Docker Engine activo, Kind, `kubectl`, `act`, `flock` (util-linux), el Maven Wrapper del repositorio y acceso a Internet para descargar las imágenes y dependencias la primera vez. No hace falta Maven instalado globalmente. El clúster debe existir ya; no se crea otro:

```bash
kind get clusters
kubectl config current-context
kubectl get nodes
```

El clúster usado aquí es `citas-medicas` y el contexto `kind-citas-medicas`. El script `scripts/act-kind.sh` valida esas condiciones. En Fedora/Linux, ACT usa `--network host`: el runner puede alcanzar el API de Kind publicado en `127.0.0.1` y PostgreSQL de servicio publicado en `localhost:55432`. Ese puerto evita chocar con el 5432 de Docker Compose. El script entrega explícitamente a ACT el endpoint Unix del socket Docker, monta los binarios locales `docker`, `kind` y `kubectl`, y monta una copia temporal, aplanada y de solo lectura del kubeconfig actual; la elimina al terminar. GitHub-hosted usa el mismo puerto publicado por su runner.

### Crear el archivo local de Secrets

El archivo `.act.secrets` está excluido de Git. Para una base ya inicializada en el namespace, genera el archivo directamente desde el Secret existente, sin mostrar sus valores en la terminal:

```bash
umask 077
{
  printf 'POSTGRES_USERNAME='
  kubectl get secret postgres-credentials -n citas-medicas -o jsonpath='{.data.username}' | base64 --decode
  printf '\nPOSTGRES_PASSWORD='
  kubectl get secret postgres-credentials -n citas-medicas -o jsonpath='{.data.password}' | base64 --decode
  printf '\n'
} > .act.secrets
chmod 600 .act.secrets
```

Si todavía no existe ese Secret, crea `.act.secrets` con estas dos claves y credenciales locales de desarrollo:

```text
POSTGRES_USERNAME=citas_user
POSTGRES_PASSWORD=change_me
```

No subas ese archivo. En una base persistente existente las credenciales deben coincidir con las usadas al inicializar PostgreSQL: cambiar el Secret no modifica el rol/password almacenado en PostgreSQL. Del mismo modo, `init.sql` se ejecuta solo cuando se inicializa un directorio de datos vacío; actualizar el ConfigMap no lo vuelve a ejecutar.

### Ejecutar solo CI con ACT

Desde la raíz del repositorio:

```bash
act pull_request -W .github/workflows/ci.yml -j test \
  --network host --env-file /dev/null --secret-file /dev/null --var-file /dev/null \
  -P ubuntu-latest=catthehacker/ubuntu:act-latest
```

Este job inicia PostgreSQL 17 Alpine con credenciales ficticias solo para CI, espera `pg_isready`, configura `DB_URL`, `DB_USERNAME` y `DB_PASSWORD`, y ejecuta `./mvnw --batch-mode --no-transfer-progress verify`. `database/init.sql` no es necesario para estas pruebas: las pruebas de servicios usan mocks y la prueba de contexto solo necesita conectar JPA a una base vacía para detectar su dialecto.

### Ejecutar el pipeline completo y desplegar en Kind

```bash
./scripts/act-kind.sh
```

El script ejecuta el evento `workflow_dispatch` con `deploy_local=true`, carga `.act.secrets` como Secrets del workflow y configura el acceso local para el contenedor ACT. El job valida el contexto existente, crea/actualiza `postgres-credentials` y `postgres-init`, aplica PVC/Service/Deployment de PostgreSQL y espera a que esté Ready antes de construir y cargar `citas-medicas:local`. Después aplica el Service y Deployment de la API, fuerza el rollout y espera su disponibilidad. `imagePullPolicy: Never` evita consultas a un registry para la imagen local. El pipeline serializa ejecuciones de despliegue con `concurrency`.

Verifica el resultado y la API con:

```bash
kubectl get deployments,pods,services -n citas-medicas
kubectl rollout status deployment/postgres -n citas-medicas
kubectl rollout status deployment/citas-medicas -n citas-medicas
kubectl port-forward -n citas-medicas service/citas-medicas 8080:8080
```

Desde otra terminal, consulta `curl -i http://localhost:8080/api/especialidades`.

### Diferencias y límites de ACT

ACT simula los jobs en contenedores Docker; no hereda automáticamente el kubeconfig del host y el acceso al socket se configura de forma explícita. El script pasa el socket, los binarios y el kubeconfig requerido y usa red host para alcanzar los puertos locales de Kind y PostgreSQL. En Fedora aplica `label=disable` al contenedor de ACT para permitir el uso del socket bajo SELinux. El socket Docker da al job control amplio sobre el daemon local: ejecuta el deploy solo con código de confianza y no uses secrets reales en workflows no confiables. El kubeconfig se monta de solo lectura, no se imprime y se borra al salir. ACT no expone de forma compatible el puerto mapeado de `job.services`, por eso el workflow reserva el puerto de host 55432; verifica que esté libre antes de ejecutar varias instancias o un servicio que lo use.

Los triggers push/pull request siguen siendo válidos en GitHub Actions, donde se ejecuta CI. El job de despliegue exige que `ACT=true`; si se intenta habilitar desde un runner hospedado fallará deliberadamente porque ese runner no puede acceder al Kind local. ACT no reproduce exactamente la red del runner hospedado, los permisos de GitHub ni sus políticas de concurrencia; ejecuta un único despliegue local a la vez.

## Compilar y ejecutar con Maven

Requiere JDK 25 y una base PostgreSQL configurada mediante `DB_URL`, `DB_USERNAME` y `DB_PASSWORD` (también pueden declararse en `.env`).

```bash
./mvnw clean compile
./mvnw test
./mvnw spring-boot:run
```

También se puede usar `mvn` instalado en el sistema. Las pruebas existentes cubren servicios de especialidades y disponibilidades, además de la prueba de contexto de Spring.

## Endpoints

Todos los recursos están bajo `/api`.

| Recurso | Rutas | Operaciones |
|---|---|---|
| Especialidades | `/especialidades`, `/especialidades/{id}` | `GET` lista/consulta, `POST` crea, `PUT` actualiza, `DELETE` elimina |
| Disponibilidades | `/disponibilidades`, `/disponibilidades/{id}` | `GET` lista/consulta, `POST` crea, `PUT` actualiza, `DELETE` elimina |
| Médicos | `/medicos`, `/medicos/{id}` | `GET` lista/consulta, `POST` crea, `PUT` actualiza, `DELETE` elimina |
| Pacientes | `/pacientes`, `/pacientes/{id}` | `GET` lista/consulta, `POST` crea, `PUT` actualiza, `DELETE` elimina |
| Citas | `/citas`, `/citas/{id}` | `GET` lista/consulta, `POST` crea, `PUT` actualiza, `DELETE` cancela |
| Estado de cita | `/citas/{id}/confirmar`, `/citas/{id}/cancelar` | `PATCH` confirma o cancela |

Las creaciones responden `201`; las consultas y actualizaciones responden `200`; las eliminaciones/cancelaciones responden `204`. Los detalles de los cuerpos JSON se definen en `dto/request` y `dto/response`.

## Estructura

```text
src/main/java/com/clinica/citas/
├── controller/   # Endpoints REST
├── service/      # Casos de uso y reglas de negocio
├── repository/   # Persistencia Spring Data JPA
├── model/        # Entidades y enum de dominio
├── dto/          # Contratos HTTP de entrada y salida
├── mapper/       # Conversión MapStruct
└── exception/    # Errores y manejo HTTP global
database/init.sql # Esquema PostgreSQL inicial
docs/             # Diagramas UML y Entidad-Relación
```

Consulta [CODEX.md](CODEX.md) para las convenciones y el contexto técnico para contribuir.
