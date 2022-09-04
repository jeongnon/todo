package com.amore.task.model.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberDto {
    /** 번호 */
    private Integer num;

    /** 이름 */
    private String name;

    public MemberDto(int num, String name) {
        this.setNum(num);
        this.setName(name);
    }
}
