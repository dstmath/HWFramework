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
        static final int TRANSACTION_addSdCardUserKeyAuth = 31;
        static final int TRANSACTION_clearSDLockPassword = 5;
        static final int TRANSACTION_eraseSDLock = 7;
        static final int TRANSACTION_forceIdle = 11;
        static final int TRANSACTION_getBootAnimSoundSwitch = 3;
        static final int TRANSACTION_getPressureLimit = 13;
        static final int TRANSACTION_getSDCardId = 9;
        static final int TRANSACTION_getSDLockState = 8;
        static final int TRANSACTION_getTestService = 26;
        static final int TRANSACTION_getTouchWeightValue = 24;
        static final int TRANSACTION_hasHaptic = 21;
        static final int TRANSACTION_isBootOrShutdownSoundCapable = 2;
        static final int TRANSACTION_isCurveScreen = 14;
        static final int TRANSACTION_isPlaying = 19;
        static final int TRANSACTION_isSupportForce = 12;
        static final int TRANSACTION_mkDataDir = 25;
        static final int TRANSACTION_pausePlayEffect = 17;
        static final int TRANSACTION_playIvtEffect = 15;
        static final int TRANSACTION_readProtectArea = 27;
        static final int TRANSACTION_resetTouchWeight = 23;
        static final int TRANSACTION_resumePausedEffect = 18;
        static final int TRANSACTION_setSDLockPassword = 4;
        static final int TRANSACTION_setSdCardCryptdEnable = 29;
        static final int TRANSACTION_startFileBackup = 10;
        static final int TRANSACTION_startHaptic = 20;
        static final int TRANSACTION_stopHaptic = 22;
        static final int TRANSACTION_stopPlayEffect = 16;
        static final int TRANSACTION_switchBootOrShutSound = 1;
        static final int TRANSACTION_unlockSDCard = 6;
        static final int TRANSACTION_unlockSdCardKey = 30;
        static final int TRANSACTION_writeProtectArea = 28;

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

            public void switchBootOrShutSound(String openOrClose) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(openOrClose);
                    this.mRemote.transact(Stub.TRANSACTION_switchBootOrShutSound, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isBootOrShutdownSoundCapable() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_isBootOrShutdownSoundCapable, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getBootAnimSoundSwitch() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getBootAnimSoundSwitch, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int setSDLockPassword(String pw) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pw);
                    this.mRemote.transact(Stub.TRANSACTION_setSDLockPassword, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_clearSDLockPassword, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_unlockSDCard, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_eraseSDLock, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_getSDLockState, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_getSDCardId, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_startFileBackup, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_forceIdle, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_isSupportForce, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public float getPressureLimit() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getPressureLimit, _data, _reply, 0);
                    _reply.readException();
                    float _result = _reply.readFloat();
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_isCurveScreen, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
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
                    this.mRemote.transact(Stub.TRANSACTION_playIvtEffect, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_stopPlayEffect, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_pausePlayEffect, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_resumePausedEffect, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_isPlaying, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
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
                    if (uri != null) {
                        _data.writeInt(Stub.TRANSACTION_switchBootOrShutSound);
                        uri.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_startHaptic, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean hasHaptic(Uri uri) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (uri != null) {
                        _data.writeInt(Stub.TRANSACTION_switchBootOrShutSound);
                        uri.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_hasHaptic, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void stopHaptic() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_stopHaptic, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_resetTouchWeight, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_getTouchWeightValue, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_mkDataDir, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public Messenger getTestService() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    Messenger messenger;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getTestService, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        messenger = (Messenger) Messenger.CREATOR.createFromParcel(_reply);
                    } else {
                        messenger = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return messenger;
                } catch (Throwable th) {
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
                    this.mRemote.transact(Stub.TRANSACTION_readProtectArea, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_writeProtectArea, _data, _reply, 0);
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
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (enable) {
                        i = Stub.TRANSACTION_switchBootOrShutSound;
                    }
                    _data.writeInt(i);
                    _data.writeString(volId);
                    this.mRemote.transact(Stub.TRANSACTION_setSdCardCryptdEnable, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_unlockSdCardKey, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_addSdCardUserKeyAuth, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
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
            boolean _result;
            int _result2;
            String _result3;
            int _arg1;
            String _arg0;
            int _arg3_length;
            int[] iArr;
            switch (code) {
                case TRANSACTION_switchBootOrShutSound /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    switchBootOrShutSound(data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_isBootOrShutdownSoundCapable /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = isBootOrShutdownSoundCapable();
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_switchBootOrShutSound : 0);
                    return true;
                case TRANSACTION_getBootAnimSoundSwitch /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getBootAnimSoundSwitch();
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_setSDLockPassword /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = setSDLockPassword(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_clearSDLockPassword /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = clearSDLockPassword();
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_unlockSDCard /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = unlockSDCard(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_eraseSDLock /*7*/:
                    data.enforceInterface(DESCRIPTOR);
                    eraseSDLock();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getSDLockState /*8*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getSDLockState();
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_getSDCardId /*9*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = getSDCardId();
                    reply.writeNoException();
                    reply.writeString(_result3);
                    return true;
                case TRANSACTION_startFileBackup /*10*/:
                    data.enforceInterface(DESCRIPTOR);
                    startFileBackup();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_forceIdle /*11*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = forceIdle();
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_isSupportForce /*12*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = isSupportForce();
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_switchBootOrShutSound : 0);
                    return true;
                case TRANSACTION_getPressureLimit /*13*/:
                    data.enforceInterface(DESCRIPTOR);
                    float _result4 = getPressureLimit();
                    reply.writeNoException();
                    reply.writeFloat(_result4);
                    return true;
                case TRANSACTION_isCurveScreen /*14*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = isCurveScreen();
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_switchBootOrShutSound : 0);
                    return true;
                case TRANSACTION_playIvtEffect /*15*/:
                    data.enforceInterface(DESCRIPTOR);
                    playIvtEffect(data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_stopPlayEffect /*16*/:
                    data.enforceInterface(DESCRIPTOR);
                    stopPlayEffect();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_pausePlayEffect /*17*/:
                    data.enforceInterface(DESCRIPTOR);
                    pausePlayEffect(data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_resumePausedEffect /*18*/:
                    data.enforceInterface(DESCRIPTOR);
                    resumePausedEffect(data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_isPlaying /*19*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = isPlaying(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_switchBootOrShutSound : 0);
                    return true;
                case TRANSACTION_startHaptic /*20*/:
                    Uri uri;
                    data.enforceInterface(DESCRIPTOR);
                    int _arg02 = data.readInt();
                    _arg1 = data.readInt();
                    if (data.readInt() != 0) {
                        uri = (Uri) Uri.CREATOR.createFromParcel(data);
                    } else {
                        uri = null;
                    }
                    _result = startHaptic(_arg02, _arg1, uri);
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_switchBootOrShutSound : 0);
                    return true;
                case TRANSACTION_hasHaptic /*21*/:
                    Uri uri2;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        uri2 = (Uri) Uri.CREATOR.createFromParcel(data);
                    } else {
                        uri2 = null;
                    }
                    _result = hasHaptic(uri2);
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_switchBootOrShutSound : 0);
                    return true;
                case TRANSACTION_stopHaptic /*22*/:
                    data.enforceInterface(DESCRIPTOR);
                    stopHaptic();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_resetTouchWeight /*23*/:
                    data.enforceInterface(DESCRIPTOR);
                    resetTouchWeight();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getTouchWeightValue /*24*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = getTouchWeightValue();
                    reply.writeNoException();
                    reply.writeString(_result3);
                    return true;
                case TRANSACTION_mkDataDir /*25*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = mkDataDir(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_switchBootOrShutSound : 0);
                    return true;
                case TRANSACTION_getTestService /*26*/:
                    data.enforceInterface(DESCRIPTOR);
                    Messenger _result5 = getTestService();
                    reply.writeNoException();
                    if (_result5 != null) {
                        reply.writeInt(TRANSACTION_switchBootOrShutSound);
                        _result5.writeToParcel(reply, TRANSACTION_switchBootOrShutSound);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_readProtectArea /*27*/:
                    String[] strArr;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    _arg1 = data.readInt();
                    int _arg2_length = data.readInt();
                    if (_arg2_length < 0) {
                        strArr = null;
                    } else {
                        strArr = new String[_arg2_length];
                    }
                    _arg3_length = data.readInt();
                    if (_arg3_length < 0) {
                        iArr = null;
                    } else {
                        iArr = new int[_arg3_length];
                    }
                    _result2 = readProtectArea(_arg0, _arg1, strArr, iArr);
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    reply.writeStringArray(strArr);
                    reply.writeIntArray(iArr);
                    return true;
                case TRANSACTION_writeProtectArea /*28*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    _arg1 = data.readInt();
                    String _arg2 = data.readString();
                    _arg3_length = data.readInt();
                    if (_arg3_length < 0) {
                        iArr = null;
                    } else {
                        iArr = new int[_arg3_length];
                    }
                    _result2 = writeProtectArea(_arg0, _arg1, _arg2, iArr);
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    reply.writeIntArray(iArr);
                    return true;
                case TRANSACTION_setSdCardCryptdEnable /*29*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = setSdCardCryptdEnable(data.readInt() != 0, data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_unlockSdCardKey /*30*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = unlockSdCardKey(data.readInt(), data.readInt(), data.createByteArray(), data.createByteArray());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_addSdCardUserKeyAuth /*31*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = addSdCardUserKeyAuth(data.readInt(), data.readInt(), data.createByteArray(), data.createByteArray());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    int addSdCardUserKeyAuth(int i, int i2, byte[] bArr, byte[] bArr2) throws RemoteException;

    int clearSDLockPassword() throws RemoteException;

    void eraseSDLock() throws RemoteException;

    int forceIdle() throws RemoteException;

    int getBootAnimSoundSwitch() throws RemoteException;

    float getPressureLimit() throws RemoteException;

    String getSDCardId() throws RemoteException;

    int getSDLockState() throws RemoteException;

    Messenger getTestService() throws RemoteException;

    String getTouchWeightValue() throws RemoteException;

    boolean hasHaptic(Uri uri) throws RemoteException;

    boolean isBootOrShutdownSoundCapable() throws RemoteException;

    boolean isCurveScreen() throws RemoteException;

    boolean isPlaying(String str) throws RemoteException;

    boolean isSupportForce() throws RemoteException;

    boolean mkDataDir(String str) throws RemoteException;

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

    void switchBootOrShutSound(String str) throws RemoteException;

    int unlockSDCard(String str) throws RemoteException;

    int unlockSdCardKey(int i, int i2, byte[] bArr, byte[] bArr2) throws RemoteException;

    int writeProtectArea(String str, int i, String str2, int[] iArr) throws RemoteException;
}
