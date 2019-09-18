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

        private static class Proxy implements IMediaSession2 {
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

        public IBinder asBinder() {
            return this;
        }

        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v40, resolved type: android.os.Bundle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v44, resolved type: android.os.Bundle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v48, resolved type: android.os.Bundle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v52, resolved type: android.os.Bundle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v56, resolved type: android.os.Bundle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v60, resolved type: android.os.Bundle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v64, resolved type: android.os.Bundle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v71, resolved type: android.os.Bundle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v75, resolved type: android.os.Bundle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v79, resolved type: android.os.Bundle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v83, resolved type: android.os.Bundle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v87, resolved type: android.os.Bundle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v91, resolved type: android.os.Bundle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v113, resolved type: android.os.Bundle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v117, resolved type: android.os.Bundle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v129, resolved type: android.os.Bundle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v138, resolved type: android.os.Bundle} */
        /* JADX WARNING: type inference failed for: r0v2 */
        /* JADX WARNING: type inference failed for: r0v36, types: [android.os.ResultReceiver] */
        /* JADX WARNING: type inference failed for: r0v124 */
        /* JADX WARNING: type inference failed for: r0v133 */
        /* JADX WARNING: type inference failed for: r0v146 */
        /* JADX WARNING: type inference failed for: r0v147 */
        /* JADX WARNING: type inference failed for: r0v148 */
        /* JADX WARNING: type inference failed for: r0v149 */
        /* JADX WARNING: type inference failed for: r0v150 */
        /* JADX WARNING: type inference failed for: r0v151 */
        /* JADX WARNING: type inference failed for: r0v152 */
        /* JADX WARNING: type inference failed for: r0v153 */
        /* JADX WARNING: type inference failed for: r0v154 */
        /* JADX WARNING: type inference failed for: r0v155 */
        /* JADX WARNING: type inference failed for: r0v156 */
        /* JADX WARNING: type inference failed for: r0v157 */
        /* JADX WARNING: type inference failed for: r0v158 */
        /* JADX WARNING: type inference failed for: r0v159 */
        /* JADX WARNING: type inference failed for: r0v160 */
        /* JADX WARNING: type inference failed for: r0v161 */
        /* JADX WARNING: type inference failed for: r0v162 */
        /* JADX WARNING: type inference failed for: r0v163 */
        /* JADX WARNING: type inference failed for: r0v164 */
        /* JADX WARNING: type inference failed for: r0v165 */
        /* JADX WARNING: Multi-variable type inference failed */
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            Bundle _arg1;
            Bundle _arg2;
            Uri _arg12;
            Uri _arg13;
            int i = code;
            Parcel parcel = data;
            if (i != 1598968902) {
                ? _arg22 = 0;
                switch (i) {
                    case 1:
                        parcel.enforceInterface(DESCRIPTOR);
                        connect(IMediaController2.Stub.asInterface(data.readStrongBinder()), data.readString());
                        return true;
                    case 2:
                        parcel.enforceInterface(DESCRIPTOR);
                        release(IMediaController2.Stub.asInterface(data.readStrongBinder()));
                        return true;
                    case 3:
                        parcel.enforceInterface(DESCRIPTOR);
                        setVolumeTo(IMediaController2.Stub.asInterface(data.readStrongBinder()), data.readInt(), data.readInt());
                        return true;
                    case 4:
                        parcel.enforceInterface(DESCRIPTOR);
                        adjustVolume(IMediaController2.Stub.asInterface(data.readStrongBinder()), data.readInt(), data.readInt());
                        return true;
                    case 5:
                        parcel.enforceInterface(DESCRIPTOR);
                        play(IMediaController2.Stub.asInterface(data.readStrongBinder()));
                        return true;
                    case 6:
                        parcel.enforceInterface(DESCRIPTOR);
                        pause(IMediaController2.Stub.asInterface(data.readStrongBinder()));
                        return true;
                    case 7:
                        parcel.enforceInterface(DESCRIPTOR);
                        reset(IMediaController2.Stub.asInterface(data.readStrongBinder()));
                        return true;
                    case 8:
                        parcel.enforceInterface(DESCRIPTOR);
                        prepare(IMediaController2.Stub.asInterface(data.readStrongBinder()));
                        return true;
                    case 9:
                        parcel.enforceInterface(DESCRIPTOR);
                        fastForward(IMediaController2.Stub.asInterface(data.readStrongBinder()));
                        return true;
                    case 10:
                        parcel.enforceInterface(DESCRIPTOR);
                        rewind(IMediaController2.Stub.asInterface(data.readStrongBinder()));
                        return true;
                    case 11:
                        parcel.enforceInterface(DESCRIPTOR);
                        seekTo(IMediaController2.Stub.asInterface(data.readStrongBinder()), data.readLong());
                        return true;
                    case 12:
                        parcel.enforceInterface(DESCRIPTOR);
                        IMediaController2 _arg0 = IMediaController2.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg1 = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg1 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg2 = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg2 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg22 = (ResultReceiver) ResultReceiver.CREATOR.createFromParcel(parcel);
                        }
                        sendCustomCommand(_arg0, _arg1, _arg2, _arg22);
                        return true;
                    case 13:
                        parcel.enforceInterface(DESCRIPTOR);
                        IMediaController2 _arg02 = IMediaController2.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg12 = (Uri) Uri.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg12 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg22 = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                        }
                        prepareFromUri(_arg02, _arg12, _arg22);
                        return true;
                    case 14:
                        parcel.enforceInterface(DESCRIPTOR);
                        IMediaController2 _arg03 = IMediaController2.Stub.asInterface(data.readStrongBinder());
                        String _arg14 = data.readString();
                        if (data.readInt() != 0) {
                            _arg22 = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                        }
                        prepareFromSearch(_arg03, _arg14, _arg22);
                        return true;
                    case 15:
                        parcel.enforceInterface(DESCRIPTOR);
                        IMediaController2 _arg04 = IMediaController2.Stub.asInterface(data.readStrongBinder());
                        String _arg15 = data.readString();
                        if (data.readInt() != 0) {
                            _arg22 = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                        }
                        prepareFromMediaId(_arg04, _arg15, _arg22);
                        return true;
                    case 16:
                        parcel.enforceInterface(DESCRIPTOR);
                        IMediaController2 _arg05 = IMediaController2.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg13 = (Uri) Uri.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg13 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg22 = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                        }
                        playFromUri(_arg05, _arg13, _arg22);
                        return true;
                    case 17:
                        parcel.enforceInterface(DESCRIPTOR);
                        IMediaController2 _arg06 = IMediaController2.Stub.asInterface(data.readStrongBinder());
                        String _arg16 = data.readString();
                        if (data.readInt() != 0) {
                            _arg22 = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                        }
                        playFromSearch(_arg06, _arg16, _arg22);
                        return true;
                    case 18:
                        parcel.enforceInterface(DESCRIPTOR);
                        IMediaController2 _arg07 = IMediaController2.Stub.asInterface(data.readStrongBinder());
                        String _arg17 = data.readString();
                        if (data.readInt() != 0) {
                            _arg22 = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                        }
                        playFromMediaId(_arg07, _arg17, _arg22);
                        return true;
                    case 19:
                        parcel.enforceInterface(DESCRIPTOR);
                        IMediaController2 _arg08 = IMediaController2.Stub.asInterface(data.readStrongBinder());
                        String _arg18 = data.readString();
                        if (data.readInt() != 0) {
                            _arg22 = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                        }
                        setRating(_arg08, _arg18, _arg22);
                        return true;
                    case 20:
                        parcel.enforceInterface(DESCRIPTOR);
                        setPlaybackSpeed(IMediaController2.Stub.asInterface(data.readStrongBinder()), data.readFloat());
                        return true;
                    case 21:
                        parcel.enforceInterface(DESCRIPTOR);
                        IMediaController2 _arg09 = IMediaController2.Stub.asInterface(data.readStrongBinder());
                        List<Bundle> _arg19 = parcel.createTypedArrayList(Bundle.CREATOR);
                        if (data.readInt() != 0) {
                            _arg22 = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                        }
                        setPlaylist(_arg09, _arg19, _arg22);
                        return true;
                    case 22:
                        parcel.enforceInterface(DESCRIPTOR);
                        IMediaController2 _arg010 = IMediaController2.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg22 = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                        }
                        updatePlaylistMetadata(_arg010, _arg22);
                        return true;
                    case 23:
                        parcel.enforceInterface(DESCRIPTOR);
                        IMediaController2 _arg011 = IMediaController2.Stub.asInterface(data.readStrongBinder());
                        int _arg110 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg22 = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                        }
                        addPlaylistItem(_arg011, _arg110, _arg22);
                        return true;
                    case 24:
                        parcel.enforceInterface(DESCRIPTOR);
                        IMediaController2 _arg012 = IMediaController2.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg22 = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                        }
                        removePlaylistItem(_arg012, _arg22);
                        return true;
                    case 25:
                        parcel.enforceInterface(DESCRIPTOR);
                        IMediaController2 _arg013 = IMediaController2.Stub.asInterface(data.readStrongBinder());
                        int _arg111 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg22 = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                        }
                        replacePlaylistItem(_arg013, _arg111, _arg22);
                        return true;
                    case 26:
                        parcel.enforceInterface(DESCRIPTOR);
                        IMediaController2 _arg014 = IMediaController2.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg22 = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                        }
                        skipToPlaylistItem(_arg014, _arg22);
                        return true;
                    case 27:
                        parcel.enforceInterface(DESCRIPTOR);
                        skipToPreviousItem(IMediaController2.Stub.asInterface(data.readStrongBinder()));
                        return true;
                    case 28:
                        parcel.enforceInterface(DESCRIPTOR);
                        skipToNextItem(IMediaController2.Stub.asInterface(data.readStrongBinder()));
                        return true;
                    case 29:
                        parcel.enforceInterface(DESCRIPTOR);
                        setRepeatMode(IMediaController2.Stub.asInterface(data.readStrongBinder()), data.readInt());
                        return true;
                    case 30:
                        parcel.enforceInterface(DESCRIPTOR);
                        setShuffleMode(IMediaController2.Stub.asInterface(data.readStrongBinder()), data.readInt());
                        return true;
                    case 31:
                        parcel.enforceInterface(DESCRIPTOR);
                        subscribeRoutesInfo(IMediaController2.Stub.asInterface(data.readStrongBinder()));
                        return true;
                    case 32:
                        parcel.enforceInterface(DESCRIPTOR);
                        unsubscribeRoutesInfo(IMediaController2.Stub.asInterface(data.readStrongBinder()));
                        return true;
                    case 33:
                        parcel.enforceInterface(DESCRIPTOR);
                        IMediaController2 _arg015 = IMediaController2.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg22 = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                        }
                        selectRoute(_arg015, _arg22);
                        return true;
                    case 34:
                        parcel.enforceInterface(DESCRIPTOR);
                        IMediaController2 _arg016 = IMediaController2.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg22 = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                        }
                        getLibraryRoot(_arg016, _arg22);
                        return true;
                    case 35:
                        parcel.enforceInterface(DESCRIPTOR);
                        getItem(IMediaController2.Stub.asInterface(data.readStrongBinder()), data.readString());
                        return true;
                    case 36:
                        parcel.enforceInterface(DESCRIPTOR);
                        IMediaController2 _arg017 = IMediaController2.Stub.asInterface(data.readStrongBinder());
                        String _arg112 = data.readString();
                        int _arg23 = data.readInt();
                        int _arg3 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg22 = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                        }
                        getChildren(_arg017, _arg112, _arg23, _arg3, _arg22);
                        return true;
                    case 37:
                        parcel.enforceInterface(DESCRIPTOR);
                        IMediaController2 _arg018 = IMediaController2.Stub.asInterface(data.readStrongBinder());
                        String _arg113 = data.readString();
                        if (data.readInt() != 0) {
                            _arg22 = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                        }
                        search(_arg018, _arg113, _arg22);
                        return true;
                    case 38:
                        parcel.enforceInterface(DESCRIPTOR);
                        IMediaController2 _arg019 = IMediaController2.Stub.asInterface(data.readStrongBinder());
                        String _arg114 = data.readString();
                        int _arg24 = data.readInt();
                        int _arg32 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg22 = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                        }
                        getSearchResult(_arg019, _arg114, _arg24, _arg32, _arg22);
                        return true;
                    case 39:
                        parcel.enforceInterface(DESCRIPTOR);
                        IMediaController2 _arg020 = IMediaController2.Stub.asInterface(data.readStrongBinder());
                        String _arg115 = data.readString();
                        if (data.readInt() != 0) {
                            _arg22 = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                        }
                        subscribe(_arg020, _arg115, _arg22);
                        return true;
                    case 40:
                        parcel.enforceInterface(DESCRIPTOR);
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
    }

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
}
