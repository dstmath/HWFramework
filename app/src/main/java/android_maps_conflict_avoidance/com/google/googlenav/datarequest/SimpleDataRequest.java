package android_maps_conflict_avoidance.com.google.googlenav.datarequest;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class SimpleDataRequest extends BaseDataRequest {
    private final byte[] data;
    private final boolean foreground;
    private final boolean immediate;
    private boolean received;
    private final int requestType;
    private boolean sent;
    private final Object waitObject;

    public SimpleDataRequest(int requestType, byte[] data, boolean immediate, boolean foreground) {
        this(requestType, data, immediate, foreground, null);
    }

    public SimpleDataRequest(int requestType, byte[] data, boolean immediate, boolean foreground, Object waitObject) {
        this.requestType = requestType;
        this.data = data;
        this.immediate = immediate;
        this.foreground = foreground;
        this.waitObject = waitObject;
    }

    public int getRequestType() {
        return this.requestType;
    }

    public boolean isImmediate() {
        return this.immediate;
    }

    public boolean isForeground() {
        return this.foreground;
    }

    public void writeRequestData(DataOutput dos) throws IOException {
        dos.write(this.data);
        this.sent = true;
        if (this.waitObject != null) {
            synchronized (this.waitObject) {
                this.waitObject.notifyAll();
            }
        }
    }

    public boolean readResponseData(DataInput dis) throws IOException {
        this.received = true;
        if (this.waitObject != null) {
            synchronized (this.waitObject) {
                this.waitObject.notifyAll();
            }
        }
        return true;
    }
}
