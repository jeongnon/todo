package com.amore.task.model.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class Member {
    /** 담당자 번호 */
    private int number;

    /** 담당자 이름 */
    private String name;

    public static Member of(int number, String name) {
        return Member.builder()
                .number(number)
                .name(name)
                .build();
    }
}
