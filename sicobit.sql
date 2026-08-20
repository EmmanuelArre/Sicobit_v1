DROP DATABASE IF EXISTS sicobit;
CREATE DATABASE sicobit;
USE sicobit;

-- grupo
CREATE TABLE grupo(
    idGrupo INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(20) NOT NULL UNIQUE
);

-- alumno
CREATE TABLE alumno(
    matricula VARCHAR(20) PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL,
    idGrupo INT NOT NULL,
    CONSTRAINT fk_alumno_grupo
        FOREIGN KEY(idGrupo)
        REFERENCES grupo(idGrupo)
);

-- laboratorio
CREATE TABLE laboratorio(
    idLaboratorio INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE,
    estatus VARCHAR(20) DEFAULT 'Disponible',
    capacidad INT NOT NULL DEFAULT 30,
    ubicacion VARCHAR(100) NOT NULL DEFAULT ''
);

CREATE TABLE equipo_computo(
    idEquipo INT AUTO_INCREMENT PRIMARY KEY,
    idLaboratorio INT NOT NULL,
    clave VARCHAR(30) NOT NULL,
    estatus VARCHAR(20) NOT NULL DEFAULT 'Disponible',
    CONSTRAINT fk_equipo_lab
        FOREIGN KEY(idLaboratorio)
        REFERENCES laboratorio(idLaboratorio)
        ON DELETE CASCADE,
    CONSTRAINT uq_equipo_lab_clave UNIQUE (idLaboratorio, clave)
);

-- bloque horario
CREATE TABLE bloque_horario(
    idBloque INT AUTO_INCREMENT PRIMARY KEY,
    horaInicio TIME NOT NULL,
    horaFin TIME NOT NULL
);

CREATE TABLE cuatrimestre(
    idCuatrimestre INT AUTO_INCREMENT PRIMARY KEY,
    periodo VARCHAR(20) NOT NULL,
    anio INT NOT NULL,
    activo TINYINT(1) NOT NULL DEFAULT 0,
    CONSTRAINT uq_cuatrimestre UNIQUE (periodo, anio)
);

-- profesor
CREATE TABLE profesor(
    idProfesor INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL,
    estatus VARCHAR(20) NOT NULL DEFAULT 'Activo'
);

-- materia
CREATE TABLE materia(
    idMateria INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL,
    estatus VARCHAR(20) NOT NULL DEFAULT 'Activa'
);

CREATE TABLE horario_clase(
    idHorario INT AUTO_INCREMENT PRIMARY KEY,
    idLaboratorio INT NOT NULL,
    idCuatrimestre INT NOT NULL,
    dia VARCHAR(15) NOT NULL,
    idBloque INT NOT NULL,
    idMateria INT NOT NULL,
    idProfesor INT NOT NULL,
    CONSTRAINT fk_horario_lab
        FOREIGN KEY(idLaboratorio) REFERENCES laboratorio(idLaboratorio),
    CONSTRAINT fk_horario_bloque
        FOREIGN KEY(idBloque) REFERENCES bloque_horario(idBloque),
    CONSTRAINT fk_horario_cuatrimestre
        FOREIGN KEY(idCuatrimestre) REFERENCES cuatrimestre(idCuatrimestre),
    CONSTRAINT fk_horario_materia
        FOREIGN KEY(idMateria) REFERENCES materia(idMateria),
    CONSTRAINT fk_horario_profesor
        FOREIGN KEY(idProfesor) REFERENCES profesor(idProfesor),
    CONSTRAINT uq_horario_ocupado UNIQUE (idLaboratorio, dia, idBloque, idCuatrimestre)
);

CREATE TABLE actividad(
    idActividad INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(80) NOT NULL UNIQUE,
    estatus VARCHAR(20) NOT NULL DEFAULT 'Activa'
);

