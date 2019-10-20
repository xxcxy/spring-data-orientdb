package org.springframework.data.orientdb3.repository.query;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public abstract class QueryUtils {

    public static String createSortQuery(final Sort sort) {
        return "select from %s order by " + sort.toString().replaceAll(":", "");
    }

    public static String createPageQuery(final Pageable pageable) {
        StringBuilder sb = new StringBuilder("select from %s ");
        if (pageable.getSort() != null) {
            sb.append(" order by ").append(pageable.getSort().toString().replaceAll(":", ""));
        }
        sb.append(" skip ")
                .append(pageable.getOffset())
                .append(" limit ")
                .append(pageable.getPageSize());
        return sb.toString();
    }
}
