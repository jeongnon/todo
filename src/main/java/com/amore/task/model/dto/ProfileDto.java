package com.amore.task.model.dto;

import com.amore.task.model.domain.Member;
import com.amore.task.model.enums.ProgressStatus;
import com.amore.task.model.enums.TaskLevel;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.HashMap;

@Getter
@Setter
public class ProfileDto {
    /** 번호 */
    private int num = 0;

    /** 날짜 */
    private LocalDate date;

    /** 담당자 */
    private MemberDto assignee;

    /** 위임자 */
    private MemberDto reporter;

    /** 중요도 */
    private TaskLevel taskLevel;

    /** 순서 */
    private Integer seq;

    /** 업무 제목 */
    private String task;

    /** 업무 설명 */
    private String description;

    /** 상태 */
    private ProgressStatus status;

    /** 로그 */
    private HashMap<String, Object> log;

    public ProfileDto() {}

    public ProfileDto(LocalDate date, MemberDto assignee, String task, String description) {
        setDate(date);
        setAssignee(assignee);
        setTask(task);
        setDescription(description);
    }
}
