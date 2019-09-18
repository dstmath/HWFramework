package org.bouncycastle.cms;

import java.io.IOException;
import java.io.OutputStream;

class NullOutputStream extends OutputStream {
    NullOutputStream() {
    }

    public void write(int i) throws IOException {
    }

    public void write(byte[] bArr) throws IOException {
    }

    public void write(byte[] bArr, int i, int i2) throws IOException {
    }
}
