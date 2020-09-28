package android.media.session;

import android.content.Intent;
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

public interface ISessionCallback extends IInterface {
    void onAdjustVolume(String str, int i, int i2, ISessionControllerCallback iSessionControllerCallback, int i3) throws RemoteException;

    void onCommand(String str, int i, int i2, ISessionControllerCallback iSessionControllerCallback, String str2, Bundle bundle, ResultReceiver resultReceiver) throws RemoteException;

    void onCustomAction(String str, int i, int i2, ISessionControllerCallback iSessionControllerCallback, String str2, Bundle bundle) throws RemoteException;

    void onFastForward(String str, int i, int i2, ISessionControllerCallback iSessionControllerCallback) throws RemoteException;

    void onMediaButton(String str, int i, int i2, Intent intent, int i3, ResultReceiver resultReceiver) throws RemoteException;

    void onMediaButtonFromController(String str, int i, int i2, ISessionControllerCallback iSessionControllerCallback, Intent intent) throws RemoteException;

    void onNext(String str, int i, int i2, ISessionControllerCallback iSessionControllerCallback) throws RemoteException;

    void onPause(String str, int i, int i2, ISessionControllerCallback iSessionControllerCallback) throws RemoteException;

    void onPlay(String str, int i, int i2, ISessionControllerCallback iSessionControllerCallback) throws RemoteException;

    void onPlayFromMediaId(String str, int i, int i2, ISessionControllerCallback iSessionControllerCallback, String str2, Bundle bundle) throws RemoteException;

    void onPlayFromSearch(String str, int i, int i2, ISessionControllerCallback iSessionControllerCallback, String str2, Bundle bundle) throws RemoteException;

    void onPlayFromUri(String str, int i, int i2, ISessionControllerCallback iSessionControllerCallback, Uri uri, Bundle bundle) throws RemoteException;

    void onPrepare(String str, int i, int i2, ISessionControllerCallback iSessionControllerCallback) throws RemoteException;

    void onPrepareFromMediaId(String str, int i, int i2, ISessionControllerCallback iSessionControllerCallback, String str2, Bundle bundle) throws RemoteException;

    void onPrepareFromSearch(String str, int i, int i2, ISessionControllerCallback iSessionControllerCallback, String str2, Bundle bundle) throws RemoteException;

    void onPrepareFromUri(String str, int i, int i2, ISessionControllerCallback iSessionControllerCallback, Uri uri, Bundle bundle) throws RemoteException;

    void onPrevious(String str, int i, int i2, ISessionControllerCallback iSessionControllerCallback) throws RemoteException;

    void onRate(String str, int i, int i2, ISessionControllerCallback iSessionControllerCallback, Rating rating) throws RemoteException;

    void onRewind(String str, int i, int i2, ISessionControllerCallback iSessionControllerCallback) throws RemoteException;

    void onSeekTo(String str, int i, int i2, ISessionControllerCallback iSessionControllerCallback, long j) throws RemoteException;

    void onSetPlaybackSpeed(String str, int i, int i2, ISessionControllerCallback iSessionControllerCallback, float f) throws RemoteException;

    void onSetVolumeTo(String str, int i, int i2, ISessionControllerCallback iSessionControllerCallback, int i3) throws RemoteException;

    void onSkipToTrack(String str, int i, int i2, ISessionControllerCallback iSessionControllerCallback, long j) throws RemoteException;

    void onStop(String str, int i, int i2, ISessionControllerCallback iSessionControllerCallback) throws RemoteException;

    public static class Default implements ISessionCallback {
        @Override // android.media.session.ISessionCallback
        public void onCommand(String packageName, int pid, int uid, ISessionControllerCallback caller, String command, Bundle args, ResultReceiver cb) throws RemoteException {
        }

        @Override // android.media.session.ISessionCallback
        public void onMediaButton(String packageName, int pid, int uid, Intent mediaButtonIntent, int sequenceNumber, ResultReceiver cb) throws RemoteException {
        }

