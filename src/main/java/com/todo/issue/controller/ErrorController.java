package com.todo.issue.controller;

import com.todo.issue.common.HandledException;
import com.todo.issue.common.ResponseMessage;
import com.todo.issue.model.enums.ResponseStatus;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

@RestControllerAdvice
public class ErrorController {

    /** 의도적으로 발생시킨 Exception */
    @ExceptionHandler(HandledException.class)
    public ResponseEntity<ResponseMessage> exceptionHandler(HandledException e) {
        return new ResponseEntity<ResponseMessage>(
                new ResponseMessage(e.getResponseStatus().getCode(), e.getResponseStatus().getMessage(), ""),
                e.getHttpStatus());
    }

    /** 404 에러처리 */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ResponseMessage> exceptionHandler(NoHandlerFoundException e) {
        return new ResponseEntity<ResponseMessage>(
                new ResponseMessage(ResponseStatus.FAIL.getCode(), "페이지를 찾을 수 없습니다.", ""),
                HttpStatus.NOT_FOUND);
    }

    /** 그 외 Exception */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseMessage> exceptionHandler(Exception e) {
        return new ResponseEntity<ResponseMessage>(
                new ResponseMessage(ResponseStatus.FAIL.getCode(), e.getMessage(), ""),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
