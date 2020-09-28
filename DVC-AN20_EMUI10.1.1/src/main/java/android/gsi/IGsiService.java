package android.gsi;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;

public interface IGsiService extends IInterface {
    public static final int BOOT_STATUS_DISABLED = 1;
    public static final int BOOT_STATUS_ENABLED = 3;
    public static final int BOOT_STATUS_NOT_INSTALLED = 0;
    public static final int BOOT_STATUS_SINGLE_BOOT = 2;
    public static final int BOOT_STATUS_WILL_WIPE = 4;
    public static final int INSTALL_ERROR_FILE_SYSTEM_CLUTTERED = 3;
    public static final int INSTALL_ERROR_GENERIC = 1;
    public static final int INSTALL_ERROR_NO_SPACE = 2;
    public static final int INSTALL_OK = 0;
    public static final int STATUS_COMPLETE = 2;
    public static final int STATUS_NO_OPERATION = 0;
    public static final int STATUS_WORKING = 1;

    int beginGsiInstall(GsiInstallParams gsiInstallParams) throws RemoteException;

    boolean cancelGsiInstall() throws RemoteException;

    boolean commitGsiChunkFromMemory(byte[] bArr) throws RemoteException;

    boolean commitGsiChunkFromStream(ParcelFileDescriptor parcelFileDescriptor, long j) throws RemoteException;

    boolean disableGsiInstall() throws RemoteException;

    int getGsiBootStatus() throws RemoteException;

    GsiProgress getInstallProgress() throws RemoteException;

    String getInstalledGsiImageDir() throws RemoteException;

    long getUserdataImageSize() throws RemoteException;

    boolean isGsiEnabled() throws RemoteException;

    boolean isGsiInstallInProgress() throws RemoteException;

    boolean isGsiInstalled() throws RemoteException;

    boolean isGsiRunning() throws RemoteException;

    boolean removeGsiInstall() throws RemoteException;

    int setGsiBootable(boolean z) throws RemoteException;

    int startGsiInstall(long j, long j2, boolean z) throws RemoteException;

    int wipeGsiUserdata() throws RemoteException;

    public static class Default implements IGsiService {
        @Override // android.gsi.IGsiService
        public int startGsiInstall(long gsiSize, long userdataSize, boolean wipeUserdata) throws RemoteException {
            return 0;
        }

        @Override // android.gsi.IGsiService
        public boolean commitGsiChunkFromStream(ParcelFileDescriptor stream, long bytes) throws RemoteException {
            return false;
        }

        @Override // android.gsi.IGsiService
        public GsiProgress getInstallProgress() throws RemoteException {
            return null;
        }

        @Override // android.gsi.IGsiService
        public boolean commitGsiChunkFromMemory(byte[] bytes) throws RemoteException {
            return false;
        }

        @Override // android.gsi.IGsiService
        public int setGsiBootable(boolean oneShot) throws RemoteException {
            return 0;
        }

        @Override // android.gsi.IGsiService
        public boolean isGsiEnabled() throws RemoteException {
            return false;
        }

        @Override // android.gsi.IGsiService
        public boolean cancelGsiInstall() throws RemoteException {
            return false;
        }

        @Override // android.gsi.IGsiService
        public boolean isGsiInstallInProgress() throws RemoteException {
            return false;
        }

        @Override // android.gsi.IGsiService
        public boolean removeGsiInstall() throws RemoteException {
            return false;
        }

        @Override // android.gsi.IGsiService
        public boolean disableGsiInstall() throws RemoteException {
            return false;
        }

        @Override // android.gsi.IGsiService
        public long getUserdataImageSize() throws RemoteException {
            return 0;
        }

        @Override // android.gsi.IGsiService
        public boolean isGsiRunning() throws RemoteException {
            return false;
        }

        @Override // android.gsi.IGsiService
        public boolean isGsiInstalled() throws RemoteException {
            return false;
        }

        @Override // android.gsi.IGsiService
        public int getGsiBootStatus() throws RemoteException {
            return 0;
        }

        @Override // android.gsi.IGsiService
        public String getInstalledGsiImageDir() throws RemoteException {
            return null;
        }

