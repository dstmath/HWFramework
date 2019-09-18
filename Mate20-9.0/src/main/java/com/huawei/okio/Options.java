package com.huawei.okio;

import java.util.AbstractList;
import java.util.RandomAccess;

public final class Options extends AbstractList<ByteString> implements RandomAccess {
    final ByteString[] byteStrings;

    private Options(ByteString[] byteStrings2) {
        this.byteStrings = byteStrings2;
    }

    public static Options of(ByteString... byteStrings2) {
        return new Options((ByteString[]) byteStrings2.clone());
    }

    public ByteString get(int i) {
        return this.byteStrings[i];
    }

    public final int size() {
        return this.byteStrings.length;
    }
}
