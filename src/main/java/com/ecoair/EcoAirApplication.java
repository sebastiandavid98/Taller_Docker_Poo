package com.ecoair;

// Importamos lo necesario para arrancar Spring Boot
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Clase principal de la aplicacion EcoAir.
 * Esta clase arranca todo el servidor Spring Boot.
 *
 * Estudiante ID: 908407
 */
@SpringBootApplication
public class EcoAirApplication {

    public static void main(String[] args) {
        // Esta linea inicia el servidor de Spring Boot
        SpringApplication.run(EcoAirApplication.class, args);
        System.out.println("EcoAir Backend arrancado correctamente - ID: 908407");
    }
}
