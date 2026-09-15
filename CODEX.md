# Contexto del proyecto: citas-medicas

API REST para la gestión de citas médicas. El proyecto se generó con Spring Initializr y usa una arquitectura por capas bajo `com.clinica.citas`.

## Stack y requisitos

- Java: **25** (JDK activo al inicializar el proyecto).
- Maven 3.9+.
- Spring Boot 4.1.1.
- Spring Web MVC, Spring Data JPA y Bean Validation.
- Lombok y MapStruct 1.6.3.
- Jakarta Persistence (`jakarta.persistence.*`), no `javax.persistence.*`.

## Comandos útiles

```bash
mvn clean compile
mvn test
mvn spring-boot:run
```

Si un entorno aislado no permite escribir en `~/.m2`, usar un repositorio temporal:

```bash
mvn -Dmaven.repo.local=/tmp/citas-medicas-m2 clean compile
```

## Estructura

```text
src/main/java/com/clinica/citas/
├── controller/       # Controladores REST base; aún sin métodos HTTP
├── service/impl/     # Interfaces y sus implementaciones
├── repository/       # Repositorios Spring Data JPA
├── model/            # Entidades JPA
│   └── enums/        # Enumeraciones del dominio
├── dto/request/      # DTOs de entrada; contratos aún por definir
├── dto/response/     # DTOs de salida; contratos aún por definir
├── mapper/           # Interfaces MapStruct; conversiones aún por definir
└── exception/        # Excepciones y manejo global HTTP
```

Además, `docs/` está versionado mediante `.gitkeep` y reservado para diagramas UML y Entidad-Relación.

## Dominio actual

- `Persona`: clase abstracta `@MappedSuperclass` con `idPersona`, nombre, apellido, email y teléfono.
- `Medico`: hereda de `Persona`, tiene número de colegiado, una o más especialidades y sus disponibilidades.
- `Paciente`: hereda de `Persona`, tiene fecha de nacimiento e historial de citas.
- `Especialidad`: catálogo de especialidades médicas.
- `Disponibilidad`: franja de un médico (`LocalDate`, `LocalTime` de inicio/fin).
- `Cita`: relaciona médico, paciente y especialidad; registra inicio (`LocalDateTime`), duración en minutos, motivo y estado.
- `EstadoCita`: `PENDIENTE`, `CONFIRMADA`, `CANCELADA`.

Las relaciones `@ManyToOne` son `LAZY`. El estado de una cita se persiste con `@Enumerated(EnumType.STRING)`.

`Medico` y `Especialidad` se relacionan mediante `@ManyToMany`. Las colecciones inversas de disponibilidades e historial de citas usan `@OneToMany(mappedBy = ...)`.

## Reglas de negocio acordadas

- La especialidad solicitada en una cita debe pertenecer al médico.
- El intervalo `[fechaHoraInicio, fechaHoraInicio + duracionMinutos]` debe estar contenido en una disponibilidad del médico.
- No puede existir otra cita `PENDIENTE` o `CONFIRMADA` del mismo médico cuyo intervalo se cruce con el de la nueva cita.
- Cancelar una cita cambia su estado a `CANCELADA`; no se elimina físicamente para conservar trazabilidad.

Estas reglas aún deben implementarse en `CitaServiceImpl` cuando se definan los DTOs y endpoints.

## Estado de la plantilla

- Hay controladores base para médico, paciente, cita y especialidad. Tienen prefijos `/api/...`, pero no métodos HTTP implementados.
- Los servicios e implementaciones de médico, paciente, cita y especialidad son contratos/esqueletos sin reglas de negocio todavía.
- Los repositorios JPA de médico, paciente, cita, especialidad y disponibilidad ya extienden `JpaRepository`.
- Los DTOs y mappers son esqueletos compilables. Añadir campos, restricciones y métodos de conversión junto con el contrato de cada endpoint.
- `GlobalExceptionHandler` responde validaciones como 400, `BadRequestException` como 400 y `ResourceNotFoundException` como 404. Las excepciones no controladas responden 500.

## Convenciones importantes

- **No usar `@Data` en entidades JPA.** Usar `@Getter`, `@Setter`, `@NoArgsConstructor` y `@AllArgsConstructor`; evita ciclos en `toString`, `equals` y `hashCode` por relaciones JPA.
- Mantener entidades en `model`, sin exponerlas directamente desde controladores. Crear DTOs para requests y responses.
- Al definir DTOs de entrada, validarlos con anotaciones `jakarta.validation` y `@Valid` en el controlador.
- Declarar transformaciones en interfaces de `mapper` con `@Mapper(componentModel = "spring")`.
- Los procesadores Lombok, MapStruct y `lombok-mapstruct-binding` ya están configurados en `pom.xml`; no retirar esa configuración.
- Centralizar errores HTTP en `GlobalExceptionHandler`; conservar el contrato `ErrorDetails` para errores de API.

## Antes de entregar cambios

1. Ejecutar `mvn clean compile`.
2. Ejecutar `mvn test` cuando se agreguen o modifiquen pruebas.
3. Verificar que no se hayan introducido `@Data` en `model/`.
4. Revisar que los nuevos endpoints utilicen DTOs, validación y respuestas HTTP adecuadas.
