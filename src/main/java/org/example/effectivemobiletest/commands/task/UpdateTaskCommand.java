package org.example.effectivemobiletest.commands.task;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.effectivemobiletest.domain.entity.User;
import org.example.effectivemobiletest.domain.enums.TaskPriority;
import org.example.effectivemobiletest.domain.enums.TaskStatus;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateTaskCommand {
    private String title;
    private String description;
    private List<String> comments;
    private TaskStatus status;
    private TaskPriority priority;
    private User performer;
}
