package com.fmcg.ecommerce.service.impl;

import com.fmcg.ecommerce.dto.product.ProductImageDto;
import com.fmcg.ecommerce.dto.product.ProductRequest;
import com.fmcg.ecommerce.dto.product.ProductResponse;
import com.fmcg.ecommerce.dto.product.UpdateProductPricingRequest;
import com.fmcg.ecommerce.entity.Category;
import com.fmcg.ecommerce.entity.Inventory;
import com.fmcg.ecommerce.entity.Product;
import com.fmcg.ecommerce.entity.ProductImage;
import com.fmcg.ecommerce.exception.BadRequestException;
import com.fmcg.ecommerce.exception.ResourceNotFoundException;
import com.fmcg.ecommerce.repository.CategoryRepository;
import com.fmcg.ecommerce.repository.InventoryRepository;
import com.fmcg.ecommerce.repository.ProductRepository;
import com.fmcg.ecommerce.service.AuditLogService;
import com.fmcg.ecommerce.repository.AuditLogRepository;
import com.fmcg.ecommerce.entity.AuditLog;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.web.multipart.MultipartFile;
import com.fmcg.ecommerce.repository.ProductImageRepository;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final InventoryRepository inventoryRepository;
    private final com.fmcg.ecommerce.repository.ProductSeoRepository productSeoRepository;
    private final ProductImageRepository productImageRepository;
    private final AuditLogService auditLogService;
    private final AuditLogRepository auditLogRepository;

    @Transactional(readOnly = true)
    public Page<ProductResponse> getProducts(String search, String status, Long categoryId,
                                              String brand, BigDecimal minPrice, BigDecimal maxPrice,
                                              int page, int size, String sortBy, String sortDir) {
        // Native SQL queries require SQL column names, not Java field names.
        // We handle ordering inside the query itself via the sortBy/sortDir SQL columns.
        // Pass an unsorted Pageable so Spring Data JPA doesn't append its own ORDER BY.
        Pageable pageable = PageRequest.of(page, size);

        return productRepository.searchProducts(
                search, status, categoryId, brand, minPrice, maxPrice, pageable
        ).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
        return toResponse(product);
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductBySku(String sku) {
        Product product = productRepository.findBySku(sku)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "SKU", sku));
        return toResponse(product);
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductByBarcode(String barcode) {
        Product product = productRepository.findByBarcode(barcode)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "barcode", barcode));
        return toResponse(product);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> compareProducts(List<Long> ids) {
        return productRepository.findByIdIn(ids).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        if (productRepository.existsBySku(request.getSku())) {
            throw new BadRequestException("Product with SKU '" + request.getSku() + "' already exists");
        }
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", request.getCategoryId()));

        Product product = Product.builder()
                .sku(request.getSku())
                .barcode(request.getBarcode())
                .itemCode(request.getItemCode())
                .title(request.getTitle())
                .description(request.getDescription())
                .shortDescription(request.getShortDescription())
                .brand(request.getBrand())
                .category(category)
                .price(request.getPrice())
                .mrp(request.getMrp() != null ? request.getMrp() : request.getPrice())
                .costPrice(request.getCostPrice() != null ? request.getCostPrice() : BigDecimal.ZERO)
                .taxRate(request.getTaxRate() != null ? request.getTaxRate() : BigDecimal.valueOf(5.0))
                .unit(request.getUnit())
                .weight(request.getWeight())
                .status(request.getStatus() != null ? request.getStatus() : "ACTIVE")
                .tags(request.getTags() != null ? String.join(",", request.getTags()) : null)
                .warehouse(request.getWarehouse())
                .supplier(request.getSupplier())
                .build();

        Product saved = productRepository.save(product);
        auditLogService.logAction("Product Created", saved.getId(), saved.getTitle(), null, null, null);
        return toResponse(saved);
    }

    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));

        if (!product.getSku().equals(request.getSku()) && productRepository.existsBySkuAndIdNot(request.getSku(), id)) {
            throw new BadRequestException("SKU '" + request.getSku() + "' is already in use");
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", request.getCategoryId()));

        product.setSku(request.getSku());
        product.setBarcode(request.getBarcode());
        product.setItemCode(request.getItemCode());
        product.setTitle(request.getTitle());
        product.setDescription(request.getDescription());
        product.setShortDescription(request.getShortDescription());
        product.setBrand(request.getBrand());
        product.setCategory(category);
        product.setPrice(request.getPrice());
        product.setMrp(request.getMrp() != null ? request.getMrp() : request.getPrice());
        product.setCostPrice(request.getCostPrice() != null ? request.getCostPrice() : BigDecimal.ZERO);
        product.setTaxRate(request.getTaxRate() != null ? request.getTaxRate() : product.getTaxRate());
        product.setUnit(request.getUnit());
        product.setWeight(request.getWeight());
        product.setStatus(request.getStatus() != null ? request.getStatus().toUpperCase() : product.getStatus());
        product.setTags(request.getTags() != null ? String.join(",", request.getTags()) : null);
        product.setWarehouse(request.getWarehouse());
        product.setSupplier(request.getSupplier());

        Product saved = productRepository.save(product);
        auditLogService.logAction("Product Updated", saved.getId(), saved.getTitle(), "Details", null, null);
        return toResponse(saved);
    }

    @Transactional
    public ProductResponse updateProductPricing(Long id, UpdateProductPricingRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));

        if (request.getPrice() != null) {
            product.setPrice(request.getPrice());
        }
        if (request.getMrp() != null) {
            product.setMrp(request.getMrp());
        }
        if (request.getCostPrice() != null) {
            product.setCostPrice(request.getCostPrice());
        }
        if (request.getTaxRate() != null) {
            product.setTaxRate(request.getTaxRate());
        }

        Product saved = productRepository.save(product);
        auditLogService.logAction("Price Updated", saved.getId(), saved.getTitle(), "Pricing", null, null);
        return toResponse(saved);
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
        
        auditLogService.logAction("Product Deleted", product.getId(), product.getTitle(), null, null, null);
        productRepository.delete(product);
    }

    // â”€â”€ Mapper â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    public ProductResponse toResponse(Product product) {
        Optional<Inventory> inventoryOpt = inventoryRepository.findByProductId(product.getId());
        ProductResponse.StockInfo stockInfo = inventoryOpt.map(inv -> {
            String stockStatus;
            if (inv.getQtyAvailable() == 0) stockStatus = "OUT_OF_STOCK";
            else if (inv.getQtyAvailable() <= inv.getReorderPoint()) stockStatus = "LOW_STOCK";
            else stockStatus = "IN_STOCK";
            return ProductResponse.StockInfo.builder()
                    .qtyAvailable(inv.getQtyAvailable())
                    .qtyReserved(inv.getQtyReserved())
                    .stockStatus(stockStatus)
                    .build();
        }).orElse(ProductResponse.StockInfo.builder()
                .qtyAvailable(0)
                .qtyReserved(0)
                .stockStatus("OUT_OF_STOCK")
                .build());

        List<ProductImageDto> images = product.getImages() == null ? List.of() :
                product.getImages().stream()
                        .map(img -> ProductImageDto.builder()
                                .id(img.getId())
                .publicId(img.getPublicId())
                                .url(img.getUrl())
                                .alt(img.getAlt())
                                .isPrimary(Boolean.TRUE.equals(img.getIsPrimary()))
                                .sortOrder(img.getSortOrder())
                                .build())
                        .collect(Collectors.toList());

        BigDecimal finalDiscountPrice = null;
        String finalPromotionName = null;

        if (product.getPromotions() != null && !product.getPromotions().isEmpty()) {
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            BigDecimal lowestPrice = product.getPrice();

            for (com.fmcg.ecommerce.entity.Promotion promo : product.getPromotions()) {
                if ("ACTIVE".equals(promo.getStatus()) &&
                        (promo.getStartDate() == null || !now.isBefore(promo.getStartDate())) &&
                        (promo.getEndDate() == null || !now.isAfter(promo.getEndDate()))) {

                    BigDecimal candidatePrice = product.getPrice();
                    if ("PERCENTAGE".equals(promo.getDiscountType()) && promo.getDiscountValue() != null) {
                        BigDecimal discountAmount = product.getPrice().multiply(promo.getDiscountValue().divide(BigDecimal.valueOf(100)));
                        candidatePrice = product.getPrice().subtract(discountAmount);
                    } else if ("FIXED".equals(promo.getDiscountType()) && promo.getDiscountValue() != null) {
                        candidatePrice = product.getPrice().subtract(promo.getDiscountValue());
                    }

                    if (candidatePrice.compareTo(BigDecimal.ZERO) < 0) {
                        candidatePrice = BigDecimal.ZERO;
                    }

                    if (candidatePrice.compareTo(lowestPrice) < 0) {
                        lowestPrice = candidatePrice;
                        finalDiscountPrice = candidatePrice;
                        finalPromotionName = promo.getName();
                    }
                }
            }
        }

        return ProductResponse.builder()
                .id(product.getId())
                .publicId(product.getPublicId())
                .sku(product.getSku())
                .barcode(product.getBarcode())
                .itemCode(product.getItemCode())
                .title(product.getTitle())
                .description(product.getDescription())
                .shortDescription(product.getShortDescription())
                .brand(product.getBrand())
                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .price(product.getPrice())
                .discountPrice(finalDiscountPrice)
                .activePromotionName(finalPromotionName)
                .mrp(product.getMrp())
                .costPrice(product.getCostPrice())
                .taxRate(product.getTaxRate())
                .unit(product.getUnit())
                .weight(product.getWeight())
                .status(product.getStatus())
                .tags(product.getTags() != null && !product.getTags().isEmpty() ? Arrays.asList(product.getTags().split(",")) : List.of())
                .warehouse(product.getWarehouse())
                .supplier(product.getSupplier())
                .images(images)
                .stock(stockInfo)
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }

    // â”€â”€ SEO Management â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    @Transactional(readOnly = true)
    public List<com.fmcg.ecommerce.dto.product.ProductSeoDto> getProductSeos(String search) {
        String searchLower = search != null ? search.toLowerCase() : null;
        return productRepository.findAll().stream()
            .filter(p -> searchLower == null || 
                         p.getTitle().toLowerCase().contains(searchLower) || 
                         (p.getSku() != null && p.getSku().toLowerCase().contains(searchLower)))
            .map(p -> {
                com.fmcg.ecommerce.entity.ProductSeo seo = p.getSeo();
                return com.fmcg.ecommerce.dto.product.ProductSeoDto.builder()
                        .productId(p.getId())
                        .productName(p.getTitle())
                        .sku(p.getSku())
                        .metaTitle(seo != null ? seo.getMetaTitle() : "")
                        .metaDescription(seo != null ? seo.getMetaDescription() : "")
                        .metaKeywords(seo != null && seo.getMetaKeywords() != null ? seo.getMetaKeywords() : java.util.Collections.emptyList())
                        .slug(seo != null ? seo.getSlug() : "")
                        .canonicalUrl(seo != null ? seo.getCanonicalUrl() : "")
                        .ogImage(seo != null ? seo.getOgImage() : "")
                        .build();
            }).collect(Collectors.toList());
    }

    @Transactional
    public com.fmcg.ecommerce.dto.product.ProductSeoDto updateProductSeo(Long productId, com.fmcg.ecommerce.dto.product.UpdateProductSeoRequest request) {
        com.fmcg.ecommerce.entity.Product product = productRepository.findById(productId)
                .orElseThrow(() -> new com.fmcg.ecommerce.exception.ResourceNotFoundException("Product", productId));
        
        com.fmcg.ecommerce.entity.ProductSeo seo = product.getSeo();
        if (seo == null) {
            seo = new com.fmcg.ecommerce.entity.ProductSeo();
            seo.setProduct(product);
        }
        
        seo.setMetaTitle(request.getMetaTitle());
        seo.setMetaDescription(request.getMetaDescription());
        if (request.getMetaKeywords() != null) {
            seo.setMetaKeywords(new java.util.ArrayList<>(request.getMetaKeywords()));
        }
        seo.setSlug(request.getSlug());
        seo.setCanonicalUrl(request.getCanonicalUrl());
        seo.setOgImage(request.getOgImage());
        
        seo = productSeoRepository.save(seo);
        
        auditLogService.logAction("SEO Updated", product.getId(), product.getTitle(), "SEO", null, null);

        return com.fmcg.ecommerce.dto.product.ProductSeoDto.builder()
                .productId(product.getId())
                .productName(product.getTitle())
                .sku(product.getSku())
                .metaTitle(seo.getMetaTitle())
                .metaDescription(seo.getMetaDescription())
                .metaKeywords(seo.getMetaKeywords() != null ? seo.getMetaKeywords() : java.util.Collections.emptyList())
                .slug(seo.getSlug())
                .canonicalUrl(seo.getCanonicalUrl())
                .ogImage(seo.getOgImage())
                .build();
    }

    @Transactional
    public ProductImage uploadProductMedia(Long productId, MultipartFile file, Boolean isPrimary, String alt, Integer sortOrder) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", productId));
        
        // Mock file upload to external storage by just using the filename for now
        // In a real application, save 'file' to AWS S3 or disk and get the URL
        String filename = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        String fakeUrl = "https://cdn.fmcg.com/products/" + filename;

        ProductImage image = ProductImage.builder()
                .product(product)
                .url(fakeUrl)
                .alt(alt)
                .isPrimary(isPrimary)
                .sortOrder(sortOrder)
                .build();
                
        return productImageRepository.save(image);
    }

    @Transactional
    public void deleteProductMedia(Long mediaId) {
        if (!productImageRepository.existsById(mediaId)) {
            throw new ResourceNotFoundException("ProductImage", mediaId);
        }
        productImageRepository.deleteById(mediaId);
    }

    @Transactional
    public void setPrimaryProductMedia(Long mediaId) {
        ProductImage image = productImageRepository.findById(mediaId)
                .orElseThrow(() -> new ResourceNotFoundException("ProductImage", mediaId));
        
        // Unset primary for all other images of this product
        List<ProductImage> allImages = productImageRepository.findByProductId(image.getProduct().getId());
        for (ProductImage img : allImages) {
            img.setIsPrimary(false);
            productImageRepository.save(img);
        }
        
        // Set this as primary
        image.setIsPrimary(true);
        productImageRepository.save(image);
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getProductAuditLogs(Long productId) {
        List<AuditLog> logs;
        if (productId != null) {
            logs = auditLogRepository.findByProductIdOrderByTimestampDesc(productId);
        } else {
            logs = auditLogRepository.findAll(Sort.by(Sort.Direction.DESC, "timestamp"));
        }

        return logs.stream().map(log -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", log.getId());
            map.put("productId", log.getProductId());
            map.put("productName", log.getProductName());
            map.put("action", log.getAction());
            map.put("field", log.getField());
            map.put("oldValue", log.getOldValue());
            map.put("newValue", log.getNewValue());
            map.put("performedBy", log.getPerformedBy());
            map.put("role", log.getRole());
            map.put("timestamp", log.getTimestamp() != null ? log.getTimestamp().toString() : null);
            return map;
        }).collect(Collectors.toList());
    }
}
