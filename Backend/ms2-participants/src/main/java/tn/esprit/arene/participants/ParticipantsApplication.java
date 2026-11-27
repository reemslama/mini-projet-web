package tn.esprit.arene.participants;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * MS2 · Participants — gère les utilisateurs (développeurs), les équipes
 * et les candidatures aux défis publiés par MS1.
 */
@SpringBootApplication
@EnableDiscoveryClient
public class ParticipantsApplication {

    public static void main(String[] args) {
        SpringApplication.run(ParticipantsApplication.class, args);
    }
}
