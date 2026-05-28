package com.fmcg.ecommerce.dto.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReorderResponse {
    private int itemsAdded;
    private int itemsSkipped;
    private String message;
}
