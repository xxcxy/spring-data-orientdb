package org.springframework.data.repository.orientdb3.support;

public interface IOrientdbConfig {
    String getUrl();

    String getServerUser();

    String getServerPassword();

    String getDatabase();

    String getUserName();

    String getPassword();

    boolean getAutoGenerateSchema();

    String getEntityScanPackage();
}
