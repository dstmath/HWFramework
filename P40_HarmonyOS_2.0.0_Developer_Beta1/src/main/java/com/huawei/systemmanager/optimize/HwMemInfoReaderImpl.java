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

    @Override // com.huawei.systemmanager.optimize.IHwMemInfoReader
    public long getFreeSize() {
        MemInfoReader memInfoReader = this.mMemInfoReader;
        if (memInfoReader != null) {
            return memInfoReader.getFreeSize();
        }
        return 0;
    }

    @Override // com.huawei.systemmanager.optimize.IHwMemInfoReader
    public long getCachedSize() {
        MemInfoReader memInfoReader = this.mMemInfoReader;
        if (memInfoReader != null) {
            return memInfoReader.getCachedSize();
        }
        return 0;
    }

    @Override // com.huawei.systemmanager.optimize.IHwMemInfoReader
    public void readMemInfo() {
        MemInfoReader memInfoReader = this.mMemInfoReader;
        if (memInfoReader != null) {
            memInfoReader.readMemInfo();
        }
    }
}
