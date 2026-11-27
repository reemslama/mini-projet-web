package tn.esprit.arene.eureka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * Serveur de découverte Eureka de la plateforme Arène.
 * Le même projet est lancé deux fois (profils peer1 et peer2) :
 * les deux serveurs se répliquent et connaissent donc tous les microservices.
 */
@SpringBootApplication
@EnableEurekaServer
public class EurekaServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }
}
