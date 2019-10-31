package org.springframework.data.orientdb3.repository.query;

import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.data.orientdb3.repository.Modifying;
import org.springframework.data.orientdb3.repository.Query;
import org.springframework.data.orientdb3.repository.support.OrientdbEntityInformation;
import org.springframework.data.orientdb3.repository.support.OrientdbIdParserHolder;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.query.Parameters;
import org.springframework.data.repository.query.QueryMethod;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import static java.lang.String.format;

/**
 * Orientdb specific extension of {@link QueryMethod}.
 *
 * @author xxcxy
 */
public class OrientdbQueryMethod extends QueryMethod {

    private final Method method;

    private final Modifying modifying;
    private final OrientdbEntityInformation<?, ?> entityInformation;

    /**
     * Creates a {@link OrientdbQueryMethod}.
     *
     * @param method                 must not be {@literal null}
     * @param metadata               must not be {@literal null}
     * @param factory                must not be {@literal null}
     * @param orientdbIdParserHolder must not be {@literal null}
     */
    public OrientdbQueryMethod(final Method method, final RepositoryMetadata metadata,
                               final ProjectionFactory factory, final OrientdbIdParserHolder orientdbIdParserHolder) {

        super(method, metadata, factory);

        Assert.notNull(method, "Method must not be null!");

        this.method = method;
        this.entityInformation = new OrientdbEntityInformation<>(getDomainClass(), orientdbIdParserHolder);
        this.modifying = AnnotatedElementUtils.findMergedAnnotation(method, Modifying.class);

        Assert.isTrue(!(isModifyingQuery() && getParameters().hasSpecialParameter()),
                format("Modifying method must not contain %s!", Parameters.TYPES));
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.repository.query.QueryMethod#getEntityInformation()
     */
    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public OrientdbEntityInformation<?, ?> getEntityInformation() {
        return this.entityInformation;
    }

    /**
     * Returns whether the finder is a modifying one.
     *
     * @return
     */
    @Override
    public boolean isModifyingQuery() {
        return modifying != null;
    }

    /**
     * Returns the actual return type of the method.
     *
     * @return
     */
    Class<?> getReturnType() {
        return method.getReturnType();
    }

    /**
     * Returns the query string declared in a {@link StringQuery} annotation or {@literal null} if neither the annotation found
     * nor the attribute was specified.
     *
     * @return
     */
    @Nullable
    String getAnnotatedQuery() {
        String query = getAnnotationValue("value", String.class);
        return StringUtils.hasText(query) ? query : null;
    }

    /**
     * Returns the required query string declared in a {@link StringQuery} annotation or throws {@link IllegalStateException} if
     * neither the annotation found nor the attribute was specified.
     *
     * @return
     * @throws IllegalStateException if no {@link StringQuery} annotation is present or the query is empty.
     * @since 2.0
     */
    String getRequiredAnnotatedQuery() throws IllegalStateException {

        String query = getAnnotatedQuery();

        if (query != null) {
            return query;
        }

        throw new IllegalStateException(format("No annotated query found for query method %s!", getName()));
    }

    /**
     * Returns the countQuery string declared in a {@link StringQuery} annotation or {@literal null} if neither the annotation
     * found nor the attribute was specified.
     *
     * @return
     */
    @Nullable
    String getCountQuery() {
        String countQuery = getAnnotationValue("countQuery", String.class);
        return StringUtils.hasText(countQuery) ? countQuery : null;
    }

    /**
     * Returns the {@link StringQuery} annotation's attribute casted to the given type or default value if no annotation
     * available.
     *
     * @param attribute
     * @param type
     * @return
     */
    private <T> T getAnnotationValue(String attribute, Class<T> type) {

        Annotation annotation = AnnotatedElementUtils.findMergedAnnotation(method, Query.class);
        if (annotation == null) {
            return type.cast(AnnotationUtils.getDefaultValue(Query.class, attribute));
        }

        return type.cast(AnnotationUtils.getValue(annotation, attribute));
    }

}
