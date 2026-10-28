# Contexto técnico para contribuir

## Proyecto y stack

API REST de gestión de citas médicas, bajo `com.clinica.citas`, con arquitectura por capas:

```text
Controller → Service → Repository → PostgreSQL
     ↘ DTO / Mapper / Bean Validation ↗
```

- Java 25, Maven Wrapper (`./mvnw`) y Spring Boot 4.1.1.
- Spring Web MVC, Spring Data JPA, Jakarta Bean Validation y PostgreSQL.
- Lombok y MapStruct 1.6.3; los procesadores de anotaciones y `lombok-mapstruct-binding` están declarados en `pom.xml`.
- Docker Compose levanta PostgreSQL 17 y la aplicación. `database/init.sql` inicializa el esquema para un volumen nuevo.

## Estado implementado

Los cinco recursos tienen controlador, interfaz de servicio, implementación, repositorio, DTOs y mappers:

- `Especialidad`: CRUD.
- `Disponibilidad`: CRUD y asociación a un médico existente; la hora final debe ser posterior a la inicial.
- `Medico`: CRUD y asociación de una o más especialidades existentes.
- `Paciente`: CRUD.
- `Cita`: creación, listado, consulta, actualización, confirmación y cancelación. La ruta `DELETE` cancela sin borrar físicamente.

La lógica de citas verifica que la especialidad pertenezca al médico, que la disponibilidad esté marcada como disponible y contenga el intervalo de la cita en la misma fecha, y que no exista cruce con citas pendientes o confirmadas de ese médico. La actualización excluye la cita actual de la validación de cruces. `CitaRequestDTO` requiere IDs, fecha futura y duración positiva; la confirmación solo acepta citas pendientes y una cita cancelada no se puede actualizar.

El manejo global responde errores de validación y de negocio como 400, recursos inexistentes como 404, conflictos de integridad como 409 y errores inesperados como 500, usando `ErrorDetails`.

Las pruebas presentes cubren los servicios de especialidades y disponibilidades, además del contexto de aplicación. No hay pruebas específicas para los servicios de médicos, pacientes o citas.

## Paquetes

```text
src/main/java/com/clinica/citas/
├── controller/       # Contratos HTTP y códigos de respuesta
├── service/           # Interfaces de casos de uso
├── service/impl/      # Reglas y coordinación con repositorios
├── repository/        # Repositorios Spring Data JPA
├── model/             # Entidades JPA
│   └── enums/         # EstadoCita
├── dto/request/       # Solicitudes y restricciones de entrada
├── dto/response/      # Respuestas de API
├── mapper/            # Conversión MapStruct
└── exception/         # Excepciones y respuesta de error
```

## Modelo de dominio

- `Persona` es `@MappedSuperclass`; `Medico` y `Paciente` definen sus propias tablas e identificadores mediante `@AttributeOverride`.
- `Medico` se relaciona con `Especialidad` mediante `@ManyToMany` y tiene disponibilidades.
- `Paciente` tiene un historial de citas.
- `Cita` referencia un médico, un paciente y una especialidad; registra inicio, duración, motivo y `EstadoCita` (`PENDIENTE`, `CONFIRMADA`, `CANCELADA`).
- Las relaciones de entidad usan carga `LAZY`; el estado se persiste como texto con `@Enumerated(EnumType.STRING)`.
- La estructura inicial de PostgreSQL está en `database/init.sql`; los diagramas de referencia están en `docs/` (`UML.puml` y los artefactos ER/UML).

## Configuración y ejecución

La aplicación obtiene `DB_URL`, `DB_USERNAME` y `DB_PASSWORD` del entorno y carga opcionalmente `.env` como archivo de propiedades. `.env.example` proporciona valores locales de desarrollo; `.env` está ignorado por Git.

```bash
cp .env.example .env
docker compose up --build
```

Para compilar y ejecutar con Maven se requiere JDK 25 y una instancia PostgreSQL accesible con esas variables:

```bash
./mvnw clean compile
./mvnw test
./mvnw spring-boot:run
```

El script SQL de inicialización crea el esquema y carga datos de prueba (12 médicos, 12 pacientes, 6 especialidades, 12 disponibilidades y 12 citas) solo al crear un volumen vacío. Las citas usan fechas futuras y disponibilidades compatibles con las reglas del servicio. `docker compose down` conserva el volumen; `docker compose down -v` lo elimina junto con los datos y permite que el siguiente arranque vuelva a ejecutar el script.

## Convenciones de implementación

- Mantener la arquitectura actual y buscar primero clases y soluciones equivalentes antes de agregar paquetes, dependencias o patrones.
- No usar `@Data` en entidades JPA. Usar anotaciones Lombok puntuales para evitar `equals`, `hashCode` o `toString` que recorran relaciones.
- No exponer entidades JPA desde los controladores. Usar DTOs y mappers MapStruct con `@Mapper(componentModel = "spring")`.
- Validar los cuerpos de entrada con Jakarta Validation en DTOs y `@Valid` en los controladores.
- Colocar reglas de negocio y coordinación entre repositorios en la capa Service; los controladores se limitan a HTTP.
- Centralizar las respuestas de error en `GlobalExceptionHandler` y mantener el contrato `ErrorDetails`.
- Mantener sincronizados el esquema y los datos iniciales de `database/init.sql`, las entidades y los diagramas. Ante discrepancias, documentar la diferencia y evitar cambios estructurales no relacionados.
- No guardar credenciales reales ni incluir `.env` en Git.

## Antes de entregar cambios

1. Ejecutar `./mvnw clean compile`.
2. Ejecutar `./mvnw test` si se agregaron o modificaron pruebas; revisar las pruebas existentes si el cambio puede afectarlas.
3. Verificar que las entidades no introduzcan `@Data` y que los nuevos endpoints usen DTOs y validación.
4. Para cambios de integración o configuración Docker, verificar el arranque con `docker compose up --build`.
