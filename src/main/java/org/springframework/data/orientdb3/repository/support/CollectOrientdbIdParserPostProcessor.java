package org.springframework.data.orientdb3.repository.support;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

/**
 * Sets up all {@link OrientdbIdParser}.
 *
 * @author xxcxy
 */
public class CollectOrientdbIdParserPostProcessor implements BeanFactoryPostProcessor {
    /**
     * Finds all {@link OrientdbIdParser} in spring context and adds to {@link OrientdbIdParserHolder}.
     *
     * @param configurableListableBeanFactory
     * @throws BeansException
     */
    @Override
    public void postProcessBeanFactory(final ConfigurableListableBeanFactory configurableListableBeanFactory)
            throws BeansException {
        OrientdbIdParserHolder holder = configurableListableBeanFactory.getBean(OrientdbIdParserHolder.class);
        for (OrientdbIdParser parser : configurableListableBeanFactory.
                getBeansOfType(OrientdbIdParser.class).values()) {
            holder.addParser(parser);
        }
    }
}
