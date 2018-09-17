package android.media.session;

import android.content.Intent;
import android.media.Rating;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ResultReceiver;

public interface ISessionCallback extends IInterface {

    public static abstract class Stub extends Binder implements ISessionCallback {
        private static final String DESCRIPTOR = "android.media.session.ISessionCallback";
        static final int TRANSACTION_onAdjustVolume = 21;
        static final int TRANSACTION_onCommand = 1;
        static final int TRANSACTION_onCustomAction = 20;
        static final int TRANSACTION_onFastForward = 16;
        static final int TRANSACTION_onMediaButton = 2;
        static final int TRANSACTION_onNext = 14;
        static final int TRANSACTION_onPause = 12;
        static final int TRANSACTION_onPlay = 7;
        static final int TRANSACTION_onPlayFromMediaId = 8;
        static final int TRANSACTION_onPlayFromSearch = 9;
        static final int TRANSACTION_onPlayFromUri = 10;
        static final int TRANSACTION_onPrepare = 3;
        static final int TRANSACTION_onPrepareFromMediaId = 4;
        static final int TRANSACTION_onPrepareFromSearch = 5;
        static final int TRANSACTION_onPrepareFromUri = 6;
        static final int TRANSACTION_onPrevious = 15;
        static final int TRANSACTION_onRate = 19;
        static final int TRANSACTION_onRewind = 17;
        static final int TRANSACTION_onSeekTo = 18;
        static final int TRANSACTION_onSetVolumeTo = 22;
        static final int TRANSACTION_onSkipToTrack = 11;
        static final int TRANSACTION_onStop = 13;

        private static class Proxy implements ISessionCallback {
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

            public void onCommand(String command, Bundle args, ResultReceiver cb) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(command);
                    if (args != null) {
                        _data.writeInt(Stub.TRANSACTION_onCommand);
                        args.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (cb != null) {
                        _data.writeInt(Stub.TRANSACTION_onCommand);
                        cb.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_onCommand, _data, null, Stub.TRANSACTION_onCommand);
                } finally {
                    _data.recycle();
                }
            }

            public void onMediaButton(Intent mediaButtonIntent, int sequenceNumber, ResultReceiver cb) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (mediaButtonIntent != null) {
                        _data.writeInt(Stub.TRANSACTION_onCommand);
                        mediaButtonIntent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(sequenceNumber);
                    if (cb != null) {
                        _data.writeInt(Stub.TRANSACTION_onCommand);
                        cb.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_onMediaButton, _data, null, Stub.TRANSACTION_onCommand);
                } finally {
                    _data.recycle();
                }
            }

