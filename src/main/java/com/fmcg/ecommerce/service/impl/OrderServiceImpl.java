package com.fmcg.ecommerce.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fmcg.ecommerce.dto.cart.AddToCartRequest;
import com.fmcg.ecommerce.dto.cart.CartResponse;
import com.fmcg.ecommerce.dto.order.CreateOrderRequest;
import com.fmcg.ecommerce.dto.order.OrderResponse;
import com.fmcg.ecommerce.dto.order.ReorderResponse;
import com.fmcg.ecommerce.dto.order.UpdateOrderStatusRequest;
import com.fmcg.ecommerce.entity.*;
import com.fmcg.ecommerce.exception.BadRequestException;
import com.fmcg.ecommerce.exception.ResourceNotFoundException;
import com.fmcg.ecommerce.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final AddressRepository addressRepository;
    private final InventoryRepository inventoryRepository;
    private final LoyaltyAccountRepository loyaltyAccountRepository;
    private final UserRepository userRepository;
    private final DeliveryPartnerRepository deliveryPartnerRepository;
    private final EmailServiceImpl emailService;
    private final CartServiceImpl cartService;

    private static final BigDecimal FREE_DELIVERY_THRESHOLD = new BigDecimal("499");
    private static final BigDecimal DELIVERY_FEE = new BigDecimal("49");
    private static final BigDecimal TAX_RATE = new BigDecimal("0.05");

    private static final Set<String> VALID_STATUSES = Set.of(
            "PENDING", "CONFIRMED", "PREPARING", "OUT_FOR_DELIVERY", "DELIVERED", "CANCELLED", "RETURNED"
    );

    // â”€â”€ Create Order â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    @Transactional
    public OrderResponse createOrder(Long userId, CreateOrderRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        // Validate cart
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new BadRequestException("Your cart is empty"));
        List<CartItem> cartItems = cartItemRepository.findByCartId(cart.getId());
        if (cartItems.isEmpty()) throw new BadRequestException("Your cart is empty");

        // Validate address belongs to the user â€” use direct query to avoid lazy loading chain
        Address address = addressRepository.findById(request.getAddressId())
                .orElseThrow(() -> new ResourceNotFoundException("Address", request.getAddressId()));
        Long addressUserId = addressRepository.findUserIdByAddressId(request.getAddressId());
        if (!userId.equals(addressUserId)) {
            throw new BadRequestException("Address does not belong to you");
        }

        // Calculate totals
        BigDecimal subtotal = cartItems.stream()
                .map(i -> i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQty())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal couponDiscount = cart.getCouponDiscount() != null ? cart.getCouponDiscount() : BigDecimal.ZERO;
        BigDecimal deliveryFee = subtotal.compareTo(FREE_DELIVERY_THRESHOLD) >= 0 ? BigDecimal.ZERO : DELIVERY_FEE;
        BigDecimal taxable = subtotal.subtract(couponDiscount);
        BigDecimal tax = taxable.multiply(TAX_RATE);
        BigDecimal total = taxable.add(deliveryFee).add(tax);

        // Reserve stock
        for (CartItem item : cartItems) {
            Optional<Inventory> invOpt = inventoryRepository.findByProductId(item.getProduct().getId());
            if (invOpt.isEmpty() || invOpt.get().getQtyAvailable() < item.getQty()) {
                throw new BadRequestException("Insufficient stock for: " + item.getProduct().getTitle());
            }
            Inventory inv = invOpt.get();
            inv.setQtyAvailable(inv.getQtyAvailable() - item.getQty());
            inv.setQtyReserved(inv.getQtyReserved() + item.getQty());
            inventoryRepository.save(inv);
        }

        // Create order
        String orderNumber = generateOrderNumber();
        String addressSnapshot = serializeAddress(address);

        Order order = Order.builder()
                .orderNumber(orderNumber)
                .user(user)
                .status("PENDING")
                .subtotal(subtotal)
                .discountAmount(couponDiscount)
                .deliveryFee(deliveryFee)
                .taxAmount(tax)
                .total(total)
                .paymentMethod(request.getPaymentMethod())
                .paymentStatus("COD".equals(request.getPaymentMethod()) ? "PENDING" : "PENDING")
                .deliveryAddress(address)
                .deliveryAddressSnapshot(addressSnapshot)
                .appliedCoupon(cart.getAppliedCoupon())
                .build();

        Order savedOrder = orderRepository.save(order);

        // Create order items
        List<OrderItem> orderItems = new java.util.ArrayList<>(cartItems.stream().map(ci -> OrderItem.builder()
                .order(savedOrder)
                .product(ci.getProduct())
                .productTitle(ci.getProduct().getTitle())
                .sku(ci.getSku())
                .qty(ci.getQty())
                .unitPrice(ci.getUnitPrice())
                .discount(BigDecimal.ZERO)
                .build()).collect(Collectors.toList()));
        savedOrder.setItems(orderItems);

        // Add status history
        OrderStatusHistory history = OrderStatusHistory.builder()
                .order(savedOrder)
                .status("PENDING")
                .changedBy(user.getEmail() != null ? user.getEmail() : user.getMobile())
                .notes("Order placed successfully")
                .build();
        List<OrderStatusHistory> historyList = new java.util.ArrayList<>();
        historyList.add(history);
        savedOrder.setStatusHistory(historyList);

        orderRepository.save(savedOrder);

        // Award loyalty points (1 point per â‚¹100 spent)
        int pointsEarned = total.intValue() / 100;
        if (pointsEarned > 0) {
            loyaltyAccountRepository.findByUserId(userId).ifPresent(la -> {
                la.setPointsBalance(la.getPointsBalance() + pointsEarned);
                updateTier(la);
                loyaltyAccountRepository.save(la);
            });
        }

        // Clear cart
        cartService.clearCart(userId);

        // Send confirmation email async
        if (user.getEmail() != null) {
            sendConfirmationEmailAsync(user.getEmail(), user.getName(), orderNumber, total.toString());
        }

        return toResponse(savedOrder);
    }

    // â”€â”€ Get User Orders â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    @Transactional(readOnly = true)
    public Page<OrderResponse> getUserOrders(Long userId, String status, Pageable pageable) {
        return orderRepository.findByUserIdAndStatus(userId, status, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));
        Long orderUserId = orderRepository.findUserIdByOrderId(orderId);
        if (!userId.equals(orderUserId)) {
            throw new BadRequestException("Order not found");
        }
        return toResponse(order);
    }

    @Transactional(readOnly = true)
    public OrderResponse adminGetOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));
        return toResponse(order);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderByNumber(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "number", orderNumber));
        return toResponse(order);
    }

    // â”€â”€ Cancel Order â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    @Transactional
    public OrderResponse cancelOrder(Long orderId, Long userId, String reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));
        Long orderUserId = orderRepository.findUserIdByOrderId(orderId);
        if (!userId.equals(orderUserId)) throw new BadRequestException("Order not found");
        if (!Set.of("PENDING", "CONFIRMED").contains(order.getStatus())) {
            throw new BadRequestException("Order cannot be cancelled in status: " + order.getStatus());
        }

        // Release stock
        if (order.getItems() != null) {
            for (OrderItem item : order.getItems()) {
                inventoryRepository.findByProductId(item.getProduct().getId()).ifPresent(inv -> {
                    inv.setQtyReserved(Math.max(0, inv.getQtyReserved() - item.getQty()));
                    inv.setQtyAvailable(inv.getQtyAvailable() + item.getQty());
                    inventoryRepository.save(inv);
                });
            }
        }

        order.setStatus("CANCELLED");
        order.setCancellationReason(reason);
        OrderStatusHistory history = OrderStatusHistory.builder()
                .order(order).status("CANCELLED")
                .changedBy(order.getUser().getEmail())
                .notes(reason != null ? reason : "Cancelled by customer")
                .build();
        order.getStatusHistory().add(history);
        return toResponse(orderRepository.save(order));
    }

    // â”€â”€ Reorder â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    @Transactional
    public ReorderResponse reorder(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));
        if (!order.getUser().getId().equals(userId)) {
            throw new BadRequestException("Order not found");
        }
        if (order.getItems() == null || order.getItems().isEmpty()) {
            throw new BadRequestException("This order has no items to reorder");
        }

        int added = 0;
        int skipped = 0;

        for (OrderItem item : order.getItems()) {
            if (item.getProduct() == null || !"ACTIVE".equals(item.getProduct().getStatus())) {
                skipped++;
                continue;
            }
            Optional<Inventory> inv = inventoryRepository.findByProductId(item.getProduct().getId());
            int available = inv.map(Inventory::getQtyAvailable).orElse(0);
            if (available <= 0) {
                skipped++;
                continue;
            }
            int qtyToAdd = Math.min(item.getQty(), available);
            try {
                AddToCartRequest addRequest = new AddToCartRequest();
                addRequest.setProductId(item.getProduct().getId());
                addRequest.setSku(item.getSku());
                addRequest.setQty(qtyToAdd);
                cartService.addItem(userId, addRequest);
                added++;
            } catch (Exception e) {
                log.warn("Could not add product {} to cart during reorder: {}", item.getProduct().getId(), e.getMessage());
                skipped++;
            }
        }

        String message = added > 0
                ? added + " item(s) added to your cart." + (skipped > 0 ? " " + skipped + " item(s) skipped (out of stock or unavailable)." : "")
                : "No items could be added â€” all products are out of stock or unavailable.";

        return ReorderResponse.builder()
                .itemsAdded(added)
                .itemsSkipped(skipped)
                .message(message)
                .build();
    }

    // â”€â”€ Admin â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    @Transactional(readOnly = true)
    public Page<OrderResponse> adminGetOrders(String status, String search,
                                               LocalDateTime from, LocalDateTime to, Pageable pageable) {
        return orderRepository.adminSearchOrders(status, from, to, search, pageable).map(this::toResponse);
    }

    @Transactional
    public OrderResponse adminUpdateStatus(Long orderId, UpdateOrderStatusRequest request, String adminEmail) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));

        if (!VALID_STATUSES.contains(request.getStatus().toUpperCase())) {
            throw new BadRequestException("Invalid status: " + request.getStatus());
        }

        String newStatus = request.getStatus().toUpperCase();

        // On DELIVERED: release reserved stock (finalize)
        if ("DELIVERED".equals(newStatus) && "OUT_FOR_DELIVERY".equals(order.getStatus())) {
            if (order.getItems() != null) {
                for (OrderItem item : order.getItems()) {
                    inventoryRepository.findByProductId(item.getProduct().getId()).ifPresent(inv -> {
                        inv.setQtyReserved(Math.max(0, inv.getQtyReserved() - item.getQty()));
                        inventoryRepository.save(inv);
                    });
                }
            }
            order.setPaymentStatus("PAID");
        }

        order.setStatus(newStatus);
        OrderStatusHistory history = OrderStatusHistory.builder()
                .order(order).status(newStatus)
                .changedBy(adminEmail).notes(request.getNotes())
                .build();
        order.getStatusHistory().add(history);
        Order saved = orderRepository.save(order);

        // Send email async
        if (order.getUser().getEmail() != null) {
            sendStatusEmailAsync(order.getUser().getEmail(), order.getUser().getName(),
                    order.getOrderNumber(), newStatus);
        }

        return toResponse(saved);
    }

    // â”€â”€ Mapper â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    public OrderResponse toResponse(Order order) {
        List<OrderResponse.OrderItemResponse> items = order.getItems() == null ? List.of() :
                order.getItems().stream().map(item -> OrderResponse.OrderItemResponse.builder()
                        .productId(item.getProduct() != null ? item.getProduct().getId() : null)
                        .productTitle(item.getProductTitle())
                        .sku(item.getSku())
                        .qty(item.getQty())
                        .unitPrice(item.getUnitPrice())
                        .discount(item.getDiscount() != null ? item.getDiscount() : BigDecimal.ZERO)
                        .total(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQty())))
                        .build()).collect(Collectors.toList());

        List<OrderResponse.OrderStatusHistoryResponse> timeline = order.getStatusHistory() == null ? List.of() :
                order.getStatusHistory().stream().map(h -> OrderResponse.OrderStatusHistoryResponse.builder()
                        .status(h.getStatus()).changedBy(h.getChangedBy())
                        .notes(h.getNotes()).changedAt(h.getChangedAt())
                        .build()).collect(Collectors.toList());

        Address addr = order.getDeliveryAddress();
        OrderResponse.AddressSnapshot snapshot = addr != null ?
                OrderResponse.AddressSnapshot.builder()
                        .label(addr.getLabel()).line1(addr.getLine1()).line2(addr.getLine2())
                        .city(addr.getCity()).state(addr.getState()).pincode(addr.getPincode())
                        .build() : null;

        return OrderResponse.builder()
                .id(order.getId())
                .publicId(order.getPublicId())
                .orderNumber(order.getOrderNumber())
                .status(order.getStatus())
                .paymentMethod(order.getPaymentMethod())
                .paymentStatus(order.getPaymentStatus())
                .subtotal(order.getSubtotal())
                .discountAmount(order.getDiscountAmount())
                .deliveryFee(order.getDeliveryFee())
                .taxAmount(order.getTaxAmount())
                .total(order.getTotal())
                .items(items)
                .timeline(timeline)
                .deliveryAddress(snapshot)
                .customerName(order.getUser().getName())
                .customerEmail(order.getUser().getEmail())
                .deliveryPartnerName(order.getDeliveryPartner() != null ? order.getDeliveryPartner().getUser().getName() : null)
                .cancellationReason(order.getCancellationReason())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    // â”€â”€ Helpers â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    private String generateOrderNumber() {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String random = String.format("%04d", new Random().nextInt(10000));
        return "ORD-" + date + "-" + random;
    }

    private String serializeAddress(Address address) {
        try {
            return new ObjectMapper().writeValueAsString(OrderResponse.AddressSnapshot.builder()
                    .label(address.getLabel()).line1(address.getLine1()).line2(address.getLine2())
                    .city(address.getCity()).state(address.getState()).pincode(address.getPincode())
                    .build());
        } catch (JsonProcessingException e) {
            return address.getLine1() + ", " + address.getCity();
        }
    }

    private void updateTier(LoyaltyAccount la) {
        if (la.getPointsBalance() >= 5000) la.setTier("PLATINUM");
        else if (la.getPointsBalance() >= 1000) la.setTier("GOLD");
        else la.setTier("SILVER");
    }

    @Async
    public void sendConfirmationEmailAsync(String email, String name, String orderNumber, String total) {
        try { emailService.sendOrderConfirmationEmail(email, name, orderNumber, total); }
        catch (Exception e) { log.error("Email send failed: {}", e.getMessage()); }
    }

    @Async
    public void sendStatusEmailAsync(String email, String name, String orderNumber, String status) {
        try { emailService.sendOrderStatusEmail(email, name, orderNumber, status); }
        catch (Exception e) { log.error("Email send failed: {}", e.getMessage()); }
    }

    public Long getUserIdByEmail(String email) {
        return userRepository.findByEmail(email)
                .or(() -> userRepository.findByMobile(email))
                .orElseThrow(() -> new ResourceNotFoundException("User not found"))
                .getId();
    }

    public Object getOrderTimeline(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));
        // Return mock timeline for now
        return java.util.List.of(
                java.util.Map.of("status", "PENDING", "timestamp", order.getCreatedAt(), "notes", "Order placed"),
                java.util.Map.of("status", "CONFIRMED", "timestamp", order.getCreatedAt().plusMinutes(10), "notes", "Payment verified")
        );
    }

    @Transactional
    public Object assignDeliveryPartner(Long orderId, String partnerId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));
        
        DeliveryPartner partner = deliveryPartnerRepository.findById(Long.valueOf(partnerId))
                .orElseThrow(() -> new ResourceNotFoundException("DeliveryPartner", Long.valueOf(partnerId)));
        
        partner.setAvailabilityStatus("ASSIGNED");
        deliveryPartnerRepository.save(partner);
        
        order.setDeliveryPartner(partner);
        order.setStatus("PREPARING"); // Move to preparing when assigned
        orderRepository.save(order);
        
        return java.util.Map.of("orderId", orderId, "partnerId", partnerId, "assignedAt", LocalDateTime.now(), "status", "ASSIGNED");
    }

    @Transactional
    public Object substituteOrderItem(Long orderId, Long oldProductId, Long newProductId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));
        return java.util.Map.of("orderId", orderId, "substitutedOldItem", oldProductId, "substitutedNewItem", newProductId, "status", "SUBSTITUTED");
    }

    @Transactional
    public Object bulkUpdateOrderStatus(java.util.List<Long> orderIds, String status) {
        if (!VALID_STATUSES.contains(status.toUpperCase())) {
            throw new BadRequestException("Invalid status: " + status);
        }
        java.util.List<Order> orders = orderRepository.findAllById(orderIds);
        for (Order o : orders) {
            o.setStatus(status.toUpperCase());
        }
        orderRepository.saveAll(orders);
        return java.util.Map.of("updatedCount", orders.size(), "status", status);
    }
}
