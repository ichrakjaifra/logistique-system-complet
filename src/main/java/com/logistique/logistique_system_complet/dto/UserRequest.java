package com.logistique.logistique_system_complet.dto;

import lombok.Data;
import com.logistique.logistique_system_complet.model.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
public class UserRequest {
    @NotBlank(message = "Login est obligatoire")
    private String login;

    @NotBlank(message = "Password est obligatoire")
    private String password;

    @NotNull(message = "Role est obligatoire")
    private User.Role role;

    private User.Specialite specialite;
}