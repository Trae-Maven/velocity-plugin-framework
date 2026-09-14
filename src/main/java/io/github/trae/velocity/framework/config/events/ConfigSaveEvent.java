package io.github.trae.velocity.framework.config.events;

import io.github.trae.velocity.framework.VelocityPlugin;
import io.github.trae.velocity.framework.event.CustomEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Fired after a configuration belonging to a {@link VelocityPlugin}
 * has been successfully saved.
 *
 * <p>Provides the plugin that owns the configuration and the
 * configuration class that was saved.</p>
 */
@AllArgsConstructor
@Getter
public class ConfigSaveEvent extends CustomEvent {

    /**
     * The plugin that owns the saved configuration.
     */
    private final VelocityPlugin plugin;

    /**
     * The configuration class that was saved.
     */
    private final Class<?> configurationClass;
}