package ch.zhaw.it.pm4.discordmanagerbe.bots.guildchannelrolepermission.validator;

import ch.zhaw.it.pm4.discordmanagerbe.bots.guildchannelrolepermission.exception.PermissionConflictException;
import net.dv8tion.jda.api.Permission;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * Validator for permission-related operations.
 * Provides methods to validate IDs and check for permission conflicts.
 */
@Component
public class PermissionValidator {

    /**
     * Validates that the provided IDs are not null or empty.
     * @param ids the IDs to validate
     */
    public void validateIds(String... ids) {
        for (String id : ids) {
            if (id == null || id.trim().isEmpty()) {
                throw new IllegalArgumentException("ID cannot be null or empty");
            }
        }
    }

    /**
     * Validates that there are no conflicting permissions
     * @param allowed the set of allowed permissions
     * @param denied the set of denied permissions
     */
    public void validateNoConflicts(Set<Permission> allowed, Set<Permission> denied) {
        Set<Permission> conflicts = new HashSet<>(allowed);
        conflicts.retainAll(denied);

        if (!conflicts.isEmpty()) {
            throw new PermissionConflictException(
                    "Conflicting permissions found (same permission in both allow and deny): " + conflicts
            );
        }
    }
}