package org.example.effectivemobiletest.commands.task;

import lombok.Data;
import org.example.effectivemobiletest.domain.entity.User;

@Data
public class Request {
    User performer;
}
