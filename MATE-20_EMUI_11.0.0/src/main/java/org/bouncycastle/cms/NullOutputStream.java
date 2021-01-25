package org.bouncycastle.cms;

import java.io.IOException;
import java.io.OutputStream;

/* access modifiers changed from: package-private */
public class NullOutputStream extends OutputStream {
    NullOutputStream() {
    }

    @Override // java.io.OutputStream
    public void write(int i) throws IOException {
    }

    @Override // java.io.OutputStream
    public void write(byte[] bArr) throws IOException {
    }

    @Override // java.io.OutputStream
    public void write(byte[] bArr, int i, int i2) throws IOException {
    }
}
