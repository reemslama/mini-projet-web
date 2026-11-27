package tn.esprit.arene.participants.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.arene.participants.entity.*;
import tn.esprit.arene.participants.repository.CandidatureRepository;
import tn.esprit.arene.participants.repository.EquipeRepository;
import tn.esprit.arene.participants.repository.UtilisateurRepository;

import java.util.List;
import java.util.UUID;

/**
 * Données de démonstration chargées au démarrage (base H2 en mémoire).
 * Permet de tester tout de suite GET /api/users, /api/teams, /api/applications.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    /** Défi fictif (en vrai, son id vient de MS1 · Challenges). */
    public static final UUID DEFI_DEMO = UUID.fromString("11111111-1111-1111-1111-111111111111");

    private final UtilisateurRepository utilisateurRepository;
    private final EquipeRepository equipeRepository;
    private final CandidatureRepository candidatureRepository;

    public DataInitializer(UtilisateurRepository utilisateurRepository,
                           EquipeRepository equipeRepository,
                           CandidatureRepository candidatureRepository) {
        this.utilisateurRepository = utilisateurRepository;
        this.equipeRepository = equipeRepository;
        this.candidatureRepository = candidatureRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (utilisateurRepository.count() > 0) {
            return;
        }
        Utilisateur amira = utilisateurRepository.save(new Utilisateur("Amira", "Ben Salah", "amira.bensalah@arene.tn",
                "amira_dev", "amira-bs", "Tunisie", "Tunis", Niveau.SENIOR, List.of("Java", "Spring Boot", "Docker")));
        Utilisateur youssef = utilisateurRepository.save(new Utilisateur("Youssef", "Trabelsi", "youssef.trabelsi@arene.tn",
                "yous_codes", "ytrabelsi", "Tunisie", "Sfax", Niveau.INTERMEDIAIRE, List.of("Angular", "TypeScript")));
        Utilisateur lina = utilisateurRepository.save(new Utilisateur("Lina", "Haddad", "lina.haddad@arene.tn",
                "lina_h", "linahaddad", "Tunisie", "Sousse", Niveau.JUNIOR, List.of("Python", "FastAPI")));

        Equipe equipe = new Equipe();
        equipe.setNom("Les Bâtisseurs");
        equipe.setDescription("Équipe full-stack spécialisée en microservices");
        equipe.setTailleMax(4);
        equipe.setChef(amira);
        equipe.ajouterMembre(amira, RoleEquipe.CHEF);
        equipe.ajouterMembre(youssef, RoleEquipe.MEMBRE);
        equipe = equipeRepository.save(equipe);

        Candidature solo = new Candidature();
        solo.setChallengeId(DEFI_DEMO);
        solo.setType(TypeCandidature.SOLO);
        solo.setDeposant(lina);
        solo.setMotivation("Je veux un premier projet réel pour mon portfolio.");
        candidatureRepository.save(solo);

        Candidature enEquipe = new Candidature();
        enEquipe.setChallengeId(DEFI_DEMO);
        enEquipe.setType(TypeCandidature.EQUIPE);
        enEquipe.setDeposant(amira);
        enEquipe.setEquipe(equipe);
        enEquipe.setMotivation("Notre équipe maîtrise déjà Spring Cloud et Angular.");
        candidatureRepository.save(enEquipe);
    }
}
