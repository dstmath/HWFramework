package android.media.midi;

import android.media.midi.IMidiDeviceServer;
import android.os.Binder;
import android.os.IBinder;
import android.os.Process;
import android.os.RemoteException;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import android.util.Log;
import com.android.internal.midi.MidiDispatcher;
import dalvik.system.CloseGuard;
import java.io.Closeable;
import java.io.FileDescriptor;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;
import libcore.io.IoUtils;

public final class MidiDeviceServer implements Closeable {
    private static final String TAG = "MidiDeviceServer";
    private final Callback mCallback;
    private MidiDeviceInfo mDeviceInfo;
    private final CloseGuard mGuard;
    private final HashMap<MidiInputPort, PortClient> mInputPortClients;
    private final int mInputPortCount;
    private final MidiDispatcher.MidiReceiverFailureHandler mInputPortFailureHandler;
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

    /* access modifiers changed from: private */
    public abstract class PortClient implements IBinder.DeathRecipient {
        final IBinder mToken;

        /* access modifiers changed from: package-private */
        public abstract void close();

        PortClient(IBinder token) {
            this.mToken = token;
            try {
                token.linkToDeath(this, 0);
            } catch (RemoteException e) {
                close();
            }
        }

        /* access modifiers changed from: package-private */
        public MidiInputPort getInputPort() {
            return null;
        }

        @Override // android.os.IBinder.DeathRecipient
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

        /* access modifiers changed from: package-private */
        @Override // android.media.midi.MidiDeviceServer.PortClient
        public void close() {
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

        /* access modifiers changed from: package-private */
        @Override // android.media.midi.MidiDeviceServer.PortClient
        public void close() {
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

        /* access modifiers changed from: package-private */
        @Override // android.media.midi.MidiDeviceServer.PortClient
        public MidiInputPort getInputPort() {
            return this.mInputPort;
        }
    }

    /* access modifiers changed from: private */
    public static FileDescriptor[] createSeqPacketSocketPair() throws IOException {
        try {
            FileDescriptor fd0 = new FileDescriptor();
            FileDescriptor fd1 = new FileDescriptor();
            Os.socketpair(OsConstants.AF_UNIX, OsConstants.SOCK_SEQPACKET, 0, fd0, fd1);
            return new FileDescriptor[]{fd0, fd1};
        } catch (ErrnoException e) {
            throw e.rethrowAsIOException();
        }
    }

    MidiDeviceServer(IMidiManager midiManager, MidiReceiver[] inputPortReceivers, int numOutputPorts, Callback callback) {
        this.mInputPorts = new CopyOnWriteArrayList<>();
        this.mGuard = CloseGuard.get();
        this.mPortClients = new HashMap<>();
        this.mInputPortClients = new HashMap<>();
        this.mServer = new IMidiDeviceServer.Stub() {
            /* class android.media.midi.MidiDeviceServer.AnonymousClass1 */

            @Override // android.media.midi.IMidiDeviceServer
            public FileDescriptor openInputPort(IBinder token, int portNumber) {
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
                            FileDescriptor[] pair = MidiDeviceServer.createSeqPacketSocketPair();
                            MidiOutputPort outputPort = new MidiOutputPort(pair[0], portNumber);
                            MidiDeviceServer.this.mInputPortOutputPorts[portNumber] = outputPort;
                            outputPort.connect(MidiDeviceServer.this.mInputPortReceivers[portNumber]);
                            InputPortClient client = new InputPortClient(token, outputPort);
                            synchronized (MidiDeviceServer.this.mPortClients) {
                                MidiDeviceServer.this.mPortClients.put(token, client);
                            }
                            MidiDeviceServer.this.mInputPortOpen[portNumber] = true;
                            MidiDeviceServer.this.updateDeviceStatus();
                            return pair[1];
                        } catch (IOException e) {
                            Log.e(MidiDeviceServer.TAG, "unable to create FileDescriptors in openInputPort");
                            return null;
                        }
                    }
                }
            }

            @Override // android.media.midi.IMidiDeviceServer
            public FileDescriptor openOutputPort(IBinder token, int portNumber) {
                if (MidiDeviceServer.this.mDeviceInfo.isPrivate() && Binder.getCallingUid() != Process.myUid()) {
                    throw new SecurityException("Can't access private device from different UID");
                } else if (portNumber < 0 || portNumber >= MidiDeviceServer.this.mOutputPortCount) {
                    Log.e(MidiDeviceServer.TAG, "portNumber out of range in openOutputPort: " + portNumber);
                    return null;
                } else {
                    try {
                        FileDescriptor[] pair = MidiDeviceServer.createSeqPacketSocketPair();
                        MidiInputPort inputPort = new MidiInputPort(pair[0], portNumber);
                        if (MidiDeviceServer.this.mDeviceInfo.getType() != 2) {
                            IoUtils.setBlocking(pair[0], false);
                        }
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
                        synchronized (MidiDeviceServer.this.mInputPortClients) {
                            MidiDeviceServer.this.mInputPortClients.put(inputPort, client);
                        }
                        return pair[1];
                    } catch (IOException e) {
                        Log.e(MidiDeviceServer.TAG, "unable to create FileDescriptors in openOutputPort");
                        return null;
                    }
                }
            }

