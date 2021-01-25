package android.net.wifi;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface ISendMgmtFrameEvent extends IInterface {
    public static final int SEND_MGMT_FRAME_ERROR_ALREADY_STARTED = 5;
    public static final int SEND_MGMT_FRAME_ERROR_MCS_UNSUPPORTED = 2;
    public static final int SEND_MGMT_FRAME_ERROR_NO_ACK = 3;
    public static final int SEND_MGMT_FRAME_ERROR_TIMEOUT = 4;
    public static final int SEND_MGMT_FRAME_ERROR_UNKNOWN = 1;

    void OnAck(int i) throws RemoteException;

    void OnFailure(int i) throws RemoteException;

    public static class Default implements ISendMgmtFrameEvent {
        @Override // android.net.wifi.ISendMgmtFrameEvent
        public void OnAck(int elapsedTimeMs) throws RemoteException {
        }

        @Override // android.net.wifi.ISendMgmtFrameEvent
        public void OnFailure(int reason) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements ISendMgmtFrameEvent {
        private static final String DESCRIPTOR = "android.net.wifi.ISendMgmtFrameEvent";
        static final int TRANSACTION_OnAck = 1;
        static final int TRANSACTION_OnFailure = 2;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ISendMgmtFrameEvent asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ISendMgmtFrameEvent)) {
                return new Proxy(obj);
            }
            return (ISendMgmtFrameEvent) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                OnAck(data.readInt());
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                OnFailure(data.readInt());
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements ISendMgmtFrameEvent {
            public static ISendMgmtFrameEvent sDefaultImpl;
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            @Override // android.os.IInterface
            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }

            @Override // android.net.wifi.ISendMgmtFrameEvent
            public void OnAck(int elapsedTimeMs) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(elapsedTimeMs);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().OnAck(elapsedTimeMs);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.ISendMgmtFrameEvent
            public void OnFailure(int reason) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(reason);
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().OnFailure(reason);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(ISendMgmtFrameEvent impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static ISendMgmtFrameEvent getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
