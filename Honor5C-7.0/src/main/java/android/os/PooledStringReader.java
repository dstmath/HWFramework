package android.os;

public class PooledStringReader {
    private final Parcel mIn;
    private final String[] mPool;

    public PooledStringReader(Parcel in) {
        this.mIn = in;
        this.mPool = new String[in.readInt()];
    }

    public int getStringCount() {
        return this.mPool.length;
    }

    public String readString() {
        int idx = this.mIn.readInt();
        if (idx >= 0) {
            return this.mPool[idx];
        }
        idx = (-idx) - 1;
        String str = this.mIn.readString();
        this.mPool[idx] = str;
        return str;
    }
}
