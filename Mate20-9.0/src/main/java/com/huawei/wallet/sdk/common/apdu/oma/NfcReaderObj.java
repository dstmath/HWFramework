package com.huawei.wallet.sdk.common.apdu.oma;

import android.se.omapi.Reader;

public class NfcReaderObj {
    private int idx;
    private Reader reader;

    public NfcReaderObj(int idx2, Reader reader2) {
        this.idx = idx2;
        this.reader = reader2;
    }

    public int getIdx() {
        return this.idx;
    }

    public Reader getReader() {
        return this.reader;
    }
}
