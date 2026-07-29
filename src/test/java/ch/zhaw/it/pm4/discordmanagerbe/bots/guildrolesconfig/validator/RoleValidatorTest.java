package ch.zhaw.it.pm4.discordmanagerbe.bots.guildrolesconfig.validator;

import ch.zhaw.it.pm4.discordmanagerbe.bots.guildrolesconfig.model.ValidationResult;
import ch.zhaw.it.pm4.discordmanagerbe.dto.ExtendedRoleDTO;
import ch.zhaw.it.pm4.discordmanagerbe.dto.ServerRoleListDTO;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoleValidatorTest {

    @Mock
    private JDA jda;

    @Mock
    private Guild guild;

    @Mock
    private Member botMember;

    private RoleValidator roleValidator;

    @BeforeEach
    void setUp() {
        roleValidator = new RoleValidator();
    }

    @Test
    void validateSyncRequest_WithValidInput_ShouldReturnSuccess() {
        
        String guildId = "123456789";
        ServerRoleListDTO roleListDTO = createValidRoleListDTO();

        when(jda.getGuildById(guildId)).thenReturn(guild);
        when(guild.getSelfMember()).thenReturn(botMember);
        when(botMember.hasPermission(Permission.MANAGE_ROLES)).thenReturn(true);

        ValidationResult result = roleValidator.validateSyncRequest(guildId, roleListDTO, jda);

        assertThat(result.isValid()).isTrue();
        assertThat(result.getGuild()).isEqualTo(guild);
        assertThat(result.getErrorMessage()).isNull();
    }

    @Test
    void validateSyncRequest_WithNullRoleListDTO_ShouldReturnFailure() {
        
        String guildId = "123456789";
        
        ValidationResult result = roleValidator.validateSyncRequest(guildId, null, jda);
        
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrorMessage()).isEqualTo("Role list DTO cannot be null");
        assertThat(result.getGuild()).isNull();
    }

    @Test
    void validateSyncRequest_WithBlankGuildId_ShouldReturnFailure() {
        
        String guildId = "   ";
        ServerRoleListDTO roleListDTO = createValidRoleListDTO();

        ValidationResult result = roleValidator.validateSyncRequest(guildId, roleListDTO, jda);

        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrorMessage()).isEqualTo("Guild ID cannot be null or empty");
    }

    @Test
    void validateSyncRequest_WithNullGuildId_ShouldReturnFailure() {
        
        ServerRoleListDTO roleListDTO = createValidRoleListDTO();

        ValidationResult result = roleValidator.validateSyncRequest(null, roleListDTO, jda);

        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrorMessage()).isEqualTo("Guild ID cannot be null or empty");
    }

    @Test
    void validateSyncRequest_WithEmptyRoleList_ShouldReturnFailure() {
        
        String guildId = "123456789";
        ServerRoleListDTO roleListDTO = new ServerRoleListDTO(Collections.emptyList());

        ValidationResult result = roleValidator.validateSyncRequest(guildId, roleListDTO, jda);

        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrorMessage()).isEqualTo("Role list cannot be empty");
    }

    @Test
    void validateSyncRequest_WithNonExistentGuild_ShouldReturnFailure() {
        
        String guildId = "123456789";
        ServerRoleListDTO roleListDTO = createValidRoleListDTO();

        when(jda.getGuildById(guildId)).thenReturn(null);

        ValidationResult result = roleValidator.validateSyncRequest(guildId, roleListDTO, jda);

        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrorMessage()).isEqualTo("Guild not found: " + guildId);
    }

    @Test
    void validateSyncRequest_WithoutManageRolesPermission_ShouldReturnFailure() {
        
        String guildId = "123456789";
        ServerRoleListDTO roleListDTO = createValidRoleListDTO();

        when(jda.getGuildById(guildId)).thenReturn(guild);
        when(guild.getSelfMember()).thenReturn(botMember);
        when(botMember.hasPermission(Permission.MANAGE_ROLES)).thenReturn(false);

        ValidationResult result = roleValidator.validateSyncRequest(guildId, roleListDTO, jda);

        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrorMessage()).isEqualTo("Bot lacks MANAGE_ROLES permission");
    }

    @Test
    void validateSyncRequest_WithInvalidRoleName_ShouldReturnFailure() {
        
        String guildId = "123456789";
        ServerRoleListDTO roleListDTO = createRoleListDTOWithInvalidRole();

        when(jda.getGuildById(guildId)).thenReturn(guild);
        when(guild.getSelfMember()).thenReturn(botMember);
        when(botMember.hasPermission(Permission.MANAGE_ROLES)).thenReturn(true);
        
        ValidationResult result = roleValidator.validateSyncRequest(guildId, roleListDTO, jda);
        
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrorMessage()).contains("Invalid role:");
    }

    @Test
    void validateSyncRequest_WithTooLongRoleName_ShouldReturnFailure() {
        
        String guildId = "123456789";
        ServerRoleListDTO roleListDTO = createRoleListDTOWithLongName();

        when(jda.getGuildById(guildId)).thenReturn(guild);
        when(guild.getSelfMember()).thenReturn(botMember);
        when(botMember.hasPermission(Permission.MANAGE_ROLES)).thenReturn(true);
        
        ValidationResult result = roleValidator.validateSyncRequest(guildId, roleListDTO, jda);
        
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrorMessage()).contains("Role name too long");
    }

    @Test
    void validateSyncRequest_WithInvalidHexColor_ShouldReturnFailure() {
        
        String guildId = "123456789";
        ServerRoleListDTO roleListDTO = createRoleListDTOWithInvalidColor();

        when(jda.getGuildById(guildId)).thenReturn(guild);
        when(guild.getSelfMember()).thenReturn(botMember);
        when(botMember.hasPermission(Permission.MANAGE_ROLES)).thenReturn(true);
        
        ValidationResult result = roleValidator.validateSyncRequest(guildId, roleListDTO, jda);
        
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrorMessage()).contains("Invalid color format");
    }

    @Test
    void validateSyncRequest_WithValidHexColors_ShouldReturnSuccess() {
        
        String guildId = "123456789";
        ServerRoleListDTO roleListDTO = createRoleListDTOWithValidColors();

        when(jda.getGuildById(guildId)).thenReturn(guild);
        when(guild.getSelfMember()).thenReturn(botMember);
        when(botMember.hasPermission(Permission.MANAGE_ROLES)).thenReturn(true);
        
        ValidationResult result = roleValidator.validateSyncRequest(guildId, roleListDTO, jda);

        assertThat(result.isValid()).isTrue();
    }

    private ServerRoleListDTO createValidRoleListDTO() {
        ExtendedRoleDTO role = new ExtendedRoleDTO();
        role.setName("TestRole");
        role.setColor("#FF0000");

        return new ServerRoleListDTO(List.of(role));
    }

    private ServerRoleListDTO createRoleListDTOWithInvalidRole() {
        ExtendedRoleDTO role = new ExtendedRoleDTO();
        role.setName(null); // Invalid: null name

        return new ServerRoleListDTO(List.of(role));
    }

    private ServerRoleListDTO createRoleListDTOWithLongName() {
        ExtendedRoleDTO role = new ExtendedRoleDTO();
        role.setName("A".repeat(101)); // Invalid: too long

        return new ServerRoleListDTO(List.of(role));
    }

    private ServerRoleListDTO createRoleListDTOWithInvalidColor() {
        ExtendedRoleDTO role = new ExtendedRoleDTO();
        role.setName("TestRole");
        role.setColor("INVALID_COLOR"); // Invalid hex color

        return new ServerRoleListDTO(List.of(role));
    }

    private ServerRoleListDTO createRoleListDTOWithValidColors() {
        ExtendedRoleDTO role1 = new ExtendedRoleDTO();
        role1.setName("Role1");
        role1.setColor("#FF0000");

        ExtendedRoleDTO role2 = new ExtendedRoleDTO();
        role2.setName("Role2");
        role2.setColor("00FF00"); // Without #

        ExtendedRoleDTO role3 = new ExtendedRoleDTO();
        role3.setName("Role3");
        role3.setColor(null); // Null color should be valid

        return new ServerRoleListDTO(Arrays.asList(role1, role2, role3));
    }
}