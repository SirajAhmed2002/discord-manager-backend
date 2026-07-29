package ch.zhaw.it.pm4.discordmanagerbe.bots.grade.service;

import ch.zhaw.it.pm4.discordmanagerbe.data.entities.Grade;
import ch.zhaw.it.pm4.discordmanagerbe.data.entities.Subject;
import ch.zhaw.it.pm4.discordmanagerbe.data.repositories.GradeRepository;
import ch.zhaw.it.pm4.discordmanagerbe.data.repositories.SubjectRepository;
import ch.zhaw.it.pm4.discordmanagerbe.dto.AddGradeRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class NotenrechnerService {

    /**
     * Service for managing subjects and grades in the grade calculator bot.
     */
    private static final Logger log = LoggerFactory.getLogger(NotenrechnerService.class);

    /**
     * Repository for accessing subjects in the database.
     */
    private final SubjectRepository subjectRepository;

    /**
     * Repository for accessing grades in the database.
     */
    private final GradeRepository gradeRepository;

    /**
     * Service for validating and normalizing grade-related parameters.
     */
    private final GradeValidationService validationService;

    /**
     * Constructor for NotenrechnerService.
     */
    @Autowired
    public NotenrechnerService(
        SubjectRepository subjectRepository,
        GradeRepository gradeRepository,
        GradeValidationService validationService) {

        this.subjectRepository = subjectRepository;
        this.gradeRepository = gradeRepository;
        this.validationService = validationService;
    }

    /**
     * Creates a new subject for a server/channel/user combination
     * @param serverId ID of the Discord server
     * @param channelId ID of the Discord channel
     * @param userId ID of the Discord user
     * @param subjectName Name of the subject
     * @param credits Number of credits
     * @param semester Semester in which the subject is taken
     * @return The created subject
     */
    @Transactional
    public Subject createSubject(String serverId, String channelId, String userId, 
                               String subjectName, int credits, String semester) {
        log.info("Creating subject {} with {} credits for semester {} and server/channel/user: {}/{}/{}", 
                 subjectName, credits, semester, serverId, channelId, userId);

        validationService.validateCreateSubjectParameters(subjectName, credits);

        Optional<Subject> existingSubject = subjectRepository.findByServerIdAndChannelIdAndUserIdAndNameAndSemester(
            serverId, channelId, userId, subjectName, semester);
            
        if (existingSubject.isPresent()) {
            throw new IllegalArgumentException("Fach " + subjectName + " existiert bereits im Semester " + semester + "!");
        }

        Subject subject = new Subject(subjectName, credits, semester, serverId, channelId, userId);
        return subjectRepository.save(subject);
    }

    /**
     * Adds a grade to a subject - new overload with AddGradeRequest
     * @param request The request containing the required parameters
     * @return The added grade
     */
    @Transactional
    public Grade addGrade(AddGradeRequest request) {
        return addGrade(
            request.serverId(), request.channelId(), request.userId(),
            request.subjectName(), request.semester(),
            request.note(), request.weight(), request.description()
        );
    }

    /**
     * Adds a grade to a subject - existing method
     * @param serverId ID of the Discord server
     * @param channelId ID of the Discord channel
     * @param userId ID of the Discord user
     * @param subjectName Name of the subject
     * @param semester Semester in which the grade is entered
     * @param value Value of the grade
     * @param weight Weight of the grade
     * @param description Description of the grade (optional)
     * @return The added grade
     */
    @Transactional
    public Grade addGrade(String serverId, String channelId, String userId, 
                         String subjectName, String semester,
                         double value, double weight, String description) {
        log.info("Adding grade {} with weight {} to subject {} (semester {}) for server/channel/user: {}/{}/{}", 
                 value, weight, subjectName, semester, serverId, channelId, userId);
        
        // Validierung und Normalisierung
        double normalizedValue = validationService.normalizeGrade(value);
        double normalizedWeight = validationService.normalizeWeight(weight);
        
        validationService.validateAddGradeParameters(subjectName, normalizedValue, normalizedWeight);
        
        // Fach suchen
        Subject subject = findSubject(serverId, channelId, userId, subjectName, semester);
        
        // Note hinzufügen
        Grade grade = new Grade(normalizedValue, normalizedWeight, description != null ? description : "", subject);
        subject.addGrade(grade);
        
        return gradeRepository.save(grade);
    }

    /**
     * Removes a subject and all its grades
     * @param serverId ID of the Discord server
     * @param channelId ID of the Discord channel
     * @param userId ID of the Discord user
     * @param subjectName Name of the subject to remove
     * @param semester Semester of the subject
     * @return true if the subject was successfully removed, false if it was not found
     */
    @Transactional
    public boolean removeSubject(String serverId, String channelId, String userId, 
                               String subjectName, String semester) {
        log.info("Removing subject {} (semester {}) for server/channel/user: {}/{}/{}", 
                 subjectName, semester, serverId, channelId, userId);
        
        Optional<Subject> subjectOpt = subjectRepository.findByServerIdAndChannelIdAndUserIdAndNameAndSemester(
            serverId, channelId, userId, subjectName, semester);
        
        if (subjectOpt.isPresent()) {
            Subject subject = subjectOpt.get();
            
            // First remove all grades
            gradeRepository.deleteBySubject(subject);
            
            // Then remove the subject
            subjectRepository.delete(subject);
            
            log.info("Successfully removed subject {} with {} grades", 
                     subjectName, subject.getGrades().size());
            return true;
        }
        
        log.warn("Subject {} not found for removal", subjectName);
        return false;
    }

    /**
     * Removes all grades from a specific subject
     * @param serverId ID of the Discord server
     * @param channelId ID of the Discord channel
     * @param userId ID of the Discord user
     * @param subjectName Name of the subject
     * @param semester Semester of the subject
     * @return the number of grades removed, or 0 if no grades were found
     */
    @Transactional
    public int removeAllGradesFromSubject(String serverId, String channelId, String userId, 
                                         String subjectName, String semester) {
        log.info("Removing all grades from subject {} (semester {}) for server/channel/user: {}/{}/{}", 
                 subjectName, semester, serverId, channelId, userId);
        
        Subject subject = findSubject(serverId, channelId, userId, subjectName, semester);
        int gradeCount = subject.getGrades().size();
        
        if (gradeCount == 0) {
            log.info("No grades found in subject {} to remove", subjectName);
            return 0;
        }
        
        // Remove all grades from the subject
        gradeRepository.deleteBySubject(subject);
        
        // Clear the grades list in the subject entity
        subject.getGrades().clear();
        
        log.info("Successfully removed {} grades from subject {}", gradeCount, subjectName);
        return gradeCount;
    }

    /**
     * Removes all subjects in a semester
     * @param serverId ID of the Discord server
     * @param channelId ID of the Discord channel
     * @param userId ID of the Discord user
     * @param semester Semester to remove
     * @return the number of subjects removed, or 0 if no subjects were found
     */
    @Transactional
    public int removeSemester(String serverId, String channelId, String userId, String semester) {
        log.info("Removing semester {} for server/channel/user: {}/{}/{}", 
                 semester, serverId, channelId, userId);
        
        List<Subject> subjects = subjectRepository.findByServerIdAndChannelIdAndUserIdAndSemester(
            serverId, channelId, userId, semester);
        
        if (subjects.isEmpty()) {
            log.warn("No subjects found in semester {} for removal", semester);
            return 0;
        }
        
        // Remove all grades first
        for (Subject subject : subjects) {
            gradeRepository.deleteBySubject(subject);
        }
        
        // Then remove all subjects
        subjectRepository.deleteAll(subjects);
        
        int removedCount = subjects.size();
        log.info("Successfully removed semester {} with {} subjects", semester, removedCount);
        return removedCount;
    }

    /**
     * Returns a list of all available semesters
     * @param serverId ID of the Discord server
     * @param channelId ID of the Discord channel
     * @param userId ID of the Discord user
     * @return List of semesters
     */
    @Transactional(readOnly = true)
    public List<String> getSemesters(String serverId, String channelId, String userId) {
        return subjectRepository.findSemestersByServerIdAndChannelIdAndUserId(serverId, channelId, userId);
    }

    /**
     * Returns an overview of all subjects for a server/channel/user combination
     * @param serverId ID of the Discord server
     * @param channelId ID of the Discord channel
     * @param userId ID of the Discord user
     * @return List of subjects
     */
    @Transactional(readOnly = true)
    public List<Subject> getSubjects(String serverId, String channelId, String userId) {
        log.info("Fetching subjects for server/channel/user: {}/{}/{}", serverId, channelId, userId);
        return subjectRepository.findByServerIdAndChannelIdAndUserId(serverId, channelId, userId);
    }

    /**
     * Returns an overview of all subjects for a specific semester
     * @param serverId ID of the Discord server
     * @param channelId ID of the Discord channel
     * @param userId ID of the Discord user
     * @param semester Semester for which the subjects are queried
     * @return List of subjects for the specified semester
     */
    @Transactional(readOnly = true)
    public List<Subject> getSubjectsForSemester(String serverId, String channelId, String userId, String semester) {
        log.info("Fetching subjects for semester {} and server/channel/user: {}/{}/{}", 
                 semester, serverId, channelId, userId);
        return subjectRepository.findByServerIdAndChannelIdAndUserIdAndSemester(serverId, channelId, userId, semester);
    }

    /**
     * Retrieves the grades for a specific subject
     * @param serverId ID of the Discord server
     * @param channelId ID of the Discord channel
     * @param userId ID of the Discord user
     * @param subjectName Name of the subject
     * @param semester Semester in which the subject is taken
     * @return List of grades for the subject
     */
    @Transactional(readOnly = true)
    public List<Grade> getGradesForSubject(String serverId, String channelId, String userId, 
                                          String subjectName, String semester) {
        log.info("Fetching grades for subject {} (semester {}) of server/channel/user: {}/{}/{}", 
                 subjectName, semester, serverId, channelId, userId);
        
        Subject subject = findSubject(serverId, channelId, userId, subjectName, semester);
        return gradeRepository.findBySubjectOrderByCreatedAtDesc(subject);
    }

    /**
     * Calculates the weighted average for a subject
     * @param serverId ID of the Discord server
     * @param channelId ID of the Discord channel
     * @param userId ID of the Discord user
     * @param subjectName Name of the subject
     * @param semester Semester in which the subject is taken
     * @return The weighted average for the subject
     */
    @Transactional(readOnly = true)
    public double calculateSubjectAverage(String serverId, String channelId, String userId, 
                                         String subjectName, String semester) {
        Subject subject = findSubject(serverId, channelId, userId, subjectName, semester);
        return subject.calculateAverage();
    }

    /**
     * Calculates the weighted overall average (weighted by credits) for a semester
     * @param serverId ID of the Discord server
     * @param channelId ID of the Discord channel
     * @param userId ID of the Discord user
     * @param semester Semester for which the average is calculated
     * @return The weighted average for the semester
     */
    @Transactional(readOnly = true)
    public double calculateSemesterAverage(String serverId, String channelId, String userId, String semester) {
        List<Subject> subjects = subjectRepository.findByServerIdAndChannelIdAndUserIdAndSemester(
            serverId, channelId, userId, semester);
        
        return calculateAverageForSubjects(subjects);
    }

    /**
     * Calculates the weighted overall average (weighted by credits) for all semesters
     * @param serverId ID of the Discord server
     * @param channelId ID of the Discord channel
     * @param userId ID of the Discord user
     * @return The weighted overall average for all subjects of the user
     */
    @Transactional(readOnly = true)
    public double calculateOverallAverage(String serverId, String channelId, String userId) {
        List<Subject> subjects = subjectRepository.findByServerIdAndChannelIdAndUserId(serverId, channelId, userId);
        
        return calculateAverageForSubjects(subjects);
    }

    /**
     * Returns an overview of all subjects with their averages, grouped by semester
     * @param serverId ID of the Discord server
     * @param channelId ID of the Discord channel
     * @param userId ID of the Discord user
     * @return A map with semesters as keys and subjects with their averages as values
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getSummary(String serverId, String channelId, String userId) {
        List<Subject> allSubjects = subjectRepository.findByServerIdAndChannelIdAndUserId(serverId, channelId, userId);
        List<String> semesters = subjectRepository.findSemestersByServerIdAndChannelIdAndUserId(serverId, channelId, userId);
        
        // Fächer ohne Semester auch berücksichtigen
        if (allSubjects.stream().anyMatch(s -> s.getSemester() == null || s.getSemester().isEmpty())) {
            semesters.add("Ohne Semester");
        }
        
        Map<String, Object> summary = new HashMap<>();
        Map<String, Object> semesterMap = new HashMap<>();
        
        // Für jedes Semester einen Eintrag erstellen
        for (String semester : semesters) {
            List<Subject> semesterSubjects = getSemesterSubjects(allSubjects, semester);
            double semesterAverage = calculateAverageForSubjects(semesterSubjects);
            
            Map<String, Object> semesterInfo = createSemesterInfo(semesterAverage, semesterSubjects);
            semesterMap.put(semester, semesterInfo);
        }
        
        summary.put("semesters", semesterMap);
        summary.put("overallAverage", calculateAverageForSubjects(allSubjects));
        
        return summary;
    }

    /**
     * Helper method to calculate the weighted average for a list of subjects
     * @param subjects List of subjects
     * @return The weighted average for the subjects, 0.0 if no subjects with grades are present
     */
    private double calculateAverageForSubjects(List<Subject> subjects) {
        double totalWeightedAverage = 0.0;
        int totalCredits = 0;
        
        for (Subject subject : subjects) {
            double average = subject.calculateAverage();
            if (average > 0) { // Nur Fächer mit Noten berücksichtigen
                totalWeightedAverage += average * subject.getCredits();
                totalCredits += subject.getCredits();
            }
        }
        
        if (totalCredits == 0) {
            return 0.0;
        }
        
        return totalWeightedAverage / totalCredits;
    }

    /**
     * Helper method to find a subject
     * @param serverId ID of the Discord server
     * @param channelId ID of the Discord channel
     * @param userId ID of the Discord user
     * @param subjectName Name of the subject
     * @param semester Semester in which the subject is taken
     * @return The found subject
     */
    private Subject findSubject(String serverId, String channelId, String userId, String subjectName, String semester) {
        return subjectRepository.findByServerIdAndChannelIdAndUserIdAndNameAndSemester(
            serverId, channelId, userId, subjectName, semester)
            .orElseThrow(() -> new IllegalArgumentException(
                String.format("Fach '%s' im Semester '%s' nicht gefunden!", subjectName, semester)));
    }

    /**
     * Helper method to filter subjects by semester
     * @param allSubjects List of all subjects
     * @param semester Semester for which the subjects should be filtered
     * @return List of subjects for the specified semester
     */
    private List<Subject> getSemesterSubjects(List<Subject> allSubjects, String semester) {
        if ("Ohne Semester".equals(semester)) {
            return allSubjects.stream()
                .filter(s -> s.getSemester() == null || s.getSemester().isEmpty())
                .toList();
        } else {
            return allSubjects.stream()
                .filter(s -> semester.equals(s.getSemester()))
                .toList();
        }
    }

    /**
     * Helper method to create the semester info map
     * @param semesterAverage Average for the semester
     * @param semesterSubjects List of subjects for the semester
     * @return Map with information about the semester
     */
    private Map<String, Object> createSemesterInfo(double semesterAverage, List<Subject> semesterSubjects) {
        Map<String, Object> semesterInfo = new HashMap<>();
        semesterInfo.put("average", semesterAverage);
        
        Map<String, Object> subjectMap = new HashMap<>();
        
        for (Subject subject : semesterSubjects) {
            double average = subject.calculateAverage();
            Map<String, Object> subjectInfo = new HashMap<>();
            subjectInfo.put("average", average);
            subjectInfo.put("credits", subject.getCredits());
            subjectInfo.put("gradeCount", subject.getGrades().size());
            
            subjectMap.put(subject.getName(), subjectInfo);
        }
        
        semesterInfo.put("subjects", subjectMap);
        return semesterInfo;
    }
}