package io.github.trae.velocity.framework.utility.search;

import com.velocitypowered.api.command.CommandSource;
import io.github.trae.utilities.search.AbstractSearchEngine;
import io.github.trae.velocity.framework.utility.UtilColor;
import io.github.trae.velocity.framework.utility.UtilMessage;
import io.github.trae.velocity.framework.utility.enums.ChatColor;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.awt.Color;
import java.util.Collection;
import java.util.function.Supplier;

/**
 * Velocity-flavoured {@link AbstractSearchEngine} that reports to a {@link CommandSource}.
 *
 * <p>Replaces the logger-backed default messaging with {@link UtilMessage}, so search feedback lands
 * in the source's chat, and supplies the colour conventions used across the framework: the input and
 * result count are highlighted in yellow by default, and matches are separated by a grey comma.</p>
 *
 * <p>Every untrusted value, meaning the search input and each candidate's raw name, has its
 * MiniMessage tags escaped before colouring, so player-controlled text always renders literally.</p>
 *
 * <p>Subclasses supply the matching rules, each candidate's raw name through
 * {@link AbstractSearchEngine#getTypeName(Object, Object)} and its colour through
 * {@link #getTypeColor(Object, CommandSource)}.</p>
 *
 * @param <Type> the type being searched for
 */
public abstract class VelocitySearchEngine<Type> extends AbstractSearchEngine<Type, CommandSource> {

    /**
     * Creates a search engine over the given candidate supplier.
     *
     * @param name               the prefix applied to informational messages, may be null or empty
     * @param collectionSupplier supplies the candidates to search, evaluated on every search
     */
    protected VelocitySearchEngine(final String name, final Supplier<Collection<? extends Type>> collectionSupplier) {
        super(name, collectionSupplier);
    }

    /**
     * Sends the message to the command source through {@link UtilMessage}, prefixed with the engine
     * name.
     *
     * @param commandSource the source to message
     * @param prefix        the engine name, may be null or empty
     * @param message       the message body
     */
    @Override
    protected final void message(final CommandSource commandSource, final String prefix, final String message) {
        UtilMessage.message(commandSource, prefix, message);
    }

    /**
     * Escapes the value's MiniMessage tags and serializes it in the colour from
     * {@link #getInputColor()}.
     *
     * @param string the value to format, such as the search input or result count
     * @return the escaped, coloured value
     */
    @Override
    protected final String getInputFormat(final String string) {
        return UtilColor.serialize(this.getInputColor(), this.escapeString(string));
    }

    /**
     * Escapes the candidate's raw name and serializes it in the colour from
     * {@link #getTypeColor(Object, CommandSource)}.
     *
     * @param type          the candidate to format
     * @param name          the candidate's raw display name
     * @param commandSource the source the message is being built for
     * @return the escaped, coloured name
     */
    @Override
    protected final String getTypeFormat(final Type type, final String name, final CommandSource commandSource) {
        return UtilColor.serialize(this.getTypeColor(type, commandSource), this.escapeString(name));
    }

    /**
     * Separator placed between formatted matches in an ambiguous-result message.
     *
     * @return a grey comma separator
     */
    @Override
    protected final String getMatchSeparator() {
        return UtilColor.serialize(ChatColor.GRAY.getColor(), ", ");
    }

    /**
     * Supplies the colour the search input and result count are displayed in.
     *
     * @return the input colour, yellow by default
     */
    protected Color getInputColor() {
        return ChatColor.YELLOW.getColor();
    }

    /**
     * Supplies the colour a candidate's name is displayed in within an ambiguous-result message.
     *
     * @param type          the candidate to colour
     * @param commandSource the source the message is being built for, allowing per-source colouring
     * @return the colour of the candidate's name
     */
    protected abstract Color getTypeColor(final Type type, final CommandSource commandSource);

    /**
     * Escapes an untrusted value so it renders literally. Backslashes are doubled first, so a trailing
     * one cannot escape the closing tag that wraps the value, then MiniMessage tags are escaped.
     *
     * @param string the value to escape
     * @return the escaped value
     */
    private String escapeString(final String string) {
        return MiniMessage.miniMessage().escapeTags(string.replace("\\", "\\\\"));
    }
}