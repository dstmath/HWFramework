package android.support.v4.media;

import android.app.PendingIntent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.support.v4.media.IMediaSession2;
import java.util.List;

public interface IMediaController2 extends IInterface {
    void onAllowedCommandsChanged(Bundle bundle) throws RemoteException;

    void onBufferingStateChanged(Bundle bundle, int i, long j) throws RemoteException;

    void onChildrenChanged(String str, int i, Bundle bundle) throws RemoteException;

    void onConnected(IMediaSession2 iMediaSession2, Bundle bundle, int i, Bundle bundle2, long j, long j2, float f, long j3, Bundle bundle3, int i2, int i3, List<Bundle> list, PendingIntent pendingIntent) throws RemoteException;

    void onCurrentMediaItemChanged(Bundle bundle) throws RemoteException;

    void onCustomCommand(Bundle bundle, Bundle bundle2, ResultReceiver resultReceiver) throws RemoteException;

    void onCustomLayoutChanged(List<Bundle> list) throws RemoteException;

    void onDisconnected() throws RemoteException;

    void onError(int i, Bundle bundle) throws RemoteException;

    void onGetChildrenDone(String str, int i, int i2, List<Bundle> list, Bundle bundle) throws RemoteException;

    void onGetItemDone(String str, Bundle bundle) throws RemoteException;

    void onGetLibraryRootDone(Bundle bundle, String str, Bundle bundle2) throws RemoteException;

    void onGetSearchResultDone(String str, int i, int i2, List<Bundle> list, Bundle bundle) throws RemoteException;

    void onPlaybackInfoChanged(Bundle bundle) throws RemoteException;

    void onPlaybackSpeedChanged(long j, long j2, float f) throws RemoteException;

    void onPlayerStateChanged(long j, long j2, int i) throws RemoteException;

    void onPlaylistChanged(List<Bundle> list, Bundle bundle) throws RemoteException;

    void onPlaylistMetadataChanged(Bundle bundle) throws RemoteException;

    void onRepeatModeChanged(int i) throws RemoteException;

    void onRoutesInfoChanged(List<Bundle> list) throws RemoteException;

    void onSearchResultChanged(String str, int i, Bundle bundle) throws RemoteException;

    void onSeekCompleted(long j, long j2, long j3) throws RemoteException;

    void onShuffleModeChanged(int i) throws RemoteException;

