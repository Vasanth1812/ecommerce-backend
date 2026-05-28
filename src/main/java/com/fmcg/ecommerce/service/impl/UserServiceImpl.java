package com.fmcg.ecommerce.service.impl;

import com.fmcg.ecommerce.dto.auth.UserSummaryDto;
import com.fmcg.ecommerce.dto.user.AddressRequest;
import com.fmcg.ecommerce.dto.user.AddressResponse;
import com.fmcg.ecommerce.dto.user.UpdateProfileRequest;
import com.fmcg.ecommerce.entity.Address;
import com.fmcg.ecommerce.entity.User;
import com.fmcg.ecommerce.exception.BadRequestException;
import com.fmcg.ecommerce.exception.ResourceNotFoundException;
import com.fmcg.ecommerce.repository.AddressRepository;
import com.fmcg.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl {

    private final UserRepository userRepository;
    private final AddressRepository addressRepository;

    public UserSummaryDto getProfile(String identifier) {
        User user = findUser(identifier);
        return toUserSummary(user);
    }

    @Transactional
    public UserSummaryDto updateProfile(String identifier, UpdateProfileRequest request) {
        User user = findUser(identifier);
        if (request.getName() != null && !request.getName().isBlank()) user.setName(request.getName());
        if (request.getMobile() != null && !request.getMobile().isBlank()) {
            if (!request.getMobile().equals(user.getMobile()) && userRepository.existsByMobile(request.getMobile()))
                throw new BadRequestException("Mobile number already registered");
            user.setMobile(request.getMobile());
        }
        if(request.getEmail()!=null && !request.getEmail().isBlank()) user.setEmail(request.getEmail());
        return toUserSummary(userRepository.save(user));
    }

    public List<AddressResponse> getAddresses(String identifier) {
        User user = findUser(identifier);
        return addressRepository.findByUserId(user.getId())
                .stream().map(this::toAddressResponse).collect(Collectors.toList());
    }

    @Transactional
    public AddressResponse addAddress(String identifier, AddressRequest request) {
        User user = findUser(identifier);
        if (addressRepository.countByUserId(user.getId()) >= 5)
            throw new BadRequestException("Maximum 5 addresses allowed");
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            addressRepository.unsetAllDefaults(user.getId());
        }
        Address address = Address.builder()
                .user(user)
                .label(request.getLabel())
                .line1(request.getLine1())
                .line2(request.getLine2())
                .city(request.getCity())
                .state(request.getState())
                .pincode(request.getPincode())
                .lat(request.getLat())
                .lng(request.getLng())
                .isDefault(Boolean.TRUE.equals(request.getIsDefault()))
                .build();
        return toAddressResponse(addressRepository.save(address));
    }

    @Transactional
    public AddressResponse updateAddress(String identifier, Long addressId, AddressRequest request) {
        User user = findUser(identifier);
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", addressId));
        if (!address.getUser().getId().equals(user.getId())) throw new BadRequestException("Address not found");
        if (Boolean.TRUE.equals(request.getIsDefault())) addressRepository.unsetAllDefaults(user.getId());
        address.setLabel(request.getLabel());
        address.setLine1(request.getLine1());
        address.setLine2(request.getLine2());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setPincode(request.getPincode());
        address.setLat(request.getLat());
        address.setLng(request.getLng());
        address.setIsDefault(Boolean.TRUE.equals(request.getIsDefault()));
        return toAddressResponse(addressRepository.save(address));
    }

    @Transactional
    public void deleteAddress(String identifier, Long addressId) {
        User user = findUser(identifier);
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", addressId));
        if (!address.getUser().getId().equals(user.getId())) throw new BadRequestException("Address not found");
        addressRepository.delete(address);
    }

    @Transactional
    public void setDefaultAddress(String identifier, Long addressId) {
        User user = findUser(identifier);
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", addressId));
        if (!address.getUser().getId().equals(user.getId())) throw new BadRequestException("Address not found");
        addressRepository.unsetAllDefaults(user.getId());
        address.setIsDefault(true);
        addressRepository.save(address);
    }

    // ── Helpers ───────────────────────────────────────────

    private User findUser(String identifier) {
        return userRepository.findByEmail(identifier)
                .or(() -> userRepository.findByMobile(identifier))
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private UserSummaryDto toUserSummary(User user) {
        return UserSummaryDto.builder()
                .id(user.getId()).name(user.getName())
                .email(user.getEmail()).mobile(user.getMobile())
                .role(user.getRole()).status(user.getStatus())
                .build();
    }

    private AddressResponse toAddressResponse(Address a) {
        return AddressResponse.builder()
                .id(a.getId()).label(a.getLabel())
                .line1(a.getLine1()).line2(a.getLine2())
                .city(a.getCity()).state(a.getState()).pincode(a.getPincode())
                .lat(a.getLat()).lng(a.getLng())
                .isDefault(Boolean.TRUE.equals(a.getIsDefault()))
                .build();
    }
}
