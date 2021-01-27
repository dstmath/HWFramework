package huawei.android.jankshield;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IJankShield extends IInterface {
    JankCheckPerfBug checkPerfBug() throws RemoteException;

    JankAppInfo getJankAppInfo(String str) throws RemoteException;

    JankProductInfo getJankProductInfo() throws RemoteException;

    boolean getState(String str) throws RemoteException;

    public static class Default implements IJankShield {
        @Override // huawei.android.jankshield.IJankShield
        public boolean getState(String name) throws RemoteException {
            return false;
        }

        @Override // huawei.android.jankshield.IJankShield
        public JankCheckPerfBug checkPerfBug() throws RemoteException {
            return null;
        }

        @Override // huawei.android.jankshield.IJankShield
        public JankAppInfo getJankAppInfo(String packageName) throws RemoteException {
            return null;
        }

        @Override // huawei.android.jankshield.IJankShield
        public JankProductInfo getJankProductInfo() throws RemoteException {
            return null;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IJankShield {
        private static final String DESCRIPTOR = "huawei.android.jankshield.IJankShield";
        static final int TRANSACTION_checkPerfBug = 2;
        static final int TRANSACTION_getJankAppInfo = 3;
        static final int TRANSACTION_getJankProductInfo = 4;
        static final int TRANSACTION_getState = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IJankShield asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IJankShield)) {
                return new Proxy(obj);
            }
            return (IJankShield) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                boolean state = getState(data.readString());
                reply.writeNoException();
                reply.writeInt(state ? 1 : 0);
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                JankCheckPerfBug _result = checkPerfBug();
                reply.writeNoException();
                if (_result != null) {
                    reply.writeInt(1);
                    _result.writeToParcel(reply, 1);
                } else {
                    reply.writeInt(0);
                }
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                JankAppInfo _result2 = getJankAppInfo(data.readString());
                reply.writeNoException();
                if (_result2 != null) {
                    reply.writeInt(1);
                    _result2.writeToParcel(reply, 1);
                } else {
                    reply.writeInt(0);
                }
                return true;
            } else if (code == 4) {
                data.enforceInterface(DESCRIPTOR);
                JankProductInfo _result3 = getJankProductInfo();
                reply.writeNoException();
                if (_result3 != null) {
                    reply.writeInt(1);
                    _result3.writeToParcel(reply, 1);
                } else {
                    reply.writeInt(0);
                }
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IJankShield {
            public static IJankShield sDefaultImpl;
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

            @Override // huawei.android.jankshield.IJankShield
            public boolean getState(String name) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(name);
                    boolean _result = false;
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getState(name);
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

            @Override // huawei.android.jankshield.IJankShield
            public JankCheckPerfBug checkPerfBug() throws RemoteException {
                JankCheckPerfBug _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().checkPerfBug();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = JankCheckPerfBug.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.jankshield.IJankShield
            public JankAppInfo getJankAppInfo(String packageName) throws RemoteException {
                JankAppInfo _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getJankAppInfo(packageName);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = JankAppInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.jankshield.IJankShield
            public JankProductInfo getJankProductInfo() throws RemoteException {
                JankProductInfo _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getJankProductInfo();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = JankProductInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
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

        public static boolean setDefaultImpl(IJankShield impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IJankShield getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
