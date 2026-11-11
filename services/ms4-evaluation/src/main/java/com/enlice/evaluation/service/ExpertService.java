package com.enlice.evaluation.service;

import com.enlice.evaluation.domain.Expert;
import com.enlice.evaluation.domain.ExpertRepository;
import com.enlice.evaluation.dto.ExpertCreate;
import com.enlice.evaluation.dto.ExpertResponse;
import com.enlice.evaluation.web.ConflictException;
import com.enlice.evaluation.web.NotFoundException;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ExpertService {

    private final ExpertRepository expertRepository;

    public ExpertService(ExpertRepository expertRepository) {
        this.expertRepository = expertRepository;
    }

    @Transactional
    public ExpertResponse create(ExpertCreate request) {
        Optional<Expert> existing = expertRepository.findByKeycloakId(request.keycloakId());
        if (existing.isPresent()) {
            throw new ConflictException("Profil expert déjà existant pour ce keycloakId");
        }
        Expert expert = new Expert(request.keycloakId(), request.displayName(), request.domains());
        return ExpertResponse.from(expertRepository.save(expert));
    }

    @Transactional(readOnly = true)
    public ExpertResponse get(UUID id) {
        return ExpertResponse.from(expertRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Profil expert introuvable : " + id)));
    }
}
