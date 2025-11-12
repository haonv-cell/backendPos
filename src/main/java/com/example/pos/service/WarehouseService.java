package com.example.pos.service;

import com.example.pos.dto.CreateWarehouseRequest;
import com.example.pos.dto.MessageResponse;
import com.example.pos.dto.UpdateWarehouseRequest;
import com.example.pos.dto.WarehouseDTO;
import com.example.pos.dto.WarehouseListResponse;
import com.example.pos.entity.Warehouse;
import com.example.pos.repository.UserRepository;
import com.example.pos.repository.WarehouseRepository;
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
public class WarehouseService {

    private final WarehouseRepository warehouseRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public WarehouseListResponse getWarehouses(int page, int size, String search, String status, String sortBy,
                                                String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Warehouse> warehousePage;

        // Clean status
        String cleanStatus = (status != null && !status.trim().isEmpty()) ? status.trim() : null;

        // Security: Prevent searching for DELETED warehouses
        if ("DELETED".equalsIgnoreCase(cleanStatus)) {
            warehousePage = Page.empty(pageable);
        }
        // Has search term
        else if (search != null && !search.trim().isEmpty()) {
            if (cleanStatus != null) {
                // Case 1: Search + Status filter
                warehousePage = warehouseRepository.searchWarehousesByStatus(cleanStatus, search.trim(), pageable);
            } else {
                // Case 2: Search only
                warehousePage = warehouseRepository.searchWarehouses(search.trim(), pageable);
            }
        }
        // No search term
        else {
            if (cleanStatus != null) {
                // Case 3: Status filter only
                warehousePage = warehouseRepository.findByStatus(cleanStatus, pageable);
            } else {
                // Case 4: No filter (exclude DELETED)
                warehousePage = warehouseRepository.findByStatusNot("DELETED", pageable);
            }
        }

        // Convert to DTOs
        List<WarehouseDTO> warehouses = warehousePage.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        // Build response
        return WarehouseListResponse.builder()
                .warehouses(warehouses)
                .totalElements(warehousePage.getTotalElements())
                .totalPages(warehousePage.getTotalPages())
                .currentPage(warehousePage.getNumber())
                .pageSize(warehousePage.getSize())
                .build();
    }

    @Transactional
    public WarehouseDTO createWarehouse(CreateWarehouseRequest request) {
        // Validate userId exists
        userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "User ID " + request.getUserId() + " does not exist"
                ));

        // Create warehouse entity
        Warehouse warehouse = Warehouse.builder()
                .name(request.getName())
                .contactPerson(request.getContactPerson())
                .phone(request.getPhone())
                .userId(request.getUserId())
                .totalProducts(0)
                .stock(0)
                .qty(0)
                .status("active")
                .build();

        Warehouse savedWarehouse = warehouseRepository.save(warehouse);

        return convertToDTO(savedWarehouse);
    }

    @Transactional
    public WarehouseDTO updateWarehouse(Integer id, UpdateWarehouseRequest request) {
        // Find warehouse
        Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Warehouse with ID " + id + " not found"
                ));

        // Validate userId if changed
        if (request.getUserId() != null && !request.getUserId().equals(warehouse.getUserId())) {
            userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "User ID " + request.getUserId() + " does not exist"
                    ));
            warehouse.setUserId(request.getUserId());
        }

        // Update allowed fields only
        if (request.getName() != null) {
            warehouse.setName(request.getName());
        }
        if (request.getContactPerson() != null) {
            warehouse.setContactPerson(request.getContactPerson());
        }
        if (request.getPhone() != null) {
            warehouse.setPhone(request.getPhone());
        }

        Warehouse updatedWarehouse = warehouseRepository.save(warehouse);

        return convertToDTO(updatedWarehouse);
    }

    @Transactional
    public MessageResponse deleteWarehouse(Integer id) {
        // Find warehouse
        Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Warehouse with ID " + id + " not found"
                ));

        // Check if already deleted
        if ("DELETED".equalsIgnoreCase(warehouse.getStatus())) {
            return MessageResponse.of("Warehouse already deleted");
        }

        // Check stock
        if (warehouse.getStock() != null && warehouse.getStock() > 0) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Không thể xóa kho vì vẫn còn tồn hàng."
            );
        }

        // Soft delete
        warehouse.setStatus("DELETED");
        warehouseRepository.save(warehouse);

        return MessageResponse.of("Warehouse deleted successfully");
    }

    private WarehouseDTO convertToDTO(Warehouse warehouse) {
        String managingUserName = null;
        if (warehouse.getUser() != null) {
            managingUserName = warehouse.getUser().getName();
        }

        return WarehouseDTO.builder()
                .id(warehouse.getId())
                .name(warehouse.getName())
                .contactPerson(warehouse.getContactPerson())
                .phone(warehouse.getPhone())
                .totalProducts(warehouse.getTotalProducts())
                .stock(warehouse.getStock())
                .qty(warehouse.getQty())
                .createdOn(warehouse.getCreatedOn())
                .status(warehouse.getStatus())
                .userId(warehouse.getUserId())
                .managingUserName(managingUserName)
                .build();
    }
}

