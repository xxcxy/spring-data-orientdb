package org.springframework.data.orientdb3.repository.query;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.orientdb3.repository.support.EntityType;

/**
 * Simple utility class to create queries.
 *
 * @author xxcxy
 */
public abstract class QueryUtils {

    /**
     * Creates a sort query.
     *
     * @param sort
     * @return
     */
    public static String createSortQuery(final Sort sort, final String entityName) {
        return "select from "
                .concat(entityName)
                .concat(" order by ")
                .concat(sort.toString().replaceAll(":", ""));
    }

    /**
     * Creates a page query.
     *
     * @param pageable
     * @return
     */
    public static String createPageQuery(final Pageable pageable, final String entityName) {
        StringBuilder sb = new StringBuilder("select from ").append(entityName);
        if (pageable.getSort() != null) {
            sb.append(" order by ").append(pageable.getSort().toString().replaceAll(":", ""));
        }
        sb.append(" skip ")
                .append(pageable.getOffset())
                .append(" limit ")
                .append(pageable.getPageSize());
        return sb.toString();
    }

    /**
     * Creates a delete all query.
     *
     * @param entityType
     * @return
     */
    public static String createDeleteAllQuery(final EntityType entityType, final String entityName) {
        if (entityType == EntityType.VERTEX) {
            return "delete VERTEX ".concat(entityName);
        } else if (entityType == EntityType.EDGE) {
            return "delete EDGE ".concat(entityName);
        } else {
            return "delete from ".concat(entityName);
        }
    }
}
