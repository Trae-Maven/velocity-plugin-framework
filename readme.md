# Velocity-Plugin-Framework

A Velocity proxy plugin framework providing structured command systems, event utilities, and lifecycle integration built on the [Hierarchy-Framework](https://github.com/Trae-Maven/hierarchy-framework).

Velocity-Plugin-Framework bridges the Velocity proxy lifecycle with the component-based hierarchy architecture, automatically handling registration and teardown of listeners, commands, and subcommands as components are initialized and shut down.

---

## Features

- Automatic Velocity registration — listeners, commands, and subcommands are registered/unregistered through hierarchy lifecycle callbacks
- Type-safe command system with sender validation — `Player`, console, or any `CommandSource`
- Built-in subcommand routing with automatic argument stripping and tab completion delegation
- Cancellable command events at every execution stage — execute and tab-complete
- Event dispatch utilities — fire-and-forget and blocking, result-returning dispatch with `CompletableFuture` resolution
- Task scheduling with `ChronoUnit`-to-`Duration` conversion — immediate, scheduled, and repeating with cancellation suppliers
- MiniMessage-based messaging — configurable prefixes, broadcasting, filtering, and ignore lists
- Adventure-native — built directly on Velocity's `Audience`/`Component` model
- Custom event base classes with allow/deny cancellation semantics on top of Velocity's `ResultedEvent`
- `EventPriority` constants mapped to Velocity's descending `PostOrder` model
- Designed for modern Java (Java 21+)

---

## Hierarchy
```
VelocityPlugin (implements Plugin)
  └─ Manager
       └─ BaseCommand / Module
            └─ BaseSubCommand / SubModule
```

Commands integrate directly into the hierarchy as Modules, and subcommands as SubModules:

| Component | Hierarchy Role | Velocity Integration |
|---|---|---|
| `VelocityPlugin` | Plugin | Proxy lifecycle bridge, component registration |
| `Manager` | Manager | Organizational grouping |
| `BaseCommand` | Module | Registered with `CommandManager` |
| `BaseSubCommand` | SubModule | Attached to parent command |

---

## Requirements

Velocity-Plugin-Framework requires Java 21+ and the Velocity API.

The following is only needed at compile time for annotation processing:
```xml
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <version>1.18.46</version>
    <scope>provided</scope>
</dependency>
```

---

## Built-in Dependencies

Velocity-Plugin-Framework depends on the following libraries, which are included automatically through Maven:

- [Hierarchy-Framework](https://github.com/Trae-Maven/hierarchy-framework) – Plugin, Manager, Module, SubModule hierarchy with lifecycle management.
- [Dependency Injector](https://github.com/Trae-Maven/dependency-injector) – Container management, classpath scanning, and component wiring.
- [Utilities](https://github.com/Trae-Maven/utilities) – Generic type resolution, string utilities, and casting helpers.

---

## Installation

Add the dependency to your Maven project:
```xml
<dependencies>
    <dependency>
        <groupId>io.github.trae</groupId>
        <artifactId>velocity-plugin-framework</artifactId>
        <version>0.0.1</version>
    </dependency>

    <dependency>
        <groupId>com.velocitypowered</groupId>
        <artifactId>velocity-api</artifactId>
        <version>3.5.0-SNAPSHOT</version>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

The Velocity API is resolved from the PaperMC repository:
```xml
<repositories>
    <repository>
        <id>papermc</id>
        <url>https://repo.papermc.io/repository/maven-public/</url>
    </repository>
</repositories>
```

---

## Quick Start

### Defining the Plugin

Unlike Bukkit, Velocity has no `onEnable`/`onDisable`. Plugins are constructed by Velocity's
dependency injector and notified of readiness through `ProxyInitializeEvent`. Your concrete
plugin class carries the `@Plugin` and `@Inject` annotations, extends `VelocityPlugin`, and
forwards the injected `ProxyServer` and data directory to `super`:

```java
@Plugin(
        id = "core",
        name = "Core",
        version = "0.0.1",
        authors = {"Trae"}
)
public class CorePlugin extends VelocityPlugin {

    @Inject
    public CorePlugin(final ProxyServer proxyServer, final @DataDirectory Path dataDirectory) {
        super(proxyServer, dataDirectory);
    }

    @Subscribe
    public void onProxyInitialize(final ProxyInitializeEvent event) {
        this.initializePlugin();
    }

    @Subscribe
    public void onProxyShutdown(final ProxyShutdownEvent event) {
        this.shutdownPlugin();
    }
}
```

### Defining a Command

Extend `BaseCommand` with the appropriate sender type. Permission is passed via the constructor:
```java
@Component
public class AccountCommand extends BaseCommand<CorePlugin, AccountManager, CommandSource> {

    public AccountCommand() {
        super("account", "Account management", List.of("acc", "client"), "core.commands.account");
    }

    @Override
    public void execute(final CommandSource sender, final String[] args) {
        UtilMessage.message(sender, "Account", "Account command executed!");
    }

    @Override
    public List<String> getTabComplete(final CommandSource sender, final String[] args) {
        return Collections.emptyList();
    }
}
```

### Defining a SubCommand

SubCommands are automatically attached to their parent command through the hierarchy:

```java
@Component
public class AccountAdminSubCommand extends BaseSubCommand<CorePlugin, AccountCommand, Player> {

    private final AccountManager accountManager;

    public AccountAdminSubCommand(final AccountManager accountManager) {
        super("admin", "Toggle Admin Mode", Collections.emptyList(), "core.commands.account.admin");

        this.accountManager = accountManager;
    }

    @Override
    public void execute(final Player player, final String[] args) {
        this.accountManager.getAccountByPlayer(player).ifPresent(account -> {
            if (account.isAdministrating()) {
                account.setAdministrating(false);

                UtilMessage.message(player, "Account", UtilString.pair("Admin Mode", "<red>Disabled</red>"));
            } else {
                account.setAdministrating(true);

                UtilMessage.message(player, "Account", UtilString.pair("Admin Mode", "<green>Enabled</green>"));
            }
        });
    }

    @Override
    public List<String> getTabComplete(final Player player, final String[] args) {
        return Collections.emptyList();
    }
}
```

This registers `/account admin` automatically — the parent `AccountCommand` routes the `admin` argument to `AccountAdminSubCommand` with the remaining args.

### Command Execution Flow
```
/account admin
  │
  ├─ Sender type validation (Player)
  ├─ Permission check (core.commands.account.admin)
  ├─ CommandExecuteEvent (cancellable)
  └─ AccountAdminSubCommand.execute(player, new String[0])
```

### Event Dispatch

Use `UtilEvent` for event dispatch. Velocity's event bus is uniformly asynchronous-capable, so
there is no synchronous/asynchronous distinction — `dispatchAsynchronous` fires and forgets,
while `supply` fires and blocks until all handlers finish, returning the event for inspection:
```java
// Fire and forget
UtilEvent.dispatchAsynchronous(new MyEvent());

// Fire and inspect after all handlers run
MyEvent event = UtilEvent.supplyAsynchronous(new MyEvent());
if (event.isCancelled()) {
    return;
}
```

### Task Execution

Use `UtilTask` for scheduling on Velocity's scheduler. Velocity runs all scheduled tasks on a
cached thread pool, so there is no main-thread concept — `execute` runs inline on the calling
thread, everything else schedules onto the pool:
```java
// Execute inline on the calling thread
UtilTask.execute(() -> {
    // immediate work
});

// Schedule onto the proxy's thread pool
UtilTask.executeAsynchronous(() -> {
    // background work or I/O
});

// Delayed task
UtilTask.executeLaterAsynchronous(() -> {
    player.sendMessage(Component.text("5 seconds later"));
}, 5, ChronoUnit.SECONDS);

// Repeating task with cancellation
UtilTask.scheduleAsynchronous(() -> {
    // periodic work
}, 0, 5, ChronoUnit.SECONDS, () -> !player.isActive());
```

### Messaging

Use `UtilMessage` for MiniMessage-formatted messaging with configurable prefixes:
```java
// Prefixed message to an audience (player or console)
UtilMessage.message(player, "Network", "You connected to <aqua>%s</aqua>.".formatted(serverName));

// Prefixed message with MiniMessage tags
UtilMessage.message(player, "Shop", "<gold>+50 coins</gold> from daily reward!");

// Message a collection of players with an ignore list
UtilMessage.message(players, "Punish", "<yellow>%s</yellow> banned <yellow>%s</yellow>.".formatted(sender.getUsername(), target.getUsername()), List.of(targetUuid));

// Broadcast to all online players
UtilMessage.broadcast("Network", "<red><bold>Restarting</bold></red> in <yellow>5 minutes</yellow>.");

// Broadcast with ignore list
UtilMessage.broadcast("Alert", "<red>Maintenance mode enabled!</red>", List.of(excludedPlayerUuid));

// Log to console
UtilMessage.log("Core", "Plugin loaded successfully!");
```

---

## Custom Events

Velocity events are plain objects — they do not extend a base class. This framework provides
two base classes so custom events integrate with `UtilEvent` and gain optional cancellation:

- Extend `CustomEvent` for events that merely notify listeners and cannot be denied.
- Extend `CustomCancellableEvent` for events that support allow/deny cancellation.

```java
@AllArgsConstructor
@Getter
public class NetworkJoinEvent extends CustomCancellableEvent {

    private final Player player;
}
```

Listen for it on any component implementing `Listener`:
```java
@Component
public class NetworkListener implements Listener {

    @Subscribe(order = EventPriority.HIGH)
    public void onNetworkJoin(final NetworkJoinEvent event) {
        if (someCondition) {
            event.setCancelled(true);
        }
    }
}
```

Fire it and inspect the result:
```java
NetworkJoinEvent event = UtilEvent.supply(new NetworkJoinEvent(player));
if (event.isCancelled()) {
    return;
}
```

> **Note:** `CustomCancellableEvent` adapts Velocity's `ResultedEvent<GenericResult>` to a
> boolean cancelled flag — a denied result is treated as cancelled. Cancellation is only
> observed by callers that use `UtilEvent.supply` (which blocks for handlers); a
> `dispatchAsynchronous` fire-and-forget cannot observe cancellation.

### Event Priority

`EventPriority` maps Bukkit-style priority names onto Velocity's `PostOrder` model. Note that
Velocity orders **descending by value** — a higher value runs earlier — so these constants are
assigned the opposite numeric values you would expect coming from Bukkit, while preserving the
familiar execution order:

| Constant | Executes | Notes |
|---|---|---|
| `BASELINE` | First | Observation/setup only — never modify the event |
| `LOWEST` | Early | |
| `LOW` | Early | |
| `NORMAL` | Default | Standard handler logic |
| `HIGH` | Late | Validation and filtering |
| `HIGHEST` | Near-last | Final say on cancellation |
| `MONITOR` | Last | Monitoring/logging only — never modify the event |

---

## Utilities

| Utility | Description |
|---|---|
| `UtilEvent` | Fire-and-forget and blocking, result-returning event dispatch |
| `UtilTask` | Task scheduling — immediate, scheduled, and repeating with `ChronoUnit`-to-`Duration` conversion |
| `UtilMessage` | MiniMessage-based messaging with configurable prefixes, broadcasting, filtering, and ignore lists |
| `UtilPlugin` | Plugin lookup — internal by name or class |
| `UtilSearch` | Audience-aware single-match search over a collection |

---

## Command Types

| Type | Sender | Use Case |
|---|---|---|
| `BaseCommand<Plugin, Manager, CommandSource>` | `CommandSource` | Any sender |
| `BaseCommand<Plugin, Manager, Player>` | `Player` | Player-only commands |

| SubCommand Type | Sender | Use Case |
|---|---|---|
| `BaseSubCommand<Plugin, Command, CommandSource>` | `CommandSource` | Any sender |
| `BaseSubCommand<Plugin, Command, Player>` | `Player` | Player-only subcommands |

---

## Event Types

| Event Type | Description |
|---|---|
| `CustomEvent` | Base non-cancellable framework event |
| `CustomCancellableEvent` | Framework event with allow/deny cancellation |

---

## Command Events

| Event | Fired When |
|---|---|
| `CommandExecuteEvent` | Any command or subcommand is about to execute |
| `CommandTabCompleteEvent` | Any command or subcommand tab completion is requested |

All command events are cancellable. Cancelling an execute event prevents execution; cancelling a tab complete event returns an empty list.

---

## Plugin Events

| Event | Fired When |
|---|---|
| `PluginInitializeEvent` | A plugin has completed hierarchy initialization |
| `PluginShutdownEvent` | A plugin is about to begin hierarchy teardown |

---

## Suggestion Providers

`DefaultSuggestions` provides reusable, case-insensitively filtered tab-complete providers:

| Provider | Suggests |
|---|---|
| `CUSTOM` | A provided list, filtered by the current argument |
| `SUB_COMMANDS` | Subcommand labels the sender is permitted to use |
| `PLAYERS` | Online players matching a predicate |
| `ALL_PLAYERS` | All online players |
| `INTERNAL_PLUGINS` | All registered framework plugins |

---

## Interfaces

| Interface | Description |
|---|---|
| `Plugin` | Hierarchy root with automatic registration callbacks (provided by Hierarchy-Framework; `VelocityPlugin` implements it) |
| `Listener` | Marker for components auto-registered with the proxy's `EventManager` |
| `Event` | Marker for all framework events |
| `SharedBaseCommand` | Shared contract between commands and subcommands — sender validation, permission, execution, and tab-complete |
| `IBaseCommand` | Command contract with subcommand management |
| `ICustomCancellableEvent` | Cancellable event adapting Velocity's `ResultedEvent` to a boolean flag |
