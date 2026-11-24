-- Datos iniciales ficticios para las fichas de prueba.
-- Los id se fijan a mano para que las fichas puedan referirse a ellos (por ejemplo, «el pedido 4 es de bruno»).

-- Usuarios de prueba (contraseñas ficticias, documentadas en el README):
--   ana   / ana12345    -> USER  («usuario A» de las fichas)
--   bruno / bruno12345  -> USER  («usuario B» de las fichas)
--   admin / admin12345  -> ADMIN
INSERT INTO usuarios (id, username, password_hash, nombre, email, rol) VALUES
    (1, 'ana',   '$2a$10$m.lc6rE0KFSEv7bUWNW5/.yeCX0YIElv7vxHbFBXDeWo1B/T0vPP.', 'Ana García',      'ana@example.com',   'USER'),
    (2, 'bruno', '$2a$10$6.dtP14t6DINRSZFq6zRBem0T1pPUKsBhQD625YrVsiE6jb5kJtEK', 'Bruno Martín',    'bruno@example.com', 'USER'),
    (3, 'admin', '$2a$10$Klp5kcwBOwCoFP4.mKjbueqmgdMN8a0O1FB5Gw77zNK1B/My3IkZy', 'Administración', 'admin@example.com', 'ADMIN');

INSERT INTO productos (id, nombre, descripcion, precio, stock) VALUES
    (1, 'Teclado mecánico',   'Teclado mecánico con interruptores marrones',  79.90, 25),
    (2, 'Ratón inalámbrico',  'Ratón óptico inalámbrico de 2,4 GHz',          24.50, 40),
    (3, 'Monitor 27"',        'Monitor IPS de 27 pulgadas y resolución QHD', 249.00, 10),
    (4, 'Auriculares',        'Auriculares de diadema con micrófono',         59.99, 30),
    (5, 'Webcam HD',          'Cámara web 1080p con micrófono integrado',     45.00, 15),
    (6, 'Disco SSD 1 TB',     'Unidad SSD NVMe de 1 TB',                      89.90, 20),
    (7, 'Alfombrilla',        'Alfombrilla de escritorio de tela',            12.00, 60),
    (8, 'Hub USB-C',          'Concentrador USB-C con 4 puertos',             34.95, 35);

-- Pedidos 1 a 3 de ana, pedidos 4 y 5 de bruno
INSERT INTO pedidos (id, usuario_id, fecha, estado, total) VALUES
    (1, 1, '2025-10-01 10:15:00+02', 'PENDIENTE', 103.90),
    (2, 1, '2025-09-20 18:40:00+02', 'ENVIADO',   249.00),
    (3, 1, '2025-09-05 09:00:00+02', 'CANCELADO',  45.00),
    (4, 2, '2025-10-02 12:30:00+02', 'PENDIENTE',  94.94),
    (5, 2, '2025-09-25 16:05:00+02', 'ENVIADO',   114.40);

INSERT INTO lineas_pedido (id, pedido_id, producto_id, cantidad, precio_unitario) VALUES
    (1, 1, 1, 1,  79.90),
    (2, 1, 7, 2,  12.00),
    (3, 2, 3, 1, 249.00),
    (4, 3, 5, 1,  45.00),
    (5, 4, 4, 1,  59.99),
    (6, 4, 8, 1,  34.95),
    (7, 5, 6, 1,  89.90),
    (8, 5, 2, 1,  24.50);

-- Como los id se han insertado a mano, se ajustan las secuencias para que
-- los registros nuevos continúen a partir del último id
SELECT setval(pg_get_serial_sequence('usuarios', 'id'),      (SELECT MAX(id) FROM usuarios));
SELECT setval(pg_get_serial_sequence('productos', 'id'),     (SELECT MAX(id) FROM productos));
SELECT setval(pg_get_serial_sequence('pedidos', 'id'),       (SELECT MAX(id) FROM pedidos));
SELECT setval(pg_get_serial_sequence('lineas_pedido', 'id'), (SELECT MAX(id) FROM lineas_pedido));
