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

## Fuente de verdad y responsabilidades

- El UML define el modelo conceptual y las relaciones del dominio.

- La arquitectura por capas define dónde se implementan las operaciones de la aplicación.

- Los métodos mostrados en el UML representan responsabilidades del dominio,
pero su implementación debe respetar la separación de capas del proyecto.

- Las operaciones que requieran acceso a repositorios, validaciones entre
múltiples entidades o coordinación de casos de uso deben implementarse
en la capa Service.

- No mover lógica entre capas únicamente para hacer coincidir literalmente
el código con la firma de un método del UML.

## Regla para agentes de IA

Antes de crear una clase, interfaz, dependencia, paquete o patrón nuevo:

- 1. Revisar si ya existe una solución equivalente.
- 2. Revisar README.md, CODEX.md y docs/UML.puml.
- 3. Mantener la arquitectura existente.
- 4. No introducir patrones adicionales sin necesidad.
- 5. No modificar el UML para justificar una implementación.
- 6. Si existe una contradicción entre documentación y código, señalarla
   antes de realizar cambios estructurales.

## Convenciones importantes

- **No usar `@Data` en entidades JPA.** Usar `@Getter`, `@Setter`, `@NoArgsConstructor` y `@AllArgsConstructor`; evita ciclos en `toString`, `equals` y `hashCode` por relaciones JPA.
- Mantener entidades en `model`, sin exponerlas directamente desde controladores. Crear DTOs para requests y responses.
- Al definir DTOs de entrada, validarlos con anotaciones `jakarta.validation` y `@Valid` en el controlador.
- Declarar transformaciones en interfaces de `mapper` con `@Mapper(componentModel = "spring")`.
- Los procesadores Lombok, MapStruct y `lombok-mapstruct-binding` ya están configurados en `pom.xml`; no retirar esa configuración.
- Centralizar errores HTTP en `GlobalExceptionHandler`; conservar el contrato `ErrorDetails` para errores de API.

## Contenerización y entorno de desarrollo

- Ejecutar la aplicación mediante Docker Compose desde el inicio del desarrollo.
- Contenerizar la aplicación Spring Boot y la base de datos.
- Cualquier integrante debe poder clonar el repositorio y levantar el sistema completo con `docker compose up --build`.
- No depender de instalaciones locales específicas de Java, Maven o base de datos.
- Mantener un entorno reproducible entre integrantes.

### Configuración

- Manejar toda configuración sensible mediante variables de entorno.
- Nunca guardar credenciales reales dentro del código ni subir archivos `.env` al repositorio.
- Mantener un archivo `.env.example` con las variables requeridas y actualizar `.gitignore` para excluir archivos sensibles.

### Base de datos

- Seguir únicamente el diagrama ER aprobado dentro de `docs/` para la estructura de la base de datos.
- Versionar dentro del repositorio los scripts de creación e inicialización de la base de datos.
- No crear tablas, columnas ni relaciones que no estén documentadas.

### Docker

- Mantener un `Dockerfile` para la aplicación Spring Boot y `docker-compose.yml` como punto único para levantar aplicación y base de datos.
- No agregar configuraciones Docker innecesarias.
- Asegurar que los cambios funcionen dentro del entorno contenerizado.

### Maven y validaciones

- Docker no reemplaza las validaciones Maven.
- Antes de entregar cambios, ejecutar `./mvnw clean compile` y verificar pruebas con `./mvnw test`.
- Antes de finalizar cambios, ejecutar `docker compose up --build` y confirmar que la aplicación inicia correctamente dentro del entorno contenerizado.

### Reglas para agentes de IA

- Antes de modificar código, revisar `CODEX.md`, `README.md`, `docs/` y el código existente relacionado.
- No crear tecnologías, dependencias o patrones sin aprobación.
- No modificar nombres de entidades, atributos o relaciones definidos en UML/ER.
- No cambiar la estructura de carpetas sin necesidad.
- Priorizar soluciones simples, mantenibles y compatibles con la arquitectura existente.

## Antes de entregar cambios

1. Ejecutar `mvn clean compile`.
2. Ejecutar `mvn test` cuando se agreguen o modifiquen pruebas.
3. Verificar que no se hayan introducido `@Data` en `model/`.
4. Revisar que los nuevos endpoints utilicen DTOs, validación y respuestas HTTP adecuadas.
