CREATE TABLE IF NOT EXISTS configuracion (
    clave TEXT PRIMARY KEY,
    valor TEXT NOT NULL
);
INSERT OR IGNORE INTO configuracion (clave, valor) VALUES
    ('capacidad_total', '6'),
    ('umbral_humo', '400'),
    ('estado_sistema', 'OPERATIVO');

CREATE TABLE IF NOT EXISTS vehiculo (
    placa          TEXT PRIMARY KEY,
    tipo_vehiculo  TEXT,
    fecha_registro TEXT
);

CREATE TABLE IF NOT EXISTS registro_acceso (
    id            INTEGER PRIMARY KEY AUTOINCREMENT,
    placa         TEXT,
    hora_entrada  TEXT NOT NULL,
    hora_salida   TEXT,
    estado_visita TEXT NOT NULL DEFAULT 'ACTIVO'
);

CREATE TABLE IF NOT EXISTS evento_seguridad (
    id                  INTEGER PRIMARY KEY AUTOINCREMENT,
    nivel_gas           INTEGER NOT NULL,
    umbral              INTEGER NOT NULL,
    timestamp           TEXT NOT NULL,
    requiere_evacuacion INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS evento_sistema (
    id        INTEGER PRIMARY KEY AUTOINCREMENT,
    tipo      TEXT NOT NULL,
    detalle   TEXT,
    timestamp TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS usuario (
    id                 TEXT PRIMARY KEY,
    username           TEXT NOT NULL UNIQUE,
    email              TEXT NOT NULL UNIQUE,
    password_hash      TEXT NOT NULL,
    salt               TEXT NOT NULL,
    activo             INTEGER NOT NULL DEFAULT 1,
    creado_en          TEXT NOT NULL,
    clave_cambiada_en  TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS rol (
    nombre TEXT PRIMARY KEY
);

CREATE TABLE IF NOT EXISTS permiso (
    codigo TEXT PRIMARY KEY
);

CREATE TABLE IF NOT EXISTS rol_permiso (
    rol_nombre     TEXT NOT NULL,
    permiso_codigo TEXT NOT NULL,
    PRIMARY KEY (rol_nombre, permiso_codigo)
);

CREATE TABLE IF NOT EXISTS usuario_rol (
    usuario_id TEXT NOT NULL,
    rol_nombre TEXT NOT NULL,
    PRIMARY KEY (usuario_id, rol_nombre)
);

INSERT OR IGNORE INTO permiso (codigo) VALUES
    ('BARRERA_CONTROL'),
    ('EMERGENCIA_CONTROL'),
    ('CONFIG_UPDATE'),
    ('CUPOS_SYNC');

INSERT OR IGNORE INTO rol (nombre) VALUES ('ADMIN'), ('OPERADOR'), ('CONSULTA');

INSERT OR IGNORE INTO rol_permiso (rol_nombre, permiso_codigo) VALUES
    ('ADMIN', 'BARRERA_CONTROL'),
    ('ADMIN', 'EMERGENCIA_CONTROL'),
    ('ADMIN', 'CONFIG_UPDATE'),
    ('ADMIN', 'CUPOS_SYNC'),
    ('OPERADOR', 'BARRERA_CONTROL'),
    ('OPERADOR', 'EMERGENCIA_CONTROL'),
    ('OPERADOR', 'CUPOS_SYNC');
