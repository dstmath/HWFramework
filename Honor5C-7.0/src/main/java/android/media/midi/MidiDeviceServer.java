package android.media.midi;

import android.media.midi.IMidiDeviceServer.Stub;
import android.os.Binder;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.ParcelFileDescriptor;
import android.os.Process;
import android.os.RemoteException;
import android.system.OsConstants;
import android.util.Log;
import com.android.internal.midi.MidiDispatcher;
import dalvik.system.CloseGuard;
import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import libcore.io.IoUtils;

public final class MidiDeviceServer implements Closeable {
    private static final String TAG = "MidiDeviceServer";
    private final Callback mCallback;
    private MidiDeviceInfo mDeviceInfo;
    private final CloseGuard mGuard;
    private final int mInputPortCount;
    private final boolean[] mInputPortOpen;
    private final MidiOutputPort[] mInputPortOutputPorts;
    private final MidiReceiver[] mInputPortReceivers;
    private final CopyOnWriteArrayList<MidiInputPort> mInputPorts;
    private boolean mIsClosed;
    private final IMidiManager mMidiManager;
    private final int mOutputPortCount;
    private MidiDispatcher[] mOutputPortDispatchers;
    private final int[] mOutputPortOpenCount;
    private final HashMap<IBinder, PortClient> mPortClients;
    private final IMidiDeviceServer mServer;

    public interface Callback {
        void onClose();

        void onDeviceStatusChanged(MidiDeviceServer midiDeviceServer, MidiDeviceStatus midiDeviceStatus);
    }

    private abstract class PortClient implements DeathRecipient {
        final IBinder mToken;

        abstract void close();

        PortClient(IBinder token) {
            this.mToken = token;
            try {
                token.linkToDeath(this, 0);
            } catch (RemoteException e) {
                close();
            }
        }

        public void binderDied() {
            close();
        }
    }

    private class InputPortClient extends PortClient {
        private final MidiOutputPort mOutputPort;

        InputPortClient(IBinder token, MidiOutputPort outputPort) {
            super(token);
            this.mOutputPort = outputPort;
        }

        void close() {
            this.mToken.unlinkToDeath(this, 0);
            synchronized (MidiDeviceServer.this.mInputPortOutputPorts) {
                int portNumber = this.mOutputPort.getPortNumber();
                MidiDeviceServer.this.mInputPortOutputPorts[portNumber] = null;
                MidiDeviceServer.this.mInputPortOpen[portNumber] = false;
                MidiDeviceServer.this.updateDeviceStatus();
            }
            IoUtils.closeQuietly(this.mOutputPort);
        }
    }

    private class OutputPortClient extends PortClient {
        private final MidiInputPort mInputPort;

        OutputPortClient(IBinder token, MidiInputPort inputPort) {
            super(token);
            this.mInputPort = inputPort;
        }

        void close() {
            this.mToken.unlinkToDeath(this, 0);
            int portNumber = this.mInputPort.getPortNumber();
            MidiDispatcher dispatcher = MidiDeviceServer.this.mOutputPortDispatchers[portNumber];
            synchronized (dispatcher) {
                dispatcher.getSender().disconnect(this.mInputPort);
                MidiDeviceServer.this.mOutputPortOpenCount[portNumber] = dispatcher.getReceiverCount();
                MidiDeviceServer.this.updateDeviceStatus();
            }
            MidiDeviceServer.this.mInputPorts.remove(this.mInputPort);
            IoUtils.closeQuietly(this.mInputPort);
        }
    }

