package io.github.trae.velocity.framework.command.interfaces;

import com.velocitypowered.api.command.CommandSource;
import io.github.trae.utilities.UtilGeneric;
import io.github.trae.utilities.UtilJava;
import io.github.trae.velocity.framework.command.events.CommandExecuteEvent;
import io.github.trae.velocity.framework.command.events.CommandTabCompleteEvent;
import io.github.trae.velocity.framework.utility.UtilEvent;
import io.github.trae.velocity.framework.utility.UtilMessage;

import java.util.Collections;
import java.util.List;

/**
 * Shared contract for commands and subcommands, providing sender validation, permission
 * checking, event dispatch, and the gated execution and tab-complete entry points.
 *
 * <p>The {@code $}-prefixed methods ({@link #$execute} and {@link #$getTabComplete}) are the
 * framework-internal entry points invoked from the command's Brigadier node callbacks. They perform
 * sender, permission, and event-cancellation gating before delegating to the user-facing
 * {@link #execute} and {@link #getTabComplete} methods.</p>
 *
 * @param <Sender> the expected {@link CommandSource} type for this command
 */
public interface SharedBaseCommand<Sender extends CommandSource> {

    /**
     * Resolves the runtime {@link Class} of this command's {@code Sender} type parameter via
     * reflection over the generic hierarchy.
     *
     * @return the class of the expected command sender type
     * @throws IllegalStateException if the sender type could not be resolved
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
     * Returns a short description of this command.
     *
     * @return the command description
     */
    String getDescription();

    /**
     * Returns the alternative labels that can be used to invoke this command.
     *
     * @return the command aliases
     */
    List<String> getAliases();

    /**
     * Returns the permission node required to use this command, or {@code null} if none.
     *
     * @return the permission node, or {@code null} if unrestricted
     */
    String getPermission();

    /**
     * Tests whether the given source is of the expected {@link #getClassOfCommandSender() sender type}.
     *
     * @param commandSource the source to test
     * @return {@code true} if the source is a valid sender type for this command
     */
    default boolean isValidSender(final CommandSource commandSource) {
        return this.getClassOfCommandSender().isInstance(commandSource);
    }

    /**
     * Tests whether the given source holds the {@link #getPermission() required permission}.
     *
     * @param commandSource the source to test
     * @return {@code true} if no permission is required, or the source holds it
     */
    default boolean hasPermission(final CommandSource commandSource) {
        return this.getPermission() == null || commandSource.hasPermission(this.getPermission());
    }

    /**
     * Executes this command. Invoked by {@link #$execute} after all gating checks pass.
     *
     * @param sender the validated sender, cast to the expected type
     * @param args   the command arguments
     */
    void execute(final Sender sender, final String[] args);

    /**
     * Provides tab-complete suggestions for this command. Invoked by {@link #$getTabComplete}
     * after all gating checks pass. Defaults to an empty list.
     *
     * @param sender the validated sender, cast to the expected type
     * @param args   the current argument input
     * @return the suggestion list
     */
    default List<String> getTabComplete(final Sender sender, final String[] args) {
        return Collections.emptyList();
    }

    /**
     * Framework-internal execution entry point. Validates the sender type and permission, fires a
     * cancellable {@link CommandExecuteEvent}, and delegates to {@link #execute} if all checks pass.
     *
     * @param commandSource the source attempting execution
     * @param args          the command arguments
     * @return {@code true} if the command was executed, {@code false} if any gating check failed
     */
    default boolean $execute(final CommandSource commandSource, final String[] args) {
        if (!(this.isValidSender(commandSource))) {
            UtilMessage.message(commandSource, "Command", "Invalid Command Sender!");
            return false;
        }

        if (!(this.hasPermission(commandSource))) {
            UtilMessage.message(commandSource, "Permissions", "You do not have permission to execute this command!");
            return false;
        }

        if (UtilEvent.supplyAsynchronous(new CommandExecuteEvent(this, commandSource)).isCancelled()) {
            return false;
        }

        this.execute(UtilJava.cast(this.getClassOfCommandSender(), commandSource), args);

        return true;
    }

    /**
     * Framework-internal tab-complete entry point. Validates the sender type and permission, fires a
     * cancellable {@link CommandTabCompleteEvent}, and delegates to {@link #getTabComplete} if all
     * checks pass.
     *
     * @param commandSource the source requesting suggestions
     * @param args          the current argument input
     * @return the suggestion list, or an empty list if any gating check failed
     */
    default List<String> $getTabComplete(final CommandSource commandSource, final String[] args) {
        if (!(this.isValidSender(commandSource))) {
            return Collections.emptyList();
        }

        if (!(this.hasPermission(commandSource))) {
            return Collections.emptyList();
        }

        if (UtilEvent.supplyAsynchronous(new CommandTabCompleteEvent(this, commandSource)).isCancelled()) {
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