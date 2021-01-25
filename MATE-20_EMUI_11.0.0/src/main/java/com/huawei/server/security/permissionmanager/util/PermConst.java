package com.huawei.server.security.permissionmanager.util;

import com.huawei.android.util.SlogEx;

public class PermConst {
    public static final String APP_LIST_KEY = "app_list_key";
    public static final String APP_MAP_KEY = "app_map_key";
    public static final String APP_UID = "uid";
    public static final String BROADCAST_KEY = "broadcast_key";
    public static final String CHECK_HW_PERM_INFO = "checkHwPerm";
    public static final String CHECK_SMS_BLOCK_STATE = "checkSmsBlockState";
    public static final int CLOSE = 2;
    public static final String COLUMN_BLOCKED_PACKAGE_NAME = "blockedPackageName";
    public static final String COLUMN_SUPPORTED_PACKAGE_NAME = "supportedPackageName";
    public static final String GET_ALL_APP_PERM_INFO = "getAllAppPermInfo";
    public static final String GET_HW_PERM_APPS_INFO = "getHwPermApps";
    public static final String GET_HW_PERM_INFO = "getHwPermInfo";
    public static final String GET_HW_PERM_INFO_FOR_PACKAGE_INSTALLER = "getHwPermInfoForInstaller";
    public static final String GET_MMS_PERM_INFO = "application/vnd.wap.mms-message";
    public static final String GET_ONE_TIME_PERMISSION_CONTROL_INFO = "getOneTimePermissionControlInfo";
    public static final String GET_PROPERTY_PERMISSIONS_HUB = "get_property_permissions_hub";
    public static final String HOTA_UPDATE_SMS_BLOCK_DATA = "hotaUpdateSmsBlockData";
    public static final String HW_PERM_SET_TABLE_NAME = "permissionCfg_";
    public static final int ILLEGAL_STATE = -1;
    public static final int ILLEGAL_UID = -1;
    public static final String INFO_TYPE_KEY = "info_type";
    public static final String INIT_SMS_BLOCK_DATA = "initSmsBlockData";
    public static final String NAME_KEY = "name_key";
    public static final String NOTIFY_SMS_PKG_REMOVE = "notifySmsPkgRemove";
    public static final String NOTIFY_SMS_USER_RESET = "notifySmsUserReset";
    public static final int NO_APPLY_PERMISSION = 4;
    public static final String ONE_TIME_PERM_TABLE_NAME = "oneTimePermPkgInfo";
    public static final String ONE_TIME_PERM_TYPE_BLOCK = "blocked";
    public static final String ONE_TIME_PERM_TYPE_SUPPORT = "supported";
    public static final int OPEN = 1;
    public static final String OPERATION_KEY = "operation_key";
    public static final String PACKAEGE_NAME_KEY = "package_name";
    public static final String PACKAGE_NAME = "packageName";
    public static final String PERMISSION_CFG = "permissionCfg";
    public static final String PERMISSION_CODE = "permissionCode";
    public static final String PERM_TYPE_KEY = "perm_type_key";
    public static final String PRE_DEFINED_SMS_CONFIG = "preDefinedSmsConfig";
    public static final String PROPERTY_PERMISSIONS_HUB_ENABLED = "permissions_hub_enabled";
    public static final int REMIND = 0;
    public static final String REMOVE_HW_PERM_INFO = "removeHwPermission";
    public static final String REPLACE_HW_PERM_INFO = "replaceHwPermInfo";
    public static final String REPLACE_PERM_ALL_APP = "replacePermForAllApp";
    public static final String RETURN_RESULT_KEY = "return_result_key";
    public static final String SET_HW_PERMS_INFO = "setHwPermissions";
    public static final String SET_HW_PERM_INFO = "setHwPermission";
    public static final String SET_HW_PERM_INFO_FOR_PACKAGE_INSTALLER = "setHwPermissionForInstaller";
    public static final String SET_ONE_TIME_PERMISSION_CONTROL_INFO = "setOneTimePermissionControlInfo";
    public static final String SET_PROPERTY_PERMISSIONS_HUB = "set_property_permissions_hub";
    public static final String SMS_BLOCK_DATA_KEY = "sms_block_data_key";
    public static final String SMS_BLOCK_TABLE_NAME = "smsBlock_";
    public static final String SUB_PERMISSION = "smsPermission";
    private static final String TAG = "PermConst";
    public static final int UNKNOWN = 3;
    public static final String USER_ID = "userId";

    private PermConst() {
        SlogEx.i(TAG, "create PerConst");
    }
}
