package com.android.server.os;

import android.content.Context;
import android.os.IDeviceIdentifiersPolicyService;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.ByteStringUtils;
import android.util.Log;
import com.android.internal.telephony.TelephonyPermissions;
import com.android.server.SystemService;
import com.android.server.UiModeManagerService;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;

public final class DeviceIdentifiersPolicyService extends SystemService {
    public static final String PERMISSION_ACCESS_UDID = "com.huawei.permission.sec.ACCESS_UDID";
    private static final String TAG = "DeviceIdentifiers";
    private static final String UDID_EXCEPTION = "AndroidRuntimeException";
    private static ArrayList<String> mGetUDIDNoEmiccID = new ArrayList<>();
    private static final Object sLock = new Object();
    private static UDIDModelWhiteConfig udidModelWhiteConfig = null;

    static {
        mGetUDIDNoEmiccID.add("ARE-");
        mGetUDIDNoEmiccID.add("ARS-");
        mGetUDIDNoEmiccID.add("COR-");
        mGetUDIDNoEmiccID.add("INE-");
        mGetUDIDNoEmiccID.add("JKM-");
        mGetUDIDNoEmiccID.add("JSN-");
        mGetUDIDNoEmiccID.add("PAR-");
        mGetUDIDNoEmiccID.add("RVL-");
        mGetUDIDNoEmiccID.add("SNE-");
        mGetUDIDNoEmiccID.add("COR");
        mGetUDIDNoEmiccID.add("INE");
        mGetUDIDNoEmiccID.add("PAR");
        mGetUDIDNoEmiccID.add("RVL");
        mGetUDIDNoEmiccID.add("SNE");
    }

    public DeviceIdentifiersPolicyService(Context context) {
        super(context);
    }

    /* JADX DEBUG: Multi-variable search result rejected for r2v0, resolved type: com.android.server.os.DeviceIdentifiersPolicyService */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r0v0, types: [com.android.server.os.DeviceIdentifiersPolicyService$DeviceIdentifiersPolicy, android.os.IBinder] */
    /* JADX WARNING: Unknown variable types count: 1 */
    @Override // com.android.server.SystemService
    public void onStart() {
        publishBinderService("device_identifiers", new DeviceIdentifiersPolicy(getContext()));
    }

    private static final class DeviceIdentifiersPolicy extends IDeviceIdentifiersPolicyService.Stub {
        static boolean isInWhiteList = false;
        static String udid = null;
        static String udidNoEmmicID = null;
        private final Context mContext;

        public DeviceIdentifiersPolicy(Context context) {
            this.mContext = context;
        }

        public String getSerial() throws RemoteException {
            if (!TelephonyPermissions.checkCallingOrSelfReadDeviceIdentifiers(this.mContext, (String) null, "getSerial")) {
                return UiModeManagerService.Shell.NIGHT_MODE_STR_UNKNOWN;
            }
            return SystemProperties.get("ro.serialno", UiModeManagerService.Shell.NIGHT_MODE_STR_UNKNOWN);
        }

        public String getSerialForPackage(String callingPackage) throws RemoteException {
            if (!TelephonyPermissions.checkCallingOrSelfReadDeviceIdentifiers(this.mContext, callingPackage, "getSerial")) {
                return UiModeManagerService.Shell.NIGHT_MODE_STR_UNKNOWN;
            }
            return SystemProperties.get("ro.serialno", UiModeManagerService.Shell.NIGHT_MODE_STR_UNKNOWN);
        }

        public String getUDID() throws RemoteException {
            this.mContext.enforceCallingOrSelfPermission(DeviceIdentifiersPolicyService.PERMISSION_ACCESS_UDID, "does not have access udid permission!");
            synchronized (DeviceIdentifiersPolicyService.sLock) {
                String readResult = "";
                if (!TextUtils.isEmpty(udid)) {
                    Log.i(DeviceIdentifiersPolicyService.TAG, "udid has been read success, return!");
                    return udid;
                }
                udid = GetUDIDNative.getUDID();
                if (!TextUtils.isEmpty(udid)) {
                    return udid;
                }
                Log.e(DeviceIdentifiersPolicyService.TAG, "GetUDIDNative getUDID return null, generated it");
                if (DeviceIdentifiersPolicyService.udidModelWhiteConfig == null) {
                    UDIDModelWhiteConfig unused = DeviceIdentifiersPolicyService.udidModelWhiteConfig = UDIDModelWhiteConfig.getInstance();
                }
                String currentModel = SystemProperties.get("ro.product.model.real", "");
                if (currentModel == null || currentModel.equals("")) {
                    currentModel = SystemProperties.get("ro.product.model", "");
                }
                isInWhiteList = DeviceIdentifiersPolicyService.udidModelWhiteConfig.isWhiteModelForUDID(currentModel);
                if (isInWhiteList) {
                    Log.w(DeviceIdentifiersPolicyService.TAG, "the phone is in udid_model_whitelist, return null!");
                    return null;
                }
                try {
                    MessageDigest messageDigest = MessageDigest.getInstance("SHA256");
                    String serNum = SystemProperties.get("ro.serialno", UiModeManagerService.Shell.NIGHT_MODE_STR_UNKNOWN);
                    if (serNum != null) {
                        readResult = readResult + serNum.toUpperCase(Locale.US);
                        Log.i(DeviceIdentifiersPolicyService.TAG, "serNum read success");
                    }
                    String emmcId = GetUDIDNative.getEmmcId();
                    if (emmcId != null) {
                        readResult = readResult + emmcId.toUpperCase(Locale.US);
                        Log.i(DeviceIdentifiersPolicyService.TAG, "emmc read success");
                    }
                    String btMacAddress = GetUDIDNative.getBtMacAddress();
                    if (btMacAddress != null) {
                        readResult = readResult + btMacAddress.toUpperCase(Locale.US);
                        Log.i(DeviceIdentifiersPolicyService.TAG, "btMacAddress read success");
                    }
                    String wifiMacAddress = GetUDIDNative.getWifiMacAddress();
                    if (wifiMacAddress != null) {
                        readResult = readResult + wifiMacAddress.toUpperCase(Locale.US);
                        Log.i(DeviceIdentifiersPolicyService.TAG, "wifiMacAddress read success");
                    }
                    if (serNum == null && emmcId == null && btMacAddress == null && wifiMacAddress == null) {
                        udid = DeviceIdentifiersPolicyService.UDID_EXCEPTION;
                    } else {
                        messageDigest.update(readResult.getBytes(Charset.forName("UTF-8")));
                        udid = ByteStringUtils.toHexString(messageDigest.digest());
                    }
                } catch (NoSuchAlgorithmException e) {
                    Log.w(DeviceIdentifiersPolicyService.TAG, "MessageDigest throw Exception!");
                }
                return udid;
            }
        }

