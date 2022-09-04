package com.amore.task.controller;

import com.amore.task.common.HandledException;
import com.amore.task.common.ResponseMessage;
import com.amore.task.model.domain.Member;
import com.amore.task.model.domain.Profile;
import com.amore.task.model.dto.MemberDto;
import com.amore.task.model.dto.ProfileDto;
import com.amore.task.model.enums.ProgressStatus;
import com.amore.task.model.enums.ResponseStatus;
import com.amore.task.model.enums.TaskLevel;
import com.amore.task.service.TaskService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

@Slf4j
@RestController
public class ProfileController {
    @Autowired
    TaskService taskService;

    /**
     * 프로필 추가
     * @param requestBody
     * @return ResponseEntity
     * @throws JsonProcessingException
     */
    @PostMapping(value = "/profile", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseMessage> handleAddProfile(@RequestBody String requestBody) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(requestBody);
        // 필수 파라미터 체크
        if ((!node.has("date")) || ("".equals(node.get("date").asText()))) {
            throw new HandledException(HttpStatus.BAD_REQUEST, ResponseStatus.BAD_REQUEST04);
        }
        if ((!node.has("assignee")) || (0 > node.get("assignee").asInt())) {
            throw new HandledException(HttpStatus.BAD_REQUEST, ResponseStatus.BAD_REQUEST02);
        }
        if ((!node.has("task")) || ("".equals(node.get("task").asText()))) {
            throw new HandledException(HttpStatus.BAD_REQUEST, ResponseStatus.BAD_REQUEST01);
        }

        if (log.isDebugEnabled()) {
            log.debug("date : {}", node.get("date").asText());
            log.debug("assignee : {}", node.get("assignee").asText());
            log.debug("task : {}", node.get("task").asText());
            if (node.has("description")) {
                log.debug("description : {}", node.get("description").asText());
            }
        }

        LocalDate taskDate = null;
        try {
            taskDate = LocalDate.parse(node.get("date").asText(), DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeException dateTimeException) {
            throw new HandledException(HttpStatus.BAD_REQUEST, ResponseStatus.BAD_REQUEST05);
        }

        // DTO
        ProfileDto profileDto = new ProfileDto();
        profileDto.setDate(taskDate);
        profileDto.setTask(node.get("task").asText());
        if ((node.has("description")) && (!"".equals(node.get("description").asText()))) {
            profileDto.setDescription(node.get("description").asText());
        }

        MemberDto memberDto = new MemberDto(node.get("assignee").asInt(), null);

        Profile profile = taskService.addProfile(memberDto, profileDto);

        ResponseMessage message = new ResponseMessage();
        message.setStatus(ResponseStatus.SUCCESS.getCode());
        message.setResult(profile);
        message.setDesc("");

        return new ResponseEntity<>(message, HttpStatus.OK);
    }

    /**
     * 프로필 리스트 조회
     * @param date
     * @param assignee
     * @return ResponseEntity
     */
    @GetMapping(value = "/profiles", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseMessage> handleGetProfiles(
            @RequestParam(value = "date", required = false, defaultValue = "") String date,
            @RequestParam(value = "assignee", required = false, defaultValue = "") Integer assignee // null허용
    ) {
        if (log.isDebugEnabled()) {
            log.debug("date : {}", date);
            log.debug("assignee : {}", assignee);
        }

        // 필수 파라미터 없음
        LocalDate taskDate = null;
        if ((null != date) && (!"".equals(date))) {
            taskDate = LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE);
        }

        MemberDto member = null;
        if ((null != assignee) && (0 <= assignee)) {
            member = new MemberDto(assignee, null);
        }

        // 조건이 입력되지 않으면 파라미터는 null로 넘김
        ArrayList<Profile> profiles = null;
        if ((null == taskDate) && (null == member)) {
            profiles = taskService.getProfiles(null);
        } else {
            ProfileDto profileDto = new ProfileDto();
            profileDto.setDate(taskDate);
            profileDto.setAssignee(member);

            profiles = taskService.getProfiles(profileDto);
        }

        ResponseMessage message = new ResponseMessage();
        message.setStatus(ResponseStatus.SUCCESS.getCode());
        message.setResult(profiles);
        message.setDesc("");

        return new ResponseEntity<>(message, HttpStatus.OK);
    }

    /**
     * 다른 담당자에게 프로필 위임
     * @param requestBody
     * @return ResponseEntity
     * @throws JsonProcessingException
     */
    @PutMapping(value = "/profile/assignee", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseMessage> handleAllocatedAssignee(@RequestBody String requestBody) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(requestBody);

        // 필수 파라미터 체크
        if ((!node.has("profile")) || ("".equals(node.get("profile").asText()))) {
            throw new HandledException(HttpStatus.BAD_REQUEST, ResponseStatus.BAD_REQUEST03);
        }
        if ((!node.has("assignee")) || (0 > node.get("assignee").asInt())) {
            throw new HandledException(HttpStatus.BAD_REQUEST, ResponseStatus.BAD_REQUEST02);
        }

        if (log.isDebugEnabled()) {
            log.debug("profile : {}", node.get("profile").asText());
            log.debug("assignee : {}", node.get("assignee").asInt());
        }

        ProfileDto profileDto = new ProfileDto();
        profileDto.setNum(node.get("profile").asInt());
        MemberDto memberDto = new MemberDto(node.get("assignee").asInt(), null);

        Profile profile = taskService.allocateTask(memberDto, profileDto);

        ResponseMessage message = new ResponseMessage();
        message.setStatus(ResponseStatus.SUCCESS.getCode());
        message.setResult(profile);
        message.setDesc("");
        return new ResponseEntity<>(message, HttpStatus.OK);
    }

    /**
     * 프로필 위임취소
     * @param requestBody
     * @return ResponseEntity
     * @throws JsonProcessingException
     */
    @DeleteMapping(value = "/profile/assignee", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseMessage> handleCancelAllocatedProfile(@RequestBody String requestBody) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(requestBody);

        // 필수 파라미터 체크
        if ((!node.has("profile")) || ("".equals(node.get("profile").asText()))) {
            throw new HandledException(HttpStatus.BAD_REQUEST, ResponseStatus.BAD_REQUEST03);
        }

        if (log.isDebugEnabled()) {
            log.debug("profile : {}", node.get("profile").asText());
        }

        ProfileDto profileDto = new ProfileDto();
        profileDto.setNum(node.get("profile").asInt());
        Profile profile = taskService.cancelAllocation(profileDto);

        ResponseMessage message = new ResponseMessage();
        message.setStatus(ResponseStatus.SUCCESS.getCode());
        message.setResult(profile);
        message.setDesc("");
        return new ResponseEntity<>(message, HttpStatus.OK);
    }

    /**
     * 프로필 삭제
     * @param requestBody
     * @return ResponseEntity
     * @throws JsonProcessingException
     */
    @DeleteMapping(value = "/profile", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseMessage> handleDeleteProfile(@RequestBody String requestBody) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(requestBody);

        // 필수 파라미터 체크
        if ((!node.has("profile")) || ("".equals(node.get("profile").asText()))) {
            throw new HandledException(HttpStatus.BAD_REQUEST, ResponseStatus.BAD_REQUEST03);
        }

        if (log.isDebugEnabled()) {
            log.debug("profile : {}", node.get("profile").asText());
        }

        ProfileDto profileDto = new ProfileDto();
        profileDto.setNum(node.get("profile").asInt());
        boolean result = taskService.deleteProfile(profileDto);

        ResponseMessage message = new ResponseMessage();
        message.setStatus(ResponseStatus.SUCCESS.getCode());
        message.setResult(result);
        message.setDesc("");
        return new ResponseEntity<>(message, HttpStatus.OK);
    }

    /**
     * 프로필 정보 변경
     * @param requestBody
     * @return ResponseEntity
     * @throws JsonProcessingException
     */
    @PutMapping(value = "/profile", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseMessage> handleUpdateProfile(@RequestBody String requestBody) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(requestBody);

        // 필수 파라미터 체크
        if ((!node.has("profile")) || ("".equals(node.get("profile").asText()))) {
            throw new HandledException(HttpStatus.BAD_REQUEST, ResponseStatus.BAD_REQUEST03);
        }

        boolean isValid = false; // 파라미터 체크를 위함
        ProfileDto profileDto = new ProfileDto();

        // 프로필 번호
        if ((node.has("profile")) && (0 < node.get("profile").asInt())) {
            profileDto.setNum(node.get("profile").asInt());
        }

        // 업무명 변경
        if ((node.has("task")) && (!"".equals(node.get("task").asText()))) {
            isValid = true;
            profileDto.setTask(node.get("task").asText());
        }

        // 간략설명 변경
        if ((node.has("description")) && (!"".equals(node.get("description")))) {
            isValid = true;
            profileDto.setDescription(node.get("description").asText());
        }

        // 상태 변경
        if ((node.has("status")) && (0 < node.get("status").asInt())) {
            // 진행중(1)과 완료(2) 외에는 유효하지 않음
            if (1 == node.get("status").asInt()) { // 진행중
                isValid = true;
                profileDto.setStatus(ProgressStatus.OPEN);
            } else if (2 == node.get("status").asInt()) { // 완료
                isValid = true;
                profileDto.setStatus(ProgressStatus.COMPLETE);
            } else {
                throw new HandledException(HttpStatus.BAD_REQUEST, ResponseStatus.BAD_REQUEST07);
            }
        }

        // 중요도
        if ((node.has("level")) && (!"".equals(node.get("level").asText()))) {
            if (TaskLevel.isValid(node.get("level").asText())) {
                // 입력받은 중요도가 유효한 값이면 변경
                profileDto.setTaskLevel(TaskLevel.valueOf(node.get("level").asText()));
            } else {
                throw new HandledException(HttpStatus.BAD_REQUEST, ResponseStatus.BAD_REQUEST06);
            }
        }

        // 순서
        if ((node.has("seq")) && (0 <= node.get("seq").asInt())) {
            profileDto.setSeq(node.get("seq").asInt());
        }

        // 요청 값이 유효하면 변경
        Profile profile = null;
        if (isValid) {
            profile = taskService.updateProfile(profileDto); // 변경 후 조회
        } else {
            profile = taskService.getProfile(profileDto.getNum()); // 변경하지 않은 프로필 조회
        }

        ResponseMessage message = new ResponseMessage();
        message.setStatus(ResponseStatus.SUCCESS.getCode());
        message.setResult(profile);
        message.setDesc("");
        return new ResponseEntity<>(message, HttpStatus.OK);
    }
}
