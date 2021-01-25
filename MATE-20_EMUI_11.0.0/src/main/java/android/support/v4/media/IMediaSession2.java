package android.support.v4.media;

import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.support.v4.media.IMediaController2;
import java.util.List;

public interface IMediaSession2 extends IInterface {
    void addPlaylistItem(IMediaController2 iMediaController2, int i, Bundle bundle) throws RemoteException;

    void adjustVolume(IMediaController2 iMediaController2, int i, int i2) throws RemoteException;

    void connect(IMediaController2 iMediaController2, String str) throws RemoteException;

    void fastForward(IMediaController2 iMediaController2) throws RemoteException;

    void getChildren(IMediaController2 iMediaController2, String str, int i, int i2, Bundle bundle) throws RemoteException;

    void getItem(IMediaController2 iMediaController2, String str) throws RemoteException;

    void getLibraryRoot(IMediaController2 iMediaController2, Bundle bundle) throws RemoteException;

    void getSearchResult(IMediaController2 iMediaController2, String str, int i, int i2, Bundle bundle) throws RemoteException;

    void pause(IMediaController2 iMediaController2) throws RemoteException;

    void play(IMediaController2 iMediaController2) throws RemoteException;

    void playFromMediaId(IMediaController2 iMediaController2, String str, Bundle bundle) throws RemoteException;

    void playFromSearch(IMediaController2 iMediaController2, String str, Bundle bundle) throws RemoteException;

    void playFromUri(IMediaController2 iMediaController2, Uri uri, Bundle bundle) throws RemoteException;

    void prepare(IMediaController2 iMediaController2) throws RemoteException;

    void prepareFromMediaId(IMediaController2 iMediaController2, String str, Bundle bundle) throws RemoteException;

    void prepareFromSearch(IMediaController2 iMediaController2, String str, Bundle bundle) throws RemoteException;

    void prepareFromUri(IMediaController2 iMediaController2, Uri uri, Bundle bundle) throws RemoteException;

    void release(IMediaController2 iMediaController2) throws RemoteException;

    void removePlaylistItem(IMediaController2 iMediaController2, Bundle bundle) throws RemoteException;

    void replacePlaylistItem(IMediaController2 iMediaController2, int i, Bundle bundle) throws RemoteException;

    void reset(IMediaController2 iMediaController2) throws RemoteException;

    void rewind(IMediaController2 iMediaController2) throws RemoteException;

    void search(IMediaController2 iMediaController2, String str, Bundle bundle) throws RemoteException;

    void seekTo(IMediaController2 iMediaController2, long j) throws RemoteException;

    void selectRoute(IMediaController2 iMediaController2, Bundle bundle) throws RemoteException;

    void sendCustomCommand(IMediaController2 iMediaController2, Bundle bundle, Bundle bundle2, ResultReceiver resultReceiver) throws RemoteException;

    void setPlaybackSpeed(IMediaController2 iMediaController2, float f) throws RemoteException;

    void setPlaylist(IMediaController2 iMediaController2, List<Bundle> list, Bundle bundle) throws RemoteException;

    void setRating(IMediaController2 iMediaController2, String str, Bundle bundle) throws RemoteException;

    void setRepeatMode(IMediaController2 iMediaController2, int i) throws RemoteException;

    void setShuffleMode(IMediaController2 iMediaController2, int i) throws RemoteException;

    void setVolumeTo(IMediaController2 iMediaController2, int i, int i2) throws RemoteException;

    void skipToNextItem(IMediaController2 iMediaController2) throws RemoteException;

    void skipToPlaylistItem(IMediaController2 iMediaController2, Bundle bundle) throws RemoteException;

    void skipToPreviousItem(IMediaController2 iMediaController2) throws RemoteException;

    void subscribe(IMediaController2 iMediaController2, String str, Bundle bundle) throws RemoteException;

    void subscribeRoutesInfo(IMediaController2 iMediaController2) throws RemoteException;

    void unsubscribe(IMediaController2 iMediaController2, String str) throws RemoteException;

