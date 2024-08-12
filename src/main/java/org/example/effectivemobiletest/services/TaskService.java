package org.example.effectivemobiletest.services;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.example.effectivemobiletest.commands.task.CreateTaskCommand;
import org.example.effectivemobiletest.commands.task.TaskResponse;
import org.example.effectivemobiletest.commands.task.UpdateTaskCommand;
import org.example.effectivemobiletest.domain.entity.Task;
import org.example.effectivemobiletest.domain.entity.User;
import org.example.effectivemobiletest.domain.enums.TaskStatus;
import org.example.effectivemobiletest.repository.TaskRepository;
import org.example.effectivemobiletest.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    public TaskResponse createTask(CreateTaskCommand createTaskCommand) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username;
        if (principal instanceof UserDetails) {
            username = ((UserDetails) principal).getUsername();
        } else {
            username = principal.toString();
        }
        User currentUser = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found " + username));
        String performEmail = createTaskCommand.getPerformerUserName();
        User performer = null;
        if (performEmail != null && !performEmail.isEmpty()) {
            Optional<User> performerOptional = userRepository.findByEmail(performEmail);
            if (performerOptional.isPresent()) {
                performer = performerOptional.get();
                System.out.println("Found performer: " + performer);
            } else {
                System.out.println("Performer not found with email: " + performEmail);
            }
        }
        var task = Task.builder()
                .title(createTaskCommand.getTitle())
                .description(createTaskCommand.getDescription())
                .author(currentUser)
                .status(TaskStatus.PENDING)
                .priority(createTaskCommand.getPriority())
                .performer(performer)
                .comments(Collections.singletonList(createTaskCommand.getComments()))
                .createdAt(LocalDateTime.now())
                .build();
        taskRepository.save(task);
        return modelMapper.map(task, TaskResponse.class);
    }

    public List<Task> getTasks(Pageable paging) {
        Page<Task> tasks = taskRepository.findAll(paging);
        return tasks.getContent();
    }

    public List<Task> getTaskByAuthor(String email, Pageable paging) {
        User author = userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("User not found " + email));
        Page<Task> pagedTasks = taskRepository.findByAuthor(author, paging);

        return pagedTasks.getContent();
    }

    public List<Task> getTaskByPerformer(String email, Pageable paging) {
        User performer = userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("User not found " + email));
        Page<Task> pagedTasks = taskRepository.findByPerformer(performer, paging);
        return pagedTasks.getContent();
    }

    @Transactional
    public void updateTask(Long id, UpdateTaskCommand taskDto) throws AccessDeniedException {

        Task existingTask = taskRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Task with ID " + id + " not found"));


        User currentUser = getCurrentAuthenticatedUsers();
        if (!existingTask.getAuthor().equals(currentUser)) {
            throw new AccessDeniedException("You are not authorized to update this task");
        }

        User performer = userRepository.findByEmail(taskDto.getPerformer().getEmail()).orElseThrow(() -> new EntityNotFoundException("Entity not found"));

        existingTask.setTitle(taskDto.getTitle());
        existingTask.setDescription(taskDto.getDescription());
        existingTask.setComments(taskDto.getComments());
        existingTask.setStatus(taskDto.getStatus());
        existingTask.setPriority(taskDto.getPriority());
        existingTask.setPerformer(performer); // Установить существующего пользователя

        taskRepository.save(existingTask);
    }

    @Transactional
    public void deleteTask(Long id) throws AccessDeniedException {

        var taskForDelete = taskRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Task not found for id: " + id));
        var currentUser = getCurrentAuthenticatedUsers();
        if (!taskForDelete.getAuthor().equals(currentUser)) {
            throw new AccessDeniedException("You do not have permission to delete this task");
        }
        taskRepository.delete(taskForDelete);
    }

    @Transactional
    public void changeTaskStatus(Long id, TaskStatus taskStatus) throws AccessDeniedException {
        var taskForUpdate = taskRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Task not found for id: " + id));

        var currentPerformer = getCurrentAuthenticatedUsers();

        if (!taskForUpdate.getPerformer().equals(currentPerformer)) {
            throw new AccessDeniedException("You do not have permission to update this task");
        }

        taskForUpdate.setStatus(taskStatus);

        taskRepository.save(taskForUpdate);
    }

    @Transactional
    public void addComment(Long id, List<String> comment) throws AccessDeniedException {
        var taskForUpdate = taskRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Task not found for id: " + id));

        var currentPerformer = getCurrentAuthenticatedUsers();

        if (currentPerformer != null &&
                (taskForUpdate.getPerformer() != null && taskForUpdate.getPerformer().equals(currentPerformer) ||
                        taskForUpdate.getAuthor() != null && taskForUpdate.getAuthor().equals(currentPerformer))) {

            taskForUpdate.setComments(comment);
            taskRepository.save(taskForUpdate);
        } else {
            throw new AccessDeniedException("You do not have permission to update this task");
        }
    }

    @Transactional
    public void addPerformer(Long id, User performer) throws AccessDeniedException {
        var taskForUpdate = taskRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Task not found for id: " + id));

        var currentUser = getCurrentAuthenticatedUsers();

        if (taskForUpdate.getAuthor().equals(currentUser)) {
            var existingPerformer = userRepository.findById(performer.getId())
                    .orElseThrow(() -> new EntityNotFoundException("User not found for id: " + performer.getId()));

            taskForUpdate.setPerformer(existingPerformer);
            taskRepository.save(taskForUpdate);
        } else {
            throw new AccessDeniedException("You do not have permission to update this task");
        }
    }

    public User getCurrentAuthenticatedUsers() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            String username = ((UserDetails) authentication.getPrincipal()).getUsername();
            return userRepository.findByEmail(username)
                    .orElseThrow(() -> new EntityNotFoundException("User with email " + username + " not found"));
        }
        throw new RuntimeException("No authenticated user found");
    }
}
