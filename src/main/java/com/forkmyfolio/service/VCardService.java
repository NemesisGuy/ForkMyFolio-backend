package com.forkmyfolio.service;

import com.forkmyfolio.model.PortfolioProfile;
import com.forkmyfolio.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Base64;
import java.util.Optional;

@Service
public class VCardService {

    private static final Logger log = LoggerFactory.getLogger(VCardService.class);

    public record VCardFile(byte[] content, String suggestedFilename) {}

    public VCardFile generateVCard(PortfolioProfile profile) {
        User user = profile.getUser();
        String fullName = user.getFirstName() + " " + user.getLastName();

        StringBuilder vcfBuilder = new StringBuilder();
        vcfBuilder.append("BEGIN:VCARD\n");
        vcfBuilder.append("VERSION:3.0\n");
        vcfBuilder.append("FN:").append(fullName).append("\n");
        vcfBuilder.append("N:").append(user.getLastName()).append(";").append(user.getFirstName()).append(";;;\n");

        if (profile.getPublicEmail() != null && !profile.getPublicEmail().isBlank()) {
            vcfBuilder.append("EMAIL;TYPE=INTERNET:").append(profile.getPublicEmail()).append("\n");
        }
        if (profile.getWebsiteUrl() != null && !profile.getWebsiteUrl().isBlank()) {
            vcfBuilder.append("URL:").append(profile.getWebsiteUrl()).append("\n");
        }

        // --- NEW: Add Photo from URL ---
        if (profile.getResumeImageUrl() != null && !profile.getResumeImageUrl().isBlank()) {
            getBase64ImageFromUrl(profile.getResumeImageUrl()).ifPresent(base64Image -> {
                // Assuming JPEG format, which is common. A more advanced implementation
                // could inspect the image bytes or URL to determine the type.
                vcfBuilder.append("PHOTO;TYPE=JPEG;ENCODING=BASE64:").append(base64Image).append("\n");
                log.info("Successfully added photo to vCard for user: {}", user.getEmail());
            });
        }

        vcfBuilder.append("END:VCARD\n");

        String filename = String.format("%s%s.vcf", user.getFirstName(), user.getLastName());
        return new VCardFile(vcfBuilder.toString().getBytes(), filename);
    }

    /**
     * Fetches an image from a URL and encodes it as a Base64 string.
     *
     * @param imageUrl The URL of the image to fetch.
     * @return An Optional containing the Base64 encoded image string, or empty if fetching fails.
     */
    private Optional<String> getBase64ImageFromUrl(String imageUrl) {
        log.debug("Attempting to fetch and encode image for vCard from URL: {}", imageUrl);
        // Use a try-with-resources block for automatic resource management
        try (InputStream in = new URL(imageUrl).openStream()) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buf = new byte[4096]; // A slightly larger buffer can be more efficient
            int n;
            while ((n = in.read(buf)) != -1) {
                out.write(buf, 0, n);
            }
            byte[] imageBytes = out.toByteArray();
            return Optional.of(Base64.getEncoder().encodeToString(imageBytes));
        } catch (IOException e) {
            log.error("Failed to fetch or encode image for vCard from URL '{}': {}", imageUrl, e.getMessage());
            return Optional.empty();
        }
    }
}