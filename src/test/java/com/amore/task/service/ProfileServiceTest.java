package com.amore.task.service;

import static org.assertj.core.api.Assertions.*;

import com.amore.task.common.HandledException;
import com.amore.task.model.domain.Member;
import com.amore.task.model.domain.Profile;
import com.amore.task.model.dto.MemberDto;
import com.amore.task.model.dto.ProfileDto;
import com.amore.task.model.enums.ProgressStatus;
import com.amore.task.model.enums.TaskLevel;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;

@Slf4j
@SpringBootTest
public class ProfileServiceTest {

    @Autowired
    ProfileService profileService;

    @AfterEach
    @Test
    public void afterEachRun() {
        profileService.getProfiles(null).clear();
    }

    @DisplayName("신규 todo 번호 발급")
    @Test
    public void getNextNumberTest() {
        int number = profileService.getNextNumber(); // 신규 번호
        log.debug("new number = " + number);
        assertThat(profileService.getNextNumber()).isGreaterThan(number);
        assertThat(profileService.getNextNumber()).isEqualTo(number + 2);
    }

    @DisplayName("todo 추가")
    @Test
    public void addTodoTest() {
        ProfileDto profileDto = new ProfileDto();
        profileDto.setDate(LocalDate.of(2022, 5, 1));
        profileDto.setAssignee(new MemberDto(0, "김희정"));
        profileDto.setTask("해야할 일");
        profileDto.setDescription("간략설명");

        int num = profileService.addProfile(profileDto);
        assertThat(num).isGreaterThanOrEqualTo(1);

        Profile profile = profileService.getProfile(num);
        assertThat(profile).isNotNull();
        assertThat(profile.getDate()).isEqualTo(profileDto.getDate());
        assertThat(profile.getAssignee().getNumber()).isEqualTo(profileDto.getAssignee().getNum());
        assertThat(profile.getTask()).isEqualTo(profileDto.getTask());
        assertThat(profile.getDescription()).isEqualTo(profileDto.getDescription());

        // addTodo 내부에서 부여한 우선순위
        log.debug("우선순위 = {}{}", profile.getTaskLevel().getName(), profile.getSeq());
        Assertions.assertThat(profile.getTaskLevel()).isNotNull();
        assertThat(profile.getSeq()).isNotNull();
        Assertions.assertThat(profile.getStatus()).isEqualTo(ProgressStatus.OPEN);
    }

    @DisplayName("동일 날짜 동일 담당자로 todo 추가 시 우선순위")
    @Test
    public void addTodoSameDateSameAssigneePriorityTest() {
        LocalDate date = LocalDate.of(2022, 5, 1);
        MemberDto assignee = new MemberDto(0, "김희정");

        ProfileDto profileDto = new ProfileDto();
        profileDto.setDate(date);
        profileDto.setAssignee(assignee);
        profileDto.setTask("해야할 일");
        profileDto.setDescription("간략설명");

        int num = profileService.addProfile(profileDto);
        assertThat(num).isGreaterThanOrEqualTo(1);

        profileDto = new ProfileDto();
        profileDto.setDate(date);
        profileDto.setAssignee(assignee);
        profileDto.setTask("해야할 일2");
        profileDto.setDescription("간략설명2");

        int nextNum = profileService.addProfile(profileDto);
        assertThat(nextNum).isGreaterThan(num); // 먼저 추가 된 해야할일의 번호보다 높다

        // addTodo 내부에서 부여한 우선순위 비교
        Profile firstProfile = profileService.getProfile(num);
        assertThat(firstProfile).isNotNull();
        Profile secondProfile = profileService.getProfile(nextNum);
        assertThat(secondProfile).isNotNull();

        log.debug("firstTodo 우선순위 = {}{}", firstProfile.getTaskLevel().getName(), firstProfile.getSeq());
        log.debug("secondTodo 우선순위 = {}{}", secondProfile.getTaskLevel().getName(), secondProfile.getSeq());
        Assertions.assertThat(secondProfile.getTaskLevel()).isEqualTo(firstProfile.getTaskLevel());
        assertThat(secondProfile.getSeq()).isGreaterThan(firstProfile.getSeq());
    }

    @DisplayName("동일 날짜 다른 담당자로 todo 추가 시 우선순위")
    @Test
    public void addTodoSameDatePriorityTest() {
        LocalDate date = LocalDate.of(2022, 5, 1);
        ProfileDto profileDto = new ProfileDto();
        profileDto.setDate(date);
        profileDto.setAssignee(new MemberDto(0, "김희정"));
        profileDto.setTask("해야할 일");
        profileDto.setDescription("간략설명");

        int num = profileService.addProfile(profileDto);
        assertThat(num).isGreaterThanOrEqualTo(1);

        profileDto = new ProfileDto();
        profileDto.setDate(date);
        profileDto.setAssignee(new MemberDto(1, "임성욱"));
        profileDto.setTask("해야할 일2");
        profileDto.setDescription("간략설명2");

        int nextNum = profileService.addProfile(profileDto);
        assertThat(nextNum).isGreaterThan(num); // 먼저 추가 된 해야할일의 번호보다 높다

        // addTodo 내부에서 부여한 우선순위 비교
        Profile profile1 = profileService.getProfile(num);
        assertThat(profile1).isNotNull();
        Profile profile2 = profileService.getProfile(nextNum);
        assertThat(profile2).isNotNull();

        log.debug("todo1 우선순위 = {}{}", profile1.getTaskLevel().getName(), profile1.getSeq());
        Assertions.assertThat(profile1.getTaskLevel()).isEqualTo(TaskLevel.B);
        assertThat(profile1.getSeq()).isEqualTo(0);

        log.debug("todo2 우선순위 = {}{}", profile2.getTaskLevel().getName(), profile2.getSeq());
        Assertions.assertThat(profile2.getTaskLevel()).isEqualTo(TaskLevel.B);
        assertThat(profile2.getSeq()).isEqualTo(0);
    }

