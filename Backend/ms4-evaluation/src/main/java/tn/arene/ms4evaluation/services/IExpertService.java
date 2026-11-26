package tn.arene.ms4evaluation.services;

import tn.arene.ms4evaluation.entities.Expert;

import java.util.List;
import java.util.UUID;

public interface IExpertService {

    Expert addExpert(Expert expert);

    Expert updateExpert(UUID id, Expert expert);

    void deleteExpert(UUID id);

    Expert getById(UUID id);

    List<Expert> findAll();
}