package android.media.session;

import android.app.PendingIntent;
import android.content.pm.ParceledListSlice;
import android.media.MediaMetadata;
import android.media.Rating;
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

            public void sendCommand(String command, Bundle args, ResultReceiver cb) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(command);
                    if (args != null) {
                        _data.writeInt(Stub.TRANSACTION_sendCommand);
                        args.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (cb != null) {
                        _data.writeInt(Stub.TRANSACTION_sendCommand);
                        cb.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_sendCommand, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean sendMediaButton(KeyEvent mediaButton) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (mediaButton != null) {
                        _data.writeInt(Stub.TRANSACTION_sendCommand);
                        mediaButton.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_sendMediaButton, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void registerCallbackListener(ISessionControllerCallback cb) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (cb != null) {
                        iBinder = cb.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_registerCallbackListener, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void unregisterCallbackListener(ISessionControllerCallback cb) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (cb != null) {
                        iBinder = cb.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_unregisterCallbackListener, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_isTransportControlEnabled, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getPackageName() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getPackageName, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_getTag, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public PendingIntent getLaunchPendingIntent() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    PendingIntent pendingIntent;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getLaunchPendingIntent, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        pendingIntent = (PendingIntent) PendingIntent.CREATOR.createFromParcel(_reply);
                    } else {
                        pendingIntent = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return pendingIntent;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long getFlags() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getFlags, _data, _reply, 0);
                    _reply.readException();
                    long _result = _reply.readLong();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ParcelableVolumeInfo getVolumeAttributes() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    ParcelableVolumeInfo parcelableVolumeInfo;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getVolumeAttributes, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        parcelableVolumeInfo = (ParcelableVolumeInfo) ParcelableVolumeInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        parcelableVolumeInfo = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return parcelableVolumeInfo;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void adjustVolume(int direction, int flags, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(direction);
                    _data.writeInt(flags);
                    _data.writeString(packageName);
                    this.mRemote.transact(Stub.TRANSACTION_adjustVolume, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setVolumeTo(int value, int flags, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(value);
                    _data.writeInt(flags);
                    _data.writeString(packageName);
                    this.mRemote.transact(Stub.TRANSACTION_setVolumeTo, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void prepare() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_prepare, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void prepareFromMediaId(String mediaId, Bundle extras) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(mediaId);
                    if (extras != null) {
                        _data.writeInt(Stub.TRANSACTION_sendCommand);
                        extras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_prepareFromMediaId, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void prepareFromSearch(String string, Bundle extras) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(string);
                    if (extras != null) {
                        _data.writeInt(Stub.TRANSACTION_sendCommand);
                        extras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_prepareFromSearch, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void prepareFromUri(Uri uri, Bundle extras) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (uri != null) {
                        _data.writeInt(Stub.TRANSACTION_sendCommand);
                        uri.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (extras != null) {
                        _data.writeInt(Stub.TRANSACTION_sendCommand);
                        extras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_prepareFromUri, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void play() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_play, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void playFromMediaId(String mediaId, Bundle extras) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(mediaId);
                    if (extras != null) {
                        _data.writeInt(Stub.TRANSACTION_sendCommand);
                        extras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_playFromMediaId, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void playFromSearch(String string, Bundle extras) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(string);
                    if (extras != null) {
                        _data.writeInt(Stub.TRANSACTION_sendCommand);
                        extras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_playFromSearch, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void playFromUri(Uri uri, Bundle extras) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (uri != null) {
                        _data.writeInt(Stub.TRANSACTION_sendCommand);
                        uri.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (extras != null) {
                        _data.writeInt(Stub.TRANSACTION_sendCommand);
                        extras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_playFromUri, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void skipToQueueItem(long id) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(id);
                    this.mRemote.transact(Stub.TRANSACTION_skipToQueueItem, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void pause() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_pause, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void stop() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_stop, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void next() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_next, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void previous() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_previous, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void fastForward() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_fastForward, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void rewind() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_rewind, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void seekTo(long pos) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(pos);
                    this.mRemote.transact(Stub.TRANSACTION_seekTo, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void rate(Rating rating) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (rating != null) {
                        _data.writeInt(Stub.TRANSACTION_sendCommand);
                        rating.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_rate, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void sendCustomAction(String action, Bundle args) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(action);
                    if (args != null) {
                        _data.writeInt(Stub.TRANSACTION_sendCommand);
                        args.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_sendCustomAction, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public MediaMetadata getMetadata() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    MediaMetadata mediaMetadata;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getMetadata, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        mediaMetadata = (MediaMetadata) MediaMetadata.CREATOR.createFromParcel(_reply);
                    } else {
                        mediaMetadata = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return mediaMetadata;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public PlaybackState getPlaybackState() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    PlaybackState playbackState;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getPlaybackState, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        playbackState = (PlaybackState) PlaybackState.CREATOR.createFromParcel(_reply);
                    } else {
                        playbackState = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return playbackState;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ParceledListSlice getQueue() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    ParceledListSlice parceledListSlice;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getQueue, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        parceledListSlice = (ParceledListSlice) ParceledListSlice.CREATOR.createFromParcel(_reply);
                    } else {
                        parceledListSlice = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return parceledListSlice;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public CharSequence getQueueTitle() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    CharSequence charSequence;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getQueueTitle, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        charSequence = (CharSequence) TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(_reply);
                    } else {
                        charSequence = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return charSequence;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public Bundle getExtras() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    Bundle bundle;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getExtras, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(_reply);
                    } else {
                        bundle = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return bundle;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getRatingType() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getRatingType, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
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

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            String _arg0;
            Bundle bundle;
            boolean _result;
            String _result2;
            Uri uri;
            switch (code) {
                case TRANSACTION_sendCommand /*1*/:
                    ResultReceiver resultReceiver;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    if (data.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle = null;
                    }
                    if (data.readInt() != 0) {
                        resultReceiver = (ResultReceiver) ResultReceiver.CREATOR.createFromParcel(data);
                    } else {
                        resultReceiver = null;
                    }
                    sendCommand(_arg0, bundle, resultReceiver);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_sendMediaButton /*2*/:
                    KeyEvent keyEvent;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        keyEvent = (KeyEvent) KeyEvent.CREATOR.createFromParcel(data);
                    } else {
                        keyEvent = null;
                    }
                    _result = sendMediaButton(keyEvent);
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_sendCommand : 0);
                    return true;
                case TRANSACTION_registerCallbackListener /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    registerCallbackListener(android.media.session.ISessionControllerCallback.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case TRANSACTION_unregisterCallbackListener /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    unregisterCallbackListener(android.media.session.ISessionControllerCallback.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case TRANSACTION_isTransportControlEnabled /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = isTransportControlEnabled();
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_sendCommand : 0);
                    return true;
                case TRANSACTION_getPackageName /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getPackageName();
                    reply.writeNoException();
                    reply.writeString(_result2);
                    return true;
                case TRANSACTION_getTag /*7*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getTag();
                    reply.writeNoException();
                    reply.writeString(_result2);
                    return true;
                case TRANSACTION_getLaunchPendingIntent /*8*/:
                    data.enforceInterface(DESCRIPTOR);
                    PendingIntent _result3 = getLaunchPendingIntent();
                    reply.writeNoException();
                    if (_result3 != null) {
                        reply.writeInt(TRANSACTION_sendCommand);
                        _result3.writeToParcel(reply, TRANSACTION_sendCommand);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_getFlags /*9*/:
                    data.enforceInterface(DESCRIPTOR);
                    long _result4 = getFlags();
                    reply.writeNoException();
                    reply.writeLong(_result4);
                    return true;
                case TRANSACTION_getVolumeAttributes /*10*/:
                    data.enforceInterface(DESCRIPTOR);
                    ParcelableVolumeInfo _result5 = getVolumeAttributes();
                    reply.writeNoException();
                    if (_result5 != null) {
                        reply.writeInt(TRANSACTION_sendCommand);
                        _result5.writeToParcel(reply, TRANSACTION_sendCommand);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_adjustVolume /*11*/:
                    data.enforceInterface(DESCRIPTOR);
                    adjustVolume(data.readInt(), data.readInt(), data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setVolumeTo /*12*/:
                    data.enforceInterface(DESCRIPTOR);
                    setVolumeTo(data.readInt(), data.readInt(), data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_prepare /*13*/:
                    data.enforceInterface(DESCRIPTOR);
                    prepare();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_prepareFromMediaId /*14*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    if (data.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle = null;
                    }
                    prepareFromMediaId(_arg0, bundle);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_prepareFromSearch /*15*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    if (data.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle = null;
                    }
                    prepareFromSearch(_arg0, bundle);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_prepareFromUri /*16*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        uri = (Uri) Uri.CREATOR.createFromParcel(data);
                    } else {
                        uri = null;
                    }
                    if (data.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle = null;
                    }
                    prepareFromUri(uri, bundle);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_play /*17*/:
                    data.enforceInterface(DESCRIPTOR);
                    play();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_playFromMediaId /*18*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    if (data.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle = null;
                    }
                    playFromMediaId(_arg0, bundle);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_playFromSearch /*19*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    if (data.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle = null;
                    }
                    playFromSearch(_arg0, bundle);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_playFromUri /*20*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        uri = (Uri) Uri.CREATOR.createFromParcel(data);
                    } else {
                        uri = null;
                    }
                    if (data.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle = null;
                    }
                    playFromUri(uri, bundle);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_skipToQueueItem /*21*/:
                    data.enforceInterface(DESCRIPTOR);
                    skipToQueueItem(data.readLong());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_pause /*22*/:
                    data.enforceInterface(DESCRIPTOR);
                    pause();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_stop /*23*/:
                    data.enforceInterface(DESCRIPTOR);
                    stop();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_next /*24*/:
                    data.enforceInterface(DESCRIPTOR);
                    next();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_previous /*25*/:
                    data.enforceInterface(DESCRIPTOR);
                    previous();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_fastForward /*26*/:
                    data.enforceInterface(DESCRIPTOR);
                    fastForward();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_rewind /*27*/:
                    data.enforceInterface(DESCRIPTOR);
                    rewind();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_seekTo /*28*/:
                    data.enforceInterface(DESCRIPTOR);
                    seekTo(data.readLong());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_rate /*29*/:
                    Rating rating;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        rating = (Rating) Rating.CREATOR.createFromParcel(data);
                    } else {
                        rating = null;
                    }
                    rate(rating);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_sendCustomAction /*30*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    if (data.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle = null;
                    }
                    sendCustomAction(_arg0, bundle);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getMetadata /*31*/:
                    data.enforceInterface(DESCRIPTOR);
                    MediaMetadata _result6 = getMetadata();
                    reply.writeNoException();
                    if (_result6 != null) {
                        reply.writeInt(TRANSACTION_sendCommand);
                        _result6.writeToParcel(reply, TRANSACTION_sendCommand);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_getPlaybackState /*32*/:
                    data.enforceInterface(DESCRIPTOR);
                    PlaybackState _result7 = getPlaybackState();
                    reply.writeNoException();
                    if (_result7 != null) {
                        reply.writeInt(TRANSACTION_sendCommand);
                        _result7.writeToParcel(reply, TRANSACTION_sendCommand);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_getQueue /*33*/:
                    data.enforceInterface(DESCRIPTOR);
                    ParceledListSlice _result8 = getQueue();
                    reply.writeNoException();
                    if (_result8 != null) {
                        reply.writeInt(TRANSACTION_sendCommand);
                        _result8.writeToParcel(reply, TRANSACTION_sendCommand);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_getQueueTitle /*34*/:
                    data.enforceInterface(DESCRIPTOR);
                    CharSequence _result9 = getQueueTitle();
                    reply.writeNoException();
                    if (_result9 != null) {
                        reply.writeInt(TRANSACTION_sendCommand);
                        TextUtils.writeToParcel(_result9, reply, TRANSACTION_sendCommand);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_getExtras /*35*/:
                    data.enforceInterface(DESCRIPTOR);
                    Bundle _result10 = getExtras();
                    reply.writeNoException();
                    if (_result10 != null) {
                        reply.writeInt(TRANSACTION_sendCommand);
                        _result10.writeToParcel(reply, TRANSACTION_sendCommand);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_getRatingType /*36*/:
                    data.enforceInterface(DESCRIPTOR);
                    int _result11 = getRatingType();
                    reply.writeNoException();
                    reply.writeInt(_result11);
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void adjustVolume(int i, int i2, String str) throws RemoteException;

    void fastForward() throws RemoteException;

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

    void next() throws RemoteException;

    void pause() throws RemoteException;

    void play() throws RemoteException;

    void playFromMediaId(String str, Bundle bundle) throws RemoteException;

    void playFromSearch(String str, Bundle bundle) throws RemoteException;

    void playFromUri(Uri uri, Bundle bundle) throws RemoteException;

    void prepare() throws RemoteException;

    void prepareFromMediaId(String str, Bundle bundle) throws RemoteException;

    void prepareFromSearch(String str, Bundle bundle) throws RemoteException;

    void prepareFromUri(Uri uri, Bundle bundle) throws RemoteException;

    void previous() throws RemoteException;

    void rate(Rating rating) throws RemoteException;

    void registerCallbackListener(ISessionControllerCallback iSessionControllerCallback) throws RemoteException;

    void rewind() throws RemoteException;

    void seekTo(long j) throws RemoteException;

    void sendCommand(String str, Bundle bundle, ResultReceiver resultReceiver) throws RemoteException;

    void sendCustomAction(String str, Bundle bundle) throws RemoteException;

    boolean sendMediaButton(KeyEvent keyEvent) throws RemoteException;

    void setVolumeTo(int i, int i2, String str) throws RemoteException;

    void skipToQueueItem(long j) throws RemoteException;

    void stop() throws RemoteException;

    void unregisterCallbackListener(ISessionControllerCallback iSessionControllerCallback) throws RemoteException;
}
