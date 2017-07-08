package android.media;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IMediaRouterService extends IInterface {

    public static abstract class Stub extends Binder implements IMediaRouterService {
        private static final String DESCRIPTOR = "android.media.IMediaRouterService";
        static final int TRANSACTION_getState = 3;
        static final int TRANSACTION_registerClientAsUser = 1;
        static final int TRANSACTION_requestSetVolume = 6;
        static final int TRANSACTION_requestUpdateVolume = 7;
        static final int TRANSACTION_setDiscoveryRequest = 4;
        static final int TRANSACTION_setSelectedRoute = 5;
        static final int TRANSACTION_unregisterClient = 2;

        private static class Proxy implements IMediaRouterService {
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

            public void registerClientAsUser(IMediaRouterClient client, String packageName, int userId) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (client != null) {
                        iBinder = client.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeString(packageName);
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_registerClientAsUser, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void unregisterClient(IMediaRouterClient client) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (client != null) {
                        iBinder = client.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_unregisterClient, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public MediaRouterClientState getState(IMediaRouterClient client) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    MediaRouterClientState mediaRouterClientState;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (client != null) {
                        iBinder = client.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_getState, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        mediaRouterClientState = (MediaRouterClientState) MediaRouterClientState.CREATOR.createFromParcel(_reply);
                    } else {
                        mediaRouterClientState = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return mediaRouterClientState;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setDiscoveryRequest(IMediaRouterClient client, int routeTypes, boolean activeScan) throws RemoteException {
                IBinder iBinder = null;
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (client != null) {
                        iBinder = client.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeInt(routeTypes);
                    if (activeScan) {
                        i = Stub.TRANSACTION_registerClientAsUser;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_setDiscoveryRequest, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setSelectedRoute(IMediaRouterClient client, String routeId, boolean explicit) throws RemoteException {
                IBinder iBinder = null;
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (client != null) {
                        iBinder = client.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeString(routeId);
                    if (explicit) {
                        i = Stub.TRANSACTION_registerClientAsUser;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_setSelectedRoute, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void requestSetVolume(IMediaRouterClient client, String routeId, int volume) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (client != null) {
                        iBinder = client.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeString(routeId);
                    _data.writeInt(volume);
                    this.mRemote.transact(Stub.TRANSACTION_requestSetVolume, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void requestUpdateVolume(IMediaRouterClient client, String routeId, int direction) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (client != null) {
                        iBinder = client.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeString(routeId);
                    _data.writeInt(direction);
                    this.mRemote.transact(Stub.TRANSACTION_requestUpdateVolume, _data, _reply, 0);
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

        public static IMediaRouterService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IMediaRouterService)) {
                return new Proxy(obj);
            }
            return (IMediaRouterService) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            boolean _arg2 = false;
            IMediaRouterClient _arg0;
            switch (code) {
                case TRANSACTION_registerClientAsUser /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    registerClientAsUser(android.media.IMediaRouterClient.Stub.asInterface(data.readStrongBinder()), data.readString(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_unregisterClient /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    unregisterClient(android.media.IMediaRouterClient.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getState /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    MediaRouterClientState _result = getState(android.media.IMediaRouterClient.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    if (_result != null) {
                        reply.writeInt(TRANSACTION_registerClientAsUser);
                        _result.writeToParcel(reply, TRANSACTION_registerClientAsUser);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_setDiscoveryRequest /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = android.media.IMediaRouterClient.Stub.asInterface(data.readStrongBinder());
                    int _arg1 = data.readInt();
                    if (data.readInt() != 0) {
                        _arg2 = true;
                    }
                    setDiscoveryRequest(_arg0, _arg1, _arg2);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setSelectedRoute /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = android.media.IMediaRouterClient.Stub.asInterface(data.readStrongBinder());
                    String _arg12 = data.readString();
                    if (data.readInt() != 0) {
                        _arg2 = true;
                    }
                    setSelectedRoute(_arg0, _arg12, _arg2);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_requestSetVolume /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    requestSetVolume(android.media.IMediaRouterClient.Stub.asInterface(data.readStrongBinder()), data.readString(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_requestUpdateVolume /*7*/:
                    data.enforceInterface(DESCRIPTOR);
                    requestUpdateVolume(android.media.IMediaRouterClient.Stub.asInterface(data.readStrongBinder()), data.readString(), data.readInt());
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

    MediaRouterClientState getState(IMediaRouterClient iMediaRouterClient) throws RemoteException;

    void registerClientAsUser(IMediaRouterClient iMediaRouterClient, String str, int i) throws RemoteException;

    void requestSetVolume(IMediaRouterClient iMediaRouterClient, String str, int i) throws RemoteException;

    void requestUpdateVolume(IMediaRouterClient iMediaRouterClient, String str, int i) throws RemoteException;

    void setDiscoveryRequest(IMediaRouterClient iMediaRouterClient, int i, boolean z) throws RemoteException;

    void setSelectedRoute(IMediaRouterClient iMediaRouterClient, String str, boolean z) throws RemoteException;

    void unregisterClient(IMediaRouterClient iMediaRouterClient) throws RemoteException;
}