CREATE TABLE registro(
    idRegistro INT AUTO_INCREMENT PRIMARY KEY,
    matricula VARCHAR(20) NOT NULL,
    idLaboratorio INT NOT NULL,
    idEquipo INT NULL,
    idActividad INT NOT NULL,
    idCuatrimestre INT NOT NULL,
    fecha DATE NOT NULL,
    dia VARCHAR(15) NOT NULL,
    idBloque INT NOT NULL,
    horaEntrada TIME,
    horaSalida TIME,
    tipo VARCHAR(20) DEFAULT 'Extraclase',
    estado VARCHAR(20) NOT NULL DEFAULT 'Activo',
    CONSTRAINT fk_registro_alumno
        FOREIGN KEY(matricula) REFERENCES alumno(matricula),
    CONSTRAINT fk_registro_lab
        FOREIGN KEY(idLaboratorio) REFERENCES laboratorio(idLaboratorio),
    CONSTRAINT fk_registro_equipo
        FOREIGN KEY(idEquipo) REFERENCES equipo_computo(idEquipo),
    CONSTRAINT fk_registro_actividad
        FOREIGN KEY(idActividad) REFERENCES actividad(idActividad),
    CONSTRAINT fk_registro_bloque
        FOREIGN KEY(idBloque) REFERENCES bloque_horario(idBloque),
    CONSTRAINT fk_registro_cuatrimestre
        FOREIGN KEY(idCuatrimestre) REFERENCES cuatrimestre(idCuatrimestre)
);

CREATE TABLE modulo(
    idModulo INT AUTO_INCREMENT PRIMARY KEY,
    clave VARCHAR(50) NOT NULL UNIQUE,
    nombre VARCHAR(100) NOT NULL
);

INSERT INTO modulo(clave, nombre) VALUES
('DASHBOARD','Dashboard'),
('LABORATORIOS','Laboratorios'),
('EQUIPO_COMPUTO','Equipo de cómputo'),
('HORARIOS_REGULARES','Horarios regulares'),
('BLOQUES_HORARIO','Bloques de horario'),
('CUATRIMESTRES','Cuatrimestres'),
('REGISTROS_EXTRACLASE','Registros extraclase'),
('GESTION_ALUMNOS','Gestión de alumnos'),
('PROFESORES','Profesores'),
('MATERIAS','Materias'),
('ACTIVIDADES','Actividades'),
('INCIDENCIAS','Incidencias'),
('REPORTES','Reportes'),
('EXPORTAR','Exportar'),
('RESPALDOS','Respaldos'),
('ADMINISTRADORES','Administradores y roles');

CREATE TABLE rol(
    idRol INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE,
    esSuperAdmin TINYINT(1) NOT NULL DEFAULT 0
);

INSERT INTO rol(nombre, esSuperAdmin) VALUES
('superadmin', 1),
('admin', 0);

CREATE TABLE rol_modulo(
    idRol INT NOT NULL,
    idModulo INT NOT NULL,
    PRIMARY KEY (idRol, idModulo),
    CONSTRAINT fk_rolmodulo_rol
        FOREIGN KEY(idRol) REFERENCES rol(idRol) ON DELETE CASCADE,
    CONSTRAINT fk_rolmodulo_modulo
        FOREIGN KEY(idModulo) REFERENCES modulo(idModulo) ON DELETE CASCADE
);

INSERT INTO rol_modulo(idRol, idModulo)
SELECT (SELECT idRol FROM rol WHERE nombre='admin'), idModulo
FROM modulo WHERE clave <> 'ADMINISTRADORES';

INSERT INTO rol_modulo(idRol, idModulo)
SELECT (SELECT idRol FROM rol WHERE nombre='superadmin'), idModulo
FROM modulo;

CREATE TABLE administrador(
    idAdministrador INT AUTO_INCREMENT PRIMARY KEY,
    usuario VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    nombre VARCHAR(150) NOT NULL,
    idRol INT NOT NULL,
    CONSTRAINT fk_administrador_rol
        FOREIGN KEY(idRol) REFERENCES rol(idRol)
);

INSERT INTO administrador(usuario, password, nombre, idRol)
VALUES (
    'admin',
    'admin123',
    'Super Administrador SICOBIT',
    (SELECT idRol FROM rol WHERE nombre = 'superadmin')
);


