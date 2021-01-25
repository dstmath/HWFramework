package android.net.wifi;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IPnoScanEvent extends IInterface {
    public static final int PNO_SCAN_OVER_OFFLOAD_BINDER_FAILURE = 0;
    public static final int PNO_SCAN_OVER_OFFLOAD_REMOTE_FAILURE = 1;

    void OnPnoNetworkFound() throws RemoteException;

    void OnPnoScanFailed() throws RemoteException;

    void OnPnoScanOverOffloadFailed(int i) throws RemoteException;

    void OnPnoScanOverOffloadStarted() throws RemoteException;

    public static class Default implements IPnoScanEvent {
        @Override // android.net.wifi.IPnoScanEvent
        public void OnPnoNetworkFound() throws RemoteException {
        }

        @Override // android.net.wifi.IPnoScanEvent
        public void OnPnoScanFailed() throws RemoteException {
        }

        @Override // android.net.wifi.IPnoScanEvent
        public void OnPnoScanOverOffloadStarted() throws RemoteException {
        }

        @Override // android.net.wifi.IPnoScanEvent
        public void OnPnoScanOverOffloadFailed(int reason) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IPnoScanEvent {
        private static final String DESCRIPTOR = "android.net.wifi.IPnoScanEvent";
        static final int TRANSACTION_OnPnoNetworkFound = 1;
        static final int TRANSACTION_OnPnoScanFailed = 2;
        static final int TRANSACTION_OnPnoScanOverOffloadFailed = 4;
        static final int TRANSACTION_OnPnoScanOverOffloadStarted = 3;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IPnoScanEvent asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IPnoScanEvent)) {
                return new Proxy(obj);
            }
            return (IPnoScanEvent) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                OnPnoNetworkFound();
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                OnPnoScanFailed();
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                OnPnoScanOverOffloadStarted();
                return true;
            } else if (code == 4) {
                data.enforceInterface(DESCRIPTOR);
                OnPnoScanOverOffloadFailed(data.readInt());
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IPnoScanEvent {
            public static IPnoScanEvent sDefaultImpl;
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

            @Override // android.net.wifi.IPnoScanEvent
            public void OnPnoNetworkFound() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().OnPnoNetworkFound();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IPnoScanEvent
            public void OnPnoScanFailed() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().OnPnoScanFailed();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IPnoScanEvent
            public void OnPnoScanOverOffloadStarted() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(3, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().OnPnoScanOverOffloadStarted();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IPnoScanEvent
            public void OnPnoScanOverOffloadFailed(int reason) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(reason);
                    if (this.mRemote.transact(4, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().OnPnoScanOverOffloadFailed(reason);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IPnoScanEvent impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IPnoScanEvent getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
