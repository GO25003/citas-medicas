# Sistema de citas médicas

API REST para administrar pacientes, médicos, especialidades, disponibilidades y citas. Está construida con Java, Spring Boot, Spring Data JPA y PostgreSQL, siguiendo una arquitectura por capas.

## Entidades

El dominio tiene cinco entidades: **Especialidad**, **Médico**, **Paciente**, **Disponibilidad** y **Cita**. Un médico tiene una o varias especialidades y varias disponibilidades; una cita relaciona a un médico, un paciente y una especialidad.

Los diagramas están en la carpeta `docs/`:

- `docs/UML.puml` y `docs/Diagrama UML.png` (diagrama UML).
- `docs/Diagrama ER.pdf` y `docs/diagrama ER.ccd` (diagrama Entidad-Relación).

## Funcionalidad actual

La API cuenta con operaciones CRUD para médicos, pacientes, especialidades y disponibilidades. Las citas permiten crear, consultar, actualizar, confirmar y cancelar; `DELETE /api/citas/{id}` también cancela y conserva el registro.

Al crear o actualizar una cita se valida que la especialidad esté asignada al médico, que el horario esté dentro de una disponibilidad activa del mismo día y que no se cruce con otra cita pendiente o confirmada de ese médico. La duración debe ser positiva y la cita no puede terminar en otro día.

Las solicitudes utilizan DTOs con Bean Validation y los errores se traducen a respuestas HTTP centralizadas.

## Tecnologías

- Java 25 y Maven (se incluye Maven Wrapper).
- Spring Boot 4.1.1, Spring Web MVC, Spring Data JPA y Bean Validation.
- PostgreSQL 17 en Docker Compose.
- Lombok y MapStruct 1.6.3.
- springdoc-openapi 3.0.0 (Swagger UI).
- JUnit 5 y Mockito para las pruebas, con H2 en memoria únicamente en el ámbito de pruebas.

## Requisitos

| Herramienta | Para qué se usa |
|---|---|
| JDK 25 | Compilar y ejecutar con Maven |
| Maven Wrapper (`./mvnw`, incluido) | Compilar y ejecutar pruebas; no hace falta Maven global |
| Docker Engine y Docker Compose | Levantar la API y PostgreSQL |
| Kind y `kubectl` | Despliegue en Kubernetes local |
| `act` y `flock` (util-linux) | Pipeline local de CI/CD |

## Variables de entorno

La aplicación lee su conexión a la base de datos de las siguientes variables. Los valores de `.env.example` son únicamente para desarrollo local; `.env` está excluido de Git.

| Variable | Valor en `.env.example` | Uso |
|---|---|---|
| `DB_URL` | `jdbc:postgresql://db:5432/citas_medicas` | URL JDBC de PostgreSQL |
| `DB_USERNAME` | `citas_user` | Usuario de la base de datos |
| `DB_PASSWORD` | `change_me` | Contraseña de la base de datos |

## Iniciar con Docker Compose

Requisitos: Docker Engine y Docker Compose.

```bash
cp .env.example .env
docker compose up --build
```

La API queda en `http://localhost:8080` y PostgreSQL en `localhost:5432`. Compose espera a que la base de datos esté saludable antes de iniciar la aplicación.

El script `database/init.sql` crea las tablas y carga datos de prueba al inicializar un volumen PostgreSQL vacío:

- 12 médicos.
- 12 pacientes.
- 6 especialidades.
- 12 disponibilidades.
- 12 citas.

El script no vuelve a ejecutarse sobre un volumen existente. Para volver a cargar esos datos desde cero, ejecuta:

```bash
docker compose down -v
docker compose up --build
```

Esto elimina los datos guardados en el volumen local.

Los valores de `.env.example` son solo para desarrollo local. El archivo `.env` está excluido de Git.

Para detener los servicios conservando los datos:

```bash
docker compose down
```

Para borrar también los datos locales y recrear la base vacía:

