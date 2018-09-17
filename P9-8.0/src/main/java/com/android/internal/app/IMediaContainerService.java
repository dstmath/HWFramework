package com.android.internal.app;

import android.content.pm.PackageInfoLite;
import android.content.res.ObbInfo;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.android.internal.os.IParcelFileDescriptorFactory;

public interface IMediaContainerService extends IInterface {

    public static abstract class Stub extends Binder implements IMediaContainerService {
        private static final String DESCRIPTOR = "com.android.internal.app.IMediaContainerService";
        static final int TRANSACTION_calculateDirectorySize = 5;
        static final int TRANSACTION_calculateInstalledSize = 8;
        static final int TRANSACTION_clearDirectory = 7;
        static final int TRANSACTION_copyPackage = 2;
        static final int TRANSACTION_copyPackageToContainer = 1;
        static final int TRANSACTION_getFileSystemStats = 6;
        static final int TRANSACTION_getMinimalPackageInfo = 3;
        static final int TRANSACTION_getObbInfo = 4;

        private static class Proxy implements IMediaContainerService {
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

            public String copyPackageToContainer(String packagePath, String containerId, String key, boolean isExternal, boolean isForwardLocked, String abiOverride) throws RemoteException {
                int i = 1;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    int i2;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packagePath);
                    _data.writeString(containerId);
                    _data.writeString(key);
                    if (isExternal) {
                        i2 = 1;
                    } else {
                        i2 = 0;
                    }
                    _data.writeInt(i2);
                    if (!isForwardLocked) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    _data.writeString(abiOverride);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int copyPackage(String packagePath, IParcelFileDescriptorFactory target) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packagePath);
                    if (target != null) {
                        iBinder = target.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public PackageInfoLite getMinimalPackageInfo(String packagePath, int flags, String abiOverride) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    PackageInfoLite _result;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packagePath);
                    _data.writeInt(flags);
                    _data.writeString(abiOverride);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (PackageInfoLite) PackageInfoLite.CREATOR.createFromParcel(_reply);
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

            public ObbInfo getObbInfo(String filename) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    ObbInfo _result;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(filename);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (ObbInfo) ObbInfo.CREATOR.createFromParcel(_reply);
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

            public long calculateDirectorySize(String directory) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(directory);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                    long _result = _reply.readLong();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long[] getFileSystemStats(String path) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(path);
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                    long[] _result = _reply.createLongArray();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void clearDirectory(String directory) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(directory);
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long calculateInstalledSize(String packagePath, boolean isForwardLocked, String abiOverride) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packagePath);
                    if (isForwardLocked) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    _data.writeString(abiOverride);
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                    long _result = _reply.readLong();
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

        public static IMediaContainerService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IMediaContainerService)) {
                return new Proxy(obj);
            }
            return (IMediaContainerService) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            long _result;
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    String _result2 = copyPackageToContainer(data.readString(), data.readString(), data.readString(), data.readInt() != 0, data.readInt() != 0, data.readString());
                    reply.writeNoException();
                    reply.writeString(_result2);
                    return true;
                case 2:
                    data.enforceInterface(DESCRIPTOR);
                    int _result3 = copyPackage(data.readString(), com.android.internal.os.IParcelFileDescriptorFactory.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    reply.writeInt(_result3);
                    return true;
                case 3:
                    data.enforceInterface(DESCRIPTOR);
                    PackageInfoLite _result4 = getMinimalPackageInfo(data.readString(), data.readInt(), data.readString());
                    reply.writeNoException();
                    if (_result4 != null) {
                        reply.writeInt(1);
                        _result4.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case 4:
                    data.enforceInterface(DESCRIPTOR);
                    ObbInfo _result5 = getObbInfo(data.readString());
                    reply.writeNoException();
                    if (_result5 != null) {
                        reply.writeInt(1);
                        _result5.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case 5:
                    data.enforceInterface(DESCRIPTOR);
                    _result = calculateDirectorySize(data.readString());
                    reply.writeNoException();
                    reply.writeLong(_result);
                    return true;
                case 6:
                    data.enforceInterface(DESCRIPTOR);
                    long[] _result6 = getFileSystemStats(data.readString());
                    reply.writeNoException();
                    reply.writeLongArray(_result6);
                    return true;
                case 7:
                    data.enforceInterface(DESCRIPTOR);
                    clearDirectory(data.readString());
                    reply.writeNoException();
                    return true;
                case 8:
                    data.enforceInterface(DESCRIPTOR);
                    _result = calculateInstalledSize(data.readString(), data.readInt() != 0, data.readString());
                    reply.writeNoException();
                    reply.writeLong(_result);
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    long calculateDirectorySize(String str) throws RemoteException;

    long calculateInstalledSize(String str, boolean z, String str2) throws RemoteException;

    void clearDirectory(String str) throws RemoteException;

    int copyPackage(String str, IParcelFileDescriptorFactory iParcelFileDescriptorFactory) throws RemoteException;

    String copyPackageToContainer(String str, String str2, String str3, boolean z, boolean z2, String str4) throws RemoteException;

    long[] getFileSystemStats(String str) throws RemoteException;

    PackageInfoLite getMinimalPackageInfo(String str, int i, String str2) throws RemoteException;

    ObbInfo getObbInfo(String str) throws RemoteException;
}
