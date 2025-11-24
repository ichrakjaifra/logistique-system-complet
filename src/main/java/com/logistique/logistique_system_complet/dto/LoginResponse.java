package com.logistique.logistique_system_complet.dto;

import lombok.Data;
import com.logistique.logistique_system_complet.model.User;

@Data
public class LoginResponse {
    private String token;
    private String login;
    private User.Role role;
    private String userId;
    private String message;

    public LoginResponse(String token, String login, User.Role role, String userId) {
        this.token = token;
        this.login = login;
        this.role = role;
        this.userId = userId;
        this.message = "Connexion réussie";
    }
}
