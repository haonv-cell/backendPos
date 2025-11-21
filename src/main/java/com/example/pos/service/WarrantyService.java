package com.example.pos.service;

import com.example.pos.dto.CreateWarrantyRequest;
import com.example.pos.dto.UpdateWarrantyRequest;
import com.example.pos.dto.WarrantyDTO;
import com.example.pos.dto.WarrantyListResponse;
import com.example.pos.entity.Warranty;
import com.example.pos.exception.ResourceNotFoundException;
import com.example.pos.repository.WarrantyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WarrantyService {

    private final WarrantyRepository warrantyRepository;

    @Transactional(readOnly = true)
    public WarrantyListResponse getWarranties(int page, int size, String search, String status, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Warranty> warrantyPage;

        String cleanStatus = (status != null && !status.trim().isEmpty()) ? status.trim() : null;

        if ("DELETED".equalsIgnoreCase(cleanStatus)) {
            warrantyPage = Page.empty(pageable);
        } else if (search != null && !search.trim().isEmpty()) {
            if (cleanStatus != null) {
                warrantyPage = warrantyRepository.searchWarrantiesByStatus(cleanStatus, search.trim(), pageable);
            } else {
                warrantyPage = warrantyRepository.searchWarranties(search.trim(), pageable);
            }
        } else {
            if (cleanStatus != null) {
                warrantyPage = warrantyRepository.findByStatus(cleanStatus, pageable);
            } else {
                warrantyPage = warrantyRepository.findByStatusNot("DELETED", pageable);
            }
        }

        List<WarrantyDTO> warranties = warrantyPage.getContent().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        return WarrantyListResponse.builder()
                .warranties(warranties)
                .currentPage(warrantyPage.getNumber())
                .totalPages(warrantyPage.getTotalPages())
                .totalItems(warrantyPage.getTotalElements())
                .build();
    }

    @Transactional
    public WarrantyDTO createWarranty(CreateWarrantyRequest request) {
        Warranty warranty = Warranty.builder()
                .name(request.getName())
                .description(request.getDescription())
                .duration(request.getDuration())
                .durationUnit(request.getDurationUnit())
                .status("active")
                .build();
        Warranty saved = warrantyRepository.save(warranty);
        return toDTO(saved);
    }

    @Transactional
    public WarrantyDTO updateWarranty(Integer id, UpdateWarrantyRequest request) {
        Warranty warranty = warrantyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Warranty", "id", id));

        if ("DELETED".equalsIgnoreCase(warranty.getStatus())) {
            throw new ResourceNotFoundException("Warranty", "id", id);
        }

        if (request.getName() != null) warranty.setName(request.getName());
        if (request.getDescription() != null) warranty.setDescription(request.getDescription());
        if (request.getDuration() != null) warranty.setDuration(request.getDuration());
        if (request.getDurationUnit() != null) warranty.setDurationUnit(request.getDurationUnit());
        if (request.getStatus() != null) warranty.setStatus(request.getStatus());

        Warranty updated = warrantyRepository.save(warranty);
        return toDTO(updated);
    }

    @Transactional
    public void deleteWarranty(Integer id) {
        Warranty warranty = warrantyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Warranty", "id", id));
        warranty.setStatus("DELETED");
        warrantyRepository.save(warranty);
    }

    private WarrantyDTO toDTO(Warranty w) {
        return WarrantyDTO.builder()
                .id(w.getId())
                .name(w.getName())
                .description(w.getDescription())
                .duration(w.getDuration())
                .durationUnit(w.getDurationUnit())
                .status(w.getStatus())
                .build();
    }
}