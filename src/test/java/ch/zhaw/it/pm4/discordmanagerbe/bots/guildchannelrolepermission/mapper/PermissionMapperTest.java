package ch.zhaw.it.pm4.discordmanagerbe.bots.guildchannelrolepermission.mapper;

import ch.zhaw.it.pm4.discordmanagerbe.dto.ChannelRolePermissionDTO;
import ch.zhaw.it.pm4.discordmanagerbe.bots.guildchannelrolepermission.config.PermissionMappingConfig;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.PermissionOverride;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.unions.GuildChannelUnion;
import net.dv8tion.jda.api.entities.channel.unions.IPermissionContainerUnion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class PermissionMapperTest {

    @Mock
    private PermissionMappingConfig mappingConfig;
    
    @Mock
    private PermissionOverride permissionOverride;
    
    @Mock
    private Role role;
    
    @Mock
    private GuildChannelUnion channel;

    @Mock
    private IPermissionContainerUnion permissionContainer;

    @InjectMocks
    private PermissionMapper permissionMapper;

    private Map<String, Permission> testMappings;

    @BeforeEach
    void setUp() {
        testMappings = new HashMap<>();
        testMappings.put("SEND_MESSAGES", Permission.MESSAGE_SEND);
        testMappings.put("READ_MESSAGES", Permission.VIEW_CHANNEL);
        testMappings.put("MANAGE_MESSAGES", Permission.MESSAGE_MANAGE);

        lenient().when(mappingConfig.getPermissionMapping()).thenReturn(testMappings);
    }

    @Test
    void parsePermissions_ValidMappedPermissions() {
        // Arrange
        List<String> permissionStrings = Arrays.asList("SEND_MESSAGES", "READ_MESSAGES");

        // Act
        Set<Permission> result = permissionMapper.parsePermissions(permissionStrings);

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.contains(Permission.MESSAGE_SEND));
        assertTrue(result.contains(Permission.VIEW_CHANNEL));
    }

    @Test
    void parsePermissions_DirectEnumPermissions() {
        // Arrange
        List<String> permissionStrings = Arrays.asList("ADMINISTRATOR", "KICK_MEMBERS");

        // Act
        Set<Permission> result = permissionMapper.parsePermissions(permissionStrings);

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.contains(Permission.ADMINISTRATOR));
        assertTrue(result.contains(Permission.KICK_MEMBERS));
    }

    @Test
    void parsePermissions_MixedCaseAndWhitespace() {
        // Arrange
        List<String> permissionStrings = Arrays.asList("  send_messages  ", "Read_Messages", "ADMINISTRATOR");

        // Act
        Set<Permission> result = permissionMapper.parsePermissions(permissionStrings);

        // Assert
        assertEquals(3, result.size()); // send_messages should map, Read_Messages should map, ADMINISTRATOR should work
        assertTrue(result.contains(Permission.MESSAGE_SEND));
        assertTrue(result.contains(Permission.ADMINISTRATOR));
    }

    @Test
    void parsePermissions_NullAndEmptyValues() {
        // Arrange
        List<String> permissionStrings = Arrays.asList("SEND_MESSAGES", null, "", "  ", "READ_MESSAGES");

        // Act
        Set<Permission> result = permissionMapper.parsePermissions(permissionStrings);

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.contains(Permission.MESSAGE_SEND));
        assertTrue(result.contains(Permission.VIEW_CHANNEL));
    }

    @Test
    void parsePermissions_NullInput() {
        // Act
        Set<Permission> result = permissionMapper.parsePermissions(null);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void parsePermissions_UnknownPermissions() {
        // Arrange
        List<String> permissionStrings = Arrays.asList("SEND_MESSAGES", "UNKNOWN_PERMISSION", "FAKE_PERM");

        // Act
        Set<Permission> result = permissionMapper.parsePermissions(permissionStrings);

        // Assert
        assertEquals(1, result.size());
        assertTrue(result.contains(Permission.MESSAGE_SEND));
    }

    @Test
    void permissionsToStringList_Success() {
        // Arrange
        Set<Permission> permissions = Set.of(Permission.MESSAGE_SEND, Permission.ADMINISTRATOR, Permission.VIEW_CHANNEL);

        // Act
        List<String> result = permissionMapper.permissionsToStringList(permissions);

        // Assert
        assertEquals(3, result.size());
        assertTrue(result.contains("MESSAGE_SEND"));
        assertTrue(result.contains("ADMINISTRATOR"));
        assertTrue(result.contains("VIEW_CHANNEL"));
        // Check that it's sorted
        assertEquals("ADMINISTRATOR", result.get(0));
    }

    @Test
    void permissionsToStringList_EmptySet() {
        // Arrange
        Set<Permission> permissions = Collections.emptySet();

        // Act
        List<String> result = permissionMapper.permissionsToStringList(permissions);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void mapToDTO_Success() {
        // Arrange
        String roleId = "123456";
        String channelId = "789012";
        EnumSet<Permission> allowed = EnumSet.of(Permission.MESSAGE_SEND, Permission.VIEW_CHANNEL);
        EnumSet<Permission> denied = EnumSet.of(Permission.MESSAGE_MANAGE);

        lenient().when(permissionOverride.getRole()).thenReturn(role);
        lenient().when(permissionOverride.getChannel()).thenReturn(permissionContainer);
        lenient().when(permissionContainer.getId()).thenReturn(channelId);
        lenient().when(role.getId()).thenReturn(roleId);
        lenient().when(channel.getId()).thenReturn(channelId);
        lenient().when(permissionOverride.getAllowed()).thenReturn(allowed);
        lenient().when(permissionOverride.getDenied()).thenReturn(denied);

        // Act
        ChannelRolePermissionDTO result = permissionMapper.mapToDTO(permissionOverride);

        // Assert
        assertNotNull(result);
        assertEquals(roleId, result.getRoleId());
        assertEquals(channelId, result.getChannelId());
        assertEquals(2, result.getAllowedPermissions().size());
        assertEquals(1, result.getDeniedPermissions().size());
        assertTrue(result.getAllowedPermissions().contains("MESSAGE_SEND"));
        assertTrue(result.getAllowedPermissions().contains("VIEW_CHANNEL"));
        assertTrue(result.getDeniedPermissions().contains("MESSAGE_MANAGE"));
    }
}