package android.bluetooth;

import java.io.IOException;
import java.io.InputStream;

final class BluetoothInputStream extends InputStream {
    private BluetoothSocket mSocket;

    BluetoothInputStream(BluetoothSocket s) {
        this.mSocket = s;
    }

    public int available() throws IOException {
        return this.mSocket.available();
    }

    public void close() throws IOException {
        this.mSocket.close();
    }

    public int read() throws IOException {
        byte[] b = new byte[1];
        if (this.mSocket.read(b, 0, 1) == 1) {
            return b[0] & 255;
        }
        return -1;
    }

    public int read(byte[] b, int offset, int length) throws IOException {
        if (b == null) {
            throw new NullPointerException("byte array is null");
        } else if ((offset | length) >= 0 && length <= b.length - offset) {
            return this.mSocket.read(b, offset, length);
        } else {
            throw new ArrayIndexOutOfBoundsException("invalid offset or length");
        }
    }
}
