package com.fmcg.ecommerce.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fmcg.ecommerce.entity.BulkJob;
import com.fmcg.ecommerce.entity.Category;
import com.fmcg.ecommerce.entity.Notification;
import com.fmcg.ecommerce.entity.Product;
import com.fmcg.ecommerce.exception.BadRequestException;
import com.fmcg.ecommerce.repository.BulkJobRepository;
import com.fmcg.ecommerce.repository.CategoryRepository;
import com.fmcg.ecommerce.repository.ProductRepository;
import com.fmcg.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class BulkImportServiceImpl {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BulkJobRepository bulkJobRepository;
    private final SseNotificationService sseNotificationService;
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;

    /**
     * Instantly accepts the CSV file and triggers the background worker.
     */
    public Map<String, String> initiateProductImport(MultipartFile file, String username) {
        if (file.isEmpty()) throw new BadRequestException("Uploaded file is empty");

        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".csv")) {
            throw new BadRequestException("Only CSV files are supported. Please upload a .csv file");
        }

        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.isBlank()) lines.add(line);
            }
        } catch (Exception e) {
            throw new BadRequestException("Failed to read CSV file: " + e.getMessage());
        }

        if (lines.isEmpty() || lines.size() == 1) {
            throw new BadRequestException("CSV file contains no data rows");
        }

        // Create the Job Ledger
        BulkJob job = BulkJob.builder()
                .jobType("PRODUCT_IMPORT")
                .status("PENDING")
                .totalRows(lines.size() - 1) // minus header
                .processedRows(0)
                .failedRows(0)
                .startedByUserId(userRepository.findByEmail(username).or(() -> userRepository.findByMobile(username)).map(com.fmcg.ecommerce.entity.User::getId).orElse(0L))
                .build();
        job = bulkJobRepository.save(job);

        // Fire & Forget Background Thread
        // (Call the method directly if Spring proxy isn't needed, but since it's @Async inside same class it might not work unless we inject self. 
        // Actually, @Async inside the same class won't be intercepted by Spring! We must move it to another service or inject itself).
        // For simplicity, let's inject itself or just do a manual CompletableFuture. 
        // We will use CompletableFuture.runAsync() to avoid proxy issues.
        
        final BulkJob finalJob = job;
        java.util.concurrent.CompletableFuture.runAsync(() -> processProductImportAsync(lines, finalJob));

        return Map.of(
                "jobId", job.getPublicId(),
                "status", "PENDING",
                "message", "CSV accepted. Processing in background."
        );
    }

    /**
     * Background Worker Thread. Parses lines and updates DB periodically.
     */
    private void processProductImportAsync(List<String> lines, BulkJob job) {
        job.setStatus("PROCESSING");
        bulkJobRepository.save(job);

        String headerLine = lines.get(0);
        String[] headers = headerLine.split(",");
        Map<String, Integer> colIndex = new HashMap<>();
        for (int i = 0; i < headers.length; i++) {
            colIndex.put(headers[i].trim().toLowerCase().replace(" ", "_"), i);
        }

        List<String> errors = new ArrayList<>();
        int processed = 0;
        int failed = 0;
        
        // Cache categories
        List<Category> allCategories = categoryRepository.findByIsActiveTrueOrderBySortOrderAsc();

        for (int rowNum = 1; rowNum < lines.size(); rowNum++) {
            String line = lines.get(rowNum);
            String[] cols = parseCsvLine(line);
            try {
                String title = getCol(cols, colIndex, "title");
                String sku = getCol(cols, colIndex, "sku");
                String priceStr = getCol(cols, colIndex, "price");

                if (title == null || title.isBlank() || sku == null || sku.isBlank() || priceStr == null || priceStr.isBlank()) {
                    errors.add("Row " + (rowNum+1) + ": Missing title, sku, or price");
                    failed++;
                    continue;
                }

                if (productRepository.existsBySku(sku.trim())) {
                    errors.add("Row " + (rowNum+1) + ": SKU '" + sku.trim() + "' already exists");
                    failed++;
                    continue;
                }

                String categoryName = getCol(cols, colIndex, "category_name");
                Category category = allCategories.stream()
                        .filter(c -> c.getName().equalsIgnoreCase(categoryName != null ? categoryName.trim() : ""))
                        .findFirst()
                        .orElse(null);

                BigDecimal price = new BigDecimal(priceStr.trim());
                String mrpStr = getCol(cols, colIndex, "mrp");
                BigDecimal mrp = (mrpStr != null && !mrpStr.isBlank()) ? new BigDecimal(mrpStr.trim()) : price;
                String costStr = getCol(cols, colIndex, "cost_price");
                BigDecimal costPrice = (costStr != null && !costStr.isBlank()) ? new BigDecimal(costStr.trim()) : BigDecimal.ZERO;

                Product product = Product.builder()
                        .title(title.trim())
                        .sku(sku.trim())
                        .barcode(getCol(cols, colIndex, "barcode"))
                        .brand(getCol(cols, colIndex, "brand"))
                        .category(category)
                        .price(price)
                        .mrp(mrp)
                        .costPrice(costPrice)
                        .taxRate(BigDecimal.valueOf(5.0))
                        .status("ACTIVE")
                        .description(getCol(cols, colIndex, "description"))
                        .build();

                productRepository.save(product);
                processed++;

            } catch (Exception e) {
                errors.add("Row " + (rowNum+1) + ": " + e.getMessage());
                failed++;
            }

            // Update Job status every 500 rows to avoid DB spam
            if (rowNum % 500 == 0) {
                job.setProcessedRows(processed);
                job.setFailedRows(failed);
                bulkJobRepository.save(job);
            }
        }

        // Final Save
        job.setStatus(failed == lines.size() - 1 ? "FAILED" : "COMPLETED");
        job.setProcessedRows(processed);
        job.setFailedRows(failed);
        try {
            job.setErrorLog(objectMapper.writeValueAsString(errors));
        } catch (Exception ignored) {}
        bulkJobRepository.save(job);

        // Push real-time notification to the Admin!
        Notification notification = Notification.builder()
                .title("Bulk Import Completed")
                .message("Your bulk import job is finished! Processed: " + processed + ", Failed: " + failed)
                .type("BULK_JOB")
                .referenceId(job.getPublicId())
                .isRead(false)
                .build();
        
        // We need to attach the admin user. We just pass the userId.
        sseNotificationService.sendNotification(job.getStartedByUserId(), notification);
    }

    public String getCsvTemplate() {
        return "title,sku,barcode,brand,category_name,price,mrp,cost_price,tax_rate,unit,weight,status,description,tags,supplier,warehouse\n" +
               "Tata Salt 1kg,SALT-TATA-1KG,8901234567890,Tata,Staples,20.00,22.00,15.00,5.0,kg,1kg,ACTIVE,Iodised Salt,salt;staples,Tata Consumer,Main Warehouse\n";
    }

    private String[] parseCsvLine(String line) {
        List<String> tokens = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder current = new StringBuilder();
        for (char ch : line.toCharArray()) {
            if (ch == '"') {
                inQuotes = !inQuotes;
            } else if (ch == ',' && !inQuotes) {
                tokens.add(current.toString().trim());
                current.setLength(0);
            } else {
                current.append(ch);
            }
        }
        tokens.add(current.toString().trim());
        return tokens.toArray(new String[0]);
    }

    private String getCol(String[] cols, Map<String, Integer> colIndex, String name) {
        Integer idx = colIndex.get(name);
        if (idx == null || idx >= cols.length) return null;
        String val = cols[idx].trim();
        return val.isEmpty() ? null : val;
    }
}