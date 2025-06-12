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

@Service
@AllArgsConstructor
@Slf4j
public class ScheduleServiceImpl {

    private static final Map<Grade, List<Subject>> GRADE_SUBJECTS = Map.ofEntries(
        Map.entry(Grade.SEXTO, Arrays.asList(
                Subject.MATEMATICAS, Subject.ESPANOL, Subject.CIENCIAS, Subject.ARTES,
                Subject.EDUCACION_FISICA, Subject.MUSICA
        )),
        Map.entry(Grade.SEPTIMO, Arrays.asList(
                Subject.MATEMATICAS, Subject.ESPANOL, Subject.CIENCIAS, Subject.HISTORIA,
                Subject.ARTES, Subject.EDUCACION_FISICA
        )),
        Map.entry(Grade.OCTAVO, Arrays.asList(
                Subject.MATEMATICAS, Subject.ESPANOL, Subject.CIENCIAS, Subject.HISTORIA,
                Subject.GEOGRAFIA, Subject.INGLES
        )),
        Map.entry(Grade.NOVENO, Arrays.asList(
                Subject.MATEMATICAS, Subject.ESPANOL, Subject.CIENCIAS, Subject.HISTORIA,
                Subject.GEOGRAFIA, Subject.INGLES, Subject.ARTES, Subject.EDUCACION_FISICA
        )),
        Map.entry(Grade.DECIMO, Arrays.asList(
                Subject.MATEMATICAS, Subject.ESPANOL, Subject.CIENCIAS, Subject.HISTORIA,
                Subject.GEOGRAFIA, Subject.INGLES, Subject.FISICA, Subject.QUIMICA
        )),
        Map.entry(Grade.UNDECIMO, Arrays.asList(
                Subject.MATEMATICAS, Subject.ESPANOL, Subject.CIENCIAS, Subject.HISTORIA,
                Subject.GEOGRAFIA, Subject.INGLES, Subject.FISICA, Subject.QUIMICA
        ))
    );

    // Definir salones fijos por grado
    private static final Map<Grade, String> GRADE_CLASSROOMS = Map.ofEntries(
        Map.entry(Grade.SEXTO, "Aula 101"),
        Map.entry(Grade.SEPTIMO, "Aula 201"),
        Map.entry(Grade.OCTAVO, "Aula 301"),
        Map.entry(Grade.NOVENO, "Aula 401"),
        Map.entry(Grade.DECIMO, "Aula 501"),
        Map.entry(Grade.UNDECIMO, "Aula 601")
    );

    // Aulas especiales para materias específicas
    private static final Map<Subject, String> SPECIAL_CLASSROOMS = Map.ofEntries(
        Map.entry(Subject.EDUCACION_FISICA, "Gimnasio"),
        Map.entry(Subject.ARTES, "Salón de Arte"),
        Map.entry(Subject.MUSICA, "Aula de Música"),
        Map.entry(Subject.INFORMATICA, "Laboratorio de Computación"),
        Map.entry(Subject.FISICA, "Laboratorio de Física"),
        Map.entry(Subject.QUIMICA, "Laboratorio de Química"),
        Map.entry(Subject.BIOLOGIA, "Laboratorio de Biología")
    );

    private final List<Subject> defaultSubjects = Arrays.asList(
            Subject.MATEMATICAS, Subject.CIENCIAS, Subject.HISTORIA, Subject.GEOGRAFIA,
            Subject.INGLES, Subject.FISICA, Subject.QUIMICA, Subject.BIOLOGIA,
            Subject.ARTES, Subject.MUSICA, Subject.INFORMATICA, Subject.FILOSOFIA,
            Subject.ECONOMIA, Subject.SOCIOLOGIA, Subject.EDUCACION_FISICA,
            Subject.LITERATURA, Subject.ESPANOL, Subject.FRANCES, Subject.SALUD,
            Subject.TECNOLOGIA
    );

    private static final int MAX_HOURS_PER_TEACHER_PER_DAY = 6;
    private static final int MAX_TOTAL_HOURS_PER_TEACHER = 25;
    private static final String START_TIME = "08:00";
    private static final int SESSIONS_PER_DAY = 6;
    private static final int RECREO_AFTER_SESSION = 3; // Recreo después de la 3ra hora
    private static final int RECREO_DURATION_MINUTES = 30; // Duración del recreo en minutos

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

