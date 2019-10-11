package io.xxcxy.spring.data.orientdb.repository.support;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class OrientdbIdParserHolder {
    private final Map<Class, OrientdbIdParser> idParserMap = new HashMap<>();

    public OrientdbIdParserHolder(final OrientdbIdParser... parsers) {
        for (OrientdbIdParser parser : parsers) {
            idParserMap.put(parser.getIdClass(), parser);
        }
    }

    public void addParser(final OrientdbIdParser orientdbIdParser) {
        idParserMap.putIfAbsent(orientdbIdParser.getIdClass(), orientdbIdParser);
    }

    public <T> Optional<OrientdbIdParser<T>> getIdParser(final Class<T> idClazz) {
        return Optional.ofNullable(idParserMap.get(idClazz));
    }

    public Optional<OrientdbIdParser> getIdParserByParserClass(final Class parserClazz) {
        for (OrientdbIdParser orientdbIdParser : idParserMap.values()) {
            if (orientdbIdParser.getClass().equals(parserClazz)) {
                return Optional.of(orientdbIdParser);
            }
        }
        return Optional.empty();
    }
}
