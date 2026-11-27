package tn.arene.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClient;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClients;

import tn.arene.loadbalancer.ChallengeHashLoadBalancerConfig;
import tn.arene.loadbalancer.ParticipantLoadBalancerConfig;

/** Point d'entrée unique de la plateforme Arène. Les routes sont déclarées dans application.properties. */
@SpringBootApplication
@EnableDiscoveryClient
@LoadBalancerClients({
        // MS2 « Candidat » : Random, avec deux instances (8082 / 8092).
        @LoadBalancerClient(name = "participants", configuration = ParticipantLoadBalancerConfig.class),
        // MS1 : deuxième algorithme, Header Hash, avec deux instances (8081 / 8091).
        @LoadBalancerClient(name = "ms1-challenges", configuration = ChallengeHashLoadBalancerConfig.class)
})
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
