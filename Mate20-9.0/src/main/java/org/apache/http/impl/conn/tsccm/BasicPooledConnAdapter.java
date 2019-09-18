package org.apache.http.impl.conn.tsccm;

import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.conn.AbstractPoolEntry;
import org.apache.http.impl.conn.AbstractPooledConnAdapter;

@Deprecated
public class BasicPooledConnAdapter extends AbstractPooledConnAdapter {
    protected BasicPooledConnAdapter(ThreadSafeClientConnManager tsccm, AbstractPoolEntry entry) {
        super(tsccm, entry);
        markReusable();
    }

    /* access modifiers changed from: protected */
    public ClientConnectionManager getManager() {
        return super.getManager();
    }

    /* access modifiers changed from: protected */
    public AbstractPoolEntry getPoolEntry() {
        return this.poolEntry;
    }

    /* access modifiers changed from: protected */
    public void detach() {
        super.detach();
    }
}