    public static abstract class Stub extends Binder implements IMediaController2 {
        private static final String DESCRIPTOR = "android.support.v4.media.IMediaController2";
        static final int TRANSACTION_onAllowedCommandsChanged = 16;
        static final int TRANSACTION_onBufferingStateChanged = 4;
        static final int TRANSACTION_onChildrenChanged = 20;
        static final int TRANSACTION_onConnected = 13;
        static final int TRANSACTION_onCurrentMediaItemChanged = 1;
        static final int TRANSACTION_onCustomCommand = 17;
        static final int TRANSACTION_onCustomLayoutChanged = 15;
        static final int TRANSACTION_onDisconnected = 14;
        static final int TRANSACTION_onError = 11;
        static final int TRANSACTION_onGetChildrenDone = 21;
        static final int TRANSACTION_onGetItemDone = 19;
        static final int TRANSACTION_onGetLibraryRootDone = 18;
        static final int TRANSACTION_onGetSearchResultDone = 23;
        static final int TRANSACTION_onPlaybackInfoChanged = 7;
        static final int TRANSACTION_onPlaybackSpeedChanged = 3;
        static final int TRANSACTION_onPlayerStateChanged = 2;
        static final int TRANSACTION_onPlaylistChanged = 5;
        static final int TRANSACTION_onPlaylistMetadataChanged = 6;
        static final int TRANSACTION_onRepeatModeChanged = 8;
        static final int TRANSACTION_onRoutesInfoChanged = 12;
        static final int TRANSACTION_onSearchResultChanged = 22;
        static final int TRANSACTION_onSeekCompleted = 10;
        static final int TRANSACTION_onShuffleModeChanged = 9;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IMediaController2 asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IMediaController2)) {
                return new Proxy(obj);
            }
            return (IMediaController2) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            Bundle _arg1;
            Bundle _arg3;
            Bundle _arg8;
            Bundle _arg0;
            Bundle _arg12;
            Bundle _arg02;
            if (code != 1598968902) {
                Bundle _arg03 = null;
                Bundle _arg4 = null;
                Bundle _arg2 = null;
                Bundle _arg42 = null;
                Bundle _arg22 = null;
                Bundle _arg13 = null;
                Bundle _arg23 = null;
                ResultReceiver _arg24 = null;
                Bundle _arg04 = null;
                PendingIntent _arg122 = null;
                Bundle _arg14 = null;
                Bundle _arg05 = null;
                Bundle _arg06 = null;
                Bundle _arg15 = null;
                Bundle _arg07 = null;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg03 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        }
                        onCurrentMediaItemChanged(_arg03);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        onPlayerStateChanged(data.readLong(), data.readLong(), data.readInt());
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        onPlaybackSpeedChanged(data.readLong(), data.readLong(), data.readFloat());
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg07 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        }
                        onBufferingStateChanged(_arg07, data.readInt(), data.readLong());
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        List<Bundle> _arg08 = data.createTypedArrayList(Bundle.CREATOR);
                        if (data.readInt() != 0) {
                            _arg15 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        }
                        onPlaylistChanged(_arg08, _arg15);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg06 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        }
                        onPlaylistMetadataChanged(_arg06);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg05 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        }
                        onPlaybackInfoChanged(_arg05);
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        onRepeatModeChanged(data.readInt());
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        onShuffleModeChanged(data.readInt());
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        onSeekCompleted(data.readLong(), data.readLong(), data.readLong());
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg09 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg14 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        }
                        onError(_arg09, _arg14);
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        onRoutesInfoChanged(data.createTypedArrayList(Bundle.CREATOR));
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        IMediaSession2 _arg010 = IMediaSession2.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg1 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg1 = null;
                        }
                        int _arg25 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg3 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg3 = null;
                        }
                        long _arg43 = data.readLong();
                        long _arg5 = data.readLong();
                        float _arg6 = data.readFloat();
                        long _arg7 = data.readLong();
                        if (data.readInt() != 0) {
                            _arg8 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg8 = null;
                        }
                        int _arg9 = data.readInt();
                        int _arg10 = data.readInt();
                        List<Bundle> _arg11 = data.createTypedArrayList(Bundle.CREATOR);
                        if (data.readInt() != 0) {
                            _arg122 = (PendingIntent) PendingIntent.CREATOR.createFromParcel(data);
                        }
                        onConnected(_arg010, _arg1, _arg25, _arg3, _arg43, _arg5, _arg6, _arg7, _arg8, _arg9, _arg10, _arg11, _arg122);
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        onDisconnected();
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        onCustomLayoutChanged(data.createTypedArrayList(Bundle.CREATOR));
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg04 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        }
                        onAllowedCommandsChanged(_arg04);
                        return true;
                    case 17:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg12 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg12 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg24 = (ResultReceiver) ResultReceiver.CREATOR.createFromParcel(data);
                        }
                        onCustomCommand(_arg0, _arg12, _arg24);
                        return true;
                    case 18:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg02 = null;
                        }
                        String _arg16 = data.readString();
                        if (data.readInt() != 0) {
                            _arg23 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        }
                        onGetLibraryRootDone(_arg02, _arg16, _arg23);
                        return true;
                    case 19:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg011 = data.readString();
                        if (data.readInt() != 0) {
                            _arg13 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        }
                        onGetItemDone(_arg011, _arg13);
                        return true;
                    case 20:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg012 = data.readString();
                        int _arg17 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg22 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        }
                        onChildrenChanged(_arg012, _arg17, _arg22);
                        return true;
                    case 21:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg013 = data.readString();
                        int _arg18 = data.readInt();
                        int _arg26 = data.readInt();
                        List<Bundle> _arg32 = data.createTypedArrayList(Bundle.CREATOR);
                        if (data.readInt() != 0) {
                            _arg42 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        }
                        onGetChildrenDone(_arg013, _arg18, _arg26, _arg32, _arg42);
                        return true;
                    case 22:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg014 = data.readString();
                        int _arg19 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg2 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        }
                        onSearchResultChanged(_arg014, _arg19, _arg2);
                        return true;
                    case 23:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg015 = data.readString();
                        int _arg110 = data.readInt();
                        int _arg27 = data.readInt();
                        List<Bundle> _arg33 = data.createTypedArrayList(Bundle.CREATOR);
                        if (data.readInt() != 0) {
                            _arg4 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        }
                        onGetSearchResultDone(_arg015, _arg110, _arg27, _arg33, _arg4);
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
        public static class Proxy implements IMediaController2 {
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

            @Override // android.support.v4.media.IMediaController2
            public void onCurrentMediaItemChanged(Bundle item) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (item != null) {
                        _data.writeInt(1);
                        item.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.IMediaController2
            public void onPlayerStateChanged(long eventTimeMs, long positionMs, int state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(eventTimeMs);
                    _data.writeLong(positionMs);
                    _data.writeInt(state);
                    this.mRemote.transact(2, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.IMediaController2
            public void onPlaybackSpeedChanged(long eventTimeMs, long positionMs, float speed) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(eventTimeMs);
                    _data.writeLong(positionMs);
                    _data.writeFloat(speed);
                    this.mRemote.transact(3, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.IMediaController2
            public void onBufferingStateChanged(Bundle item, int state, long bufferedPositionMs) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (item != null) {
                        _data.writeInt(1);
                        item.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(state);
                    _data.writeLong(bufferedPositionMs);
                    this.mRemote.transact(4, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.IMediaController2
            public void onPlaylistChanged(List<Bundle> playlist, Bundle metadata) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeTypedList(playlist);
                    if (metadata != null) {
                        _data.writeInt(1);
                        metadata.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(5, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.IMediaController2
            public void onPlaylistMetadataChanged(Bundle metadata) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (metadata != null) {
                        _data.writeInt(1);
                        metadata.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(6, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.IMediaController2
            public void onPlaybackInfoChanged(Bundle playbackInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (playbackInfo != null) {
                        _data.writeInt(1);
                        playbackInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(7, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.IMediaController2
            public void onRepeatModeChanged(int repeatMode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(repeatMode);
                    this.mRemote.transact(8, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.IMediaController2
            public void onShuffleModeChanged(int shuffleMode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(shuffleMode);
                    this.mRemote.transact(9, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.IMediaController2
            public void onSeekCompleted(long eventTimeMs, long positionMs, long seekPositionMs) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(eventTimeMs);
                    _data.writeLong(positionMs);
                    _data.writeLong(seekPositionMs);
                    this.mRemote.transact(10, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.IMediaController2
            public void onError(int errorCode, Bundle extras) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(errorCode);
                    if (extras != null) {
                        _data.writeInt(1);
                        extras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(11, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.IMediaController2
            public void onRoutesInfoChanged(List<Bundle> routes) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeTypedList(routes);
                    this.mRemote.transact(12, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.IMediaController2
            public void onConnected(IMediaSession2 sessionBinder, Bundle commandGroup, int playerState, Bundle currentItem, long positionEventTimeMs, long positionMs, float playbackSpeed, long bufferedPositionMs, Bundle playbackInfo, int repeatMode, int shuffleMode, List<Bundle> playlist, PendingIntent sessionActivity) throws RemoteException {
                Throwable th;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(sessionBinder != null ? sessionBinder.asBinder() : null);
                    if (commandGroup != null) {
                        _data.writeInt(1);
                        commandGroup.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(playerState);
                    if (currentItem != null) {
                        _data.writeInt(1);
                        currentItem.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    try {
                        _data.writeLong(positionEventTimeMs);
                    } catch (Throwable th2) {
                        th = th2;
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeLong(positionMs);
                        try {
                            _data.writeFloat(playbackSpeed);
                            try {
                                _data.writeLong(bufferedPositionMs);
                                if (playbackInfo != null) {
                                    _data.writeInt(1);
                                    playbackInfo.writeToParcel(_data, 0);
                                } else {
                                    _data.writeInt(0);
                                }
                            } catch (Throwable th3) {
                                th = th3;
                                _data.recycle();
                                throw th;
                            }
                        } catch (Throwable th4) {
                            th = th4;
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th5) {
                        th = th5;
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(repeatMode);
                        try {
                            _data.writeInt(shuffleMode);
                            _data.writeTypedList(playlist);
                            if (sessionActivity != null) {
                                _data.writeInt(1);
                                sessionActivity.writeToParcel(_data, 0);
                            } else {
                                _data.writeInt(0);
                            }
                            this.mRemote.transact(13, _data, null, 1);
                            _data.recycle();
                        } catch (Throwable th6) {
                            th = th6;
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th7) {
                        th = th7;
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th8) {
                    th = th8;
                    _data.recycle();
                    throw th;
                }
            }

            @Override // android.support.v4.media.IMediaController2
            public void onDisconnected() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(14, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.IMediaController2
            public void onCustomLayoutChanged(List<Bundle> commandButtonlist) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeTypedList(commandButtonlist);
                    this.mRemote.transact(15, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.IMediaController2
            public void onAllowedCommandsChanged(Bundle commands) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (commands != null) {
                        _data.writeInt(1);
                        commands.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(16, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.IMediaController2
            public void onCustomCommand(Bundle command, Bundle args, ResultReceiver receiver) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
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
                    this.mRemote.transact(17, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.IMediaController2
            public void onGetLibraryRootDone(Bundle rootHints, String rootMediaId, Bundle rootExtra) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (rootHints != null) {
                        _data.writeInt(1);
                        rootHints.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(rootMediaId);
                    if (rootExtra != null) {
                        _data.writeInt(1);
                        rootExtra.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(18, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.IMediaController2
            public void onGetItemDone(String mediaId, Bundle result) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(mediaId);
                    if (result != null) {
                        _data.writeInt(1);
                        result.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(19, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.IMediaController2
            public void onChildrenChanged(String parentId, int itemCount, Bundle extras) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(parentId);
                    _data.writeInt(itemCount);
                    if (extras != null) {
                        _data.writeInt(1);
                        extras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(20, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.IMediaController2
            public void onGetChildrenDone(String parentId, int page, int pageSize, List<Bundle> result, Bundle extras) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(parentId);
                    _data.writeInt(page);
                    _data.writeInt(pageSize);
                    _data.writeTypedList(result);
                    if (extras != null) {
                        _data.writeInt(1);
                        extras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(21, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.IMediaController2
            public void onSearchResultChanged(String query, int itemCount, Bundle extras) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(query);
                    _data.writeInt(itemCount);
                    if (extras != null) {
                        _data.writeInt(1);
                        extras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(22, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.IMediaController2
            public void onGetSearchResultDone(String query, int page, int pageSize, List<Bundle> result, Bundle extras) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(query);
                    _data.writeInt(page);
                    _data.writeInt(pageSize);
                    _data.writeTypedList(result);
                    if (extras != null) {
                        _data.writeInt(1);
                        extras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(23, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }
        }
    }
}
