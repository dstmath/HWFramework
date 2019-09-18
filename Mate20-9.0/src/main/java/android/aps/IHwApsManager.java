package android.aps;

import android.aps.IApsManagerServiceCallback;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.List;

public interface IHwApsManager extends IInterface {

    public static abstract class Stub extends Binder implements IHwApsManager {
        private static final String DESCRIPTOR = "android.aps.IHwApsManager";
        static final int TRANSACTION_deletePackageApsInfo = 15;
        static final int TRANSACTION_disableFeatures = 17;
        static final int TRANSACTION_enableFeatures = 18;
        static final int TRANSACTION_getAllApsPackages = 20;
        static final int TRANSACTION_getAllPackagesApsInfo = 19;
        static final int TRANSACTION_getBrightness = 13;
        static final int TRANSACTION_getDynamicFps = 36;
        static final int TRANSACTION_getDynamicResolutionRatio = 34;
        static final int TRANSACTION_getFbSkip = 29;
        static final int TRANSACTION_getFps = 11;
        static final int TRANSACTION_getHighpToLowp = 30;
        static final int TRANSACTION_getMaxFps = 12;
        static final int TRANSACTION_getMipMap = 32;
        static final int TRANSACTION_getPackageApsInfo = 9;
        static final int TRANSACTION_getResolution = 10;
        static final int TRANSACTION_getSeviceVersion = 23;
        static final int TRANSACTION_getShadowMap = 31;
        static final int TRANSACTION_getTexture = 14;
        static final int TRANSACTION_isFeaturesEnabled = 16;
        static final int TRANSACTION_registerCallback = 22;
        static final int TRANSACTION_setBrightness = 6;
        static final int TRANSACTION_setDescentGradeResolution = 3;
        static final int TRANSACTION_setDynamicFps = 35;
        static final int TRANSACTION_setDynamicResolutionRatio = 33;
        static final int TRANSACTION_setFbSkip = 25;
        static final int TRANSACTION_setFps = 4;
        static final int TRANSACTION_setHighpToLowp = 26;
        static final int TRANSACTION_setLowResolutionMode = 2;
        static final int TRANSACTION_setMaxFps = 5;
        static final int TRANSACTION_setMipMap = 28;
        static final int TRANSACTION_setPackageApsInfo = 8;
        static final int TRANSACTION_setResolution = 1;
        static final int TRANSACTION_setShadowMap = 27;
        static final int TRANSACTION_setTexture = 7;
        static final int TRANSACTION_stopPackages = 24;
        static final int TRANSACTION_updateApsInfo = 21;

        private static class Proxy implements IHwApsManager {
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

