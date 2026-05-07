package com.example.worklog.api.mapper;

import com.example.worklog.api.dto.ProjectUserDTO;
import com.example.worklog.infrastructure.persistence.entity.ProjectUserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ProjectUserMapper {
    
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userName", expression = "java(entity.getUser().getFirst_name() + ' ' + entity.getUser().getLast_name())")
    @Mapping(target = "userEmail", source = "user.email")
    ProjectUserDTO toDTO(ProjectUserEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "project", ignore = true)
    @Mapping(target = "user", ignore = true)
    ProjectUserEntity toEntity(ProjectUserDTO dto);
}
