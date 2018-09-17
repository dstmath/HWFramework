package com.android.server.os;

import android.content.Context;
import android.os.Binder;
import android.os.IDeviceIdentifiersPolicyService.Stub;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.util.ByteStringUtils;
import android.util.Log;
import com.android.server.SystemService;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

public final class DeviceIdentifiersPolicyService extends SystemService {
    public static final String PERMISSION_ACCESS_UDID = "com.huawei.permission.sec.ACCESS_UDID";
    private static final String TAG = "DeviceIdentifiers";
    private static final String UDID_EXCEPTION = "AndroidRuntimeException";
    private static UDIDModelWhiteConfig udidModelWhiteConfig = null;

    private static final class DeviceIdentifiersPolicy extends Stub {
        static boolean isInWhiteList = false;
        static String udid = null;
        private final Context mContext;

        public DeviceIdentifiersPolicy(Context context) {
            this.mContext = context;
        }

        public String getSerial() throws RemoteException {
            if (UserHandle.getAppId(Binder.getCallingUid()) == 1000 || this.mContext.checkCallingOrSelfPermission("android.permission.READ_PHONE_STATE") == 0 || this.mContext.checkCallingOrSelfPermission("android.permission.READ_PRIVILEGED_PHONE_STATE") == 0) {
                return SystemProperties.get("ro.serialno", Shell.NIGHT_MODE_STR_UNKNOWN);
            }
            throw new SecurityException("getSerial requires READ_PHONE_STATE or READ_PRIVILEGED_PHONE_STATE permission");
        }

        public String getUDID() throws RemoteException {
            this.mContext.enforceCallingOrSelfPermission(DeviceIdentifiersPolicyService.PERMISSION_ACCESS_UDID, "does not have access udid permission!");
            String readResult = "";
            if (udid != null) {
                Log.i(DeviceIdentifiersPolicyService.TAG, "udid has been read success, return!");
                return udid;
            }
            if (DeviceIdentifiersPolicyService.udidModelWhiteConfig == null) {
                DeviceIdentifiersPolicyService.udidModelWhiteConfig = UDIDModelWhiteConfig.getInstance();
                isInWhiteList = DeviceIdentifiersPolicyService.udidModelWhiteConfig.isWhiteModelForUDID(SystemProperties.get("ro.product.model", ""));
            }
            if (isInWhiteList) {
                Log.w(DeviceIdentifiersPolicyService.TAG, "the phone is in udid_model_whitelist, return null!");
                return null;
            }
            try {
                MessageDigest messageDigest = MessageDigest.getInstance("SHA256");
                String serNum = SystemProperties.get("ro.serialno", Shell.NIGHT_MODE_STR_UNKNOWN);
                if (serNum != null) {
                    readResult = readResult + serNum.toUpperCase(Locale.US);
                }
                String emmcId = GetUDIDNative.getEmmcId();
                if (emmcId != null) {
                    readResult = readResult + emmcId.toUpperCase(Locale.US);
                }
                String btMacAddress = GetUDIDNative.getBtMacAddress();
                if (btMacAddress != null) {
                    readResult = readResult + btMacAddress.toUpperCase(Locale.US);
                }
                String wifiMacAddress = GetUDIDNative.getWifiMacAddress();
                if (wifiMacAddress != null) {
                    readResult = readResult + wifiMacAddress.toUpperCase(Locale.US);
                }
                if (serNum == null && emmcId == null && btMacAddress == null && wifiMacAddress == null) {
                    udid = DeviceIdentifiersPolicyService.UDID_EXCEPTION;
                    return udid;
                }
                messageDigest.update(readResult.getBytes(Charset.forName("UTF-8")));
                udid = ByteStringUtils.toHexString(messageDigest.digest());
                return udid;
            } catch (NoSuchAlgorithmException e) {
                Log.w(DeviceIdentifiersPolicyService.TAG, "MessageDigest throw Exception!");
            }
        }
    }

    public DeviceIdentifiersPolicyService(Context context) {
        super(context);
    }

    public void onStart() {
        publishBinderService("device_identifiers", new DeviceIdentifiersPolicy(getContext()));
    }
}
