package ch.zhaw.it.pm4.discordmanagerbe.data.repositories;

import ch.zhaw.it.pm4.discordmanagerbe.data.entities.Grade;
import ch.zhaw.it.pm4.discordmanagerbe.data.entities.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for Grade entity operations.
 * Provides CRUD operations and custom queries for grade management.
 */
@Repository
public interface GradeRepository extends JpaRepository<Grade, Long> {

    /**
     * Finds all grades for a specific subject.
     *
     * @param subject the subject to find grades for
     * @return list of grades for the subject
     */
    List<Grade> findBySubject(Subject subject);

    /**
     * Finds all grades for a specific subject ordered by creation date descending.
     *
     * @param subject the subject to find grades for
     * @return list of grades ordered by creation date (newest first)
     */
    List<Grade> findBySubjectOrderByCreatedAtDesc(Subject subject);

    /**
     * Deletes all grades for a specific subject.
     * This method is needed for the remove subject functionality.
     *
     * @param subject the subject to delete grades for
     */
    @Modifying
    @Query("DELETE FROM Grade g WHERE g.subject = :subject")
    void deleteBySubject(@Param("subject") Subject subject);

    /**
     * Alternative method using Spring Data JPA naming convention.
     * This does the same as the @Query method above.
     *
     * @param subject the subject to delete grades for
     */
    void deleteAllBySubject(Subject subject);
}