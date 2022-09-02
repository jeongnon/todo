package com.todo.issue.service;

import static org.assertj.core.api.Assertions.*;
import com.todo.issue.model.domain.Profile;
import com.todo.issue.model.domain.Todo;
import com.todo.issue.model.dto.TodoDto;
import com.todo.issue.model.enums.ProgressStatus;
import com.todo.issue.model.enums.TaskLevel;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.config.Task;

import java.time.LocalDate;
import java.util.ArrayList;

@Slf4j
@SpringBootTest
public class TodoServiceTest {

    @Autowired
    TodoService todoService;

    @AfterEach
    @Test
    public void afterEachRun() {
        todoService.getTodos(null).clear();
    }

    @DisplayName("신규 todo 번호 발급")
    @Test
    public void getNextNumberTest() {
        int number = todoService.getNextNumber(); // 신규 번호
        log.debug("new number = " + number);
        assertThat(todoService.getNextNumber()).isGreaterThan(number);
        assertThat(todoService.getNextNumber()).isEqualTo(number + 2);
    }

    @DisplayName("todo 추가")
    @Test
    public void addTodoTest() {
        TodoDto todoDto = new TodoDto();
        todoDto.setDate(LocalDate.of(2022, 5, 1));
        todoDto.setAssignee(Profile.of(0, "김희정"));
        todoDto.setTask("해야할 일");
        todoDto.setDescription("간략설명");

        int num = todoService.addTodo(false, todoDto);
        assertThat(num).isGreaterThanOrEqualTo(1);

        Todo todo = todoService.getTodo(num);
        assertThat(todo).isNotNull();
        assertThat(todo.getDate()).isEqualTo(todoDto.getDate());
        assertThat(todo.getAssignee().getNumber()).isEqualTo(todoDto.getAssignee().getNumber());
        assertThat(todo.getTask()).isEqualTo(todoDto.getTask());
        assertThat(todo.getDescription()).isEqualTo(todoDto.getDescription());

        // addTodo 내부에서 부여한 우선순위
        log.debug("우선순위 = {}{}", todo.getTaskLevel().getName(), todo.getSeq());
        assertThat(todo.getTaskLevel()).isNotNull();
        assertThat(todo.getSeq()).isNotNull();
        assertThat(todo.getStatus()).isEqualTo(ProgressStatus.OPEN);
    }

    @DisplayName("동일 날짜 동일 담당자로 todo 추가 시 우선순위")
    @Test
    public void addTodoSameDateSameAssigneePriorityTest() {
        LocalDate date = LocalDate.of(2022, 5, 1);
        Profile assignee = Profile.of(0, "김희정");

        TodoDto todoDto = new TodoDto();
        todoDto.setDate(date);
        todoDto.setAssignee(assignee);
        todoDto.setTask("해야할 일");
        todoDto.setDescription("간략설명");

        int num = todoService.addTodo(false, todoDto);
        assertThat(num).isGreaterThanOrEqualTo(1);

        todoDto = new TodoDto();
        todoDto.setDate(date);
        todoDto.setAssignee(assignee);
        todoDto.setTask("해야할 일2");
        todoDto.setDescription("간략설명2");

        int nextNum = todoService.addTodo(false, todoDto);
        assertThat(nextNum).isGreaterThan(num); // 먼저 추가 된 해야할일의 번호보다 높다

        // addTodo 내부에서 부여한 우선순위 비교
        Todo firstTodo = todoService.getTodo(num);
        assertThat(firstTodo).isNotNull();
        Todo secondTodo = todoService.getTodo(nextNum);
        assertThat(secondTodo).isNotNull();

        log.debug("firstTodo 우선순위 = {}{}", firstTodo.getTaskLevel().getName(), firstTodo.getSeq());
        log.debug("secondTodo 우선순위 = {}{}", secondTodo.getTaskLevel().getName(), secondTodo.getSeq());
        assertThat(secondTodo.getTaskLevel()).isEqualTo(firstTodo.getTaskLevel());
        assertThat(secondTodo.getSeq()).isGreaterThan(firstTodo.getSeq());
    }

