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
                int i = 1;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    if (uri != null) {
                        _data.writeInt(1);
                        uri.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (aa != null) {
                        _data.writeInt(1);
                        aa.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeFloat(volume);
                    if (!looping) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void stop(IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    this.mRemote.transact(2, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public boolean isPlaying(IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    this.mRemote.transact(3, _data, _reply, 0);
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
                int i = 1;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    _data.writeFloat(volume);
                    if (!looping) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(4, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void playAsync(Uri uri, UserHandle user, boolean looping, AudioAttributes aa) throws RemoteException {
                int i = 1;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (uri != null) {
                        _data.writeInt(1);
                        uri.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (user != null) {
                        _data.writeInt(1);
                        user.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!looping) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    if (aa != null) {
                        _data.writeInt(1);
                        aa.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(5, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void stopAsync() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(6, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public String getTitle(Uri uri) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (uri != null) {
                        _data.writeInt(1);
                        uri.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(7, _data, _reply, 0);
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
                    ParcelFileDescriptor _result;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (uri != null) {
                        _data.writeInt(1);
                        uri.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (ParcelFileDescriptor) ParcelFileDescriptor.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
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
            Uri _arg0;
            switch (code) {
                case 1:
                    Uri _arg1;
                    AudioAttributes _arg2;
                    data.enforceInterface(DESCRIPTOR);
                    IBinder _arg02 = data.readStrongBinder();
                    if (data.readInt() != 0) {
                        _arg1 = (Uri) Uri.CREATOR.createFromParcel(data);
                    } else {
                        _arg1 = null;
                    }
                    if (data.readInt() != 0) {
                        _arg2 = (AudioAttributes) AudioAttributes.CREATOR.createFromParcel(data);
                    } else {
                        _arg2 = null;
                    }
                    play(_arg02, _arg1, _arg2, data.readFloat(), data.readInt() != 0);
                    return true;
                case 2:
                    data.enforceInterface(DESCRIPTOR);
                    stop(data.readStrongBinder());
                    return true;
                case 3:
                    data.enforceInterface(DESCRIPTOR);
                    boolean _result = isPlaying(data.readStrongBinder());
                    reply.writeNoException();
                    reply.writeInt(_result ? 1 : 0);
                    return true;
                case 4:
                    data.enforceInterface(DESCRIPTOR);
                    setPlaybackProperties(data.readStrongBinder(), data.readFloat(), data.readInt() != 0);
                    return true;
                case 5:
                    UserHandle _arg12;
                    AudioAttributes _arg3;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg0 = (Uri) Uri.CREATOR.createFromParcel(data);
                    } else {
                        _arg0 = null;
                    }
                    if (data.readInt() != 0) {
                        _arg12 = (UserHandle) UserHandle.CREATOR.createFromParcel(data);
                    } else {
                        _arg12 = null;
                    }
                    boolean _arg22 = data.readInt() != 0;
                    if (data.readInt() != 0) {
                        _arg3 = (AudioAttributes) AudioAttributes.CREATOR.createFromParcel(data);
                    } else {
                        _arg3 = null;
                    }
                    playAsync(_arg0, _arg12, _arg22, _arg3);
                    return true;
                case 6:
                    data.enforceInterface(DESCRIPTOR);
                    stopAsync();
                    return true;
                case 7:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg0 = (Uri) Uri.CREATOR.createFromParcel(data);
                    } else {
                        _arg0 = null;
                    }
                    String _result2 = getTitle(_arg0);
                    reply.writeNoException();
                    reply.writeString(_result2);
                    return true;
                case 8:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg0 = (Uri) Uri.CREATOR.createFromParcel(data);
                    } else {
                        _arg0 = null;
                    }
                    ParcelFileDescriptor _result3 = openRingtone(_arg0);
                    reply.writeNoException();
                    if (_result3 != null) {
                        reply.writeInt(1);
                        _result3.writeToParcel(reply, 1);
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
