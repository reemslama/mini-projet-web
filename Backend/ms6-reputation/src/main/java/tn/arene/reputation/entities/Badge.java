package tn.arene.reputation.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** Badge detenu par un profil (ManyToOne vers ReputationProfile). */
@Entity
@Table(name = "badge", uniqueConstraints = @UniqueConstraint(columnNames = {"profile_id", "code"}))
@Getter
@Setter
@NoArgsConstructor
public class Badge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String code;

    @Column(nullable = false)
    private String label;

    private LocalDateTime awardedAt = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "profile_id")
    @JsonIgnore
    private ReputationProfile profile;

    public static Badge award(String code, String label) {
        Badge b = new Badge();
        b.code = code;
        b.label = label;
        return b;
    }

    public boolean matches(String code) {
        return this.code.equalsIgnoreCase(code);
    }
}