        @Override // android.media.session.ISessionCallback
        public void onMediaButtonFromController(String packageName, int pid, int uid, ISessionControllerCallback caller, Intent mediaButtonIntent) throws RemoteException {
        }

        @Override // android.media.session.ISessionCallback
        public void onPrepare(String packageName, int pid, int uid, ISessionControllerCallback caller) throws RemoteException {
        }

        @Override // android.media.session.ISessionCallback
        public void onPrepareFromMediaId(String packageName, int pid, int uid, ISessionControllerCallback caller, String mediaId, Bundle extras) throws RemoteException {
        }

        @Override // android.media.session.ISessionCallback
        public void onPrepareFromSearch(String packageName, int pid, int uid, ISessionControllerCallback caller, String query, Bundle extras) throws RemoteException {
        }

        @Override // android.media.session.ISessionCallback
        public void onPrepareFromUri(String packageName, int pid, int uid, ISessionControllerCallback caller, Uri uri, Bundle extras) throws RemoteException {
        }

        @Override // android.media.session.ISessionCallback
        public void onPlay(String packageName, int pid, int uid, ISessionControllerCallback caller) throws RemoteException {
        }

        @Override // android.media.session.ISessionCallback
        public void onPlayFromMediaId(String packageName, int pid, int uid, ISessionControllerCallback caller, String mediaId, Bundle extras) throws RemoteException {
        }

        @Override // android.media.session.ISessionCallback
        public void onPlayFromSearch(String packageName, int pid, int uid, ISessionControllerCallback caller, String query, Bundle extras) throws RemoteException {
        }

        @Override // android.media.session.ISessionCallback
        public void onPlayFromUri(String packageName, int pid, int uid, ISessionControllerCallback caller, Uri uri, Bundle extras) throws RemoteException {
        }

        @Override // android.media.session.ISessionCallback
        public void onSkipToTrack(String packageName, int pid, int uid, ISessionControllerCallback caller, long id) throws RemoteException {
        }

        @Override // android.media.session.ISessionCallback
        public void onPause(String packageName, int pid, int uid, ISessionControllerCallback caller) throws RemoteException {
        }

        @Override // android.media.session.ISessionCallback
        public void onStop(String packageName, int pid, int uid, ISessionControllerCallback caller) throws RemoteException {
        }

        @Override // android.media.session.ISessionCallback
        public void onNext(String packageName, int pid, int uid, ISessionControllerCallback caller) throws RemoteException {
        }

        @Override // android.media.session.ISessionCallback
        public void onPrevious(String packageName, int pid, int uid, ISessionControllerCallback caller) throws RemoteException {
        }

        @Override // android.media.session.ISessionCallback
        public void onFastForward(String packageName, int pid, int uid, ISessionControllerCallback caller) throws RemoteException {
        }

        @Override // android.media.session.ISessionCallback
        public void onRewind(String packageName, int pid, int uid, ISessionControllerCallback caller) throws RemoteException {
        }

        @Override // android.media.session.ISessionCallback
        public void onSeekTo(String packageName, int pid, int uid, ISessionControllerCallback caller, long pos) throws RemoteException {
        }

        @Override // android.media.session.ISessionCallback
        public void onRate(String packageName, int pid, int uid, ISessionControllerCallback caller, Rating rating) throws RemoteException {
        }

        @Override // android.media.session.ISessionCallback
        public void onSetPlaybackSpeed(String packageName, int pid, int uid, ISessionControllerCallback caller, float speed) throws RemoteException {
        }

        @Override // android.media.session.ISessionCallback
        public void onCustomAction(String packageName, int pid, int uid, ISessionControllerCallback caller, String action, Bundle args) throws RemoteException {
        }

        @Override // android.media.session.ISessionCallback
        public void onAdjustVolume(String packageName, int pid, int uid, ISessionControllerCallback caller, int direction) throws RemoteException {
        }

