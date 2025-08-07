package com.forkmyfolio.controller.guest;

import com.forkmyfolio.aop.TrackVisitor;
import com.forkmyfolio.dto.response.PortfolioDto;
import com.forkmyfolio.dto.response.PublicUserDto;
import com.forkmyfolio.dto.response.PortfolioProfileDto;
import com.forkmyfolio.mapper.PublicUserMapper;
import com.forkmyfolio.mapper.PortfolioProfileMapper;
import com.forkmyfolio.model.User;
import com.forkmyfolio.model.enums.VisitorStatType;
import com.forkmyfolio.service.PortfolioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/portfolios")
@Tag(name = "Public Portfolios", description = "Endpoints for viewing public user portfolios.")
@RequiredArgsConstructor
public class PortfolioController {

    private final PortfolioService portfolioService;
    private final PublicUserMapper publicUserMapper;
    private final PortfolioProfileMapper portfolioProfileMapper;

    @GetMapping("/{slug}")
    @Operation(summary = "Get a user's public portfolio shell by their slug",
            description = "Retrieves the main public sections of a user's portfolio, such as their profile.")
    @TrackVisitor(VisitorStatType.TOTAL_VISITS)
    public ResponseEntity<PortfolioDto> getPortfolioBySlug(
            @Parameter(description = "The unique, URL-friendly slug of the user.", example = "jane-doe")
            @PathVariable String slug) {

        User user = portfolioService.getPublicPortfolioUserBySlug(slug);

        PublicUserDto userDto = publicUserMapper.toDto(user);
        PortfolioProfileDto profileDto = portfolioProfileMapper.toDto(user.getPortfolioProfile());

        PortfolioDto portfolioDto = new PortfolioDto();
        portfolioDto.setUser(userDto);
        portfolioDto.setProfile(profileDto);

        return ResponseEntity.ok(portfolioDto);
    }
}
