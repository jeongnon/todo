package com.amore.task.controller;

import com.amore.task.model.enums.ResponseStatus;
import com.amore.task.service.TaskService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTestContextBootstrapper;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.BootstrapWith;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Slf4j
@SpringBootTest
@WebMvcTest
@BootstrapWith(SpringBootTestContextBootstrapper.class)
public class ProfileControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaskService taskService;

    /**
     * 프로필 추가 API 테스트
     * @throws Exception
     */
    @DisplayName("프로필 추가 API")
    @Test
    public void handleAddProfileTest() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode resultNode = null;
        MvcResult result = null;
        String body = null;
        HashMap<String, Object> paramBody = new HashMap<>();

        // 필수 파라미터 체크 - 날짜
        result = mockMvc.perform(post("/profile")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(mapper.writeValueAsString(paramBody)))
                .andExpect(status().isBadRequest()).andReturn();
        resultNode = mapper.readTree(result.getResponse().getContentAsString());
        assertThat(resultNode.get("status").asText()).isEqualTo(ResponseStatus.BAD_REQUEST04.getCode());

        paramBody.put("date", LocalDate.of(2022, 6, 1).toString()); // 날짜 추가

        // 필수 파라미터 체크 - 담당자
        result = mockMvc.perform(post("/profile")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(mapper.writeValueAsString(paramBody)))
                .andExpect(status().isBadRequest()).andReturn();
        resultNode = mapper.readTree(result.getResponse().getContentAsString());
        assertThat(resultNode.get("status").asText()).isEqualTo(ResponseStatus.BAD_REQUEST02.getCode());

        paramBody.put("assignee", 0); // 담당자 번호 추가

        // 필수 파라미터 체크 - 업무명
        result = mockMvc.perform(post("/profile")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(mapper.writeValueAsString(paramBody)))
                .andExpect(status().isBadRequest()).andReturn();
        resultNode = mapper.readTree(result.getResponse().getContentAsString());
        assertThat(resultNode.get("status").asText()).isEqualTo(ResponseStatus.BAD_REQUEST01.getCode());

        paramBody.put("task", "업무명 입력"); // 업무명 추가

        // 필수 파라미터 누락 없음 - 정상OK
        result = mockMvc.perform(post("/profile")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(mapper.writeValueAsString(paramBody)))
                .andExpect(status().isOk()).andReturn();
        resultNode = mapper.readTree(result.getResponse().getContentAsString());
        assertThat(resultNode.get("status").asText()).isEqualTo(ResponseStatus.SUCCESS.getCode());
    }

    /**
     * 프로필 리스트 조회
     * @throws Exception
     */
    @DisplayName("프로필 리스트 조회 API")
    @Test
    public void handleGetProfilesTest() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode resultNode = null;
        MvcResult result = null;
        String body = null;
        HashMap<String, Object> paramBody = new HashMap<>();

        // 필수 파라미터 없음
        result = mockMvc.perform(get("/profiles")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(mapper.writeValueAsString(paramBody)))
                .andExpect(status().isOk()).andReturn();
        resultNode = mapper.readTree(result.getResponse().getContentAsString());
        assertThat(resultNode.get("status").asText()).isEqualTo(ResponseStatus.SUCCESS.getCode());

        // 날짜 파라미터 입력 (선택)
