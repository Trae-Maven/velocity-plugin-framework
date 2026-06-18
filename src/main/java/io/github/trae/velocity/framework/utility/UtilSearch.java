package io.github.trae.velocity.framework.utility;

import io.github.trae.utilities.UtilCollection;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.audience.Audience;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Utility class for performing audience-aware searches over a collection.
 *
 * <p>Adapts {@link UtilCollection#search} by routing its feedback messages to a Velocity
 * {@link Audience} through {@link UtilMessage}, allowing search results, ambiguous matches,
 * and no-match notices to be reported directly to a command source.</p>
 */
@UtilityClass
public class UtilSearch {

    /**
     * Searches the collection for a single match, informing the audience of the outcome.
     *
     * @param clazz             the element type's class
     * @param collection        the collection to search
     * @param typePredicate     filters which elements are eligible for matching
     * @param equalsPredicate   tests for an exact match against the input
     * @param containsPredicate tests for a partial match against the input
     * @param listConsumer      receives the list of candidate matches when ambiguous
     * @param colorFunction     applies coloring to feedback message text
     * @param resultFunction    renders a matched element to its display string
     * @param prefix            the message prefix used for audience feedback
     * @param audience          the audience to send search feedback to
     * @param input             the search input string
     * @param inform            whether to send feedback messages to the audience
     * @param <Type>            the element type
     * @return the single matching element, or an empty optional if no unambiguous match was found
     */
    public static <Type> Optional<Type> search(final Class<Type> clazz, final Collection<? extends Type> collection, final Predicate<Type> typePredicate, final Predicate<Type> equalsPredicate, final Predicate<Type> containsPredicate, final Consumer<List<Type>> listConsumer, final Function<String, String> colorFunction, final Function<Type, String> resultFunction, final String prefix, final Audience audience, final String input, final boolean inform) {
        final Consumer<String> messageConsumer = message -> UtilMessage.message(audience, prefix, message);

        return UtilCollection.search(clazz, collection, typePredicate, equalsPredicate, containsPredicate, listConsumer, messageConsumer, colorFunction, resultFunction, input, inform);
    }
}