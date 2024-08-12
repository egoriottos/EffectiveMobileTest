package org.example.effectivemobiletest.services;

import lombok.RequiredArgsConstructor;
import org.example.effectivemobiletest.commands.user.CreateUserCommand;
import org.example.effectivemobiletest.commands.user.authRequest.AuthenticationRequest;
import org.example.effectivemobiletest.commands.user.authResponse.AuthenticationResponse;
import org.example.effectivemobiletest.domain.entity.User;
import org.example.effectivemobiletest.domain.enums.Role;
import org.example.effectivemobiletest.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthenticationResponse createUser(CreateUserCommand createUserCommand) {
        var user = User.builder()
                .userName(createUserCommand.getUserName())
                .email(createUserCommand.getEmail())
                .password(passwordEncoder.encode(createUserCommand.getPassword()))
                .role(Role.USER)
                .createdAt(LocalDateTime.now())
                .build();
        userRepository.save(user);
        var jwtToken = jwtService.generateToken(user);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }

    public AuthenticationResponse createAdmin(CreateUserCommand createUserCommand) {
        var user = User.builder()
                .userName(createUserCommand.getUserName())
                .email(createUserCommand.getEmail())
                .password(passwordEncoder.encode(createUserCommand.getPassword()))
                .role(Role.ADMIN)
                .createdAt(LocalDateTime.now())
                .build();
        userRepository.save(user);
        var jwtToken = jwtService.generateToken(user);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest authenticationRequest) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authenticationRequest.getEmail(), authenticationRequest.getPassword()));
        var user = userRepository.findByEmail(authenticationRequest.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("Email not found " + authenticationRequest.getEmail()));
        var token = jwtService.generateToken(user);
        return AuthenticationResponse.builder()
                .token(token)
                .build();
    }

    @Transactional
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}