//        paramBody = new HashMap<>();
        paramBody.put("date", LocalDate.of(2022, 5, 1).toString());
        result = mockMvc.perform(get("/profiles")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(mapper.writeValueAsString(paramBody)))
                .andExpect(status().isOk()).andReturn();
        resultNode = mapper.readTree(result.getResponse().getContentAsString());
        assertThat(resultNode.get("status").asText()).isEqualTo(ResponseStatus.SUCCESS.getCode());

        // 날짜 파라미터 입력 (선택)
        paramBody = new HashMap<>();
        paramBody.put("assignee", 0);
        result = mockMvc.perform(get("/profiles")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(mapper.writeValueAsString(paramBody)))
                .andExpect(status().isOk()).andReturn();
        resultNode = mapper.readTree(result.getResponse().getContentAsString());
        assertThat(resultNode.get("status").asText()).isEqualTo(ResponseStatus.SUCCESS.getCode());
    }

    /**
     * 프로필 위임 API 테스트
     * @throws Exception
     */
    @DisplayName("프로필 위임 API")
    @Test
    public void handleAllocatedAssigneeTest() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode resultNode = null;
        MvcResult result = null;
        String body = null;
        HashMap<String, Object> paramBody = new HashMap<>();

        // 필수 파라미터 없음 - 프로필 번호
        result = mockMvc.perform(put("/profile/assignee")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(mapper.writeValueAsString(paramBody)))
                .andExpect(status().isBadRequest()).andReturn();
        resultNode = mapper.readTree(result.getResponse().getContentAsString());
        assertThat(resultNode.get("status").asText()).isEqualTo(ResponseStatus.BAD_REQUEST03.getCode());

        paramBody.put("profile", 1); // 프로필 번호
        
        // 필수 파라미터 - 담당자 번호
        result = mockMvc.perform(put("/profile/assignee")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(mapper.writeValueAsString(paramBody)))
                .andExpect(status().isBadRequest()).andReturn();
        resultNode = mapper.readTree(result.getResponse().getContentAsString());
        assertThat(resultNode.get("status").asText()).isEqualTo(ResponseStatus.BAD_REQUEST02.getCode());

        paramBody.put("assignee", 0); // 담당자 번호

        // 필수 파라미터 모두 입력
        result = mockMvc.perform(put("/profile/assignee")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(mapper.writeValueAsString(paramBody)))
                .andExpect(status().isOk()).andReturn();
        resultNode = mapper.readTree(result.getResponse().getContentAsString());
        assertThat(resultNode.get("status").asText()).isEqualTo(ResponseStatus.SUCCESS.getCode());
    }

    /**
     * 프로필 위임 취소 API 테스트
     * @throws Exception
     */
    @DisplayName("프로필 위임 취소 API")
    @Test
    public void handleCancelAllocatedProfileTest() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode resultNode = null;
        MvcResult result = null;
        String body = null;
        HashMap<String, Object> paramBody = new HashMap<>();

        // 필수 파라미터 없음 - 프로필 번호
        result = mockMvc.perform(delete("/profile/assignee")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(mapper.writeValueAsString(paramBody)))
                .andExpect(status().isBadRequest()).andReturn();
        resultNode = mapper.readTree(result.getResponse().getContentAsString());
        assertThat(resultNode.get("status").asText()).isEqualTo(ResponseStatus.BAD_REQUEST03.getCode());

        paramBody.put("profile", 0);

        // 필수 파라미터 입력 - 프로필 번호
        result = mockMvc.perform(delete("/profile/assignee")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(mapper.writeValueAsString(paramBody)))
                .andExpect(status().isOk()).andReturn();
        resultNode = mapper.readTree(result.getResponse().getContentAsString());
        assertThat(resultNode.get("status").asText()).isEqualTo(ResponseStatus.SUCCESS.getCode());
    }

    /**
     * 프로필 삭제 API 테스트
     */
    @DisplayName("프로필 삭제 API")
    @Test
    public void handleDeleteProfileTest() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode resultNode = null;
        MvcResult result = null;
        String body = null;
        HashMap<String, Object> paramBody = new HashMap<>();

        // 필수 파라미터 없음 - 프로필 번호
        result = mockMvc.perform(delete("/profile")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(mapper.writeValueAsString(paramBody)))
                .andExpect(status().isBadRequest()).andReturn();
        resultNode = mapper.readTree(result.getResponse().getContentAsString());
        assertThat(resultNode.get("status").asText()).isEqualTo(ResponseStatus.BAD_REQUEST03.getCode());

        // 필수 파라미터 모두 입력
        paramBody.put("profile", 0);
        result = mockMvc.perform(delete("/profile")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(mapper.writeValueAsString(paramBody)))
                .andExpect(status().isOk()).andReturn();
        resultNode = mapper.readTree(result.getResponse().getContentAsString());
        assertThat(resultNode.get("status").asText()).isEqualTo(ResponseStatus.SUCCESS.getCode());
    }

    @DisplayName("프로필 변경 API")
    @Test
    public void handleUpdateProfileTest() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode resultNode = null;
        MvcResult result = null;
        String body = null;
        HashMap<String, Object> paramBody = new HashMap<>();

        // 필수 파라미터 없음 - 프로필 번호
        result = mockMvc.perform(put("/profile")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(mapper.writeValueAsString(paramBody)))
                .andExpect(status().isBadRequest()).andReturn();
        resultNode = mapper.readTree(result.getResponse().getContentAsString());
        assertThat(resultNode.get("status").asText()).isEqualTo(ResponseStatus.BAD_REQUEST03.getCode());
    
        // 유효하지않은 상태 값 입력
        paramBody.put("profile", 1);
        paramBody.put("status", 3);
        result = mockMvc.perform(put("/profile")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(mapper.writeValueAsString(paramBody)))
                .andExpect(status().isBadRequest()).andReturn();
        resultNode = mapper.readTree(result.getResponse().getContentAsString());
        assertThat(resultNode.get("status").asText()).isEqualTo(ResponseStatus.BAD_REQUEST07.getCode());

        // 유효한 상태 값 입력
        paramBody = new HashMap<>();
        paramBody.put("profile", 1);
        paramBody.put("status", 1);
        result = mockMvc.perform(put("/profile")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(mapper.writeValueAsString(paramBody)))
                .andExpect(status().isOk()).andReturn();
        resultNode = mapper.readTree(result.getResponse().getContentAsString());
        assertThat(resultNode.get("status").asText()).isEqualTo(ResponseStatus.SUCCESS.getCode());

        // 유효하지 않은 중요도 입력
        paramBody = new HashMap<>();
        paramBody.put("profile", 1);
        paramBody.put("level", "E");
        result = mockMvc.perform(put("/profile")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(mapper.writeValueAsString(paramBody)))
                .andExpect(status().isBadRequest()).andReturn();
        resultNode = mapper.readTree(result.getResponse().getContentAsString());
        assertThat(resultNode.get("status").asText()).isEqualTo(ResponseStatus.BAD_REQUEST06.getCode());

        // 유효한 중요도 입력
        paramBody = new HashMap<>();
        paramBody.put("profile", 1);
        paramBody.put("level", "A");
        result = mockMvc.perform(put("/profile")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(mapper.writeValueAsString(paramBody)))
                .andExpect(status().isOk()).andReturn();
        resultNode = mapper.readTree(result.getResponse().getContentAsString());
        assertThat(resultNode.get("status").asText()).isEqualTo(ResponseStatus.SUCCESS.getCode());
    }
}
