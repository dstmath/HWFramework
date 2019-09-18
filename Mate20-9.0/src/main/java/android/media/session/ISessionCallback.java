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

    public static abstract class Stub extends Binder implements ISessionCallback {
        private static final String DESCRIPTOR = "android.media.session.ISessionCallback";
        static final int TRANSACTION_onAdjustVolume = 22;
        static final int TRANSACTION_onCommand = 1;
        static final int TRANSACTION_onCustomAction = 21;
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
        static final int TRANSACTION_onSetVolumeTo = 23;
        static final int TRANSACTION_onSkipToTrack = 12;
        static final int TRANSACTION_onStop = 14;

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

            public void onCommand(String packageName, int pid, int uid, ISessionControllerCallback caller, String command, Bundle args, ResultReceiver cb) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(pid);
                    _data.writeInt(uid);
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
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onMediaButton(String packageName, int pid, int uid, Intent mediaButtonIntent, int sequenceNumber, ResultReceiver cb) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(pid);
                    _data.writeInt(uid);
                    if (mediaButtonIntent != null) {
                        _data.writeInt(1);
                        mediaButtonIntent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(sequenceNumber);
                    if (cb != null) {
                        _data.writeInt(1);
                        cb.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(2, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

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
                    this.mRemote.transact(3, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onPrepare(String packageName, int pid, int uid, ISessionControllerCallback caller) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(pid);
                    _data.writeInt(uid);
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    this.mRemote.transact(4, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onPrepareFromMediaId(String packageName, int pid, int uid, ISessionControllerCallback caller, String mediaId, Bundle extras) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(pid);
                    _data.writeInt(uid);
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    _data.writeString(mediaId);
                    if (extras != null) {
                        _data.writeInt(1);
                        extras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(5, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onPrepareFromSearch(String packageName, int pid, int uid, ISessionControllerCallback caller, String query, Bundle extras) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(pid);
                    _data.writeInt(uid);
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    _data.writeString(query);
                    if (extras != null) {
                        _data.writeInt(1);
                        extras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(6, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onPrepareFromUri(String packageName, int pid, int uid, ISessionControllerCallback caller, Uri uri, Bundle extras) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(pid);
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
                    this.mRemote.transact(7, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onPlay(String packageName, int pid, int uid, ISessionControllerCallback caller) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(pid);
                    _data.writeInt(uid);
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    this.mRemote.transact(8, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onPlayFromMediaId(String packageName, int pid, int uid, ISessionControllerCallback caller, String mediaId, Bundle extras) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(pid);
                    _data.writeInt(uid);
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    _data.writeString(mediaId);
                    if (extras != null) {
                        _data.writeInt(1);
                        extras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(9, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onPlayFromSearch(String packageName, int pid, int uid, ISessionControllerCallback caller, String query, Bundle extras) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(pid);
                    _data.writeInt(uid);
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    _data.writeString(query);
                    if (extras != null) {
                        _data.writeInt(1);
                        extras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(10, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onPlayFromUri(String packageName, int pid, int uid, ISessionControllerCallback caller, Uri uri, Bundle extras) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(pid);
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
                    this.mRemote.transact(11, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onSkipToTrack(String packageName, int pid, int uid, ISessionControllerCallback caller, long id) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(pid);
                    _data.writeInt(uid);
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    _data.writeLong(id);
                    this.mRemote.transact(12, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onPause(String packageName, int pid, int uid, ISessionControllerCallback caller) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(pid);
                    _data.writeInt(uid);
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    this.mRemote.transact(13, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onStop(String packageName, int pid, int uid, ISessionControllerCallback caller) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(pid);
                    _data.writeInt(uid);
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    this.mRemote.transact(14, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onNext(String packageName, int pid, int uid, ISessionControllerCallback caller) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(pid);
                    _data.writeInt(uid);
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    this.mRemote.transact(15, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onPrevious(String packageName, int pid, int uid, ISessionControllerCallback caller) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(pid);
                    _data.writeInt(uid);
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    this.mRemote.transact(16, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onFastForward(String packageName, int pid, int uid, ISessionControllerCallback caller) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(pid);
                    _data.writeInt(uid);
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    this.mRemote.transact(17, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onRewind(String packageName, int pid, int uid, ISessionControllerCallback caller) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(pid);
                    _data.writeInt(uid);
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    this.mRemote.transact(18, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onSeekTo(String packageName, int pid, int uid, ISessionControllerCallback caller, long pos) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(pid);
                    _data.writeInt(uid);
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    _data.writeLong(pos);
                    this.mRemote.transact(19, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

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
                    this.mRemote.transact(20, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onCustomAction(String packageName, int pid, int uid, ISessionControllerCallback caller, String action, Bundle args) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(pid);
                    _data.writeInt(uid);
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    _data.writeString(action);
                    if (args != null) {
                        _data.writeInt(1);
                        args.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(21, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onAdjustVolume(String packageName, int pid, int uid, ISessionControllerCallback caller, int direction) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(pid);
                    _data.writeInt(uid);
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    _data.writeInt(direction);
                    this.mRemote.transact(22, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onSetVolumeTo(String packageName, int pid, int uid, ISessionControllerCallback caller, int value) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(pid);
                    _data.writeInt(uid);
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    _data.writeInt(value);
                    this.mRemote.transact(23, _data, null, 1);
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

        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v2, resolved type: android.os.ResultReceiver} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v3, resolved type: android.os.ResultReceiver} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v8, resolved type: android.os.ResultReceiver} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v13, resolved type: android.os.ResultReceiver} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v19, resolved type: android.os.ResultReceiver} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r6v5, resolved type: android.os.Bundle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v24, resolved type: android.os.ResultReceiver} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r6v6, resolved type: android.os.Bundle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v29, resolved type: android.os.ResultReceiver} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r6v7, resolved type: android.os.Bundle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v35, resolved type: android.os.ResultReceiver} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r6v8, resolved type: android.os.Bundle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v40, resolved type: android.os.ResultReceiver} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r6v9, resolved type: android.os.Bundle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v45, resolved type: android.os.ResultReceiver} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r6v10, resolved type: android.os.Bundle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v60, resolved type: android.os.ResultReceiver} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v15, resolved type: android.media.Rating} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v65, resolved type: android.os.ResultReceiver} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r6v12, resolved type: android.os.Bundle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v75, resolved type: android.os.ResultReceiver} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v76, resolved type: android.os.ResultReceiver} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v77, resolved type: android.os.ResultReceiver} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v78, resolved type: android.os.ResultReceiver} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v79, resolved type: android.os.ResultReceiver} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v80, resolved type: android.os.ResultReceiver} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v81, resolved type: android.os.ResultReceiver} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v82, resolved type: android.os.ResultReceiver} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v83, resolved type: android.os.ResultReceiver} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v84, resolved type: android.os.ResultReceiver} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v85, resolved type: android.os.ResultReceiver} */
        /* JADX WARNING: type inference failed for: r5v2, types: [android.content.Intent] */
        /* JADX WARNING: type inference failed for: r0v17, types: [android.content.Intent] */
        /* JADX WARNING: type inference failed for: r0v23, types: [android.os.Bundle] */
        /* JADX WARNING: type inference failed for: r0v28, types: [android.os.Bundle] */
        /* JADX WARNING: type inference failed for: r0v33, types: [android.os.Bundle] */
        /* JADX WARNING: type inference failed for: r0v39, types: [android.os.Bundle] */
        /* JADX WARNING: type inference failed for: r0v44, types: [android.os.Bundle] */
        /* JADX WARNING: type inference failed for: r0v49, types: [android.os.Bundle] */
        /* JADX WARNING: type inference failed for: r0v64, types: [android.media.Rating] */
        /* JADX WARNING: type inference failed for: r0v69, types: [android.os.Bundle] */
        /* JADX WARNING: Multi-variable type inference failed */
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            Bundle _arg5;
            Intent _arg3;
            Uri _arg4;
            Uri _arg42;
            int i = code;
            Parcel parcel = data;
            if (i != 1598968902) {
                ResultReceiver _arg52 = null;
                switch (i) {
                    case 1:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg0 = data.readString();
                        int _arg1 = data.readInt();
                        int _arg2 = data.readInt();
                        ISessionControllerCallback _arg32 = ISessionControllerCallback.Stub.asInterface(data.readStrongBinder());
                        String _arg43 = data.readString();
                        if (data.readInt() != 0) {
                            _arg5 = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg5 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg52 = (ResultReceiver) ResultReceiver.CREATOR.createFromParcel(parcel);
                        }
                        onCommand(_arg0, _arg1, _arg2, _arg32, _arg43, _arg5, _arg52);
                        return true;
                    case 2:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg02 = data.readString();
                        int _arg12 = data.readInt();
                        int _arg22 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg3 = Intent.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg3 = null;
                        }
                        int _arg44 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg52 = (ResultReceiver) ResultReceiver.CREATOR.createFromParcel(parcel);
                        }
                        onMediaButton(_arg02, _arg12, _arg22, _arg3, _arg44, _arg52);
                        return true;
                    case 3:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg03 = data.readString();
                        int _arg13 = data.readInt();
                        int _arg23 = data.readInt();
                        ISessionControllerCallback _arg33 = ISessionControllerCallback.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg52 = Intent.CREATOR.createFromParcel(parcel);
                        }
                        onMediaButtonFromController(_arg03, _arg13, _arg23, _arg33, _arg52);
                        return true;
                    case 4:
                        parcel.enforceInterface(DESCRIPTOR);
                        onPrepare(data.readString(), data.readInt(), data.readInt(), ISessionControllerCallback.Stub.asInterface(data.readStrongBinder()));
                        return true;
                    case 5:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg04 = data.readString();
                        int _arg14 = data.readInt();
                        int _arg24 = data.readInt();
                        ISessionControllerCallback _arg34 = ISessionControllerCallback.Stub.asInterface(data.readStrongBinder());
                        String _arg45 = data.readString();
                        if (data.readInt() != 0) {
                            _arg52 = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                        }
                        onPrepareFromMediaId(_arg04, _arg14, _arg24, _arg34, _arg45, _arg52);
                        return true;
                    case 6:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg05 = data.readString();
                        int _arg15 = data.readInt();
                        int _arg25 = data.readInt();
                        ISessionControllerCallback _arg35 = ISessionControllerCallback.Stub.asInterface(data.readStrongBinder());
                        String _arg46 = data.readString();
                        if (data.readInt() != 0) {
                            _arg52 = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                        }
                        onPrepareFromSearch(_arg05, _arg15, _arg25, _arg35, _arg46, _arg52);
                        return true;
                    case 7:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg06 = data.readString();
                        int _arg16 = data.readInt();
                        int _arg26 = data.readInt();
                        ISessionControllerCallback _arg36 = ISessionControllerCallback.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg4 = Uri.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg4 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg52 = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                        }
                        onPrepareFromUri(_arg06, _arg16, _arg26, _arg36, _arg4, _arg52);
                        return true;
                    case 8:
                        parcel.enforceInterface(DESCRIPTOR);
                        onPlay(data.readString(), data.readInt(), data.readInt(), ISessionControllerCallback.Stub.asInterface(data.readStrongBinder()));
                        return true;
                    case 9:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg07 = data.readString();
                        int _arg17 = data.readInt();
                        int _arg27 = data.readInt();
                        ISessionControllerCallback _arg37 = ISessionControllerCallback.Stub.asInterface(data.readStrongBinder());
                        String _arg47 = data.readString();
                        if (data.readInt() != 0) {
                            _arg52 = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                        }
                        onPlayFromMediaId(_arg07, _arg17, _arg27, _arg37, _arg47, _arg52);
                        return true;
                    case 10:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg08 = data.readString();
                        int _arg18 = data.readInt();
                        int _arg28 = data.readInt();
                        ISessionControllerCallback _arg38 = ISessionControllerCallback.Stub.asInterface(data.readStrongBinder());
                        String _arg48 = data.readString();
                        if (data.readInt() != 0) {
                            _arg52 = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                        }
                        onPlayFromSearch(_arg08, _arg18, _arg28, _arg38, _arg48, _arg52);
                        return true;
                    case 11:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg09 = data.readString();
                        int _arg19 = data.readInt();
                        int _arg29 = data.readInt();
                        ISessionControllerCallback _arg39 = ISessionControllerCallback.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg42 = Uri.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg42 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg52 = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                        }
                        onPlayFromUri(_arg09, _arg19, _arg29, _arg39, _arg42, _arg52);
                        return true;
                    case 12:
                        parcel.enforceInterface(DESCRIPTOR);
                        onSkipToTrack(data.readString(), data.readInt(), data.readInt(), ISessionControllerCallback.Stub.asInterface(data.readStrongBinder()), data.readLong());
                        return true;
                    case 13:
                        parcel.enforceInterface(DESCRIPTOR);
                        onPause(data.readString(), data.readInt(), data.readInt(), ISessionControllerCallback.Stub.asInterface(data.readStrongBinder()));
                        return true;
                    case 14:
                        parcel.enforceInterface(DESCRIPTOR);
                        onStop(data.readString(), data.readInt(), data.readInt(), ISessionControllerCallback.Stub.asInterface(data.readStrongBinder()));
                        return true;
                    case 15:
                        parcel.enforceInterface(DESCRIPTOR);
                        onNext(data.readString(), data.readInt(), data.readInt(), ISessionControllerCallback.Stub.asInterface(data.readStrongBinder()));
                        return true;
                    case 16:
                        parcel.enforceInterface(DESCRIPTOR);
                        onPrevious(data.readString(), data.readInt(), data.readInt(), ISessionControllerCallback.Stub.asInterface(data.readStrongBinder()));
                        return true;
                    case 17:
                        parcel.enforceInterface(DESCRIPTOR);
                        onFastForward(data.readString(), data.readInt(), data.readInt(), ISessionControllerCallback.Stub.asInterface(data.readStrongBinder()));
                        return true;
                    case 18:
                        parcel.enforceInterface(DESCRIPTOR);
                        onRewind(data.readString(), data.readInt(), data.readInt(), ISessionControllerCallback.Stub.asInterface(data.readStrongBinder()));
                        return true;
                    case 19:
                        parcel.enforceInterface(DESCRIPTOR);
                        onSeekTo(data.readString(), data.readInt(), data.readInt(), ISessionControllerCallback.Stub.asInterface(data.readStrongBinder()), data.readLong());
                        return true;
                    case 20:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg010 = data.readString();
                        int _arg110 = data.readInt();
                        int _arg210 = data.readInt();
                        ISessionControllerCallback _arg310 = ISessionControllerCallback.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg52 = Rating.CREATOR.createFromParcel(parcel);
                        }
                        onRate(_arg010, _arg110, _arg210, _arg310, _arg52);
                        return true;
                    case 21:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg011 = data.readString();
                        int _arg111 = data.readInt();
                        int _arg211 = data.readInt();
                        ISessionControllerCallback _arg311 = ISessionControllerCallback.Stub.asInterface(data.readStrongBinder());
                        String _arg49 = data.readString();
                        if (data.readInt() != 0) {
                            _arg52 = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                        }
                        onCustomAction(_arg011, _arg111, _arg211, _arg311, _arg49, _arg52);
                        return true;
                    case 22:
                        parcel.enforceInterface(DESCRIPTOR);
                        onAdjustVolume(data.readString(), data.readInt(), data.readInt(), ISessionControllerCallback.Stub.asInterface(data.readStrongBinder()), data.readInt());
                        return true;
                    case 23:
                        parcel.enforceInterface(DESCRIPTOR);
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
    }

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

    void onSetVolumeTo(String str, int i, int i2, ISessionControllerCallback iSessionControllerCallback, int i3) throws RemoteException;

    void onSkipToTrack(String str, int i, int i2, ISessionControllerCallback iSessionControllerCallback, long j) throws RemoteException;

    void onStop(String str, int i, int i2, ISessionControllerCallback iSessionControllerCallback) throws RemoteException;
}
