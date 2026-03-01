package com.henrique.picpaysimplified.controller;

import com.henrique.picpaysimplified.dtos.userDto.DetailsUserDto;
import com.henrique.picpaysimplified.dtos.userDto.RegisterUserDto;
import com.henrique.picpaysimplified.dtos.userDto.UpdateUserDto;
import com.henrique.picpaysimplified.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Tag(name = "User Controller", description = "Endpoints for user management")
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Endpoint to register a new user in the system.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User successfully registered.",
                    content = @Content(schema = @Schema(implementation = DetailsUserDto.class), mediaType = "application/json")),
            @ApiResponse(responseCode = "409", description = "Bad request, invalid user data provided."),
            @ApiResponse(responseCode = "500", description = "Internal server error.")
    })
    public ResponseEntity<DetailsUserDto> registerUser(@RequestBody @Valid RegisterUserDto userDto, UriComponentsBuilder uriBuilder) {
        var user = userService.registerUser(userDto);
        var uri = uriBuilder.path("user/{id}").buildAndExpand(user.getId()).toUri();
        return ResponseEntity.created(uri).body(new DetailsUserDto(user));
    }

    @GetMapping("/list")
    @Operation(summary = "List users with pagination", description = "Endpoint to retrieve a paginated list of users.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of users.",
                    content = @Content(schema = @Schema(implementation = DetailsUserDto.class), mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Internal server error.")
    })
    public ResponseEntity<List<DetailsUserDto>> listUsers(Pageable pageable) {
        var list = userService.listUsers(pageable);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/myProfile")
    @Operation(summary = "Get my profile", description = "Endpoint to retrieve the profile information of the authenticated user.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved user profile.",
                    content = @Content(schema = @Schema(implementation = DetailsUserDto.class), mediaType = "application/json")),
            @ApiResponse(responseCode = "401", description = "Unauthorized, user is not authenticated."),
            @ApiResponse(responseCode = "500", description = "Internal server error.")
    })
    public ResponseEntity<DetailsUserDto> myProfile(Authentication authentication) {
        var user = userService.myProfile(authentication);
        return ResponseEntity.ok(new DetailsUserDto(user));
    }

    @PutMapping("/update")
    @Operation(summary = "Update user profile", description = "Endpoint to update the profile information of the authenticated user.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully updated user profile.",
                    content = @Content(schema = @Schema(implementation = DetailsUserDto.class), mediaType = "application/json")),
            @ApiResponse(responseCode = "401", description = "Unauthorized, user is not authenticated."),
            @ApiResponse(responseCode = "409", description = "Bad request, invalid user data provided."),
            @ApiResponse(responseCode = "500", description = "Internal server error.")
    })
    public ResponseEntity<DetailsUserDto> updateProfile(Authentication authentication, @RequestBody @Valid UpdateUserDto userDto) {
        var user = userService.updateProfile(authentication, userDto);
        return ResponseEntity.ok(new DetailsUserDto(user));
    }

}
