package com.amore.task.service;

import com.amore.task.common.HandledException;
import com.amore.task.model.domain.Member;
import com.amore.task.model.domain.Profile;
import com.amore.task.model.dto.MemberDto;
import com.amore.task.model.dto.ProfileDto;
import com.amore.task.model.enums.ProgressStatus;
import com.amore.task.model.enums.ResponseStatus;
import com.amore.task.model.enums.TaskLevel;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ProfileService {
    /** 해야할일 목록 */
    private ArrayList<Profile> profiles;

    /** 해야할일 번호 */
    private int number;

    public ProfileService() {
        this.number = 0;
        this.profiles = new ArrayList<Profile>();
    }

    /**
     * 신규 번호를 발급하여 해야할일을 구분
     * @return 해야할일 번호
     */
    @Synchronized
    public int getNextNumber() {
        this.number++;
        return this.number;
    }

    /**
     * 해야할일의 인덱스 찾기
     * @param profileDto
     * @return 인덱스
     */
    public int getPriority(ProfileDto profileDto) {
        boolean isValidIndex = false;
        int index = 0;

        for (; index < this.profiles.size(); index++) {
            // 1. 날짜 비교
            if (profileDto.getDate().isBefore(this.profiles.get(index).getDate())) {
                // 해당 인덱스의 날짜보다 이전일 경우, 인덱스로 추가
                break;
            } else if (profileDto.getDate().isAfter(this.profiles.get(index).getDate())) {
                // 다음 인덱스 확인
                continue;
            } else {
                // 2. 날짜가 동일한 경우 담당자 비교
                if (profileDto.getAssignee().getNum() < this.profiles.get(index).getAssignee().getNumber()) {
                    // 해당 인덱스의 담당자 번호보다 낮은 경우, 해당 인덱스로 추가
                    break;
                } else if (profileDto.getAssignee().getNum() > this.profiles.get(index).getAssignee().getNumber()) {
                    // 다음 인덱스 확인
                    continue;
                } else {
                    // 3. 담당자가 동일한 경우 최하순위 바로 아래 순위를 검색
                    while (index < this.profiles.size()) {
                        if (null != profileDto.getTaskLevel()) {
                            if (profileDto.getTaskLevel().getCode() < this.profiles.get(index).getTaskLevel().getCode()) {
                                // 다음 인덱스가 하위의 중요도인 경우 해당 인덱스로 추가
                                isValidIndex = true;
                                break;
                            } else if (profileDto.getTaskLevel().getCode() > this.profiles.get(index).getTaskLevel().getCode()) {
                                // 중요도A보다 높은 중요도의 경우 다음 인덱스 확인
                            } else {
                                // 동일 중요도 내 순서
                                if ((index + 1) < this.profiles.size()) {
                                    // 다음 인덱스와 중요도가 다름
                                    if (this.profiles.get(index).getTaskLevel().getCode() < this.profiles.get(index + 1).getTaskLevel().getCode()) {
                                        isValidIndex = true;
                                        break;
                                    } else {
                                        // 순서 비교
                                        if (profileDto.getSeq() <= this.profiles.get(index).getSeq()) {
                                            // 다음 인덱스 보다 순서가 낮거나 동일하면 해당 인덱스로 입력
                                            isValidIndex = true;
//                                            index++;
                                            break;
                                        }
                                    }
                                } else { // 다음 인덱스 없음
                                    isValidIndex = true;
                                    index++;
                                    break;
                                }
                            }

                        }

                        // 담당자가 바뀌면 해당 인덱스로 추가
                        if ((index + 1) < this.profiles.size()) {
                            if (profileDto.getAssignee().getNum() != this.profiles.get(index + 1).getAssignee().getNumber()) {
                                isValidIndex = true;
                                index++;
                                break;
                            }
                        } else {
                            isValidIndex = true;
                            index++;
                            break;
                        }

                        index++;
                    } // end of while

                    // 입력할 인덱스를 찾은 경우 반복문을 종료
                    if (isValidIndex) {
                        break;
                    }
                }
            }
        }

        return index;
    }

    @Synchronized
    public int addProfile(ProfileDto profileDto) {
        int index = 0; // 추가 될 위치
        TaskLevel taskLevel = null;
        int sequence = 0; // 동일 날짜의 최초 추가 시 초기값

        // 중요도 지정(지정하지 않으면 최하위순으로 부여)
        if (ProgressStatus.ASSIGN.equals(profileDto.getStatus())) {
            // 해야할일을 위임받은 경우 우선순위는 A 중 최하순위
            taskLevel = TaskLevel.A;
            profileDto.setTaskLevel(taskLevel);
        } else if (null != profileDto.getTaskLevel()) {
            // 해야할일의 중요도가 지정되어있을 경우(변경) 동일 중요도 내 최하순위
            taskLevel = profileDto.getTaskLevel();
        }

        // 해야할일 변경 시 번호를 새로 부여하지않음
        int number = (0 == profileDto.getNum()) ? this.getNextNumber() : profileDto.getNum();

        if (0 < this.profiles.size()) { // 해야할일이 존재할 경우
            index = getPriority(profileDto); // 입력 할 위치(인덱스)
        }

        // 해야할일이 추가되어야하는 위치(인덱스)에 맞는 중요도와 순서를 부여
        if (null == taskLevel) {
            // 위임받거나 중요도가 지정되지않음 아닌 경우 앞의 인덱스 날짜와 담당자가 동일하면 중요도와 동일하게 부여
            // 담당자가 다른 경우 초기값 B0 부여
            if ((0 < index) && (profileDto.getDate().isEqual(this.profiles.get(index - 1).getDate()))
                    && (profileDto.getAssignee().getNum() == this.profiles.get(index - 1).getAssignee().getNumber())) {
                taskLevel = this.profiles.get(index - 1).getTaskLevel();
                sequence = this.profiles.get(index - 1).getSeq() + 1; // 앞의 인덱스 위치의 순서 +1
            } else {
                taskLevel = TaskLevel.B; // 동일 날짜의 최초 추가 시 초기값
                // sequence = 0; 초기값과 동일
            }
        } else {
            // 동일 중요도 내 순서
            if (0 < this.profiles.size()) {
                if ((0 < index) && (taskLevel.equals(this.profiles.get(index - 1).getTaskLevel()))
                        && (profileDto.getAssignee().getNum() == this.profiles.get(index - 1).getAssignee().getNumber())) {
                    // 동일 중요도 내 다음 순서로 우선순위 입력
                    sequence = this.profiles.get(index - 1).getSeq() + 1;
                }

                // 이후의 해야할일의 우선순위 재부여
                int targetIndex = index;
                while (targetIndex < this.profiles.size()) {
                    // 동일 담당자의 동일 중요도인 경우, 순서를 +1 해주어야함
                    if ((profileDto.getAssignee().getNum() == this.profiles.get(targetIndex).getAssignee().getNumber())
                            && (taskLevel.equals(this.profiles.get(targetIndex).getTaskLevel()))) {
                        this.profiles.get(targetIndex).addSequence();
                    } else {
                        break;
                    }
                    targetIndex++;
                }
            } else {
                // sequence = 0; 초기값과 동일
            }
        }

        if (null == taskLevel) {
            taskLevel = TaskLevel.B; // 동일 날짜의 최초 추가 시 초기값
        }

        log.debug("[add] index = {} / taskLevel = {} / seq = {} / num = {}", index, taskLevel.getName(), sequence, number);
        if (ProgressStatus.ASSIGN.equals(profileDto.getStatus())) {
            this.profiles.add(index, Profile.of(number
                    , profileDto.getDate()
                    , Member.of(profileDto.getAssignee().getNum(), profileDto.getAssignee().getName())
                    , taskLevel
                    , sequence
                    , profileDto.getTask()
                    , ProgressStatus.ASSIGN
                    , profileDto.getDescription()
                    , Member.of(profileDto.getReporter().getNum(), profileDto.getReporter().getName()) // 위임자
                    , profileDto.getLog() // 위임 전 값
            ));
        } else {
            ProgressStatus progressStatus = ProgressStatus.OPEN;
            if (ProgressStatus.CANCEL.equals(profileDto.getStatus())) {
                progressStatus = ProgressStatus.CANCEL;
            }

            this.profiles.add(index, Profile.of(number
                    , profileDto.getDate()
                    , Member.of(profileDto.getAssignee().getNum(), profileDto.getAssignee().getName())
                    , taskLevel
                    , sequence
                    , profileDto.getTask()
                    , progressStatus
                    , profileDto.getDescription()
            ));
        }

        return number;
    }

    /**
     * 해야할일 번호로 데이터 추출
     * @param num
     * @return 해야할일
     */
    public Profile getProfile(int num) {
        // 해야할일의 번호는 1부터 부여 함
        if (0 < num) {
            boolean isExist = false;
            int index = 0;
            while (index < this.profiles.size()) {
                if (num == this.profiles.get(index).getNum()) {
                    isExist = true; // 번호가 일치하는 해야할일이 있음
                    break;
                }
                index++;
            }
    
            if (isExist) {
                return this.profiles.get(index);
            } else {
                // 번호가 일치하는 해야할일 없음
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * 해야할일 리스트를 반환
     * @return 리스트
     */
    public ArrayList<Profile> getProfiles(ProfileDto profileDto) {
        if (null == profileDto) { // 전체 조회
            return this.profiles;
        } else {
            ArrayList<Profile> result = null;

            if (null != profileDto.getDate()) { // 특정 날짜에 해당하는 해야할일 조회
                result = (ArrayList<Profile>) this.profiles.stream()
                        .filter(profile -> profileDto.getDate().equals(profile.getDate()))
                        .collect(Collectors.toList());
            }

            if (null != profileDto.getAssignee()) {
                // 특정 담당자에 해당하는 해야할일 조회
                if (null != result) {
                    // 특정 날짜조건도 있을 경우 상단 블럭(if)에 해당하여 조회한 결과에 담당자를 필터하여 결과 추출
                    result = (ArrayList<Profile>) result.stream()
                            .filter(profile -> profileDto.getAssignee().getNum() == profile.getAssignee().getNumber())
                            .collect(Collectors.toList());
                } else {
                    result = (ArrayList<Profile>) this.profiles.stream()
                            .filter(profile -> profileDto.getAssignee().getNum() == profile.getAssignee().getNumber())
                            .collect(Collectors.toList());
                }
            }

            return result;
        }
    }

    /**
     * 해야할일의 번호로 삭제
     * @param num
     * @return 삭제 결과
     */
    @Synchronized
    public boolean removeProfile(int num) {
        boolean isValid = false; // 일치하는 번호가 위치한 인덱스가 유효한지(찾지못했을 경우 false)
        int index = 0; // 삭제 할 인덱스
        for (; index < this.profiles.size(); index++) {
            if (num == this.profiles.get(index).getNum()) {
                isValid = true;
                break;
            }
        }
        
        // 해야할일 삭제
        if (isValid) {
            Profile target = this.profiles.get(index); // 삭제 대상 해야할일의 중요도
            this.profiles.remove(index);

            // 삭제 후 인덱스(삭제 시 인덱스 -1)부터 동일 중요도 내 순서 -1
            while (index < this.profiles.size()) {
                if (target.getTaskLevel().equals(this.profiles.get(index).getTaskLevel())
                        && (target.getAssignee().getNumber() == this.profiles.get(index).getAssignee().getNumber())) { // 상단의 taskLevel 변수에 할당한 것과 다른 정보(삭제 됨)
                    // 동일 중요도의 순서 -1
                    this.profiles.get(index).minusSequence();
                } else {
                    // 중요도가 다르면 종료
                    break;
                }

                index++;
            }

            return true;
        }

        return false;
    }

    /**
     * 해야할일을 다른 담당자에게 위임
     * @param profileDto
     * @return 결과
     */
    public void allocateAssignee(ProfileDto profileDto) {
        if (null != profileDto && 0 != profileDto.getNum() && null != profileDto.getAssignee()) {
            Profile profile = this.getProfile(profileDto.getNum()); // 해야할일 조회
            if (null != profile) {
                this.removeProfile(profileDto.getNum()); // 해야할일 삭제
                ProfileDto allocateTodo = new ProfileDto();
                allocateTodo.setNum(profile.getNum()); // 번호는 고유값이므로 변경하지않음
                allocateTodo.setReporter(new MemberDto(profile.getAssignee().getNumber(), profile.getAssignee().getName())); // 담당자를 위임자로 변경
                allocateTodo.setAssignee(profileDto.getAssignee()); // 새로운 담당자
                allocateTodo.setDate(profile.getDate());
                allocateTodo.setTask(profile.getTask());
                allocateTodo.setDescription(profile.getDescription());
                allocateTodo.setStatus(ProgressStatus.ASSIGN); // 위임
                
                // 위임취소 시 원상복구를 위한 값 보관
                HashMap<String, Object> log = new HashMap<>();
                log.put(Profile.KEY_TASK_LEVEL, profile.getTaskLevel());
                log.put(Profile.KEY_SEQ, profile.getSeq());
                allocateTodo.setLog(log);
                this.addProfile(allocateTodo); // 새로운 담당자의 해야할일로 추가
            } else {
                // 해당하는 번호의 해야할일이 없으면 HandledException 발생
                throw new HandledException(HttpStatus.BAD_REQUEST, ResponseStatus.FAIL02);
            }
        }
    }

    /**
     * 위임 취소하면 원래의 담당자(위임자)로 변경되며 우선순위도 이전의 값으로 변경한다.
     * @param todoNum
     * @return 결과
     */
    public void cancelAllocatedProfile(int todoNum) {
        Profile profile = this.getProfile(todoNum); // 해야할일을 조회
        if (null == profile) {
            // 해야할지 유무 체크
            throw new HandledException(HttpStatus.BAD_REQUEST, ResponseStatus.FAIL02);
        }

        if ((null != profile.getReporter())) { // 위임받은게 맞는지 확인
            ProfileDto profileDto = new ProfileDto();
            profileDto.setNum(profile.getNum()); // 번호는 고유값이므로 변경하지않음
            profileDto.setAssignee(new MemberDto(profile.getReporter().getNumber(), profile.getReporter().getName())); // 위임자를 담당자로 변경
            profileDto.setDate(profile.getDate());
            profileDto.setTask(profile.getTask());
            profileDto.setDescription(profile.getDescription());
            profileDto.setStatus(ProgressStatus.CANCEL);

            // 위임취소의 경우 이전의 값(중요도, 순서)으로 변경
            // 내역이 없으면 새로운 우선순위로 변경(중요도와 순서를 부여하지 않음)
            if (null != profile.getLog()) {
                profileDto.setTaskLevel((TaskLevel) profile.getLog().get(Profile.KEY_TASK_LEVEL));
                profileDto.setSeq((int) profile.getLog().get(Profile.KEY_SEQ));
            }

            this.removeProfile(profile.getNum()); // 위임받은 해야할일 제거
            this.addProfile(profileDto);
        } else {
            // 번호와 일치하는 해야할일이 없으면 HandledException 발생
            throw new HandledException(HttpStatus.BAD_REQUEST, ResponseStatus.FAIL03);
        }
    }

    /**
     * 해야할일 정보(task, description, 중요도, 순서, 상태) 변경
     * @param profileDto
     */
    public void updateProfile(ProfileDto profileDto) {
        Profile profile = this.getProfile(profileDto.getNum());
        if (null == profile) {
            // 일치하는 해야할일이 없으면 HandledException 발생
            throw new HandledException(HttpStatus.BAD_REQUEST, ResponseStatus.FAIL02);
        } else {
            // 우선순위가 변경되는지 확인
            boolean isUpdate = false;
            if ((null != profileDto.getTaskLevel()) || (null != profileDto.getSeq())) {
                // 현재 값과 비교
                if (!profile.getTaskLevel().equals(profileDto.getTaskLevel())) {
                    isUpdate = true;
                }
                if (!profile.getSeq().equals(profileDto.getSeq())) {
                    isUpdate = true;
                }
            }

            if (!isUpdate) {
                if ((null != profileDto.getTask()) && (!profile.getTask().equals(profileDto.getTask()))) { // task 변경
                    profile.setTask(profileDto.getTask());
                }
                if ((null != profileDto.getDescription()) && (!profile.getDescription().equals(profileDto.getDescription()))) { // description 변경
                    profile.setDescription(profileDto.getDescription());
                }
                if ((null != profileDto.getStatus()) && (!profile.getStatus().equals(profileDto.getStatus()))) { // 상태 변경
                    profile.setStatus(profileDto.getStatus());
                }
            } else {
                ProfileDto updateDto = new ProfileDto();
                updateDto.setNum(profile.getNum());
                updateDto.setDate(profile.getDate());
                updateDto.setAssignee(new MemberDto(profile.getAssignee().getNumber(), profile.getAssignee().getName()));
                // task 변경
                if (null != profileDto.getTask()) {
                    updateDto.setTask(profileDto.getTask());
                } else {
                    updateDto.setTask(profile.getTask());
                }
                // description 변경
                if (null != profileDto.getDescription()) {
                    updateDto.setDescription(profileDto.getDescription());
                } else {
                    updateDto.setDescription(profile.getDescription());
                }
                // 상태 변경
                if (null != profileDto.getStatus()) {
                    updateDto.setStatus(profileDto.getStatus());
                } else {
                    updateDto.setStatus(profile.getStatus());
                }
                // 중요도
                if (null != profileDto.getTaskLevel()) {
                    updateDto.setTaskLevel(profileDto.getTaskLevel());
                } else {
                    updateDto.setTaskLevel(profile.getTaskLevel());
                }
                // 순서
                if (null != profileDto.getSeq()) {
                    updateDto.setSeq(profileDto.getSeq());
                } else {
                    updateDto.setSeq(profile.getSeq());
                }

                this.removeProfile(profile.getNum());
                this.addProfile(updateDto);
            }
        }
    }
}
