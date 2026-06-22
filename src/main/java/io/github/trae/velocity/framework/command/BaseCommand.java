package io.github.trae.velocity.framework.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.CommandSource;
import io.github.trae.hf.Manager;
import io.github.trae.hf.Module;
import io.github.trae.velocity.framework.VelocityPlugin;
import io.github.trae.velocity.framework.command.constants.DefaultSuggestions;
import io.github.trae.velocity.framework.command.interfaces.IBaseCommand;
import io.github.trae.velocity.framework.command.interfaces.SharedBaseCommand;
import io.github.trae.velocity.framework.event.interfaces.Listener;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
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
 * On construction it eagerly builds the {@link CommandMeta} used for registration; the
 * {@link BrigadierCommand} node tree is built lazily at registration time via
 * {@link #generateBrigadierCommand()} so it reflects every subcommand that has self-registered by
 * then.</p>
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

    private final CommandMeta commandMeta;

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

        this.commandMeta = this.generateCommandMeta(this.getPlugin());
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

    /**
     * {@inheritDoc}
     * <p>
     * For the first argument, suggests the labels of registered subcommands; deeper arguments fall
     * back to the {@link SharedBaseCommand} default.
     */
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

    /**
     * {@inheritDoc}
     * <p>
     * A {@code BaseCommand} is a leaf when its subcommand registry is empty.
     */
    @Override
    public boolean isLeafCommand() {
        return this.subCommands.isEmpty();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Builds the meta from this command's label and aliases against the proxy's command manager.
     */
    @Override
    public CommandMeta generateCommandMeta(final VelocityPlugin velocityPlugin) {
        return velocityPlugin.getProxyServer().getCommandManager()
                .metaBuilder(this.label)
                .aliases(this.aliases.toArray(new String[0]))
                .plugin(velocityPlugin)
                .build();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Builds a root literal node for this command's label. When this command is a
     * {@link #isLeafCommand() leaf}, a single greedy argument node is attached that delegates all
     * input to this command's own {@code $execute}/{@code $getTabComplete}. Otherwise a literal node
     * (plus alias redirects) is attached per registered subcommand, each carrying its own greedy
     * argument node that routes execution and tab-completion to that subcommand. The two layouts are
     * mutually exclusive so a greedy node never collides with sibling subcommand literals.
     */
    @Override
    public BrigadierCommand generateBrigadierCommand() {
        final LiteralArgumentBuilder<CommandSource> literalArgumentBuilder =
                BrigadierCommand.literalArgumentBuilder(this.label)
                        .executes(context -> {
                                    this.$execute(context.getSource(), this.parseArgs(context, 0));
                                    return 1;
                                }
                        );

        if (this.isLeafCommand()) {
            // leaf command: single greedy node, all input routed to this command
            literalArgumentBuilder.then(BrigadierCommand.requiredArgumentBuilder("args", StringArgumentType.greedyString())
                    .suggests((context, builder) -> this.buildSuggestions(this, context, builder))
                    .executes(context -> {
                                this.$execute(context.getSource(), this.parseArgs(context, 0));
                                return 1;
                            }
                    )
            );
        }

        for (final BaseSubCommand<?, ?, ?> baseSubCommand : this.subCommands.values()) {
            final LiteralCommandNode<CommandSource> literalCommandNode =
                    BrigadierCommand.literalArgumentBuilder(baseSubCommand.getLabel())
                            .then(BrigadierCommand.requiredArgumentBuilder("args", StringArgumentType.greedyString())
                                    .suggests((context, builder) -> this.buildSuggestions(baseSubCommand, context, builder))
                                    .executes(context -> {
                                        baseSubCommand.$execute(context.getSource(), this.parseArgs(context, 1));
                                        return 1;
                                    }))
                            .executes(context -> {
                                baseSubCommand.$execute(context.getSource(), this.parseArgs(context, 1));
                                return 1;
                            })
                            .build();

            literalArgumentBuilder.then(literalCommandNode);

            for (final String alias : baseSubCommand.getAliases()) {
                literalArgumentBuilder.then(BrigadierCommand.literalArgumentBuilder(alias).redirect(literalCommandNode).build());
            }
        }

        return new BrigadierCommand(literalArgumentBuilder.build());
    }

    /**
     * Splits the raw command input into arguments, dropping the command label and the first
     * {@code startIndex} tokens.
     *
     * <p>{@code startIndex} is {@code 0} for the root command (arguments relative to the label) and
     * {@code 1} for a subcommand (additionally dropping the subcommand token, so the subcommand
     * receives arguments relative to itself). An empty array is returned when no arguments follow
     * the label.</p>
     *
     * @param context    the command context carrying the raw input
     * @param startIndex the number of leading argument tokens to drop
     * @return the resolved argument array
     */
    private String[] parseArgs(final CommandContext<CommandSource> context, final int startIndex) {
        final String input = context.getInput();

        final int space = input.indexOf(' ');

        if (space < 0) {
            return new String[0];
        }

        final String[] args = input.substring(space + 1).split(" ", -1);

        return Arrays.copyOfRange(args, startIndex, args.length);
    }

    /**
     * Builds tab-complete suggestions for the given command target from the current input buffer.
     *
     * <p>Tokenizes the input after the command label, collapsing repeated spaces and appending a
     * trailing empty token when the buffer ends in whitespace. When the target is a subcommand
     * (i.e. not this command), the leading subcommand token is dropped so the subcommand receives
     * arguments relative to itself. Suggestions are anchored to the offset of the token currently
     * being typed so the client replaces only that token.</p>
     *
     * @param sharedBaseCommand  the command or subcommand to source suggestions from
     * @param commandContext     the command context carrying the source
     * @param suggestionsBuilder the Brigadier suggestions builder for the current input
     * @return the future completing with the anchored suggestions
     */
    private CompletableFuture<Suggestions> buildSuggestions(final SharedBaseCommand<?> sharedBaseCommand, final CommandContext<CommandSource> commandContext, final SuggestionsBuilder suggestionsBuilder) {
        final String input = suggestionsBuilder.getInput();

        final int firstSpace = input.indexOf(' ');
        final String afterLabel = firstSpace < 0 ? "" : input.substring(firstSpace + 1);

        final boolean trailingSpace = afterLabel.endsWith(" ");
        final String[] rawTokens = afterLabel.isBlank() ? new String[0] : afterLabel.trim().split(" +");

        final int dropFirst = sharedBaseCommand != this ? 1 : 0;

        final List<String> list = new ArrayList<>(Arrays.asList(rawTokens));

        for (int i = 0; i < dropFirst && !(list.isEmpty()); i++) {
            list.removeFirst();
        }
        if (trailingSpace) {
            list.add("");
        }
        if (list.isEmpty()) {
            list.add("");
        }

        final String[] args = list.toArray(new String[0]);

        final String current = args[args.length - 1];
        final int offset = input.length() - current.length();
        final SuggestionsBuilder anchored = suggestionsBuilder.createOffset(offset);

        for (final String suggestion : sharedBaseCommand.$getTabComplete(commandContext.getSource(), args)) {
            anchored.suggest(suggestion);
        }

        return anchored.buildFuture();
    }
}