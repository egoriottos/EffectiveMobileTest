package org.example.effectivemobiletest.controller;

import lombok.RequiredArgsConstructor;
import org.example.effectivemobiletest.commands.task.*;
import org.example.effectivemobiletest.domain.entity.Task;
import org.example.effectivemobiletest.domain.enums.TaskStatus;
import org.example.effectivemobiletest.services.TaskService;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.List;

@RestController
@RequestMapping("api/v1/task")
@RequiredArgsConstructor
public class TaskController {
    private final TaskService taskService;
    private final ModelMapper modelMapper;
    //todo
    //создание Таска
    @PostMapping("/create")
    public ResponseEntity<TaskResponse> createTask(@RequestBody TaskRequest command) {
        return ResponseEntity.ok(taskService.createTask(modelMapper.map(command, CreateTaskCommand.class)));
    }

    //получение всех Тасков
    @GetMapping("/all")
    public ResponseEntity<Page<TaskResponse>> getAllTasks(@RequestParam(defaultValue = "0") Integer page,@RequestParam(defaultValue = "10") Integer size) {
        return ResponseEntity.ok(taskService.getTasks(page, size));
    }

    //получение Тасков по автору
    @GetMapping("/author/{authorName}")
    public ResponseEntity<List<Task>> getTasksByAuthor(@PathVariable String authorName,@RequestParam(defaultValue = "0") Integer page,@RequestParam(defaultValue = "10") Integer size) {
        return ResponseEntity.ok(taskService.getTaskByAuthor(authorName, page, size));
    }
    //todo
    //получение Тасков по исполнителю
    @GetMapping("/performer/{performerName}")
    public ResponseEntity<List<Task>> getTasksByPerformer(@PathVariable String performerName,@RequestParam(defaultValue = "0") Integer page, @RequestParam(defaultValue = "10") Integer size) {
        return ResponseEntity.ok(taskService.getTaskByPerformer(performerName, page, size));
    }
    //todo
    //обновление Таска
    @PutMapping("/update/{id}")
    public ResponseEntity<String> updateTask(@PathVariable Long id, @RequestBody UpdateTaskCommand command) {
        try {
            taskService.updateTask(id, command);
            return ResponseEntity.ok("Task updated successfully " + id);
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(403).body("Access denied you are not an author");
        }
    }
    //todo
    //удаление Таска
    @DeleteMapping("delete/task/{id}")
    public ResponseEntity<String> deleteTask(@PathVariable Long id) {
        try {
            taskService.deleteTask(id);
            return ResponseEntity.ok("Task deleted successfully " + id);
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(403).body("Access denied: you are not an author");
        }
    }
    //todo
    //добавление комментарие
    @PostMapping("/add/comment/{taskId}")
    public ResponseEntity<String> addComment(@PathVariable Long taskId, @RequestBody List<String> comments) {
        try {
            taskService.addComment(taskId, comments);
            return ResponseEntity.ok("Comment was added successfully " + comments + " " + taskId);
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(403).body("Access denied: you are not author or performer");
        }
    }
    //todo
    //добавление исполнителя
    @PostMapping("/add/performer/{taskId}")
    public ResponseEntity<String> addPerformer(@PathVariable Long taskId,@RequestBody Request performer) {
        try {
            taskService.addPerformer(taskId, performer.getPerformer());
            return ResponseEntity.ok("Performer added successfully " + performer.getPerformer() + " " + "taskId= " + taskId);
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(403).body("Access denied: you are not an author");
        }
    }
    //todo
    @PutMapping("/change/status/{taskId}")
    public ResponseEntity<String> changeStatus(@PathVariable Long taskId, @RequestBody TaskStatus status) {
        try {
            taskService.changeTaskStatus(taskId, status);
            return ResponseEntity.ok("Task status changed successfully " + status + " in task with id " + taskId);
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(403).body("Access denied: you are not an performer");
        }
    }
    //todo
    @PostMapping("/create/byAdmin")
    public ResponseEntity<TaskResponse> createTaskByAdmin(@RequestBody CreateTaskCommand command) {
        return ResponseEntity.ok(taskService.createTaskByAdmin(command));
    }
}
