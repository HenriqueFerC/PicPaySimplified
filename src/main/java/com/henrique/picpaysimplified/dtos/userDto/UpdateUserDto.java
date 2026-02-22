package com.henrique.picpaysimplified.dtos.userDto;

import com.henrique.picpaysimplified.model.TypeUser;

public record UpdateUserDto(String fullName, String cpfCnpj, String email, String password, TypeUser typeUser) {
}
