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
├── controller/       # Endpoints REST
├── service/impl/     # Interfaces y sus implementaciones
├── repository/       # Repositorios Spring Data JPA
├── model/            # Entidades JPA
│   └── enums/        # Enumeraciones del dominio
├── dto/request/      # Payloads de entrada validados
├── dto/response/     # Respuestas de la API
├── mapper/           # Interfaces MapStruct
└── exception/        # Excepciones y manejo global HTTP
```

## Dominio actual

- `Persona`: clase abstracta `@MappedSuperclass` con `idPersona`, nombre, apellido, email y teléfono.
- `Medico`: hereda de `Persona`, tiene especialidad y número de colegiado.
- `Paciente`: hereda de `Persona`, tiene fecha de nacimiento.
- `Especialidad`: catálogo de especialidades médicas.
- `Disponibilidad`: franja de un médico (`LocalDate`, `LocalTime` de inicio/fin).
- `Cita`: relaciona médico y paciente, con `LocalDateTime`, motivo y estado.
- `EstadoCita`: `PENDIENTE`, `CONFIRMADA`, `CANCELADA`.

Las relaciones `@ManyToOne` son `LAZY`. El estado de una cita se persiste con `@Enumerated(EnumType.STRING)`.

## Convenciones importantes

- **No usar `@Data` en entidades JPA.** Usar `@Getter`, `@Setter`, `@NoArgsConstructor` y `@AllArgsConstructor`; evita ciclos en `toString`, `equals` y `hashCode` por relaciones JPA.
- Mantener entidades en `model`, sin exponerlas directamente desde controladores. Crear DTOs para requests y responses.
- Validar los DTOs de entrada con anotaciones `jakarta.validation` y `@Valid` en el controlador.
- Declarar transformaciones en interfaces de `mapper` con `@Mapper(componentModel = "spring")`.
- Los procesadores Lombok, MapStruct y `lombok-mapstruct-binding` ya están configurados en `pom.xml`; no retirar esa configuración.
- Centralizar errores HTTP en `GlobalExceptionHandler`; conservar el contrato `ErrorDetails` para errores de API.

## Antes de entregar cambios

1. Ejecutar `mvn clean compile`.
2. Ejecutar `mvn test` cuando se agreguen o modifiquen pruebas.
3. Verificar que no se hayan introducido `@Data` en `model/`.
4. Revisar que los nuevos endpoints utilicen DTOs, validación y respuestas HTTP adecuadas.