    void unsubscribeRoutesInfo(IMediaController2 iMediaController2) throws RemoteException;

    void updatePlaylistMetadata(IMediaController2 iMediaController2, Bundle bundle) throws RemoteException;

    public static abstract class Stub extends Binder implements IMediaSession2 {
        private static final String DESCRIPTOR = "android.support.v4.media.IMediaSession2";
        static final int TRANSACTION_addPlaylistItem = 23;
        static final int TRANSACTION_adjustVolume = 4;
        static final int TRANSACTION_connect = 1;
        static final int TRANSACTION_fastForward = 9;
        static final int TRANSACTION_getChildren = 36;
        static final int TRANSACTION_getItem = 35;
        static final int TRANSACTION_getLibraryRoot = 34;
        static final int TRANSACTION_getSearchResult = 38;
        static final int TRANSACTION_pause = 6;
        static final int TRANSACTION_play = 5;
        static final int TRANSACTION_playFromMediaId = 18;
        static final int TRANSACTION_playFromSearch = 17;
        static final int TRANSACTION_playFromUri = 16;
        static final int TRANSACTION_prepare = 8;
        static final int TRANSACTION_prepareFromMediaId = 15;
        static final int TRANSACTION_prepareFromSearch = 14;
        static final int TRANSACTION_prepareFromUri = 13;
        static final int TRANSACTION_release = 2;
        static final int TRANSACTION_removePlaylistItem = 24;
        static final int TRANSACTION_replacePlaylistItem = 25;
        static final int TRANSACTION_reset = 7;
        static final int TRANSACTION_rewind = 10;
        static final int TRANSACTION_search = 37;
        static final int TRANSACTION_seekTo = 11;
        static final int TRANSACTION_selectRoute = 33;
        static final int TRANSACTION_sendCustomCommand = 12;
        static final int TRANSACTION_setPlaybackSpeed = 20;
        static final int TRANSACTION_setPlaylist = 21;
        static final int TRANSACTION_setRating = 19;
        static final int TRANSACTION_setRepeatMode = 29;
        static final int TRANSACTION_setShuffleMode = 30;
        static final int TRANSACTION_setVolumeTo = 3;
        static final int TRANSACTION_skipToNextItem = 28;
        static final int TRANSACTION_skipToPlaylistItem = 26;
        static final int TRANSACTION_skipToPreviousItem = 27;
        static final int TRANSACTION_subscribe = 39;
        static final int TRANSACTION_subscribeRoutesInfo = 31;
        static final int TRANSACTION_unsubscribe = 40;
        static final int TRANSACTION_unsubscribeRoutesInfo = 32;
        static final int TRANSACTION_updatePlaylistMetadata = 22;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IMediaSession2 asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IMediaSession2)) {
                return new Proxy(obj);
            }
            return (IMediaSession2) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            Bundle _arg1;
            Bundle _arg2;
            Uri _arg12;
            Uri _arg13;
            if (code != 1598968902) {
                ResultReceiver _arg3 = null;
                Bundle _arg22 = null;
                Bundle _arg4 = null;
                Bundle _arg23 = null;
                Bundle _arg42 = null;
                Bundle _arg14 = null;
                Bundle _arg15 = null;
                Bundle _arg16 = null;
                Bundle _arg24 = null;
                Bundle _arg17 = null;
                Bundle _arg25 = null;
                Bundle _arg18 = null;
                Bundle _arg26 = null;
                Bundle _arg27 = null;
                Bundle _arg28 = null;
                Bundle _arg29 = null;
                Bundle _arg210 = null;
                Bundle _arg211 = null;
                Bundle _arg212 = null;
                Bundle _arg213 = null;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        connect(IMediaController2.Stub.asInterface(data.readStrongBinder()), data.readString());
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        release(IMediaController2.Stub.asInterface(data.readStrongBinder()));
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        setVolumeTo(IMediaController2.Stub.asInterface(data.readStrongBinder()), data.readInt(), data.readInt());
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        adjustVolume(IMediaController2.Stub.asInterface(data.readStrongBinder()), data.readInt(), data.readInt());
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        play(IMediaController2.Stub.asInterface(data.readStrongBinder()));
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        pause(IMediaController2.Stub.asInterface(data.readStrongBinder()));
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        reset(IMediaController2.Stub.asInterface(data.readStrongBinder()));
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        prepare(IMediaController2.Stub.asInterface(data.readStrongBinder()));
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        fastForward(IMediaController2.Stub.asInterface(data.readStrongBinder()));
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        rewind(IMediaController2.Stub.asInterface(data.readStrongBinder()));
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        seekTo(IMediaController2.Stub.asInterface(data.readStrongBinder()), data.readLong());
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        IMediaController2 _arg0 = IMediaController2.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg1 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg1 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg2 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg2 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg3 = (ResultReceiver) ResultReceiver.CREATOR.createFromParcel(data);
                        }
                        sendCustomCommand(_arg0, _arg1, _arg2, _arg3);
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        IMediaController2 _arg02 = IMediaController2.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg12 = (Uri) Uri.CREATOR.createFromParcel(data);
                        } else {
                            _arg12 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg213 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        }
                        prepareFromUri(_arg02, _arg12, _arg213);
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        IMediaController2 _arg03 = IMediaController2.Stub.asInterface(data.readStrongBinder());
                        String _arg19 = data.readString();
                        if (data.readInt() != 0) {
                            _arg212 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        }
                        prepareFromSearch(_arg03, _arg19, _arg212);
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        IMediaController2 _arg04 = IMediaController2.Stub.asInterface(data.readStrongBinder());
                        String _arg110 = data.readString();
                        if (data.readInt() != 0) {
                            _arg211 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        }
                        prepareFromMediaId(_arg04, _arg110, _arg211);
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        IMediaController2 _arg05 = IMediaController2.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg13 = (Uri) Uri.CREATOR.createFromParcel(data);
                        } else {
                            _arg13 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg210 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        }
                        playFromUri(_arg05, _arg13, _arg210);
                        return true;
                    case 17:
                        data.enforceInterface(DESCRIPTOR);
                        IMediaController2 _arg06 = IMediaController2.Stub.asInterface(data.readStrongBinder());
                        String _arg111 = data.readString();
                        if (data.readInt() != 0) {
                            _arg29 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        }
                        playFromSearch(_arg06, _arg111, _arg29);
                        return true;
                    case 18:
                        data.enforceInterface(DESCRIPTOR);
                        IMediaController2 _arg07 = IMediaController2.Stub.asInterface(data.readStrongBinder());
                        String _arg112 = data.readString();
                        if (data.readInt() != 0) {
                            _arg28 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        }
                        playFromMediaId(_arg07, _arg112, _arg28);
                        return true;
                    case 19:
                        data.enforceInterface(DESCRIPTOR);
                        IMediaController2 _arg08 = IMediaController2.Stub.asInterface(data.readStrongBinder());
                        String _arg113 = data.readString();
                        if (data.readInt() != 0) {
                            _arg27 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        }
                        setRating(_arg08, _arg113, _arg27);
                        return true;
                    case 20:
                        data.enforceInterface(DESCRIPTOR);
                        setPlaybackSpeed(IMediaController2.Stub.asInterface(data.readStrongBinder()), data.readFloat());
                        return true;
                    case 21:
                        data.enforceInterface(DESCRIPTOR);
                        IMediaController2 _arg09 = IMediaController2.Stub.asInterface(data.readStrongBinder());
                        List<Bundle> _arg114 = data.createTypedArrayList(Bundle.CREATOR);
                        if (data.readInt() != 0) {
                            _arg26 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        }
                        setPlaylist(_arg09, _arg114, _arg26);
                        return true;
                    case 22:
                        data.enforceInterface(DESCRIPTOR);
                        IMediaController2 _arg010 = IMediaController2.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg18 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        }
                        updatePlaylistMetadata(_arg010, _arg18);
                        return true;
                    case 23:
                        data.enforceInterface(DESCRIPTOR);
                        IMediaController2 _arg011 = IMediaController2.Stub.asInterface(data.readStrongBinder());
                        int _arg115 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg25 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        }
                        addPlaylistItem(_arg011, _arg115, _arg25);
                        return true;
                    case 24:
                        data.enforceInterface(DESCRIPTOR);
                        IMediaController2 _arg012 = IMediaController2.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg17 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        }
                        removePlaylistItem(_arg012, _arg17);
                        return true;
                    case 25:
                        data.enforceInterface(DESCRIPTOR);
                        IMediaController2 _arg013 = IMediaController2.Stub.asInterface(data.readStrongBinder());
                        int _arg116 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg24 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        }
                        replacePlaylistItem(_arg013, _arg116, _arg24);
                        return true;
                    case 26:
                        data.enforceInterface(DESCRIPTOR);
                        IMediaController2 _arg014 = IMediaController2.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg16 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        }
                        skipToPlaylistItem(_arg014, _arg16);
                        return true;
                    case 27:
                        data.enforceInterface(DESCRIPTOR);
                        skipToPreviousItem(IMediaController2.Stub.asInterface(data.readStrongBinder()));
                        return true;
                    case 28:
                        data.enforceInterface(DESCRIPTOR);
                        skipToNextItem(IMediaController2.Stub.asInterface(data.readStrongBinder()));
                        return true;
                    case 29:
                        data.enforceInterface(DESCRIPTOR);
                        setRepeatMode(IMediaController2.Stub.asInterface(data.readStrongBinder()), data.readInt());
                        return true;
                    case 30:
                        data.enforceInterface(DESCRIPTOR);
                        setShuffleMode(IMediaController2.Stub.asInterface(data.readStrongBinder()), data.readInt());
                        return true;
                    case 31:
                        data.enforceInterface(DESCRIPTOR);
                        subscribeRoutesInfo(IMediaController2.Stub.asInterface(data.readStrongBinder()));
                        return true;
                    case 32:
                        data.enforceInterface(DESCRIPTOR);
                        unsubscribeRoutesInfo(IMediaController2.Stub.asInterface(data.readStrongBinder()));
                        return true;
                    case 33:
                        data.enforceInterface(DESCRIPTOR);
                        IMediaController2 _arg015 = IMediaController2.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg15 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        }
                        selectRoute(_arg015, _arg15);
                        return true;
                    case 34:
                        data.enforceInterface(DESCRIPTOR);
                        IMediaController2 _arg016 = IMediaController2.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg14 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        }
                        getLibraryRoot(_arg016, _arg14);
                        return true;
                    case 35:
                        data.enforceInterface(DESCRIPTOR);
                        getItem(IMediaController2.Stub.asInterface(data.readStrongBinder()), data.readString());
                        return true;
                    case 36:
                        data.enforceInterface(DESCRIPTOR);
                        IMediaController2 _arg017 = IMediaController2.Stub.asInterface(data.readStrongBinder());
                        String _arg117 = data.readString();
                        int _arg214 = data.readInt();
                        int _arg32 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg42 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        }
                        getChildren(_arg017, _arg117, _arg214, _arg32, _arg42);
                        return true;
                    case 37:
                        data.enforceInterface(DESCRIPTOR);
                        IMediaController2 _arg018 = IMediaController2.Stub.asInterface(data.readStrongBinder());
                        String _arg118 = data.readString();
                        if (data.readInt() != 0) {
                            _arg23 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        }
                        search(_arg018, _arg118, _arg23);
                        return true;
                    case 38:
                        data.enforceInterface(DESCRIPTOR);
                        IMediaController2 _arg019 = IMediaController2.Stub.asInterface(data.readStrongBinder());
                        String _arg119 = data.readString();
                        int _arg215 = data.readInt();
                        int _arg33 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg4 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        }
                        getSearchResult(_arg019, _arg119, _arg215, _arg33, _arg4);
                        return true;
                    case 39:
                        data.enforceInterface(DESCRIPTOR);
                        IMediaController2 _arg020 = IMediaController2.Stub.asInterface(data.readStrongBinder());
                        String _arg120 = data.readString();
                        if (data.readInt() != 0) {
                            _arg22 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        }
                        subscribe(_arg020, _arg120, _arg22);
                        return true;
                    case 40:
                        data.enforceInterface(DESCRIPTOR);
                        unsubscribe(IMediaController2.Stub.asInterface(data.readStrongBinder()), data.readString());
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
        public static class Proxy implements IMediaSession2 {
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

            @Override // android.support.v4.media.IMediaSession2
            public void connect(IMediaController2 caller, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    _data.writeString(callingPackage);
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.IMediaSession2
            public void release(IMediaController2 caller) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    this.mRemote.transact(2, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.IMediaSession2
            public void setVolumeTo(IMediaController2 caller, int value, int flags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    _data.writeInt(value);
                    _data.writeInt(flags);
                    this.mRemote.transact(3, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.IMediaSession2
            public void adjustVolume(IMediaController2 caller, int direction, int flags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    _data.writeInt(direction);
                    _data.writeInt(flags);
                    this.mRemote.transact(4, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.IMediaSession2
            public void play(IMediaController2 caller) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    this.mRemote.transact(5, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.IMediaSession2
            public void pause(IMediaController2 caller) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    this.mRemote.transact(6, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.IMediaSession2
            public void reset(IMediaController2 caller) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    this.mRemote.transact(7, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.IMediaSession2
            public void prepare(IMediaController2 caller) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    this.mRemote.transact(8, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.IMediaSession2
            public void fastForward(IMediaController2 caller) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    this.mRemote.transact(9, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.IMediaSession2
            public void rewind(IMediaController2 caller) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    this.mRemote.transact(10, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.IMediaSession2
            public void seekTo(IMediaController2 caller, long pos) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    _data.writeLong(pos);
                    this.mRemote.transact(11, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.IMediaSession2
            public void sendCustomCommand(IMediaController2 caller, Bundle command, Bundle args, ResultReceiver receiver) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    if (command != null) {
                        _data.writeInt(1);
                        command.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (args != null) {
                        _data.writeInt(1);
                        args.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (receiver != null) {
                        _data.writeInt(1);
                        receiver.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(12, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.IMediaSession2
            public void prepareFromUri(IMediaController2 caller, Uri uri, Bundle extras) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    if (uri != null) {
                        _data.writeInt(1);
                        uri.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (extras != null) {
                        _data.writeInt(1);
                        extras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(13, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.IMediaSession2
            public void prepareFromSearch(IMediaController2 caller, String query, Bundle extras) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    _data.writeString(query);
                    if (extras != null) {
                        _data.writeInt(1);
                        extras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(14, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.IMediaSession2
            public void prepareFromMediaId(IMediaController2 caller, String mediaId, Bundle extras) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    _data.writeString(mediaId);
                    if (extras != null) {
                        _data.writeInt(1);
                        extras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(15, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.IMediaSession2
            public void playFromUri(IMediaController2 caller, Uri uri, Bundle extras) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    if (uri != null) {
                        _data.writeInt(1);
                        uri.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (extras != null) {
                        _data.writeInt(1);
                        extras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(16, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.IMediaSession2
            public void playFromSearch(IMediaController2 caller, String query, Bundle extras) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    _data.writeString(query);
                    if (extras != null) {
                        _data.writeInt(1);
                        extras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(17, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.IMediaSession2
            public void playFromMediaId(IMediaController2 caller, String mediaId, Bundle extras) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    _data.writeString(mediaId);
                    if (extras != null) {
                        _data.writeInt(1);
                        extras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(18, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.IMediaSession2
            public void setRating(IMediaController2 caller, String mediaId, Bundle rating) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    _data.writeString(mediaId);
                    if (rating != null) {
                        _data.writeInt(1);
                        rating.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(19, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.IMediaSession2
            public void setPlaybackSpeed(IMediaController2 caller, float speed) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    _data.writeFloat(speed);
                    this.mRemote.transact(20, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.IMediaSession2
            public void setPlaylist(IMediaController2 caller, List<Bundle> playlist, Bundle metadata) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    _data.writeTypedList(playlist);
                    if (metadata != null) {
                        _data.writeInt(1);
                        metadata.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(21, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.IMediaSession2
            public void updatePlaylistMetadata(IMediaController2 caller, Bundle metadata) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    if (metadata != null) {
                        _data.writeInt(1);
                        metadata.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(22, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.IMediaSession2
            public void addPlaylistItem(IMediaController2 caller, int index, Bundle mediaItem) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    _data.writeInt(index);
                    if (mediaItem != null) {
                        _data.writeInt(1);
                        mediaItem.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(23, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.IMediaSession2
            public void removePlaylistItem(IMediaController2 caller, Bundle mediaItem) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    if (mediaItem != null) {
                        _data.writeInt(1);
                        mediaItem.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(24, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.IMediaSession2
            public void replacePlaylistItem(IMediaController2 caller, int index, Bundle mediaItem) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    _data.writeInt(index);
                    if (mediaItem != null) {
                        _data.writeInt(1);
                        mediaItem.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(25, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.IMediaSession2
            public void skipToPlaylistItem(IMediaController2 caller, Bundle mediaItem) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    if (mediaItem != null) {
                        _data.writeInt(1);
                        mediaItem.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(26, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.IMediaSession2
            public void skipToPreviousItem(IMediaController2 caller) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    this.mRemote.transact(27, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.IMediaSession2
            public void skipToNextItem(IMediaController2 caller) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    this.mRemote.transact(28, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.IMediaSession2
            public void setRepeatMode(IMediaController2 caller, int repeatMode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    _data.writeInt(repeatMode);
                    this.mRemote.transact(29, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.IMediaSession2
            public void setShuffleMode(IMediaController2 caller, int shuffleMode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    _data.writeInt(shuffleMode);
                    this.mRemote.transact(30, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.IMediaSession2
            public void subscribeRoutesInfo(IMediaController2 caller) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    this.mRemote.transact(31, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.IMediaSession2
            public void unsubscribeRoutesInfo(IMediaController2 caller) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    this.mRemote.transact(32, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.IMediaSession2
            public void selectRoute(IMediaController2 caller, Bundle route) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    if (route != null) {
                        _data.writeInt(1);
                        route.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(33, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.IMediaSession2
            public void getLibraryRoot(IMediaController2 caller, Bundle rootHints) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    if (rootHints != null) {
                        _data.writeInt(1);
                        rootHints.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(34, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.IMediaSession2
            public void getItem(IMediaController2 caller, String mediaId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    _data.writeString(mediaId);
                    this.mRemote.transact(35, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.IMediaSession2
            public void getChildren(IMediaController2 caller, String parentId, int page, int pageSize, Bundle extras) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    _data.writeString(parentId);
                    _data.writeInt(page);
                    _data.writeInt(pageSize);
                    if (extras != null) {
                        _data.writeInt(1);
                        extras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(36, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.IMediaSession2
            public void search(IMediaController2 caller, String query, Bundle extras) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    _data.writeString(query);
                    if (extras != null) {
                        _data.writeInt(1);
                        extras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(37, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.IMediaSession2
            public void getSearchResult(IMediaController2 caller, String query, int page, int pageSize, Bundle extras) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    _data.writeString(query);
                    _data.writeInt(page);
                    _data.writeInt(pageSize);
                    if (extras != null) {
                        _data.writeInt(1);
                        extras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(38, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.IMediaSession2
            public void subscribe(IMediaController2 caller, String parentId, Bundle extras) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    _data.writeString(parentId);
                    if (extras != null) {
                        _data.writeInt(1);
                        extras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(39, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.IMediaSession2
            public void unsubscribe(IMediaController2 caller, String parentId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    _data.writeString(parentId);
                    this.mRemote.transact(40, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }
        }
    }
}