        @Override // android.media.session.ISessionCallback
        public void onSetVolumeTo(String packageName, int pid, int uid, ISessionControllerCallback caller, int value) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements ISessionCallback {
        private static final String DESCRIPTOR = "android.media.session.ISessionCallback";
        static final int TRANSACTION_onAdjustVolume = 23;
        static final int TRANSACTION_onCommand = 1;
        static final int TRANSACTION_onCustomAction = 22;
        static final int TRANSACTION_onFastForward = 17;
        static final int TRANSACTION_onMediaButton = 2;
        static final int TRANSACTION_onMediaButtonFromController = 3;
        static final int TRANSACTION_onNext = 15;
        static final int TRANSACTION_onPause = 13;
        static final int TRANSACTION_onPlay = 8;
        static final int TRANSACTION_onPlayFromMediaId = 9;
        static final int TRANSACTION_onPlayFromSearch = 10;
        static final int TRANSACTION_onPlayFromUri = 11;
        static final int TRANSACTION_onPrepare = 4;
        static final int TRANSACTION_onPrepareFromMediaId = 5;
        static final int TRANSACTION_onPrepareFromSearch = 6;
        static final int TRANSACTION_onPrepareFromUri = 7;
        static final int TRANSACTION_onPrevious = 16;
        static final int TRANSACTION_onRate = 20;
        static final int TRANSACTION_onRewind = 18;
        static final int TRANSACTION_onSeekTo = 19;
        static final int TRANSACTION_onSetPlaybackSpeed = 21;
        static final int TRANSACTION_onSetVolumeTo = 24;
        static final int TRANSACTION_onSkipToTrack = 12;
        static final int TRANSACTION_onStop = 14;

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

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "onCommand";
                case 2:
                    return "onMediaButton";
                case 3:
                    return "onMediaButtonFromController";
                case 4:
                    return "onPrepare";
                case 5:
                    return "onPrepareFromMediaId";
                case 6:
                    return "onPrepareFromSearch";
                case 7:
                    return "onPrepareFromUri";
                case 8:
                    return "onPlay";
                case 9:
                    return "onPlayFromMediaId";
                case 10:
                    return "onPlayFromSearch";
                case 11:
                    return "onPlayFromUri";
                case 12:
                    return "onSkipToTrack";
                case 13:
                    return "onPause";
                case 14:
                    return "onStop";
                case 15:
                    return "onNext";
                case 16:
                    return "onPrevious";
                case 17:
                    return "onFastForward";
                case 18:
                    return "onRewind";
                case 19:
                    return "onSeekTo";
                case 20:
                    return "onRate";
                case 21:
                    return "onSetPlaybackSpeed";
                case 22:
                    return "onCustomAction";
                case 23:
                    return "onAdjustVolume";
                case 24:
                    return "onSetVolumeTo";
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
            Bundle _arg5;
            ResultReceiver _arg6;
            Intent _arg3;
            ResultReceiver _arg52;
            Intent _arg4;
            Bundle _arg53;
            Bundle _arg54;
            Uri _arg42;
            Bundle _arg55;
            Bundle _arg56;
            Bundle _arg57;
            Uri _arg43;
            Bundle _arg58;
            Rating _arg44;
            Bundle _arg59;
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg0 = data.readString();
                        int _arg1 = data.readInt();
                        int _arg2 = data.readInt();
                        ISessionControllerCallback _arg32 = ISessionControllerCallback.Stub.asInterface(data.readStrongBinder());
                        String _arg45 = data.readString();
                        if (data.readInt() != 0) {
                            _arg5 = Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg5 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg6 = ResultReceiver.CREATOR.createFromParcel(data);
                        } else {
                            _arg6 = null;
                        }
                        onCommand(_arg0, _arg1, _arg2, _arg32, _arg45, _arg5, _arg6);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg02 = data.readString();
                        int _arg12 = data.readInt();
                        int _arg22 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg3 = Intent.CREATOR.createFromParcel(data);
                        } else {
                            _arg3 = null;
                        }
                        int _arg46 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg52 = ResultReceiver.CREATOR.createFromParcel(data);
                        } else {
                            _arg52 = null;
                        }
                        onMediaButton(_arg02, _arg12, _arg22, _arg3, _arg46, _arg52);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg03 = data.readString();
                        int _arg13 = data.readInt();
                        int _arg23 = data.readInt();
                        ISessionControllerCallback _arg33 = ISessionControllerCallback.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg4 = Intent.CREATOR.createFromParcel(data);
                        } else {
                            _arg4 = null;
                        }
                        onMediaButtonFromController(_arg03, _arg13, _arg23, _arg33, _arg4);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        onPrepare(data.readString(), data.readInt(), data.readInt(), ISessionControllerCallback.Stub.asInterface(data.readStrongBinder()));
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg04 = data.readString();
                        int _arg14 = data.readInt();
                        int _arg24 = data.readInt();
                        ISessionControllerCallback _arg34 = ISessionControllerCallback.Stub.asInterface(data.readStrongBinder());
                        String _arg47 = data.readString();
                        if (data.readInt() != 0) {
                            _arg53 = Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg53 = null;
                        }
                        onPrepareFromMediaId(_arg04, _arg14, _arg24, _arg34, _arg47, _arg53);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg05 = data.readString();
                        int _arg15 = data.readInt();
                        int _arg25 = data.readInt();
                        ISessionControllerCallback _arg35 = ISessionControllerCallback.Stub.asInterface(data.readStrongBinder());
                        String _arg48 = data.readString();
                        if (data.readInt() != 0) {
                            _arg54 = Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg54 = null;
                        }
                        onPrepareFromSearch(_arg05, _arg15, _arg25, _arg35, _arg48, _arg54);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg06 = data.readString();
                        int _arg16 = data.readInt();
                        int _arg26 = data.readInt();
                        ISessionControllerCallback _arg36 = ISessionControllerCallback.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg42 = Uri.CREATOR.createFromParcel(data);
                        } else {
                            _arg42 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg55 = Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg55 = null;
                        }
                        onPrepareFromUri(_arg06, _arg16, _arg26, _arg36, _arg42, _arg55);
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        onPlay(data.readString(), data.readInt(), data.readInt(), ISessionControllerCallback.Stub.asInterface(data.readStrongBinder()));
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg07 = data.readString();
                        int _arg17 = data.readInt();
                        int _arg27 = data.readInt();
                        ISessionControllerCallback _arg37 = ISessionControllerCallback.Stub.asInterface(data.readStrongBinder());
                        String _arg49 = data.readString();
                        if (data.readInt() != 0) {
                            _arg56 = Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg56 = null;
                        }
                        onPlayFromMediaId(_arg07, _arg17, _arg27, _arg37, _arg49, _arg56);
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg08 = data.readString();
                        int _arg18 = data.readInt();
                        int _arg28 = data.readInt();
                        ISessionControllerCallback _arg38 = ISessionControllerCallback.Stub.asInterface(data.readStrongBinder());
                        String _arg410 = data.readString();
                        if (data.readInt() != 0) {
                            _arg57 = Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg57 = null;
                        }
                        onPlayFromSearch(_arg08, _arg18, _arg28, _arg38, _arg410, _arg57);
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg09 = data.readString();
                        int _arg19 = data.readInt();
                        int _arg29 = data.readInt();
                        ISessionControllerCallback _arg39 = ISessionControllerCallback.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg43 = Uri.CREATOR.createFromParcel(data);
                        } else {
                            _arg43 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg58 = Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg58 = null;
                        }
                        onPlayFromUri(_arg09, _arg19, _arg29, _arg39, _arg43, _arg58);
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        onSkipToTrack(data.readString(), data.readInt(), data.readInt(), ISessionControllerCallback.Stub.asInterface(data.readStrongBinder()), data.readLong());
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        onPause(data.readString(), data.readInt(), data.readInt(), ISessionControllerCallback.Stub.asInterface(data.readStrongBinder()));
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        onStop(data.readString(), data.readInt(), data.readInt(), ISessionControllerCallback.Stub.asInterface(data.readStrongBinder()));
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        onNext(data.readString(), data.readInt(), data.readInt(), ISessionControllerCallback.Stub.asInterface(data.readStrongBinder()));
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        onPrevious(data.readString(), data.readInt(), data.readInt(), ISessionControllerCallback.Stub.asInterface(data.readStrongBinder()));
                        return true;
                    case 17:
                        data.enforceInterface(DESCRIPTOR);
                        onFastForward(data.readString(), data.readInt(), data.readInt(), ISessionControllerCallback.Stub.asInterface(data.readStrongBinder()));
                        return true;
                    case 18:
                        data.enforceInterface(DESCRIPTOR);
                        onRewind(data.readString(), data.readInt(), data.readInt(), ISessionControllerCallback.Stub.asInterface(data.readStrongBinder()));
                        return true;
                    case 19:
                        data.enforceInterface(DESCRIPTOR);
                        onSeekTo(data.readString(), data.readInt(), data.readInt(), ISessionControllerCallback.Stub.asInterface(data.readStrongBinder()), data.readLong());
                        return true;
                    case 20:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg010 = data.readString();
                        int _arg110 = data.readInt();
                        int _arg210 = data.readInt();
                        ISessionControllerCallback _arg310 = ISessionControllerCallback.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg44 = Rating.CREATOR.createFromParcel(data);
                        } else {
                            _arg44 = null;
                        }
                        onRate(_arg010, _arg110, _arg210, _arg310, _arg44);
                        return true;
                    case 21:
                        data.enforceInterface(DESCRIPTOR);
                        onSetPlaybackSpeed(data.readString(), data.readInt(), data.readInt(), ISessionControllerCallback.Stub.asInterface(data.readStrongBinder()), data.readFloat());
                        return true;
                    case 22:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg011 = data.readString();
                        int _arg111 = data.readInt();
                        int _arg211 = data.readInt();
                        ISessionControllerCallback _arg311 = ISessionControllerCallback.Stub.asInterface(data.readStrongBinder());
                        String _arg411 = data.readString();
                        if (data.readInt() != 0) {
                            _arg59 = Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg59 = null;
                        }
                        onCustomAction(_arg011, _arg111, _arg211, _arg311, _arg411, _arg59);
                        return true;
                    case 23:
                        data.enforceInterface(DESCRIPTOR);
                        onAdjustVolume(data.readString(), data.readInt(), data.readInt(), ISessionControllerCallback.Stub.asInterface(data.readStrongBinder()), data.readInt());
                        return true;
                    case 24:
                        data.enforceInterface(DESCRIPTOR);
                        onSetVolumeTo(data.readString(), data.readInt(), data.readInt(), ISessionControllerCallback.Stub.asInterface(data.readStrongBinder()), data.readInt());
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
        public static class Proxy implements ISessionCallback {
            public static ISessionCallback sDefaultImpl;
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

            @Override // android.media.session.ISessionCallback
            public void onCommand(String packageName, int pid, int uid, ISessionControllerCallback caller, String command, Bundle args, ResultReceiver cb) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeString(packageName);
                    } catch (Throwable th) {
                        th = th;
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(pid);
                    } catch (Throwable th2) {
                        th = th2;
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(uid);
                        _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                        try {
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
                            if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                                _data.recycle();
                                return;
                            }
                            Stub.getDefaultImpl().onCommand(packageName, pid, uid, caller, command, args, cb);
                            _data.recycle();
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
            }

            @Override // android.media.session.ISessionCallback
            public void onMediaButton(String packageName, int pid, int uid, Intent mediaButtonIntent, int sequenceNumber, ResultReceiver cb) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeString(packageName);
                    } catch (Throwable th) {
                        th = th;
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(pid);
                    } catch (Throwable th2) {
                        th = th2;
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(uid);
                        if (mediaButtonIntent != null) {
                            _data.writeInt(1);
                            mediaButtonIntent.writeToParcel(_data, 0);
                        } else {
                            _data.writeInt(0);
                        }
                        try {
                            _data.writeInt(sequenceNumber);
                            if (cb != null) {
                                _data.writeInt(1);
                                cb.writeToParcel(_data, 0);
                            } else {
                                _data.writeInt(0);
                            }
                            try {
                                if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                                    _data.recycle();
                                    return;
                                }
                                Stub.getDefaultImpl().onMediaButton(packageName, pid, uid, mediaButtonIntent, sequenceNumber, cb);
                                _data.recycle();
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
                } catch (Throwable th6) {
                    th = th6;
                    _data.recycle();
                    throw th;
                }
            }

            @Override // android.media.session.ISessionCallback
            public void onMediaButtonFromController(String packageName, int pid, int uid, ISessionControllerCallback caller, Intent mediaButtonIntent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(pid);
                    _data.writeInt(uid);
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    if (mediaButtonIntent != null) {
                        _data.writeInt(1);
                        mediaButtonIntent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(3, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onMediaButtonFromController(packageName, pid, uid, caller, mediaButtonIntent);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.media.session.ISessionCallback
            public void onPrepare(String packageName, int pid, int uid, ISessionControllerCallback caller) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(pid);
                    _data.writeInt(uid);
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    if (this.mRemote.transact(4, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onPrepare(packageName, pid, uid, caller);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.media.session.ISessionCallback
            public void onPrepareFromMediaId(String packageName, int pid, int uid, ISessionControllerCallback caller, String mediaId, Bundle extras) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeString(packageName);
                    } catch (Throwable th) {
                        th = th;
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(pid);
                        try {
                            _data.writeInt(uid);
                            _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                        } catch (Throwable th2) {
                            th = th2;
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeString(mediaId);
                        if (extras != null) {
                            _data.writeInt(1);
                            extras.writeToParcel(_data, 0);
                        } else {
                            _data.writeInt(0);
                        }
                        try {
                            if (this.mRemote.transact(5, _data, null, 1) || Stub.getDefaultImpl() == null) {
                                _data.recycle();
                                return;
                            }
                            Stub.getDefaultImpl().onPrepareFromMediaId(packageName, pid, uid, caller, mediaId, extras);
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
                } catch (Throwable th6) {
                    th = th6;
                    _data.recycle();
                    throw th;
                }
            }

            @Override // android.media.session.ISessionCallback
            public void onPrepareFromSearch(String packageName, int pid, int uid, ISessionControllerCallback caller, String query, Bundle extras) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeString(packageName);
                    } catch (Throwable th) {
                        th = th;
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(pid);
                        try {
                            _data.writeInt(uid);
                            _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                        } catch (Throwable th2) {
                            th = th2;
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeString(query);
                        if (extras != null) {
                            _data.writeInt(1);
                            extras.writeToParcel(_data, 0);
                        } else {
                            _data.writeInt(0);
                        }
                        try {
                            if (this.mRemote.transact(6, _data, null, 1) || Stub.getDefaultImpl() == null) {
                                _data.recycle();
                                return;
                            }
                            Stub.getDefaultImpl().onPrepareFromSearch(packageName, pid, uid, caller, query, extras);
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
                } catch (Throwable th6) {
                    th = th6;
                    _data.recycle();
                    throw th;
                }
            }

            @Override // android.media.session.ISessionCallback
            public void onPrepareFromUri(String packageName, int pid, int uid, ISessionControllerCallback caller, Uri uri, Bundle extras) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeString(packageName);
                    } catch (Throwable th) {
                        th = th;
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(pid);
                    } catch (Throwable th2) {
                        th = th2;
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(uid);
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
                        try {
                            if (this.mRemote.transact(7, _data, null, 1) || Stub.getDefaultImpl() == null) {
                                _data.recycle();
                                return;
                            }
                            Stub.getDefaultImpl().onPrepareFromUri(packageName, pid, uid, caller, uri, extras);
                            _data.recycle();
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
            }

            @Override // android.media.session.ISessionCallback
            public void onPlay(String packageName, int pid, int uid, ISessionControllerCallback caller) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(pid);
                    _data.writeInt(uid);
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    if (this.mRemote.transact(8, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onPlay(packageName, pid, uid, caller);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.media.session.ISessionCallback
            public void onPlayFromMediaId(String packageName, int pid, int uid, ISessionControllerCallback caller, String mediaId, Bundle extras) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeString(packageName);
                    } catch (Throwable th) {
                        th = th;
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(pid);
                        try {
                            _data.writeInt(uid);
                            _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                        } catch (Throwable th2) {
                            th = th2;
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeString(mediaId);
                        if (extras != null) {
                            _data.writeInt(1);
                            extras.writeToParcel(_data, 0);
                        } else {
                            _data.writeInt(0);
                        }
                        try {
                            if (this.mRemote.transact(9, _data, null, 1) || Stub.getDefaultImpl() == null) {
                                _data.recycle();
                                return;
                            }
                            Stub.getDefaultImpl().onPlayFromMediaId(packageName, pid, uid, caller, mediaId, extras);
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
                } catch (Throwable th6) {
                    th = th6;
                    _data.recycle();
                    throw th;
                }
            }

            @Override // android.media.session.ISessionCallback
            public void onPlayFromSearch(String packageName, int pid, int uid, ISessionControllerCallback caller, String query, Bundle extras) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeString(packageName);
                    } catch (Throwable th) {
                        th = th;
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(pid);
                        try {
                            _data.writeInt(uid);
                            _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                        } catch (Throwable th2) {
                            th = th2;
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeString(query);
                        if (extras != null) {
                            _data.writeInt(1);
                            extras.writeToParcel(_data, 0);
                        } else {
                            _data.writeInt(0);
                        }
                        try {
                            if (this.mRemote.transact(10, _data, null, 1) || Stub.getDefaultImpl() == null) {
                                _data.recycle();
                                return;
                            }
                            Stub.getDefaultImpl().onPlayFromSearch(packageName, pid, uid, caller, query, extras);
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
                } catch (Throwable th6) {
                    th = th6;
                    _data.recycle();
                    throw th;
                }
            }

            @Override // android.media.session.ISessionCallback
            public void onPlayFromUri(String packageName, int pid, int uid, ISessionControllerCallback caller, Uri uri, Bundle extras) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeString(packageName);
                    } catch (Throwable th) {
                        th = th;
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(pid);
                    } catch (Throwable th2) {
                        th = th2;
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(uid);
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
                        try {
                            if (this.mRemote.transact(11, _data, null, 1) || Stub.getDefaultImpl() == null) {
                                _data.recycle();
                                return;
                            }
                            Stub.getDefaultImpl().onPlayFromUri(packageName, pid, uid, caller, uri, extras);
                            _data.recycle();
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
            }

            @Override // android.media.session.ISessionCallback
            public void onSkipToTrack(String packageName, int pid, int uid, ISessionControllerCallback caller, long id) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeString(packageName);
                    } catch (Throwable th) {
                        th = th;
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(pid);
                        try {
                            _data.writeInt(uid);
                            _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                            try {
                                _data.writeLong(id);
                            } catch (Throwable th2) {
                                th = th2;
                                _data.recycle();
                                throw th;
                            }
                        } catch (Throwable th3) {
                            th = th3;
                            _data.recycle();
                            throw th;
                        }
                        try {
                            if (this.mRemote.transact(12, _data, null, 1) || Stub.getDefaultImpl() == null) {
                                _data.recycle();
                                return;
                            }
                            Stub.getDefaultImpl().onSkipToTrack(packageName, pid, uid, caller, id);
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
                } catch (Throwable th6) {
                    th = th6;
                    _data.recycle();
                    throw th;
                }
            }

            @Override // android.media.session.ISessionCallback
            public void onPause(String packageName, int pid, int uid, ISessionControllerCallback caller) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(pid);
                    _data.writeInt(uid);
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    if (this.mRemote.transact(13, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onPause(packageName, pid, uid, caller);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.media.session.ISessionCallback
            public void onStop(String packageName, int pid, int uid, ISessionControllerCallback caller) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(pid);
                    _data.writeInt(uid);
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    if (this.mRemote.transact(14, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onStop(packageName, pid, uid, caller);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.media.session.ISessionCallback
            public void onNext(String packageName, int pid, int uid, ISessionControllerCallback caller) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(pid);
                    _data.writeInt(uid);
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    if (this.mRemote.transact(15, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onNext(packageName, pid, uid, caller);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.media.session.ISessionCallback
            public void onPrevious(String packageName, int pid, int uid, ISessionControllerCallback caller) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(pid);
                    _data.writeInt(uid);
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    if (this.mRemote.transact(16, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onPrevious(packageName, pid, uid, caller);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.media.session.ISessionCallback
            public void onFastForward(String packageName, int pid, int uid, ISessionControllerCallback caller) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(pid);
                    _data.writeInt(uid);
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    if (this.mRemote.transact(17, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onFastForward(packageName, pid, uid, caller);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.media.session.ISessionCallback
            public void onRewind(String packageName, int pid, int uid, ISessionControllerCallback caller) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(pid);
                    _data.writeInt(uid);
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    if (this.mRemote.transact(18, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onRewind(packageName, pid, uid, caller);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.media.session.ISessionCallback
            public void onSeekTo(String packageName, int pid, int uid, ISessionControllerCallback caller, long pos) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeString(packageName);
                    } catch (Throwable th) {
                        th = th;
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(pid);
                        try {
                            _data.writeInt(uid);
                            _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                            try {
                                _data.writeLong(pos);
                            } catch (Throwable th2) {
                                th = th2;
                                _data.recycle();
                                throw th;
                            }
                        } catch (Throwable th3) {
                            th = th3;
                            _data.recycle();
                            throw th;
                        }
                        try {
                            if (this.mRemote.transact(19, _data, null, 1) || Stub.getDefaultImpl() == null) {
                                _data.recycle();
                                return;
                            }
                            Stub.getDefaultImpl().onSeekTo(packageName, pid, uid, caller, pos);
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
                } catch (Throwable th6) {
                    th = th6;
                    _data.recycle();
                    throw th;
                }
            }

            @Override // android.media.session.ISessionCallback
            public void onRate(String packageName, int pid, int uid, ISessionControllerCallback caller, Rating rating) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(pid);
                    _data.writeInt(uid);
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    if (rating != null) {
                        _data.writeInt(1);
                        rating.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(20, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onRate(packageName, pid, uid, caller, rating);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.media.session.ISessionCallback
            public void onSetPlaybackSpeed(String packageName, int pid, int uid, ISessionControllerCallback caller, float speed) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(pid);
                    _data.writeInt(uid);
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    _data.writeFloat(speed);
                    if (this.mRemote.transact(21, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onSetPlaybackSpeed(packageName, pid, uid, caller, speed);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.media.session.ISessionCallback
            public void onCustomAction(String packageName, int pid, int uid, ISessionControllerCallback caller, String action, Bundle args) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeString(packageName);
                    } catch (Throwable th) {
                        th = th;
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(pid);
                        try {
                            _data.writeInt(uid);
                            _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                        } catch (Throwable th2) {
                            th = th2;
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeString(action);
                        if (args != null) {
                            _data.writeInt(1);
                            args.writeToParcel(_data, 0);
                        } else {
                            _data.writeInt(0);
                        }
                        try {
                            if (this.mRemote.transact(22, _data, null, 1) || Stub.getDefaultImpl() == null) {
                                _data.recycle();
                                return;
                            }
                            Stub.getDefaultImpl().onCustomAction(packageName, pid, uid, caller, action, args);
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
                } catch (Throwable th6) {
                    th = th6;
                    _data.recycle();
                    throw th;
                }
            }

            @Override // android.media.session.ISessionCallback
            public void onAdjustVolume(String packageName, int pid, int uid, ISessionControllerCallback caller, int direction) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(pid);
                    _data.writeInt(uid);
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    _data.writeInt(direction);
                    if (this.mRemote.transact(23, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onAdjustVolume(packageName, pid, uid, caller, direction);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.media.session.ISessionCallback
            public void onSetVolumeTo(String packageName, int pid, int uid, ISessionControllerCallback caller, int value) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(pid);
                    _data.writeInt(uid);
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    _data.writeInt(value);
                    if (this.mRemote.transact(24, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onSetVolumeTo(packageName, pid, uid, caller, value);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(ISessionCallback impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static ISessionCallback getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