        @Override // android.gsi.IGsiService
        public int beginGsiInstall(GsiInstallParams params) throws RemoteException {
            return 0;
        }

        @Override // android.gsi.IGsiService
        public int wipeGsiUserdata() throws RemoteException {
            return 0;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IGsiService {
        private static final String DESCRIPTOR = "android.gsi.IGsiService";
        static final int TRANSACTION_beginGsiInstall = 16;
        static final int TRANSACTION_cancelGsiInstall = 7;
        static final int TRANSACTION_commitGsiChunkFromMemory = 4;
        static final int TRANSACTION_commitGsiChunkFromStream = 2;
        static final int TRANSACTION_disableGsiInstall = 10;
        static final int TRANSACTION_getGsiBootStatus = 14;
        static final int TRANSACTION_getInstallProgress = 3;
        static final int TRANSACTION_getInstalledGsiImageDir = 15;
        static final int TRANSACTION_getUserdataImageSize = 11;
        static final int TRANSACTION_isGsiEnabled = 6;
        static final int TRANSACTION_isGsiInstallInProgress = 8;
        static final int TRANSACTION_isGsiInstalled = 13;
        static final int TRANSACTION_isGsiRunning = 12;
        static final int TRANSACTION_removeGsiInstall = 9;
        static final int TRANSACTION_setGsiBootable = 5;
        static final int TRANSACTION_startGsiInstall = 1;
        static final int TRANSACTION_wipeGsiUserdata = 17;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IGsiService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IGsiService)) {
                return new Proxy(obj);
            }
            return (IGsiService) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "startGsiInstall";
                case 2:
                    return "commitGsiChunkFromStream";
                case 3:
                    return "getInstallProgress";
                case 4:
                    return "commitGsiChunkFromMemory";
                case 5:
                    return "setGsiBootable";
                case 6:
                    return "isGsiEnabled";
                case 7:
                    return "cancelGsiInstall";
                case 8:
                    return "isGsiInstallInProgress";
                case 9:
                    return "removeGsiInstall";
                case 10:
                    return "disableGsiInstall";
                case 11:
                    return "getUserdataImageSize";
                case 12:
                    return "isGsiRunning";
                case 13:
                    return "isGsiInstalled";
                case 14:
                    return "getGsiBootStatus";
                case 15:
                    return "getInstalledGsiImageDir";
                case 16:
                    return "beginGsiInstall";
                case 17:
                    return "wipeGsiUserdata";
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
            ParcelFileDescriptor _arg0;
            GsiInstallParams _arg02;
            if (code != 1598968902) {
                boolean _arg03 = false;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        int _result = startGsiInstall(data.readLong(), data.readLong(), data.readInt() != 0);
                        reply.writeNoException();
                        reply.writeInt(_result);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = ParcelFileDescriptor.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        boolean commitGsiChunkFromStream = commitGsiChunkFromStream(_arg0, data.readLong());
                        reply.writeNoException();
                        reply.writeInt(commitGsiChunkFromStream ? 1 : 0);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        GsiProgress _result2 = getInstallProgress();
                        reply.writeNoException();
                        if (_result2 != null) {
                            reply.writeInt(1);
                            _result2.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        boolean commitGsiChunkFromMemory = commitGsiChunkFromMemory(data.createByteArray());
                        reply.writeNoException();
                        reply.writeInt(commitGsiChunkFromMemory ? 1 : 0);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg03 = true;
                        }
                        int _result3 = setGsiBootable(_arg03);
                        reply.writeNoException();
                        reply.writeInt(_result3);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isGsiEnabled = isGsiEnabled();
                        reply.writeNoException();
                        reply.writeInt(isGsiEnabled ? 1 : 0);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        boolean cancelGsiInstall = cancelGsiInstall();
                        reply.writeNoException();
                        reply.writeInt(cancelGsiInstall ? 1 : 0);
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isGsiInstallInProgress = isGsiInstallInProgress();
                        reply.writeNoException();
                        reply.writeInt(isGsiInstallInProgress ? 1 : 0);
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        boolean removeGsiInstall = removeGsiInstall();
                        reply.writeNoException();
                        reply.writeInt(removeGsiInstall ? 1 : 0);
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        boolean disableGsiInstall = disableGsiInstall();
                        reply.writeNoException();
                        reply.writeInt(disableGsiInstall ? 1 : 0);
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        long _result4 = getUserdataImageSize();
                        reply.writeNoException();
                        reply.writeLong(_result4);
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isGsiRunning = isGsiRunning();
                        reply.writeNoException();
                        reply.writeInt(isGsiRunning ? 1 : 0);
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isGsiInstalled = isGsiInstalled();
                        reply.writeNoException();
                        reply.writeInt(isGsiInstalled ? 1 : 0);
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        int _result5 = getGsiBootStatus();
                        reply.writeNoException();
                        reply.writeInt(_result5);
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        String _result6 = getInstalledGsiImageDir();
                        reply.writeNoException();
                        reply.writeString(_result6);
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = GsiInstallParams.CREATOR.createFromParcel(data);
                        } else {
                            _arg02 = null;
                        }
                        int _result7 = beginGsiInstall(_arg02);
                        reply.writeNoException();
                        reply.writeInt(_result7);
                        return true;
                    case 17:
                        data.enforceInterface(DESCRIPTOR);
                        int _result8 = wipeGsiUserdata();
                        reply.writeNoException();
                        reply.writeInt(_result8);
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
        public static class Proxy implements IGsiService {
            public static IGsiService sDefaultImpl;
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

