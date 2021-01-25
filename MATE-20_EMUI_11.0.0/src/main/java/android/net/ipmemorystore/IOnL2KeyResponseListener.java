package android.net.ipmemorystore;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IOnL2KeyResponseListener extends IInterface {
    public static final int VERSION = 3;

    int getInterfaceVersion() throws RemoteException;

    void onL2KeyResponse(StatusParcelable statusParcelable, String str) throws RemoteException;

    public static class Default implements IOnL2KeyResponseListener {
        @Override // android.net.ipmemorystore.IOnL2KeyResponseListener
        public void onL2KeyResponse(StatusParcelable status, String l2Key) throws RemoteException {
        }

        @Override // android.net.ipmemorystore.IOnL2KeyResponseListener
        public int getInterfaceVersion() {
            return -1;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IOnL2KeyResponseListener {
        private static final String DESCRIPTOR = "android.net.ipmemorystore.IOnL2KeyResponseListener";
        static final int TRANSACTION_getInterfaceVersion = 16777215;
        static final int TRANSACTION_onL2KeyResponse = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IOnL2KeyResponseListener asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IOnL2KeyResponseListener)) {
                return new Proxy(obj);
            }
            return (IOnL2KeyResponseListener) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            StatusParcelable _arg0;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg0 = StatusParcelable.CREATOR.createFromParcel(data);
                } else {
                    _arg0 = null;
                }
                onL2KeyResponse(_arg0, data.readString());
                return true;
            } else if (code == TRANSACTION_getInterfaceVersion) {
                data.enforceInterface(DESCRIPTOR);
                reply.writeNoException();
                reply.writeInt(getInterfaceVersion());
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IOnL2KeyResponseListener {
            public static IOnL2KeyResponseListener sDefaultImpl;
            private int mCachedVersion = -1;
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

            @Override // android.net.ipmemorystore.IOnL2KeyResponseListener
            public void onL2KeyResponse(StatusParcelable status, String l2Key) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (status != null) {
                        _data.writeInt(1);
                        status.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(l2Key);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onL2KeyResponse(status, l2Key);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.net.ipmemorystore.IOnL2KeyResponseListener
            public int getInterfaceVersion() throws RemoteException {
                if (this.mCachedVersion == -1) {
                    Parcel data = Parcel.obtain();
                    Parcel reply = Parcel.obtain();
                    try {
                        data.writeInterfaceToken(Stub.DESCRIPTOR);
                        this.mRemote.transact(Stub.TRANSACTION_getInterfaceVersion, data, reply, 0);
                        reply.readException();
                        this.mCachedVersion = reply.readInt();
                    } finally {
                        reply.recycle();
                        data.recycle();
                    }
                }
                return this.mCachedVersion;
            }
        }

        public static boolean setDefaultImpl(IOnL2KeyResponseListener impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IOnL2KeyResponseListener getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
