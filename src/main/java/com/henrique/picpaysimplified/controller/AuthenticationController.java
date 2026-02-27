package com.henrique.picpaysimplified.controller;

import com.henrique.picpaysimplified.config.security.JwtUtil;
import com.henrique.picpaysimplified.dtos.authenticationDto.LoginDataDto;
import com.henrique.picpaysimplified.dtos.authenticationDto.TokenJwtDto;
import com.henrique.picpaysimplified.exceptions.ConflictException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints for user authentication and JWT token generation.")
public class AuthenticationController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    @Operation(summary = "User Login", description = "Authenticate user and generate JWT token.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successful authentication, returns JWT token.",
                    content = @Content(schema = @Schema(implementation = TokenJwtDto.class), mediaType = "application/json")),
            @ApiResponse(responseCode = "409", description = "Invalid email or password."),
            @ApiResponse(responseCode = "500", description = "Internal server error.")
    })
    public ResponseEntity<TokenJwtDto> login(@RequestBody LoginDataDto loginDto) {
        try {
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginDto.email(), loginDto.password()));
            var token = jwtUtil.generateToken(authentication.getName());
            return ResponseEntity.ok().body(new TokenJwtDto(token));
        } catch (Exception e) {
            throw new ConflictException("Invalid email or password.", e.getCause());
        }
    }
}
