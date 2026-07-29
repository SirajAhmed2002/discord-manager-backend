package ch.zhaw.it.pm4.discordmanagerbe.bots.guildrolesconfig.service;

import ch.zhaw.it.pm4.discordmanagerbe.bots.guildrolesconfig.model.RoleSyncAnalysis;
import ch.zhaw.it.pm4.discordmanagerbe.bots.guildrolesconfig.model.RoleSyncResult;
import ch.zhaw.it.pm4.discordmanagerbe.bots.guildrolesconfig.model.RoleUpdateInfo;
import ch.zhaw.it.pm4.discordmanagerbe.dto.ExtendedRoleDTO;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.managers.RoleManager;
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction;
import net.dv8tion.jda.api.requests.restaction.RoleAction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.awt.Color;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleOperationServiceTest {

    @Mock
    private Guild guild;
    
    @Mock
    private Member botMember;
    
    @Mock
    private Role existingRole;
    
    @Mock
    private Role newRole;
    
    @Mock
    private Role roleToDelete;
    
    @Mock
    private RoleAction roleAction;
    
    @Mock
    private RoleManager roleManager;
    
    @Mock
    private AuditableRestAction<Void> deleteAction;

    private RoleOperationService roleOperationService;

    @BeforeEach
    void setUp() {
        roleOperationService = new RoleOperationService();
        setupMocks();
    }

    private void setupMocks() {
        lenient().when(guild.getName()).thenReturn("TestGuild");
        lenient().when(guild.getSelfMember()).thenReturn(botMember);
        lenient().when(guild.createRole()).thenReturn(roleAction);

        lenient().when(existingRole.getName()).thenReturn("ExistingRole");
        lenient().when(existingRole.getId()).thenReturn("123");
        lenient().when(existingRole.getManager()).thenReturn(roleManager);
        lenient().when(existingRole.isManaged()).thenReturn(false);
        lenient().when(existingRole.isPublicRole()).thenReturn(false);

        lenient().when(newRole.getName()).thenReturn("NewRole");
        lenient().when(newRole.getId()).thenReturn("456");

        lenient().when(roleToDelete.getName()).thenReturn("RoleToDelete");
        lenient().when(roleToDelete.getId()).thenReturn("789");
        lenient().when(roleToDelete.delete()).thenReturn(deleteAction);
        lenient().when(roleToDelete.isManaged()).thenReturn(false);
        lenient().when(roleToDelete.isPublicRole()).thenReturn(false);

        lenient().when(botMember.canInteract(any(Role.class))).thenReturn(true);

        lenient().when(roleAction.setName(any())).thenReturn(roleAction);
        lenient().when(roleAction.setColor(any(Color.class))).thenReturn(roleAction);
        lenient().when(roleAction.setPermissions(anyCollection())).thenReturn(roleAction);
        lenient().when(roleAction.complete()).thenReturn(newRole);

        lenient().when(roleManager.setName(any())).thenReturn(roleManager);
        lenient().when(roleManager.setColor(any(Color.class))).thenReturn(roleManager);
        lenient().when(roleManager.setPermissions(anyCollection())).thenReturn(roleManager);
    }

    @Test
    void applyRoleChanges_WithSuccessfulOperations_ShouldReturnSuccessResult() {
        
        RoleSyncAnalysis analysis = createAnalysisWithAllOperations();

        RoleSyncResult result = roleOperationService.applyRoleChanges(guild, analysis, true);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getCreatedRoles()).hasSize(1);
        assertThat(result.getCreatedRoles().getFirst().getName()).isEqualTo("NewRole");
        assertThat(result.getUpdatedRoles()).hasSize(1);
        assertThat(result.getDeletedRoles()).hasSize(1);
        assertThat(result.getErrors()).isEmpty();
    }

    @Test
    void applyRoleChanges_WithDeleteUnlistedRolesFalse_ShouldNotDeleteRoles() {
        
        RoleSyncAnalysis analysis = createAnalysisWithAllOperations();

        RoleSyncResult result = roleOperationService.applyRoleChanges(guild, analysis, false);
        
        assertThat(result.getDeletedRoles()).isEmpty();
        verify(roleToDelete, never()).delete();
    }

    @Test
    void applyRoleChanges_WithRoleCreationFailure_ShouldAddError() {
        
        RoleSyncAnalysis analysis = createAnalysisWithRoleToCreate();
        when(roleAction.complete()).thenThrow(new RuntimeException("Creation failed"));
        
        RoleSyncResult result = roleOperationService.applyRoleChanges(guild, analysis, false);
        
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getCreatedRoles()).isEmpty();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().getFirst()).contains("Failed to create role 'TestRole': Creation failed");
    }

    @Test
    void applyRoleChanges_WithRoleUpdateFailure_ShouldAddError() {
        
        RoleSyncAnalysis analysis = createAnalysisWithRoleToUpdate();
        when(roleManager.complete()).thenThrow(new RuntimeException("Update failed"));
        
        RoleSyncResult result = roleOperationService.applyRoleChanges(guild, analysis, false);
        
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getUpdatedRoles()).isEmpty();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().getFirst()).contains("Failed to update role 'ExistingRole': Update failed");
    }

    @Test
    void applyRoleChanges_WithRoleDeletionFailure_ShouldAddError() {
        
        RoleSyncAnalysis analysis = createAnalysisWithRoleToDelete();
        when(deleteAction.complete()).thenThrow(new RuntimeException("Deletion failed"));
        
        RoleSyncResult result = roleOperationService.applyRoleChanges(guild, analysis, true);
        
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getDeletedRoles()).isEmpty();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().getFirst()).contains("Failed to delete role 'RoleToDelete': Deletion failed");
    }

    @Test
    void applyRoleChanges_WithInsufficientPermissions_ShouldAddError() {
        
        RoleSyncAnalysis analysis = createAnalysisWithRoleToUpdate();
        when(botMember.canInteract(existingRole)).thenReturn(false);
        
        RoleSyncResult result = roleOperationService.applyRoleChanges(guild, analysis, false);
        
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getUpdatedRoles()).isEmpty();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().getFirst()).contains("Cannot update role 'ExistingRole': Insufficient permissions");
    }

    @Test
    void applyRoleChanges_WithManagedRole_ShouldAddError() {
        
        RoleSyncAnalysis analysis = createAnalysisWithRoleToUpdate();
        when(existingRole.isManaged()).thenReturn(true);
        
        RoleSyncResult result = roleOperationService.applyRoleChanges(guild, analysis, false);
        
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getUpdatedRoles()).isEmpty();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().getFirst()).contains("Cannot update role 'ExistingRole': Insufficient permissions");
    }

    @Test
    void applyRoleChanges_WithPublicRole_ShouldAddError() {
        
        RoleSyncAnalysis analysis = createAnalysisWithRoleToDelete();
        when(roleToDelete.isPublicRole()).thenReturn(true);
        
        RoleSyncResult result = roleOperationService.applyRoleChanges(guild, analysis, true);
        
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getDeletedRoles()).isEmpty();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().getFirst()).contains("Cannot delete role 'RoleToDelete': Insufficient permissions");
    }

    @Test
    void applyRoleChanges_WithRoleActionConfiguration_ShouldConfigureCorrectly() {
        
        ExtendedRoleDTO roleDto = createExtendedRoleDTO();
        RoleSyncAnalysis analysis = new RoleSyncAnalysis();
        analysis.addRoleToCreate(roleDto);
        
        roleOperationService.applyRoleChanges(guild, analysis, false);
        
        verify(roleAction).setName("TestRole");
        verify(roleAction).setColor(any(Color.class));
        verify(roleAction).setPermissions(anyCollection());
        verify(roleAction).complete();
    }

    @Test
    void applyRoleChanges_WithRoleManagerConfiguration_ShouldConfigureCorrectly() {
        
        RoleSyncAnalysis analysis = createAnalysisWithRoleToUpdate();
        
        roleOperationService.applyRoleChanges(guild, analysis, false);
        
        verify(roleManager).setName("UpdatedRole");
        verify(roleManager).setColor(any(Color.class));
        verify(roleManager).setPermissions(anyCollection());
        verify(roleManager).complete();
    }

    @Test
    void applyRoleChanges_WithEmptyAnalysis_ShouldReturnSuccessWithNoChanges() {
        
        RoleSyncAnalysis analysis = new RoleSyncAnalysis();
        
        RoleSyncResult result = roleOperationService.applyRoleChanges(guild, analysis, false);
        
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getCreatedRoles()).isEmpty();
        assertThat(result.getUpdatedRoles()).isEmpty();
        assertThat(result.getDeletedRoles()).isEmpty();
        assertThat(result.getErrors()).isEmpty();
    }

    @Test
    void applyRoleChanges_WithInvalidPermissions_ShouldSkipInvalidOnes() {
        
        ExtendedRoleDTO roleWithInvalidPermissions = new ExtendedRoleDTO();
        roleWithInvalidPermissions.setName("TestRole");
        roleWithInvalidPermissions.setPermissions(Arrays.asList("MESSAGE_SEND", "INVALID_PERMISSION", "MESSAGE_READ"));
        
        RoleSyncAnalysis analysis = new RoleSyncAnalysis();
        analysis.addRoleToCreate(roleWithInvalidPermissions);
        
        RoleSyncResult result = roleOperationService.applyRoleChanges(guild, analysis, false);
        
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getCreatedRoles()).hasSize(1);
        assertThat(result.getErrors()).isEmpty(); // Invalid permissions should be silently ignored
        
        verify(roleAction).setPermissions(anyCollection()); // Should still set permissions (valid ones only)
        verify(roleAction).complete();
    }

    private RoleSyncAnalysis createAnalysisWithAllOperations() {
        RoleSyncAnalysis analysis = new RoleSyncAnalysis();
        analysis.addRoleToCreate(createExtendedRoleDTO());
        analysis.addRoleToUpdate(createRoleUpdateInfo());
        analysis.addRolesToDelete(List.of(roleToDelete));
        return analysis;
    }

    private RoleSyncAnalysis createAnalysisWithRoleToCreate() {
        RoleSyncAnalysis analysis = new RoleSyncAnalysis();
        analysis.addRoleToCreate(createExtendedRoleDTO());
        return analysis;
    }

    private RoleSyncAnalysis createAnalysisWithRoleToUpdate() {
        RoleSyncAnalysis analysis = new RoleSyncAnalysis();
        analysis.addRoleToUpdate(createRoleUpdateInfo());
        return analysis;
    }

    private RoleSyncAnalysis createAnalysisWithRoleToDelete() {
        RoleSyncAnalysis analysis = new RoleSyncAnalysis();
        analysis.addRolesToDelete(List.of(roleToDelete));
        return analysis;
    }

    private ExtendedRoleDTO createExtendedRoleDTO() {
        ExtendedRoleDTO dto = new ExtendedRoleDTO();
        dto.setName("TestRole");
        dto.setColor("#FF0000");
        dto.setPermissions(Arrays.asList("MESSAGE_SEND", "MESSAGE_READ"));
        return dto;
    }

    private RoleUpdateInfo createRoleUpdateInfo() {
        ExtendedRoleDTO desiredRole = new ExtendedRoleDTO();
        desiredRole.setName("UpdatedRole");
        desiredRole.setColor("#00FF00");
        desiredRole.setPermissions(Arrays.asList("MESSAGE_SEND", "MESSAGE_READ")); // Fixed: use valid permissions

        RoleUpdateInfo updateInfo = new RoleUpdateInfo();
        updateInfo.setCurrentRole(existingRole);
        updateInfo.setDesiredRole(desiredRole);
        updateInfo.setNameChanged(true);
        updateInfo.setColorChanged(true);
        updateInfo.setPermissionsChanged(true);
        updateInfo.addChange("Name: 'ExistingRole' → 'UpdatedRole'");
        updateInfo.addChange("Color: #FF0000 → #00FF00");
        updateInfo.addChange("Permissions updated");
        
        return updateInfo;
    }
}