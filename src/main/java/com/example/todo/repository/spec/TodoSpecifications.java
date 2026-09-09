package com.example.todo.repository.spec;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import com.example.todo.entity.Todo;

public final class TodoSpecifications {

    private TodoSpecifications() {
    }

    public static Specification<Todo> hasKeyword(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return null;
        }
        String likePattern = "%" + keyword.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("description")), likePattern);
    }

    public static Specification<Todo> hasCompletedStatus(Boolean completed) {
        if (completed == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("completed"), completed);
    }

    @SafeVarargs
    public static Specification<Todo> combine(Specification<Todo>... specs) {
        Specification<Todo> result = (root, query, cb) -> cb.conjunction();
        for (Specification<Todo> spec : specs) {
            if (spec != null) {
                result = result.and(spec);
            }
        }
        return result;
    }
}