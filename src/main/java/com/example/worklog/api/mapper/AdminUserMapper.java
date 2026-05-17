package com.example.worklog.api.mapper;

import com.example.worklog.api.dto.AdminUserDTO;
import com.example.worklog.infrastructure.persistence.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AdminUserMapper {
    AdminUserDTO toDTO(UserEntity userEntity);
}
