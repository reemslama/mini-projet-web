package tn.arene.soumission.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import tn.arene.soumission.dto.SubmissionCreateRequest;
import tn.arene.soumission.dto.SubmissionResponse;
import tn.arene.soumission.entities.Submission;
import tn.arene.soumission.entities.SubmissionStatus;
import tn.arene.soumission.entities.VerdictResult;
import tn.arene.soumission.exceptions.ResourceNotFoundException;
import tn.arene.soumission.repositories.SubmissionRepository;
import tn.arene.soumission.services.impl.SubmissionServiceImpl;

/** Cas d'usage de soumission : persistence, recherche et transitions. */
@ExtendWith(MockitoExtension.class)
class SubmissionServiceImplTest {

	private static final UUID ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
	private static final UUID CHALLENGE_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
	private static final UUID APPLICANT_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

	@Mock
	private SubmissionRepository repository;

	@InjectMocks
	private SubmissionServiceImpl service;

	private Submission draft() {
		return Submission.create(CHALLENGE_ID, APPLICANT_ID, "https://github.com/example/solution.git", "main");
	}

	@Test
	void createPersistsDraftAndReturnsProjection() {
		when(repository.save(any(Submission.class))).thenAnswer(invocation -> invocation.getArgument(0));

		SubmissionResponse response = service.create(
				new SubmissionCreateRequest(CHALLENGE_ID, APPLICANT_ID, "https://github.com/example/solution.git", null));

		ArgumentCaptor<Submission> captor = ArgumentCaptor.forClass(Submission.class);
		verify(repository).save(captor.capture());
		assertThat(captor.getValue().getChallengeId()).isEqualTo(CHALLENGE_ID);
		assertThat(captor.getValue().getApplicantId()).isEqualTo(APPLICANT_ID);
		assertThat(response.status()).isEqualTo(SubmissionStatus.DRAFT);
		assertThat(response.branch()).isEqualTo("main");
	}

	@Test
	void findByIdThrowsWhenMissing() {
		when(repository.findById(ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.findById(ID))
				.isInstanceOf(ResourceNotFoundException.class)
				.hasMessageContaining(ID.toString());
	}

	@Test
	void findByChallengeIdUsesTheStatusFilterOnlyWhenProvided() {
		Submission submission = draft();
		when(repository.findByChallengeId(CHALLENGE_ID)).thenReturn(List.of(submission));
		when(repository.findByChallengeIdAndStatus(CHALLENGE_ID, SubmissionStatus.DRAFT))
				.thenReturn(List.of(submission));

		assertThat(service.findByChallengeId(CHALLENGE_ID, null)).hasSize(1);
		assertThat(service.findByChallengeId(CHALLENGE_ID, SubmissionStatus.DRAFT)).hasSize(1);
		verify(repository).findByChallengeId(CHALLENGE_ID);
		verify(repository).findByChallengeIdAndStatus(CHALLENGE_ID, SubmissionStatus.DRAFT);
	}

	@Test
	void submitUpdatesStatusAndFrozenCommit() {
		Submission submission = draft();
		when(repository.findById(ID)).thenReturn(Optional.of(submission));
		when(repository.save(submission)).thenReturn(submission);

		SubmissionResponse response = service.submit(ID, "abc123", LocalDateTime.now().plusDays(1));

		assertThat(response.status()).isEqualTo(SubmissionStatus.SUBMITTED);
		assertThat(response.commitSha()).isEqualTo("abc123");
		assertThat(response.submittedAt()).isNotNull();
		verify(repository).save(submission);
	}

	@ParameterizedTest
	@CsvSource({ "PASS,PASSED", "FAIL,REJECTED" })
	void applyVerdictSetsTheFinalStatus(VerdictResult result, SubmissionStatus expectedStatus) {
		Submission submission = draft();
		when(repository.findById(ID)).thenReturn(Optional.of(submission));
		when(repository.save(submission)).thenReturn(submission);

		SubmissionResponse response = service.applyVerdict(ID, result);

		assertThat(response.status()).isEqualTo(expectedStatus);
		verify(repository).save(submission);
	}
}
