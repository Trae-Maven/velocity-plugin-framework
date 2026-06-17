package io.github.trae.velocity.framework.event.interfaces;

/**
 * Marker interface for all framework events.
 *
 * <p>Implemented by every event dispatched through {@link io.github.trae.velocity.framework.utility.UtilEvent},
 * allowing the dispatch methods to constrain their type parameters to framework events. Velocity's
 * event bus accepts any object, so this interface carries no behavior — it exists purely to identify
 * an event as belonging to this framework.</p>
 */
public interface Event {
}