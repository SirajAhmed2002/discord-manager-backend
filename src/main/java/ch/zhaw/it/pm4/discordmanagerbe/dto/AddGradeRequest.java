package ch.zhaw.it.pm4.discordmanagerbe.dto;

/**
 * Request DTO for adding a grade to a subject.
 * Contains all necessary information to create a new grade entry.
 */
public record AddGradeRequest(
        String serverId,
        String channelId,
        String userId,
        String subjectName,
        double note,
        double weight,
        String semester,
        String description
) {

    /**
     * Creates a new builder instance for constructing AddGradeRequest objects.
     * @return A new Builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder class for constructing AddGradeRequest objects step by step.
     */
    public static class Builder {
        /** The Discord server ID */
        private String serverId;
        /** The Discord channel ID */
        private String channelId;
        /** The Discord user ID */
        private String userId;
        /** The subject name */
        private String subjectName;
        /** The grade value */
        private double note;
        /** The grade weight */
        private double weight;
        /** The semester */
        private String semester;
        /** The grade description */
        private String description;

        /**
         * Sets the server ID.
         * @param serverId The Discord server ID
         * @return This builder instance
         */
        public Builder serverId(String serverId) {
            this.serverId = serverId;
            return this;
        }

        /**
         * Sets the channel ID.
         * @param channelId The Discord channel ID
         * @return This builder instance
         */
        public Builder channelId(String channelId) {
            this.channelId = channelId;
            return this;
        }

        /**
         * Sets the user ID.
         * @param userId The Discord user ID
         * @return This builder instance
         */
        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }

        /**
         * Sets the subject name.
         * @param subjectName The name of the subject
         * @return This builder instance
         */
        public Builder subjectName(String subjectName) {
            this.subjectName = subjectName;
            return this;
        }

        /**
         * Sets the grade value.
         * @param note The grade value
         * @return This builder instance
         */
        public Builder note(double note) {
            this.note = note;
            return this;
        }

        /**
         * Sets the grade weight.
         * @param weight The weight for grade calculation
         * @return This builder instance
         */
        public Builder weight(double weight) {
            this.weight = weight;
            return this;
        }

        /**
         * Sets the semester.
         * @param semester The semester when the grade was achieved
         * @return This builder instance
         */
        public Builder semester(String semester) {
            this.semester = semester;
            return this;
        }

        /**
         * Sets the description.
         * @param description Optional description of the grade
         * @return This builder instance
         */
        public Builder description(String description) {
            this.description = description;
            return this;
        }

        /**
         * Builds the AddGradeRequest object with the configured values.
         * @return A new AddGradeRequest instance
         */
        public AddGradeRequest build() {
            return new AddGradeRequest(
                    serverId, channelId, userId, subjectName,
                    note, weight, semester, description
            );
        }
    }
}