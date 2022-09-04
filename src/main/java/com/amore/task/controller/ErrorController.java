package com.amore.task.controller;

import com.amore.task.common.HandledException;
import com.amore.task.common.ResponseMessage;
import com.amore.task.model.enums.ResponseStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

@Slf4j
@RestControllerAdvice
public class ErrorController {

    /** 의도적으로 발생시킨 Exception */
    @ExceptionHandler(HandledException.class)
    public ResponseEntity<ResponseMessage> exceptionHandler(HandledException e) {
        log.error("HandledException:: {} - {}", e.getClass(), e.getMessage());
        if (log.isDebugEnabled()) {
            e.printStackTrace();
        }
        return new ResponseEntity<ResponseMessage>(
                new ResponseMessage(e.getResponseStatus().getCode(), e.getResponseStatus().getMessage(), ""), e.getHttpStatus());
    }

    /** 404 에러처리 */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ResponseMessage> exceptionHandler(NoHandlerFoundException e) {
        log.error("NoHandlerFoundException:: {} - {}", e.getClass(), e.getMessage());
        if (log.isDebugEnabled()) {
            e.printStackTrace();
        }
        return new ResponseEntity<ResponseMessage>(
                new ResponseMessage(ResponseStatus.FAIL.getCode(), "페이지를 찾을 수 없습니다.", ""), HttpStatus.NOT_FOUND);
    }

    /** 그 외 Exception */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseMessage> exceptionHandler(Exception e) {
        log.error("Exception:: {} - {}", e.getClass(), e.getMessage());
        if (log.isDebugEnabled()) {
            e.printStackTrace();
        }
        return new ResponseEntity<ResponseMessage>(
                new ResponseMessage(ResponseStatus.FAIL.getCode(), e.getMessage(), ""), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
