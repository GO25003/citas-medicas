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

La API queda en `http://localhost:8080` y PostgreSQL en `localhost:5432`. Compose espera a que la base de datos esté saludable antes de iniciar la aplicación. El script `database/init.sql` crea las tablas al inicializar un volumen PostgreSQL vacío; no se vuelve a ejecutar sobre un volumen existente.

Los valores de `.env.example` son solo para desarrollo local. `.env` está excluido de Git. Para detener los servicios conservando los datos, ejecuta `docker compose down`. Para borrar también los datos locales y recrear la base vacía, ejecuta `docker compose down -v`.

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
