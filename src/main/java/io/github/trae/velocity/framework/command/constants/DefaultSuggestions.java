package io.github.trae.velocity.framework.command.constants;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import io.github.trae.hf.Plugin;
import io.github.trae.utilities.objects.function.BiFunction;
import io.github.trae.utilities.objects.function.TriFunction;
import io.github.trae.velocity.framework.command.BaseCommand;
import io.github.trae.velocity.framework.command.BaseSubCommand;
import io.github.trae.velocity.framework.utility.UtilPlugin;

import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Reusable tab-complete suggestion providers for use in {@link BaseCommand} and
 * {@link BaseSubCommand} implementations.
 * <p>
 * All suggestions are case-insensitively filtered against the current argument input
 * using {@link #CUSTOM}.
 */
public class DefaultSuggestions {

    /**
     * Filters a provided list of strings to those that start with the given argument,
     * case-insensitively.
     * <p>
     * Used internally by all other suggestion providers as the base filter.
     *
     * <p>Usage:
     * <pre>{@code
     * DefaultSuggestions.CUSTOM.apply(List.of("create", "delete", "list"), "cr");
     * // returns ["create"]
     * }</pre>
     */
    public static final BiFunction<List<String>, String, List<String>> CUSTOM = (list, arg) -> list.stream().filter(string -> string.toLowerCase(Locale.ROOT).startsWith(arg.toLowerCase(Locale.ROOT))).toList();

    /**
     * Suggests the labels of subcommands registered to the given {@link BaseCommand} that
     * the sender is permitted to use.
     * <p>
     * Filters by both {@link BaseSubCommand#isValidSender} and {@link BaseSubCommand#hasPermission}
     * before applying the {@link #CUSTOM} prefix filter.
     *
     * <p>Usage:
     * <pre>{@code
     * DefaultSuggestions.SUB_COMMANDS.apply(baseCommand, sender, arg);
     * }</pre>
     */
    public static final TriFunction<BaseCommand<?, ?, ?>, CommandSource, String, List<String>> SUB_COMMANDS = (baseCommand, commandSender, arg) -> CUSTOM.apply(
            baseCommand
                    .getSubCommands()
                    .values()
                    .stream()
                    .filter(baseSubCommand -> baseSubCommand.isValidSender(commandSender) && baseSubCommand.hasPermission(commandSender))
                    .map(BaseSubCommand::getLabel).toList(),
            arg
    );

    /**
     * Suggests the names of online players matching the given predicate, filtered by the
     * current argument input.
     *
     * <p>Usage:
     * <pre>{@code
     * DefaultSuggestions.PLAYERS.apply(player -> player.getWorld().getName().equals("world"), arg);
     * }</pre>
     */
    public static final BiFunction<Predicate<Player>, String, List<String>> PLAYERS = (predicate, arg) -> CUSTOM.apply(
            UtilPlugin.getInstance().getProxyServer().getAllPlayers().stream().filter(predicate).map(Player::getUsername).toList(),
            arg
    );

    /**
     * Suggests the names of all online players, filtered by the current argument input.
     */
    public static final Function<String, List<String>> ALL_PLAYERS = arg -> PLAYERS.apply(__ -> true, arg);

    /**
     * Suggests the names of all registered internal plugins, filtered by the current argument input.
     */
    public static final Function<String, List<String>> INTERNAL_PLUGINS = arg -> CUSTOM.apply(
            UtilPlugin.getInternalPluginList().stream().map(Plugin::getPluginName).toList(),
            arg
    );
}