package org.apache.xml.dtm.ref;

import java.util.BitSet;
import org.apache.xml.res.XMLErrorResources;
import org.apache.xml.res.XMLMessages;

public class CoroutineManager {
    static final int ANYBODY = -1;
    static final int NOBODY = -1;
    static final int m_unreasonableId = 1024;
    BitSet m_activeIDs = new BitSet();
    int m_nextCoroutine = -1;
    Object m_yield = null;

    /* JADX WARNING: Missing block: B:6:0x000e, code:
            if (r3.m_activeIDs.get(r4) != false) goto L_0x0010;
     */
    /* JADX WARNING: Missing block: B:8:0x0011, code:
            return -1;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized int co_joinCoroutineSet(int coroutineID) {
        if (coroutineID < 0) {
            coroutineID = 0;
            while (coroutineID < 1024) {
                if (!this.m_activeIDs.get(coroutineID)) {
                    break;
                }
                coroutineID++;
            }
            if (coroutineID >= 1024) {
                return -1;
            }
        } else if (coroutineID < 1024) {
        }
        this.m_activeIDs.set(coroutineID);
        return coroutineID;
    }

    public synchronized Object co_entry_pause(int thisCoroutine) throws NoSuchMethodException {
        if (this.m_activeIDs.get(thisCoroutine)) {
            while (this.m_nextCoroutine != thisCoroutine) {
                try {
                    wait();
                } catch (InterruptedException e) {
                }
            }
        } else {
            throw new NoSuchMethodException();
        }
        return this.m_yield;
    }

    public synchronized Object co_resume(Object arg_object, int thisCoroutine, int toCoroutine) throws NoSuchMethodException {
        if (this.m_activeIDs.get(toCoroutine)) {
            this.m_yield = arg_object;
            this.m_nextCoroutine = toCoroutine;
            notify();
            while (true) {
                if (this.m_nextCoroutine == thisCoroutine && this.m_nextCoroutine != -1) {
                    if (this.m_nextCoroutine != -1) {
                        if (this.m_nextCoroutine == -1) {
                            co_exit(thisCoroutine);
                            throw new NoSuchMethodException(XMLMessages.createXMLMessage(XMLErrorResources.ER_COROUTINE_CO_EXIT, null));
                        }
                    }
                }
                try {
                    wait();
                } catch (InterruptedException e) {
                }
            }
        } else {
            throw new NoSuchMethodException(XMLMessages.createXMLMessage(XMLErrorResources.ER_COROUTINE_NOT_AVAIL, new Object[]{Integer.toString(toCoroutine)}));
        }
        return this.m_yield;
    }

    public synchronized void co_exit(int thisCoroutine) {
        this.m_activeIDs.clear(thisCoroutine);
        this.m_nextCoroutine = -1;
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
