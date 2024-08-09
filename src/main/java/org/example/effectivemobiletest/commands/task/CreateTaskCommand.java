package org.example.effectivemobiletest.commands.task;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.effectivemobiletest.domain.entity.User;
import org.example.effectivemobiletest.domain.enums.TaskPriority;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateTaskCommand {
    private String title;
    private String description;
    private String comments;
    private TaskPriority priority;
    private User author;
    private String performerUserName;
}
