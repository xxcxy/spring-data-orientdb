package org.springframework.data.orientdb3.springboot.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.orientdb3.support.IOrientdbConfig;

/**
 * Special adapter for Springboot.
 *
 * @author xxcxy
 */
@Configuration("orientdbConfig")
@ConfigurationProperties(prefix = "spring.data.orientdb3")
public class OrientdbProperties implements IOrientdbConfig {

    private String hosts;
    private String username;
    private String password;
    private String databaseUsername;
    private String databasePassword;
    private String databaseName;
    private boolean autoGenerateSchema;
    private String entityScanPackage;
    private String projectionScanPackage;

    /*
     * (non-Javadoc)
     * @see IOrientdbConfig#getEntityScanPackage()
     */
    @Override
    public String getEntityScanPackage() {
        return entityScanPackage;
    }

    /*
     * (non-Javadoc)
     * @see IOrientdbConfig#getDatabaseUsername()
     */
    @Override
    public String getDatabaseUsername() {
        return databaseUsername;
    }

    /*
     * (non-Javadoc)
     * @see IOrientdbConfig#getDatabasePassword()
     */
    @Override
    public String getDatabasePassword() {
        return databasePassword;
    }

    /*
     * (non-Javadoc)
     * @see IOrientdbConfig#getDatabaseName()
     */
    @Override
    public String getDatabaseName() {
        return databaseName;
    }

    /*
     * (non-Javadoc)
     * @see IOrientdbConfig#getUsername()
     */
    @Override
    public String getUsername() {
        return username;
    }

    /*
     * (non-Javadoc)
     * @see IOrientdbConfig#getPassword()
     */
    @Override
    public String getPassword() {
        return password;
    }

    /*
     * (non-Javadoc)
     * @see IOrientdbConfig#getHosts()
     */
    @Override
    public String getHosts() {
        return hosts;
    }

    /*
     * (non-Javadoc)
     * @see IOrientdbConfig#getAutoGenerateSchema()
     */
    @Override
    public boolean getAutoGenerateSchema() {
        return autoGenerateSchema;
    }

    /*
     * (non-Javadoc)
     * @see IOrientdbConfig#getProjectionScanPackage()
     */
    @Override
    public String getProjectionScanPackage() {
        return projectionScanPackage;
    }

    /**
     * Sets hosts.
     *
     * @param hosts
     */
    public void setHosts(final String hosts) {
        this.hosts = hosts;
    }

    /**
     * Sets user name.
     *
     * @param username
     */
    public void setUsername(final String username) {
        this.username = username;
    }

    /**
     * Sets password.
     *
     * @param password
     */
    public void setPassword(final String password) {
        this.password = password;
    }

    /**
     * Sets database user name.
     *
     * @param databaseUsername
     */
    public void setDatabaseUsername(final String databaseUsername) {
        this.databaseUsername = databaseUsername;
    }

    /**
     * Sets database password.
     *
     * @param databasePassword
     */
    public void setDatabasePassword(final String databasePassword) {
        this.databasePassword = databasePassword;
    }

    /**
     * Sets database name.
     *
     * @param databaseName
     */
    public void setDatabaseName(final String databaseName) {
        this.databaseName = databaseName;
    }

    /**
     * Sets autoGenerateSchema.
     *
     * @param autoGenerateSchema
     */
    public void setAutoGenerateSchema(final boolean autoGenerateSchema) {
        this.autoGenerateSchema = autoGenerateSchema;
    }

    /**
     * Sets entity scan package.
     *
     * @param entityScanPackage
     */
    public void setEntityScanPackage(final String entityScanPackage) {
        this.entityScanPackage = entityScanPackage;
    }

    /**
     * Sets projection scan package.
     *
     * @param projectionScanPackage
     */
    public void setProjectionScanPackage(final String projectionScanPackage) {
        this.projectionScanPackage = projectionScanPackage;
    }
}
