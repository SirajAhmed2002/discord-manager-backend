package ch.zhaw.it.pm4.discordmanagerbe.service;

import ch.zhaw.it.pm4.discordmanagerbe.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for TemplateService.
 * Tests all public methods and verifies the predefined template structure.
 */
@SpringBootTest
class TemplateServiceTest {

    private TemplateService templateService;

    @BeforeEach
    void setUp() {
        templateService = new TemplateService();
    }

    @Test
    void shouldInitializeWithPredefinedTemplates() {
        // When
        List<TemplateDTO> templates = templateService.getAllTemplates();

        // Then
        assertNotNull(templates);
        assertEquals(1, templates.size());
        assertEquals("Beispiel", templates.getFirst().getTemplateName());
    }

    @Test
    void shouldReturnTemplateByExistingName() {
        // When
        ServerConfigDTO config = templateService.getTemplateByName("Beispiel");

        // Then
        assertNotNull(config);
        assertNotNull(config.getCategories());
        assertNotNull(config.getTextChannels());
        assertNotNull(config.getVoiceChannels());
    }

    @Test
    void shouldReturnNullForNonExistingTemplateName() {
        // When
        ServerConfigDTO config = templateService.getTemplateByName("NonExisting");

        // Then
        assertNull(config);
    }

    @Test
    void shouldReturnNullForNullTemplateName() {
        // When
        ServerConfigDTO config = templateService.getTemplateByName(null);

        // Then
        assertNull(config);
    }

    @Test
    void shouldReturnNullForEmptyTemplateName() {
        // When
        ServerConfigDTO config = templateService.getTemplateByName("");

        // Then
        assertNull(config);
    }

    @Test
    void shouldVerifySwen2ServerTemplateStructure() {
        // When
        ServerConfigDTO config = templateService.getTemplateByName("Beispiel");

        // Then
        assertNotNull(config);

        // Verify categories
        List<CategoryDTO> categories = config.getCategories();
        assertEquals(2, categories.size());

        // Check Swen2 category
        CategoryDTO swen2Category = categories.stream()
                .filter(cat -> "Swen2".equals(cat.getName()))
                .findFirst()
                .orElse(null);
        assertNotNull(swen2Category);
        assertEquals("1", swen2Category.getId());
        assertEquals(ChannelType.TEXT, swen2Category.getChannelType());
        assertEquals(0, swen2Category.getPosition());

        // Check PM4 category
        CategoryDTO pm4Category = categories.stream()
                .filter(cat -> "PM4".equals(cat.getName()))
                .findFirst()
                .orElse(null);
        assertNotNull(pm4Category);
        assertEquals("2", pm4Category.getId());
        assertEquals(ChannelType.TEXT, pm4Category.getChannelType());
        assertEquals(1, pm4Category.getPosition());
    }

    @Test
    void shouldVerifyTextChannelsStructure() {
        // When
        ServerConfigDTO config = templateService.getTemplateByName("Beispiel");

        // Then
        List<TextChannelDTO> textChannels = config.getTextChannels();
        assertEquals(2, textChannels.size());

        // Check Swen2 text channel
        TextChannelDTO swen2TextChannel = textChannels.stream()
                .filter(channel -> "Swen2-Textkanal".equals(channel.getName()))
                .findFirst()
                .orElse(null);
        assertNotNull(swen2TextChannel);
        assertEquals("3", swen2TextChannel.getId());
        assertEquals("1", swen2TextChannel.getParentCategoryId());
        assertEquals(ChannelType.TEXT, swen2TextChannel.getChannelType());
        assertEquals(0, swen2TextChannel.getPosition());

        // Check PM4 text channel
        TextChannelDTO pm4TextChannel = textChannels.stream()
                .filter(channel -> "PM4-Textkanal".equals(channel.getName()))
                .findFirst()
                .orElse(null);
        assertNotNull(pm4TextChannel);
        assertEquals("4", pm4TextChannel.getId());
        assertEquals("2", pm4TextChannel.getParentCategoryId());
        assertEquals(ChannelType.TEXT, pm4TextChannel.getChannelType());
        assertEquals(0, pm4TextChannel.getPosition());
    }

