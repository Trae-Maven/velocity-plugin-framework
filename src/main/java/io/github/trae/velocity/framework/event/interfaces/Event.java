package io.github.trae.velocity.framework.event.interfaces;

import com.velocitypowered.api.event.annotation.AwaitingEvent;

/**
 * Marker interface for all framework events.
 *
 * <p>Implemented by every event dispatched through {@link io.github.trae.velocity.framework.utility.UtilEvent}.
 * Carries {@link AwaitingEvent}, so every framework event is treated as awaiting — the future
 * returned by {@code fire} completes only after all handlers, including asynchronous continuations,
 * have finished.</p>
 */
@AwaitingEvent
public interface Event {
}