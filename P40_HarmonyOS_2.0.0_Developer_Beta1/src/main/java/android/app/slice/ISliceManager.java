package android.app.slice;

import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface ISliceManager extends IInterface {
    void applyRestore(byte[] bArr, int i) throws RemoteException;

    int checkSlicePermission(Uri uri, String str, String str2, int i, int i2, String[] strArr) throws RemoteException;

    byte[] getBackupPayload(int i) throws RemoteException;

    Uri[] getPinnedSlices(String str) throws RemoteException;

    SliceSpec[] getPinnedSpecs(Uri uri, String str) throws RemoteException;

    void grantPermissionFromUser(Uri uri, String str, String str2, boolean z) throws RemoteException;

    void grantSlicePermission(String str, String str2, Uri uri) throws RemoteException;

    boolean hasSliceAccess(String str) throws RemoteException;

    void pinSlice(String str, Uri uri, SliceSpec[] sliceSpecArr, IBinder iBinder) throws RemoteException;

    void revokeSlicePermission(String str, String str2, Uri uri) throws RemoteException;

    void unpinSlice(String str, Uri uri, IBinder iBinder) throws RemoteException;

    public static class Default implements ISliceManager {
        @Override // android.app.slice.ISliceManager
        public void pinSlice(String pkg, Uri uri, SliceSpec[] specs, IBinder token) throws RemoteException {
        }

        @Override // android.app.slice.ISliceManager
        public void unpinSlice(String pkg, Uri uri, IBinder token) throws RemoteException {
        }

        @Override // android.app.slice.ISliceManager
        public boolean hasSliceAccess(String pkg) throws RemoteException {
            return false;
        }

        @Override // android.app.slice.ISliceManager
        public SliceSpec[] getPinnedSpecs(Uri uri, String pkg) throws RemoteException {
            return null;
        }

        @Override // android.app.slice.ISliceManager
        public Uri[] getPinnedSlices(String pkg) throws RemoteException {
            return null;
        }

        @Override // android.app.slice.ISliceManager
        public byte[] getBackupPayload(int user) throws RemoteException {
            return null;
        }

        @Override // android.app.slice.ISliceManager
        public void applyRestore(byte[] payload, int user) throws RemoteException {
        }

        @Override // android.app.slice.ISliceManager
        public void grantSlicePermission(String callingPkg, String toPkg, Uri uri) throws RemoteException {
        }

        @Override // android.app.slice.ISliceManager
        public void revokeSlicePermission(String callingPkg, String toPkg, Uri uri) throws RemoteException {
        }

        @Override // android.app.slice.ISliceManager
        public int checkSlicePermission(Uri uri, String callingPkg, String pkg, int pid, int uid, String[] autoGrantPermissions) throws RemoteException {
            return 0;
        }

        @Override // android.app.slice.ISliceManager
        public void grantPermissionFromUser(Uri uri, String pkg, String callingPkg, boolean allSlices) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements ISliceManager {
        private static final String DESCRIPTOR = "android.app.slice.ISliceManager";
        static final int TRANSACTION_applyRestore = 7;
        static final int TRANSACTION_checkSlicePermission = 10;
        static final int TRANSACTION_getBackupPayload = 6;
        static final int TRANSACTION_getPinnedSlices = 5;
        static final int TRANSACTION_getPinnedSpecs = 4;
        static final int TRANSACTION_grantPermissionFromUser = 11;
        static final int TRANSACTION_grantSlicePermission = 8;
        static final int TRANSACTION_hasSliceAccess = 3;
        static final int TRANSACTION_pinSlice = 1;
        static final int TRANSACTION_revokeSlicePermission = 9;
        static final int TRANSACTION_unpinSlice = 2;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ISliceManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ISliceManager)) {
                return new Proxy(obj);
            }
            return (ISliceManager) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "pinSlice";
                case 2:
                    return "unpinSlice";
                case 3:
                    return "hasSliceAccess";
                case 4:
                    return "getPinnedSpecs";
                case 5:
                    return "getPinnedSlices";
                case 6:
                    return "getBackupPayload";
                case 7:
                    return "applyRestore";
                case 8:
                    return "grantSlicePermission";
                case 9:
                    return "revokeSlicePermission";
                case 10:
                    return "checkSlicePermission";
                case 11:
                    return "grantPermissionFromUser";
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
            Uri _arg12;
            Uri _arg0;
            Uri _arg2;
            Uri _arg22;
            Uri _arg02;
            Uri _arg03;
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg04 = data.readString();
                        if (data.readInt() != 0) {
                            _arg1 = Uri.CREATOR.createFromParcel(data);
                        } else {
                            _arg1 = null;
                        }
                        pinSlice(_arg04, _arg1, (SliceSpec[]) data.createTypedArray(SliceSpec.CREATOR), data.readStrongBinder());
                        reply.writeNoException();
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg05 = data.readString();
                        if (data.readInt() != 0) {
                            _arg12 = Uri.CREATOR.createFromParcel(data);
                        } else {
                            _arg12 = null;
                        }
                        unpinSlice(_arg05, _arg12, data.readStrongBinder());
                        reply.writeNoException();
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        boolean hasSliceAccess = hasSliceAccess(data.readString());
                        reply.writeNoException();
                        reply.writeInt(hasSliceAccess ? 1 : 0);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = Uri.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        SliceSpec[] _result = getPinnedSpecs(_arg0, data.readString());
                        reply.writeNoException();
                        reply.writeTypedArray(_result, 1);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        Uri[] _result2 = getPinnedSlices(data.readString());
                        reply.writeNoException();
                        reply.writeTypedArray(_result2, 1);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        byte[] _result3 = getBackupPayload(data.readInt());
                        reply.writeNoException();
                        reply.writeByteArray(_result3);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        applyRestore(data.createByteArray(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg06 = data.readString();
                        String _arg13 = data.readString();
                        if (data.readInt() != 0) {
                            _arg2 = Uri.CREATOR.createFromParcel(data);
                        } else {
                            _arg2 = null;
                        }
                        grantSlicePermission(_arg06, _arg13, _arg2);
                        reply.writeNoException();
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg07 = data.readString();
                        String _arg14 = data.readString();
                        if (data.readInt() != 0) {
                            _arg22 = Uri.CREATOR.createFromParcel(data);
                        } else {
                            _arg22 = null;
                        }
                        revokeSlicePermission(_arg07, _arg14, _arg22);
                        reply.writeNoException();
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = Uri.CREATOR.createFromParcel(data);
                        } else {
                            _arg02 = null;
                        }
                        int _result4 = checkSlicePermission(_arg02, data.readString(), data.readString(), data.readInt(), data.readInt(), data.createStringArray());
                        reply.writeNoException();
                        reply.writeInt(_result4);
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg03 = Uri.CREATOR.createFromParcel(data);
                        } else {
                            _arg03 = null;
                        }
                        grantPermissionFromUser(_arg03, data.readString(), data.readString(), data.readInt() != 0);
                        reply.writeNoException();
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
        public static class Proxy implements ISliceManager {
            public static ISliceManager sDefaultImpl;
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

            @Override // android.app.slice.ISliceManager
            public void pinSlice(String pkg, Uri uri, SliceSpec[] specs, IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    if (uri != null) {
                        _data.writeInt(1);
                        uri.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeTypedArray(specs, 0);
                    _data.writeStrongBinder(token);
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().pinSlice(pkg, uri, specs, token);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.slice.ISliceManager
            public void unpinSlice(String pkg, Uri uri, IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    if (uri != null) {
                        _data.writeInt(1);
                        uri.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(token);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().unpinSlice(pkg, uri, token);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.slice.ISliceManager
            public boolean hasSliceAccess(String pkg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    boolean _result = false;
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().hasSliceAccess(pkg);
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

            @Override // android.app.slice.ISliceManager
            public SliceSpec[] getPinnedSpecs(Uri uri, String pkg) throws RemoteException {
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
                    _data.writeString(pkg);
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getPinnedSpecs(uri, pkg);
                    }
                    _reply.readException();
                    SliceSpec[] _result = (SliceSpec[]) _reply.createTypedArray(SliceSpec.CREATOR);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.slice.ISliceManager
            public Uri[] getPinnedSlices(String pkg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getPinnedSlices(pkg);
                    }
                    _reply.readException();
                    Uri[] _result = (Uri[]) _reply.createTypedArray(Uri.CREATOR);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.slice.ISliceManager
            public byte[] getBackupPayload(int user) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(user);
                    if (!this.mRemote.transact(6, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getBackupPayload(user);
                    }
                    _reply.readException();
                    byte[] _result = _reply.createByteArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.slice.ISliceManager
            public void applyRestore(byte[] payload, int user) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeByteArray(payload);
                    _data.writeInt(user);
                    if (this.mRemote.transact(7, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().applyRestore(payload, user);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.slice.ISliceManager
            public void grantSlicePermission(String callingPkg, String toPkg, Uri uri) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPkg);
                    _data.writeString(toPkg);
                    if (uri != null) {
                        _data.writeInt(1);
                        uri.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(8, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().grantSlicePermission(callingPkg, toPkg, uri);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.slice.ISliceManager
            public void revokeSlicePermission(String callingPkg, String toPkg, Uri uri) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPkg);
                    _data.writeString(toPkg);
                    if (uri != null) {
                        _data.writeInt(1);
                        uri.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(9, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().revokeSlicePermission(callingPkg, toPkg, uri);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.slice.ISliceManager
            public int checkSlicePermission(Uri uri, String callingPkg, String pkg, int pid, int uid, String[] autoGrantPermissions) throws RemoteException {
                Throwable th;
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
                    try {
                        _data.writeString(callingPkg);
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeString(pkg);
                    } catch (Throwable th3) {
                        th = th3;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(pid);
                        try {
                            _data.writeInt(uid);
                        } catch (Throwable th4) {
                            th = th4;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                        try {
                            _data.writeStringArray(autoGrantPermissions);
                            if (this.mRemote.transact(10, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                int _result = _reply.readInt();
                                _reply.recycle();
                                _data.recycle();
                                return _result;
                            }
                            int checkSlicePermission = Stub.getDefaultImpl().checkSlicePermission(uri, callingPkg, pkg, pid, uid, autoGrantPermissions);
                            _reply.recycle();
                            _data.recycle();
                            return checkSlicePermission;
                        } catch (Throwable th5) {
                            th = th5;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th6) {
                        th = th6;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th7) {
                    th = th7;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // android.app.slice.ISliceManager
            public void grantPermissionFromUser(Uri uri, String pkg, String callingPkg, boolean allSlices) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    int i = 1;
                    if (uri != null) {
                        _data.writeInt(1);
                        uri.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(pkg);
                    _data.writeString(callingPkg);
                    if (!allSlices) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    if (this.mRemote.transact(11, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().grantPermissionFromUser(uri, pkg, callingPkg, allSlices);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(ISliceManager impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static ISliceManager getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
