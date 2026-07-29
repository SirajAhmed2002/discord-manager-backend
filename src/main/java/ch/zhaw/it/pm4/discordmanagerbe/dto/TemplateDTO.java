package ch.zhaw.it.pm4.discordmanagerbe.dto;

/**
 * DTO for representing a template configuration.
 */
public class TemplateDTO {

    /**
     * The name of the template.
     */
    private String templateName;

    /**
     * The server configuration associated with the template.
     */
    private ServerConfigDTO template;

    /**
     * Default constructor for TemplatesDTO.
     */
    public TemplateDTO() {
        // Default constructor
    }

    /**
     * Gets the name of the template.
     * @return The name of the template
     */
    public String getTemplateName() {
        return templateName;
    }

    /**
     * Sets the name of the template.
     * @param templateName The name of the template to set
     */
    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    /**
     * Gets the server configuration associated with the template.
     * @return The server configuration of the template
     */
    public ServerConfigDTO getTemplate() {
        return template;
    }

    /**
     * Sets the server configuration associated with the template.
     * @param template The server configuration to set for the template
     */
    public void setTemplate(ServerConfigDTO template) {
        this.template = template;
    }
}
