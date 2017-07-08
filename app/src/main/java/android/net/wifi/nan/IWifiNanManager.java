package android.net.wifi.nan;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IWifiNanManager extends IInterface {

    public static abstract class Stub extends Binder implements IWifiNanManager {
        private static final String DESCRIPTOR = "android.net.wifi.nan.IWifiNanManager";
        static final int TRANSACTION_connect = 1;
        static final int TRANSACTION_createSession = 4;
        static final int TRANSACTION_destroySession = 9;
        static final int TRANSACTION_disconnect = 2;
        static final int TRANSACTION_publish = 5;
        static final int TRANSACTION_requestConfig = 3;
        static final int TRANSACTION_sendMessage = 7;
        static final int TRANSACTION_stopSession = 8;
        static final int TRANSACTION_subscribe = 6;

        private static class Proxy implements IWifiNanManager {
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

            public void connect(IBinder binder, IWifiNanEventListener listener, int events) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(binder);
                    if (listener != null) {
                        iBinder = listener.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeInt(events);
                    this.mRemote.transact(Stub.TRANSACTION_connect, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void disconnect(IBinder binder) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(binder);
                    this.mRemote.transact(Stub.TRANSACTION_disconnect, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void requestConfig(ConfigRequest configRequest) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (configRequest != null) {
                        _data.writeInt(Stub.TRANSACTION_connect);
                        configRequest.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_requestConfig, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int createSession(IWifiNanSessionListener listener, int events) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (listener != null) {
                        iBinder = listener.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeInt(events);
                    this.mRemote.transact(Stub.TRANSACTION_createSession, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void publish(int sessionId, PublishData publishData, PublishSettings publishSettings) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(sessionId);
                    if (publishData != null) {
                        _data.writeInt(Stub.TRANSACTION_connect);
                        publishData.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (publishSettings != null) {
                        _data.writeInt(Stub.TRANSACTION_connect);
                        publishSettings.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_publish, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void subscribe(int sessionId, SubscribeData subscribeData, SubscribeSettings subscribeSettings) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(sessionId);
                    if (subscribeData != null) {
                        _data.writeInt(Stub.TRANSACTION_connect);
                        subscribeData.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (subscribeSettings != null) {
                        _data.writeInt(Stub.TRANSACTION_connect);
                        subscribeSettings.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_subscribe, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void sendMessage(int sessionId, int peerId, byte[] message, int messageLength, int messageId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(sessionId);
                    _data.writeInt(peerId);
                    _data.writeByteArray(message);
                    _data.writeInt(messageLength);
                    _data.writeInt(messageId);
                    this.mRemote.transact(Stub.TRANSACTION_sendMessage, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void stopSession(int sessionId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(sessionId);
                    this.mRemote.transact(Stub.TRANSACTION_stopSession, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void destroySession(int sessionId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(sessionId);
                    this.mRemote.transact(Stub.TRANSACTION_destroySession, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IWifiNanManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IWifiNanManager)) {
                return new Proxy(obj);
            }
            return (IWifiNanManager) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            int _arg0;
            switch (code) {
                case TRANSACTION_connect /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    connect(data.readStrongBinder(), android.net.wifi.nan.IWifiNanEventListener.Stub.asInterface(data.readStrongBinder()), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_disconnect /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    disconnect(data.readStrongBinder());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_requestConfig /*3*/:
                    ConfigRequest configRequest;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        configRequest = (ConfigRequest) ConfigRequest.CREATOR.createFromParcel(data);
                    } else {
                        configRequest = null;
                    }
                    requestConfig(configRequest);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_createSession /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    int _result = createSession(android.net.wifi.nan.IWifiNanSessionListener.Stub.asInterface(data.readStrongBinder()), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_publish /*5*/:
                    PublishData publishData;
                    PublishSettings publishSettings;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readInt();
                    if (data.readInt() != 0) {
                        publishData = (PublishData) PublishData.CREATOR.createFromParcel(data);
                    } else {
                        publishData = null;
                    }
                    if (data.readInt() != 0) {
                        publishSettings = (PublishSettings) PublishSettings.CREATOR.createFromParcel(data);
                    } else {
                        publishSettings = null;
                    }
                    publish(_arg0, publishData, publishSettings);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_subscribe /*6*/:
                    SubscribeData subscribeData;
                    SubscribeSettings subscribeSettings;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readInt();
                    if (data.readInt() != 0) {
                        subscribeData = (SubscribeData) SubscribeData.CREATOR.createFromParcel(data);
                    } else {
                        subscribeData = null;
                    }
                    if (data.readInt() != 0) {
                        subscribeSettings = (SubscribeSettings) SubscribeSettings.CREATOR.createFromParcel(data);
                    } else {
                        subscribeSettings = null;
                    }
                    subscribe(_arg0, subscribeData, subscribeSettings);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_sendMessage /*7*/:
                    data.enforceInterface(DESCRIPTOR);
                    sendMessage(data.readInt(), data.readInt(), data.createByteArray(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_stopSession /*8*/:
                    data.enforceInterface(DESCRIPTOR);
                    stopSession(data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_destroySession /*9*/:
                    data.enforceInterface(DESCRIPTOR);
                    destroySession(data.readInt());
                    reply.writeNoException();
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void connect(IBinder iBinder, IWifiNanEventListener iWifiNanEventListener, int i) throws RemoteException;

    int createSession(IWifiNanSessionListener iWifiNanSessionListener, int i) throws RemoteException;

    void destroySession(int i) throws RemoteException;

    void disconnect(IBinder iBinder) throws RemoteException;

    void publish(int i, PublishData publishData, PublishSettings publishSettings) throws RemoteException;

    void requestConfig(ConfigRequest configRequest) throws RemoteException;

    void sendMessage(int i, int i2, byte[] bArr, int i3, int i4) throws RemoteException;

    void stopSession(int i) throws RemoteException;

    void subscribe(int i, SubscribeData subscribeData, SubscribeSettings subscribeSettings) throws RemoteException;
}
