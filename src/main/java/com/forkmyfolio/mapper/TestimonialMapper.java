package com.forkmyfolio.mapper;

import com.forkmyfolio.dto.create.CreateTestimonialRequest;
import com.forkmyfolio.dto.response.TestimonialDto;
import com.forkmyfolio.dto.update.UpdateTestimonialRequest;
import com.forkmyfolio.model.Testimonial;
import com.forkmyfolio.model.User;
import org.springframework.stereotype.Component;

/**
 * Maps between Testimonial domain objects and their related DTOs.
 */
@Component
public class TestimonialMapper {

    /**
     * Converts a Testimonial entity to a TestimonialDto for API responses.
     *
     * @param testimonial The entity to convert.
     * @return The corresponding DTO.
     */
    public TestimonialDto toDto(Testimonial testimonial) {
        if (testimonial == null) {
            return null;
        }
        // KEY CHANGE: Use setters for clarity and correctness.
        TestimonialDto dto = new TestimonialDto();
        dto.setUuid(testimonial.getUuid());
        dto.setQuote(testimonial.getQuote());
        dto.setAuthorName(testimonial.getAuthorName());
        // Corrected to use authorTitle from the entity.
        dto.setAuthorTitle(testimonial.getAuthorTitle());
        return dto;
    }

    /**
     * Converts a CreateTestimonialRequest DTO into a new Testimonial entity.
     *
     * @param request The DTO with new testimonial data.
     * @param owner   The User who will own this testimonial.
     * @return A new Testimonial entity, ready to be persisted.
     */
    public Testimonial toEntity(CreateTestimonialRequest request, User owner) {
        if (request == null) {
            return null;
        }
        Testimonial testimonial = new Testimonial();
        testimonial.setQuote(request.getQuote());
        testimonial.setAuthorName(request.getAuthorName());
        testimonial.setAuthorTitle(request.getAuthorTitle());
        testimonial.setUser(owner);
        return testimonial;
    }

    /**
     * Converts a TestimonialDto from a backup file into a new Testimonial entity.
     * This is used by the RestoreService.
     *
     * @param dto   The DTO from the backup file.
     * @param owner The User who will own this new testimonial.
     * @return A new Testimonial entity, ready to be persisted.
     */
    public Testimonial toEntityFromDto(TestimonialDto dto, User owner) {
        if (dto == null) {
            return null;
        }
        Testimonial testimonial = new Testimonial();
        // Note: We do not set ID or UUID, allowing the DB to generate them.
        testimonial.setQuote(dto.getQuote());
        testimonial.setAuthorName(dto.getAuthorName());
        // Corrected to use authorTitle from the DTO.
        testimonial.setAuthorTitle(dto.getAuthorTitle());
        testimonial.setUser(owner);
        return testimonial;
    }

    /**
     * Applies updates from an UpdateTestimonialRequest to an existing Testimonial entity.
     *
     * @param request     The DTO with the fields to update.
     * @param testimonial The existing entity to be updated.
     */
    public void applyUpdateFromRequest(UpdateTestimonialRequest request, Testimonial testimonial) {
        if (request == null || testimonial == null) {
            return;
        }

        request.getQuote().ifPresent(testimonial::setQuote);
        request.getAuthorName().ifPresent(testimonial::setAuthorName);
        request.getAuthorTitle().ifPresent(testimonial::setAuthorTitle);
    }
}