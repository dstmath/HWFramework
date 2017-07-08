package android.media.midi;

import android.os.Binder;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.os.Process;
import android.os.RemoteException;
import android.util.Log;
import dalvik.system.CloseGuard;
import java.io.Closeable;
import java.io.IOException;
import libcore.io.IoUtils;

public final class MidiDevice implements Closeable {
    private static final String TAG = "MidiDevice";
    private final IBinder mClientToken;
    private final MidiDeviceInfo mDeviceInfo;
    private final IMidiDeviceServer mDeviceServer;
    private final IBinder mDeviceToken;
    private final CloseGuard mGuard;
    private boolean mIsDeviceClosed;
    private final IMidiManager mMidiManager;

    public class MidiConnection implements Closeable {
        private final CloseGuard mGuard;
        private final IMidiDeviceServer mInputPortDeviceServer;
        private final IBinder mInputPortToken;
        private boolean mIsClosed;
        private final IBinder mOutputPortToken;

        MidiConnection(IBinder outputPortToken, MidiInputPort inputPort) {
            this.mGuard = CloseGuard.get();
            this.mInputPortDeviceServer = inputPort.getDeviceServer();
            this.mInputPortToken = inputPort.getToken();
            this.mOutputPortToken = outputPortToken;
            this.mGuard.open("close");
        }

        public void close() throws IOException {
            synchronized (this.mGuard) {
                if (this.mIsClosed) {
                    return;
                }
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

        protected void finalize() throws Throwable {
            try {
                this.mGuard.warnIfOpen();
                close();
            } finally {
                super.finalize();
            }
        }
    }

    MidiDevice(MidiDeviceInfo deviceInfo, IMidiDeviceServer server, IMidiManager midiManager, IBinder clientToken, IBinder deviceToken) {
        this.mGuard = CloseGuard.get();
        this.mDeviceInfo = deviceInfo;
        this.mDeviceServer = server;
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
            ParcelFileDescriptor pfd = this.mDeviceServer.openInputPort(token, portNumber);
            if (pfd == null) {
                return null;
            }
            return new MidiInputPort(this.mDeviceServer, token, pfd, portNumber);
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
            ParcelFileDescriptor pfd = this.mDeviceServer.openOutputPort(token, portNumber);
            if (pfd == null) {
                return null;
            }
            return new MidiOutputPort(this.mDeviceServer, token, pfd, portNumber);
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException in openOutputPort");
            return null;
        }
    }

    public MidiConnection connectPorts(MidiInputPort inputPort, int outputPortNumber) {
        if (outputPortNumber < 0 || outputPortNumber >= this.mDeviceInfo.getOutputPortCount()) {
            throw new IllegalArgumentException("outputPortNumber out of range");
        } else if (this.mIsDeviceClosed) {
            return null;
        } else {
            ParcelFileDescriptor pfd = inputPort.claimFileDescriptor();
            if (pfd == null) {
                return null;
            }
            try {
                IBinder token = new Binder();
                if (this.mDeviceServer.connectPorts(token, pfd, outputPortNumber) != Process.myPid()) {
                    IoUtils.closeQuietly(pfd);
                }
                return new MidiConnection(token, inputPort);
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException in connectPorts");
                return null;
            }
        }
    }

    public void close() throws IOException {
        synchronized (this.mGuard) {
            if (!this.mIsDeviceClosed) {
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

    protected void finalize() throws Throwable {
        try {
            this.mGuard.warnIfOpen();
            close();
        } finally {
            super.finalize();
        }
    }

    public String toString() {
        return "MidiDevice: " + this.mDeviceInfo.toString();
    }
}
