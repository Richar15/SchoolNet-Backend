package com.RichardDev.SchoolNet.service.exeption;

public class UniqueFieldException extends RuntimeException {
    public UniqueFieldException(String message) {
        super(message);
    }
}
