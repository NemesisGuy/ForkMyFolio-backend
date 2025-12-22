
package com.forkmyfolio.controller.guest;

import com.forkmyfolio.model.User;
import com.forkmyfolio.service.PortfolioService;
import com.forkmyfolio.service.impl.MarkdownGenerationService;
import com.forkmyfolio.service.impl.PdfGenerationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class PortfolioDownloadControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PdfGenerationService pdfGenerationService;

    @Mock
    private MarkdownGenerationService markdownGenerationService;

    @Mock
    private PortfolioService portfolioService;

    @InjectMocks
    private PortfolioDownloadController portfolioDownloadController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(portfolioDownloadController).build();
    }

    @Test
    void downloadPortfolioAsPdf_shouldReturnPdfFile() throws Exception {
        User user = new User();
        user.setFirstName("John");
        user.setLastName("Doe");
        byte[] pdfContent = "test-pdf-content".getBytes(StandardCharsets.UTF_8);
        PdfGenerationService.PdfFile pdfFile = new PdfGenerationService.PdfFile(pdfContent, "portfolio.pdf");

        when(portfolioService.getPublicPortfolioUserBySlug(anyString())).thenReturn(user);
        when(pdfGenerationService.generatePortfolioPdf(any(User.class), anyString())).thenReturn(pdfFile);

        mockMvc.perform(get("/api/v1/portfolios/john-doe/pdf"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"portfolio.pdf\""))
                .andExpect(content().bytes(pdfContent));
    }

    @Test
    void downloadPortfolioAsMarkdown_shouldReturnMarkdownFile() throws Exception {
        User user = new User();
        user.setFirstName("Jane");
        user.setLastName("Doe");
        byte[] mdContent = "test-md-content".getBytes(StandardCharsets.UTF_8);
        MarkdownGenerationService.MarkdownFile mdFile = new MarkdownGenerationService.MarkdownFile(mdContent,
                "portfolio.md");

        when(portfolioService.getPublicPortfolioUserBySlug(anyString())).thenReturn(user);
        when(markdownGenerationService.generatePortfolioMarkdown(any(User.class))).thenReturn(mdFile);

        mockMvc.perform(get("/api/v1/portfolios/jane-doe/markdown"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/markdown;charset=UTF-8"))
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"portfolio.md\""))
                .andExpect(content().string(is(new String(mdContent, StandardCharsets.UTF_8))));
    }
}
