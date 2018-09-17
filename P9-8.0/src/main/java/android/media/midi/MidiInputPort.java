package android.media.midi;

import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import dalvik.system.CloseGuard;
import java.io.Closeable;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import libcore.io.IoUtils;

public final class MidiInputPort extends MidiReceiver implements Closeable {
    private static final String TAG = "MidiInputPort";
    private final byte[] mBuffer;
    private IMidiDeviceServer mDeviceServer;
    private FileDescriptor mFileDescriptor;
    private final CloseGuard mGuard;
    private boolean mIsClosed;
    private FileOutputStream mOutputStream;
    private final int mPortNumber;
    private final IBinder mToken;

    MidiInputPort(IMidiDeviceServer server, IBinder token, FileDescriptor fd, int portNumber) {
        super(MidiPortImpl.MAX_PACKET_DATA_SIZE);
        this.mGuard = CloseGuard.get();
        this.mBuffer = new byte[1024];
        this.mDeviceServer = server;
        this.mToken = token;
        this.mFileDescriptor = fd;
        this.mPortNumber = portNumber;
        this.mOutputStream = new FileOutputStream(fd);
        this.mGuard.open("close");
    }

    MidiInputPort(FileDescriptor fd, int portNumber) {
        this(null, null, fd, portNumber);
    }

    public final int getPortNumber() {
        return this.mPortNumber;
    }

    public void onSend(byte[] msg, int offset, int count, long timestamp) throws IOException {
        if (offset < 0 || count < 0 || offset + count > msg.length) {
            throw new IllegalArgumentException("offset or count out of range");
        } else if (count > MidiPortImpl.MAX_PACKET_DATA_SIZE) {
            throw new IllegalArgumentException("count exceeds max message size");
        } else {
            synchronized (this.mBuffer) {
                if (this.mOutputStream == null) {
                    throw new IOException("MidiInputPort is closed");
                }
                this.mOutputStream.write(this.mBuffer, 0, MidiPortImpl.packData(msg, offset, count, timestamp, this.mBuffer));
            }
        }
    }

    public void onFlush() throws IOException {
        synchronized (this.mBuffer) {
            if (this.mOutputStream == null) {
                throw new IOException("MidiInputPort is closed");
            }
            this.mOutputStream.write(this.mBuffer, 0, MidiPortImpl.packFlush(this.mBuffer));
        }
    }

    FileDescriptor claimFileDescriptor() {
        synchronized (this.mGuard) {
            synchronized (this.mBuffer) {
                FileDescriptor fd = this.mFileDescriptor;
                if (fd == null) {
                    return null;
                }
                IoUtils.closeQuietly(this.mOutputStream);
                this.mFileDescriptor = null;
                this.mOutputStream = null;
                this.mIsClosed = true;
                return fd;
            }
        }
    }

    IBinder getToken() {
        return this.mToken;
    }

    IMidiDeviceServer getDeviceServer() {
        return this.mDeviceServer;
    }

    public void close() throws IOException {
        synchronized (this.mGuard) {
            if (this.mIsClosed) {
                return;
            }
            this.mGuard.close();
            synchronized (this.mBuffer) {
                if (this.mFileDescriptor != null) {
                    IoUtils.closeQuietly(this.mFileDescriptor);
                    this.mFileDescriptor = null;
                }
                if (this.mOutputStream != null) {
                    this.mOutputStream.close();
                    this.mOutputStream = null;
                }
            }
            if (this.mDeviceServer != null) {
                try {
                    this.mDeviceServer.closePort(this.mToken);
                } catch (RemoteException e) {
                    Log.e(TAG, "RemoteException in MidiInputPort.close()");
                }
            }
            this.mIsClosed = true;
            return;
        }
    }

    protected void finalize() throws Throwable {
        try {
            this.mGuard.warnIfOpen();
            this.mDeviceServer = null;
            close();
        } finally {
            super.finalize();
        }
    }
}
