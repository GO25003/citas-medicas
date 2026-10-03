# Sistema de citas médicas

Backend para la gestión centralizada de pacientes, médicos, especialidades, disponibilidades y citas médicas. El objetivo es proporcionar una base mantenible para el agendamiento clínico, con reglas de dominio claras y una API REST preparada para crecer.

## Alcance funcional

- Registro y administración de médicos, pacientes y especialidades.
- Definición de franjas de disponibilidad por médico.
- Agendamiento, confirmación y cancelación de citas.
- Seguimiento del estado de una cita: `PENDIENTE`, `CONFIRMADA` o `CANCELADA`.

El modelo de dominio incluye las entidades `Medico`, `Paciente`, `Especialidad`, `Disponibilidad` y `Cita`. Un médico puede ejercer una o más especialidades; una cita indica la especialidad solicitada, su hora de inicio y su duración. Las fechas y horas se representan con las clases de `java.time` para evitar ambigüedades de formato y zona horaria.

## Reglas de negocio

- Una cita solo puede solicitarse para una especialidad asignada al médico seleccionado.
- La hora de inicio y el intervalo completo de la cita deben estar dentro de una disponibilidad vigente del médico.
- Un médico no puede tener citas `PENDIENTE` o `CONFIRMADA` con intervalos de tiempo que se crucen.
- La cancelación se representa mediante el estado `CANCELADA`, preservando el historial de la cita.

Estas validaciones forman parte de la lógica de servicio y serán aplicadas al implementar las operaciones de agendamiento y reprogramación.

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

El directorio `docs/` ya está incluido en el repositorio y está reservado para los diagramas UML y Entidad-Relación. El modelo implementado corresponde a la estructura descrita en la sección de alcance funcional.

## Requisitos

- Docker Engine y Docker Compose.
- Para compilar o ejecutar fuera de Docker: JDK 25 y Maven 3.9 o el Maven Wrapper incluido.

## Entorno con Docker Compose

La forma recomendada de levantar el entorno de desarrollo es ejecutar desde la raíz del repositorio:

```bash
cp .env.example .env
docker compose up --build
```

El archivo `.env` contiene la configuración local de conexión y está excluido de Git. `.env.example` contiene valores de ejemplo; se pueden ajustar en `.env`. Compose también tiene valores de desarrollo predeterminados si se ejecuta sin crear `.env`.

La aplicación Spring Boot queda disponible en `http://localhost:8080` y PostgreSQL en el puerto `5432`. PostgreSQL inicializa las tablas desde `database/init.sql` cuando crea por primera vez el volumen de datos.

### Después de hacer cambios

- **Cambios en el código Java o configuración incluida en la imagen:** reconstruir y levantar con `docker compose up --build`. Compose recrea el contenedor de la aplicación; el volumen de PostgreSQL se conserva.
- **Cambios en `.env`:** ejecutar `docker compose up -d` para recrear los servicios con las nuevas variables. Si también cambió el código, usar `docker compose up --build`.
- **Cambios en `database/init.sql`:** el script solo se ejecuta automáticamente cuando PostgreSQL inicializa un volumen vacío. Para aplicar cambios a una base existente, gestionar la migración o reinicializar el volumen en desarrollo.
- **Detener el entorno conservando la base de datos:** `docker compose down`.
- **Borrar también los datos de PostgreSQL:** `docker compose down -v` y luego `docker compose up --build`. Elimina permanentemente el volumen y todos sus datos; úsalo solo cuando quieras empezar con una base vacía.

Los valores predeterminados son solo para desarrollo local. No guardar credenciales reales en el repositorio ni reutilizar esos valores fuera de este entorno.

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

## Estado de la API

Los controladores base y sus prefijos están creados, pero todavía no contienen métodos HTTP; por tanto, **ninguna ruta expone operaciones funcionales**. Las siguientes son las rutas previstas para la implementación:

| Método | Ruta | Propósito |
|---|---|---|
| `POST` | `/api/citas` | Crear una cita médica. |
| `PATCH` | `/api/citas/{id}/estado` | Confirmar o cancelar una cita. |
| `GET` | `/api/medicos/{id}/disponibilidad` | Consultar la disponibilidad de un médico. |
| `POST` | `/api/medicos` | Registrar un médico. |
| `POST` | `/api/pacientes` | Registrar un paciente. |
| `POST` | `/api/especialidades` | Registrar una especialidad. |

## Estado de la plantilla

El repositorio incluye esqueletos compilables de controladores, servicios, implementaciones de servicio, repositorios JPA, DTOs y mappers MapStruct. Los DTOs y mappers aún no definen campos ni conversiones, y los servicios todavía no aplican las reglas de negocio descritas arriba. Esta estructura permite que cada contribución se incorpore en su capa correspondiente sin redefinir la organización del proyecto.

## Desarrollo

Consulta [CODEX.md](CODEX.md) para conocer la estructura de paquetes, convenciones JPA/Lombok/MapStruct y la lista de verificación para contribuciones.
