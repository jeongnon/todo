package com.amore.task.service;

import com.amore.task.model.domain.Member;
import lombok.Synchronized;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class MemberService {
    /** 담당자 및 위임자 */
    private ArrayList<Member> members;

    /** 담당자 번호 */
    private int number;

    /**
     * 생성자
     */
    public MemberService() {
        this.members = new ArrayList<Member>();
        this.members.add(Member.of(0, "김희정"));
        this.members.add(Member.of(1, "임성욱"));
        this.members.add(Member.of(2, "임시완"));
        this.members.add(Member.of(3, "임시현"));
        this.members.add(Member.of(4, "우영우"));

        this.number = 5;
    }

    /**
     * 이름이 일치하는 프로필 반환
     * @param number 프로필번호
     * @return null 또는 member
     */
    public Member getMember(int number) {
        return this.members.stream()
                .filter(p -> number == p.getNumber())
                .findFirst()
                .orElse(null);
    }

    /**
     * 신규 담당자 번호 반급
     * @return 담당자 번호
     */
    @Synchronized
    public int getNextNumber() {
        this.number++;
        return this.number;
    }

    /**
     * 담당자 등록
     * @param name 이름
     * @return 등록 결과
     */
    public boolean addMember(String name) {
        int number = this.getNextNumber(); // 신규 번호를 발급
        
        // 신규 번호가 유효한 값이고 중복되지않음을 체크
        if ((0 < number) && (null == getMember(number))) {
            this.members.add(Member.of(number, name));
        } else {
            return false;
        }

        return true;
    }

    /**
     * 전체 프로필 리스트를 조회
     * @return 리스트
     */
    public ArrayList<Member> getProfiles() {
        return this.members;
    }

}