    @DisplayName("다른 날짜 동일 담당자로 todo 추가 시 우선순위")
    @Test
    public void addTodoSameAssigneePriorityTest() {
        MemberDto assignee = new MemberDto(10, "김희정");
        ProfileDto profileDto = new ProfileDto();
        profileDto.setDate(LocalDate.of(2023, 5, 1));
        profileDto.setAssignee(assignee);
        profileDto.setTask("해야할 일");
        profileDto.setDescription("간략설명");

        int num1 = profileService.addProfile(profileDto);
        assertThat(num1).isGreaterThanOrEqualTo(1);

        profileDto = new ProfileDto();
        profileDto.setDate(LocalDate.of(2023, 5, 1));
        profileDto.setAssignee(assignee);
        profileDto.setTask("해야할 일2");
        profileDto.setDescription("간략설명2");

        int num2 = profileService.addProfile(profileDto);
        assertThat(num2).isGreaterThan(num1); // 먼저 추가 된 해야할일의 번호보다 높다

        profileDto = new ProfileDto();
        profileDto.setDate(LocalDate.of(2023, 6, 1));
        profileDto.setAssignee(assignee);
        profileDto.setTask("해야할 일3");
        profileDto.setDescription("간략설명3");

        int num3 = profileService.addProfile(profileDto);
        assertThat(num3).isGreaterThan(num2); // 먼저 추가 된 해야할일의 번호보다 높다

        // addTodo 내부에서 부여한 우선순위 비교
        Profile profile1 = profileService.getProfile(num1);
        assertThat(profile1).isNotNull();
        Profile profile2 = profileService.getProfile(num2);
        assertThat(profile2).isNotNull();
        Profile profile3 = profileService.getProfile(num3);
        assertThat(profile3).isNotNull();

        log.debug("todo1 우선순위 = {}{}", profile1.getTaskLevel().getName(), profile1.getSeq());
        Assertions.assertThat(profile1.getTaskLevel()).isEqualTo(TaskLevel.B);
        assertThat(profile1.getSeq()).isEqualTo(0);

        log.debug("todo2 우선순위 = {}{}", profile2.getTaskLevel().getName(), profile2.getSeq());
        Assertions.assertThat(profile2.getTaskLevel()).isEqualTo(TaskLevel.B);
        assertThat(profile2.getSeq()).isEqualTo(1);

        log.debug("todo3 우선순위 = {}{}", profile3.getTaskLevel().getName(), profile3.getSeq());
        Assertions.assertThat(profile3.getTaskLevel()).isEqualTo(TaskLevel.B);
        assertThat(profile3.getSeq()).isEqualTo(0);
    }

    @DisplayName("다른 중요도로 todo 추가 시 우선순위")
    @Test
    public void addTodoNotSameTaskLevelPriorityTest() {
        LocalDate date = LocalDate.of(2022, 5, 1);
        MemberDto member = new MemberDto(20, "김희정");
        ProfileDto profileDto = new ProfileDto();
        profileDto.setDate(date);
        profileDto.setAssignee(member);
        profileDto.setTask("해야할 일");
        profileDto.setDescription("간략설명");

        int num = profileService.addProfile(profileDto);
        assertThat(num).isGreaterThanOrEqualTo(1);

        profileDto = new ProfileDto();
        profileDto.setDate(date);
        profileDto.setAssignee(member);
        profileDto.setTaskLevel(TaskLevel.C); // 다른중요도로 입력
        profileDto.setTask("해야할 일2");
        profileDto.setDescription("간략설명2");

        int nextNum = profileService.addProfile(profileDto);
        assertThat(nextNum).isGreaterThan(num); // 먼저 추가 된 해야할일의 번호보다 높다

        // addTodo 내부에서 부여한 우선순위 비교
        Profile profile1 = profileService.getProfile(num);
        assertThat(profile1).isNotNull();
        Profile profile2 = profileService.getProfile(nextNum);
        assertThat(profile2).isNotNull();

        log.debug("todo1 우선순위 = {}{}", profile1.getTaskLevel().getName(), profile1.getSeq());
        Assertions.assertThat(profile1.getTaskLevel()).isEqualTo(TaskLevel.B);
        assertThat(profile1.getSeq()).isEqualTo(0);

        log.debug("todo2 우선순위 = {}{}", profile2.getTaskLevel().getName(), profile2.getSeq());
        Assertions.assertThat(profile2.getTaskLevel()).isEqualTo(TaskLevel.C);
        assertThat(profile2.getSeq()).isEqualTo(0);
    }

    @DisplayName("다른 중요도로 todo 추가 시 우선순위")
    @Test
    public void addTodoNotSameTaskLevelPriority2Test() {
        LocalDate date = LocalDate.of(2022, 5, 1);
        MemberDto member = new MemberDto(30, "김희정");
        ProfileDto profileDto = new ProfileDto();
        profileDto.setDate(date);
        profileDto.setAssignee(member);
        profileDto.setTask("해야할 일");
        profileDto.setDescription("간략설명");

        int num = profileService.addProfile(profileDto);
        assertThat(num).isGreaterThanOrEqualTo(1);

        profileDto = new ProfileDto();
        profileDto.setDate(date);
        profileDto.setAssignee(member);
        profileDto.setTaskLevel(TaskLevel.S); // 다른중요도로 입력
        profileDto.setTask("해야할 일2");
        profileDto.setDescription("간략설명2");

        int nextNum = profileService.addProfile(profileDto);
        assertThat(nextNum).isGreaterThan(num); // 먼저 추가 된 해야할일의 번호보다 높다

        // addTodo 내부에서 부여한 우선순위 비교
        Profile profile1 = profileService.getProfile(num);
        assertThat(profile1).isNotNull();
        Profile profile2 = profileService.getProfile(nextNum);
        assertThat(profile2).isNotNull();

        log.debug("todo1 우선순위 = {}{}", profile1.getTaskLevel().getName(), profile1.getSeq());
        Assertions.assertThat(profile1.getTaskLevel()).isEqualTo(TaskLevel.B);
        assertThat(profile1.getSeq()).isEqualTo(0);

        log.debug("todo2 우선순위 = {}{}", profile2.getTaskLevel().getName(), profile2.getSeq());
        Assertions.assertThat(profile2.getTaskLevel()).isEqualTo(TaskLevel.S);
        assertThat(profile2.getSeq()).isEqualTo(0);
    }

