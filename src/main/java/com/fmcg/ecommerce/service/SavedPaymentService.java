package com.fmcg.ecommerce.service;

import com.fmcg.ecommerce.dto.account.SavedPaymentDto;

import java.util.List;

public interface SavedPaymentService {
    List<SavedPaymentDto.Response> getSavedPayments(Long userId);
    SavedPaymentDto.Response savePaymentMethod(Long userId, SavedPaymentDto.Request request);
    void deletePaymentMethod(Long userId, Long paymentId);
}
