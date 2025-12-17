package com.wallet.biochain.mappers;

import com.wallet.biochain.dto.UserDTO;
import com.wallet.biochain.entities.User;
import com.wallet.biochain.enums.Role;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class UserMapper {

    public UserDTO toDTO(User user) {
        if (user == null) {
            return null;
        }

        Set<String> roles = user.getRoles() != null 
                ? user.getRoles().stream().map(Role::name).collect(Collectors.toSet())
                : Set.of();

        return new UserDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getPhoneNumber(),
                user.getIsActive(),
                user.getWallets() != null ? user.getWallets().size() : 0,
                user.getCreatedAt(),
                roles
        );
    }

    public List<UserDTO> toDTOList(List<User> users) {
        if (users == null) {
            return null;
        }

        return users.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
}