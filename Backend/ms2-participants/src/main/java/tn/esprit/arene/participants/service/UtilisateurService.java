package tn.esprit.arene.participants.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.arene.participants.entity.Equipe;
import tn.esprit.arene.participants.entity.MembreEquipe;
import tn.esprit.arene.participants.entity.Utilisateur;
import tn.esprit.arene.participants.exception.BusinessException;
import tn.esprit.arene.participants.exception.ResourceNotFoundException;
import tn.esprit.arene.participants.repository.CandidatureRepository;
import tn.esprit.arene.participants.repository.EquipeRepository;
import tn.esprit.arene.participants.repository.MembreEquipeRepository;
import tn.esprit.arene.participants.repository.UtilisateurRepository;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;
    private final MembreEquipeRepository membreRepository;
    private final EquipeRepository equipeRepository;
    private final CandidatureRepository candidatureRepository;

    public UtilisateurService(UtilisateurRepository utilisateurRepository,
                              MembreEquipeRepository membreRepository,
                              EquipeRepository equipeRepository,
                              CandidatureRepository candidatureRepository) {
        this.utilisateurRepository = utilisateurRepository;
        this.membreRepository = membreRepository;
        this.equipeRepository = equipeRepository;
        this.candidatureRepository = candidatureRepository;
    }

    public Utilisateur creer(Utilisateur u) {
        if (utilisateurRepository.existsByEmailIgnoreCase(u.getEmail())) {
            throw new BusinessException("Un utilisateur utilise déjà l'email " + u.getEmail());
        }
        if (utilisateurRepository.existsByPseudoIgnoreCase(u.getPseudo())) {
            throw new BusinessException("Le pseudo " + u.getPseudo() + " est déjà pris");
        }
        u.setId(null);
        return utilisateurRepository.save(u);
    }

    @Transactional(readOnly = true)
    public List<Utilisateur> lister() {
        return utilisateurRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Utilisateur trouver(UUID id) {
        return utilisateurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable : " + id));
    }

    public Utilisateur modifier(UUID id, Utilisateur donnees) {
        Utilisateur u = trouver(id);
        if (!u.getEmail().equalsIgnoreCase(donnees.getEmail())
                && utilisateurRepository.existsByEmailIgnoreCase(donnees.getEmail())) {
            throw new BusinessException("Un utilisateur utilise déjà l'email " + donnees.getEmail());
        }
        if (!u.getPseudo().equalsIgnoreCase(donnees.getPseudo())
                && utilisateurRepository.existsByPseudoIgnoreCase(donnees.getPseudo())) {
            throw new BusinessException("Le pseudo " + donnees.getPseudo() + " est déjà pris");
        }
        u.setPrenom(donnees.getPrenom());
        u.setNom(donnees.getNom());
        u.setEmail(donnees.getEmail());
        u.setPseudo(donnees.getPseudo());
        u.setGithubUsername(donnees.getGithubUsername());
        u.setTelephone(donnees.getTelephone());
        u.setPays(donnees.getPays());
        u.setVille(donnees.getVille());
        u.setBio(donnees.getBio());
        if (donnees.getNiveau() != null) {
            u.setNiveau(donnees.getNiveau());
        }
        u.getCompetences().clear();
        if (donnees.getCompetences() != null) {
            u.getCompetences().addAll(donnees.getCompetences());
        }
        u.setDisponible(donnees.isDisponible());
        if (donnees.getStatut() != null) {
            u.setStatut(donnees.getStatut());
        }
        return u; // dirty checking : sauvegardé à la fin de la transaction
    }

    public void supprimer(UUID id) {
        Utilisateur u = trouver(id);
        if (equipeRepository.existsByChefId(id)) {
            throw new BusinessException("Impossible : cet utilisateur est chef d'une équipe");
        }
        if (candidatureRepository.existsByDeposantId(id)) {
            throw new BusinessException("Impossible : cet utilisateur a des candidatures");
        }
        membreRepository.deleteAll(membreRepository.findByUtilisateurId(id));
        utilisateurRepository.delete(u);
    }

    /** Les équipes dont l'utilisateur est membre. */
    @Transactional(readOnly = true)
    public List<Equipe> equipesDe(UUID id) {
        trouver(id);
        return membreRepository.findByUtilisateurId(id).stream()
                .map(MembreEquipe::getEquipe)
                .toList();
    }
}
