package android.service.media;

import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ResultReceiver;

public interface IMediaBrowserService extends IInterface {

    public static abstract class Stub extends Binder implements IMediaBrowserService {
        private static final String DESCRIPTOR = "android.service.media.IMediaBrowserService";
        static final int TRANSACTION_addSubscription = 6;
        static final int TRANSACTION_addSubscriptionDeprecated = 3;
        static final int TRANSACTION_connect = 1;
        static final int TRANSACTION_disconnect = 2;
        static final int TRANSACTION_getMediaItem = 5;
        static final int TRANSACTION_removeSubscription = 7;
        static final int TRANSACTION_removeSubscriptionDeprecated = 4;

        private static class Proxy implements IMediaBrowserService {
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

            public void connect(String pkg, Bundle rootHints, IMediaBrowserServiceCallbacks callbacks) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    if (rootHints != null) {
                        _data.writeInt(Stub.TRANSACTION_connect);
                        rootHints.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (callbacks != null) {
                        iBinder = callbacks.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_connect, _data, null, Stub.TRANSACTION_connect);
                } finally {
                    _data.recycle();
                }
            }

            public void disconnect(IMediaBrowserServiceCallbacks callbacks) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (callbacks != null) {
                        iBinder = callbacks.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_disconnect, _data, null, Stub.TRANSACTION_connect);
                } finally {
                    _data.recycle();
                }
            }

            public void addSubscriptionDeprecated(String uri, IMediaBrowserServiceCallbacks callbacks) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(uri);
                    if (callbacks != null) {
                        iBinder = callbacks.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_addSubscriptionDeprecated, _data, null, Stub.TRANSACTION_connect);
                } finally {
                    _data.recycle();
                }
            }

            public void removeSubscriptionDeprecated(String uri, IMediaBrowserServiceCallbacks callbacks) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(uri);
                    if (callbacks != null) {
                        iBinder = callbacks.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_removeSubscriptionDeprecated, _data, null, Stub.TRANSACTION_connect);
                } finally {
                    _data.recycle();
                }
            }

            public void getMediaItem(String uri, ResultReceiver cb, IMediaBrowserServiceCallbacks callbacks) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(uri);
                    if (cb != null) {
                        _data.writeInt(Stub.TRANSACTION_connect);
                        cb.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (callbacks != null) {
                        iBinder = callbacks.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_getMediaItem, _data, null, Stub.TRANSACTION_connect);
                } finally {
                    _data.recycle();
                }
            }

            public void addSubscription(String uri, IBinder token, Bundle options, IMediaBrowserServiceCallbacks callbacks) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(uri);
                    _data.writeStrongBinder(token);
                    if (options != null) {
                        _data.writeInt(Stub.TRANSACTION_connect);
                        options.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (callbacks != null) {
                        iBinder = callbacks.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_addSubscription, _data, null, Stub.TRANSACTION_connect);
                } finally {
                    _data.recycle();
                }
            }

            public void removeSubscription(String uri, IBinder token, IMediaBrowserServiceCallbacks callbacks) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(uri);
                    _data.writeStrongBinder(token);
                    if (callbacks != null) {
                        iBinder = callbacks.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_removeSubscription, _data, null, Stub.TRANSACTION_connect);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IMediaBrowserService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IMediaBrowserService)) {
                return new Proxy(obj);
            }
            return (IMediaBrowserService) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            String _arg0;
            switch (code) {
                case TRANSACTION_connect /*1*/:
                    Bundle bundle;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    if (data.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle = null;
                    }
                    connect(_arg0, bundle, android.service.media.IMediaBrowserServiceCallbacks.Stub.asInterface(data.readStrongBinder()));
                    return true;
                case TRANSACTION_disconnect /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    disconnect(android.service.media.IMediaBrowserServiceCallbacks.Stub.asInterface(data.readStrongBinder()));
                    return true;
                case TRANSACTION_addSubscriptionDeprecated /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    addSubscriptionDeprecated(data.readString(), android.service.media.IMediaBrowserServiceCallbacks.Stub.asInterface(data.readStrongBinder()));
                    return true;
                case TRANSACTION_removeSubscriptionDeprecated /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    removeSubscriptionDeprecated(data.readString(), android.service.media.IMediaBrowserServiceCallbacks.Stub.asInterface(data.readStrongBinder()));
                    return true;
                case TRANSACTION_getMediaItem /*5*/:
                    ResultReceiver resultReceiver;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    if (data.readInt() != 0) {
                        resultReceiver = (ResultReceiver) ResultReceiver.CREATOR.createFromParcel(data);
                    } else {
                        resultReceiver = null;
                    }
                    getMediaItem(_arg0, resultReceiver, android.service.media.IMediaBrowserServiceCallbacks.Stub.asInterface(data.readStrongBinder()));
                    return true;
                case TRANSACTION_addSubscription /*6*/:
                    Bundle bundle2;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    IBinder _arg1 = data.readStrongBinder();
                    if (data.readInt() != 0) {
                        bundle2 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle2 = null;
                    }
                    addSubscription(_arg0, _arg1, bundle2, android.service.media.IMediaBrowserServiceCallbacks.Stub.asInterface(data.readStrongBinder()));
                    return true;
                case TRANSACTION_removeSubscription /*7*/:
                    data.enforceInterface(DESCRIPTOR);
                    removeSubscription(data.readString(), data.readStrongBinder(), android.service.media.IMediaBrowserServiceCallbacks.Stub.asInterface(data.readStrongBinder()));
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void addSubscription(String str, IBinder iBinder, Bundle bundle, IMediaBrowserServiceCallbacks iMediaBrowserServiceCallbacks) throws RemoteException;

    void addSubscriptionDeprecated(String str, IMediaBrowserServiceCallbacks iMediaBrowserServiceCallbacks) throws RemoteException;

    void connect(String str, Bundle bundle, IMediaBrowserServiceCallbacks iMediaBrowserServiceCallbacks) throws RemoteException;

    void disconnect(IMediaBrowserServiceCallbacks iMediaBrowserServiceCallbacks) throws RemoteException;

    void getMediaItem(String str, ResultReceiver resultReceiver, IMediaBrowserServiceCallbacks iMediaBrowserServiceCallbacks) throws RemoteException;

    void removeSubscription(String str, IBinder iBinder, IMediaBrowserServiceCallbacks iMediaBrowserServiceCallbacks) throws RemoteException;

    void removeSubscriptionDeprecated(String str, IMediaBrowserServiceCallbacks iMediaBrowserServiceCallbacks) throws RemoteException;
}
