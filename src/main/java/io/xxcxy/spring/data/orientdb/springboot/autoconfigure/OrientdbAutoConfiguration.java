package io.xxcxy.spring.data.orientdb.springboot.autoconfigure;

import io.xxcxy.spring.data.orientdb.support.SessionFactory;
import io.xxcxy.spring.data.orientdb.transaction.OrientdbTransactionManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
