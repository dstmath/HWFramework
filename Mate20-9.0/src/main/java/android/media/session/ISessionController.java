package android.media.session;

import android.app.PendingIntent;
import android.content.pm.ParceledListSlice;
import android.media.MediaMetadata;
import android.media.Rating;
import android.media.session.ISessionControllerCallback;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.text.TextUtils;
import android.view.KeyEvent;

public interface ISessionController extends IInterface {

    public static abstract class Stub extends Binder implements ISessionController {
        private static final String DESCRIPTOR = "android.media.session.ISessionController";
        static final int TRANSACTION_adjustVolume = 11;
        static final int TRANSACTION_fastForward = 26;
        static final int TRANSACTION_getExtras = 35;
        static final int TRANSACTION_getFlags = 9;
        static final int TRANSACTION_getLaunchPendingIntent = 8;
        static final int TRANSACTION_getMetadata = 31;
        static final int TRANSACTION_getPackageName = 6;
        static final int TRANSACTION_getPlaybackState = 32;
        static final int TRANSACTION_getQueue = 33;
        static final int TRANSACTION_getQueueTitle = 34;
        static final int TRANSACTION_getRatingType = 36;
        static final int TRANSACTION_getTag = 7;
        static final int TRANSACTION_getVolumeAttributes = 10;
        static final int TRANSACTION_isTransportControlEnabled = 5;
        static final int TRANSACTION_next = 24;
        static final int TRANSACTION_pause = 22;
        static final int TRANSACTION_play = 17;
        static final int TRANSACTION_playFromMediaId = 18;
        static final int TRANSACTION_playFromSearch = 19;
        static final int TRANSACTION_playFromUri = 20;
        static final int TRANSACTION_prepare = 13;
        static final int TRANSACTION_prepareFromMediaId = 14;
        static final int TRANSACTION_prepareFromSearch = 15;
        static final int TRANSACTION_prepareFromUri = 16;
        static final int TRANSACTION_previous = 25;
        static final int TRANSACTION_rate = 29;
        static final int TRANSACTION_registerCallbackListener = 3;
        static final int TRANSACTION_rewind = 27;
        static final int TRANSACTION_seekTo = 28;
        static final int TRANSACTION_sendCommand = 1;
        static final int TRANSACTION_sendCustomAction = 30;
        static final int TRANSACTION_sendMediaButton = 2;
        static final int TRANSACTION_setVolumeTo = 12;
        static final int TRANSACTION_skipToQueueItem = 21;
        static final int TRANSACTION_stop = 23;
        static final int TRANSACTION_unregisterCallbackListener = 4;

        private static class Proxy implements ISessionController {
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

