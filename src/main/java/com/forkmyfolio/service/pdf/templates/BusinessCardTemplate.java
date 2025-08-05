package com.forkmyfolio.service.pdf.templates;

import com.forkmyfolio.model.PortfolioProfile;
import com.forkmyfolio.model.User;
import com.forkmyfolio.service.impl.PdfGenerationService;
import com.forkmyfolio.service.pdf.PortfolioData;
import com.itextpdf.barcodes.BarcodeQRCode;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.action.PdfAction;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;

import java.io.IOException;

public class BusinessCardTemplate implements PortfolioPdfTemplate {

    @Override
    public PageSize getPageSize() {
        // Standard US business card: 3.5 x 2 inches. 1 inch = 72 points.
        return new PageSize(3.5f * 72, 2f * 72);
    }

    @Override
    public void generate(Document document, PdfGenerationService.PdfContext ctx, PortfolioData data) throws IOException {
        document.setMargins(18, 18, 18, 18); // 0.25 inch margins

        PortfolioProfile profile = data.profile();
        User user = profile.getUser();

        Table mainTable = new Table(UnitValue.createPercentArray(new float[]{3, 2})).useAllAvailableWidth();
        mainTable.setHeight(UnitValue.createPercentValue(100));

        // Left side: Text info
        Cell leftCell = new Cell().setBorder(Border.NO_BORDER).setVerticalAlignment(VerticalAlignment.MIDDLE);
        leftCell.add(new Paragraph(user.getFirstName() + " " + user.getLastName())
                .setFont(ctx.nameFont).setFontSize(14).setMarginBottom(2));
        leftCell.add(new Paragraph(profile.getHeadline())
                .setFont(ctx.itemSubtitleFont).setFontSize(8).setFontColor(PdfGenerationService.SECONDARY_COLOR).setMarginBottom(10));

        if (profile.getPublicEmail() != null) {
            leftCell.add(new Paragraph(profile.getPublicEmail()).setFontSize(7));
        }

        // --- MODIFIED: Make website URL a clickable link ---
        if (profile.getWebsiteUrl() != null && !profile.getWebsiteUrl().isBlank()) {
            String cleanUrl = profile.getWebsiteUrl().replaceFirst("^(https?://)", "");
            Text linkText = new Text(cleanUrl)
                    .setFontSize(7)
                    .setFontColor(PdfGenerationService.ACCENT_COLOR) // Use a standard accent color
                    .setUnderline()
                    .setAction(PdfAction.createURI(profile.getWebsiteUrl()));
            leftCell.add(new Paragraph(linkText));
        }
        mainTable.addCell(leftCell);

        // Right side: QR Code
        Cell rightCell = new Cell().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT).setVerticalAlignment(VerticalAlignment.MIDDLE);
        if (profile.getWebsiteUrl() != null && !profile.getWebsiteUrl().isBlank()) {
            BarcodeQRCode qrCode = new BarcodeQRCode(profile.getWebsiteUrl());
            Image qrImage = new Image(qrCode.createFormXObject(ColorConstants.BLACK, ctx.pdfDocument))
                    .setWidth(72).setHeight(72); // 1x1 inch
            rightCell.add(qrImage);
        }
        mainTable.addCell(rightCell);

        document.add(mainTable);
    }
}