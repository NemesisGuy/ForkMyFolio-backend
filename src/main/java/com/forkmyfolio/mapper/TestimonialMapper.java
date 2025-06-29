package com.forkmyfolio.mapper;

import com.forkmyfolio.dto.CreateTestimonialRequest;
import com.forkmyfolio.dto.TestimonialDto;
import com.forkmyfolio.dto.UpdateTestimonialRequest;
import com.forkmyfolio.model.Testimonial;
import com.forkmyfolio.model.User;
import org.springframework.stereotype.Component;

@Component
public class TestimonialMapper {

    public TestimonialDto toDto(Testimonial testimonial) {
        if (testimonial == null) return null;
        TestimonialDto dto = new TestimonialDto();
        dto.setId(testimonial.getId());
        dto.setQuote(testimonial.getQuote());
        dto.setAuthorName(testimonial.getAuthorName());
        dto.setAuthorTitle(testimonial.getAuthorTitle());
        return dto;
    }

    public Testimonial toEntity(CreateTestimonialRequest request, User owner) {
        if (request == null) return null;
        Testimonial testimonial = new Testimonial();
        testimonial.setQuote(request.getQuote());
        testimonial.setAuthorName(request.getAuthorName());
        testimonial.setAuthorTitle(request.getAuthorTitle());
        testimonial.setUser(owner);
        return testimonial;
    }

    public void applyUpdateFromRequest(UpdateTestimonialRequest request, Testimonial testimonial) {
        if (request == null || testimonial == null) return;
        request.getQuote().ifPresent(testimonial::setQuote);
        request.getAuthorName().ifPresent(testimonial::setAuthorName);
        request.getAuthorTitle().ifPresent(testimonial::setAuthorTitle);
    }
}