package org.apache.xml.dtm.ref;

import java.util.BitSet;
import org.apache.xml.res.XMLErrorResources;
import org.apache.xml.res.XMLMessages;

public class CoroutineManager {
    static final int ANYBODY = -1;
    static final int NOBODY = -1;
    static final int m_unreasonableId = 1024;
    BitSet m_activeIDs;
    int m_nextCoroutine;
    Object m_yield;

    public CoroutineManager() {
        this.m_activeIDs = new BitSet();
        this.m_yield = null;
        this.m_nextCoroutine = NOBODY;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized int co_joinCoroutineSet(int coroutineID) {
        if (coroutineID >= 0) {
            if (coroutineID < m_unreasonableId) {
            }
            return NOBODY;
        }
        coroutineID = 0;
        while (coroutineID < m_unreasonableId) {
            if (!this.m_activeIDs.get(coroutineID)) {
                break;
            }
            coroutineID++;
        }
        if (coroutineID >= m_unreasonableId) {
            return NOBODY;
        }
        this.m_activeIDs.set(coroutineID);
        return coroutineID;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized Object co_entry_pause(int thisCoroutine) throws NoSuchMethodException {
        if (this.m_activeIDs.get(thisCoroutine)) {
            while (true) {
                if (this.m_nextCoroutine != thisCoroutine) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                    }
                }
            }
        } else {
            throw new NoSuchMethodException();
        }
        return this.m_yield;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized Object co_resume(Object arg_object, int thisCoroutine, int toCoroutine) throws NoSuchMethodException {
        if (this.m_activeIDs.get(toCoroutine)) {
            this.m_yield = arg_object;
            this.m_nextCoroutine = toCoroutine;
            notify();
            while (true) {
                if (this.m_nextCoroutine == thisCoroutine && this.m_nextCoroutine != NOBODY) {
                    if (this.m_nextCoroutine != NOBODY) {
                        break;
                    }
                }
                try {
                    wait();
                } catch (InterruptedException e) {
                }
            }
            if (this.m_nextCoroutine == NOBODY) {
                co_exit(thisCoroutine);
                throw new NoSuchMethodException(XMLMessages.createXMLMessage(XMLErrorResources.ER_COROUTINE_CO_EXIT, null));
            }
        } else {
            throw new NoSuchMethodException(XMLMessages.createXMLMessage(XMLErrorResources.ER_COROUTINE_NOT_AVAIL, new Object[]{Integer.toString(toCoroutine)}));
        }
        return this.m_yield;
    }

    public synchronized void co_exit(int thisCoroutine) {
        this.m_activeIDs.clear(thisCoroutine);
        this.m_nextCoroutine = NOBODY;
        notify();
    }

    public synchronized void co_exit_to(Object arg_object, int thisCoroutine, int toCoroutine) throws NoSuchMethodException {
        if (this.m_activeIDs.get(toCoroutine)) {
            this.m_yield = arg_object;
            this.m_nextCoroutine = toCoroutine;
            this.m_activeIDs.clear(thisCoroutine);
            notify();
        } else {
            throw new NoSuchMethodException(XMLMessages.createXMLMessage(XMLErrorResources.ER_COROUTINE_NOT_AVAIL, new Object[]{Integer.toString(toCoroutine)}));
        }
    }
}