    @DisplayName("동일 날짜 다른 담당자로 todo 추가 시 우선순위")
    @Test
    public void addTodoSameDatePriorityTest() {
        LocalDate date = LocalDate.of(2022, 5, 1);
        TodoDto todoDto = new TodoDto();
        todoDto.setDate(date);
        todoDto.setAssignee(Profile.of(100, "김희정"));
        todoDto.setTask("해야할 일");
        todoDto.setDescription("간략설명");

        int num = todoService.addTodo(false, todoDto);
        assertThat(num).isGreaterThanOrEqualTo(1);

        todoDto = new TodoDto();
        todoDto.setDate(date);
        todoDto.setAssignee(Profile.of(101, "임성욱"));
        todoDto.setTask("해야할 일2");
        todoDto.setDescription("간략설명2");

        int nextNum = todoService.addTodo(false, todoDto);
        assertThat(nextNum).isGreaterThan(num); // 먼저 추가 된 해야할일의 번호보다 높다

        // addTodo 내부에서 부여한 우선순위 비교
        Todo todo1 = todoService.getTodo(num);
        assertThat(todo1).isNotNull();
        Todo todo2 = todoService.getTodo(nextNum);
        assertThat(todo2).isNotNull();

        log.debug("todo1 우선순위 = {}{}", todo1.getTaskLevel().getName(), todo1.getSeq());
        assertThat(todo1.getTaskLevel()).isEqualTo(TaskLevel.B);
        assertThat(todo1.getSeq()).isEqualTo(0);

        log.debug("todo2 우선순위 = {}{}", todo2.getTaskLevel().getName(), todo2.getSeq());
        assertThat(todo2.getTaskLevel()).isEqualTo(TaskLevel.B);
        assertThat(todo2.getSeq()).isEqualTo(0);
    }

    @DisplayName("다른 날짜 동일 담당자로 todo 추가 시 우선순위")
    @Test
    public void addTodoSameAssigneePriorityTest() {
        Profile assignee = Profile.of(10, "김희정");
        TodoDto todoDto = new TodoDto();
        todoDto.setDate(LocalDate.of(2023, 5, 1));
        todoDto.setAssignee(assignee);
        todoDto.setTask("해야할 일");
        todoDto.setDescription("간략설명");

        int num1 = todoService.addTodo(false, todoDto);
        assertThat(num1).isGreaterThanOrEqualTo(1);

        todoDto = new TodoDto();
        todoDto.setDate(LocalDate.of(2023, 5, 1));
        todoDto.setAssignee(assignee);
        todoDto.setTask("해야할 일2");
        todoDto.setDescription("간략설명2");

        int num2 = todoService.addTodo(false, todoDto);
        assertThat(num2).isGreaterThan(num1); // 먼저 추가 된 해야할일의 번호보다 높다

        todoDto = new TodoDto();
        todoDto.setDate(LocalDate.of(2023, 6, 1));
        todoDto.setAssignee(assignee);
        todoDto.setTask("해야할 일3");
        todoDto.setDescription("간략설명3");

        int num3 = todoService.addTodo(false, todoDto);
        assertThat(num3).isGreaterThan(num2); // 먼저 추가 된 해야할일의 번호보다 높다

        // addTodo 내부에서 부여한 우선순위 비교
        Todo todo1 = todoService.getTodo(num1);
        assertThat(todo1).isNotNull();
        Todo todo2 = todoService.getTodo(num2);
        assertThat(todo2).isNotNull();
        Todo todo3 = todoService.getTodo(num3);
        assertThat(todo3).isNotNull();

        log.debug("todo1 우선순위 = {}{}", todo1.getTaskLevel().getName(), todo1.getSeq());
        assertThat(todo1.getTaskLevel()).isEqualTo(TaskLevel.B);
        assertThat(todo1.getSeq()).isEqualTo(0);

        log.debug("todo2 우선순위 = {}{}", todo2.getTaskLevel().getName(), todo2.getSeq());
        assertThat(todo2.getTaskLevel()).isEqualTo(TaskLevel.B);
        assertThat(todo2.getSeq()).isEqualTo(1);

        log.debug("todo3 우선순위 = {}{}", todo3.getTaskLevel().getName(), todo3.getSeq());
        assertThat(todo3.getTaskLevel()).isEqualTo(TaskLevel.B);
        assertThat(todo3.getSeq()).isEqualTo(0);
    }

