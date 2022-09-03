package com.amore.task.model.enums;

import lombok.Getter;

@Getter
public enum ResponseStatus {
    SUCCESS("00", "성공"),
    FAIL("01", "실패"),

    FAIL02("02", "해당하는 업무가 없습니다. 번호를 확인해주세요."),
    FAIL03("03", "위임받은 업무가 아닙니다. 위임 취소할 수 없습니다.");

    /** 코드 */
    private final String code;

    /** 메시지 */
    private final String message;

    ResponseStatus(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
