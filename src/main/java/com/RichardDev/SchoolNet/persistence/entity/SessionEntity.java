package com.RichardDev.SchoolNet.persistence.entity;

import com.RichardDev.SchoolNet.constant.Subject;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SessionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String day;
    private String start;
    @Column(name = "end_time")
    private String end;

    @Enumerated(EnumType.STRING)
    private Subject subject;

    private String teacher;

    private String classroom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    private ScheduleEntity schedule;

    public SessionEntity(String day, String start, String end, Subject subject,String classroom, String teacher) {
        this.day = day;
        this.start = start;
        this.end = end;
        this.subject = subject;
        this.classroom = classroom;
        this.teacher = teacher;
    }
}
