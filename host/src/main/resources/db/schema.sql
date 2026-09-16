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
