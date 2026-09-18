package io.github.trae.velocity.framework.utility;

import com.velocitypowered.api.permission.PermissionSubject;
import io.github.trae.utilities.UtilString;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.UtilityClass;

/**
 * Provides utility methods for checking Velocity permissions with support for global
 * wildcard permissions.
 *
 * <p>A permission subject is considered to have a permission when any of the following
 * conditions are satisfied:</p>
 *
 * <ul>
 *     <li>The requested permission is empty.</li>
 *     <li>The subject has the global {@code *} permission.</li>
 *     <li>The subject has the configured custom wildcard permission.</li>
 *     <li>The subject directly has the requested permission.</li>
 * </ul>
 *
 * <p>Velocity has no operator concept; the console source is granted every permission by the
 * proxy itself, so no explicit case is needed for it.</p>
 */
@UtilityClass
public class UtilPermission {

    /**
     * Additional wildcard node treated as granting every permission, such as a network admin
     * node. Defaults to {@code *}.
     */
    @Getter
    @Setter
    private static String customWildcardPermission = "*";

    /**
     * Determines whether a permission subject has access to the specified permission.
     *
     * <p>An empty permission is always permitted. Otherwise, access is granted when the subject
     * has the global {@code *} permission, has the configured custom wildcard permission, or
     * directly has the requested permission.</p>
     *
     * @param permissionSubject the subject to check
     * @param permission        the permission to check
     * @return {@code true} if access is permitted, otherwise {@code false}
     */
    public static boolean hasPermission(final PermissionSubject permissionSubject, final String permission) {
        if (UtilString.isEmpty(permission)) {
            return true;
        }

        if (permissionSubject != null) {
            if (permissionSubject.hasPermission("*")) {
                return true;
            }

            if (permissionSubject.hasPermission(customWildcardPermission)) {
                return true;
            }

            return permissionSubject.hasPermission(permission);
        }

        return false;
    }
}