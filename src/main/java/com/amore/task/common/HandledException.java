package com.amore.task.common;

import com.amore.task.model.enums.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

/**
 * 의도적으로 발생시킨 Exception
 */
@Getter
@Setter
public class HandledException extends RuntimeException {
    /** HTTP Status */
    public HttpStatus httpStatus;

    /** 메시지 */
    public String message;

    /** Exception 원인 */
    public ResponseStatus responseStatus;

    public HandledException(HttpStatus httpStatus, ResponseStatus responseStatus) {
        super(responseStatus.getMessage());
        this.setHttpStatus(httpStatus);
        this.setResponseStatus(responseStatus);
    }

    public HandledException(HttpStatus httpStatus, String message) {
        super(message);
        this.setHttpStatus(httpStatus);
        this.setMessage(message);
    }
}
