package com.fmcg.ecommerce.service.impl;

import com.fmcg.ecommerce.dto.category.CategoryRequest;
import com.fmcg.ecommerce.dto.category.CategoryResponse;
import com.fmcg.ecommerce.entity.Category;
import com.fmcg.ecommerce.exception.BadRequestException;
import com.fmcg.ecommerce.exception.ResourceNotFoundException;
import com.fmcg.ecommerce.repository.CategoryRepository;
import com.fmcg.ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findByIsActiveTrueOrderBySortOrderAsc()
                .stream().map(c -> toResponse(c, false)).collect(Collectors.toList());
    }

    public CategoryResponse getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));
        return toResponse(category, true);
    }

    public List<CategoryResponse> getCategoryTree() {
        List<Category> all = categoryRepository.findByIsActiveTrueOrderBySortOrderAsc();
        Map<Long, List<CategoryResponse>> childrenMap = all.stream()
                .filter(c -> c.getParentId() != null)
                .collect(Collectors.groupingBy(
                        Category::getParentId,
                        Collectors.mapping(c -> toResponse(c, false), Collectors.toList())
                ));
        return all.stream()
                .filter(c -> c.getParentId() == null)
                .map(c -> {
                    CategoryResponse resp = toResponse(c, false);
                    resp.setChildren(childrenMap.getOrDefault(c.getId(), new ArrayList<>()));
                    return resp;
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        String slug = generateSlug(request.getName(), request.getSlug());
        if (categoryRepository.existsBySlug(slug)) {
            throw new BadRequestException("Category with slug '" + slug + "' already exists");
        }
        Category category = Category.builder()
                .name(request.getName())
                .slug(slug)
                .description(request.getDescription())
                .parentId(request.getParentId())
                .bannerUrl(request.getBannerUrl())
                .gstRate(request.getGstRate() != null ? request.getGstRate() : BigDecimal.valueOf(5.0))
                .sortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();
        return toResponse(categoryRepository.save(category), false);
    }

    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));
        String slug = generateSlug(request.getName(), request.getSlug());
        if (categoryRepository.existsBySlugAndIdNot(slug, id)) {
            throw new BadRequestException("Slug '" + slug + "' is already taken");
        }
        category.setName(request.getName());
        category.setSlug(slug);
        category.setDescription(request.getDescription());
        category.setParentId(request.getParentId());
        category.setBannerUrl(request.getBannerUrl());
        if (request.getGstRate() != null) category.setGstRate(request.getGstRate());
        if (request.getSortOrder() != null) category.setSortOrder(request.getSortOrder());
        if (request.getIsActive() != null) category.setIsActive(request.getIsActive());
        return toResponse(categoryRepository.save(category), false);
    }

    @Transactional
    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Category", id);
        }
        List<Category> children = categoryRepository.findByParentIdAndIsActiveTrueOrderBySortOrderAsc(id);
        if (!children.isEmpty()) {
            throw new BadRequestException("Cannot delete category with subcategories. Remove subcategories first.");
        }
        categoryRepository.deleteById(id);
    }

    // ── Helpers ──────────────────────────────────────────

    private String generateSlug(String name, String provided) {
        if (provided != null && !provided.isBlank()) return provided.trim().toLowerCase().replaceAll("[^a-z0-9-]", "-");
        return name.trim().toLowerCase().replaceAll("\\s+", "-").replaceAll("[^a-z0-9-]", "");
    }

    private CategoryResponse toResponse(Category c, boolean withChildren) {
        CategoryResponse resp = CategoryResponse.builder()
                .id(c.getId())
                .name(c.getName())
                .slug(c.getSlug())
                .description(c.getDescription())
                .parentId(c.getParentId())
                .bannerUrl(c.getBannerUrl())
                .gstRate(c.getGstRate())
                .sortOrder(c.getSortOrder())
                .isActive(Boolean.TRUE.equals(c.getIsActive()))
                .productCount(0)
                .children(new ArrayList<>())
                .build();
        if (withChildren) {
            List<CategoryResponse> children = categoryRepository
                    .findByParentIdAndIsActiveTrueOrderBySortOrderAsc(c.getId())
                    .stream().map(ch -> toResponse(ch, false)).collect(Collectors.toList());
            resp.setChildren(children);
        }
        return resp;
    }
}
