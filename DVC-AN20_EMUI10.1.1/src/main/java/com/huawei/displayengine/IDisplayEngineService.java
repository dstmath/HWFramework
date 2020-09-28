package com.huawei.displayengine;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.PersistableBundle;
import android.os.RemoteException;
import com.huawei.displayengine.IDisplayEngineCallback;

public interface IDisplayEngineService extends IInterface {
    public static final int COLOR_B = 2;
    public static final int COLOR_G = 1;
    public static final int COLOR_R = 0;
    public static final int COLOR_RGB_NUM = 3;
    public static final int DE_ACTION_ABORT = 51;
    public static final int DE_ACTION_AR_UNKNOWN = 18;
    public static final int DE_ACTION_AR_VEHICLE = 19;
    public static final int DE_ACTION_AR_WALK = 20;
    public static final int DE_ACTION_DALTONIAN_DEU = 25;
    public static final int DE_ACTION_DALTONIAN_PRO = 26;
    public static final int DE_ACTION_DALTONIAN_SIM_ALL = 31;
    public static final int DE_ACTION_DALTONIAN_SIM_DEU = 32;
    public static final int DE_ACTION_DALTONIAN_SIM_PRO = 33;
    public static final int DE_ACTION_DALTONIAN_SIM_TRI = 34;
    public static final int DE_ACTION_DALTONIAN_TRI = 27;
    public static final int DE_ACTION_ENTER = 14;
    public static final int DE_ACTION_EXIT = 15;
    public static final int DE_ACTION_FILM_FILTER_A1 = 36;
    public static final int DE_ACTION_FILM_FILTER_A2 = 37;
    public static final int DE_ACTION_FILM_FILTER_A3 = 38;
    public static final int DE_ACTION_FILM_FILTER_B1 = 39;
    public static final int DE_ACTION_FILM_FILTER_B2 = 40;
    public static final int DE_ACTION_FILM_FILTER_B3 = 41;
    public static final int DE_ACTION_FILM_FILTER_C1 = 42;
    public static final int DE_ACTION_FILM_FILTER_C2 = 43;
    public static final int DE_ACTION_FILM_FILTER_C3 = 44;
    public static final int DE_ACTION_FILM_FILTER_F1 = 45;
    public static final int DE_ACTION_FILM_FILTER_F2 = 46;
    public static final int DE_ACTION_FILM_FILTER_F3 = 47;
    public static final int DE_ACTION_FINISH = 50;
    public static final int DE_ACTION_FOLD_STATE_EXPAND = 0;
    public static final int DE_ACTION_FOLD_STATE_FOLDED = 1;
    public static final int DE_ACTION_FOLD_STATE_HALF_FOLDED = 2;
    public static final int DE_ACTION_FOLD_STATE_MAX = 3;
    public static final int DE_ACTION_FULLSCREEN_DOLBY = 35;
    public static final int DE_ACTION_FULLSCREEN_EXIT = 8;
    public static final int DE_ACTION_FULLSCREEN_PAUSE = 6;
    public static final int DE_ACTION_FULLSCREEN_RESUME = 7;
    public static final int DE_ACTION_FULLSCREEN_START = 4;
    public static final int DE_ACTION_FULLSCREEN_STOP = 5;
    public static final int DE_ACTION_FULLSCREEN_VIEW = 10;
    public static final int DE_ACTION_IMAGE_EXIT = 13;
    public static final int DE_ACTION_LIVE_IMAGE = 11;
    public static final int DE_ACTION_MAX = 52;
    public static final int DE_ACTION_MODE_OFF = 17;
    public static final int DE_ACTION_MODE_ON = 16;
    public static final int DE_ACTION_MODE_TIME_OFF = 49;
    public static final int DE_ACTION_MODE_TIME_ON = 48;
    public static final int DE_ACTION_MOTION_APP = 22;
    public static final int DE_ACTION_MOTION_HOME = 21;
    public static final int DE_ACTION_MOTION_RECENT = 23;
    public static final int DE_ACTION_MOTION_START = 24;
    public static final int DE_ACTION_ONLINE_FULLSCREEN_VIEW = 12;
    public static final int DE_ACTION_PAUSE = 2;
    public static final int DE_ACTION_PG_2DGAME_FRONT = 10011;
    public static final int DE_ACTION_PG_3DGAME_FRONT = 10002;
    public static final int DE_ACTION_PG_BROWSER_FRONT = 10001;
    public static final int DE_ACTION_PG_CAMERA_END = 10017;
    public static final int DE_ACTION_PG_CAMERA_FRONT = 10007;
    public static final int DE_ACTION_PG_DEFAULT_FRONT = 10000;
    public static final int DE_ACTION_PG_EBOOK_FRONT = 10003;
    public static final int DE_ACTION_PG_GALLERY_FRONT = 10004;
    public static final int DE_ACTION_PG_INPUT_END = 10006;
    public static final int DE_ACTION_PG_INPUT_START = 10005;
    public static final int DE_ACTION_PG_LAUNCHER_FRONT = 10010;
    public static final int DE_ACTION_PG_MAX = 10018;
    public static final int DE_ACTION_PG_MMS_FRONT = 10013;
    public static final int DE_ACTION_PG_OFFICE_FRONT = 10008;
    public static final int DE_ACTION_PG_VIDEO_END = 10016;
    public static final int DE_ACTION_PG_VIDEO_FRONT = 10009;
    public static final int DE_ACTION_PG_VIDEO_START = 10015;
    public static final int DE_ACTION_RESUME = 3;
    public static final int DE_ACTION_START = 0;
    public static final int DE_ACTION_STOP = 1;
    public static final int DE_ACTION_THUMBNAIL = 9;
    public static final int DE_ACTION_TOP1_GAME_ON = 29;
    public static final int DE_ACTION_TOP2_GAME_ON = 30;
    public static final int DE_ACTION_TOP_GAME_OFF = 28;
    public static final int DE_ALGORITHM_BRIGHTNESSTRAINING = 1;
    public static final int DE_ALGORITHM_IMAGEPROCESS = 0;
    public static final int DE_ALGORITHM_MAX = 2;
    public static final int DE_DATA_MAX = 16;
    public static final int DE_DATA_TYPE_3D_COLORTEMP = 7;
    public static final int DE_DATA_TYPE_ALPHA = 14;
    public static final int DE_DATA_TYPE_AMBIENTPARAM = 9;
    public static final int DE_DATA_TYPE_CAMERA = 3;
    public static final int DE_DATA_TYPE_FRAMERATE = 15;
    public static final int DE_DATA_TYPE_IAWARE = 10;
    public static final int DE_DATA_TYPE_IMAGE = 0;
    public static final int DE_DATA_TYPE_IMAGE_INFO = 4;
    public static final int DE_DATA_TYPE_MANUFACTURE_BRIGHTNESS = 11;
    public static final int DE_DATA_TYPE_SUPPORTED_FILTERS = 12;
    public static final int DE_DATA_TYPE_UD_FINGER_PRINT_BACKLIGHT = 13;
    public static final int DE_DATA_TYPE_VIDEO = 1;
    public static final int DE_DATA_TYPE_VIDEO_HDR10 = 2;
    public static final int DE_DATA_TYPE_XNIT = 5;
    public static final int DE_DATA_TYPE_XNIT_BRIGHTLEVEL = 6;
    public static final int DE_EFFECT_MAX = 11;
    public static final int DE_EFFECT_TYPE_CALIB_APPLY = 10;
    public static final int DE_EFFECT_TYPE_CALIB_CHECK = 7;
    public static final int DE_EFFECT_TYPE_CALIB_INFO = 6;
    public static final int DE_EFFECT_TYPE_CALIB_LVLS = 8;
    public static final int DE_EFFECT_TYPE_CALIB_TIME = 9;
    public static final int DE_EFFECT_TYPE_HBM_INFO = 1;
    public static final int DE_EFFECT_TYPE_IS_IMAGE = 4;
    public static final int DE_EFFECT_TYPE_PANEL_INFO = 2;
    public static final int DE_EFFECT_TYPE_PANEL_INFO_ALL = 5;
    public static final int DE_EFFECT_TYPE_PANEL_NAME = 0;
    public static final int DE_EFFECT_TYPE_PANEL_VERSION = 3;
    public static final int DE_EVENT_FOLDING_COMPENSATION = 0;
    public static final int DE_EVENT_FRAME_RATE = 1;
    public static final int DE_EVENT_MAX = 2;
    public static final int DE_FEATURE_3D_COLOR_TEMPERATURE = 18;
    public static final int DE_FEATURE_ACL = 24;
    public static final int DE_FEATURE_AMOLED = 25;
    public static final int DE_FEATURE_BLC = 2;
    public static final int DE_FEATURE_CABC = 12;
    public static final int DE_FEATURE_COLORMODE = 11;
    public static final int DE_FEATURE_COLOR_INVERSE = 28;
    public static final int DE_FEATURE_CONTRAST = 1;
    public static final int DE_FEATURE_DALTONIAN = 27;
    public static final int DE_FEATURE_DC_BRIGHTNESS_DIMMING = 32;
    public static final int DE_FEATURE_EYE_PROTECT = 17;
    public static final int DE_FEATURE_EYE_PROTECT_WITHCT = 21;
    public static final int DE_FEATURE_EYE_PROTECT_WITHGMP = 29;
    public static final int DE_FEATURE_FILM_COLOR_FILTER = 30;
    public static final int DE_FEATURE_FOLDINGCOMPENSATION = 33;
    public static final int DE_FEATURE_GAMMA = 7;
    public static final int DE_FEATURE_GMP = 3;
    public static final int DE_FEATURE_HBM = 20;
    public static final int DE_FEATURE_HDR10 = 15;
    public static final int DE_FEATURE_HUE = 5;
    public static final int DE_FEATURE_IGAMMA = 8;
    public static final int DE_FEATURE_LRE = 9;
    public static final int DE_FEATURE_MAX = 36;
    public static final int DE_FEATURE_NATURAL_TONE = 23;
    public static final int DE_FEATURE_PANELINFO = 14;
    public static final int DE_FEATURE_READING = 26;
    public static final int DE_FEATURE_READING_GLOBAL = 34;
    public static final int DE_FEATURE_RGBW = 13;
    public static final int DE_FEATURE_RGLED = 19;
    public static final int DE_FEATURE_SAT = 6;
    public static final int DE_FEATURE_SCREEN_TIME_CONTROL = 31;
    public static final int DE_FEATURE_SHARP = 0;
    public static final int DE_FEATURE_SHARP2P = 22;
    public static final int DE_FEATURE_SPR = 35;
    public static final int DE_FEATURE_SRE = 10;
    public static final int DE_FEATURE_XCC = 4;
    public static final int DE_FEATURE_XNIT = 16;
    public static final int DE_SCENE_AOD = 33;
    public static final int DE_SCENE_BACKLIGHT = 26;
    public static final int DE_SCENE_BACKLIGHT_CHANGE = 21;
    public static final int DE_SCENE_BOOT_CMPL = 18;
    public static final int DE_SCENE_CAMERA = 4;
    public static final int DE_SCENE_COLORMODE = 13;
    public static final int DE_SCENE_COLORTEMP = 11;
    public static final int DE_SCENE_COLOR_INVERSE = 40;
    public static final int DE_SCENE_DALTONIAN = 39;
    public static final int DE_SCENE_DC_BRIGHTNESS_DIMMING = 44;
    public static final int DE_SCENE_EYEPROTECTION = 15;
    public static final int DE_SCENE_FACTORY_GMP = 47;
    public static final int DE_SCENE_FILM_FILTER = 41;
    public static final int DE_SCENE_FINGERPRINT_HBM = 34;
    public static final int DE_SCENE_FINGER_PRINT = 29;
    public static final int DE_SCENE_FOLDABLE_STATE = 43;
    public static final int DE_SCENE_FOLD_CALIB = 46;
    public static final int DE_SCENE_GAME = 36;
    public static final int DE_SCENE_HBM_DIMMING = 28;
    public static final int DE_SCENE_IMAGE = 3;
    public static final int DE_SCENE_MAX = 49;
    public static final int DE_SCENE_MMITEST = 35;
    public static final int DE_SCENE_MOTION = 38;
    public static final int DE_SCENE_NATURAL_TONE = 25;
    public static final int DE_SCENE_PG = 0;
    public static final int DE_SCENE_PG_EX = 17;
    public static final int DE_SCENE_POWERMODE = 10;
    public static final int DE_SCENE_PROCAMERA = 14;
    public static final int DE_SCENE_READMODE = 37;
    public static final int DE_SCENE_READMODE_FROMPG = 45;
    public static final int DE_SCENE_READMODE_GLOBAL = 48;
    public static final int DE_SCENE_REAL_POWERMODE = 24;
    public static final int DE_SCENE_RGLED = 20;
    public static final int DE_SCENE_SCREEN_TIME_CONTROL = 42;
    public static final int DE_SCENE_SRE = 12;
    public static final int DE_SCENE_UD_ENROLL_FINGER_PRINT = 31;
    public static final int DE_SCENE_UD_FINGER_PRINT_LOCK = 30;
    public static final int DE_SCENE_UD_USER_PRESENT = 32;
    public static final int DE_SCENE_UI = 5;
    public static final int DE_SCENE_VIDEO = 1;
    public static final int DE_SCENE_VIDEO_APP = 23;
    public static final int DE_SCENE_VIDEO_HDR10 = 2;
    public static final int DE_SCENE_WEB = 6;
    public static final int FOLD_CALIB_INFO_COLOR = 1;
    public static final int FOLD_CALIB_INFO_GAIN = 3;
    public static final int FOLD_CALIB_INFO_LEVEL = 0;
    public static final int FOLD_CALIB_INFO_NUM = 4;
    public static final int FOLD_CALIB_INFO_REGION = 2;
    public static final int FOLD_CALIB_LEVEL_NUM = 3;
    public static final int FOLD_PANEL_REGION_DIRECTION = 2;
    public static final int FOLD_PANEL_REGION_LINE_1 = 0;
    public static final int FOLD_PANEL_REGION_LINE_2 = 1;
    public static final int FOLD_PANEL_REGION_NUM = 3;
    public static final int PANEL_DIRECTION_BOTTOM = 2;
    public static final int PANEL_DIRECTION_LEFT = 1;
    public static final int PANEL_DIRECTION_RIGHT = 3;
    public static final int PANEL_DIRECTION_TOP = 0;

