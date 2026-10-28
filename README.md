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

La interfaz administrativa queda en `http://localhost:3000`, la API en `http://localhost:8080` y PostgreSQL en `localhost:5432`. Compose espera a que la base de datos esté saludable antes de iniciar la aplicación. Nginx sirve el frontend y reenvía `/api` al servicio `app` dentro de la red de Compose. El script `database/init.sql` crea las tablas y carga datos de prueba al inicializar un volumen PostgreSQL vacío (12 médicos, 12 pacientes, 6 especialidades, 12 disponibilidades y 12 citas); no se vuelve a ejecutar sobre un volumen existente. Para volver a cargar esos datos desde cero, ejecuta `docker compose down -v` y luego `docker compose up --build` (esto elimina los datos guardados en el volumen local).

Los valores de `.env.example` son solo para desarrollo local. `.env` está excluido de Git. Para detener los servicios conservando los datos, ejecuta `docker compose down`. Para borrar también los datos locales y recrear la base vacía, ejecuta `docker compose down -v`.

## Frontend

El frontend usa Vite, React 18, TypeScript estricto, Tailwind CSS, React Router, TanStack Query, React Hook Form, Zod, Axios y Sonner.

Para trabajar localmente con la API levantada en Docker (o en `localhost:8080`):

```bash
cd frontend
cp .env.example .env
npm install
npm run dev
```

Vite sirve la aplicación en `http://localhost:5173` y hace proxy de `/api` a `VITE_API_PROXY_TARGET` (por defecto `http://localhost:8080`). En Docker, abre `http://localhost:3000`; Nginx resuelve el servicio `app:8080` usando la red interna de Compose. La recarga directa de rutas como `/citas` funciona mediante el fallback SPA.

La aplicación tiene resumen, citas y módulos CRUD de pacientes, médicos, especialidades y disponibilidades. Incluye búsqueda y paginación en cliente, filtros en citas, confirmación/cancelación, diálogos de confirmación, tema claro/oscuro y mensajes de error de la API.

### Estructura

```text
frontend/
├── src/api/          # Cliente Axios y recursos REST
├── src/hooks/        # Consultas y mutaciones TanStack Query
├── src/types/        # Tipos request/response de la API
├── src/components/   # Layout y componentes compartidos
├── src/pages/        # Páginas y módulos
└── src/lib/          # Esquemas y utilidades
```

### Supuestos del contrato del backend

- `LocalDateTime` de las citas no lleva zona horaria ni offset. La interfaz envía y muestra la hora local del navegador como `YYYY-MM-DDTHH:mm:ss`; no convierte a UTC.
- `LocalDate` usa `YYYY-MM-DD` y `LocalTime` `HH:mm:ss`. La API responde las listas como arreglos sin paginación.
- Para médicos el request requiere `idsEspecialidades` como lista de números, mientras el response devuelve objetos `especialidades`. Para citas, el response expone nombres además de IDs.
- Disponibilidad es un intervalo fechado por médico; no hay regla de repetición semanal. La cita se ofrece solo dentro de un intervalo marcado como disponible en esa fecha. El backend sigue siendo la autoridad final para verificar cruces, asociación de especialidad y reglas de negocio.
- La interfaz permite elegir una especialidad para la cita y filtra los médicos usando `especialidades` en su respuesta. Para editar/crear médicos, la API espera IDs de especialidad; la UI muestra el campo de asociación mediante dichos IDs.
- Los errores del backend usan `{ timestamp, status, error, message, path }`. Se presenta `message`; en conflictos de integridad se conserva el mensaje amigable que devuelve el servidor.
- El backend no implementa autenticación; esta interfaz asume acceso interno abierto y no añade login.

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
