package io.github.trae.velocity.framework.command.interfaces;

import io.github.trae.velocity.framework.command.BaseSubCommand;

import java.util.Optional;

/**
 * Contract for a command that owns a registry of subcommands.
 *
 * <p>The {@code $}-prefixed mutators are framework-internal and invoked during component
 * lifecycle to attach and detach subcommands from their parent.</p>
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
}