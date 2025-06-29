package com.forkmyfolio.service.impl;

import com.forkmyfolio.exception.ResourceNotFoundException;
import com.forkmyfolio.model.Qualification;
import com.forkmyfolio.model.User;
import com.forkmyfolio.repository.QualificationRepository;
import com.forkmyfolio.repository.UserRepository;
import com.forkmyfolio.service.QualificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class QualificationServiceImpl implements QualificationService {

    private final QualificationRepository qualificationRepository;
    private final UserRepository userRepository;

    @Autowired
    public QualificationServiceImpl(QualificationRepository qualificationRepository, UserRepository userRepository) {
        this.qualificationRepository = qualificationRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Qualification> getPublicQualifications() {
        User owner = userRepository.findFirstByOrderByIdAsc()
                .orElseThrow(() -> new IllegalStateException("Portfolio owner user not found in the database."));
        return qualificationRepository.findByUserOrderByCompletionYearDesc(owner);
    }

    @Override
    @Transactional(readOnly = true)
    public Qualification getQualificationByUuid(UUID uuid) {
        return qualificationRepository.findByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Qualification not found with UUID: " + uuid));
    }

    @Override
    @Transactional
    public Qualification createQualification(Qualification qualification) {
        return qualificationRepository.save(qualification);
    }

    // REMOVED the old updateQualification method.
    // ADDED a simple save method for the controller to use.
    @Override
    @Transactional
    public Qualification save(Qualification qualification) {
        // The controller is now responsible for fetching the entity,
        // applying updates, and then passing the modified entity here to be saved.
        return qualificationRepository.save(qualification);
    }

    @Override
    @Transactional
    public void deleteQualification(UUID uuid, User currentUser) {
        Qualification qualificationToDelete = getQualificationByUuid(uuid);
        if (!qualificationToDelete.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("User does not have permission to delete this qualification.");
        }
        qualificationRepository.delete(qualificationToDelete);
    }
}