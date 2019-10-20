package org.springframework.data.orientdb3.repository.support;

import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.data.orientdb3.support.SessionFactory;

public class SchemaGenerationPostProcessor implements ApplicationListener<ApplicationStartedEvent> {

    @Override
    public void onApplicationEvent(final ApplicationStartedEvent contextStartedEvent) {
        // Generate schema
        SessionFactory sessionFactory = contextStartedEvent.getApplicationContext().getBean(SessionFactory.class);
        sessionFactory.generateSchema();
    }
}