    @Test
    void shouldVerifyVoiceChannelsStructure() {
        // When
        ServerConfigDTO config = templateService.getTemplateByName("Beispiel");

        // Then
        List<VoiceChannelDTO> voiceChannels = config.getVoiceChannels();
        assertEquals(2, voiceChannels.size());

        // Check Swen2 voice channel
        VoiceChannelDTO swen2VoiceChannel = voiceChannels.stream()
                .filter(channel -> "Swen2-Sprachkanal".equals(channel.getName()))
                .findFirst()
                .orElse(null);
        assertNotNull(swen2VoiceChannel);
        assertEquals("5", swen2VoiceChannel.getId());
        assertEquals("1", swen2VoiceChannel.getParentCategoryId());
        assertEquals(ChannelType.VOICE, swen2VoiceChannel.getChannelType());
        assertEquals(0, swen2VoiceChannel.getPosition());

        // Check PM4 voice channel
        VoiceChannelDTO pm4VoiceChannel = voiceChannels.stream()
                .filter(channel -> "PM4-Sprachkanal".equals(channel.getName()))
                .findFirst()
                .orElse(null);
        assertNotNull(pm4VoiceChannel);
        assertEquals("6", pm4VoiceChannel.getId());
        assertEquals("2", pm4VoiceChannel.getParentCategoryId());
        assertEquals(ChannelType.VOICE, pm4VoiceChannel.getChannelType());
        assertEquals(0, pm4VoiceChannel.getPosition());
    }

    @Test
    void shouldReturnAllTemplatesAsNonNullList() {
        // When
        List<TemplateDTO> templates = templateService.getAllTemplates();

        // Then
        assertNotNull(templates);
        assertFalse(templates.isEmpty());
    }

    @Test
    void shouldVerifyTemplateNameCaseSensitivity() {
        // When
        ServerConfigDTO configLowerCase = templateService.getTemplateByName("beispiel");
        ServerConfigDTO configUpperCase = templateService.getTemplateByName("BEISPIEL");
        ServerConfigDTO configCorrectCase = templateService.getTemplateByName("Beispiel");

        // Then
        assertNull(configLowerCase, "Template name should be case sensitive");
        assertNull(configUpperCase, "Template name should be case sensitive");
        assertNotNull(configCorrectCase, "Correct case should return template");
    }

    @Test
    void shouldVerifyGetAllTemplatesReturnsImmutableReference() {
        // When
        List<TemplateDTO> templates1 = templateService.getAllTemplates();
        List<TemplateDTO> templates2 = templateService.getAllTemplates();

        // Then
        assertSame(templates1, templates2, 
            "getAllTemplates should return the same list reference (consider returning a copy for better encapsulation)");
    }

    @Test
    void shouldVerifyParentChildRelationshipsAreCorrect() {
        // When
        ServerConfigDTO config = templateService.getTemplateByName("Beispiel");

        // Then
        List<CategoryDTO> categories = config.getCategories();
        List<TextChannelDTO> textChannels = config.getTextChannels();
        List<VoiceChannelDTO> voiceChannels = config.getVoiceChannels();

        // Verify all text channels have valid parent categories
        for (TextChannelDTO textChannel : textChannels) {
            boolean hasValidParent = categories.stream()
                    .anyMatch(cat -> cat.getId().equals(textChannel.getParentCategoryId()));
            assertTrue(hasValidParent, 
                "Text channel " + textChannel.getName() + " should have a valid parent category");
        }

        // Verify all voice channels have valid parent categories
        for (VoiceChannelDTO voiceChannel : voiceChannels) {
            boolean hasValidParent = categories.stream()
                    .anyMatch(cat -> cat.getId().equals(voiceChannel.getParentCategoryId()));
            assertTrue(hasValidParent, 
                "Voice channel " + voiceChannel.getName() + " should have a valid parent category");
        }
    }
}