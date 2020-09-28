package com.huawei.displayengine;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.PersistableBundle;
import android.os.RemoteException;

public interface IDisplayEngineCallback extends IInterface {
    void onEvent(int i, int i2) throws RemoteException;

    void onEventWithData(int i, PersistableBundle persistableBundle) throws RemoteException;

    public static class Default implements IDisplayEngineCallback {
        @Override // com.huawei.displayengine.IDisplayEngineCallback
        public void onEvent(int event, int extra) throws RemoteException {
        }

        @Override // com.huawei.displayengine.IDisplayEngineCallback
        public void onEventWithData(int event, PersistableBundle data) throws RemoteException {
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IDisplayEngineCallback {
        private static final String DESCRIPTOR = "com.huawei.displayengine.IDisplayEngineCallback";
        static final int TRANSACTION_onEvent = 1;
        static final int TRANSACTION_onEventWithData = 2;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IDisplayEngineCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IDisplayEngineCallback)) {
                return new Proxy(obj);
            }
            return (IDisplayEngineCallback) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            PersistableBundle _arg1;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                onEvent(data.readInt(), data.readInt());
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                int _arg0 = data.readInt();
                if (data.readInt() != 0) {
                    _arg1 = (PersistableBundle) PersistableBundle.CREATOR.createFromParcel(data);
                } else {
                    _arg1 = null;
                }
                onEventWithData(_arg0, _arg1);
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IDisplayEngineCallback {
            public static IDisplayEngineCallback sDefaultImpl;
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

            @Override // com.huawei.displayengine.IDisplayEngineCallback
            public void onEvent(int event, int extra) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(event);
                    _data.writeInt(extra);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onEvent(event, extra);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.huawei.displayengine.IDisplayEngineCallback
            public void onEventWithData(int event, PersistableBundle data) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(event);
                    if (data != null) {
                        _data.writeInt(1);
                        data.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onEventWithData(event, data);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IDisplayEngineCallback impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IDisplayEngineCallback getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