    @DisplayName("다른 담당자에게 위임 시 우선순위와 status")
    @Test
    public void addTodoAllocatedPriorityTest() {
        LocalDate date = LocalDate.of(2022, 5, 1);
        MemberDto assignee = new MemberDto(40, "김희정");
        MemberDto reporter = new MemberDto(41, "김희정");

        ProfileDto profileDto = new ProfileDto();
        profileDto.setDate(date);
        profileDto.setAssignee(assignee);
        profileDto.setReporter(reporter);
        profileDto.setTask("해야할 일");
        profileDto.setDescription("간략설명");
        profileDto.setTaskLevel(TaskLevel.D);
        profileDto.setSeq(3);
        profileDto.setStatus(ProgressStatus.ASSIGN);

        int num = profileService.addProfile(profileDto);
        assertThat(num).isGreaterThanOrEqualTo(1);

        Profile profile = profileService.getProfile(num);

        log.debug("todo 우선순위 = {}{}", profile.getTaskLevel().getName(), profile.getSeq());
        Assertions.assertThat(profile.getTaskLevel()).isEqualTo(TaskLevel.A);
        assertThat(profile.getSeq()).isEqualTo(0);
        Assertions.assertThat(profile.getStatus()).isEqualTo(ProgressStatus.ASSIGN);

        profileDto = new ProfileDto();
        profileDto.setDate(date);
        profileDto.setAssignee(assignee);
        profileDto.setReporter(reporter);
        profileDto.setTask("해야할 일");
        profileDto.setDescription("간략설명");
        profileDto.setTaskLevel(TaskLevel.D);
        profileDto.setSeq(3);
        profileDto.setStatus(ProgressStatus.ASSIGN);

        int num2 = profileService.addProfile(profileDto);
        assertThat(num).isGreaterThanOrEqualTo(1);

        Profile profile2 = profileService.getProfile(num2);

        log.debug("todo 우선순위 = {}{}", profile2.getTaskLevel().getName(), profile2.getSeq());
        Assertions.assertThat(profile2.getTaskLevel()).isEqualTo(TaskLevel.A);
        assertThat(profile2.getSeq()).isEqualTo(1);
        Assertions.assertThat(profile2.getStatus()).isEqualTo(ProgressStatus.ASSIGN);
    }

    @DisplayName("A0 다음으로 추가 된 해야할일의 우선순위")
    @Test
    public void addTodoAllocatedPriority2Test() {
        LocalDate date = LocalDate.of(2022, 5, 1);
        MemberDto assignee = new MemberDto(42, "김희정");
        MemberDto reporter = new MemberDto(43, "김희정");

        ProfileDto profileDto = new ProfileDto();
        profileDto.setDate(date);
        profileDto.setAssignee(assignee);
        profileDto.setReporter(reporter);
        profileDto.setTask("해야할 일");
        profileDto.setDescription("간략설명");
        profileDto.setTaskLevel(TaskLevel.D);
        profileDto.setSeq(3);
        profileDto.setStatus(ProgressStatus.ASSIGN);

        int num = profileService.addProfile(profileDto);
        assertThat(num).isGreaterThanOrEqualTo(1);

        Profile profile = profileService.getProfile(num);

        log.debug("todo 우선순위 = {}{}", profile.getTaskLevel().getName(), profile.getSeq());
        Assertions.assertThat(profile.getTaskLevel()).isEqualTo(TaskLevel.A);
        assertThat(profile.getSeq()).isEqualTo(0);
        Assertions.assertThat(profile.getStatus()).isEqualTo(ProgressStatus.ASSIGN);

        profileDto = new ProfileDto();
        profileDto.setDate(date);
        profileDto.setAssignee(assignee);
        profileDto.setReporter(reporter);
        profileDto.setTask("해야할 일");
        profileDto.setDescription("간략설명");

        int num2 = profileService.addProfile(profileDto);
        assertThat(num).isGreaterThanOrEqualTo(1);

        Profile profile2 = profileService.getProfile(num2);

        log.debug("todo 우선순위 = {}{}", profile2.getTaskLevel().getName(), profile2.getSeq());
        Assertions.assertThat(profile2.getTaskLevel()).isEqualTo(TaskLevel.A);
        assertThat(profile2.getSeq()).isEqualTo(1);
        Assertions.assertThat(profile2.getStatus()).isEqualTo(ProgressStatus.OPEN);
    }

