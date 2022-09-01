package com.todo.issue.service;

import com.todo.issue.model.domain.Profile;
import lombok.Synchronized;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class MemberService {
    /** 담당자 및 위임자 */
    private ArrayList<Profile> profiles;

    /** 담당자 번호 */
    private int number;

    /**
     * 생성자
     */
    public MemberService() {
        this.profiles = new ArrayList<Profile>();
        this.profiles.add(Profile.of(0, "김희정"));
        this.profiles.add(Profile.of(1, "임성욱"));
        this.profiles.add(Profile.of(2, "임시완"));
        this.profiles.add(Profile.of(3, "임시현"));
        this.profiles.add(Profile.of(4, "우영우"));

        this.number = 5;
    }

    /**
     * 이름이 일치하는 프로필 반환
     * @param number
     * @return null 또는 profile
     */
    public Profile getProfile(int number) {
        return this.profiles.stream()
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
     * @param name
     * @return 등록 결과
     */
    public boolean addProfile(String name) {
        int number = this.getNextNumber(); // 신규 번호를 발급
        
        // 신규 번호가 유효한 값이고 중복되지않음을 체크
        if ((0 < number) && (null == getProfile(number))) {
            this.profiles.add(Profile.of(number, name));
        } else {
            return false;
        }

        return true;
    }

    /**
     * 전체 프로필 리스트를 조회
     * @return 리스트
     */
    public ArrayList<Profile> getProfiles() {
        return this.profiles;
    }

}
