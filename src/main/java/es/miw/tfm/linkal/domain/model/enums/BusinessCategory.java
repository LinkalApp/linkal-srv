package es.miw.tfm.linkal.domain.model.enums;

import java.util.List;

public final class BusinessCategory {

    private BusinessCategory() {}

    public static final List<String> STANDARD = List.of(
            "Moda y Ropa",
            "Belleza y Cosmética",
            "Gastronomía",
            "Tecnología",
            "Fitness y Deporte",
            "Viajes",
            "Gaming",
            "Arte y Diseño",
            "Lifestyle",
            "Restauración y Hostelería"
    );

    public static final String OTHER = "Otra";
}

