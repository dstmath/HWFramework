package android.net.wifi;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IScanEvent extends IInterface {
    void OnScanFailed() throws RemoteException;

    void OnScanResultReady() throws RemoteException;

    public static class Default implements IScanEvent {
        @Override // android.net.wifi.IScanEvent
        public void OnScanResultReady() throws RemoteException {
        }

        @Override // android.net.wifi.IScanEvent
        public void OnScanFailed() throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IScanEvent {
        private static final String DESCRIPTOR = "android.net.wifi.IScanEvent";
        static final int TRANSACTION_OnScanFailed = 2;
        static final int TRANSACTION_OnScanResultReady = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IScanEvent asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IScanEvent)) {
                return new Proxy(obj);
            }
            return (IScanEvent) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                OnScanResultReady();
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                OnScanFailed();
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IScanEvent {
            public static IScanEvent sDefaultImpl;
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

            @Override // android.net.wifi.IScanEvent
            public void OnScanResultReady() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().OnScanResultReady();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IScanEvent
            public void OnScanFailed() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().OnScanFailed();
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IScanEvent impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IScanEvent getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
