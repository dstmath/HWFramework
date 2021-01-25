package android.media.session;

import android.app.PendingIntent;
import android.content.pm.ParceledListSlice;
import android.media.AudioAttributes;
import android.media.MediaMetadata;
import android.media.session.ISessionController;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.text.TextUtils;

public interface ISession extends IInterface {
    void destroySession() throws RemoteException;

    ISessionController getController() throws RemoteException;

    void sendEvent(String str, Bundle bundle) throws RemoteException;

    void setActive(boolean z) throws RemoteException;

    void setCurrentVolume(int i) throws RemoteException;

    void setExtras(Bundle bundle) throws RemoteException;

    void setFlags(int i) throws RemoteException;

    void setLaunchPendingIntent(PendingIntent pendingIntent) throws RemoteException;

    void setMediaButtonReceiver(PendingIntent pendingIntent) throws RemoteException;

    void setMetadata(MediaMetadata mediaMetadata, long j, String str) throws RemoteException;

    void setPlaybackState(PlaybackState playbackState) throws RemoteException;

    void setPlaybackToLocal(AudioAttributes audioAttributes) throws RemoteException;

    void setPlaybackToRemote(int i, int i2) throws RemoteException;

    void setQueue(ParceledListSlice parceledListSlice) throws RemoteException;

    void setQueueTitle(CharSequence charSequence) throws RemoteException;

    void setRatingType(int i) throws RemoteException;

    public static class Default implements ISession {
        @Override // android.media.session.ISession
        public void sendEvent(String event, Bundle data) throws RemoteException {
        }

        @Override // android.media.session.ISession
        public ISessionController getController() throws RemoteException {
            return null;
        }

        @Override // android.media.session.ISession
        public void setFlags(int flags) throws RemoteException {
        }

        @Override // android.media.session.ISession
        public void setActive(boolean active) throws RemoteException {
        }

        @Override // android.media.session.ISession
        public void setMediaButtonReceiver(PendingIntent mbr) throws RemoteException {
        }

        @Override // android.media.session.ISession
        public void setLaunchPendingIntent(PendingIntent pi) throws RemoteException {
        }

        @Override // android.media.session.ISession
        public void destroySession() throws RemoteException {
        }

        @Override // android.media.session.ISession
        public void setMetadata(MediaMetadata metadata, long duration, String metadataDescription) throws RemoteException {
        }

        @Override // android.media.session.ISession
        public void setPlaybackState(PlaybackState state) throws RemoteException {
        }

        @Override // android.media.session.ISession
        public void setQueue(ParceledListSlice queue) throws RemoteException {
        }

        @Override // android.media.session.ISession
        public void setQueueTitle(CharSequence title) throws RemoteException {
        }

        @Override // android.media.session.ISession
        public void setExtras(Bundle extras) throws RemoteException {
        }

        @Override // android.media.session.ISession
        public void setRatingType(int type) throws RemoteException {
        }

        @Override // android.media.session.ISession
        public void setPlaybackToLocal(AudioAttributes attributes) throws RemoteException {
        }

        @Override // android.media.session.ISession
        public void setPlaybackToRemote(int control, int max) throws RemoteException {
        }

