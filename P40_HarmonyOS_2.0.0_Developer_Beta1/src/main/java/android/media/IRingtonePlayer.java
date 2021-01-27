package android.media;

import android.media.VolumeShaper;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.os.UserHandle;

public interface IRingtonePlayer extends IInterface {
    String getTitle(Uri uri) throws RemoteException;

    boolean isPlaying(IBinder iBinder) throws RemoteException;

    ParcelFileDescriptor openRingtone(Uri uri) throws RemoteException;

    void play(IBinder iBinder, Uri uri, AudioAttributes audioAttributes, float f, boolean z) throws RemoteException;

    void playAsync(Uri uri, UserHandle userHandle, boolean z, AudioAttributes audioAttributes) throws RemoteException;

    void playWithVolumeShaping(IBinder iBinder, Uri uri, AudioAttributes audioAttributes, float f, boolean z, VolumeShaper.Configuration configuration) throws RemoteException;

    void setPlaybackProperties(IBinder iBinder, float f, boolean z) throws RemoteException;

    void stop(IBinder iBinder) throws RemoteException;

    void stopAsync() throws RemoteException;

    public static class Default implements IRingtonePlayer {
        @Override // android.media.IRingtonePlayer
        public void play(IBinder token, Uri uri, AudioAttributes aa, float volume, boolean looping) throws RemoteException {
        }

        @Override // android.media.IRingtonePlayer
        public void playWithVolumeShaping(IBinder token, Uri uri, AudioAttributes aa, float volume, boolean looping, VolumeShaper.Configuration volumeShaperConfig) throws RemoteException {
        }

        @Override // android.media.IRingtonePlayer
        public void stop(IBinder token) throws RemoteException {
        }

        @Override // android.media.IRingtonePlayer
        public boolean isPlaying(IBinder token) throws RemoteException {
            return false;
        }

        @Override // android.media.IRingtonePlayer
        public void setPlaybackProperties(IBinder token, float volume, boolean looping) throws RemoteException {
        }

        @Override // android.media.IRingtonePlayer
        public void playAsync(Uri uri, UserHandle user, boolean looping, AudioAttributes aa) throws RemoteException {
        }

        @Override // android.media.IRingtonePlayer
        public void stopAsync() throws RemoteException {
        }

        @Override // android.media.IRingtonePlayer
        public String getTitle(Uri uri) throws RemoteException {
            return null;
        }