    int getEffect(int i, int i2, byte[] bArr, int i3) throws RemoteException;

    int getEffectEx(int i, int i2, int[] iArr, int i3) throws RemoteException;

    int getSupported(int i) throws RemoteException;

    void registerCallback(IDisplayEngineCallback iDisplayEngineCallback) throws RemoteException;

    int setData(int i, PersistableBundle persistableBundle) throws RemoteException;

    int setEffect(int i, int i2, PersistableBundle persistableBundle) throws RemoteException;

    int setScene(int i, int i2) throws RemoteException;

    void unregisterCallback(IDisplayEngineCallback iDisplayEngineCallback) throws RemoteException;

    public static class Default implements IDisplayEngineService {
        @Override // com.huawei.displayengine.IDisplayEngineService
        public int getSupported(int feature) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.displayengine.IDisplayEngineService
        public int setScene(int scene, int action) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.displayengine.IDisplayEngineService
        public int setData(int type, PersistableBundle data) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.displayengine.IDisplayEngineService
        public int getEffectEx(int feature, int type, int[] status, int length) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.displayengine.IDisplayEngineService
        public int getEffect(int feature, int type, byte[] status, int length) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.displayengine.IDisplayEngineService
        public int setEffect(int feature, int mode, PersistableBundle data) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.displayengine.IDisplayEngineService
        public void registerCallback(IDisplayEngineCallback cb) throws RemoteException {
        }