    // Método para calcular la hora de inicio y fin de una sesión considerando el recreo
    private Map<String, String> calculateSessionTime(int sessionIndex, int startHour) {
        int adjustedSessionIndex = sessionIndex;
        int timeAdjustment = 0;

        // Si estamos después del recreo, ajustamos el índice y añadimos el tiempo del recreo
        if (sessionIndex >= RECREO_AFTER_SESSION) {
            timeAdjustment = RECREO_DURATION_MINUTES / 60; // Ajuste en horas
            int minutesAdjustment = RECREO_DURATION_MINUTES % 60; // Ajuste en minutos

            int startHourValue = startHour + sessionIndex + timeAdjustment;
            int endHourValue = startHour + sessionIndex + timeAdjustment + 1;

            String startTime = String.format("%02d:%02d", startHourValue, minutesAdjustment);
            String endTime = String.format("%02d:%02d", endHourValue, minutesAdjustment);

            return Map.of("startTime", startTime, "endTime", endTime);
        } else {
            // Para sesiones antes del recreo, el cálculo es normal
            String startTime = String.format("%02d:00", startHour + sessionIndex);
            String endTime = String.format("%02d:00", startHour + sessionIndex + 1);

            return Map.of("startTime", startTime, "endTime", endTime);
        }
    }

    // Método para obtener el salón para un grado y materia
    private String getClassroomForGradeAndSubject(Grade grade, Subject subject) {
        // Si la materia requiere un aula especializada, usamos esa
        if (SPECIAL_CLASSROOMS.containsKey(subject)) {
            return SPECIAL_CLASSROOMS.get(subject);
        }

        // De lo contrario, usamos el aula regular del grado
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
            if (teacher.getAreaOfExpertise() != null &&
                    teacher.getAreaOfExpertise().equals(subject)) {

                if (tracker.assignTimeSlot(teacher.getName(), timeSlot)) {
                    log.debug("Profesor {} asignado para {} en {}",
                            teacher.getName(), subject, timeSlot);
                    return teacher.getName();
                }
            }
        }

