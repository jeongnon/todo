package com.amore.task.service;

import com.amore.task.common.HandledException;
import com.amore.task.model.domain.Member;
import com.amore.task.model.domain.Profile;
import com.amore.task.model.dto.MemberDto;
import com.amore.task.model.dto.ProfileDto;
import com.amore.task.model.enums.ResponseStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class TaskService {
    @Autowired
    MemberService memberService;

    @Autowired
    ProfileService profileService;

    /**
     * 작업자 추가
     * @param memberDto
     * @return 결과
     */
    public boolean addMember(MemberDto memberDto) {
        return memberService.addMember(memberDto.getName());
    }

    /**
     * 프로필 추가
     * @param profileDto
     * @return 추가 한 프로필
     */
    public Profile addProfile(MemberDto memberDto, ProfileDto profileDto) {
        // 담당자 조회
        Member member = memberService.getMember(memberDto.getNum());
        if (null == member) {
            throw new HandledException(HttpStatus.BAD_REQUEST, ResponseStatus.FAIL04);
        } else {
            profileDto.setAssignee(new MemberDto(member.getNumber(), member.getName()));
        }

        int number = profileService.addProfile(profileDto);
        if (0 < number) {
            return profileService.getProfile(number);
        } else {
            return null;
        }
    }

    /**
     * 프로필 정보 변경
     * @param profileDto
     * @return 변경 된 프로필
     */
    public Profile updateProfile(ProfileDto profileDto) {
        Profile profile = profileService.getProfile(profileDto.getNum());
        if (null == profile) {
            throw new HandledException(HttpStatus.BAD_REQUEST, ResponseStatus.FAIL02);
        }

        profileService.updateProfile(profileDto);
        profile = profileService.getProfile(profileDto.getNum()); // 변경 된 정보를 조회
        return profile;
    }

    /**
     * 다른 담당자에게 위임
     * @param memberDto
     * @param profileDto
     * @return 위임한 프로필
     */
    public Profile allocateTask(MemberDto memberDto, ProfileDto profileDto) {
        Member assignee = memberService.getMember(memberDto.getNum());
        if (null == assignee) {
            throw new HandledException(HttpStatus.BAD_REQUEST, ResponseStatus.FAIL04);
        }

        Profile profile = profileService.getProfile(profileDto.getNum());
        if (null == profile) {
            throw new HandledException(HttpStatus.BAD_REQUEST, ResponseStatus.FAIL02);
        }

        profileDto.setAssignee(new MemberDto(assignee.getNumber(), assignee.getName())); // 담당자
        profileService.allocateAssignee(profileDto);
        return profileService.getProfile(profileDto.getNum());
    }

    /**
     * 위임 취소
     * @param profileDto
     * @return 취소 결과
     */
    public Profile cancelAllocation(ProfileDto profileDto) {
        Profile profile = profileService.getProfile(profileDto.getNum());
        if (null == profile) {
            throw new HandledException(HttpStatus.BAD_REQUEST, ResponseStatus.FAIL02);
        }

        profileService.cancelAllocatedProfile(profile.getNum());
        return profile = profileService.getProfile(profileDto.getNum());
    }

    /**
     * 프로필 삭제
     * @param profileDto
     * @return 삭제 결과
     */
    public boolean deleteProfile(ProfileDto profileDto) {
        Profile profile = profileService.getProfile(profileDto.getNum());
        if (null == profile) {
            throw new HandledException(HttpStatus.BAD_REQUEST, ResponseStatus.FAIL02);
        }

        return profileService.removeProfile(profile.getNum());
    }

    /**
     * 프로필 리스트 조회
     * @param profileDto
     * @return 프로필 리스트
     */
    public ArrayList<Profile> getProfiles(ProfileDto profileDto) {
        return profileService.getProfiles(profileDto);
    }

    /**
     * 프로필 조회
     * @param num
     * @return 프로필
     */
    public Profile getProfile(int num) {
        return profileService.getProfile(num);
    }
}
