package io.github.trae.velocity.framework.utility;

import com.velocitypowered.api.command.CommandSource;
import io.github.trae.utilities.UtilJava;
import io.github.trae.velocity.framework.VelocityPlugin;
import io.github.trae.velocity.framework.utility.search.types.InternalPluginSearchEngine;
import lombok.experimental.UtilityClass;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Utility class for registering and retrieving active {@link VelocityPlugin} instances.
 *
 * <p>Maintains an internal registry of framework plugins keyed by upper-cased plugin name, exposing
 * direct lookup by name, name-style searching backed by {@link InternalPluginSearchEngine}, and
 * resolution by plugin class. Velocity has no equivalent of a static plugin accessor, so every
 * lookup here is served from this registry, which plugins populate as they enable. Used internally
 * by framework utilities that require a plugin reference without direct access to the plugin
 * hierarchy.</p>
 */
@UtilityClass
public class UtilPlugin {

    /**
     * Registry of framework plugins keyed by upper-cased plugin name, in registration order.
     */
    private static final LinkedHashMap<String, VelocityPlugin> internalPluginMap = new LinkedHashMap<>();

    /**
     * Search engine used to resolve plugin names for
     * {@link #searchInternalPlugin(CommandSource, String, boolean, Predicate)}.
     */
    private static final InternalPluginSearchEngine INTERNAL_PLUGIN_SEARCH_ENGINE = new InternalPluginSearchEngine();

    /**
     * Returns an immutable snapshot of all registered internal plugins.
     *
     * @return an unmodifiable list of all {@link VelocityPlugin} instances
     */
    public static List<VelocityPlugin> getInternalPlugins() {
        return List.copyOf(internalPluginMap.values());
    }

    /**
     * Registers a plugin in the internal registry, replacing any existing entry under the same name.
     *
     * @param velocityPlugin the plugin to register
     */
    public static void addInternalPlugin(final VelocityPlugin velocityPlugin) {
        internalPluginMap.put(velocityPlugin.getPluginName().toUpperCase(Locale.ROOT), velocityPlugin);
    }

    /**
     * Removes a plugin from the internal registry, doing nothing if it was never registered.
     *
     * @param velocityPlugin the plugin to remove
     */
    public static void removeInternalPlugin(final VelocityPlugin velocityPlugin) {
        internalPluginMap.remove(velocityPlugin.getPluginName().toUpperCase(Locale.ROOT));
    }

    /**
     * Looks up a registered plugin by exact name, ignoring case.
     *
     * @param name the plugin name to resolve
     * @return the registered plugin, or null if no plugin is registered under that name
     */
    public static VelocityPlugin getInternalPluginByName(final String name) {
        return internalPluginMap.getOrDefault(name.toUpperCase(Locale.ROOT), null);
    }

    /**
     * Searches the internal registry for the plugin identified by the given input.
     *
     * <p>An exact name match wins immediately, otherwise a single partial match is returned. An empty
     * or ambiguous search yields an empty result and, when informing is enabled, messages the source
     * with the outcome.</p>
     *
     * @param sender    the command source to inform of the search outcome
     * @param input     the search input
     * @param inform    whether to message the source when the search fails to resolve
     * @param predicate an optional filter applied before matching, or null to consider every plugin
     * @return the resolved plugin, or {@link Optional#empty()} if the search was empty or ambiguous
     */
    public static Optional<VelocityPlugin> searchInternalPlugin(final CommandSource sender, final String input, final boolean inform, final Predicate<VelocityPlugin> predicate) {
        return INTERNAL_PLUGIN_SEARCH_ENGINE.find(
                sender,
                input,
                inform,
                predicate
        );
    }

    /**
     * Searches the internal registry for the plugin identified by the given input, without filtering.
     *
     * @param sender the command source to inform of the search outcome
     * @param input  the search input
     * @param inform whether to message the source when the search fails to resolve
     * @return the resolved plugin, or {@link Optional#empty()} if the search was empty or ambiguous
     * @see #searchInternalPlugin(CommandSource, String, boolean, Predicate)
     */
    public static Optional<VelocityPlugin> searchInternalPlugin(final CommandSource sender, final String input, final boolean inform) {
        return searchInternalPlugin(sender, input, inform, null);
    }

    /**
     * Resolves the first registered plugin assignable to the given class.
     *
     * @param clazz    the plugin class to match
     * @param <Plugin> the plugin type
     * @return the matching plugin instance, or {@code null} if none matches
     */
    public static <Plugin extends VelocityPlugin> Plugin getInstanceByClass(final Class<Plugin> clazz) {
        return UtilJava.cast(clazz, internalPluginMap.values().stream().filter(clazz::isInstance).findFirst().orElse(null));
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