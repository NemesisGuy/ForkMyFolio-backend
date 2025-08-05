package com.forkmyfolio.mapper;

import com.forkmyfolio.dto.response.PublicUserDto;
import com.forkmyfolio.dto.response.UserDto;
import com.forkmyfolio.dto.update.UpdateUserAccountRequest;
import com.forkmyfolio.model.User;
import org.springframework.stereotype.Component;

/**
 * Mapper class responsible for converting between User domain models and User-related DTOs.
 * This keeps the conversion logic separate from the service and controller layers.
 */
@Component
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
        dto.setId(user.getUuid());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setSlug(user.getSlug());
        dto.setProfileImageUrl(user.getProfileImageUrl());
        dto.setProvider(user.getProvider());
        dto.setProviderId(user.getProviderId());
        dto.setActive(user.isActive());
        dto.setRoles(user.getRoles());


        return dto;
    }

    /**
     * Applies updates from an UpdateUserAccountRequest DTO to an existing User entity.
     * This method modifies the passed-in User object directly.
     *
     * @param request The DTO containing the update information.
     * @param user    The User entity to be updated.
     */
    public void applyUpdateFromRequest(UpdateUserAccountRequest request, User user) {
        if (request == null || user == null) {
            return;
        }
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setProfileImageUrl(request.getProfileImageUrl());
    }


    /**
     * Converts a User entity to a PublicUserDto, exposing only public information.
     *
     * @param user The User entity to convert.
     * @return The corresponding PublicUserDto.
     */
    public PublicUserDto toPublicUserDto(User user) {
        if (user == null) {
            return null;
        }
        PublicUserDto dto = new PublicUserDto();
        dto.setSlug(user.getSlug());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setProfileImageUrl(user.getProfileImageUrl());
        dto.setActive(user.isActive());
        dto.setRoles(user.getRoles());

        return dto;
    }
}