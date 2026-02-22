package com.henrique.picpaysimplified.controller;

import com.henrique.picpaysimplified.config.security.JwtUtil;
import com.henrique.picpaysimplified.dtos.authenticationDto.LoginDataDto;
import com.henrique.picpaysimplified.dtos.authenticationDto.TokenJwtDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<TokenJwtDto> login(@RequestBody LoginDataDto loginDto) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginDto.email(), loginDto.password()));
        var token = jwtUtil.generateToken(authentication.getName());
        return ResponseEntity.ok().body(new TokenJwtDto(token));
    }
}
