package org.springframework.data.orientdb3.springboot.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.orientdb3.support.IOrientdbConfig;

@Configuration("orientdbConfig")
@ConfigurationProperties(prefix = "spring.data.orientdb")
public class OrientdbProperties implements IOrientdbConfig {

    private String url;
    private String userName;
    private String password;
    private String serverUser;
    private String serverPassword;
    private String database;
    private boolean autoGenerateSchema;
    private String entityScanPackage;

    @Override
    public String getEntityScanPackage() {
        return entityScanPackage;
    }

    @Override
    public String getServerUser() {
        return serverUser;
    }

    @Override
    public String getServerPassword() {
        return serverPassword;
    }

    @Override
    public String getDatabase() {
        return database;
    }

    @Override
    public String getUserName() {
        return userName;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public boolean getAutoGenerateSchema() {
        return autoGenerateSchema;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setUserName(final String userName) {
        this.userName = userName;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    public void setServerUser(final String serverUser) {
        this.serverUser = serverUser;
    }

    public void setServerPassword(final String serverPassword) {
        this.serverPassword = serverPassword;
    }

    public void setAutoGenerateSchema(final boolean autoGenerateSchema) {
        this.autoGenerateSchema = autoGenerateSchema;
    }

    public void setEntityScanPackage(final String entityScanPackage) {
        this.entityScanPackage = entityScanPackage;
    }

    public void setDatabase(final String database) {
        this.database = database;
    }
}
