package com.huawei.android.hardware.input;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IHwTHPEventListener extends IInterface {
    void onHwTHPEvent(int i) throws RemoteException;

    void onHwTpEvent(int i, int i2, String str) throws RemoteException;

    public static class Default implements IHwTHPEventListener {
        @Override // com.huawei.android.hardware.input.IHwTHPEventListener
        public void onHwTHPEvent(int event) throws RemoteException {
        }

        @Override // com.huawei.android.hardware.input.IHwTHPEventListener
        public void onHwTpEvent(int eventClass, int eventCode, String extraInfo) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IHwTHPEventListener {
        private static final String DESCRIPTOR = "com.huawei.android.hardware.input.IHwTHPEventListener";
        static final int TRANSACTION_onHwTHPEvent = 1;
        static final int TRANSACTION_onHwTpEvent = 2;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IHwTHPEventListener asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHwTHPEventListener)) {
                return new Proxy(obj);
            }
            return (IHwTHPEventListener) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "onHwTHPEvent";
            }
            if (transactionCode != 2) {
                return null;
            }
            return "onHwTpEvent";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                onHwTHPEvent(data.readInt());
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                onHwTpEvent(data.readInt(), data.readInt(), data.readString());
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IHwTHPEventListener {
            public static IHwTHPEventListener sDefaultImpl;
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

            @Override // com.huawei.android.hardware.input.IHwTHPEventListener
            public void onHwTHPEvent(int event) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(event);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onHwTHPEvent(event);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.hardware.input.IHwTHPEventListener
            public void onHwTpEvent(int eventClass, int eventCode, String extraInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(eventClass);
                    _data.writeInt(eventCode);
                    _data.writeString(extraInfo);
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onHwTpEvent(eventClass, eventCode, extraInfo);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IHwTHPEventListener impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IHwTHPEventListener getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
