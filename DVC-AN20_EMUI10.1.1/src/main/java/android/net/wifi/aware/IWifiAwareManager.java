package android.net.wifi.aware;

import android.net.wifi.aware.IWifiAwareDiscoverySessionCallback;
import android.net.wifi.aware.IWifiAwareEventCallback;
import android.net.wifi.aware.IWifiAwareMacAddressProvider;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.List;

public interface IWifiAwareManager extends IInterface {
    void connect(IBinder iBinder, String str, IWifiAwareEventCallback iWifiAwareEventCallback, ConfigRequest configRequest, boolean z) throws RemoteException;

    void disconnect(int i, IBinder iBinder) throws RemoteException;

    Characteristics getCharacteristics() throws RemoteException;

    boolean isUsageEnabled() throws RemoteException;

    void publish(String str, int i, PublishConfig publishConfig, IWifiAwareDiscoverySessionCallback iWifiAwareDiscoverySessionCallback) throws RemoteException;

    void requestMacAddresses(int i, List list, IWifiAwareMacAddressProvider iWifiAwareMacAddressProvider) throws RemoteException;

    void sendMessage(int i, int i2, int i3, byte[] bArr, int i4, int i5) throws RemoteException;

    void subscribe(String str, int i, SubscribeConfig subscribeConfig, IWifiAwareDiscoverySessionCallback iWifiAwareDiscoverySessionCallback) throws RemoteException;

    void terminateSession(int i, int i2) throws RemoteException;

    void updatePublish(int i, int i2, PublishConfig publishConfig) throws RemoteException;

    void updateSubscribe(int i, int i2, SubscribeConfig subscribeConfig) throws RemoteException;

    public static class Default implements IWifiAwareManager {
        @Override // android.net.wifi.aware.IWifiAwareManager
        public boolean isUsageEnabled() throws RemoteException {
            return false;
        }

        @Override // android.net.wifi.aware.IWifiAwareManager
        public Characteristics getCharacteristics() throws RemoteException {
            return null;
        }

        @Override // android.net.wifi.aware.IWifiAwareManager
        public void connect(IBinder binder, String callingPackage, IWifiAwareEventCallback callback, ConfigRequest configRequest, boolean notifyOnIdentityChanged) throws RemoteException {
        }

        @Override // android.net.wifi.aware.IWifiAwareManager
        public void disconnect(int clientId, IBinder binder) throws RemoteException {
        }

        @Override // android.net.wifi.aware.IWifiAwareManager
        public void publish(String callingPackage, int clientId, PublishConfig publishConfig, IWifiAwareDiscoverySessionCallback callback) throws RemoteException {
        }

        @Override // android.net.wifi.aware.IWifiAwareManager
        public void subscribe(String callingPackage, int clientId, SubscribeConfig subscribeConfig, IWifiAwareDiscoverySessionCallback callback) throws RemoteException {
        }

        @Override // android.net.wifi.aware.IWifiAwareManager
        public void updatePublish(int clientId, int discoverySessionId, PublishConfig publishConfig) throws RemoteException {
        }

        @Override // android.net.wifi.aware.IWifiAwareManager
        public void updateSubscribe(int clientId, int discoverySessionId, SubscribeConfig subscribeConfig) throws RemoteException {
        }

        @Override // android.net.wifi.aware.IWifiAwareManager
        public void sendMessage(int clientId, int discoverySessionId, int peerId, byte[] message, int messageId, int retryCount) throws RemoteException {
        }

        @Override // android.net.wifi.aware.IWifiAwareManager
        public void terminateSession(int clientId, int discoverySessionId) throws RemoteException {
        }