    @DisplayName("다른 중요도로 todo 추가 시 우선순위")
    @Test
    public void addTodoNotSameTaskLevelPriorityTest() {
        LocalDate date = LocalDate.of(2022, 5, 1);
        Profile profile = Profile.of(20, "김희정");
        TodoDto todoDto = new TodoDto();
        todoDto.setDate(date);
        todoDto.setAssignee(profile);
        todoDto.setTask("해야할 일");
        todoDto.setDescription("간략설명");

        int num = todoService.addTodo(false, todoDto);
        assertThat(num).isGreaterThanOrEqualTo(1);

        todoDto = new TodoDto();
        todoDto.setDate(date);
        todoDto.setAssignee(profile);
        todoDto.setTaskLevel(TaskLevel.C); // 다른중요도로 입력
        todoDto.setTask("해야할 일2");
        todoDto.setDescription("간략설명2");

        int nextNum = todoService.addTodo(false, todoDto);
        assertThat(nextNum).isGreaterThan(num); // 먼저 추가 된 해야할일의 번호보다 높다

        // addTodo 내부에서 부여한 우선순위 비교
        Todo todo1 = todoService.getTodo(num);
        assertThat(todo1).isNotNull();
        Todo todo2 = todoService.getTodo(nextNum);
        assertThat(todo2).isNotNull();

        log.debug("todo1 우선순위 = {}{}", todo1.getTaskLevel().getName(), todo1.getSeq());
        assertThat(todo1.getTaskLevel()).isEqualTo(TaskLevel.B);
        assertThat(todo1.getSeq()).isEqualTo(0);

        log.debug("todo2 우선순위 = {}{}", todo2.getTaskLevel().getName(), todo2.getSeq());
        assertThat(todo2.getTaskLevel()).isEqualTo(TaskLevel.C);
        assertThat(todo2.getSeq()).isEqualTo(0);
    }

    @DisplayName("다른 중요도로 todo 추가 시 우선순위")
    @Test
    public void addTodoNotSameTaskLevelPriority2Test() {
        LocalDate date = LocalDate.of(2022, 5, 1);
        Profile profile = Profile.of(30, "김희정");
        TodoDto todoDto = new TodoDto();
        todoDto.setDate(date);
        todoDto.setAssignee(profile);
        todoDto.setTask("해야할 일");
        todoDto.setDescription("간략설명");

        int num = todoService.addTodo(false, todoDto);
        assertThat(num).isGreaterThanOrEqualTo(1);

        todoDto = new TodoDto();
        todoDto.setDate(date);
        todoDto.setAssignee(profile);
        todoDto.setTaskLevel(TaskLevel.S); // 다른중요도로 입력
        todoDto.setTask("해야할 일2");
        todoDto.setDescription("간략설명2");

        int nextNum = todoService.addTodo(false, todoDto);
        assertThat(nextNum).isGreaterThan(num); // 먼저 추가 된 해야할일의 번호보다 높다

        // addTodo 내부에서 부여한 우선순위 비교
        Todo todo1 = todoService.getTodo(num);
        assertThat(todo1).isNotNull();
        Todo todo2 = todoService.getTodo(nextNum);
        assertThat(todo2).isNotNull();

        log.debug("todo1 우선순위 = {}{}", todo1.getTaskLevel().getName(), todo1.getSeq());
        assertThat(todo1.getTaskLevel()).isEqualTo(TaskLevel.B);
        assertThat(todo1.getSeq()).isEqualTo(0);

        log.debug("todo2 우선순위 = {}{}", todo2.getTaskLevel().getName(), todo2.getSeq());
        assertThat(todo2.getTaskLevel()).isEqualTo(TaskLevel.S);
        assertThat(todo2.getSeq()).isEqualTo(0);
    }

