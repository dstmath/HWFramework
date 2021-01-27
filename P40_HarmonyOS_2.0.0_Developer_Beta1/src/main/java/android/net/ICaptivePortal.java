package android.net;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface ICaptivePortal extends IInterface {
    void appResponse(int i) throws RemoteException;

    void logEvent(int i, String str) throws RemoteException;

    public static class Default implements ICaptivePortal {
        @Override // android.net.ICaptivePortal
        public void appResponse(int response) throws RemoteException {
        }

        @Override // android.net.ICaptivePortal
        public void logEvent(int eventId, String packageName) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements ICaptivePortal {
        private static final String DESCRIPTOR = "android.net.ICaptivePortal";
        static final int TRANSACTION_appResponse = 1;
        static final int TRANSACTION_logEvent = 2;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ICaptivePortal asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ICaptivePortal)) {
                return new Proxy(obj);
            }
            return (ICaptivePortal) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "appResponse";
            }
            if (transactionCode != 2) {
                return null;
            }
            return "logEvent";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                appResponse(data.readInt());
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                logEvent(data.readInt(), data.readString());
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements ICaptivePortal {
            public static ICaptivePortal sDefaultImpl;
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

            @Override // android.net.ICaptivePortal
            public void appResponse(int response) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(response);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().appResponse(response);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.net.ICaptivePortal
            public void logEvent(int eventId, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(eventId);
                    _data.writeString(packageName);
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().logEvent(eventId, packageName);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(ICaptivePortal impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static ICaptivePortal getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
