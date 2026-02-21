package com.henrique.picpaysimplified.service;

import com.henrique.picpaysimplified.dtos.userDto.DetailsUserDto;
import com.henrique.picpaysimplified.dtos.userDto.RegisterUserDto;
import com.henrique.picpaysimplified.model.User;
import com.henrique.picpaysimplified.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional(readOnly = true)
    public List<DetailsUserDto> listUsers(Pageable pageable) {
        return userRepository.findAll(pageable).stream().map(DetailsUserDto::new).toList();
    }

    public void existsByEmail(String email) {
        try {
            boolean exists = userRepository.existsByEmail(email);
            if(exists) {
                throw new CredentialException("Email already exists " + email);
            }
        } catch (CredentialException e) {
            throw new RuntimeException("Email already exists " + e.getCause());
        }
    }

    public void existsByCpfCnpj(String cpfCnpj) {
        try {
            boolean exists = userRepository.existsByCpfCnpj(cpfCnpj);
            if(exists) {
                throw new CredentialException("Cpf or Cnpj already exists " + cpfCnpj);
            }
        } catch (CredentialException e) {
            throw new RuntimeException("Cpf or Cnpj already exists " + e.getCause());
        }
    }

}
