package ru.rmamedov.courses.exception.course;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_ACCEPTABLE)
public class CourseNotSavedException extends RuntimeException {

    public CourseNotSavedException(String message) {
        super(message);
    }
}
