package io.github.trae.velocity.framework.command.interfaces;

import com.velocitypowered.api.command.CommandSource;
import io.github.trae.utilities.UtilGeneric;
import io.github.trae.utilities.UtilJava;
import io.github.trae.velocity.framework.command.events.CommandExecuteEvent;
import io.github.trae.velocity.framework.command.events.CommandTabCompleteEvent;
import io.github.trae.velocity.framework.utility.UtilEvent;
import io.github.trae.velocity.framework.utility.UtilMessage;
import io.github.trae.velocity.framework.utility.UtilPermission;

import java.util.Collections;
import java.util.List;

/**
 * Shared contract for both {@link io.github.trae.velocity.framework.command.BaseCommand} and
 * {@link io.github.trae.velocity.framework.command.BaseSubCommand}, providing sender validation,
 * permission checks, event dispatching, and execution routing.
 *
 * @param <Sender> the expected {@link CommandSource} type for this command
 */
public interface SharedBaseCommand<Sender extends CommandSource> {

    /**
     * Resolves the generic {@link CommandSource} type parameter declared on the implementing class.
     * <p>
     * Uses {@link UtilGeneric#getGenericParameter} to reflectively extract the first type argument
     * of {@link SharedBaseCommand} from the concrete class. The result is used for sender validation
     * and casting in {@link #isValidSender} and {@link #$execute}.
     *
     * @return the resolved {@link Class} of {@code Sender}
     * @throws IllegalStateException if the generic parameter cannot be resolved
     */
    @SuppressWarnings("unchecked")
    default Class<Sender> getClassOfCommandSender() {
        final Class<?> commandSenderClass = UtilGeneric.getGenericParameter(this.getClass(), SharedBaseCommand.class, 0);
        if (commandSenderClass == null) {
            throw new IllegalStateException("Could not resolve command sender type for: %s".formatted(this.getClass().getName()));
        }

        return (Class<Sender>) commandSenderClass;
    }

    /**
     * Returns the primary label used to invoke this command.
     *
     * @return the command label
     */
    String getLabel();

    /**
     * Returns a short description of what this command does.
     *
     * @return the command description
     */
    String getDescription();

    /**
     * Returns the list of alternative labels that can also invoke this command.
     *
     * @return the command aliases
     */
    List<String> getAliases();

    /**
     * Returns the permission node required to execute this command, or {@code null} if no
     * permission is required.
     *
     * @return the permission node, or {@code null}
     */
    String getPermission();

    /**
     * Returns whether the given {@link CommandSource} is an instance of the expected sender type.
     * <p>
     * Delegates to {@link #getClassOfCommandSender()} for the type check.
     *
     * @param commandSource the source to validate
     * @return {@code true} if the source matches the expected type
     */
    default boolean isValidSender(final CommandSource commandSource) {
        return this.getClassOfCommandSender().isInstance(commandSource);
    }

    /**
     * Checks whether the specified {@link CommandSource} has permission to execute this command.
     *
     * <p>This check delegates to {@link UtilPermission#hasPermission} using the permission
     * returned by {@link #getPermission()}.</p>
     *
     * @param commandSource the command source to check
     * @return {@code true} if the command source is permitted to execute this command,
     * otherwise {@code false}
     */
    default boolean hasPermission(final CommandSource commandSource) {
        return UtilPermission.hasPermission(commandSource, this.getPermission());
    }

    /**
     * Executes the command logic for the given typed sender and arguments.
     * <p>
     * Only called after sender validation, permission checks, and event dispatch have all passed
     * in {@link #$execute}. Implementations should not repeat those checks here.
     *
     * @param sender the validated and cast sender
     * @param args   the remaining command arguments
     */
    void execute(final Sender sender, final String[] args);

    /**
     * Returns tab-complete suggestions for the given typed sender and arguments.
     * <p>
     * Only called after sender validation, permission checks, and event dispatch have all passed
     * in {@link #$getTabComplete}. Returns an empty list by default.
     *
     * @param sender the validated and cast sender
     * @param args   the current argument input
     * @return a list of suggestions, or an empty list if none
     */
    default List<String> getTabComplete(final Sender sender, final String[] args) {
        return Collections.emptyList();
    }

    /**
     * Internal execution entry point called from this command's Brigadier node callbacks.
     * <p>
     * Performs the following checks in order before delegating to {@link #execute}:
     * <ol>
     *   <li>Validates the sender type via {@link #isValidSender}</li>
     *   <li>Checks permission via {@link #hasPermission}</li>
     *   <li>Fires a cancellable {@link CommandExecuteEvent} via {@link UtilEvent#supply}</li>
     * </ol>
     * If any check fails or the event is cancelled, execution is aborted and {@code false} is returned.
     *
     * @param commandSource the raw source from Velocity
     * @param args          the command arguments
     * @return {@code true} if the command was executed successfully, {@code false} otherwise
     */
    default boolean $execute(final CommandSource commandSource, final String[] args) {
        if (!this.isValidSender(commandSource)) {
            UtilMessage.message(commandSource, "Command", "Invalid Command Sender!");
            return false;
        }

        if (!this.hasPermission(commandSource)) {
            UtilMessage.message(commandSource, "Permissions", "You do not have permission to execute this command!");
            return false;
        }

        if (UtilEvent.supply(new CommandExecuteEvent(this, commandSource)).isCancelled()) {
            return false;
        }

        this.execute(UtilJava.cast(this.getClassOfCommandSender(), commandSource), args);
        return true;
    }

    /**
     * Internal tab-complete entry point called from this command's Brigadier node callbacks.
     * <p>
     * Performs the following checks in order before delegating to {@link #getTabComplete}:
     * <ol>
     *   <li>Validates the sender type via {@link #isValidSender}</li>
     *   <li>Checks permission via {@link #hasPermission}</li>
     *   <li>Fires a cancellable {@link CommandTabCompleteEvent} via {@link UtilEvent#supply}</li>
     * </ol>
     * Returns an empty list if any check fails or the event is cancelled.
     *
     * @param commandSource the raw source from Velocity
     * @param args          the current argument input
     * @return a list of suggestions, or an empty list if blocked
     */
    default List<String> $getTabComplete(final CommandSource commandSource, final String[] args) {
        if (!this.isValidSender(commandSource)) {
            return Collections.emptyList();
        }

        if (!this.hasPermission(commandSource)) {
            return Collections.emptyList();
        }

        if (UtilEvent.supply(new CommandTabCompleteEvent(this, commandSource)).isCancelled()) {
            return Collections.emptyList();
        }

        return this.getTabComplete(UtilJava.cast(this.getClassOfCommandSender(), commandSource), args);
    }

    /**
     * Returns the usage string for this command.
     *
     * @return the usage string
     */
    String getUsage();
}