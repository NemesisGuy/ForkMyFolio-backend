package com.forkmyfolio.mapper;

import com.forkmyfolio.dto.response.UserDto;
import com.forkmyfolio.dto.update.UpdateUserRequest;
import com.forkmyfolio.model.User;
import org.springframework.stereotype.Component;

/**
 * Mapper class responsible for converting between User domain models and User-related DTOs.
 * This keeps the conversion logic separate from the service and controller layers.
 */
@Component // Or make methods static if no dependencies are needed
public class UserMapper {

    /**
     * Converts a User entity to a UserDto.
     *
     * @param user The User entity to convert.
     * @return The corresponding UserDto.
     */
    public UserDto toDto(User user) {
        if (user == null) {
            return null;
        }
        UserDto dto = new UserDto();
        /* dto.setId(user.getId());*/
        dto.setId(user.getUuid());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setProfileImageUrl(user.getProfileImageUrl());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        // Convert the Set<Role> to Set<String> for the DTO
        dto.setRoles(user.getRoles());
        return dto;
    }

    /**
     * Applies updates from an UpdateUserRequest DTO to an existing User entity.
     * This method modifies the passed-in User object directly.
     *
     * @param request The DTO containing the update information.
     * @param user    The User entity to be updated.
     */
    public void applyUpdateFromRequest(UpdateUserRequest request, User user) {
        if (request == null || user == null) {
            return;
        }
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setProfileImageUrl(request.getProfileImageUrl());
        // Add any other fields from UpdateUserRequest here
    }
}