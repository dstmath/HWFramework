package com.huawei.android.internal.util;

import com.android.internal.util.MemInfoReader;

public class MemInfoReaderExt {
    private MemInfoReader mMemInfoReader = new MemInfoReader();

    public void readMemInfo() {
        this.mMemInfoReader.readMemInfo();
    }

    public long[] getRawInfo() {
        return this.mMemInfoReader.getRawInfo();
    }

    public long getTotalSize() {
        return this.mMemInfoReader.getTotalSize();
    }
}
