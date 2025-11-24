package com.logistique.logistique_system_complet.controller;

import com.logistique.logistique_system_complet.dto.ColisResponse;
import com.logistique.logistique_system_complet.mapper.ColisMapper;
import com.logistique.logistique_system_complet.model.Colis;
import com.logistique.logistique_system_complet.service.ColisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transporteur/colis")
@CrossOrigin(origins = "*")
@Tag(name = "Transporteur - Gestion des Colis", description = "API de gestion des colis pour les transporteurs")
public class TransporteurColisController {

    private final ColisService colisService;
    private final ColisMapper colisMapper;

    public TransporteurColisController(ColisService colisService, ColisMapper colisMapper) {
        this.colisService = colisService;
        this.colisMapper = colisMapper;
    }

    @GetMapping
    @Operation(summary = "Lister les colis assignés au transporteur")
    public ResponseEntity<Page<ColisResponse>> getMesColis(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        String transporteurId = getCurrentUserId(authentication);
        Pageable pageable = PageRequest.of(page, size);
        Page<Colis> colisPage = colisService.getColisByTransporteur(transporteurId, pageable);
        Page<ColisResponse> responsePage = colisPage.map(colisMapper::toResponse);
        return ResponseEntity.ok(responsePage);
    }

    @GetMapping("/search")
    @Operation(summary = "Rechercher mes colis par adresse")
    public ResponseEntity<Page<ColisResponse>> searchMesColisByAdresse(
            Authentication authentication,
            @RequestParam String adresse,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        String transporteurId = getCurrentUserId(authentication);
        Pageable pageable = PageRequest.of(page, size);
        Page<Colis> colisPage = colisService.searchColisByAdresseAndTransporteur(adresse, transporteurId, pageable);
        Page<ColisResponse> responsePage = colisPage.map(colisMapper::toResponse);
        return ResponseEntity.ok(responsePage);
    }

    @GetMapping("/filter")
    @Operation(summary = "Filtrer mes colis par statut")
    public ResponseEntity<Page<ColisResponse>> filterMesColis(
            Authentication authentication,
            @RequestParam Colis.StatutColis statut,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        String transporteurId = getCurrentUserId(authentication);
        Pageable pageable = PageRequest.of(page, size);
        Page<Colis> colisPage = colisService.filterColisByStatut(statut, pageable)
                .filter(colis -> colis.getTransporteurId() != null && colis.getTransporteurId().equals(transporteurId));

        Page<ColisResponse> responsePage = colisPage.map(colisMapper::toResponse);
        return ResponseEntity.ok(responsePage);
    }

    @PutMapping("/{id}/statut")
    @Operation(summary = "Mettre à jour le statut de mon colis")
    public ResponseEntity<ColisResponse> updateStatutColis(
            Authentication authentication,
            @PathVariable String id,
            @RequestBody Colis.StatutColis statut) {

        String transporteurId = getCurrentUserId(authentication);
        Colis colis = colisService.updateStatutColis(id, statut, transporteurId);
        ColisResponse response = colisMapper.toResponse(colis);
        return ResponseEntity.ok(response);
    }

    private String getCurrentUserId(Authentication authentication) {
        return authentication.getName();
    }
}