package com.forkmyfolio.service.impl;

import com.forkmyfolio.exception.ResourceNotFoundException;
import com.forkmyfolio.model.Qualification;
import com.forkmyfolio.model.User;
import com.forkmyfolio.model.enums.QualificationLevel;
import com.forkmyfolio.repository.QualificationRepository;
import com.forkmyfolio.service.QualificationService;
import com.forkmyfolio.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Implementation of the {@link QualificationService} interface.
 * This class contains the business logic for managing qualifications.
 * It interacts with the {@link QualificationRepository} and {@link UserService}
 * to perform its operations, adhering to the application's architectural rules.
 */
@Service
@RequiredArgsConstructor
public class QualificationServiceImpl implements QualificationService {

    private final QualificationRepository qualificationRepository;
    private final UserService userService;

    @Override
    @Transactional(readOnly = true)
    public List<Qualification> getQualificationsForCurrentUser() {
        User currentUser = userService.getCurrentAuthenticatedUser();
        return qualificationRepository.findByUser(currentUser);
    }

    @Override
    @Transactional(readOnly = true)
    public Qualification getQualificationByUuidForCurrentUser(UUID qualificationUuid) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        return qualificationRepository.findByUuidAndUser(qualificationUuid, currentUser)
                .orElseThrow(() -> new ResourceNotFoundException("Qualification not found with UUID: " + qualificationUuid));
    }

    @Override
    @Transactional
    public Qualification createQualificationForCurrentUser(String qualificationName, String institutionName, String institutionLogoUrl, String institutionWebsite, String fieldOfStudy, QualificationLevel level, Integer startYear, Integer completionYear, Boolean stillStudying, String grade, String credentialUrl, boolean visible) {
        User currentUser = userService.getCurrentAuthenticatedUser();

        Qualification qualification = new Qualification();
        qualification.setUser(currentUser);
        qualification.setQualificationName(qualificationName);
        qualification.setInstitutionName(institutionName);
        qualification.setInstitutionLogoUrl(institutionLogoUrl);
        qualification.setInstitutionWebsite(institutionWebsite);
        qualification.setFieldOfStudy(fieldOfStudy);
        qualification.setLevel(level);
        qualification.setStartYear(startYear);
        qualification.setCompletionYear(completionYear);
        qualification.setStillStudying(stillStudying);
        qualification.setGrade(grade);
        qualification.setCredentialUrl(credentialUrl);
        qualification.setVisible(visible);

        return qualificationRepository.save(qualification);
    }

    @Override
    @Transactional
    public Qualification updateQualificationForCurrentUser(UUID qualificationUuid, String qualificationName, String institutionName, String institutionLogoUrl, String institutionWebsite, String fieldOfStudy, QualificationLevel level, Integer startYear, Integer completionYear, Boolean stillStudying, String grade, String credentialUrl, Boolean visible) {
        Qualification qualificationToUpdate = getQualificationByUuidForCurrentUser(qualificationUuid);

        qualificationToUpdate.setQualificationName(qualificationName);
        qualificationToUpdate.setInstitutionName(institutionName);
        qualificationToUpdate.setInstitutionLogoUrl(institutionLogoUrl);
        qualificationToUpdate.setInstitutionWebsite(institutionWebsite);
        qualificationToUpdate.setFieldOfStudy(fieldOfStudy);
        qualificationToUpdate.setLevel(level);
        qualificationToUpdate.setStartYear(startYear);
        qualificationToUpdate.setCompletionYear(completionYear);
        qualificationToUpdate.setStillStudying(stillStudying);
        qualificationToUpdate.setGrade(grade);
        qualificationToUpdate.setCredentialUrl(credentialUrl);
        qualificationToUpdate.setVisible(visible);

        return qualificationRepository.save(qualificationToUpdate);
    }

    @Override
    @Transactional
    public void deleteQualificationForCurrentUser(UUID qualificationUuid) {
        Qualification qualificationToDelete = getQualificationByUuidForCurrentUser(qualificationUuid);
        qualificationRepository.delete(qualificationToDelete);
    }
}