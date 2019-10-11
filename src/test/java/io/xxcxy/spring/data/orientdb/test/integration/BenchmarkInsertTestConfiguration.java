package io.xxcxy.spring.data.orientdb.test.integration;

import io.xxcxy.spring.data.orientdb.repository.config.EnableOrientdbRepositories;
import io.xxcxy.spring.data.orientdb.repository.support.OrientdbIdParser;
import io.xxcxy.spring.data.orientdb.support.IOrientdbConfig;
import io.xxcxy.spring.data.orientdb.support.SessionFactory;
import io.xxcxy.spring.data.orientdb.test.integration.IdParser.CustIdParser;
import io.xxcxy.spring.data.orientdb.transaction.OrientdbTransactionManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@EnableOrientdbRepositories
public class BenchmarkInsertTestConfiguration {


    @Bean("orientdbConfig")
    public IOrientdbConfig orientdbConfig() {
        return new IOrientdbConfig() {
            @Override
            public String getUrl() {
                return "plocal:orient-db/spring-data-test";
            }

            @Override
            public String getServerUser() {
                return null;
            }

            @Override
            public String getServerPassword() {
                return null;
            }

            @Override
            public String getDatabase() {
                return "benchmarkinsert";
            }

            @Override
            public String getUserName() {
                return "admin";
            }

            @Override
            public String getPassword() {
                return "admin";
            }
        };
    }

    @Bean
    public PlatformTransactionManager transactionManager(SessionFactory sessionFactory) {
        return new OrientdbTransactionManager(sessionFactory);
    }

    @Bean
    public OrientdbIdParser custIdParser() {
        return new CustIdParser();
    }
}
