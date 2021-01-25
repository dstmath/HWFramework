package huawei.android.security;

import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IHwAntiMalPlugin extends IInterface {
    int getAntimalProtectionPolicy(int i, Bundle bundle) throws RemoteException;

    boolean isAntiMalProtectionOn(Bundle bundle) throws RemoteException;

    boolean setMalData(int i, Bundle bundle) throws RemoteException;

    public static class Default implements IHwAntiMalPlugin {
        @Override // huawei.android.security.IHwAntiMalPlugin
        public boolean isAntiMalProtectionOn(Bundle params) throws RemoteException {
            return false;
        }

        @Override // huawei.android.security.IHwAntiMalPlugin
        public int getAntimalProtectionPolicy(int type, Bundle params) throws RemoteException {
            return 0;
        }

        @Override // huawei.android.security.IHwAntiMalPlugin
        public boolean setMalData(int type, Bundle features) throws RemoteException {
            return false;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IHwAntiMalPlugin {
        private static final String DESCRIPTOR = "huawei.android.security.IHwAntiMalPlugin";
        static final int TRANSACTION_getAntimalProtectionPolicy = 2;
        static final int TRANSACTION_isAntiMalProtectionOn = 1;
        static final int TRANSACTION_setMalData = 3;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IHwAntiMalPlugin asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHwAntiMalPlugin)) {
                return new Proxy(obj);
            }
            return (IHwAntiMalPlugin) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            Bundle _arg0;
            Bundle _arg1;
            Bundle _arg12;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg0 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                } else {
                    _arg0 = null;
                }
                boolean isAntiMalProtectionOn = isAntiMalProtectionOn(_arg0);
                reply.writeNoException();
                reply.writeInt(isAntiMalProtectionOn ? 1 : 0);
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                int _arg02 = data.readInt();
                if (data.readInt() != 0) {
                    _arg1 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                } else {
                    _arg1 = null;
                }
                int _result = getAntimalProtectionPolicy(_arg02, _arg1);
                reply.writeNoException();
                reply.writeInt(_result);
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                int _arg03 = data.readInt();
                if (data.readInt() != 0) {
                    _arg12 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                } else {
                    _arg12 = null;
                }
                boolean malData = setMalData(_arg03, _arg12);
                reply.writeNoException();
                reply.writeInt(malData ? 1 : 0);
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IHwAntiMalPlugin {
            public static IHwAntiMalPlugin sDefaultImpl;
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

            @Override // huawei.android.security.IHwAntiMalPlugin
            public boolean isAntiMalProtectionOn(Bundle params) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (params != null) {
                        _data.writeInt(1);
                        params.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isAntiMalProtectionOn(params);
                    }
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.security.IHwAntiMalPlugin
            public int getAntimalProtectionPolicy(int type, Bundle params) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(type);
                    if (params != null) {
                        _data.writeInt(1);
                        params.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAntimalProtectionPolicy(type, params);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.security.IHwAntiMalPlugin
            public boolean setMalData(int type, Bundle features) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(type);
                    boolean _result = true;
                    if (features != null) {
                        _data.writeInt(1);
                        features.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setMalData(type, features);
                    }
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IHwAntiMalPlugin impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IHwAntiMalPlugin getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
