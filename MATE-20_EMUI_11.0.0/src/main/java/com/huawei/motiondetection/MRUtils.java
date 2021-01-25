package com.huawei.motiondetection;

import android.app.ActivityManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.UserHandle;
import android.provider.Settings;
import com.huawei.android.telephony.SignalStrengthEx;
import java.util.List;
import java.util.Locale;

public final class MRUtils {
    private static final int DEFAULT_VALUE = -1;
    private static final Uri HWMOTIONS_CONTENT_URI = Uri.parse("content://com.huawei.providers.motions/hwmotions");
    private static final Uri HWMOTIONS_CONTENT_URI_EX = Uri.parse("content://com.huawei.providers.motions.ex/hwmotions");
    public static final int MOTION_DETECTION_VERSION_CODE = 81301;
    public static final String MOTION_DETECTION_VERSION_NAME = "8.1.301";
    private static final int MOTION_SERVICE_CONFIG_VERSION_CODE = 81301;
    public static final int NOT_MULTI_USER = -1;
    private static final String TAG = "MRUtils";

    private MRUtils() {
    }

    public static String localProcess(String orgStr) {
        if (orgStr == null) {
            return "";
        }
        return orgStr.toLowerCase(Locale.getDefault()).trim();
    }

    public static void setMotionEnableState(Context context, String motionitemkey, int enable) {
        setMotionEnableState(context, motionitemkey, enable, false);
    }

    public static void setMotionEnableState(Context context, String motionitemkey, int enable, boolean isEx) {
        try {
            if (isMotionServiceConfigurable(context)) {
                ContentValues contents = new ContentValues();
                contents.put("enable", Integer.valueOf(enable));
                context.getContentResolver().update(isEx ? HWMOTIONS_CONTENT_URI_EX : HWMOTIONS_CONTENT_URI, contents, "name=?", new String[]{motionitemkey});
                context.getContentResolver().notifyChange(isEx ? HWMOTIONS_CONTENT_URI_EX : HWMOTIONS_CONTENT_URI, null);
                return;
            }
            Settings.System.putInt(context.getContentResolver(), motionitemkey, enable);
        } catch (IllegalArgumentException e) {
            MRLog.e(TAG, "setMotionEnableState throw IllegalArgumentException");
        } catch (SecurityException e2) {
            MRLog.e(TAG, "setMotionEnableState throw SecurityException");
        }
    }

    public static void observerMotionEnableStateChange(Context context, ContentObserver observer, int userId) {
        context.getContentResolver().registerContentObserver(HWMOTIONS_CONTENT_URI, false, observer, userId);
    }

    public static void setMotionEnableStateAsUser(Context context, String motionItemKey, int enable, int userId) {
        Context contextForUser = getContextAsUser(context, userId);
        if (contextForUser == null) {
            MRLog.w(TAG, "setMotionEnableStateAsUser get context as user error.");
            return;
        }
        try {
            if (isMotionServiceConfigurable(contextForUser)) {
                ContentValues contents = new ContentValues();
                contents.put("enable", Integer.valueOf(enable));
                contextForUser.getContentResolver().update(HWMOTIONS_CONTENT_URI, contents, "name=?", new String[]{motionItemKey});
                return;
            }
            Settings.System.putInt(contextForUser.getContentResolver(), motionItemKey, enable);
        } catch (IllegalArgumentException e) {
            MRLog.e(TAG, "setMotionEnableStateAsUser throw IllegalArgumentException");
        } catch (SecurityException e2) {
            MRLog.e(TAG, "setMotionEnableStateAsUser throw SecurityException");
        }
    }

    public static int getMotionEnableState(Context context, String motionItemKey) {
        return getMotionEnableState(context, motionItemKey, false);
    }

