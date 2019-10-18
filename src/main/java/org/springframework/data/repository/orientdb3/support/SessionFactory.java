package org.springframework.data.repository.orientdb3.support;

import com.orientechnologies.orient.core.db.ODatabasePool;
import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.db.OrientDB;
import com.orientechnologies.orient.core.db.OrientDBConfig;

public class SessionFactory {

    private final OrientDB orientDB;
    private final ODatabasePool pool;

    public SessionFactory(final IOrientdbConfig orientdbConfig) {
        orientDB = new OrientDB(orientdbConfig.getUrl(), orientdbConfig.getServerUser(),
                orientdbConfig.getServerPassword(), OrientDBConfig.defaultConfig());
        pool = new ODatabasePool(orientDB, orientdbConfig.getDatabase(), orientdbConfig.getUserName(),
                orientdbConfig.getPassword());
    }

    public ODatabaseSession openSession() {
        return pool.acquire();
    }

    public void destroy() {
        pool.close();
        orientDB.close();
    }
}
