package com.example.worklog.api.mapper;

import com.example.worklog.api.dto.TaskUserDTO;
import com.example.worklog.infrastructure.persistence.entity.TaskUserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TaskUserMapper {
    
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userName", expression = "java(entity.getUser().getFirst_name() + ' ' + entity.getUser().getLast_name())")
    @Mapping(target = "userEmail", source = "user.email") 
    TaskUserDTO toDTO(TaskUserEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "task", ignore = true)
    @Mapping(target = "user", ignore = true)
    TaskUserEntity toEntity(TaskUserDTO dto);
}
