package tn.arene.reputation.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI ms6OpenApi() {
        return new OpenAPI().info(new Info()
                .title("Arene - MS6 Reputation")
                .version("1.0.0")
                .description("Profils, badges, evenements de reputation et classements. "
                        + "Le contrat gele dans contracts/ reste la source de verite."));
    }
}
