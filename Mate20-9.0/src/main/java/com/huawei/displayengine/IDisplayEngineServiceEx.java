package com.huawei.displayengine;

import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.PersistableBundle;
import android.os.RemoteException;
import java.util.List;

public interface IDisplayEngineServiceEx extends IInterface {
    public static final int DE_ACTION_AR_UNKNOWN = 18;
    public static final int DE_ACTION_AR_VEHICLE = 19;
    public static final int DE_ACTION_AR_WALK = 20;
    public static final int DE_ACTION_DALTONIAN_DEU = 25;
    public static final int DE_ACTION_DALTONIAN_PRO = 26;
    public static final int DE_ACTION_DALTONIAN_TRI = 27;
    public static final int DE_ACTION_ENTER = 14;
    public static final int DE_ACTION_EXIT = 15;
    public static final int DE_ACTION_FULLSCREEN_EXIT = 8;
    public static final int DE_ACTION_FULLSCREEN_PAUSE = 6;
    public static final int DE_ACTION_FULLSCREEN_RESUME = 7;
    public static final int DE_ACTION_FULLSCREEN_START = 4;
    public static final int DE_ACTION_FULLSCREEN_STOP = 5;
    public static final int DE_ACTION_FULLSCREEN_VIEW = 10;
    public static final int DE_ACTION_IMAGE_EXIT = 13;
    public static final int DE_ACTION_LIVE_IMAGE = 11;
    public static final int DE_ACTION_MAX = 31;
    public static final int DE_ACTION_MODE_OFF = 17;
    public static final int DE_ACTION_MODE_ON = 16;
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
    public static final int DE_DATA_MAX = 12;
    public static final int DE_DATA_TYPE_3D_COLORTEMP = 7;
    public static final int DE_DATA_TYPE_AMBIENTPARAM = 9;
    public static final int DE_DATA_TYPE_CAMERA = 3;
    public static final int DE_DATA_TYPE_IAWARE = 10;
    public static final int DE_DATA_TYPE_IMAGE = 0;
    public static final int DE_DATA_TYPE_IMAGE_INFO = 4;
    public static final int DE_DATA_TYPE_MANUFACTURE_BRIGHTNESS = 11;
    public static final int DE_DATA_TYPE_VIDEO = 1;
    public static final int DE_DATA_TYPE_VIDEO_HDR10 = 2;
    public static final int DE_DATA_TYPE_XNIT = 5;
    public static final int DE_DATA_TYPE_XNIT_BRIGHTLEVEL = 6;
    public static final int DE_EFFECT_MAX = 5;
    public static final int DE_EFFECT_TYPE_HBM_INFO = 1;
    public static final int DE_EFFECT_TYPE_IS_IMAGE = 4;
    public static final int DE_EFFECT_TYPE_PANEL_INFO = 2;
    public static final int DE_EFFECT_TYPE_PANEL_NAME = 0;
    public static final int DE_EFFECT_TYPE_PANEL_VERSION = 3;
    public static final int DE_FEATURE_3D_COLOR_TEMPERATURE = 18;
    public static final int DE_FEATURE_ACL = 24;
    public static final int DE_FEATURE_AMOLED = 25;
    public static final int DE_FEATURE_BLC = 2;
    public static final int DE_FEATURE_CABC = 12;
    public static final int DE_FEATURE_COLORMODE = 11;
    public static final int DE_FEATURE_COLOR_INVERSE = 28;
    public static final int DE_FEATURE_CONTRAST = 1;
    public static final int DE_FEATURE_DALTONIAN = 27;
    public static final int DE_FEATURE_DC_BRIGHTNESS_DIMMING = 30;
    public static final int DE_FEATURE_EYE_PROTECT = 17;
    public static final int DE_FEATURE_EYE_PROTECT_WITHCT = 21;
    public static final int DE_FEATURE_EYE_PROTECT_WITHGMP = 29;
    public static final int DE_FEATURE_GAMMA = 7;
    public static final int DE_FEATURE_GMP = 3;
    public static final int DE_FEATURE_HBM = 20;
    public static final int DE_FEATURE_HDR10 = 15;
    public static final int DE_FEATURE_HUE = 5;
    public static final int DE_FEATURE_IGAMMA = 8;
    public static final int DE_FEATURE_LRE = 9;
    public static final int DE_FEATURE_MAX = 32;
    public static final int DE_FEATURE_NATURAL_TONE = 23;
    public static final int DE_FEATURE_PANELINFO = 14;
    public static final int DE_FEATURE_READING = 26;
    public static final int DE_FEATURE_RGBW = 13;
    public static final int DE_FEATURE_RGLED = 19;
    public static final int DE_FEATURE_SAT = 6;
    public static final int DE_FEATURE_SCREEN_TIME_CONTROL = 31;
    public static final int DE_FEATURE_SHARP = 0;
    public static final int DE_FEATURE_SHARP2P = 22;
    public static final int DE_FEATURE_SRE = 10;
    public static final int DE_FEATURE_XCC = 4;
    public static final int DE_FEATURE_XNIT = 16;
    public static final int DE_MESSAGE_ID_CUSTOM = 2;
    public static final int DE_MESSAGE_ID_MAX = 3;
    public static final int DE_SCENE_AOD = 33;
    public static final int DE_SCENE_BACKLIGHT = 26;
    public static final int DE_SCENE_BACKLIGHT_CHANGE = 21;
    public static final int DE_SCENE_BOOT_CMPL = 18;
    public static final int DE_SCENE_CAMERA = 4;
    public static final int DE_SCENE_COLORMODE = 13;
    public static final int DE_SCENE_COLORTEMP = 11;
    public static final int DE_SCENE_COLOR_INVERSE = 40;
    public static final int DE_SCENE_DALTONIAN = 39;
    public static final int DE_SCENE_DC_BRIGHTNESS_DIMMING = 41;
    public static final int DE_SCENE_EYEPROTECTION = 15;
    public static final int DE_SCENE_FINGERPRINT_HBM = 34;
    public static final int DE_SCENE_FINGER_PRINT = 29;
    public static final int DE_SCENE_GAME = 36;
    public static final int DE_SCENE_HBM_DIMMING = 28;
    public static final int DE_SCENE_IMAGE = 3;
    public static final int DE_SCENE_MAX = 43;
    public static final int DE_SCENE_MMITEST = 35;
    public static final int DE_SCENE_MOTION = 38;
    public static final int DE_SCENE_NATURAL_TONE = 25;
    public static final int DE_SCENE_PG = 0;
    public static final int DE_SCENE_PG_EX = 17;
    public static final int DE_SCENE_POWERMODE = 10;
    public static final int DE_SCENE_PROCAMERA = 14;
    public static final int DE_SCENE_QQ = 8;
    public static final int DE_SCENE_READMODE = 37;
    public static final int DE_SCENE_REAL_POWERMODE = 24;
    public static final int DE_SCENE_RGLED = 20;
    public static final int DE_SCENE_SCREEN_TIME_CONTROL = 42;
    public static final int DE_SCENE_SRE = 12;
    public static final int DE_SCENE_TAOBAO = 9;
    public static final int DE_SCENE_UD_ENROLL_FINGER_PRINT = 31;
    public static final int DE_SCENE_UD_FINGER_PRINT_LOCK = 30;
    public static final int DE_SCENE_UD_USER_PRESENT = 32;
    public static final int DE_SCENE_UI = 5;
    public static final int DE_SCENE_VIDEO = 1;
    public static final int DE_SCENE_VIDEO_APP = 23;
    public static final int DE_SCENE_VIDEO_HDR10 = 2;
    public static final int DE_SCENE_WEB = 6;
    public static final int DE_SCENE_WECHAT = 7;

