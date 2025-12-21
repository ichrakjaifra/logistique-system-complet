package com.logistique.logistique_system_complet.dto;

import lombok.Data;
import com.logistique.logistique_system_complet.model.User;

@Data
public class UserResponse {
    private String id;
    private String login;
    private User.Role role;
    private boolean active;
    private User.StatutTransporteur statut;
    private User.Specialite specialite;
}
