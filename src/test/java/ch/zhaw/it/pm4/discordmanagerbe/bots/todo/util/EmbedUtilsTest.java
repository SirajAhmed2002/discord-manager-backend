package ch.zhaw.it.pm4.discordmanagerbe.bots.todo.util;

import net.dv8tion.jda.api.entities.MessageEmbed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.awt.Color;

import static ch.zhaw.it.pm4.discordmanagerbe.bots.todo.constant.ToDoConstants.*;
import static org.assertj.core.api.Assertions.*;

@DisplayName("EmbedUtils Tests")
class EmbedUtilsTest {

    @Nested
    @DisplayName("Basic Embed Creation Tests")
    class BasicEmbedCreationTests {

        @Test
        @DisplayName("Should create basic embed with all parameters")
        void shouldCreateBasicEmbedWithAllParameters() {
            // Given
            String title = "Test Title";
            String description = "Test Description";
            Color color = Color.BLUE;

            // When
            MessageEmbed result = EmbedUtils.createEmbed(title, description, color);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getTitle()).isEqualTo(title);
            assertThat(result.getDescription()).isEqualTo(description);
            assertThat(result.getColor()).isEqualTo(color);
        }

        @Test
        @DisplayName("Should create embed with null title")
        void shouldCreateEmbedWithNullTitle() {
            // When
            MessageEmbed result = EmbedUtils.createEmbed(null, "Description", Color.BLUE);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getTitle()).isNull();
            assertThat(result.getDescription()).isEqualTo("Description");
        }

        @Test
        @DisplayName("Should create embed with null description")
        void shouldCreateEmbedWithNullDescription() {
            // When
            MessageEmbed result = EmbedUtils.createEmbed("Title", null, Color.BLUE);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getTitle()).isEqualTo("Title");
            assertThat(result.getDescription()).isNull();
        }
    }

    @Nested
    @DisplayName("Semantic Embed Factory Tests")
    class SemanticEmbedFactoryTests {

        @Test
        @DisplayName("Should create success embed with green color")
        void shouldCreateSuccessEmbedWithGreenColor() {
            // Given
            String title = "Success Title";
            String description = "Success Description";

            // When
            MessageEmbed result = EmbedUtils.createSuccessEmbed(title, description);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getTitle()).isEqualTo(title);
            assertThat(result.getDescription()).isEqualTo(description);
            assertThat(result.getColor()).isEqualTo(COLOR_SUCCESS);
        }

        @Test
        @DisplayName("Should create error embed with red color")
        void shouldCreateErrorEmbedWithRedColor() {
            // Given
            String title = "Error Title";
            String description = "Error Description";

            // When
            MessageEmbed result = EmbedUtils.createErrorEmbed(title, description);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getTitle()).isEqualTo(title);
            assertThat(result.getDescription()).isEqualTo(description);
            assertThat(result.getColor()).isEqualTo(COLOR_ERROR);
        }

        @Test
        @DisplayName("Should create warning embed with orange color")
        void shouldCreateWarningEmbedWithOrangeColor() {
            // Given
            String title = "Warning Title";
            String description = "Warning Description";

            // When
            MessageEmbed result = EmbedUtils.createWarningEmbed(title, description);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getTitle()).isEqualTo(title);
            assertThat(result.getDescription()).isEqualTo(description);
            assertThat(result.getColor()).isEqualTo(COLOR_WARNING);
        }

        @Test
        @DisplayName("Should create info embed with blue color")
        void shouldCreateInfoEmbedWithBlueColor() {
            // Given
            String title = "Info Title";
            String description = "Info Description";

            // When
            MessageEmbed result = EmbedUtils.createInfoEmbed(title, description);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getTitle()).isEqualTo(title);
            assertThat(result.getDescription()).isEqualTo(description);
            assertThat(result.getColor()).isEqualTo(COLOR_PRIMARY);
        }
    }

    @Nested
    @DisplayName("Domain-Specific Embed Factory Tests")
    class DomainSpecificEmbedFactoryTests {

        @Test
        @DisplayName("Should create task display embed")
        void shouldCreateTaskDisplayEmbed() {
            // Given
            String taskTitle = "My Task";
            String taskContent = "Task details here";

            // When
            MessageEmbed result = EmbedUtils.createTaskDisplayEmbed(taskTitle, taskContent);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getTitle()).isEqualTo(taskTitle);
            assertThat(result.getDescription()).isEqualTo(taskContent);
            assertThat(result.getColor()).isEqualTo(COLOR_PRIMARY);
        }

        @Test
        @DisplayName("Should create task list embed")
        void shouldCreateTaskListEmbed() {
            // Given
            String taskList = "Task 1\nTask 2\nTask 3";

            // When
            MessageEmbed result = EmbedUtils.createTaskListEmbed(taskList);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getTitle()).isEqualTo(Messages.YOUR_TASKS);
            assertThat(result.getDescription()).isEqualTo(taskList);
            assertThat(result.getColor()).isEqualTo(COLOR_PRIMARY);
        }
    }

    @Nested
    @DisplayName("Pre-configured Message Embed Tests")
    class PreConfiguredMessageEmbedTests {

        @Test
        @DisplayName("Should create session expired embed")
        void shouldCreateSessionExpiredEmbed() {
            // When
            MessageEmbed result = EmbedUtils.createSessionExpiredEmbed();

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getTitle()).isEqualTo(Messages.SESSION_EXPIRED);
            assertThat(result.getDescription()).isEqualTo(Messages.RESTART_PROCESS);
            assertThat(result.getColor()).isEqualTo(COLOR_ERROR);
        }

        @Test
        @DisplayName("Should create no tasks found embed")
        void shouldCreateNoTasksFoundEmbed() {
            // When
            MessageEmbed result = EmbedUtils.createNoTasksFoundEmbed();

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getTitle()).isEqualTo(Messages.NO_TASKS_FOUND);
            assertThat(result.getDescription()).isEqualTo(Messages.NO_TASKS_CREATED);
            assertThat(result.getColor()).isEqualTo(COLOR_ERROR);
        }

        @Test
        @DisplayName("Should create task not found embed")
        void shouldCreateTaskNotFoundEmbed() {
            // When
            MessageEmbed result = EmbedUtils.createTaskNotFoundEmbed();

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getTitle()).isEqualTo(Messages.ERROR);
            assertThat(result.getDescription()).isEqualTo(Messages.TASK_NOT_FOUND);
            assertThat(result.getColor()).isEqualTo(COLOR_ERROR);
        }
    }

    @Nested
    @DisplayName("Validation Error Embed Tests")
    class ValidationErrorEmbedTests {

        @Test
        @DisplayName("Should create invalid title embed")
        void shouldCreateInvalidTitleEmbed() {
            // When
            MessageEmbed result = EmbedUtils.createInvalidTitleEmbed();

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getTitle()).isEqualTo(Messages.INVALID_TITLE);
            assertThat(result.getDescription()).isEqualTo(Messages.TITLE_NOT_EMPTY);
            assertThat(result.getColor()).isEqualTo(COLOR_ERROR);
        }

        @Test
        @DisplayName("Should create invalid date embed")
        void shouldCreateInvalidDateEmbed() {
            // When
            MessageEmbed result = EmbedUtils.createInvalidDateEmbed();

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getTitle()).isEqualTo(Messages.INVALID_DATE);
            assertThat(result.getDescription()).isEqualTo(Messages.DATE_NOT_VALID);
            assertThat(result.getColor()).isEqualTo(COLOR_ERROR);
        }

        @Test
        @DisplayName("Should create date in past embed")
        void shouldCreateDateInPastEmbed() {
            // When
            MessageEmbed result = EmbedUtils.createDateInPastEmbed();

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getTitle()).isEqualTo(Messages.DATE_IN_PAST);
            assertThat(result.getDescription()).isEqualTo(Messages.DATE_MUST_BE_FUTURE);
            assertThat(result.getColor()).isEqualTo(COLOR_ERROR);
        }

        @Test
        @DisplayName("Should create invalid time embed")
        void shouldCreateInvalidTimeEmbed() {
            // When
            MessageEmbed result = EmbedUtils.createInvalidTimeEmbed();

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getTitle()).isEqualTo(Messages.INVALID_TIME);
            assertThat(result.getDescription()).isEqualTo(Messages.TIME_NOT_VALID);
            assertThat(result.getColor()).isEqualTo(COLOR_ERROR);
        }

        @Test
        @DisplayName("Should create time in past embed")
        void shouldCreateTimeInPastEmbed() {
            // When
            MessageEmbed result = EmbedUtils.createTimeInPastEmbed();

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getTitle()).isEqualTo(Messages.TIME_IN_PAST);
            assertThat(result.getDescription()).isEqualTo(Messages.TIME_MUST_BE_FUTURE);
            assertThat(result.getColor()).isEqualTo(COLOR_ERROR);
        }
    }

    @Nested
    @DisplayName("Success Operation Embed Tests")
    class SuccessOperationEmbedTests {

        @Test
        @DisplayName("Should create task created embed")
        void shouldCreateTaskCreatedEmbed() {
            // Given
            String taskDescription = "Task has been created successfully";

            // When
            MessageEmbed result = EmbedUtils.createTaskCreatedEmbed(taskDescription);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getTitle()).isEqualTo(Messages.TASK_CREATED);
            assertThat(result.getDescription()).isEqualTo(taskDescription);
            assertThat(result.getColor()).isEqualTo(COLOR_SUCCESS);
        }

        @Test
        @DisplayName("Should create task removed embed")
        void shouldCreateTaskRemovedEmbed() {
            // Given
            String taskTitle = "My Important Task";

            // When
            MessageEmbed result = EmbedUtils.createTaskRemovedEmbed(taskTitle);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getTitle()).isEqualTo(Messages.TASK_REMOVED);
            assertThat(result.getDescription()).isEqualTo(
                String.format(Messages.TASK_REMOVED_SUCCESS, taskTitle)
            );
            assertThat(result.getColor()).isEqualTo(COLOR_SUCCESS);
        }
    }

    @Nested
    @DisplayName("Reminder-Specific Embed Tests")
    class ReminderSpecificEmbedTests {

        @Test
        @DisplayName("Should create reminder selection embed without error")
        void shouldCreateReminderSelectionEmbedWithoutError() {
            // When
            MessageEmbed result = EmbedUtils.createReminderSelectionEmbed(false);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getTitle()).isEqualTo(Messages.ADD_REMINDER);
            assertThat(result.getDescription()).isEqualTo(Messages.REMINDER_QUESTION);
            assertThat(result.getColor()).isEqualTo(COLOR_WARNING);
        }

        @Test
        @DisplayName("Should create reminder selection embed with error")
        void shouldCreateReminderSelectionEmbedWithError() {
            // When
            MessageEmbed result = EmbedUtils.createReminderSelectionEmbed(true);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getTitle()).isEqualTo(Messages.INVALID_REMINDER);
            assertThat(result.getDescription()).isEqualTo(Messages.REMINDER_IN_PAST);
            assertThat(result.getColor()).isEqualTo(COLOR_ERROR);
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle very long strings")
        void shouldHandleVeryLongStrings() {
            // Given
            String longTitle = "A".repeat(256); // Discord embed title limit is 256
            String longDescription = "B".repeat(2048); // Discord embed description limit is 2048

            // When
            MessageEmbed result = EmbedUtils.createEmbed(longTitle, longDescription, Color.BLUE);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getTitle()).isEqualTo(longTitle);
            assertThat(result.getDescription()).isEqualTo(longDescription);
        }

        @Test
        @DisplayName("Should handle special characters")
        void shouldHandleSpecialCharacters() {
            // Given
            String titleWithSpecialChars = "Test 🚀 Title with émojis and spëcial chars";
            String descWithSpecialChars = "Description with\nnewlines and\ttabs";

            // When
            MessageEmbed result = EmbedUtils.createEmbed(titleWithSpecialChars, descWithSpecialChars, Color.BLUE);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getTitle()).isEqualTo(titleWithSpecialChars);
            assertThat(result.getDescription()).isEqualTo(descWithSpecialChars);
        }
    }
}