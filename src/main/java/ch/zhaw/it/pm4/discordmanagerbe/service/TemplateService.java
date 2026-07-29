package ch.zhaw.it.pm4.discordmanagerbe.service;

import ch.zhaw.it.pm4.discordmanagerbe.dto.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Service for managing server configuration templates.
 * Provides methods to create and retrieve predefined server templates.
 */
@Service
public class TemplateService {

    /**
     * List of predefined templates for server configurations.
     */
    private final List<TemplateDTO> templates;

    /**
     * Default constructor for TemplateService.
     */
    public TemplateService() {
        templates = new ArrayList<>();
        createSwen2ServerTemplate();
    }

    /**
     * Creates a predefined server template for the Swen2 course.
     */
    private void createSwen2ServerTemplate() {
        // Create a server configuration template
        ServerConfigDTO config = new ServerConfigDTO();
        List<CategoryDTO> categories = new ArrayList<>();
        List<TextChannelDTO> textChannels = new ArrayList<>();
        List<VoiceChannelDTO> voiceChannels = new ArrayList<>();

        CategoryDTO categorySwen = new CategoryDTO();
        categorySwen.setName("Swen2");
        categorySwen.setId("1");
        categorySwen.setChannelType(ChannelType.TEXT);
        categorySwen.setPosition(0);
        categories.add(categorySwen);

        CategoryDTO categoryPm = new CategoryDTO();
        categoryPm.setName("PM4");
        categoryPm.setId("2");
        categoryPm.setChannelType(ChannelType.TEXT);
        categoryPm.setPosition(1);
        categories.add(categoryPm);

        TextChannelDTO textChannelSwen = new TextChannelDTO();
        textChannelSwen.setName("Swen2-Textkanal");
        textChannelSwen.setId("3");
        textChannelSwen.setParentCategoryId(categorySwen.getId());
        textChannelSwen.setChannelType(ChannelType.TEXT);
        textChannelSwen.setPosition(0);
        textChannels.add(textChannelSwen);

        TextChannelDTO textChannelPm = new TextChannelDTO();
        textChannelPm.setName("PM4-Textkanal");
        textChannelPm.setId("4");
        textChannelPm.setParentCategoryId(categoryPm.getId());
        textChannelPm.setChannelType(ChannelType.TEXT);
        textChannelPm.setPosition(0);
        textChannels.add(textChannelPm);

        VoiceChannelDTO voiceChannelSwen = new VoiceChannelDTO();
        voiceChannelSwen.setName("Swen2-Sprachkanal");
        voiceChannelSwen.setId("5");
        voiceChannelSwen.setParentCategoryId(categorySwen.getId());
        voiceChannelSwen.setChannelType(ChannelType.VOICE);
        voiceChannelSwen.setPosition(0);
        voiceChannels.add(voiceChannelSwen);

        VoiceChannelDTO voiceChannelPm = new VoiceChannelDTO();
        voiceChannelPm.setName("PM4-Sprachkanal");
        voiceChannelPm.setId("6");
        voiceChannelPm.setParentCategoryId(categoryPm.getId());
        voiceChannelPm.setChannelType(ChannelType.VOICE);
        voiceChannelPm.setPosition(0);
        voiceChannels.add(voiceChannelPm);

        config.setCategories(categories);
        config.setTextChannels(textChannels);
        config.setVoiceChannels(voiceChannels);

        TemplateDTO template = new TemplateDTO();
        template.setTemplateName("Beispiel");
        template.setTemplate(config);

        templates.add(template);
    }

    /**
     * Retrieves a server configuration template by its name.
     * @param name the name of the template to retrieve
     * @return the ServerConfigDTO for the specified template name, or null if not found
     */
    public ServerConfigDTO getTemplateByName(String name) {
        return templates.stream()
                .filter(template -> template.getTemplateName().equals(name))
                .findFirst()
                .map(TemplateDTO::getTemplate)  // Use map() instead of ifPresent()
                .orElse(null);  // Return null if not found, or throw exception
    }

    /**
     * Retrieves all available templates.
     * @return a list of TemplateDTO objects representing all templates
     */
    public List<TemplateDTO> getAllTemplates() {
        return templates;  // Return a copy to avoid external modification
    }
}
