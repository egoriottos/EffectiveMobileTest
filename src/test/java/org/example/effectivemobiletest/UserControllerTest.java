package org.example.effectivemobiletest;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.effectivemobiletest.commands.user.UserRequest;
import org.example.effectivemobiletest.commands.user.authResponse.AuthenticationResponse;
import org.example.effectivemobiletest.controller.UserController;
import org.example.effectivemobiletest.commands.user.CreateUserCommand;
import org.example.effectivemobiletest.domain.entity.User;
import org.example.effectivemobiletest.domain.enums.Role;
import org.example.effectivemobiletest.repository.UserRepository;
import org.example.effectivemobiletest.services.JwtService;
import org.example.effectivemobiletest.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

@WebMvcTest(UserController.class)
public class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserController userController;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void deleteUser()throws Exception{
        User user = User.builder().id(1L).email("test@email.com").password("123").userName("egor").build();
        Mockito.when(userRepository.findById(Mockito.any())).thenReturn(Optional.of(user));

        mockMvc.perform(
                        delete("/api/v1/users/delete/user/1")
                                .with(csrf())
                                .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk());
    }

}

