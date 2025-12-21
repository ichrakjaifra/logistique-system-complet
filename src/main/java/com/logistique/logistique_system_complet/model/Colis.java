package com.logistique.logistique_system_complet.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "colis")
public class Colis {
    @Id
    private String id;

    @NotNull(message = "Type colis est obligatoire")
    private TypeColis type;

    @Positive(message = "Poids doit être positif")
    private double poids;

    @NotBlank(message = "Adresse destination est obligatoire")
    private String adresseDestination;

    private StatutColis statut;

    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
    private String transporteurId;

    private String instructionsManutention;
    private Double temperatureMin;
    private Double temperatureMax;

    public enum TypeColis {
        STANDARD, FRAGILE, FRIGO
    }

    public enum StatutColis {
        EN_ATTENTE, EN_TRANSIT, LIVRE, ANNULE
    }
}
