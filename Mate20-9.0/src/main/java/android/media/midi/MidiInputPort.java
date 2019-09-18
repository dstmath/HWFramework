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
        super(1015);
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
        } else if (count <= 1015) {
            synchronized (this.mBuffer) {
                if (this.mOutputStream != null) {
                    this.mOutputStream.write(this.mBuffer, 0, MidiPortImpl.packData(msg, offset, count, timestamp, this.mBuffer));
                } else {
                    throw new IOException("MidiInputPort is closed");
                }
            }
        } else {
            throw new IllegalArgumentException("count exceeds max message size");
        }
    }

    public void onFlush() throws IOException {
        synchronized (this.mBuffer) {
            if (this.mOutputStream != null) {
                this.mOutputStream.write(this.mBuffer, 0, MidiPortImpl.packFlush(this.mBuffer));
            } else {
                throw new IOException("MidiInputPort is closed");
            }
        }
    }

    /* access modifiers changed from: package-private */
    public FileDescriptor claimFileDescriptor() {
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

    /* access modifiers changed from: package-private */
    public IBinder getToken() {
        return this.mToken;
    }

    /* access modifiers changed from: package-private */
    public IMidiDeviceServer getDeviceServer() {
        return this.mDeviceServer;
    }

    public void close() throws IOException {
        synchronized (this.mGuard) {
            if (!this.mIsClosed) {
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
            }
        }
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        try {
            if (this.mGuard != null) {
                this.mGuard.warnIfOpen();
            }
            this.mDeviceServer = null;
            close();
        } finally {
            super.finalize();
        }
    }
}
