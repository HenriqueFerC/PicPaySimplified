package com.henrique.picpaysimplified.dtos.userDto;

public record DetailsUserDto(String cpfCnpj, String email, String password, String typeUser) {
    public DetailsUserDto(com.henrique.picpaysimplified.model.User user) {
        this(user.getCpfCnpj(), user.getEmail(), user.getPassword(), user.getTypeUser().name());
    }
}
