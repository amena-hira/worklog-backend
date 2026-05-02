package com.example.worklog.api.mapper;

import org.mapstruct.Mapper;
import com.example.worklog.api.dto.UserDTO;
import com.example.worklog.infrastructure.persistence.entity.UserEntity;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDTO toDTO(UserEntity userEntity);
    UserEntity toEntity(UserDTO userDTO);
}
