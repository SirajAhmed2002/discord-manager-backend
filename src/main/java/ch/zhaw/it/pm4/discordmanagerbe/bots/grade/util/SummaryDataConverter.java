package ch.zhaw.it.pm4.discordmanagerbe.bots.grade.util;

import ch.zhaw.it.pm4.discordmanagerbe.dto.SemesterSummary;
import ch.zhaw.it.pm4.discordmanagerbe.dto.SubjectSummary;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Converts legacy Map-based summary data to typed DTOs
 */
@Component
public class SummaryDataConverter {

    /**
     * Converts Map-based semester data to typed SemesterSummary objects
     * @param summary the summary data containing semesters and subjects
     * @return a list of SemesterSummary objects
     */
    @SuppressWarnings("unchecked")
    public List<SemesterSummary> convertToSemesterSummaries(Map<String, Object> summary) {
        Map<String, Object> semestersMap = (Map<String, Object>) summary.get("semesters");
        List<SemesterSummary> semesters = new ArrayList<>();

        for (Map.Entry<String, Object> semesterEntry : semestersMap.entrySet()) {
            String semesterName = semesterEntry.getKey();
            Map<String, Object> semesterInfo = (Map<String, Object>) semesterEntry.getValue();

            double semesterAverage = (Double) semesterInfo.get("average");
            Map<String, Object> subjectsMap = (Map<String, Object>) semesterInfo.get("subjects");

            List<SubjectSummary> subjects = convertToSubjectSummaries(subjectsMap);
            semesters.add(new SemesterSummary(semesterName, semesterAverage, subjects));
        }

        return semesters;
    }

    /**
     * Converts Map-based subject data to typed SubjectSummary objects
     * @param subjectsMap the map containing subject data
     * @return a list of SubjectSummary objects
     */
    @SuppressWarnings("unchecked")
    private List<SubjectSummary> convertToSubjectSummaries(Map<String, Object> subjectsMap) {
        List<SubjectSummary> subjects = new ArrayList<>();

        for (Map.Entry<String, Object> subjectEntry : subjectsMap.entrySet()) {
            String subjectName = subjectEntry.getKey();
            Map<String, Object> subjectInfo = (Map<String, Object>) subjectEntry.getValue();

            double subjectAverage = (Double) subjectInfo.get("average");
            int credits = (Integer) subjectInfo.get("credits");
            int gradeCount = (Integer) subjectInfo.get("gradeCount");

            subjects.add(new SubjectSummary(subjectName, subjectAverage, credits, gradeCount));
        }

        return subjects;
    }
}