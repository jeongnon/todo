package com.todo.issue.service;

import static org.assertj.core.api.Assertions.*;
import com.todo.issue.model.domain.Profile;
import com.todo.issue.model.domain.Todo;
import com.todo.issue.model.enums.TaskLevel;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.ArrayList;

@Slf4j
@SpringBootTest
public class TodoServiceTest {

    @Autowired
    TodoService todoService;

    @DisplayName("todo 목록 정렬하기 - 날짜순")
    @Test
    public void sortTodosForDateTest() {
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
        Profile assignee = Profile.of(1, "김희정");
        int seq = 3;

        ArrayList<Todo> todos = new ArrayList<>();
        todos.add(Todo.of(0, dates.get(3), assignee, TaskLevel.S, seq, "task", "description"));
        todos.add(Todo.of(1, dates.get(2), assignee, TaskLevel.S, seq, "task", "description"));
        todos.add(Todo.of(2, dates.get(5), assignee, TaskLevel.S, seq, "task", "description"));
        todos.add(Todo.of(3, dates.get(1), assignee, TaskLevel.S, seq, "task", "description"));
        todos.add(Todo.of(4, dates.get(6), assignee, TaskLevel.S, seq, "task", "description"));
        todos.add(Todo.of(5, dates.get(0), assignee, TaskLevel.S, seq, "task", "description"));
        todos.add(Todo.of(6, dates.get(4), assignee, TaskLevel.S, seq, "task", "description"));
        todoService.sortTodos(todos);

        for (int i = 0; i < todos.size(); i++) {
            log.debug("{} : {} / {}", i, todos.get(i).getDate().toString(), dates.get(i));
            assertThat(todos.get(i).getDate()).isEqualTo(dates.get(i));
        }
    }

    @DisplayName("todo 목록 정렬하기 - 담당자순")
    @Test
    public void sortTodosForAssigneeTest() {
        // 담당자순 정렬을 위해 Profile 리스트를 만들어 정렬 후 순서가 일치하는지 결과 비교
        ArrayList<Profile> profiles = new ArrayList<>();
        profiles.add(Profile.of(0, "김희정"));
        profiles.add(Profile.of(0, "김희정"));
        profiles.add(Profile.of(1, "김희정"));
        profiles.add(Profile.of(2, "임시완"));
        profiles.add(Profile.of(2, "임시완"));
        profiles.add(Profile.of(3, "임성욱")); // index = 5

        // 담당자 외 정보는 동일하게 입력
        LocalDate date = LocalDate.of(2022, 6, 1);
        int seq = 3;

        ArrayList<Todo> todos = new ArrayList<>();
        todos.add(Todo.of(0, date, profiles.get(1), TaskLevel.S, seq, "task", "description"));
        todos.add(Todo.of(1, date, profiles.get(3), TaskLevel.S, seq, "task", "description"));
        todos.add(Todo.of(2, date, profiles.get(2), TaskLevel.S, seq, "task", "description"));
        todos.add(Todo.of(3, date, profiles.get(5), TaskLevel.S, seq, "task", "description"));
        todos.add(Todo.of(4, date, profiles.get(4), TaskLevel.S, seq, "task", "description"));
        todos.add(Todo.of(5, date, profiles.get(0), TaskLevel.S, seq, "task", "description"));
        todoService.sortTodos(todos);

        for (int i = 0; i < todos.size(); i++) {
            log.debug("{} : {}({}) / {}({})", i, todos.get(i).getAssignee().getName(), todos.get(i).getAssignee().getNumber()
                    , profiles.get(i).getName(), profiles.get(i).getNumber());
            assertThat(todos.get(i).getAssignee().getName()).isEqualTo(profiles.get(i).getName());
            assertThat(todos.get(i).getAssignee().getNumber()).isEqualTo(profiles.get(i).getNumber());
        }
    }

    @DisplayName("todo 목록 정렬하기 - 중요도순")
    @Test
    public void sortTodosForPriorityTest() {
        // 중요도순 정렬을 확인하기 위해 중요도리스트를 만들어 정렬 후 비교
        ArrayList<TaskLevel> priorities = new ArrayList<>();
        priorities.add(TaskLevel.S);
        priorities.add(TaskLevel.S);
        priorities.add(TaskLevel.A);
        priorities.add(TaskLevel.A);
        priorities.add(TaskLevel.B);
        priorities.add(TaskLevel.C);
        priorities.add(TaskLevel.C);
        priorities.add(TaskLevel.D); // index = 7

        // 우선순위 외 정보는 동일하게 입력
        LocalDate date = LocalDate.of(2022, 6, 1);
        Profile assignee = Profile.of(0, "김희정");
        int seq = 3;

        ArrayList<Todo> todos = new ArrayList<>();
        todos.add(Todo.of(0, date, assignee, priorities.get(1), seq, "task", "description"));
        todos.add(Todo.of(1, date, assignee, priorities.get(2), seq, "task", "description"));
        todos.add(Todo.of(2, date, assignee, priorities.get(4), seq, "task", "description"));
        todos.add(Todo.of(3, date, assignee, priorities.get(0), seq, "task", "description"));
        todos.add(Todo.of(4, date, assignee, priorities.get(3), seq, "task", "description"));
        todos.add(Todo.of(5, date, assignee, priorities.get(7), seq, "task", "description"));
        todos.add(Todo.of(6, date, assignee, priorities.get(6), seq, "task", "description"));
        todos.add(Todo.of(7, date, assignee, priorities.get(5), seq, "task", "description"));
        todoService.sortTodos(todos);

        for (int i = 0; i < todos.size(); i++) {
            log.debug("{} : {} / {}", i, todos.get(i).getTaskLevel().getName(), priorities.get(i).getName());
            assertThat(todos.get(i).getTaskLevel().getName()).isEqualTo(priorities.get(i).getName());
        }
    }

