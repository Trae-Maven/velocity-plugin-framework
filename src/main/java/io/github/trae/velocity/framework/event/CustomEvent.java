package io.github.trae.velocity.framework.event;

import io.github.trae.velocity.framework.event.interfaces.Event;

/**
 * Base class for non-cancellable framework events.
 *
 * <p>Extend this for events that merely notify listeners and cannot be denied. For events that
 * support cancellation, extend {@link CustomCancellableEvent} instead.</p>
 */
public class CustomEvent implements Event {
}