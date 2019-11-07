package org.springframework.data.orientdb3.repository.config;


import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.data.orientdb3.repository.EdgeEntity;
import org.springframework.data.orientdb3.repository.ElementEntity;
import org.springframework.data.orientdb3.repository.EmbeddedEntity;
import org.springframework.data.orientdb3.repository.OrientdbRepository;
import org.springframework.data.orientdb3.repository.VertexEntity;
import org.springframework.data.orientdb3.repository.support.CollectOrientdbIdParserPostProcessor;
import org.springframework.data.orientdb3.repository.support.OrientdbIdParserHolder;
import org.springframework.data.orientdb3.repository.support.OrientdbRepositoryFactoryBean;
import org.springframework.data.orientdb3.repository.support.StringIdParser;
import org.springframework.data.orientdb3.support.SessionFactory;
import org.springframework.data.repository.config.AnnotationRepositoryConfigurationSource;
import org.springframework.data.repository.config.RepositoryConfigurationExtensionSupport;
import org.springframework.data.repository.config.RepositoryConfigurationSource;
import org.springframework.data.repository.config.XmlRepositoryConfigurationSource;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import static org.springframework.beans.factory.support.AbstractBeanDefinition.AUTOWIRE_BY_TYPE;

/**
 * Orientdb specific configuration extension parsing custom attributes from the XML namespace and
 * {@link EnableOrientdbRepositories} annotation.
 * {@link PersistenceExceptionTranslationPostProcessor} to enable exception translation of persistence specific
 * exceptions into Spring's {@link DataAccessException} hierarchy.
 *
 * @author xxcxy
 */
public class OrientdbRepositoryConfigurationExtension extends RepositoryConfigurationExtensionSupport {

    /**
     * See {@link AbstractBeanDefinition#INFER_METHOD}.
     */
    private static final String DEFAULT_SESSION_FACTORY_BEAN_NAME = "sessionFactory";
    private static final String DEFAULT_MAPPING_CONTEXT_BEAN_NAME = "mappingContext";
    private static final String DEFAULT_ID_PARSER_HOLDER = "orientdbIdParserHolder";
    private static final String DEFAULT_TRANSACTION_MANAGER_BEAN_NAME = "transactionManager";
    private static final String ENABLE_DEFAULT_TRANSACTIONS_ATTRIBUTE = "enableDefaultTransactions";
    private static final String DEFAULT_DB_CONFIG_BEAN_NAME = "orientdbConfig";

