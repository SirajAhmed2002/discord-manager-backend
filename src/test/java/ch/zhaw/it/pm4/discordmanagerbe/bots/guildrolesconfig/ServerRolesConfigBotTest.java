package ch.zhaw.it.pm4.discordmanagerbe.bots.guildrolesconfig;

import ch.zhaw.it.pm4.discordmanagerbe.bots.guildrolesconfig.analyzer.RoleAnalyzer;
import ch.zhaw.it.pm4.discordmanagerbe.bots.guildrolesconfig.model.RoleSyncAnalysis;
import ch.zhaw.it.pm4.discordmanagerbe.bots.guildrolesconfig.model.RoleSyncResult;
import ch.zhaw.it.pm4.discordmanagerbe.bots.guildrolesconfig.model.ValidationResult;
import ch.zhaw.it.pm4.discordmanagerbe.bots.guildrolesconfig.service.RoleOperationService;
import ch.zhaw.it.pm4.discordmanagerbe.bots.guildrolesconfig.validator.RoleValidator;
import ch.zhaw.it.pm4.discordmanagerbe.dto.ExtendedRoleDTO;
import ch.zhaw.it.pm4.discordmanagerbe.dto.ServerRoleListDTO;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServerRolesConfigBotTest {

    @Mock
    private JDA jda;

    @Mock
    private RoleAnalyzer roleAnalyzer;

    @Mock
    private RoleOperationService roleOperationService;

    @Mock
    private RoleValidator roleValidator;

    @Mock
    private Guild guild;

    private ServerRolesConfigBot serverRolesConfigBot;

    @BeforeEach
    void setUp() {
        serverRolesConfigBot = new ServerRolesConfigBot(
                jda, roleAnalyzer, roleOperationService, roleValidator
        );
    }

    @Test
    void syncGuildRoles_WithValidInput_ShouldReturnSuccessResult() {
        
        String guildId = "123456789";
        ServerRoleListDTO roleListDTO = createValidRoleListDTO();
        ValidationResult validationResult = ValidationResult.success(guild);
        RoleSyncAnalysis analysis = new RoleSyncAnalysis();
        RoleSyncResult expectedResult = RoleSyncResult.success("Sync completed");

        when(roleValidator.validateSyncRequest(guildId, roleListDTO, jda))
                .thenReturn(validationResult);
        when(roleAnalyzer.analyzeRoleChanges(guild, roleListDTO))
                .thenReturn(analysis);
        when(roleOperationService.applyRoleChanges(guild, analysis, true))
                .thenReturn(expectedResult);

        RoleSyncResult result = serverRolesConfigBot.syncGuildRoles(guildId, roleListDTO, true);

        assertThat(result).isEqualTo(expectedResult);
        verify(roleValidator).validateSyncRequest(guildId, roleListDTO, jda);
        verify(roleAnalyzer).analyzeRoleChanges(guild, roleListDTO);
        verify(roleOperationService).applyRoleChanges(guild, analysis, true);
    }

    @Test
    void syncGuildRoles_WithInvalidInput_ShouldReturnFailureResult() {
        
        String guildId = "123456789";
        ServerRoleListDTO roleListDTO = createValidRoleListDTO();
        ValidationResult validationResult = ValidationResult.failure("Invalid input");

        when(roleValidator.validateSyncRequest(guildId, roleListDTO, jda))
                .thenReturn(validationResult);

        RoleSyncResult result = serverRolesConfigBot.syncGuildRoles(guildId, roleListDTO, true);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMessage()).isEqualTo("Invalid input");
        verify(roleAnalyzer, never()).analyzeRoleChanges(any(), any());
        verify(roleOperationService, never()).applyRoleChanges(any(), any(), anyBoolean());
    }

    @Test
    void syncGuildRoles_WithException_ShouldReturnFailureResult() {
        
        String guildId = "123456789";
        ServerRoleListDTO roleListDTO = createValidRoleListDTO();
        ValidationResult validationResult = ValidationResult.success(guild);

        when(roleValidator.validateSyncRequest(guildId, roleListDTO, jda))
                .thenReturn(validationResult);
        when(roleAnalyzer.analyzeRoleChanges(guild, roleListDTO))
                .thenThrow(new RuntimeException("Unexpected error"));

        RoleSyncResult result = serverRolesConfigBot.syncGuildRoles(guildId, roleListDTO, true);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMessage()).contains("Unexpected error");
    }

    @Test
    void syncGuildRoles_WithDeleteUnlistedRolesFalse_ShouldPassCorrectParameter() {
        
        String guildId = "123456789";
        ServerRoleListDTO roleListDTO = createValidRoleListDTO();
        ValidationResult validationResult = ValidationResult.success(guild);
        RoleSyncAnalysis analysis = new RoleSyncAnalysis();
        RoleSyncResult expectedResult = RoleSyncResult.success("Sync completed");

        when(roleValidator.validateSyncRequest(guildId, roleListDTO, jda))
                .thenReturn(validationResult);
        when(roleAnalyzer.analyzeRoleChanges(guild, roleListDTO))
                .thenReturn(analysis);
        when(roleOperationService.applyRoleChanges(guild, analysis, false))
                .thenReturn(expectedResult);

        RoleSyncResult result = serverRolesConfigBot.syncGuildRoles(guildId, roleListDTO, false);

        assertThat(result).isEqualTo(expectedResult);
        verify(roleOperationService).applyRoleChanges(guild, analysis, false);
    }

    @Test
    void syncGuildRoles_WithNullGuildId_ShouldHandleGracefully() {
        
        ServerRoleListDTO roleListDTO = createValidRoleListDTO();
        ValidationResult validationResult = ValidationResult.failure("Guild ID cannot be null or empty");

        when(roleValidator.validateSyncRequest(null, roleListDTO, jda))
                .thenReturn(validationResult);

        RoleSyncResult result = serverRolesConfigBot.syncGuildRoles(null, roleListDTO, true);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMessage()).contains("Guild ID cannot be null or empty");
    }

    @Test
    void syncGuildRoles_WithNullRoleListDTO_ShouldHandleGracefully() {
        
        String guildId = "123456789";
        ValidationResult validationResult = ValidationResult.failure("Role list DTO cannot be null");

        when(roleValidator.validateSyncRequest(guildId, null, jda))
                .thenReturn(validationResult);

        RoleSyncResult result = serverRolesConfigBot.syncGuildRoles(guildId, null, true);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMessage()).contains("Role list DTO cannot be null");
    }

    @Test
    void syncGuildRoles_ShouldHandleAnalyzerException() {
        
        String guildId = "123456789";
        ServerRoleListDTO roleListDTO = createValidRoleListDTO();
        ValidationResult validationResult = ValidationResult.success(guild);

        when(roleValidator.validateSyncRequest(guildId, roleListDTO, jda))
                .thenReturn(validationResult);
        when(roleAnalyzer.analyzeRoleChanges(guild, roleListDTO))
                .thenThrow(new RuntimeException("Analysis failed"));

        RoleSyncResult result = serverRolesConfigBot.syncGuildRoles(guildId, roleListDTO, true);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMessage()).contains("Unexpected error: Analysis failed");
        verify(roleOperationService, never()).applyRoleChanges(any(), any(), anyBoolean());
    }

    @Test
    void syncGuildRoles_ShouldHandleOperationServiceException() {
        
        String guildId = "123456789";
        ServerRoleListDTO roleListDTO = createValidRoleListDTO();
        ValidationResult validationResult = ValidationResult.success(guild);
        RoleSyncAnalysis analysis = new RoleSyncAnalysis();

        when(roleValidator.validateSyncRequest(guildId, roleListDTO, jda))
                .thenReturn(validationResult);
        when(roleAnalyzer.analyzeRoleChanges(guild, roleListDTO))
                .thenReturn(analysis);
        when(roleOperationService.applyRoleChanges(guild, analysis, true))
                .thenThrow(new RuntimeException("Operation failed"));

        RoleSyncResult result = serverRolesConfigBot.syncGuildRoles(guildId, roleListDTO, true);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMessage()).contains("Unexpected error: Operation failed");
    }

    private ServerRoleListDTO createValidRoleListDTO() {
        ExtendedRoleDTO role = new ExtendedRoleDTO();
        role.setName("TestRole");
        role.setColor("#FF0000");

        return new ServerRoleListDTO(List.of(role));
    }
}