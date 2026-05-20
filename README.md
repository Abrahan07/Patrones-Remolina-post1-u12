# Unidad 12: Integración de Patrones y Arquitecturas

Sistema de gestión de pedidos en Spring Boot que integra cuatro patrones de diseño (Factory, Strategy, Observer y Facade) siguiendo una arquitectura hexagonal, con verificación de calidad mediante SonarQube.

---

## Arquitectura del Sistema

El proyecto sigue una arquitectura hexagonal (ports & adapters) organizada en paquetes por funcionalidad:

```
com.empresa.pedidos/
├── dominio/          → Entidades, enums, eventos y puertos (interfaces)
├── aplicacion/       → Factory para selección dinámica de procesadores
├── infraestructura/  → Implementaciones de persistencia y notificaciones
└── adaptadores/      → Procesadores (Strategy), Facade y controlador REST
```

El flujo completo del sistema es:

```
PedidoController → FachadaPedidos → ProcesadorPedidoFactory
    → ProcesadorPedido (Strategy) → RepositorioPedidos
    → PedidoProcesadoEvent → NotificacionEmail / NotificacionLog (Observer)
```

---

## Patrones de Diseño Implementados

### 1. Strategy — Desacoplamiento del algoritmo de cálculo

**Problema que resuelve:** el código legacy tenía un bloque `if/else` con la lógica de cálculo de costo mezclada en el servicio principal, aumentando la complejidad ciclomática cada vez que se agregaba un nuevo tipo de pedido.

**Solución:** se define la interfaz `ProcesadorPedido` como puerto de dominio y tres implementaciones independientes, cada una con su propio algoritmo encapsulado:

| Implementación | Cálculo |
|---|---|
| `ProcesadorPedidoEstandar` | subtotal × 1.1 |
| `ProcesadorPedidoExpress` | subtotal × 1.3 |
| `ProcesadorPedidoInternacional` | subtotal × 1.5 + 25.0 |

**Resultado:** agregar un nuevo tipo de pedido solo requiere crear una nueva clase sin modificar el servicio existente (principio Open/Closed).

---

### 2. Factory — Selección dinámica de Strategy

**Problema que resuelve:** el servicio necesitaba seleccionar el procesador correcto según el tipo de pedido sin conocer las implementaciones concretas ni usar condicionales.

**Solución:** `ProcesadorPedidoFactory` recibe todas las implementaciones de `ProcesadorPedido` inyectadas automáticamente por Spring y las indexa en un `Map<TipoPedido, ProcesadorPedido>`, eliminando cualquier condicional en la capa de aplicación.

**Resultado:** la Cyclomatic Complexity del flujo de selección se reduce a 1.

---

### 3. Observer — Notificación desacoplada con Spring Events

**Problema que resuelve:** el código legacy acoplaba directamente `JavaMailSender` al servicio de negocio, mezclando responsabilidades y dificultando agregar nuevos canales de notificación.

**Solución:** se publica un evento de dominio `PedidoProcesadoEvent` usando `ApplicationEventPublisher`. Los listeners `NotificacionEmail` y `NotificacionLog` reaccionan independientemente con `@EventListener`, sin que el servicio conozca quién escucha.

**Resultado:** agregar un nuevo canal de notificación (SMS, push, webhook) solo requiere crear un nuevo listener sin modificar el servicio.

---

### 4. Facade — Simplificación de la interfaz para el controlador REST

**Problema que resuelve:** el controlador REST no debería conocer la Factory, el repositorio ni el publisher de eventos por separado, ya que eso acopla la capa de presentación a la lógica interna.

**Solución:** `FachadaPedidos` unifica las tres operaciones (procesar, guardar, publicar evento) en un único punto de entrada para el controlador.

**Resultado:** `PedidoController` tiene una sola dependencia (`FachadaPedidos`) y la Cyclomatic Complexity del flujo principal es 1.

---

## Métricas de Calidad SonarQube

### Antes de la refactorización (código legacy)

| Métrica | Valor |
|---|---|
| Cyclomatic Complexity | 4 |
| Cognitive Complexity | 6 |
| Acoplamiento | Directo a JavaMailSender y JPA Repository |
| Cobertura de pruebas | 0% |

### Después de la refactorización

| Métrica | Valor |
|---|---|
| Quality Gate | Passed ✅ |
| Bugs | 0 |
| Vulnerabilities | 0 |
| Security Hotspots | 0 |
| Maintainability | A |
| Cobertura de pruebas | 88.7% |
| Duplicaciones | 0% |
| Code Smells | 3 (deuda técnica: 25 min) |
| Pruebas unitarias detectadas | 13 |

### Capturas de SonarQube

**Dashboard general con Quality Gate Passed:**

![Dashboard SonarQube](img/captura1.png)

**Métricas con cobertura 88.7%:**

![Métricas de cobertura](img/captura2.png)

**Code Smells detectados:**

![Code Smells](img/captura3.png)

---

## Pruebas Implementadas

| Clase de prueba | Patrón cubierto | Número de pruebas |
|---|---|---|
| `ProcesadorPedidoStrategyTest` | Strategy | 3 pruebas unitarias |
| `ProcesadorPedidoFactoryTest` | Factory | 4 pruebas unitarias |
| `NotificacionObserverTest` | Observer | 2 pruebas unitarias |
| `FachadaPedidosIntegrationTest` | Facade | 3 pruebas de integración `@SpringBootTest` |

Para ejecutar todas las pruebas:

```bash
./mvnw clean test
```

---

## Endpoints REST

| Método | Endpoint | Descripción |
|---|---|---|
| POST | `/api/pedidos` | Crea y procesa un nuevo pedido |
| GET | `/api/pedidos/{id}` | Busca un pedido por ID |

**Ejemplo de petición POST:**

```json
{
    "tipo": "ESTANDAR",
    "subtotal": 100.0
}
```

**Respuesta esperada:**

```json
{
    "id": 1,
    "tipo": "ESTANDAR",
    "estado": "PROCESADO",
    "subtotal": 100.0,
    "costo": 110.0
}
```

---

## Ejecución Local

```bash
# Compilar el proyecto
./mvnw clean package -DskipTests

# Ejecutar la aplicación
./mvnw spring-boot:run
```

La aplicación usa H2 en memoria. La consola H2 está disponible en:
```
http://localhost:8080/h2-console
```

---

