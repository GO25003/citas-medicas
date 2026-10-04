
-- 1. Tabla: medico
CREATE TABLE medico (
    id_medico BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY, -- Long en JPA; Persona es @MappedSuperclass
    nombre VARCHAR(100) NOT NULL, --
    apellido VARCHAR(100) NOT NULL, --
    email VARCHAR(150) UNIQUE NOT NULL, --
    telefono VARCHAR(20), --
    numero_colegiado VARCHAR(50) UNIQUE NOT NULL --
);

-- 2. Tabla: paciente
CREATE TABLE paciente (
    id_paciente BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY, -- Long en JPA; Persona es @MappedSuperclass
    nombre VARCHAR(100) NOT NULL, --
    apellido VARCHAR(100) NOT NULL, --
    email VARCHAR(150) UNIQUE NOT NULL, --
    telefono VARCHAR(20), --
    fecha_nacimiento DATE NOT NULL --
);

-- 3. Tabla: especialidad
CREATE TABLE especialidad (
    id_especialidad BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY, -- Long en JPA
    nombre VARCHAR(100) UNIQUE NOT NULL, --
    descripcion TEXT --
);

-- 4. Tabla asociativa: medico_especialidad (Relación N:M)
CREATE TABLE medico_especialidad (
    id_medico BIGINT NOT NULL, --
    id_especialidad BIGINT NOT NULL, --
    PRIMARY KEY (id_medico, id_especialidad),
    CONSTRAINT fk_medicoespecialidad_medico FOREIGN KEY (id_medico)
        REFERENCES medico(id_medico) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_medicoespecialidad_especialidad FOREIGN KEY (id_especialidad)
        REFERENCES especialidad(id_especialidad) ON DELETE RESTRICT ON UPDATE CASCADE
);

-- 5. Tabla: disponibilidad (Relación 1:N con medico)
CREATE TABLE disponibilidad (
    id_disponibilidad BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY, -- Long en JPA
    id_medico BIGINT NOT NULL, --
    fecha DATE NOT NULL, --
    hora_inicio TIME NOT NULL, --
    hora_fin TIME NOT NULL, --
    disponible BOOLEAN NOT NULL DEFAULT TRUE, --
    CONSTRAINT fk_disponibilidad_medico FOREIGN KEY (id_medico)
        REFERENCES medico(id_medico) ON DELETE RESTRICT ON UPDATE CASCADE
);

-- 6. Tabla: cita (Relaciones 1:N con medico, paciente y especialidad)
CREATE TABLE cita (
    id_cita BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY, -- Long en JPA
    id_medico BIGINT NOT NULL, --
    id_paciente BIGINT NOT NULL, --
    id_especialidad BIGINT NOT NULL, --
    fecha_hora_inicio TIMESTAMP NOT NULL, --
    duracion_minutos INT NOT NULL, --
    motivo TEXT, --
    estado VARCHAR(50) NOT NULL DEFAULT 'PENDIENTE', -- Valor de EstadoCita
    CONSTRAINT fk_cita_medico FOREIGN KEY (id_medico)
        REFERENCES medico(id_medico) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_cita_paciente FOREIGN KEY (id_paciente)
        REFERENCES paciente(id_paciente) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_cita_especialidad FOREIGN KEY (id_especialidad)
        REFERENCES especialidad(id_especialidad) ON DELETE RESTRICT ON UPDATE CASCADE
);
