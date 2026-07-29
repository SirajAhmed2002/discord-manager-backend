package ch.zhaw.it.pm4.discordmanagerbe.dto;

/**
 * Abstract base class for channels that can be categorized.
 * Extends ChannelDTO with category assignment functionality.
 */
public abstract class ChannelCategorizableDTO extends ChannelDTO {
    /** The ID of the parent category this channel belongs to */
    private String parentCategoryId;

    /**
     * Gets the parent category ID.
     * @return The parent category ID, null if not assigned to a category
     */
    public String getParentCategoryId() {
        return parentCategoryId;
    }

    /**
     * Sets the parent category ID.
     * @param parentCategoryId The parent category ID to set
     */
    public void setParentCategoryId(String parentCategoryId) {
        this.parentCategoryId = parentCategoryId;
    }
}