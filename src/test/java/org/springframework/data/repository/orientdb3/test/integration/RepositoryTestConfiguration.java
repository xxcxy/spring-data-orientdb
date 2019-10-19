package org.springframework.data.repository.orientdb3.test.integration;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.repository.orientdb3.repository.config.EnableOrientdbRepositories;
import org.springframework.data.repository.orientdb3.repository.support.OrientdbIdParser;
import org.springframework.data.repository.orientdb3.support.IOrientdbConfig;
import org.springframework.data.repository.orientdb3.support.SessionFactory;
import org.springframework.data.repository.orientdb3.test.integration.IdParser.CustIdParser;
import org.springframework.data.repository.orientdb3.transaction.OrientdbTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@EnableOrientdbRepositories
public class RepositoryTestConfiguration {


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
                return "test";
            }

            @Override
            public String getUserName() {
                return "admin";
            }

            @Override
            public boolean getAutoGenerateSchema() {
                return false;
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
