package org.example.effectivemobiletest.commands.user;

import lombok.Data;
import org.example.effectivemobiletest.domain.enums.Role;

@Data
public class CreateUserCommand {
    private String email;
    private String password;
    private String userName;
    private Role role;
}
