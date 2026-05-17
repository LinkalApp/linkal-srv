package es.miw.tfm.linkal.infrastructure.jpa.entities;

import es.miw.tfm.linkal.domain.model.Business;
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
@Table(name = "businesses")
@DiscriminatorValue("BUSINESS")
public class BusinessEntity extends UserEntity {

    private String address;
    private String province;
    private String website;
    private String category;

    @Builder.Default
    @OneToMany(mappedBy = "business", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CampaignEntity> campaigns = new ArrayList<>();

    public BusinessEntity(Business business) {
        BeanUtils.copyProperties(business, this);
    }

    public Business toBusiness() {
        Business business = new Business();
        BeanUtils.copyProperties(this, business);
        return business;
    }
}
