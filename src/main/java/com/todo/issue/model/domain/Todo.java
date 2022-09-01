package com.todo.issue.model.domain;

import com.todo.issue.model.enums.TaskLevel;
import com.todo.issue.model.enums.ProgressStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
public class Todo {
    /** 번호 */
    private int num;

    /** 날짜 */
    private LocalDate date;

    /** 담당자 */
    private Profile assignee;

    /** 위임자 */
    private String reporter;

    /** 중요도 */
    private TaskLevel taskLevel;

    /** 순서 */
    private int seq;

    /** 업무 제목 */
    private String task;

    /** 업무 설명 */
    private String description;

    /** 상태 */
    private String status;

    public static Todo of(int num, LocalDate date, Profile assignee, TaskLevel taskLevel, int seq, String task, String description) {
        Todo todo = Todo.builder()
                .num(num)
                .date(date)
                .assignee(assignee)
                .taskLevel(taskLevel)
                .seq(seq)
                .task(task)
                .status(ProgressStatus.OPEN.getStatus())
                .build();

        // 업무설명은 선택사항
        if ((null != description) && (!"".equals(description))) {
            todo.setDescription(description);
        }

        return todo;
    }
}