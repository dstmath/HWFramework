package android.media.tv;

import android.media.tv.ITvInputSession;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.List;

public interface ITvInputSessionCallback extends IInterface {
    void onChannelRetuned(Uri uri) throws RemoteException;

    void onContentAllowed() throws RemoteException;

    void onContentBlocked(String str) throws RemoteException;

    void onError(int i) throws RemoteException;

    void onLayoutSurface(int i, int i2, int i3, int i4) throws RemoteException;

    void onRecordingStopped(Uri uri) throws RemoteException;

    void onSessionCreated(ITvInputSession iTvInputSession, IBinder iBinder) throws RemoteException;

    void onSessionEvent(String str, Bundle bundle) throws RemoteException;

    void onTimeShiftCurrentPositionChanged(long j) throws RemoteException;

    void onTimeShiftStartPositionChanged(long j) throws RemoteException;

    void onTimeShiftStatusChanged(int i) throws RemoteException;

    void onTrackSelected(int i, String str) throws RemoteException;

    void onTracksChanged(List<TvTrackInfo> list) throws RemoteException;

    void onTuned(Uri uri) throws RemoteException;

    void onVideoAvailable() throws RemoteException;

    void onVideoUnavailable(int i) throws RemoteException;

    public static class Default implements ITvInputSessionCallback {
        @Override // android.media.tv.ITvInputSessionCallback
        public void onSessionCreated(ITvInputSession session, IBinder hardwareSessionToken) throws RemoteException {
        }

        @Override // android.media.tv.ITvInputSessionCallback
        public void onSessionEvent(String name, Bundle args) throws RemoteException {
        }

        @Override // android.media.tv.ITvInputSessionCallback
        public void onChannelRetuned(Uri channelUri) throws RemoteException {
        }

        @Override // android.media.tv.ITvInputSessionCallback
        public void onTracksChanged(List<TvTrackInfo> list) throws RemoteException {
        }

        @Override // android.media.tv.ITvInputSessionCallback
        public void onTrackSelected(int type, String trackId) throws RemoteException {
        }

        @Override // android.media.tv.ITvInputSessionCallback
        public void onVideoAvailable() throws RemoteException {
        }

        @Override // android.media.tv.ITvInputSessionCallback
        public void onVideoUnavailable(int reason) throws RemoteException {
        }

        @Override // android.media.tv.ITvInputSessionCallback
        public void onContentAllowed() throws RemoteException {
        }

        @Override // android.media.tv.ITvInputSessionCallback
        public void onContentBlocked(String rating) throws RemoteException {
        }

        @Override // android.media.tv.ITvInputSessionCallback
        public void onLayoutSurface(int left, int top, int right, int bottom) throws RemoteException {
        }

        @Override // android.media.tv.ITvInputSessionCallback
        public void onTimeShiftStatusChanged(int status) throws RemoteException {
        }

        @Override // android.media.tv.ITvInputSessionCallback
        public void onTimeShiftStartPositionChanged(long timeMs) throws RemoteException {
        }

        @Override // android.media.tv.ITvInputSessionCallback
        public void onTimeShiftCurrentPositionChanged(long timeMs) throws RemoteException {
        }

        @Override // android.media.tv.ITvInputSessionCallback
        public void onTuned(Uri channelUri) throws RemoteException {
        }

        @Override // android.media.tv.ITvInputSessionCallback
        public void onRecordingStopped(Uri recordedProgramUri) throws RemoteException {
        }