        public String getSecondaryUDID() throws RemoteException {
            this.mContext.enforceCallingOrSelfPermission(DeviceIdentifiersPolicyService.PERMISSION_ACCESS_UDID, "does not have access udid permission!");
            synchronized (DeviceIdentifiersPolicyService.sLock) {
                if (!TextUtils.isEmpty(udidNoEmmicID)) {
                    Log.i(DeviceIdentifiersPolicyService.TAG, "udid has been read success, return!");
                    return udidNoEmmicID;
                }
                if (DeviceIdentifiersPolicyService.udidModelWhiteConfig == null) {
                    UDIDModelWhiteConfig unused = DeviceIdentifiersPolicyService.udidModelWhiteConfig = UDIDModelWhiteConfig.getInstance();
                }
                String currentModel = SystemProperties.get("ro.product.model.real", "");
                if (currentModel == null || currentModel.equals("")) {
                    currentModel = SystemProperties.get("ro.product.model", "");
                }
                isInWhiteList = DeviceIdentifiersPolicyService.udidModelWhiteConfig.isWhiteModelForUDID(currentModel);
                if (isInWhiteList) {
                    Log.w(DeviceIdentifiersPolicyService.TAG, "the phone is in udid_model_whitelist, return null!");
                    return null;
                }
                boolean isRightProduct = false;
                if (!currentModel.equals("")) {
                    Iterator it = DeviceIdentifiersPolicyService.mGetUDIDNoEmiccID.iterator();
                    while (true) {
                        if (!it.hasNext()) {
                            break;
                        }
                        String model = (String) it.next();
                        if (currentModel.equals(model) || currentModel.startsWith(model)) {
                            break;
                        }
                        isRightProduct = false;
                    }
                    isRightProduct = true;
                    if (!isRightProduct) {
                        Log.e(DeviceIdentifiersPolicyService.TAG, "Product model is not in list");
                        return DeviceIdentifiersPolicyService.UDID_EXCEPTION;
                    }
                    setUdidNoEmmicID();
                    return udidNoEmmicID;
                }
                Log.e(DeviceIdentifiersPolicyService.TAG, "Product model is null");
                return DeviceIdentifiersPolicyService.UDID_EXCEPTION;
            }
        }

        private void setUdidNoEmmicID() {
            String readResult = "";
            try {
                MessageDigest messageDigest = MessageDigest.getInstance("SHA256");
                String serNum = SystemProperties.get("ro.serialno", UiModeManagerService.Shell.NIGHT_MODE_STR_UNKNOWN);
                if (serNum != null) {
                    readResult = readResult + serNum.toUpperCase(Locale.ROOT);
                }
                String btMacAddress = GetUDIDNative.getBtMacAddress();
                if (btMacAddress != null) {
                    readResult = readResult + btMacAddress.toUpperCase(Locale.ROOT);
                }
                String wifiMacAddress = GetUDIDNative.getWifiMacAddress();
                if (wifiMacAddress != null) {
                    readResult = readResult + wifiMacAddress.toUpperCase(Locale.ROOT);
                }
                if (!(serNum == null || btMacAddress == null)) {
                    if (wifiMacAddress != null) {
                        messageDigest.update(readResult.getBytes(Charset.forName("UTF-8")));
                        udidNoEmmicID = ByteStringUtils.toHexString(messageDigest.digest());
                        return;
                    }
                }
                Log.e(DeviceIdentifiersPolicyService.TAG, "serialno, bluetooth, wifi address null");
                udidNoEmmicID = DeviceIdentifiersPolicyService.UDID_EXCEPTION;
            } catch (NoSuchAlgorithmException e) {
                Log.w(DeviceIdentifiersPolicyService.TAG, "MessageDigest throw Exception!");
            }
        }
    }
}
