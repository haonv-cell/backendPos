package com.example.pos.service;

import com.example.pos.dto.CreateUnitRequest;
import com.example.pos.dto.MessageResponse;
import com.example.pos.dto.UpdateUnitRequest;
import com.example.pos.dto.UnitDTO;
import com.example.pos.dto.UnitListResponse;
import com.example.pos.entity.Unit;
import com.example.pos.exception.ResourceNotFoundException;
import com.example.pos.repository.UnitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UnitService {

    private final UnitRepository unitRepository;

    @Transactional(readOnly = true)
    public UnitListResponse getUnits(int page, int size, String search, String status, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Unit> unitPage;

        // Clean status
        String cleanStatus = (status != null && !status.trim().isEmpty()) ? status.trim() : null;

        // Security: Prevent searching for DELETED units
        if ("DELETED".equalsIgnoreCase(cleanStatus)) {
            unitPage = Page.empty(pageable);
        }
        // Has search term
        else if (search != null && !search.trim().isEmpty()) {
            if (cleanStatus != null) {
                // Case 1: Search + Status filter
                unitPage = unitRepository.searchUnitsByStatus(cleanStatus, search.trim(), pageable);
            } else {
                // Case 2: Search only
                unitPage = unitRepository.searchUnits(search.trim(), pageable);
            }
        }
        // No search term
        else {
            if (cleanStatus != null) {
                // Case 3: Status filter only
                unitPage = unitRepository.findByStatus(cleanStatus, pageable);
            } else {
                // Case 4: No filter (exclude DELETED)
                unitPage = unitRepository.findByStatusNot("DELETED", pageable);
            }
        }

        // Convert to DTOs
        List<UnitDTO> units = unitPage.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        // Build response
        return UnitListResponse.builder()
                .units(units)
                .currentPage(unitPage.getNumber())
                .totalPages(unitPage.getTotalPages())
                .totalItems(unitPage.getTotalElements())
                .build();
    }

    @Transactional
    public UnitDTO createUnit(CreateUnitRequest request) {
        // Create unit entity
        Unit unit = Unit.builder()
                .name(request.getName())
                .shortName(request.getShortName())
                .noOfProducts(0)
                .status("active")
                .build();

        Unit savedUnit = unitRepository.save(unit);

        return convertToDTO(savedUnit);
    }

    @Transactional
    public UnitDTO updateUnit(Integer id, UpdateUnitRequest request) {
        // Find unit
        Unit unit = unitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Unit", "id", id));

        // Check if already deleted
        if ("DELETED".equalsIgnoreCase(unit.getStatus())) {
            throw new ResourceNotFoundException("Unit", "id", id);
        }

        // Update allowed fields only
        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            unit.setName(request.getName());
        }
        if (request.getShortName() != null && !request.getShortName().trim().isEmpty()) {
            unit.setShortName(request.getShortName());
        }
        if (request.getStatus() != null && !request.getStatus().trim().isEmpty()) {
            unit.setStatus(request.getStatus());
        }

        Unit updatedUnit = unitRepository.save(unit);

        return convertToDTO(updatedUnit);
    }

    @Transactional
    public MessageResponse deleteUnit(Integer id) {
        // Find unit
        Unit unit = unitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Unit", "id", id));

        // Check if already deleted
        if ("DELETED".equalsIgnoreCase(unit.getStatus())) {
            throw new ResourceNotFoundException("Unit", "id", id);
        }

        // Check if unit is being used by products
        if (unit.getNoOfProducts() != null && unit.getNoOfProducts() > 0) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Cannot delete unit because it is being used by " + unit.getNoOfProducts() + " product(s)"
            );
        }

        // Soft delete
        unit.setStatus("DELETED");
        unitRepository.save(unit);

        return MessageResponse.of("Unit deleted successfully");
    }

    private UnitDTO convertToDTO(Unit unit) {
        return UnitDTO.builder()
                .id(unit.getId())
                .name(unit.getName())
                .shortName(unit.getShortName())
                .noOfProducts(unit.getNoOfProducts())
                .createdAt(unit.getCreatedAt())
                .status(unit.getStatus())
                .build();
    }
}

