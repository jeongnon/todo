package com.amore.task.model.enums;

import lombok.Getter;

@Getter
public enum ResponseStatus {
    SUCCESS("00", "성공"),
    FAIL("01", "실패"),

    FAIL02("02", "해당하는 업무가 없습니다. 번호를 확인해주세요."),
    FAIL03("03", "위임받은 업무가 아닙니다. 위임 취소할 수 없습니다."),
    FAIL04("04", "작업자가 존재하지 않습니다."),

    BAD_REQUEST01("05", "필수 값인 업무 제목이 누락되었습니다."),
    BAD_REQUEST02("06", "필수 값인 담당자 번호가 누락되었습니다."),
    BAD_REQUEST03("07", "필수 값인 프로필 번호가 누락되었습니다."),
    BAD_REQUEST04("08", "필수 값인 날짜가 누락되었습니다."),
    BAD_REQUEST05("09", "날짜 형식이 잘못되었습니다. 예시)2022-05-02"),
    BAD_REQUEST06("10", "중요도를 잘못입력하였습니다."),
    BAD_REQUEST07("11", "상태 변경 값을 잘못입력하였습니다.");

    /** 코드 */
    private final String code;

    /** 메시지 */
    private final String message;

    ResponseStatus(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
