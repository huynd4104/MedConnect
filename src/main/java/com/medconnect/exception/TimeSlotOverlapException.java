package com.medconnect.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT) // 409 Conflict
public class TimeSlotOverlapException extends RuntimeException {
    public TimeSlotOverlapException(String message) {
        super(message);
    }
}