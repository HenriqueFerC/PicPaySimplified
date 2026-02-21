package com.henrique.picpaysimplified.controller;

import com.henrique.picpaysimplified.dtos.userDto.DetailsUserDto;
import com.henrique.picpaysimplified.dtos.userDto.RegisterUserDto;
import com.henrique.picpaysimplified.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<DetailsUserDto> registerUser(@RequestBody RegisterUserDto userDto, UriComponentsBuilder uriBuilder) {
        var user = userService.registerUser(userDto);
        var uri = uriBuilder.path("user/{id}").buildAndExpand(user.getId()).toUri();
        return ResponseEntity.created(uri).body(new DetailsUserDto(user));
    }

    @GetMapping("/list")
    public ResponseEntity<List<DetailsUserDto>> listUsers(Pageable pageable) {
        var list = userService.listUsers(pageable);
        return ResponseEntity.ok(list);
    }
}
