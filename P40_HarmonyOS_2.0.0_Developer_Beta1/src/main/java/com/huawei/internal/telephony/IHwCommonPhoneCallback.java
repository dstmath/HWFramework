package com.huawei.internal.telephony;

import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IHwCommonPhoneCallback extends IInterface {
    void onCallback1(int i) throws RemoteException;

    void onCallback2(int i, int i2) throws RemoteException;

    void onCallback3(int i, int i2, Bundle bundle) throws RemoteException;

    public static class Default implements IHwCommonPhoneCallback {
        @Override // com.huawei.internal.telephony.IHwCommonPhoneCallback
        public void onCallback1(int param) throws RemoteException {
        }

        @Override // com.huawei.internal.telephony.IHwCommonPhoneCallback
        public void onCallback2(int param1, int param2) throws RemoteException {
        }

        @Override // com.huawei.internal.telephony.IHwCommonPhoneCallback
        public void onCallback3(int param1, int param2, Bundle param3) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IHwCommonPhoneCallback {
        private static final String DESCRIPTOR = "com.huawei.internal.telephony.IHwCommonPhoneCallback";
        static final int TRANSACTION_onCallback1 = 1;
        static final int TRANSACTION_onCallback2 = 2;
        static final int TRANSACTION_onCallback3 = 3;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IHwCommonPhoneCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHwCommonPhoneCallback)) {
                return new Proxy(obj);
            }
            return (IHwCommonPhoneCallback) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            Bundle _arg2;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                onCallback1(data.readInt());
                reply.writeNoException();
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                onCallback2(data.readInt(), data.readInt());
                reply.writeNoException();
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                int _arg0 = data.readInt();
                int _arg1 = data.readInt();
                if (data.readInt() != 0) {
                    _arg2 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                } else {
                    _arg2 = null;
                }
                onCallback3(_arg0, _arg1, _arg2);
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
        public static class Proxy implements IHwCommonPhoneCallback {
            public static IHwCommonPhoneCallback sDefaultImpl;
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

            @Override // com.huawei.internal.telephony.IHwCommonPhoneCallback
            public void onCallback1(int param) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(param);
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onCallback1(param);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.internal.telephony.IHwCommonPhoneCallback
            public void onCallback2(int param1, int param2) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(param1);
                    _data.writeInt(param2);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onCallback2(param1, param2);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.internal.telephony.IHwCommonPhoneCallback
            public void onCallback3(int param1, int param2, Bundle param3) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(param1);
                    _data.writeInt(param2);
                    if (param3 != null) {
                        _data.writeInt(1);
                        param3.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(3, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onCallback3(param1, param2, param3);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IHwCommonPhoneCallback impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IHwCommonPhoneCallback getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
