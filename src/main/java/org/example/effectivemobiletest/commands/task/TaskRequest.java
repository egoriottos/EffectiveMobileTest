package org.example.effectivemobiletest.commands.task;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.effectivemobiletest.domain.entity.User;
import org.example.effectivemobiletest.domain.enums.TaskPriority;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskRequest {
    private String title;
    private String description;
    private String comments;
    private TaskPriority priority;
    private User performer;
}
