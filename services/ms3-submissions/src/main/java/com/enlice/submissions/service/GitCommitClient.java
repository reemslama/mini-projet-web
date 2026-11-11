package com.enlice.submissions.service;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Résout le SHA de commit figé via l'API GitHub (délégation externe).
 * En cas d'indisponibilité, un SHA déterministe de secours est utilisé
 * (le comportement de « freeze » reste inchangé).
 */
@Component
public class GitCommitClient {

    private final RestClient restClient;

    public GitCommitClient(@Value("${GITHUB_API_BASE_URL:https://api.github.com}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    public String resolve(String repoUrl, String branch) {
        try {
            String[] segments = repoUrl.replace("https://", "").split("/");
            if (segments.length < 2) {
                return fallbackSha(repoUrl, branch);
            }
            String owner = segments[0];
            String repo = segments[1];
            JsonNode body = restClient.get().uri("/repos/{owner}/{repo}/commits/{branch}", owner, repo, branch)
                    .retrieve().body(JsonNode.class);
            String sha = body == null ? null : body.path("sha").asText();
            if (sha == null || sha.isBlank()) {
                return fallbackSha(repoUrl, branch);
            }
            return sha;
        } catch (Exception ex) {
            return fallbackSha(repoUrl, branch);
        }
    }

    private String fallbackSha(String repoUrl, String branch) {
        String normalized = repoUrl.replace("https://", "").replace("http://", "").replace("/", "_");
        return Integer.toHexString(Math.abs((normalized + "|" + branch).hashCode()) * 2654435761)
                .substring(0, 7);
    }
}
