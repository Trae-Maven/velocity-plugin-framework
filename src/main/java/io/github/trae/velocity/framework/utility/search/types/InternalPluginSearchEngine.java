package io.github.trae.velocity.framework.utility.search.types;

import io.github.trae.velocity.framework.VelocityPlugin;
import io.github.trae.velocity.framework.utility.UtilColor;
import io.github.trae.velocity.framework.utility.UtilPlugin;
import io.github.trae.velocity.framework.utility.enums.ChatColor;
import io.github.trae.velocity.framework.utility.search.VelocitySearchEngine;

import java.util.Locale;

/**
 * Search engine resolving framework plugins registered with {@link UtilPlugin}.
 *
 * <p>Candidates are read live from the internal plugin registry on every search, and matched on
 * plugin name: case-insensitive equality for an exact hit, case-insensitive substring for a partial
 * one.</p>
 */
public class InternalPluginSearchEngine extends VelocitySearchEngine<VelocityPlugin> {

    /**
     * Creates a search engine over the internal plugin registry.
     */
    public InternalPluginSearchEngine() {
        super("Internal Plugin Search", UtilPlugin::getInternalPlugins);
    }

    /**
     * {@inheritDoc}
     *
     * @return the plugin name serialized in yellow
     */
    @Override
    protected String getTypeFormat(final VelocityPlugin velocityPlugin) {
        return UtilColor.serialize(ChatColor.YELLOW.getColor(), velocityPlugin.getPluginName());
    }

    /**
     * {@inheritDoc}
     *
     * <p>Compares the plugin name to the input, ignoring case.
     */
    @Override
    protected boolean isExact(final VelocityPlugin velocityPlugin, final String result) {
        return velocityPlugin.getPluginName().equalsIgnoreCase(result);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Tests whether the plugin name contains the input, ignoring case.
     */
    @Override
    protected boolean isMatching(final VelocityPlugin velocityPlugin, final String result) {
        return velocityPlugin.getPluginName().toLowerCase(Locale.ROOT).contains(result.toLowerCase(Locale.ROOT));
    }
}