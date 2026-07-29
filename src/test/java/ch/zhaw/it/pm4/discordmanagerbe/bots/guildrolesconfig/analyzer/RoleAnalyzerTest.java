package ch.zhaw.it.pm4.discordmanagerbe.bots.guildrolesconfig.analyzer;

import ch.zhaw.it.pm4.discordmanagerbe.bots.guildrolesconfig.model.RoleSyncAnalysis;
import ch.zhaw.it.pm4.discordmanagerbe.dto.ExtendedRoleDTO;
import ch.zhaw.it.pm4.discordmanagerbe.dto.ServerRoleListDTO;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.awt.Color;
import java.util.Arrays;
import java.util.EnumSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class RoleAnalyzerTest {

    @Mock
    private Guild guild;

    @Mock
    private Role existingRole1;

    @Mock
    private Role existingRole2;

    @Mock
    private Role managedRole;

    @Mock
    private Role publicRole;

    private RoleAnalyzer roleAnalyzer;

    @BeforeEach
    void setUp() {
        roleAnalyzer = new RoleAnalyzer();
        setupMockRoles();
    }

    private void setupMockRoles() {
        // Setup existing role 1
        lenient().when(existingRole1.getId()).thenReturn("111");
        lenient().when(existingRole1.getName()).thenReturn("ExistingRole1");
        lenient().when(existingRole1.getColor()).thenReturn(Color.RED);
        lenient().when(existingRole1.getPermissions()).thenReturn(EnumSet.of(Permission.MESSAGE_SEND));
        lenient().when(existingRole1.isManaged()).thenReturn(false);
        lenient().when(existingRole1.isPublicRole()).thenReturn(false);

        // Setup existing role 2
        lenient().when(existingRole2.getId()).thenReturn("222");
        lenient().when(existingRole2.getName()).thenReturn("ExistingRole2");
        lenient().when(existingRole2.getColor()).thenReturn(Color.BLUE);
        lenient().when(existingRole2.getPermissions()).thenReturn(EnumSet.of(Permission.MESSAGE_MANAGE));
        lenient().when(existingRole2.isManaged()).thenReturn(false);
        lenient().when(existingRole2.isPublicRole()).thenReturn(false);

        // Setup managed role (should not be deleted)
        lenient().when(managedRole.getId()).thenReturn("333");
        lenient().when(managedRole.getName()).thenReturn("ManagedRole");
        lenient().when(managedRole.isManaged()).thenReturn(true);
        lenient().when(managedRole.isPublicRole()).thenReturn(false);

        // Setup public role (should not be deleted)
        lenient().when(publicRole.getId()).thenReturn("444");
        lenient().when(publicRole.getName()).thenReturn("@everyone");
        lenient().when(publicRole.isManaged()).thenReturn(false);
        lenient().when(publicRole.isPublicRole()).thenReturn(true);

        lenient().when(guild.getName()).thenReturn("TestGuild");
        lenient().when(guild.getRoles()).thenReturn(Arrays.asList(existingRole1, existingRole2, managedRole, publicRole));
    }

    @Test
    void analyzeRoleChanges_WithNewRoles_ShouldIdentifyRolesToCreate() {
        
        ServerRoleListDTO roleListDTO = createRoleListDTO(
                createRoleDTO("NewRole1", null, "#FF0000"),
                createRoleDTO("NewRole2", null, "#00FF00")
        );

        RoleSyncAnalysis analysis = roleAnalyzer.analyzeRoleChanges(guild, roleListDTO);

        assertThat(analysis.getRolesToCreate()).hasSize(2);
        assertThat(analysis.getRolesToCreate())
                .extracting(ExtendedRoleDTO::getName)
                .containsExactlyInAnyOrder("NewRole1", "NewRole2");
        assertThat(analysis.getRolesToUpdate()).isEmpty();
        assertThat(analysis.getRolesToDelete()).hasSize(2); // existingRole1 and existingRole2
    }

    @Test
    void analyzeRoleChanges_WithExistingRoleById_ShouldIdentifyRoleToUpdate() {
        
        ServerRoleListDTO roleListDTO = createRoleListDTO(
                createRoleDTO("UpdatedRole1", "111", "#00FF00") // Changed color
        );

        RoleSyncAnalysis analysis = roleAnalyzer.analyzeRoleChanges(guild, roleListDTO);

        assertThat(analysis.getRolesToCreate()).isEmpty();
        assertThat(analysis.getRolesToUpdate()).hasSize(1);
        assertThat(analysis.getRolesToUpdate().getFirst().getCurrentRole()).isEqualTo(existingRole1);
        assertThat(analysis.getRolesToUpdate().getFirst().hasChanges()).isTrue();
        assertThat(analysis.getRolesToDelete()).hasSize(1); // existingRole2
    }

    @Test
    void analyzeRoleChanges_WithExistingRoleByName_ShouldIdentifyRoleToUpdate() {
        
        ServerRoleListDTO roleListDTO = createRoleListDTO(
                createRoleDTO("ExistingRole1", null, "#00FF00") // Find by name, changed color
        );

        RoleSyncAnalysis analysis = roleAnalyzer.analyzeRoleChanges(guild, roleListDTO);

        assertThat(analysis.getRolesToCreate()).isEmpty();
        assertThat(analysis.getRolesToUpdate()).hasSize(1);
        assertThat(analysis.getRolesToUpdate().getFirst().getCurrentRole()).isEqualTo(existingRole1);
        assertThat(analysis.getRolesToDelete()).hasSize(1); // existingRole2
    }

    @Test
    void analyzeRoleChanges_WithUnchangedRoles_ShouldNotIdentifyUpdates() {
        
        ServerRoleListDTO roleListDTO = createRoleListDTO(
                createRoleDTO("ExistingRole1", "111", "#FF0000"), // Same color as existing
                createRoleDTO("ExistingRole2", "222", "#0000FF") // Same color as existing
        );

        RoleSyncAnalysis analysis = roleAnalyzer.analyzeRoleChanges(guild, roleListDTO);

        assertThat(analysis.getRolesToCreate()).isEmpty();
        assertThat(analysis.getRolesToUpdate()).isEmpty(); // No changes detected
        assertThat(analysis.getRolesToDelete()).isEmpty(); // All roles preserved
    }

    @Test
    void analyzeRoleChanges_WithMixedChanges_ShouldIdentifyAllChangeTypes() {
        
        ServerRoleListDTO roleListDTO = createRoleListDTO(
                createRoleDTO("ExistingRole1", "111", "#00FF00"), // Update existing
                createRoleDTO("NewRole", null, "#FFFF00") // Create new
                // ExistingRole2 not included - should be deleted
        );

        RoleSyncAnalysis analysis = roleAnalyzer.analyzeRoleChanges(guild, roleListDTO);

        assertThat(analysis.getRolesToCreate()).hasSize(1);
        assertThat(analysis.getRolesToCreate().getFirst().getName()).isEqualTo("NewRole");

        assertThat(analysis.getRolesToUpdate()).hasSize(1);
        assertThat(analysis.getRolesToUpdate().getFirst().getCurrentRole()).isEqualTo(existingRole1);

        assertThat(analysis.getRolesToDelete()).hasSize(1);
        assertThat(analysis.getRolesToDelete().getFirst()).isEqualTo(existingRole2);
    }

    @Test
    void analyzeRoleChanges_ShouldNotDeleteSystemRoles() {
        
        ServerRoleListDTO roleListDTO = createRoleListDTO(
                createRoleDTO("NewRole", null, "#FF0000")
        );

        RoleSyncAnalysis analysis = roleAnalyzer.analyzeRoleChanges(guild, roleListDTO);

        assertThat(analysis.getRolesToDelete())
                .containsExactlyInAnyOrder(existingRole1, existingRole2)
                .doesNotContain(managedRole, publicRole);
    }

    @Test
    void analyzeRoleChanges_WithNameChange_ShouldDetectNameChange() {
        
        ServerRoleListDTO roleListDTO = createRoleListDTO(
                createRoleDTO("NewName", "111", "#FF0000") // Same color, different name
        );

        RoleSyncAnalysis analysis = roleAnalyzer.analyzeRoleChanges(guild, roleListDTO);

        assertThat(analysis.getRolesToUpdate()).hasSize(1);
        var updateInfo = analysis.getRolesToUpdate().getFirst();
        assertThat(updateInfo.isNameChanged()).isTrue();
        assertThat(updateInfo.isColorChanged()).isFalse();
        assertThat(updateInfo.getChanges()).contains("Name: 'ExistingRole1' → 'NewName'");
    }

    @Test
    void analyzeRoleChanges_WithPermissionChange_ShouldDetectPermissionChange() {
        
        ExtendedRoleDTO roleDTO = createRoleDTO("ExistingRole1", "111", "#FF0000");
        roleDTO.setPermissions(Arrays.asList("MESSAGE_WRITE", "MESSAGE_READ")); // Different permissions

        ServerRoleListDTO roleListDTO = createRoleListDTO(roleDTO);

        RoleSyncAnalysis analysis = roleAnalyzer.analyzeRoleChanges(guild, roleListDTO);

        assertThat(analysis.getRolesToUpdate()).hasSize(1);
        var updateInfo = analysis.getRolesToUpdate().getFirst();
        assertThat(updateInfo.isPermissionsChanged()).isTrue();
        assertThat(updateInfo.getChanges()).contains("Permissions updated");
    }

    @Test
    void analyzeRoleChanges_WithEmptyRoleList_ShouldDeleteAllNonSystemRoles() {
        
        ServerRoleListDTO roleListDTO = createRoleListDTO(); // Empty list

        RoleSyncAnalysis analysis = roleAnalyzer.analyzeRoleChanges(guild, roleListDTO);
        
        assertThat(analysis.getRolesToCreate()).isEmpty();
        assertThat(analysis.getRolesToUpdate()).isEmpty();
        assertThat(analysis.getRolesToDelete())
                .containsExactlyInAnyOrder(existingRole1, existingRole2)
                .doesNotContain(managedRole, publicRole);
    }

    @Test
    void analyzeRoleChanges_ShouldLogResults() {
        
        ServerRoleListDTO roleListDTO = createRoleListDTO(
                createRoleDTO("NewRole", null, "#FF0000"),
                createRoleDTO("UpdatedRole", "111", "#00FF00")
        );

        RoleSyncAnalysis analysis = roleAnalyzer.analyzeRoleChanges(guild, roleListDTO);

        assertThat(analysis.hasChanges()).isTrue();
        assertThat(analysis.getSummary()).contains("1 to create", "1 to update", "1 to delete");
    }

    private ServerRoleListDTO createRoleListDTO(ExtendedRoleDTO... roles) {
        return new ServerRoleListDTO(Arrays.asList(roles));
    }

    private ExtendedRoleDTO createRoleDTO(String name, String id, String color) {
        ExtendedRoleDTO dto = new ExtendedRoleDTO();
        dto.setName(name);
        dto.setId(id);
        dto.setColor(color);
        return dto;
    }
}