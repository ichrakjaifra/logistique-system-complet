package com.logistique.logistique_system_complet.mapper;

import com.logistique.logistique_system_complet.dto.ColisRequest;
import com.logistique.logistique_system_complet.dto.ColisResponse;
import com.logistique.logistique_system_complet.model.Colis;
import com.logistique.logistique_system_complet.model.User;
import com.logistique.logistique_system_complet.repository.UserRepository;
import org.springframework.stereotype.Component;
import java.util.Optional;

@Component
public class ColisMapper {

    private final UserRepository userRepository;

    public ColisMapper(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Colis toEntity(ColisRequest request) {
        return Colis.builder()
                .type(request.getType())
                .poids(request.getPoids())
                .adresseDestination(request.getAdresseDestination())
                .instructionsManutention(request.getInstructionsManutention())
                .temperatureMin(request.getTemperatureMin())
                .temperatureMax(request.getTemperatureMax())
                .build();
    }

    public ColisResponse toResponse(Colis colis) {
        ColisResponse response = new ColisResponse();
        response.setId(colis.getId());
        response.setType(colis.getType());
        response.setPoids(colis.getPoids());
        response.setAdresseDestination(colis.getAdresseDestination());
        response.setStatut(colis.getStatut());
        response.setDateCreation(colis.getDateCreation());
        response.setDateModification(colis.getDateModification());
        response.setTransporteurId(colis.getTransporteurId());
        response.setInstructionsManutention(colis.getInstructionsManutention());
        response.setTemperatureMin(colis.getTemperatureMin());
        response.setTemperatureMax(colis.getTemperatureMax());

        if (colis.getTransporteurId() != null) {
            Optional<User> transporteur = userRepository.findById(colis.getTransporteurId());
            transporteur.ifPresent(user -> response.setTransporteurLogin(user.getLogin()));
        }

        return response;
    }
}
