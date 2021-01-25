package com.huawei.android.content;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;
import android.view.Display;
import com.huawei.annotation.HwSystemApi;
import java.io.File;

public abstract class ContextEx {
    public static final String BLUETOOTH_BPP_SERVICE = "bluetooth_bpp_service";
    public static final String BLUETOOTH_DUN_SERVICE = "bluetooth_dun";
    public static final String BLUETOOTH_FM_RECEIVER_SERVICE = "bluetooth_fm_receiver_service";
    public static final String BLUETOOTH_FM_TRANSMITTER_SERVICE = "bluetooth_fm_transmitter_service";
    public static final String BLUETOOTH_FTP_SERVICE = "bluetooth_ftp";
    public static final String BLUETOOTH_OPP_SERVICE = "bluetooth_opp_service";
    public static final String BLUETOOTH_PBAP_SERVICE = "bluetooth_pbs";
    public static final String BLUETOOTH_SAP_SERVICE = "bluetooth_sap";
    public static final String BLUETOOTH_TEST_SERVICE = "bluetooth_test";
    public static final String COUNTRY_DETECTOR = "country_detector";
    @HwSystemApi
    public static final String DEVICE_IDLE_CONTROLLER = "deviceidle";
    public static final String MSIM_TELEPHONY_SERVICE = "phone_msim";
    public static final String NETWORKMANAGEMENT_SERVICE = "network_management";
    @HwSystemApi
    public static final String OVERLAY_SERVICE = "overlay";
    private static final String TAG = "ContextEx";

    public static String getStatusBarService() {
        return "statusbar";
    }

    public static boolean isCredentialProtectedStorage(Context context) {
        return context.isCredentialProtectedStorage();
    }

    public static Context createCredentialProtectedStorageContext(Context context) {
        return context.createCredentialProtectedStorageContext();
    }

    public static int getUserId(Context context) {
        return context.getUserId();
    }

    public static File getSharedPrefsFile(Context context, String name) {
        return context.getSharedPrefsFile(name);
    }

    public static Intent registerReceiverAsUser(Context context, BroadcastReceiver receiver, UserHandle user, IntentFilter filter, String broadcastPermission, Handler scheduler) {
        if (context != null) {
            return context.registerReceiverAsUser(receiver, user, filter, broadcastPermission, scheduler);
        }
        return null;
    }

    public static ComponentName startServiceAsUser(Context context, Intent service, UserHandle user) {
        if (context != null) {
            return context.startServiceAsUser(service, user);
        }
        return null;
    }

    public static void startActivityAsUser(Context context, Intent intent, Bundle options, UserHandle userId) {
        if (context != null) {
            context.startActivityAsUser(intent, options, userId);
        }
    }

    @HwSystemApi
    public static void startActivityAsUser(Context context, Intent intent, UserHandle user) {
        if (context != null) {
            context.startActivityAsUser(intent, user);
        }
    }

    public static Context createPackageContextAsUser(Context context, String packageName, int flags, UserHandle user) throws PackageManager.NameNotFoundException {
        return context.createPackageContextAsUser(packageName, flags, user);
    }

    public static boolean stopServiceAsUser(Context context, Intent service, UserHandle user) {
        if (context != null) {
            return context.stopServiceAsUser(service, user);
        }
        return false;
    }

    public static boolean bindServiceAsUser(Context context, Intent service, ServiceConnection conn, int flags, UserHandle user) {
        if (context != null) {
            return context.bindServiceAsUser(service, conn, flags, user);
        }
        return false;
    }

    @HwSystemApi
    public static String getOpPackageName(Context context) {
        return context.getOpPackageName();
    }

    @HwSystemApi
    public static boolean hasRtlSupport(Context context) {
        if (context != null) {
            return context.getApplicationInfo().hasRtlSupport();
        }
        return false;
    }

    @HwSystemApi
    public static String getBasePackageName(Context context) {
        if (context != null) {
            return context.getBasePackageName();
        }
        return "";
    }

    @HwSystemApi
    public static Display getDisplay(Context context) {
        if (context != null) {
            return context.getDisplay();
        }
        return null;
    }

    @HwSystemApi
    public static void updateDisplay(Context context, int displayId) {
        if (context != null) {
            context.updateDisplay(displayId);
        }
    }
}
