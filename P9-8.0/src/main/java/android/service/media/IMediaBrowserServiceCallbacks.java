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
                        _data.writeInt(1);
                        session.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (extras != null) {
                        _data.writeInt(1);
                        extras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onConnectFailed() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(2, _data, null, 1);
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
                        _data.writeInt(1);
                        list.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(3, _data, null, 1);
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
                        _data.writeInt(1);
                        list.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (options != null) {
                        _data.writeInt(1);
                        options.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(4, _data, null, 1);
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
            Bundle _arg2;
            ParceledListSlice _arg1;
            switch (code) {
                case 1:
                    Token _arg12;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    if (data.readInt() != 0) {
                        _arg12 = (Token) Token.CREATOR.createFromParcel(data);
                    } else {
                        _arg12 = null;
                    }
                    if (data.readInt() != 0) {
                        _arg2 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        _arg2 = null;
                    }
                    onConnect(_arg0, _arg12, _arg2);
                    return true;
                case 2:
                    data.enforceInterface(DESCRIPTOR);
                    onConnectFailed();
                    return true;
                case 3:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    if (data.readInt() != 0) {
                        _arg1 = (ParceledListSlice) ParceledListSlice.CREATOR.createFromParcel(data);
                    } else {
                        _arg1 = null;
                    }
                    onLoadChildren(_arg0, _arg1);
                    return true;
                case 4:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    if (data.readInt() != 0) {
                        _arg1 = (ParceledListSlice) ParceledListSlice.CREATOR.createFromParcel(data);
                    } else {
                        _arg1 = null;
                    }
                    if (data.readInt() != 0) {
                        _arg2 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        _arg2 = null;
                    }
                    onLoadChildrenWithOptions(_arg0, _arg1, _arg2);
                    return true;
                case 1598968902:
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