    @DisplayName("A0 다음으로 추가 된 다른 날짜의 해야할일의 우선순위")
    @Test
    public void addTodoAllocatedPriorityNotSameDateTest() {
        MemberDto assignee = new MemberDto(44, "김희정");

        ProfileDto profileDto = new ProfileDto();
        profileDto.setDate(LocalDate.of(2022, 5, 1));
        profileDto.setAssignee(assignee);
        profileDto.setReporter(new MemberDto(45, "김희정"));
        profileDto.setTask("해야할 일");
        profileDto.setDescription("간략설명");
        profileDto.setTaskLevel(TaskLevel.D);
        profileDto.setSeq(3);
        profileDto.setStatus(ProgressStatus.ASSIGN);

        int num = profileService.addProfile(profileDto);
        assertThat(num).isGreaterThanOrEqualTo(1);

        Profile profile = profileService.getProfile(num);

        log.debug("todo 우선순위 = {}{}", profile.getTaskLevel().getName(), profile.getSeq());
        Assertions.assertThat(profile.getTaskLevel()).isEqualTo(TaskLevel.A);
        assertThat(profile.getSeq()).isEqualTo(0);
        Assertions.assertThat(profile.getStatus()).isEqualTo(ProgressStatus.ASSIGN);

        profileDto = new ProfileDto();
        profileDto.setDate(LocalDate.of(2022, 5, 2));
        profileDto.setAssignee(assignee);
        profileDto.setTask("해야할 일");
        profileDto.setDescription("간략설명");

        int num2 = profileService.addProfile(profileDto);
        assertThat(num).isGreaterThanOrEqualTo(1);

        Profile profile2 = profileService.getProfile(num2);

        log.debug("todo 우선순위 = {}{}", profile2.getTaskLevel().getName(), profile2.getSeq());
        Assertions.assertThat(profile2.getTaskLevel()).isEqualTo(TaskLevel.B);
        assertThat(profile2.getSeq()).isEqualTo(0);
        Assertions.assertThat(profile2.getStatus()).isEqualTo(ProgressStatus.OPEN);
    }

    @DisplayName("todo 추가 시 날짜순 입력")
    @Test
    public void addTodoForDateTest() {
        // 날짜순 정렬 뒤 비교하기 위해 LocalDate 리스트를 만들어 정렬 후 결과와 순서가 일치하는지 확인
        ArrayList<LocalDate> dates = new ArrayList<>();
        dates.add(LocalDate.of(2022, 5, 1));
        dates.add(LocalDate.of(2022, 5, 1));
        dates.add(LocalDate.of(2022, 5, 2));
        dates.add(LocalDate.of(2022, 5, 2));
        dates.add(LocalDate.of(2022, 5, 3));
        dates.add(LocalDate.of(2022, 5, 4));
        dates.add(LocalDate.of(2022, 5, 5)); // index = 6

        // 날짜만 변경하고 나머지 정보는 동일하게 입력
        MemberDto assignee = new MemberDto(48, "김희정");
        int seq = 3;
        String task = "task";
        String description = "description";

        ProfileDto profileDto = null;
        Profile profile = null;
        for (int index = 0; index < dates.size(); index++) {
            profileDto = new ProfileDto();
            profileDto.setDate(dates.get(index));
            profileDto.setAssignee(assignee);
            profileDto.setTaskLevel(TaskLevel.S);
            profileDto.setTask(task);
            profileDto.setDescription(description);

            int num = profileService.addProfile(profileDto);
            profile = profileService.getProfile(num);
            log.debug("num = {} / priority = {}({})", profile.getNum(), profile.getTaskLevel().getName(), profile.getSeq());
        }

        // 입력 시 정렬되어 들어가기때문에 순서가 동일한지 확인
        ArrayList<Profile> profiles = profileService.getProfiles(null); // 해야할일 목록 조회
        for (int index = 0; index < dates.size(); index++) {
            log.debug("{} : {} / {} / {}({})", index, profiles.get(index).getDate().toString(), dates.get(index), profiles.get(index).getTaskLevel().getName(), profiles.get(index).getSeq());
            assertThat(profiles.get(index).getDate()).isEqualTo(dates.get(index));
        }
    }

    @DisplayName("todo 추가 시 담당자순 입력")
    @Test
    public void addTodoForAssigneeTest() {
        // 담당자순 정렬을 위해 Profile 리스트를 만들어 정렬 후 순서가 일치하는지 결과 비교
        ArrayList<MemberDto> members = new ArrayList<>();
        members.add(new MemberDto(50, "김희정"));
        members.add(new MemberDto(50, "김희정"));
        members.add(new MemberDto(51, "김희정"));
        members.add(new MemberDto(52, "임시완"));
        members.add(new MemberDto(52, "임시완"));
        members.add(new MemberDto(53, "임성욱")); // index = 5

        // 담당자 외 정보는 동일하게 입력
        LocalDate date = LocalDate.of(2022, 6, 1);
        String task = "task";
        String description = "description";
        
        int[] todoSequences = {1, 3, 2, 5, 4, 0}; // 임의순서로 해야할일을 추가
        ProfileDto profileDto = null;
        Profile profile = null;
        for (int index = 0; index < todoSequences.length; index++) {
            profileDto = new ProfileDto();
            profileDto.setDate(date);
            profileDto.setAssignee(members.get(todoSequences[index]));
            profileDto.setTask(task);
            profileDto.setDescription(description);

            int num = profileService.addProfile(profileDto);
            profile = profileService.getProfile(num);
            log.debug("num = {} / priority = {}({})", profile.getNum(), profile.getTaskLevel().getName(), profile.getSeq());
        }

        ArrayList<Profile> profiles = profileService.getProfiles(null);
        for (int index = 0; index < profiles.size(); index++) {
            log.debug("{} : {}({}) / {}({}) / {}({})", index, profiles.get(index).getAssignee().getName(), profiles.get(index).getAssignee().getNumber()
                    , members.get(index).getName(), members.get(index).getNum(), profiles.get(index).getTaskLevel().getName(), profiles.get(index).getSeq());
            assertThat(profiles.get(index).getAssignee().getName()).isEqualTo(members.get(index).getName());
            assertThat(profiles.get(index).getAssignee().getNumber()).isEqualTo(members.get(index).getNum());
        }
    }

