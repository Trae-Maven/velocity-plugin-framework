package io.github.trae.velocity.framework.command.events;

import com.velocitypowered.api.command.CommandSource;
import io.github.trae.velocity.framework.command.interfaces.SharedBaseCommand;
import io.github.trae.velocity.framework.event.CustomCancellableEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Fired before tab-complete suggestions are returned via {@link SharedBaseCommand#$getTabComplete}.
 * <p>
 * Cancelling this event causes an empty suggestion list to be returned. Can be listened to
 * by any plugin to intercept or suppress tab-completion globally.
 */
@AllArgsConstructor
@Getter
public class CommandTabCompleteEvent extends CustomCancellableEvent {

    /**
     * The command whose tab-completion is being requested.
     */
    private final SharedBaseCommand<?> command;

    /**
     * The sender requesting tab-complete suggestions.
     */
    private final CommandSource sender;
}