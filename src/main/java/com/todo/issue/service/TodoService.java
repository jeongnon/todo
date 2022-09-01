package com.todo.issue.service;

import com.todo.issue.model.domain.Todo;
import com.todo.issue.model.dto.TodoDto;
import com.todo.issue.model.enums.TaskLevel;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
public class TodoService {
    /** 해야할일 목록 */
    private ArrayList<Todo> todos;

    /** 해야할일 번호 */
    private int number;

    /** 해야할일 목록 날짜 > 담당자 > 중요도 > 순번으로 정렬 */
    private Comparator<Todo> comparator;

    public TodoService() {
        this.todos = new ArrayList<Todo>();
        this.number = 0;
        this.comparator = new Comparator<Todo>() { // comparator 구현
            @Override
            public int compare(Todo todo1, Todo todo2) {
                // 1. 날짜순 오름차순
                if (todo1.getDate().isBefore(todo2.getDate())) { // todo1가 더 과거의 날짜
                    return -1; // 선순위
                } else if (todo1.getDate().isAfter(todo2.getDate())) {
                    return 1; // 후순위
                } else {
                    // 2. 담당자별
                    if (todo1.getAssignee().getNumber() < todo2.getAssignee().getNumber()) {
                        return -1;
                    } else if (todo1.getAssignee().getNumber() > todo2.getAssignee().getNumber()) {
                        return 1;
                    } else {
                        // 3. 중요도순
                        if (todo1.getTaskLevel().getCode() < todo2.getTaskLevel().getCode()) {
                            return -1; // 선순위
                        } else if (todo1.getTaskLevel().getCode() > todo2.getTaskLevel().getCode()) {
                            return 1; // 후순위
                        } else {
                            // 4. 순번 오름차순
                            if (todo1.getSeq() < todo2.getSeq()) {
                                return -1; // 선순위
                            } else if (todo1.getSeq() > todo2.getSeq()) {
                                return 1; // 후순위
                            } else {
                                return 0; // 동등
                            }
                        }
                    }
                }
            }
        };
    }

    /**
     * 해야할일 목록을 정렬
     * @param target
     */
    public void sortTodos(List<Todo> target) {
        Collections.sort(target, this.comparator);
    }

    /**
     * 해야할일 등록
     * @param todoDto
     * @return 등록 결과
     */
    public boolean registTodo(TodoDto todoDto) {
        // TODO
        return false; // TODO
    }

    @Synchronized
    public boolean addTodo(boolean isAllocated, Todo todo) {
        int index = 0;
        int sequence = 0; // 동일 날짜의 최초 추가 시 초기값
        TaskLevel taskLevel = TaskLevel.B; // 동일 날짜의 최초 추가 시 초기값
        if (isAllocated) { // 원래 담당자에 의해 위임받은 경우 중요도 변경
            taskLevel = TaskLevel.A; // TODO 해당 케이스 처리해줘야함
        }


        if (0 < this.todos.size()) { // 해야할일이 존재할 경우
            for (; index < this.todos.size(); index++) {
                if (todo.getDate().isBefore(this.todos.get(index).getDate())) {
                    // 해당 날짜 최초 입력이기때문에 초기값(priority, sequence) 사용
                    break;
                } else if (todo.getDate().isEqual(this.todos.get(index).getDate())) {
                    // 날짜가 동일한 경우 동일 담당자를 비교
                    if (todo.getAssignee().getNumber() == this.todos.get(index).getAssignee().getNumber()) {
                        // 동일 담당자의 최하위순위를 찾기
                        while (((index + 1) < this.todos.size())
                                && (todo.getAssignee().getNumber() == this.todos.get(index + 1).getAssignee().getNumber())) {
                            index = index + 1; // 다음 인덱스의 담당자와 동일하니, 그 다음 인덱스 담당자와 비교

                        }

                        // 담당자의 마지막 해야할일 인덱스를 찾았으니, 해당 인덱스가 최하위순위
                        taskLevel = this.todos.get(index).getTaskLevel();
                        sequence = this.todos.get(index).getSeq() + 1; // 다음 순위로 추가해야하므로 순서 +1
                    } else {
                        // 담당자가 다른 경우 다음 인덱스의 날짜와 비교하여 더 이전의 날짜에 해당하거나
                        // 담당자 번호가 더 낮으면 (index+1)로 해야할일을 추가 = 동일 날짜의 동일 담당자의 해야할일이 없음
                        if ((todo.getDate().isBefore(this.todos.get(index + 1).getDate()))
                                || (todo.getAssignee().getNumber() < this.todos.get(index + 1).getAssignee().getNumber())) {
                            index = index + 1;
                            break;
                        }
                    }
                } else {
                    // 날짜가 다른 경우 다음 인덱스의 날짜와 비교하여 더 이전의 날짜에 해당하면 (index+1)로 추가하고 동일 날짜가 없음 = 초기값 사용
                    if (todo.getDate().isBefore(this.todos.get(index + 1).getDate())) {
                        index = index + 1;
                        break;
                    }
                }
            }
        }
        
        this.todos.add(index, todo);
        
        return false; // TODO
    }

}
