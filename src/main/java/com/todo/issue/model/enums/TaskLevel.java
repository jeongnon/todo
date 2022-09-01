package com.todo.issue.model.enums;

import lombok.Getter;

/**
 * 중요도
 */
@Getter
public enum TaskLevel {
    S(0, "S"),
    A(1, "A"),
    B(2, "B"),
    C(3, "C"),
    D(4, "D");

    private final int code;
    private final String name;

    TaskLevel(int code, String name) {
        this.code = code;
        this.name = name;
    }
}
