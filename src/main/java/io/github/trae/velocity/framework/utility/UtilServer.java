package io.github.trae.velocity.framework.utility;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import io.github.trae.velocity.framework.utility.search.types.NetworkPlayerSearchEngine;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * Utility methods for querying the state of the proxy.
 *
 * <p>Covers network-wide player lookups by unique id, username, and user-supplied search input, plus
 * filtered snapshots of the connected player set. Every lookup is served through the proxy handle on
 * the first registered plugin, so these helpers are only usable once a plugin has registered itself
 * with {@link UtilPlugin}.</p>
 */
@UtilityClass
public class UtilServer {

    /**
     * Search engine backing the {@code searchPlayer} helpers.
     */
    private static final NetworkPlayerSearchEngine PLAYER_SEARCH_ENGINE = new NetworkPlayerSearchEngine();

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

    /**
     * Resolves a connected player by their unique id.
     *
     * @param id the player's unique id
     * @return an {@link Optional} containing the player, or empty if none is connected with that id
     */
    public static Optional<Player> getOnlinePlayerById(final UUID id) {
        return UtilPlugin.getInstance().getProxyServer().getPlayer(id);
    }

    /**
     * Resolves a connected player by their exact username, ignoring case.
     *
     * @param name the player's username
     * @return an {@link Optional} containing the player, or empty if none is connected with that name
     */
    public static Optional<Player> getOnlinePlayerByName(final String name) {
        return UtilPlugin.getInstance().getProxyServer().getPlayer(name);
    }

    /**
     * Searches the connected players for the player identified by the given input.
     *
     * <p>An exact name match wins immediately, otherwise a single partial match is returned. An empty
     * or ambiguous search yields an empty result and, when informing is enabled, messages the source
     * with the outcome.</p>
     *
     * @param sender    the command source to inform of the search outcome
     * @param input     the search input
     * @param inform    whether to message the source when the search fails to resolve
     * @param predicate an optional filter applied before matching, or null to consider every player
     * @return the resolved player, or {@link Optional#empty()} if the search was empty or ambiguous
     */
    public static Optional<Player> searchPlayer(final CommandSource sender, final String input, final boolean inform, final Predicate<Player> predicate) {
        return PLAYER_SEARCH_ENGINE.find(sender, input, inform, predicate);
    }

    /**
     * Searches the connected players for the player identified by the given input, without filtering.
     *
     * @param sender the command source to inform of the search outcome
     * @param input  the search input
     * @param inform whether to message the source when the search fails to resolve
     * @return the resolved player, or {@link Optional#empty()} if the search was empty or ambiguous
     * @see #searchPlayer(CommandSource, String, boolean, Predicate)
     */
    public static Optional<Player> searchPlayer(final CommandSource sender, final String input, final boolean inform) {
        return searchPlayer(sender, input, inform, null);
    }
}