        @Override // android.media.session.ISession
        public void setCurrentVolume(int currentVolume) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements ISession {
        private static final String DESCRIPTOR = "android.media.session.ISession";
        static final int TRANSACTION_destroySession = 7;
        static final int TRANSACTION_getController = 2;
        static final int TRANSACTION_sendEvent = 1;
        static final int TRANSACTION_setActive = 4;
        static final int TRANSACTION_setCurrentVolume = 16;
        static final int TRANSACTION_setExtras = 12;
        static final int TRANSACTION_setFlags = 3;
        static final int TRANSACTION_setLaunchPendingIntent = 6;
        static final int TRANSACTION_setMediaButtonReceiver = 5;
        static final int TRANSACTION_setMetadata = 8;
        static final int TRANSACTION_setPlaybackState = 9;
        static final int TRANSACTION_setPlaybackToLocal = 14;
        static final int TRANSACTION_setPlaybackToRemote = 15;
        static final int TRANSACTION_setQueue = 10;
        static final int TRANSACTION_setQueueTitle = 11;
        static final int TRANSACTION_setRatingType = 13;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ISession asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ISession)) {
                return new Proxy(obj);
            }
            return (ISession) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "sendEvent";
                case 2:
                    return "getController";
                case 3:
                    return "setFlags";
                case 4:
                    return "setActive";
                case 5:
                    return "setMediaButtonReceiver";
                case 6:
                    return "setLaunchPendingIntent";
                case 7:
                    return "destroySession";
                case 8:
                    return "setMetadata";
                case 9:
                    return "setPlaybackState";
                case 10:
                    return "setQueue";
                case 11:
                    return "setQueueTitle";
                case 12:
                    return "setExtras";
                case 13:
                    return "setRatingType";
                case 14:
                    return "setPlaybackToLocal";
                case 15:
                    return "setPlaybackToRemote";
                case 16:
                    return "setCurrentVolume";
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
            PendingIntent _arg0;
            PendingIntent _arg02;
            MediaMetadata _arg03;
            PlaybackState _arg04;
            ParceledListSlice _arg05;
            CharSequence _arg06;
            Bundle _arg07;
            AudioAttributes _arg08;
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg09 = data.readString();
                        if (data.readInt() != 0) {
                            _arg1 = Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg1 = null;
                        }
                        sendEvent(_arg09, _arg1);
                        reply.writeNoException();
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        ISessionController _result = getController();
                        reply.writeNoException();
                        reply.writeStrongBinder(_result != null ? _result.asBinder() : null);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        setFlags(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        setActive(data.readInt() != 0);
                        reply.writeNoException();
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = PendingIntent.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        setMediaButtonReceiver(_arg0);
                        reply.writeNoException();
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = PendingIntent.CREATOR.createFromParcel(data);
                        } else {
                            _arg02 = null;
                        }
                        setLaunchPendingIntent(_arg02);
                        reply.writeNoException();
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        destroySession();
                        reply.writeNoException();
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg03 = MediaMetadata.CREATOR.createFromParcel(data);
                        } else {
                            _arg03 = null;
                        }
                        setMetadata(_arg03, data.readLong(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg04 = PlaybackState.CREATOR.createFromParcel(data);
                        } else {
                            _arg04 = null;
                        }
                        setPlaybackState(_arg04);
                        reply.writeNoException();
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg05 = ParceledListSlice.CREATOR.createFromParcel(data);
                        } else {
                            _arg05 = null;
                        }
                        setQueue(_arg05);
                        reply.writeNoException();
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg06 = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(data);
                        } else {
                            _arg06 = null;
                        }
                        setQueueTitle(_arg06);
                        reply.writeNoException();
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg07 = Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg07 = null;
                        }
                        setExtras(_arg07);
                        reply.writeNoException();
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        setRatingType(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg08 = AudioAttributes.CREATOR.createFromParcel(data);
                        } else {
                            _arg08 = null;
                        }
                        setPlaybackToLocal(_arg08);
                        reply.writeNoException();
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        setPlaybackToRemote(data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        setCurrentVolume(data.readInt());
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
        public static class Proxy implements ISession {
            public static ISession sDefaultImpl;
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

            @Override // android.media.session.ISession
            public void sendEvent(String event, Bundle data) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(event);
                    if (data != null) {
                        _data.writeInt(1);
                        data.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().sendEvent(event, data);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.media.session.ISession
            public ISessionController getController() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getController();
                    }
                    _reply.readException();
                    ISessionController _result = ISessionController.Stub.asInterface(_reply.readStrongBinder());
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.media.session.ISession
            public void setFlags(int flags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(flags);
                    if (this.mRemote.transact(3, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setFlags(flags);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.media.session.ISession
            public void setActive(boolean active) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(active ? 1 : 0);
                    if (this.mRemote.transact(4, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setActive(active);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.media.session.ISession
            public void setMediaButtonReceiver(PendingIntent mbr) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (mbr != null) {
                        _data.writeInt(1);
                        mbr.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(5, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setMediaButtonReceiver(mbr);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.media.session.ISession
            public void setLaunchPendingIntent(PendingIntent pi) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (pi != null) {
                        _data.writeInt(1);
                        pi.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(6, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setLaunchPendingIntent(pi);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.media.session.ISession
            public void destroySession() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(7, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().destroySession();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.media.session.ISession
            public void setMetadata(MediaMetadata metadata, long duration, String metadataDescription) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (metadata != null) {
                        _data.writeInt(1);
                        metadata.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeLong(duration);
                    _data.writeString(metadataDescription);
                    if (this.mRemote.transact(8, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setMetadata(metadata, duration, metadataDescription);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.media.session.ISession
            public void setPlaybackState(PlaybackState state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (state != null) {
                        _data.writeInt(1);
                        state.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(9, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setPlaybackState(state);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.media.session.ISession
            public void setQueue(ParceledListSlice queue) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (queue != null) {
                        _data.writeInt(1);
                        queue.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(10, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setQueue(queue);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.media.session.ISession
            public void setQueueTitle(CharSequence title) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (title != null) {
                        _data.writeInt(1);
                        TextUtils.writeToParcel(title, _data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(11, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setQueueTitle(title);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.media.session.ISession
            public void setExtras(Bundle extras) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (extras != null) {
                        _data.writeInt(1);
                        extras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(12, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setExtras(extras);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.media.session.ISession
            public void setRatingType(int type) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(type);
                    if (this.mRemote.transact(13, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setRatingType(type);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.media.session.ISession
            public void setPlaybackToLocal(AudioAttributes attributes) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (attributes != null) {
                        _data.writeInt(1);
                        attributes.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(14, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setPlaybackToLocal(attributes);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.media.session.ISession
            public void setPlaybackToRemote(int control, int max) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(control);
                    _data.writeInt(max);
                    if (this.mRemote.transact(15, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setPlaybackToRemote(control, max);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.media.session.ISession
            public void setCurrentVolume(int currentVolume) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(currentVolume);
                    if (this.mRemote.transact(16, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setCurrentVolume(currentVolume);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(ISession impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static ISession getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
