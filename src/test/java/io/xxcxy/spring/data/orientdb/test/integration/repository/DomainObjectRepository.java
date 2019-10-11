package io.xxcxy.spring.data.orientdb.test.integration.repository;

import io.xxcxy.spring.data.orientdb.repository.OrientdbRepository;
import io.xxcxy.spring.data.orientdb.test.DomainObject;
import io.xxcxy.spring.data.orientdb.test.integration.IdParser.CustId;

public interface DomainObjectRepository extends OrientdbRepository<DomainObject, CustId> {
}