    MidiDeviceServer(IMidiManager midiManager, MidiReceiver[] inputPortReceivers, int numOutputPorts, Callback callback) {
        this.mInputPorts = new CopyOnWriteArrayList();
        this.mGuard = CloseGuard.get();
        this.mPortClients = new HashMap();
        this.mServer = new Stub() {
            public ParcelFileDescriptor openInputPort(IBinder token, int portNumber) {
                if (MidiDeviceServer.this.mDeviceInfo.isPrivate() && Binder.getCallingUid() != Process.myUid()) {
                    throw new SecurityException("Can't access private device from different UID");
                } else if (portNumber < 0 || portNumber >= MidiDeviceServer.this.mInputPortCount) {
                    Log.e(MidiDeviceServer.TAG, "portNumber out of range in openInputPort: " + portNumber);
                    return null;
                } else {
                    synchronized (MidiDeviceServer.this.mInputPortOutputPorts) {
                        if (MidiDeviceServer.this.mInputPortOutputPorts[portNumber] != null) {
                            Log.d(MidiDeviceServer.TAG, "port " + portNumber + " already open");
                            return null;
                        }
                        try {
                            ParcelFileDescriptor[] pair = ParcelFileDescriptor.createSocketPair(OsConstants.SOCK_SEQPACKET);
                            MidiOutputPort outputPort = new MidiOutputPort(pair[0], portNumber);
                            MidiDeviceServer.this.mInputPortOutputPorts[portNumber] = outputPort;
                            outputPort.connect(MidiDeviceServer.this.mInputPortReceivers[portNumber]);
                            InputPortClient client = new InputPortClient(token, outputPort);
                            synchronized (MidiDeviceServer.this.mPortClients) {
                                MidiDeviceServer.this.mPortClients.put(token, client);
                            }
                            MidiDeviceServer.this.mInputPortOpen[portNumber] = true;
                            MidiDeviceServer.this.updateDeviceStatus();
                            ParcelFileDescriptor parcelFileDescriptor = pair[1];
                            return parcelFileDescriptor;
                        } catch (IOException e) {
                            Log.e(MidiDeviceServer.TAG, "unable to create ParcelFileDescriptors in openInputPort");
                            return null;
                        }
                    }
                }
            }

            public ParcelFileDescriptor openOutputPort(IBinder token, int portNumber) {
                if (MidiDeviceServer.this.mDeviceInfo.isPrivate() && Binder.getCallingUid() != Process.myUid()) {
                    throw new SecurityException("Can't access private device from different UID");
                } else if (portNumber < 0 || portNumber >= MidiDeviceServer.this.mOutputPortCount) {
                    Log.e(MidiDeviceServer.TAG, "portNumber out of range in openOutputPort: " + portNumber);
                    return null;
                } else {
                    try {
                        ParcelFileDescriptor[] pair = ParcelFileDescriptor.createSocketPair(OsConstants.SOCK_SEQPACKET);
                        MidiInputPort inputPort = new MidiInputPort(pair[0], portNumber);
                        MidiDispatcher dispatcher = MidiDeviceServer.this.mOutputPortDispatchers[portNumber];
                        synchronized (dispatcher) {
                            dispatcher.getSender().connect(inputPort);
                            MidiDeviceServer.this.mOutputPortOpenCount[portNumber] = dispatcher.getReceiverCount();
                            MidiDeviceServer.this.updateDeviceStatus();
                        }
                        MidiDeviceServer.this.mInputPorts.add(inputPort);
                        OutputPortClient client = new OutputPortClient(token, inputPort);
                        synchronized (MidiDeviceServer.this.mPortClients) {
                            MidiDeviceServer.this.mPortClients.put(token, client);
                        }
                        return pair[1];
                    } catch (IOException e) {
                        Log.e(MidiDeviceServer.TAG, "unable to create ParcelFileDescriptors in openOutputPort");
                        return null;
                    }
                }
            }

            public void closePort(IBinder token) {
                synchronized (MidiDeviceServer.this.mPortClients) {
                    PortClient client = (PortClient) MidiDeviceServer.this.mPortClients.remove(token);
                    if (client != null) {
                        client.close();
                    }
                }
            }

            public void closeDevice() {
                if (MidiDeviceServer.this.mCallback != null) {
                    MidiDeviceServer.this.mCallback.onClose();
                }
                IoUtils.closeQuietly(MidiDeviceServer.this);
            }

            public int connectPorts(IBinder token, ParcelFileDescriptor pfd, int outputPortNumber) {
                MidiInputPort inputPort = new MidiInputPort(pfd, outputPortNumber);
                MidiDispatcher dispatcher = MidiDeviceServer.this.mOutputPortDispatchers[outputPortNumber];
                synchronized (dispatcher) {
                    dispatcher.getSender().connect(inputPort);
                    MidiDeviceServer.this.mOutputPortOpenCount[outputPortNumber] = dispatcher.getReceiverCount();
                    MidiDeviceServer.this.updateDeviceStatus();
                }
                MidiDeviceServer.this.mInputPorts.add(inputPort);
                OutputPortClient client = new OutputPortClient(token, inputPort);
                synchronized (MidiDeviceServer.this.mPortClients) {
                    MidiDeviceServer.this.mPortClients.put(token, client);
                }
                return Process.myPid();
            }

            public MidiDeviceInfo getDeviceInfo() {
                return MidiDeviceServer.this.mDeviceInfo;
            }

            public void setDeviceInfo(MidiDeviceInfo deviceInfo) {
                if (Binder.getCallingUid() != Process.SYSTEM_UID) {
                    throw new SecurityException("setDeviceInfo should only be called by MidiService");
                } else if (MidiDeviceServer.this.mDeviceInfo != null) {
                    throw new IllegalStateException("setDeviceInfo should only be called once");
                } else {
                    MidiDeviceServer.this.mDeviceInfo = deviceInfo;
                }
            }
        };
        this.mMidiManager = midiManager;
        this.mInputPortReceivers = inputPortReceivers;
        this.mInputPortCount = inputPortReceivers.length;
        this.mOutputPortCount = numOutputPorts;
        this.mCallback = callback;
        this.mInputPortOutputPorts = new MidiOutputPort[this.mInputPortCount];
        this.mOutputPortDispatchers = new MidiDispatcher[numOutputPorts];
        for (int i = 0; i < numOutputPorts; i++) {
            this.mOutputPortDispatchers[i] = new MidiDispatcher();
        }
        this.mInputPortOpen = new boolean[this.mInputPortCount];
        this.mOutputPortOpenCount = new int[numOutputPorts];
        this.mGuard.open("close");
    }