CREATE TABLE prestamo_equipo(
    idPrestamo INT AUTO_INCREMENT PRIMARY KEY,
    matricula VARCHAR(20) NOT NULL,
    idLaboratorio INT NOT NULL,
    idEquipo INT NOT NULL,
    fecha DATE NOT NULL,
    horaPrestamo TIME NOT NULL,
    horaDevolucion TIME NULL,
    observaciones VARCHAR(255),
    CONSTRAINT fk_prestamo_alumno
        FOREIGN KEY(matricula) REFERENCES alumno(matricula),
    CONSTRAINT fk_prestamo_lab
        FOREIGN KEY(idLaboratorio) REFERENCES laboratorio(idLaboratorio),
    CONSTRAINT fk_prestamo_equipo
        FOREIGN KEY(idEquipo) REFERENCES equipo_computo(idEquipo)
);

CREATE TABLE incidencia(
    idIncidencia INT AUTO_INCREMENT PRIMARY KEY,
    matricula VARCHAR(20) NOT NULL,
    idLaboratorio INT NOT NULL,
    idEquipo INT NULL,
    fecha DATE NOT NULL,
    hora TIME NOT NULL,
    tipo VARCHAR(50) NOT NULL,
    descripcion VARCHAR(500) NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'Pendiente',
    fechaResolucion DATE NULL,
    observacionesAdmin VARCHAR(500),
    CONSTRAINT fk_incidencia_alumno
        FOREIGN KEY(matricula) REFERENCES alumno(matricula),
    CONSTRAINT fk_incidencia_lab
        FOREIGN KEY(idLaboratorio) REFERENCES laboratorio(idLaboratorio),
    CONSTRAINT fk_incidencia_equipo
        FOREIGN KEY(idEquipo) REFERENCES equipo_computo(idEquipo)
);

-- respaldo
CREATE TABLE respaldo(
    idRespaldo INT AUTO_INCREMENT PRIMARY KEY,
    nombreArchivo VARCHAR(150) NOT NULL,
    rutaCompleta VARCHAR(400) NOT NULL,
    fecha DATE NOT NULL,
    hora TIME NOT NULL,
    tamanoBytes BIGINT NOT NULL DEFAULT 0,
    estado VARCHAR(20) NOT NULL DEFAULT 'Exitoso',
    tipo VARCHAR(20) NOT NULL DEFAULT 'Respaldo'
);

-- catálogos base útiles para pruebas
INSERT INTO cuatrimestre(periodo, anio, activo) VALUES
('Enero-Abril', 2026, 1);

INSERT INTO actividad(nombre) VALUES
('Tarea'),
('Proyecto'),
('Práctica'),
('Investigación');

INSERT INTO bloque_horario (horaInicio, horaFin) VALUES
('08:00:00', '08:50:00'),
('09:00:00', '09:50:00'),
('10:00:00', '10:50:00'),
('11:00:00', '11:50:00'),
('12:20:00', '13:10:00'),
('13:15:00', '14:05:00'),
('14:10:00', '15:00:00'),
('15:10:00', '16:00:00'),
('16:00:00', '17:00:00'),
('17:00:00', '18:00:00'),
('18:00:00', '19:00:00'),
('19:00:00', '20:00:00'),
('20:00:00', '21:00:00');

INSERT INTO equipo_computo (idLaboratorio, clave, estatus)
SELECT l.idLaboratorio, CONCAT('PC-', LPAD(n.n, 2, '0')), 'Disponible'
FROM laboratorio l
JOIN (
    WITH RECURSIVE seq(n) AS (
        SELECT 1
        UNION ALL
        SELECT n + 1 FROM seq WHERE n < 200
    )
    SELECT n FROM seq
) n ON n.n <= l.capacidad
WHERE NOT EXISTS (
    SELECT 1 FROM equipo_computo e
    WHERE e.idLaboratorio = l.idLaboratorio
      AND e.clave = CONCAT('PC-', LPAD(n.n, 2, '0'))
);