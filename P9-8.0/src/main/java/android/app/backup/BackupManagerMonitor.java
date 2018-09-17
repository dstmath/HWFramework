package android.app.backup;

import android.os.Bundle;

public class BackupManagerMonitor {
    public static final String EXTRA_LOG_CANCEL_ALL = "android.app.backup.extra.LOG_CANCEL_ALL";
    public static final String EXTRA_LOG_EVENT_CATEGORY = "android.app.backup.extra.LOG_EVENT_CATEGORY";
    public static final String EXTRA_LOG_EVENT_ID = "android.app.backup.extra.LOG_EVENT_ID";
    public static final String EXTRA_LOG_EVENT_PACKAGE_NAME = "android.app.backup.extra.LOG_EVENT_PACKAGE_NAME";
    public static final String EXTRA_LOG_EVENT_PACKAGE_VERSION = "android.app.backup.extra.LOG_EVENT_PACKAGE_VERSION";
    public static final String EXTRA_LOG_EXCEPTION_FULL_BACKUP = "android.app.backup.extra.LOG_EXCEPTION_FULL_BACKUP";
    public static final String EXTRA_LOG_ILLEGAL_KEY = "android.app.backup.extra.LOG_ILLEGAL_KEY";
    public static final String EXTRA_LOG_MANIFEST_PACKAGE_NAME = "android.app.backup.extra.LOG_MANIFEST_PACKAGE_NAME";
    public static final String EXTRA_LOG_OLD_VERSION = "android.app.backup.extra.LOG_OLD_VERSION";
    public static final String EXTRA_LOG_POLICY_ALLOW_APKS = "android.app.backup.extra.LOG_POLICY_ALLOW_APKS";
    public static final String EXTRA_LOG_PREFLIGHT_ERROR = "android.app.backup.extra.LOG_PREFLIGHT_ERROR";
    public static final String EXTRA_LOG_RESTORE_ANYWAY = "android.app.backup.extra.LOG_RESTORE_ANYWAY";
    public static final String EXTRA_LOG_RESTORE_VERSION = "android.app.backup.extra.LOG_RESTORE_VERSION";
    public static final String EXTRA_LOG_WIDGET_PACKAGE_NAME = "android.app.backup.extra.LOG_WIDGET_PACKAGE_NAME";
    public static final int LOG_EVENT_CATEGORY_AGENT = 2;
    public static final int LOG_EVENT_CATEGORY_BACKUP_MANAGER_POLICY = 3;
    public static final int LOG_EVENT_CATEGORY_TRANSPORT = 1;
    public static final int LOG_EVENT_ID_APK_NOT_INSTALLED = 40;
    public static final int LOG_EVENT_ID_APP_HAS_NO_AGENT = 28;
    public static final int LOG_EVENT_ID_BACKUP_DISABLED = 13;
    public static final int LOG_EVENT_ID_CANNOT_RESTORE_WITHOUT_APK = 41;
    public static final int LOG_EVENT_ID_CANT_FIND_AGENT = 30;
    public static final int LOG_EVENT_ID_CORRUPT_MANIFEST = 46;
    public static final int LOG_EVENT_ID_DEVICE_NOT_PROVISIONED = 14;
    public static final int LOG_EVENT_ID_ERROR_PREFLIGHT = 16;
    public static final int LOG_EVENT_ID_EXCEPTION_FULL_BACKUP = 19;
    public static final int LOG_EVENT_ID_EXPECTED_DIFFERENT_PACKAGE = 43;
    public static final int LOG_EVENT_ID_FULL_BACKUP_CANCEL = 4;
    public static final int LOG_EVENT_ID_FULL_RESTORE_ALLOW_BACKUP_FALSE = 39;
    public static final int LOG_EVENT_ID_FULL_RESTORE_SIGNATURE_MISMATCH = 37;
    public static final int LOG_EVENT_ID_FULL_RESTORE_TIMEOUT = 45;
    public static final int LOG_EVENT_ID_ILLEGAL_KEY = 5;
    public static final int LOG_EVENT_ID_KEY_VALUE_BACKUP_CANCEL = 21;
    public static final int LOG_EVENT_ID_KEY_VALUE_RESTORE_TIMEOUT = 31;
    public static final int LOG_EVENT_ID_LOST_TRANSPORT = 25;
    public static final int LOG_EVENT_ID_MISSING_SIGNATURE = 42;
    public static final int LOG_EVENT_ID_NO_DATA_TO_SEND = 7;
    public static final int LOG_EVENT_ID_NO_PACKAGES = 49;
    public static final int LOG_EVENT_ID_NO_PM_METADATA_RECEIVED = 23;
    public static final int LOG_EVENT_ID_NO_RESTORE_METADATA_AVAILABLE = 22;
    public static final int LOG_EVENT_ID_PACKAGE_INELIGIBLE = 9;
    public static final int LOG_EVENT_ID_PACKAGE_KEY_VALUE_PARTICIPANT = 10;
    public static final int LOG_EVENT_ID_PACKAGE_NOT_FOUND = 12;
    public static final int LOG_EVENT_ID_PACKAGE_NOT_PRESENT = 26;
    public static final int LOG_EVENT_ID_PACKAGE_STOPPED = 11;
    public static final int LOG_EVENT_ID_PACKAGE_TRANSPORT_NOT_PRESENT = 15;
    public static final int LOG_EVENT_ID_PM_AGENT_HAS_NO_METADATA = 24;
    public static final int LOG_EVENT_ID_QUOTA_HIT_PREFLIGHT = 18;
    public static final int LOG_EVENT_ID_RESTORE_ANY_VERSION = 34;
    public static final int LOG_EVENT_ID_RESTORE_VERSION_HIGHER = 27;
    public static final int LOG_EVENT_ID_SIGNATURE_MISMATCH = 29;
    public static final int LOG_EVENT_ID_SYSTEM_APP_NO_AGENT = 38;
    public static final int LOG_EVENT_ID_TRANSPORT_IS_NULL = 50;
    public static final int LOG_EVENT_ID_UNKNOWN_VERSION = 44;
    public static final int LOG_EVENT_ID_VERSIONS_MATCH = 35;
    public static final int LOG_EVENT_ID_VERSION_OF_BACKUP_OLDER = 36;
    public static final int LOG_EVENT_ID_WIDGET_METADATA_MISMATCH = 47;
    public static final int LOG_EVENT_ID_WIDGET_UNKNOWN_VERSION = 48;

    public void onEvent(Bundle event) {
    }
}
