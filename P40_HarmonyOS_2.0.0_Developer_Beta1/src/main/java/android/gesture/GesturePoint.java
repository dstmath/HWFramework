package android.gesture;

import java.io.DataInputStream;
import java.io.IOException;

public class GesturePoint {
    public final long timestamp;
    public final float x;
    public final float y;

    public GesturePoint(float x2, float y2, long t) {
        this.x = x2;
        this.y = y2;
        this.timestamp = t;
    }

    static GesturePoint deserialize(DataInputStream in) throws IOException {
        return new GesturePoint(in.readFloat(), in.readFloat(), in.readLong());
    }

    public Object clone() {
        return new GesturePoint(this.x, this.y, this.timestamp);
    }
}