    @DisplayName("다른 담당자에게 위임 시 우선순위와 status")
    @Test
    public void addTodoAllocatedPriorityTest() {
        LocalDate date = LocalDate.of(2022, 5, 1);
        Profile assignee = Profile.of(40, "김희정");
        Profile reporter = Profile.of(41, "김희정");

        TodoDto todoDto = new TodoDto();
        todoDto.setDate(date);
        todoDto.setAssignee(assignee);
        todoDto.setReporter(reporter);
        todoDto.setTask("해야할 일");
        todoDto.setDescription("간략설명");
        todoDto.setTaskLevel(TaskLevel.D);
        todoDto.setSeq(3);
        todoDto.setStatus(ProgressStatus.OPEN);

        int num = todoService.addTodo(true, todoDto);
        assertThat(num).isGreaterThanOrEqualTo(1);

        Todo todo = todoService.getTodo(num);

        log.debug("todo 우선순위 = {}{}", todo.getTaskLevel().getName(), todo.getSeq());
        assertThat(todo.getTaskLevel()).isEqualTo(TaskLevel.A);
        assertThat(todo.getSeq()).isEqualTo(0);
        assertThat(todo.getStatus()).isEqualTo(ProgressStatus.ASSIGN);

        todoDto = new TodoDto();
        todoDto.setDate(date);
        todoDto.setAssignee(assignee);
        todoDto.setReporter(reporter);
        todoDto.setTask("해야할 일");
        todoDto.setDescription("간략설명");
        todoDto.setTaskLevel(TaskLevel.D);
        todoDto.setSeq(3);
        todoDto.setStatus(ProgressStatus.OPEN);

        int num2 = todoService.addTodo(true, todoDto);
        assertThat(num).isGreaterThanOrEqualTo(1);

        Todo todo2 = todoService.getTodo(num2);

        log.debug("todo 우선순위 = {}{}", todo2.getTaskLevel().getName(), todo2.getSeq());
        assertThat(todo2.getTaskLevel()).isEqualTo(TaskLevel.A);
        assertThat(todo2.getSeq()).isEqualTo(1);
        assertThat(todo2.getStatus()).isEqualTo(ProgressStatus.ASSIGN);
    }

    @DisplayName("A0 다음으로 추가 된 해야할일의 우선순위")
    @Test
    public void addTodoAllocatedPriority2Test() {
        LocalDate date = LocalDate.of(2022, 5, 1);
        Profile assignee = Profile.of(42, "김희정");
        Profile reporter = Profile.of(43, "김희정");

        TodoDto todoDto = new TodoDto();
        todoDto.setDate(date);
        todoDto.setAssignee(assignee);
        todoDto.setReporter(reporter);
        todoDto.setTask("해야할 일");
        todoDto.setDescription("간략설명");
        todoDto.setTaskLevel(TaskLevel.D);
        todoDto.setSeq(3);
        todoDto.setStatus(ProgressStatus.OPEN);

        int num = todoService.addTodo(true, todoDto);
        assertThat(num).isGreaterThanOrEqualTo(1);

        Todo todo = todoService.getTodo(num);

        log.debug("todo 우선순위 = {}{}", todo.getTaskLevel().getName(), todo.getSeq());
        assertThat(todo.getTaskLevel()).isEqualTo(TaskLevel.A);
        assertThat(todo.getSeq()).isEqualTo(0);
        assertThat(todo.getStatus()).isEqualTo(ProgressStatus.ASSIGN);

        todoDto = new TodoDto();
        todoDto.setDate(date);
        todoDto.setAssignee(assignee);
        todoDto.setReporter(reporter);
        todoDto.setTask("해야할 일");
        todoDto.setDescription("간략설명");

        int num2 = todoService.addTodo(false, todoDto);
        assertThat(num).isGreaterThanOrEqualTo(1);

        Todo todo2 = todoService.getTodo(num2);

        log.debug("todo 우선순위 = {}{}", todo2.getTaskLevel().getName(), todo2.getSeq());
        assertThat(todo2.getTaskLevel()).isEqualTo(TaskLevel.A);
        assertThat(todo2.getSeq()).isEqualTo(1);
        assertThat(todo2.getStatus()).isEqualTo(ProgressStatus.OPEN);
    }

