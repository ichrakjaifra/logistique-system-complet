package com.logistique.logistique_system_complet.dto;

import lombok.Data;
import com.logistique.logistique_system_complet.model.Colis;
import java.time.LocalDateTime;

@Data
public class ColisResponse {
    private String id;
    private Colis.TypeColis type;
    private double poids;
    private String adresseDestination;
    private Colis.StatutColis statut;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
    private String transporteurId;
    private String transporteurLogin;
    private String instructionsManutention;
    private Double temperatureMin;
    private Double temperatureMax;
}
