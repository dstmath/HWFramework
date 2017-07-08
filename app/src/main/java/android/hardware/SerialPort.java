package android.hardware;

import android.os.ParcelFileDescriptor;
import java.io.FileDescriptor;
import java.io.IOException;
import java.nio.ByteBuffer;

public class SerialPort {
    private static final String TAG = "SerialPort";
    private ParcelFileDescriptor mFileDescriptor;
    private final String mName;
    private int mNativeContext;

    private native void native_close();

    private native void native_open(FileDescriptor fileDescriptor, int i) throws IOException;

    private native int native_read_array(byte[] bArr, int i) throws IOException;

    private native int native_read_direct(ByteBuffer byteBuffer, int i) throws IOException;

    private native void native_send_break();

    private native void native_write_array(byte[] bArr, int i) throws IOException;

    private native void native_write_direct(ByteBuffer byteBuffer, int i) throws IOException;

    public SerialPort(String name) {
        this.mName = name;
    }

    public void open(ParcelFileDescriptor pfd, int speed) throws IOException {
        native_open(pfd.getFileDescriptor(), speed);
        this.mFileDescriptor = pfd;
    }

    public void close() throws IOException {
        if (this.mFileDescriptor != null) {
            this.mFileDescriptor.close();
            this.mFileDescriptor = null;
        }
        native_close();
    }

    public String getName() {
        return this.mName;
    }

    public int read(ByteBuffer buffer) throws IOException {
        if (buffer.isDirect()) {
            return native_read_direct(buffer, buffer.remaining());
        }
        if (buffer.hasArray()) {
            return native_read_array(buffer.array(), buffer.remaining());
        }
        throw new IllegalArgumentException("buffer is not direct and has no array");
    }

    public void write(ByteBuffer buffer, int length) throws IOException {
        if (buffer.isDirect()) {
            native_write_direct(buffer, length);
        } else if (buffer.hasArray()) {
            native_write_array(buffer.array(), length);
        } else {
            throw new IllegalArgumentException("buffer is not direct and has no array");
        }
    }

    public void sendBreak() {
        native_send_break();
    }
}
