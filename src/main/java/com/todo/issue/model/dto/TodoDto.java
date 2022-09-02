package com.todo.issue.model.dto;

import com.todo.issue.model.domain.Profile;
import com.todo.issue.model.enums.ProgressStatus;
import com.todo.issue.model.enums.TaskLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.HashMap;

@Getter
@Setter
public class TodoDto {
    /** 번호 */
    private int num = 0;

    /** 날짜 */
    private LocalDate date;

    /** 담당자 */
    private Profile assignee;

    /** 위임자 */
    private Profile reporter;

    /** 중요도 */
    private TaskLevel taskLevel;

    /** 순서 */
    private int seq;

    /** 업무 제목 */
    private String task;

    /** 업무 설명 */
    private String description;

    /** 상태 */
    private ProgressStatus status;

    /** 로그 */
    private HashMap<String, Object> log;

    public TodoDto() {}

    public TodoDto(LocalDate date, Profile assignee, String task, String description) {
        setDate(date);
        setAssignee(assignee);
        setTask(task);
        setDescription(description);
    }
}
