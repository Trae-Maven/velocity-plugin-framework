package io.github.trae.velocity.framework.utility;

import com.velocitypowered.api.proxy.Player;
import lombok.experimental.UtilityClass;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Optional;

/**
 * Provides utility helper methods for players.
 */
@UtilityClass
public class UtilPlayer {

    /**
     * Gets the IP address of the specified player.
     *
     * @param player the player
     * @return the player's IP address, or empty if unavailable
     */
    public static Optional<String> getIpAddress(final Player player) {
        return Optional.ofNullable(player.getRemoteAddress())
                .map(InetSocketAddress::getAddress)
                .map(InetAddress::getHostAddress);
    }
}