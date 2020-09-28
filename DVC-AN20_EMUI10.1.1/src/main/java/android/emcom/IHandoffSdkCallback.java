package android.emcom;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IHandoffSdkCallback extends IInterface {
    void handoffDataEvent(String str) throws RemoteException;

    void handoffStateChg(int i) throws RemoteException;

    public static class Default implements IHandoffSdkCallback {
        @Override // android.emcom.IHandoffSdkCallback
        public void handoffStateChg(int state) throws RemoteException {
        }

        @Override // android.emcom.IHandoffSdkCallback
        public void handoffDataEvent(String para) throws RemoteException {
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IHandoffSdkCallback {
        private static final String DESCRIPTOR = "android.emcom.IHandoffSdkCallback";
        static final int TRANSACTION_handoffDataEvent = 2;
        static final int TRANSACTION_handoffStateChg = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IHandoffSdkCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHandoffSdkCallback)) {
                return new Proxy(obj);
            }
            return (IHandoffSdkCallback) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                handoffStateChg(data.readInt());
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                handoffDataEvent(data.readString());
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IHandoffSdkCallback {
            public static IHandoffSdkCallback sDefaultImpl;
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

            @Override // android.emcom.IHandoffSdkCallback
            public void handoffStateChg(int state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(state);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().handoffStateChg(state);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.emcom.IHandoffSdkCallback
            public void handoffDataEvent(String para) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(para);
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().handoffDataEvent(para);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IHandoffSdkCallback impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IHandoffSdkCallback getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
