package android.media.midi;

import android.os.Binder;
import android.os.IBinder;
import android.os.Process;
import android.os.RemoteException;
import android.util.Log;
import dalvik.system.CloseGuard;
import java.io.Closeable;
import java.io.FileDescriptor;
import java.io.IOException;
import libcore.io.IoUtils;

public final class MidiDevice implements Closeable {
    private static final String TAG = "MidiDevice";
    private final IBinder mClientToken;
    private final MidiDeviceInfo mDeviceInfo;
    private final IMidiDeviceServer mDeviceServer;
    private final IBinder mDeviceServerBinder;
    private final IBinder mDeviceToken;
    private final CloseGuard mGuard = CloseGuard.get();
    private boolean mIsDeviceClosed;
    private final IMidiManager mMidiManager;
    private long mNativeHandle;

    public class MidiConnection implements Closeable {
        private final CloseGuard mGuard = CloseGuard.get();
        private final IMidiDeviceServer mInputPortDeviceServer;
        private final IBinder mInputPortToken;
        private boolean mIsClosed;
        private final IBinder mOutputPortToken;

        MidiConnection(IBinder outputPortToken, MidiInputPort inputPort) {
            this.mInputPortDeviceServer = inputPort.getDeviceServer();
            this.mInputPortToken = inputPort.getToken();
            this.mOutputPortToken = outputPortToken;
            this.mGuard.open("close");
        }

        @Override // java.io.Closeable, java.lang.AutoCloseable
        public void close() throws IOException {
            synchronized (this.mGuard) {
                if (!this.mIsClosed) {
                    this.mGuard.close();
                    try {
                        this.mInputPortDeviceServer.closePort(this.mInputPortToken);
                        MidiDevice.this.mDeviceServer.closePort(this.mOutputPortToken);
                    } catch (RemoteException e) {
                        Log.e(MidiDevice.TAG, "RemoteException in MidiConnection.close");
                    }
                    this.mIsClosed = true;
                }
            }
        }

        /* access modifiers changed from: protected */
        @Override // java.lang.Object
        public void finalize() throws Throwable {
            try {
                if (this.mGuard != null) {
                    this.mGuard.warnIfOpen();
                }
                close();
            } finally {
                super.finalize();
            }
        }
    }

    MidiDevice(MidiDeviceInfo deviceInfo, IMidiDeviceServer server, IMidiManager midiManager, IBinder clientToken, IBinder deviceToken) {
        this.mDeviceInfo = deviceInfo;
        this.mDeviceServer = server;
        this.mDeviceServerBinder = this.mDeviceServer.asBinder();
        this.mMidiManager = midiManager;
        this.mClientToken = clientToken;
        this.mDeviceToken = deviceToken;
        this.mGuard.open("close");
    }

    public MidiDeviceInfo getInfo() {
        return this.mDeviceInfo;
    }

    public MidiInputPort openInputPort(int portNumber) {
        if (this.mIsDeviceClosed) {
            return null;
        }
        try {
            IBinder token = new Binder();
            FileDescriptor fd = this.mDeviceServer.openInputPort(token, portNumber);
            if (fd == null) {
                return null;
            }
            return new MidiInputPort(this.mDeviceServer, token, fd, portNumber);
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException in openInputPort");
            return null;
        }
    }

    public MidiOutputPort openOutputPort(int portNumber) {
        if (this.mIsDeviceClosed) {
            return null;
        }
        try {
            IBinder token = new Binder();
            FileDescriptor fd = this.mDeviceServer.openOutputPort(token, portNumber);
            if (fd == null) {
                return null;
            }
            return new MidiOutputPort(this.mDeviceServer, token, fd, portNumber);
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException in openOutputPort");
            return null;
        }
    }

    public MidiConnection connectPorts(MidiInputPort inputPort, int outputPortNumber) {
        FileDescriptor fd;
        if (outputPortNumber < 0 || outputPortNumber >= this.mDeviceInfo.getOutputPortCount()) {
            throw new IllegalArgumentException("outputPortNumber out of range");
        } else if (this.mIsDeviceClosed || (fd = inputPort.claimFileDescriptor()) == null) {
            return null;
        } else {
            try {
                IBinder token = new Binder();
                if (this.mDeviceServer.connectPorts(token, fd, outputPortNumber) != Process.myPid()) {
                    IoUtils.closeQuietly(fd);
                }
                return new MidiConnection(token, inputPort);
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException in connectPorts");
                return null;
            }
        }
    }

    @Override // java.io.Closeable, java.lang.AutoCloseable
    public void close() throws IOException {
        synchronized (this.mGuard) {
            if (this.mNativeHandle != 0) {
                Log.w(TAG, "MidiDevice#close() called while there is an outstanding native client 0x" + Long.toHexString(this.mNativeHandle));
            }
            if (!this.mIsDeviceClosed && this.mNativeHandle == 0) {
                this.mGuard.close();
                this.mIsDeviceClosed = true;
                try {
                    this.mMidiManager.closeDevice(this.mClientToken, this.mDeviceToken);
                } catch (RemoteException e) {
                    Log.e(TAG, "RemoteException in closeDevice");
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // java.lang.Object
    public void finalize() throws Throwable {
        try {
            if (this.mGuard != null) {
                this.mGuard.warnIfOpen();
            }
            close();
        } finally {
            super.finalize();
        }
    }

    public String toString() {
        return "MidiDevice: " + this.mDeviceInfo.toString();
    }
}
