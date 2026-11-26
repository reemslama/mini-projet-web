package tn.arene.ms5.adapter.persistence;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Vecteur sérialisé en texte pour H2 ; la colonne pgvector est ajoutée en production (V2). */
@Entity
@Table(name = "code_embeddings")
public class CodeEmbeddingEntity {

	@Id
	@Column(name = "id")
	private UUID id;

	@Column(name = "submission_id", nullable = false)
	private UUID submissionId;

	@Column(name = "challenge_id", nullable = false)
	private UUID challengeId;

	@Column(name = "chunk_id", nullable = false)
	private String chunkId;

	@Column(name = "source_path")
	private String sourcePath;

	@Column(name = "content_sha256")
	private String contentSha256;

	@Column(name = "model_version", nullable = false)
	private String modelVersion;

	@Column(name = "vector_text", nullable = false, length = 8192)
	private String vectorText;

	public UUID getId() { return id; }
	public void setId(UUID id) { this.id = id; }
	public UUID getSubmissionId() { return submissionId; }
	public void setSubmissionId(UUID submissionId) { this.submissionId = submissionId; }
	public UUID getChallengeId() { return challengeId; }
	public void setChallengeId(UUID challengeId) { this.challengeId = challengeId; }
	public String getChunkId() { return chunkId; }
	public void setChunkId(String chunkId) { this.chunkId = chunkId; }
	public String getSourcePath() { return sourcePath; }
	public void setSourcePath(String sourcePath) { this.sourcePath = sourcePath; }
	public String getContentSha256() { return contentSha256; }
	public void setContentSha256(String contentSha256) { this.contentSha256 = contentSha256; }
	public String getModelVersion() { return modelVersion; }
	public void setModelVersion(String modelVersion) { this.modelVersion = modelVersion; }
	public String getVectorText() { return vectorText; }
	public void setVectorText(String vectorText) { this.vectorText = vectorText; }
}
