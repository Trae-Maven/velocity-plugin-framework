package io.github.trae.velocity.framework.command.interfaces;

import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandMeta;
import io.github.trae.velocity.framework.VelocityPlugin;
import io.github.trae.velocity.framework.command.BaseSubCommand;

import java.util.Optional;

/**
 * Contract for a command that owns a registry of subcommands and bridges itself into Velocity's
 * command manager.
 *
 * <p>The {@code $}-prefixed mutators are framework-internal and invoked during component lifecycle
 * to attach and detach subcommands from their parent. The {@code generate} methods produce the
 * Velocity registration artifacts: an eagerly built {@link CommandMeta} and a {@link BrigadierCommand}
 * whose node tree reflects the command's registered subcommands.</p>
 */
public interface IBaseCommand {

    /**
     * Registers the given subcommand against this command.
     *
     * @param baseSubCommand the subcommand to add
     */
    void $addSubCommand(final BaseSubCommand<?, ?, ?> baseSubCommand);

    /**
     * Unregisters the given subcommand from this command.
     *
     * @param baseSubCommand the subcommand to remove
     */
    void $removeSubCommand(final BaseSubCommand<?, ?, ?> baseSubCommand);

    /**
     * Resolves a registered subcommand by its label or one of its aliases.
     *
     * @param label the label or alias to look up
     * @return the matching subcommand, or an empty optional if none matches
     */
    Optional<BaseSubCommand<?, ?, ?>> getSubCommandByLabel(final String label);

    /**
     * Tests whether this command is a leaf, i.e. owns no subcommands.
     *
     * <p>A leaf command builds a single greedy root argument node that feeds all input to its own
     * {@code $execute}/{@code $getTabComplete}; a non-leaf command builds a literal node per
     * subcommand so the Brigadier dispatcher routes directly to them. The two layouts are mutually
     * exclusive to avoid a greedy-argument node colliding with sibling subcommand literals.</p>
     *
     * @return {@code true} if this command has no subcommands
     */
    boolean isLeafCommand();

    /**
     * Builds the {@link CommandMeta} used to register this command with Velocity's command manager.
     *
     * <p>The meta carries the command's primary label and aliases; alias routing to the underlying
     * {@link BrigadierCommand} is handled by Velocity at the command-manager level.</p>
     *
     * @param velocityPlugin the owning plugin, used to access the proxy's command manager
     * @return the command meta for registration
     */
    CommandMeta generateCommandMeta(final VelocityPlugin velocityPlugin);

    /**
     * Builds the {@link BrigadierCommand} node tree for this command.
     *
     * <p>For a {@link #isLeafCommand() leaf} command this is a single greedy argument node; otherwise
     * it is a literal node per registered subcommand (plus alias redirects), each carrying its own
     * greedy argument node for tab-completion and execution. Built fresh at registration time so the
     * tree reflects every subcommand that has self-registered by then.</p>
     *
     * @return the Brigadier command for registration
     */
    BrigadierCommand generateBrigadierCommand();
}