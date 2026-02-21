package com.henrique.picpaysimplified.dtos.userDto;

import com.henrique.picpaysimplified.model.TypeUser;
import com.henrique.picpaysimplified.model.User;

public record DetailsUserDto(Integer id, String cpfCnpj, String email, String password, TypeUser typeUser) {
    public DetailsUserDto(User user) {
        this(user.getId(), user.getCpfCnpj(), user.getEmail(), user.getPassword(), user.getTypeUser());
    }
}
