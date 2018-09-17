package com.android.org.bouncycastle.crypto.io;

import com.android.org.bouncycastle.crypto.Mac;
import java.io.IOException;
import java.io.OutputStream;

public class MacOutputStream extends OutputStream {
    protected Mac mac;

    public MacOutputStream(Mac mac) {
        this.mac = mac;
    }

    public void write(int b) throws IOException {
        this.mac.update((byte) b);
    }

    public void write(byte[] b, int off, int len) throws IOException {
        this.mac.update(b, off, len);
    }

    public byte[] getMac() {
        byte[] res = new byte[this.mac.getMacSize()];
        this.mac.doFinal(res, 0);
        return res;
    }
}
