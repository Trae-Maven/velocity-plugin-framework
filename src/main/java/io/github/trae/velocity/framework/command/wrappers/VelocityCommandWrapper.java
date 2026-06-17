package io.github.trae.velocity.framework.command.wrappers;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import io.github.trae.velocity.framework.command.BaseCommand;
import io.github.trae.velocity.framework.command.BaseSubCommand;
import lombok.AllArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Velocity-facing wrapper that bridges a {@link BaseCommand} into the proxy's command manager.
 *
 * <p>Constructed automatically by {@link BaseCommand} and registered during component
 * initialization. Delegates execution and tab-completion to the appropriate {@link BaseSubCommand}
 * if the first argument matches a registered subcommand label or alias, otherwise falls through to
 * the parent command.</p>
 */
@AllArgsConstructor
public class VelocityCommandWrapper implements SimpleCommand {

    private final BaseCommand<?, ?, ?> baseCommand;

    /**
     * Routes execution to a matching subcommand if {@code args[0]} resolves to one, stripping the
     * subcommand label from the argument array before delegating. Falls through to the parent
     * command if no subcommand matches.
     *
     * @param invocation the invocation context supplying the source and arguments
     */
    @Override
    public void execute(final Invocation invocation) {
        final CommandSource commandSource = invocation.source();
        final String[] args = invocation.arguments();

        if (args.length > 0) {
            final Optional<BaseSubCommand<?, ?, ?>> baseSubCommandOptional = this.baseCommand.getSubCommandByLabel(args[0]);
            if (baseSubCommandOptional.isPresent()) {
                baseSubCommandOptional.get().$execute(commandSource, Arrays.copyOfRange(args, 1, args.length));
                return;
            }
        }

        this.baseCommand.$execute(commandSource, args);
    }

    /**
     * Routes tab-completion to a matching subcommand if {@code args[0]} resolves to one, stripping
     * the subcommand label from the argument array before delegating. Falls through to the parent
     * command if no subcommand matches.
     *
     * @param invocation the invocation context supplying the source and arguments
     * @return the suggestion list
     */
    @Override
    public List<String> suggest(final Invocation invocation) {
        final CommandSource commandSource = invocation.source();
        final String[] args = invocation.arguments();

        if (args.length > 0) {
            final Optional<BaseSubCommand<?, ?, ?>> baseSubCommandOptional = this.baseCommand.getSubCommandByLabel(args[0]);
            if (baseSubCommandOptional.isPresent()) {
                return baseSubCommandOptional.get().$getTabComplete(commandSource, Arrays.copyOfRange(args, 1, args.length));
            }
        }

        return this.baseCommand.$getTabComplete(commandSource, args);
    }

    /**
     * Tests whether the source may use the wrapped command. Invoked by Velocity before
     * {@link #execute}; if it returns {@code false}, handling is forwarded to the backend server.
     *
     * @param invocation the invocation context supplying the source
     * @return {@code true} if the source holds the command's permission
     */
    @Override
    public boolean hasPermission(final Invocation invocation) {
        return this.baseCommand.hasPermission(invocation.source());
    }
}