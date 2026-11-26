package tn.arene.soumission.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import tn.arene.soumission.dto.SubmissionResponse;
import tn.arene.soumission.dto.VerdictRequest;
import tn.arene.soumission.dto.VerdictResponse;
import tn.arene.soumission.entities.Submission;
import tn.arene.soumission.entities.SubmissionStatus;
import tn.arene.soumission.entities.Verdict;
import tn.arene.soumission.entities.VerdictResult;
import tn.arene.soumission.exceptions.ConflictException;
import tn.arene.soumission.exceptions.ResourceNotFoundException;
import tn.arene.soumission.repositories.VerdictRepository;
import tn.arene.soumission.services.impl.VerdictServiceImpl;

/** Règles de dépôt d'un verdict renvoyé par MS5. */
@ExtendWith(MockitoExtension.class)
class VerdictServiceImplTest {

	private static final UUID ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
	private static final UUID OTHER_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");

	@Mock
	private VerdictRepository verdictRepository;

	@Mock
	private ISubmissionService submissionService;

	@InjectMocks
	private VerdictServiceImpl service;

	private VerdictRequest request(UUID submissionId, VerdictResult result, int passed, int failed, int total) {
		return new VerdictRequest(submissionId, result, passed, failed, total, 80.0, "https://ci.example.com/logs");
	}

	private Submission submitted() {
		Submission submission = Submission.create(
				UUID.fromString("11111111-1111-1111-1111-111111111111"),
				UUID.fromString("22222222-2222-2222-2222-222222222222"),
				"https://github.com/example/solution.git", "main");
		submission.submit("abc123", java.time.LocalDateTime.now().plusDays(1));
		return submission;
	}

	@Test
	void depositRejectsMismatchedSubmissionId() {
		when(submissionService.requireEntity(ID)).thenReturn(submitted());

		assertThatThrownBy(() -> service.deposit(ID, request(OTHER_ID, VerdictResult.PASS, 10, 0, 10)))
				.isInstanceOf(ConflictException.class)
				.hasMessageContaining("ne correspond pas");
		verifyNoInteractions(verdictRepository);
	}

	@Test
	void depositRejectsInconsistentTestCounts() {
		when(submissionService.requireEntity(ID)).thenReturn(submitted());

		assertThatThrownBy(() -> service.deposit(ID, request(ID, VerdictResult.PASS, 5, 5, 8)))
				.isInstanceOf(ConflictException.class)
				.hasMessageContaining("passed + failed");
		verifyNoInteractions(verdictRepository);
	}

	@Test
	void depositRejectsDraftSubmission() {
		Submission draft = submitted();
		draft.setStatus(SubmissionStatus.DRAFT);
		when(submissionService.requireEntity(ID)).thenReturn(draft);

		assertThatThrownBy(() -> service.deposit(ID, request(ID, VerdictResult.PASS, 10, 0, 10)))
				.isInstanceOf(ConflictException.class)
				.hasMessageContaining("pas éligible");
		verifyNoInteractions(verdictRepository);
	}

	@Test
	void depositOnSubmittedSubmissionMovesToVerifyingThenAppliesVerdict() {
		Submission submission = submitted();
		when(submissionService.requireEntity(ID)).thenReturn(submission);
		when(submissionService.markVerifying(ID)).thenAnswer(invocation -> {
			submission.markVerifying();
			return SubmissionResponse.from(submission);
		});
		when(verdictRepository.save(any(Verdict.class))).thenAnswer(invocation -> invocation.getArgument(0));

		VerdictResponse response = service.deposit(ID, request(ID, VerdictResult.PASS, 10, 0, 10));

		assertThat(response.submissionId()).isEqualTo(ID);
		assertThat(response.result()).isEqualTo(VerdictResult.PASS);
		assertThat(response.total()).isEqualTo(10);
		assertThat(response.coverage()).isEqualTo(80.0);
		verify(submissionService).markVerifying(ID);
		verify(submissionService).applyVerdict(ID, VerdictResult.PASS);
	}

	@Test
	void findBySubmissionIdThrowsWhenMissing() {
		when(verdictRepository.findBySubmissionId(ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.findBySubmissionId(ID))
				.isInstanceOf(ResourceNotFoundException.class)
				.hasMessageContaining(ID.toString());
	}
}
