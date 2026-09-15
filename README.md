# Sistema de citas médicas

Backend para la gestión centralizada de pacientes, médicos, especialidades, disponibilidades y citas médicas. El objetivo es proporcionar una base mantenible para el agendamiento clínico, con reglas de dominio claras y una API REST preparada para crecer.

## Alcance funcional

- Registro y administración de médicos, pacientes y especialidades.
- Definición de franjas de disponibilidad por médico.
- Agendamiento, confirmación y cancelación de citas.
- Seguimiento del estado de una cita: `PENDIENTE`, `CONFIRMADA` o `CANCELADA`.

El modelo de dominio incluye las entidades `Medico`, `Paciente`, `Especialidad`, `Disponibilidad` y `Cita`. Las fechas y horas se representan con las clases de `java.time` para evitar ambigüedades de formato y zona horaria.

## Arquitectura y tecnologías

La aplicación sigue una arquitectura en capas, orientada a separar responsabilidades y facilitar las pruebas y la evolución del sistema:

```text
Controller → Service → Repository → Base de datos
                 ↑
          DTO / Mapper / Validación
```

- Java 25 (JDK activo del proyecto).
- Spring Boot 4.1.1.
- Maven.
- Spring Web MVC para la API REST.
- Spring Data JPA para persistencia.
- Bean Validation para validar solicitudes.
- Lombok para reducir código repetitivo.
- MapStruct para conversiones entre entidades y DTOs.

Las entidades JPA no utilizan `@Data`, evitando efectos no deseados en relaciones de persistencia. Los errores HTTP se centralizan con `@RestControllerAdvice` y el contrato `ErrorDetails`.

## Diagramas

Los diagramas UML y Entidad-Relación se incorporarán en `docs/` cuando estén disponibles. El modelo implementado corresponde a la estructura descrita en la sección de alcance funcional.

## Requisitos

- JDK 25.
- Maven 3.9 o superior, o el Maven Wrapper incluido en el repositorio.
- Una base de datos compatible con JPA cuando se habilite la configuración de persistencia del entorno.

## Compilación y ejecución

Desde la raíz del repositorio:

```bash
./mvnw clean compile
./mvnw spring-boot:run
```

También puede utilizarse Maven instalado en el sistema:

```bash
mvn clean compile
mvn spring-boot:run
```

## Rutas API previstas

La estructura de controladores está preparada, pero los endpoints aún no se han implementado. Las rutas iniciales previstas son:

| Método | Ruta | Propósito |
|---|---|---|
| `POST` | `/api/citas` | Crear una cita médica. |
| `PATCH` | `/api/citas/{id}/estado` | Confirmar o cancelar una cita. |
| `GET` | `/api/medicos/{id}/disponibilidad` | Consultar la disponibilidad de un médico. |
| `POST` | `/api/medicos` | Registrar un médico. |
| `POST` | `/api/pacientes` | Registrar un paciente. |

## Desarrollo

Consulta [CODEX.md](CODEX.md) para conocer la estructura de paquetes, convenciones JPA/Lombok/MapStruct y la lista de verificación para contribuciones.
