package com.android.server.os;

import android.content.Context;
import android.os.Binder;
import android.os.IDeviceIdentifiersPolicyService;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.ByteStringUtils;
import android.util.Log;
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
    /* access modifiers changed from: private */
    public static ArrayList<String> mGetUDIDNoEmiccID = new ArrayList<>();
    /* access modifiers changed from: private */
    public static final Object sLock = new Object();
    /* access modifiers changed from: private */
    public static UDIDModelWhiteConfig udidModelWhiteConfig = null;

    private static final class DeviceIdentifiersPolicy extends IDeviceIdentifiersPolicyService.Stub {
        static boolean isInWhiteList = false;
        static String udid = null;
        static String udidNoEmmicID = null;
        private final Context mContext;

        public DeviceIdentifiersPolicy(Context context) {
            this.mContext = context;
        }

        public String getSerial() throws RemoteException {
            if (UserHandle.getAppId(Binder.getCallingUid()) == 1000 || this.mContext.checkCallingOrSelfPermission("android.permission.READ_PHONE_STATE") == 0 || this.mContext.checkCallingOrSelfPermission("android.permission.READ_PRIVILEGED_PHONE_STATE") == 0) {
                return SystemProperties.get("ro.serialno", UiModeManagerService.Shell.NIGHT_MODE_STR_UNKNOWN);
            }
            throw new SecurityException("getSerial requires READ_PHONE_STATE or READ_PRIVILEGED_PHONE_STATE permission");
        }

        public String getUDID() throws RemoteException {
            this.mContext.enforceCallingOrSelfPermission(DeviceIdentifiersPolicyService.PERMISSION_ACCESS_UDID, "does not have access udid permission!");
            synchronized (DeviceIdentifiersPolicyService.sLock) {
                String readResult = BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;
                if (!TextUtils.isEmpty(udid)) {
                    Log.i(DeviceIdentifiersPolicyService.TAG, "udid has been read success, return!");
                    String str = udid;
                    return str;
                }
                udid = GetUDIDNative.getUDID();
                if (!TextUtils.isEmpty(udid)) {
                    String str2 = udid;
                    return str2;
                }
                Log.e(DeviceIdentifiersPolicyService.TAG, "GetUDIDNative getUDID return null, generated it");
                if (DeviceIdentifiersPolicyService.udidModelWhiteConfig == null) {
                    UDIDModelWhiteConfig unused = DeviceIdentifiersPolicyService.udidModelWhiteConfig = UDIDModelWhiteConfig.getInstance();
                }
                String currentModel = SystemProperties.get("ro.product.model.real", BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
                if (currentModel == null || currentModel.equals(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS)) {
                    currentModel = SystemProperties.get("ro.product.model", BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
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
                    } else {
                        messageDigest.update(readResult.getBytes(Charset.forName("UTF-8")));
                        udid = ByteStringUtils.toHexString(messageDigest.digest());
                    }
                } catch (NoSuchAlgorithmException e) {
                    Log.w(DeviceIdentifiersPolicyService.TAG, "MessageDigest throw Exception!");
                }
                String str3 = udid;
                return str3;
            }
        }

        public String getSecondaryUDID() throws RemoteException {
            this.mContext.enforceCallingOrSelfPermission(DeviceIdentifiersPolicyService.PERMISSION_ACCESS_UDID, "does not have access udid permission!");
            synchronized (DeviceIdentifiersPolicyService.sLock) {
                if (!TextUtils.isEmpty(udidNoEmmicID)) {
                    Log.i(DeviceIdentifiersPolicyService.TAG, "udid has been read success, return!");
                    String str = udidNoEmmicID;
                    return str;
                }
                if (DeviceIdentifiersPolicyService.udidModelWhiteConfig == null) {
                    UDIDModelWhiteConfig unused = DeviceIdentifiersPolicyService.udidModelWhiteConfig = UDIDModelWhiteConfig.getInstance();
                }
                String currentModel = SystemProperties.get("ro.product.model.real", BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
                if (currentModel == null || currentModel.equals(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS)) {
                    currentModel = SystemProperties.get("ro.product.model", BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
                }
                isInWhiteList = DeviceIdentifiersPolicyService.udidModelWhiteConfig.isWhiteModelForUDID(currentModel);
                if (isInWhiteList) {
                    Log.w(DeviceIdentifiersPolicyService.TAG, "the phone is in udid_model_whitelist, return null!");
                    return null;
                }
                boolean isRightProduct = false;
                if (!currentModel.equals(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS)) {
                    Iterator it = DeviceIdentifiersPolicyService.mGetUDIDNoEmiccID.iterator();
                    while (true) {
                        if (!it.hasNext()) {
                            break;
                        }
                        String model = (String) it.next();
                        if (currentModel.equals(model) || currentModel.startsWith(model)) {
                            isRightProduct = true;
                        } else {
                            isRightProduct = false;
                        }
                    }
                    isRightProduct = true;
                    if (!isRightProduct) {
                        Log.e(DeviceIdentifiersPolicyService.TAG, "Product model is not in list");
                        return DeviceIdentifiersPolicyService.UDID_EXCEPTION;
                    }
                    String readResult = BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;
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
                                String str2 = udidNoEmmicID;
                                return str2;
                            }
                        }
                        Log.e(DeviceIdentifiersPolicyService.TAG, "serialno, bluetooth, wifi address null");
                        udidNoEmmicID = DeviceIdentifiersPolicyService.UDID_EXCEPTION;
                    } catch (NoSuchAlgorithmException e) {
                        Log.w(DeviceIdentifiersPolicyService.TAG, "MessageDigest throw Exception!");
                    }
                    String str22 = udidNoEmmicID;
                    return str22;
                }
                Log.e(DeviceIdentifiersPolicyService.TAG, "Product model is null");
                return DeviceIdentifiersPolicyService.UDID_EXCEPTION;
            }
        }
    }

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

    /* JADX WARNING: type inference failed for: r1v0, types: [com.android.server.os.DeviceIdentifiersPolicyService$DeviceIdentifiersPolicy, android.os.IBinder] */
    public void onStart() {
        publishBinderService("device_identifiers", new DeviceIdentifiersPolicy(getContext()));
    }
}