            @Override // android.media.midi.IMidiDeviceServer
            public void closePort(IBinder token) {
                MidiInputPort inputPort = null;
                synchronized (MidiDeviceServer.this.mPortClients) {
                    PortClient client = (PortClient) MidiDeviceServer.this.mPortClients.remove(token);
                    if (client != null) {
                        inputPort = client.getInputPort();
                        client.close();
                    }
                }
                if (inputPort != null) {
                    synchronized (MidiDeviceServer.this.mInputPortClients) {
                        MidiDeviceServer.this.mInputPortClients.remove(inputPort);
                    }
                }
            }

            @Override // android.media.midi.IMidiDeviceServer
            public void closeDevice() {
                if (MidiDeviceServer.this.mCallback != null) {
                    MidiDeviceServer.this.mCallback.onClose();
                }
                IoUtils.closeQuietly(MidiDeviceServer.this);
            }

            @Override // android.media.midi.IMidiDeviceServer
            public int connectPorts(IBinder token, FileDescriptor fd, int outputPortNumber) {
                MidiInputPort inputPort = new MidiInputPort(fd, outputPortNumber);
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
                synchronized (MidiDeviceServer.this.mInputPortClients) {
                    MidiDeviceServer.this.mInputPortClients.put(inputPort, client);
                }
                return Process.myPid();
            }

            @Override // android.media.midi.IMidiDeviceServer
            public MidiDeviceInfo getDeviceInfo() {
                return MidiDeviceServer.this.mDeviceInfo;
            }

            @Override // android.media.midi.IMidiDeviceServer
            public void setDeviceInfo(MidiDeviceInfo deviceInfo) {
                if (Binder.getCallingUid() != 1000) {
                    throw new SecurityException("setDeviceInfo should only be called by MidiService");
                } else if (MidiDeviceServer.this.mDeviceInfo == null) {
                    MidiDeviceServer.this.mDeviceInfo = deviceInfo;
                } else {
                    throw new IllegalStateException("setDeviceInfo should only be called once");
                }
            }
        };
        this.mInputPortFailureHandler = new MidiDispatcher.MidiReceiverFailureHandler() {
            /* class android.media.midi.MidiDeviceServer.AnonymousClass2 */

            @Override // com.android.internal.midi.MidiDispatcher.MidiReceiverFailureHandler
            public void onReceiverFailure(MidiReceiver receiver, IOException failure) {
                PortClient client;
                Log.e(MidiDeviceServer.TAG, "MidiInputPort failed to send data", failure);
                synchronized (MidiDeviceServer.this.mInputPortClients) {
                    client = (PortClient) MidiDeviceServer.this.mInputPortClients.remove(receiver);
                }
                if (client != null) {
                    client.close();
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
            this.mOutputPortDispatchers[i] = new MidiDispatcher(this.mInputPortFailureHandler);
        }
        this.mInputPortOpen = new boolean[this.mInputPortCount];
        this.mOutputPortOpenCount = new int[numOutputPorts];
        this.mGuard.open("close");
    }

    MidiDeviceServer(IMidiManager midiManager, MidiReceiver[] inputPortReceivers, MidiDeviceInfo deviceInfo, Callback callback) {
        this(midiManager, inputPortReceivers, deviceInfo.getOutputPortCount(), callback);
        this.mDeviceInfo = deviceInfo;
    }

    /* access modifiers changed from: package-private */
    public IMidiDeviceServer getBinderInterface() {
        return this.mServer;
    }

    public IBinder asBinder() {
        return this.mServer.asBinder();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateDeviceStatus() {
        long identityToken = Binder.clearCallingIdentity();
        MidiDeviceStatus status = new MidiDeviceStatus(this.mDeviceInfo, this.mInputPortOpen, this.mOutputPortOpenCount);
        Callback callback = this.mCallback;
        if (callback != null) {
            callback.onDeviceStatusChanged(this, status);
        }
        try {
            this.mMidiManager.setDeviceStatus(this.mServer, status);
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException in updateDeviceStatus");
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identityToken);
            throw th;
        }
        Binder.restoreCallingIdentity(identityToken);
    }

    @Override // java.io.Closeable, java.lang.AutoCloseable
    public void close() throws IOException {
        synchronized (this.mGuard) {
            if (!this.mIsClosed) {
                this.mGuard.close();
                for (int i = 0; i < this.mInputPortCount; i++) {
                    MidiOutputPort outputPort = this.mInputPortOutputPorts[i];
                    if (outputPort != null) {
                        IoUtils.closeQuietly(outputPort);
                        this.mInputPortOutputPorts[i] = null;
                    }
                }
                Iterator<MidiInputPort> it = this.mInputPorts.iterator();
                while (it.hasNext()) {
                    IoUtils.closeQuietly(it.next());
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

    public MidiReceiver[] getOutputPortReceivers() {
        int i = this.mOutputPortCount;
        MidiReceiver[] receivers = new MidiReceiver[i];
        System.arraycopy(this.mOutputPortDispatchers, 0, receivers, 0, i);
        return receivers;
    }
}
