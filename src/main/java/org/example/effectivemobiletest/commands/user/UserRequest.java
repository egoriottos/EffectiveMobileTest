package org.example.effectivemobiletest.commands.user;

import lombok.Data;

@Data
public class UserRequest {
    private String email;
    private String password;
    private String userName;
}
