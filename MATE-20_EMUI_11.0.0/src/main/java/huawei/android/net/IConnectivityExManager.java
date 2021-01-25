package huawei.android.net;

import android.net.NetworkRequest;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Messenger;
import android.os.Parcel;
import android.os.RemoteException;
import huawei.android.net.slice.IAppInfoCallback;
import huawei.android.net.slice.INetworkSliceStateListener;
import huawei.android.net.slice.TrafficDescriptor;

public interface IConnectivityExManager extends IInterface {
    boolean bindUidProcessToNetwork(int i, int i2) throws RemoteException;

    int getNetIdBySlotId(int i) throws RemoteException;

    int getUsbP2pState() throws RemoteException;

    boolean hasPermissionForSlice(int i) throws RemoteException;

    void initAppInfo(String str, int i, IAppInfoCallback iAppInfoCallback) throws RemoteException;

    boolean isAllUidProcessUnbindToNetwork(int i) throws RemoteException;

    boolean isApIpv4AddressFixed() throws RemoteException;

    boolean isNetworkSliceSupported() throws RemoteException;

    boolean isUidProcessBindedToNetwork(int i, int i2) throws RemoteException;

    int listenForUsbP2p(int i, Messenger messenger, IBinder iBinder) throws RemoteException;

    boolean registerListener(int i, INetworkSliceStateListener iNetworkSliceStateListener) throws RemoteException;

    boolean releaseNetworkSlice(int i, int i2) throws RemoteException;

    void releaseUsbP2pRequest(int i) throws RemoteException;

    int requestForUsbP2p(int i, Messenger messenger, IBinder iBinder) throws RemoteException;

    NetworkRequest requestNetworkSlice(int i, TrafficDescriptor trafficDescriptor, Messenger messenger, int i2) throws RemoteException;

    void setApIpv4AddressFixed(boolean z) throws RemoteException;

    void setSmartKeyguardLevel(String str) throws RemoteException;

    void setUseCtrlSocket(boolean z) throws RemoteException;

    boolean unbindAllUidProcessToNetwork(int i) throws RemoteException;

    boolean unregisterListener(int i, INetworkSliceStateListener iNetworkSliceStateListener) throws RemoteException;

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

        @Override // huawei.android.net.IConnectivityExManager
        public boolean isNetworkSliceSupported() throws RemoteException {
            return false;
        }

        @Override // huawei.android.net.IConnectivityExManager
        public void initAppInfo(String appId, int uid, IAppInfoCallback appInfoCallback) throws RemoteException {
        }

        @Override // huawei.android.net.IConnectivityExManager
        public boolean registerListener(int uid, INetworkSliceStateListener networkSliceStateListener) throws RemoteException {
            return false;
        }

        @Override // huawei.android.net.IConnectivityExManager
        public boolean unregisterListener(int uid, INetworkSliceStateListener networkSliceStateListener) throws RemoteException {
            return false;
        }

        @Override // huawei.android.net.IConnectivityExManager
        public NetworkRequest requestNetworkSlice(int uid, TrafficDescriptor trafficDescriptor, Messenger messenger, int timeoutMs) throws RemoteException {
            return null;
        }

        @Override // huawei.android.net.IConnectivityExManager
        public boolean releaseNetworkSlice(int uid, int requestId) throws RemoteException {
            return false;
        }

        @Override // huawei.android.net.IConnectivityExManager
        public int getUsbP2pState() throws RemoteException {
            return 0;
        }

        @Override // huawei.android.net.IConnectivityExManager
        public int requestForUsbP2p(int requestId, Messenger messenger, IBinder binder) throws RemoteException {
            return 0;
        }

        @Override // huawei.android.net.IConnectivityExManager
        public int listenForUsbP2p(int listenId, Messenger messenger, IBinder binder) throws RemoteException {
            return 0;
        }

        @Override // huawei.android.net.IConnectivityExManager
        public void releaseUsbP2pRequest(int requestId) throws RemoteException {
        }

