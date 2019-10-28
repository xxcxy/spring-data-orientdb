package org.springframework.data.orientdb3.repository;

import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.db.ODatabaseType;
import com.orientechnologies.orient.core.db.OrientDB;
import com.orientechnologies.orient.core.db.OrientDBConfig;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.orientdb3.support.SessionFactory;
import org.springframework.data.orientdb3.transaction.SessionHolder;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
public class RepositoryTestBase {
    @Autowired
    protected SessionFactory sessionFactory;

    public static void initDb(final String hosts) {
        try {
            OrientDB orientDB = new OrientDB(hosts, OrientDBConfig.defaultConfig());
            if (orientDB.exists("repository_test")) {
                orientDB.drop("repository_test");
            }
            orientDB.create("repository_test", ODatabaseType.PLOCAL);
            orientDB.close();
        } catch (Exception e) {
        }
    }

    protected ODatabaseSession getSession() {
        Object sessionHolder = TransactionSynchronizationManager.getResource(sessionFactory);
        if (sessionHolder != null) {
            return ((SessionHolder) sessionHolder).getSession();
        }
        return sessionFactory.openSession();
    }
}
