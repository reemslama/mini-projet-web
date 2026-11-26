package tn.arene.soumission.controllers;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import com.fasterxml.jackson.databind.ObjectMapper;

import tn.arene.soumission.repositories.SubmissionRepository;
import tn.arene.soumission.repositories.VerdictRepository;

/** Parcours HTTP complets de MS3 (H2 en mémoire), y compris les endpoints Actuator. */
@SpringBootTest(properties = { "eureka.client.enabled=false", "spring.jpa.open-in-view=false" })
@AutoConfigureMockMvc
class SubmissionRestControllerTest {

	private static final UUID CHALLENGE_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
	private static final UUID OTHER_CHALLENGE_ID = UUID.fromString("99999999-9999-9999-9999-999999999999");
	private static final String FUTURE_DEADLINE = "2030-12-31T23:59:59";

	@Autowired
	private MockMvc mvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private SubmissionRepository submissionRepository;

	@Autowired
	private VerdictRepository verdictRepository;

	@BeforeEach
	void cleanDatabase() {
		verdictRepository.deleteAll();
		submissionRepository.deleteAll();
	}

	private UUID createDraft() throws Exception {
		return createDraft(CHALLENGE_ID, UUID.randomUUID());
	}

	private UUID createDraft(UUID challengeId, UUID applicantId) throws Exception {
		String body = """
				{
				  "challengeId": "%s",
				  "applicantId": "%s",
				  "repoUrl": "https://github.com/example/solution.git",
				  "branch": "main"
				}
				""".formatted(challengeId, applicantId);

		MvcResult result = mvc.perform(post("/api/submissions").contentType(APPLICATION_JSON).content(body))
				.andExpect(status().isCreated())
				.andReturn();
		return UUID.fromString(objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText());
	}

	private ResultActions submit(UUID id, String commitSha, String deadline) throws Exception {
		return mvc.perform(post("/api/submissions/{id}/submit", id)
				.param("commitSha", commitSha)
				.param("deadline", deadline));
	}

	private ResultActions depositVerdict(UUID id, UUID submissionIdInBody, String result,
			int passed, int failed, int total) throws Exception {
		String body = """
				{
				  "submissionId": "%s",
				  "result": "%s",
				  "passed": %d,
				  "failed": %d,
				  "total": %d,
				  "coverage": 95.5,
				  "logsUrl": "https://ci.example.com/builds/42/logs"
				}
				""".formatted(submissionIdInBody, result, passed, failed, total);

		return mvc.perform(put("/api/submissions/{id}/verdict", id)
				.contentType(APPLICATION_JSON)
				.content(body));
	}