    /*
     * (non-Javadoc)
     * @see org.springframework.data.repository.config.RepositoryConfigurationExtensionSupport#getModuleName()
     */
    @Override
    public String getModuleName() {
        return "Orientdb";
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.repository.config14.RepositoryConfigurationExtension#getRepositoryFactoryBeanClassName()
     */
    @Override
    public String getRepositoryFactoryBeanClassName() {
        return OrientdbRepositoryFactoryBean.class.getName();
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.repository.config14.RepositoryConfigurationExtensionSupport#getModulePrefix()
     */
    @Override
    protected String getModulePrefix() {
        return getModuleName().toLowerCase();
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.repository.config.RepositoryConfigurationExtensionSupport#getIdentifyingAnnotations()
     */
    @Override
    @SuppressWarnings("unchecked")
    protected Collection<Class<? extends Annotation>> getIdentifyingAnnotations() {
        return Arrays.asList(ElementEntity.class, VertexEntity.class, EdgeEntity.class, EmbeddedEntity.class);
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.repository.config.RepositoryConfigurationExtensionSupport#getIdentifyingTypes()
     */
    @Override
    protected Collection<Class<?>> getIdentifyingTypes() {
        return Collections.singleton(OrientdbRepository.class);
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.repository.config.RepositoryConfigurationExtensionSupport#postProcess(org.springframework.beans.factory.support.BeanDefinitionBuilder, org.springframework.data.repository.config.RepositoryConfigurationSource)
     */
    @Override
    public void postProcess(BeanDefinitionBuilder builder, RepositoryConfigurationSource source) {
        Optional<String> transactionManagerRef = source.getAttribute("transactionManagerRef");
        builder.addPropertyValue("transactionManager",
                transactionManagerRef.orElse(DEFAULT_TRANSACTION_MANAGER_BEAN_NAME));
        builder.addPropertyReference("sessionFactory", DEFAULT_SESSION_FACTORY_BEAN_NAME);
        builder.addPropertyReference("mappingContext", DEFAULT_MAPPING_CONTEXT_BEAN_NAME);
        builder.addPropertyReference("orientdbIdParserHolder", DEFAULT_ID_PARSER_HOLDER);
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.repository.config.RepositoryConfigurationExtensionSupport#postProcess(org.springframework.beans.factory.support.BeanDefinitionBuilder, org.springframework.data.repository.config.AnnotationRepositoryConfigurationSource)
     */
    @Override
    public void postProcess(BeanDefinitionBuilder builder, AnnotationRepositoryConfigurationSource config) {

        AnnotationAttributes attributes = config.getAttributes();

        builder.addPropertyValue(ENABLE_DEFAULT_TRANSACTIONS_ATTRIBUTE,
                attributes.getBoolean(ENABLE_DEFAULT_TRANSACTIONS_ATTRIBUTE));
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.repository.config.RepositoryConfigurationExtensionSupport#postProcess(org.springframework.beans.factory.support.BeanDefinitionBuilder, org.springframework.data.repository.config.XmlRepositoryConfigurationSource)
     */
    @Override
    public void postProcess(BeanDefinitionBuilder builder, XmlRepositoryConfigurationSource config) {

        Optional<String> enableDefaultTransactions = config.getAttribute(ENABLE_DEFAULT_TRANSACTIONS_ATTRIBUTE);

        if (enableDefaultTransactions.filter(StringUtils::hasText).isPresent()) {
            builder.addPropertyValue(ENABLE_DEFAULT_TRANSACTIONS_ATTRIBUTE, enableDefaultTransactions.get());
        }
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.repository.config.RepositoryConfigurationExtensionSupport#registerBeansForRoot(org.springframework.beans.factory.support.BeanDefinitionRegistry, org.springframework.data.repository.config.RepositoryConfigurationSource)
     */
    @Override
    public void registerBeansForRoot(BeanDefinitionRegistry registry, RepositoryConfigurationSource config) {

        super.registerBeansForRoot(registry, config);

        Object source = config.getSource();
        registerIfNotAlreadyRegistered(() -> BeanDefinitionBuilder.rootBeanDefinition(SessionFactory.class)
                        .setAutowireMode(AUTOWIRE_BY_TYPE)
                        .setDestroyMethodName("destroy")
                        .addConstructorArgReference(DEFAULT_DB_CONFIG_BEAN_NAME)
                        .getBeanDefinition(), registry,
                DEFAULT_SESSION_FACTORY_BEAN_NAME, source);
        registerIfNotAlreadyRegistered(() -> BeanDefinitionBuilder.rootBeanDefinition(OrientdbIdParserHolder.class)
                .setAutowireMode(AUTOWIRE_BY_TYPE)
                .addConstructorArgValue(new StringIdParser())
                .getBeanDefinition(), registry, DEFAULT_ID_PARSER_HOLDER, source);
        registerLazyIfNotAlreadyRegistered(
                () -> new RootBeanDefinition(CollectOrientdbIdParserPostProcessor.class), registry,
                "collectOrientdbIdParserPostProcessor", source);
        registerLazyIfNotAlreadyRegistered(() ->
                        new RootBeanDefinition(OrientdbMappingContextFactoryBean.class), registry,
                DEFAULT_MAPPING_CONTEXT_BEAN_NAME, source);
    }

}
