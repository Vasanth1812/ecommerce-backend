package com.fmcg.ecommerce.controller.admin;

import com.fmcg.ecommerce.common.ApiResponse;
import com.fmcg.ecommerce.entity.BulkJob;
import com.fmcg.ecommerce.repository.BulkJobRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/bulk-jobs")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Tag(name = "Admin - Bulk Jobs", description = "Track background processing tasks")
public class BulkJobController {

    private final BulkJobRepository bulkJobRepository;

    @GetMapping
    @Operation(summary = "List all bulk jobs")
    public ResponseEntity<ApiResponse<List<BulkJob>>> getAllJobs() {
        return ResponseEntity.ok(ApiResponse.ok(bulkJobRepository.findAll(org.springframework.data.domain.Sort.by("createdAt").descending())));
    }

    @GetMapping("/{publicId}")
    @Operation(summary = "Get specific bulk job status")
    public ResponseEntity<ApiResponse<BulkJob>> getJobStatus(@PathVariable String publicId) {
        return bulkJobRepository.findByPublicId(publicId)
                .map(job -> ResponseEntity.ok(ApiResponse.ok(job)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}