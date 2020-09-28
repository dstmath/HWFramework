package android.bluetooth;

import java.io.IOException;
import java.io.OutputStream;

/* access modifiers changed from: package-private */
public final class BluetoothOutputStream extends OutputStream {
    private BluetoothSocket mSocket;

    BluetoothOutputStream(BluetoothSocket s) {
        this.mSocket = s;
    }

    @Override // java.io.OutputStream, java.io.Closeable, java.lang.AutoCloseable
    public void close() throws IOException {
        this.mSocket.close();
    }

    @Override // java.io.OutputStream
    public void write(int oneByte) throws IOException {
        this.mSocket.write(new byte[]{(byte) oneByte}, 0, 1);
    }

    @Override // java.io.OutputStream
    public void write(byte[] b, int offset, int count) throws IOException {
        if (b == null) {
            throw new NullPointerException("buffer is null");
        } else if ((offset | count) < 0 || count > b.length - offset) {
            throw new IndexOutOfBoundsException("invalid offset or length");
        } else {
            this.mSocket.write(b, offset, count);
        }
    }

    @Override // java.io.OutputStream, java.io.Flushable
    public void flush() throws IOException {
        this.mSocket.flush();
    }
}