    public static int getMotionEnableState(Context context, String motionitemkey, boolean isEx) {
        int enabled = -1;
        if (isMotionServiceConfigurable(context)) {
            try {
                Cursor cursor = context.getContentResolver().query(isEx ? HWMOTIONS_CONTENT_URI_EX : HWMOTIONS_CONTENT_URI, new String[]{"name"}, "enable=1 and name=?", new String[]{motionitemkey}, null);
                if (cursor == null || cursor.getCount() <= 0) {
                    if (cursor != null) {
                        cursor.close();
                    }
                    cursor = context.getContentResolver().query(isEx ? HWMOTIONS_CONTENT_URI_EX : HWMOTIONS_CONTENT_URI, new String[]{"name"}, "enable=0 and name=?", new String[]{motionitemkey}, null);
                    if (cursor != null && cursor.getCount() > 0) {
                        enabled = 0;
                    }
                } else {
                    enabled = 1;
                }
                if (cursor != null) {
                    cursor.close();
                }
            } catch (IllegalArgumentException e) {
                MRLog.w(TAG, "getMotionEnableState->IllegalArgumentException happened");
            } catch (SecurityException e2) {
                MRLog.w(TAG, "getMotionEnableState->SecurityException happened");
            } catch (Exception e3) {
                MRLog.w(TAG, "getMotionEnableState->exception happened");
            }
        } else {
            enabled = Settings.System.getInt(context.getContentResolver(), motionitemkey, -1);
        }
        MRLog.d(TAG, "getMotionEnableState enabled: " + enabled);
        return enabled;
    }

    public static int getMotionEnableStateAsUser(Context context, String motionItemKey, int userId) {
        int enabled = -1;
        Context contextForUser = getContextAsUser(context, userId);
        if (contextForUser == null) {
            MRLog.w(TAG, "getMotionEnableStateAsUser get context as user error.");
            return -1;
        }
        if (isMotionServiceConfigurable(contextForUser)) {
            try {
                Cursor cursor = contextForUser.getContentResolver().query(HWMOTIONS_CONTENT_URI, new String[]{"name"}, "enable=1 and name=?", new String[]{motionItemKey}, null);
                if (cursor == null || cursor.getCount() <= 0) {
                    if (cursor != null) {
                        cursor.close();
                    }
                    cursor = contextForUser.getContentResolver().query(HWMOTIONS_CONTENT_URI, new String[]{"name"}, "enable=0 and name=?", new String[]{motionItemKey}, null);
                    if (cursor != null && cursor.getCount() > 0) {
                        enabled = 0;
                    }
                } else {
                    enabled = 1;
                }
                if (cursor != null) {
                    cursor.close();
                }
            } catch (IllegalArgumentException e) {
                MRLog.w(TAG, "getMotionEnableState->IllegalArgumentException happened");
            } catch (SecurityException e2) {
                MRLog.w(TAG, "getMotionEnableState->SecurityException happened");
            } catch (Exception e3) {
                MRLog.w(TAG, "getMotionEnableState->exception happened");
            }
        } else {
            enabled = Settings.System.getInt(contextForUser.getContentResolver(), motionItemKey, -1);
        }
        MRLog.d(TAG, "getMotionEnableStateAsUser enabled: " + enabled);
        return enabled;
    }

    public static boolean isMotionConfigSupported(Context context, String motionitemkey) {
        boolean isSupported = false;
        try {
            Cursor cursor = context.getContentResolver().query(HWMOTIONS_CONTENT_URI, new String[]{"name"}, "support=1 and name=?", new String[]{motionitemkey}, null);
            if (cursor != null) {
                if (cursor.getCount() > 0) {
                    isSupported = true;
                }
                cursor.close();
            }
        } catch (IllegalArgumentException e) {
            MRLog.w(TAG, "getMotionEnableState->IllegalArgumentException happened");
        } catch (SecurityException e2) {
            MRLog.w(TAG, "getMotionEnableState->SecurityException happened");
        } catch (Exception e3) {
            MRLog.w(TAG, "getMotionEnableState->exception happened");
        }
        return isSupported;
    }

