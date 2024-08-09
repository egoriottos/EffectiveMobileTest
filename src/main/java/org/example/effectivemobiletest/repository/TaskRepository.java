package org.example.effectivemobiletest.repository;

import org.example.effectivemobiletest.domain.entity.Task;
import org.example.effectivemobiletest.domain.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByAuthor(User user, Pageable pageable);
    List<Task> findByPerformerUsername(String username, Pageable pageable);
}
