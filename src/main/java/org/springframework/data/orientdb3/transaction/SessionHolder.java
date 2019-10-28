package org.springframework.data.orientdb3.transaction;

import com.orientechnologies.orient.core.db.ODatabaseSession;
import org.springframework.transaction.support.ResourceHolderSupport;
import org.springframework.util.Assert;

/**
 * A {@link ODatabaseSession} Holder.
 *
 * @author xxcxy
 */
public class SessionHolder extends ResourceHolderSupport {

    private ODatabaseSession session;

    /**
     * Sets the session.
     *
     * @param session
     */
    public synchronized void setSession(final ODatabaseSession session) {
        Assert.isNull(this.session, "Session has assigned");
        this.session = session;
    }

    /**
     * Gets the session.
     *
     * @return
     */
    public ODatabaseSession getSession() {
        return this.session;
    }

    /*
     * (non-Javadoc)
     * @see ResourceHolderSupport#clear()
     */
    @Override
    public void clear() {
        super.clear();
        if (session != null && !session.isClosed()) {
            session.close();
        }
    }
}