        @Override // android.media.IRingtonePlayer
        public ParcelFileDescriptor openRingtone(Uri uri) throws RemoteException {
            return null;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IRingtonePlayer {
        private static final String DESCRIPTOR = "android.media.IRingtonePlayer";
        static final int TRANSACTION_getTitle = 8;
        static final int TRANSACTION_isPlaying = 4;
        static final int TRANSACTION_openRingtone = 9;
        static final int TRANSACTION_play = 1;
        static final int TRANSACTION_playAsync = 6;
        static final int TRANSACTION_playWithVolumeShaping = 2;
        static final int TRANSACTION_setPlaybackProperties = 5;
        static final int TRANSACTION_stop = 3;
        static final int TRANSACTION_stopAsync = 7;

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

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "play";
                case 2:
                    return "playWithVolumeShaping";
                case 3:
                    return "stop";
                case 4:
                    return "isPlaying";
                case 5:
                    return "setPlaybackProperties";
                case 6:
                    return "playAsync";
                case 7:
                    return "stopAsync";
                case 8:
                    return "getTitle";
                case 9:
                    return "openRingtone";
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
            Uri _arg1;
            AudioAttributes _arg2;
            Uri _arg12;
            AudioAttributes _arg22;
            VolumeShaper.Configuration _arg5;
            Uri _arg0;
            UserHandle _arg13;
            AudioAttributes _arg3;
            Uri _arg02;
            Uri _arg03;
            if (code != 1598968902) {
                boolean _arg23 = false;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        IBinder _arg04 = data.readStrongBinder();
                        if (data.readInt() != 0) {
                            _arg1 = Uri.CREATOR.createFromParcel(data);
                        } else {
                            _arg1 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg2 = AudioAttributes.CREATOR.createFromParcel(data);
                        } else {
                            _arg2 = null;
                        }
                        play(_arg04, _arg1, _arg2, data.readFloat(), data.readInt() != 0);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        IBinder _arg05 = data.readStrongBinder();
                        if (data.readInt() != 0) {
                            _arg12 = Uri.CREATOR.createFromParcel(data);
                        } else {
                            _arg12 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg22 = AudioAttributes.CREATOR.createFromParcel(data);
                        } else {
                            _arg22 = null;
                        }
                        float _arg32 = data.readFloat();
                        boolean _arg4 = data.readInt() != 0;
                        if (data.readInt() != 0) {
                            _arg5 = VolumeShaper.Configuration.CREATOR.createFromParcel(data);
                        } else {
                            _arg5 = null;
                        }
                        playWithVolumeShaping(_arg05, _arg12, _arg22, _arg32, _arg4, _arg5);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        stop(data.readStrongBinder());
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isPlaying = isPlaying(data.readStrongBinder());
                        reply.writeNoException();
                        reply.writeInt(isPlaying ? 1 : 0);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        IBinder _arg06 = data.readStrongBinder();
                        float _arg14 = data.readFloat();
                        if (data.readInt() != 0) {
                            _arg23 = true;
                        }
                        setPlaybackProperties(_arg06, _arg14, _arg23);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = Uri.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg13 = UserHandle.CREATOR.createFromParcel(data);
                        } else {
                            _arg13 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg23 = true;
                        }
                        if (data.readInt() != 0) {
                            _arg3 = AudioAttributes.CREATOR.createFromParcel(data);
                        } else {
                            _arg3 = null;
                        }
                        playAsync(_arg0, _arg13, _arg23, _arg3);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        stopAsync();
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = Uri.CREATOR.createFromParcel(data);
                        } else {
                            _arg02 = null;
                        }
                        String _result = getTitle(_arg02);
                        reply.writeNoException();
                        reply.writeString(_result);
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg03 = Uri.CREATOR.createFromParcel(data);
                        } else {
                            _arg03 = null;
                        }
                        ParcelFileDescriptor _result2 = openRingtone(_arg03);
                        reply.writeNoException();
                        if (_result2 != null) {
                            reply.writeInt(1);
                            _result2.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
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
        public static class Proxy implements IRingtonePlayer {
            public static IRingtonePlayer sDefaultImpl;
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

            @Override // android.media.IRingtonePlayer
            public void play(IBinder token, Uri uri, AudioAttributes aa, float volume, boolean looping) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    int i = 0;
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
                    if (looping) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().play(token, uri, aa, volume, looping);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.media.IRingtonePlayer
            public void playWithVolumeShaping(IBinder token, Uri uri, AudioAttributes aa, float volume, boolean looping, VolumeShaper.Configuration volumeShaperConfig) throws RemoteException {
                Throwable th;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
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
                    } catch (Throwable th2) {
                        th = th2;
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeFloat(volume);
                        _data.writeInt(looping ? 1 : 0);
                        if (volumeShaperConfig != null) {
                            _data.writeInt(1);
                            volumeShaperConfig.writeToParcel(_data, 0);
                        } else {
                            _data.writeInt(0);
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        _data.recycle();
                        throw th;
                    }
                    try {
                        if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                            _data.recycle();
                            return;
                        }
                        Stub.getDefaultImpl().playWithVolumeShaping(token, uri, aa, volume, looping, volumeShaperConfig);
                        _data.recycle();
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
            }

            @Override // android.media.IRingtonePlayer
            public void stop(IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    if (this.mRemote.transact(3, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().stop(token);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.media.IRingtonePlayer
            public boolean isPlaying(IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    boolean _result = false;
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isPlaying(token);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.media.IRingtonePlayer
            public void setPlaybackProperties(IBinder token, float volume, boolean looping) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    _data.writeFloat(volume);
                    _data.writeInt(looping ? 1 : 0);
                    if (this.mRemote.transact(5, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().setPlaybackProperties(token, volume, looping);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.media.IRingtonePlayer
            public void playAsync(Uri uri, UserHandle user, boolean looping, AudioAttributes aa) throws RemoteException {
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
                    _data.writeInt(looping ? 1 : 0);
                    if (aa != null) {
                        _data.writeInt(1);
                        aa.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(6, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().playAsync(uri, user, looping, aa);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.media.IRingtonePlayer
            public void stopAsync() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(7, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().stopAsync();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.media.IRingtonePlayer
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
                    if (!this.mRemote.transact(8, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getTitle(uri);
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.media.IRingtonePlayer
            public ParcelFileDescriptor openRingtone(Uri uri) throws RemoteException {
                ParcelFileDescriptor _result;
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
                    if (!this.mRemote.transact(9, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().openRingtone(uri);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ParcelFileDescriptor.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IRingtonePlayer impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IRingtonePlayer getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