        @Override // com.huawei.displayengine.IDisplayEngineService
        public void unregisterCallback(IDisplayEngineCallback cb) throws RemoteException {
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IDisplayEngineService {
        private static final String DESCRIPTOR = "com.huawei.displayengine.IDisplayEngineService";
        static final int TRANSACTION_getEffect = 5;
        static final int TRANSACTION_getEffectEx = 4;
        static final int TRANSACTION_getSupported = 1;
        static final int TRANSACTION_registerCallback = 7;
        static final int TRANSACTION_setData = 3;
        static final int TRANSACTION_setEffect = 6;
        static final int TRANSACTION_setScene = 2;
        static final int TRANSACTION_unregisterCallback = 8;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IDisplayEngineService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IDisplayEngineService)) {
                return new Proxy(obj);
            }
            return (IDisplayEngineService) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            PersistableBundle _arg1;
            PersistableBundle _arg2;
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        int _result = getSupported(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        int _result2 = setScene(data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result2);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg0 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg1 = (PersistableBundle) PersistableBundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg1 = null;
                        }
                        int _result3 = setData(_arg0, _arg1);
                        reply.writeNoException();
                        reply.writeInt(_result3);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg02 = data.readInt();
                        int _arg12 = data.readInt();
                        int[] _arg22 = data.createIntArray();
                        int _result4 = getEffectEx(_arg02, _arg12, _arg22, data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result4);
                        reply.writeIntArray(_arg22);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg03 = data.readInt();
                        int _arg13 = data.readInt();
                        byte[] _arg23 = data.createByteArray();
                        int _result5 = getEffect(_arg03, _arg13, _arg23, data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result5);
                        reply.writeByteArray(_arg23);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg04 = data.readInt();
                        int _arg14 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg2 = (PersistableBundle) PersistableBundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg2 = null;
                        }
                        int _result6 = setEffect(_arg04, _arg14, _arg2);
                        reply.writeNoException();
                        reply.writeInt(_result6);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        registerCallback(IDisplayEngineCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        unregisterCallback(IDisplayEngineCallback.Stub.asInterface(data.readStrongBinder()));
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
        public static class Proxy implements IDisplayEngineService {
            public static IDisplayEngineService sDefaultImpl;
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

            @Override // com.huawei.displayengine.IDisplayEngineService
            public int getSupported(int feature) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(feature);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getSupported(feature);
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

            @Override // com.huawei.displayengine.IDisplayEngineService
            public int setScene(int scene, int action) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(scene);
                    _data.writeInt(action);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setScene(scene, action);
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

            @Override // com.huawei.displayengine.IDisplayEngineService
            public int setData(int type, PersistableBundle data) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(type);
                    if (data != null) {
                        _data.writeInt(1);
                        data.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setData(type, data);
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

            @Override // com.huawei.displayengine.IDisplayEngineService
            public int getEffectEx(int feature, int type, int[] status, int length) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(feature);
                    _data.writeInt(type);
                    _data.writeIntArray(status);
                    _data.writeInt(length);
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getEffectEx(feature, type, status, length);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.readIntArray(status);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.displayengine.IDisplayEngineService
            public int getEffect(int feature, int type, byte[] status, int length) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(feature);
                    _data.writeInt(type);
                    _data.writeByteArray(status);
                    _data.writeInt(length);
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getEffect(feature, type, status, length);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.readByteArray(status);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.displayengine.IDisplayEngineService
            public int setEffect(int feature, int mode, PersistableBundle data) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(feature);
                    _data.writeInt(mode);
                    if (data != null) {
                        _data.writeInt(1);
                        data.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(6, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setEffect(feature, mode, data);
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

            @Override // com.huawei.displayengine.IDisplayEngineService
            public void registerCallback(IDisplayEngineCallback cb) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(cb != null ? cb.asBinder() : null);
                    if (this.mRemote.transact(7, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().registerCallback(cb);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.displayengine.IDisplayEngineService
            public void unregisterCallback(IDisplayEngineCallback cb) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(cb != null ? cb.asBinder() : null);
                    if (this.mRemote.transact(8, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().unregisterCallback(cb);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IDisplayEngineService impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IDisplayEngineService getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
