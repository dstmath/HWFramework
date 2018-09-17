package android.widget.sr;

import android.util.Log;

public class SRMemoryRecorder {
    private static final int MAX_SIZE = 53477376;
    private static final String SR_TAG = "SuperResolution";
    private int mMemoryCount = 0;

    public synchronized int getMemoryCount() {
        return this.mMemoryCount;
    }

    public synchronized boolean enoughRoomForSize(int size) {
        boolean ret;
        ret = this.mMemoryCount + size < MAX_SIZE;
        Log.d(SR_TAG, "enoughRoomForSize: size = " + size + "  mem = " + this.mMemoryCount + "MAX_SIZE = " + MAX_SIZE + " ret = " + ret);
        return ret;
    }

    public synchronized void add(int size) {
        Log.d(SR_TAG, "add: size = " + size + " before add mem = " + this.mMemoryCount);
        this.mMemoryCount += size;
    }

    public synchronized void remove(int size) {
        Log.d(SR_TAG, "remove: size = " + size + " before remove mem = " + this.mMemoryCount);
        this.mMemoryCount -= size;
    }
}
