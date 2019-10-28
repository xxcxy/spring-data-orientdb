package org.springframework.data.orientdb3.repository.support;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * A IdParser Holder.
 *
 * @author xxcxy
 */
public class OrientdbIdParserHolder {
    private final Map<Class, OrientdbIdParser> idParserMap = new HashMap<>();

    /**
     * Creates a new {@link OrientdbIdParserHolder}.
     *
     * @param parsers
     */
    public OrientdbIdParserHolder(final OrientdbIdParser... parsers) {
        for (OrientdbIdParser parser : parsers) {
            idParserMap.put(parser.getIdClass(), parser);
        }
    }

    /**
     * Adds a {@link OrientdbIdParser} to this {@link OrientdbIdParserHolder}.
     *
     * @param orientdbIdParser
     */
    public void addParser(final OrientdbIdParser orientdbIdParser) {
        idParserMap.putIfAbsent(orientdbIdParser.getIdClass(), orientdbIdParser);
    }

    /**
     * Gets a {@link OrientdbIdParser} for a given class.
     *
     * @param idClazz
     * @param <T>
     * @return
     */
    public <T> Optional<OrientdbIdParser<T>> getIdParser(final Class<T> idClazz) {
        return Optional.ofNullable(idParserMap.get(idClazz));
    }

    /**
     * Gets a {@link OrientdbIdParser} that can parse the given class.
     *
     * @param parserClazz
     * @return
     */
    public Optional<OrientdbIdParser> getIdParserByParserClass(final Class parserClazz) {
        for (OrientdbIdParser orientdbIdParser : idParserMap.values()) {
            if (orientdbIdParser.getClass().equals(parserClazz)) {
                return Optional.of(orientdbIdParser);
            }
        }
        return Optional.empty();
    }
}
