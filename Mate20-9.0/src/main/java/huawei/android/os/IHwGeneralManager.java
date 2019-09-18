package huawei.android.os;

import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Messenger;
import android.os.Parcel;
import android.os.RemoteException;

public interface IHwGeneralManager extends IInterface {

    public static abstract class Stub extends Binder implements IHwGeneralManager {
        private static final String DESCRIPTOR = "huawei.android.os.IHwGeneralManager";
        static final int TRANSACTION_addSdCardUserKeyAuth = 28;
        static final int TRANSACTION_backupSecretkey = 29;
        static final int TRANSACTION_clearSDLockPassword = 2;
        static final int TRANSACTION_doSdcardCheckRW = 37;
        static final int TRANSACTION_eraseSDLock = 4;
        static final int TRANSACTION_forceIdle = 8;
        static final int TRANSACTION_getDeviceId = 36;
        static final int TRANSACTION_getLocalDevStat = 35;
        static final int TRANSACTION_getPartitionInfo = 31;
        static final int TRANSACTION_getPressureLimit = 10;
        static final int TRANSACTION_getSDCardId = 6;
        static final int TRANSACTION_getSDLockState = 5;
        static final int TRANSACTION_getTestService = 23;
        static final int TRANSACTION_getTouchWeightValue = 21;
        static final int TRANSACTION_hasHaptic = 18;
        static final int TRANSACTION_isCurveScreen = 11;
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

        private static class Proxy implements IHwGeneralManager {
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

            public int setSDLockPassword(String pw) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pw);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int clearSDLockPassword() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int unlockSDCard(String pw) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pw);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void eraseSDLock() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getSDLockState() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getSDCardId() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void startFileBackup() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int forceIdle() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isSupportForce() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(9, _data, _reply, 0);
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