    @DisplayName("todo 목록 정렬하기 - 순서")
    @Test
    public void sortTodosForSequenceTest() {
        // 순서 외 정보는 동일하게 입력
        LocalDate date = LocalDate.of(2022, 6, 1);
        Profile assignee = Profile.of(0, "김희정");

        ArrayList<Todo> todos = new ArrayList<>();
        todos.add(Todo.of(0, date, assignee, TaskLevel.B, 0, "task", "description"));
        todos.add(Todo.of(1, date, assignee, TaskLevel.B, 2, "task", "description"));
        todos.add(Todo.of(2, date, assignee, TaskLevel.B, 3, "task", "description"));
        todos.add(Todo.of(3, date, assignee, TaskLevel.B, 5, "task", "description"));
        todos.add(Todo.of(4, date, assignee, TaskLevel.B, 4, "task", "description"));
        todos.add(Todo.of(5, date, assignee, TaskLevel.B, 6, "task", "description"));
        todos.add(Todo.of(6, date, assignee, TaskLevel.B, 1, "task", "description"));
        todoService.sortTodos(todos);

        for (int seq = 0; seq < todos.size(); seq++) {
            log.debug("{} : {}", seq, todos.get(seq).getSeq());
            assertThat(todos.get(seq).getSeq()).isEqualTo(seq);
        }
    }

    @DisplayName("todo 목록 정렬하기")
    @Test
    public void sortTodosTest() {
        ArrayList<Todo> todos = new ArrayList<>();
        todos.add(Todo.of(0, LocalDate.of(2022, 5, 1), Profile.of(0, "김희정"), TaskLevel.S, 0, "task", "description"));
        todos.add(Todo.of(1, LocalDate.of(2022, 5, 1), Profile.of(0, "김희정"), TaskLevel.B, 0, "task", "description"));
        todos.add(Todo.of(2, LocalDate.of(2022, 5, 1), Profile.of(0, "김희정"), TaskLevel.B, 1, "task", "description"));
        todos.add(Todo.of(3, LocalDate.of(2022, 5, 1), Profile.of(1, "임성욱"), TaskLevel.A, 0, "task", "description"));
        todos.add(Todo.of(4, LocalDate.of(2022, 5, 2), Profile.of(0, "김희정"), TaskLevel.B, 0, "task", "description"));
        todos.add(Todo.of(5, LocalDate.of(2022, 5, 2), Profile.of(0, "김희정"), TaskLevel.C, 0, "task", "description"));
        todos.add(Todo.of(6, LocalDate.of(2022, 5, 2), Profile.of(0, "김희정"), TaskLevel.C, 1, "task", "description"));
        todos.add(Todo.of(7, LocalDate.of(2022, 5, 2), Profile.of(2, "임시완"), TaskLevel.S, 0, "task", "description"));
        todos.add(Todo.of(8, LocalDate.of(2022, 5, 2), Profile.of(2, "임시완"), TaskLevel.S, 1, "task", "description"));
        todos.add(Todo.of(9, LocalDate.of(2022, 5, 4), Profile.of(1, "임성욱"), TaskLevel.A, 0, "task", "description"));
        todos.add(Todo.of(10, LocalDate.of(2022, 5, 4), Profile.of(1, "임성욱"), TaskLevel.A, 1, "task", "description"));
        // index = 10;

        ArrayList<Todo> target = new ArrayList<>();
        target.add(todos.get(1));
        target.add(todos.get(2));
        target.add(todos.get(3));
        target.add(todos.get(0));
        target.add(todos.get(4));
        target.add(todos.get(7));
        target.add(todos.get(9));
        target.add(todos.get(8));
        target.add(todos.get(6));
        target.add(todos.get(10));
        target.add(todos.get(5));
        todoService.sortTodos(target);

        for (int i = 0; i < target.size(); i++) {
            log.debug("{} : {} / {} / {} / {}", i, target.get(i).getDate(), target.get(i).getAssignee().getNumber()
                    , target.get(i).getTaskLevel().getName(), target.get(i).getSeq());
            assertThat(target.get(i).getDate()).isEqualTo(todos.get(i).getDate());
            assertThat(target.get(i).getAssignee().getNumber()).isEqualTo(todos.get(i).getAssignee().getNumber());
            assertThat(target.get(i).getTaskLevel().getName()).isEqualTo(todos.get(i).getTaskLevel().getName());
        }
    }
}
