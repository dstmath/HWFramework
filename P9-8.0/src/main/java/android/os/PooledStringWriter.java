package android.os;

import java.util.HashMap;

public class PooledStringWriter {
    private int mNext;
    private final Parcel mOut;
    private final HashMap<String, Integer> mPool = new HashMap();
    private int mStart;

    public PooledStringWriter(Parcel out) {
        this.mOut = out;
        this.mStart = out.dataPosition();
        out.writeInt(0);
    }

    public void writeString(String str) {
        Integer cur = (Integer) this.mPool.get(str);
        if (cur != null) {
            this.mOut.writeInt(cur.intValue());
            return;
        }
        this.mPool.put(str, Integer.valueOf(this.mNext));
        this.mOut.writeInt(-(this.mNext + 1));
        this.mOut.writeString(str);
        this.mNext++;
    }

    public int getStringCount() {
        return this.mPool.size();
    }

    public void finish() {
        int pos = this.mOut.dataPosition();
        this.mOut.setDataPosition(this.mStart);
        this.mOut.writeInt(this.mNext);
        this.mOut.setDataPosition(pos);
    }
}
