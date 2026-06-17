package io.github.trae.velocity.framework.command;

import com.velocitypowered.api.command.CommandSource;
import io.github.trae.hf.Manager;
import io.github.trae.hf.Module;
import io.github.trae.velocity.framework.VelocityPlugin;
import io.github.trae.velocity.framework.command.interfaces.IBaseCommand;
import io.github.trae.velocity.framework.command.interfaces.SharedBaseCommand;
import io.github.trae.velocity.framework.command.wrappers.VelocityCommandWrapper;
import lombok.Getter;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Abstract base class for top-level commands in the framework.
 *
 * <p>Combines {@link Module} from the hierarchy framework with {@link SharedBaseCommand} and
 * {@link IBaseCommand} to provide a typed command that owns a registry of {@link BaseSubCommand}s.
 * On construction it builds a {@link VelocityCommandWrapper} that bridges it into Velocity's
 * command manager; the wrapper is registered during component initialization.</p>
 *
 * @param <Plugin>          the plugin type this command belongs to
 * @param <VelocityManager> the owning {@link Manager} type
 * @param <Sender>          the expected {@link CommandSource} type
 */
@Getter
public abstract class BaseCommand<Plugin extends VelocityPlugin, VelocityManager extends Manager<Plugin>, Sender extends CommandSource> implements Module<Plugin, VelocityManager>, SharedBaseCommand<Sender>, IBaseCommand {

    private final String label, description;
    private final List<String> aliases;
    private final String permission;

    private final LinkedHashMap<String, BaseSubCommand<?, ?, ?>> subCommands = new LinkedHashMap<>();

    private final VelocityCommandWrapper velocityCommandWrapper;

    /**
     * Constructs a command with a permission node.
     *
     * @param label       the primary label used to invoke this command
     * @param description a short description of this command
     * @param aliases     alternative labels for this command
     * @param permission  the permission node required to use this command, or {@code null} for none
     */
    public BaseCommand(final String label, final String description, final List<String> aliases, final String permission) {
        this.label = label;
        this.description = description;
        this.permission = permission;
        this.aliases = aliases;

        this.velocityCommandWrapper = new VelocityCommandWrapper(this);
    }

    /**
     * Constructs a command without a permission node.
     *
     * @param label       the primary label used to invoke this command
     * @param description a short description of this command
     * @param aliases     alternative labels for this command
     * @see #BaseCommand(String, String, List, String)
     */
    public BaseCommand(final String label, final String description, final List<String> aliases) {
        this(label, description, aliases, null);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Returns {@code /<label>}, e.g. {@code /faction}.
     */
    @Override
    public String getUsage() {
        return "/%s".formatted(this.getLabel());
    }

    /**
     * {@inheritDoc}
     * <p>
     * Stores the subcommand keyed by its lowercased label.
     */
    @Override
    public void $addSubCommand(final BaseSubCommand<?, ?, ?> baseSubCommand) {
        this.subCommands.put(baseSubCommand.getLabel().toLowerCase(Locale.ROOT), baseSubCommand);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Removes the subcommand keyed by its lowercased label.
     */
    @Override
    public void $removeSubCommand(final BaseSubCommand<?, ?, ?> baseSubCommand) {
        this.subCommands.remove(baseSubCommand.getLabel().toLowerCase(Locale.ROOT));
    }

    /**
     * {@inheritDoc}
     * <p>
     * Resolves first by exact lowercased label, then falls back to scanning subcommand aliases.
     */
    @Override
    public Optional<BaseSubCommand<?, ?, ?>> getSubCommandByLabel(final String label) {
        final String lowerLabel = label.toLowerCase(Locale.ROOT);

        final BaseSubCommand<?, ?, ?> baseSubCommand = this.subCommands.get(lowerLabel);
        if (baseSubCommand != null) {
            return Optional.of(baseSubCommand);
        }

        return this.subCommands.values().stream().filter(value -> value.getAliases().contains(lowerLabel)).findFirst();
    }
}