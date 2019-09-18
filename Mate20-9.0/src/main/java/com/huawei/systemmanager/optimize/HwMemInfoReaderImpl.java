package com.huawei.systemmanager.optimize;

import com.android.internal.util.MemInfoReader;

class HwMemInfoReaderImpl implements IHwMemInfoReader {
    private static IHwMemInfoReader sInstance;
    private MemInfoReader mMemInfoReader = new MemInfoReader();

    private HwMemInfoReaderImpl() {
    }

    public static synchronized IHwMemInfoReader getIsntance() {
        synchronized (HwMemInfoReaderImpl.class) {
            HwMemInfoReaderImpl tmp = new HwMemInfoReaderImpl();
            if (tmp.mMemInfoReader == null) {
                return null;
            }
            return tmp;
        }
    }

    public long getFreeSize() {
        if (this.mMemInfoReader != null) {
            return this.mMemInfoReader.getFreeSize();
        }
        return 0;
    }

    public long getCachedSize() {
        if (this.mMemInfoReader != null) {
            return this.mMemInfoReader.getCachedSize();
        }
        return 0;
    }

    public void readMemInfo() {
        if (this.mMemInfoReader != null) {
            this.mMemInfoReader.readMemInfo();
        }
    }
}
