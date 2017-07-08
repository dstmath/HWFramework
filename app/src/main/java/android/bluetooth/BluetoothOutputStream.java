package android.bluetooth;

import java.io.IOException;
import java.io.OutputStream;

final class BluetoothOutputStream extends OutputStream {
    private BluetoothSocket mSocket;

    BluetoothOutputStream(BluetoothSocket s) {
        this.mSocket = s;
    }

    public void close() throws IOException {
        this.mSocket.close();
    }

    public void write(int oneByte) throws IOException {
        this.mSocket.write(new byte[]{(byte) oneByte}, 0, 1);
    }

    public void write(byte[] b, int offset, int count) throws IOException {
        if (b == null) {
            throw new NullPointerException("buffer is null");
        } else if ((offset | count) < 0 || count > b.length - offset) {
            throw new IndexOutOfBoundsException("invalid offset or length");
        } else {
            this.mSocket.write(b, offset, count);
        }
    }

    public void flush() throws IOException {
        this.mSocket.flush();
    }
}
