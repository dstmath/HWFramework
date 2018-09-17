package com.huawei.internal.util;

import com.android.internal.util.MemInfoReader;

public class MemInfoReaderEx {
    private MemInfoReader mMemInfoReader = new MemInfoReader();

    public void readMemInfo() {
        this.mMemInfoReader.readMemInfo();
    }

    public long[] getRawInfo() {
        return this.mMemInfoReader.getRawInfo();
    }
}
