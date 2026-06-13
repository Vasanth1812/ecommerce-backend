package com.fmcg.ecommerce.service;

import com.fmcg.ecommerce.dto.account.ReturnDto;

import java.util.List;

public interface ReturnService {
    List<ReturnDto.Response> getReturns(Long userId);
    ReturnDto.Response submitReturn(Long userId, ReturnDto.Request request);
}
