package com.henrique.picpaysimplified.dtos.userDto;

import com.henrique.picpaysimplified.model.UserType;
import com.henrique.picpaysimplified.model.User;

public record DetailsUserDto(Integer id, String fullName, String cpfCnpj, String email, UserType userType) {
    public DetailsUserDto(User user) {
        this(user.getId(), user.getFullName(), user.getCpfCnpj(), user.getEmail(), user.getUserType());
    }
}
