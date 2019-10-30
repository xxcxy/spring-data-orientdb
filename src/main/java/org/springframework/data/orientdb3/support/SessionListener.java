package org.springframework.data.orientdb3.support;

import com.orientechnologies.orient.core.command.OCommandExecutor;
import com.orientechnologies.orient.core.command.OCommandRequestText;
import com.orientechnologies.orient.core.db.ODatabase;
import com.orientechnologies.orient.core.db.ODatabaseListener;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Set;

/**
 * A listener that listen for events in the session lifecycle.
 *
 * @author xxcxy
 */
public class SessionListener implements ODatabaseListener {
    private static final ThreadLocal<Set<WeakReference<EntityProxyInterface>>> sessionEntitySet = new ThreadLocal();

    /**
     * Binds the {@link EntityProxyInterface} to threadLocal.
     *
     * @param entityProxyInterface
     */
    public static void bindEntityProxy(final EntityProxyInterface entityProxyInterface) {
        Set<WeakReference<EntityProxyInterface>> set = sessionEntitySet.get();
        if (set == null) {
            set = new HashSet<>();
            sessionEntitySet.set(set);
        }
        set.add(new WeakReference<>(entityProxyInterface));
    }

    /*
     * (non-Javadoc)
     * @see ODatabaseListener#onBeforeTxCommit()
     */
    @Override
    public void onBeforeTxCommit(final ODatabase oDatabase) {
        Set<WeakReference<EntityProxyInterface>> set = sessionEntitySet.get();
        if (set == null) {
            return;
        }
        for (WeakReference<EntityProxyInterface> entityProxy : set) {
            EntityProxyInterface ep = entityProxy.get();
            if (ep != null) {
                ep.loadStable();
            }
        }
    }

    /**
     * Orientdb will change the element id after commit, so it need to load the id after commit.
     *
     * @param oDatabase
     */
    @Override
    public void onAfterTxCommit(final ODatabase oDatabase) {
        Set<WeakReference<EntityProxyInterface>> set = sessionEntitySet.get();
        if (set == null) {
            return;
        }
        for (WeakReference<EntityProxyInterface> entityProxy : set) {
            EntityProxyInterface ep = entityProxy.get();
            if (ep != null) {
                ep.loadId();
            }
        }
    }

    /*
     * (non-Javadoc)
     * @see ODatabaseListener#onClose()
     */
    @Override
    public void onClose(final ODatabase oDatabase) {
    }

    /*
     * (non-Javadoc)
     * @see ODatabaseListener#onCreate()
     */
    @Override
    public void onCreate(final ODatabase oDatabase) {
    }

    /*
     * (non-Javadoc)
     * @see ODatabaseListener#onDelete()
     */
    @Override
    public void onDelete(final ODatabase oDatabase) {
    }

    /*
     * (non-Javadoc)
     * @see ODatabaseListener#onOpen()
     */
    @Override
    public void onOpen(final ODatabase oDatabase) {
    }

    /*
     * (non-Javadoc)
     * @see ODatabaseListener#onBeforeTxBegin()
     */
    @Override
    public void onBeforeTxBegin(final ODatabase oDatabase) {
    }

    /*
     * (non-Javadoc)
     * @see ODatabaseListener#onBeforeTxRollback()
     */
    @Override
    public void onBeforeTxRollback(final ODatabase oDatabase) {
    }

    /*
     * (non-Javadoc)
     * @see ODatabaseListener#onAfterTxRollback()
     */
    @Override
    public void onAfterTxRollback(final ODatabase oDatabase) {
    }

    /*
     * (non-Javadoc)
     * @see ODatabaseListener#onBeforeCommand()
     */
    @Override
    public void onBeforeCommand(final OCommandRequestText oCommandRequestText,
                                final OCommandExecutor oCommandExecutor) {
    }

    /*
     * (non-Javadoc)
     * @see ODatabaseListener#onAfterCommand()
     */
    @Override
    public void onAfterCommand(final OCommandRequestText oCommandRequestText,
                               final OCommandExecutor oCommandExecutor, final Object o) {
        System.out.println("====================================" + oCommandRequestText.getText());
    }
}
