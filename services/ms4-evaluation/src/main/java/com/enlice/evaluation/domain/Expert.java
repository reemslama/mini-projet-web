package com.enlice.evaluation.domain;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "experts")
public class Expert {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "keycloak_id", nullable = false, unique = true, length = 64)
    private String keycloakId;

    @Column(name = "display_name", nullable = false, length = 120)
    private String displayName;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "expert_domains", joinColumns = @JoinColumn(name = "expert_id"))
    @Column(name = "domain", length = 80)
    private Set<String> domains = new LinkedHashSet<>();

    protected Expert() {
    }

    public Expert(String keycloakId, String displayName, Set<String> domains) {
        this.id = UUID.randomUUID();
        this.keycloakId = keycloakId;
        this.displayName = displayName;
        this.domains = domains == null ? new LinkedHashSet<>() : new LinkedHashSet<>(domains);
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getKeycloakId() { return keycloakId; }
    public void setKeycloakId(String keycloakId) { this.keycloakId = keycloakId; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public Set<String> getDomains() { return domains; }
    public void setDomains(Set<String> domains) { this.domains = domains == null ? new LinkedHashSet<>() : new LinkedHashSet<>(domains); }
}
