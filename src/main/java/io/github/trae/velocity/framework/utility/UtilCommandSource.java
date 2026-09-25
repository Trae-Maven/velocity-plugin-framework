package io.github.trae.velocity.framework.utility;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.ConsoleCommandSource;
import com.velocitypowered.api.proxy.Player;
import lombok.experimental.UtilityClass;

import java.util.UUID;

/**
 * Provides utility methods for working with Velocity {@link CommandSource command sources}.
 */
@UtilityClass
public class UtilCommandSource {

    private static final UUID CONSOLE_ID = new UUID(0L, 0L);
    private static final String CONSOLE_NAME = "Console";

    /**
     * Gets the unique identifier of a command source.
     *
     * @param commandSource the command source
     * @return the player's unique identifier, the console identifier for the console, or {@code null} if unsupported
     */
    public static UUID getCommandSourceId(final CommandSource commandSource) {
        if (commandSource instanceof final Player player) {
            return player.getUniqueId();
        }

        if (commandSource instanceof ConsoleCommandSource) {
            return CONSOLE_ID;
        }

        return null;
    }

    /**
     * Gets the name of a command source.
     *
     * @param commandSource the command source
     * @return the player's username, {@code Console} for the console, or {@code null} if unsupported
     */
    public static String getCommandSourceName(final CommandSource commandSource) {
        if (commandSource instanceof final Player player) {
            return player.getUsername();
        }

        if (commandSource instanceof ConsoleCommandSource) {
            return CONSOLE_NAME;
        }

        return null;
    }
}