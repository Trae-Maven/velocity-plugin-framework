package io.github.trae.velocity.framework.command;

import com.velocitypowered.api.command.CommandSource;
import io.github.trae.hf.SubModule;
import io.github.trae.velocity.framework.VelocityPlugin;
import io.github.trae.velocity.framework.command.interfaces.SharedBaseCommand;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Abstract base class for subcommands belonging to a {@link BaseCommand}.
 *
 * <p>Combines {@link SubModule} and {@link SharedBaseCommand} to provide a typed subcommand with
 * its own sender validation, permission check, and tab-complete logic. On construction the subcommand
 * registers itself against its parent command via
 * {@link io.github.trae.velocity.framework.command.interfaces.IBaseCommand#$addSubCommand}.</p>
 *
 * @param <Plugin>        the plugin type this subcommand belongs to
 * @param <ParentCommand> the parent {@link BaseCommand} type
 * @param <Sender>        the expected {@link CommandSource} type
 */
@Getter
public abstract class BaseSubCommand<Plugin extends VelocityPlugin, ParentCommand extends BaseCommand<Plugin, ?, ?>, Sender extends CommandSource> implements SubModule<Plugin, ParentCommand>, SharedBaseCommand<Sender> {

    private final String label, description;
    private final List<String> aliases;
    private final String permission;

    /**
     * Display order for this subcommand in tab-completion suggestions; lower values appear first.
     * Defaults to {@link Integer#MAX_VALUE} so unset subcommands sort last. Set via
     * {@link #setIndex(int)} from the subclass constructor.
     */
    @Setter
    private int index = Integer.MAX_VALUE;

    /**
     * Constructs a subcommand with a permission node and registers it against its parent command.
     *
     * @param label       the primary label used to invoke this subcommand
     * @param description a short description of this subcommand
     * @param aliases     alternative labels for this subcommand
     * @param permission  the permission node required to use this subcommand, or {@code null} for none
     */
    public BaseSubCommand(final String label, final String description, final List<String> aliases, final String permission) {
        this.label = label;
        this.description = description;
        this.aliases = aliases;
        this.permission = permission;

        this.getModule().$addSubCommand(this);
    }

    /**
     * Constructs a subcommand without a permission node.
     *
     * <p>Equivalent to the all-arguments constructor with {@code null} as the permission, meaning
     * all senders of the correct type may execute it.</p>
     *
     * @param label       the primary label used to invoke this subcommand
     * @param description a short description of this subcommand
     * @param aliases     alternative labels for this subcommand
     * @see #BaseSubCommand(String, String, java.util.List, String)
     */
    public BaseSubCommand(final String label, final String description, final List<String> aliases) {
        this(label, description, aliases, null);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Returns {@code <parentUsage> <label>}, e.g. {@code /faction create}.
     */
    @Override
    public String getUsage() {
        return "%s %s".formatted(this.getModule().getUsage(), this.getLabel());
    }
}