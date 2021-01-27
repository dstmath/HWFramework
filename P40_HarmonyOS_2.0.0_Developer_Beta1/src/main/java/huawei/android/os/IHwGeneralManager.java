package huawei.android.os;

import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Messenger;
import android.os.Parcel;
import android.os.RemoteException;

public interface IHwGeneralManager extends IInterface {
    int addSdCardUserKeyAuth(int i, int i2, byte[] bArr, byte[] bArr2) throws RemoteException;

    int backupSecretkey() throws RemoteException;

    int clearSDLockPassword() throws RemoteException;

    int doSdcardCheckRW() throws RemoteException;

    void eraseSDLock() throws RemoteException;

    int forceIdle() throws RemoteException;

    String getDeviceId(int i) throws RemoteException;

    String[] getIsolatedStorageApps(int i) throws RemoteException;

    int getLocalDevStat(int i) throws RemoteException;

    long getPartitionInfo(String str, int i) throws RemoteException;

    float getPressureLimit() throws RemoteException;

    String getSDCardId() throws RemoteException;

    int getSDLockState() throws RemoteException;

    Messenger getTestService() throws RemoteException;

    String getTouchWeightValue() throws RemoteException;

    boolean hasHaptic(Uri uri) throws RemoteException;

    boolean isCurveScreen() throws RemoteException;

    boolean isIsolatedStorageApp(int i, String str) throws RemoteException;

    boolean isPlaying(String str) throws RemoteException;

    boolean isSupportForce() throws RemoteException;

    int isSupportedCifs() throws RemoteException;

    boolean mkDataDir(String str) throws RemoteException;

    String mountCifs(String str, String str2, IBinder iBinder) throws RemoteException;

    void pausePlayEffect(String str) throws RemoteException;

    void playIvtEffect(String str) throws RemoteException;

    int readProtectArea(String str, int i, String[] strArr, int[] iArr) throws RemoteException;

    void resetTouchWeight() throws RemoteException;

    void resumePausedEffect(String str) throws RemoteException;

    int setSDLockPassword(String str) throws RemoteException;

    int setSdCardCryptdEnable(boolean z, String str) throws RemoteException;

    void startFileBackup() throws RemoteException;

    boolean startHaptic(int i, int i2, Uri uri) throws RemoteException;

    void stopHaptic() throws RemoteException;

    void stopPlayEffect() throws RemoteException;

    boolean supportHwPush() throws RemoteException;

    int unlockSDCard(String str) throws RemoteException;

    int unlockSdCardKey(int i, int i2, byte[] bArr, byte[] bArr2) throws RemoteException;

    void unmountCifs(String str) throws RemoteException;

    int writeProtectArea(String str, int i, String str2, int[] iArr) throws RemoteException;

    public static class Default implements IHwGeneralManager {
        @Override // huawei.android.os.IHwGeneralManager
        public int setSDLockPassword(String pw) throws RemoteException {
            return 0;
        }

        @Override // huawei.android.os.IHwGeneralManager
        public int clearSDLockPassword() throws RemoteException {
            return 0;
        }

        @Override // huawei.android.os.IHwGeneralManager
        public int unlockSDCard(String pw) throws RemoteException {
            return 0;
        }

        @Override // huawei.android.os.IHwGeneralManager
        public void eraseSDLock() throws RemoteException {
        }

        @Override // huawei.android.os.IHwGeneralManager
        public int getSDLockState() throws RemoteException {
            return 0;
        }

        @Override // huawei.android.os.IHwGeneralManager
        public String getSDCardId() throws RemoteException {
            return null;
        }

        @Override // huawei.android.os.IHwGeneralManager
        public void startFileBackup() throws RemoteException {
        }

        @Override // huawei.android.os.IHwGeneralManager
        public int forceIdle() throws RemoteException {
            return 0;
        }

        @Override // huawei.android.os.IHwGeneralManager
        public boolean isSupportForce() throws RemoteException {
            return false;
        }

        @Override // huawei.android.os.IHwGeneralManager
        public float getPressureLimit() throws RemoteException {
            return 0.0f;
        }

        @Override // huawei.android.os.IHwGeneralManager
        public boolean isCurveScreen() throws RemoteException {
            return false;
        }

        @Override // huawei.android.os.IHwGeneralManager
        public void playIvtEffect(String effectName) throws RemoteException {
        }

        @Override // huawei.android.os.IHwGeneralManager
        public void stopPlayEffect() throws RemoteException {
        }

