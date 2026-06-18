package io.github.trae.velocity.framework.utility;

import io.github.trae.velocity.framework.VelocityPlugin;
import io.github.trae.velocity.framework.event.interfaces.Event;
import lombok.experimental.UtilityClass;

import java.util.concurrent.CompletableFuture;

/**
 * Utility class for dispatching framework {@link Event}s through Velocity's event bus.
 *
 * <p>Provides fire-and-forget dispatch via {@link #dispatchAsynchronous} and future-returning
 * dispatch via {@link #supplyAsynchronous}, whose future completes after all handlers finish. Each
 * has an overload that resolves the owning plugin from {@link UtilPlugin#getInstance()} when not
 * supplied explicitly.</p>
 */
@UtilityClass
public class UtilEvent {

    /**
     * Fires the given event to the event bus and returns immediately without waiting for handlers.
     *
     * @param velocityPlugin the plugin associated with the dispatch
     * @param event          the event to fire
     * @param <T>            the event type
     * @throws IllegalArgumentException if {@code velocityPlugin} or {@code event} is {@code null}
     */
    public static <T extends Event> void dispatchAsynchronous(final VelocityPlugin velocityPlugin, final T event) {
        if (velocityPlugin == null) {
            throw new IllegalArgumentException("Velocity Plugin cannot be null.");
        }

        if (event == null) {
            throw new IllegalArgumentException("Event cannot be null.");
        }

        velocityPlugin.getProxyServer().getEventManager().fireAndForget(event);
    }

    /**
     * Fires the given event to the event bus using the default plugin instance.
     *
     * @param event the event to fire
     * @param <T>   the event type
     * @see #dispatchAsynchronous(VelocityPlugin, Event)
     */
    public static <T extends Event> void dispatchAsynchronous(final T event) {
        dispatchAsynchronous(UtilPlugin.getInstance(), event);
    }

    /**
     * Fires the given event to the event bus, returning a future that completes once all handlers
     * have finished.
     *
     * <p>Because every {@link Event} is {@code @AwaitingEvent}, the returned future completes only
     * after all handlers — including their asynchronous continuations — have run. Useful for
     * cancellable events where the caller inspects the result afterwards via the completed future.</p>
     *
     * @param velocityPlugin the plugin associated with the dispatch
     * @param event          the event to fire
     * @param <R>            the event type
     * @return a future completing with the same event instance after all handlers have been invoked
     * @throws IllegalArgumentException if {@code velocityPlugin} or {@code event} is {@code null}
     */
    public static <R extends Event> CompletableFuture<R> supplyAsynchronous(final VelocityPlugin velocityPlugin, final R event) {
        if (velocityPlugin == null) {
            throw new IllegalArgumentException("Velocity Plugin cannot be null.");
        }

        if (event == null) {
            throw new IllegalArgumentException("Event cannot be null.");
        }

        return velocityPlugin.getProxyServer().getEventManager().fire(event);
    }

    /**
     * Fires the given event to the event bus using the default plugin instance.
     *
     * @param event the event to fire
     * @param <R>   the event type
     * @return a future completing with the same event instance after all handlers have been invoked
     * @see #supplyAsynchronous(VelocityPlugin, Event)
     */
    public static <R extends Event> CompletableFuture<R> supplyAsynchronous(final R event) {
        return supplyAsynchronous(UtilPlugin.getInstance(), event);
    }
}