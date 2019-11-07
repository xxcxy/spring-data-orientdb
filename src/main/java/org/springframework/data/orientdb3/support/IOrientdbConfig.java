package org.springframework.data.orientdb3.support;

/**
 * The orientdb config properties.
 *
 * @author xxcxy
 */
public interface IOrientdbConfig {
    /**
     * Gets the hosts.
     *
     * @return
     */
    String getHosts();

    /**
     * Gets the database user name.
     *
     * @return
     */
    String getDatabaseUsername();

    /**
     * Gets the database password.
     *
     * @return
     */
    String getDatabasePassword();

    /**
     * Gets the database name.
     *
     * @return
     */
    String getDatabaseName();

    /**
     * Gets the user name.
     *
     * @return
     */
    String getUsername();

    /**
     * Gets the password.
     *
     * @return
     */
    String getPassword();

    /**
     * Gets the autoGenerateSchema.
     *
     * @return
     */
    boolean getAutoGenerateSchema();

    /**
     * Gets the entity scan package.
     *
     * @return
     */
    String getEntityScanPackage();

    /**
     * Gets the projections scan package.
     *
     * @return
     */
    String getProjectionScanPackage();
}
