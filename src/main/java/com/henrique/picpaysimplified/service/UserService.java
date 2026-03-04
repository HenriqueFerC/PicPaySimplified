package com.henrique.picpaysimplified.service;

import com.henrique.picpaysimplified.dtos.userDto.DetailsUserDto;
import com.henrique.picpaysimplified.dtos.userDto.RegisterUserDto;
import com.henrique.picpaysimplified.dtos.userDto.UpdateUserDto;
import com.henrique.picpaysimplified.exceptions.ConflictException;
import com.henrique.picpaysimplified.exceptions.ResourceNotFoundException;
import com.henrique.picpaysimplified.exceptions.UnauthorizedException;
import com.henrique.picpaysimplified.model.User;
import com.henrique.picpaysimplified.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
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
    public User updateProfile(Authentication authentication, UpdateUserDto userDto) {
        var user = findUserByEmail(authentication);
        if (!userDto.cpfCnpj().equals(user.getCpfCnpj())) {
            validateCpfOrCnpjDoesNotExists(userDto.cpfCnpj());
        }

        if(!userDto.email().equals(user.getEmail())) {
            validateEmailDoesNotExists(userDto.email());
        }

        user.updateUser(userDto.fullName() != null ? userDto.fullName() : user.getFullName(),
                userDto.cpfCnpj(),
                userDto.email(),
                userDto.password() != null ? passwordEncoder.encode(userDto.password()) : user.getPassword(),
                userDto.typeUser() != null ? userDto.typeUser() : user.getTypeUser());
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public List<DetailsUserDto> listUsers(Pageable pageable) {
        return userRepository.findAll(pageable).stream().map(DetailsUserDto::new).toList();
    }

    @Transactional(readOnly = true)
    public User myProfile(Authentication authentication) {
        return findUserByEmail(authentication);
    }

    private User findUserByEmail(Authentication authentication) {
        var user = userAuthenticated(authentication);
        return userRepository.findByEmail(user.getUsername()).orElseThrow(
                () -> new ResourceNotFoundException("User not found " + user.getUsername())
        );
    }

    private org.springframework.security.core.userdetails.User userAuthenticated(Authentication authentication) {
        var authenticated = (org.springframework.security.core.userdetails.User) authentication.getPrincipal();
        if (authenticated == null) {
            throw new UnauthorizedException("User not authenticated.");
        }
        return authenticated;
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
