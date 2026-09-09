package com.example.todo.service;

import com.example.todo.dto.request.TodoRequest;
import com.example.todo.dto.response.TodoResponse;

import java.util.List;

public interface TodoService {

    TodoResponse create(TodoRequest request);

    TodoResponse update(Long id, TodoRequest request);

    void delete(Long id);

    TodoResponse getById(Long id);

    List<TodoResponse> search(String keyword, Boolean completed, String sortBy, String direction);

    TodoResponse setCompleted(Long id, boolean completed);
}
