package org.springframework.data.orientdb3.repository;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.orientdb3.repository.config.EnableOrientdbRepositories;
import org.springframework.data.orientdb3.support.IOrientdbConfig;
import org.springframework.data.orientdb3.support.SessionFactory;
import org.springframework.data.orientdb3.transaction.OrientdbTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@EnableOrientdbRepositories
class RepositoryTestConfig {

    public IOrientdbConfig orientdbConfig(final String hosts) {
        return new IOrientdbConfig() {
            @Override
            public String getHosts() {
                return hosts;
            }

            @Override
            public String getDatabaseUsername() {
                return null;
            }

            @Override
            public String getDatabasePassword() {
                return null;
            }

            @Override
            public String getDatabaseName() {
                return "repository_test";
            }

            @Override
            public String getUsername() {
                return "admin";
            }

            @Override
            public boolean getAutoGenerateSchema() {
                return true;
            }

            @Override
            public String getEntityScanPackage() {
                return "org.springframework.data.orientdb3.test.sample";
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
}
