package com.amore.task.model.enums;

import lombok.Getter;
import org.springframework.scheduling.config.Task;

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

    public static boolean isValid(String taskLevel) {
        try {
            TaskLevel.valueOf(taskLevel);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
