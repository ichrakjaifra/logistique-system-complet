package com.logistique.logistique_system_complet.mapper;

import com.logistique.logistique_system_complet.dto.UserRequest;
import com.logistique.logistique_system_complet.dto.UserResponse;
import com.logistique.logistique_system_complet.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public User toEntity(UserRequest request) {
        return User.builder()
                .login(request.getLogin())
                .password(request.getPassword())
                .role(request.getRole())
                .specialite(request.getSpecialite())
                .build();
    }

    public UserResponse toResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setLogin(user.getLogin());
        response.setRole(user.getRole());
        response.setActive(user.isActive());
        response.setStatut(user.getStatut());
        response.setSpecialite(user.getSpecialite());
        return response;
    }
}
