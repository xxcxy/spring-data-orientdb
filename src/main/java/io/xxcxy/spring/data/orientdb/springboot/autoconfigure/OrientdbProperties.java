package io.xxcxy.spring.data.orientdb.springboot.autoconfigure;

import io.xxcxy.spring.data.orientdb.support.IOrientdbConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration("orientdbConfig")
@ConfigurationProperties(prefix = "spring.data.orientdb")
public class OrientdbProperties implements IOrientdbConfig {

    private String url;

    private String userName;

    private String password;

    private String serverUser;

    private String serverPassword;

    private String database;

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

    public void setDatabase(final String database) {
        this.database = database;
    }
}