        throw new TeacherScheduleConflictException(
                String.format("No hay profesores disponibles con experiencia en %s para %s",
                        subject.name(), timeSlot));
    }

    private List<Subject> getSubjectsForGrade(Grade grade) {
        return GRADE_SUBJECTS.getOrDefault(grade, defaultSubjects);
    }

    private List<Subject> distributeSubjects(Grade grade) {
        List<Subject> gradeSubjects = getSubjectsForGrade(grade);
        List<Subject> distributedSubjects = new ArrayList<>();

        int totalSessions = weekDays.size() * SESSIONS_PER_DAY;

        Map<Subject, Integer> subjectFrequency = new HashMap<>();
        int sessionsPerSubject = totalSessions / gradeSubjects.size();
        int remainingSessions = totalSessions % gradeSubjects.size();

        for (Subject subject : gradeSubjects) {
            subjectFrequency.put(subject, sessionsPerSubject);
        }

        List<Subject> prioritySubjects = Arrays.asList(
                Subject.MATEMATICAS, Subject.ESPANOL, Subject.CIENCIAS
        );

        for (Subject subject : prioritySubjects) {
            if (remainingSessions > 0 && subjectFrequency.containsKey(subject)) {
                subjectFrequency.put(subject, subjectFrequency.get(subject) + 1);
                remainingSessions--;
            }
        }

        List<Subject> shuffledSubjects = new ArrayList<>();
        for (Map.Entry<Subject, Integer> entry : subjectFrequency.entrySet()) {
            for (int i = 0; i < entry.getValue(); i++) {
                shuffledSubjects.add(entry.getKey());
            }
        }

        Collections.shuffle(shuffledSubjects);
        return shuffledSubjects;
    }

    @Transactional
    public ScheduleEntity createSchedule(Grade grade) {
        validateTeacherAvailability();

        List<SessionEntity> sessionEntities = new ArrayList<>();
        TeacherAvailabilityTracker tracker = new TeacherAvailabilityTracker();

        List<Subject> distributedSubjects = distributeSubjects(grade);
        int subjectIndex = 0;

        int startHour = Integer.parseInt(START_TIME.split(":")[0]);

        try {
            for (String day : weekDays) {
                // Crear entrada para el recreo después de la tercera hora
                if (RECREO_AFTER_SESSION > 0 && RECREO_AFTER_SESSION < SESSIONS_PER_DAY) {
                    String recreoStartTime = String.format("%02d:00", startHour + RECREO_AFTER_SESSION);
                    String recreoEndTime = String.format("%02d:%02d", startHour + RECREO_AFTER_SESSION, RECREO_DURATION_MINUTES);

                    // Añadir una entrada para el recreo (no es una sesión normal)
                    SessionEntity recreoSession = new SessionEntity(
                            day, recreoStartTime, recreoEndTime, null, "RECREO", "Patio Central");
                    sessionEntities.add(recreoSession);
                }

                for (int sessionIndex = 0; sessionIndex < SESSIONS_PER_DAY; sessionIndex++) {
                    Map<String, String> times = calculateSessionTime(sessionIndex, startHour);
                    String startTime = times.get("startTime");
                    String endTime = times.get("endTime");

                    Subject subject = distributedSubjects.get(subjectIndex % distributedSubjects.size());
                    String teacherName = assignTeacher(day, startTime, subject, tracker);
                    String classroom = getClassroomForGradeAndSubject(grade, subject);

                    SessionEntity session = new SessionEntity(day, startTime, endTime, subject, teacherName, classroom);
                    sessionEntities.add(session);

                    subjectIndex++;
                }
            }

            ScheduleEntity scheduleEntity = new ScheduleEntity(grade, sessionEntities);
            ScheduleEntity savedSchedule = scheduleRepository.save(scheduleEntity);

            log.info("Horario creado exitosamente para el grado: {}. Sesiones: {}",
                    grade, sessionEntities.size());
            logTeacherWorkload(tracker);

            return savedSchedule;

        } catch (TeacherScheduleConflictException e) {
            log.error("Error al crear horario para grado {}: {}", grade, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado al crear horario para grado {}", grade, e);
            throw new RuntimeException("Error interno al generar horario", e);
        }
    }

    @Transactional
    public ScheduleEntity createDeterministicSchedule(Grade grade) {
        validateTeacherAvailability();

        List<SessionEntity> sessionEntities = new ArrayList<>();
        TeacherAvailabilityTracker tracker = new TeacherAvailabilityTracker();

        List<Subject> gradeSubjects = getSubjectsForGrade(grade);
        int startHour = Integer.parseInt(START_TIME.split(":")[0]);

        try {
            // Creamos un patrón de distribución de materias fijo para cada día
            Map<String, List<Subject>> dailySubjects = new HashMap<>();

            // Definimos patrones específicos para cada día
            dailySubjects.put("Lunes", new ArrayList<>(gradeSubjects));
            dailySubjects.put("Martes", rotateSubjects(new ArrayList<>(gradeSubjects), 1));
            dailySubjects.put("Miércoles", rotateSubjects(new ArrayList<>(gradeSubjects), 2));
            dailySubjects.put("Jueves", rotateSubjects(new ArrayList<>(gradeSubjects), 3));
            dailySubjects.put("Viernes", rotateSubjects(new ArrayList<>(gradeSubjects), 4));

            // Generamos las sesiones para cada día
            for (String day : weekDays) {
                // Crear entrada para el recreo después de la tercera hora
                if (RECREO_AFTER_SESSION > 0 && RECREO_AFTER_SESSION < SESSIONS_PER_DAY) {
                    String recreoStartTime = String.format("%02d:00", startHour + RECREO_AFTER_SESSION);
                    String recreoEndTime = String.format("%02d:%02d", startHour + RECREO_AFTER_SESSION, RECREO_DURATION_MINUTES);

                    // Añadir una entrada para el recreo (no es una sesión normal)
                    SessionEntity recreoSession = new SessionEntity(
                            day, recreoStartTime, recreoEndTime, null, "RECREO", "Patio Central");
                    sessionEntities.add(recreoSession);
                }

                List<Subject> daySubjects = dailySubjects.get(day);

                for (int sessionIndex = 0; sessionIndex < SESSIONS_PER_DAY; sessionIndex++) {
                    Map<String, String> times = calculateSessionTime(sessionIndex, startHour);
                    String startTime = times.get("startTime");
                    String endTime = times.get("endTime");

                    // Si hay menos materias que sesiones, repetimos las materias
                    Subject subject = daySubjects.get(sessionIndex % daySubjects.size());
                    String teacherName = assignTeacher(day, startTime, subject, tracker);
                    String classroom = getClassroomForGradeAndSubject(grade, subject);

                    SessionEntity session = new SessionEntity(day, startTime, endTime, subject, teacherName, classroom);
                    sessionEntities.add(session);
                }
            }

            ScheduleEntity scheduleEntity = new ScheduleEntity(grade, sessionEntities);
            ScheduleEntity savedSchedule = scheduleRepository.save(scheduleEntity);

            log.info("Horario determinístico creado para el grado: {}. Sesiones: {}",
                    grade, sessionEntities.size());
            logTeacherWorkload(tracker);

            return savedSchedule;

        } catch (Exception e) {
            log.error("Error al crear horario determinístico para grado {}", grade, e);
            throw new RuntimeException("Error al generar horario determinístico", e);
        }
    }

    // Método auxiliar para rotar las materias
    private List<Subject> rotateSubjects(List<Subject> subjects, int positions) {
        if (subjects.isEmpty() || positions <= 0) return subjects;

        for (int i = 0; i < positions; i++) {
            Subject first = subjects.remove(0);
            subjects.add(first);
        }

        return subjects;
    }

    @Transactional
    public Map<Grade, ScheduleEntity> createSchedulesForAllGrades() {
        validateTeacherAvailability();

        Map<Grade, ScheduleEntity> schedules = new HashMap<>();
        TeacherAvailabilityTracker globalTracker = new TeacherAvailabilityTracker();

        log.info("Iniciando generación de horarios para todos los grados");

        for (Grade grade : Grade.values()) {
            try {
                ScheduleEntity schedule = createScheduleWithTracker(grade, globalTracker);
                schedules.put(grade, schedule);
                log.info("Horario generado exitosamente para grado: {}", grade);
            } catch (TeacherScheduleConflictException e) {
                log.error("No se pudo generar horario para grado {}: {}", grade, e.getMessage());
                throw new TeacherScheduleConflictException(
                        String.format("Error al generar horario para %s: %s", grade, e.getMessage()));
            }
        }

        log.info("Generación de horarios completada. Grados procesados: {}", schedules.size());
        logTeacherWorkload(globalTracker);

        return schedules;
    }

    @Transactional
    public Map<Grade, ScheduleEntity> createDeterministicSchedulesForAllGrades() {
        validateTeacherAvailability();

        Map<Grade, ScheduleEntity> schedules = new HashMap<>();
        TeacherAvailabilityTracker globalTracker = new TeacherAvailabilityTracker();

        log.info("Iniciando generación de horarios determinísticos para todos los grados");

        for (Grade grade : Grade.values()) {
            try {
                ScheduleEntity schedule = createDeterministicScheduleWithTracker(grade, globalTracker);
                schedules.put(grade, schedule);
                log.info("Horario determinístico generado exitosamente para grado: {}", grade);
            } catch (TeacherScheduleConflictException e) {
                log.error("No se pudo generar horario determinístico para grado {}: {}", grade, e.getMessage());
                throw new TeacherScheduleConflictException(
                        String.format("Error al generar horario determinístico para %s: %s", grade, e.getMessage()));
            }
        }

        log.info("Generación de horarios determinísticos completada. Grados procesados: {}", schedules.size());
        logTeacherWorkload(globalTracker);

        return schedules;
    }

    private ScheduleEntity createScheduleWithTracker(Grade grade, TeacherAvailabilityTracker tracker) {
        List<SessionEntity> sessionEntities = new ArrayList<>();
        List<Subject> distributedSubjects = distributeSubjects(grade);
        int subjectIndex = 0;
        int startHour = Integer.parseInt(START_TIME.split(":")[0]);

        for (String day : weekDays) {
            // Crear entrada para el recreo después de la tercera hora
            if (RECREO_AFTER_SESSION > 0 && RECREO_AFTER_SESSION < SESSIONS_PER_DAY) {
                String recreoStartTime = String.format("%02d:00", startHour + RECREO_AFTER_SESSION);
                String recreoEndTime = String.format("%02d:%02d", startHour + RECREO_AFTER_SESSION, RECREO_DURATION_MINUTES);

                // Añadir una entrada para el recreo (no es una sesión normal)
                SessionEntity recreoSession = new SessionEntity(
                        day, recreoStartTime, recreoEndTime, null, "RECREO", "Patio Central");
                sessionEntities.add(recreoSession);
            }

            for (int sessionIndex = 0; sessionIndex < SESSIONS_PER_DAY; sessionIndex++) {
                Map<String, String> times = calculateSessionTime(sessionIndex, startHour);
                String startTime = times.get("startTime");
                String endTime = times.get("endTime");

                Subject subject = distributedSubjects.get(subjectIndex % distributedSubjects.size());
                String teacherName = assignTeacher(day, startTime, subject, tracker);
                String classroom = getClassroomForGradeAndSubject(grade, subject);

                SessionEntity session = new SessionEntity(day, startTime, endTime, subject, teacherName, classroom);
                sessionEntities.add(session);

                subjectIndex++;
            }
        }

        ScheduleEntity scheduleEntity = new ScheduleEntity(grade, sessionEntities);
        return scheduleRepository.save(scheduleEntity);
    }

    private ScheduleEntity createDeterministicScheduleWithTracker(Grade grade, TeacherAvailabilityTracker tracker) {
        List<SessionEntity> sessionEntities = new ArrayList<>();
        List<Subject> gradeSubjects = getSubjectsForGrade(grade);
        int startHour = Integer.parseInt(START_TIME.split(":")[0]);

        // Creamos un patrón de distribución de materias fijo para cada día
        Map<String, List<Subject>> dailySubjects = new HashMap<>();

        // Definimos patrones específicos para cada día
        dailySubjects.put("Lunes", new ArrayList<>(gradeSubjects));
        dailySubjects.put("Martes", rotateSubjects(new ArrayList<>(gradeSubjects), 1));
        dailySubjects.put("Miércoles", rotateSubjects(new ArrayList<>(gradeSubjects), 2));
        dailySubjects.put("Jueves", rotateSubjects(new ArrayList<>(gradeSubjects), 3));
        dailySubjects.put("Viernes", rotateSubjects(new ArrayList<>(gradeSubjects), 4));

        // Generamos las sesiones para cada día
        for (String day : weekDays) {
            // Crear entrada para el recreo después de la tercera hora
            if (RECREO_AFTER_SESSION > 0 && RECREO_AFTER_SESSION < SESSIONS_PER_DAY) {
                String recreoStartTime = String.format("%02d:00", startHour + RECREO_AFTER_SESSION);
                String recreoEndTime = String.format("%02d:%02d", startHour + RECREO_AFTER_SESSION, RECREO_DURATION_MINUTES);

                // Añadir una entrada para el recreo (no es una sesión normal)
                SessionEntity recreoSession = new SessionEntity(
                        day, recreoStartTime, recreoEndTime, null, "RECREO", "Patio Central");
                sessionEntities.add(recreoSession);
            }

            List<Subject> daySubjects = dailySubjects.get(day);

            for (int sessionIndex = 0; sessionIndex < SESSIONS_PER_DAY; sessionIndex++) {
                Map<String, String> times = calculateSessionTime(sessionIndex, startHour);
                String startTime = times.get("startTime");
                String endTime = times.get("endTime");

                // Si hay menos materias que sesiones, repetimos las materias
                Subject subject = daySubjects.get(sessionIndex % daySubjects.size());
                String teacherName = assignTeacher(day, startTime, subject, tracker);
                String classroom = getClassroomForGradeAndSubject(grade, subject);

                SessionEntity session = new SessionEntity(day, startTime, endTime, subject, teacherName, classroom);
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
                Subject.MATEMATICAS, Subject.ESPANOL, Subject.CIENCIAS
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
                .filter(s -> s.getSubject() != null) // Excluir recreos
                .collect(java.util.stream.Collectors.groupingBy(
                        SessionEntity::getSubject,
                        java.util.stream.Collectors.counting()
                ));

        Map<String, Long> teacherCount = sessions.stream()
                .filter(s -> !s.getTeacher().equals("RECREO")) // Excluir recreos
                .collect(java.util.stream.Collectors.groupingBy(
                        SessionEntity::getTeacher,
                        java.util.stream.Collectors.counting()
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