package io.github.trae.velocity.framework.plugin.events;

import io.github.trae.velocity.framework.VelocityPlugin;
import io.github.trae.velocity.framework.event.CustomEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Fired after a {@link VelocityPlugin} has completed its hierarchy initialization.
 *
 * <p>Dispatched by {@link VelocityPlugin#initializePlugin()} once all components have been
 * initialized, allowing listeners to react to the plugin becoming fully ready.</p>
 */
@AllArgsConstructor
@Getter
public class PluginInitializeEvent extends CustomEvent {

    /**
     * The plugin that has been initialized.
     */
    private final VelocityPlugin plugin;
}