    @DisplayName("A0 다음으로 추가 된 다른 날짜의 해야할일의 우선순위")
    @Test
    public void addTodoAllocatedPriorityNotSameDateTest() {
        Profile assignee = Profile.of(44, "김희정");

        TodoDto todoDto = new TodoDto();
        todoDto.setDate(LocalDate.of(2022, 5, 1));
        todoDto.setAssignee(assignee);
        todoDto.setReporter(Profile.of(45, "김희정"));
        todoDto.setTask("해야할 일");
        todoDto.setDescription("간략설명");
        todoDto.setTaskLevel(TaskLevel.D);
        todoDto.setSeq(3);
        todoDto.setStatus(ProgressStatus.OPEN);

        int num = todoService.addTodo(true, todoDto);
        assertThat(num).isGreaterThanOrEqualTo(1);

        Todo todo = todoService.getTodo(num);

        log.debug("todo 우선순위 = {}{}", todo.getTaskLevel().getName(), todo.getSeq());
        assertThat(todo.getTaskLevel()).isEqualTo(TaskLevel.A);
        assertThat(todo.getSeq()).isEqualTo(0);
        assertThat(todo.getStatus()).isEqualTo(ProgressStatus.ASSIGN);

        todoDto = new TodoDto();
        todoDto.setDate(LocalDate.of(2022, 5, 2));
        todoDto.setAssignee(assignee);
        todoDto.setTask("해야할 일");
        todoDto.setDescription("간략설명");

        int num2 = todoService.addTodo(false, todoDto);
        assertThat(num).isGreaterThanOrEqualTo(1);

        Todo todo2 = todoService.getTodo(num2);

        log.debug("todo 우선순위 = {}{}", todo2.getTaskLevel().getName(), todo2.getSeq());
        assertThat(todo2.getTaskLevel()).isEqualTo(TaskLevel.B);
        assertThat(todo2.getSeq()).isEqualTo(0);
        assertThat(todo2.getStatus()).isEqualTo(ProgressStatus.OPEN);
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
        Profile assignee = Profile.of(48, "김희정");
        int seq = 3;
        String task = "task";
        String description = "description";

        TodoDto todoDto = null;
        Todo todo = null;
        for (int index = 0; index < dates.size(); index++) {
            todoDto = new TodoDto();
            todoDto.setDate(dates.get(index));
            todoDto.setAssignee(assignee);
            todoDto.setTaskLevel(TaskLevel.S);
            todoDto.setTask(task);
            todoDto.setDescription(description);

            int num = todoService.addTodo(false, todoDto);
            todo = todoService.getTodo(num);
            log.debug("num = {} / priority = {}({})", todo.getNum(), todo.getTaskLevel().getName(), todo.getSeq());
        }

        // 입력 시 정렬되어 들어가기때문에 순서가 동일한지 확인
        ArrayList<Todo> todos = todoService.getTodos(null); // 해야할일 목록 조회
        for (int index = 0; index < dates.size(); index++) {
            log.debug("{} : {} / {} / {}({})", index, todos.get(index).getDate().toString(), dates.get(index), todos.get(index).getTaskLevel().getName(), todos.get(index).getSeq());
            assertThat(todos.get(index).getDate()).isEqualTo(dates.get(index));
        }
    }

