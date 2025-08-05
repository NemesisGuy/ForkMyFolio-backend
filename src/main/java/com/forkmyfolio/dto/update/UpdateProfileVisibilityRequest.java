package com.forkmyfolio.dto.update;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UpdateProfileVisibilityRequest {

    /**
     * The desired public visibility state for the entire portfolio.
     */
    @NotNull(message = "Visibility status cannot be null.")
    private Boolean isPublic;
}