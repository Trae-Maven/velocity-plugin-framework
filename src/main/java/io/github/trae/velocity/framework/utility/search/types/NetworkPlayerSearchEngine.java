package io.github.trae.velocity.framework.utility.search.types;

import com.velocitypowered.api.proxy.Player;
import io.github.trae.velocity.framework.utility.UtilColor;
import io.github.trae.velocity.framework.utility.UtilServer;
import io.github.trae.velocity.framework.utility.enums.ChatColor;
import io.github.trae.velocity.framework.utility.search.VelocitySearchEngine;

import java.util.Locale;

/**
 * Search engine resolving players connected anywhere on the network.
 *
 * <p>Candidates are read live from {@link UtilServer#getOnlinePlayers()} on every search, so the
 * scope is every player on the proxy rather than one backend server, and matched on username:
 * case-insensitive equality for an exact hit, case-insensitive substring for a partial one.</p>
 */
public class NetworkPlayerSearchEngine extends VelocitySearchEngine<Player> {

    /**
     * Creates a search engine over the connected player set.
     */
    public NetworkPlayerSearchEngine() {
        super("Network Player Search", UtilServer::getOnlinePlayers);
    }

    /**
     * {@inheritDoc}
     *
     * @return the player's username serialized in yellow
     */
    @Override
    protected String getTypeFormat(final Player player) {
        return UtilColor.serialize(ChatColor.YELLOW.getColor(), player.getUsername());
    }

    /**
     * {@inheritDoc}
     *
     * <p>Compares the player's username to the input, ignoring case.
     */
    @Override
    protected boolean isExact(final Player player, final String result) {
        return player.getUsername().equalsIgnoreCase(result);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Tests whether the player's username contains the input, ignoring case.
     */
    @Override
    protected boolean isMatching(final Player player, final String result) {
        return player.getUsername().toLowerCase(Locale.ROOT).contains(result.toLowerCase(Locale.ROOT));
    }
}