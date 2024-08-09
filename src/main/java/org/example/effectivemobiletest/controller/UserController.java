package org.example.effectivemobiletest.controller;

import lombok.RequiredArgsConstructor;
import org.example.effectivemobiletest.commands.user.CreateUserCommand;
import org.example.effectivemobiletest.commands.user.UserRequest;
import org.example.effectivemobiletest.commands.user.authRequest.AuthenticationRequest;
import org.example.effectivemobiletest.commands.user.authResponse.AuthenticationResponse;
import org.example.effectivemobiletest.services.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final ModelMapper mapper;

    //контроллер на создание пользователя и выдачи токена
    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> createUser(@RequestBody UserRequest command) {
        return ResponseEntity.ok(userService.createUser(mapper.map(command, CreateUserCommand.class)));
    }

    //аунтефикация
    @PostMapping("/auth")
    public ResponseEntity<AuthenticationResponse> auth(@RequestBody AuthenticationRequest authenticationRequest) {
        return ResponseEntity.ok(userService.authenticate(authenticationRequest));
    }

    //удаление
    @DeleteMapping("/delete/user/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok("Deleted user with id " + id);
    }

    //создание админа
    @PostMapping("/admin")
    public ResponseEntity<AuthenticationResponse> admin(@RequestBody  UserRequest command) {
        return ResponseEntity.ok(userService.createAdmin(mapper.map(command, CreateUserCommand.class)));
    }
}