```bash
docker compose down -v
```

## Despliegue en Kubernetes

Los manifiestos de `k8s/` despliegan PostgreSQL 17 y la API en el namespace `citas-medicas`.

La API utiliza la imagen local `citas-medicas:local` con `imagePullPolicy: Never`; por ello, la imagen debe estar disponible en el runtime del clúster antes de crear el Deployment.

El Service interno de la base de datos se llama `postgres` y la API utiliza la siguiente conexión:

```text
jdbc:postgresql://postgres:5432/citas_medicas
```

### Requisitos y comprobación del clúster

Se necesitan Docker, `kubectl` configurado para un clúster en ejecución y un runtime accesible desde Kubernetes.

Comprueba el contexto antes de continuar:

```bash
kubectl config current-context
kubectl cluster-info
kubectl get nodes -o wide
```

El entorno disponible para este proyecto utiliza Kind:

- Contexto: `kind-citas-medicas`.
- Nombre del clúster: `citas-medicas`.

Antes de desplegar, confirma que ese contexto responde con los comandos anteriores.

Los pasos de CI/CD más abajo requieren un entorno local con acceso al host donde se ejecuta Kind.

### Construir y cargar la imagen

Desde la raíz del repositorio, construye la imagen:

```bash
docker build -t citas-medicas:local .
```

Carga la imagen en los nodos del clúster Kind:

```bash
kind load docker-image citas-medicas:local --name citas-medicas
```

### Crear credenciales y configuración SQL

Los siguientes valores son únicamente para desarrollo. No guardes credenciales reales en archivos versionados.

Crea primero el namespace:

```bash
kubectl apply -f k8s/namespace.yaml
```

Después, crea el Secret con las credenciales de PostgreSQL:

```bash
kubectl create secret generic postgres-credentials \
  --namespace citas-medicas \
  --from-literal=username=citas_user \
  --from-literal=password=change_me
```

Crea el ConfigMap con el script de inicialización:

```bash
kubectl create configmap postgres-init \
  --namespace citas-medicas \
  --from-file=init.sql=database/init.sql
```

El ConfigMap monta el SQL en `/docker-entrypoint-initdb.d/01-init.sql`.

La imagen oficial de PostgreSQL ejecuta estos scripts al inicializar un directorio de datos vacío. Los reinicios con un PVC ya inicializado no vuelven a ejecutar el script, igual que sucede con el volumen de Compose.

### Aplicar y verificar

Aplica los manifiestos de Kubernetes:

```bash
kubectl apply -f k8s/
```

Comprueba el estado de los Pods:

```bash
kubectl get pods -n citas-medicas -w
```

Cuando ambos Pods aparezcan como `Running` y `Ready`, ejecuta las siguientes comprobaciones:

```bash
kubectl get deployments,pods,services,pvc -n citas-medicas
```

Consulta los detalles de los Pods:

```bash
kubectl describe pod -n citas-medicas -l app=postgres
kubectl describe pod -n citas-medicas -l app=citas-medicas
```

Consulta los logs:

```bash
kubectl logs -n citas-medicas deployment/postgres
kubectl logs -n citas-medicas deployment/citas-medicas
```

Comprueba la resolución y disponibilidad del Service PostgreSQL desde la red del clúster:

```bash
kubectl run postgres-check -n citas-medicas --rm -i --restart=Never \
  --image=postgres:17-alpine -- \
  pg_isready -h postgres -p 5432 -U citas_user -d citas_medicas
```

El init container de la aplicación repite `pg_isready` contra `postgres` hasta que la base acepte conexiones.

En los logs de Spring Boot, confirma que no aparezcan errores JDBC ni fallos de conexión.

### Acceder a la API

El Service de la API es de tipo `ClusterIP`. Mantén el siguiente comando en ejecución y accede desde otra terminal:

```bash
kubectl port-forward -n citas-medicas service/citas-medicas 8080:8080
```

Comprueba la API:

