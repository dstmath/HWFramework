package com.huawei.airlink;

import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.huawei.airlink.IAirLinkAidlCallBack;

public interface IAirLinkAidlInterface extends IInterface {
    void commonOperation(String str, Bundle bundle) throws RemoteException;

    boolean registerCallback(IAirLinkAidlCallBack iAirLinkAidlCallBack) throws RemoteException;

    boolean unRegisterCallback() throws RemoteException;

    public static class Default implements IAirLinkAidlInterface {
        @Override // com.huawei.airlink.IAirLinkAidlInterface
        public boolean registerCallback(IAirLinkAidlCallBack callback) throws RemoteException {
            return false;
        }

        @Override // com.huawei.airlink.IAirLinkAidlInterface
        public boolean unRegisterCallback() throws RemoteException {
            return false;
        }

        @Override // com.huawei.airlink.IAirLinkAidlInterface
        public void commonOperation(String operation, Bundle bundle) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IAirLinkAidlInterface {
        private static final String DESCRIPTOR = "com.huawei.airlink.IAirLinkAidlInterface";
        static final int TRANSACTION_commonOperation = 3;
        static final int TRANSACTION_registerCallback = 1;
        static final int TRANSACTION_unRegisterCallback = 2;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IAirLinkAidlInterface asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IAirLinkAidlInterface)) {
                return new Proxy(obj);
            }
            return (IAirLinkAidlInterface) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            Bundle _arg1;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                boolean registerCallback = registerCallback(IAirLinkAidlCallBack.Stub.asInterface(data.readStrongBinder()));
                reply.writeNoException();
                reply.writeInt(registerCallback ? 1 : 0);
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                boolean unRegisterCallback = unRegisterCallback();
                reply.writeNoException();
                reply.writeInt(unRegisterCallback ? 1 : 0);
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                String _arg0 = data.readString();
                if (data.readInt() != 0) {
                    _arg1 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                } else {
                    _arg1 = null;
                }
                commonOperation(_arg0, _arg1);
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
        public static class Proxy implements IAirLinkAidlInterface {
            public static IAirLinkAidlInterface sDefaultImpl;
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

            @Override // com.huawei.airlink.IAirLinkAidlInterface
            public boolean registerCallback(IAirLinkAidlCallBack callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    boolean _result = false;
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().registerCallback(callback);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.airlink.IAirLinkAidlInterface
            public boolean unRegisterCallback() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().unRegisterCallback();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.airlink.IAirLinkAidlInterface
            public void commonOperation(String operation, Bundle bundle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(operation);
                    if (bundle != null) {
                        _data.writeInt(1);
                        bundle.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(3, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().commonOperation(operation, bundle);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IAirLinkAidlInterface impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IAirLinkAidlInterface getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
