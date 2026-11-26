package tn.esprit.autoloc.ms4evaluation.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.esprit.autoloc.ms4evaluation.entities.Expert;
import tn.esprit.autoloc.ms4evaluation.services.IExpertService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/experts")
@RequiredArgsConstructor
public class ExpertRestController {

    private final IExpertService expertService;

    @PostMapping("/add")
    public Expert addExpert(@RequestBody Expert expert) {
        return expertService.addExpert(expert);
    }

    @GetMapping("/getAll")
    public List<Expert> getAllExperts() {
        return expertService.findAll();
    }

    @GetMapping("/{id}")
    public Expert getExpert(@PathVariable UUID id) {
        return expertService.getById(id);
    }

    @PutMapping("/update/{id}")
    public Expert updateExpert(@PathVariable UUID id,
                               @RequestBody Expert expert) {
        return expertService.updateExpert(id, expert);
    }

    @DeleteMapping("/delete/{id}")
    public void deleteExpert(@PathVariable UUID id) {
        expertService.deleteExpert(id);
    }
}