package com.logistique.logistique_system_complet.dto;

import lombok.Data;
import com.logistique.logistique_system_complet.model.Colis;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Data
public class ColisRequest {
    @NotNull(message = "Type colis est obligatoire")
    private Colis.TypeColis type;

    @Positive(message = "Poids doit être positif")
    private double poids;

    @NotBlank(message = "Adresse destination est obligatoire")
    private String adresseDestination;

    private String instructionsManutention;
    private Double temperatureMin;
    private Double temperatureMax;
}
