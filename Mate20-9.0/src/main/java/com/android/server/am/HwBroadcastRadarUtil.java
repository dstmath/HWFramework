package com.android.server.am;

import android.common.HwFrameworkFactory;
import android.content.Intent;
import android.os.Bundle;
import android.util.Flog;
import android.util.LogException;
import com.android.server.UiModeManagerService;
import java.util.HashMap;
import java.util.HashSet;

public class HwBroadcastRadarUtil {
    private static final int BUG_TYPE_BROADCAST = 104;
    private static final String CATEGORY = "framework";
    public static final String KEY_ACTION = "action";
    public static final String KEY_ACTION_COUNT = "actionCount";
    public static final String KEY_BROADCAST_INTENT = "intent";
    public static final String KEY_MMS_BROADCAST_FLAG = "mmsFlag";
    public static final String KEY_PACKAGE = "package";
    public static final String KEY_RECEIVER = "receiver";
    public static final String KEY_RECEIVE_TIME = "receiveTime";
    public static final String KEY_VERSION_CODE = "versionCode";
    public static final String KEY_VERSION_NAME = "versionName";
    private static final int LEVEL_A = 65;
    private static final int LEVEL_B = 66;
    private static final int LEVEL_C = 67;
    private static final int LEVEL_D = 68;
    private static final int MAX_BODY_SIZE = 512;
    public static final int MAX_BROADCASTQUEUE_LENGTH = 150;
    private static final int MAX_HEAD_SIZE = 256;
    public static final int SCENE_DEF_BROADCAST_OVERLENGTH = 2801;
    public static final int SCENE_DEF_RECEIVER_TIMEOUT = 2803;
    public static final long SYSTEM_BOOT_COMPLETED_TIME = 1800000;
    private static final String TAG = "BroadcastRadar";
    private static LogException mLogException = HwFrameworkFactory.getLogException();
    private HashMap<Integer, HashMap<String, HashSet<String>>> mUploadedPackages = new HashMap<>();

    public void handleBroadcastQueueOverlength(Bundle bundle) {
        if (bundle != null) {
            String packageName = bundle.getString("package", BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
            String actionName = bundle.getString(KEY_ACTION, BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
            int actionCount = bundle.getInt(KEY_ACTION_COUNT, 0);
            boolean isContainsMms = bundle.getBoolean(KEY_MMS_BROADCAST_FLAG, false);
            String receiverName = bundle.getString(KEY_RECEIVER, UiModeManagerService.Shell.NIGHT_MODE_STR_UNKNOWN);
            String versionName = bundle.getString(KEY_VERSION_NAME, UiModeManagerService.Shell.NIGHT_MODE_STR_UNKNOWN);
            StringBuilder body = new StringBuilder(512);
            body.append("Package:");
            body.append(packageName);
            body.append(" send Broadcast[");
            body.append(actionName);
            body.append("] for ");
            body.append(actionCount);
            body.append(" times, current queue ");
            body.append(isContainsMms ? "contains" : "contains not");
            body.append(" mms broadcast");
            body.append(" and curReceiver is ");
            body.append(receiverName);
            uploadExceptionLog(104, SCENE_DEF_BROADCAST_OVERLENGTH, packageName, versionName, body.toString(), bundle);
        }
    }

    public void handleReceiverTimeOut(Bundle bundle) {
        if (bundle != null) {
            String packageName = bundle.getString("package", BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
            String receiverName = bundle.getString(KEY_RECEIVER, UiModeManagerService.Shell.NIGHT_MODE_STR_UNKNOWN);
            String versionName = bundle.getString(KEY_VERSION_NAME, UiModeManagerService.Shell.NIGHT_MODE_STR_UNKNOWN);
            float receiverTime = bundle.getFloat(KEY_RECEIVE_TIME, -1.0f);
            Object objIntent = bundle.getParcelable(KEY_BROADCAST_INTENT);
            Intent intent = objIntent != null ? (Intent) objIntent : new Intent();
            StringBuilder body = new StringBuilder(512);
            body.append("Receiver[");
            body.append(receiverName);
            body.append("] from ");
            body.append(packageName);
            body.append(" receiving ");
            body.append(intent);
            body.append(" tooks ");
            body.append(receiverTime);
            body.append("s.");
            uploadExceptionLog(104, 2803, packageName, versionName, body.toString(), bundle);
        }
    }

    private boolean isPackageUploaded(int sceneDef, String pkg, String action) {
        HashMap<String, HashSet<String>> uploadedPackages = this.mUploadedPackages.get(Integer.valueOf(sceneDef));
        if (uploadedPackages == null) {
            return false;
        }
        HashSet<String> pkgActions = uploadedPackages.get(pkg);
        if (pkgActions == null || !pkgActions.contains(action)) {
            return false;
        }
        return true;
    }

    private void uploadExceptionLog(int bugType, int sceneDef, String pkg, String apkVersion, String body, Bundle extras) {
        try {
            if (!isPackageUploaded(sceneDef, pkg, extras.getString(KEY_ACTION, null))) {
                StringBuilder header = new StringBuilder(256);
                header.append("Package:");
                header.append(pkg);
                header.append("\n");
                header.append("APK version:");
                header.append(apkVersion);
                header.append("\n");
                header.append("Bug type:");
                header.append(bugType);
                header.append("\n");
                header.append("Scene def:");
                header.append(sceneDef);
                if (sceneDef == 2801) {
                    Flog.i(104, "Trigger order broadcast queue overlength radar upload.");
                } else if (sceneDef != 2803) {
                    Flog.i(104, "Trigger broadcast radar upload for scene " + sceneDef + ".");
                } else {
                    Flog.i(104, "Trigger receiver timeout radar upload. Receiver[" + extras.getString(KEY_RECEIVER, UiModeManagerService.Shell.NIGHT_MODE_STR_UNKNOWN) + "] from " + pkg + " receiving " + extras.getParcelable(KEY_BROADCAST_INTENT) + " tooks " + extras.getFloat(KEY_RECEIVE_TIME, -1.0f) + "s.");
                }
                mLogException.msg(CATEGORY, 65, header.toString(), body);
                Flog.i(104, "Radar upload for Package: " + pkg + ", BugType: " + bugType + ", Scene: " + sceneDef + ".");
                HashMap<String, HashSet<String>> uploadedPackages = this.mUploadedPackages.get(Integer.valueOf(sceneDef));
                if (uploadedPackages == null) {
                    uploadedPackages = new HashMap<>();
                    this.mUploadedPackages.put(Integer.valueOf(sceneDef), uploadedPackages);
                }
                HashSet<String> pkgActions = uploadedPackages.get(pkg);
                if (pkgActions == null) {
                    pkgActions = new HashSet<>();
                    uploadedPackages.put(pkg, pkgActions);
                }
                pkgActions.add(extras.getString(KEY_ACTION));
            }
        } catch (RuntimeException ex) {
            Flog.e(104, "radar upload failed.", ex);
        }
    }
}