            public float getPressureLimit() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(10, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readFloat();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isCurveScreen() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(11, _data, _reply, 0);
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

            public void playIvtEffect(String effectName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(effectName);
                    this.mRemote.transact(12, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void stopPlayEffect() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(13, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void pausePlayEffect(String effectName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(effectName);
                    this.mRemote.transact(14, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void resumePausedEffect(String effectName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(effectName);
                    this.mRemote.transact(15, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isPlaying(String effectName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(effectName);
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
                    this.mRemote.transact(17, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

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
                    this.mRemote.transact(18, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void stopHaptic() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(19, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void resetTouchWeight() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(20, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getTouchWeightValue() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(21, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean mkDataDir(String path) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(path);
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

            public Messenger getTestService() throws RemoteException {
                Messenger _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(23, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (Messenger) Messenger.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

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
                    this.mRemote.transact(24, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.readStringArray(readBuf);
                    _reply.readIntArray(errorNum);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

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
                    this.mRemote.transact(25, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.readIntArray(errorNum);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int setSdCardCryptdEnable(boolean enable, String volId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(enable);
                    _data.writeString(volId);
                    this.mRemote.transact(26, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int unlockSdCardKey(int userId, int serialNumber, byte[] token, byte[] secret) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    _data.writeInt(serialNumber);
                    _data.writeByteArray(token);
                    _data.writeByteArray(secret);
                    this.mRemote.transact(27, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int addSdCardUserKeyAuth(int userId, int serialNumber, byte[] token, byte[] secret) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    _data.writeInt(serialNumber);
                    _data.writeByteArray(token);
                    _data.writeByteArray(secret);
                    this.mRemote.transact(28, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int backupSecretkey() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(29, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean supportHwPush() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(30, _data, _reply, 0);
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

            public long getPartitionInfo(String partitionName, int infoType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(partitionName);
                    _data.writeInt(infoType);
                    this.mRemote.transact(31, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readLong();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String mountCifs(String source, String option, IBinder binder) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(source);
                    _data.writeString(option);
                    _data.writeStrongBinder(binder);
                    this.mRemote.transact(32, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void unmountCifs(String mountPoint) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(mountPoint);
                    this.mRemote.transact(33, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int isSupportedCifs() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(34, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getLocalDevStat(int dev) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(dev);
                    this.mRemote.transact(35, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getDeviceId(int dev) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(dev);
                    this.mRemote.transact(36, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int doSdcardCheckRW() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(37, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

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

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            String[] _arg2;
            int[] _arg3;
            int[] _arg32;
            if (code != 1598968902) {
                Uri _arg0 = null;
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
                        boolean _result7 = isSupportForce();
                        reply.writeNoException();
                        reply.writeInt(_result7);
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        float _result8 = getPressureLimit();
                        reply.writeNoException();
                        reply.writeFloat(_result8);
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        boolean _result9 = isCurveScreen();
                        reply.writeNoException();
                        reply.writeInt(_result9);
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
                        boolean _result10 = isPlaying(data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result10);
                        return true;
                    case 17:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg03 = data.readInt();
                        int _arg1 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg0 = (Uri) Uri.CREATOR.createFromParcel(data);
                        }
                        boolean _result11 = startHaptic(_arg03, _arg1, _arg0);
                        reply.writeNoException();
                        reply.writeInt(_result11);
                        return true;
                    case 18:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (Uri) Uri.CREATOR.createFromParcel(data);
                        }
                        boolean _result12 = hasHaptic(_arg0);
                        reply.writeNoException();
                        reply.writeInt(_result12);
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
                        String _result13 = getTouchWeightValue();
                        reply.writeNoException();
                        reply.writeString(_result13);
                        return true;
                    case 22:
                        data.enforceInterface(DESCRIPTOR);
                        boolean _result14 = mkDataDir(data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result14);
                        return true;
                    case 23:
                        data.enforceInterface(DESCRIPTOR);
                        Messenger _result15 = getTestService();
                        reply.writeNoException();
                        if (_result15 != null) {
                            reply.writeInt(1);
                            _result15.writeToParcel(reply, 1);
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
                            _arg2 = null;
                        } else {
                            _arg2 = new String[_arg2_length];
                        }
                        int _arg3_length = data.readInt();
                        if (_arg3_length < 0) {
                            _arg3 = null;
                        } else {
                            _arg3 = new int[_arg3_length];
                        }
                        int _result16 = readProtectArea(_arg04, _arg12, _arg2, _arg3);
                        reply.writeNoException();
                        reply.writeInt(_result16);
                        reply.writeStringArray(_arg2);
                        reply.writeIntArray(_arg3);
                        return true;
                    case 25:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg05 = data.readString();
                        int _arg13 = data.readInt();
                        String _arg22 = data.readString();
                        int _arg3_length2 = data.readInt();
                        if (_arg3_length2 < 0) {
                            _arg32 = null;
                        } else {
                            _arg32 = new int[_arg3_length2];
                        }
                        int _result17 = writeProtectArea(_arg05, _arg13, _arg22, _arg32);
                        reply.writeNoException();
                        reply.writeInt(_result17);
                        reply.writeIntArray(_arg32);
                        return true;
                    case 26:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = true;
                        }
                        int _result18 = setSdCardCryptdEnable(_arg02, data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result18);
                        return true;
                    case 27:
                        data.enforceInterface(DESCRIPTOR);
                        int _result19 = unlockSdCardKey(data.readInt(), data.readInt(), data.createByteArray(), data.createByteArray());
                        reply.writeNoException();
                        reply.writeInt(_result19);
                        return true;
                    case 28:
                        data.enforceInterface(DESCRIPTOR);
                        int _result20 = addSdCardUserKeyAuth(data.readInt(), data.readInt(), data.createByteArray(), data.createByteArray());
                        reply.writeNoException();
                        reply.writeInt(_result20);
                        return true;
                    case 29:
                        data.enforceInterface(DESCRIPTOR);
                        int _result21 = backupSecretkey();
                        reply.writeNoException();
                        reply.writeInt(_result21);
                        return true;
                    case 30:
                        data.enforceInterface(DESCRIPTOR);
                        boolean _result22 = supportHwPush();
                        reply.writeNoException();
                        reply.writeInt(_result22);
                        return true;
                    case 31:
                        data.enforceInterface(DESCRIPTOR);
                        long _result23 = getPartitionInfo(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeLong(_result23);
                        return true;
                    case 32:
                        data.enforceInterface(DESCRIPTOR);
                        String _result24 = mountCifs(data.readString(), data.readString(), data.readStrongBinder());
                        reply.writeNoException();
                        reply.writeString(_result24);
                        return true;
                    case 33:
                        data.enforceInterface(DESCRIPTOR);
                        unmountCifs(data.readString());
                        reply.writeNoException();
                        return true;
                    case 34:
                        data.enforceInterface(DESCRIPTOR);
                        int _result25 = isSupportedCifs();
                        reply.writeNoException();
                        reply.writeInt(_result25);
                        return true;
                    case 35:
                        data.enforceInterface(DESCRIPTOR);
                        int _result26 = getLocalDevStat(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result26);
                        return true;
                    case 36:
                        data.enforceInterface(DESCRIPTOR);
                        String _result27 = getDeviceId(data.readInt());
                        reply.writeNoException();
                        reply.writeString(_result27);
                        return true;
                    case 37:
                        data.enforceInterface(DESCRIPTOR);
                        int _result28 = doSdcardCheckRW();
                        reply.writeNoException();
                        reply.writeInt(_result28);
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

    int addSdCardUserKeyAuth(int i, int i2, byte[] bArr, byte[] bArr2) throws RemoteException;

    int backupSecretkey() throws RemoteException;

    int clearSDLockPassword() throws RemoteException;

    int doSdcardCheckRW() throws RemoteException;

    void eraseSDLock() throws RemoteException;

    int forceIdle() throws RemoteException;

    String getDeviceId(int i) throws RemoteException;

    int getLocalDevStat(int i) throws RemoteException;

    long getPartitionInfo(String str, int i) throws RemoteException;

    float getPressureLimit() throws RemoteException;

    String getSDCardId() throws RemoteException;

    int getSDLockState() throws RemoteException;

    Messenger getTestService() throws RemoteException;

    String getTouchWeightValue() throws RemoteException;

    boolean hasHaptic(Uri uri) throws RemoteException;

    boolean isCurveScreen() throws RemoteException;

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
}
