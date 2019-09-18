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
import java.util.ArrayList;
import java.util.List;

public interface IMediaController2 extends IInterface {

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

        private static class Proxy implements IMediaController2 {
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

            public void onConnected(IMediaSession2 sessionBinder, Bundle commandGroup, int playerState, Bundle currentItem, long positionEventTimeMs, long positionMs, float playbackSpeed, long bufferedPositionMs, Bundle playbackInfo, int repeatMode, int shuffleMode, List<Bundle> playlist, PendingIntent sessionActivity) throws RemoteException {
                Bundle bundle = commandGroup;
                Bundle bundle2 = currentItem;
                Bundle bundle3 = playbackInfo;
                PendingIntent pendingIntent = sessionActivity;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(sessionBinder != null ? sessionBinder.asBinder() : null);
                    if (bundle != null) {
                        _data.writeInt(1);
                        bundle.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(playerState);
                    if (bundle2 != null) {
                        _data.writeInt(1);
                        bundle2.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    try {
                        _data.writeLong(positionEventTimeMs);
                    } catch (Throwable th) {
                        th = th;
                        long j = positionMs;
                        float f = playbackSpeed;
                        long j2 = bufferedPositionMs;
                        int i = repeatMode;
                        int i2 = shuffleMode;
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeLong(positionMs);
                        try {
                            _data.writeFloat(playbackSpeed);
                            try {
                                _data.writeLong(bufferedPositionMs);
                                if (bundle3 != null) {
                                    _data.writeInt(1);
                                    bundle3.writeToParcel(_data, 0);
                                } else {
                                    _data.writeInt(0);
                                }
                            } catch (Throwable th2) {
                                th = th2;
                                int i3 = repeatMode;
                                int i22 = shuffleMode;
                                _data.recycle();
                                throw th;
                            }
                        } catch (Throwable th3) {
                            th = th3;
                            long j22 = bufferedPositionMs;
                            int i32 = repeatMode;
                            int i222 = shuffleMode;
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th4) {
                        th = th4;
                        float f2 = playbackSpeed;
                        long j222 = bufferedPositionMs;
                        int i322 = repeatMode;
                        int i2222 = shuffleMode;
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(repeatMode);
                        try {
                            _data.writeInt(shuffleMode);
                            _data.writeTypedList(playlist);
                            if (pendingIntent != null) {
                                _data.writeInt(1);
                                pendingIntent.writeToParcel(_data, 0);
                            } else {
                                _data.writeInt(0);
                            }
                            this.mRemote.transact(13, _data, null, 1);
                            _data.recycle();
                        } catch (Throwable th5) {
                            th = th5;
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th6) {
                        th = th6;
                        int i22222 = shuffleMode;
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th7) {
                    th = th7;
                    long j3 = positionEventTimeMs;
                    long j4 = positionMs;
                    float f22 = playbackSpeed;
                    long j2222 = bufferedPositionMs;
                    int i3222 = repeatMode;
                    int i222222 = shuffleMode;
                    _data.recycle();
                    throw th;
                }
            }

            public void onDisconnected() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(14, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

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

        public IBinder asBinder() {
            return this;
        }

        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v3, resolved type: android.os.Bundle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v11, resolved type: android.os.Bundle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v15, resolved type: android.os.Bundle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v19, resolved type: android.os.Bundle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v23, resolved type: android.os.Bundle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v33, resolved type: android.os.Bundle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v49, resolved type: android.os.Bundle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v57, resolved type: android.os.Bundle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v61, resolved type: android.os.Bundle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v65, resolved type: android.os.Bundle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v74, resolved type: android.os.Bundle} */
        /* JADX WARNING: type inference failed for: r0v2 */
        /* JADX WARNING: type inference failed for: r0v40 */
        /* JADX WARNING: type inference failed for: r0v53, types: [android.os.ResultReceiver] */
        /* JADX WARNING: type inference failed for: r0v69 */
        /* JADX WARNING: type inference failed for: r0v78 */
        /* JADX WARNING: type inference failed for: r0v84 */
        /* JADX WARNING: type inference failed for: r0v85 */
        /* JADX WARNING: type inference failed for: r0v86 */
        /* JADX WARNING: type inference failed for: r0v87 */
        /* JADX WARNING: type inference failed for: r0v88 */
        /* JADX WARNING: type inference failed for: r0v89 */
        /* JADX WARNING: type inference failed for: r0v90 */
        /* JADX WARNING: type inference failed for: r0v91 */
        /* JADX WARNING: type inference failed for: r0v92 */
        /* JADX WARNING: type inference failed for: r0v93 */
        /* JADX WARNING: type inference failed for: r0v94 */
        /* JADX WARNING: type inference failed for: r0v95 */
        /* JADX WARNING: type inference failed for: r0v96 */
        /* JADX WARNING: type inference failed for: r0v97 */
        /* JADX WARNING: type inference failed for: r0v98 */
        /* JADX WARNING: Multi-variable type inference failed */
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            Bundle _arg1;
            Bundle _arg3;
            Bundle _arg8;
            Bundle _arg0;
            Bundle _arg12;
            Bundle _arg02;
            int i = code;
            Parcel parcel = data;
            if (i != 1598968902) {
                ? _arg2 = 0;
                switch (i) {
                    case 1:
                        Parcel parcel2 = parcel;
                        parcel2.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg2 = (Bundle) Bundle.CREATOR.createFromParcel(parcel2);
                        }
                        onCurrentMediaItemChanged(_arg2);
                        return true;
                    case 2:
                        parcel.enforceInterface(DESCRIPTOR);
                        onPlayerStateChanged(data.readLong(), data.readLong(), data.readInt());
                        return true;
                    case 3:
                        parcel.enforceInterface(DESCRIPTOR);
                        onPlaybackSpeedChanged(data.readLong(), data.readLong(), data.readFloat());
                        return true;
                    case 4:
                        Parcel parcel3 = parcel;
                        parcel3.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg2 = (Bundle) Bundle.CREATOR.createFromParcel(parcel3);
                        }
                        onBufferingStateChanged(_arg2, data.readInt(), data.readLong());
                        return true;
                    case 5:
                        Parcel parcel4 = parcel;
                        parcel4.enforceInterface(DESCRIPTOR);
                        List<Bundle> _arg03 = parcel4.createTypedArrayList(Bundle.CREATOR);
                        if (data.readInt() != 0) {
                            _arg2 = (Bundle) Bundle.CREATOR.createFromParcel(parcel4);
                        }
                        onPlaylistChanged(_arg03, _arg2);
                        return true;
                    case 6:
                        Parcel parcel5 = parcel;
                        parcel5.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg2 = (Bundle) Bundle.CREATOR.createFromParcel(parcel5);
                        }
                        onPlaylistMetadataChanged(_arg2);
                        return true;
                    case 7:
                        Parcel parcel6 = parcel;
                        parcel6.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg2 = (Bundle) Bundle.CREATOR.createFromParcel(parcel6);
                        }
                        onPlaybackInfoChanged(_arg2);
                        return true;
                    case 8:
                        parcel.enforceInterface(DESCRIPTOR);
                        onRepeatModeChanged(data.readInt());
                        return true;
                    case 9:
                        parcel.enforceInterface(DESCRIPTOR);
                        onShuffleModeChanged(data.readInt());
                        return true;
                    case 10:
                        parcel.enforceInterface(DESCRIPTOR);
                        onSeekCompleted(data.readLong(), data.readLong(), data.readLong());
                        return true;
                    case 11:
                        Parcel parcel7 = parcel;
                        parcel7.enforceInterface(DESCRIPTOR);
                        int _arg04 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg2 = (Bundle) Bundle.CREATOR.createFromParcel(parcel7);
                        }
                        onError(_arg04, _arg2);
                        return true;
                    case 12:
                        Parcel parcel8 = data;
                        parcel8.enforceInterface(DESCRIPTOR);
                        onRoutesInfoChanged(parcel8.createTypedArrayList(Bundle.CREATOR));
                        return true;
                    case 13:
                        parcel.enforceInterface(DESCRIPTOR);
                        IMediaSession2 _arg05 = IMediaSession2.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg1 = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg1 = null;
                        }
                        int _arg22 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg3 = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg3 = null;
                        }
                        long _arg4 = data.readLong();
                        long _arg5 = data.readLong();
                        float _arg6 = data.readFloat();
                        long _arg7 = data.readLong();
                        if (data.readInt() != 0) {
                            _arg8 = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg8 = null;
                        }
                        int _arg9 = data.readInt();
                        int _arg10 = data.readInt();
                        ArrayList createTypedArrayList = parcel.createTypedArrayList(Bundle.CREATOR);
                        if (data.readInt() != 0) {
                            _arg2 = (PendingIntent) PendingIntent.CREATOR.createFromParcel(parcel);
                        }
                        onConnected(_arg05, _arg1, _arg22, _arg3, _arg4, _arg5, _arg6, _arg7, _arg8, _arg9, _arg10, createTypedArrayList, _arg2);
                        return true;
                    case 14:
                        parcel.enforceInterface(DESCRIPTOR);
                        onDisconnected();
                        return true;
                    case 15:
                        parcel.enforceInterface(DESCRIPTOR);
                        onCustomLayoutChanged(parcel.createTypedArrayList(Bundle.CREATOR));
                        return true;
                    case 16:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg2 = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                        }
                        onAllowedCommandsChanged(_arg2);
                        return true;
                    case 17:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg0 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg12 = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg12 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg2 = (ResultReceiver) ResultReceiver.CREATOR.createFromParcel(parcel);
                        }
                        onCustomCommand(_arg0, _arg12, _arg2);
                        return true;
                    case 18:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg02 = null;
                        }
                        String _arg13 = data.readString();
                        if (data.readInt() != 0) {
                            _arg2 = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                        }
                        onGetLibraryRootDone(_arg02, _arg13, _arg2);
                        return true;
                    case 19:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg06 = data.readString();
                        if (data.readInt() != 0) {
                            _arg2 = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                        }
                        onGetItemDone(_arg06, _arg2);
                        return true;
                    case 20:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg07 = data.readString();
                        int _arg14 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg2 = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                        }
                        onChildrenChanged(_arg07, _arg14, _arg2);
                        return true;
                    case 21:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg08 = data.readString();
                        int _arg15 = data.readInt();
                        int _arg23 = data.readInt();
                        ArrayList createTypedArrayList2 = parcel.createTypedArrayList(Bundle.CREATOR);
                        if (data.readInt() != 0) {
                            _arg2 = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                        }
                        onGetChildrenDone(_arg08, _arg15, _arg23, createTypedArrayList2, _arg2);
                        return true;
                    case 22:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg09 = data.readString();
                        int _arg16 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg2 = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                        }
                        onSearchResultChanged(_arg09, _arg16, _arg2);
                        return true;
                    case 23:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg010 = data.readString();
                        int _arg17 = data.readInt();
                        int _arg24 = data.readInt();
                        ArrayList createTypedArrayList3 = parcel.createTypedArrayList(Bundle.CREATOR);
                        if (data.readInt() != 0) {
                            _arg2 = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                        }
                        onGetSearchResultDone(_arg010, _arg17, _arg24, createTypedArrayList3, _arg2);
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                Parcel parcel9 = parcel;
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }
    }

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
}
