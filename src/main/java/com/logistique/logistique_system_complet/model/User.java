package com.logistique.logistique_system_complet.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "users")
public class User {
    @Id
    private String id;

    @Indexed(unique = true)
    @NotBlank(message = "Login est obligatoire")
    private String login;

    @NotBlank(message = "Password est obligatoire")
    private String password;

    @NotNull(message = "Role est obligatoire")
    private Role role;

    private boolean active;

    private StatutTransporteur statut;
    private Specialite specialite;

    public enum Role {
        ADMIN, TRANSPORTEUR
    }

    public enum StatutTransporteur {
        DISPONIBLE, EN_LIVRAISON
    }

    public enum Specialite {
        STANDARD, FRAGILE, FRIGO
    }
}
