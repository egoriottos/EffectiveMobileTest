package org.example.effectivemobiletest.commands.task;

import lombok.Data;
import org.example.effectivemobiletest.domain.entity.User;
import org.example.effectivemobiletest.domain.enums.TaskPriority;
import org.example.effectivemobiletest.domain.enums.TaskStatus;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class TaskResponse {
    private String title;
    private String description;
    private List<String> comments;
    private TaskStatus status;
    private TaskPriority priority;
    private User author;
    private User performer;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
