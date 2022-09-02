package com.todo.issue.model.domain;

import com.todo.issue.model.enums.ProgressStatus;
import com.todo.issue.model.enums.TaskLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.HashMap;

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
    
    /** 위임취소 시 원상복구 값 */
    private HashMap<String, Object> log;

    /** log(Map)에 입력할 KEY로 사용 */
    public static final String KEY_TASK_LEVEL = "taskLevel";
    public static final String KEY_SEQ = "seq";

    public static Todo of(int num, LocalDate date, Profile assignee, TaskLevel taskLevel, int seq, String task, ProgressStatus status, String description) {
        Todo todo = Todo.builder()
                .num(num)
                .date(date)
                .assignee(assignee)
                .taskLevel(taskLevel)
                .seq(seq)
                .task(task)
                .status(status)
                .description(description)
                .build();

        return todo;
    }

    public static Todo of(int num, LocalDate date, Profile assignee, TaskLevel taskLevel, int seq, String task, ProgressStatus status
            , String description, Profile reporter, HashMap<String, Object> log) {
        Todo todo = Todo.builder()
                .num(num)
                .date(date)
                .assignee(assignee)
                .taskLevel(taskLevel)
                .seq(seq)
                .task(task)
                .status(status)
                .description(description)
                .reporter(reporter)
                .log(log)
                .build();

        return todo;
    }
}