package io.github.trae.velocity.framework.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.command.PlayerAvailableCommandsEvent;
import io.github.trae.hf.Manager;
import io.github.trae.hf.Module;
import io.github.trae.velocity.framework.VelocityPlugin;
import io.github.trae.velocity.framework.command.constants.DefaultSuggestions;
import io.github.trae.velocity.framework.command.interfaces.IBaseCommand;
import io.github.trae.velocity.framework.command.interfaces.SharedBaseCommand;
import io.github.trae.velocity.framework.command.wrappers.VelocityCommandWrapper;
import io.github.trae.velocity.framework.event.interfaces.Listener;
import lombok.Getter;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Abstract base class for top-level commands in the framework.
 *
 * <p>Combines {@link Module} from the hierarchy framework with {@link SharedBaseCommand} and
 * {@link IBaseCommand} to provide a typed command that owns a registry of {@link BaseSubCommand}s.
 * On construction it builds a {@link VelocityCommandWrapper} that bridges it into Velocity's
 * command manager; the wrapper is registered during component initialization.</p>
 *
 * @param <Plugin>          the plugin type this command belongs to
 * @param <VelocityManager> the owning {@link Manager} type
 * @param <Sender>          the expected {@link CommandSource} type
 */
@Getter
public abstract class BaseCommand<Plugin extends VelocityPlugin, VelocityManager extends Manager<Plugin>, Sender extends CommandSource> implements Module<Plugin, VelocityManager>, SharedBaseCommand<Sender>, IBaseCommand, Listener {

    private final String label, description;
    private final List<String> aliases;
    private final String permission;

    private final LinkedHashMap<String, BaseSubCommand<?, ?, ?>> subCommands = new LinkedHashMap<>();

    private final VelocityCommandWrapper velocityCommandWrapper;

    /**
     * Constructs a command with a permission node.
     *
     * @param label       the primary label used to invoke this command
     * @param description a short description of this command
     * @param aliases     alternative labels for this command
     * @param permission  the permission node required to use this command, or {@code null} for none
     */
    public BaseCommand(final String label, final String description, final List<String> aliases, final String permission) {
        this.label = label;
        this.description = description;
        this.permission = permission;
        this.aliases = aliases;

        this.velocityCommandWrapper = new VelocityCommandWrapper(this);
    }

    /**
     * Constructs a command without a permission node.
     *
     * @param label       the primary label used to invoke this command
     * @param description a short description of this command
     * @param aliases     alternative labels for this command
     * @see #BaseCommand(String, String, List, String)
     */
    public BaseCommand(final String label, final String description, final List<String> aliases) {
        this(label, description, aliases, null);
    }

    @Override
    public List<String> getTabComplete(final Sender sender, final String[] args) {
        if (args.length == 1) {
            return DefaultSuggestions.SUB_COMMANDS.apply(this, sender, args[0]);
        }

        return SharedBaseCommand.super.getTabComplete(sender, args);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Returns {@code /<label>}, e.g. {@code /faction}.
     */
    @Override
    public String getUsage() {
        return "/%s".formatted(this.getLabel());
    }

    /**
     * {@inheritDoc}
     * <p>
     * Stores the subcommand keyed by its lowercased label.
     */
    @Override
    public void $addSubCommand(final BaseSubCommand<?, ?, ?> baseSubCommand) {
        this.subCommands.put(baseSubCommand.getLabel().toLowerCase(Locale.ROOT), baseSubCommand);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Removes the subcommand keyed by its lowercased label.
     */
    @Override
    public void $removeSubCommand(final BaseSubCommand<?, ?, ?> baseSubCommand) {
        this.subCommands.remove(baseSubCommand.getLabel().toLowerCase(Locale.ROOT));
    }

    /**
     * {@inheritDoc}
     * <p>
     * Resolves first by exact lowercased label, then falls back to scanning subcommand aliases.
     */
    @Override
    public Optional<BaseSubCommand<?, ?, ?>> getSubCommandByLabel(final String label) {
        final String lowerLabel = label.toLowerCase(Locale.ROOT);

        final BaseSubCommand<?, ?, ?> baseSubCommand = this.subCommands.get(lowerLabel);
        if (baseSubCommand != null) {
            return Optional.of(baseSubCommand);
        }

        return this.subCommands.values().stream().filter(value -> value.getAliases().contains(lowerLabel)).findFirst();
    }

    @SuppressWarnings("unchecked")
    @Subscribe
    public void onPlayerAvailableCommands(final PlayerAvailableCommandsEvent event) {
        final RootCommandNode<CommandSource> rootCommandNode = (RootCommandNode<CommandSource>) event.getRootNode();

        // permission gate for the root command
        if (!(this.hasPermission(event.getPlayer()))) {
            return;
        }

        final LiteralArgumentBuilder<CommandSource> literalArgumentBuilder = BrigadierCommand.literalArgumentBuilder(this.label)
                .then(BrigadierCommand.requiredArgumentBuilder("args", StringArgumentType.greedyString())
                        .suggests((context, suggestionsBuilder) -> this.buildSuggestions(this, context, suggestionsBuilder)));

        for (final BaseSubCommand<?, ?, ?> baseSubCommand : this.subCommands.values()) {
            if (!(baseSubCommand.hasPermission(event.getPlayer()))) {
                continue;
            }

            final LiteralCommandNode<CommandSource> literalCommandNode = BrigadierCommand.literalArgumentBuilder(baseSubCommand.getLabel())
                    .then(BrigadierCommand.requiredArgumentBuilder("args", StringArgumentType.greedyString())
                            .suggests((context, suggestionsBuilder) -> this.buildSuggestions(baseSubCommand, context, suggestionsBuilder)))
                    .build();

            literalArgumentBuilder.then(literalCommandNode);

            for (final String alias : baseSubCommand.getAliases()) {
                literalArgumentBuilder.then(BrigadierCommand.literalArgumentBuilder(alias).redirect(literalCommandNode).build());
            }
        }

        final LiteralCommandNode<CommandSource> literalCommandNode = literalArgumentBuilder.build();

        rootCommandNode.addChild(literalCommandNode);

        for (final String alias : this.aliases) {
            rootCommandNode.addChild(BrigadierCommand.literalArgumentBuilder(alias).redirect(literalCommandNode).build());
        }
    }

    private CompletableFuture<Suggestions> buildSuggestions(final SharedBaseCommand<?> command, final CommandContext<CommandSource> context, final SuggestionsBuilder suggestionsBuilder) {
        final String remaining = suggestionsBuilder.getRemaining();
        final boolean trailingSpace = remaining.endsWith(" ");

        final String[] tokens = remaining.isBlank() ? new String[0] : remaining.trim().split(" ");

        final String[] args;
        if (trailingSpace) {
            args = new String[tokens.length + 1];
            System.arraycopy(tokens, 0, args, 0, tokens.length);
            args[tokens.length] = "";
        } else {
            args = tokens.length == 0 ? new String[]{""} : tokens;
        }

        final String current = args[args.length - 1];
        final int offset = suggestionsBuilder.getInput().length() - current.length();
        final SuggestionsBuilder anchored = suggestionsBuilder.createOffset(offset);

        final List<String> result = command.$getTabComplete(context.getSource(), args);

        for (final String suggestion : result) {
            anchored.suggest(suggestion);
        }

        return anchored.buildFuture();
    }
}