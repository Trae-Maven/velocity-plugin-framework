package io.github.trae.velocity.framework.utility;

import com.velocitypowered.api.scheduler.ScheduledTask;
import io.github.trae.velocity.framework.VelocityPlugin;
import lombok.experimental.UtilityClass;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.function.Supplier;

/**
 * Utility class for executing tasks on Velocity's scheduler.
 *
 * <p>Provides immediate, delayed, and repeating execution. Velocity's scheduler runs all
 * tasks on a cached thread pool, so there is no main-thread/synchronous distinction; the
 * {@link #execute(Runnable)} method runs inline on the calling thread, while all other
 * methods schedule onto the proxy's thread pool. Convenience overloads delegate to
 * {@link UtilPlugin#getInstance()} for the plugin reference.</p>
 */
@UtilityClass
public class UtilTask {

    /**
     * Executes a {@link Runnable} immediately on the calling thread.
     *
     * @param runnable the task to execute
     * @throws IllegalArgumentException if {@code runnable} is {@code null}
     */
    public static void execute(final Runnable runnable) {
        if (runnable == null) {
            throw new IllegalArgumentException("Runnable cannot be null.");
        }

        runnable.run();
    }

    /**
     * Schedules a {@link Runnable} to run on the proxy's thread pool.
     *
     * @param velocityPlugin the plugin owning the task
     * @param runnable       the task to execute
     * @throws IllegalArgumentException if {@code velocityPlugin} or {@code runnable} is {@code null}
     */
    public static void executeAsynchronous(final VelocityPlugin velocityPlugin, final Runnable runnable) {
        if (velocityPlugin == null) {
            throw new IllegalArgumentException("Velocity Plugin cannot be null.");
        }

        if (runnable == null) {
            throw new IllegalArgumentException("Runnable cannot be null.");
        }

        velocityPlugin.getProxyServer().getScheduler().buildTask(velocityPlugin, runnable).schedule();
    }

    /**
     * Schedules a {@link Runnable} to run on the proxy's thread pool using the default plugin instance.
     *
     * @param runnable the task to execute
     * @see #executeAsynchronous(VelocityPlugin, Runnable)
     */
    public static void executeAsynchronous(final Runnable runnable) {
        executeAsynchronous(UtilPlugin.getInstance(), runnable);
    }

    /**
     * Schedules a {@link Runnable} to run once after the specified delay.
     *
     * @param velocityPlugin the plugin owning the task
     * @param runnable       the task to execute
     * @param delay          the amount of time to delay execution
     * @param chronoUnit     the time unit of the {@code delay} parameter
     * @throws IllegalArgumentException if {@code velocityPlugin} or {@code runnable} is {@code null}
     */
    public static void executeLaterAsynchronous(final VelocityPlugin velocityPlugin, final Runnable runnable, final int delay, final ChronoUnit chronoUnit) {
        if (velocityPlugin == null) {
            throw new IllegalArgumentException("Velocity Plugin cannot be null.");
        }

        if (runnable == null) {
            throw new IllegalArgumentException("Runnable cannot be null.");
        }

        final Duration delayDuration = Duration.of(delay, chronoUnit);

        velocityPlugin.getProxyServer().getScheduler().buildTask(velocityPlugin, runnable).delay(delayDuration).schedule();
    }

    /**
     * Schedules a {@link Runnable} to run once after the specified delay, using the default plugin instance.
     *
     * @param runnable   the task to execute
     * @param delay      the amount of time to delay execution
     * @param chronoUnit the time unit of the {@code delay} parameter
     * @see #executeLaterAsynchronous(VelocityPlugin, Runnable, int, ChronoUnit)
     */
    public static void executeLaterAsynchronous(final Runnable runnable, final int delay, final ChronoUnit chronoUnit) {
        executeLaterAsynchronous(UtilPlugin.getInstance(), runnable, delay, chronoUnit);
    }

