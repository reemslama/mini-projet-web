package tn.arene.ms5.adapter.integration;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.fasterxml.jackson.databind.ObjectMapper;

import tn.arene.ms5.application.port.SubmissionGateway;

/** Client synchrone MS3 (OpenFeign/OCR possible en S6 sans changer le port applicatif). */
@Component
@ConditionalOnProperty(name = "ms5.enrichment.mode", havingValue = "feign")
public class RestClientSubmissionGateway implements SubmissionGateway {

	private final RestClient client;
	private final ObjectMapper mapper;

	public RestClientSubmissionGateway(@Value("${ms3.url:http://localhost:8083}") String baseUrl,
			ObjectMapper mapper) {
		this.client = RestClient.builder().baseUrl(baseUrl).build();
		this.mapper = mapper;
	}

	@Override
	public Optional<Snapshot> find(UUID submissionId) {
		try {
			String body = client.get().uri("/api/submissions/{id}", submissionId)
					.retrieve().body(String.class);
			if (body == null) return Optional.empty();
			Map<?, ?> json = mapper.readValue(body, Map.class);
			return Optional.of(new Snapshot(
					UUID.fromString(json.get("id").toString()),
					UUID.fromString(json.get("challengeId").toString()),
					json.get("applicantId") == null ? null : UUID.fromString(json.get("applicantId").toString()),
					String.valueOf(json.get("repoUrl")),
					json.get("branch") == null ? "main" : String.valueOf(json.get("branch")),
					json.get("commitSha") == null ? null : String.valueOf(json.get("commitSha"))));
		} catch (RestClientException | java.io.IOException exception) {
			return Optional.empty();
		}
	}
}
