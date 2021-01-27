package org.apache.http.impl.conn.tsccm;

import java.io.IOException;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Queue;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.util.LangUtils;

@Deprecated
public class RouteSpecificPool {
    protected final LinkedList<BasicPoolEntry> freeEntries;
    private final Log log = LogFactory.getLog(getClass());
    protected final int maxEntries;
    protected int numEntries;
    protected final HttpRoute route;
    protected final Queue<WaitingThread> waitingThreads;

    public RouteSpecificPool(HttpRoute route2, int maxEntries2) {
        this.route = route2;
        this.maxEntries = maxEntries2;
        this.freeEntries = new LinkedList<>();
        this.waitingThreads = new LinkedList();
        this.numEntries = 0;
    }

    public final HttpRoute getRoute() {
        return this.route;
    }

    public final int getMaxEntries() {
        return this.maxEntries;
    }

    public boolean isUnused() {
        return this.numEntries < 1 && this.waitingThreads.isEmpty();
    }

    public int getCapacity() {
        return this.maxEntries - this.numEntries;
    }

    public final int getEntryCount() {
        return this.numEntries;
    }

    public BasicPoolEntry allocEntry(Object state) {
        if (!this.freeEntries.isEmpty()) {
            LinkedList<BasicPoolEntry> linkedList = this.freeEntries;
            ListIterator<BasicPoolEntry> it = linkedList.listIterator(linkedList.size());
            while (it.hasPrevious()) {
                BasicPoolEntry entry = it.previous();
                if (LangUtils.equals(state, entry.getState())) {
                    it.remove();
                    return entry;
                }
            }
        }
        if (this.freeEntries.isEmpty()) {
            return null;
        }
        BasicPoolEntry entry2 = this.freeEntries.remove();
        entry2.setState(null);
        try {
            entry2.getConnection().close();
        } catch (IOException ex) {
            this.log.debug("I/O error closing connection", ex);
        }
        return entry2;
    }

    public void freeEntry(BasicPoolEntry entry) {
        int i = this.numEntries;
        if (i < 1) {
            throw new IllegalStateException("No entry created for this pool. " + this.route);
        } else if (i > this.freeEntries.size()) {
            this.freeEntries.add(entry);
        } else {
            throw new IllegalStateException("No entry allocated from this pool. " + this.route);
        }
    }

    public void createdEntry(BasicPoolEntry entry) {
        if (this.route.equals(entry.getPlannedRoute())) {
            this.numEntries++;
            return;
        }
        throw new IllegalArgumentException("Entry not planned for this pool.\npool: " + this.route + "\nplan: " + entry.getPlannedRoute());
    }

    public boolean deleteEntry(BasicPoolEntry entry) {
        boolean found = this.freeEntries.remove(entry);
        if (found) {
            this.numEntries--;
        }
        return found;
    }

    public void dropEntry() {
        int i = this.numEntries;
        if (i >= 1) {
            this.numEntries = i - 1;
            return;
        }
        throw new IllegalStateException("There is no entry that could be dropped.");
    }

    public void queueThread(WaitingThread wt) {
        if (wt != null) {
            this.waitingThreads.add(wt);
            return;
        }
        throw new IllegalArgumentException("Waiting thread must not be null.");
    }

    public boolean hasThread() {
        return !this.waitingThreads.isEmpty();
    }

    public WaitingThread nextThread() {
        return this.waitingThreads.peek();
    }

    public void removeThread(WaitingThread wt) {
        if (wt != null) {
            this.waitingThreads.remove(wt);
        }
    }
}
