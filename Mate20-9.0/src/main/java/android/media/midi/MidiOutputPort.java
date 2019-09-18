package android.media.midi;

import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.util.Log;
import com.android.internal.midi.MidiDispatcher;
import dalvik.system.CloseGuard;
import java.io.Closeable;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import libcore.io.IoUtils;

public final class MidiOutputPort extends MidiSender implements Closeable {
    private static final String TAG = "MidiOutputPort";
    private IMidiDeviceServer mDeviceServer;
    /* access modifiers changed from: private */
    public final MidiDispatcher mDispatcher;
    private final CloseGuard mGuard;
    /* access modifiers changed from: private */
    public final FileInputStream mInputStream;
    private boolean mIsClosed;
    private final int mPortNumber;
    private final Thread mThread;
    private final IBinder mToken;

    MidiOutputPort(IMidiDeviceServer server, IBinder token, FileDescriptor fd, int portNumber) {
        this.mDispatcher = new MidiDispatcher();
        this.mGuard = CloseGuard.get();
        this.mThread = new Thread() {
            public void run() {
                byte[] buffer = new byte[1024];
                while (true) {
                    try {
                        int count = MidiOutputPort.this.mInputStream.read(buffer);
                        if (count >= 0) {
                            int packetType = MidiPortImpl.getPacketType(buffer, count);
                            switch (packetType) {
                                case 1:
                                    MidiOutputPort.this.mDispatcher.send(buffer, MidiPortImpl.getDataOffset(buffer, count), MidiPortImpl.getDataSize(buffer, count), MidiPortImpl.getPacketTimestamp(buffer, count));
                                    break;
                                case 2:
                                    MidiOutputPort.this.mDispatcher.flush();
                                    break;
                                default:
                                    Log.e(MidiOutputPort.TAG, "Unknown packet type " + packetType);
                                    break;
                            }
                        } else {
                            IoUtils.closeQuietly(MidiOutputPort.this.mInputStream);
                            return;
                        }
                    } catch (IOException e) {
                        Log.e(MidiOutputPort.TAG, "read failed", e);
                    } catch (Throwable th) {
                        IoUtils.closeQuietly(MidiOutputPort.this.mInputStream);
                        throw th;
                    }
                }
            }
        };
        this.mDeviceServer = server;
        this.mToken = token;
        this.mPortNumber = portNumber;
        this.mInputStream = new ParcelFileDescriptor.AutoCloseInputStream(new ParcelFileDescriptor(fd));
        this.mThread.start();
        this.mGuard.open("close");
    }

    MidiOutputPort(FileDescriptor fd, int portNumber) {
        this(null, null, fd, portNumber);
    }

    public final int getPortNumber() {
        return this.mPortNumber;
    }

    public void onConnect(MidiReceiver receiver) {
        this.mDispatcher.getSender().connect(receiver);
    }

    public void onDisconnect(MidiReceiver receiver) {
        this.mDispatcher.getSender().disconnect(receiver);
    }

    public void close() throws IOException {
        synchronized (this.mGuard) {
            if (!this.mIsClosed) {
                this.mGuard.close();
                this.mInputStream.close();
                if (this.mDeviceServer != null) {
                    try {
                        this.mDeviceServer.closePort(this.mToken);
                    } catch (RemoteException e) {
                        Log.e(TAG, "RemoteException in MidiOutputPort.close()");
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
