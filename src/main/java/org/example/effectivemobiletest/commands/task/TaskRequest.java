package org.example.effectivemobiletest.commands.task;

import lombok.Data;
import org.example.effectivemobiletest.domain.enums.TaskPriority;

@Data
public class TaskRequest {
    private String title;
    private String description;
    private String comments;
    private TaskPriority priority;
    private String performer;
}
