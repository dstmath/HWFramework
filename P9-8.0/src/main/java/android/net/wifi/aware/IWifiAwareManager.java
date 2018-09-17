package android.net.wifi.aware;

import android.net.wifi.RttManager.ParcelableRttParams;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IWifiAwareManager extends IInterface {

    public static abstract class Stub extends Binder implements IWifiAwareManager {
        private static final String DESCRIPTOR = "android.net.wifi.aware.IWifiAwareManager";
        static final int TRANSACTION_connect = 3;
        static final int TRANSACTION_disconnect = 4;
        static final int TRANSACTION_getCharacteristics = 2;
        static final int TRANSACTION_isUsageEnabled = 1;
        static final int TRANSACTION_publish = 5;
        static final int TRANSACTION_sendMessage = 9;
        static final int TRANSACTION_startRanging = 11;
        static final int TRANSACTION_subscribe = 6;
        static final int TRANSACTION_terminateSession = 10;
        static final int TRANSACTION_updatePublish = 7;
        static final int TRANSACTION_updateSubscribe = 8;

        private static class Proxy implements IWifiAwareManager {
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

            public boolean isUsageEnabled() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public Characteristics getCharacteristics() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    Characteristics _result;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (Characteristics) Characteristics.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void connect(IBinder binder, String callingPackage, IWifiAwareEventCallback callback, ConfigRequest configRequest, boolean notifyOnIdentityChanged) throws RemoteException {
                int i = 1;
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(binder);
                    _data.writeString(callingPackage);
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
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
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void disconnect(int clientId, IBinder binder) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(clientId);
                    _data.writeStrongBinder(binder);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void publish(int clientId, PublishConfig publishConfig, IWifiAwareDiscoverySessionCallback callback) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(clientId);
                    if (publishConfig != null) {
                        _data.writeInt(1);
                        publishConfig.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void subscribe(int clientId, SubscribeConfig subscribeConfig, IWifiAwareDiscoverySessionCallback callback) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(clientId);
                    if (subscribeConfig != null) {
                        _data.writeInt(1);
                        subscribeConfig.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

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
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

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
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void sendMessage(int clientId, int discoverySessionId, int peerId, byte[] message, int messageId, int retryCount) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(clientId);
                    _data.writeInt(discoverySessionId);
                    _data.writeInt(peerId);
                    _data.writeByteArray(message);
                    _data.writeInt(messageId);
                    _data.writeInt(retryCount);
                    this.mRemote.transact(9, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void terminateSession(int clientId, int discoverySessionId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(clientId);
                    _data.writeInt(discoverySessionId);
                    this.mRemote.transact(10, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int startRanging(int clientId, int discoverySessionId, ParcelableRttParams parms) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(clientId);
                    _data.writeInt(discoverySessionId);
                    if (parms != null) {
                        _data.writeInt(1);
                        parms.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(11, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

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

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            int _arg0;
            int _arg1;
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    boolean _result = isUsageEnabled();
                    reply.writeNoException();
                    reply.writeInt(_result ? 1 : 0);
                    return true;
                case 2:
                    data.enforceInterface(DESCRIPTOR);
                    Characteristics _result2 = getCharacteristics();
                    reply.writeNoException();
                    if (_result2 != null) {
                        reply.writeInt(1);
                        _result2.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case 3:
                    ConfigRequest _arg3;
                    data.enforceInterface(DESCRIPTOR);
                    IBinder _arg02 = data.readStrongBinder();
                    String _arg12 = data.readString();
                    IWifiAwareEventCallback _arg2 = android.net.wifi.aware.IWifiAwareEventCallback.Stub.asInterface(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        _arg3 = (ConfigRequest) ConfigRequest.CREATOR.createFromParcel(data);
                    } else {
                        _arg3 = null;
                    }
                    connect(_arg02, _arg12, _arg2, _arg3, data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case 4:
                    data.enforceInterface(DESCRIPTOR);
                    disconnect(data.readInt(), data.readStrongBinder());
                    reply.writeNoException();
                    return true;
                case 5:
                    PublishConfig _arg13;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readInt();
                    if (data.readInt() != 0) {
                        _arg13 = (PublishConfig) PublishConfig.CREATOR.createFromParcel(data);
                    } else {
                        _arg13 = null;
                    }
                    publish(_arg0, _arg13, android.net.wifi.aware.IWifiAwareDiscoverySessionCallback.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case 6:
                    SubscribeConfig _arg14;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readInt();
                    if (data.readInt() != 0) {
                        _arg14 = (SubscribeConfig) SubscribeConfig.CREATOR.createFromParcel(data);
                    } else {
                        _arg14 = null;
                    }
                    subscribe(_arg0, _arg14, android.net.wifi.aware.IWifiAwareDiscoverySessionCallback.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case 7:
                    PublishConfig _arg22;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readInt();
                    _arg1 = data.readInt();
                    if (data.readInt() != 0) {
                        _arg22 = (PublishConfig) PublishConfig.CREATOR.createFromParcel(data);
                    } else {
                        _arg22 = null;
                    }
                    updatePublish(_arg0, _arg1, _arg22);
                    reply.writeNoException();
                    return true;
                case 8:
                    SubscribeConfig _arg23;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readInt();
                    _arg1 = data.readInt();
                    if (data.readInt() != 0) {
                        _arg23 = (SubscribeConfig) SubscribeConfig.CREATOR.createFromParcel(data);
                    } else {
                        _arg23 = null;
                    }
                    updateSubscribe(_arg0, _arg1, _arg23);
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
                    ParcelableRttParams _arg24;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readInt();
                    _arg1 = data.readInt();
                    if (data.readInt() != 0) {
                        _arg24 = (ParcelableRttParams) ParcelableRttParams.CREATOR.createFromParcel(data);
                    } else {
                        _arg24 = null;
                    }
                    int _result3 = startRanging(_arg0, _arg1, _arg24);
                    reply.writeNoException();
                    reply.writeInt(_result3);
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void connect(IBinder iBinder, String str, IWifiAwareEventCallback iWifiAwareEventCallback, ConfigRequest configRequest, boolean z) throws RemoteException;

    void disconnect(int i, IBinder iBinder) throws RemoteException;

    Characteristics getCharacteristics() throws RemoteException;

    boolean isUsageEnabled() throws RemoteException;

    void publish(int i, PublishConfig publishConfig, IWifiAwareDiscoverySessionCallback iWifiAwareDiscoverySessionCallback) throws RemoteException;

    void sendMessage(int i, int i2, int i3, byte[] bArr, int i4, int i5) throws RemoteException;

    int startRanging(int i, int i2, ParcelableRttParams parcelableRttParams) throws RemoteException;

    void subscribe(int i, SubscribeConfig subscribeConfig, IWifiAwareDiscoverySessionCallback iWifiAwareDiscoverySessionCallback) throws RemoteException;

    void terminateSession(int i, int i2) throws RemoteException;

    void updatePublish(int i, int i2, PublishConfig publishConfig) throws RemoteException;

    void updateSubscribe(int i, int i2, SubscribeConfig subscribeConfig) throws RemoteException;
}
