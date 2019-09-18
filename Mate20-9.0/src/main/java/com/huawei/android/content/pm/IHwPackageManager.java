package com.huawei.android.content.pm;

import android.os.Binder;
import android.os.IBackupSessionCallback;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import java.util.List;

public interface IHwPackageManager extends IInterface {

    public static abstract class Stub extends Binder implements IHwPackageManager {
        private static final String DESCRIPTOR = "com.huawei.android.content.pm.IHwPackageManager";
        static final int TRANSACTION_executeBackupTask = 13;
        static final int TRANSACTION_finishBackupSession = 14;
        static final int TRANSACTION_getAppUseNotchMode = 2;
        static final int TRANSACTION_getApplicationAspectRatio = 8;
        static final int TRANSACTION_getApplicationMaxAspectRatio = 6;
        static final int TRANSACTION_getHwPublicityAppList = 10;
        static final int TRANSACTION_getHwPublicityAppParcelFileDescriptor = 11;
        static final int TRANSACTION_getMapleEnableFlag = 25;
        static final int TRANSACTION_getMspesOEMConfig = 23;
        static final int TRANSACTION_getPreinstalledApkList = 9;
        static final int TRANSACTION_getResourcePackageNameByIcon = 15;
        static final int TRANSACTION_getScanInstallList = 17;
        static final int TRANSACTION_isMapleEnv = 20;
        static final int TRANSACTION_isPerfOptEnable = 1;
        static final int TRANSACTION_pmInstallHwTheme = 19;
        static final int TRANSACTION_readMspesFile = 21;
        static final int TRANSACTION_scanInstallApk = 16;
        static final int TRANSACTION_setAppCanUninstall = 4;
        static final int TRANSACTION_setAppUseNotchMode = 3;
        static final int TRANSACTION_setApplicationAspectRatio = 7;
        static final int TRANSACTION_setApplicationMaxAspectRatio = 5;
        static final int TRANSACTION_setHdbKey = 18;
        static final int TRANSACTION_setMapleEnableFlag = 26;
        static final int TRANSACTION_startBackupSession = 12;
        static final int TRANSACTION_updateMspesOEMConfig = 24;
        static final int TRANSACTION_writeMspesFile = 22;

        private static class Proxy implements IHwPackageManager {
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

