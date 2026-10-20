package com.ecoair.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/alertas")
public class AlertaController {

    @GetMapping("/estado")
    public Map<String, Object> obtenerEstado() {
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("sistema", "EcoAir Backend");
        respuesta.put("version", "1.0.0");
        respuesta.put("estudiante_id", "908407");
        respuesta.put("estado", "ACTIVO");
        respuesta.put("timestamp", LocalDateTime.now().toString());
        return respuesta;
    }

    @GetMapping("/alerta")
    public Map<String, Object> procesarAlerta() {
        Map<String, Object> alerta = new HashMap<>();
        alerta.put("tipo", "CALIDAD_AIRE");
        alerta.put("nivel", "MODERADO");
        alerta.put("pm25", 35.6);
        alerta.put("co2_ppm", 412);
        alerta.put("mensaje", "Niveles dentro del rango aceptable");
        alerta.put("procesado_por", "sensor_user");
        return alerta;
    }
}
