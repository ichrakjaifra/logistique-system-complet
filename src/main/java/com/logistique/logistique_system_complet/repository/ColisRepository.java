package com.logistique.logistique_system_complet.repository;

import com.logistique.logistique_system_complet.model.Colis;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ColisRepository extends MongoRepository<Colis, String> {
    Page<Colis> findByTransporteurId(String transporteurId, Pageable pageable);
    Page<Colis> findByAdresseDestinationContainingIgnoreCase(String adresse, Pageable pageable);
    Page<Colis> findByAdresseDestinationContainingIgnoreCaseAndTransporteurId(String adresse, String transporteurId, Pageable pageable);
    Page<Colis> findByType(Colis.TypeColis type, Pageable pageable);
    Page<Colis> findByStatut(Colis.StatutColis statut, Pageable pageable);
    Page<Colis> findByTypeAndStatut(Colis.TypeColis type, Colis.StatutColis statut, Pageable pageable);
    List<Colis> findByTransporteurIdAndStatut(String transporteurId, Colis.StatutColis statut);
}