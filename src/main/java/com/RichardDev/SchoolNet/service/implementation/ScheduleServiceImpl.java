package com.RichardDev.SchoolNet.service.implementation;

import com.RichardDev.SchoolNet.constant.Grade;
import com.RichardDev.SchoolNet.constant.Subject;
import com.RichardDev.SchoolNet.persistence.entity.ScheduleEntity;
import com.RichardDev.SchoolNet.persistence.entity.SessionEntity;
import com.RichardDev.SchoolNet.persistence.entity.TeacherEntity;
import com.RichardDev.SchoolNet.persistence.repository.ScheduleRepository;
import com.RichardDev.SchoolNet.persistence.repository.TeacherRepository;
import com.RichardDev.SchoolNet.service.exeption.TeacherScheduleConflictException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class ScheduleServiceImpl {

    // Modificado para incluir todas las materias en todos los grados
    private static final Map<Grade, List<Subject>> GRADE_SUBJECTS = Map.ofEntries(
            Map.entry(Grade.SEXTO, Arrays.asList(Subject.values())),
            Map.entry(Grade.SEPTIMO, Arrays.asList(Subject.values())),
            Map.entry(Grade.OCTAVO, Arrays.asList(Subject.values())),
            Map.entry(Grade.NOVENO, Arrays.asList(Subject.values())),
            Map.entry(Grade.DECIMO, Arrays.asList(Subject.values())),
            Map.entry(Grade.UNDECIMO, Arrays.asList(Subject.values()))
    );

    private static final Map<Grade, String> GRADE_CLASSROOMS = Map.ofEntries(
            Map.entry(Grade.SEXTO, "Aula 101"),
            Map.entry(Grade.SEPTIMO, "Aula 201"),
            Map.entry(Grade.OCTAVO, "Aula 301"),
            Map.entry(Grade.NOVENO, "Aula 401"),
            Map.entry(Grade.DECIMO, "Aula 501"),
            Map.entry(Grade.UNDECIMO, "Aula 601")
    );

    private static final Map<Subject, String> SPECIAL_CLASSROOMS = Map.ofEntries(
            Map.entry(Subject.EDUCACION_FISICA, "Gimnasio"),
            Map.entry(Subject.ARTES, "Salón de Arte"),
            Map.entry(Subject.INFORMATICA, "Laboratorio de Computación"),
            Map.entry(Subject.FISICA, "Laboratorio de Física"),
            Map.entry(Subject.QUIMICA, "Laboratorio de Química"),
            Map.entry(Subject.BIOLOGIA, "Laboratorio de Biología")
    );

    private static final int MAX_HOURS_PER_TEACHER_PER_DAY = 6;
    private static final int MAX_TOTAL_HOURS_PER_TEACHER = 25;
    private static final String START_TIME = "08:00";
    private static final int SESSIONS_PER_DAY = 6;
    private static final int RECREO_AFTER_SESSION = 3;
    private static final int RECREO_DURATION_MINUTES = 30;
    private static final int MIN_DAYS_PER_SUBJECT = 2;
    private static final int MAX_HOURS_PER_SUBJECT_PER_DAY = 2;

    private final List<String> weekDays = Arrays.asList("Lunes", "Martes", "Miércoles", "Jueves", "Viernes");

    private final TeacherRepository teacherRepository;
    private final ScheduleRepository scheduleRepository;

    private static class TeacherAvailabilityTracker {
        private final Map<String, Set<String>> teacherScheduleMap = new ConcurrentHashMap<>();
        private final Map<String, Integer> teacherHourCount = new ConcurrentHashMap<>();

        public synchronized boolean assignTimeSlot(String teacherName, String timeSlot) {
            Set<String> assignments = teacherScheduleMap.getOrDefault(teacherName, new HashSet<>());
            int currentHours = teacherHourCount.getOrDefault(teacherName, 0);

            if (assignments.contains(timeSlot)) {
                return false;
            }

            if (currentHours >= MAX_TOTAL_HOURS_PER_TEACHER) {
                return false;
            }

            String day = timeSlot.split("_")[0];
            long dailyHours = assignments.stream()
                    .filter(slot -> slot.startsWith(day + "_"))
                    .count();

            if (dailyHours >= MAX_HOURS_PER_TEACHER_PER_DAY) {
                return false;
            }

            assignments.add(timeSlot);
            teacherScheduleMap.put(teacherName, assignments);
            teacherHourCount.put(teacherName, currentHours + 1);
            return true;
        }

        public void clear() {
            teacherScheduleMap.clear();
            teacherHourCount.clear();
        }

        public Map<String, Integer> getTeacherHourCounts() {
            return new HashMap<>(teacherHourCount);
        }
    }

    private Map<String, String> calculateSessionTime(int sessionIndex, int startHour) {
        if (sessionIndex >= RECREO_AFTER_SESSION) {
            int minutesAdjustment = RECREO_DURATION_MINUTES;
            int hoursAdjustment = minutesAdjustment / 60;
            int remainingMinutes = minutesAdjustment % 60;

            int startHourValue = startHour + sessionIndex + hoursAdjustment;
            int startMinuteValue = remainingMinutes;
            int endHourValue = startHourValue + 1;
            int endMinuteValue = remainingMinutes;

            String startTime = String.format("%02d:%02d", startHourValue, startMinuteValue);
            String endTime = String.format("%02d:%02d", endHourValue, endMinuteValue);

            return Map.of("startTime", startTime, "endTime", endTime);
        } else {
            String startTime = String.format("%02d:00", startHour + sessionIndex);
            String endTime = String.format("%02d:00", startHour + sessionIndex + 1);

            return Map.of("startTime", startTime, "endTime", endTime);
        }
    }

    private String getClassroomForGradeAndSubject(Grade grade, Subject subject) {
        if (SPECIAL_CLASSROOMS.containsKey(subject)) {
            return SPECIAL_CLASSROOMS.get(subject);
        }
        return GRADE_CLASSROOMS.getOrDefault(grade, "Aula sin asignar");
    }

    private String assignTeacher(String day, String startTime, Subject subject,
                               TeacherAvailabilityTracker tracker) {
        String timeSlot = day + "_" + startTime;
        List<TeacherEntity> teachers = teacherRepository.findByAreaOfExpertise(subject);

        if (teachers.isEmpty()) {
            log.warn("No se encontraron profesores para la materia: {}", subject);
            throw new TeacherScheduleConflictException(
                    "No hay profesores registrados con experiencia en " + subject.name());
        }

        for (TeacherEntity teacher : teachers) {
            if (tracker.assignTimeSlot(teacher.getName(), timeSlot)) {
                log.debug("Profesor {} asignado para {} en {}", teacher.getName(), subject, timeSlot);
                return teacher.getName();
            }
        }

        throw new TeacherScheduleConflictException(
                String.format("No hay profesores disponibles con experiencia en %s para %s",
                        subject.name(), timeSlot));
    }

    private List<Subject> getSubjectsForGrade(Grade grade) {
        return GRADE_SUBJECTS.getOrDefault(grade, Arrays.asList(Subject.values()));
    }

    /**
     * Genera un plan semanal completo asegurando que todas las materias se ven
     * en al menos dos días diferentes y máximo 2 horas por día.
     */
    private Map<String, List<Subject>> generateBalancedWeeklyPlan(Grade grade) {
        List<Subject> gradeSubjects = new ArrayList<>(getSubjectsForGrade(grade));
        Map<String, List<Subject>> weeklyPlan = new HashMap<>();

        // Inicializar días vacíos
        for (String day : weekDays) {
            weeklyPlan.put(day, new ArrayList<>());
        }

        // Estructuras para seguimiento de distribución
        Map<Subject, Set<String>> subjectDaysMap = new HashMap<>();
        Map<String, Map<Subject, Integer>> subjectCountPerDay = new HashMap<>();

        // Inicializar estructuras de seguimiento
        for (Subject subject : gradeSubjects) {
            subjectDaysMap.put(subject, new HashSet<>());
        }

        for (String day : weekDays) {
            subjectCountPerDay.put(day, new HashMap<>());
            for (Subject subject : gradeSubjects) {
                subjectCountPerDay.get(day).put(subject, 0);
            }
        }

        // Priorizar materias clave para distribución inicial
        List<Subject> prioritySubjects = Arrays.asList(
                Subject.MATEMATICAS, Subject.ESPANOL, Subject.HISTORIA,
                Subject.GEOGRAFIA, Subject.INGLES, Subject.FISICA,
                Subject.QUIMICA, Subject.BIOLOGIA, Subject.FILOSOFIA,
                Subject.ECONOMIA, Subject.LITERATURA
        );

        List<Subject> prioritizedSubjects = new ArrayList<>();
        for (Subject subject : prioritySubjects) {
            if (gradeSubjects.contains(subject)) {
                prioritizedSubjects.add(subject);
            }
        }

        // Agregar el resto de materias que no están en la lista de prioridades
        for (Subject subject : gradeSubjects) {
            if (!prioritizedSubjects.contains(subject)) {
                prioritizedSubjects.add(subject);
            }
        }

        // PASO 1: Distribuir cada materia en al menos 2 días diferentes
        for (Subject subject : prioritizedSubjects) {
            // Elegir al menos MIN_DAYS_PER_SUBJECT días diferentes para cada materia
            List<String> availableDays = new ArrayList<>(weekDays);
            Collections.shuffle(availableDays);

            int daysAssigned = 0;
            for (String day : availableDays) {
                if (daysAssigned >= MIN_DAYS_PER_SUBJECT) break;

                List<Subject> dayPlan = weeklyPlan.get(day);
                Map<Subject, Integer> daySubjectCount = subjectCountPerDay.get(day);

                // Solo asignar si hay espacio y no excede el límite diario
                if (dayPlan.size() < SESSIONS_PER_DAY &&
                    daySubjectCount.get(subject) < MAX_HOURS_PER_SUBJECT_PER_DAY) {

                    dayPlan.add(subject);
                    subjectDaysMap.get(subject).add(day);
                    daySubjectCount.put(subject, daySubjectCount.get(subject) + 1);
                    daysAssigned++;
                }
            }
        }

        // PASO 2: Formar bloques de 2 horas para materias principales cuando sea posible
        for (String day : weekDays) {
            List<Subject> dayPlan = weeklyPlan.get(day);
            Map<Subject, Integer> daySubjectCount = subjectCountPerDay.get(day);

            for (int i = 0; i < dayPlan.size(); i++) {
                Subject subject = dayPlan.get(i);

                // Si ya hay una aparición y podemos añadir otra (para formar un bloque de 2)
                if (daySubjectCount.get(subject) == 1 && dayPlan.size() < SESSIONS_PER_DAY) {
                    dayPlan.add(subject);
                    daySubjectCount.put(subject, daySubjectCount.get(subject) + 1);
                }
            }
        }

        // PASO 3: Completar el horario con las materias menos utilizadas
        boolean changes;
        do {
            changes = false;

            for (String day : weekDays) {
                List<Subject> dayPlan = weeklyPlan.get(day);
                Map<Subject, Integer> daySubjectCount = subjectCountPerDay.get(day);

                while (dayPlan.size() < SESSIONS_PER_DAY) {
                    // Encontrar la materia que puede añadirse a este día
                    Subject bestSubject = findBestSubjectForDay(
                            weeklyPlan, day, subjectDaysMap, subjectCountPerDay, prioritizedSubjects);

                    if (bestSubject != null) {
                        dayPlan.add(bestSubject);
                        daySubjectCount.put(bestSubject, daySubjectCount.get(bestSubject) + 1);
                        subjectDaysMap.get(bestSubject).add(day);
                        changes = true;
                    } else {
                        // Si no encontramos ninguna materia adecuada, usar la primera disponible
                        for (Subject subject : prioritizedSubjects) {
                            if (daySubjectCount.get(subject) < MAX_HOURS_PER_SUBJECT_PER_DAY) {
                                dayPlan.add(subject);
                                daySubjectCount.put(subject, daySubjectCount.get(subject) + 1);
                                subjectDaysMap.get(subject).add(day);
                                changes = true;
                                break;
                            }
                        }
                    }

                    // Si no pudimos añadir ninguna materia, salir del bucle
                    if (!changes) break;
                }
            }
        } while (changes && hasIncompleteDay(weeklyPlan));

        // Validación final
        validateFinalSchedule(weeklyPlan, subjectDaysMap, subjectCountPerDay, prioritizedSubjects);

        return weeklyPlan;
    }

    private void validateFinalSchedule(
            Map<String, List<Subject>> weeklyPlan,
            Map<Subject, Set<String>> subjectDaysMap,
            Map<String, Map<Subject, Integer>> subjectCountPerDay,
            List<Subject> allSubjects) {

        StringBuilder warnings = new StringBuilder();

        // Verificar que cada materia aparezca en el horario
        for (Subject subject : allSubjects) {
            int totalHours = 0;
            for (String day : weekDays) {
                totalHours += subjectCountPerDay.get(day).getOrDefault(subject, 0);
            }

            if (totalHours == 0) {
                warnings.append("ALERTA: La materia ").append(subject)
                       .append(" no aparece en el horario\n");
            } else if (subjectDaysMap.get(subject).size() < MIN_DAYS_PER_SUBJECT) {
                warnings.append("Advertencia: ").append(subject)
                       .append(" solo se ve en ").append(subjectDaysMap.get(subject).size())
                       .append(" días diferentes (mínimo recomendado: ").append(MIN_DAYS_PER_SUBJECT).append(")\n");
            }
        }

        if (warnings.length() > 0) {
            log.warn(warnings.toString());
        }
    }

    private boolean hasIncompleteDay(Map<String, List<Subject>> weeklyPlan) {
        return weeklyPlan.values().stream().anyMatch(day -> day.size() < SESSIONS_PER_DAY);
    }

    private Subject findBestSubjectForDay(
            Map<String, List<Subject>> weeklyPlan,
            String day,
            Map<Subject, Set<String>> subjectDaysMap,
            Map<String, Map<Subject, Integer>> subjectCountPerDay,
            List<Subject> allSubjects) {

        Map<Subject, Integer> daySubjectCount = subjectCountPerDay.get(day);

        return allSubjects.stream()
            .filter(subject -> daySubjectCount.get(subject) < MAX_HOURS_PER_SUBJECT_PER_DAY)
            .sorted((s1, s2) -> {
                // Priorizar materias que no aparecen en el mínimo de días
                int days1 = subjectDaysMap.get(s1).size();
                int days2 = subjectDaysMap.get(s2).size();

                if ((days1 < MIN_DAYS_PER_SUBJECT) != (days2 < MIN_DAYS_PER_SUBJECT)) {
                    return Boolean.compare(days2 < MIN_DAYS_PER_SUBJECT, days1 < MIN_DAYS_PER_SUBJECT);
                }

                // Priorizar materias con menos horas totales
                int total1 = getTotalAppearances(s1, subjectCountPerDay);
                int total2 = getTotalAppearances(s2, subjectCountPerDay);

                return Integer.compare(total1, total2);
            })
            .findFirst()
            .orElse(null);
    }

    private int getTotalAppearances(Subject subject, Map<String, Map<Subject, Integer>> subjectCountPerDay) {
        return subjectCountPerDay.values().stream()
                .mapToInt(dayCount -> dayCount.getOrDefault(subject, 0))
                .sum();
    }

    @Transactional
    public ScheduleEntity createSchedule(Grade grade) {
        validateTeacherAvailability();

        List<SessionEntity> sessionEntities = new ArrayList<>();
        TeacherAvailabilityTracker tracker = new TeacherAvailabilityTracker();

        Map<String, List<Subject>> weeklyPlan = generateBalancedWeeklyPlan(grade);
        int startHour = Integer.parseInt(START_TIME.split(":")[0]);

        try {
            for (String day : weekDays) {
                // Añadir el recreo
                if (RECREO_AFTER_SESSION > 0 && RECREO_AFTER_SESSION < SESSIONS_PER_DAY) {
                    String recreoStartTime = String.format("%02d:00", startHour + RECREO_AFTER_SESSION);
                    String recreoEndTime = String.format("%02d:%02d", startHour + RECREO_AFTER_SESSION, RECREO_DURATION_MINUTES);

                    SessionEntity recreoSession = new SessionEntity(
                            day, recreoStartTime, recreoEndTime, null, "Patio Central", "RECREO"
                    );
                    sessionEntities.add(recreoSession);
                }

                List<Subject> daySubjects = weeklyPlan.get(day);

                for (int sessionIndex = 0; sessionIndex < SESSIONS_PER_DAY; sessionIndex++) {
                    Map<String, String> times = calculateSessionTime(sessionIndex, startHour);
                    String startTime = times.get("startTime");
                    String endTime = times.get("endTime");

                    Subject subject = daySubjects.get(sessionIndex);
                    String teacherName = assignTeacher(day, startTime, subject, tracker);
                    String classroom = getClassroomForGradeAndSubject(grade, subject);

                    SessionEntity session = new SessionEntity(day, startTime, endTime, subject, classroom, teacherName);
                    sessionEntities.add(session);
                }
            }

            ScheduleEntity scheduleEntity = new ScheduleEntity(grade, sessionEntities);
            ScheduleEntity savedSchedule = scheduleRepository.save(scheduleEntity);

            log.info("Horario creado para el grado: {}. Sesiones: {}", grade, sessionEntities.size());
            logTeacherWorkload(tracker);

            return savedSchedule;

        } catch (Exception e) {
            log.error("Error al crear horario para grado {}: {}", grade, e.getMessage());
            throw e;
        }
    }



    private ScheduleEntity createScheduleWithTracker(Grade grade, TeacherAvailabilityTracker tracker) {
        List<SessionEntity> sessionEntities = new ArrayList<>();
        Map<String, List<Subject>> weeklyPlan = generateBalancedWeeklyPlan(grade);
        int startHour = Integer.parseInt(START_TIME.split(":")[0]);

        for (String day : weekDays) {
            // Añadir el recreo
            if (RECREO_AFTER_SESSION > 0 && RECREO_AFTER_SESSION < SESSIONS_PER_DAY) {
                String recreoStartTime = String.format("%02d:00", startHour + RECREO_AFTER_SESSION);
                String recreoEndTime = String.format("%02d:%02d", startHour + RECREO_AFTER_SESSION, RECREO_DURATION_MINUTES);

                SessionEntity recreoSession = new SessionEntity(
                        day, recreoStartTime, recreoEndTime, null, "Patio Central", "RECREO"
                );
                sessionEntities.add(recreoSession);
            }

            List<Subject> daySubjects = weeklyPlan.get(day);

            for (int sessionIndex = 0; sessionIndex < SESSIONS_PER_DAY; sessionIndex++) {
                Map<String, String> times = calculateSessionTime(sessionIndex, startHour);
                String startTime = times.get("startTime");
                String endTime = times.get("endTime");

                Subject subject = daySubjects.get(sessionIndex);
                String teacherName = assignTeacher(day, startTime, subject, tracker);
                String classroom = getClassroomForGradeAndSubject(grade, subject);

                SessionEntity session = new SessionEntity(day, startTime, endTime, subject, classroom, teacherName);
                sessionEntities.add(session);
            }
        }

        ScheduleEntity scheduleEntity = new ScheduleEntity(grade, sessionEntities);
        return scheduleRepository.save(scheduleEntity);
    }

    private void validateTeacherAvailability() {
        long teacherCount = teacherRepository.count();
        if (teacherCount == 0) {
            throw new TeacherScheduleConflictException("No hay profesores registrados para generar horarios");
        }

        List<Subject> criticalSubjects = Arrays.asList(
                Subject.MATEMATICAS, Subject.ESPANOL
        );

        for (Subject subject : criticalSubjects) {
            List<TeacherEntity> teachers = teacherRepository.findByAreaOfExpertise(subject);
            if (teachers.isEmpty()) {
                log.warn("No hay profesores disponibles para la materia crítica: {}", subject);
            }
        }

        log.info("Validación completada. Total de profesores: {}", teacherCount);
    }

    private void logTeacherWorkload(TeacherAvailabilityTracker tracker) {
        Map<String, Integer> workload = tracker.getTeacherHourCounts();
        log.info("Carga de trabajo de profesores:");
        workload.forEach((teacher, hours) ->
                log.info("- {}: {} horas semanales", teacher, hours));
    }

    public Map<String, Object> getScheduleStatistics(ScheduleEntity schedule) {
        Map<String, Object> stats = new HashMap<>();

        if (schedule == null || schedule.getSessions() == null) {
            return stats;
        }

        List<SessionEntity> sessions = schedule.getSessions();

        Map<Subject, Long> subjectCount = sessions.stream()
                .filter(s -> s.getSubject() != null)
                .collect(Collectors.groupingBy(
                        SessionEntity::getSubject,
                        Collectors.counting()
                ));

        Map<String, Long> teacherCount = sessions.stream()
                .filter(s -> !s.getTeacher().equals("RECREO"))
                .collect(Collectors.groupingBy(
                        SessionEntity::getTeacher,
                        Collectors.counting()
                ));

        stats.put("totalSessions", sessions.size());
        stats.put("subjectDistribution", subjectCount);
        stats.put("teacherDistribution", teacherCount);
        stats.put("grade", schedule.getGrade());

        return stats;
    }

    @Transactional(readOnly = true)
    public List<ScheduleEntity> getSchedulesByGrade(Grade grade) {
        return scheduleRepository.findByGrade(grade);
    }

    @Transactional(readOnly = true)
    public List<ScheduleEntity> getAllSchedules() {
        return scheduleRepository.findAll();
    }

    @Transactional
    public boolean deleteSchedulesByGrade(Grade grade) {
        List<ScheduleEntity> schedules = scheduleRepository.findByGrade(grade);
        if (!schedules.isEmpty()) {
            scheduleRepository.deleteAll(schedules);
            log.info("Eliminados {} horarios para el grado {}", schedules.size(), grade);
            return true;
        }
        return false;
    }

    @Transactional
    public boolean deleteAllSchedules() {
        long count = scheduleRepository.count();
        if (count > 0) {
            scheduleRepository.deleteAll();
            log.info("Eliminados {} horarios en total", count);
            return true;
        }
        return false;
    }
}