        @Override // android.net.wifi.aware.IWifiAwareManager
        public void requestMacAddresses(int uid, List peerIds, IWifiAwareMacAddressProvider callback) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IWifiAwareManager {
        private static final String DESCRIPTOR = "android.net.wifi.aware.IWifiAwareManager";
        static final int TRANSACTION_connect = 3;
        static final int TRANSACTION_disconnect = 4;
        static final int TRANSACTION_getCharacteristics = 2;
        static final int TRANSACTION_isUsageEnabled = 1;
        static final int TRANSACTION_publish = 5;
        static final int TRANSACTION_requestMacAddresses = 11;
        static final int TRANSACTION_sendMessage = 9;
        static final int TRANSACTION_subscribe = 6;
        static final int TRANSACTION_terminateSession = 10;
        static final int TRANSACTION_updatePublish = 7;
        static final int TRANSACTION_updateSubscribe = 8;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IWifiAwareManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IWifiAwareManager)) {
                return new Proxy(obj);
            }
            return (IWifiAwareManager) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "isUsageEnabled";
                case 2:
                    return "getCharacteristics";
                case 3:
                    return "connect";
                case 4:
                    return "disconnect";
                case 5:
                    return "publish";
                case 6:
                    return "subscribe";
                case 7:
                    return "updatePublish";
                case 8:
                    return "updateSubscribe";
                case 9:
                    return "sendMessage";
                case 10:
                    return "terminateSession";
                case 11:
                    return "requestMacAddresses";
                default:
                    return null;
            }
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            ConfigRequest _arg3;
            PublishConfig _arg2;
            SubscribeConfig _arg22;
            PublishConfig _arg23;
            SubscribeConfig _arg24;
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isUsageEnabled = isUsageEnabled();
                        reply.writeNoException();
                        reply.writeInt(isUsageEnabled ? 1 : 0);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        Characteristics _result = getCharacteristics();
                        reply.writeNoException();
                        if (_result != null) {
                            reply.writeInt(1);
                            _result.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        IBinder _arg0 = data.readStrongBinder();
                        String _arg1 = data.readString();
                        IWifiAwareEventCallback _arg25 = IWifiAwareEventCallback.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg3 = ConfigRequest.CREATOR.createFromParcel(data);
                        } else {
                            _arg3 = null;
                        }
                        connect(_arg0, _arg1, _arg25, _arg3, data.readInt() != 0);
                        reply.writeNoException();
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        disconnect(data.readInt(), data.readStrongBinder());
                        reply.writeNoException();
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg02 = data.readString();
                        int _arg12 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg2 = PublishConfig.CREATOR.createFromParcel(data);
                        } else {
                            _arg2 = null;
                        }
                        publish(_arg02, _arg12, _arg2, IWifiAwareDiscoverySessionCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg03 = data.readString();
                        int _arg13 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg22 = SubscribeConfig.CREATOR.createFromParcel(data);
                        } else {
                            _arg22 = null;
                        }
                        subscribe(_arg03, _arg13, _arg22, IWifiAwareDiscoverySessionCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg04 = data.readInt();
                        int _arg14 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg23 = PublishConfig.CREATOR.createFromParcel(data);
                        } else {
                            _arg23 = null;
                        }
                        updatePublish(_arg04, _arg14, _arg23);
                        reply.writeNoException();
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg05 = data.readInt();
                        int _arg15 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg24 = SubscribeConfig.CREATOR.createFromParcel(data);
                        } else {
                            _arg24 = null;
                        }
                        updateSubscribe(_arg05, _arg15, _arg24);
                        reply.writeNoException();
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        sendMessage(data.readInt(), data.readInt(), data.readInt(), data.createByteArray(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        terminateSession(data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        requestMacAddresses(data.readInt(), data.readArrayList(getClass().getClassLoader()), IWifiAwareMacAddressProvider.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
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
        public static class Proxy implements IWifiAwareManager {
            public static IWifiAwareManager sDefaultImpl;
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

            @Override // android.net.wifi.aware.IWifiAwareManager
            public boolean isUsageEnabled() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isUsageEnabled();
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

            @Override // android.net.wifi.aware.IWifiAwareManager
            public Characteristics getCharacteristics() throws RemoteException {
                Characteristics _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getCharacteristics();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = Characteristics.CREATOR.createFromParcel(_reply);
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

            @Override // android.net.wifi.aware.IWifiAwareManager
            public void connect(IBinder binder, String callingPackage, IWifiAwareEventCallback callback, ConfigRequest configRequest, boolean notifyOnIdentityChanged) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(binder);
                    _data.writeString(callingPackage);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    int i = 1;
                    if (configRequest != null) {
                        _data.writeInt(1);
                        configRequest.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!notifyOnIdentityChanged) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    if (this.mRemote.transact(3, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().connect(binder, callingPackage, callback, configRequest, notifyOnIdentityChanged);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.aware.IWifiAwareManager
            public void disconnect(int clientId, IBinder binder) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(clientId);
                    _data.writeStrongBinder(binder);
                    if (this.mRemote.transact(4, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().disconnect(clientId, binder);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.aware.IWifiAwareManager
            public void publish(String callingPackage, int clientId, PublishConfig publishConfig, IWifiAwareDiscoverySessionCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    _data.writeInt(clientId);
                    if (publishConfig != null) {
                        _data.writeInt(1);
                        publishConfig.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (this.mRemote.transact(5, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().publish(callingPackage, clientId, publishConfig, callback);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.aware.IWifiAwareManager
            public void subscribe(String callingPackage, int clientId, SubscribeConfig subscribeConfig, IWifiAwareDiscoverySessionCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    _data.writeInt(clientId);
                    if (subscribeConfig != null) {
                        _data.writeInt(1);
                        subscribeConfig.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (this.mRemote.transact(6, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().subscribe(callingPackage, clientId, subscribeConfig, callback);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.aware.IWifiAwareManager
            public void updatePublish(int clientId, int discoverySessionId, PublishConfig publishConfig) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(clientId);
                    _data.writeInt(discoverySessionId);
                    if (publishConfig != null) {
                        _data.writeInt(1);
                        publishConfig.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(7, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().updatePublish(clientId, discoverySessionId, publishConfig);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.aware.IWifiAwareManager
            public void updateSubscribe(int clientId, int discoverySessionId, SubscribeConfig subscribeConfig) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(clientId);
                    _data.writeInt(discoverySessionId);
                    if (subscribeConfig != null) {
                        _data.writeInt(1);
                        subscribeConfig.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(8, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().updateSubscribe(clientId, discoverySessionId, subscribeConfig);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.aware.IWifiAwareManager
            public void sendMessage(int clientId, int discoverySessionId, int peerId, byte[] message, int messageId, int retryCount) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeInt(clientId);
                    } catch (Throwable th) {
                        th = th;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(discoverySessionId);
                        try {
                            _data.writeInt(peerId);
                            try {
                                _data.writeByteArray(message);
                            } catch (Throwable th2) {
                                th = th2;
                                _reply.recycle();
                                _data.recycle();
                                throw th;
                            }
                        } catch (Throwable th3) {
                            th = th3;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th4) {
                        th = th4;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(messageId);
                        try {
                            _data.writeInt(retryCount);
                            if (this.mRemote.transact(9, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                _reply.recycle();
                                _data.recycle();
                                return;
                            }
                            Stub.getDefaultImpl().sendMessage(clientId, discoverySessionId, peerId, message, messageId, retryCount);
                            _reply.recycle();
                            _data.recycle();
                        } catch (Throwable th5) {
                            th = th5;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th6) {
                        th = th6;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th7) {
                    th = th7;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // android.net.wifi.aware.IWifiAwareManager
            public void terminateSession(int clientId, int discoverySessionId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(clientId);
                    _data.writeInt(discoverySessionId);
                    if (this.mRemote.transact(10, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().terminateSession(clientId, discoverySessionId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.aware.IWifiAwareManager
            public void requestMacAddresses(int uid, List peerIds, IWifiAwareMacAddressProvider callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeList(peerIds);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (this.mRemote.transact(11, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().requestMacAddresses(uid, peerIds, callback);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IWifiAwareManager impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IWifiAwareManager getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
