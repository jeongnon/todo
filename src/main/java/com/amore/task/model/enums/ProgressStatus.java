package com.amore.task.model.enums;

import lombok.Getter;

/**
 * profile 상태 (진행 상황)
 */
@Getter
public enum ProgressStatus {
    OPEN("진행 중"),
    COMPLETE("완료"),
    CANCEL("취소"),
    ASSIGN("위임");

    private final String name;

    ProgressStatus(String name) {
        this.name = name;
    }
}