            public void sendCommand(String packageName, ISessionControllerCallback caller, String command, Bundle args, ResultReceiver cb) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    _data.writeString(command);
                    if (args != null) {
                        _data.writeInt(1);
                        args.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (cb != null) {
                        _data.writeInt(1);
                        cb.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean sendMediaButton(String packageName, ISessionControllerCallback caller, boolean asSystemService, KeyEvent mediaButton) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    _data.writeInt(asSystemService);
                    boolean _result = true;
                    if (mediaButton != null) {
                        _data.writeInt(1);
                        mediaButton.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void registerCallbackListener(String packageName, ISessionControllerCallback cb) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeStrongBinder(cb != null ? cb.asBinder() : null);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void unregisterCallbackListener(ISessionControllerCallback cb) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(cb != null ? cb.asBinder() : null);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isTransportControlEnabled() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getPackageName() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getTag() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public PendingIntent getLaunchPendingIntent() throws RemoteException {
                PendingIntent _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = PendingIntent.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long getFlags() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(9, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readLong();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ParcelableVolumeInfo getVolumeAttributes() throws RemoteException {
                ParcelableVolumeInfo _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(10, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ParcelableVolumeInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void adjustVolume(String packageName, ISessionControllerCallback caller, boolean asSystemService, int direction, int flags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    _data.writeInt(asSystemService);
                    _data.writeInt(direction);
                    _data.writeInt(flags);
                    this.mRemote.transact(11, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setVolumeTo(String packageName, ISessionControllerCallback caller, int value, int flags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    _data.writeInt(value);
                    _data.writeInt(flags);
                    this.mRemote.transact(12, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void prepare(String packageName, ISessionControllerCallback caller) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    this.mRemote.transact(13, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void prepareFromMediaId(String packageName, ISessionControllerCallback caller, String mediaId, Bundle extras) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    _data.writeString(mediaId);
                    if (extras != null) {
                        _data.writeInt(1);
                        extras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(14, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void prepareFromSearch(String packageName, ISessionControllerCallback caller, String string, Bundle extras) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    _data.writeString(string);
                    if (extras != null) {
                        _data.writeInt(1);
                        extras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(15, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void prepareFromUri(String packageName, ISessionControllerCallback caller, Uri uri, Bundle extras) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
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
                    this.mRemote.transact(16, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void play(String packageName, ISessionControllerCallback caller) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    this.mRemote.transact(17, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void playFromMediaId(String packageName, ISessionControllerCallback caller, String mediaId, Bundle extras) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    _data.writeString(mediaId);
                    if (extras != null) {
                        _data.writeInt(1);
                        extras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(18, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void playFromSearch(String packageName, ISessionControllerCallback caller, String string, Bundle extras) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    _data.writeString(string);
                    if (extras != null) {
                        _data.writeInt(1);
                        extras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(19, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void playFromUri(String packageName, ISessionControllerCallback caller, Uri uri, Bundle extras) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
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
                    this.mRemote.transact(20, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void skipToQueueItem(String packageName, ISessionControllerCallback caller, long id) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    _data.writeLong(id);
                    this.mRemote.transact(21, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void pause(String packageName, ISessionControllerCallback caller) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    this.mRemote.transact(22, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void stop(String packageName, ISessionControllerCallback caller) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    this.mRemote.transact(23, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void next(String packageName, ISessionControllerCallback caller) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    this.mRemote.transact(24, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void previous(String packageName, ISessionControllerCallback caller) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    this.mRemote.transact(25, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void fastForward(String packageName, ISessionControllerCallback caller) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    this.mRemote.transact(26, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void rewind(String packageName, ISessionControllerCallback caller) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    this.mRemote.transact(27, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void seekTo(String packageName, ISessionControllerCallback caller, long pos) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    _data.writeLong(pos);
                    this.mRemote.transact(28, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void rate(String packageName, ISessionControllerCallback caller, Rating rating) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    if (rating != null) {
                        _data.writeInt(1);
                        rating.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(29, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void sendCustomAction(String packageName, ISessionControllerCallback caller, String action, Bundle args) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    _data.writeString(action);
                    if (args != null) {
                        _data.writeInt(1);
                        args.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(30, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public MediaMetadata getMetadata() throws RemoteException {
                MediaMetadata _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(31, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = MediaMetadata.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public PlaybackState getPlaybackState() throws RemoteException {
                PlaybackState _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(32, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = PlaybackState.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ParceledListSlice getQueue() throws RemoteException {
                ParceledListSlice _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(33, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (ParceledListSlice) ParceledListSlice.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public CharSequence getQueueTitle() throws RemoteException {
                CharSequence _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(34, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (CharSequence) TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public Bundle getExtras() throws RemoteException {
                Bundle _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(35, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (Bundle) Bundle.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getRatingType() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(36, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ISessionController asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ISessionController)) {
                return new Proxy(obj);
            }
            return (ISessionController) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v2, resolved type: android.view.KeyEvent} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v17, resolved type: android.os.Bundle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v21, resolved type: android.os.Bundle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v25, resolved type: android.os.Bundle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v31, resolved type: android.os.Bundle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v35, resolved type: android.os.Bundle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v39, resolved type: android.os.Bundle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v59, resolved type: android.media.Rating} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v63, resolved type: android.os.Bundle} */
        /* JADX WARNING: type inference failed for: r1v0 */
        /* JADX WARNING: type inference failed for: r1v72 */
        /* JADX WARNING: type inference failed for: r1v73 */
        /* JADX WARNING: type inference failed for: r1v74 */
        /* JADX WARNING: type inference failed for: r1v75 */
        /* JADX WARNING: type inference failed for: r1v76 */
        /* JADX WARNING: type inference failed for: r1v77 */
        /* JADX WARNING: type inference failed for: r1v78 */
        /* JADX WARNING: type inference failed for: r1v79 */
        /* JADX WARNING: type inference failed for: r1v80 */
        /* JADX WARNING: Multi-variable type inference failed */
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            Bundle _arg3;
            ResultReceiver _arg4;
            Uri _arg2;
            Uri _arg22;
            int i = code;
            Parcel parcel = data;
            Parcel parcel2 = reply;
            if (i != 1598968902) {
                boolean _arg23 = false;
                ? _arg32 = 0;
                switch (i) {
                    case 1:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg0 = data.readString();
                        ISessionControllerCallback _arg1 = ISessionControllerCallback.Stub.asInterface(data.readStrongBinder());
                        String _arg24 = data.readString();
                        if (data.readInt() != 0) {
                            _arg3 = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg3 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg4 = (ResultReceiver) ResultReceiver.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg4 = null;
                        }
                        sendCommand(_arg0, _arg1, _arg24, _arg3, _arg4);
                        reply.writeNoException();
                        return true;
                    case 2:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg02 = data.readString();
                        ISessionControllerCallback _arg12 = ISessionControllerCallback.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg23 = true;
                        }
                        if (data.readInt() != 0) {
                            _arg32 = (KeyEvent) KeyEvent.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result = sendMediaButton(_arg02, _arg12, _arg23, _arg32);
                        reply.writeNoException();
                        parcel2.writeInt(_result);
                        return true;
                    case 3:
                        parcel.enforceInterface(DESCRIPTOR);
                        registerCallbackListener(data.readString(), ISessionControllerCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 4:
                        parcel.enforceInterface(DESCRIPTOR);
                        unregisterCallbackListener(ISessionControllerCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 5:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result2 = isTransportControlEnabled();
                        reply.writeNoException();
                        parcel2.writeInt(_result2);
                        return true;
                    case 6:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _result3 = getPackageName();
                        reply.writeNoException();
                        parcel2.writeString(_result3);
                        return true;
                    case 7:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _result4 = getTag();
                        reply.writeNoException();
                        parcel2.writeString(_result4);
                        return true;
                    case 8:
                        parcel.enforceInterface(DESCRIPTOR);
                        PendingIntent _result5 = getLaunchPendingIntent();
                        reply.writeNoException();
                        if (_result5 != null) {
                            parcel2.writeInt(1);
                            _result5.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 9:
                        parcel.enforceInterface(DESCRIPTOR);
                        long _result6 = getFlags();
                        reply.writeNoException();
                        parcel2.writeLong(_result6);
                        return true;
                    case 10:
                        parcel.enforceInterface(DESCRIPTOR);
                        ParcelableVolumeInfo _result7 = getVolumeAttributes();
                        reply.writeNoException();
                        if (_result7 != null) {
                            parcel2.writeInt(1);
                            _result7.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 11:
                        parcel.enforceInterface(DESCRIPTOR);
                        adjustVolume(data.readString(), ISessionControllerCallback.Stub.asInterface(data.readStrongBinder()), data.readInt() != 0, data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 12:
                        parcel.enforceInterface(DESCRIPTOR);
                        setVolumeTo(data.readString(), ISessionControllerCallback.Stub.asInterface(data.readStrongBinder()), data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 13:
                        parcel.enforceInterface(DESCRIPTOR);
                        prepare(data.readString(), ISessionControllerCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 14:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg03 = data.readString();
                        ISessionControllerCallback _arg13 = ISessionControllerCallback.Stub.asInterface(data.readStrongBinder());
                        String _arg25 = data.readString();
                        if (data.readInt() != 0) {
                            _arg32 = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                        }
                        prepareFromMediaId(_arg03, _arg13, _arg25, _arg32);
                        reply.writeNoException();
                        return true;
                    case 15:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg04 = data.readString();
                        ISessionControllerCallback _arg14 = ISessionControllerCallback.Stub.asInterface(data.readStrongBinder());
                        String _arg26 = data.readString();
                        if (data.readInt() != 0) {
                            _arg32 = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                        }
                        prepareFromSearch(_arg04, _arg14, _arg26, _arg32);
                        reply.writeNoException();
                        return true;
                    case 16:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg05 = data.readString();
                        ISessionControllerCallback _arg15 = ISessionControllerCallback.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg2 = Uri.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg2 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg32 = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                        }
                        prepareFromUri(_arg05, _arg15, _arg2, _arg32);
                        reply.writeNoException();
                        return true;
                    case 17:
                        parcel.enforceInterface(DESCRIPTOR);
                        play(data.readString(), ISessionControllerCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 18:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg06 = data.readString();
                        ISessionControllerCallback _arg16 = ISessionControllerCallback.Stub.asInterface(data.readStrongBinder());
                        String _arg27 = data.readString();
                        if (data.readInt() != 0) {
                            _arg32 = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                        }
                        playFromMediaId(_arg06, _arg16, _arg27, _arg32);
                        reply.writeNoException();
                        return true;
                    case 19:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg07 = data.readString();
                        ISessionControllerCallback _arg17 = ISessionControllerCallback.Stub.asInterface(data.readStrongBinder());
                        String _arg28 = data.readString();
                        if (data.readInt() != 0) {
                            _arg32 = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                        }
                        playFromSearch(_arg07, _arg17, _arg28, _arg32);
                        reply.writeNoException();
                        return true;
                    case 20:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg08 = data.readString();
                        ISessionControllerCallback _arg18 = ISessionControllerCallback.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg22 = Uri.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg22 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg32 = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                        }
                        playFromUri(_arg08, _arg18, _arg22, _arg32);
                        reply.writeNoException();
                        return true;
                    case 21:
                        parcel.enforceInterface(DESCRIPTOR);
                        skipToQueueItem(data.readString(), ISessionControllerCallback.Stub.asInterface(data.readStrongBinder()), data.readLong());
                        reply.writeNoException();
                        return true;
                    case 22:
                        parcel.enforceInterface(DESCRIPTOR);
                        pause(data.readString(), ISessionControllerCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 23:
                        parcel.enforceInterface(DESCRIPTOR);
                        stop(data.readString(), ISessionControllerCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 24:
                        parcel.enforceInterface(DESCRIPTOR);
                        next(data.readString(), ISessionControllerCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 25:
                        parcel.enforceInterface(DESCRIPTOR);
                        previous(data.readString(), ISessionControllerCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 26:
                        parcel.enforceInterface(DESCRIPTOR);
                        fastForward(data.readString(), ISessionControllerCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 27:
                        parcel.enforceInterface(DESCRIPTOR);
                        rewind(data.readString(), ISessionControllerCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 28:
                        parcel.enforceInterface(DESCRIPTOR);
                        seekTo(data.readString(), ISessionControllerCallback.Stub.asInterface(data.readStrongBinder()), data.readLong());
                        reply.writeNoException();
                        return true;
                    case 29:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg09 = data.readString();
                        ISessionControllerCallback _arg19 = ISessionControllerCallback.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg32 = Rating.CREATOR.createFromParcel(parcel);
                        }
                        rate(_arg09, _arg19, _arg32);
                        reply.writeNoException();
                        return true;
                    case 30:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg010 = data.readString();
                        ISessionControllerCallback _arg110 = ISessionControllerCallback.Stub.asInterface(data.readStrongBinder());
                        String _arg29 = data.readString();
                        if (data.readInt() != 0) {
                            _arg32 = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                        }
                        sendCustomAction(_arg010, _arg110, _arg29, _arg32);
                        reply.writeNoException();
                        return true;
                    case 31:
                        parcel.enforceInterface(DESCRIPTOR);
                        MediaMetadata _result8 = getMetadata();
                        reply.writeNoException();
                        if (_result8 != null) {
                            parcel2.writeInt(1);
                            _result8.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 32:
                        parcel.enforceInterface(DESCRIPTOR);
                        PlaybackState _result9 = getPlaybackState();
                        reply.writeNoException();
                        if (_result9 != null) {
                            parcel2.writeInt(1);
                            _result9.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 33:
                        parcel.enforceInterface(DESCRIPTOR);
                        ParceledListSlice _result10 = getQueue();
                        reply.writeNoException();
                        if (_result10 != null) {
                            parcel2.writeInt(1);
                            _result10.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 34:
                        parcel.enforceInterface(DESCRIPTOR);
                        CharSequence _result11 = getQueueTitle();
                        reply.writeNoException();
                        if (_result11 != null) {
                            parcel2.writeInt(1);
                            TextUtils.writeToParcel(_result11, parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 35:
                        parcel.enforceInterface(DESCRIPTOR);
                        Bundle _result12 = getExtras();
                        reply.writeNoException();
                        if (_result12 != null) {
                            parcel2.writeInt(1);
                            _result12.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 36:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result13 = getRatingType();
                        reply.writeNoException();
                        parcel2.writeInt(_result13);
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                parcel2.writeString(DESCRIPTOR);
                return true;
            }
        }
    }

    void adjustVolume(String str, ISessionControllerCallback iSessionControllerCallback, boolean z, int i, int i2) throws RemoteException;

    void fastForward(String str, ISessionControllerCallback iSessionControllerCallback) throws RemoteException;

    Bundle getExtras() throws RemoteException;

    long getFlags() throws RemoteException;

    PendingIntent getLaunchPendingIntent() throws RemoteException;

    MediaMetadata getMetadata() throws RemoteException;

    String getPackageName() throws RemoteException;

    PlaybackState getPlaybackState() throws RemoteException;

    ParceledListSlice getQueue() throws RemoteException;

    CharSequence getQueueTitle() throws RemoteException;

    int getRatingType() throws RemoteException;

    String getTag() throws RemoteException;

    ParcelableVolumeInfo getVolumeAttributes() throws RemoteException;

    boolean isTransportControlEnabled() throws RemoteException;

    void next(String str, ISessionControllerCallback iSessionControllerCallback) throws RemoteException;

    void pause(String str, ISessionControllerCallback iSessionControllerCallback) throws RemoteException;

    void play(String str, ISessionControllerCallback iSessionControllerCallback) throws RemoteException;

    void playFromMediaId(String str, ISessionControllerCallback iSessionControllerCallback, String str2, Bundle bundle) throws RemoteException;

    void playFromSearch(String str, ISessionControllerCallback iSessionControllerCallback, String str2, Bundle bundle) throws RemoteException;

    void playFromUri(String str, ISessionControllerCallback iSessionControllerCallback, Uri uri, Bundle bundle) throws RemoteException;

    void prepare(String str, ISessionControllerCallback iSessionControllerCallback) throws RemoteException;

    void prepareFromMediaId(String str, ISessionControllerCallback iSessionControllerCallback, String str2, Bundle bundle) throws RemoteException;

    void prepareFromSearch(String str, ISessionControllerCallback iSessionControllerCallback, String str2, Bundle bundle) throws RemoteException;

    void prepareFromUri(String str, ISessionControllerCallback iSessionControllerCallback, Uri uri, Bundle bundle) throws RemoteException;

    void previous(String str, ISessionControllerCallback iSessionControllerCallback) throws RemoteException;

    void rate(String str, ISessionControllerCallback iSessionControllerCallback, Rating rating) throws RemoteException;

    void registerCallbackListener(String str, ISessionControllerCallback iSessionControllerCallback) throws RemoteException;

    void rewind(String str, ISessionControllerCallback iSessionControllerCallback) throws RemoteException;

    void seekTo(String str, ISessionControllerCallback iSessionControllerCallback, long j) throws RemoteException;

    void sendCommand(String str, ISessionControllerCallback iSessionControllerCallback, String str2, Bundle bundle, ResultReceiver resultReceiver) throws RemoteException;

    void sendCustomAction(String str, ISessionControllerCallback iSessionControllerCallback, String str2, Bundle bundle) throws RemoteException;

    boolean sendMediaButton(String str, ISessionControllerCallback iSessionControllerCallback, boolean z, KeyEvent keyEvent) throws RemoteException;

    void setVolumeTo(String str, ISessionControllerCallback iSessionControllerCallback, int i, int i2) throws RemoteException;

    void skipToQueueItem(String str, ISessionControllerCallback iSessionControllerCallback, long j) throws RemoteException;

    void stop(String str, ISessionControllerCallback iSessionControllerCallback) throws RemoteException;

    void unregisterCallbackListener(ISessionControllerCallback iSessionControllerCallback) throws RemoteException;
}