            public void onPrepare() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_onPrepare, _data, null, Stub.TRANSACTION_onCommand);
                } finally {
                    _data.recycle();
                }
            }

            public void onPrepareFromMediaId(String mediaId, Bundle extras) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(mediaId);
                    if (extras != null) {
                        _data.writeInt(Stub.TRANSACTION_onCommand);
                        extras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_onPrepareFromMediaId, _data, null, Stub.TRANSACTION_onCommand);
                } finally {
                    _data.recycle();
                }
            }

            public void onPrepareFromSearch(String query, Bundle extras) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(query);
                    if (extras != null) {
                        _data.writeInt(Stub.TRANSACTION_onCommand);
                        extras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_onPrepareFromSearch, _data, null, Stub.TRANSACTION_onCommand);
                } finally {
                    _data.recycle();
                }
            }

            public void onPrepareFromUri(Uri uri, Bundle extras) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (uri != null) {
                        _data.writeInt(Stub.TRANSACTION_onCommand);
                        uri.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (extras != null) {
                        _data.writeInt(Stub.TRANSACTION_onCommand);
                        extras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_onPrepareFromUri, _data, null, Stub.TRANSACTION_onCommand);
                } finally {
                    _data.recycle();
                }
            }

            public void onPlay() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_onPlay, _data, null, Stub.TRANSACTION_onCommand);
                } finally {
                    _data.recycle();
                }
            }

            public void onPlayFromMediaId(String mediaId, Bundle extras) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(mediaId);
                    if (extras != null) {
                        _data.writeInt(Stub.TRANSACTION_onCommand);
                        extras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_onPlayFromMediaId, _data, null, Stub.TRANSACTION_onCommand);
                } finally {
                    _data.recycle();
                }
            }

            public void onPlayFromSearch(String query, Bundle extras) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(query);
                    if (extras != null) {
                        _data.writeInt(Stub.TRANSACTION_onCommand);
                        extras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_onPlayFromSearch, _data, null, Stub.TRANSACTION_onCommand);
                } finally {
                    _data.recycle();
                }
            }

            public void onPlayFromUri(Uri uri, Bundle extras) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (uri != null) {
                        _data.writeInt(Stub.TRANSACTION_onCommand);
                        uri.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (extras != null) {
                        _data.writeInt(Stub.TRANSACTION_onCommand);
                        extras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_onPlayFromUri, _data, null, Stub.TRANSACTION_onCommand);
                } finally {
                    _data.recycle();
                }
            }

            public void onSkipToTrack(long id) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(id);
                    this.mRemote.transact(Stub.TRANSACTION_onSkipToTrack, _data, null, Stub.TRANSACTION_onCommand);
                } finally {
                    _data.recycle();
                }
            }

            public void onPause() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_onPause, _data, null, Stub.TRANSACTION_onCommand);
                } finally {
                    _data.recycle();
                }
            }

            public void onStop() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_onStop, _data, null, Stub.TRANSACTION_onCommand);
                } finally {
                    _data.recycle();
                }
            }

            public void onNext() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_onNext, _data, null, Stub.TRANSACTION_onCommand);
                } finally {
                    _data.recycle();
                }
            }

            public void onPrevious() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_onPrevious, _data, null, Stub.TRANSACTION_onCommand);
                } finally {
                    _data.recycle();
                }
            }

            public void onFastForward() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_onFastForward, _data, null, Stub.TRANSACTION_onCommand);
                } finally {
                    _data.recycle();
                }
            }

            public void onRewind() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_onRewind, _data, null, Stub.TRANSACTION_onCommand);
                } finally {
                    _data.recycle();
                }
            }

            public void onSeekTo(long pos) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(pos);
                    this.mRemote.transact(Stub.TRANSACTION_onSeekTo, _data, null, Stub.TRANSACTION_onCommand);
                } finally {
                    _data.recycle();
                }
            }

            public void onRate(Rating rating) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (rating != null) {
                        _data.writeInt(Stub.TRANSACTION_onCommand);
                        rating.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_onRate, _data, null, Stub.TRANSACTION_onCommand);
                } finally {
                    _data.recycle();
                }
            }

            public void onCustomAction(String action, Bundle args) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(action);
                    if (args != null) {
                        _data.writeInt(Stub.TRANSACTION_onCommand);
                        args.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_onCustomAction, _data, null, Stub.TRANSACTION_onCommand);
                } finally {
                    _data.recycle();
                }
            }

            public void onAdjustVolume(int direction) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(direction);
                    this.mRemote.transact(Stub.TRANSACTION_onAdjustVolume, _data, null, Stub.TRANSACTION_onCommand);
                } finally {
                    _data.recycle();
                }
            }

            public void onSetVolumeTo(int value) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(value);
                    this.mRemote.transact(Stub.TRANSACTION_onSetVolumeTo, _data, null, Stub.TRANSACTION_onCommand);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ISessionCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ISessionCallback)) {
                return new Proxy(obj);
            }
            return (ISessionCallback) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            String _arg0;
            Bundle bundle;
            ResultReceiver resultReceiver;
            Uri uri;
            switch (code) {
                case TRANSACTION_onCommand /*1*/:
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
                    onCommand(_arg0, bundle, resultReceiver);
                    return true;
                case TRANSACTION_onMediaButton /*2*/:
                    Intent intent;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        intent = (Intent) Intent.CREATOR.createFromParcel(data);
                    } else {
                        intent = null;
                    }
                    int _arg1 = data.readInt();
                    if (data.readInt() != 0) {
                        resultReceiver = (ResultReceiver) ResultReceiver.CREATOR.createFromParcel(data);
                    } else {
                        resultReceiver = null;
                    }
                    onMediaButton(intent, _arg1, resultReceiver);
                    return true;
                case TRANSACTION_onPrepare /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    onPrepare();
                    return true;
                case TRANSACTION_onPrepareFromMediaId /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    if (data.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle = null;
                    }
                    onPrepareFromMediaId(_arg0, bundle);
                    return true;
                case TRANSACTION_onPrepareFromSearch /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    if (data.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle = null;
                    }
                    onPrepareFromSearch(_arg0, bundle);
                    return true;
                case TRANSACTION_onPrepareFromUri /*6*/:
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
                    onPrepareFromUri(uri, bundle);
                    return true;
                case TRANSACTION_onPlay /*7*/:
                    data.enforceInterface(DESCRIPTOR);
                    onPlay();
                    return true;
                case TRANSACTION_onPlayFromMediaId /*8*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    if (data.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle = null;
                    }
                    onPlayFromMediaId(_arg0, bundle);
                    return true;
                case TRANSACTION_onPlayFromSearch /*9*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    if (data.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle = null;
                    }
                    onPlayFromSearch(_arg0, bundle);
                    return true;
                case TRANSACTION_onPlayFromUri /*10*/:
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
                    onPlayFromUri(uri, bundle);
                    return true;
                case TRANSACTION_onSkipToTrack /*11*/:
                    data.enforceInterface(DESCRIPTOR);
                    onSkipToTrack(data.readLong());
                    return true;
                case TRANSACTION_onPause /*12*/:
                    data.enforceInterface(DESCRIPTOR);
                    onPause();
                    return true;
                case TRANSACTION_onStop /*13*/:
                    data.enforceInterface(DESCRIPTOR);
                    onStop();
                    return true;
                case TRANSACTION_onNext /*14*/:
                    data.enforceInterface(DESCRIPTOR);
                    onNext();
                    return true;
                case TRANSACTION_onPrevious /*15*/:
                    data.enforceInterface(DESCRIPTOR);
                    onPrevious();
                    return true;
                case TRANSACTION_onFastForward /*16*/:
                    data.enforceInterface(DESCRIPTOR);
                    onFastForward();
                    return true;
                case TRANSACTION_onRewind /*17*/:
                    data.enforceInterface(DESCRIPTOR);
                    onRewind();
                    return true;
                case TRANSACTION_onSeekTo /*18*/:
                    data.enforceInterface(DESCRIPTOR);
                    onSeekTo(data.readLong());
                    return true;
                case TRANSACTION_onRate /*19*/:
                    Rating rating;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        rating = (Rating) Rating.CREATOR.createFromParcel(data);
                    } else {
                        rating = null;
                    }
                    onRate(rating);
                    return true;
                case TRANSACTION_onCustomAction /*20*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    if (data.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle = null;
                    }
                    onCustomAction(_arg0, bundle);
                    return true;
                case TRANSACTION_onAdjustVolume /*21*/:
                    data.enforceInterface(DESCRIPTOR);
                    onAdjustVolume(data.readInt());
                    return true;
                case TRANSACTION_onSetVolumeTo /*22*/:
                    data.enforceInterface(DESCRIPTOR);
                    onSetVolumeTo(data.readInt());
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void onAdjustVolume(int i) throws RemoteException;

    void onCommand(String str, Bundle bundle, ResultReceiver resultReceiver) throws RemoteException;

    void onCustomAction(String str, Bundle bundle) throws RemoteException;

    void onFastForward() throws RemoteException;

    void onMediaButton(Intent intent, int i, ResultReceiver resultReceiver) throws RemoteException;

    void onNext() throws RemoteException;

    void onPause() throws RemoteException;

    void onPlay() throws RemoteException;

    void onPlayFromMediaId(String str, Bundle bundle) throws RemoteException;

    void onPlayFromSearch(String str, Bundle bundle) throws RemoteException;

    void onPlayFromUri(Uri uri, Bundle bundle) throws RemoteException;

    void onPrepare() throws RemoteException;

    void onPrepareFromMediaId(String str, Bundle bundle) throws RemoteException;

    void onPrepareFromSearch(String str, Bundle bundle) throws RemoteException;

    void onPrepareFromUri(Uri uri, Bundle bundle) throws RemoteException;

    void onPrevious() throws RemoteException;

    void onRate(Rating rating) throws RemoteException;

    void onRewind() throws RemoteException;

    void onSeekTo(long j) throws RemoteException;

    void onSetVolumeTo(int i) throws RemoteException;

    void onSkipToTrack(long j) throws RemoteException;

    void onStop() throws RemoteException;
}
