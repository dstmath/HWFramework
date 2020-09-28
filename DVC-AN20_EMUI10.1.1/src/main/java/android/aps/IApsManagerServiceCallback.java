package android.aps;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.List;

public interface IApsManagerServiceCallback extends IInterface {
    void doCallback(int i, int i2) throws RemoteException;

    void onAppsInfoChanged(List<String> list) throws RemoteException;

    public static class Default implements IApsManagerServiceCallback {
        @Override // android.aps.IApsManagerServiceCallback
        public void onAppsInfoChanged(List<String> list) throws RemoteException {
        }

        @Override // android.aps.IApsManagerServiceCallback
        public void doCallback(int apsCallbackCode, int data) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IApsManagerServiceCallback {
        private static final String DESCRIPTOR = "android.aps.IApsManagerServiceCallback";
        static final int TRANSACTION_doCallback = 2;
        static final int TRANSACTION_onAppsInfoChanged = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IApsManagerServiceCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IApsManagerServiceCallback)) {
                return new Proxy(obj);
            }
            return (IApsManagerServiceCallback) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "onAppsInfoChanged";
            }
            if (transactionCode != 2) {
                return null;
            }
            return "doCallback";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                onAppsInfoChanged(data.createStringArrayList());
                reply.writeNoException();
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                doCallback(data.readInt(), data.readInt());
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IApsManagerServiceCallback {
            public static IApsManagerServiceCallback sDefaultImpl;
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

            @Override // android.aps.IApsManagerServiceCallback
            public void onAppsInfoChanged(List<String> pkgs) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(pkgs);
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onAppsInfoChanged(pkgs);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.aps.IApsManagerServiceCallback
            public void doCallback(int apsCallbackCode, int data) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(apsCallbackCode);
                    _data.writeInt(data);
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().doCallback(apsCallbackCode, data);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IApsManagerServiceCallback impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IApsManagerServiceCallback getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
