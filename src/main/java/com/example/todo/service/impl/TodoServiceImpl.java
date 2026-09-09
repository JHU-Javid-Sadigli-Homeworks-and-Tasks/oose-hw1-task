package com.example.todo.service.impl;

import com.example.todo.dto.request.TodoRequest;
import com.example.todo.dto.response.TodoResponse;
import com.example.todo.entity.Todo;
import com.example.todo.exception.TodoNotFoundException;
import com.example.todo.repository.TodoRepository;
import com.example.todo.repository.spec.TodoSpecifications;
import com.example.todo.service.TodoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TodoServiceImpl implements TodoService {

    private static final Set<String> SORTABLE_FIELDS = Set.of("deadline", "description", "createdAt", "completed");

    private final TodoRepository todoRepository;

    @Override
    public TodoResponse create(TodoRequest request) {
        Todo todo = Todo.builder()
                .description(request.getDescription())
                .deadline(request.getDeadline())
                .completed(false)
                .build();
        Todo saved = todoRepository.save(todo);
        return toResponse(saved);
    }

    @Override
    public TodoResponse update(Long id, TodoRequest request) {
        Todo todo = findEntityOrThrow(id);
        todo.setDescription(request.getDescription());
        todo.setDeadline(request.getDeadline());
        return toResponse(todoRepository.save(todo));
    }

    @Override
    public void delete(Long id) {
        if (!todoRepository.existsById(id)) {
            throw new TodoNotFoundException(id);
        }
        todoRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public TodoResponse getById(Long id) {
        return toResponse(findEntityOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TodoResponse> search(String keyword, Boolean completed, String sortBy, String direction) {
        Specification<Todo> specification = TodoSpecifications.combine(
                TodoSpecifications.hasKeyword(keyword),
                TodoSpecifications.hasCompletedStatus(completed)
        );

        Sort sort = buildSort(sortBy, direction);

        return todoRepository.findAll(specification, sort).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public TodoResponse setCompleted(Long id, boolean completed) {
        Todo todo = findEntityOrThrow(id);
        todo.setCompleted(completed);
        return toResponse(todoRepository.save(todo));
    }

    private Todo findEntityOrThrow(Long id) {
        return todoRepository.findById(id)
                .orElseThrow(() -> new TodoNotFoundException(id));
    }

    private Sort buildSort(String sortBy, String direction) {
        String field = StringUtils.hasText(sortBy) && SORTABLE_FIELDS.contains(sortBy) ? sortBy : "deadline";
        Sort.Direction dir = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
        return Sort.by(dir, field);
    }

    private TodoResponse toResponse(Todo todo) {
        boolean overdue = !todo.isCompleted() && todo.getDeadline() != null
                && todo.getDeadline().isBefore(LocalDate.now());
        return TodoResponse.builder()
                .id(todo.getId())
                .description(todo.getDescription())
                .deadline(todo.getDeadline())
                .completed(todo.isCompleted())
                .overdue(overdue)
                .build();
    }
}
