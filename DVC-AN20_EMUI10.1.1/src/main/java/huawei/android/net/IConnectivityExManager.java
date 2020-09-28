package huawei.android.net;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IConnectivityExManager extends IInterface {
    boolean bindUidProcessToNetwork(int i, int i2) throws RemoteException;

    int getNetIdBySlotId(int i) throws RemoteException;

    boolean isAllUidProcessUnbindToNetwork(int i) throws RemoteException;

    boolean isApIpv4AddressFixed() throws RemoteException;

    boolean isUidProcessBindedToNetwork(int i, int i2) throws RemoteException;

    void setApIpv4AddressFixed(boolean z) throws RemoteException;

    void setSmartKeyguardLevel(String str) throws RemoteException;

    void setUseCtrlSocket(boolean z) throws RemoteException;

    boolean unbindAllUidProcessToNetwork(int i) throws RemoteException;

    public static class Default implements IConnectivityExManager {
        @Override // huawei.android.net.IConnectivityExManager
        public void setSmartKeyguardLevel(String level) throws RemoteException {
        }

        @Override // huawei.android.net.IConnectivityExManager
        public void setUseCtrlSocket(boolean flag) throws RemoteException {
        }

        @Override // huawei.android.net.IConnectivityExManager
        public void setApIpv4AddressFixed(boolean isFixed) throws RemoteException {
        }

        @Override // huawei.android.net.IConnectivityExManager
        public boolean isApIpv4AddressFixed() throws RemoteException {
            return false;
        }

        @Override // huawei.android.net.IConnectivityExManager
        public boolean bindUidProcessToNetwork(int netId, int uid) throws RemoteException {
            return false;
        }

        @Override // huawei.android.net.IConnectivityExManager
        public boolean unbindAllUidProcessToNetwork(int netId) throws RemoteException {
            return false;
        }

        @Override // huawei.android.net.IConnectivityExManager
        public boolean isUidProcessBindedToNetwork(int netId, int uid) throws RemoteException {
            return false;
        }

        @Override // huawei.android.net.IConnectivityExManager
        public boolean isAllUidProcessUnbindToNetwork(int netId) throws RemoteException {
            return false;
        }

        @Override // huawei.android.net.IConnectivityExManager
        public int getNetIdBySlotId(int slotId) throws RemoteException {
            return 0;
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IConnectivityExManager {
        private static final String DESCRIPTOR = "huawei.android.net.IConnectivityExManager";
        static final int TRANSACTION_bindUidProcessToNetwork = 5;
        static final int TRANSACTION_getNetIdBySlotId = 9;
        static final int TRANSACTION_isAllUidProcessUnbindToNetwork = 8;
        static final int TRANSACTION_isApIpv4AddressFixed = 4;
        static final int TRANSACTION_isUidProcessBindedToNetwork = 7;
        static final int TRANSACTION_setApIpv4AddressFixed = 3;
        static final int TRANSACTION_setSmartKeyguardLevel = 1;
        static final int TRANSACTION_setUseCtrlSocket = 2;
        static final int TRANSACTION_unbindAllUidProcessToNetwork = 6;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IConnectivityExManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IConnectivityExManager)) {
                return new Proxy(obj);
            }
            return (IConnectivityExManager) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code != 1598968902) {
                boolean _arg0 = false;
                boolean _arg02 = false;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        setSmartKeyguardLevel(data.readString());
                        reply.writeNoException();
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = true;
                        }
                        setUseCtrlSocket(_arg0);
                        reply.writeNoException();
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = true;
                        }
                        setApIpv4AddressFixed(_arg02);
                        reply.writeNoException();
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isApIpv4AddressFixed = isApIpv4AddressFixed();
                        reply.writeNoException();
                        reply.writeInt(isApIpv4AddressFixed ? 1 : 0);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        boolean bindUidProcessToNetwork = bindUidProcessToNetwork(data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(bindUidProcessToNetwork ? 1 : 0);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        boolean unbindAllUidProcessToNetwork = unbindAllUidProcessToNetwork(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(unbindAllUidProcessToNetwork ? 1 : 0);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isUidProcessBindedToNetwork = isUidProcessBindedToNetwork(data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(isUidProcessBindedToNetwork ? 1 : 0);
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isAllUidProcessUnbindToNetwork = isAllUidProcessUnbindToNetwork(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(isAllUidProcessUnbindToNetwork ? 1 : 0);
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        int _result = getNetIdBySlotId(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result);
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IConnectivityExManager {
            public static IConnectivityExManager sDefaultImpl;
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

            @Override // huawei.android.net.IConnectivityExManager
            public void setSmartKeyguardLevel(String level) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(level);
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setSmartKeyguardLevel(level);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.net.IConnectivityExManager
            public void setUseCtrlSocket(boolean flag) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(flag ? 1 : 0);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setUseCtrlSocket(flag);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.net.IConnectivityExManager
            public void setApIpv4AddressFixed(boolean isFixed) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(isFixed ? 1 : 0);
                    if (this.mRemote.transact(3, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setApIpv4AddressFixed(isFixed);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.net.IConnectivityExManager
            public boolean isApIpv4AddressFixed() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isApIpv4AddressFixed();
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

            @Override // huawei.android.net.IConnectivityExManager
            public boolean bindUidProcessToNetwork(int netId, int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(netId);
                    _data.writeInt(uid);
                    boolean _result = false;
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().bindUidProcessToNetwork(netId, uid);
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

            @Override // huawei.android.net.IConnectivityExManager
            public boolean unbindAllUidProcessToNetwork(int netId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(netId);
                    boolean _result = false;
                    if (!this.mRemote.transact(6, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().unbindAllUidProcessToNetwork(netId);
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

            @Override // huawei.android.net.IConnectivityExManager
            public boolean isUidProcessBindedToNetwork(int netId, int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(netId);
                    _data.writeInt(uid);
                    boolean _result = false;
                    if (!this.mRemote.transact(7, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isUidProcessBindedToNetwork(netId, uid);
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

            @Override // huawei.android.net.IConnectivityExManager
            public boolean isAllUidProcessUnbindToNetwork(int netId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(netId);
                    boolean _result = false;
                    if (!this.mRemote.transact(8, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isAllUidProcessUnbindToNetwork(netId);
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

            @Override // huawei.android.net.IConnectivityExManager
            public int getNetIdBySlotId(int slotId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotId);
                    if (!this.mRemote.transact(9, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getNetIdBySlotId(slotId);
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
        }

        public static boolean setDefaultImpl(IConnectivityExManager impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IConnectivityExManager getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
