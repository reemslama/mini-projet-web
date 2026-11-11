package com.enlice.participants.service;

import com.enlice.participants.domain.Developer;
import com.enlice.participants.domain.DeveloperRepository;
import com.enlice.participants.dto.DeveloperCreate;
import com.enlice.participants.dto.DeveloperResponse;
import com.enlice.participants.web.ConflictException;
import com.enlice.participants.web.NotFoundException;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeveloperService {

    private final DeveloperRepository developerRepository;

    public DeveloperService(DeveloperRepository developerRepository) {
        this.developerRepository = developerRepository;
    }

    @Transactional
    public DeveloperResponse create(DeveloperCreate request) {
        Optional<Developer> existing = developerRepository.findByKeycloakId(request.keycloakId());
        if (existing.isPresent()) {
            throw new ConflictException("Profil développeur déjà existant pour ce keycloakId");
        }
        Developer developer = new Developer(request.keycloakId(), request.displayName(), request.email(), request.skills(),
                Instant.now());
        return DeveloperResponse.from(developerRepository.save(developer));
    }

    @Transactional(readOnly = true)
    public DeveloperResponse get(UUID id) {
        return DeveloperResponse.from(developerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Profil développeur introuvable : " + id)));
    }

    @Transactional(readOnly = true)
    public DeveloperResponse getByKeycloakId(String keycloakId) {
        Developer developer = developerRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new NotFoundException("Profil développeur introuvable pour ce compte : " + keycloakId));
        return DeveloperResponse.from(developer);
    }
}
