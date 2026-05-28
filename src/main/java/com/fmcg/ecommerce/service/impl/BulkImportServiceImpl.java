package com.fmcg.ecommerce.service.impl;

import com.fmcg.ecommerce.entity.Category;
import com.fmcg.ecommerce.entity.Product;
import com.fmcg.ecommerce.exception.BadRequestException;
import com.fmcg.ecommerce.repository.CategoryRepository;
import com.fmcg.ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class BulkImportServiceImpl {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    /**
     * Bulk import products from a CSV file.
     *
     * Expected CSV columns (header row required):
     * title, sku, barcode, brand, category_name, price, mrp, cost_price, tax_rate, unit, weight, status, description, tags
     */
    @Transactional
    public Map<String, Object> importProductsFromCsv(MultipartFile file) {
        if (file.isEmpty()) throw new BadRequestException("Uploaded file is empty");

        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".csv")) {
            throw new BadRequestException("Only CSV files are supported. Please upload a .csv file");
        }

        List<String> errors = new ArrayList<>();
        List<String> imported = new ArrayList<>();
        List<String> skipped = new ArrayList<>();
        int rowNum = 1;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            String headerLine = reader.readLine();
            if (headerLine == null) throw new BadRequestException("CSV file has no header row");

            String[] headers = headerLine.split(",");
            Map<String, Integer> colIndex = new HashMap<>();
            for (int i = 0; i < headers.length; i++) {
                colIndex.put(headers[i].trim().toLowerCase().replace(" ", "_"), i);
            }

            // Validate required columns
            List<String> required = List.of("title", "sku", "price", "category_name");
            for (String col : required) {
                if (!colIndex.containsKey(col)) {
                    throw new BadRequestException("Missing required CSV column: '" + col + "'");
                }
            }

            String line;
            while ((line = reader.readLine()) != null) {
                rowNum++;
                if (line.isBlank()) continue;

                String[] cols = parseCsvLine(line);
                try {
                    String title = getCol(cols, colIndex, "title");
                    String sku = getCol(cols, colIndex, "sku");
                    String priceStr = getCol(cols, colIndex, "price");

                    if (title == null || title.isBlank()) {
                        errors.add("Row " + rowNum + ": title is required");
                        continue;
                    }
                    if (sku == null || sku.isBlank()) {
                        errors.add("Row " + rowNum + ": sku is required");
                        continue;
                    }
                    if (priceStr == null || priceStr.isBlank()) {
                        errors.add("Row " + rowNum + ": price is required");
                        continue;
                    }

                    // Skip if SKU already exists
                    if (productRepository.existsBySku(sku.trim())) {
                        skipped.add("Row " + rowNum + ": SKU '" + sku.trim() + "' already exists — skipped");
                        continue;
                    }

                    // Resolve category
                    String categoryName = getCol(cols, colIndex, "category_name");
                    Category category = null;
                    if (categoryName != null && !categoryName.isBlank()) {
                        category = categoryRepository.findByIsActiveTrueOrderBySortOrderAsc()
                                .stream()
                                .filter(c -> c.getName().equalsIgnoreCase(categoryName.trim()))
                                .findFirst()
                                .orElse(null);
                        if (category == null) {
                            errors.add("Row " + rowNum + ": Category '" + categoryName + "' not found — skipped");
                            continue;
                        }
                    } else {
                        errors.add("Row " + rowNum + ": category_name is required");
                        continue;
                    }

                    BigDecimal price = new BigDecimal(priceStr.trim());
                    String mrpStr = getCol(cols, colIndex, "mrp");
                    BigDecimal mrp = (mrpStr != null && !mrpStr.isBlank()) ? new BigDecimal(mrpStr.trim()) : price;
                    String costStr = getCol(cols, colIndex, "cost_price");
                    BigDecimal costPrice = (costStr != null && !costStr.isBlank()) ? new BigDecimal(costStr.trim()) : BigDecimal.ZERO;
                    String taxStr = getCol(cols, colIndex, "tax_rate");
                    BigDecimal taxRate = (taxStr != null && !taxStr.isBlank()) ? new BigDecimal(taxStr.trim()) : BigDecimal.valueOf(5.0);
                    String status = getCol(cols, colIndex, "status");
                    if (status == null || status.isBlank()) status = "ACTIVE";

                    Product product = Product.builder()
                            .title(title.trim())
                            .sku(sku.trim())
                            .barcode(getCol(cols, colIndex, "barcode"))
                            .brand(getCol(cols, colIndex, "brand"))
                            .category(category)
                            .price(price)
                            .mrp(mrp)
                            .costPrice(costPrice)
                            .taxRate(taxRate)
                            .unit(getCol(cols, colIndex, "unit"))
                            .weight(getCol(cols, colIndex, "weight"))
                            .status(status.trim().toUpperCase())
                            .description(getCol(cols, colIndex, "description"))
                            .tags(getCol(cols, colIndex, "tags"))
                            .supplier(getCol(cols, colIndex, "supplier"))
                            .warehouse(getCol(cols, colIndex, "warehouse"))
                            .build();

                    productRepository.save(product);
                    imported.add("Row " + rowNum + ": '" + title.trim() + "' (SKU: " + sku.trim() + ")");

                } catch (NumberFormatException e) {
                    errors.add("Row " + rowNum + ": Invalid number format — " + e.getMessage());
                } catch (Exception e) {
                    errors.add("Row " + rowNum + ": Error — " + e.getMessage());
                    log.error("Error importing row {}: {}", rowNum, e.getMessage());
                }
            }

        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new BadRequestException("Failed to parse CSV file: " + e.getMessage());
        }

        Map<String, Object> result = new HashMap<>();
        result.put("totalRows", rowNum - 1);
        result.put("imported", imported.size());
        result.put("skipped", skipped.size());
        result.put("errors", errors.size());
        result.put("importedProducts", imported);
        result.put("skippedProducts", skipped);
        result.put("errorDetails", errors);
        result.put("summary", "Imported " + imported.size() + " | Skipped " + skipped.size() + " | Errors " + errors.size());
        return result;
    }

    /**
     * Returns a CSV template string for product import.
     */
    public String getCsvTemplate() {
        return "title,sku,barcode,brand,category_name,price,mrp,cost_price,tax_rate,unit,weight,status,description,tags,supplier,warehouse\n" +
               "Tata Salt 1kg,SALT-TATA-1KG,8901234567890,Tata,Staples,20.00,22.00,15.00,5.0,kg,1kg,ACTIVE,Iodised Salt,salt;staples,Tata Consumer,Main Warehouse\n" +
               "Aashirvaad Atta 5kg,ATTA-ASH-5KG,8901234567891,Aashirvaad,Staples,249.00,260.00,180.00,5.0,bag,5kg,ACTIVE,Whole Wheat Flour,atta;flour;wheat,ITC,Main Warehouse\n";
    }

    // ── CSV Helpers ───────────────────────────────────────

    private String[] parseCsvLine(String line) {
        // Basic CSV parser that handles quoted fields
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
