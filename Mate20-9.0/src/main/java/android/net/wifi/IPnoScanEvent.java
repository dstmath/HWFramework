package android.net.wifi;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IPnoScanEvent extends IInterface {
    public static final int PNO_SCAN_OVER_OFFLOAD_BINDER_FAILURE = 0;
    public static final int PNO_SCAN_OVER_OFFLOAD_REMOTE_FAILURE = 1;

    public static abstract class Stub extends Binder implements IPnoScanEvent {
        private static final String DESCRIPTOR = "android.net.wifi.IPnoScanEvent";
        static final int TRANSACTION_OnPnoNetworkFound = 1;
        static final int TRANSACTION_OnPnoScanFailed = 2;
        static final int TRANSACTION_OnPnoScanOverOffloadFailed = 4;
        static final int TRANSACTION_OnPnoScanOverOffloadStarted = 3;

        private static class Proxy implements IPnoScanEvent {
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }

            public void OnPnoNetworkFound() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void OnPnoScanFailed() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(2, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void OnPnoScanOverOffloadStarted() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(3, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void OnPnoScanOverOffloadFailed(int reason) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(reason);
                    this.mRemote.transact(4, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }
        }

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

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        OnPnoNetworkFound();
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        OnPnoScanFailed();
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        OnPnoScanOverOffloadStarted();
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        OnPnoScanOverOffloadFailed(data.readInt());
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }
    }

    void OnPnoNetworkFound() throws RemoteException;

    void OnPnoScanFailed() throws RemoteException;

    void OnPnoScanOverOffloadFailed(int i) throws RemoteException;

    void OnPnoScanOverOffloadStarted() throws RemoteException;
}
