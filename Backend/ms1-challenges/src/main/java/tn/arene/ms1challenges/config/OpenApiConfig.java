package tn.arene.ms1challenges.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Arène · MS1 Challenges")
                        .version("1.0.0")
                        .description("Espace entreprise, défis, critères d'acceptation, primes, catalogue.")
                        .contact(new Contact().name("Chaima Dahdouh")))
                .servers(List.of(
                        new Server().url("http://localhost:8080/ms1").description("Via la Gateway"),
                        new Server().url("http://localhost:8081").description("Accès direct (dev)")
                ));
    }
}
