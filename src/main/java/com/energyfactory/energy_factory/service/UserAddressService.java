package com.energyfactory.energy_factory.service;

import com.energyfactory.energy_factory.dto.UserAddressCreateRequestDto;
import com.energyfactory.energy_factory.dto.UserAddressResponseDto;
import com.energyfactory.energy_factory.entity.User;
import com.energyfactory.energy_factory.entity.UserAddress;
import com.energyfactory.energy_factory.exception.BusinessException;
import com.energyfactory.energy_factory.repository.UserAddressRepository;
import com.energyfactory.energy_factory.repository.UserRepository;
import com.energyfactory.energy_factory.utils.enums.ResultCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserAddressService {

    private final UserAddressRepository userAddressRepository;
    private final UserRepository userRepository;

    @Transactional
    public UserAddressResponseDto createAddress(Long userId, UserAddressCreateRequestDto requestDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ResultCode.USER_NOT_FOUND));

        boolean hasDefault = userAddressRepository.existsByUserAndIsDefaultTrue(user);
        boolean shouldSetAsDefault = requestDto.getIsDefault() || !hasDefault;

        if (shouldSetAsDefault && hasDefault) {
            userAddressRepository.clearDefaultForUser(user);
        }

        UserAddress userAddress = UserAddress.builder()
                .user(user)
                .recipientName(requestDto.getRecipientName())
                .phone(requestDto.getPhone())
                .postalCode(requestDto.getPostalCode())
                .addressLine1(requestDto.getAddressLine1())
                .addressLine2(requestDto.getAddressLine2())
                .isDefault(shouldSetAsDefault)
                .build();

        userAddressRepository.save(userAddress);
        return convertToResponseDto(userAddress);
    }

    public List<UserAddressResponseDto> getUserAddresses(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ResultCode.USER_NOT_FOUND));

        List<UserAddress> addresses = userAddressRepository.findByUserOrderByIsDefaultDescCreatedAtDesc(user);
        return addresses.stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    public UserAddressResponseDto getAddress(Long userId, Long addressId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ResultCode.USER_NOT_FOUND));

        UserAddress address = userAddressRepository.findByUserAndId(user, addressId)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND));

        return convertToResponseDto(address);
    }

    @Transactional
    public UserAddressResponseDto updateAddress(Long userId, Long addressId, UserAddressCreateRequestDto requestDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ResultCode.USER_NOT_FOUND));

        UserAddress address = userAddressRepository.findByUserAndId(user, addressId)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND));

        if (requestDto.getIsDefault() && !address.getIsDefault()) {
            userAddressRepository.clearDefaultForUser(user);
        }

        UserAddress updatedAddress = UserAddress.builder()
                .id(address.getId())
                .user(user)
                .recipientName(requestDto.getRecipientName())
                .phone(requestDto.getPhone())
                .postalCode(requestDto.getPostalCode())
                .addressLine1(requestDto.getAddressLine1())
                .addressLine2(requestDto.getAddressLine2())
                .isDefault(requestDto.getIsDefault())
                .createdAt(address.getCreatedAt())
                .build();

        userAddressRepository.save(updatedAddress);
        return convertToResponseDto(updatedAddress);
    }

    @Transactional
    public void deleteAddress(Long userId, Long addressId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ResultCode.USER_NOT_FOUND));

        UserAddress address = userAddressRepository.findByUserAndId(user, addressId)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND));

        boolean wasDefault = address.getIsDefault();
        userAddressRepository.delete(address);

        if (wasDefault) {
            List<UserAddress> remainingAddresses = userAddressRepository.findByUserOrderByIsDefaultDescCreatedAtDesc(user);
            if (!remainingAddresses.isEmpty()) {
                UserAddress firstAddress = remainingAddresses.get(0);
                firstAddress.setAsDefault();
                userAddressRepository.save(firstAddress);
            }
        }
    }

    @Transactional
    public UserAddressResponseDto setDefaultAddress(Long userId, Long addressId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ResultCode.USER_NOT_FOUND));

        UserAddress address = userAddressRepository.findByUserAndId(user, addressId)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND));

        if (!address.getIsDefault()) {
            userAddressRepository.clearDefaultForUser(user);
            address.setAsDefault();
            userAddressRepository.save(address);
        }

        return convertToResponseDto(address);
    }

    public UserAddressResponseDto getDefaultAddress(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ResultCode.USER_NOT_FOUND));

        UserAddress defaultAddress = userAddressRepository.findByUserAndIsDefaultTrue(user)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND));

        return convertToResponseDto(defaultAddress);
    }

    private UserAddressResponseDto convertToResponseDto(UserAddress address) {
        return UserAddressResponseDto.builder()
                .id(address.getId())
                .recipientName(address.getRecipientName())
                .phone(address.getPhone())
                .postalCode(address.getPostalCode())
                .addressLine1(address.getAddressLine1())
                .addressLine2(address.getAddressLine2())
                .isDefault(address.getIsDefault())
                .createdAt(address.getCreatedAt())
                .updatedAt(address.getUpdatedAt())
                .build();
    }
}