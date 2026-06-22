package io.github.trae.velocity.framework.utility;

import com.velocitypowered.api.proxy.Player;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Utility methods for querying the state of the proxy.
 */
@UtilityClass
public class UtilServer {

    /**
     * Returns the players currently connected to the proxy, optionally filtered by a predicate.
     *
     * @param predicate the filter to apply; players failing the test are excluded.
     *                  If {@code null}, all connected players are returned.
     * @return a mutable {@link List} of matching players
     */
    public static List<Player> getOnlinePlayers(final Predicate<Player> predicate) {
        final List<Player> playerList = new ArrayList<>(UtilPlugin.getInstance().getProxyServer().getAllPlayers());

        if (predicate != null) {
            playerList.removeIf(predicate.negate());
        }

        return playerList;
    }

    /**
     * Returns all players currently connected to the proxy.
     *
     * @return a mutable {@link List} of all connected players
     */
    public static List<Player> getOnlinePlayers() {
        return getOnlinePlayers(null);
    }
}