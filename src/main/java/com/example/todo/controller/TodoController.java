package com.example.todo.controller;

import com.example.todo.dto.request.TodoRequest;
import com.example.todo.dto.response.TodoResponse;
import com.example.todo.service.TodoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/todos")
@RequiredArgsConstructor
public class TodoController {

    private final TodoService todoService;

    @GetMapping
    public String list(@RequestParam(required = false) String keyword,
                        @RequestParam(required = false) Boolean completed,
                        @RequestParam(defaultValue = "deadline") String sortBy,
                        @RequestParam(defaultValue = "asc") String direction,
                        Model model) {

        List<TodoResponse> todos = todoService.search(keyword, completed, sortBy, direction);

        model.addAttribute("todos", todos);
        model.addAttribute("keyword", keyword);
        model.addAttribute("completed", completed);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("direction", direction);
        return "todos/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("todoRequest", new TodoRequest());
        model.addAttribute("formAction", "/todos");
        model.addAttribute("formTitle", "Add a new task");
        return "todos/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("todoRequest") TodoRequest todoRequest,
                          BindingResult bindingResult,
                          Model model,
                          RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("formAction", "/todos");
            model.addAttribute("formTitle", "Add a new task");
            return "todos/form";
        }
        todoService.create(todoRequest);
        redirectAttributes.addFlashAttribute("message", "Task added successfully.");
        return "redirect:/todos";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        TodoResponse todo = todoService.getById(id);

        TodoRequest todoRequest = new TodoRequest(todo.getDescription(), todo.getDeadline());
        model.addAttribute("todoRequest", todoRequest);
        model.addAttribute("todoId", id);
        model.addAttribute("formAction", "/todos/" + id);
        model.addAttribute("formTitle", "Edit task");
        return "todos/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                          @Valid @ModelAttribute("todoRequest") TodoRequest todoRequest,
                          BindingResult bindingResult,
                          Model model,
                          RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("todoId", id);
            model.addAttribute("formAction", "/todos/" + id);
            model.addAttribute("formTitle", "Edit task");
            return "todos/form";
        }
        todoService.update(id, todoRequest);
        redirectAttributes.addFlashAttribute("message", "Task updated successfully.");
        return "redirect:/todos";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        todoService.delete(id);
        redirectAttributes.addFlashAttribute("message", "Task deleted.");
        return "redirect:/todos";
    }

    @PostMapping("/{id}/complete")
    public String setCompleted(@PathVariable Long id,
                                @RequestParam boolean completed,
                                RedirectAttributes redirectAttributes) {
        todoService.setCompleted(id, completed);
        redirectAttributes.addFlashAttribute("message",
                completed ? "Task marked as completed." : "Task marked as pending.");
        return "redirect:/todos";
    }
}
