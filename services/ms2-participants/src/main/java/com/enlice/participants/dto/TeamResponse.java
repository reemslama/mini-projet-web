package com.enlice.participants.dto;

import com.enlice.participants.domain.Team;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

public record TeamResponse(
        UUID id,
        String name,
        UUID ownerId,
        Set<UUID> memberIds) {

    public static TeamResponse from(Team team) {
        return new TeamResponse(team.getId(), team.getName(), team.getOwnerId(), new LinkedHashSet<>(team.getMemberIds()));
    }
}