        @Override // huawei.android.net.IConnectivityExManager
        public boolean hasPermissionForSlice(int uid) throws RemoteException {
            return false;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IConnectivityExManager {
        private static final String DESCRIPTOR = "huawei.android.net.IConnectivityExManager";
        static final int TRANSACTION_bindUidProcessToNetwork = 5;
        static final int TRANSACTION_getNetIdBySlotId = 9;
        static final int TRANSACTION_getUsbP2pState = 16;
        static final int TRANSACTION_hasPermissionForSlice = 20;
        static final int TRANSACTION_initAppInfo = 11;
        static final int TRANSACTION_isAllUidProcessUnbindToNetwork = 8;
        static final int TRANSACTION_isApIpv4AddressFixed = 4;
        static final int TRANSACTION_isNetworkSliceSupported = 10;
        static final int TRANSACTION_isUidProcessBindedToNetwork = 7;
        static final int TRANSACTION_listenForUsbP2p = 18;
        static final int TRANSACTION_registerListener = 12;
        static final int TRANSACTION_releaseNetworkSlice = 15;
        static final int TRANSACTION_releaseUsbP2pRequest = 19;
        static final int TRANSACTION_requestForUsbP2p = 17;
        static final int TRANSACTION_requestNetworkSlice = 14;
        static final int TRANSACTION_setApIpv4AddressFixed = 3;
        static final int TRANSACTION_setSmartKeyguardLevel = 1;
        static final int TRANSACTION_setUseCtrlSocket = 2;
        static final int TRANSACTION_unbindAllUidProcessToNetwork = 6;
        static final int TRANSACTION_unregisterListener = 13;

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

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            TrafficDescriptor _arg1;
            Messenger _arg2;
            Messenger _arg12;
            Messenger _arg13;
            if (code != 1598968902) {
                boolean _arg0 = false;
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
                            _arg0 = true;
                        }
                        setApIpv4AddressFixed(_arg0);
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
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isNetworkSliceSupported = isNetworkSliceSupported();
                        reply.writeNoException();
                        reply.writeInt(isNetworkSliceSupported ? 1 : 0);
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        initAppInfo(data.readString(), data.readInt(), IAppInfoCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        boolean registerListener = registerListener(data.readInt(), INetworkSliceStateListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(registerListener ? 1 : 0);
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        boolean unregisterListener = unregisterListener(data.readInt(), INetworkSliceStateListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(unregisterListener ? 1 : 0);
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg02 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg1 = TrafficDescriptor.CREATOR.createFromParcel(data);
                        } else {
                            _arg1 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg2 = (Messenger) Messenger.CREATOR.createFromParcel(data);
                        } else {
                            _arg2 = null;
                        }
                        NetworkRequest _result2 = requestNetworkSlice(_arg02, _arg1, _arg2, data.readInt());
                        reply.writeNoException();
                        if (_result2 != null) {
                            reply.writeInt(1);
                            _result2.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        boolean releaseNetworkSlice = releaseNetworkSlice(data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(releaseNetworkSlice ? 1 : 0);
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        int _result3 = getUsbP2pState();
                        reply.writeNoException();
                        reply.writeInt(_result3);
                        return true;
                    case 17:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg03 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg12 = (Messenger) Messenger.CREATOR.createFromParcel(data);
                        } else {
                            _arg12 = null;
                        }
                        int _result4 = requestForUsbP2p(_arg03, _arg12, data.readStrongBinder());
                        reply.writeNoException();
                        reply.writeInt(_result4);
                        return true;
                    case 18:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg04 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg13 = (Messenger) Messenger.CREATOR.createFromParcel(data);
                        } else {
                            _arg13 = null;
                        }
                        int _result5 = listenForUsbP2p(_arg04, _arg13, data.readStrongBinder());
                        reply.writeNoException();
                        reply.writeInt(_result5);
                        return true;
                    case 19:
                        data.enforceInterface(DESCRIPTOR);
                        releaseUsbP2pRequest(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 20:
                        data.enforceInterface(DESCRIPTOR);
                        boolean hasPermissionForSlice = hasPermissionForSlice(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(hasPermissionForSlice ? 1 : 0);
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

            @Override // android.os.IInterface
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

            @Override // huawei.android.net.IConnectivityExManager
            public boolean isNetworkSliceSupported() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(10, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isNetworkSliceSupported();
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
            public void initAppInfo(String appId, int uid, IAppInfoCallback appInfoCallback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(appId);
                    _data.writeInt(uid);
                    _data.writeStrongBinder(appInfoCallback != null ? appInfoCallback.asBinder() : null);
                    if (this.mRemote.transact(11, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().initAppInfo(appId, uid, appInfoCallback);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.net.IConnectivityExManager
            public boolean registerListener(int uid, INetworkSliceStateListener networkSliceStateListener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeStrongBinder(networkSliceStateListener != null ? networkSliceStateListener.asBinder() : null);
                    boolean _result = false;
                    if (!this.mRemote.transact(12, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().registerListener(uid, networkSliceStateListener);
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
            public boolean unregisterListener(int uid, INetworkSliceStateListener networkSliceStateListener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeStrongBinder(networkSliceStateListener != null ? networkSliceStateListener.asBinder() : null);
                    boolean _result = false;
                    if (!this.mRemote.transact(13, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().unregisterListener(uid, networkSliceStateListener);
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
            public NetworkRequest requestNetworkSlice(int uid, TrafficDescriptor trafficDescriptor, Messenger messenger, int timeoutMs) throws RemoteException {
                NetworkRequest _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    if (trafficDescriptor != null) {
                        _data.writeInt(1);
                        trafficDescriptor.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (messenger != null) {
                        _data.writeInt(1);
                        messenger.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(timeoutMs);
                    if (!this.mRemote.transact(14, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().requestNetworkSlice(uid, trafficDescriptor, messenger, timeoutMs);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (NetworkRequest) NetworkRequest.CREATOR.createFromParcel(_reply);
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

            @Override // huawei.android.net.IConnectivityExManager
            public boolean releaseNetworkSlice(int uid, int requestId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeInt(requestId);
                    boolean _result = false;
                    if (!this.mRemote.transact(15, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().releaseNetworkSlice(uid, requestId);
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
            public int getUsbP2pState() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(16, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getUsbP2pState();
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

            @Override // huawei.android.net.IConnectivityExManager
            public int requestForUsbP2p(int requestId, Messenger messenger, IBinder binder) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(requestId);
                    if (messenger != null) {
                        _data.writeInt(1);
                        messenger.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(binder);
                    if (!this.mRemote.transact(17, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().requestForUsbP2p(requestId, messenger, binder);
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

            @Override // huawei.android.net.IConnectivityExManager
            public int listenForUsbP2p(int listenId, Messenger messenger, IBinder binder) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(listenId);
                    if (messenger != null) {
                        _data.writeInt(1);
                        messenger.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(binder);
                    if (!this.mRemote.transact(18, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().listenForUsbP2p(listenId, messenger, binder);
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

            @Override // huawei.android.net.IConnectivityExManager
            public void releaseUsbP2pRequest(int requestId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(requestId);
                    if (this.mRemote.transact(19, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().releaseUsbP2pRequest(requestId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.net.IConnectivityExManager
            public boolean hasPermissionForSlice(int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    boolean _result = false;
                    if (!this.mRemote.transact(20, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().hasPermissionForSlice(uid);
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
