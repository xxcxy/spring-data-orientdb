package io.xxcxy.spring.data.orientdb.test.integration.repository;

import io.xxcxy.spring.data.orientdb.repository.OrientdbRepository;
import io.xxcxy.spring.data.orientdb.test.BenchmarkObject;

public interface BenchmarkObjectRepository extends OrientdbRepository<BenchmarkObject, String> {
}
