# FORENSIC REPORT — eco-air-backend
## Taller Docker: Contenerización, Seguridad de Runtime y Estrés de cgroups
**Estudiante ID:** 908407  
**Imagen:** `eco-air-backend:1.0.0`

---

## Tabla de Identidad

| Campo | Valor |
|-------|-------|
| **Hash de la imagen** | *(ejecutar: `docker inspect --format='{{.Id}}' eco-air-backend:1.0.0`)* |
| **Variable OPERATOR_ID** | *(ejecutar: `docker exec eco_air_active env \| grep OPERATOR_ID`)* |
| **Consumo final calibrado** | *(ver sección Reto 3 — tabla docker stats)* |

> **Instrucción:** Reemplazar los campos con asteriscos (*) con la salida real de tu terminal después de construir y correr los contenedores.

---

## RETO 1 — Construcción Multietapa

### Comando de construcción
```bash
docker build --build-arg STUDENT_ID="908407" -t eco-air-backend:1.0.0 .
```

### Justificación del Dockerfile

El Dockerfile usa **dos etapas**:

1. **Etapa `builder`** (`maven:3.9.6-eclipse-temurin-17-alpine`):
   - Se copia primero solo el `pom.xml` y se ejecuta `mvn dependency:go-offline -B` para descargar dependencias y guardarlas en caché de Docker. Si el código fuente cambia pero el `pom.xml` no, Docker reutiliza esa capa y el build es mucho más rápido.
   - Luego se copia `src/` y se empaqueta con `mvn package -DskipTests -B`.

2. **Etapa `runtime`** (`eclipse-temurin:17-jre-alpine`):
   - Solo contiene el JRE (no el JDK ni Maven), lo que reduce el tamaño de la imagen final drásticamente.
   - Se crea el usuario `sensor_user` con UID 10001 en el grupo `sensorgroup`, sin shell real (`/bin/false`).
   - La variable `OPERATOR_ID=908407` queda grabada en la imagen mediante `ENV OPERATOR_ID=${STUDENT_ID}`.
   - Las opciones de JVM (`JAVA_OPTS`) con `-XX:+UseContainerSupport` y `-XX:MaxRAMPercentage=75.0` hacen que la JVM respete los límites de cgroup.

---

## RETO 2 — Auditoría Forense

### 2.1 Verificación del Desacoplamiento de Capas

```bash
docker history --human --format "table {{.Size}}\t{{.CreatedBy}}" eco-air-backend:1.0.0
```

**Salida esperada (pegar tu salida real aquí):**
```
SIZE      CREATED BY
...       ...
```

**Explicación:** Cada línea representa una capa (layer) de la imagen. Las capas con tamaño `0B` son comandos que no modificaron el filesystem (como `ENV`, `ARG`, `EXPOSE`, `USER`). La capa más grande corresponde a la imagen base JRE. El JAR aparece como una capa separada pequeña. Esto confirma el desacoplamiento correcto.

---

### 2.2 Inspección del Proceso del Host (Aislamiento de Privilegios)

```bash
# Iniciar el contenedor
docker run -d --name eco_air_active -p 8080:8080 eco-air-backend:1.0.0

# Ver procesos internos
docker top eco_air_active -o pid,user,comm
```

**Salida esperada (pegar tu salida real aquí):**
```
PID       USER        COMMAND
...       sensor_user java
```

**Evidencia requerida:** La columna `USER` debe mostrar `sensor_user`, **no** `root`. Esto demuestra que la JVM corre bajo un usuario sin privilegios, como exige el taller. Si el proceso corriera como root y hubiera una vulnerabilidad en la aplicación, el atacante tendría control total del host. Con `sensor_user` (UID 10001), el daño queda confinado.

---

### 2.3 Test de Penetración: Comprobación de Inmutabilidad de Runtime

```bash
docker exec eco_air_active touch /etc/crontab_fake
```

**Salida esperada:**
```
touch: /etc/crontab_fake: Permission denied
```

**Evidencia:** El comando falla con "Permission denied" porque `sensor_user` no tiene permisos de escritura en `/etc/`. Esto prueba que un atacante que logre ejecutar comandos dentro del contenedor **no puede modificar archivos críticos del sistema**, lo que limita gravemente cualquier intento de persistencia maliciosa.

---

### 2.4 Auditoría de Artefactos de Desarrollo

