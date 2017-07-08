package android.media.midi;

import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.provider.DocumentsContract.Document;
import android.util.Log;
import dalvik.system.CloseGuard;
import java.io.Closeable;
import java.io.FileOutputStream;
import java.io.IOException;
import libcore.io.IoUtils;

public final class MidiInputPort extends MidiReceiver implements Closeable {
    private static final String TAG = "MidiInputPort";
    private final byte[] mBuffer;
    private IMidiDeviceServer mDeviceServer;
    private final CloseGuard mGuard;
    private boolean mIsClosed;
    private FileOutputStream mOutputStream;
    private ParcelFileDescriptor mParcelFileDescriptor;
    private final int mPortNumber;
    private final IBinder mToken;

    MidiInputPort(IMidiDeviceServer server, IBinder token, ParcelFileDescriptor pfd, int portNumber) {
        super(MidiPortImpl.MAX_PACKET_DATA_SIZE);
        this.mGuard = CloseGuard.get();
        this.mBuffer = new byte[Document.FLAG_SUPPORTS_REMOVE];
        this.mDeviceServer = server;
        this.mToken = token;
        this.mParcelFileDescriptor = pfd;
        this.mPortNumber = portNumber;
        this.mOutputStream = new FileOutputStream(pfd.getFileDescriptor());
        this.mGuard.open("close");
    }

    MidiInputPort(ParcelFileDescriptor pfd, int portNumber) {
        this(null, null, pfd, portNumber);
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

    ParcelFileDescriptor claimFileDescriptor() {
        synchronized (this.mGuard) {
            synchronized (this.mBuffer) {
                ParcelFileDescriptor pfd = this.mParcelFileDescriptor;
                if (pfd == null) {
                    return null;
                }
                IoUtils.closeQuietly(this.mOutputStream);
                this.mParcelFileDescriptor = null;
                this.mOutputStream = null;
                this.mIsClosed = true;
                return pfd;
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
                if (this.mParcelFileDescriptor != null) {
                    this.mParcelFileDescriptor.close();
                    this.mParcelFileDescriptor = null;
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
