package es.miw.tfm.linkal.infrastructure.jpa.entities;

import es.miw.tfm.linkal.domain.model.Influencer;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class InfluencerEntityTest {
    // -------------------------------------------------------------------------
    //  Constructor InfluencerEntity(Influencer)
    // -------------------------------------------------------------------------

    @Test
    void constructor_shouldCopyBaseFields() {
        Influencer influencer = buildInfluencer();

        InfluencerEntity entity = new InfluencerEntity(influencer);

        assertEquals("Irene", entity.getName());
        assertEquals("irene@test.com", entity.getEmail());
        assertEquals("hashedPass", entity.getPassword());
        assertEquals("600111222", entity.getPhoneNumber());
        assertEquals("Descripción de prueba", entity.getDescription());
    }

    @Test
    void constructor_shouldCopyInfluencerSpecificFields() {
        Influencer influencer = buildInfluencer();

        InfluencerEntity entity = new InfluencerEntity(influencer);

        assertEquals("ArtistIrene", entity.getArtisticName());
        assertEquals("@irene_ig", entity.getInstagram());
        assertEquals("@irene_tt", entity.getTiktok());
        assertEquals("@irene_yt", entity.getYoutube());
    }

    @Test
    void constructor_shouldAlwaysSetVerifiedFalse() {
        Influencer influencer = buildInfluencer();
        influencer.setVerified(true); // aunque venga true, debe quedar false

        InfluencerEntity entity = new InfluencerEntity(influencer);

        assertFalse(entity.getVerified(),
                "verified debe ser false independientemente del valor de entrada");
    }

    @Test
    void constructor_shouldCopyInterestsList() {
        Influencer influencer = buildInfluencer();
        influencer.setInterests(List.of("Moda", "Viajes", "Fitness"));

        InfluencerEntity entity = new InfluencerEntity(influencer);

        assertEquals(3, entity.getInterests().size());
        assertTrue(entity.getInterests().containsAll(List.of("Moda", "Viajes", "Fitness")));
    }

    @Test
    void constructor_shouldHandleNullInterests() {
        Influencer influencer = buildInfluencer();
        influencer.setInterests(null);

        InfluencerEntity entity = new InfluencerEntity(influencer);

        assertNotNull(entity.getInterests());
    }

    @Test
    void constructor_shouldProduceIndependentInterestsList() {
        List<String> originalInterests = new ArrayList<>(List.of("Moda", "Viajes"));
        Influencer influencer = buildInfluencer();
        influencer.setInterests(originalInterests);

        InfluencerEntity entity = new InfluencerEntity(influencer);
        originalInterests.add("Extra");

        assertEquals(2, entity.getInterests().size());
    }

    // -------------------------------------------------------------------------
    //  toInfluencer()
    // -------------------------------------------------------------------------

    @Test
    void toInfluencer_shouldMapBaseFields() {
        InfluencerEntity entity = buildInfluencerEntity();

        Influencer influencer = entity.toInfluencer();

        assertEquals("Irene", influencer.getName());
        assertEquals("irene@test.com", influencer.getEmail());
        assertEquals("hashedPass", influencer.getPassword());
    }

    @Test
    void toInfluencer_shouldMapInfluencerSpecificFields() {
        InfluencerEntity entity = buildInfluencerEntity();

        Influencer influencer = entity.toInfluencer();

        assertEquals("ArtistIrene", influencer.getArtisticName());
        assertEquals("@irene_ig", influencer.getInstagram());
        assertEquals("@irene_tt", influencer.getTiktok());
        assertEquals("@irene_yt", influencer.getYoutube());
    }

    @Test
    void toInfluencer_shouldMapInterestsList() {
        InfluencerEntity entity = buildInfluencerEntity();
        entity.setInterests(new ArrayList<>(List.of("Moda", "Viajes")));

        Influencer influencer = entity.toInfluencer();

        assertEquals(2, influencer.getInterests().size());
        assertTrue(influencer.getInterests().containsAll(List.of("Moda", "Viajes")));
    }

    @Test
    void toInfluencer_shouldReturnNewInstanceEachTime() {
        InfluencerEntity entity = buildInfluencerEntity();

        Influencer i1 = entity.toInfluencer();
        Influencer i2 = entity.toInfluencer();

        assertNotSame(i1, i2);
    }

    // -------------------------------------------------------------------------
    //  helpers
    // -------------------------------------------------------------------------

    private Influencer buildInfluencer() {
        Influencer influencer = new Influencer();
        influencer.setName("Irene");
        influencer.setEmail("irene@test.com");
        influencer.setPassword("hashedPass");
        influencer.setPhoneNumber("600111222");
        influencer.setDescription("Descripción de prueba");
        influencer.setArtisticName("ArtistIrene");
        influencer.setInstagram("@irene_ig");
        influencer.setTiktok("@irene_tt");
        influencer.setYoutube("@irene_yt");
        return influencer;
    }

    private InfluencerEntity buildInfluencerEntity() {
        InfluencerEntity entity = new InfluencerEntity();
        entity.setId(UUID.randomUUID());
        entity.setName("Irene");
        entity.setEmail("irene@test.com");
        entity.setPassword("hashedPass");
        entity.setArtisticName("ArtistIrene");
        entity.setInstagram("@irene_ig");
        entity.setTiktok("@irene_tt");
        entity.setYoutube("@irene_yt");
        entity.setInterests(new ArrayList<>());
        entity.setVerified(false);
        return entity;
    }
}
