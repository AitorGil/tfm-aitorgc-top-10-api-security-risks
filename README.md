# Desarrollo seguro de APIs con Spring Boot frente al OWASP API Security Top 10 (2023)

Repositorio del Trabajo de Fin de Máster de Aitor Gil Callejo. Es una guía práctica para desarrolladores con conocimientos básicos de Java y Spring Boot, pero sin formación en seguridad.

Para cada riesgo seleccionado del [OWASP API Security Top 10 (2023)](https://owasp.org/API-Security/editions/2023/en/0x11-t10/) hay dos ejemplos ejecutables:

- **vulnerable**: la aplicación base con el fallo introducido;
- **corregido**: el ejemplo vulnerable con la corrección aplicada.

Los dos parten de una **aplicación base** común: una API REST de gestión de pedidos sin vulnerabilidades intencionadas. Un `diff` entre las carpetas `vulnerable/` y `corregido/` de un caso muestra solo la corrección.

> **Aviso.** Los ejemplos vulnerables contienen fallos de seguridad a propósito. Ejecútalos solo en tu equipo y nunca los despliegues en un entorno accesible desde fuera. Todas las claves y contraseñas del repositorio son ficticias.

## Requisitos previos

- [Docker](https://docs.docker.com/get-docker/) con Docker Compose v2 (`docker compose`).
- Un cliente HTTP: `curl` o [Postman](https://www.postman.com/) (cada caso incluye una colección).
- Opcional, solo para compilar fuera de Docker: JDK 21 y Maven 3.9 o posterior.

No hace falta instalar Java ni PostgreSQL: Docker Compose compila la aplicación y arranca la base de datos.

## Estructura

```
/
├── base/                 # Aplicación base de referencia (sin vulnerabilidades)
├── postman/              # Colección de Postman con todos los endpoints de la base
├── api1-bola/            # Un directorio por cada riesgo seleccionado
│   ├── README.md         # Descripción del caso, arranque y pasos de la ficha de prueba
│   ├── postman/          # Colección de Postman con las peticiones de la ficha
│   ├── vulnerable/       # Proyecto Spring Boot independiente
│   └── corregido/        # Proyecto Spring Boot independiente
└── …
```

Cada carpeta `base/`, `*/vulnerable/` y `*/corregido/` es un proyecto Maven independiente, con su propio `pom.xml`, `Dockerfile` y `docker-compose.yml`.

## Casos

| Riesgo | Carpeta | Estado |
|---|---|---|
| API1:2023 Broken Object Level Authorization | `api1-bola/` | Pendiente |
| API2:2023 Broken Authentication | — | Pendiente |
| API3:2023 Broken Object Property Level Authorization | — | Pendiente |
| API4:2023 Unrestricted Resource Consumption | — | Pendiente |
| API5:2023 Broken Function Level Authorization | — | Pendiente |
| API6:2023 Unrestricted Access to Sensitive Business Flows | — | Pendiente |
| API8:2023 Security Misconfiguration | — | Pendiente |

## Cómo arrancar un proyecto

Desde la carpeta del proyecto (`base/`, o la carpeta `vulnerable/` o `corregido/` de un caso):

```bash
docker compose up --build
```

La API queda disponible en `http://localhost:8080`. Para comprobar que está en marcha:

```bash
curl http://localhost:8080/actuator/health
# {"groups":["liveness","readiness"],"status":"UP"}
```

Para pararla:

```bash
docker compose down
```

Notas:

- **Los datos vuelven siempre al estado inicial.** PostgreSQL guarda los datos en memoria, sin volumen persistente, y Flyway carga los datos iniciales en cada arranque. Después de `docker compose down`, el siguiente arranque empieza de cero.
- **Todos los proyectos usan el puerto 8080**, así que se ejecutan de uno en uno. Para el proyecto que esté en marcha con `docker compose down` antes de arrancar otro.
- PostgreSQL no se expone fuera de la red de Docker.
- Cada `docker-compose.yml` fija el nombre del proyecto de Docker Compose (`name:`): `base` para la aplicación base y el nombre de la carpeta del caso (por ejemplo, `api1-bola`) para sus dos versiones. Las versiones vulnerable y corregida de un caso comparten proyecto, así que al arrancar una se sustituyen los contenedores de la otra. Para ver qué proyecto está en marcha: `docker compose ls`.

## La aplicación base

API REST de gestión de pedidos con Spring Boot 4.2, Spring Security 7 (servidor de recursos OAuth 2.0 con JWT), Spring Data JPA, PostgreSQL 18 y Flyway. Es la referencia de lo que se considera «correcto» en la guía.

### Usuarios de prueba

| Usuario | Contraseña | Rol | En las fichas |
|---|---|---|---|
| `ana` | `ana12345` | USER | Usuario A |
| `bruno` | `bruno12345` | USER | Usuario B |
| `admin` | `admin12345` | ADMIN | Administrador |

Las contraseñas se guardan en la base de datos como hash BCrypt.

### Datos iniciales

- **Productos**: 8 productos ficticios, con id del 1 al 8.
- **Pedidos**:

| Pedido | Propietario | Estado | Total |
|---|---|---|---|
| 1 | ana | PENDIENTE | 103,90 |
| 2 | ana | ENVIADO | 249,00 |
| 3 | ana | CANCELADO | 45,00 |
| 4 | bruno | PENDIENTE | 94,94 |
| 5 | bruno | ENVIADO | 114,40 |

### Endpoints

| Método y ruta | Descripción | Acceso |
|---|---|---|
| `POST /api/auth/login` | Inicia sesión y devuelve un JWT. Tras 5 intentos fallidos seguidos, el usuario queda bloqueado 15 minutos | Público |
| `GET /api/productos` | Lista productos, con paginación (`?page=0&size=20&sort=precio,desc`, máximo 50 por página) | USER, ADMIN |
| `GET /api/productos/{id}` | Consulta un producto | USER, ADMIN |
| `POST /api/productos` | Crea un producto | ADMIN |
| `PUT /api/productos/{id}` | Modifica un producto | ADMIN |
| `DELETE /api/productos/{id}` | Elimina un producto que no aparezca en ningún pedido | ADMIN |
| `GET /api/pedidos` | Lista los pedidos del usuario autenticado, con paginación, del más reciente al más antiguo | USER, ADMIN |
| `GET /api/pedidos/{id}` | Consulta un pedido | Propietario |
| `POST /api/pedidos` | Crea un pedido para el usuario autenticado | USER, ADMIN |
| `POST /api/pedidos/{id}/cancelacion` | Cancela un pedido pendiente y devuelve las unidades al stock | Propietario |
| `GET /api/usuarios/me` | Perfil del usuario autenticado | USER, ADMIN |
| `PUT /api/usuarios/me` | Modifica el nombre y el email del usuario autenticado | USER, ADMIN |
| `GET /api/admin/usuarios` | Lista todos los usuarios, con paginación, ordenados por id | ADMIN |
| `GET /actuator/health` | Estado de la aplicación | Público |

Cualquier otra ruta o método se rechaza (denegación por defecto).

**Paginación.** Todos los listados admiten `?page=` (empieza en 0) y `?size=` (20 por defecto, 50 como máximo; si se pide más, se aplican 50). Solo el catálogo de productos admite además `?sort=campo,asc|desc`. En pedidos y usuarios el orden lo fija el servidor, para que nadie pueda ordenar por campos internos, como el hash de la contraseña. La respuesta tiene este formato:

```json
{ "contenido": [ … ], "pagina": 0, "tamano": 20, "totalElementos": 3, "totalPaginas": 1 }
```

### Probar la API con `curl`

**1. Iniciar sesión** y guardar el token en una variable:

```bash
curl -s -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"ana","password":"ana12345"}'
# {"token":"eyJ...","tipo":"Bearer","expiraEnSegundos":1800}

TOKEN_ANA=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"ana","password":"ana12345"}' | sed 's/.*"token":"\([^"]*\)".*/\1/')
```

El token caduca a los 30 minutos. Después hay que volver a iniciar sesión.

**2. Usar el token** en la cabecera `Authorization`:

```bash
# Catálogo, segunda página de 3 productos
curl -s "http://localhost:8080/api/productos?page=1&size=3" -H "Authorization: Bearer $TOKEN_ANA"

# Pedidos de ana, de 2 en 2
curl -s "http://localhost:8080/api/pedidos?page=0&size=2" -H "Authorization: Bearer $TOKEN_ANA"

# Un pedido de ana: 200
curl -s http://localhost:8080/api/pedidos/1 -H "Authorization: Bearer $TOKEN_ANA"

# Un pedido de bruno: 404, igual que si no existiera
curl -s http://localhost:8080/api/pedidos/4 -H "Authorization: Bearer $TOKEN_ANA"

# Crear un pedido: el precio, el total y el estado los pone el servidor
curl -s -X POST http://localhost:8080/api/pedidos \
  -H "Authorization: Bearer $TOKEN_ANA" -H 'Content-Type: application/json' \
  -d '{"lineas":[{"productoId":2,"cantidad":1},{"productoId":7,"cantidad":3}]}'

# Cancelar un pedido pendiente
curl -s -X POST http://localhost:8080/api/pedidos/1/cancelacion -H "Authorization: Bearer $TOKEN_ANA"

# Perfil propio
curl -s http://localhost:8080/api/usuarios/me -H "Authorization: Bearer $TOKEN_ANA"
curl -s -X PUT http://localhost:8080/api/usuarios/me \
  -H "Authorization: Bearer $TOKEN_ANA" -H 'Content-Type: application/json' \
  -d '{"nombre":"Ana García","email":"ana.garcia@example.com"}'

# Ruta de administración con un usuario normal: 403
curl -s http://localhost:8080/api/admin/usuarios -H "Authorization: Bearer $TOKEN_ANA"
```

**3. Operaciones de administración** con el token de `admin`:

```bash
TOKEN_ADMIN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"admin12345"}' | sed 's/.*"token":"\([^"]*\)".*/\1/')

curl -s http://localhost:8080/api/admin/usuarios -H "Authorization: Bearer $TOKEN_ADMIN"

curl -s -X POST http://localhost:8080/api/productos \
  -H "Authorization: Bearer $TOKEN_ADMIN" -H 'Content-Type: application/json' \
  -d '{"nombre":"Lámpara LED","descripcion":"Lámpara de escritorio","precio":19.99,"stock":10}'
```

### Probar la API con Postman

La colección `postman/pedidos-base.postman_collection.json` incluye todos los endpoints de la base, con los casos correctos y los de error (37 peticiones). Para usarla:

1. En Postman, **Import** y selecciona el fichero.
2. Ejecuta primero la carpeta **0. Autenticación**: guarda los tokens de ana, bruno y admin en las variables de la colección.
3. Ejecuta el resto de carpetas en orden, o la colección entera con el *Collection Runner*. El nombre de cada petición indica el código HTTP esperado.

Los tokens caducan a los 30 minutos: si empiezan a aparecer respuestas 401, repite la carpeta 0. La URL de la API está en la variable de colección `baseUrl` (`http://localhost:8080`).

### Formato de los errores

Todos los errores siguen el formato estándar *Problem Details* (RFC 9457, `application/problem+json`), sin trazas ni detalles internos:

```json
{ "title": "Not Found", "status": 404, "detail": "Pedido no encontrado" }
```

Los errores de validación incluyen además los campos que fallan:

```json
{
  "title": "Bad Request",
  "status": 400,
  "detail": "La petición contiene datos no válidos",
  "errores": { "email": "debe ser una dirección de correo electrónico con formato correcto" }
}
```

| Código | Cuándo se devuelve |
|---|---|
| 400 | Datos de entrada no válidos, JSON mal formado o parámetro con formato incorrecto |
| 401 | Sin token, con un token no válido o caducado, o credenciales incorrectas en el login |
| 403 | Usuario autenticado sin permiso para la ruta, o ruta no prevista |
| 404 | El recurso no existe o pertenece a otro usuario |
| 409 | La operación no es posible en el estado actual (cancelar un pedido enviado, stock insuficiente…) o otra petición ha modificado los mismos datos a la vez |
| 415 | El cuerpo no se envía como `application/json` |
| 429 | Demasiados intentos de login fallidos; la cabecera `Retry-After` indica cuántos segundos esperar |

### Medidas de seguridad de la base

- Contraseñas guardadas con BCrypt.
- JWT firmado (HS256), con caducidad de 30 minutos, validado en cada petición.
- Límite de intentos de login: tras 5 fallos seguidos, el usuario queda bloqueado 15 minutos. El contador está en memoria, así que se reinicia al reiniciar la aplicación.
- Reglas de acceso para todos los endpoints y denegación por defecto del resto.
- Comprobación del propietario en todas las operaciones sobre pedidos, con respuesta 404 si el pedido es de otro usuario.
- DTO de entrada y salida que limitan los campos visibles y modificables. El rol, el estado y el total nunca se reciben del cliente.
- Validación de la entrada y paginación de todos los listados, con un tamaño máximo de página y en la base de datos (nunca en memoria).
- Bloqueo optimista (`@Version`) en los productos, para que dos pedidos simultáneos no dejen el stock mal.
- Errores sin trazas ni detalles internos, en un formato JSON homogéneo.
- Solo `/actuator/health` expuesto.
- API sin estado: sin sesiones y sin CSRF, porque solo se autentica con *Bearer token*.

### Compilar fuera de Docker (opcional)

Con JDK 21 y Maven:

```bash
cd base
mvn package
```

Para ejecutar el JAR fuera de Docker hace falta un PostgreSQL en `localhost:5432` (base de datos, usuario y contraseña en `src/main/resources/application.yml`) y la variable de entorno `JWT_SECRETO` con al menos 32 caracteres.
