package com.RichardDev.SchoolNet.persistence.entity;

import com.RichardDev.SchoolNet.constant.Grade;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "schedules")
@Getter
@Setter
@NoArgsConstructor
public class ScheduleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private Grade grade;

    @OneToMany(mappedBy = "schedule", cascade = CascadeType.ALL, orphanRemoval = true)

    private List<SessionEntity> sessions;

    public ScheduleEntity(Grade grade, List<SessionEntity> sessions) {
        this.grade = grade;
        this.sessions = sessions;
        if (sessions != null) {
            sessions.forEach(session -> session.setSchedule(this));
        }
    }

    public void setSessions(List<SessionEntity> sessions) {
        this.sessions = sessions;
        if (this.sessions != null) {
            this.sessions.forEach(session -> session.setSchedule(this));
        }
    }
}
