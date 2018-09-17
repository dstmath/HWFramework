package sun.net.www.http;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Stack;

/* compiled from: KeepAliveCache */
class ClientVector extends Stack<KeepAliveEntry> {
    private static final long serialVersionUID = -8680532108106489459L;
    int nap;

    ClientVector(int nap) {
        this.nap = nap;
    }

    synchronized HttpClient get() {
        if (empty()) {
            return null;
        }
        HttpClient hc = null;
        long currentTime = System.currentTimeMillis();
        do {
            KeepAliveEntry e = (KeepAliveEntry) pop();
            if (currentTime - e.idleStartTime > ((long) this.nap)) {
                e.hc.closeServer();
            } else {
                hc = e.hc;
            }
            if (hc != null) {
                break;
            }
        } while (!empty());
        return hc;
    }

    synchronized void put(HttpClient h) {
        if (size() >= KeepAliveCache.getMaxConnections()) {
            h.closeServer();
        } else {
            push(new KeepAliveEntry(h, System.currentTimeMillis()));
        }
    }

    private void writeObject(ObjectOutputStream stream) throws IOException {
        throw new NotSerializableException();
    }

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        throw new NotSerializableException();
    }
}