    @DisplayName("todo 추가 시 담당자순 입력")
    @Test
    public void addTodoForAssigneeTest() {
        // 담당자순 정렬을 위해 Profile 리스트를 만들어 정렬 후 순서가 일치하는지 결과 비교
        ArrayList<Profile> profiles = new ArrayList<>();
        profiles.add(Profile.of(50, "김희정"));
        profiles.add(Profile.of(50, "김희정"));
        profiles.add(Profile.of(51, "김희정"));
        profiles.add(Profile.of(52, "임시완"));
        profiles.add(Profile.of(52, "임시완"));
        profiles.add(Profile.of(53, "임성욱")); // index = 5

        // 담당자 외 정보는 동일하게 입력
        LocalDate date = LocalDate.of(2022, 6, 1);
        String task = "task";
        String description = "description";
        
        int[] todoSequences = {1, 3, 2, 5, 4, 0}; // 임의순서로 해야할일을 추가
        TodoDto todoDto = null;
        Todo todo = null;
        for (int index = 0; index < todoSequences.length; index++) {
            todoDto = new TodoDto();
            todoDto.setDate(date);
            todoDto.setAssignee(profiles.get(todoSequences[index]));
            todoDto.setTask(task);
            todoDto.setDescription(description);

            int num = todoService.addTodo(false, todoDto);
            todo = todoService.getTodo(num);
            log.debug("num = {} / priority = {}({})", todo.getNum(), todo.getTaskLevel().getName(), todo.getSeq());
        }

        ArrayList<Todo> todos = todoService.getTodos(null);
        for (int index = 0; index < todos.size(); index++) {
            log.debug("{} : {}({}) / {}({}) / {}({})", index, todos.get(index).getAssignee().getName(), todos.get(index).getAssignee().getNumber()
                    , profiles.get(index).getName(), profiles.get(index).getNumber(), todos.get(index).getTaskLevel().getName(), todos.get(index).getSeq());
            assertThat(todos.get(index).getAssignee().getName()).isEqualTo(profiles.get(index).getName());
            assertThat(todos.get(index).getAssignee().getNumber()).isEqualTo(profiles.get(index).getNumber());
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
        Profile assignee = Profile.of(55, "김희정");
        String task = "task";
        String description = "description";

        int[] todoSequences = {1, 2, 4, 0, 3, 7, 6, 5}; // 임의순서로 해야할일을 추가
        TodoDto todoDto = null;
        Todo todo = null;
        for (int index = 0; index < todoSequences.length; index++) {
            todoDto = new TodoDto();
            todoDto.setDate(date);
            todoDto.setAssignee(assignee);
            todoDto.setTaskLevel(taskLevels.get(todoSequences[index]));
            todoDto.setTask(task);
            todoDto.setDescription(description);

            int num = todoService.addTodo(false, todoDto);
            todo = todoService.getTodo(num);
            log.debug("num = {} / priority = {}({})", todo.getNum(), todo.getTaskLevel().getName(), todo.getSeq());
        }

        ArrayList<Todo> todos = todoService.getTodos(null);
        for (int index = 0; index < todos.size(); index++) {
            log.debug("{} : {}({}) / {}", index, todos.get(index).getTaskLevel().getName(), todos.get(index).getSeq(), taskLevels.get(index).getName());
            assertThat(todos.get(index).getTaskLevel()).isEqualTo(taskLevels.get(index));
        }
    }

    @DisplayName("todo 추가 시 순서")
    @Test
    public void addTodosForSequenceTest() {
        // 순서 외 정보는 동일하게 입력
        LocalDate date = LocalDate.of(2022, 6, 1);
        Profile assignee = Profile.of(56, "김희정");
        String task = "task";
        String description = "description";

        TodoDto todoDto = null;
        Todo todo = null;
        for (int index = 0; index < 10; index++) {
            todoDto = new TodoDto();
            todoDto.setDate(date);
            todoDto.setAssignee(assignee);
            todoDto.setTask(task);
            todoDto.setDescription(description);

            int num = todoService.addTodo(false, todoDto);
            todo = todoService.getTodo(num);
            log.debug("num = {} / priority = {}({})", todo.getNum(), todo.getTaskLevel().getName(), todo.getSeq());
        }

        ArrayList<Todo> todos = todoService.getTodos(null);
        for (int index = 0; index < todos.size(); index++) {
            log.debug("{} : {}({})", index, todos.get(index).getTaskLevel().getName(), todos.get(index).getSeq());
            assertThat(todos.get(index).getTaskLevel()).isEqualTo(TaskLevel.B);
            assertThat(todos.get(index).getSeq()).isEqualTo(index);
        }
    }

    @DisplayName("todo 삭제 시 순서 변경")
    @Test
    public void removeTodosForSequenceTest() {
        LocalDate date = LocalDate.of(2022, 6, 1);
        Profile assignee = Profile.of(57, "김희정");

        TodoDto todoDto = new TodoDto();
        todoDto.setDate(date);
        todoDto.setAssignee(assignee);
        todoDto.setTask("해야할 일");
        todoDto.setDescription("간략설명");

        int num = todoService.addTodo(false, todoDto);
        assertThat(num).isGreaterThanOrEqualTo(1);

        todoDto = new TodoDto();
        todoDto.setDate(date);
        todoDto.setAssignee(assignee);
        todoDto.setTask("해야할 일2");
        todoDto.setDescription("간략설명2");

        int nextNum = todoService.addTodo(false, todoDto);
        assertThat(nextNum).isGreaterThan(num); // 먼저 추가 된 해야할일의 번호보다 높다

        // addTodo 내부에서 부여한 우선순위 비교
        Todo firstTodo = todoService.getTodo(num);
        Todo secondTodo = todoService.getTodo(nextNum);

        log.debug("firstTodo 우선순위 ={} {}{}", firstTodo.getNum(), firstTodo.getTaskLevel().getName(), firstTodo.getSeq());
        log.debug("secondTodo 우선순위 ={} {}{}", secondTodo.getNum(), secondTodo.getTaskLevel().getName(), secondTodo.getSeq());
        assertThat(secondTodo.getTaskLevel()).isEqualTo(firstTodo.getTaskLevel());
        assertThat(secondTodo.getSeq()).isGreaterThan(firstTodo.getSeq());
        
        assertThat(todoService.removeTodo(num)).isTrue(); // firstTodo 삭제
        Todo renewSecondTodo = todoService.getTodo(nextNum); // 해야할일의 번호는 변경되지않음
        log.debug("renewSecondTodo 우선순위 = {}{}", renewSecondTodo.getTaskLevel().getName(), renewSecondTodo.getSeq());
        assertThat(renewSecondTodo.getTaskLevel()).isEqualTo(secondTodo.getTaskLevel());
        assertThat(renewSecondTodo.getSeq()).isLessThan(secondTodo.getSeq());
    }

    @DisplayName("todo 삭제 시 다른 중요도의 순서 유지")
    @Test
    public void removeTodosNotSameTaskLevelTest() {
        LocalDate date = LocalDate.of(2022, 6, 1);
        Profile assignee = Profile.of(60, "김희정");

        TodoDto todoDto = new TodoDto();
        todoDto.setDate(date);
        todoDto.setAssignee(assignee);
        todoDto.setTask("해야할 일");
        todoDto.setDescription("간략설명");

        int num = todoService.addTodo(false, todoDto);
        assertThat(num).isGreaterThanOrEqualTo(1);

        todoDto = new TodoDto();
        todoDto.setDate(date);
        todoDto.setAssignee(assignee);
        todoDto.setTask("해야할 일2");
        todoDto.setDescription("간략설명2");
        todoDto.setTaskLevel(TaskLevel.D);

        int nextNum = todoService.addTodo(false, todoDto);
        assertThat(nextNum).isGreaterThan(num); // 먼저 추가 된 해야할일의 번호보다 높다

        // addTodo 내부에서 부여한 우선순위 비교
        Todo firstTodo = todoService.getTodo(num);
        Todo secondTodo = todoService.getTodo(nextNum);

        log.debug("firstTodo 우선순위 = {}{}", firstTodo.getTaskLevel().getName(), firstTodo.getSeq());
        log.debug("secondTodo 우선순위 = {}{}", secondTodo.getTaskLevel().getName(), secondTodo.getSeq());
        assertThat(firstTodo.getTaskLevel()).isEqualTo(TaskLevel.B);
        assertThat(firstTodo.getSeq()).isEqualTo(0);
        assertThat(secondTodo.getTaskLevel()).isEqualTo(todoDto.getTaskLevel());
        assertThat(secondTodo.getSeq()).isEqualTo(0);

        assertThat(todoService.removeTodo(num)).isTrue(); // firstTodo 삭제
        Todo renewSecondTodo = todoService.getTodo(nextNum); // 해야할일의 번호는 변경되지않음
        log.debug("renewSecondTodo 우선순위 = {}{}", renewSecondTodo.getTaskLevel().getName(), renewSecondTodo.getSeq());
        assertThat(renewSecondTodo.getTaskLevel()).isEqualTo(secondTodo.getTaskLevel());
        assertThat(renewSecondTodo.getSeq()).isEqualTo(secondTodo.getSeq());
    }

    @DisplayName("특정 날짜, 특정 담당자의 해야할일 조회")
    @Test
    public void getTodosOfDateTest() {
        LocalDate date = LocalDate.of(2022, 5, 1);
        Profile assignee = Profile.of(0, "김희정");
        
        ArrayList<Todo> todos = todoService.getTodos(null);
        todos.add(Todo.of(1, date, assignee, TaskLevel.B, 0, "", ProgressStatus.OPEN, ""));
        todos.add(Todo.of(2, date, assignee, TaskLevel.B, 1, "", ProgressStatus.OPEN, ""));
        todos.add(Todo.of(3, date, Profile.of(1, "임성욱"), TaskLevel.B, 0, "", ProgressStatus.OPEN, ""));
        todos.add(Todo.of(4, LocalDate.of(2022, 5, 10), assignee, TaskLevel.B, 0, "", ProgressStatus.OPEN, ""));
        todos.add(Todo.of(5, LocalDate.of(2022, 5, 10), assignee, TaskLevel.B, 1, "", ProgressStatus.OPEN, ""));
        todos.add(Todo.of(6, LocalDate.of(2022, 5, 10), Profile.of(1, "임성욱"), TaskLevel.B, 0, "", ProgressStatus.OPEN, ""));

        // 2022-05-01 날짜의 해야할일 조회
        TodoDto todoDto = new TodoDto(date, null, null, null);
        ArrayList<Todo> dateTodos = todoService.getTodos(todoDto);
        assertThat(dateTodos).isNotNull();
        assertThat(dateTodos.size()).isEqualTo(3);
        assertThat(dateTodos.get(0).getNum()).isEqualTo(1);
        assertThat(dateTodos.get(1).getNum()).isEqualTo(2);
        assertThat(dateTodos.get(2).getNum()).isEqualTo(3);
        
        // 김희정(0) 담당자의 해야할일 조회
        todoDto = new TodoDto(null, assignee, null, null);
        dateTodos = todoService.getTodos(todoDto);
        assertThat(dateTodos).isNotNull();
        assertThat(dateTodos.size()).isEqualTo(4);
        assertThat(dateTodos.get(0).getNum()).isEqualTo(1);
        assertThat(dateTodos.get(1).getNum()).isEqualTo(2);
        assertThat(dateTodos.get(2).getNum()).isEqualTo(4);
        assertThat(dateTodos.get(3).getNum()).isEqualTo(5);

        // 2022-05-01 + 김희정(0) 날짜와 담당자의 해야할일 조회
        todoDto = new TodoDto(date, assignee, null, null);
        dateTodos = todoService.getTodos(todoDto);
        assertThat(dateTodos).isNotNull();
        assertThat(dateTodos.size()).isEqualTo(2);
        assertThat(dateTodos.get(0).getNum()).isEqualTo(1);
        assertThat(dateTodos.get(1).getNum()).isEqualTo(2);
    }

    @DisplayName("todo 위임")
    @Test
    public void allocateAssigneeTest() {
        LocalDate date = LocalDate.of(2022, 5, 1);
        Profile assignee = Profile.of(0, "김희정");
        Profile allocatedAssignee = Profile.of(1, "김희정");
        int num1 = 2;
        int num2 = 3;
        int num3 = 4;

        ArrayList<Todo> todos = todoService.getTodos(null);
        todos.add(Todo.of(1, date, assignee, TaskLevel.B, 0, "", ProgressStatus.OPEN, ""));
        todos.add(Todo.of(num1, date, assignee, TaskLevel.B, 1, "", ProgressStatus.OPEN, ""));
        todos.add(Todo.of(num2, date, assignee, TaskLevel.B, 2, "", ProgressStatus.OPEN, ""));
        todos.add(Todo.of(num3, date, assignee, TaskLevel.C, 0, "", ProgressStatus.OPEN, ""));

        // 해야할일을 위임
        TodoDto todoDto = new TodoDto();
        todoDto.setNum(num1);
        todoDto.setAssignee(allocatedAssignee);
        assertThat(todoService.allocateAssignee(todoDto)).isTrue();
        
        Todo todo = todoService.getTodo(num1); // 위임한 해야할일을 조회
        assertThat(todo.getAssignee().getNumber()).isEqualTo(allocatedAssignee.getNumber()); // 담당자
        assertThat(todo.getReporter().getNumber()).isEqualTo(assignee.getNumber()); // 위임자
        assertThat(todo.getStatus()).isEqualTo(ProgressStatus.ASSIGN); // "위임"
        assertThat(todo.getTaskLevel()).isEqualTo(TaskLevel.A);
        assertThat(todo.getSeq()).isEqualTo(0);
        assertThat(todo.getLog().get(Todo.KEY_TASK_LEVEL)).isEqualTo(TaskLevel.B);
        assertThat(todo.getLog().get(Todo.KEY_SEQ)).isEqualTo(1);

        // 위임 후 다음 순위의 해야할일의 순서 변경 확인
        todo = todoService.getTodo(num2);
        assertThat(todo.getTaskLevel()).isEqualTo(TaskLevel.B);
        assertThat(todo.getSeq()).isEqualTo(1); // 순서 -1

        // 위임 후 중요도가 다른 해야할일의 순서 변경 없음
        todo = todoService.getTodo(num3);
        assertThat(todo.getTaskLevel()).isEqualTo(TaskLevel.C);
        assertThat(todo.getSeq()).isEqualTo(0);
    }
}