    @DisplayName("todo 추가 시 중요도순으로 입력")
    @Test
    public void addTodosForTaskLevelTest() {
        // 중요도순 정렬을 확인하기 위해 중요도리스트를 만들어 정렬 후 비교
        ArrayList<TaskLevel> taskLevels = new ArrayList<>();
        taskLevels.add(TaskLevel.S);
        taskLevels.add(TaskLevel.S);
        taskLevels.add(TaskLevel.A);
        taskLevels.add(TaskLevel.A);
        taskLevels.add(TaskLevel.B);
        taskLevels.add(TaskLevel.C);
        taskLevels.add(TaskLevel.C);
        taskLevels.add(TaskLevel.D); // index = 7

        // 우선순위 외 정보는 동일하게 입력
        LocalDate date = LocalDate.of(2022, 6, 1);
        MemberDto assignee = new MemberDto(55, "김희정");
        String task = "task";
        String description = "description";

        int[] todoSequences = {1, 2, 4, 0, 3, 7, 6, 5}; // 임의순서로 해야할일을 추가
        ProfileDto profileDto = null;
        Profile profile = null;
        for (int index = 0; index < todoSequences.length; index++) {
            profileDto = new ProfileDto();
            profileDto.setDate(date);
            profileDto.setAssignee(assignee);
            profileDto.setTaskLevel(taskLevels.get(todoSequences[index]));
            profileDto.setTask(task);
            profileDto.setDescription(description);

            int num = profileService.addProfile(profileDto);
            profile = profileService.getProfile(num);
            log.debug("num = {} / priority = {}({})", profile.getNum(), profile.getTaskLevel().getName(), profile.getSeq());
        }

        ArrayList<Profile> profiles = profileService.getProfiles(null);
        for (int index = 0; index < profiles.size(); index++) {
            log.debug("{} : {}({}) / {}", index, profiles.get(index).getTaskLevel().getName(), profiles.get(index).getSeq(), taskLevels.get(index).getName());
            Assertions.assertThat(profiles.get(index).getTaskLevel()).isEqualTo(taskLevels.get(index));
        }
    }

    @DisplayName("todo 추가 시 순서")
    @Test
    public void addTodosForSequenceTest() {
        // 순서 외 정보는 동일하게 입력
        LocalDate date = LocalDate.of(2022, 6, 1);
        MemberDto assignee = new MemberDto(56, "김희정");
        String task = "task";
        String description = "description";

        ProfileDto profileDto = null;
        Profile profile = null;
        for (int index = 0; index < 10; index++) {
            profileDto = new ProfileDto();
            profileDto.setDate(date);
            profileDto.setAssignee(assignee);
            profileDto.setTask(task);
            profileDto.setDescription(description);

            int num = profileService.addProfile(profileDto);
            profile = profileService.getProfile(num);
            log.debug("num = {} / priority = {}({})", profile.getNum(), profile.getTaskLevel().getName(), profile.getSeq());
        }

        ArrayList<Profile> profiles = profileService.getProfiles(null);
        for (int index = 0; index < profiles.size(); index++) {
            log.debug("{} : {}({})", index, profiles.get(index).getTaskLevel().getName(), profiles.get(index).getSeq());
            Assertions.assertThat(profiles.get(index).getTaskLevel()).isEqualTo(TaskLevel.B);
            assertThat(profiles.get(index).getSeq()).isEqualTo(index);
        }
    }

    @DisplayName("todo 삭제 시 순서 변경")
    @Test
    public void removeTodosForSequenceTest() {
        LocalDate date = LocalDate.of(2022, 6, 1);
        Member assignee = Member.of(57, "김희정");
        int num1 = 1, seq1 = 0;
        int num2 = 2, seq2 = 1;

        ArrayList<Profile> profiles = profileService.getProfiles(null);
        profiles.add(Profile.of(num1, date, assignee, TaskLevel.B, seq1, "", ProgressStatus.OPEN, ""));
        profiles.add(Profile.of(num2, date, assignee, TaskLevel.B, seq2, "", ProgressStatus.OPEN, ""));

        // addTodo 내부에서 부여한 우선순위 비교
        Profile firstProfile = profileService.getProfile(num1);
        Profile secondProfile = profileService.getProfile(num2);

        log.debug("firstTodo 우선순위 ={} {}{}", firstProfile.getNum(), firstProfile.getTaskLevel().getName(), firstProfile.getSeq());
        log.debug("secondTodo 우선순위 ={} {}{}", secondProfile.getNum(), secondProfile.getTaskLevel().getName(), secondProfile.getSeq());
        Assertions.assertThat(secondProfile.getTaskLevel()).isEqualTo(firstProfile.getTaskLevel());
        assertThat(secondProfile.getSeq()).isGreaterThan(seq1);
        
        assertThat(profileService.removeProfile(1)).isTrue(); // firstTodo 삭제
        log.debug("secondTodo 우선순위 = {}{}", secondProfile.getTaskLevel().getName(), secondProfile.getSeq());
        Assertions.assertThat(secondProfile.getTaskLevel()).isEqualTo(secondProfile.getTaskLevel());
        assertThat(secondProfile.getSeq()).isLessThan(seq2);
    }

    @DisplayName("todo 삭제 시 다른 중요도의 순서 유지")
    @Test
    public void removeTodosNotSameTaskLevelTest() {
        LocalDate date = LocalDate.of(2022, 6, 1);
        MemberDto assignee = new MemberDto(60, "김희정");

        ProfileDto profileDto = new ProfileDto();
        profileDto.setDate(date);
        profileDto.setAssignee(assignee);
        profileDto.setTask("해야할 일");
        profileDto.setDescription("간략설명");

        int num = profileService.addProfile(profileDto);
        assertThat(num).isGreaterThanOrEqualTo(1);

        profileDto = new ProfileDto();
        profileDto.setDate(date);
        profileDto.setAssignee(assignee);
        profileDto.setTask("해야할 일2");
        profileDto.setDescription("간략설명2");
        profileDto.setTaskLevel(TaskLevel.D);

        int nextNum = profileService.addProfile(profileDto);
        assertThat(nextNum).isGreaterThan(num); // 먼저 추가 된 해야할일의 번호보다 높다

        // addTodo 내부에서 부여한 우선순위 비교
        Profile firstProfile = profileService.getProfile(num);
        Profile secondProfile = profileService.getProfile(nextNum);

        log.debug("firstTodo 우선순위 = {}{}", firstProfile.getTaskLevel().getName(), firstProfile.getSeq());
        log.debug("secondTodo 우선순위 = {}{}", secondProfile.getTaskLevel().getName(), secondProfile.getSeq());
        Assertions.assertThat(firstProfile.getTaskLevel()).isEqualTo(TaskLevel.B);
        assertThat(firstProfile.getSeq()).isEqualTo(0);
        Assertions.assertThat(secondProfile.getTaskLevel()).isEqualTo(profileDto.getTaskLevel());
        assertThat(secondProfile.getSeq()).isEqualTo(0);

        assertThat(profileService.removeProfile(num)).isTrue(); // firstTodo 삭제
        Profile renewSecondProfile = profileService.getProfile(nextNum); // 해야할일의 번호는 변경되지않음
        log.debug("renewSecondTodo 우선순위 = {}{}", renewSecondProfile.getTaskLevel().getName(), renewSecondProfile.getSeq());
        Assertions.assertThat(renewSecondProfile.getTaskLevel()).isEqualTo(secondProfile.getTaskLevel());
        assertThat(renewSecondProfile.getSeq()).isEqualTo(secondProfile.getSeq());
    }