            public boolean isPerfOptEnable(String packageName, int optType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(optType);
                    boolean _result = false;
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getAppUseNotchMode(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setAppUseNotchMode(String packageName, int mode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(mode);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setAppCanUninstall(String packageName, boolean canUninstall) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(canUninstall);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setApplicationMaxAspectRatio(String packageName, float ar) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeFloat(ar);
                    boolean _result = false;
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public float getApplicationMaxAspectRatio(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readFloat();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setApplicationAspectRatio(String packageName, String aspectName, float ar) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeString(aspectName);
                    _data.writeFloat(ar);
                    boolean _result = false;
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public float getApplicationAspectRatio(String packageName, String aspectName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeString(aspectName);
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readFloat();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<String> getPreinstalledApkList() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(9, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createStringArrayList();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<String> getHwPublicityAppList() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(10, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createStringArrayList();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ParcelFileDescriptor getHwPublicityAppParcelFileDescriptor() throws RemoteException {
                ParcelFileDescriptor _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(11, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (ParcelFileDescriptor) ParcelFileDescriptor.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int startBackupSession(IBackupSessionCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    this.mRemote.transact(12, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int executeBackupTask(int sessionId, String taskCmd) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(sessionId);
                    _data.writeString(taskCmd);
                    this.mRemote.transact(13, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int finishBackupSession(int sessionId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(sessionId);
                    this.mRemote.transact(14, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getResourcePackageNameByIcon(String pkgName, int icon, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkgName);
                    _data.writeInt(icon);
                    _data.writeInt(userId);
                    this.mRemote.transact(15, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean scanInstallApk(String apkFile) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(apkFile);
                    boolean _result = false;
                    this.mRemote.transact(16, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<String> getScanInstallList() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(17, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createStringArrayList();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setHdbKey(String key) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(key);
                    this.mRemote.transact(18, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean pmInstallHwTheme(String themePath, boolean setwallpaper, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(themePath);
                    _data.writeInt(setwallpaper);
                    _data.writeInt(userId);
                    boolean _result = false;
                    this.mRemote.transact(19, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isMapleEnv() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(20, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String readMspesFile(String fileName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(fileName);
                    this.mRemote.transact(21, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean writeMspesFile(String fileName, String content) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(fileName);
                    _data.writeString(content);
                    boolean _result = false;
                    this.mRemote.transact(22, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getMspesOEMConfig() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(23, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int updateMspesOEMConfig(String src) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(src);
                    this.mRemote.transact(24, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean getMapleEnableFlag(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    boolean _result = false;
                    this.mRemote.transact(25, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setMapleEnableFlag(String packageName, boolean flag) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(flag);
                    this.mRemote.transact(26, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IHwPackageManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHwPackageManager)) {
                return new Proxy(obj);
            }
            return (IHwPackageManager) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code != 1598968902) {
                boolean _arg1 = false;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        boolean _result = isPerfOptEnable(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        int _result2 = getAppUseNotchMode(data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result2);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        setAppUseNotchMode(data.readString(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg0 = data.readString();
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        setAppCanUninstall(_arg0, _arg1);
                        reply.writeNoException();
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        boolean _result3 = setApplicationMaxAspectRatio(data.readString(), data.readFloat());
                        reply.writeNoException();
                        reply.writeInt(_result3);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        float _result4 = getApplicationMaxAspectRatio(data.readString());
                        reply.writeNoException();
                        reply.writeFloat(_result4);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        boolean _result5 = setApplicationAspectRatio(data.readString(), data.readString(), data.readFloat());
                        reply.writeNoException();
                        reply.writeInt(_result5);
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        float _result6 = getApplicationAspectRatio(data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeFloat(_result6);
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        List<String> _result7 = getPreinstalledApkList();
                        reply.writeNoException();
                        reply.writeStringList(_result7);
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        List<String> _result8 = getHwPublicityAppList();
                        reply.writeNoException();
                        reply.writeStringList(_result8);
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        ParcelFileDescriptor _result9 = getHwPublicityAppParcelFileDescriptor();
                        reply.writeNoException();
                        if (_result9 != null) {
                            reply.writeInt(1);
                            _result9.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        int _result10 = startBackupSession(IBackupSessionCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(_result10);
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        int _result11 = executeBackupTask(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result11);
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        int _result12 = finishBackupSession(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result12);
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        String _result13 = getResourcePackageNameByIcon(data.readString(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeString(_result13);
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        boolean _result14 = scanInstallApk(data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result14);
                        return true;
                    case 17:
                        data.enforceInterface(DESCRIPTOR);
                        List<String> _result15 = getScanInstallList();
                        reply.writeNoException();
                        reply.writeStringList(_result15);
                        return true;
                    case 18:
                        data.enforceInterface(DESCRIPTOR);
                        setHdbKey(data.readString());
                        reply.writeNoException();
                        return true;
                    case 19:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg02 = data.readString();
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        boolean _result16 = pmInstallHwTheme(_arg02, _arg1, data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result16);
                        return true;
                    case 20:
                        data.enforceInterface(DESCRIPTOR);
                        boolean _result17 = isMapleEnv();
                        reply.writeNoException();
                        reply.writeInt(_result17);
                        return true;
                    case 21:
                        data.enforceInterface(DESCRIPTOR);
                        String _result18 = readMspesFile(data.readString());
                        reply.writeNoException();
                        reply.writeString(_result18);
                        return true;
                    case 22:
                        data.enforceInterface(DESCRIPTOR);
                        boolean _result19 = writeMspesFile(data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result19);
                        return true;
                    case 23:
                        data.enforceInterface(DESCRIPTOR);
                        String _result20 = getMspesOEMConfig();
                        reply.writeNoException();
                        reply.writeString(_result20);
                        return true;
                    case 24:
                        data.enforceInterface(DESCRIPTOR);
                        int _result21 = updateMspesOEMConfig(data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result21);
                        return true;
                    case 25:
                        data.enforceInterface(DESCRIPTOR);
                        boolean _result22 = getMapleEnableFlag(data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result22);
                        return true;
                    case 26:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg03 = data.readString();
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        setMapleEnableFlag(_arg03, _arg1);
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
    }

    int executeBackupTask(int i, String str) throws RemoteException;

    int finishBackupSession(int i) throws RemoteException;

    int getAppUseNotchMode(String str) throws RemoteException;

    float getApplicationAspectRatio(String str, String str2) throws RemoteException;

    float getApplicationMaxAspectRatio(String str) throws RemoteException;

    List<String> getHwPublicityAppList() throws RemoteException;

    ParcelFileDescriptor getHwPublicityAppParcelFileDescriptor() throws RemoteException;

    boolean getMapleEnableFlag(String str) throws RemoteException;

    String getMspesOEMConfig() throws RemoteException;

    List<String> getPreinstalledApkList() throws RemoteException;

    String getResourcePackageNameByIcon(String str, int i, int i2) throws RemoteException;

    List<String> getScanInstallList() throws RemoteException;

    boolean isMapleEnv() throws RemoteException;

    boolean isPerfOptEnable(String str, int i) throws RemoteException;

    boolean pmInstallHwTheme(String str, boolean z, int i) throws RemoteException;

    String readMspesFile(String str) throws RemoteException;

    boolean scanInstallApk(String str) throws RemoteException;

    void setAppCanUninstall(String str, boolean z) throws RemoteException;

    void setAppUseNotchMode(String str, int i) throws RemoteException;

    boolean setApplicationAspectRatio(String str, String str2, float f) throws RemoteException;

    boolean setApplicationMaxAspectRatio(String str, float f) throws RemoteException;

    void setHdbKey(String str) throws RemoteException;

    void setMapleEnableFlag(String str, boolean z) throws RemoteException;

    int startBackupSession(IBackupSessionCallback iBackupSessionCallback) throws RemoteException;

    int updateMspesOEMConfig(String str) throws RemoteException;

    boolean writeMspesFile(String str, String str2) throws RemoteException;
}
