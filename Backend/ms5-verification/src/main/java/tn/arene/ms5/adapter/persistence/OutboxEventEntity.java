package tn.arene.ms5.adapter.persistence;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

@Entity
@Table(name = "outbox_events")
public class OutboxEventEntity {

	@Id
	@Column(name = "id")
	private UUID id;

	@Column(name = "event_type", nullable = false)
	private String eventType;

	@Column(name = "routing_key", nullable = false)
	private String routingKey;

	@Column(name = "aggregation_id", nullable = false)
	private UUID aggregationId;

	@Lob
	@Column(name = "payload", nullable = false)
	private String payload;

	@Column(name = "status", nullable = false)
	private String status;

	@Column(name = "attempts", nullable = false)
	private int attempts;

	@Column(name = "available_at", nullable = false)
	private LocalDateTime availableAt;

	@Column(name = "published_at")
	private LocalDateTime publishedAt;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	public UUID getId() { return id; }
	public void setId(UUID id) { this.id = id; }
	public String getEventType() { return eventType; }
	public void setEventType(String eventType) { this.eventType = eventType; }
	public String getRoutingKey() { return routingKey; }
	public void setRoutingKey(String routingKey) { this.routingKey = routingKey; }
	public UUID getAggregationId() { return aggregationId; }
	public void setAggregationId(UUID aggregationId) { this.aggregationId = aggregationId; }
	public String getPayload() { return payload; }
	public void setPayload(String payload) { this.payload = payload; }
	public String getStatus() { return status; }
	public void setStatus(String status) { this.status = status; }
	public int getAttempts() { return attempts; }
	public void setAttempts(int attempts) { this.attempts = attempts; }
	public LocalDateTime getAvailableAt() { return availableAt; }
	public void setAvailableAt(LocalDateTime availableAt) { this.availableAt = availableAt; }
	public LocalDateTime getPublishedAt() { return publishedAt; }
	public void setPublishedAt(LocalDateTime publishedAt) { this.publishedAt = publishedAt; }
	public LocalDateTime getCreatedAt() { return createdAt; }
	public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
