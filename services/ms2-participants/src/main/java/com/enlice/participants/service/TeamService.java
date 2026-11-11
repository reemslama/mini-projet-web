package com.enlice.participants.service;

import com.enlice.participants.domain.Team;
import com.enlice.participants.domain.TeamRepository;
import com.enlice.participants.dto.TeamCreate;
import com.enlice.participants.dto.TeamResponse;
import com.enlice.participants.web.NotFoundException;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TeamService {

    private final TeamRepository teamRepository;

    public TeamService(TeamRepository teamRepository) {
        this.teamRepository = teamRepository;
    }

    @Transactional
    public TeamResponse create(TeamCreate request) {
        Team team = new Team(request.name(), request.ownerId(), request.memberIds(), Instant.now());
        return TeamResponse.from(teamRepository.save(team));
    }

    @Transactional(readOnly = true)
    public TeamResponse get(UUID id) {
        return TeamResponse.from(teamRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Équipe introuvable : " + id)));
    }
}
