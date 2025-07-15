package com.forkmyfolio.mapper;

import com.forkmyfolio.dto.create.CreateQualificationRequest;
import com.forkmyfolio.dto.response.QualificationDto;
import com.forkmyfolio.dto.update.UpdateQualificationRequest;
import com.forkmyfolio.model.Qualification;
import com.forkmyfolio.model.User;
import org.springframework.stereotype.Component;

@Component
public class QualificationMapper {

    /**
     * Converts a Qualification entity to a QualificationDto.
     */
    public QualificationDto toDto(Qualification qualification) {
        if (qualification == null) return null;

        QualificationDto dto = new QualificationDto();
        dto.setUuid(qualification.getUuid());
        dto.setQualificationName(qualification.getQualificationName());
        dto.setInstitutionName(qualification.getInstitutionName());
        dto.setCompletionYear(qualification.getCompletionYear());
        dto.setGrade(qualification.getGrade());
        return dto;
    }

    /**
     * Converts a CreateQualificationRequest DTO to a new Qualification entity.
     */
    public Qualification toEntity(CreateQualificationRequest request, User owner) {
        if (request == null) return null;

        Qualification qualification = new Qualification();
        qualification.setQualificationName(request.getQualificationName());
        qualification.setInstitutionName(request.getInstitutionName());
        qualification.setCompletionYear(request.getCompletionYear());
        qualification.setGrade(request.getGrade());
        qualification.setUser(owner);
        return qualification;
    }

    /**
     * Applies updates from an UpdateQualificationRequest DTO to an existing Qualification entity.
     */
    public void applyUpdateFromRequest(UpdateQualificationRequest request, Qualification qualification) {
        if (request == null || qualification == null) return;

        request.getQualificationName().ifPresent(qualification::setQualificationName);
        request.getInstitutionName().ifPresent(qualification::setInstitutionName);
        request.getCompletionYear().ifPresent(qualification::setCompletionYear);
        request.getGrade().ifPresent(qualification::setGrade);
    }
}