        @Override // android.media.tv.ITvInputSessionCallback
        public void onError(int error) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements ITvInputSessionCallback {
        private static final String DESCRIPTOR = "android.media.tv.ITvInputSessionCallback";
        static final int TRANSACTION_onChannelRetuned = 3;
        static final int TRANSACTION_onContentAllowed = 8;
        static final int TRANSACTION_onContentBlocked = 9;
        static final int TRANSACTION_onError = 16;
        static final int TRANSACTION_onLayoutSurface = 10;
        static final int TRANSACTION_onRecordingStopped = 15;
        static final int TRANSACTION_onSessionCreated = 1;
        static final int TRANSACTION_onSessionEvent = 2;
        static final int TRANSACTION_onTimeShiftCurrentPositionChanged = 13;
        static final int TRANSACTION_onTimeShiftStartPositionChanged = 12;
        static final int TRANSACTION_onTimeShiftStatusChanged = 11;
        static final int TRANSACTION_onTrackSelected = 5;
        static final int TRANSACTION_onTracksChanged = 4;
        static final int TRANSACTION_onTuned = 14;
        static final int TRANSACTION_onVideoAvailable = 6;
        static final int TRANSACTION_onVideoUnavailable = 7;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ITvInputSessionCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ITvInputSessionCallback)) {
                return new Proxy(obj);
            }
            return (ITvInputSessionCallback) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "onSessionCreated";
                case 2:
                    return "onSessionEvent";
                case 3:
                    return "onChannelRetuned";
                case 4:
                    return "onTracksChanged";
                case 5:
                    return "onTrackSelected";
                case 6:
                    return "onVideoAvailable";
                case 7:
                    return "onVideoUnavailable";
                case 8:
                    return "onContentAllowed";
                case 9:
                    return "onContentBlocked";
                case 10:
                    return "onLayoutSurface";
                case 11:
                    return "onTimeShiftStatusChanged";
                case 12:
                    return "onTimeShiftStartPositionChanged";
                case 13:
                    return "onTimeShiftCurrentPositionChanged";
                case 14:
                    return "onTuned";
                case 15:
                    return "onRecordingStopped";
                case 16:
                    return "onError";
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
            Uri _arg0;
            Uri _arg02;
            Uri _arg03;
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        onSessionCreated(ITvInputSession.Stub.asInterface(data.readStrongBinder()), data.readStrongBinder());
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg04 = data.readString();
                        if (data.readInt() != 0) {
                            _arg1 = Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg1 = null;
                        }
                        onSessionEvent(_arg04, _arg1);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = Uri.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        onChannelRetuned(_arg0);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        onTracksChanged(data.createTypedArrayList(TvTrackInfo.CREATOR));
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        onTrackSelected(data.readInt(), data.readString());
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        onVideoAvailable();
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        onVideoUnavailable(data.readInt());
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        onContentAllowed();
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        onContentBlocked(data.readString());
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        onLayoutSurface(data.readInt(), data.readInt(), data.readInt(), data.readInt());
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        onTimeShiftStatusChanged(data.readInt());
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        onTimeShiftStartPositionChanged(data.readLong());
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        onTimeShiftCurrentPositionChanged(data.readLong());
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = Uri.CREATOR.createFromParcel(data);
                        } else {
                            _arg02 = null;
                        }
                        onTuned(_arg02);
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg03 = Uri.CREATOR.createFromParcel(data);
                        } else {
                            _arg03 = null;
                        }
                        onRecordingStopped(_arg03);
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        onError(data.readInt());
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
        public static class Proxy implements ITvInputSessionCallback {
            public static ITvInputSessionCallback sDefaultImpl;
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

            @Override // android.media.tv.ITvInputSessionCallback
            public void onSessionCreated(ITvInputSession session, IBinder hardwareSessionToken) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(session != null ? session.asBinder() : null);
                    _data.writeStrongBinder(hardwareSessionToken);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onSessionCreated(session, hardwareSessionToken);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.media.tv.ITvInputSessionCallback
            public void onSessionEvent(String name, Bundle args) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(name);
                    if (args != null) {
                        _data.writeInt(1);
                        args.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onSessionEvent(name, args);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.media.tv.ITvInputSessionCallback
            public void onChannelRetuned(Uri channelUri) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (channelUri != null) {
                        _data.writeInt(1);
                        channelUri.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(3, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onChannelRetuned(channelUri);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.media.tv.ITvInputSessionCallback
            public void onTracksChanged(List<TvTrackInfo> tracks) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeTypedList(tracks);
                    if (this.mRemote.transact(4, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onTracksChanged(tracks);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.media.tv.ITvInputSessionCallback
            public void onTrackSelected(int type, String trackId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(type);
                    _data.writeString(trackId);
                    if (this.mRemote.transact(5, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onTrackSelected(type, trackId);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.media.tv.ITvInputSessionCallback
            public void onVideoAvailable() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(6, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onVideoAvailable();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.media.tv.ITvInputSessionCallback
            public void onVideoUnavailable(int reason) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(reason);
                    if (this.mRemote.transact(7, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onVideoUnavailable(reason);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.media.tv.ITvInputSessionCallback
            public void onContentAllowed() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(8, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onContentAllowed();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.media.tv.ITvInputSessionCallback
            public void onContentBlocked(String rating) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(rating);
                    if (this.mRemote.transact(9, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onContentBlocked(rating);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.media.tv.ITvInputSessionCallback
            public void onLayoutSurface(int left, int top, int right, int bottom) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(left);
                    _data.writeInt(top);
                    _data.writeInt(right);
                    _data.writeInt(bottom);
                    if (this.mRemote.transact(10, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onLayoutSurface(left, top, right, bottom);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.media.tv.ITvInputSessionCallback
            public void onTimeShiftStatusChanged(int status) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(status);
                    if (this.mRemote.transact(11, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onTimeShiftStatusChanged(status);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.media.tv.ITvInputSessionCallback
            public void onTimeShiftStartPositionChanged(long timeMs) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(timeMs);
                    if (this.mRemote.transact(12, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onTimeShiftStartPositionChanged(timeMs);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.media.tv.ITvInputSessionCallback
            public void onTimeShiftCurrentPositionChanged(long timeMs) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(timeMs);
                    if (this.mRemote.transact(13, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onTimeShiftCurrentPositionChanged(timeMs);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.media.tv.ITvInputSessionCallback
            public void onTuned(Uri channelUri) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (channelUri != null) {
                        _data.writeInt(1);
                        channelUri.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(14, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onTuned(channelUri);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.media.tv.ITvInputSessionCallback
            public void onRecordingStopped(Uri recordedProgramUri) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (recordedProgramUri != null) {
                        _data.writeInt(1);
                        recordedProgramUri.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(15, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onRecordingStopped(recordedProgramUri);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.media.tv.ITvInputSessionCallback
            public void onError(int error) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(error);
                    if (this.mRemote.transact(16, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onError(error);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(ITvInputSessionCallback impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static ITvInputSessionCallback getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
