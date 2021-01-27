package com.huawei.server.security.permissionmanager.recommendpermission;

import android.content.Context;
import android.content.pm.PackageManager;
import android.support.annotation.Nullable;
import android.util.LongSparseArray;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.util.SlogEx;
import com.huawei.server.security.permissionmanager.struct.HwPermissionInfo;
import com.huawei.server.security.permissionmanager.util.PermConst;
import com.huawei.server.security.permissionmanager.util.PermissionType;
import java.util.ArrayList;

public class RecommendPermUtil {
    private static final String ACCESS_BROWSER_RECORDS = "ACCESS_BROWSER_RECORDS";
    private static final String CALL_FORWARD = "CALL_FORWARD";
    private static final int DEFAULT_SOURCE_ID = 0;
    public static final int DISABLE_VALUE = 0;
    public static final int ENABLE_VALUE = 1;
    public static final int INVALID_VALUE = -1;
    private static final boolean IS_SUPPORT_BROWSER_HISTORY = SystemPropertiesEx.getBoolean("ro.config.browser_history_perm", false);
    private static final String PERMISSION_MANAGER = "hsm_permission";
    private static final int RECOMMEND_DEFAULT_CAPACITY = 21;
    private static final ArrayList<HwPermissionInfo> RECOMMEND_PERMISSION_LIST = new ArrayList<>((int) RECOMMEND_DEFAULT_CAPACITY);
    private static final String SEND_MMS = "SEND_MMS";
    private static final String SHORTCUT = "SHORTCUT";
    private static final String SOURCE_TYPE_STRING = "string";
    private static final String TAG = "RecommendPermissionUtils";

    static {
        RECOMMEND_PERMISSION_LIST.add(PermConst.READ_MSG_INFO);
        RECOMMEND_PERMISSION_LIST.add(PermConst.SEND_SMS_INFO);
        RECOMMEND_PERMISSION_LIST.add(PermConst.RECEIVE_SMS_INFO);
        RECOMMEND_PERMISSION_LIST.add(PermConst.CALL_PHONE_INFO);
        RECOMMEND_PERMISSION_LIST.add(PermConst.READ_CALLLOG_INFO);
        RECOMMEND_PERMISSION_LIST.add(PermConst.WRITE_CALL_LOG_INFO);
        RECOMMEND_PERMISSION_LIST.add(PermConst.READ_PHONE_STATUS_INFO);
        RECOMMEND_PERMISSION_LIST.add(PermConst.READ_CONTACTS_INFO);
        RECOMMEND_PERMISSION_LIST.add(PermConst.WRITE_CONTACTS_INFO);
        RECOMMEND_PERMISSION_LIST.add(PermConst.READ_CALENDAR_INFO);
        RECOMMEND_PERMISSION_LIST.add(PermConst.WRITE_CALENDAR_INFO);
        RECOMMEND_PERMISSION_LIST.add(PermConst.STORAGE_INFO);
        RECOMMEND_PERMISSION_LIST.add(PermConst.CAMERA_INFO);
        RECOMMEND_PERMISSION_LIST.add(PermConst.RECORD_AUDIO_INFO);
        RECOMMEND_PERMISSION_LIST.add(PermConst.BODY_SENSOR_INFO);
        RECOMMEND_PERMISSION_LIST.add(PermConst.LOCATION_INFO);
        RECOMMEND_PERMISSION_LIST.add(PermConst.CALL_FORWARD_INFO);
        RECOMMEND_PERMISSION_LIST.add(PermConst.ACTIVITY_RECOGNITION_INFO);
        RECOMMEND_PERMISSION_LIST.add(PermConst.SEND_MMS_INFO);
        if (IS_SUPPORT_BROWSER_HISTORY) {
            RECOMMEND_PERMISSION_LIST.add(PermConst.ACCESS_BROWSER_RECORDS_INFO);
        }
        RECOMMEND_PERMISSION_LIST.add(PermConst.EDIT_SHORTCUT_INFO);
    }

    private static class StringId {
        static final String CALLLOG_PERMISSION_NAME = "CalllogPermissionName";
        static final String CALL_PHONE_PERMISSION = "CallPhonePermission";
        static final String CAMERA_PERMISSION_GONGXIN = "CameraPermission_gongxin";
        static final String CONTACTS_PERMISSION_NAME = "ContactsPermissionName";
        static final String EDIT_SHORTCUT_PERMISSION = "Edit_shortcut_Permission";
        static final String LOCATION_PERMISSION_NAME = "LocationPermissionName";
        static final String MSG_PERMISSION_NAME = "MsgPermissionName_gongxin";
        static final String PAY_PROTECT_PERMISSION = "PayProtectPermission_gongxin";
        static final String PERMGROUPLAB_ACTIVITY_RECOGNITION = "permgrouplab_activityRecognition";
        static final String PERMGROUPLAB_USE_SENSORS = "permgrouplab_use_sensors";
        static final String PERMISSION_ACCESS_BROWSER_RECORDS = "permission_access_browser_records";
        static final String PERMISSION_CALL_FORWARD = "permission_call_forward";
        static final String PERMISSION_MODIFY_CALENDAR = "permission_modify_calendar";
        static final String PERMISSION_RECEIVE_SMS = "permission_receive_SMS";
        static final String PHONE_RECORDER_PERMISSION_ADD = "PhoneRecorderPermissionAdd";
        static final String READ_CALENDAR_PERMISSION = "ReadCalendarPermission";
        static final String READ_PHONE_CODE_PERMISSION = "ReadPhoneCodePermission";
        static final String SEND_MMS_PERMISSION = "SendMMSPermission";
        static final String STORAGE_PERMISSION = "storage_permission";
        static final String WRITE_CALLLOG_PERMISSION_NAME = "WriteCalllogPermissionName";
        static final String WRITE_CONTACTS_PERMISSION_NAME = "WriteContactsPermissionName";

