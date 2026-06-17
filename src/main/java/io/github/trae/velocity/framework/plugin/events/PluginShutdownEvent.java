package io.github.trae.velocity.framework.plugin.events;

import io.github.trae.velocity.framework.VelocityPlugin;
import io.github.trae.velocity.framework.event.CustomEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Fired before a {@link VelocityPlugin} begins its hierarchy teardown.
 *
 * <p>Dispatched by {@link VelocityPlugin#shutdownPlugin()} before any components are shut
 * down, allowing listeners to react while the plugin and its components are still active.</p>
 */
@AllArgsConstructor
@Getter
public class PluginShutdownEvent extends CustomEvent {

    /**
     * The plugin that is about to shut down.
     */
    private final VelocityPlugin plugin;
}