package com.fmcg.ecommerce.controller.account;

import com.fmcg.ecommerce.common.ApiResponse;
import com.fmcg.ecommerce.dto.account.ShoppingListDto;
import com.fmcg.ecommerce.entity.User;
import com.fmcg.ecommerce.repository.UserRepository;
import com.fmcg.ecommerce.service.ShoppingListService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/lists")
@RequiredArgsConstructor
@Tag(name = "Customer - Shopping Lists", description = "APIs for managing custom shared/personal lists")
@SecurityRequirement(name = "bearerAuth")
public class ShoppingListController {

    private final ShoppingListService shoppingListService;
    private final UserRepository userRepository;

    private Long getUserId(Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getId();
    }

    @GetMapping
    @Operation(summary = "Get all shopping lists for the user")
    public ResponseEntity<ApiResponse<List<ShoppingListDto.ListResponse>>> getShoppingLists(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.ok(shoppingListService.getShoppingLists(getUserId(auth))));
    }

    @PostMapping
    @Operation(summary = "Create a new shopping list")
    public ResponseEntity<ApiResponse<ShoppingListDto.ListResponse>> createShoppingList(
            Authentication auth,
            @RequestBody ShoppingListDto.CreateListRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("List created successfully", 
                shoppingListService.createShoppingList(getUserId(auth), request)));
    }

    @PostMapping("/{listId}/items")
    @Operation(summary = "Add an item to a specific list")
    public ResponseEntity<ApiResponse<ShoppingListDto.ListResponse>> addItemToList(
            Authentication auth,
            @PathVariable Long listId,
            @RequestBody ShoppingListDto.AddItemRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Item added to list", 
                shoppingListService.addProductToList(getUserId(auth), listId, request)));
    }

    @DeleteMapping("/{listId}/items/{productId}")
    @Operation(summary = "Remove an item from a list")
    public ResponseEntity<ApiResponse<String>> removeItemFromList(
            Authentication auth,
            @PathVariable Long listId,
            @PathVariable Long productId) {
        shoppingListService.removeProductFromList(getUserId(auth), listId, productId);
        return ResponseEntity.ok(ApiResponse.ok("Item removed successfully"));
    }

    @DeleteMapping("/{listId}")
    @Operation(summary = "Delete an entire shopping list")
    public ResponseEntity<ApiResponse<String>> deleteList(
            Authentication auth,
            @PathVariable Long listId) {
        shoppingListService.deleteShoppingList(getUserId(auth), listId);
        return ResponseEntity.ok(ApiResponse.ok("Shopping list deleted successfully"));
    }
}
