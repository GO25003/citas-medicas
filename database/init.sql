
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

-- Datos de prueba para desarrollo local. Se cargan únicamente cuando PostgreSQL
-- inicializa un volumen nuevo. Los IDs quedan consecutivos desde 1.
INSERT INTO especialidad (nombre, descripcion) VALUES
    ('Medicina General', 'Atención primaria y evaluación integral'),
    ('Cardiología', 'Prevención y tratamiento de enfermedades cardiovasculares'),
    ('Dermatología', 'Diagnóstico y tratamiento de afecciones de la piel'),
    ('Pediatría', 'Atención médica de niñas, niños y adolescentes'),
    ('Ginecología', 'Salud reproductiva y atención ginecológica'),
    ('Oftalmología', 'Evaluación y cuidado de la salud visual');

INSERT INTO medico (nombre, apellido, email, telefono, numero_colegiado) VALUES
    ('Ana', 'Martínez', 'ana.martinez@clinica.test', '7000-1001', 'COL-1001'),
    ('Carlos', 'Hernández', 'carlos.hernandez@clinica.test', '7000-1002', 'COL-1002'),
    ('Beatriz', 'López', 'beatriz.lopez@clinica.test', '7000-1003', 'COL-1003'),
    ('José', 'Ramírez', 'jose.ramirez@clinica.test', '7000-1004', 'COL-1004'),
    ('María', 'García', 'maria.garcia@clinica.test', '7000-1005', 'COL-1005'),
    ('Luis', 'Flores', 'luis.flores@clinica.test', '7000-1006', 'COL-1006'),
    ('Claudia', 'Rivera', 'claudia.rivera@clinica.test', '7000-1007', 'COL-1007'),
    ('Roberto', 'Díaz', 'roberto.diaz@clinica.test', '7000-1008', 'COL-1008'),
    ('Sofía', 'Castro', 'sofia.castro@clinica.test', '7000-1009', 'COL-1009'),
    ('Miguel', 'Morales', 'miguel.morales@clinica.test', '7000-1010', 'COL-1010'),
    ('Elena', 'Reyes', 'elena.reyes@clinica.test', '7000-1011', 'COL-1011'),
    ('Daniel', 'Cruz', 'daniel.cruz@clinica.test', '7000-1012', 'COL-1012');

INSERT INTO paciente (nombre, apellido, email, telefono, fecha_nacimiento) VALUES
    ('Laura', 'Gómez', 'laura.gomez@paciente.test', '7100-2001', '1990-04-12'),
    ('Pedro', 'Vásquez', 'pedro.vasquez@paciente.test', '7100-2002', '1985-09-23'),
    ('Gabriela', 'Méndez', 'gabriela.mendez@paciente.test', '7100-2003', '2001-02-08'),
    ('Andrés', 'Pérez', 'andres.perez@paciente.test', '7100-2004', '1978-11-17'),
    ('Valeria', 'Chávez', 'valeria.chavez@paciente.test', '7100-2005', '1995-06-30'),
    ('Diego', 'Renderos', 'diego.renderos@paciente.test', '7100-2006', '2010-03-14'),
    ('Patricia', 'Aguilar', 'patricia.aguilar@paciente.test', '7100-2007', '1969-12-05'),
    ('Fernando', 'Alvarado', 'fernando.alvarado@paciente.test', '7100-2008', '1988-07-21'),
    ('Isabel', 'Mejía', 'isabel.mejia@paciente.test', '7100-2009', '1998-01-19'),
    ('Óscar', 'Guardado', 'oscar.guardado@paciente.test', '7100-2010', '1974-05-11'),
    ('Camila', 'Portillo', 'camila.portillo@paciente.test', '7100-2011', '2005-10-02'),
    ('Javier', 'Rivas', 'javier.rivas@paciente.test', '7100-2012', '1992-08-26');

INSERT INTO medico_especialidad (id_medico, id_especialidad) VALUES
    (1, 1), (2, 2), (3, 3), (4, 4), (5, 5), (6, 6),
    (7, 1), (8, 2), (9, 3), (10, 4), (11, 5), (12, 6);

-- Una disponibilidad futura por médico, con espacio para una cita de 30 minutos.
INSERT INTO disponibilidad (id_medico, fecha, hora_inicio, hora_fin, disponible)
SELECT n, CURRENT_DATE + n, TIME '08:00', TIME '12:00', TRUE
FROM generate_series(1, 12) AS n;

-- Citas sin cruces: cada médico y paciente tiene una cita en un día distinto.
INSERT INTO cita (id_medico, id_paciente, id_especialidad, fecha_hora_inicio,
                  duracion_minutos, motivo, estado)
SELECT n, n, ((n - 1) % 6) + 1,
       (CURRENT_DATE + n) + TIME '09:00', 30,
       'Consulta de prueba ' || n,
       CASE WHEN n % 3 = 0 THEN 'CONFIRMADA' ELSE 'PENDIENTE' END
FROM generate_series(1, 12) AS n;

-- Alinear las secuencias de identidad con los registros precargados.
SELECT setval(pg_get_serial_sequence('especialidad', 'id_especialidad'), (SELECT MAX(id_especialidad) FROM especialidad));
SELECT setval(pg_get_serial_sequence('medico', 'id_medico'), (SELECT MAX(id_medico) FROM medico));
SELECT setval(pg_get_serial_sequence('paciente', 'id_paciente'), (SELECT MAX(id_paciente) FROM paciente));
SELECT setval(pg_get_serial_sequence('disponibilidad', 'id_disponibilidad'), (SELECT MAX(id_disponibilidad) FROM disponibilidad));
SELECT setval(pg_get_serial_sequence('cita', 'id_cita'), (SELECT MAX(id_cita) FROM cita));
