package com.RichardDev.SchoolNet.presentation.controller;

import com.RichardDev.SchoolNet.constant.Grade;
import com.RichardDev.SchoolNet.persistence.entity.ScheduleEntity;
import com.RichardDev.SchoolNet.service.implementation.ScheduleServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/schedules")
@AllArgsConstructor
public class ScheduleController {

    private final ScheduleServiceImpl scheduleService;


    @PostMapping("/{grade}")
    public ResponseEntity<ScheduleEntity> createSchedule(@PathVariable Grade grade) {
        ScheduleEntity schedule = scheduleService.createSchedule(grade);
        return ResponseEntity.ok(schedule);
    }


    @GetMapping("/{grade}")
    public ResponseEntity<List<ScheduleEntity>> getSchedulesByGrade(@PathVariable Grade grade) {
        List<ScheduleEntity> schedules = scheduleService.getSchedulesByGrade(grade);
        return ResponseEntity.ok(schedules);
    }

    @GetMapping
    public ResponseEntity<List<ScheduleEntity>> getAllSchedules() {
        List<ScheduleEntity> schedules = scheduleService.getAllSchedules();
        return ResponseEntity.ok(schedules);
    }

    @GetMapping("/stats/{id}")
    public ResponseEntity<Map<String, Object>> getScheduleStatistics(@PathVariable Long id) {
        ScheduleEntity schedule = scheduleService.getAllSchedules().stream()
                .filter(s -> s.getId().equals(id))
                .findFirst()
                .orElse(null);

        if (schedule == null) {
            return ResponseEntity.notFound().build();
        }

        Map<String, Object> stats = scheduleService.getScheduleStatistics(schedule);
        return ResponseEntity.ok(stats);
    }

    @DeleteMapping("/{grade}")
    public ResponseEntity<Void> deleteSchedulesByGrade(@PathVariable Grade grade) {
        boolean deleted = scheduleService.deleteSchedulesByGrade(grade);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteAllSchedules() {
        boolean deleted = scheduleService.deleteAllSchedules();
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