        @Override // huawei.android.os.IHwGeneralManager
        public void pausePlayEffect(String effectName) throws RemoteException {
        }

        @Override // huawei.android.os.IHwGeneralManager
        public void resumePausedEffect(String effectName) throws RemoteException {
        }

        @Override // huawei.android.os.IHwGeneralManager
        public boolean isPlaying(String effectName) throws RemoteException {
            return false;
        }

        @Override // huawei.android.os.IHwGeneralManager
        public boolean startHaptic(int callerID, int ringtoneType, Uri uri) throws RemoteException {
            return false;
        }

        @Override // huawei.android.os.IHwGeneralManager
        public boolean hasHaptic(Uri uri) throws RemoteException {
            return false;
        }

        @Override // huawei.android.os.IHwGeneralManager
        public void stopHaptic() throws RemoteException {
        }

        @Override // huawei.android.os.IHwGeneralManager
        public void resetTouchWeight() throws RemoteException {
        }

        @Override // huawei.android.os.IHwGeneralManager
        public String getTouchWeightValue() throws RemoteException {
            return null;
        }

        @Override // huawei.android.os.IHwGeneralManager
        public boolean mkDataDir(String path) throws RemoteException {
            return false;
        }

        @Override // huawei.android.os.IHwGeneralManager
        public Messenger getTestService() throws RemoteException {
            return null;
        }

        @Override // huawei.android.os.IHwGeneralManager
        public int readProtectArea(String optItem, int readBufLen, String[] readBuf, int[] errorNum) throws RemoteException {
            return 0;
        }

        @Override // huawei.android.os.IHwGeneralManager
        public int writeProtectArea(String optItem, int writeLen, String writeBuf, int[] errorNum) throws RemoteException {
            return 0;
        }

        @Override // huawei.android.os.IHwGeneralManager
        public int setSdCardCryptdEnable(boolean enable, String volId) throws RemoteException {
            return 0;
        }

        @Override // huawei.android.os.IHwGeneralManager
        public int unlockSdCardKey(int userId, int serialNumber, byte[] token, byte[] secret) throws RemoteException {
            return 0;
        }

        @Override // huawei.android.os.IHwGeneralManager
        public int addSdCardUserKeyAuth(int userId, int serialNumber, byte[] token, byte[] secret) throws RemoteException {
            return 0;
        }

        @Override // huawei.android.os.IHwGeneralManager
        public int backupSecretkey() throws RemoteException {
            return 0;
        }

        @Override // huawei.android.os.IHwGeneralManager
        public boolean supportHwPush() throws RemoteException {
            return false;
        }

        @Override // huawei.android.os.IHwGeneralManager
        public long getPartitionInfo(String partitionName, int infoType) throws RemoteException {
            return 0;
        }

        @Override // huawei.android.os.IHwGeneralManager
        public String mountCifs(String source, String option, IBinder binder) throws RemoteException {
            return null;
        }

        @Override // huawei.android.os.IHwGeneralManager
        public void unmountCifs(String mountPoint) throws RemoteException {
        }

        @Override // huawei.android.os.IHwGeneralManager
        public int isSupportedCifs() throws RemoteException {
            return 0;
        }

        @Override // huawei.android.os.IHwGeneralManager
        public int getLocalDevStat(int dev) throws RemoteException {
            return 0;
        }

        @Override // huawei.android.os.IHwGeneralManager
        public String getDeviceId(int dev) throws RemoteException {
            return null;
        }

        @Override // huawei.android.os.IHwGeneralManager
        public int doSdcardCheckRW() throws RemoteException {
            return 0;
        }

        @Override // huawei.android.os.IHwGeneralManager
        public boolean isIsolatedStorageApp(int uid, String packageName) throws RemoteException {
            return false;
        }

