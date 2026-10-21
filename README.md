# eco-air-backend

Taller Docker Semana 13 — Estudiante ID: 908407

## Estructura

```
eco-air-backend/
├── Dockerfile
├── .dockerignore
├── pom.xml
├── src/
├── audit_session.log
└── FORENSIC_REPORT.md
```

## Comandos

```bash
docker build --build-arg STUDENT_ID="908407" -t eco-air-backend:1.0.0 .
docker run -d --name eco_air_active -p 8080:8080 eco-air-backend:1.0.0
```

## Endpoints

| Método | URL |
|--------|-----|
| GET | `/api/alertas/estado` |
| GET | `/api/alertas/alerta` |
| GET | `/actuator/health` |
