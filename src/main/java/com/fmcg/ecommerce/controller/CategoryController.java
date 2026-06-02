package com.fmcg.ecommerce.controller;

import com.fmcg.ecommerce.common.ApiResponse;
import com.fmcg.ecommerce.dto.category.CategoryRequest;
import com.fmcg.ecommerce.dto.category.CategoryResponse;
import com.fmcg.ecommerce.service.impl.CategoryServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Categories", description = "Product category APIs")
public class CategoryController {

    private final CategoryServiceImpl categoryService;

    // ── Public Endpoints ──────────────────────────────────

    @GetMapping("/api/v1/categories")
    @Operation(summary = "Get all active categories (flat list)")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getAllCategories() {
        return ResponseEntity.ok(ApiResponse.ok(categoryService.getAllCategories()));
    }

    @GetMapping("/api/v1/categories/tree")
    @Operation(summary = "Get categories as a tree (parent → children)")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getCategoryTree() {
        return ResponseEntity.ok(ApiResponse.ok(categoryService.getCategoryTree()));
    }

    @GetMapping("/api/v1/categories/{id}")
    @Operation(summary = "Get category by ID with subcategories")
    public ResponseEntity<ApiResponse<CategoryResponse>> getCategoryById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(categoryService.getCategoryById(id)));
    }

    // ── Admin Endpoints ───────────────────────────────────

    @GetMapping("/api/v1/admin/categories")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin: Get all categories (including inactive)")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getAllAdminCategories() {
        return ResponseEntity.ok(ApiResponse.ok(categoryService.getAllAdminCategories()));
    }

    @PostMapping("/api/v1/admin/categories")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin: Create category")
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(
            @Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Category created", categoryService.createCategory(request)));
    }

    @PutMapping("/api/v1/admin/categories/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin: Update category")
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(
            @PathVariable Long id, @Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Category updated", categoryService.updateCategory(id, request)));
    }

    @DeleteMapping("/api/v1/admin/categories/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin: Delete category")
    public ResponseEntity<ApiResponse<String>> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok(ApiResponse.ok("Category deleted"));
    }
}
