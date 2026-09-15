-- Esquema de la base de datos (SQLite)
-- Smart Parking Lot

CREATE TABLE IF NOT EXISTS config (
    key   TEXT PRIMARY KEY,
    value TEXT NOT NULL
);

-- Valores iniciales sugeridos (capacidad configurable, umbral de humo, estado)
INSERT OR IGNORE INTO config (key, value) VALUES
    ('total_capacity', '6'),
    ('smoke_threshold', '400'),
    ('system_state', 'OPERATIONAL');

CREATE TABLE IF NOT EXISTS visit (
    id         INTEGER PRIMARY KEY AUTOINCREMENT,
    vehicle_id TEXT,
    entry_time TEXT NOT NULL,
    exit_time  TEXT,
    status     TEXT NOT NULL DEFAULT 'ACTIVE'
);

CREATE TABLE IF NOT EXISTS security_event (
    id             INTEGER PRIMARY KEY AUTOINCREMENT,
    measured_level INTEGER NOT NULL,
    threshold      INTEGER NOT NULL,
    occurred_at    TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS system_event (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    event_type  TEXT NOT NULL,
    detail      TEXT,
    occurred_at TEXT NOT NULL
);
