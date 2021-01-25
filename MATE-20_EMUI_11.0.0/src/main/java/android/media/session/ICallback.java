package android.media.session;

import android.content.ComponentName;
import android.media.session.MediaSession;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.view.KeyEvent;

public interface ICallback extends IInterface {
    void onAddressedPlayerChangedToMediaButtonReceiver(ComponentName componentName) throws RemoteException;

    void onAddressedPlayerChangedToMediaSession(MediaSession.Token token) throws RemoteException;

    void onMediaKeyEventDispatchedToMediaButtonReceiver(KeyEvent keyEvent, ComponentName componentName) throws RemoteException;

    void onMediaKeyEventDispatchedToMediaSession(KeyEvent keyEvent, MediaSession.Token token) throws RemoteException;

    public static class Default implements ICallback {
        @Override // android.media.session.ICallback
        public void onMediaKeyEventDispatchedToMediaSession(KeyEvent event, MediaSession.Token sessionToken) throws RemoteException {
        }

        @Override // android.media.session.ICallback
        public void onMediaKeyEventDispatchedToMediaButtonReceiver(KeyEvent event, ComponentName mediaButtonReceiver) throws RemoteException {
        }

        @Override // android.media.session.ICallback
        public void onAddressedPlayerChangedToMediaSession(MediaSession.Token sessionToken) throws RemoteException {
        }

        @Override // android.media.session.ICallback
        public void onAddressedPlayerChangedToMediaButtonReceiver(ComponentName mediaButtonReceiver) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements ICallback {
        private static final String DESCRIPTOR = "android.media.session.ICallback";
        static final int TRANSACTION_onAddressedPlayerChangedToMediaButtonReceiver = 4;
        static final int TRANSACTION_onAddressedPlayerChangedToMediaSession = 3;
        static final int TRANSACTION_onMediaKeyEventDispatchedToMediaButtonReceiver = 2;
        static final int TRANSACTION_onMediaKeyEventDispatchedToMediaSession = 1;

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

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "onMediaKeyEventDispatchedToMediaSession";
            }
            if (transactionCode == 2) {
                return "onMediaKeyEventDispatchedToMediaButtonReceiver";
            }
            if (transactionCode == 3) {
                return "onAddressedPlayerChangedToMediaSession";
            }
            if (transactionCode != 4) {
                return null;
            }
            return "onAddressedPlayerChangedToMediaButtonReceiver";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            KeyEvent _arg0;
            MediaSession.Token _arg1;
            KeyEvent _arg02;
            ComponentName _arg12;
            MediaSession.Token _arg03;
            ComponentName _arg04;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg0 = KeyEvent.CREATOR.createFromParcel(data);
                } else {
                    _arg0 = null;
                }
                if (data.readInt() != 0) {
                    _arg1 = MediaSession.Token.CREATOR.createFromParcel(data);
                } else {
                    _arg1 = null;
                }
                onMediaKeyEventDispatchedToMediaSession(_arg0, _arg1);
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg02 = KeyEvent.CREATOR.createFromParcel(data);
                } else {
                    _arg02 = null;
                }
                if (data.readInt() != 0) {
                    _arg12 = ComponentName.CREATOR.createFromParcel(data);
                } else {
                    _arg12 = null;
                }
                onMediaKeyEventDispatchedToMediaButtonReceiver(_arg02, _arg12);
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg03 = MediaSession.Token.CREATOR.createFromParcel(data);
                } else {
                    _arg03 = null;
                }
                onAddressedPlayerChangedToMediaSession(_arg03);
                return true;
            } else if (code == 4) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg04 = ComponentName.CREATOR.createFromParcel(data);
                } else {
                    _arg04 = null;
                }
                onAddressedPlayerChangedToMediaButtonReceiver(_arg04);
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements ICallback {
            public static ICallback sDefaultImpl;
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

            @Override // android.media.session.ICallback
            public void onMediaKeyEventDispatchedToMediaSession(KeyEvent event, MediaSession.Token sessionToken) throws RemoteException {
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
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onMediaKeyEventDispatchedToMediaSession(event, sessionToken);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.media.session.ICallback
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
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onMediaKeyEventDispatchedToMediaButtonReceiver(event, mediaButtonReceiver);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.media.session.ICallback
            public void onAddressedPlayerChangedToMediaSession(MediaSession.Token sessionToken) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (sessionToken != null) {
                        _data.writeInt(1);
                        sessionToken.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(3, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onAddressedPlayerChangedToMediaSession(sessionToken);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.media.session.ICallback
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
                    if (this.mRemote.transact(4, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onAddressedPlayerChangedToMediaButtonReceiver(mediaButtonReceiver);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(ICallback impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static ICallback getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
