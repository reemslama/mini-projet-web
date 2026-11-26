package tn.esprit.autoloc.ms4evaluation.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.autoloc.ms4evaluation.entities.Expert;
import tn.esprit.autoloc.ms4evaluation.repositories.ExpertRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ExpertServiceImpl implements IExpertService {

    private final ExpertRepository expertRepository;

    @Override
    public Expert addExpert(Expert expert) {
        expert.setId(null);
        return expertRepository.save(expert);
    }

    @Override
    public Expert updateExpert(UUID id, Expert expert) {
        getById(id);
        expert.setId(id);
        return expertRepository.save(expert);
    }

    @Override
    public void deleteExpert(UUID id) {
        expertRepository.deleteById(id);
    }

    @Override
    public Expert getById(UUID id) {
        return expertRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Expert introuvable"));
    }

    @Override
    public List<Expert> findAll() {
        return expertRepository.findAll();
    }
}