```bash
curl -i http://localhost:8080/api/especialidades
```

Swagger UI también estará disponible en:

`http://localhost:8080/swagger-ui/index.html`

### Detener y eliminar

Para detener la aplicación y PostgreSQL y eliminar todos los recursos del namespace, incluido el PVC y sus datos:

```bash
kubectl delete namespace citas-medicas
```

El namespace y sus recursos se pueden volver a crear siguiendo estos pasos.

El borrado del PVC elimina los datos persistidos del clúster.

## CI/CD local con ACT

El workflow `.github/workflows/ci.yml` conserva triggers compatibles con GitHub Actions para CI y ofrece el despliegue únicamente como job manual y local.

No configura runners registrados ni intenta conectar GitHub con el clúster Kind de Fedora.

### Requisitos y entorno

Se necesita Docker Engine activo, Kind, `kubectl`, `act`, `flock` (util-linux), el Maven Wrapper del repositorio y acceso a Internet para descargar las imágenes y dependencias la primera vez.

No hace falta Maven instalado globalmente.

El clúster debe existir previamente; el pipeline no crea otro:

```bash
kind get clusters
kubectl config current-context
kubectl get nodes
```

El clúster utilizado es `citas-medicas` y el contexto `kind-citas-medicas`.

El script `scripts/act-kind.sh` valida esas condiciones.

En Fedora/Linux, ACT utiliza `--network host`, lo que permite al runner alcanzar el API de Kind publicado en `127.0.0.1` y PostgreSQL de servicio publicado en `localhost:55432`.

Ese puerto evita conflictos con el puerto `5432` de Docker Compose.

El script entrega explícitamente a ACT el endpoint Unix del socket Docker, monta los binarios locales `docker`, `kind` y `kubectl`, y monta una copia temporal, aplanada y de solo lectura del kubeconfig actual. Esta copia se elimina al terminar.

GitHub-hosted utiliza el mismo puerto publicado por su runner.

### Crear el archivo local de Secrets

El archivo `.act.secrets` está excluido de Git.

Para una base ya inicializada en el namespace, genera el archivo directamente desde el Secret existente, sin mostrar sus valores en la terminal:

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

No subas ese archivo.

En una base persistente existente, las credenciales deben coincidir con las utilizadas al inicializar PostgreSQL: cambiar el Secret no modifica el rol ni la contraseña almacenados en PostgreSQL.

Del mismo modo, `init.sql` se ejecuta únicamente cuando se inicializa un directorio de datos vacío; actualizar el ConfigMap no vuelve a ejecutar el script.

### Ejecutar solo CI con ACT

Desde la raíz del repositorio:

```bash
act pull_request -W .github/workflows/ci.yml -j test \
  --network host --env-file /dev/null --secret-file /dev/null --var-file /dev/null \
  -P ubuntu-latest=catthehacker/ubuntu:act-latest
```

Este job inicia PostgreSQL 17 Alpine con credenciales ficticias para CI, espera mediante `pg_isready`, configura `DB_URL`, `DB_USERNAME` y `DB_PASSWORD`, y ejecuta:

```bash
./mvnw --batch-mode --no-transfer-progress verify
```

`database/init.sql` no es necesario para estas pruebas: las pruebas de servicios utilizan mocks y la prueba de contexto solo necesita conectar JPA a una base vacía para detectar su dialecto.

### Ejecutar el pipeline completo y desplegar en Kind

Desde la raíz del repositorio:

```bash
./scripts/act-kind.sh
```

El script ejecuta el evento `workflow_dispatch` con `deploy_local=true`, carga `.act.secrets` como Secrets del workflow y configura el acceso local para el contenedor ACT.

El job realiza las siguientes operaciones:

1. Valida el contexto existente de Kubernetes.
2. Crea o actualiza `postgres-credentials` y `postgres-init`.
3. Aplica el PVC, Service y Deployment de PostgreSQL.
4. Espera a que PostgreSQL esté Ready.
5. Construye y carga la imagen `citas-medicas:local`.
6. Aplica el Service y Deployment de la API.
7. Fuerza el rollout y espera su disponibilidad.