            @Override // android.gsi.IGsiService
            public int startGsiInstall(long gsiSize, long userdataSize, boolean wipeUserdata) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(gsiSize);
                    _data.writeLong(userdataSize);
                    _data.writeInt(wipeUserdata ? 1 : 0);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().startGsiInstall(gsiSize, userdataSize, wipeUserdata);
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

            @Override // android.gsi.IGsiService
            public boolean commitGsiChunkFromStream(ParcelFileDescriptor stream, long bytes) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (stream != null) {
                        _data.writeInt(1);
                        stream.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeLong(bytes);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().commitGsiChunkFromStream(stream, bytes);
                    }
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.gsi.IGsiService
            public GsiProgress getInstallProgress() throws RemoteException {
                GsiProgress _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getInstallProgress();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = GsiProgress.CREATOR.createFromParcel(_reply);
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

            @Override // android.gsi.IGsiService
            public boolean commitGsiChunkFromMemory(byte[] bytes) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeByteArray(bytes);
                    boolean _result = false;
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().commitGsiChunkFromMemory(bytes);
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

            @Override // android.gsi.IGsiService
            public int setGsiBootable(boolean oneShot) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(oneShot ? 1 : 0);
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setGsiBootable(oneShot);
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

            @Override // android.gsi.IGsiService
            public boolean isGsiEnabled() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(6, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isGsiEnabled();
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

            @Override // android.gsi.IGsiService
            public boolean cancelGsiInstall() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(7, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().cancelGsiInstall();
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

            @Override // android.gsi.IGsiService
            public boolean isGsiInstallInProgress() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(8, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isGsiInstallInProgress();
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

            @Override // android.gsi.IGsiService
            public boolean removeGsiInstall() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(9, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().removeGsiInstall();
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

            @Override // android.gsi.IGsiService
            public boolean disableGsiInstall() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(10, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().disableGsiInstall();
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

            @Override // android.gsi.IGsiService
            public long getUserdataImageSize() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(11, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getUserdataImageSize();
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

            @Override // android.gsi.IGsiService
            public boolean isGsiRunning() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(12, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isGsiRunning();
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

            @Override // android.gsi.IGsiService
            public boolean isGsiInstalled() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(13, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isGsiInstalled();
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

            @Override // android.gsi.IGsiService
            public int getGsiBootStatus() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(14, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getGsiBootStatus();
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

            @Override // android.gsi.IGsiService
            public String getInstalledGsiImageDir() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(15, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getInstalledGsiImageDir();
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

            @Override // android.gsi.IGsiService
            public int beginGsiInstall(GsiInstallParams params) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (params != null) {
                        _data.writeInt(1);
                        params.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(16, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().beginGsiInstall(params);
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

            @Override // android.gsi.IGsiService
            public int wipeGsiUserdata() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(17, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().wipeGsiUserdata();
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
        }

        public static boolean setDefaultImpl(IGsiService impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IGsiService getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
