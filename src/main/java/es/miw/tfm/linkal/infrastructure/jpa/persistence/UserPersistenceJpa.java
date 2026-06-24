package es.miw.tfm.linkal.infrastructure.jpa.persistence;

import es.miw.tfm.linkal.domain.exceptions.NotFoundException;
import es.miw.tfm.linkal.domain.model.AdminUserDetail;
import es.miw.tfm.linkal.domain.model.enums.RoleType;
import es.miw.tfm.linkal.domain.persistence.UserPersistence;
import es.miw.tfm.linkal.infrastructure.jpa.entities.BusinessEntity;
import es.miw.tfm.linkal.infrastructure.jpa.entities.InfluencerEntity;
import es.miw.tfm.linkal.infrastructure.jpa.entities.UserEntity;
import es.miw.tfm.linkal.infrastructure.jpa.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class UserPersistenceJpa implements UserPersistence {
    private final UserRepository userRepository;

    @Override
    public List<AdminUserDetail> findAll(RoleType role, Boolean verified) {
        List<UserEntity> entities = userRepository.findAllFiltered(role, verified);
        List<AdminUserDetail> result = new ArrayList<>();
        for (UserEntity entity : entities) {
            result.add(toAdminUserDetail(entity));
        }
        return result;
    }

    @Override
    public AdminUserDetail findById(UUID id) {
        UserEntity entity = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found: " + id));
        return toAdminUserDetail(entity);
    }

    @Override
    public AdminUserDetail updateVerified(UUID id, Boolean verified) {
        UserEntity entity = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found: " + id));
        entity.setVerified(verified);
        UserEntity saved = userRepository.save(entity);
        return toAdminUserDetail(saved);
    }

    // Helpers --------------------------------------------------------------------------

    private AdminUserDetail toAdminUserDetail(UserEntity entity) {
        AdminUserDetail.AdminUserDetailBuilder builder = AdminUserDetail.builder()
                .id(entity.getId())
                .name(entity.getName())
                .email(entity.getEmail())
                .phoneNumber(entity.getPhoneNumber())
                .description(entity.getDescription())
                .verified(entity.getVerified())
                .role(entity.getRole());

        if (entity instanceof InfluencerEntity influencer) {
            builder.artisticName(influencer.getArtisticName())
                    .interests(influencer.getInterests() != null
                            ? new ArrayList<>(influencer.getInterests())
                            : new ArrayList<>())
                    .instagram(influencer.getInstagram())
                    .tiktok(influencer.getTiktok())
                    .youtube(influencer.getYoutube());
        } else if (entity instanceof BusinessEntity business) {
            builder.address(business.getAddress())
                    .province(business.getProvince())
                    .website(business.getWebsite())
                    .category(business.getCategory());
        }

        return builder.build();
    }
}
