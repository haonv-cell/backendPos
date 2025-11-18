package com.example.pos.service;

import com.example.pos.dto.CategoryDTO;
import com.example.pos.dto.CategoryListResponse;
import com.example.pos.dto.CreateCategoryRequest;
import com.example.pos.dto.MessageResponse;
import com.example.pos.dto.UpdateCategoryRequest;
import com.example.pos.entity.Category;
import com.example.pos.exception.ResourceNotFoundException;
import com.example.pos.repository.CategoryRepository;
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
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public CategoryListResponse getCategories(int page, int size, String search, String status, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Category> categoryPage;

        // Clean status
        String cleanStatus = (status != null && !status.trim().isEmpty()) ? status.trim() : null;

        // Security: Prevent searching for DELETED categories
        if ("DELETED".equalsIgnoreCase(cleanStatus)) {
            categoryPage = Page.empty(pageable);
        }
        // Has search term
        else if (search != null && !search.trim().isEmpty()) {
            if (cleanStatus != null) {
                // Case 1: Search + Status filter
                categoryPage = categoryRepository.searchCategoriesByStatus(cleanStatus, search.trim(), pageable);
            } else {
                // Case 2: Search only
                categoryPage = categoryRepository.searchCategories(search.trim(), pageable);
            }
        }
        // No search term
        else {
            if (cleanStatus != null) {
                // Case 3: Status filter only
                categoryPage = categoryRepository.findByStatus(cleanStatus, pageable);
            } else {
                // Case 4: No filter (exclude DELETED)
                categoryPage = categoryRepository.findByStatusNot("DELETED", pageable);
            }
        }

        // Convert to DTOs
        List<CategoryDTO> categories = categoryPage.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        // Build response
        return CategoryListResponse.builder()
                .categories(categories)
                .currentPage(categoryPage.getNumber())
                .totalPages(categoryPage.getTotalPages())
                .totalItems(categoryPage.getTotalElements())
                .build();
    }

    @Transactional
    public CategoryDTO createCategory(CreateCategoryRequest request) {
        // Generate slug from name if not provided
        String slug = request.getSlug();
        if (slug == null || slug.trim().isEmpty()) {
            slug = generateSlug(request.getName());
        }

        // Check if slug already exists
        Optional<Category> existingCategory = categoryRepository.findBySlug(slug);
        if (existingCategory.isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Category with slug '" + slug + "' already exists"
            );
        }

        // Create category entity
        Category category = Category.builder()
                .name(request.getName())
                .slug(slug)
                .status(request.getStatus() != null ? request.getStatus() : "active")
                .build();

        Category savedCategory = categoryRepository.save(category);

        return convertToDTO(savedCategory);
    }

    @Transactional
    public CategoryDTO updateCategory(Integer id, UpdateCategoryRequest request) {
        // Find category
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));

        // Check if already deleted
        if ("DELETED".equalsIgnoreCase(category.getStatus())) {
            throw new ResourceNotFoundException("Category", "id", id);
        }

        // Update allowed fields only
        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            category.setName(request.getName());
        }
        
        if (request.getSlug() != null && !request.getSlug().trim().isEmpty()) {
            // Check if new slug is different and already exists
            if (!request.getSlug().equals(category.getSlug())) {
                Optional<Category> existingCategory = categoryRepository.findBySlug(request.getSlug());
                if (existingCategory.isPresent() && !existingCategory.get().getId().equals(id)) {
                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "Category with slug '" + request.getSlug() + "' already exists"
                    );
                }
                category.setSlug(request.getSlug());
            }
        }
        
        if (request.getStatus() != null && !request.getStatus().trim().isEmpty()) {
            category.setStatus(request.getStatus());
        }

        Category updatedCategory = categoryRepository.save(category);

        return convertToDTO(updatedCategory);
    }

    @Transactional
    public MessageResponse deleteCategory(Integer id) {
        // Find category
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));

        // Check if already deleted
        if ("DELETED".equalsIgnoreCase(category.getStatus())) {
            throw new ResourceNotFoundException("Category", "id", id);
        }

        // Soft delete
        category.setStatus("DELETED");
        categoryRepository.save(category);

        return MessageResponse.of("Category deleted successfully");
    }

    private CategoryDTO convertToDTO(Category category) {
        return CategoryDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .createdAt(category.getCreatedAt())
                .status(category.getStatus())
                .build();
    }

    private String generateSlug(String name) {
        return name.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .trim();
    }
}

