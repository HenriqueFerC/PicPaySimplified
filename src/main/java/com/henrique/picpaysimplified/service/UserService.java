package com.henrique.picpaysimplified.service;

import com.henrique.picpaysimplified.dtos.userDto.DetailsUserDto;
import com.henrique.picpaysimplified.dtos.userDto.RegisterUserDto;
import com.henrique.picpaysimplified.dtos.userDto.UpdateUserDto;
import com.henrique.picpaysimplified.exceptions.ConflictException;
import com.henrique.picpaysimplified.exceptions.ResourceNotFoundException;
import com.henrique.picpaysimplified.model.User;
import com.henrique.picpaysimplified.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User registerUser(RegisterUserDto userDto) {
        validateEmailDoesNotExists(userDto.email());
        validateCpfOrCnpjDoesNotExists(userDto.cpfCnpj());
        var user = new User(userDto);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    @Transactional
    public User updateProfile(String email, UpdateUserDto userDto) {
        var user = findUserByEmail(email);
        if (!userDto.cpfCnpj().equals(user.getCpfCnpj())) {
            validateCpfOrCnpjDoesNotExists(userDto.cpfCnpj());
        }

        if (!userDto.email().equals(user.getEmail())) {
            validateEmailDoesNotExists(userDto.email());
        }

        user.updateUser(userDto.fullName() != null ? userDto.fullName() : user.getFullName(),
                userDto.cpfCnpj(),
                userDto.email(),
                userDto.password() != null ? passwordEncoder.encode(userDto.password()) : user.getPassword(),
                userDto.userType() != null ? userDto.userType() : user.getUserType());
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public List<DetailsUserDto> listUsers(Pageable pageable) {
        return userRepository.findAll(pageable).stream().map(DetailsUserDto::new).toList();
    }

    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(
                () -> new ResourceNotFoundException("User not found " + email)
        );
    }

    public User findUserByCpfCnpj(String cpfCnpj) {
        return userRepository.findByCpfCnpj(cpfCnpj).orElseThrow(
                () -> new ResourceNotFoundException("User not found " + cpfCnpj)
        );
    }

    private void validateEmailDoesNotExists(String email) {
        boolean exists = userRepository.existsByEmail(email);
        if (exists) {
            throw new ConflictException("Email already exists " + email);
        }
    }

    private void validateCpfOrCnpjDoesNotExists(String cpfCnpj) {
        boolean exists = userRepository.existsByCpfCnpj(cpfCnpj);
        if (exists) {
            throw new ConflictException("Cpf or Cnpj already exists " + cpfCnpj);
        }
    }

}
