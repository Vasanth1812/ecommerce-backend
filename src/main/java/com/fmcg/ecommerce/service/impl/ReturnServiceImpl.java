package com.fmcg.ecommerce.service.impl;

import com.fmcg.ecommerce.dto.account.ReturnDto;
import com.fmcg.ecommerce.entity.*;
import com.fmcg.ecommerce.exception.BadRequestException;
import com.fmcg.ecommerce.exception.ResourceNotFoundException;
import com.fmcg.ecommerce.repository.OrderItemRepository;
import com.fmcg.ecommerce.repository.OrderRepository;
import com.fmcg.ecommerce.repository.OrderReturnRepository;
import com.fmcg.ecommerce.repository.UserRepository;
import com.fmcg.ecommerce.service.ReturnService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReturnServiceImpl implements ReturnService {

    private final OrderReturnRepository returnRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ReturnDto.Response> getReturns(Long userId) {
        return returnRepository.findByUserId(userId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ReturnDto.Response submitReturn(Long userId, ReturnDto.Request request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order", request.getOrderId()));

        if (!order.getUser().getId().equals(userId)) {
            throw new BadRequestException("This order does not belong to you.");
        }

        if (!order.getStatus().equals("DELIVERED")) {
            throw new BadRequestException("You can only return delivered orders.");
        }

        OrderReturn orderReturn = OrderReturn.builder()
                .order(order)
                .user(user)
                .reason(request.getReason())
                .status("PENDING")
                .refundAmount(BigDecimal.ZERO)
                .build();

        BigDecimal calculatedRefund = BigDecimal.ZERO;

        for (ReturnDto.ReturnItemRequest itemReq : request.getItems()) {
            OrderItem orderItem = orderItemRepository.findById(itemReq.getOrderItemId())
                    .orElseThrow(() -> new ResourceNotFoundException("OrderItem", itemReq.getOrderItemId()));

            if (!orderItem.getOrder().getId().equals(order.getId())) {
                throw new BadRequestException("OrderItem does not belong to the specified Order.");
            }

            if (itemReq.getQty() > orderItem.getQty()) {
                throw new BadRequestException("Return quantity cannot exceed purchased quantity.");
            }

            OrderReturnItem returnItem = OrderReturnItem.builder()
                    .orderReturn(orderReturn)
                    .orderItem(orderItem)
                    .qty(itemReq.getQty())
                    .build();

            orderReturn.getItems().add(returnItem);
            
            // Calculate refund amount based on effective price paid
            BigDecimal lineItemPrice = orderItem.getUnitPrice().multiply(new BigDecimal(itemReq.getQty()));
            calculatedRefund = calculatedRefund.add(lineItemPrice);
        }

        orderReturn.setRefundAmount(calculatedRefund);
        return toResponse(returnRepository.save(orderReturn));
    }

    private ReturnDto.Response toResponse(OrderReturn orderReturn) {
        List<ReturnDto.ReturnItemResponse> items = orderReturn.getItems().stream().map(i ->
                ReturnDto.ReturnItemResponse.builder()
                        .id(i.getId())
                        .orderItemId(i.getOrderItem().getId())
                        .productTitle(i.getOrderItem().getProduct().getTitle())
                        .qty(i.getQty())
                        .build()
        ).collect(Collectors.toList());

        return ReturnDto.Response.builder()
                .id(orderReturn.getId())
                .orderNumber(orderReturn.getOrder().getOrderNumber())
                .reason(orderReturn.getReason())
                .status(orderReturn.getStatus())
                .refundAmount(orderReturn.getRefundAmount())
                .createdAt(orderReturn.getCreatedAt())
                .items(items)
                .build();
    }
}
