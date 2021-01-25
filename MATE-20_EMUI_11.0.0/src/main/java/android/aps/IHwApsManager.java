package android.aps;

import android.aps.IApsManagerServiceCallback;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.List;

public interface IHwApsManager extends IInterface {
    boolean deletePackageApsInfo(String str) throws RemoteException;

    boolean disableFeatures(int i) throws RemoteException;

    boolean enableFeatures(int i) throws RemoteException;

    List<String> getAllApsPackages() throws RemoteException;

    List<ApsAppInfo> getAllPackagesApsInfo() throws RemoteException;

    int getBrightness(String str) throws RemoteException;

    int getDynamicFps(String str) throws RemoteException;

    float getDynamicResolutionRatio(String str) throws RemoteException;

    int getFps(String str) throws RemoteException;

    int getMaxFps(String str) throws RemoteException;

    ApsAppInfo getPackageApsInfo(String str) throws RemoteException;

    float getResolution(String str) throws RemoteException;

    float getSeviceVersion() throws RemoteException;

    int getTexture(String str) throws RemoteException;

    int isFeaturesEnabled(int i) throws RemoteException;

    boolean registerCallback(String str, IApsManagerServiceCallback iApsManagerServiceCallback) throws RemoteException;

    int setBrightness(String str, int i) throws RemoteException;

    int setDynamicFps(String str, int i) throws RemoteException;

    int setDynamicResolutionRatio(String str, float f) throws RemoteException;

    int setFps(String str, int i) throws RemoteException;

    int setLowResolutionMode(int i) throws RemoteException;

    int setMaxFps(String str, int i) throws RemoteException;

    int setPackageApsInfo(String str, ApsAppInfo apsAppInfo) throws RemoteException;

    int setResolution(String str, float f, boolean z) throws RemoteException;

    int setTexture(String str, int i) throws RemoteException;

    boolean stopPackages(List<String> list) throws RemoteException;

    boolean updateApsInfo(List<ApsAppInfo> list) throws RemoteException;

    public static class Default implements IHwApsManager {
        @Override // android.aps.IHwApsManager
        public int setResolution(String pkgName, float ratio, boolean switchable) throws RemoteException {
            return 0;
        }

        @Override // android.aps.IHwApsManager
        public int setLowResolutionMode(int lowResolutionMode) throws RemoteException {
            return 0;
        }

        @Override // android.aps.IHwApsManager
        public int setFps(String pkgName, int fps) throws RemoteException {
            return 0;
        }

        @Override // android.aps.IHwApsManager
        public int setMaxFps(String pkgName, int fps) throws RemoteException {
            return 0;
        }

        @Override // android.aps.IHwApsManager
        public int setBrightness(String pkgName, int ratioPercent) throws RemoteException {
            return 0;
        }

        @Override // android.aps.IHwApsManager
        public int setTexture(String pkgName, int ratioPercent) throws RemoteException {
            return 0;
        }

        @Override // android.aps.IHwApsManager
        public int setPackageApsInfo(String pkgName, ApsAppInfo info) throws RemoteException {
            return 0;
        }

        @Override // android.aps.IHwApsManager
        public ApsAppInfo getPackageApsInfo(String pkgName) throws RemoteException {
            return null;
        }

        @Override // android.aps.IHwApsManager
        public float getResolution(String pkgName) throws RemoteException {
            return 0.0f;
        }

        @Override // android.aps.IHwApsManager
        public int getFps(String pkgName) throws RemoteException {
            return 0;
        }

        @Override // android.aps.IHwApsManager
        public int getMaxFps(String pkgName) throws RemoteException {
            return 0;
        }

        @Override // android.aps.IHwApsManager
        public int getBrightness(String pkgName) throws RemoteException {
            return 0;
        }

        @Override // android.aps.IHwApsManager
        public int getTexture(String pkgName) throws RemoteException {
            return 0;
        }

        @Override // android.aps.IHwApsManager
        public boolean deletePackageApsInfo(String pkgName) throws RemoteException {
            return false;
        }

        @Override // android.aps.IHwApsManager
        public int isFeaturesEnabled(int bitmask) throws RemoteException {
            return 0;
        }

        @Override // android.aps.IHwApsManager
        public boolean disableFeatures(int bitmask) throws RemoteException {
            return false;
        }

        @Override // android.aps.IHwApsManager
        public boolean enableFeatures(int bitmak) throws RemoteException {
            return false;
        }

        @Override // android.aps.IHwApsManager
        public List<ApsAppInfo> getAllPackagesApsInfo() throws RemoteException {
            return null;
        }

        @Override // android.aps.IHwApsManager
        public List<String> getAllApsPackages() throws RemoteException {
            return null;
        }

