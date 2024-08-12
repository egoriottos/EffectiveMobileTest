package org.example.effectivemobiletest.commands.task;

import lombok.Data;
import org.example.effectivemobiletest.domain.enums.TaskStatus;

@Data
public class TaskStatusUpdateRequest {
    private TaskStatus status;
}
