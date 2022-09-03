package com.amore.task.service;

import com.amore.task.common.HandledException;
import com.amore.task.model.domain.Todo;
import com.amore.task.model.dto.TodoDto;
import com.amore.task.model.enums.ProgressStatus;
import com.amore.task.model.enums.ResponseStatus;
import com.amore.task.model.enums.TaskLevel;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

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
     * @param todoDto
     * @return 인덱스
     */
    public int getPriority(TodoDto todoDto) {
        boolean isValidIndex = false;
        int index = 0;

        for (; index < this.todos.size(); index++) {
            // 1. 날짜 비교
            if (todoDto.getDate().isBefore(this.todos.get(index).getDate())) {
                // 해당 인덱스의 날짜보다 이전일 경우, 인덱스로 추가
                break;
            } else if (todoDto.getDate().isAfter(this.todos.get(index).getDate())) {
                // 다음 인덱스 확인
                continue;
            } else {
                // 2. 날짜가 동일한 경우 담당자 비교
                if (todoDto.getAssignee().getNumber() < this.todos.get(index).getAssignee().getNumber()) {
                    // 해당 인덱스의 담당자 번호보다 낮은 경우, 해당 인덱스로 추가
                    break;
                } else if (todoDto.getAssignee().getNumber() > this.todos.get(index).getAssignee().getNumber()) {
                    // 다음 인덱스 확인
                    continue;
                } else {
                    // 3. 담당자가 동일한 경우 최하순위 바로 아래 순위를 검색
                    while (index < this.todos.size()) {
                        if (null != todoDto.getTaskLevel()) {
                            if (todoDto.getTaskLevel().getCode() < this.todos.get(index).getTaskLevel().getCode()) {
                                // 다음 인덱스가 하위의 중요도인 경우 해당 인덱스로 추가
                                isValidIndex = true;
                                break;
                            } else if (todoDto.getTaskLevel().getCode() > this.todos.get(index).getTaskLevel().getCode()) {
                                // 중요도A보다 높은 중요도의 경우 다음 인덱스 확인
                            } else {
                                // 동일 중요도 내 순서
                                if ((index + 1) < this.todos.size()) {
                                    // 다음 인덱스와 중요도가 다름
                                    if (this.todos.get(index).getTaskLevel().getCode() < this.todos.get(index + 1).getTaskLevel().getCode()) {
                                        isValidIndex = true;
                                        break;
                                    } else {
                                        // 순서 비교
                                        if (todoDto.getSeq() <= this.todos.get(index).getSeq()) {
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
                        if ((index + 1) < this.todos.size()) {
                            if (todoDto.getAssignee().getNumber() != this.todos.get(index + 1).getAssignee().getNumber()) {
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
    public int addTodo(TodoDto todoDto) {
        int index = 0; // 추가 될 위치
        TaskLevel taskLevel = null;
        int sequence = 0; // 동일 날짜의 최초 추가 시 초기값

        // 중요도 지정(지정하지 않으면 최하위순으로 부여)
        if (ProgressStatus.ASSIGN.equals(todoDto.getStatus())) {
            // 해야할일을 위임받은 경우 우선순위는 A 중 최하순위
            taskLevel = TaskLevel.A;
        } else if (null != todoDto.getTaskLevel()) {
            // 해야할일의 중요도가 지정되어있을 경우(변경) 동일 중요도 내 최하순위
            taskLevel = todoDto.getTaskLevel();
        }

        // 해야할일 변경 시 번호를 새로 부여하지않음
        int number = (0 == todoDto.getNum()) ? this.getNextNumber() : todoDto.getNum();

        if (0 < this.todos.size()) { // 해야할일이 존재할 경우
            index = getPriority(todoDto); // 입력 할 위치(인덱스)
        }

        // 해야할일이 추가되어야하는 위치(인덱스)에 맞는 중요도와 순서를 부여
        if (null == taskLevel) {
            // 위임받거나 중요도가 지정되지않음 아닌 경우 앞의 인덱스 날짜와 담당자가 동일하면 중요도와 동일하게 부여
            // 담당자가 다른 경우 초기값 B0 부여
            if ((0 < this.todos.size()) && (todoDto.getDate().isEqual(this.todos.get(index - 1).getDate()))
                    && (todoDto.getAssignee().getNumber() == this.todos.get(index - 1).getAssignee().getNumber())) {
                taskLevel = this.todos.get(index - 1).getTaskLevel();
                sequence = this.todos.get(index - 1).getSeq() + 1; // 앞의 인덱스 위치의 순서 +1
            } else {
                taskLevel = TaskLevel.B; // 동일 날짜의 최초 추가 시 초기값
                // sequence = 0; 초기값과 동일
            }
        } else {
            // 동일 중요도 내 순서
            if (0 < this.todos.size()) {
                if ((0 < index) && (taskLevel.equals(this.todos.get(index - 1).getTaskLevel()))) {
                    // 동일 중요도 내 다음 순서로 우선순위 입력
                    sequence = this.todos.get(index - 1).getSeq() + 1;
                }

                // 이후의 해야할일의 우선순위 재부여
                int targetIndex = index;
                while (targetIndex < this.todos.size()) {
                    // 동일 담당자의 동일 중요도인 경우, 순서를 +1 해주어야함
                    if ((todoDto.getAssignee().getNumber() == this.todos.get(targetIndex).getAssignee().getNumber())
                            && (taskLevel.equals(this.todos.get(targetIndex).getTaskLevel()))) {
                        this.todos.get(targetIndex).addSequence();
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
        if (ProgressStatus.ASSIGN.equals(todoDto.getStatus())) {
            this.todos.add(index, Todo.of(number
                    , todoDto.getDate()
                    , todoDto.getAssignee()
                    , taskLevel
                    , sequence
                    , todoDto.getTask()
                    , ProgressStatus.ASSIGN
                    , todoDto.getDescription()
                    , todoDto.getReporter() // 위임자
                    , todoDto.getLog() // 위임 전 값
            ));
        } else {
            ProgressStatus progressStatus = ProgressStatus.OPEN;
            if (ProgressStatus.CANCEL.equals(todoDto.getStatus())) {
                progressStatus = ProgressStatus.CANCEL;
            }

            this.todos.add(index, Todo.of(number
                    , todoDto.getDate()
                    , todoDto.getAssignee()
                    , taskLevel
                    , sequence
                    , todoDto.getTask()
                    , progressStatus
                    , todoDto.getDescription()
            ));
        }

        return number;
    }

    /**
     * 해야할일 번호로 데이터 추출
     * @param num
     * @return 해야할일
     */
    public Todo getTodo(int num) {
        // 해야할일의 번호는 1부터 부여 함
        if (0 < num) {
            boolean isExist = false;
            int index = 0;
            while (index < this.todos.size()) {
                if (num == this.todos.get(index).getNum()) {
                    isExist = true; // 번호가 일치하는 해야할일이 있음
                    break;
                }
                index++;
            }
    
            if (isExist) {
                return this.todos.get(index);
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
    public ArrayList<Todo> getTodos(TodoDto todoDto) {
        if (null == todoDto) { // 전체 조회
            return this.todos;
        } else {
            ArrayList<Todo> result = null;

            if (null != todoDto.getDate()) { // 특정 날짜에 해당하는 해야할일 조회
                result = (ArrayList<Todo>) this.todos.stream()
                        .filter(todo -> todoDto.getDate().equals(todo.getDate()))
                        .collect(Collectors.toList());
            }

            if (null != todoDto.getAssignee()) {
                // 특정 담당자에 해당하는 해야할일 조회
                if (null != result) {
                    // 특정 날짜조건도 있을 경우 상단 블럭(if)에 해당하여 조회한 결과에 담당자를 필터하여 결과 추출
                    result = (ArrayList<Todo>) result.stream()
                            .filter(todo -> todoDto.getAssignee().getNumber() == todo.getAssignee().getNumber())
                            .collect(Collectors.toList());
                } else {
                    result = (ArrayList<Todo>) this.todos.stream()
                            .filter(todo -> todoDto.getAssignee().getNumber() == todo.getAssignee().getNumber())
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
    public boolean removeTodo(int num) {
        boolean isValid = false; // 일치하는 번호가 위치한 인덱스가 유효한지(찾지못했을 경우 false)
        int index = 0; // 삭제 할 인덱스
        for (; index < this.todos.size(); index++) {
            if (num == this.todos.get(index).getNum()) {
                isValid = true;
                break;
            }
        }
        
        // 해야할일 삭제
        if (isValid) {
            TaskLevel taskLevel = this.todos.get(index).getTaskLevel(); // 삭제 대상 해야할일의 중요도
            this.todos.remove(index);

            // 삭제 후 인덱스(삭제 시 인덱스 -1)부터 동일 중요도 내 순서 -1
            while (index < this.todos.size()) {
                if (taskLevel.equals(this.todos.get(index).getTaskLevel())) { // 상단의 taskLevel 변수에 할당한 것과 다른 정보(삭제 됨)
                    // 동일 중요도의 순서 -1
                    this.todos.get(index).minusSequence();
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
     * @param todoDto
     * @return 결과
     */
    public void allocateAssignee(TodoDto todoDto) {
        if (null != todoDto && 0 != todoDto.getNum() && null != todoDto.getAssignee()) {
            Todo todo = this.getTodo(todoDto.getNum()); // 해야할일 조회
            if (null != todo) {
                this.removeTodo(todoDto.getNum()); // 해야할일 삭제
                TodoDto allocateTodo = new TodoDto();
                allocateTodo.setNum(todo.getNum()); // 번호는 고유값이므로 변경하지않음
                allocateTodo.setReporter(todo.getAssignee()); // 담당자를 위임자로 변경
                allocateTodo.setAssignee(todoDto.getAssignee()); // 새로운 담당자
                allocateTodo.setDate(todo.getDate());
                allocateTodo.setTask(todo.getTask());
                allocateTodo.setDescription(todo.getDescription());
                allocateTodo.setStatus(ProgressStatus.ASSIGN); // 위임
                
                // 위임취소 시 원상복구를 위한 값 보관
                HashMap<String, Object> log = new HashMap<>();
                log.put(Todo.KEY_TASK_LEVEL, todo.getTaskLevel());
                log.put(Todo.KEY_SEQ, todo.getSeq());
                allocateTodo.setLog(log);
                this.addTodo(allocateTodo); // 새로운 담당자의 해야할일로 추가
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
    public void cancelAllocatedTodo(int todoNum) {
        Todo todo = this.getTodo(todoNum); // 해야할일을 조회
        if (null == todo) {
            // 해야할지 유무 체크
            throw new HandledException(HttpStatus.BAD_REQUEST, ResponseStatus.FAIL02);
        }

        if ((null != todo.getReporter())) { // 위임받은게 맞는지 확인
            TodoDto todoDto = new TodoDto();
            todoDto.setNum(todo.getNum()); // 번호는 고유값이므로 변경하지않음
            todoDto.setAssignee(todo.getReporter()); // 위임자를 담당자로 변경
            todoDto.setDate(todo.getDate());
            todoDto.setTask(todo.getTask());
            todoDto.setDescription(todo.getDescription());
            todoDto.setStatus(ProgressStatus.CANCEL);

            // 위임취소의 경우 이전의 값(중요도, 순서)으로 변경
            // 내역이 없으면 새로운 우선순위로 변경(중요도와 순서를 부여하지 않음)
            if (null != todo.getLog()) {
                todoDto.setTaskLevel((TaskLevel) todo.getLog().get(Todo.KEY_TASK_LEVEL));
                todoDto.setSeq((int) todo.getLog().get(Todo.KEY_SEQ));
            }

            this.removeTodo(todo.getNum()); // 위임받은 해야할일 제거
            this.addTodo(todoDto);
        } else {
            // 번호와 일치하는 해야할일이 없으면 HandledException 발생
            throw new HandledException(HttpStatus.BAD_REQUEST, ResponseStatus.FAIL03);
        }
    }

    /**
     * 해야할일 정보(task, description, 중요도, 순서, 상태) 변경
     * @param todoDto
     */
    public void updateTodo(TodoDto todoDto) {
        Todo todo = this.getTodo(todoDto.getNum());
        if (null == todo) {
            // 일치하는 해야할일이 없으면 HandledException 발생
            throw new HandledException(HttpStatus.BAD_REQUEST, ResponseStatus.FAIL02);
        } else {
            // 우선순위가 변경되는지 확인
            boolean isUpdate = false;
            if ((null != todoDto.getTaskLevel()) || (null != todoDto.getSeq())) {
                // 현재 값과 비교
                if (!todo.getTaskLevel().equals(todoDto.getTaskLevel())) {
                    isUpdate = true;
                }
                if (!todo.getSeq().equals(todoDto.getSeq())) {
                    isUpdate = true;
                }
            }

            if (!isUpdate) {
                if ((null != todoDto.getTask()) && (!todo.getTask().equals(todoDto.getTask()))) { // task 변경
                    todo.setTask(todoDto.getTask());
                }
                if ((null != todoDto.getDescription()) && (!todo.getDescription().equals(todoDto.getDescription()))) { // description 변경
                    todo.setDescription(todoDto.getDescription());
                }
                if ((null != todoDto.getStatus()) && (!todo.getStatus().equals(todoDto.getStatus()))) { // 상태 변경
                    todo.setStatus(todoDto.getStatus());
                }
            } else {
                TodoDto updateDto = new TodoDto();
                updateDto.setNum(todo.getNum());
                updateDto.setDate(todo.getDate());
                updateDto.setAssignee(todo.getAssignee());
                // task 변경
                if (null != todoDto.getTask()) {
                    updateDto.setTask(todoDto.getTask());
                } else {
                    updateDto.setTask(todo.getTask());
                }
                // description 변경
                if (null != todoDto.getDescription()) {
                    updateDto.setDescription(todoDto.getDescription());
                } else {
                    updateDto.setDescription(todo.getDescription());
                }
                // 상태 변경
                if (null != todoDto.getStatus()) {
                    updateDto.setStatus(todoDto.getStatus());
                } else {
                    updateDto.setStatus(todo.getStatus());
                }
                // 중요도
                if (null != todoDto.getTaskLevel()) {
                    updateDto.setTaskLevel(todoDto.getTaskLevel());
                } else {
                    updateDto.setTaskLevel(todo.getTaskLevel());
                }
                // 순서
                if (null != todoDto.getSeq()) {
                    updateDto.setSeq(todoDto.getSeq());
                } else {
                    updateDto.setSeq(todo.getSeq());
                }

                this.removeTodo(todo.getNum());
                this.addTodo(updateDto);
            }
        }
    }
}
