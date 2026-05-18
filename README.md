# Microservicio de Carrito y Pre-Órdenes ('carrito')

## Descripción
Módulo intermedio que agrupa los lotes ganados por un mismo usuario antes de proceder a la facturación y despacho unificado, simulando un flujo de pre-orden de compra.

* **Puerto:** `8086`
* **Base de Datos:** `carrito_db` (MySQL)


## Funcionalidades Clave
* Consolidación temporal de artículos adjudicados.
* Cálculo automático de totales acumulados.
* Limpieza automática del carrito tras procesarse las órdenes de pago.


## Configuración (`application.properties`)
* server.port=8086
* spring.datasource.url=jdbc:mysql://localhost:3306/carrito_db
* spring.datasource.username=root
* spring.datasource.password=
* spring.jpa.hibernate.ddl-auto=update

* spring.security.user.name=admin_carrito
* spring.security.user.password=Carrito2026!

* logging.level.cl.sda1085.carrito=DEBUG


## Pasos para Ejecutar

### 1. Preparación de la Base de Datos
Antes de ejecutar el servicio, crear la conexión a la base de datos de MySQL (XAMPP) corriendo en el puerto `3306` y con el nombre 'carritos_db'.

### 2. Verificación de Credenciales
Revisar que el archivo application.properties tenga por defecto, usuario root y contraseña vacía.

### 3. Lanzamiento del Microservicio
Ejecutar (run) la clase principal con la anotación @SpringBootApplication (CarritosApplication.java).

### 4. Reglas de Seguridad
Al consumir los endpoints en Postman, ten en cuenta el comportamiento de la cadena de filtros de seguridad:
* **Operaciones de Cliente:** La gestión del carrito de compras (añadir productos, actualizar cantidades y eliminar ítems) requiere autenticación activa con un rol de cliente autorizado.
* **Consultas de Administrador:** El acceso a reportes globales de carritos activos o auditorías del estado de las órdenes requiere credenciales de administrador general.
* **Persistencia de Datos:** Asegurarse de enviar los identificadores de usuario (`idUsuario`) y de producto (`idProducto`) correctos en el cuerpo de la petición (JSON) para evitar infracciones de claves foráneas con las bases de datos externas de los microservicios de usuarios y productos.
