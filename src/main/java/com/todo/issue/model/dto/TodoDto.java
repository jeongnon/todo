package com.todo.issue.model.dto;

import com.todo.issue.model.domain.Profile;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class TodoDto {
    /** 날짜 */
    private LocalDate date;

    /** 담당자 */
    private Profile assignee;

    /** 위임자 */
    private String reporter;

    /** 중요도 */
    private String priority;

    /** 업무 제목 */
    private String task;

    /** 업무 설명 */
    private String description;

    /** 상태 */
    private String status;
}
