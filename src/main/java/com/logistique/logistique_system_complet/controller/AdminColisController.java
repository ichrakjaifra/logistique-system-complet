package com.logistique.logistique_system_complet.controller;

import com.logistique.logistique_system_complet.dto.ColisRequest;
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
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin/colis")
@CrossOrigin(origins = "*")
@Tag(name = "Admin - Gestion des Colis", description = "API de gestion des colis pour les administrateurs")
public class AdminColisController {

    private final ColisService colisService;
    private final ColisMapper colisMapper;

    public AdminColisController(ColisService colisService, ColisMapper colisMapper) {
        this.colisService = colisService;
        this.colisMapper = colisMapper;
    }

    @GetMapping
    @Operation(summary = "Lister tous les colis avec pagination")
    public ResponseEntity<Page<ColisResponse>> getAllColis(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Colis> colisPage = colisService.getAllColis(pageable);
        Page<ColisResponse> responsePage = colisPage.map(colisMapper::toResponse);
        return ResponseEntity.ok(responsePage);
    }

    @GetMapping("/search")
    @Operation(summary = "Rechercher des colis par adresse")
    public ResponseEntity<Page<ColisResponse>> searchColisByAdresse(
            @RequestParam String adresse,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Colis> colisPage = colisService.searchColisByAdresse(adresse, pageable);
        Page<ColisResponse> responsePage = colisPage.map(colisMapper::toResponse);
        return ResponseEntity.ok(responsePage);
    }

    @GetMapping("/filter")
    @Operation(summary = "Filtrer les colis par type et/ou statut")
    public ResponseEntity<Page<ColisResponse>> filterColis(
            @RequestParam(required = false) Colis.TypeColis type,
            @RequestParam(required = false) Colis.StatutColis statut,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Colis> colisPage;

        if (type != null && statut != null) {
            colisPage = colisService.filterColisByTypeAndStatut(type, statut, pageable);
        } else if (type != null) {
            colisPage = colisService.filterColisByType(type, pageable);
        } else if (statut != null) {
            colisPage = colisService.filterColisByStatut(statut, pageable);
        } else {
            colisPage = colisService.getAllColis(pageable);
        }

        Page<ColisResponse> responsePage = colisPage.map(colisMapper::toResponse);
        return ResponseEntity.ok(responsePage);
    }

    @PostMapping
    @Operation(summary = "Créer un nouveau colis")
    public ResponseEntity<ColisResponse> createColis(@Valid @RequestBody ColisRequest request) {
        Colis colis = colisMapper.toEntity(request);
        Colis savedColis = colisService.createColis(colis);
        ColisResponse response = colisMapper.toResponse(savedColis);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier un colis existant")
    public ResponseEntity<ColisResponse> updateColis(@PathVariable String id, @Valid @RequestBody ColisRequest request) {
        Colis colis = colisMapper.toEntity(request);
        colis.setId(id);
        Colis updatedColis = colisService.updateColis(id, colis);
        ColisResponse response = colisMapper.toResponse(updatedColis);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{colisId}/assign/{transporteurId}")
    @Operation(summary = "Assigner un colis à un transporteur")
    public ResponseEntity<ColisResponse> assignerColis(@PathVariable String colisId, @PathVariable String transporteurId) {
        Colis colis = colisService.assignerColis(colisId, transporteurId);
        ColisResponse response = colisMapper.toResponse(colis);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/statut")
    @Operation(summary = "Mettre à jour le statut d'un colis")
    public ResponseEntity<ColisResponse> updateStatut(@PathVariable String id, @RequestBody Colis.StatutColis statut) {
        Colis colis = colisService.updateStatutColis(id, statut, null);
        ColisResponse response = colisMapper.toResponse(colis);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un colis")
    public ResponseEntity<Void> deleteColis(@PathVariable String id) {
        colisService.deleteColis(id);
        return ResponseEntity.ok().build();
    }
}