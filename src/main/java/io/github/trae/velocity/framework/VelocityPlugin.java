package io.github.trae.velocity.framework;

import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.proxy.ProxyServer;
import io.github.trae.di.InjectorApi;
import io.github.trae.hf.Plugin;
import io.github.trae.velocity.framework.command.BaseCommand;
import io.github.trae.velocity.framework.command.BaseSubCommand;
import io.github.trae.velocity.framework.event.interfaces.Listener;
import io.github.trae.velocity.framework.plugin.events.PluginInitializeEvent;
import io.github.trae.velocity.framework.plugin.events.PluginShutdownEvent;
import io.github.trae.velocity.framework.utility.UtilEvent;
import io.github.trae.velocity.framework.utility.UtilPlugin;
import io.github.trae.velocity.framework.utility.UtilTask;
import lombok.Getter;

import java.nio.file.Path;

/**
 * Base class for all Velocity plugins using the framework.
 *
 * <p>Implements {@link Plugin} from the hierarchy framework, bridging the Velocity proxy
 * lifecycle with the component-based architecture. Automatically handles registration and
 * teardown of listeners, commands, and subcommands as components are initialized and shut
 * down through the hierarchy.</p>
 *
 * <p>Concrete plugins extend this class and supply a constructor annotated with
 * {@code @Inject}, forwarding the injected {@link ProxyServer} and {@code @DataDirectory}
 * {@link Path} to {@code super}. The concrete plugin is responsible for invoking
 * {@link #initializePlugin()} and {@link #shutdownPlugin()} in response to the proxy's
 * initialize and shutdown events.</p>
 */
@Getter
public class VelocityPlugin implements Plugin {

    private final ProxyServer proxyServer;
    private final Path dataDirectory;

    /**
     * Creates a new {@link VelocityPlugin} and configures the dependency injection framework
     * for this application.
     *
     * <p>Registers this plugin's data directory as the configuration directory for
     * {@link io.github.trae.di.configuration.annotations.Configuration @Configuration} file
     * resolution, and sets up the per-application executors used to dispatch
     * {@link io.github.trae.di.annotations.method.Scheduler @Scheduler} tasks via {@link UtilTask}.</p>
     *
     * @param proxyServer   the proxy server instance, injected by the concrete plugin
     * @param dataDirectory the plugin's data directory, injected by the concrete plugin
     */
    public VelocityPlugin(final ProxyServer proxyServer, final Path dataDirectory) {
        this.proxyServer = proxyServer;
        this.dataDirectory = dataDirectory;

        InjectorApi.setConfigurationDirectory(this.getClass(), dataDirectory);

        InjectorApi.setSynchronousExecutor(this.getClass(), UtilTask::execute);
        InjectorApi.setAsynchronousExecutor(this.getClass(), UtilTask::executeAsynchronous);
    }

    /**
     * Initializes the plugin by running the hierarchy lifecycle via
     * {@link Plugin#initializePlugin()}, dispatching a {@link PluginInitializeEvent} to notify
     * listeners that the plugin is fully initialized, then registering it as an internal plugin.
     */
    @Override
    public void initializePlugin() {
        Plugin.super.initializePlugin();

        UtilEvent.dispatchAsynchronous(this, new PluginInitializeEvent(this));

        UtilPlugin.addInternalPlugin(this);
    }

    /**
     * Dispatches a {@link PluginShutdownEvent} to notify listeners that the plugin is about to
     * shut down, runs the hierarchy teardown via {@link Plugin#shutdownPlugin()}, then deregisters
     * it as an internal plugin.
     */
    @Override
    public void shutdownPlugin() {
        UtilEvent.dispatchAsynchronous(this, new PluginShutdownEvent(this));

        Plugin.super.shutdownPlugin();

        UtilPlugin.removeInternalPlugin(this);
    }

    /**
     * Called when a component is initialized within the hierarchy.
     *
     * <p>Delegates to {@link Plugin#onComponentInitialize(Object)} to invoke
     * {@link io.github.trae.hf.Frame#initializeFrame()}, then performs automatic Velocity
     * registration based on the component type:</p>
     * <ul>
     *     <li>{@link Listener} — registered with the proxy's {@code EventManager}</li>
     *     <li>{@link BaseCommand} — registered with the proxy's {@code CommandManager} under a
     *         {@link CommandMeta} built from its label and aliases</li>
     *     <li>{@link BaseSubCommand} — attached to its parent command's subcommand map</li>
     * </ul>
     *
     * @param instance the component being initialized
     */
    @Override
    public void onComponentInitialize(final Object instance) {
        Plugin.super.onComponentInitialize(instance);

        if (instance instanceof final Listener listener) {
            this.proxyServer.getEventManager().register(this, listener);
        }

        if (instance instanceof final BaseCommand<?, ?, ?> baseCommand) {
            final CommandManager commandManager = this.proxyServer.getCommandManager();

            final CommandMeta commandMeta = commandManager.metaBuilder(baseCommand.getLabel()).aliases(baseCommand.getAliases().toArray(new String[0])).plugin(this).build();

            commandManager.register(commandMeta, baseCommand.getVelocityCommandWrapper());
        }

        if (instance instanceof final BaseSubCommand<?, ?, ?> baseSubCommand) {
            baseSubCommand.getModule().$addSubCommand(baseSubCommand);
        }
    }

    /**
     * Called when a component is shut down within the hierarchy.
     *
     * <p>Performs automatic Velocity deregistration based on the component type, then delegates
     * to {@link Plugin#onComponentShutdown(Object)} to invoke
     * {@link io.github.trae.hf.Frame#shutdownFrame()}:</p>
     * <ul>
     *     <li>{@link Listener} — unregistered from the proxy's {@code EventManager}</li>
     *     <li>{@link BaseCommand} — unregistered from the proxy's {@code CommandManager}</li>
     *     <li>{@link BaseSubCommand} — removed from its parent command's subcommand map</li>
     * </ul>
     *
     * @param instance the component being shut down
     */
    @Override
    public void onComponentShutdown(final Object instance) {
        if (instance instanceof final Listener listener) {
            this.proxyServer.getEventManager().unregisterListener(this, listener);
        }

        if (instance instanceof final BaseCommand<?, ?, ?> baseCommand) {
            this.proxyServer.getCommandManager().unregister(baseCommand.getLabel());
        }

        if (instance instanceof final BaseSubCommand<?, ?, ?> baseSubCommand) {
            baseSubCommand.getModule().$removeSubCommand(baseSubCommand);
        }

        Plugin.super.onComponentShutdown(instance);
    }
}