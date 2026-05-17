package es.miw.tfm.linkal.infrastructure.jpa.entities;

import es.miw.tfm.linkal.domain.model.Influencer;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "influencers")
@DiscriminatorValue("INFLUENCER")
public class InfluencerEntity extends UserEntity {

    private String artisticName;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "influencer_interests", joinColumns = @JoinColumn(name = "influencer_id"))
    @Column(name = "interest")
    private List<String> interests = new ArrayList<>();

    private String instagram;

    private String tiktok;

    private String youtube;

    @OneToMany(mappedBy = "influencer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MatchEntity> matches = new ArrayList<>();

    public InfluencerEntity(Influencer influencer) {
        BeanUtils.copyProperties(influencer, this, "interests", "matches");
        this.setVerified(false);
        if (influencer.getInterests() != null) {
            this.interests = new ArrayList<>(influencer.getInterests());
        }
    }

    public Influencer toInfluencer() {
        Influencer influencer = new Influencer();
        BeanUtils.copyProperties(this, influencer, "interests", "matches");
        influencer.setInterests(new ArrayList<>(this.interests));
        return influencer;
    }
}