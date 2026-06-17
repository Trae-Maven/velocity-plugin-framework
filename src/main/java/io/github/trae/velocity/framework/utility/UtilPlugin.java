package io.github.trae.velocity.framework.utility;

import io.github.trae.utilities.UtilJava;
import io.github.trae.velocity.framework.VelocityPlugin;
import lombok.Getter;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Registry of all {@link VelocityPlugin} instances built on this framework.
 *
 * <p>Plugins add themselves on initialization and remove themselves on shutdown, allowing
 * other utilities to resolve a plugin instance by class or simple name without an explicit
 * reference.</p>
 */
@UtilityClass
public class UtilPlugin {

    /**
     * The list of currently registered framework plugin instances.
     */
    @Getter
    private static final List<VelocityPlugin> internalPluginList = new ArrayList<>();

    /**
     * Registers a plugin instance in the internal list.
     *
     * @param velocityPlugin the plugin to add
     */
    public static void addInternalPlugin(final VelocityPlugin velocityPlugin) {
        internalPluginList.add(velocityPlugin);
    }

    /**
     * Removes a plugin instance from the internal list.
     *
     * @param velocityPlugin the plugin to remove
     */
    public static void removeInternalPlugin(final VelocityPlugin velocityPlugin) {
        internalPluginList.remove(velocityPlugin);
    }

    /**
     * Resolves a registered plugin by its simple class name, case-insensitively.
     *
     * @param name the simple class name to match
     * @return the matching plugin, or an empty optional if none matches
     */
    public static Optional<VelocityPlugin> getInternalPluginByName(final String name) {
        return internalPluginList.stream().filter(velocityPlugin -> velocityPlugin.getClass().getSimpleName().equalsIgnoreCase(name)).findFirst();
    }

    /**
     * Resolves the first registered plugin assignable to the given class.
     *
     * @param clazz    the plugin class to match
     * @param <Plugin> the plugin type
     * @return the matching plugin instance, or {@code null} if none matches
     */
    public static <Plugin extends VelocityPlugin> Plugin getInstanceByClass(final Class<Plugin> clazz) {
        return UtilJava.cast(clazz, internalPluginList.stream().filter(clazz::isInstance).findFirst().orElse(null));
    }

    /**
     * Resolves the first registered plugin instance.
     *
     * @return the first registered {@link VelocityPlugin}, or {@code null} if none are registered
     * @see #getInstanceByClass(Class)
     */
    public static VelocityPlugin getInstance() {
        return getInstanceByClass(VelocityPlugin.class);
    }
}