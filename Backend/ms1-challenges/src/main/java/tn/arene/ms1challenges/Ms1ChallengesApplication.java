package tn.arene.ms1challenges;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class Ms1ChallengesApplication {

    public static void main(String[] args) {
        SpringApplication.run(Ms1ChallengesApplication.class, args);
    }
}
