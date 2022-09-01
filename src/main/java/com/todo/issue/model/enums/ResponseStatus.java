package com.todo.issue.model.enums;

import lombok.Getter;

@Getter
public enum ResponseStatus {
    SUCCESS("00", "성공"),
    FAIL("01", "실패"),

    FAIL02("02", "");

    /** 코드 */
    private final String code;

    /** 메시지 */
    private final String message;

    ResponseStatus(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