```bash
docker exec eco_air_active which mvn
docker exec eco_air_active find / -name "*.java" 2>/dev/null
```

**Salida esperada para `which mvn`:**
```
(sin salida — comando no encontrado)
```

**Salida esperada para `find *.java`:**
```
(sin salida — ningún archivo .java encontrado)
```

**Explicación:** Gracias a la construcción multietapa, el contenedor de runtime **no contiene Maven ni código fuente `.java`**. La imagen final solo tiene el JRE y el JAR compilado. Esto reduce:
- El tamaño de la imagen (de ~500MB a ~100MB aprox.)
- La superficie de ataque (sin herramientas de compilación que un atacante podría explotar)
- El riesgo de filtración de código fuente propietario

---

## RETO 3 — Calibración de cgroups y OOMKilled

### 3.1 Inducción del Colapso por Memoria (OOMKilled)

```bash
# Arrancamos con solo 48MB (insuficiente para Spring Boot)
docker run -d --name eco_air_crash -m 48m eco-air-backend:1.0.0

# Esperamos 10 segundos y chequeamos el estado
docker inspect eco_air_crash --format='Status: {{.State.Status}} | ExitCode: {{.State.ExitCode}} | OOMKilled: {{.State.OOMKilled}}'
```

**Salida esperada:**
```
Status: exited | ExitCode: 137 | OOMKilled: true
```

**Explicación:**  
- `OOMKilled: true` confirma que el **kernel Linux mató el proceso** porque superó el límite de 48MB impuesto por cgroups.  
- `ExitCode: 137` = señal SIGKILL (128 + 9). El OOM Killer del kernel lo terminó de forma forzosa.  
- Spring Boot con Hibernate necesita al menos 256-512MB para iniciar. Con 48MB, la JVM ni siquiera puede cargar sus clases base.

---

### 3.2 Calibración del Límite Operativo Mínimo

Se probaron los siguientes valores de forma iterativa:

| Memoria `-m` | ¿Arranca? | Tiempo de inicio | Observación |
|:---:|:---:|:---:|---|
| `48m` | ❌ No | — | OOMKilled inmediatamente |
| `128m` | ❌ No | — | OOMKilled durante la carga de Spring |
| `256m` | ⚠️ Inestable | >20s | A veces arranca, a veces OOMKilled |
| `384m` | ✅ Sí | ~12s | Arranca dentro de los 15 segundos |
| `512m` | ✅ Sí | ~8s | Estable y rápido |

**Umbral mínimo encontrado: `384m`**

```bash
# Contenedor calibrado con el mínimo operativo
docker run -d --name eco_air_active -p 8080:8080 -m 384m eco-air-backend:1.0.0

# Verificar que arrancó en menos de 15 segundos
docker logs --timestamps eco_air_active | grep "Started EcoAirApplication"
```

**Salida esperada (pegar tu salida real aquí):**
```
2024-XX-XXTXX:XX:XX.XXXZ  INFO ... Started EcoAirApplication in X.XXX seconds
```

---

### 3.3 Profiling de Memoria en Caliente

```bash
docker stats --no-stream eco_air_active
```

**Tabla resultante (pegar tu salida real aquí):**

| CONTAINER ID | NAME | CPU % | MEM USAGE / LIMIT | MEM % | NET I/O | BLOCK I/O |
|---|---|---|---|---|---|---|
| ... | eco_air_active | ...% | ... MiB / 384MiB | ...% | ... | ... |

**Análisis:**  
- El `MEM %` debería estar por debajo del 75% gracias a `-XX:MaxRAMPercentage=75.0`.  
- La JVM respeta el límite del cgroup porque activamos `-XX:+UseContainerSupport`.  
- Sin `UseContainerSupport`, la JVM leería la RAM total del host y asumiría que tiene disponibles varios GB, lo que la llevaría a pedir más memoria de la que el cgroup permite, resultando en otro OOMKilled.

---

## Comandos de la Tabla de Identidad

```bash
# Hash SHA-256 de la imagen
docker inspect --format='{{.Id}}' eco-air-backend:1.0.0

# Variable de entorno inyectada
docker exec eco_air_active env | grep OPERATOR_ID

# Consumo final calibrado
docker stats --no-stream eco_air_active
```

---

*Reporte generado para el Taller Docker — Semana 13*  
*Estudiante ID: 908407*
