package com.forkmyfolio.service.impl;

import com.forkmyfolio.exception.ResourceNotFoundException;
import com.forkmyfolio.model.Testimonial;
import com.forkmyfolio.model.User;
import com.forkmyfolio.repository.TestimonialRepository;
import com.forkmyfolio.service.TestimonialService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TestimonialServiceImpl implements TestimonialService {

    private final TestimonialRepository testimonialRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Testimonial> getTestimonialsForUser(User user) {
        return testimonialRepository.findByUserOrderByCreatedAtDesc(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Testimonial findTestimonialByUuidAndUser(UUID uuid, User user) {
        Testimonial testimonial = testimonialRepository.findByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Testimonial not found with UUID: " + uuid));

        if (!testimonial.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("Access denied. You do not own this testimonial record.");
        }
        return testimonial;
    }

    @Override
    @Transactional
    public Testimonial createTestimonial(Testimonial testimonial) {
        return testimonialRepository.save(testimonial);
    }

    @Override
    @Transactional
    public Testimonial save(Testimonial testimonial) {
        // This method is used for updates. The ownership check happens before this is called.
        return testimonialRepository.save(testimonial);
    }

    @Override
    @Transactional
    public void deleteTestimonial(UUID uuid, User currentUser) {
        // findTestimonialByUuidAndUser performs both the lookup and the ownership check.
        Testimonial testimonialToDelete = findTestimonialByUuidAndUser(uuid, currentUser);
        testimonialRepository.delete(testimonialToDelete);
    }
}