	@Test
	void createReturns201WithDraftProjection() throws Exception {
		String body = """
				{
				  "challengeId": "%s",
				  "applicantId": "%s",
				  "repoUrl": "https://github.com/example/solution.git",
				  "branch": "feature/optimize"
				}
				""".formatted(CHALLENGE_ID, UUID.randomUUID());

		mvc.perform(post("/api/submissions").contentType(APPLICATION_JSON).content(body))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNotEmpty())
				.andExpect(jsonPath("$.challengeId").value(CHALLENGE_ID.toString()))
				.andExpect(jsonPath("$.branch").value("feature/optimize"))
				.andExpect(jsonPath("$.status").value("DRAFT"))
				.andExpect(jsonPath("$.commitSha").value(org.hamcrest.Matchers.nullValue()));
	}

	@Test
	void createWithMissingRepoUrlReturns400ErrorEnvelope() throws Exception {
		String body = """
				{
				  "challengeId": "%s",
				  "applicantId": "%s",
				  "branch": "main"
				}
				""".formatted(CHALLENGE_ID, UUID.randomUUID());

		mvc.perform(post("/api/submissions").contentType(APPLICATION_JSON).content(body))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.error").value("Bad Request"))
				.andExpect(jsonPath("$.message").exists())
				.andExpect(jsonPath("$.path").value("/api/submissions"));
	}

	@Test
	void getByIdReturns404ErrorEnvelopeForUnknownId() throws Exception {
		UUID unknown = UUID.randomUUID();

		mvc.perform(get("/api/submissions/{id}", unknown))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404))
				.andExpect(jsonPath("$.error").value("Not Found"))
				.andExpect(jsonPath("$.path").value("/api/submissions/" + unknown));
	}

	@Test
	void listFiltersByChallengeAndStatus() throws Exception {
		UUID first = createDraft();
		createDraft();
		submit(first, "abc123", FUTURE_DEADLINE);

		mvc.perform(get("/api/submissions").param("challengeId", CHALLENGE_ID.toString()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(2)));
		mvc.perform(get("/api/submissions").param("challengeId", CHALLENGE_ID.toString()).param("status", "DRAFT"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(1)));
		mvc.perform(get("/api/submissions").param("challengeId", CHALLENGE_ID.toString()).param("status", "SUBMITTED"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id").value(first.toString()));
		mvc.perform(get("/api/submissions").param("challengeId", OTHER_CHALLENGE_ID.toString()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(0)));
	}

	@Test
	void submitFreezesCommitAndRejectsASecondSubmission() throws Exception {
		UUID id = createDraft();

		mvc.perform(post("/api/submissions/{id}/submit", id)
				.param("commitSha", "abc123")
				.param("deadline", FUTURE_DEADLINE))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("SUBMITTED"))
				.andExpect(jsonPath("$.commitSha").value("abc123"))
				.andExpect(jsonPath("$.submittedAt").exists());

		mvc.perform(post("/api/submissions/{id}/submit", id)
				.param("commitSha", "def456")
				.param("deadline", FUTURE_DEADLINE))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.status").value(409))
				.andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("déjà figée")));

		mvc.perform(get("/api/submissions/{id}", id))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.commitSha").value("abc123"));
	}

	@Test
	void submitAfterDeadlineReturns409() throws Exception {
		UUID id = createDraft();

		submit(id, "toolate", "2020-01-01T00:00:00")
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Date limite dépassée")));

		mvc.perform(get("/api/submissions/{id}", id))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("DRAFT"));
	}

	@Test
	void passVerdictClosesSubmissionAsPassedAndIsRetrievable() throws Exception {
		UUID id = createDraft();
		submit(id, "abc123", FUTURE_DEADLINE);

		depositVerdict(id, id, "PASS", 10, 0, 10)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.result").value("PASS"))
				.andExpect(jsonPath("$.total").value(10));

		mvc.perform(get("/api/submissions/{id}", id))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("PASSED"));

		mvc.perform(get("/api/submissions/{id}/verdict", id))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.submissionId").value(id.toString()))
				.andExpect(jsonPath("$.result").value("PASS"));
	}

	@Test
	void failVerdictClosesSubmissionAsRejected() throws Exception {
		UUID id = createDraft();
		submit(id, "abc123", FUTURE_DEADLINE);

		depositVerdict(id, id, "FAIL", 4, 6, 10)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.result").value("FAIL"));

		mvc.perform(get("/api/submissions/{id}", id))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("REJECTED"));
	}

	@Test
	void verdictWithMismatchedIdReturns409() throws Exception {
		UUID id = createDraft();
		submit(id, "abc123", FUTURE_DEADLINE);

		depositVerdict(id, UUID.randomUUID(), "PASS", 10, 0, 10)
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("ne correspond pas")));
	}

	@Test
	void verdictOnDraftReturns409() throws Exception {
		UUID id = createDraft();

		depositVerdict(id, id, "PASS", 10, 0, 10)
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.status").value(409))
				.andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("pas éligible")));
	}

	@Test
	void missingChallengeIdReturns400ErrorEnvelope() throws Exception {
		mvc.perform(get("/api/submissions"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.error").value("Bad Request"))
				.andExpect(jsonPath("$.message").isNotEmpty())
				.andExpect(jsonPath("$.path").value("/api/submissions"));
	}

	@Test
	void invalidQueryParameterReturns400ErrorEnvelope() throws Exception {
		mvc.perform(get("/api/submissions").param("challengeId", "not-a-uuid"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.error").value("Bad Request"))
				.andExpect(jsonPath("$.message").isNotEmpty())
				.andExpect(jsonPath("$.path").value("/api/submissions"));

		mvc.perform(get("/api/submissions").param("challengeId", CHALLENGE_ID.toString()).param("status", "BOGUS"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.message").isNotEmpty());
	}

	@Test
	void actuatorEndpointsAreExposedForMonitoring() throws Exception {
		mvc.perform(get("/actuator/health"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("UP"));
		mvc.perform(get("/actuator/info"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.app.name").value("MS3-Soumissions"));
		mvc.perform(get("/actuator/metrics"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.names").isArray());
		mvc.perform(get("/actuator/metrics/http.server.requests"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("http.server.requests"));
	}
}