    /**
     * Schedules a {@link Runnable} to run repeatedly at a fixed interval, with an optional
     * cancellation supplier.
     *
     * <p>If a {@code cancelSupplier} is provided, it is checked before each invocation — if it
     * returns {@code true}, the task is cancelled and the runnable will not execute.</p>
     *
     * @param velocityPlugin the plugin owning the task
     * @param runnable       the task to execute on each tick
     * @param initialDelay   the amount of time to delay first execution
     * @param period         the interval between successive executions
     * @param chronoUnit     the time unit of the {@code initialDelay} and {@code period} parameters
     * @param cancelSupplier a supplier that returns {@code true} to cancel the task (may be {@code null})
     * @throws IllegalArgumentException if {@code velocityPlugin}, {@code runnable}, or {@code chronoUnit} is
     *                                  {@code null}, or if {@code initialDelay} or {@code period} is negative
     */
    public static void scheduleAsynchronous(final VelocityPlugin velocityPlugin, final Runnable runnable, final int initialDelay, final int period, final ChronoUnit chronoUnit, final Supplier<Boolean> cancelSupplier) {
        if (velocityPlugin == null) {
            throw new IllegalArgumentException("Velocity Plugin cannot be null.");
        }

        if (runnable == null) {
            throw new IllegalArgumentException("Runnable cannot be null.");
        }

        if (initialDelay < 0 || period < 0) {
            throw new IllegalArgumentException("Initial delay and Period must be >= 0.");
        }

        if (chronoUnit == null) {
            throw new IllegalArgumentException("Chrono Unit cannot be null.");
        }

        final Duration initialDelayDuration = Duration.of(initialDelay, chronoUnit);
        final Duration periodDuration = Duration.of(period, chronoUnit);

        velocityPlugin.getProxyServer().getScheduler().buildTask(velocityPlugin, (final ScheduledTask scheduledTask) -> {
            if (cancelSupplier != null && cancelSupplier.get()) {
                scheduledTask.cancel();
                return;
            }

            runnable.run();
        }).delay(initialDelayDuration).repeat(periodDuration).schedule();
    }

    /**
     * Schedules a {@link Runnable} to run repeatedly at a fixed interval.
     *
     * @param velocityPlugin the plugin owning the task
     * @param runnable       the task to execute on each tick
     * @param initialDelay   the amount of time to delay first execution
     * @param period         the interval between successive executions
     * @param chronoUnit     the time unit of the {@code initialDelay} and {@code period} parameters
     * @see #scheduleAsynchronous(VelocityPlugin, Runnable, int, int, ChronoUnit, Supplier)
     */
    public static void scheduleAsynchronous(final VelocityPlugin velocityPlugin, final Runnable runnable, final int initialDelay, final int period, final ChronoUnit chronoUnit) {
        scheduleAsynchronous(velocityPlugin, runnable, initialDelay, period, chronoUnit, null);
    }

    /**
     * Schedules a {@link Runnable} to run repeatedly at a fixed interval using the default plugin
     * instance, with an optional cancellation supplier.
     *
     * @param runnable       the task to execute on each tick
     * @param initialDelay   the amount of time to delay first execution
     * @param period         the interval between successive executions
     * @param chronoUnit     the time unit of the {@code initialDelay} and {@code period} parameters
     * @param cancelSupplier a supplier that returns {@code true} to cancel the task (may be {@code null})
     * @see #scheduleAsynchronous(VelocityPlugin, Runnable, int, int, ChronoUnit, Supplier)
     */
    public static void scheduleAsynchronous(final Runnable runnable, final int initialDelay, final int period, final ChronoUnit chronoUnit, final Supplier<Boolean> cancelSupplier) {
        scheduleAsynchronous(UtilPlugin.getInstance(), runnable, initialDelay, period, chronoUnit, cancelSupplier);
    }

    /**
     * Schedules a {@link Runnable} to run repeatedly at a fixed interval using the default plugin instance.
     *
     * @param runnable     the task to execute on each tick
     * @param initialDelay the amount of time to delay first execution
     * @param period       the interval between successive executions
     * @param chronoUnit   the time unit of the {@code initialDelay} and {@code period} parameters
     * @see #scheduleAsynchronous(VelocityPlugin, Runnable, int, int, ChronoUnit, Supplier)
     */
    public static void scheduleAsynchronous(final Runnable runnable, final int initialDelay, final int period, final ChronoUnit chronoUnit) {
        scheduleAsynchronous(UtilPlugin.getInstance(), runnable, initialDelay, period, chronoUnit, null);
    }
}