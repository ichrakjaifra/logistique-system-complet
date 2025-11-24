package com.logistique.logistique_system_complet.service;

import com.logistique.logistique_system_complet.model.Colis;
import com.logistique.logistique_system_complet.model.User;
import com.logistique.logistique_system_complet.repository.ColisRepository;
import com.logistique.logistique_system_complet.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class ColisService {

    private final ColisRepository colisRepository;
    private final UserRepository userRepository;

    public ColisService(ColisRepository colisRepository, UserRepository userRepository) {
        this.colisRepository = colisRepository;
        this.userRepository = userRepository;
    }

    public Colis createColis(Colis colis) {
        colis.setDateCreation(LocalDateTime.now());
        colis.setDateModification(LocalDateTime.now());
        colis.setStatut(Colis.StatutColis.EN_ATTENTE);
        return colisRepository.save(colis);
    }

    public Page<Colis> getAllColis(Pageable pageable) {
        return colisRepository.findAll(pageable);
    }

    public Page<Colis> getColisByTransporteur(String transporteurId, Pageable pageable) {
        return colisRepository.findByTransporteurId(transporteurId, pageable);
    }

    public Page<Colis> searchColisByAdresse(String adresse, Pageable pageable) {
        return colisRepository.findByAdresseDestinationContainingIgnoreCase(adresse, pageable);
    }

    public Page<Colis> searchColisByAdresseAndTransporteur(String adresse, String transporteurId, Pageable pageable) {
        return colisRepository.findByAdresseDestinationContainingIgnoreCaseAndTransporteurId(adresse, transporteurId, pageable);
    }

    public Page<Colis> filterColisByType(Colis.TypeColis type, Pageable pageable) {
        return colisRepository.findByType(type, pageable);
    }

    public Page<Colis> filterColisByStatut(Colis.StatutColis statut, Pageable pageable) {
        return colisRepository.findByStatut(statut, pageable);
    }

    public Page<Colis> filterColisByTypeAndStatut(Colis.TypeColis type, Colis.StatutColis statut, Pageable pageable) {
        return colisRepository.findByTypeAndStatut(type, statut, pageable);
    }

    public Optional<Colis> getColisById(String id) {
        return colisRepository.findById(id);
    }

    public Colis updateColis(String id, Colis colisDetails) {
        Colis colis = colisRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Colis non trouvé"));

        colis.setPoids(colisDetails.getPoids());
        colis.setAdresseDestination(colisDetails.getAdresseDestination());
        colis.setDateModification(LocalDateTime.now());

        if (colis.getType() == Colis.TypeColis.FRAGILE) {
            colis.setInstructionsManutention(colisDetails.getInstructionsManutention());
        } else if (colis.getType() == Colis.TypeColis.FRIGO) {
            colis.setTemperatureMin(colisDetails.getTemperatureMin());
            colis.setTemperatureMax(colisDetails.getTemperatureMax());
        }

        return colisRepository.save(colis);
    }

    public Colis assignerColis(String colisId, String transporteurId) {
        Colis colis = colisRepository.findById(colisId)
                .orElseThrow(() -> new RuntimeException("Colis non trouvé"));

        User transporteur = userRepository.findById(transporteurId)
                .filter(u -> u.getRole() == User.Role.TRANSPORTEUR && u.isActive())
                .orElseThrow(() -> new RuntimeException("Transporteur non trouvé ou inactif"));

        if (!transporteur.getSpecialite().name().equals(colis.getType().name())) {
            throw new RuntimeException("Le transporteur n'a pas la spécialité requise pour ce type de colis");
        }

        if (transporteur.getStatut() != User.StatutTransporteur.DISPONIBLE) {
            throw new RuntimeException("Le transporteur n'est pas disponible");
        }

        colis.setTransporteurId(transporteurId);
        colis.setStatut(Colis.StatutColis.EN_TRANSIT);
        colis.setDateModification(LocalDateTime.now());

        transporteur.setStatut(User.StatutTransporteur.EN_LIVRAISON);
        userRepository.save(transporteur);

        return colisRepository.save(colis);
    }

    public Colis updateStatutColis(String colisId, Colis.StatutColis nouveauStatut, String transporteurId) {
        Colis colis = colisRepository.findById(colisId)
                .orElseThrow(() -> new RuntimeException("Colis non trouvé"));

        if (transporteurId != null && !colis.getTransporteurId().equals(transporteurId)) {
            throw new RuntimeException("Vous n'êtes pas autorisé à modifier ce colis");
        }

        colis.setStatut(nouveauStatut);
        colis.setDateModification(LocalDateTime.now());

        if (nouveauStatut == Colis.StatutColis.LIVRE || nouveauStatut == Colis.StatutColis.ANNULE) {
            if (colis.getTransporteurId() != null) {
                userRepository.findById(colis.getTransporteurId()).ifPresent(transporteur -> {
                    transporteur.setStatut(User.StatutTransporteur.DISPONIBLE);
                    userRepository.save(transporteur);
                });
            }
        }

        return colisRepository.save(colis);
    }

    public void deleteColis(String id) {
        colisRepository.deleteById(id);
    }
}