        private StringId() {
        }
    }

    private RecommendPermUtil() {
    }

    public static LongSparseArray<String> getHwPermissionKeyMap() {
        LongSparseArray<String> keyMap = new LongSparseArray<>();
        keyMap.put(PermissionType.SEND_MMS, SEND_MMS);
        keyMap.put(PermissionType.EDIT_SHORTCUT, SHORTCUT);
        keyMap.put(PermissionType.CALL_FORWARD, CALL_FORWARD);
        if (IS_SUPPORT_BROWSER_HISTORY) {
            keyMap.put(PermissionType.ACCESS_BROWSER_RECORDS, ACCESS_BROWSER_RECORDS);
        }
        return keyMap;
    }

    public static LongSparseArray<Integer> getRecommendPermStringId(Context context) {
        LongSparseArray<Integer> stringIdMap = new LongSparseArray<>();
        if (context == null) {
            return stringIdMap;
        }
        Context remoteContext = null;
        try {
            remoteContext = context.createPackageContext(PermConst.SYSTEM_MANAGER_PACKAGE_NAME, 2);
        } catch (PackageManager.NameNotFoundException e) {
            SlogEx.e(TAG, "illegal package name");
        } catch (Exception e2) {
            SlogEx.e(TAG, "get source id but get unexpected exception");
        }
        stringIdMap.put(4, Integer.valueOf(getStringId(remoteContext, "MsgPermissionName_gongxin")));
        stringIdMap.put(32, Integer.valueOf(getStringId(remoteContext, "PayProtectPermission_gongxin")));
        stringIdMap.put(PermissionType.RECEIVE_SMS, Integer.valueOf(getStringId(remoteContext, "permission_receive_SMS")));
        stringIdMap.put(64, Integer.valueOf(getStringId(remoteContext, "CallPhonePermission")));
        stringIdMap.put(2, Integer.valueOf(getStringId(remoteContext, "CalllogPermissionName")));
        stringIdMap.put(PermissionType.WRITE_CALL_LOG, Integer.valueOf(getStringId(remoteContext, "WriteCalllogPermissionName")));
        stringIdMap.put(16, Integer.valueOf(getStringId(remoteContext, "ReadPhoneCodePermission")));
        stringIdMap.put(1, Integer.valueOf(getStringId(remoteContext, "ContactsPermissionName")));
        stringIdMap.put(PermissionType.WRITE_CONTACTS, Integer.valueOf(getStringId(remoteContext, "WriteContactsPermissionName")));
        stringIdMap.put(PermissionType.READ_CALENDAR, Integer.valueOf(getStringId(remoteContext, "ReadCalendarPermission")));
        stringIdMap.put(PermissionType.WRITE_CALENDAR, Integer.valueOf(getStringId(remoteContext, "permission_modify_calendar")));
        stringIdMap.put(256, Integer.valueOf(getStringId(remoteContext, "storage_permission")));
        stringIdMap.put(PermissionType.CAMERA, Integer.valueOf(getStringId(remoteContext, "CameraPermission_gongxin")));
        stringIdMap.put(128, Integer.valueOf(getStringId(remoteContext, "PhoneRecorderPermissionAdd")));
        stringIdMap.put(PermissionType.BODY_SENSORS, Integer.valueOf(getStringId(remoteContext, "permgrouplab_use_sensors")));
        stringIdMap.put(8, Integer.valueOf(getStringId(remoteContext, "LocationPermissionName")));
        stringIdMap.put(PermissionType.ACTIVITY_RECOGNITION, Integer.valueOf(getStringId(remoteContext, "permgrouplab_activityRecognition")));
        stringIdMap.put(PermissionType.CALL_FORWARD, Integer.valueOf(getStringId(remoteContext, "permission_call_forward")));
        stringIdMap.put(PermissionType.SEND_MMS, Integer.valueOf(getStringId(remoteContext, "SendMMSPermission")));
        if (IS_SUPPORT_BROWSER_HISTORY) {
            stringIdMap.put(PermissionType.ACCESS_BROWSER_RECORDS, Integer.valueOf(getStringId(remoteContext, "permission_access_browser_records")));
        }
        stringIdMap.put(PermissionType.EDIT_SHORTCUT, Integer.valueOf(getStringId(remoteContext, "Edit_shortcut_Permission")));
        return stringIdMap;
    }

    public static ArrayList<HwPermissionInfo> getRecommendPermissions() {
        return RECOMMEND_PERMISSION_LIST;
    }

    private static int getStringId(@Nullable Context remoteContext, String sourceName) {
        if (remoteContext != null) {
            return remoteContext.getResources().getIdentifier(sourceName, SOURCE_TYPE_STRING, PermConst.SYSTEM_MANAGER_PACKAGE_NAME);
        }
        return 0;
    }
}
