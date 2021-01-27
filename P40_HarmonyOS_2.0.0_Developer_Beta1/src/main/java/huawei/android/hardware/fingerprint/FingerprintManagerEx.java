package huawei.android.hardware.fingerprint;

import android.content.Context;
import android.graphics.Rect;
import android.hardware.fingerprint.Fingerprint;
import android.hardware.fingerprint.FingerprintManager;
import android.hardware.fingerprint.IFingerprintService;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.util.Log;
import com.huawei.fingerprint.Authenticator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FingerprintManagerEx {
    private static final int CODE_DISABLE_FINGERPRINT_VIEW = 1114;
    private static final int CODE_ENABLE_FINGERPRINT_VIEW = 1115;
    private static final int CODE_FINGERPRINT_LOGO_POSITION = 1130;
    private static final int CODE_FINGERPRINT_LOGO_RADIUS = 1129;
    private static final int CODE_FINGERPRINT_MMI_TEST = 1131;
    private static final int CODE_FINGERPRINT_WEATHER_DATA = 1128;
    private static final int CODE_GET_FINGERPRINT_LIST_ENROLLED = 1118;
    private static final int CODE_GET_HARDWARE_POSITION = 1110;
    private static final int CODE_GET_HARDWARE_TYPE = 1109;
    private static final int CODE_GET_HIGHLIGHT_SPOT_RADIUS = 1122;
    private static final int CODE_GET_HOVER_SUPPORT = 1113;
    private static final int CODE_GET_TOKEN_LEN = 1103;
    private static final int CODE_IS_FINGERPRINT_HARDWARE_DETECTED = 1119;
    private static final int CODE_IS_FP_NEED_CALIBRATE = 1101;
    private static final int CODE_IS_SUPPORT_DUAL_FINGERPRINT = 1120;
    private static final int CODE_KEEP_MASK_SHOW_AFTER_AUTHENTICATION = 1116;
    private static final int CODE_NOTIFY_OPTICAL_CAPTURE = 1111;
    private static final int CODE_REMOVE_FINGERPRINT = 1107;
    private static final int CODE_REMOVE_MASK_AND_SHOW_CANCEL = 1117;
    private static final int CODE_SET_CALIBRATE_MODE = 1102;
    private static final int CODE_SET_FINGERPRINT_MASK_VIEW = 1104;
    private static final int CODE_SET_HOVER_SWITCH = 1112;
    private static final int CODE_SHOW_FINGERPRINT_BUTTON = 1106;
    private static final int CODE_SHOW_FINGERPRINT_VIEW = 1105;
    private static final int CODE_SUSPEND_AUTHENTICATE = 1108;
    private static final int CODE_SUSPEND_ENROLL = 1123;
    private static final int CODE_UDFINGERPRINT_SPOTCOLOR = 1124;
    private static final String DESCRIPTOR_FINGERPRINT_SERVICE = "android.hardware.fingerprint.IFingerprintService";
    private static final int FINGERPRINT_BACK_ULTRASONIC = 0;
    private static final int FINGERPRINT_FRONT_ULTRASONIC = 1;
    private static final int FINGERPRINT_HARDWARE_OPTICAL = 1;
    private static final int FINGERPRINT_HARDWARE_OUTSCREEN = 0;
    private static final int FINGERPRINT_HARDWARE_ULTRASONIC = 2;
    private static final int FINGERPRINT_NOT_ULTRASONIC = -1;
    private static final int FINGERPRINT_POSITION_BOTTOM_RIGHT_X = 2;
    private static final int FINGERPRINT_POSITION_BOTTOM_RIGHT_Y = 3;
    private static final int FINGERPRINT_POSITION_TOP_LEFT_X = 0;
    private static final int FINGERPRINT_POSITION_TOP_LEFT_Y = 1;
    private static final int FINGERPRINT_SLIDE_ULTRASONIC = 3;
    private static final int FINGERPRINT_UNDER_DISPLAY_ULTRASONIC = 2;
    private static final int FLAG_FINGERPRINT_LOCATION_BACK = 1;
    private static final int FLAG_FINGERPRINT_LOCATION_FRONT = 2;
    private static final int FLAG_FINGERPRINT_LOCATION_SLIDE = 8;
    private static final int FLAG_FINGERPRINT_LOCATION_UNDER_DISPLAY = 4;
    private static final int FLAG_FINGERPRINT_POSITION_MASK = 65535;
    private static final int FLAG_FINGERPRINT_TYPE_CAPACITANCE = 1;
    private static final int FLAG_FINGERPRINT_TYPE_MASK = 15;
    private static final int FLAG_FINGERPRINT_TYPE_OPTICAL = 2;
    private static final int FLAG_FINGERPRINT_TYPE_ULTRASONIC = 3;
    private static final int FRONT_FINGERPRINT_NAVIGATION_TRIKEY = SystemProperties.getInt("ro.config.hw_front_fp_trikey", 0);
    private static final int HOVER_HARDWARE_NOT_SUPPORT = 0;
    private static final int HOVER_HARDWARE_SUPPORT = 1;
    private static final int INVALID_VALUE = -1;
    private static final boolean IS_FRONT_FINGERPRINT_NAVIGATION = SystemProperties.getBoolean("ro.config.hw_front_fp_navi", false);
    private static final int MSG_BINDER_SUCCESS_FLAG = 170;
    private static final int MSG_MMI_UD_UI_LOGO_SIZE = 910;
    private static final String TAG = "FingerprintManagerEx";
    private static int mDetailsType = -1;
    private static HashMap<Integer, Integer> mHardwareInfo = new HashMap<>();
    private static int mType = -1;
    private static int[] sPosition = {-1, -1, -1, -1};
    private static int[] sSpotPosition = {-1, -1, -1, -1};
    private Authenticator authenticator = Authenticator.getAuthenticator();
    private FingerprintManager fingerprintManager;
    private Context mContext;
    private IFingerprintService mService = IFingerprintService.Stub.asInterface(ServiceManager.getService("fingerprint"));

    public interface AuthenticatorListener {
        void onIsUserIDValidResult(boolean z);

        void onUserVerificationResult(int i, byte[] bArr, byte[] bArr2);
    }

    public FingerprintManagerEx(Context context) {
        this.mContext = context;
    }

    public int getRemainingNum() {
        IFingerprintService iFingerprintService = this.mService;
        if (iFingerprintService == null) {
            return -1;
        }
        try {
            return iFingerprintService.getRemainingNum();
        } catch (RemoteException e) {
            Log.w(TAG, "Remote exception in getRemainingNum");
            return -1;
        }
    }

    public long getRemainingTime() {
        IFingerprintService iFingerprintService = this.mService;
        if (iFingerprintService == null) {
            return 0;
        }
        try {
            return iFingerprintService.getRemainingTime();
        } catch (RemoteException e) {
            Log.w(TAG, "Remote exception in getRemainingTime: ", e);
            return 0;
        }
    }

    public static boolean isFpNeedCalibrate() {
        if (FRONT_FINGERPRINT_NAVIGATION_TRIKEY != 0 || !IS_FRONT_FINGERPRINT_NAVIGATION) {
            return false;
        }
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        IBinder fingerprintService = ServiceManager.getService("fingerprint");
        int result = -1;
        if (fingerprintService != null) {
            try {
                data.writeInterfaceToken(DESCRIPTOR_FINGERPRINT_SERVICE);
                fingerprintService.transact(1101, data, reply, 0);
                reply.readException();
                result = reply.readInt();
            } catch (RemoteException e) {
                Log.e(TAG, "isFpNeedCalibrate with RemoteException happened");
            } catch (Throwable th) {
                reply.recycle();
                data.recycle();
                throw th;
            }
        }
        reply.recycle();
        data.recycle();
        Log.d(TAG, "isFpNeedCalibrate result: " + result);
        if (result == 1) {
            return true;
        }
        return false;
    }

    public static void setCalibrateMode(int mode) {
        Log.d(TAG, "setCalibrateMode: " + mode);
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        IBinder fingerprintService = ServiceManager.getService("fingerprint");
        if (fingerprintService != null) {
            try {
                data.writeInterfaceToken(DESCRIPTOR_FINGERPRINT_SERVICE);
                data.writeInt(mode);
                fingerprintService.transact(1102, data, reply, 0);
                reply.readException();
            } catch (RemoteException e) {
                Log.e(TAG, "setCalibrateMode with RemoteException happened");
            } catch (Throwable th) {
                reply.recycle();
                data.recycle();
                throw th;
            }
        }
        reply.recycle();
        data.recycle();
    }

    public static int getTokenLen() {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        IBinder fingerprintService = ServiceManager.getService("fingerprint");
        int len = -1;
        if (fingerprintService != null) {
            try {
                data.writeInterfaceToken(DESCRIPTOR_FINGERPRINT_SERVICE);
                fingerprintService.transact(1103, data, reply, 0);
                reply.readException();
                len = reply.readInt();
            } catch (RemoteException e) {
                Log.e(TAG, "getTokenLen with RemoteException happened");
            } catch (Throwable th) {
                reply.recycle();
                data.recycle();
                throw th;
            }
        }
        reply.recycle();
        data.recycle();
        Log.d(TAG, "getTokenLen len: " + len);
        return len;
    }

    public static void setFingerprintMaskView(Bundle bundle) {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        IBinder fingerprintService = ServiceManager.getService("fingerprint");
        if (fingerprintService != null) {
            try {
                data.writeInterfaceToken(DESCRIPTOR_FINGERPRINT_SERVICE);
                data.writeBundle(bundle);
                fingerprintService.transact(1104, data, reply, 0);
                reply.readException();
            } catch (RemoteException e) {
                Log.e(TAG, "setFingerprintMaskView with RemoteException happened");
            } catch (Throwable th) {
                reply.recycle();
                data.recycle();
                throw th;
            }
        }
        reply.recycle();
        data.recycle();
    }

    public static void showFingerprintView() {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        IBinder fingerprintService = ServiceManager.getService("fingerprint");
        if (fingerprintService != null) {
            try {
                data.writeInterfaceToken(DESCRIPTOR_FINGERPRINT_SERVICE);
                fingerprintService.transact(CODE_SHOW_FINGERPRINT_VIEW, data, reply, 0);
                reply.readException();
            } catch (RemoteException e) {
                Log.e(TAG, "showFingerprintView with RemoteException happened");
            } catch (Throwable th) {
                reply.recycle();
                data.recycle();
                throw th;
            }
        }
        reply.recycle();
        data.recycle();
    }

    public static void showSuspensionButton(int centerX, int centerY) {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        IBinder fingerprintService = ServiceManager.getService("fingerprint");
        if (fingerprintService != null) {
            try {
                data.writeInterfaceToken(DESCRIPTOR_FINGERPRINT_SERVICE);
                data.writeInt(centerX);
                data.writeInt(centerY);
                fingerprintService.transact(CODE_SHOW_FINGERPRINT_BUTTON, data, reply, 0);
                reply.readException();
            } catch (RemoteException e) {
                Log.e(TAG, "showSuspensionButton with RemoteException happened");
            } catch (Throwable th) {
                reply.recycle();
                data.recycle();
                throw th;
            }
        }
        reply.recycle();
        data.recycle();
    }

    public static void removeFingerView() {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        IBinder fingerprintService = ServiceManager.getService("fingerprint");
        if (fingerprintService != null) {
            try {
                data.writeInterfaceToken(DESCRIPTOR_FINGERPRINT_SERVICE);
                fingerprintService.transact(CODE_REMOVE_FINGERPRINT, data, reply, 0);
                reply.readException();
            } catch (RemoteException e) {
                Log.e(TAG, "removeFingerView with RemoteException happened");
            } catch (Throwable th) {
                reply.recycle();
                data.recycle();
                throw th;
            }
        }
        reply.recycle();
        data.recycle();
    }

    public static void suspendAuthentication(int status) {
        if (!hasFingerprintInScreen()) {
            Log.w(TAG, "do not have UD device suspend invalid");
        } else {
            Log.d(TAG, "suspendAuthentication: " + status);
        }
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        IBinder fingerprintService = ServiceManager.getService("fingerprint");
        if (fingerprintService != null) {
            try {
                data.writeInterfaceToken(DESCRIPTOR_FINGERPRINT_SERVICE);
                data.writeInt(status);
                fingerprintService.transact(CODE_SUSPEND_AUTHENTICATE, data, reply, 0);
                reply.readException();
            } catch (RemoteException e) {
                Log.e(TAG, "suspendAuthentication with RemoteException happened");
            } catch (Throwable th) {
                reply.recycle();
                data.recycle();
                throw th;
            }
        }
        reply.recycle();
        data.recycle();
    }

    public static int suspendEnroll(int status) {
        int result = -1;
        if (!hasFingerprintInScreen()) {
            Log.w(TAG, "do not have UD device suspend invalid");
            return -1;
        }
        Log.d(TAG, "suspendEnroll: " + status);
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        IBinder fingerprintService = ServiceManager.getService("fingerprint");
        if (fingerprintService != null) {
            try {
                data.writeInterfaceToken(DESCRIPTOR_FINGERPRINT_SERVICE);
                data.writeInt(status);
                fingerprintService.transact(CODE_SUSPEND_ENROLL, data, reply, 0);
                reply.readException();
                result = reply.readInt();
            } catch (RemoteException e) {
                Log.e(TAG, "suspendEnroll with RemoteException happened");
            } catch (Throwable th) {
                reply.recycle();
                data.recycle();
                throw th;
            }
        }
        reply.recycle();
        data.recycle();
        return result;
    }

    public static int getHardwareType() {
        int type = mType;
        if (type != -1) {
            return type;
        }
        mHardwareInfo = getHardwareInfo();
        if (mHardwareInfo.isEmpty()) {
            int type2 = SystemProperties.getInt("persist.sys.fingerprint.hardwareType", -1);
            Log.d(TAG, "use SystemProperties type :" + type2);
            return type2;
        }
        if (mHardwareInfo.containsKey(4)) {
            int physical = Integer.parseInt(mHardwareInfo.get(4).toString());
            if (physical == 2) {
                type = 1;
            } else if (physical == 3) {
                type = 2;
            }
            Log.d(TAG, "LOCATION_UNDER_DISPLAY :" + physical);
        } else {
            type = 0;
        }
        mType = type;
        Log.d(TAG, "type:" + type);
        return type;
    }

    public static int getUltrasonicFingerprintType() {
        int type = -1;
        if (mHardwareInfo.isEmpty()) {
            mHardwareInfo = getHardwareInfo();
        }
        if (mHardwareInfo.containsKey(2)) {
            if (mHardwareInfo.get(2).intValue() == 3) {
                type = 1;
            }
        } else if (mHardwareInfo.containsKey(1)) {
            if (mHardwareInfo.get(1).intValue() == 3) {
                type = 0;
            }
        } else if (mHardwareInfo.containsKey(4)) {
            if (mHardwareInfo.get(4).intValue() == 3) {
                type = 2;
            }
        } else if (mHardwareInfo.containsKey(8) && mHardwareInfo.get(8).intValue() == 3) {
            type = 3;
        }
        Log.d(TAG, "getUltrasonicFingerprintType :" + type);
        return type;
    }

    public static boolean isSupportDualFingerprint() {
        Log.d(TAG, "isSupportDualFingerprint called.");
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        IBinder fingerprintService = ServiceManager.getService("fingerprint");
        if (fingerprintService != null) {
            try {
                data.writeInterfaceToken(DESCRIPTOR_FINGERPRINT_SERVICE);
                fingerprintService.transact(CODE_IS_SUPPORT_DUAL_FINGERPRINT, data, reply, 0);
                reply.readException();
                boolean isSupportDualFp = reply.readBoolean();
                Log.d(TAG, "isSupportDualFingerprint is: " + isSupportDualFp);
                reply.recycle();
                data.recycle();
                return isSupportDualFp;
            } catch (RemoteException e) {
                Log.e(TAG, "isSupportDualFingerprint with RemoteException happened");
            } catch (Throwable th) {
                reply.recycle();
                data.recycle();
                throw th;
            }
        }
        reply.recycle();
        data.recycle();
        return false;
    }

    public List<Fingerprint> getEnrolledFingerprints(int targetDevice) {
        return getEnrolledFingerprints(targetDevice, UserHandle.myUserId());
    }

    public List<Fingerprint> getEnrolledFingerprints(int targetDevice, int userId) {
        ArrayList arrayList = new ArrayList();
        String opPackageName = this.mContext.getOpPackageName();
        if (opPackageName == null || "".equals(opPackageName)) {
            Log.d(TAG, "calling opPackageName is invalid");
            return arrayList;
        }
        Log.d(TAG, "getEnrolledFingerprints calling package: " + opPackageName + " targetDevice: " + targetDevice);
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        IBinder fingerprintService = ServiceManager.getService("fingerprint");
        if (fingerprintService != null) {
            try {
                data.writeInterfaceToken(DESCRIPTOR_FINGERPRINT_SERVICE);
                data.writeInt(targetDevice);
                data.writeString(opPackageName);
                data.writeInt(userId);
                fingerprintService.transact(CODE_GET_FINGERPRINT_LIST_ENROLLED, data, reply, 0);
                reply.readException();
                reply.readTypedList(arrayList, Fingerprint.CREATOR);
            } catch (RemoteException e) {
                Log.e(TAG, "getEnrolledFingerprints with RemoteException happened");
            } catch (Throwable th) {
                reply.recycle();
                data.recycle();
                throw th;
            }
        }
        reply.recycle();
        data.recycle();
        return arrayList;
    }

    public boolean hasEnrolledFingerprints(int targetDevice) {
        String opPackageName = this.mContext.getOpPackageName();
        if (opPackageName == null || "".equals(opPackageName)) {
            Log.d(TAG, "calling opPackageName is invalid");
            return false;
        }
        Log.d(TAG, "hasEnrolledFingerprints calling package: " + opPackageName + " targetDevice: " + targetDevice);
        if (getEnrolledFingerprints(targetDevice).size() > 0) {
            return true;
        }
        return false;
    }

    public boolean isHardwareDetected(int targetDevice) {
        String opPackageName = this.mContext.getOpPackageName();
        if (opPackageName == null || "".equals(opPackageName)) {
            Log.d(TAG, "calling opPackageName is invalid");
            return false;
        }
        Log.d(TAG, "isHardwareDetected calling package: " + opPackageName + " targetDevice: " + targetDevice);
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        IBinder fingerprintService = ServiceManager.getService("fingerprint");
        if (fingerprintService != null) {
            try {
                data.writeInterfaceToken(DESCRIPTOR_FINGERPRINT_SERVICE);
                data.writeInt(targetDevice);
                data.writeString(opPackageName);
                fingerprintService.transact(CODE_IS_FINGERPRINT_HARDWARE_DETECTED, data, reply, 0);
                reply.readException();
                boolean isHardwareDetected = reply.readBoolean();
                Log.d(TAG, "isHardwareDetected is: " + isHardwareDetected);
                reply.recycle();
                data.recycle();
                return isHardwareDetected;
            } catch (RemoteException e) {
                Log.e(TAG, "isHardwareDetected with RemoteException happened");
            } catch (Throwable th) {
                reply.recycle();
                data.recycle();
                throw th;
            }
        }
        reply.recycle();
        data.recycle();
        return false;
    }

    private static HashMap<Integer, Integer> getHardwareInfo() {
        HashMap<Integer, Integer> hardwareInfo = new HashMap<>();
        int typeDetails = getHardwareTypeDetailsFromHal();
        Log.d(TAG, "typeDetails:" + typeDetails);
        if (typeDetails != -1) {
            int offset = -1;
            if ((typeDetails & 1) != 0) {
                offset = -1 + 1;
                int physicalType = (typeDetails >> ((offset * 4) + 8)) & 15;
                hardwareInfo.put(1, Integer.valueOf(physicalType));
                Log.d(TAG, "LOCATION_BACK physicalType :" + physicalType);
            }
            if ((typeDetails & 2) != 0) {
                offset++;
                int physicalType2 = (typeDetails >> ((offset * 4) + 8)) & 15;
                hardwareInfo.put(2, Integer.valueOf(physicalType2));
                Log.d(TAG, "LOCATION_FRONT physicalType :" + physicalType2);
            }
            if ((typeDetails & 4) != 0) {
                offset++;
                int physicalType3 = (typeDetails >> ((offset * 4) + 8)) & 15;
                hardwareInfo.put(4, Integer.valueOf(physicalType3));
                Log.d(TAG, "LOCATION_UNDER_DISPLAY physicalType :" + physicalType3);
            }
            if ((typeDetails & 8) != 0) {
                int physicalType4 = (typeDetails >> (((offset + 1) * 4) + 8)) & 15;
                hardwareInfo.put(8, Integer.valueOf(physicalType4));
                Log.d(TAG, "LOCATION_SLIDE physicalType :" + physicalType4);
            }
        }
        return hardwareInfo;
    }

    private static int getHardwareTypeDetailsFromHal() {
        Log.d(TAG, "getHardwareType mDetailsType:" + mDetailsType);
        int type = mDetailsType;
        if (type != -1) {
            return type;
        }
        int type2 = -1;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        IBinder fingerprintService = ServiceManager.getService("fingerprint");
        if (fingerprintService != null) {
            try {
                data.writeInterfaceToken(DESCRIPTOR_FINGERPRINT_SERVICE);
                fingerprintService.transact(CODE_GET_HARDWARE_TYPE, data, reply, 0);
                reply.readException();
                type2 = reply.readInt();
            } catch (RemoteException e) {
                Log.e(TAG, "getHardwareTypeDetailsFromHal with RemoteException happened");
            } catch (Throwable th) {
                reply.recycle();
                data.recycle();
                throw th;
            }
        }
        reply.recycle();
        data.recycle();
        Log.d(TAG, "getHardwareType from Hal: " + type2);
        mDetailsType = type2;
        return type2;
    }

    public static boolean hasFingerprintInScreen() {
        int hardHardwareType = getHardwareType();
        return (hardHardwareType == 0 || hardHardwareType == -1) ? false : true;
    }

    private static int[] getPositionFromHal(int positionType) {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        IBinder binder = ServiceManager.getService("fingerprint");
        int[] position = {-1, -1, -1, -1};
        if (binder != null) {
            try {
                data.writeInterfaceToken(DESCRIPTOR_FINGERPRINT_SERVICE);
                binder.transact(positionType, data, reply, 0);
                reply.readException();
                position[0] = reply.readInt();
                position[1] = reply.readInt();
                position[2] = reply.readInt();
                position[3] = reply.readInt();
            } catch (RemoteException e) {
                Log.e(TAG, "getPositionFromHal with RemoteException happened: " + positionType);
            } catch (Throwable th) {
                reply.recycle();
                data.recycle();
                throw th;
            }
        }
        reply.recycle();
        data.recycle();
        return position;
    }

    public static Rect getFingerprintRect() {
        int[] position = getSpotPosition();
        return new Rect(position[0], position[1], position[2], position[3]);
    }

    public static int[] getHardwarePosition() {
        Log.d(TAG, "getHardwarePosition sPosition[0] " + sPosition[0]);
        int[] position = sPosition;
        if (position[0] != -1) {
            return position;
        }
        int[] pxPosition = getPositionFromHal(CODE_FINGERPRINT_LOGO_POSITION);
        for (int i = 0; i < 4; i++) {
            Log.d(TAG, "getHardwarePosition from hal after covert: " + pxPosition[i]);
        }
        sPosition = pxPosition;
        return pxPosition;
    }

    public static int getUDFingerprintSpotColor() {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        IBinder fingerprintService = ServiceManager.getService("fingerprint");
        int spotColor = 0;
        if (fingerprintService != null) {
            try {
                data.writeInterfaceToken(DESCRIPTOR_FINGERPRINT_SERVICE);
                fingerprintService.transact(CODE_UDFINGERPRINT_SPOTCOLOR, data, reply, 0);
                reply.readException();
                spotColor = reply.readInt();
            } catch (RemoteException e) {
                Log.e(TAG, "getUDFingerprintSpotColor with RemoteException happened");
            } catch (Throwable th) {
                reply.recycle();
                data.recycle();
                throw th;
            }
        }
        reply.recycle();
        data.recycle();
        return spotColor;
    }

    public static void notifyCaptureOpticalImage() {
        if (getHardwareType() != 1) {
            Log.d(TAG, "not Optical sensor notifyCapture failed");
            return;
        }
        Log.d(TAG, "notifyCaptureOpticalImage");
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        IBinder fingerprintService = ServiceManager.getService("fingerprint");
        if (fingerprintService != null) {
            try {
                data.writeInterfaceToken(DESCRIPTOR_FINGERPRINT_SERVICE);
                fingerprintService.transact(CODE_NOTIFY_OPTICAL_CAPTURE, data, reply, 0);
                reply.readException();
            } catch (RemoteException e) {
                Log.e(TAG, "notifyCaptureOpticalImage with RemoteException happened");
            } catch (Throwable th) {
                reply.recycle();
                data.recycle();
                throw th;
            }
        }
        reply.recycle();
        data.recycle();
    }

    public static void setHoverEventSwitch(int enabled) {
        Log.d(TAG, "setHoverEventSwitch: " + enabled);
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        IBinder fingerprintService = ServiceManager.getService("fingerprint");
        if (fingerprintService != null) {
            try {
                data.writeInterfaceToken(DESCRIPTOR_FINGERPRINT_SERVICE);
                data.writeInt(enabled);
                fingerprintService.transact(CODE_SET_HOVER_SWITCH, data, reply, 0);
                reply.readException();
            } catch (RemoteException e) {
                Log.e(TAG, "setHoverEventSwitch with RemoteException happened");
            } catch (Throwable th) {
                reply.recycle();
                data.recycle();
                throw th;
            }
        }
        reply.recycle();
        data.recycle();
    }

    public static boolean syncEnvDataToFpService(int data, String str) {
        int result = -1;
        Log.e(TAG, "syncEnvDataToFpService IN");
        Parcel dataWrite = Parcel.obtain();
        Parcel dateReply = Parcel.obtain();
        IBinder binderHandle = ServiceManager.getService("fingerprint");
        if (binderHandle != null) {
            try {
                dataWrite.writeInterfaceToken(DESCRIPTOR_FINGERPRINT_SERVICE);
                dataWrite.writeInt(data);
                dataWrite.writeString(str);
                binderHandle.transact(CODE_FINGERPRINT_WEATHER_DATA, dataWrite, dateReply, 0);
                dateReply.readException();
                result = dateReply.readInt();
            } catch (RemoteException e) {
                Log.e(TAG, "syncEnvDataToFpService with RemoteException happened");
            } catch (Throwable th) {
                dateReply.recycle();
                dataWrite.recycle();
                throw th;
            }
        }
        dateReply.recycle();
        dataWrite.recycle();
        if (result == MSG_BINDER_SUCCESS_FLAG) {
            return true;
        }
        return false;
    }

    public static boolean isHoverEventSupport() {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        IBinder fingerprintService = ServiceManager.getService("fingerprint");
        int type = -1;
        if (fingerprintService != null) {
            try {
                data.writeInterfaceToken(DESCRIPTOR_FINGERPRINT_SERVICE);
                fingerprintService.transact(CODE_GET_HOVER_SUPPORT, data, reply, 0);
                reply.readException();
                type = reply.readInt();
            } catch (RemoteException e) {
                Log.e(TAG, "isHoverEventSupport with RemoteException happened");
            } catch (Throwable th) {
                reply.recycle();
                data.recycle();
                throw th;
            }
        }
        reply.recycle();
        data.recycle();
        Log.d(TAG, "isHoverEventSupport from Hal: " + type);
        if (type == -1) {
            type = SystemProperties.getInt("persist.sys.fingerprint.hoverSupport", 0);
            Log.d(TAG, "isHoverEventSupport use SystemProperties type :" + type);
        }
        if (type == 1) {
            return true;
        }
        return false;
    }

    public void disableFingerprintView(boolean hasAnimation) {
        Log.d(TAG, "disableFingerprintView: " + hasAnimation);
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        IBinder fingerprintService = ServiceManager.getService("fingerprint");
        if (fingerprintService != null) {
            try {
                data.writeInterfaceToken(DESCRIPTOR_FINGERPRINT_SERVICE);
                data.writeBoolean(hasAnimation);
                fingerprintService.transact(CODE_DISABLE_FINGERPRINT_VIEW, data, reply, 0);
                reply.readException();
            } catch (RemoteException e) {
                Log.e(TAG, "disableFingerprintView with RemoteException happened");
            } catch (Throwable th) {
                reply.recycle();
                data.recycle();
                throw th;
            }
        }
        reply.recycle();
        data.recycle();
    }

    public void enableFingerprintView(boolean hasAnimation, int initStatus) {
        Log.d(TAG, "enableFingerprintView: hasAnimation =" + hasAnimation + ",initStatus = " + initStatus);
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        IBinder fingerprintService = ServiceManager.getService("fingerprint");
        if (fingerprintService != null) {
            try {
                data.writeInterfaceToken(DESCRIPTOR_FINGERPRINT_SERVICE);
                data.writeBoolean(hasAnimation);
                data.writeInt(initStatus);
                fingerprintService.transact(CODE_ENABLE_FINGERPRINT_VIEW, data, reply, 0);
                reply.readException();
            } catch (RemoteException e) {
                Log.e(TAG, "enableFingerprintView with RemoteException happened");
            } catch (Throwable th) {
                reply.recycle();
                data.recycle();
                throw th;
            }
        }
        reply.recycle();
        data.recycle();
    }

    public static void keepMaskShowAfterAuthentication() {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        IBinder fingerprintService = ServiceManager.getService("fingerprint");
        if (fingerprintService != null) {
            try {
                data.writeInterfaceToken(DESCRIPTOR_FINGERPRINT_SERVICE);
                fingerprintService.transact(CODE_KEEP_MASK_SHOW_AFTER_AUTHENTICATION, data, reply, 0);
                reply.readException();
            } catch (RemoteException e) {
                Log.e(TAG, "keepMaskShowAfterAuthentication with RemoteException happened");
            } catch (Throwable th) {
                reply.recycle();
                data.recycle();
                throw th;
            }
        }
        reply.recycle();
        data.recycle();
    }

    public static void removeMaskAndShowButton() {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        IBinder fingerprintService = ServiceManager.getService("fingerprint");
        if (fingerprintService != null) {
            try {
                data.writeInterfaceToken(DESCRIPTOR_FINGERPRINT_SERVICE);
                fingerprintService.transact(CODE_REMOVE_MASK_AND_SHOW_CANCEL, data, reply, 0);
                reply.readException();
            } catch (RemoteException e) {
                Log.e(TAG, "removeMaskAndShowButton with RemoteException happened");
            } catch (Throwable th) {
                reply.recycle();
                data.recycle();
                throw th;
            }
        }
        reply.recycle();
        data.recycle();
    }

    public static int getHighLightspotRadius() {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        IBinder fingerprintService = ServiceManager.getService("fingerprint");
        int radius = -1;
        if (fingerprintService != null) {
            try {
                data.writeInterfaceToken(DESCRIPTOR_FINGERPRINT_SERVICE);
                fingerprintService.transact(CODE_GET_HIGHLIGHT_SPOT_RADIUS, data, reply, 0);
                reply.readException();
                radius = reply.readInt();
            } catch (RemoteException e) {
                Log.e(TAG, "getHighLightspotRadius with RemoteException happened");
            } catch (Throwable th) {
                reply.recycle();
                data.recycle();
                throw th;
            }
        }
        reply.recycle();
        data.recycle();
        return radius;
    }

    public List<Integer> getFingerIds() {
        Context context = this.mContext;
        if (context != null) {
            this.fingerprintManager = (FingerprintManager) context.getSystemService("fingerprint");
            FingerprintManager fingerprintManager2 = this.fingerprintManager;
            if (fingerprintManager2 == null) {
                Log.e(TAG, "call getFingerIds() Error:the service Context.FINGERPRINT_SERVICE is not supported.");
                return null;
            }
            List<Fingerprint> fingerprints = fingerprintManager2.getEnrolledFingerprints();
            List<Integer> result = new ArrayList<>();
            if (fingerprints == null || fingerprints.size() == 0) {
                return result;
            }
            int size = fingerprints.size();
            for (int i = 0; i < size; i++) {
                result.add(Integer.valueOf(fingerprints.get(i).getBiometricId()));
            }
            return result;
        }
        throw new NullPointerException("The params context cannot be null");
    }

    public void resetFingerprintTimeout() {
        Context context = this.mContext;
        if (context != null) {
            this.fingerprintManager = (FingerprintManager) context.getSystemService("fingerprint");
            FingerprintManager fingerprintManager2 = this.fingerprintManager;
            if (fingerprintManager2 == null) {
                Log.e(TAG, "call resetFingerprintTimeout() Error:the service Context.FINGERPRINT_SERVICE is not supported.");
            } else {
                fingerprintManager2.resetTimeout(new byte[0]);
            }
        } else {
            throw new NullPointerException("context cannot be null.");
        }
    }

    public boolean isFingerSensorDetected() {
        Context context = this.mContext;
        if (context != null) {
            this.fingerprintManager = (FingerprintManager) context.getSystemService("fingerprint");
            FingerprintManager fingerprintManager2 = this.fingerprintManager;
            if (fingerprintManager2 != null) {
                return fingerprintManager2.isHardwareDetected();
            }
            Log.e(TAG, "call isFingerSensorDetected() Error:the service Context.FINGERPRINT_SERVICE is not supported.");
            return false;
        }
        throw new NullPointerException("The params context cannot be null.");
    }

    public boolean hasEnrolledFingerprints() {
        Context context = this.mContext;
        if (context != null) {
            this.fingerprintManager = (FingerprintManager) context.getSystemService("fingerprint");
            FingerprintManager fingerprintManager2 = this.fingerprintManager;
            if (fingerprintManager2 != null) {
                return fingerprintManager2.hasEnrolledFingerprints();
            }
            Log.e(TAG, "call hasEnrolledFingerprints() Error:the service Context.FINGERPRINT_SERVICE is not supported.");
            return false;
        }
        throw new NullPointerException("The params context cannot be null.");
    }

    public int verifyUser(FingerprintManager.AuthenticationCallback callbak, byte[] finalChallenge, String aaid, final AuthenticatorListener listener) {
        Authenticator authenticator2 = this.authenticator;
        if (authenticator2 != null) {
            return authenticator2.verifyUser(callbak, finalChallenge, aaid, new Authenticator.AuthenticatorListener() {
                /* class huawei.android.hardware.fingerprint.FingerprintManagerEx.AnonymousClass1 */

                public void onUserVerificationResult(int result, byte[] userid, byte[] uvt) {
                    AuthenticatorListener authenticatorListener = listener;
                    if (authenticatorListener != null) {
                        authenticatorListener.onUserVerificationResult(result, userid, uvt);
                    }
                }

                public void onIsUserIDValidResult(boolean isUserIdValid) {
                    AuthenticatorListener authenticatorListener = listener;
                    if (authenticatorListener != null) {
                        authenticatorListener.onIsUserIDValidResult(isUserIdValid);
                    }
                }
            });
        }
        Log.e(TAG, "call fun verifyUser Error: authenticator == null.");
        return 0;
    }

    public int cancelVerify() {
        Authenticator authenticator2 = this.authenticator;
        if (authenticator2 != null) {
            return authenticator2.cancelVerify();
        }
        Log.e(TAG, "call fun cancelVerify Error: authenticator == null.");
        return 0;
    }

    public void resetAuthenticationCallback() {
        Authenticator authenticator2 = this.authenticator;
        if (authenticator2 == null) {
            Log.e(TAG, "call fun resetAuthenticationCallback Error: authenticator == null.");
        } else {
            authenticator2.setAuthenticationCallback((FingerprintManager.AuthenticationCallback) null);
        }
    }

    private static int getInformationFromHal(int halCommand) {
        if (halCommand <= 0) {
            return -1;
        }
        Parcel spotData = Parcel.obtain();
        Parcel spotReply = Parcel.obtain();
        IBinder fingerprintService = ServiceManager.getService("fingerprint");
        int result = -1;
        if (fingerprintService != null) {
            try {
                spotData.writeInterfaceToken(DESCRIPTOR_FINGERPRINT_SERVICE);
                fingerprintService.transact(halCommand, spotData, spotReply, 0);
                spotReply.readException();
                result = spotReply.readInt();
            } catch (RemoteException e) {
                Log.e(TAG, "getInformationFromHal:" + halCommand);
            } catch (Throwable th) {
                spotReply.recycle();
                spotData.recycle();
                throw th;
            }
        }
        spotReply.recycle();
        spotData.recycle();
        return result;
    }

    public static int getSpotColor() {
        return getInformationFromHal(CODE_UDFINGERPRINT_SPOTCOLOR);
    }

    public static int getSpotRadius() {
        return getInformationFromHal(CODE_GET_HIGHLIGHT_SPOT_RADIUS);
    }

    public static int[] getSpotPosition() {
        Log.d(TAG, "getSpotPosition sSpotPosition[0] " + sSpotPosition[0]);
        int[] position = sSpotPosition;
        if (position[0] != -1) {
            return position;
        }
        int[] pxPosition = getPositionFromHal(CODE_GET_HARDWARE_POSITION);
        for (int i = 0; i <= 3; i++) {
            Log.d(TAG, "getSpotPosition from hal after covert: " + pxPosition[i]);
        }
        sSpotPosition = pxPosition;
        return pxPosition;
    }

    public static int getFingerLogoRadius() {
        return getInformationFromHal(CODE_FINGERPRINT_LOGO_RADIUS);
    }

    public int mmiFingerprintTest(int testType, String param) {
        Log.i(TAG, "FPT testType " + testType + " param " + param);
        if (this.fingerprintManager == null) {
            this.fingerprintManager = (FingerprintManager) this.mContext.getSystemService("fingerprint");
        }
        FingerprintManager fingerprintManager2 = this.fingerprintManager;
        if (fingerprintManager2 == null) {
            Log.e(TAG, "FPT service not exist.");
            return -1;
        }
        try {
            if (!fingerprintManager2.isHardwareDetected()) {
                this.fingerprintManager.preEnroll();
            }
            int result = mmiFingerprintTestToService(testType, param);
            Log.i(TAG, "FPT result " + result);
            return result;
        } catch (Exception e) {
            Log.e(TAG, "FPT fail");
            return -1;
        }
    }

    private int mmiFingerprintTestToService(int type, String param) {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        IBinder binder = ServiceManager.getService("fingerprint");
        int result = -1;
        if (binder != null) {
            try {
                data.writeInterfaceToken(DESCRIPTOR_FINGERPRINT_SERVICE);
                data.writeInt(type);
                data.writeString(param);
                binder.transact(CODE_FINGERPRINT_MMI_TEST, data, reply, 0);
                reply.readException();
                result = reply.readInt();
            } catch (RemoteException e) {
                Log.e(TAG, "FPT to service with RemoteException happened.");
                result = -1;
            } catch (Throwable th) {
                reply.recycle();
                data.recycle();
                throw th;
            }
        }
        reply.recycle();
        data.recycle();
        return result;
    }
}