    @DisplayName("특정 날짜, 특정 담당자의 해야할일 조회")
    @Test
    public void getTodosOfDateTest() {
        LocalDate date = LocalDate.of(2022, 5, 1);
        Member assignee = Member.of(0, "김희정");
        
        ArrayList<Profile> profiles = profileService.getProfiles(null);
        profiles.add(Profile.of(1, date, assignee, TaskLevel.B, 0, "", ProgressStatus.OPEN, ""));
        profiles.add(Profile.of(2, date, assignee, TaskLevel.B, 1, "", ProgressStatus.OPEN, ""));
        profiles.add(Profile.of(3, date, Member.of(1, "임성욱"), TaskLevel.B, 0, "", ProgressStatus.OPEN, ""));
        profiles.add(Profile.of(4, LocalDate.of(2022, 5, 10), assignee, TaskLevel.B, 0, "", ProgressStatus.OPEN, ""));
        profiles.add(Profile.of(5, LocalDate.of(2022, 5, 10), assignee, TaskLevel.B, 1, "", ProgressStatus.OPEN, ""));
        profiles.add(Profile.of(6, LocalDate.of(2022, 5, 10), Member.of(1, "임성욱"), TaskLevel.B, 0, "", ProgressStatus.OPEN, ""));

        // 2022-05-01 날짜의 해야할일 조회
        ProfileDto profileDto = new ProfileDto(date, null, null, null);
        ArrayList<Profile> dateProfiles = profileService.getProfiles(profileDto);
        assertThat(dateProfiles).isNotNull();
        assertThat(dateProfiles.size()).isEqualTo(3);
        assertThat(dateProfiles.get(0).getNum()).isEqualTo(1);
        assertThat(dateProfiles.get(1).getNum()).isEqualTo(2);
        assertThat(dateProfiles.get(2).getNum()).isEqualTo(3);
        
        // 김희정(0) 담당자의 해야할일 조회
        MemberDto memberDto = new MemberDto(assignee.getNumber(), assignee.getName());
        profileDto = new ProfileDto(null, memberDto, null, null);
        dateProfiles = profileService.getProfiles(profileDto);
        assertThat(dateProfiles).isNotNull();
        assertThat(dateProfiles.size()).isEqualTo(4);
        assertThat(dateProfiles.get(0).getNum()).isEqualTo(1);
        assertThat(dateProfiles.get(1).getNum()).isEqualTo(2);
        assertThat(dateProfiles.get(2).getNum()).isEqualTo(4);
        assertThat(dateProfiles.get(3).getNum()).isEqualTo(5);

        // 2022-05-01 + 김희정(0) 날짜와 담당자의 해야할일 조회
        profileDto = new ProfileDto(date, memberDto, null, null);
        dateProfiles = profileService.getProfiles(profileDto);
        assertThat(dateProfiles).isNotNull();
        assertThat(dateProfiles.size()).isEqualTo(2);
        assertThat(dateProfiles.get(0).getNum()).isEqualTo(1);
        assertThat(dateProfiles.get(1).getNum()).isEqualTo(2);
    }

    @DisplayName("todo 위임")
    @Test
    public void allocateAssigneeTest() {
        LocalDate date = LocalDate.of(2022, 5, 1);
        Member assignee = Member.of(0, "김희정");
        MemberDto allocatedAssignee = new MemberDto(1, "김희정");
        int num1 = 2;
        int num2 = 3;
        int num3 = 4;

        ArrayList<Profile> profiles = profileService.getProfiles(null);
        profiles.add(Profile.of(1, date, assignee, TaskLevel.B, 0, "", ProgressStatus.OPEN, ""));
        profiles.add(Profile.of(num1, date, assignee, TaskLevel.B, 1, "", ProgressStatus.OPEN, ""));
        profiles.add(Profile.of(num2, date, assignee, TaskLevel.B, 2, "", ProgressStatus.OPEN, ""));
        profiles.add(Profile.of(num3, date, assignee, TaskLevel.C, 0, "", ProgressStatus.OPEN, ""));

        // 해야할일을 위임
        ProfileDto profileDto = new ProfileDto();
        profileDto.setNum(num1);

        profileDto.setAssignee(allocatedAssignee);
        profileService.allocateAssignee(profileDto);
        
        Profile profile = profileService.getProfile(num1); // 위임한 해야할일을 조회
        assertThat(profile.getAssignee().getNumber()).isEqualTo(allocatedAssignee.getNum()); // 담당자
        assertThat(profile.getReporter().getNumber()).isEqualTo(assignee.getNumber()); // 위임자
        Assertions.assertThat(profile.getStatus()).isEqualTo(ProgressStatus.ASSIGN); // "위임"
        Assertions.assertThat(profile.getTaskLevel()).isEqualTo(TaskLevel.A);
        assertThat(profile.getSeq()).isEqualTo(0);
        assertThat(profile.getLog().get(Profile.KEY_TASK_LEVEL)).isEqualTo(TaskLevel.B);
        assertThat(profile.getLog().get(Profile.KEY_SEQ)).isEqualTo(1);

        // 위임 후 다음 순위의 해야할일의 순서 변경 확인
        profile = profileService.getProfile(num2);
        Assertions.assertThat(profile.getTaskLevel()).isEqualTo(TaskLevel.B);
        assertThat(profile.getSeq()).isEqualTo(1); // 순서 -1

        // 위임 후 중요도가 다른 해야할일의 순서 변경 없음
        profile = profileService.getProfile(num3);
        Assertions.assertThat(profile.getTaskLevel()).isEqualTo(TaskLevel.C);
        assertThat(profile.getSeq()).isEqualTo(0);
    }

