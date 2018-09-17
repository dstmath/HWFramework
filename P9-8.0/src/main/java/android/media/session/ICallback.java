package android.media.session;

import android.content.ComponentName;
import android.media.session.MediaSession.Token;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.view.KeyEvent;

public interface ICallback extends IInterface {

    public static abstract class Stub extends Binder implements ICallback {
        private static final String DESCRIPTOR = "android.media.session.ICallback";
        static final int TRANSACTION_onAddressedPlayerChangedToMediaButtonReceiver = 4;
        static final int TRANSACTION_onAddressedPlayerChangedToMediaSession = 3;
        static final int TRANSACTION_onMediaKeyEventDispatchedToMediaButtonReceiver = 2;
        static final int TRANSACTION_onMediaKeyEventDispatchedToMediaSession = 1;

        private static class Proxy implements ICallback {
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

            public void onMediaKeyEventDispatchedToMediaSession(KeyEvent event, Token sessionToken) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (event != null) {
                        _data.writeInt(1);
                        event.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (sessionToken != null) {
                        _data.writeInt(1);
                        sessionToken.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onMediaKeyEventDispatchedToMediaButtonReceiver(KeyEvent event, ComponentName mediaButtonReceiver) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (event != null) {
                        _data.writeInt(1);
                        event.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (mediaButtonReceiver != null) {
                        _data.writeInt(1);
                        mediaButtonReceiver.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(2, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onAddressedPlayerChangedToMediaSession(Token sessionToken) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (sessionToken != null) {
                        _data.writeInt(1);
                        sessionToken.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(3, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onAddressedPlayerChangedToMediaButtonReceiver(ComponentName mediaButtonReceiver) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (mediaButtonReceiver != null) {
                        _data.writeInt(1);
                        mediaButtonReceiver.writeToParcel(_data, 0);
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

        public static ICallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ICallback)) {
                return new Proxy(obj);
            }
            return (ICallback) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            KeyEvent _arg0;
            switch (code) {
                case 1:
                    Token _arg1;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg0 = (KeyEvent) KeyEvent.CREATOR.createFromParcel(data);
                    } else {
                        _arg0 = null;
                    }
                    if (data.readInt() != 0) {
                        _arg1 = (Token) Token.CREATOR.createFromParcel(data);
                    } else {
                        _arg1 = null;
                    }
                    onMediaKeyEventDispatchedToMediaSession(_arg0, _arg1);
                    return true;
                case 2:
                    ComponentName _arg12;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg0 = (KeyEvent) KeyEvent.CREATOR.createFromParcel(data);
                    } else {
                        _arg0 = null;
                    }
                    if (data.readInt() != 0) {
                        _arg12 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                    } else {
                        _arg12 = null;
                    }
                    onMediaKeyEventDispatchedToMediaButtonReceiver(_arg0, _arg12);
                    return true;
                case 3:
                    Token _arg02;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg02 = (Token) Token.CREATOR.createFromParcel(data);
                    } else {
                        _arg02 = null;
                    }
                    onAddressedPlayerChangedToMediaSession(_arg02);
                    return true;
                case 4:
                    ComponentName _arg03;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg03 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                    } else {
                        _arg03 = null;
                    }
                    onAddressedPlayerChangedToMediaButtonReceiver(_arg03);
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void onAddressedPlayerChangedToMediaButtonReceiver(ComponentName componentName) throws RemoteException;

    void onAddressedPlayerChangedToMediaSession(Token token) throws RemoteException;

    void onMediaKeyEventDispatchedToMediaButtonReceiver(KeyEvent keyEvent, ComponentName componentName) throws RemoteException;

    void onMediaKeyEventDispatchedToMediaSession(KeyEvent keyEvent, Token token) throws RemoteException;
}