    public static boolean isMotionConfigSupportedAsUser(Context context, String motionitemkey, int userId) {
        boolean isSupported = false;
        Context contextForUser = getContextAsUser(context, userId);
        if (contextForUser == null) {
            MRLog.w(TAG, "isMotionConfigSupportedAsUser get context as user error.");
            return false;
        }
        try {
            Cursor cursor = contextForUser.getContentResolver().query(HWMOTIONS_CONTENT_URI, new String[]{"name"}, "support=1 and name=?", new String[]{motionitemkey}, null);
            if (cursor != null) {
                if (cursor.getCount() > 0) {
                    isSupported = true;
                }
                cursor.close();
            }
        } catch (IllegalArgumentException e) {
            MRLog.w(TAG, "getMotionEnableState->IllegalArgumentException happened");
        } catch (SecurityException e2) {
            MRLog.w(TAG, "getMotionEnableState->SecurityException happened");
        } catch (Exception e3) {
            MRLog.w(TAG, "getMotionEnableState->exception happened");
        }
        return isSupported;
    }

    public static boolean isServiceRunning(Context context, String serviceProcess) {
        List<ActivityManager.RunningServiceInfo> serviceList = ((ActivityManager) context.getSystemService("activity")).getRunningServices(SignalStrengthEx.INVALID);
        if (serviceList == null) {
            MRLog.w(TAG, "isServiceRunning serviceList == null");
            return false;
        }
        for (ActivityManager.RunningServiceInfo service : serviceList) {
            if (service.process.equals(serviceProcess) && service.pid > 0) {
                return true;
            }
        }
        return false;
    }

    public static boolean isServiceRunningAsUser(Context context, String serviceProcess, int userId) {
        Context contextForUser = getContextAsUser(context, userId);
        if (contextForUser == null) {
            MRLog.w(TAG, "isServiceRunning get context as user error.");
            return false;
        }
        List<ActivityManager.RunningServiceInfo> serviceList = ((ActivityManager) contextForUser.getSystemService("activity")).getRunningServices(SignalStrengthEx.INVALID);
        if (serviceList == null) {
            MRLog.w(TAG, "isServiceRunningAsUser serviceList == null");
        } else {
            for (ActivityManager.RunningServiceInfo service : serviceList) {
                if (service.process.equals(serviceProcess) && service.pid > 0) {
                    if (UserHandle.getUserId(service.uid) == userId) {
                        MRLog.d(TAG, "service.process: " + service.process + " service.pid = " + service.pid + " serviceProcess: " + serviceProcess + " service.uid = " + service.uid + " user: " + UserHandle.getUserId(service.uid) + " current user id: " + userId);
                        return true;
                    }
                    MRLog.w(TAG, "service.uid = " + service.uid + " user: " + UserHandle.getUserId(service.uid) + " current user id: " + userId);
                }
            }
        }
        return false;
    }

    private static boolean isMotionServiceConfigurable(Context context) {
        try {
            PackageInfo serviceAppInfo = context.getPackageManager().getPackageInfo(MotionConfig.MOTION_SERVICE_PACKAGE, 1);
            if (serviceAppInfo == null || serviceAppInfo.versionCode < 81301) {
                return false;
            }
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            MRLog.w(TAG, "isMotionServiceConfigurable->NameNotFoundException happened");
            return false;
        }
    }

    public static int getMotionDetectionVersionCode() {
        return 81301;
    }

    public static String getMotionDetectionVersionName() {
        return MOTION_DETECTION_VERSION_NAME;
    }

    private static Context getContextAsUser(Context context, int userId) {
        if (userId == -1) {
            return context;
        }
        try {
            return context.createPackageContextAsUser(context.getPackageName(), 4, new UserHandle(userId));
        } catch (PackageManager.NameNotFoundException e) {
            MRLog.w(TAG, "getContextAsUser->NameNotFoundException happened");
            return null;
        }
    }
}
