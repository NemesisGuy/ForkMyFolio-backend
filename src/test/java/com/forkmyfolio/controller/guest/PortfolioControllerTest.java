package com.forkmyfolio.controller.guest;

import com.forkmyfolio.mapper.PortfolioProfileMapper;
import com.forkmyfolio.mapper.PublicUserMapper;
import com.forkmyfolio.model.User;
import com.forkmyfolio.service.PortfolioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PortfolioControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PortfolioService portfolioService;

    @Mock
    private PublicUserMapper publicUserMapper;

    @Mock
    private PortfolioProfileMapper portfolioProfileMapper;

    @InjectMocks
    private PortfolioController portfolioController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(portfolioController).build();
    }

    @Test
    void getPortfolioBySlug_shouldReturnOk() throws Exception {
        User user = new User();
        when(portfolioService.getPublicPortfolioUserBySlug("test-slug")).thenReturn(user);

        mockMvc.perform(get("/api/v1/portfolios/{slug}", "test-slug"))
                .andExpect(status().isOk());
    }
}
