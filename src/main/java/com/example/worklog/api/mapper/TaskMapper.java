package com.example.worklog.api.mapper;

import com.example.worklog.api.dto.TaskDTO;
import com.example.worklog.infrastructure.persistence.entity.TaskEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

/**
 * MapStruct mapper for converting between TaskEntity and TaskDTO.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = {TaskUserMapper.class})
public interface TaskMapper {
    
    @Mapping(target = "projectId", source = "project.id")
    @Mapping(target = "projectName", source = "project.name") // Extracts the name from the nested project entity
    @Mapping(target = "projectColor", source = "project.color")
    @Mapping(target = "createdByUserEmail", source = "createdBy.email")
    TaskDTO toDTO(TaskEntity taskEntity);

    @Mapping(target = "project", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "assignees", ignore = true)
    TaskEntity toEntity(TaskDTO taskDTO);
}
