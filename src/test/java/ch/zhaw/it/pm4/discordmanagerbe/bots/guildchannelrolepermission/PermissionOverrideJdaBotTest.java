package ch.zhaw.it.pm4.discordmanagerbe.bots.guildchannelrolepermission;

import ch.zhaw.it.pm4.discordmanagerbe.dto.ChannelRolePermissionsDTO;
import ch.zhaw.it.pm4.discordmanagerbe.bots.guildchannelrolepermission.model.PermissionOverrideResult;
import ch.zhaw.it.pm4.discordmanagerbe.bots.guildchannelrolepermission.service.PermissionOverrideService;
import net.dv8tion.jda.api.JDA;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PermissionOverrideJdaBotTest {

    @Mock
    private JDA jda;
    
    @Mock
    private PermissionOverrideService permissionOverrideService;

    @InjectMocks
    private PermissionOverrideJdaBot permissionOverrideJdaBot;

    private static final String SERVER_ID = "123456789";
    private static final String CHANNEL_ID = "987654321";
    private static final String ROLE_ID = "555666777";

    @Test
    void setRolePermissionOverride_Success() {
        // Arrange
        ChannelRolePermissionsDTO dto = new ChannelRolePermissionsDTO();
        PermissionOverrideResult<Void> successResult = PermissionOverrideResult.success();
        
        when(permissionOverrideService.applyPermissionOverrides(SERVER_ID, CHANNEL_ID, dto))
                .thenReturn(successResult);

        // Act
        boolean result = permissionOverrideJdaBot.setRolePermissionOverride(SERVER_ID, CHANNEL_ID, dto);

        // Assert
        assertTrue(result);
    }

    @Test
    void setRolePermissionOverride_Failure() {
        // Arrange
        ChannelRolePermissionsDTO dto = new ChannelRolePermissionsDTO();
        PermissionOverrideResult<Void> failureResult = PermissionOverrideResult.failure("Error occurred");
        
        when(permissionOverrideService.applyPermissionOverrides(SERVER_ID, CHANNEL_ID, dto))
                .thenReturn(failureResult);

        // Act
        boolean result = permissionOverrideJdaBot.setRolePermissionOverride(SERVER_ID, CHANNEL_ID, dto);

        // Assert
        assertFalse(result);
    }

    @Test
    void getAllChannelPermissionOverrides_Success() {
        // Arrange
        ChannelRolePermissionsDTO expectedDto = new ChannelRolePermissionsDTO();
        PermissionOverrideResult<ChannelRolePermissionsDTO> successResult = 
                PermissionOverrideResult.success(expectedDto);
        
        when(permissionOverrideService.getAllChannelPermissionOverrides(SERVER_ID, CHANNEL_ID))
                .thenReturn(successResult);

        // Act
        ChannelRolePermissionsDTO result = permissionOverrideJdaBot
                .getAllChannelPermissionOverrides(SERVER_ID, CHANNEL_ID);

        // Assert
        assertEquals(expectedDto, result);
    }

    @Test
    void getAllChannelPermissionOverrides_Failure() {
        // Arrange
        PermissionOverrideResult<ChannelRolePermissionsDTO> failureResult = 
                PermissionOverrideResult.failure("Error occurred");
        
        when(permissionOverrideService.getAllChannelPermissionOverrides(SERVER_ID, CHANNEL_ID))
                .thenReturn(failureResult);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> 
                permissionOverrideJdaBot.getAllChannelPermissionOverrides(SERVER_ID, CHANNEL_ID));
    }

    @Test
    void removeRolePermissionOverride_Success() {
        // Arrange
        PermissionOverrideResult<Void> successResult = PermissionOverrideResult.success();
        
        when(permissionOverrideService.removeRolePermissionOverride(SERVER_ID, CHANNEL_ID, ROLE_ID))
                .thenReturn(successResult);

        // Act
        boolean result = permissionOverrideJdaBot.removeRolePermissionOverride(SERVER_ID, CHANNEL_ID, ROLE_ID);

        // Assert
        assertTrue(result);
    }

    @Test
    void removeRolePermissionOverride_Failure() {
        // Arrange
        PermissionOverrideResult<Void> failureResult = PermissionOverrideResult.failure("Error occurred");
        
        when(permissionOverrideService.removeRolePermissionOverride(SERVER_ID, CHANNEL_ID, ROLE_ID))
                .thenReturn(failureResult);

        // Act
        boolean result = permissionOverrideJdaBot.removeRolePermissionOverride(SERVER_ID, CHANNEL_ID, ROLE_ID);

        // Assert
        assertFalse(result);
    }
}