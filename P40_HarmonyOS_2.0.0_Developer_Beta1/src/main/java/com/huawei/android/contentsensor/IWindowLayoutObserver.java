package com.huawei.android.contentsensor;

import android.content.ComponentName;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IWindowLayoutObserver extends IInterface {
    void onLayoutChanged(int i, int i2, ComponentName componentName) throws RemoteException;

    public static class Default implements IWindowLayoutObserver {
        @Override // com.huawei.android.contentsensor.IWindowLayoutObserver
        public void onLayoutChanged(int pid, int uid, ComponentName componentName) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IWindowLayoutObserver {
        private static final String DESCRIPTOR = "com.huawei.android.contentsensor.IWindowLayoutObserver";
        static final int TRANSACTION_onLayoutChanged = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IWindowLayoutObserver asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IWindowLayoutObserver)) {
                return new Proxy(obj);
            }
            return (IWindowLayoutObserver) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            ComponentName _arg2;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                int _arg0 = data.readInt();
                int _arg1 = data.readInt();
                if (data.readInt() != 0) {
                    _arg2 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                } else {
                    _arg2 = null;
                }
                onLayoutChanged(_arg0, _arg1, _arg2);
                reply.writeNoException();
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IWindowLayoutObserver {
            public static IWindowLayoutObserver sDefaultImpl;
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

            @Override // com.huawei.android.contentsensor.IWindowLayoutObserver
            public void onLayoutChanged(int pid, int uid, ComponentName componentName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(pid);
                    _data.writeInt(uid);
                    if (componentName != null) {
                        _data.writeInt(1);
                        componentName.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onLayoutChanged(pid, uid, componentName);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IWindowLayoutObserver impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IWindowLayoutObserver getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