    @DisplayName("위임 취소")
    @Test
    public void cancelAllocatedTodoTest() {
        LocalDate date = LocalDate.of(2022, 5, 1);
        Member assignee = Member.of(0, "김희정");
        Member reporter = Member.of(1, "김희정");
        TaskLevel taskLevel = TaskLevel.C;
        int seq = 0;
        HashMap<String, Object> log = new HashMap<>();
        log.put(Profile.KEY_TASK_LEVEL, taskLevel);
        log.put(Profile.KEY_SEQ, 0);
        int num1 = 1;
        int num2 = 2, seq2 = 1;
        int num3 = 3, seq3 = 2;

        ArrayList<Profile> profiles = profileService.getProfiles(null);
        profiles.add(Profile.of(num1, date, assignee, TaskLevel.A, 0, ""
                , ProgressStatus.ASSIGN, "", reporter, log));
        profiles.add(Profile.of(num2, date, assignee, TaskLevel.A, 1, "", ProgressStatus.OPEN, ""));
        profiles.add(Profile.of(num3, date, assignee, TaskLevel.A, 2, "", ProgressStatus.OPEN, ""));

        try {
            profileService.cancelAllocatedProfile(0);
            fail("번호와 일치하는 해야할일이 없어 HandledException이 발생해야 함");
        } catch (HandledException e) {
            profileService.cancelAllocatedProfile(num1);

            Profile profile = profileService.getProfile(num1); // 위임취소한 해야할일 조회
            assertThat(profile.getStatus()).isEqualTo(ProgressStatus.CANCEL);
            assertThat(profile.getAssignee().getNumber()).isEqualTo(reporter.getNumber()); // 위임자가 담당자로 변경
            assertThat(profile.getTaskLevel()).isEqualTo(taskLevel);
            assertThat(profile.getSeq()).isEqualTo(seq);

            // 후순위 해야할일의 순서 재부여
            profile = profileService.getProfile(num2);
            assertThat(profile.getSeq()).isEqualTo(seq2 - 1);

            profile = profileService.getProfile(num3);
            assertThat(profile.getSeq()).isEqualTo(seq3 - 1);
        }
    }

    @DisplayName("우선순위 변경 - 선순위로 입력")
    @Test
    public void addTodoForSequence1Test() {
        LocalDate date = LocalDate.of(2022, 5, 1);
        Member assignee = Member.of(0, "김희정");
        int num1 = 2, seq1 = 1;
        int num2 = 3, seq2 = 2;
        int num3 = 4;

        ArrayList<Profile> profiles = profileService.getProfiles(null);
        profiles.add(Profile.of(1, date, assignee, TaskLevel.B, 0, "", ProgressStatus.OPEN, ""));
        profiles.add(Profile.of(num1, date, assignee, TaskLevel.B, 1, "", ProgressStatus.OPEN, ""));
        profiles.add(Profile.of(num2, date, assignee, TaskLevel.B, 2, "", ProgressStatus.OPEN, ""));
        profiles.add(Profile.of(num3, date, assignee, TaskLevel.C, 0, "", ProgressStatus.OPEN, ""));

        // 추가 할 우선순위
        ProfileDto profileDto = new ProfileDto();
        profileDto.setNum(num3);
        profileDto.setDate(date);
        profileDto.setAssignee(new MemberDto(assignee.getNumber(), assignee.getName()));
        profileDto.setTaskLevel(TaskLevel.B);
        profileDto.setSeq(1); // 순서 지정
        profileDto.setTask("task");
        profileDto.setDescription("description");
        profileDto.setStatus(ProgressStatus.OPEN);

        int num = profileService.addProfile(profileDto);
        Profile profile = profileService.getProfile(num);
        assertThat(num).isEqualTo(num3); // 번호는 변경되지 않음
        assertThat(profile.getTaskLevel()).isEqualTo(profileDto.getTaskLevel());
        assertThat(profile.getSeq()).isEqualTo(profileDto.getSeq());

        profile = profileService.getProfile(num1);
        assertThat(profile.getTaskLevel()).isEqualTo(TaskLevel.B);
        assertThat(profile.getSeq()).isEqualTo(seq1 + 1);

        profile = profileService.getProfile(num2);
        assertThat(profile.getTaskLevel()).isEqualTo(TaskLevel.B);
        assertThat(profile.getSeq()).isEqualTo(seq2 + 1);
    }

