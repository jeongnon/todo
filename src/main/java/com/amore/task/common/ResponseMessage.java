package com.amore.task.common;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
public class ResponseMessage {
    /** 상태 값 */
    private String status;

    /** 상태 설명 */
    private String desc;

    /** 결과 */
    private Object result;

    /** 날짜 */
    private String date;

    /** 시간 */
    private String time;

    /**
     * 생성자
     */
    public ResponseMessage() {
        this.setDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        this.setTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
    }

    /**
     * 생성자
     * 날짜/시간은 현재시간 기준 설정
     * @param status 상태값
     * @param desc 설명
     * @param result 결과값
     */
    public ResponseMessage(String status, String desc, Object result) {
        this.status = status;
        this.desc = desc;
        this.result = result;
        this.setDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        this.setTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
    }
}