        @Override // android.aps.IHwApsManager
        public boolean updateApsInfo(List<ApsAppInfo> list) throws RemoteException {
            return false;
        }

        @Override // android.aps.IHwApsManager
        public boolean registerCallback(String pkgName, IApsManagerServiceCallback callback) throws RemoteException {
            return false;
        }

        @Override // android.aps.IHwApsManager
        public float getSeviceVersion() throws RemoteException {
            return 0.0f;
        }

        @Override // android.aps.IHwApsManager
        public boolean stopPackages(List<String> list) throws RemoteException {
            return false;
        }

        @Override // android.aps.IHwApsManager
        public int setDynamicResolutionRatio(String pkgName, float ratio) throws RemoteException {
            return 0;
        }

        @Override // android.aps.IHwApsManager
        public float getDynamicResolutionRatio(String pkgName) throws RemoteException {
            return 0.0f;
        }

        @Override // android.aps.IHwApsManager
        public int setDynamicFps(String pkgName, int fps) throws RemoteException {
            return 0;
        }

        @Override // android.aps.IHwApsManager
        public int getDynamicFps(String pkgName) throws RemoteException {
            return 0;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IHwApsManager {
        private static final String DESCRIPTOR = "android.aps.IHwApsManager";
        static final int TRANSACTION_deletePackageApsInfo = 14;
        static final int TRANSACTION_disableFeatures = 16;
        static final int TRANSACTION_enableFeatures = 17;
        static final int TRANSACTION_getAllApsPackages = 19;
        static final int TRANSACTION_getAllPackagesApsInfo = 18;
        static final int TRANSACTION_getBrightness = 12;
        static final int TRANSACTION_getDynamicFps = 27;
        static final int TRANSACTION_getDynamicResolutionRatio = 25;
        static final int TRANSACTION_getFps = 10;
        static final int TRANSACTION_getMaxFps = 11;
        static final int TRANSACTION_getPackageApsInfo = 8;
        static final int TRANSACTION_getResolution = 9;
        static final int TRANSACTION_getSeviceVersion = 22;
        static final int TRANSACTION_getTexture = 13;
        static final int TRANSACTION_isFeaturesEnabled = 15;
        static final int TRANSACTION_registerCallback = 21;
        static final int TRANSACTION_setBrightness = 5;
        static final int TRANSACTION_setDynamicFps = 26;
        static final int TRANSACTION_setDynamicResolutionRatio = 24;
        static final int TRANSACTION_setFps = 3;
        static final int TRANSACTION_setLowResolutionMode = 2;
        static final int TRANSACTION_setMaxFps = 4;
        static final int TRANSACTION_setPackageApsInfo = 7;
        static final int TRANSACTION_setResolution = 1;
        static final int TRANSACTION_setTexture = 6;
        static final int TRANSACTION_stopPackages = 23;
        static final int TRANSACTION_updateApsInfo = 20;

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

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "setResolution";
                case 2:
                    return "setLowResolutionMode";
                case 3:
                    return "setFps";
                case 4:
                    return "setMaxFps";
                case 5:
                    return "setBrightness";
                case 6:
                    return "setTexture";
                case 7:
                    return "setPackageApsInfo";
                case 8:
                    return "getPackageApsInfo";
                case 9:
                    return "getResolution";
                case 10:
                    return "getFps";
                case 11:
                    return "getMaxFps";
                case 12:
                    return "getBrightness";
                case 13:
                    return "getTexture";
                case 14:
                    return "deletePackageApsInfo";
                case 15:
                    return "isFeaturesEnabled";
                case 16:
                    return "disableFeatures";
                case 17:
                    return "enableFeatures";
                case 18:
                    return "getAllPackagesApsInfo";
                case 19:
                    return "getAllApsPackages";
                case 20:
                    return "updateApsInfo";
                case 21:
                    return "registerCallback";
                case 22:
                    return "getSeviceVersion";
                case 23:
                    return "stopPackages";
                case 24:
                    return "setDynamicResolutionRatio";
                case 25:
                    return "getDynamicResolutionRatio";
                case 26:
                    return "setDynamicFps";
                case 27:
                    return "getDynamicFps";
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
            ApsAppInfo _arg1;
            if (code != 1598968902) {
                boolean _arg2 = false;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg0 = data.readString();
                        float _arg12 = data.readFloat();
                        if (data.readInt() != 0) {
                            _arg2 = true;
                        }
                        int _result = setResolution(_arg0, _arg12, _arg2);
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
                        int _result3 = setFps(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result3);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        int _result4 = setMaxFps(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result4);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        int _result5 = setBrightness(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result5);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        int _result6 = setTexture(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result6);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg02 = data.readString();
                        if (data.readInt() != 0) {
                            _arg1 = ApsAppInfo.CREATOR.createFromParcel(data);
                        } else {
                            _arg1 = null;
                        }
                        int _result7 = setPackageApsInfo(_arg02, _arg1);
                        reply.writeNoException();
                        reply.writeInt(_result7);
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        ApsAppInfo _result8 = getPackageApsInfo(data.readString());
                        reply.writeNoException();
                        if (_result8 != null) {
                            reply.writeInt(1);
                            _result8.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        float _result9 = getResolution(data.readString());
                        reply.writeNoException();
                        reply.writeFloat(_result9);
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        int _result10 = getFps(data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result10);
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        int _result11 = getMaxFps(data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result11);
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        int _result12 = getBrightness(data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result12);
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        int _result13 = getTexture(data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result13);
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        boolean deletePackageApsInfo = deletePackageApsInfo(data.readString());
                        reply.writeNoException();
                        reply.writeInt(deletePackageApsInfo ? 1 : 0);
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        int _result14 = isFeaturesEnabled(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result14);
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        boolean disableFeatures = disableFeatures(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(disableFeatures ? 1 : 0);
                        return true;
                    case 17:
                        data.enforceInterface(DESCRIPTOR);
                        boolean enableFeatures = enableFeatures(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(enableFeatures ? 1 : 0);
                        return true;
                    case 18:
                        data.enforceInterface(DESCRIPTOR);
                        List<ApsAppInfo> _result15 = getAllPackagesApsInfo();
                        reply.writeNoException();
                        reply.writeTypedList(_result15);
                        return true;
                    case 19:
                        data.enforceInterface(DESCRIPTOR);
                        List<String> _result16 = getAllApsPackages();
                        reply.writeNoException();
                        reply.writeStringList(_result16);
                        return true;
                    case 20:
                        data.enforceInterface(DESCRIPTOR);
                        boolean updateApsInfo = updateApsInfo(data.createTypedArrayList(ApsAppInfo.CREATOR));
                        reply.writeNoException();
                        reply.writeInt(updateApsInfo ? 1 : 0);
                        return true;
                    case 21:
                        data.enforceInterface(DESCRIPTOR);
                        boolean registerCallback = registerCallback(data.readString(), IApsManagerServiceCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(registerCallback ? 1 : 0);
                        return true;
                    case 22:
                        data.enforceInterface(DESCRIPTOR);
                        float _result17 = getSeviceVersion();
                        reply.writeNoException();
                        reply.writeFloat(_result17);
                        return true;
                    case 23:
                        data.enforceInterface(DESCRIPTOR);
                        boolean stopPackages = stopPackages(data.createStringArrayList());
                        reply.writeNoException();
                        reply.writeInt(stopPackages ? 1 : 0);
                        return true;
                    case 24:
                        data.enforceInterface(DESCRIPTOR);
                        int _result18 = setDynamicResolutionRatio(data.readString(), data.readFloat());
                        reply.writeNoException();
                        reply.writeInt(_result18);
                        return true;
                    case 25:
                        data.enforceInterface(DESCRIPTOR);
                        float _result19 = getDynamicResolutionRatio(data.readString());
                        reply.writeNoException();
                        reply.writeFloat(_result19);
                        return true;
                    case 26:
                        data.enforceInterface(DESCRIPTOR);
                        int _result20 = setDynamicFps(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result20);
                        return true;
                    case 27:
                        data.enforceInterface(DESCRIPTOR);
                        int _result21 = getDynamicFps(data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result21);
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
        public static class Proxy implements IHwApsManager {
            public static IHwApsManager sDefaultImpl;
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

            @Override // android.aps.IHwApsManager
            public int setResolution(String pkgName, float ratio, boolean switchable) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkgName);
                    _data.writeFloat(ratio);
                    _data.writeInt(switchable ? 1 : 0);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setResolution(pkgName, ratio, switchable);
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

            @Override // android.aps.IHwApsManager
            public int setLowResolutionMode(int lowResolutionMode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(lowResolutionMode);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setLowResolutionMode(lowResolutionMode);
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

            @Override // android.aps.IHwApsManager
            public int setFps(String pkgName, int fps) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkgName);
                    _data.writeInt(fps);
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setFps(pkgName, fps);
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

            @Override // android.aps.IHwApsManager
            public int setMaxFps(String pkgName, int fps) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkgName);
                    _data.writeInt(fps);
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setMaxFps(pkgName, fps);
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

            @Override // android.aps.IHwApsManager
            public int setBrightness(String pkgName, int ratioPercent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkgName);
                    _data.writeInt(ratioPercent);
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setBrightness(pkgName, ratioPercent);
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

            @Override // android.aps.IHwApsManager
            public int setTexture(String pkgName, int ratioPercent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkgName);
                    _data.writeInt(ratioPercent);
                    if (!this.mRemote.transact(6, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setTexture(pkgName, ratioPercent);
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

            @Override // android.aps.IHwApsManager
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
                    if (!this.mRemote.transact(7, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setPackageApsInfo(pkgName, info);
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

            @Override // android.aps.IHwApsManager
            public ApsAppInfo getPackageApsInfo(String pkgName) throws RemoteException {
                ApsAppInfo _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkgName);
                    if (!this.mRemote.transact(8, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getPackageApsInfo(pkgName);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ApsAppInfo.CREATOR.createFromParcel(_reply);
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

            @Override // android.aps.IHwApsManager
            public float getResolution(String pkgName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkgName);
                    if (!this.mRemote.transact(9, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getResolution(pkgName);
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

            @Override // android.aps.IHwApsManager
            public int getFps(String pkgName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkgName);
                    if (!this.mRemote.transact(10, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getFps(pkgName);
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

            @Override // android.aps.IHwApsManager
            public int getMaxFps(String pkgName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkgName);
                    if (!this.mRemote.transact(11, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getMaxFps(pkgName);
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

            @Override // android.aps.IHwApsManager
            public int getBrightness(String pkgName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkgName);
                    if (!this.mRemote.transact(12, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getBrightness(pkgName);
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

            @Override // android.aps.IHwApsManager
            public int getTexture(String pkgName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkgName);
                    if (!this.mRemote.transact(13, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getTexture(pkgName);
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

            @Override // android.aps.IHwApsManager
            public boolean deletePackageApsInfo(String pkgName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkgName);
                    boolean _result = false;
                    if (!this.mRemote.transact(14, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().deletePackageApsInfo(pkgName);
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

            @Override // android.aps.IHwApsManager
            public int isFeaturesEnabled(int bitmask) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(bitmask);
                    if (!this.mRemote.transact(15, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isFeaturesEnabled(bitmask);
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

            @Override // android.aps.IHwApsManager
            public boolean disableFeatures(int bitmask) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(bitmask);
                    boolean _result = false;
                    if (!this.mRemote.transact(16, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().disableFeatures(bitmask);
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

            @Override // android.aps.IHwApsManager
            public boolean enableFeatures(int bitmak) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(bitmak);
                    boolean _result = false;
                    if (!this.mRemote.transact(17, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().enableFeatures(bitmak);
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

            @Override // android.aps.IHwApsManager
            public List<ApsAppInfo> getAllPackagesApsInfo() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(18, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAllPackagesApsInfo();
                    }
                    _reply.readException();
                    List<ApsAppInfo> _result = _reply.createTypedArrayList(ApsAppInfo.CREATOR);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.aps.IHwApsManager
            public List<String> getAllApsPackages() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(19, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAllApsPackages();
                    }
                    _reply.readException();
                    List<String> _result = _reply.createStringArrayList();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.aps.IHwApsManager
            public boolean updateApsInfo(List<ApsAppInfo> infos) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeTypedList(infos);
                    boolean _result = false;
                    if (!this.mRemote.transact(20, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().updateApsInfo(infos);
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

            @Override // android.aps.IHwApsManager
            public boolean registerCallback(String pkgName, IApsManagerServiceCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkgName);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    boolean _result = false;
                    if (!this.mRemote.transact(21, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().registerCallback(pkgName, callback);
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

            @Override // android.aps.IHwApsManager
            public float getSeviceVersion() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(22, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getSeviceVersion();
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

            @Override // android.aps.IHwApsManager
            public boolean stopPackages(List<String> pkgs) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(pkgs);
                    boolean _result = false;
                    if (!this.mRemote.transact(23, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().stopPackages(pkgs);
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

            @Override // android.aps.IHwApsManager
            public int setDynamicResolutionRatio(String pkgName, float ratio) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkgName);
                    _data.writeFloat(ratio);
                    if (!this.mRemote.transact(24, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setDynamicResolutionRatio(pkgName, ratio);
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

            @Override // android.aps.IHwApsManager
            public float getDynamicResolutionRatio(String pkgName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkgName);
                    if (!this.mRemote.transact(25, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getDynamicResolutionRatio(pkgName);
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

            @Override // android.aps.IHwApsManager
            public int setDynamicFps(String pkgName, int fps) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkgName);
                    _data.writeInt(fps);
                    if (!this.mRemote.transact(26, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setDynamicFps(pkgName, fps);
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

            @Override // android.aps.IHwApsManager
            public int getDynamicFps(String pkgName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkgName);
                    if (!this.mRemote.transact(27, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getDynamicFps(pkgName);
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

        public static boolean setDefaultImpl(IHwApsManager impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IHwApsManager getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
