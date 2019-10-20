package org.springframework.data.orientdb3.repository.query;

import com.orientechnologies.orient.core.sql.executor.OResultSet;
import org.springframework.data.orientdb3.repository.support.OrientdbEntityInformation;

import java.util.List;
import java.util.stream.Collectors;

public class TypedQuery<T, ID> {

    private final OResultSet oResultSet;
    private final OrientdbEntityInformation<T, ID> entityInformation;

    public TypedQuery(final OResultSet oResultSet, final OrientdbEntityInformation<T, ID> entityInformation) {
        this.oResultSet = oResultSet;
        this.entityInformation = entityInformation;
    }

    public List<T> getResultList() {
        return oResultSet.elementStream().map(entityInformation::convertToEntity).collect(Collectors.toList());
    }
}
