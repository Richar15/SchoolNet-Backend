package com.RichardDev.SchoolNet.service.exeption;

public class TeacherScheduleConflictException extends RuntimeException {
    public TeacherScheduleConflictException(String message) {
        super(message);
    }
  public TeacherScheduleConflictException(String message, Throwable cause) {
    super(message, cause);
  }
}
