package com.fmcg.ecommerce.controller;

import com.fmcg.ecommerce.common.ApiResponse;
import com.fmcg.ecommerce.common.PagedResponse;
import com.fmcg.ecommerce.dto.product.ProductRequest;
import com.fmcg.ecommerce.dto.product.ProductResponse;
import com.fmcg.ecommerce.service.impl.BulkImportServiceImpl;
import com.fmcg.ecommerce.service.impl.ProductServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Products", description = "Product catalog APIs")
public class ProductController {

    private final ProductServiceImpl productService;
    private final BulkImportServiceImpl bulkImportService;

    // ── Public Product Endpoints ──────────────────────────

    @GetMapping("/api/v1/products")
    @Operation(summary = "Get all products with filters and pagination")
    public ResponseEntity<ApiResponse<PagedResponse<ProductResponse>>> getProducts(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Page<ProductResponse> products = productService.getProducts(
                search, status, categoryId, brand, minPrice, maxPrice, page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.ok(PagedResponse.from(products)));
    }

    @GetMapping("/api/v1/products/{id}")
    @Operation(summary = "Get product by ID")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(productService.getProductById(id)));
    }

    @GetMapping("/api/v1/products/barcode/{code}")
    @Operation(summary = "Get product by barcode")
    public ResponseEntity<ApiResponse<ProductResponse>> getByBarcode(@PathVariable String code) {
        return ResponseEntity.ok(ApiResponse.ok(productService.getProductByBarcode(code)));
    }

    @GetMapping("/api/v1/products/search")
    @Operation(summary = "Search products")
    public ResponseEntity<ApiResponse<PagedResponse<ProductResponse>>> searchProducts(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {

        Page<ProductResponse> products = productService.getProducts(
                q, "ACTIVE", null, null, null, null, page, size, "createdAt", "desc");
        return ResponseEntity.ok(ApiResponse.ok(PagedResponse.from(products)));
    }

    @GetMapping("/api/v1/products/compare")
    @Operation(summary = "Compare up to 4 products")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> compareProducts(
            @RequestParam List<Long> ids) {
        return ResponseEntity.ok(ApiResponse.ok(productService.compareProducts(ids)));
    }

    // ── Admin Product Endpoints ───────────────────────────

    @PostMapping("/api/v1/admin/products")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin: Create product")
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
            @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Product created successfully", productService.createProduct(request)));
    }

    @PutMapping("/api/v1/admin/products/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin: Update product")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @PathVariable Long id, @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Product updated", productService.updateProduct(id, request)));
    }

    @DeleteMapping("/api/v1/admin/products/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin: Delete product")
    public ResponseEntity<ApiResponse<String>> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(ApiResponse.ok("Product deleted successfully"));
    }

    @PostMapping(value = "/api/v1/admin/products/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin: Bulk import products from CSV file")
    public ResponseEntity<ApiResponse<java.util.Map<String, Object>>> importProducts(
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(ApiResponse.ok("Import complete", bulkImportService.importProductsFromCsv(file)));
    }

    @GetMapping("/api/v1/admin/products/import/template")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin: Download CSV import template")
    public ResponseEntity<byte[]> downloadTemplate() {
        String csv = bulkImportService.getCsvTemplate();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"product_import_template.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    // ── SEO Management ────────────────────────────────────────

    @GetMapping("/api/v1/admin/products/seo")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get SEO data for products")
    public ResponseEntity<ApiResponse<java.util.List<com.fmcg.ecommerce.dto.product.ProductSeoDto>>> getProductSeos(
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(ApiResponse.ok(productService.getProductSeos(search)));
    }

    @PatchMapping("/api/v1/admin/products/{id}/seo")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update product SEO data")
    public ResponseEntity<ApiResponse<com.fmcg.ecommerce.dto.product.ProductSeoDto>> updateProductSeo(
            @PathVariable Long id, @RequestBody com.fmcg.ecommerce.dto.product.UpdateProductSeoRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("SEO updated successfully", productService.updateProductSeo(id, request)));
    }
}
