package android.media;

import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.os.UserHandle;

public interface IRingtonePlayer extends IInterface {

    public static abstract class Stub extends Binder implements IRingtonePlayer {
        private static final String DESCRIPTOR = "android.media.IRingtonePlayer";
        static final int TRANSACTION_getTitle = 7;
        static final int TRANSACTION_isPlaying = 3;
        static final int TRANSACTION_openRingtone = 8;
        static final int TRANSACTION_play = 1;
        static final int TRANSACTION_playAsync = 5;
        static final int TRANSACTION_setPlaybackProperties = 4;
        static final int TRANSACTION_stop = 2;
        static final int TRANSACTION_stopAsync = 6;

        private static class Proxy implements IRingtonePlayer {
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

            public void play(IBinder token, Uri uri, AudioAttributes aa, float volume, boolean looping) throws RemoteException {
                int i = Stub.TRANSACTION_play;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    if (uri != null) {
                        _data.writeInt(Stub.TRANSACTION_play);
                        uri.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (aa != null) {
                        _data.writeInt(Stub.TRANSACTION_play);
                        aa.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeFloat(volume);
                    if (!looping) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_play, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void stop(IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    this.mRemote.transact(Stub.TRANSACTION_stop, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isPlaying(IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    this.mRemote.transact(Stub.TRANSACTION_isPlaying, _data, _reply, 0);
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

            public void setPlaybackProperties(IBinder token, float volume, boolean looping) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    _data.writeFloat(volume);
                    if (looping) {
                        i = Stub.TRANSACTION_play;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_setPlaybackProperties, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void playAsync(Uri uri, UserHandle user, boolean looping, AudioAttributes aa) throws RemoteException {
                int i = Stub.TRANSACTION_play;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (uri != null) {
                        _data.writeInt(Stub.TRANSACTION_play);
                        uri.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (user != null) {
                        _data.writeInt(Stub.TRANSACTION_play);
                        user.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!looping) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    if (aa != null) {
                        _data.writeInt(Stub.TRANSACTION_play);
                        aa.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_playAsync, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void stopAsync() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_stopAsync, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getTitle(Uri uri) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (uri != null) {
                        _data.writeInt(Stub.TRANSACTION_play);
                        uri.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_getTitle, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ParcelFileDescriptor openRingtone(Uri uri) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    ParcelFileDescriptor parcelFileDescriptor;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (uri != null) {
                        _data.writeInt(Stub.TRANSACTION_play);
                        uri.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_openRingtone, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        parcelFileDescriptor = (ParcelFileDescriptor) ParcelFileDescriptor.CREATOR.createFromParcel(_reply);
                    } else {
                        parcelFileDescriptor = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return parcelFileDescriptor;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IRingtonePlayer asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IRingtonePlayer)) {
                return new Proxy(obj);
            }
            return (IRingtonePlayer) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            Uri uri;
            switch (code) {
                case TRANSACTION_play /*1*/:
                    Uri uri2;
                    AudioAttributes audioAttributes;
                    data.enforceInterface(DESCRIPTOR);
                    IBinder _arg0 = data.readStrongBinder();
                    if (data.readInt() != 0) {
                        uri2 = (Uri) Uri.CREATOR.createFromParcel(data);
                    } else {
                        uri2 = null;
                    }
                    if (data.readInt() != 0) {
                        audioAttributes = (AudioAttributes) AudioAttributes.CREATOR.createFromParcel(data);
                    } else {
                        audioAttributes = null;
                    }
                    play(_arg0, uri2, audioAttributes, data.readFloat(), data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_stop /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    stop(data.readStrongBinder());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_isPlaying /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    boolean _result = isPlaying(data.readStrongBinder());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_play : 0);
                    return true;
                case TRANSACTION_setPlaybackProperties /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    setPlaybackProperties(data.readStrongBinder(), data.readFloat(), data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_playAsync /*5*/:
                    UserHandle userHandle;
                    AudioAttributes audioAttributes2;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        uri = (Uri) Uri.CREATOR.createFromParcel(data);
                    } else {
                        uri = null;
                    }
                    if (data.readInt() != 0) {
                        userHandle = (UserHandle) UserHandle.CREATOR.createFromParcel(data);
                    } else {
                        userHandle = null;
                    }
                    boolean _arg2 = data.readInt() != 0;
                    if (data.readInt() != 0) {
                        audioAttributes2 = (AudioAttributes) AudioAttributes.CREATOR.createFromParcel(data);
                    } else {
                        audioAttributes2 = null;
                    }
                    playAsync(uri, userHandle, _arg2, audioAttributes2);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_stopAsync /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    stopAsync();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getTitle /*7*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        uri = (Uri) Uri.CREATOR.createFromParcel(data);
                    } else {
                        uri = null;
                    }
                    String _result2 = getTitle(uri);
                    reply.writeNoException();
                    reply.writeString(_result2);
                    return true;
                case TRANSACTION_openRingtone /*8*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        uri = (Uri) Uri.CREATOR.createFromParcel(data);
                    } else {
                        uri = null;
                    }
                    ParcelFileDescriptor _result3 = openRingtone(uri);
                    reply.writeNoException();
                    if (_result3 != null) {
                        reply.writeInt(TRANSACTION_play);
                        _result3.writeToParcel(reply, TRANSACTION_play);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    String getTitle(Uri uri) throws RemoteException;

    boolean isPlaying(IBinder iBinder) throws RemoteException;

    ParcelFileDescriptor openRingtone(Uri uri) throws RemoteException;

    void play(IBinder iBinder, Uri uri, AudioAttributes audioAttributes, float f, boolean z) throws RemoteException;

    void playAsync(Uri uri, UserHandle userHandle, boolean z, AudioAttributes audioAttributes) throws RemoteException;

    void setPlaybackProperties(IBinder iBinder, float f, boolean z) throws RemoteException;

    void stop(IBinder iBinder) throws RemoteException;

    void stopAsync() throws RemoteException;
}
