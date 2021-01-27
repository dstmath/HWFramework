package android.service.media;

import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.service.media.IMediaBrowserServiceCallbacks;

public interface IMediaBrowserService extends IInterface {
    void addSubscription(String str, IBinder iBinder, Bundle bundle, IMediaBrowserServiceCallbacks iMediaBrowserServiceCallbacks) throws RemoteException;

    void addSubscriptionDeprecated(String str, IMediaBrowserServiceCallbacks iMediaBrowserServiceCallbacks) throws RemoteException;

    void connect(String str, Bundle bundle, IMediaBrowserServiceCallbacks iMediaBrowserServiceCallbacks) throws RemoteException;

    void disconnect(IMediaBrowserServiceCallbacks iMediaBrowserServiceCallbacks) throws RemoteException;

    void getMediaItem(String str, ResultReceiver resultReceiver, IMediaBrowserServiceCallbacks iMediaBrowserServiceCallbacks) throws RemoteException;

    void removeSubscription(String str, IBinder iBinder, IMediaBrowserServiceCallbacks iMediaBrowserServiceCallbacks) throws RemoteException;

    void removeSubscriptionDeprecated(String str, IMediaBrowserServiceCallbacks iMediaBrowserServiceCallbacks) throws RemoteException;

    public static class Default implements IMediaBrowserService {
        @Override // android.service.media.IMediaBrowserService
        public void connect(String pkg, Bundle rootHints, IMediaBrowserServiceCallbacks callbacks) throws RemoteException {
        }

        @Override // android.service.media.IMediaBrowserService
        public void disconnect(IMediaBrowserServiceCallbacks callbacks) throws RemoteException {
        }

        @Override // android.service.media.IMediaBrowserService
        public void addSubscriptionDeprecated(String uri, IMediaBrowserServiceCallbacks callbacks) throws RemoteException {
        }

        @Override // android.service.media.IMediaBrowserService
        public void removeSubscriptionDeprecated(String uri, IMediaBrowserServiceCallbacks callbacks) throws RemoteException {
        }

        @Override // android.service.media.IMediaBrowserService
        public void getMediaItem(String uri, ResultReceiver cb, IMediaBrowserServiceCallbacks callbacks) throws RemoteException {
        }

        @Override // android.service.media.IMediaBrowserService
        public void addSubscription(String uri, IBinder token, Bundle options, IMediaBrowserServiceCallbacks callbacks) throws RemoteException {
        }

        @Override // android.service.media.IMediaBrowserService
        public void removeSubscription(String uri, IBinder token, IMediaBrowserServiceCallbacks callbacks) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IMediaBrowserService {
        private static final String DESCRIPTOR = "android.service.media.IMediaBrowserService";
        static final int TRANSACTION_addSubscription = 6;
        static final int TRANSACTION_addSubscriptionDeprecated = 3;
        static final int TRANSACTION_connect = 1;
        static final int TRANSACTION_disconnect = 2;
        static final int TRANSACTION_getMediaItem = 5;
        static final int TRANSACTION_removeSubscription = 7;
        static final int TRANSACTION_removeSubscriptionDeprecated = 4;

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

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "connect";
                case 2:
                    return "disconnect";
                case 3:
                    return "addSubscriptionDeprecated";
                case 4:
                    return "removeSubscriptionDeprecated";
                case 5:
                    return "getMediaItem";
                case 6:
                    return "addSubscription";
                case 7:
                    return "removeSubscription";
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
            Bundle _arg1;
            ResultReceiver _arg12;
            Bundle _arg2;
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg0 = data.readString();
                        if (data.readInt() != 0) {
                            _arg1 = Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg1 = null;
                        }
                        connect(_arg0, _arg1, IMediaBrowserServiceCallbacks.Stub.asInterface(data.readStrongBinder()));
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        disconnect(IMediaBrowserServiceCallbacks.Stub.asInterface(data.readStrongBinder()));
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        addSubscriptionDeprecated(data.readString(), IMediaBrowserServiceCallbacks.Stub.asInterface(data.readStrongBinder()));
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        removeSubscriptionDeprecated(data.readString(), IMediaBrowserServiceCallbacks.Stub.asInterface(data.readStrongBinder()));
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg02 = data.readString();
                        if (data.readInt() != 0) {
                            _arg12 = ResultReceiver.CREATOR.createFromParcel(data);
                        } else {
                            _arg12 = null;
                        }
                        getMediaItem(_arg02, _arg12, IMediaBrowserServiceCallbacks.Stub.asInterface(data.readStrongBinder()));
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg03 = data.readString();
                        IBinder _arg13 = data.readStrongBinder();
                        if (data.readInt() != 0) {
                            _arg2 = Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg2 = null;
                        }
                        addSubscription(_arg03, _arg13, _arg2, IMediaBrowserServiceCallbacks.Stub.asInterface(data.readStrongBinder()));
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        removeSubscription(data.readString(), data.readStrongBinder(), IMediaBrowserServiceCallbacks.Stub.asInterface(data.readStrongBinder()));
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
        public static class Proxy implements IMediaBrowserService {
            public static IMediaBrowserService sDefaultImpl;
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

            @Override // android.service.media.IMediaBrowserService
            public void connect(String pkg, Bundle rootHints, IMediaBrowserServiceCallbacks callbacks) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    if (rootHints != null) {
                        _data.writeInt(1);
                        rootHints.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(callbacks != null ? callbacks.asBinder() : null);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().connect(pkg, rootHints, callbacks);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.service.media.IMediaBrowserService
            public void disconnect(IMediaBrowserServiceCallbacks callbacks) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callbacks != null ? callbacks.asBinder() : null);
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().disconnect(callbacks);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.service.media.IMediaBrowserService
            public void addSubscriptionDeprecated(String uri, IMediaBrowserServiceCallbacks callbacks) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(uri);
                    _data.writeStrongBinder(callbacks != null ? callbacks.asBinder() : null);
                    if (this.mRemote.transact(3, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().addSubscriptionDeprecated(uri, callbacks);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.service.media.IMediaBrowserService
            public void removeSubscriptionDeprecated(String uri, IMediaBrowserServiceCallbacks callbacks) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(uri);
                    _data.writeStrongBinder(callbacks != null ? callbacks.asBinder() : null);
                    if (this.mRemote.transact(4, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().removeSubscriptionDeprecated(uri, callbacks);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.service.media.IMediaBrowserService
            public void getMediaItem(String uri, ResultReceiver cb, IMediaBrowserServiceCallbacks callbacks) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(uri);
                    if (cb != null) {
                        _data.writeInt(1);
                        cb.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(callbacks != null ? callbacks.asBinder() : null);
                    if (this.mRemote.transact(5, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().getMediaItem(uri, cb, callbacks);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.service.media.IMediaBrowserService
            public void addSubscription(String uri, IBinder token, Bundle options, IMediaBrowserServiceCallbacks callbacks) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(uri);
                    _data.writeStrongBinder(token);
                    if (options != null) {
                        _data.writeInt(1);
                        options.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(callbacks != null ? callbacks.asBinder() : null);
                    if (this.mRemote.transact(6, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().addSubscription(uri, token, options, callbacks);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.service.media.IMediaBrowserService
            public void removeSubscription(String uri, IBinder token, IMediaBrowserServiceCallbacks callbacks) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(uri);
                    _data.writeStrongBinder(token);
                    _data.writeStrongBinder(callbacks != null ? callbacks.asBinder() : null);
                    if (this.mRemote.transact(7, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().removeSubscription(uri, token, callbacks);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IMediaBrowserService impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IMediaBrowserService getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
