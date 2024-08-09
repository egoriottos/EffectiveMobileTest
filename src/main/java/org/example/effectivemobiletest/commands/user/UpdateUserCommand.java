package org.example.effectivemobiletest.commands.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateUserCommand {
    private String email;
    private String password;
    private String userName;
}