    public static abstract class Stub extends Binder implements IDisplayEngineServiceEx {
        private static final String DESCRIPTOR = "com.huawei.displayengine.IDisplayEngineServiceEx";
        static final int TRANSACTION_getAllRecords = 8;
        static final int TRANSACTION_getEffect = 5;
        static final int TRANSACTION_getSupported = 1;
        static final int TRANSACTION_sendMessage = 4;
        static final int TRANSACTION_setData = 3;
        static final int TRANSACTION_setEffect = 6;
        static final int TRANSACTION_setScene = 2;
        static final int TRANSACTION_updateLightSensorState = 7;

        private static class Proxy implements IDisplayEngineServiceEx {
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

            public int getSupported(int feature) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(feature);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int setScene(int scene, int action) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(scene);
                    _data.writeInt(action);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

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
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int sendMessage(int messageID, Bundle data) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(messageID);
                    if (data != null) {
                        _data.writeInt(1);
                        data.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getEffect(int feature, int type, byte[] status, int length) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(feature);
                    _data.writeInt(type);
                    _data.writeByteArray(status);
                    _data.writeInt(length);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.readByteArray(status);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

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
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void updateLightSensorState(boolean sensorEnable) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(sensorEnable);
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<Bundle> getAllRecords(String name, Bundle info) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(name);
                    if (info != null) {
                        _data.writeInt(1);
                        info.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createTypedArrayList(Bundle.CREATOR);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IDisplayEngineServiceEx asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IDisplayEngineServiceEx)) {
                return new Proxy(obj);
            }
            return (IDisplayEngineServiceEx) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v4, resolved type: android.os.PersistableBundle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v8, resolved type: android.os.Bundle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v13, resolved type: android.os.PersistableBundle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v21, resolved type: android.os.Bundle} */
        /* JADX WARNING: type inference failed for: r1v1 */
        /* JADX WARNING: type inference failed for: r1v26 */
        /* JADX WARNING: type inference failed for: r1v27 */
        /* JADX WARNING: type inference failed for: r1v28 */
        /* JADX WARNING: type inference failed for: r1v29 */
        /* JADX WARNING: Multi-variable type inference failed */
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code != 1598968902) {
                ? _arg1 = 0;
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
                        }
                        int _result3 = setData(_arg0, _arg1);
                        reply.writeNoException();
                        reply.writeInt(_result3);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg02 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg1 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        }
                        int _result4 = sendMessage(_arg02, _arg1);
                        reply.writeNoException();
                        reply.writeInt(_result4);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg03 = data.readInt();
                        int _arg12 = data.readInt();
                        byte[] _arg2 = data.createByteArray();
                        int _result5 = getEffect(_arg03, _arg12, _arg2, data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result5);
                        reply.writeByteArray(_arg2);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg04 = data.readInt();
                        int _arg13 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg1 = (PersistableBundle) PersistableBundle.CREATOR.createFromParcel(data);
                        }
                        int _result6 = setEffect(_arg04, _arg13, _arg1);
                        reply.writeNoException();
                        reply.writeInt(_result6);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        updateLightSensorState(data.readInt() != 0);
                        reply.writeNoException();
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg05 = data.readString();
                        if (data.readInt() != 0) {
                            _arg1 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        }
                        List<Bundle> _result7 = getAllRecords(_arg05, _arg1);
                        reply.writeNoException();
                        reply.writeTypedList(_result7);
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

    List<Bundle> getAllRecords(String str, Bundle bundle) throws RemoteException;

    int getEffect(int i, int i2, byte[] bArr, int i3) throws RemoteException;

    int getSupported(int i) throws RemoteException;

    int sendMessage(int i, Bundle bundle) throws RemoteException;

    int setData(int i, PersistableBundle persistableBundle) throws RemoteException;

    int setEffect(int i, int i2, PersistableBundle persistableBundle) throws RemoteException;

    int setScene(int i, int i2) throws RemoteException;

    void updateLightSensorState(boolean z) throws RemoteException;
}
