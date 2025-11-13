package com.example.pos.service;

import com.example.pos.dto.CreateStoreRequest;
import com.example.pos.dto.MessageResponse;
import com.example.pos.dto.StoreDTO;
import com.example.pos.dto.StoreListResponse;
import com.example.pos.dto.UpdateStoreRequest;
import com.example.pos.entity.Role;
import com.example.pos.entity.Store;
import com.example.pos.entity.User;
import com.example.pos.entity.Warehouse;
import com.example.pos.repository.StoreRepository;
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
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StoreService {

    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private final WarehouseRepository warehouseRepository;

    @Transactional(readOnly = true)
    public StoreListResponse getStores(int page, int size, String search, String status, String sortBy,
                                       String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Store> storePage;

        // Clean status
        String cleanStatus = (status != null && !status.trim().isEmpty()) ? status.trim() : null;

        // Security: Prevent searching for DELETED stores
        if ("DELETED".equalsIgnoreCase(cleanStatus)) {
            storePage = Page.empty(pageable);
        }
        // Has search term
        else if (search != null && !search.trim().isEmpty()) {
            if (cleanStatus != null) {
                // Case 1: Search + Status filter
                storePage = storeRepository.searchStoresByStatus(cleanStatus, search.trim(), pageable);
            } else {
                // Case 2: Search only
                storePage = storeRepository.searchStores(search.trim(), pageable);
            }
        }
        // No search term
        else {
            if (cleanStatus != null) {
                // Case 3: Status filter only
                storePage = storeRepository.findByStatus(cleanStatus, pageable);
            } else {
                // Case 4: No filter (exclude DELETED)
                storePage = storeRepository.findByStatusNot("DELETED", pageable);
            }
        }

        // Convert to DTOs
        List<StoreDTO> stores = storePage.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        // Build response
        return StoreListResponse.builder()
                .stores(stores)
                .totalElements(storePage.getTotalElements())
                .totalPages(storePage.getTotalPages())
                .currentPage(storePage.getNumber())
                .pageSize(storePage.getSize())
                .build();
    }

    @Transactional
    public StoreDTO createStore(CreateStoreRequest request) {
        // Validate email uniqueness
        Optional<Store> existingEmail = storeRepository.findByEmail(request.getEmail());
        if (existingEmail.isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Email already exists"
            );
        }

        // Validate code uniqueness (if provided)
        if (request.getCode() != null && !request.getCode().trim().isEmpty()) {
            Optional<Store> existingCode = storeRepository.findByCode(request.getCode());
            if (existingCode.isPresent()) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Store code already exists"
                );
            }
        }

        // Validate userId exists and has STORE_OWNER role
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "User ID " + request.getUserId() + " does not exist"
                ));

        validateStoreOwnerRole(user);

        // Validate warehouseId exists and is not deleted
        Warehouse warehouse = warehouseRepository.findById(request.getWarehouseId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Warehouse ID " + request.getWarehouseId() + " does not exist"
                ));

        if ("DELETED".equalsIgnoreCase(warehouse.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Cannot assign store to a deleted warehouse"
            );
        }

        // Create store entity with userName from User
        Store store = Store.builder()
                .code(request.getCode())
                .name(request.getName())
                .userName(user.getName())  // Set userName from User entity
                .email(request.getEmail())
                .phone(request.getPhone())
                .address(request.getAddress())
                .city(request.getCity())
                .country(request.getCountry())
                .warehouseId(request.getWarehouseId())
                .userId(request.getUserId())
                .totalProducts(0)
                .totalStock(0)
                .status("active")
                .build();

        Store savedStore = storeRepository.save(store);

        // Fetch the store with relationships loaded
        Store storeWithRelations = storeRepository.findByIdWithRelations(savedStore.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Failed to retrieve created store"
                ));

        return convertToDTO(storeWithRelations);
    }

    @Transactional
    public StoreDTO updateStore(Integer id, UpdateStoreRequest request) {
        // Find store
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Store with ID " + id + " not found"
                ));

        // Validate email uniqueness (if changed)
        if (request.getEmail() != null && !request.getEmail().equals(store.getEmail())) {
            Optional<Store> existingEmail = storeRepository.findByEmail(request.getEmail());
            if (existingEmail.isPresent()) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Email already exists"
                );
            }
            store.setEmail(request.getEmail());
        }

        // Validate code uniqueness (if changed)
        if (request.getCode() != null && !request.getCode().equals(store.getCode())) {
            Optional<Store> existingCode = storeRepository.findByCode(request.getCode());
            if (existingCode.isPresent()) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Store code already exists"
                );
            }
            store.setCode(request.getCode());
        }

        // Validate userId if changed
        if (request.getUserId() != null && !request.getUserId().equals(store.getUserId())) {
            User user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "User ID " + request.getUserId() + " does not exist"
                    ));

            validateStoreOwnerRole(user);

            store.setUserId(request.getUserId());
            store.setUserName(user.getName());  // Update userName when userId changes
        }

        // Validate warehouseId if changed
        if (request.getWarehouseId() != null && !request.getWarehouseId().equals(store.getWarehouseId())) {
            Warehouse warehouse = warehouseRepository.findById(request.getWarehouseId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Warehouse ID " + request.getWarehouseId() + " does not exist"
                    ));

            if ("DELETED".equalsIgnoreCase(warehouse.getStatus())) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Cannot assign store to a deleted warehouse"
                );
            }

            store.setWarehouseId(request.getWarehouseId());
        }

        // Update allowed fields only
        if (request.getName() != null) {
            store.setName(request.getName());
        }
        if (request.getPhone() != null) {
            store.setPhone(request.getPhone());
        }
        if (request.getAddress() != null) {
            store.setAddress(request.getAddress());
        }
        if (request.getCity() != null) {
            store.setCity(request.getCity());
        }
        if (request.getCountry() != null) {
            store.setCountry(request.getCountry());
        }

        Store updatedStore = storeRepository.save(store);

        // Fetch the store with relationships loaded
        Store storeWithRelations = storeRepository.findByIdWithRelations(updatedStore.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Failed to retrieve updated store"
                ));

        return convertToDTO(storeWithRelations);
    }

    @Transactional
    public MessageResponse deleteStore(Integer id) {
        // Find store
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Store with ID " + id + " not found"
                ));

        // Check if already deleted
        if ("DELETED".equalsIgnoreCase(store.getStatus())) {
            return MessageResponse.of("Store already deleted");
        }

        // Check stock
        if (store.getTotalStock() != null && store.getTotalStock() > 0) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Không thể xóa cửa hàng vì vẫn còn tồn kho."
            );
        }

        // Soft delete
        store.setStatus("DELETED");
        storeRepository.save(store);

        return MessageResponse.of("Store deleted successfully");
    }

    /**
     * Validate user role for store management
     * Only STORE_OWNER can manage stores
     */
    private void validateStoreOwnerRole(User user) {
        if (user.getRole() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "User does not have a role assigned"
            );
        }

        if (user.getRole() != Role.STORE_OWNER) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "User must have STORE_OWNER role to manage stores. Current role: " + user.getRole()
            );
        }
    }

    private StoreDTO convertToDTO(Store store) {
        String userName = store.getUserName();  // Get from database field first
        String warehouseName = null;

        // Fallback: Get userName from User relationship if not in database
        if (userName == null) {
            if (store.getUser() != null) {
                userName = store.getUser().getName();
            } else if (store.getUserId() != null) {
                // Last resort: fetch user manually if relationship not loaded
                userName = userRepository.findById(store.getUserId())
                        .map(User::getName)
                        .orElse(null);
            }
        }

        // Get warehouseName from Warehouse relationship or fetch manually
        if (store.getWarehouse() != null) {
            warehouseName = store.getWarehouse().getName();
        } else if (store.getWarehouseId() != null) {
            // Fallback: fetch warehouse manually if relationship not loaded
            warehouseName = warehouseRepository.findById(store.getWarehouseId())
                    .map(Warehouse::getName)
                    .orElse(null);
        }

        return StoreDTO.builder()
                .id(store.getId())
                .code(store.getCode())
                .name(store.getName())
                .userName(userName)
                .email(store.getEmail())
                .phone(store.getPhone())
                .address(store.getAddress())
                .city(store.getCity())
                .country(store.getCountry())
                .warehouseId(store.getWarehouseId())
                .warehouseName(warehouseName)
                .userId(store.getUserId())
                .totalProducts(store.getTotalProducts())
                .totalStock(store.getTotalStock())
                .status(store.getStatus())
                .createdAt(store.getCreatedAt())
                .updatedAt(store.getUpdatedAt())
                .build();
    }
}

