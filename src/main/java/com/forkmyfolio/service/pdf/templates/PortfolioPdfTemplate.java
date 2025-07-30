package com.forkmyfolio.service.pdf.templates;

import com.forkmyfolio.service.impl.PdfGenerationService;
import com.forkmyfolio.service.pdf.PortfolioData;
import com.itextpdf.kernel.geom.PageSize; // <-- IMPORT
import com.itextpdf.layout.Document;

import java.io.IOException;

/**
 * Defines the contract for a PDF template. Each implementation is responsible
 * for rendering the portfolio data into the provided iText Document.
 */
public interface PortfolioPdfTemplate {

    /**
     * Generates the full PDF content and layout into the document.
     *
     * @param document The iText Document to which content will be added.
     * @param context  The context containing shared resources like fonts.
     * @param data     The portfolio data to be rendered.
     * @throws IOException if a resource (like a font) cannot be loaded.
     */
    void generate(Document document, PdfGenerationService.PdfContext context, PortfolioData data) throws IOException;

    /**
     * Defines the page size for this template.
     *
     * @return The iText PageSize object. Defaults to A4.
     */
    default PageSize getPageSize() {
        return PageSize.A4;
    }
}