            public int setResolution(String pkgName, float ratio, boolean switchable) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkgName);
                    _data.writeFloat(ratio);
                    _data.writeInt(switchable);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int setLowResolutionMode(int lowResolutionMode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(lowResolutionMode);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int setDescentGradeResolution(String pkgName, int reduceLevel, boolean switchable) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkgName);
                    _data.writeInt(reduceLevel);
                    _data.writeInt(switchable);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int setFps(String pkgName, int fps) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkgName);
                    _data.writeInt(fps);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int setMaxFps(String pkgName, int fps) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkgName);
                    _data.writeInt(fps);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int setBrightness(String pkgName, int ratioPercent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkgName);
                    _data.writeInt(ratioPercent);
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int setTexture(String pkgName, int ratioPercent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkgName);
                    _data.writeInt(ratioPercent);
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int setPackageApsInfo(String pkgName, ApsAppInfo info) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkgName);
                    if (info != null) {
                        _data.writeInt(1);
                        info.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ApsAppInfo getPackageApsInfo(String pkgName) throws RemoteException {
                ApsAppInfo _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkgName);
                    this.mRemote.transact(9, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ApsAppInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public float getResolution(String pkgName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkgName);
                    this.mRemote.transact(10, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readFloat();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getFps(String pkgName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkgName);
                    this.mRemote.transact(11, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getMaxFps(String pkgName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkgName);
                    this.mRemote.transact(12, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getBrightness(String pkgName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkgName);
                    this.mRemote.transact(13, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getTexture(String pkgName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkgName);
                    this.mRemote.transact(14, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean deletePackageApsInfo(String pkgName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkgName);
                    boolean _result = false;
                    this.mRemote.transact(15, _data, _reply, 0);
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

            public int isFeaturesEnabled(int bitmask) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(bitmask);
                    this.mRemote.transact(16, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean disableFeatures(int bitmask) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(bitmask);
                    boolean _result = false;
                    this.mRemote.transact(17, _data, _reply, 0);
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

            public boolean enableFeatures(int bitmak) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(bitmak);
                    boolean _result = false;
                    this.mRemote.transact(18, _data, _reply, 0);
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

            public List<ApsAppInfo> getAllPackagesApsInfo() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(19, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createTypedArrayList(ApsAppInfo.CREATOR);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<String> getAllApsPackages() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(20, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createStringArrayList();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean updateApsInfo(List<ApsAppInfo> infos) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeTypedList(infos);
                    boolean _result = false;
                    this.mRemote.transact(21, _data, _reply, 0);
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

            public boolean registerCallback(String pkgName, IApsManagerServiceCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkgName);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
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

            public float getSeviceVersion() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(23, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readFloat();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean stopPackages(List<String> pkgs) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(pkgs);
                    boolean _result = false;
                    this.mRemote.transact(24, _data, _reply, 0);
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

            public int setFbSkip(String pkgName, boolean onoff) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkgName);
                    _data.writeInt(onoff);
                    this.mRemote.transact(25, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int setHighpToLowp(String pkgName, boolean onoff) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkgName);
                    _data.writeInt(onoff);
                    this.mRemote.transact(26, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int setShadowMap(String pkgName, int status) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkgName);
                    _data.writeInt(status);
                    this.mRemote.transact(27, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int setMipMap(String pkgName, int status) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkgName);
                    _data.writeInt(status);
                    this.mRemote.transact(28, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean getFbSkip(String pkgName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkgName);
                    boolean _result = false;
                    this.mRemote.transact(29, _data, _reply, 0);
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

            public boolean getHighpToLowp(String pkgName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkgName);
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

            public int getShadowMap(String pkgName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkgName);
                    this.mRemote.transact(31, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getMipMap(String pkgName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkgName);
                    this.mRemote.transact(32, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int setDynamicResolutionRatio(String pkgName, float ratio) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkgName);
                    _data.writeFloat(ratio);
                    this.mRemote.transact(33, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public float getDynamicResolutionRatio(String pkgName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkgName);
                    this.mRemote.transact(34, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readFloat();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int setDynamicFps(String pkgName, int fps) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkgName);
                    _data.writeInt(fps);
                    this.mRemote.transact(35, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getDynamicFps(String pkgName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkgName);
                    this.mRemote.transact(36, _data, _reply, 0);
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

        public static IHwApsManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHwApsManager)) {
                return new Proxy(obj);
            }
            return (IHwApsManager) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            ApsAppInfo _arg1;
            if (code != 1598968902) {
                boolean _arg12 = false;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg0 = data.readString();
                        float _arg13 = data.readFloat();
                        if (data.readInt() != 0) {
                            _arg12 = true;
                        }
                        int _result = setResolution(_arg0, _arg13, _arg12);
                        reply.writeNoException();
                        reply.writeInt(_result);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        int _result2 = setLowResolutionMode(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result2);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg02 = data.readString();
                        int _arg14 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg12 = true;
                        }
                        int _result3 = setDescentGradeResolution(_arg02, _arg14, _arg12);
                        reply.writeNoException();
                        reply.writeInt(_result3);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        int _result4 = setFps(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result4);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        int _result5 = setMaxFps(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result5);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        int _result6 = setBrightness(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result6);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        int _result7 = setTexture(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result7);
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg03 = data.readString();
                        if (data.readInt() != 0) {
                            _arg1 = ApsAppInfo.CREATOR.createFromParcel(data);
                        } else {
                            _arg1 = null;
                        }
                        int _result8 = setPackageApsInfo(_arg03, _arg1);
                        reply.writeNoException();
                        reply.writeInt(_result8);
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        ApsAppInfo _result9 = getPackageApsInfo(data.readString());
                        reply.writeNoException();
                        if (_result9 != null) {
                            reply.writeInt(1);
                            _result9.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        float _result10 = getResolution(data.readString());
                        reply.writeNoException();
                        reply.writeFloat(_result10);
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        int _result11 = getFps(data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result11);
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        int _result12 = getMaxFps(data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result12);
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        int _result13 = getBrightness(data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result13);
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        int _result14 = getTexture(data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result14);
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        boolean _result15 = deletePackageApsInfo(data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result15);
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        int _result16 = isFeaturesEnabled(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result16);
                        return true;
                    case 17:
                        data.enforceInterface(DESCRIPTOR);
                        boolean _result17 = disableFeatures(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result17);
                        return true;
                    case 18:
                        data.enforceInterface(DESCRIPTOR);
                        boolean _result18 = enableFeatures(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result18);
                        return true;
                    case 19:
                        data.enforceInterface(DESCRIPTOR);
                        List<ApsAppInfo> _result19 = getAllPackagesApsInfo();
                        reply.writeNoException();
                        reply.writeTypedList(_result19);
                        return true;
                    case 20:
                        data.enforceInterface(DESCRIPTOR);
                        List<String> _result20 = getAllApsPackages();
                        reply.writeNoException();
                        reply.writeStringList(_result20);
                        return true;
                    case 21:
                        data.enforceInterface(DESCRIPTOR);
                        boolean _result21 = updateApsInfo(data.createTypedArrayList(ApsAppInfo.CREATOR));
                        reply.writeNoException();
                        reply.writeInt(_result21);
                        return true;
                    case 22:
                        data.enforceInterface(DESCRIPTOR);
                        boolean _result22 = registerCallback(data.readString(), IApsManagerServiceCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(_result22);
                        return true;
                    case 23:
                        data.enforceInterface(DESCRIPTOR);
                        float _result23 = getSeviceVersion();
                        reply.writeNoException();
                        reply.writeFloat(_result23);
                        return true;
                    case 24:
                        data.enforceInterface(DESCRIPTOR);
                        boolean _result24 = stopPackages(data.createStringArrayList());
                        reply.writeNoException();
                        reply.writeInt(_result24);
                        return true;
                    case 25:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg04 = data.readString();
                        if (data.readInt() != 0) {
                            _arg12 = true;
                        }
                        int _result25 = setFbSkip(_arg04, _arg12);
                        reply.writeNoException();
                        reply.writeInt(_result25);
                        return true;
                    case 26:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg05 = data.readString();
                        if (data.readInt() != 0) {
                            _arg12 = true;
                        }
                        int _result26 = setHighpToLowp(_arg05, _arg12);
                        reply.writeNoException();
                        reply.writeInt(_result26);
                        return true;
                    case 27:
                        data.enforceInterface(DESCRIPTOR);
                        int _result27 = setShadowMap(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result27);
                        return true;
                    case 28:
                        data.enforceInterface(DESCRIPTOR);
                        int _result28 = setMipMap(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result28);
                        return true;
                    case 29:
                        data.enforceInterface(DESCRIPTOR);
                        boolean _result29 = getFbSkip(data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result29);
                        return true;
                    case 30:
                        data.enforceInterface(DESCRIPTOR);
                        boolean _result30 = getHighpToLowp(data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result30);
                        return true;
                    case 31:
                        data.enforceInterface(DESCRIPTOR);
                        int _result31 = getShadowMap(data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result31);
                        return true;
                    case 32:
                        data.enforceInterface(DESCRIPTOR);
                        int _result32 = getMipMap(data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result32);
                        return true;
                    case 33:
                        data.enforceInterface(DESCRIPTOR);
                        int _result33 = setDynamicResolutionRatio(data.readString(), data.readFloat());
                        reply.writeNoException();
                        reply.writeInt(_result33);
                        return true;
                    case 34:
                        data.enforceInterface(DESCRIPTOR);
                        float _result34 = getDynamicResolutionRatio(data.readString());
                        reply.writeNoException();
                        reply.writeFloat(_result34);
                        return true;
                    case 35:
                        data.enforceInterface(DESCRIPTOR);
                        int _result35 = setDynamicFps(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result35);
                        return true;
                    case 36:
                        data.enforceInterface(DESCRIPTOR);
                        int _result36 = getDynamicFps(data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result36);
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

    boolean deletePackageApsInfo(String str) throws RemoteException;

    boolean disableFeatures(int i) throws RemoteException;

    boolean enableFeatures(int i) throws RemoteException;

    List<String> getAllApsPackages() throws RemoteException;

    List<ApsAppInfo> getAllPackagesApsInfo() throws RemoteException;

    int getBrightness(String str) throws RemoteException;

    int getDynamicFps(String str) throws RemoteException;

    float getDynamicResolutionRatio(String str) throws RemoteException;

    boolean getFbSkip(String str) throws RemoteException;

    int getFps(String str) throws RemoteException;

    boolean getHighpToLowp(String str) throws RemoteException;

    int getMaxFps(String str) throws RemoteException;

    int getMipMap(String str) throws RemoteException;

    ApsAppInfo getPackageApsInfo(String str) throws RemoteException;

    float getResolution(String str) throws RemoteException;

    float getSeviceVersion() throws RemoteException;

    int getShadowMap(String str) throws RemoteException;

    int getTexture(String str) throws RemoteException;

    int isFeaturesEnabled(int i) throws RemoteException;

    boolean registerCallback(String str, IApsManagerServiceCallback iApsManagerServiceCallback) throws RemoteException;

    int setBrightness(String str, int i) throws RemoteException;

    int setDescentGradeResolution(String str, int i, boolean z) throws RemoteException;

    int setDynamicFps(String str, int i) throws RemoteException;

    int setDynamicResolutionRatio(String str, float f) throws RemoteException;

    int setFbSkip(String str, boolean z) throws RemoteException;

    int setFps(String str, int i) throws RemoteException;

    int setHighpToLowp(String str, boolean z) throws RemoteException;

    int setLowResolutionMode(int i) throws RemoteException;

    int setMaxFps(String str, int i) throws RemoteException;

    int setMipMap(String str, int i) throws RemoteException;

    int setPackageApsInfo(String str, ApsAppInfo apsAppInfo) throws RemoteException;

    int setResolution(String str, float f, boolean z) throws RemoteException;

    int setShadowMap(String str, int i) throws RemoteException;

    int setTexture(String str, int i) throws RemoteException;

    boolean stopPackages(List<String> list) throws RemoteException;

    boolean updateApsInfo(List<ApsAppInfo> list) throws RemoteException;
}
