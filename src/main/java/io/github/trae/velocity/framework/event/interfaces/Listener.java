package io.github.trae.velocity.framework.event.interfaces;

/**
 * Marker interface for framework event listeners.
 *
 * <p>Components implementing this interface are automatically registered with the proxy's
 * {@code EventManager} during {@link io.github.trae.velocity.framework.VelocityPlugin#onComponentInitialize(Object)}
 * and unregistered on shutdown. Velocity discovers the actual handlers by reflectively scanning the
 * registered object for {@link com.velocitypowered.api.event.Subscribe @Subscribe} methods, so this
 * interface carries no methods — it exists purely to opt a component into automatic registration.</p>
 */
public interface Listener {
}