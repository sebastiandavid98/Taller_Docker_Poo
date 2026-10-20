# eco-air-backend 🌿

Backend de alertas ambientales — Taller Docker Semana 13  
**Estudiante ID:** 908407

## Estructura del proyecto

```
eco-air-backend/
├── Dockerfile          # Imagen multietapa builder + runtime
├── .dockerignore       # Archivos excluidos del contexto de build
├── pom.xml             # Dependencias Maven (Spring Boot 3)
├── src/                # Código fuente Java
├── audit_session.log   # Registro de comandos ejecutados
└── FORENSIC_REPORT.md  # Reporte de auditoría forense
```

## Cómo construir y correr

```bash
# 1. Construir la imagen (obligatorio incluir STUDENT_ID)
docker build --build-arg STUDENT_ID="908407" -t eco-air-backend:1.0.0 .

# 2. Correr el contenedor
docker run -d --name eco_air_active -p 8080:8080 eco-air-backend:1.0.0

# 3. Verificar que está corriendo
curl http://localhost:8080/api/alertas/estado
```

## Endpoints disponibles

| Método | URL | Descripción |
|--------|-----|-------------|
| GET | `/api/alertas/estado` | Estado del sistema |
| GET | `/api/alertas/alerta` | Simulación de alerta ambiental |
| GET | `/actuator/health` | Health check del contenedor |
