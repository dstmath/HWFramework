package com.huawei.wallet.sdk.common.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.TextUtils;
import com.huawei.wallet.sdk.common.log.LogC;
import java.lang.reflect.Field;

public class BaseLibUtil {
    private static final int ANDROID_M_CODE = 23;
    private static final int CANNOT_GETIMEI_ON_NOPHONE = 1;
    private static final int CAN_GETIMEI_ON_NOPHONE = 0;
    private static final int NOT_CHECK_GETIMEI = -1;
    private static int canGetImeiOnNoPhone = -1;

    public static boolean isGrantedPermission(Context mContext, String permissionDesc) {
        if (Build.VERSION.SDK_INT < 23) {
            return true;
        }
        if (mContext.checkSelfPermission(permissionDesc) != 0) {
            return false;
        }
        return true;
    }

    public static boolean isSystemApp(Context mContext, String packageName) {
        boolean isSystem = false;
        try {
            PackageManager packageManager = mContext.getPackageManager();
            if (packageManager == null) {
                return false;
            }
            if ((packageManager.getPackageInfo(packageName, 0).applicationInfo.flags & 1) != 0) {
                isSystem = true;
            }
            return isSystem;
        } catch (PackageManager.NameNotFoundException e) {
            LogC.e("isSystemApp checking occur NameNotFoundException!", false);
            return false;
        }
    }

    private static boolean supportNewPermissionCheck() {
        boolean z = false;
        try {
            Class<?> TelephonyManagerExClass = Class.forName("android.telephony.HwTelephonyManager");
            Field field = TelephonyManagerExClass.getDeclaredField("SUPPORT_SYSTEMAPP_GET_DEVICEID");
            field.setAccessible(true);
            int value = field.getInt(TelephonyManagerExClass);
            field.setAccessible(false);
            if (value == 1) {
                z = true;
            }
            return z;
        } catch (NoSuchFieldException e) {
            LogC.e("supportNewPermissionCheck--NoSuchFieldException", false);
            return false;
        } catch (IllegalAccessException e2) {
            LogC.e("supportNewPermissionCheck--IllegalAccessException", false);
            return false;
        } catch (IllegalArgumentException e3) {
            LogC.e("supportNewPermissionCheck--IllegalArgumentException", false);
            return false;
        } catch (ClassNotFoundException e4) {
            LogC.e("supportNewPermissionCheck--ClassNotFoundException", false);
            return false;
        }
    }

    public static boolean canGetImeiOnNoPhonePermission(Context mContext) {
        boolean z = true;
        if (-1 != canGetImeiOnNoPhone) {
            if (canGetImeiOnNoPhone != 0) {
                z = false;
            }
            return z;
        } else if (mContext == null) {
            return false;
        } else {
            String pkgName = mContext.getPackageName();
            if (TextUtils.isEmpty(pkgName)) {
                pkgName = "com.huawei.wallet";
            }
            if (!isSystemApp(mContext, pkgName) || !supportNewPermissionCheck()) {
                canGetImeiOnNoPhone = 1;
                LogC.i("readphonestate--canGetImeiOnNoPhonePermission false", false);
                return false;
            }
            canGetImeiOnNoPhone = 0;
            LogC.i("readphonestate--canGetImeiOnNoPhonePermission true", false);
            return true;
        }
    }
}
