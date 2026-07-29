package ch.zhaw.it.pm4.discordmanagerbe.bots.guildchannelrolepermission.service;

import ch.zhaw.it.pm4.discordmanagerbe.dto.ChannelRolePermissionDTO;
import ch.zhaw.it.pm4.discordmanagerbe.dto.ChannelRolePermissionsDTO;
import ch.zhaw.it.pm4.discordmanagerbe.bots.guildchannelrolepermission.exception.EntityNotFoundException;
import ch.zhaw.it.pm4.discordmanagerbe.bots.guildchannelrolepermission.mapper.PermissionMapper;
import ch.zhaw.it.pm4.discordmanagerbe.bots.guildchannelrolepermission.model.PermissionOverrideResult;
import ch.zhaw.it.pm4.discordmanagerbe.bots.guildchannelrolepermission.validator.PermissionValidator;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.PermissionOverride;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.attribute.IPermissionContainer;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction;
import net.dv8tion.jda.api.requests.restaction.PermissionOverrideAction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PermissionOverrideServiceTest {

    @Mock
    private GuildEntityService guildEntityService;
    
    @Mock
    private PermissionValidator validator;
    
    @Mock
    private PermissionMapper permissionMapper;
    
    @Mock
    private Guild guild;
    
    @Mock
    private GuildChannel channel;
    
    @Mock
    private Role role;
    
    @Mock
    private PermissionOverrideAction permissionOverrideAction;

    @Mock
    private IPermissionContainer permissionContainer;

    @Mock
    private PermissionOverride permissionOverride;
    
    @Mock
    private RestAction<Void> restAction;

    @Mock
    private AuditableRestAction auditableRestAction;

    @InjectMocks
    private PermissionOverrideService permissionOverrideService;

    private static final String SERVER_ID = "123456789";
    private static final String CHANNEL_ID = "987654321";
    private static final String ROLE_ID = "555666777";

    private ChannelRolePermissionsDTO createTestDTO() {
        ChannelRolePermissionDTO override = new ChannelRolePermissionDTO();
        override.setRoleId(ROLE_ID);
        override.setChannelId(CHANNEL_ID);
        override.setAllowedPermissions(Arrays.asList("SEND_MESSAGES", "READ_MESSAGES"));
        override.setDeniedPermissions(Arrays.asList("MANAGE_MESSAGES"));

        ChannelRolePermissionsDTO dto = new ChannelRolePermissionsDTO();
        dto.setOverrides(Collections.singletonList(override));
        return dto;
    }

    @BeforeEach
    void setUp() {
        // Common mock setup
        lenient().when(guildEntityService.getGuild(SERVER_ID)).thenReturn(Optional.of(guild));
        lenient().when(guildEntityService.getChannel(guild, CHANNEL_ID)).thenReturn(Optional.of(channel));
        lenient().when(guildEntityService.getRole(guild, ROLE_ID)).thenReturn(Optional.of(role));

        lenient().when(channel.getPermissionContainer()).thenReturn(permissionContainer);
        lenient().when(permissionContainer.upsertPermissionOverride(role)).thenReturn(permissionOverrideAction);
        lenient().when(permissionOverrideAction.setAllowed(anyCollection())).thenReturn(permissionOverrideAction);
        lenient().when(permissionOverrideAction.setDenied(anyCollection())).thenReturn(permissionOverrideAction);
        lenient().when(permissionOverrideAction.complete()).thenReturn(null);

        lenient().when(restAction.complete()).thenReturn(null);
    }

    @Test
    void applyPermissionOverrides_Success() {
        // Arrange
        ChannelRolePermissionsDTO dto = createTestDTO();
        Set<Permission> allowedPerms = Set.of(Permission.MESSAGE_SEND, Permission.VIEW_CHANNEL);
        Set<Permission> deniedPerms = Set.of(Permission.MESSAGE_MANAGE);

        when(permissionMapper.parsePermissions(anyList()))
                .thenReturn(allowedPerms)
                .thenReturn(deniedPerms);

        // Act
        PermissionOverrideResult<Void> result = permissionOverrideService
                .applyPermissionOverrides(SERVER_ID, CHANNEL_ID, dto);

        // Assert
        assertTrue(result.isSuccess());
        verify(validator).validateIds(SERVER_ID, CHANNEL_ID);
        verify(validator).validateNoConflicts(allowedPerms, deniedPerms);
        verify(permissionOverrideAction).complete();
    }

    @Test
    void applyPermissionOverrides_ValidationFailure() {
        // Arrange
        ChannelRolePermissionsDTO dto = createTestDTO();
        doThrow(new IllegalArgumentException("Invalid ID"))
                .when(validator).validateIds(SERVER_ID, CHANNEL_ID);

        // Act
        PermissionOverrideResult<Void> result = permissionOverrideService
                .applyPermissionOverrides(SERVER_ID, CHANNEL_ID, dto);

        // Assert
        assertEquals("Failed to apply permission overrides", result.getMessage());
        assertTrue(result.getException().isPresent());
    }

    @Test
    void applyPermissionOverrides_GuildNotFound() {
        // Arrange
        ChannelRolePermissionsDTO dto = createTestDTO();
        when(guildEntityService.getGuild(SERVER_ID)).thenReturn(Optional.empty());

        // Act
        PermissionOverrideResult<Void> result = permissionOverrideService
                .applyPermissionOverrides(SERVER_ID, CHANNEL_ID, dto);

        // Assert
        assertTrue(result.getException().isPresent());
        assertInstanceOf(EntityNotFoundException.class, result.getException().get());
    }

    @Test
    void getAllChannelPermissionOverrides_Success() {
        // Arrange
        List<PermissionOverride> overrides = List.of(permissionOverride);
        ChannelRolePermissionDTO expectedDTO = new ChannelRolePermissionDTO();
        expectedDTO.setRoleId(ROLE_ID);

        when(channel.getPermissionContainer()).thenReturn(permissionContainer);
        when(permissionContainer.getPermissionOverrides()).thenReturn(overrides);
        when(permissionOverride.isRoleOverride()).thenReturn(true);
        when(permissionMapper.mapToDTO(permissionOverride)).thenReturn(expectedDTO);

        // Act
        PermissionOverrideResult<ChannelRolePermissionsDTO> result = permissionOverrideService
                .getAllChannelPermissionOverrides(SERVER_ID, CHANNEL_ID);

        // Assert
        assertTrue(result.isSuccess());
        assertTrue(result.getData().isPresent());
        assertEquals(1, result.getData().get().getOverrides().size());
        assertEquals(ROLE_ID, result.getData().get().getOverrides().get(0).getRoleId());
    }

    @Test
    void getAllChannelPermissionOverrides_ChannelNotFound() {
        // Arrange
        when(guildEntityService.getChannel(guild, CHANNEL_ID)).thenReturn(Optional.empty());

        // Act
        PermissionOverrideResult<ChannelRolePermissionsDTO> result = permissionOverrideService
                .getAllChannelPermissionOverrides(SERVER_ID, CHANNEL_ID);

        // Assert
        assertTrue(result.getException().isPresent());
        assertInstanceOf(EntityNotFoundException.class, result.getException().get());
    }

    @Test
    void removeRolePermissionOverride_Success() {
        // Arrange
        when(channel.getPermissionContainer()).thenReturn(permissionContainer);
        when(permissionContainer.getPermissionOverride(role)).thenReturn(permissionOverride);
        when(permissionOverride.delete()).thenReturn(auditableRestAction);

        // Act
        PermissionOverrideResult<Void> result = permissionOverrideService
                .removeRolePermissionOverride(SERVER_ID, CHANNEL_ID, ROLE_ID);

        // Assert
        assertTrue(result.isSuccess());
        verify(permissionOverride).delete();
    }

    @Test
    void removeRolePermissionOverride_OverrideNotFound() {
        // Arrange
        when(channel.getPermissionContainer()).thenReturn(permissionContainer);
        when(permissionContainer.getPermissionOverride(role)).thenReturn(null);

        // Act
        PermissionOverrideResult<Void> result = permissionOverrideService
                .removeRolePermissionOverride(SERVER_ID, CHANNEL_ID, ROLE_ID);

        // Assert
        assertTrue(result.getMessage().contains("No permission override found"));
    }

    @Test
    void removeRolePermissionOverride_RoleNotFound() {
        // Arrange
        when(guildEntityService.getRole(guild, ROLE_ID)).thenReturn(Optional.empty());

        // Act
        PermissionOverrideResult<Void> result = permissionOverrideService
                .removeRolePermissionOverride(SERVER_ID, CHANNEL_ID, ROLE_ID);

        // Assert
        assertTrue(result.getException().isPresent());
        assertInstanceOf(EntityNotFoundException.class, result.getException().get());
    }
}