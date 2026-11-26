package tn.arene.ms5.adapter.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import tn.arene.ms5.adapter.persistence.CodeEmbeddingJpaRepository;
import tn.arene.ms5.adapter.persistence.OutboxEventJpaRepository;
import tn.arene.ms5.adapter.persistence.SimilarityReportJpaRepository;
import tn.arene.ms5.adapter.persistence.VerificationJpaRepository;
import tn.arene.ms5.application.usecase.VerificationWorker;

/** Les 6 endpoints gelés, le worker déterministe et les conventions d'erreur. */
@SpringBootTest(properties = { "eureka.client.enabled=false", "ms5.worker.initial-delay-ms=999999",
		"ms5.outbox.poll-ms=999999" })
@AutoConfigureMockMvc
class VerificationRestControllerTest {

	private static final UUID SUBMISSION = UUID.fromString("33333333-3333-3333-3333-333333333333");
	private static final UUID CHALLENGE = UUID.fromString("11111111-1111-1111-1111-111111111111");

	@Autowired private MockMvc mvc;
	@Autowired private ObjectMapper mapper;
	@Autowired private VerificationWorker worker;
	@Autowired private VerificationJpaRepository verifications;
	@Autowired private SimilarityReportJpaRepository reports;
	@Autowired private CodeEmbeddingJpaRepository embeddings;
	@Autowired private OutboxEventJpaRepository outbox;

	@BeforeEach
	void clean() {
		outbox.deleteAll();
		reports.deleteAll();
		embeddings.deleteAll();
		verifications.deleteAll();
	}

	private String create(String body) throws Exception {
		String payload = mvc.perform(post("/api/verifications").contentType(MediaType.APPLICATION_JSON)
				.content(body))
				.andExpect(status().isAccepted())
				.andReturn().getResponse().getContentAsString();
		return mapper.readTree(payload).get("id").asText();
	}

	@Test
	void launchReturns202WithQueuedVerificationAndNoInternalFields() throws Exception {
		mvc.perform(post("/api/verifications").contentType(MediaType.APPLICATION_JSON)
				.content("{\"submissionId\":\"" + SUBMISSION + "\"}"))
				.andExpect(status().isAccepted())
				.andExpect(jsonPath("$.id").isNotEmpty())
				.andExpect(jsonPath("$.submissionId").value(SUBMISSION.toString()))
				.andExpect(jsonPath("$.status").value("QUEUED"))
				.andExpect(jsonPath("$.result").value(org.hamcrest.Matchers.nullValue()))
				.andExpect(jsonPath("$.sandboxImage").isNotEmpty())
				.andExpect(jsonPath("$.specDigest").doesNotExist())
				.andExpect(jsonPath("$.applicantId").doesNotExist());
	}

	@Test
	void workerCompletesWithDeterministicVerdictAndPublishesOutbox() throws Exception {
		String id = create("{\"submissionId\":\"" + SUBMISSION + "\"}");
		worker.poll();

		mvc.perform(get("/api/verifications/{id}", id))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("COMPLETED"))
				.andExpect(jsonPath("$.result").value("PASS"))
				.andExpect(jsonPath("$.passed").value(3))
				.andExpect(jsonPath("$.failed").value(0))
				.andExpect(jsonPath("$.total").value(3))
				.andExpect(jsonPath("$.coverage").value(0.83))
				.andExpect(jsonPath("$.startedAt").isNotEmpty())
				.andExpect(jsonPath("$.finishedAt").isNotEmpty());

		mvc.perform(get("/api/verifications/{id}/logs", id))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
				.andExpect(content().string(org.hamcrest.Matchers.containsString("runner=local-fixture")));

		org.assertj.core.api.Assertions.assertThat(outbox.findAll()).hasSize(1);
		org.assertj.core.api.Assertions.assertThat(outbox.findAll().get(0).getEventType())
				.isEqualTo("verification.completed");
	}

	@Test
	void duplicateLaunchReturnsSameActiveVerification() throws Exception {
		String body = "{\"submissionId\":\"" + SUBMISSION + "\"}";
		String first = create(body);
		String second = create(body);
		org.assertj.core.api.Assertions.assertThat(second).isEqualTo(first);
		org.assertj.core.api.Assertions.assertThat(verifications.count()).isEqualTo(1);
	}

	@Test
	void unknownVerificationReturns404Envelope() throws Exception {
		UUID unknown = UUID.randomUUID();
		mvc.perform(get("/api/verifications/{id}", unknown))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404))
				.andExpect(jsonPath("$.error").value("Not Found"))
				.andExpect(jsonPath("$.message").exists())
				.andExpect(jsonPath("$.path").value("/api/verifications/" + unknown));
	}

	@Test
	void invalidCommandAndMissingSubmissionIdReturn400Envelope() throws Exception {
		mvc.perform(post("/api/verifications").contentType(MediaType.APPLICATION_JSON)
				.content("{\"submissionId\":\"" + SUBMISSION + "\",\"testCommand\":\"npm test; rm -rf /\"}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.error").value("Bad Request"));

		mvc.perform(post("/api/verifications").contentType(MediaType.APPLICATION_JSON).content("{}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400));
	}

	@Test
	void similarityAnalyzeAndLookupMatchTheFrozenContract() throws Exception {
		String body = "{\"submissionId\":\"" + SUBMISSION + "\",\"challengeId\":\"" + CHALLENGE + "\"}";
		mvc.perform(post("/api/similarity/analyze").contentType(MediaType.APPLICATION_JSON).content(body))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.submissionId").value(SUBMISSION.toString()))
				.andExpect(jsonPath("$.challengeId").value(CHALLENGE.toString()))
				.andExpect(jsonPath("$.maxSimilarity").value(0))
				.andExpect(jsonPath("$.flagged").value(false))
				.andExpect(jsonPath("$.matches").isArray())
				.andExpect(jsonPath("$.analyzedAt").isNotEmpty());

		mvc.perform(get("/api/similarity/{submissionId}", SUBMISSION))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.submissionId").value(SUBMISSION.toString()));

		mvc.perform(get("/api/similarity/{submissionId}", UUID.randomUUID()))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404));
	}

	@Test
	void aiEndpointReturns503WhenDisabledAndVerdictIsUnaffected() throws Exception {
		String id = create("{\"submissionId\":\"" + SUBMISSION + "\"}");
		worker.poll();

		mvc.perform(post("/api/ai/summarize/{submissionId}", SUBMISSION))
				.andExpect(status().isServiceUnavailable())
				.andExpect(jsonPath("$.status").value(503))
				.andExpect(jsonPath("$.error").value("Service Unavailable"));

		mvc.perform(get("/api/verifications/{id}", id))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.result").value("PASS"));
	}

	@Test
	void actuatorEndpointsAreExposed() throws Exception {
		mvc.perform(get("/actuator/health"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("UP"));
		mvc.perform(get("/actuator/info"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.app.name").value("MS5-Verification"));
		mvc.perform(get("/actuator/metrics"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.names").isArray());
	}
}
