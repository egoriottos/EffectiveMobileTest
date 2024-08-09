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

    //создание задачи аунтефицированным пользователем
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

    //Админ создает Таски и может сам назначить автора и исполнителя
    public TaskResponse createTaskByAdmin(CreateTaskCommand createTaskCommand) {
        User performer = null;
        if (createTaskCommand.getPerformerUserName() != null) {
            performer = userRepository.findByEmail(createTaskCommand.getPerformerUserName())
                    .orElseThrow(() -> new UsernameNotFoundException("Performer not found " + createTaskCommand.getPerformerUserName()));
        }
        var task = Task.builder()
                .title(createTaskCommand.getTitle())
                .description(createTaskCommand.getDescription())
                .author(createTaskCommand.getAuthor())
                .status(TaskStatus.PENDING)
                .priority(createTaskCommand.getPriority())
                .performer(performer)
                .comments(Collections.singletonList(createTaskCommand.getComments()))
                .createdAt(LocalDateTime.now())
                .build();
        taskRepository.save(task);
        return modelMapper.map(task, TaskResponse.class);
    }

    //список задач с пагинацией
    public Page<TaskResponse> getTasks(Integer offset, Integer limit) {
        Pageable nextPage = PageRequest.of(offset, limit);

        return modelMapper.map(taskRepository.findAll(nextPage), Page.class);
    }

    //список задач по автору
    public List<Task> getTaskByAuthor(String email, Integer offset, Integer limit) {
        Pageable nextPage = PageRequest.of(offset, limit);
        User author = userRepository.findByEmail(email).orElseThrow(()-> new UsernameNotFoundException("User not found " + email));
        return taskRepository.findByAuthor(author, nextPage);
    }

    //список задач по исплнителю
    public List<Task> getTaskByPerformer(String username, Integer offset, Integer limit) {
        Pageable nextPage = PageRequest.of(offset, limit);
        return taskRepository.findByPerformerUsername(username, nextPage);
    }

    //обновление задачи
    @Transactional
    public void updateTask(Long id, UpdateTaskCommand updateTaskCommand) throws AccessDeniedException {
        var taskForUpdate = taskRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Task not found for id: " + id));
        User currentUser = getCurrentAuthenticatedUser();
        if (!taskForUpdate.getAuthor().equals(currentUser)) {
            throw new AccessDeniedException("You do not have permission to update this task");
        }
        if (!taskForUpdate.getTitle().equals(updateTaskCommand.getTitle())
                || !taskForUpdate.getDescription().equals(updateTaskCommand.getDescription())
                || !taskForUpdate.getStatus().equals(updateTaskCommand.getStatus())
                || !taskForUpdate.getPriority().equals(updateTaskCommand.getPriority())
                || !taskForUpdate.getPerformer().equals(updateTaskCommand.getPerformer())
                || !taskForUpdate.getComments().equals(updateTaskCommand.getComments())
        ) {
            taskForUpdate.setTitle(updateTaskCommand.getTitle());
            taskForUpdate.setDescription(updateTaskCommand.getDescription());
            taskForUpdate.setStatus(updateTaskCommand.getStatus());
            taskForUpdate.setPriority(updateTaskCommand.getPriority());
            taskForUpdate.setPerformer(updateTaskCommand.getPerformer());
            taskForUpdate.setComments(updateTaskCommand.getComments());
            taskForUpdate.setUpdatedAt(LocalDateTime.now());
            taskRepository.save(taskForUpdate);
        }
    }

    //удаляем задачу
    @Transactional
    public void deleteTask(Long id) throws AccessDeniedException {
        var taskForDelete = taskRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Task not found for id: " + id));
        var currentUser = getCurrentAuthenticatedUser();
        if (!taskForDelete.getAuthor().equals(currentUser)) {
            throw new AccessDeniedException("You do not have permission to delete this task");
        }
        taskRepository.delete(taskForDelete);
    }

    //меняем статус задачи
    @Transactional
    public void changeTaskStatus(Long id, TaskStatus taskStatus) throws AccessDeniedException {
        var taskForUpdate = taskRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Task not found for id: " + id));
        var currentPerformer = getCurrentAuthenticatedUser();
        if (!taskForUpdate.getPerformer().equals(currentPerformer)) {
            throw new AccessDeniedException("You do not have permission to update this task");
        }
        taskForUpdate.setStatus(taskStatus);
        taskRepository.save(taskForUpdate);

    }

    //добавление комментариев
    @Transactional
    public void addComment(Long id, List<String> comment) throws AccessDeniedException {
        var taskForUpdate = taskRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Task not found for id: " + id));
        var currentPerformer = getCurrentAuthenticatedUser();
        if (!taskForUpdate.getPerformer().equals(currentPerformer)
                || !taskForUpdate.getAuthor().equals(currentPerformer)) {
            throw new AccessDeniedException("You do not have permission to update this task");
        }
        taskForUpdate.setComments(comment);
        taskRepository.save(taskForUpdate);
    }

    //добавления исполнителя
    @Transactional
    public void addPerformer(Long id, User performer) throws AccessDeniedException {
        var taskForUpdate = taskRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Task not found for id: " + id));
        var currentAuthor = getCurrentAuthenticatedUser();
        if (!taskForUpdate.getAuthor().equals(currentAuthor)) {
            throw new AccessDeniedException("You do not have permission to update this task");
        }
        taskForUpdate.setPerformer(performer);
        taskRepository.save(taskForUpdate);
    }

    //метод чтобы пользователь мог только свои задачи менять если автор и статус если исполнитель
    private User getCurrentAuthenticatedUser() throws AccessDeniedException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null && !authentication.isAuthenticated()) {
            throw new AccessDeniedException("You must be authenticated");
        }
        String email = authentication.getPrincipal().toString();
        return userRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("User not found " + email));
    }

}
