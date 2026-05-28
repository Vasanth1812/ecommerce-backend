package com.fmcg.ecommerce.service.impl;

import com.fmcg.ecommerce.dto.vendor.VendorRequest;
import com.fmcg.ecommerce.dto.vendor.VendorResponse;
import com.fmcg.ecommerce.entity.Vendor;
import com.fmcg.ecommerce.exception.BadRequestException;
import com.fmcg.ecommerce.exception.ResourceNotFoundException;
import com.fmcg.ecommerce.repository.VendorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VendorService {

    private final VendorRepository vendorRepository;

    @Transactional(readOnly = true)
    public Page<VendorResponse> getVendors(String search, String status, Pageable pageable) {
        return vendorRepository.searchVendors(search, status, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public VendorResponse getVendor(Long id) {
        return toResponse(vendorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor", id)));
    }

    @Transactional
    public VendorResponse createVendor(VendorRequest request) {
        Vendor vendor = Vendor.builder()
                .businessName(request.getBusinessName())
                .contactName(request.getContactName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .gstNumber(request.getGstNumber())
                .status(request.getStatus() != null ? request.getStatus() : "ACTIVE")
                .commissionRate(request.getCommissionRate())
                .build();
        return toResponse(vendorRepository.save(vendor));
    }

    @Transactional
    public VendorResponse updateVendor(Long id, VendorRequest request) {
        Vendor vendor = vendorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor", id));

        vendor.setBusinessName(request.getBusinessName());
        vendor.setContactName(request.getContactName());
        vendor.setEmail(request.getEmail());
        vendor.setPhone(request.getPhone());
        vendor.setGstNumber(request.getGstNumber());
        if (request.getStatus() != null) {
            vendor.setStatus(request.getStatus());
        }
        if (request.getCommissionRate() != null) {
            vendor.setCommissionRate(request.getCommissionRate());
        }

        return toResponse(vendorRepository.save(vendor));
    }

    @Transactional
    public void deleteVendor(Long id) {
        if (!vendorRepository.existsById(id)) {
            throw new ResourceNotFoundException("Vendor", id);
        }
        vendorRepository.deleteById(id);
    }

    private VendorResponse toResponse(Vendor vendor) {
        return VendorResponse.builder()
                .id(vendor.getId())
                .businessName(vendor.getBusinessName())
                .contactName(vendor.getContactName())
                .email(vendor.getEmail())
                .phone(vendor.getPhone())
                .gstNumber(vendor.getGstNumber())
                .status(vendor.getStatus())
                .commissionRate(vendor.getCommissionRate())
                .createdAt(vendor.getCreatedAt())
                .updatedAt(vendor.getUpdatedAt())
                .build();
    }
}
