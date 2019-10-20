package org.springframework.data.orientdb3.transaction;

import com.orientechnologies.orient.core.db.ODatabaseSession;
import org.springframework.transaction.support.ResourceHolderSupport;
import org.springframework.util.Assert;

public class SessionHolder extends ResourceHolderSupport {

    private ODatabaseSession session;

    public synchronized void setSession(final ODatabaseSession session) {
        Assert.isNull(this.session, "Session has assigned");
        this.session = session;
    }

    public ODatabaseSession getSession() {
        return this.session;
    }

    @Override
    public void clear() {
        super.clear();
        if (session != null && !session.isClosed()) {
            session.close();
        }
    }
}