`imagePullPolicy: Never` evita consultas a un registry para la imagen local.

El pipeline serializa las ejecuciones de despliegue mediante `concurrency`.

Verifica el resultado:

```bash
kubectl get deployments,pods,services -n citas-medicas
kubectl rollout status deployment/postgres -n citas-medicas
kubectl rollout status deployment/citas-medicas -n citas-medicas
```

Accede a la API:

```bash
kubectl port-forward -n citas-medicas service/citas-medicas 8080:8080
```

Desde otra terminal, consulta:

```bash
curl -i http://localhost:8080/api/especialidades
```

### Diferencias y límites de ACT

ACT simula los jobs en contenedores Docker; no hereda automáticamente el kubeconfig del host y el acceso al socket se configura explícitamente.

El script pasa el socket, los binarios y el kubeconfig requerido y utiliza red host para alcanzar los puertos locales de Kind y PostgreSQL.

En Fedora aplica `label=disable` al contenedor de ACT para permitir el uso del socket bajo SELinux.

El socket Docker proporciona al job control amplio sobre el daemon local. Por ello, ejecuta el despliegue únicamente con código de confianza y no utilices secrets reales en workflows no confiables.

El kubeconfig se monta de solo lectura, no se imprime y se elimina al salir.

ACT no expone de forma compatible el puerto mapeado de `job.services`; por eso el workflow reserva el puerto de host `55432`. Verifica que esté libre antes de ejecutar varias instancias o un servicio que lo utilice.

Los triggers `push` y `pull_request` siguen siendo válidos en GitHub Actions, donde se ejecuta CI.

El job de despliegue exige que `ACT=true`; si se intenta habilitar desde un runner hospedado, fallará deliberadamente porque ese runner no puede acceder al Kind local.

ACT no reproduce exactamente la red del runner hospedado, los permisos de GitHub ni sus políticas de concurrencia. Ejecuta un único despliegue local a la vez.

## Compilar y ejecutar con Maven

Requiere JDK 25 y una base PostgreSQL configurada mediante `DB_URL`, `DB_USERNAME` y `DB_PASSWORD` (también pueden declararse en `.env`).

Para compilar el proyecto:

```bash
./mvnw clean compile
```

Para ejecutar la aplicación:

```bash
./mvnw spring-boot:run
```

También se puede utilizar `mvn` instalado en el sistema.

## Pruebas

Las pruebas no necesitan PostgreSQL ni Docker.

La prueba de contexto de Spring utiliza una base H2 en memoria (dependencia únicamente de ámbito `test`, configurada en `src/test/resources/application.properties`) y las demás pruebas utilizan Mockito.

Para ejecutar las pruebas:

```bash
./mvnw test
```

Las clases de prueba se encuentran en `src/test/java/com/clinica/citas/`:

| Clase de prueba | Qué cubre |
|---|---|
| `CitasMedicasApplicationTests` | Carga del contexto de Spring |
| `EspecialidadServiceImplTest` | Servicio de especialidades |
| `DisponibilidadServiceImplTest` | Servicio de disponibilidades |
| `MedicoServiceImplTest` | Servicio de médicos |
| `PacienteServiceImplTest` | Servicio de pacientes |
| `CitaServiceImplTest` | Servicio de citas y sus reglas de negocio |

Este mismo proceso es el que ejecuta el pipeline mediante `./mvnw verify`.

## Documentación de la API con Swagger

La API se documenta con OpenAPI mediante springdoc.

Con la aplicación en ejecución, se puede acceder a:

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- Especificación OpenAPI en JSON: `http://localhost:8080/v3/api-docs`

Los endpoints aparecen agrupados por recurso: Citas, Disponibilidades, Especialidades, Médicos y Pacientes.

Para probar una operación:

1. Abre el endpoint.
2. Pulsa **Try it out**.
3. Completa los parámetros o el cuerpo JSON.
4. Pulsa **Execute**.
5. Consulta la respuesta del servidor.

La información general (título, versión y descripción) se define en `src/main/java/com/clinica/citas/config/OpenApiConfig.java`.

## Colección de Postman

La colección `API Citas Médicas` está en:

`docs/postman/API_Citas_Medicas.postman_collection.json`

Incluye las carpetas:

- **Especialidad**
- **Médico**
- **Disponibilidad**

Cada carpeta incluye casos de éxito (crear con `201`, listar, obtener por ID, actualizar y eliminar) y casos de error (`404` al consultar un ID inexistente y `400` al crear con cuerpo vacío).

Para utilizarla:

1. Levanta la API (por ejemplo, con `docker compose up --build`).
2. En Postman, importa el archivo JSON mediante **Import**.
3. La variable `baseUrl` de la colección tiene el valor `http://localhost:8080`; cámbialo si la API está en otra dirección.
4. Ejecuta las carpetas en este orden:
   - Especialidad.
   - Médico.
   - Disponibilidad.

Médico necesita el `especialidadId` y Disponibilidad necesita el `medicoId`. Los scripts de la pestaña **Tests** guardan automáticamente esos identificadores al crear los registros.

Los nombres, correos y números de colegiado de los cuerpos utilizan `{{$timestamp}}`, por lo que la colección se puede ejecutar varias veces sin chocar con registros existentes.

## Manejo de errores

Los errores se centralizan en `GlobalExceptionHandler` y devuelven siempre el mismo formato JSON, sin exponer trazas ni nombres de clases internas.

Ejemplo de respuesta:

```json
{
  "timestamp": "2026-11-23T09:34:11.23677808",
  "status": 409,
  "error": "Conflict",
  "message": "La operación entra en conflicto con datos existentes o relacionados",
  "path": "/api/especialidades"
}
```

Los códigos HTTP utilizados son:

| Código | Cuándo ocurre |
|---|---|
| `400 Bad Request` | Datos inválidos (Bean Validation), cuerpo mal formado o violación de una regla de negocio |
| `404 Not Found` | El recurso solicitado no existe |
| `409 Conflict` | Conflicto con datos existentes o relacionados (por ejemplo, una especialidad duplicada) |
| `500 Internal Server Error` | Error inesperado; el detalle queda en el log del servidor, no en la respuesta |

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

Las creaciones responden `201`; las consultas y actualizaciones responden `200`; las eliminaciones y cancelaciones responden `204`.

Los detalles de los cuerpos JSON se definen en `dto/request` y `dto/response`, y se pueden consultar y probar en Swagger UI.

## Estructura

```text
src/main/java/com/clinica/citas/
├── config/       # Configuración de OpenAPI (Swagger)
├── controller/   # Endpoints REST
├── service/      # Casos de uso y reglas de negocio
├── repository/   # Persistencia Spring Data JPA
├── model/        # Entidades y enum de dominio
├── dto/          # Contratos HTTP de entrada y salida
├── mapper/       # Conversión MapStruct
└── exception/    # Errores y manejo HTTP global

src/test/         # Pruebas JUnit y Mockito
database/init.sql # Esquema PostgreSQL inicial
k8s/              # Manifiestos de Kubernetes
scripts/          # Scripts de apoyo (act-kind.sh)
.github/workflows/ci.yml  # Pipeline de CI/CD
docs/             # Diagramas UML y Entidad-Relación
docs/postman/     # Colección de Postman
```

Consulta `CODEX.md` para las convenciones y el contexto técnico para contribuir.

## Integrantes

| Integrante | Rol |
|---|---|
| Fernando José Tesorero Brito | Integrante A: pruebas JUnit, manejo de errores HTTP, Swagger y colección de Postman |
| Carlos Ernesto Garcia Ocampo | Integrante B: Kubernetes y pipeline de CI/CD con ACT |