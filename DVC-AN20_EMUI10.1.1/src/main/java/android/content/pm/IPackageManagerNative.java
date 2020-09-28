package android.content.pm;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IPackageManagerNative extends IInterface {
    public static final int LOCATION_PRODUCT = 4;
    public static final int LOCATION_SYSTEM = 1;
    public static final int LOCATION_VENDOR = 2;

    String getInstallerForPackage(String str) throws RemoteException;

    int getLocationFlags(String str) throws RemoteException;

    String getModuleMetadataPackageName() throws RemoteException;

    String[] getNamesForUids(int[] iArr) throws RemoteException;

    int getTargetSdkVersionForPackage(String str) throws RemoteException;

    long getVersionCodeForPackage(String str) throws RemoteException;

    boolean[] isAudioPlaybackCaptureAllowed(String[] strArr) throws RemoteException;

    public static class Default implements IPackageManagerNative {
        @Override // android.content.pm.IPackageManagerNative
        public String[] getNamesForUids(int[] uids) throws RemoteException {
            return null;
        }

        @Override // android.content.pm.IPackageManagerNative
        public String getInstallerForPackage(String packageName) throws RemoteException {
            return null;
        }

        @Override // android.content.pm.IPackageManagerNative
        public long getVersionCodeForPackage(String packageName) throws RemoteException {
            return 0;
        }

        @Override // android.content.pm.IPackageManagerNative
        public boolean[] isAudioPlaybackCaptureAllowed(String[] packageNames) throws RemoteException {
            return null;
        }

        @Override // android.content.pm.IPackageManagerNative
        public int getLocationFlags(String packageName) throws RemoteException {
            return 0;
        }

        @Override // android.content.pm.IPackageManagerNative
        public int getTargetSdkVersionForPackage(String packageName) throws RemoteException {
            return 0;
        }

        @Override // android.content.pm.IPackageManagerNative
        public String getModuleMetadataPackageName() throws RemoteException {
            return null;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IPackageManagerNative {
        private static final String DESCRIPTOR = "android.content.pm.IPackageManagerNative";
        static final int TRANSACTION_getInstallerForPackage = 2;
        static final int TRANSACTION_getLocationFlags = 5;
        static final int TRANSACTION_getModuleMetadataPackageName = 7;
        static final int TRANSACTION_getNamesForUids = 1;
        static final int TRANSACTION_getTargetSdkVersionForPackage = 6;
        static final int TRANSACTION_getVersionCodeForPackage = 3;
        static final int TRANSACTION_isAudioPlaybackCaptureAllowed = 4;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IPackageManagerNative asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IPackageManagerNative)) {
                return new Proxy(obj);
            }
            return (IPackageManagerNative) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "getNamesForUids";
                case 2:
                    return "getInstallerForPackage";
                case 3:
                    return "getVersionCodeForPackage";
                case 4:
                    return "isAudioPlaybackCaptureAllowed";
                case 5:
                    return "getLocationFlags";
                case 6:
                    return "getTargetSdkVersionForPackage";
                case 7:
                    return "getModuleMetadataPackageName";
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
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        String[] _result = getNamesForUids(data.createIntArray());
                        reply.writeNoException();
                        reply.writeStringArray(_result);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        String _result2 = getInstallerForPackage(data.readString());
                        reply.writeNoException();
                        reply.writeString(_result2);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        long _result3 = getVersionCodeForPackage(data.readString());
                        reply.writeNoException();
                        reply.writeLong(_result3);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        boolean[] _result4 = isAudioPlaybackCaptureAllowed(data.createStringArray());
                        reply.writeNoException();
                        reply.writeBooleanArray(_result4);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        int _result5 = getLocationFlags(data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result5);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        int _result6 = getTargetSdkVersionForPackage(data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result6);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        String _result7 = getModuleMetadataPackageName();
                        reply.writeNoException();
                        reply.writeString(_result7);
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
        public static class Proxy implements IPackageManagerNative {
            public static IPackageManagerNative sDefaultImpl;
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

            @Override // android.content.pm.IPackageManagerNative
            public String[] getNamesForUids(int[] uids) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeIntArray(uids);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getNamesForUids(uids);
                    }
                    _reply.readException();
                    String[] _result = _reply.createStringArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.pm.IPackageManagerNative
            public String getInstallerForPackage(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getInstallerForPackage(packageName);
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

            @Override // android.content.pm.IPackageManagerNative
            public long getVersionCodeForPackage(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getVersionCodeForPackage(packageName);
                    }
                    _reply.readException();
                    long _result = _reply.readLong();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.pm.IPackageManagerNative
            public boolean[] isAudioPlaybackCaptureAllowed(String[] packageNames) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringArray(packageNames);
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isAudioPlaybackCaptureAllowed(packageNames);
                    }
                    _reply.readException();
                    boolean[] _result = _reply.createBooleanArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.pm.IPackageManagerNative
            public int getLocationFlags(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getLocationFlags(packageName);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.pm.IPackageManagerNative
            public int getTargetSdkVersionForPackage(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    if (!this.mRemote.transact(6, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getTargetSdkVersionForPackage(packageName);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.pm.IPackageManagerNative
            public String getModuleMetadataPackageName() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(7, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getModuleMetadataPackageName();
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
        }

        public static boolean setDefaultImpl(IPackageManagerNative impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IPackageManagerNative getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
