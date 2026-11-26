package tn.arene.soumission.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

/** Configuration Swagger / OpenAPI alignée sur le contrat gelé MS3. */
@Configuration
public class OpenApiConfig {

	@Bean
	public OpenAPI ms3OpenAPI() {
		return new OpenAPI()
				.info(new Info()
						.title("Arène · MS3 Soumissions")
						.version("1.0.0")
						.description("Dépôt GitHub, commit figé, artefacts, cycle de vie et contrôle de "
								+ "date limite. Contrat de référence : contracts/ms3-submissions.openapi.yaml."))
				.servers(List.of(
						new Server().url("http://localhost:8080/ms3").description("Via l'API Gateway"),
						new Server().url("http://localhost:8083").description("Accès direct (développement)")))
				.components(new Components().addSecuritySchemes("bearerAuth", new SecurityScheme()
						.type(SecurityScheme.Type.HTTP)
						.scheme("bearer")
						.bearerFormat("JWT")
						.description("Jeton OIDC Keycloak, vérifié par la Gateway puis par MS3.")));
	}
}
