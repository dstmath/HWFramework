package android.service.media;

import android.content.pm.ParceledListSlice;
import android.media.session.MediaSession.Token;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IMediaBrowserServiceCallbacks extends IInterface {

    public static abstract class Stub extends Binder implements IMediaBrowserServiceCallbacks {
        private static final String DESCRIPTOR = "android.service.media.IMediaBrowserServiceCallbacks";
        static final int TRANSACTION_onConnect = 1;
        static final int TRANSACTION_onConnectFailed = 2;
        static final int TRANSACTION_onLoadChildren = 3;
        static final int TRANSACTION_onLoadChildrenWithOptions = 4;

        private static class Proxy implements IMediaBrowserServiceCallbacks {
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

            public void onConnect(String root, Token session, Bundle extras) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(root);
                    if (session != null) {
                        _data.writeInt(Stub.TRANSACTION_onConnect);
                        session.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (extras != null) {
                        _data.writeInt(Stub.TRANSACTION_onConnect);
                        extras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_onConnect, _data, null, Stub.TRANSACTION_onConnect);
                } finally {
                    _data.recycle();
                }
            }

            public void onConnectFailed() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_onConnectFailed, _data, null, Stub.TRANSACTION_onConnect);
                } finally {
                    _data.recycle();
                }
            }

            public void onLoadChildren(String mediaId, ParceledListSlice list) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(mediaId);
                    if (list != null) {
                        _data.writeInt(Stub.TRANSACTION_onConnect);
                        list.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_onLoadChildren, _data, null, Stub.TRANSACTION_onConnect);
                } finally {
                    _data.recycle();
                }
            }

            public void onLoadChildrenWithOptions(String mediaId, ParceledListSlice list, Bundle options) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(mediaId);
                    if (list != null) {
                        _data.writeInt(Stub.TRANSACTION_onConnect);
                        list.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (options != null) {
                        _data.writeInt(Stub.TRANSACTION_onConnect);
                        options.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_onLoadChildrenWithOptions, _data, null, Stub.TRANSACTION_onConnect);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IMediaBrowserServiceCallbacks asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IMediaBrowserServiceCallbacks)) {
                return new Proxy(obj);
            }
            return (IMediaBrowserServiceCallbacks) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            String _arg0;
            Bundle bundle;
            ParceledListSlice parceledListSlice;
            switch (code) {
                case TRANSACTION_onConnect /*1*/:
                    Token token;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    if (data.readInt() != 0) {
                        token = (Token) Token.CREATOR.createFromParcel(data);
                    } else {
                        token = null;
                    }
                    if (data.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle = null;
                    }
                    onConnect(_arg0, token, bundle);
                    return true;
                case TRANSACTION_onConnectFailed /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    onConnectFailed();
                    return true;
                case TRANSACTION_onLoadChildren /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    if (data.readInt() != 0) {
                        parceledListSlice = (ParceledListSlice) ParceledListSlice.CREATOR.createFromParcel(data);
                    } else {
                        parceledListSlice = null;
                    }
                    onLoadChildren(_arg0, parceledListSlice);
                    return true;
                case TRANSACTION_onLoadChildrenWithOptions /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    if (data.readInt() != 0) {
                        parceledListSlice = (ParceledListSlice) ParceledListSlice.CREATOR.createFromParcel(data);
                    } else {
                        parceledListSlice = null;
                    }
                    if (data.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle = null;
                    }
                    onLoadChildrenWithOptions(_arg0, parceledListSlice, bundle);
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void onConnect(String str, Token token, Bundle bundle) throws RemoteException;

    void onConnectFailed() throws RemoteException;

    void onLoadChildren(String str, ParceledListSlice parceledListSlice) throws RemoteException;

    void onLoadChildrenWithOptions(String str, ParceledListSlice parceledListSlice, Bundle bundle) throws RemoteException;
}
