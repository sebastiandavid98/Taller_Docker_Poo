# FORENSIC REPORT — eco-air-backend
**Estudiante ID:** 908407  
**Imagen:** `eco-air-backend:1.0.0`

---

## Tabla de Identidad

| Campo | Valor |
|-------|-------|
| **Hash de la imagen** | |
| **Variable OPERATOR_ID** | |
| **Consumo final calibrado** | |

---

## RETO 1 — Construcción Multietapa

```bash
docker build --build-arg STUDENT_ID="908407" -t eco-air-backend:1.0.0 .
```

---

## RETO 2 — Auditoría Forense

### 2.1 Verificación del Desacoplamiento de Capas

```bash
docker history --human --format "table {{.Size}}\t{{.CreatedBy}}" eco-air-backend:1.0.0
```

```
SIZE      CREATED BY

```

---

### 2.2 Inspección del Proceso del Host

```bash
docker run -d --name eco_air_active -p 8080:8080 eco-air-backend:1.0.0
docker top eco_air_active -o pid,user,comm
```

```
PID       USER        COMMAND

```

---

### 2.3 Test de Penetración

```bash
docker exec eco_air_active touch /etc/crontab_fake
```

```

```

---

### 2.4 Auditoría de Artefactos de Desarrollo

```bash
docker exec eco_air_active which mvn
docker exec eco_air_active find / -name "*.java" 2>/dev/null
```

```

```

---

## RETO 3 — Calibración de cgroups y OOMKilled

### 3.1 Inducción del Colapso por Memoria

```bash
docker run -d --name eco_air_crash -m 48m eco-air-backend:1.0.0
docker inspect eco_air_crash --format='Status: {{.State.Status}} | ExitCode: {{.State.ExitCode}} | OOMKilled: {{.State.OOMKilled}}'
```

```

```

---

### 3.2 Calibración del Límite Operativo Mínimo

| Memoria `-m` | ¿Arranca? | Tiempo de inicio |
|:---:|:---:|:---:|
| `48m` | No | — |
| `128m` | No | — |
| `256m` | No | — |
| `384m` | Sí | |
| `512m` | Sí | |

```bash
docker logs --timestamps eco_air_active | grep "Started EcoAirApplication"
```

```

```

---

### 3.3 Profiling de Memoria en Caliente

```bash
docker stats --no-stream eco_air_active
```

| CONTAINER ID | NAME | CPU % | MEM USAGE / LIMIT | MEM % | NET I/O | BLOCK I/O |
|---|---|---|---|---|---|---|
| | eco_air_active | | | | | |

---

## Comandos de la Tabla de Identidad

```bash
docker inspect --format='{{.Id}}' eco-air-backend:1.0.0
docker exec eco_air_active env | grep OPERATOR_ID
docker stats --no-stream eco_air_active
```

```

```

---

*Taller Docker — Semana 13 | Estudiante ID: 908407*
