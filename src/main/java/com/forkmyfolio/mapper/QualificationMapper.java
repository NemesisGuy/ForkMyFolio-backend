package com.forkmyfolio.mapper;

import com.forkmyfolio.dto.create.CreateQualificationRequest;
import com.forkmyfolio.dto.response.QualificationDto;
import com.forkmyfolio.dto.update.UpdateQualificationRequest;
import com.forkmyfolio.model.Qualification;
import com.forkmyfolio.model.User;
import org.springframework.stereotype.Component;

/**
 * Maps between Qualification domain objects and their related DTOs.
 */
@Component
public class QualificationMapper {

    /**
     * Converts a Qualification entity to a QualificationDto for API responses.
     *
     * @param qualification The entity to convert.
     * @return The corresponding DTO.
     */
    public QualificationDto toDto(Qualification qualification) {
        if (qualification == null) {
            return null;
        }
        QualificationDto dto = new QualificationDto();
        dto.setUuid(qualification.getUuid());
        dto.setQualificationName(qualification.getQualificationName());
        dto.setInstitutionName(qualification.getInstitutionName());
        dto.setCompletionYear(qualification.getCompletionYear());
        dto.setGrade(qualification.getGrade());
        return dto;
    }

    /**
     * Converts a CreateQualificationRequest DTO into a new Qualification entity.
     *
     * @param request The DTO with new qualification data.
     * @param owner   The User who will own this qualification.
     * @return A new Qualification entity, ready to be persisted.
     */
    public Qualification toEntity(CreateQualificationRequest request, User owner) {
        if (request == null) {
            return null;
        }
        Qualification qualification = new Qualification();
        qualification.setQualificationName(request.getQualificationName());
        qualification.setInstitutionName(request.getInstitutionName());
        qualification.setCompletionYear(request.getCompletionYear());
        qualification.setGrade(request.getGrade());
        qualification.setUser(owner);
        return qualification;
    }

    /**
     * Converts a QualificationDto from a backup file into a new Qualification entity.
     * This is used by the RestoreService.
     *
     * @param dto   The DTO from the backup file.
     * @param owner The User who will own this new qualification.
     * @return A new Qualification entity, ready to be persisted.
     */
    public Qualification toEntityFromDto(QualificationDto dto, User owner) {
        if (dto == null) {
            return null;
        }
        Qualification qualification = new Qualification();
        // Note: We do not set ID or UUID, allowing the DB to generate them.
        qualification.setQualificationName(dto.getQualificationName());
        qualification.setInstitutionName(dto.getInstitutionName());
        qualification.setCompletionYear(dto.getCompletionYear());
        qualification.setGrade(dto.getGrade());
        qualification.setUser(owner);
        return qualification;
    }
    /**
     * Applies updates from an UpdateQualificationRequest to an existing Qualification entity.
     *
     * @param request       The DTO with the fields to update.
     * @param qualification The existing entity to be updated.
     */
    public void applyUpdateFromRequest(UpdateQualificationRequest request, Qualification qualification) {
        if (request == null || qualification == null) {
            return;
        }

        request.getQualificationName().ifPresent(qualification::setQualificationName);
        request.getInstitutionName().ifPresent(qualification::setInstitutionName);
        request.getCompletionYear().ifPresent(qualification::setCompletionYear);
        request.getGrade().ifPresent(qualification::setGrade);
    }
}