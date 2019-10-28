package org.springframework.data.orientdb3.springboot.autoconfigure;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.orientdb3.support.SessionFactory;
import org.springframework.data.orientdb3.transaction.OrientdbTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Special adapter for Springboot.
 *
 * @author xxcxy
 */
@Configuration
public class OrientdbAutoConfiguration {

    /**
     * Creates a new {@link OrientdbTransactionManager}.
     *
     * @param sessionFactory
     * @return
     */
    @Bean
    @SuppressWarnings({"rawtypes", "unchecked"})
    @ConditionalOnMissingBean(PlatformTransactionManager.class)
    public PlatformTransactionManager transactionManager(final SessionFactory sessionFactory) {
        return new OrientdbTransactionManager(sessionFactory);
    }
}