        @Override // huawei.android.os.IHwGeneralManager
        public String[] getIsolatedStorageApps(int excludeFlag) throws RemoteException {
            return null;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IHwGeneralManager {
        private static final String DESCRIPTOR = "huawei.android.os.IHwGeneralManager";
        static final int TRANSACTION_addSdCardUserKeyAuth = 28;
        static final int TRANSACTION_backupSecretkey = 29;
        static final int TRANSACTION_clearSDLockPassword = 2;
        static final int TRANSACTION_doSdcardCheckRW = 37;
        static final int TRANSACTION_eraseSDLock = 4;
        static final int TRANSACTION_forceIdle = 8;
        static final int TRANSACTION_getDeviceId = 36;
        static final int TRANSACTION_getIsolatedStorageApps = 39;
        static final int TRANSACTION_getLocalDevStat = 35;
        static final int TRANSACTION_getPartitionInfo = 31;
        static final int TRANSACTION_getPressureLimit = 10;
        static final int TRANSACTION_getSDCardId = 6;
        static final int TRANSACTION_getSDLockState = 5;
        static final int TRANSACTION_getTestService = 23;
        static final int TRANSACTION_getTouchWeightValue = 21;
        static final int TRANSACTION_hasHaptic = 18;
        static final int TRANSACTION_isCurveScreen = 11;
        static final int TRANSACTION_isIsolatedStorageApp = 38;
        static final int TRANSACTION_isPlaying = 16;
        static final int TRANSACTION_isSupportForce = 9;
        static final int TRANSACTION_isSupportedCifs = 34;
        static final int TRANSACTION_mkDataDir = 22;
        static final int TRANSACTION_mountCifs = 32;
        static final int TRANSACTION_pausePlayEffect = 14;
        static final int TRANSACTION_playIvtEffect = 12;
        static final int TRANSACTION_readProtectArea = 24;
        static final int TRANSACTION_resetTouchWeight = 20;
        static final int TRANSACTION_resumePausedEffect = 15;
        static final int TRANSACTION_setSDLockPassword = 1;
        static final int TRANSACTION_setSdCardCryptdEnable = 26;
        static final int TRANSACTION_startFileBackup = 7;
        static final int TRANSACTION_startHaptic = 17;
        static final int TRANSACTION_stopHaptic = 19;
        static final int TRANSACTION_stopPlayEffect = 13;
        static final int TRANSACTION_supportHwPush = 30;
        static final int TRANSACTION_unlockSDCard = 3;
        static final int TRANSACTION_unlockSdCardKey = 27;
        static final int TRANSACTION_unmountCifs = 33;
        static final int TRANSACTION_writeProtectArea = 25;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IHwGeneralManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHwGeneralManager)) {
                return new Proxy(obj);
            }
            return (IHwGeneralManager) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            Uri _arg2;
            Uri _arg0;
            String[] _arg22;
            int[] _arg3;
            int[] _arg32;
            if (code != 1598968902) {
                boolean _arg02 = false;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        int _result = setSDLockPassword(data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        int _result2 = clearSDLockPassword();
                        reply.writeNoException();
                        reply.writeInt(_result2);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        int _result3 = unlockSDCard(data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result3);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        eraseSDLock();
                        reply.writeNoException();
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        int _result4 = getSDLockState();
                        reply.writeNoException();
                        reply.writeInt(_result4);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        String _result5 = getSDCardId();
                        reply.writeNoException();
                        reply.writeString(_result5);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        startFileBackup();
                        reply.writeNoException();
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        int _result6 = forceIdle();
                        reply.writeNoException();
                        reply.writeInt(_result6);
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isSupportForce = isSupportForce();
                        reply.writeNoException();
                        reply.writeInt(isSupportForce ? 1 : 0);
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        float _result7 = getPressureLimit();
                        reply.writeNoException();
                        reply.writeFloat(_result7);
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isCurveScreen = isCurveScreen();
                        reply.writeNoException();
                        reply.writeInt(isCurveScreen ? 1 : 0);
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        playIvtEffect(data.readString());
                        reply.writeNoException();
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        stopPlayEffect();
                        reply.writeNoException();
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        pausePlayEffect(data.readString());
                        reply.writeNoException();
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        resumePausedEffect(data.readString());
                        reply.writeNoException();
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isPlaying = isPlaying(data.readString());
                        reply.writeNoException();
                        reply.writeInt(isPlaying ? 1 : 0);
                        return true;
                    case 17:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg03 = data.readInt();
                        int _arg1 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg2 = (Uri) Uri.CREATOR.createFromParcel(data);
                        } else {
                            _arg2 = null;
                        }
                        boolean startHaptic = startHaptic(_arg03, _arg1, _arg2);
                        reply.writeNoException();
                        reply.writeInt(startHaptic ? 1 : 0);
                        return true;
                    case 18:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (Uri) Uri.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        boolean hasHaptic = hasHaptic(_arg0);
                        reply.writeNoException();
                        reply.writeInt(hasHaptic ? 1 : 0);
                        return true;
                    case 19:
                        data.enforceInterface(DESCRIPTOR);
                        stopHaptic();
                        reply.writeNoException();
                        return true;
                    case 20:
                        data.enforceInterface(DESCRIPTOR);
                        resetTouchWeight();
                        reply.writeNoException();
                        return true;
                    case 21:
                        data.enforceInterface(DESCRIPTOR);
                        String _result8 = getTouchWeightValue();
                        reply.writeNoException();
                        reply.writeString(_result8);
                        return true;
                    case 22:
                        data.enforceInterface(DESCRIPTOR);
                        boolean mkDataDir = mkDataDir(data.readString());
                        reply.writeNoException();
                        reply.writeInt(mkDataDir ? 1 : 0);
                        return true;
                    case 23:
                        data.enforceInterface(DESCRIPTOR);
                        Messenger _result9 = getTestService();
                        reply.writeNoException();
                        if (_result9 != null) {
                            reply.writeInt(1);
                            _result9.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 24:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg04 = data.readString();
                        int _arg12 = data.readInt();
                        int _arg2_length = data.readInt();
                        if (_arg2_length < 0) {
                            _arg22 = null;
                        } else {
                            _arg22 = new String[_arg2_length];
                        }
                        int _arg3_length = data.readInt();
                        if (_arg3_length < 0) {
                            _arg3 = null;
                        } else {
                            _arg3 = new int[_arg3_length];
                        }
                        int _result10 = readProtectArea(_arg04, _arg12, _arg22, _arg3);
                        reply.writeNoException();
                        reply.writeInt(_result10);
                        reply.writeStringArray(_arg22);
                        reply.writeIntArray(_arg3);
                        return true;
                    case 25:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg05 = data.readString();
                        int _arg13 = data.readInt();
                        String _arg23 = data.readString();
                        int _arg3_length2 = data.readInt();
                        if (_arg3_length2 < 0) {
                            _arg32 = null;
                        } else {
                            _arg32 = new int[_arg3_length2];
                        }
                        int _result11 = writeProtectArea(_arg05, _arg13, _arg23, _arg32);
                        reply.writeNoException();
                        reply.writeInt(_result11);
                        reply.writeIntArray(_arg32);
                        return true;
                    case 26:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = true;
                        }
                        int _result12 = setSdCardCryptdEnable(_arg02, data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result12);
                        return true;
                    case 27:
                        data.enforceInterface(DESCRIPTOR);
                        int _result13 = unlockSdCardKey(data.readInt(), data.readInt(), data.createByteArray(), data.createByteArray());
                        reply.writeNoException();
                        reply.writeInt(_result13);
                        return true;
                    case 28:
                        data.enforceInterface(DESCRIPTOR);
                        int _result14 = addSdCardUserKeyAuth(data.readInt(), data.readInt(), data.createByteArray(), data.createByteArray());
                        reply.writeNoException();
                        reply.writeInt(_result14);
                        return true;
                    case 29:
                        data.enforceInterface(DESCRIPTOR);
                        int _result15 = backupSecretkey();
                        reply.writeNoException();
                        reply.writeInt(_result15);
                        return true;
                    case 30:
                        data.enforceInterface(DESCRIPTOR);
                        boolean supportHwPush = supportHwPush();
                        reply.writeNoException();
                        reply.writeInt(supportHwPush ? 1 : 0);
                        return true;
                    case 31:
                        data.enforceInterface(DESCRIPTOR);
                        long _result16 = getPartitionInfo(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeLong(_result16);
                        return true;
                    case 32:
                        data.enforceInterface(DESCRIPTOR);
                        String _result17 = mountCifs(data.readString(), data.readString(), data.readStrongBinder());
                        reply.writeNoException();
                        reply.writeString(_result17);
                        return true;
                    case 33:
                        data.enforceInterface(DESCRIPTOR);
                        unmountCifs(data.readString());
                        reply.writeNoException();
                        return true;
                    case 34:
                        data.enforceInterface(DESCRIPTOR);
                        int _result18 = isSupportedCifs();
                        reply.writeNoException();
                        reply.writeInt(_result18);
                        return true;
                    case 35:
                        data.enforceInterface(DESCRIPTOR);
                        int _result19 = getLocalDevStat(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result19);
                        return true;
                    case 36:
                        data.enforceInterface(DESCRIPTOR);
                        String _result20 = getDeviceId(data.readInt());
                        reply.writeNoException();
                        reply.writeString(_result20);
                        return true;
                    case 37:
                        data.enforceInterface(DESCRIPTOR);
                        int _result21 = doSdcardCheckRW();
                        reply.writeNoException();
                        reply.writeInt(_result21);
                        return true;
                    case 38:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isIsolatedStorageApp = isIsolatedStorageApp(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(isIsolatedStorageApp ? 1 : 0);
                        return true;
                    case 39:
                        data.enforceInterface(DESCRIPTOR);
                        String[] _result22 = getIsolatedStorageApps(data.readInt());
                        reply.writeNoException();
                        reply.writeStringArray(_result22);
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
        public static class Proxy implements IHwGeneralManager {
            public static IHwGeneralManager sDefaultImpl;
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

            @Override // huawei.android.os.IHwGeneralManager
            public int setSDLockPassword(String pw) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pw);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setSDLockPassword(pw);
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

            @Override // huawei.android.os.IHwGeneralManager
            public int clearSDLockPassword() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().clearSDLockPassword();
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

            @Override // huawei.android.os.IHwGeneralManager
            public int unlockSDCard(String pw) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pw);
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().unlockSDCard(pw);
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

            @Override // huawei.android.os.IHwGeneralManager
            public void eraseSDLock() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(4, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().eraseSDLock();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.os.IHwGeneralManager
            public int getSDLockState() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getSDLockState();
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

            @Override // huawei.android.os.IHwGeneralManager
            public String getSDCardId() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(6, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getSDCardId();
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

            @Override // huawei.android.os.IHwGeneralManager
            public void startFileBackup() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(7, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().startFileBackup();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.os.IHwGeneralManager
            public int forceIdle() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(8, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().forceIdle();
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

            @Override // huawei.android.os.IHwGeneralManager
            public boolean isSupportForce() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(9, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isSupportForce();
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

            @Override // huawei.android.os.IHwGeneralManager
            public float getPressureLimit() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(10, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getPressureLimit();
                    }
                    _reply.readException();
                    float _result = _reply.readFloat();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.os.IHwGeneralManager
            public boolean isCurveScreen() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(11, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isCurveScreen();
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

            @Override // huawei.android.os.IHwGeneralManager
            public void playIvtEffect(String effectName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(effectName);
                    if (this.mRemote.transact(12, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().playIvtEffect(effectName);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.os.IHwGeneralManager
            public void stopPlayEffect() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(13, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().stopPlayEffect();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.os.IHwGeneralManager
            public void pausePlayEffect(String effectName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(effectName);
                    if (this.mRemote.transact(14, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().pausePlayEffect(effectName);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.os.IHwGeneralManager
            public void resumePausedEffect(String effectName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(effectName);
                    if (this.mRemote.transact(15, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().resumePausedEffect(effectName);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.os.IHwGeneralManager
            public boolean isPlaying(String effectName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(effectName);
                    boolean _result = false;
                    if (!this.mRemote.transact(16, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isPlaying(effectName);
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

            @Override // huawei.android.os.IHwGeneralManager
            public boolean startHaptic(int callerID, int ringtoneType, Uri uri) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(callerID);
                    _data.writeInt(ringtoneType);
                    boolean _result = true;
                    if (uri != null) {
                        _data.writeInt(1);
                        uri.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(17, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().startHaptic(callerID, ringtoneType, uri);
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

            @Override // huawei.android.os.IHwGeneralManager
            public boolean hasHaptic(Uri uri) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (uri != null) {
                        _data.writeInt(1);
                        uri.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(18, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().hasHaptic(uri);
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

            @Override // huawei.android.os.IHwGeneralManager
            public void stopHaptic() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(19, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().stopHaptic();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.os.IHwGeneralManager
            public void resetTouchWeight() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(20, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().resetTouchWeight();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.os.IHwGeneralManager
            public String getTouchWeightValue() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(21, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getTouchWeightValue();
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

            @Override // huawei.android.os.IHwGeneralManager
            public boolean mkDataDir(String path) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(path);
                    boolean _result = false;
                    if (!this.mRemote.transact(22, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().mkDataDir(path);
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

            @Override // huawei.android.os.IHwGeneralManager
            public Messenger getTestService() throws RemoteException {
                Messenger _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(23, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getTestService();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (Messenger) Messenger.CREATOR.createFromParcel(_reply);
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

            @Override // huawei.android.os.IHwGeneralManager
            public int readProtectArea(String optItem, int readBufLen, String[] readBuf, int[] errorNum) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(optItem);
                    _data.writeInt(readBufLen);
                    if (readBuf == null) {
                        _data.writeInt(-1);
                    } else {
                        _data.writeInt(readBuf.length);
                    }
                    if (errorNum == null) {
                        _data.writeInt(-1);
                    } else {
                        _data.writeInt(errorNum.length);
                    }
                    if (!this.mRemote.transact(24, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().readProtectArea(optItem, readBufLen, readBuf, errorNum);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.readStringArray(readBuf);
                    _reply.readIntArray(errorNum);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.os.IHwGeneralManager
            public int writeProtectArea(String optItem, int writeLen, String writeBuf, int[] errorNum) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(optItem);
                    _data.writeInt(writeLen);
                    _data.writeString(writeBuf);
                    if (errorNum == null) {
                        _data.writeInt(-1);
                    } else {
                        _data.writeInt(errorNum.length);
                    }
                    if (!this.mRemote.transact(25, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().writeProtectArea(optItem, writeLen, writeBuf, errorNum);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.readIntArray(errorNum);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.os.IHwGeneralManager
            public int setSdCardCryptdEnable(boolean enable, String volId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(enable ? 1 : 0);
                    _data.writeString(volId);
                    if (!this.mRemote.transact(26, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setSdCardCryptdEnable(enable, volId);
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

            @Override // huawei.android.os.IHwGeneralManager
            public int unlockSdCardKey(int userId, int serialNumber, byte[] token, byte[] secret) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    _data.writeInt(serialNumber);
                    _data.writeByteArray(token);
                    _data.writeByteArray(secret);
                    if (!this.mRemote.transact(27, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().unlockSdCardKey(userId, serialNumber, token, secret);
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

            @Override // huawei.android.os.IHwGeneralManager
            public int addSdCardUserKeyAuth(int userId, int serialNumber, byte[] token, byte[] secret) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    _data.writeInt(serialNumber);
                    _data.writeByteArray(token);
                    _data.writeByteArray(secret);
                    if (!this.mRemote.transact(28, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().addSdCardUserKeyAuth(userId, serialNumber, token, secret);
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

            @Override // huawei.android.os.IHwGeneralManager
            public int backupSecretkey() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(29, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().backupSecretkey();
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

            @Override // huawei.android.os.IHwGeneralManager
            public boolean supportHwPush() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(30, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().supportHwPush();
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

            @Override // huawei.android.os.IHwGeneralManager
            public long getPartitionInfo(String partitionName, int infoType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(partitionName);
                    _data.writeInt(infoType);
                    if (!this.mRemote.transact(31, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getPartitionInfo(partitionName, infoType);
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

            @Override // huawei.android.os.IHwGeneralManager
            public String mountCifs(String source, String option, IBinder binder) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(source);
                    _data.writeString(option);
                    _data.writeStrongBinder(binder);
                    if (!this.mRemote.transact(32, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().mountCifs(source, option, binder);
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

            @Override // huawei.android.os.IHwGeneralManager
            public void unmountCifs(String mountPoint) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(mountPoint);
                    if (this.mRemote.transact(33, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().unmountCifs(mountPoint);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.os.IHwGeneralManager
            public int isSupportedCifs() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(34, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isSupportedCifs();
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

            @Override // huawei.android.os.IHwGeneralManager
            public int getLocalDevStat(int dev) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(dev);
                    if (!this.mRemote.transact(35, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getLocalDevStat(dev);
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

            @Override // huawei.android.os.IHwGeneralManager
            public String getDeviceId(int dev) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(dev);
                    if (!this.mRemote.transact(36, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getDeviceId(dev);
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

            @Override // huawei.android.os.IHwGeneralManager
            public int doSdcardCheckRW() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(37, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().doSdcardCheckRW();
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

            @Override // huawei.android.os.IHwGeneralManager
            public boolean isIsolatedStorageApp(int uid, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeString(packageName);
                    boolean _result = false;
                    if (!this.mRemote.transact(38, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isIsolatedStorageApp(uid, packageName);
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

            @Override // huawei.android.os.IHwGeneralManager
            public String[] getIsolatedStorageApps(int excludeFlag) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(excludeFlag);
                    if (!this.mRemote.transact(39, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getIsolatedStorageApps(excludeFlag);
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
        }

        public static boolean setDefaultImpl(IHwGeneralManager impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IHwGeneralManager getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
