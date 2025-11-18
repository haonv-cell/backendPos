package com.example.pos.service;

import com.example.pos.dto.*;
import com.example.pos.dto.subcategory.*;
import com.example.pos.entity.Category;
import com.example.pos.entity.SubCategory;
import com.example.pos.exception.ResourceNotFoundException;
import com.example.pos.repository.CategoryRepository;
import com.example.pos.repository.SubCategoryRepository;
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
public class SubCategoryService {

    private final SubCategoryRepository subCategoryRepository;
    private final CategoryRepository categoryRepository; // Cần thiết để xác thực Category

    /**
     * Lấy danh sách danh mục phụ với phân trang, tìm kiếm và lọc
     */
    @Transactional(readOnly = true)
    public SubCategoryListResponse getSubCategories(
            int page, int size, String search, String status, Integer categoryId, String sortBy, String sortDir) {

        // 1. Sắp xếp (Sort)
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        // 2. Phân trang (Pageable)
        Pageable pageable = PageRequest.of(page, size, sort);

        // 3. Chuẩn bị biến lọc
        Page<SubCategory> subCategoryPage;
        String cleanStatus = (status != null && !status.trim().isEmpty()) ? status.trim() : null;
        String cleanSearch = (search != null && !search.trim().isEmpty()) ? search.trim() : null;

        // 4. Logic truy vấn (phức tạp)
        if ("DELETED".equalsIgnoreCase(cleanStatus)) {
            subCategoryPage = Page.empty(pageable); // Không bao giờ hiển thị mục đã xóa
        }
        // --- Có TÌM KIẾM ---
        else if (cleanSearch != null) {
            if (cleanStatus != null) {
                if (categoryId != null) {
                    // 1. Search + Status + Category
                    subCategoryPage = subCategoryRepository.searchSubCategoriesByStatusAndCategory(cleanStatus, categoryId, cleanSearch, pageable);
                } else {
                    // 2. Search + Status
                    subCategoryPage = subCategoryRepository.searchSubCategoriesByStatus(cleanStatus, cleanSearch, pageable);
                }
            } else { // Không có Status
                if (categoryId != null) {
                    // 3. Search + Category
                    subCategoryPage = subCategoryRepository.searchSubCategoriesByCategory(categoryId, cleanSearch, pageable);
                } else {
                    // 4. Search only
                    subCategoryPage = subCategoryRepository.searchSubCategories(cleanSearch, pageable);
                }
            }
        }
        // --- Không có TÌM KIẾM ---
        else {
            if (cleanStatus != null) {
                if (categoryId != null) {
                    // 5. Status + Category
                    subCategoryPage = subCategoryRepository.findByCategoryIdAndStatus(categoryId, cleanStatus, pageable);
                } else {
                    // 6. Status only
                    subCategoryPage = subCategoryRepository.findByStatus(cleanStatus, pageable);
                }
            } else { // Không có Status
                if (categoryId != null) {
                    // 7. Category only
                    subCategoryPage = subCategoryRepository.findByCategoryIdAndStatusNot(categoryId, "DELETED", pageable);
                } else {
                    // 8. Default (Không lọc gì cả)
                    subCategoryPage = subCategoryRepository.findByStatusNot("DELETED", pageable);
                }
            }
        }

        // 5. Chuyển đổi sang DTO
        List<SubCategoryDTO> subCategories = subCategoryPage.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        // 6. Xây dựng đối tượng Response
        return SubCategoryListResponse.builder()
                .subCategories(subCategories)
                .currentPage(subCategoryPage.getNumber())
                .totalPages(subCategoryPage.getTotalPages())
                .totalItems(subCategoryPage.getTotalElements())
                .build();
    }

    /**
     * Tạo một danh mục phụ mới
     */
    @Transactional
    public SubCategoryDTO createSubCategory(CreateSubCategoryRequest request) {
        // 1. Kiểm tra Category cha có tồn tại không
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));

        // 2. Kiểm tra mã 'code' đã tồn tại chưa
        if (subCategoryRepository.findByCode(request.getCode()).isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "SubCategory with code '" + request.getCode() + "' already exists"
            );
        }

        // 3. Xây dựng Entity
        SubCategory subCategory = SubCategory.builder()
                .name(request.getName())
                .code(request.getCode())
                .category(category) // Gán đối tượng Category
                .description(request.getDescription())
                .imageUrl(request.getImageUrl())
                .status(request.getStatus() != null ? request.getStatus() : "active")
                .build();

        // 4. Lưu và trả về DTO
        SubCategory savedSubCategory = subCategoryRepository.save(subCategory);
        return convertToDTO(savedSubCategory);
    }

    /**
     * Cập nhật một danh mục phụ
     */
    @Transactional
    public SubCategoryDTO updateSubCategory(Integer id, UpdateSubCategoryRequest request) {
        // 1. Tìm danh mục phụ
        SubCategory subCategory = subCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SubCategory", "id", id));

        // 2. Kiểm tra nếu đã bị "xóa mềm"
        if ("DELETED".equalsIgnoreCase(subCategory.getStatus())) {
            throw new ResourceNotFoundException("SubCategory", "id", id);
        }

        // 3. Cập nhật các trường thông thường
        if (request.getName() != null) {
            subCategory.setName(request.getName());
        }
        if (request.getDescription() != null) {
            subCategory.setDescription(request.getDescription());
        }
        if (request.getStatus() != null) {
            subCategory.setStatus(request.getStatus());
        }
        if (request.getImageUrl() != null) {
            subCategory.setImageUrl(request.getImageUrl());
        }

        // 4. Cập nhật 'code' (nếu có thay đổi và cần kiểm tra unique)
        if (request.getCode() != null && !request.getCode().equals(subCategory.getCode())) {
            if (subCategoryRepository.findByCode(request.getCode()).isPresent()) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "SubCategory with code '" + request.getCode() + "' already exists"
                );
            }
            subCategory.setCode(request.getCode());
        }

        // 5. Cập nhật 'categoryId' (nếu có thay đổi)
        if (request.getCategoryId() != null && !request.getCategoryId().equals(subCategory.getCategory().getId())) {
            Category newCategory = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));
            subCategory.setCategory(newCategory);
        }

        // 6. Lưu và trả về DTO
        SubCategory updatedSubCategory = subCategoryRepository.save(subCategory);
        return convertToDTO(updatedSubCategory);
    }

    /**
     * Xóa mềm một danh mục phụ
     */
    @Transactional
    public MessageResponse deleteSubCategory(Integer id) {
        // 1. Tìm
        SubCategory subCategory = subCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SubCategory", "id", id));

        // 2. Kiểm tra
        if ("DELETED".equalsIgnoreCase(subCategory.getStatus())) {
            throw new ResourceNotFoundException("SubCategory", "id", id);
        }

        // 3. Xóa mềm
        subCategory.setStatus("DELETED");
        subCategoryRepository.save(subCategory);

        return MessageResponse.of("SubCategory deleted successfully");
    }

    /**
     * Hàm tiện ích (helper) để chuyển Entity sang DTO
     */
    private SubCategoryDTO convertToDTO(SubCategory subCategory) {
        return SubCategoryDTO.builder()
                .id(subCategory.getId())
                .name(subCategory.getName())
                .code(subCategory.getCode())
                .description(subCategory.getDescription())
                .status(subCategory.getStatus())
                .imageUrl(subCategory.getImageUrl())
                .createdAt(subCategory.getCreatedAt())
                // Lấy thông tin từ Category cha
                .categoryId(subCategory.getCategory().getId())
                .categoryName(subCategory.getCategory().getName())
                .build();
    }
}