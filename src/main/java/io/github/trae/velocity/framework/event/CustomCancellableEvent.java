package io.github.trae.velocity.framework.event;

import io.github.trae.velocity.framework.event.interfaces.Event;
import io.github.trae.velocity.framework.event.interfaces.ICustomCancellableEvent;
import lombok.Getter;

import java.util.Objects;

/**
 * Base class for cancellable framework events.
 *
 * <p>Combines the {@link Event} marker with {@link ICustomCancellableEvent} to provide the
 * allow/deny result backing the cancellation flag. The result defaults to
 * {@link com.velocitypowered.api.event.ResultedEvent.GenericResult#allowed() allowed} and can be
 * toggled via {@link ICustomCancellableEvent#setCancelled(boolean)} or set directly with
 * {@link #setResult}.</p>
 */
@Getter
public class CustomCancellableEvent implements Event, ICustomCancellableEvent {

    private GenericResult result = GenericResult.allowed();

    /**
     * Sets the result of this event.
     *
     * @param result the new result; must not be {@code null}
     * @throws NullPointerException if {@code result} is {@code null}
     */
    @Override
    public void setResult(final GenericResult result) {
        this.result = Objects.requireNonNull(result, "Result cannot be null.");
    }
}