    @DisplayName("우선순위 변경 - 동일 중요도 내 최우선순으로 입력")
    @Test
    public void addTodoForSequence2Test() {
        LocalDate date = LocalDate.of(2022, 5, 1);
        Member assignee = Member.of(0, "김희정");
        int num1 = 1, seq1 = 0;
        int num2 = 2, seq2 = 1;
        int num3 = 3, seq3 = 2;
        int num4 = 4;

        ArrayList<Profile> profiles = profileService.getProfiles(null);
        profiles.add(Profile.of(num1, date, assignee, TaskLevel.B, seq1, "", ProgressStatus.OPEN, ""));
        profiles.add(Profile.of(num2, date, assignee, TaskLevel.B, seq2, "", ProgressStatus.OPEN, ""));
        profiles.add(Profile.of(num3, date, assignee, TaskLevel.B, seq3, "", ProgressStatus.OPEN, ""));

        // 추가 할 우선순위
        ProfileDto profileDto = new ProfileDto();
        profileDto.setNum(num4);
        profileDto.setDate(date);
        profileDto.setAssignee(new MemberDto(assignee.getNumber(), assignee.getName()));
        profileDto.setTaskLevel(TaskLevel.B);
        profileDto.setSeq(0); // 순서 지정
        profileDto.setTask("task");
        profileDto.setDescription("description");
        profileDto.setStatus(ProgressStatus.OPEN);

        int num = profileService.addProfile(profileDto);

        ArrayList<Profile> a = profileService.getProfiles(null);
        for (int index = 0; index < a.size(); index++) {
            log.debug("num: {} / {}{}", a.get(index).getNum(), a.get(index).getTaskLevel().getName(), a.get(index).getSeq());
        }

        Profile profile = profileService.getProfile(num);
        assertThat(num).isEqualTo(num4); // 번호는 변경되지 않음
        assertThat(profile.getTaskLevel()).isEqualTo(profileDto.getTaskLevel());
        assertThat(profile.getSeq()).isEqualTo(profileDto.getSeq());

        profile = profileService.getProfile(num1);
        assertThat(profile.getSeq()).isEqualTo(seq1 + 1);

        profile = profileService.getProfile(num2);
        assertThat(profile.getSeq()).isEqualTo(seq2 + 1);

        profile = profileService.getProfile(num3);
        assertThat(profile.getSeq()).isEqualTo(seq3 + 1);
    }
    
    @DisplayName("해야할일 기본정보 변경")
    @Test
    public void updateTodoTest() {
        int num = 1;
        String task = "인사";
        String description = "김희정입니다.";
        ProgressStatus status = ProgressStatus.COMPLETE;
        ArrayList<Profile> profiles = profileService.getProfiles(null);
        profiles.add(Profile.of(num, LocalDate.of(2022, 5, 1), Member.of(0, "김희정"), TaskLevel.B, 0, "", ProgressStatus.OPEN, ""));

        ProfileDto profileDto = new ProfileDto();
        profileDto.setNum(num);
        profileDto.setTask(task);
        profileDto.setDescription(description);
        profileDto.setStatus(status);
        profileService.updateProfile(profileDto);

        Profile profile = profileService.getProfile(num);
        assertThat(profile.getTask()).isEqualTo(task);
        assertThat(profile.getDescription()).isEqualTo(description);
        assertThat(profile.getStatus()).isEqualTo(status);
    }

    @DisplayName("해야할일 우선순위 변경")
    @Test
    public void updateTodoForSeq1Test() {
        LocalDate date = LocalDate.of(2022, 5, 1);
        Member assignee = Member.of(0, "김희정");
        int num1 = 1, seq1 = 0;
        int num2 = 2, seq2 = 1;
        int num3 = 3, seq3 = 2;

        ArrayList<Profile> profiles = profileService.getProfiles(null);
        profiles.add(Profile.of(num1, date, assignee, TaskLevel.B, seq1, "", ProgressStatus.OPEN, ""));
        profiles.add(Profile.of(num2, date, assignee, TaskLevel.B, seq2, "", ProgressStatus.OPEN, ""));
        profiles.add(Profile.of(num3, date, assignee, TaskLevel.B, seq3, "", ProgressStatus.OPEN, ""));

        // seq=2인 업무를 최우선으로 변경
        ProfileDto profileDto = new ProfileDto();
        profileDto.setNum(num3);
        profileDto.setSeq(0);

        profileService.updateProfile(profileDto);

        Profile profile = profileService.getProfile(num3);
        assertThat(profile.getSeq()).isEqualTo(0);

        profile = profileService.getProfile(num1);
        assertThat(profile.getSeq()).isEqualTo(seq1 + 1);

        profile = profileService.getProfile(num2);
        assertThat(profile.getSeq()).isEqualTo(seq2 + 1);
    }

    @DisplayName("해야할일 우선순위 변경")
    @Test
    public void updateTodoForSeq2Test() {
        LocalDate date = LocalDate.of(2022, 5, 1);
        Member assignee = Member.of(0, "김희정");
        int num1 = 1, seq1 = 0;
        int num2 = 2, seq2 = 1;
        int num3 = 3;

        ArrayList<Profile> profiles = profileService.getProfiles(null);
        profiles.add(Profile.of(num1, date, assignee, TaskLevel.B, seq1, "", ProgressStatus.OPEN, ""));
        profiles.add(Profile.of(num2, date, assignee, TaskLevel.B, seq2, "", ProgressStatus.OPEN, ""));
        profiles.add(Profile.of(num3, date, assignee, TaskLevel.C, 2, "", ProgressStatus.OPEN, ""));

        // seq=2인 업무를 최우선으로 변경
        ProfileDto profileDto = new ProfileDto();
        profileDto.setNum(num3);
        profileDto.setTaskLevel(TaskLevel.B);

        profileService.updateProfile(profileDto);

        Profile profile = profileService.getProfile(num3);
        assertThat(profile.getSeq()).isGreaterThan(seq2); // B급의 최하위순

        profile = profileService.getProfile(num1);
        assertThat(profile.getSeq()).isEqualTo(seq1);

        profile = profileService.getProfile(num2);
        assertThat(profile.getSeq()).isEqualTo(seq2);

        ArrayList<Profile> a = profileService.getProfiles(null);
        for (int index = 0; index < a.size(); index++) {
            log.debug("num: {} / {}{}", a.get(index).getNum(), a.get(index).getTaskLevel().getName(), a.get(index).getSeq());
        }
    }
}
