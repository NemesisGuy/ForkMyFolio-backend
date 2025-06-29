package com.forkmyfolio.service.impl;

import com.forkmyfolio.exception.ResourceNotFoundException;
import com.forkmyfolio.model.Testimonial;
import com.forkmyfolio.model.User;
import com.forkmyfolio.repository.TestimonialRepository;
import com.forkmyfolio.repository.UserRepository;
import com.forkmyfolio.service.TestimonialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class TestimonialServiceImpl implements TestimonialService {

    private final TestimonialRepository testimonialRepository;
    private final UserRepository userRepository;

    @Autowired
    public TestimonialServiceImpl(TestimonialRepository testimonialRepository, UserRepository userRepository) {
        this.testimonialRepository = testimonialRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Testimonial> getPublicTestimonials() {
        User owner = userRepository.findFirstByOrderByIdAsc()
                .orElseThrow(() -> new IllegalStateException("Portfolio owner user not found."));
        return testimonialRepository.findByUserOrderByCreatedAtDesc(owner);
    }

    @Override
    @Transactional(readOnly = true)
    public Testimonial getTestimonialByUuid(UUID uuid) {
        return testimonialRepository.findByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Testimonial not found with UUID: " + uuid));
    }

    @Override
    @Transactional
    public Testimonial createTestimonial(Testimonial testimonial) {
        return testimonialRepository.save(testimonial);
    }

    @Override
    @Transactional
    public Testimonial save(Testimonial testimonial) {
        return testimonialRepository.save(testimonial);
    }

    @Override
    @Transactional
    public void deleteTestimonial(UUID uuid, User currentUser) {
        Testimonial testimonialToDelete = getTestimonialByUuid(uuid);
        if (!testimonialToDelete.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("User does not have permission to delete this testimonial.");
        }
        testimonialRepository.delete(testimonialToDelete);
    }
}