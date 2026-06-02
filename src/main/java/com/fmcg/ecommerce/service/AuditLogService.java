package com.fmcg.ecommerce.service;

import com.fmcg.ecommerce.entity.AuditLog;
import com.fmcg.ecommerce.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public void logAction(String action, Long productId, String productName, String field, String oldValue, String newValue) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String user = (auth != null && auth.getName() != null) ? auth.getName() : "System";
        String role = "Admin";
        
        if (auth != null && auth.getAuthorities() != null && !auth.getAuthorities().isEmpty()) {
            role = auth.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");
        }

        AuditLog log = AuditLog.builder()
                .action(action)
                .productId(productId)
                .productName(productName)
                .field(field)
                .oldValue(oldValue)
                .newValue(newValue)
                .performedBy(user)
                .role(role)
                .build();
                
        auditLogRepository.save(log);
    }
}
