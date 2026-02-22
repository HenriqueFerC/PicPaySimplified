package com.henrique.picpaysimplified.service;

import com.henrique.picpaysimplified.dtos.userDto.DetailsUserDto;
import com.henrique.picpaysimplified.dtos.userDto.RegisterUserDto;
import com.henrique.picpaysimplified.dtos.userDto.UpdateUserDto;
import com.henrique.picpaysimplified.model.User;
import com.henrique.picpaysimplified.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import javax.security.auth.login.CredentialException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User registerUser(RegisterUserDto userDto) {
        existsByEmail(userDto.email());
        existsByCpfCnpj(userDto.cpfCnpj());
        var user = new User(userDto);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    @Transactional
    public User updateProfile(Authentication authentication, UpdateUserDto userDto) {
        existsByEmail(userDto.email());
        existsByCpfCnpj(userDto.cpfCnpj());
        var user = findUserByEmail(authentication);
        user.updateUser(userDto.fullName() != null ? userDto.fullName() : user.getFullName(),
                userDto.cpfCnpj() != null ? userDto.cpfCnpj() : user.getCpfCnpj(),
                userDto.email() != null ? userDto.email() : user.getEmail(),
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

    public User findUserByEmail(Authentication authentication) {
        try {
            var user = userAuthenticated(authentication);
            return userRepository.findByEmail(user.getUsername()).orElseThrow(
                    () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")
            );
        } catch (SecurityException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated.", e.getCause());
        }
    }

    public org.springframework.security.core.userdetails.User userAuthenticated(Authentication authentication) {
        var authenticated = (org.springframework.security.core.userdetails.User) authentication.getPrincipal();
        if (authenticated == null) {
            throw new SecurityException("User not authenticated.");
        }
        return authenticated;
    }

    public void existsByEmail(String email) {
        try {
            boolean exists = userRepository.existsByEmail(email);
            if (exists) {
                throw new CredentialException("Email already exists " + email);
            }
        } catch (CredentialException e) {
            throw new RuntimeException("Email already exists " + e.getCause());
        }
    }

    public void existsByCpfCnpj(String cpfCnpj) {
        try {
            boolean exists = userRepository.existsByCpfCnpj(cpfCnpj);
            if (exists) {
                throw new CredentialException("Cpf or Cnpj already exists " + cpfCnpj);
            }
        } catch (CredentialException e) {
            throw new RuntimeException("Cpf or Cnpj already exists " + e.getCause());
        }
    }

}
