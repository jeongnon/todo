package com.amore.task.service;

import static org.assertj.core.api.Assertions.*;

import com.amore.task.model.domain.Member;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@Slf4j
@SpringBootTest
public class MemberServiceTest {

    @Autowired
    MemberService memberService;

    @DisplayName("담당자 번호로 member 조회")
    @Test
    public void getProfileTest() {
        int number = 0;
        Member member = memberService.getMember(number);
        assertThat(member.getNumber()).isEqualTo(number);
        assertThat(member.getName()).isEqualTo("김희정");
    }

    @DisplayName("해당하는 담당자 번호 없음")
    @Test
    public void getNoProfileTest() {
        int number = memberService.getNextNumber(); // 신규 담당자 번호
        log.debug("new number = " + number);
        assertThat(memberService.getMember(number)).isNull();
    }

    @DisplayName("신규 담당자 번호 발급")
    @Test
    public void getNextNumberTest() {
        int number = memberService.getNextNumber(); // 신규 담당자 번호
        log.debug("new number = " + number);
        assertThat(memberService.getNextNumber()).isGreaterThan(number);
        assertThat(memberService.getNextNumber()).isEqualTo(number + 2);
    }

    @DisplayName("담당자 등록")
    @Test
    public void addMemberTest() {
        String name = "김희정";
        int number = memberService.getNextNumber(); // 등록한 담당자의 번호와 비교하기 위함
        log.debug("new number = {}", number);

        if (memberService.addMember(name)) {
            // 등록한 담당자의 번호는 number 이후 신규 발급된 번호이기때문에 1 증가하였음
            assertThat(memberService.getMember(number + 1).getName()).isEqualTo(name);
            assertThat(memberService.getMember(number + 1).getNumber()).isEqualTo(number + 1);
        } else {
            fail("담당자 등록 실패");
        }
    }

    @DisplayName("담당자 리스트 조회")
    @Test
    public void getProfilesTest() {
        List<Member> members = memberService.getProfiles(); // 전체 리스트 조회
        assertThat(members.size()).isGreaterThanOrEqualTo(5); // 초기 세팅 된 담당자 5명보다 많아야 함
        members.forEach(p -> {
            log.debug("number = {}, name = {}", p.getNumber(), p.getName());
        });
    }
}
