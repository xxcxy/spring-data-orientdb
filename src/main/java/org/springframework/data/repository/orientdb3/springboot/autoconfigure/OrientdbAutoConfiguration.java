package org.springframework.data.repository.orientdb3.springboot.autoconfigure;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.repository.orientdb3.support.SessionFactory;
import org.springframework.data.repository.orientdb3.transaction.OrientdbTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class OrientdbAutoConfiguration {

    @Bean
    @SuppressWarnings({"rawtypes", "unchecked"})
    @ConditionalOnMissingBean(PlatformTransactionManager.class)
    public PlatformTransactionManager transactionManager(final SessionFactory sessionFactory) {
        return new OrientdbTransactionManager(sessionFactory);
    }
}
