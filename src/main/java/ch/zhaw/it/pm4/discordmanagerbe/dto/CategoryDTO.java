package ch.zhaw.it.pm4.discordmanagerbe.dto;

/**
 * Data Transfer Object for Discord category channels.
 * Extends ChannelDTO with category-specific properties.
 */
public class CategoryDTO extends ChannelDTO {
    /** The ID of the parent category (if this category is nested) */
    private String parentCategoryId;

    /**
     * Default constructor.
     * Sets the channel type to CATEGORY.
     */
    public CategoryDTO() {
        this.setChannelType(ChannelType.CATEGORY);
    }

    /**
     * Gets the parent category ID.
     * @return The parent category ID, null if this is a top-level category
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