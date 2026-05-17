package com.example.worklog.api.mapper;

import com.example.worklog.api.dto.ProjectDTO;
import com.example.worklog.infrastructure.persistence.entity.ProjectEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

/**
 * MapStruct mapper for converting between ProjectEntity (Database) and ProjectDTO (API).
 * Spring will automatically generate the implementation class at compile time.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = {ProjectUserMapper.class})
public interface ProjectMapper {
    
    /**
     * Converts a Project database entity into a DTO for the API response.
     * Extracts just the email from the nested 'createdBy' UserEntity.
     */
    @Mapping(target = "createdByUserEmail", source = "createdBy.email")
    ProjectDTO toDTO(ProjectEntity projectEntity);

    /**
     * Converts a DTO from an API request into a Project database entity.
     * We ignore complex relationships (createdBy, tasks, members) here because 
     * they usually require database lookups in the Service layer to attach properly.
     */
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "tasks", ignore = true)
    @Mapping(target = "members", ignore = true)
    ProjectEntity toEntity(ProjectDTO projectDTO);
}
