package io.github.trae.velocity.framework.event.interfaces;

import com.velocitypowered.api.event.ResultedEvent;

/**
 * Adds Bukkit-style cancellation semantics on top of Velocity's {@link ResultedEvent}.
 *
 * <p>Velocity models event outcomes through a {@link ResultedEvent.GenericResult} that is either
 * allowed or denied. This interface adapts that model to the familiar boolean cancelled flag:
 * a denied result is treated as cancelled, and an allowed result as not cancelled.</p>
 */
public interface ICustomCancellableEvent extends ResultedEvent<ResultedEvent.GenericResult> {

    /**
     * Returns whether this event has been cancelled.
     *
     * @return {@code true} if the result is denied, {@code false} if it is allowed
     */
    default boolean isCancelled() {
        return !(this.getResult().isAllowed());
    }

    /**
     * Sets the cancelled state of this event.
     *
     * @param cancelled {@code true} to deny the event, {@code false} to allow it
     */
    default void setCancelled(final boolean cancelled) {
        this.setResult(cancelled ? GenericResult.denied() : GenericResult.allowed());
    }
}