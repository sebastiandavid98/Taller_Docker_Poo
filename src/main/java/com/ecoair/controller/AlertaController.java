package com.ecoair.controller;

// Importamos las clases de Spring para crear un controlador REST
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Controlador de alertas ambientales.
 * Expone endpoints para consultar el estado del sistema EcoAir.
 *
 * Un endpoint es como una "puerta" por donde otros programas
 * pueden hablar con nuestra aplicacion usando HTTP.
 */
@RestController
@RequestMapping("/api/alertas")
public class AlertaController {

    /**
     * Endpoint de estado: GET /api/alertas/estado
     * Devuelve informacion basica del sistema.
     */
    @GetMapping("/estado")
    public Map<String, Object> obtenerEstado() {
        // Usamos un Map para armar la respuesta en formato JSON
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("sistema", "EcoAir Backend");
        respuesta.put("version", "1.0.0");
        respuesta.put("estudiante_id", "908407");
        respuesta.put("estado", "ACTIVO");
        respuesta.put("timestamp", LocalDateTime.now().toString());
        return respuesta;
    }

    /**
     * Endpoint de alerta: GET /api/alertas/alerta
     * Simula el procesamiento de una alerta ambiental.
     */
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