    MidiDeviceServer(IMidiManager midiManager, MidiReceiver[] inputPortReceivers, MidiDeviceInfo deviceInfo, Callback callback) {
        this(midiManager, inputPortReceivers, deviceInfo.getOutputPortCount(), callback);
        this.mDeviceInfo = deviceInfo;
    }

    IMidiDeviceServer getBinderInterface() {
        return this.mServer;
    }

    public IBinder asBinder() {
        return this.mServer.asBinder();
    }

    private void updateDeviceStatus() {
        long identityToken = Binder.clearCallingIdentity();
        MidiDeviceStatus status = new MidiDeviceStatus(this.mDeviceInfo, this.mInputPortOpen, this.mOutputPortOpenCount);
        if (this.mCallback != null) {
            this.mCallback.onDeviceStatusChanged(this, status);
        }
        try {
            this.mMidiManager.setDeviceStatus(this.mServer, status);
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException in updateDeviceStatus");
        } finally {
            Binder.restoreCallingIdentity(identityToken);
        }
    }

    public void close() throws IOException {
        synchronized (this.mGuard) {
            if (this.mIsClosed) {
                return;
            }
            this.mGuard.close();
            for (int i = 0; i < this.mInputPortCount; i++) {
                MidiOutputPort outputPort = this.mInputPortOutputPorts[i];
                if (outputPort != null) {
                    IoUtils.closeQuietly(outputPort);
                    this.mInputPortOutputPorts[i] = null;
                }
            }
            for (MidiInputPort inputPort : this.mInputPorts) {
                IoUtils.closeQuietly(inputPort);
            }
            this.mInputPorts.clear();
            try {
                this.mMidiManager.unregisterDeviceServer(this.mServer);
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException in unregisterDeviceServer");
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

    public MidiReceiver[] getOutputPortReceivers() {
        MidiReceiver[] receivers = new MidiReceiver[this.mOutputPortCount];
        System.arraycopy(this.mOutputPortDispatchers, 0, receivers, 0, this.mOutputPortCount);
        return receivers;
    }
}
