package android.provider;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.telecom.Log;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class BlockedNumberContract {
    public static final String AUTHORITY = "com.android.blockednumber";
    public static final Uri AUTHORITY_URI = Uri.parse("content://com.android.blockednumber");
    public static final String EXTRA_CALL_PRESENTATION = "extra_call_presentation";
    public static final String EXTRA_CONTACT_EXIST = "extra_contact_exist";
    public static final String EXTRA_ENHANCED_SETTING_KEY = "extra_enhanced_setting_key";
    public static final String EXTRA_ENHANCED_SETTING_VALUE = "extra_enhanced_setting_value";
    private static final String LOG_TAG = BlockedNumberContract.class.getSimpleName();
    public static final String METHOD_CAN_CURRENT_USER_BLOCK_NUMBERS = "can_current_user_block_numbers";
    public static final String METHOD_IS_BLOCKED = "is_blocked";
    public static final String METHOD_UNBLOCK = "unblock";
    public static final String RES_BLOCK_STATUS = "block_status";
    public static final String RES_CAN_BLOCK_NUMBERS = "can_block";
    public static final String RES_ENHANCED_SETTING_IS_ENABLED = "enhanced_setting_enabled";
    public static final String RES_NUMBER_IS_BLOCKED = "blocked";
    public static final String RES_NUM_ROWS_DELETED = "num_deleted";
    public static final String RES_SHOW_EMERGENCY_CALL_NOTIFICATION = "show_emergency_call_notification";
    public static final int STATUS_BLOCKED_IN_LIST = 1;
    public static final int STATUS_BLOCKED_NOT_IN_CONTACTS = 5;
    public static final int STATUS_BLOCKED_PAYPHONE = 4;
    public static final int STATUS_BLOCKED_RESTRICTED = 2;
    public static final int STATUS_BLOCKED_UNKNOWN_NUMBER = 3;
    public static final int STATUS_NOT_BLOCKED = 0;

    @Retention(RetentionPolicy.SOURCE)
    public @interface BlockStatus {
    }

    private BlockedNumberContract() {
    }

    public static class BlockedNumbers {
        public static final String COLUMN_E164_NUMBER = "e164_number";
        public static final String COLUMN_ID = "_id";
        public static final String COLUMN_ORIGINAL_NUMBER = "original_number";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/blocked_number";
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/blocked_number";
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BlockedNumberContract.AUTHORITY_URI, BlockedNumberContract.RES_NUMBER_IS_BLOCKED);

        private BlockedNumbers() {
        }
    }

    public static boolean isBlocked(Context context, String phoneNumber) {
        try {
            Bundle res = context.getContentResolver().call(AUTHORITY_URI, METHOD_IS_BLOCKED, phoneNumber, (Bundle) null);
            boolean isBlocked = res != null && res.getBoolean(RES_NUMBER_IS_BLOCKED, false);
            Log.d(LOG_TAG, "isBlocked: phoneNumber=%s, isBlocked=%b", Log.piiHandle(phoneNumber), Boolean.valueOf(isBlocked));
            return isBlocked;
        } catch (IllegalArgumentException | NullPointerException e) {
            Log.w((String) null, "isBlocked: provider not ready.", new Object[0]);
            return false;
        }
    }

    public static int unblock(Context context, String phoneNumber) {
        Log.d(LOG_TAG, "unblock: phoneNumber=%s", Log.piiHandle(phoneNumber));
        return context.getContentResolver().call(AUTHORITY_URI, METHOD_UNBLOCK, phoneNumber, (Bundle) null).getInt(RES_NUM_ROWS_DELETED, 0);
    }

    public static boolean canCurrentUserBlockNumbers(Context context) {
        try {
            Bundle res = context.getContentResolver().call(AUTHORITY_URI, METHOD_CAN_CURRENT_USER_BLOCK_NUMBERS, (String) null, (Bundle) null);
            if (res == null || !res.getBoolean(RES_CAN_BLOCK_NUMBERS, false)) {
                return false;
            }
            return true;
        } catch (IllegalArgumentException | NullPointerException e) {
            Log.w((String) null, "canCurrentUserBlockNumbers: provider not ready.", new Object[0]);
            return false;
        }
    }

    public static class SystemContract {
        public static final String ACTION_BLOCK_SUPPRESSION_STATE_CHANGED = "android.provider.action.BLOCK_SUPPRESSION_STATE_CHANGED";
        public static final String ENHANCED_SETTING_KEY_BLOCK_PAYPHONE = "block_payphone_calls_setting";
        public static final String ENHANCED_SETTING_KEY_BLOCK_PRIVATE = "block_private_number_calls_setting";
        public static final String ENHANCED_SETTING_KEY_BLOCK_UNKNOWN = "block_unknown_calls_setting";
        public static final String ENHANCED_SETTING_KEY_BLOCK_UNREGISTERED = "block_numbers_not_in_contacts_setting";
        public static final String ENHANCED_SETTING_KEY_SHOW_EMERGENCY_CALL_NOTIFICATION = "show_emergency_call_notification";
        public static final String METHOD_END_BLOCK_SUPPRESSION = "end_block_suppression";
        public static final String METHOD_GET_BLOCK_SUPPRESSION_STATUS = "get_block_suppression_status";
        public static final String METHOD_GET_ENHANCED_BLOCK_SETTING = "get_enhanced_block_setting";
        public static final String METHOD_NOTIFY_EMERGENCY_CONTACT = "notify_emergency_contact";
        public static final String METHOD_SET_ENHANCED_BLOCK_SETTING = "set_enhanced_block_setting";
        public static final String METHOD_SHOULD_SHOW_EMERGENCY_CALL_NOTIFICATION = "should_show_emergency_call_notification";
        public static final String METHOD_SHOULD_SYSTEM_BLOCK_NUMBER = "should_system_block_number";
        public static final String RES_BLOCKING_SUPPRESSED_UNTIL_TIMESTAMP = "blocking_suppressed_until_timestamp";
        public static final String RES_IS_BLOCKING_SUPPRESSED = "blocking_suppressed";

        public static void notifyEmergencyContact(Context context) {
            try {
                Log.i(BlockedNumberContract.LOG_TAG, "notifyEmergencyContact; caller=%s", context.getOpPackageName());
                context.getContentResolver().call(BlockedNumberContract.AUTHORITY_URI, METHOD_NOTIFY_EMERGENCY_CONTACT, (String) null, (Bundle) null);
            } catch (IllegalArgumentException | NullPointerException e) {
                Log.w((String) null, "notifyEmergencyContact: provider not ready.", new Object[0]);
            }
        }

        public static void endBlockSuppression(Context context) {
            String caller = context.getOpPackageName();
            Log.i(BlockedNumberContract.LOG_TAG, "endBlockSuppression: caller=%s", caller);
            context.getContentResolver().call(BlockedNumberContract.AUTHORITY_URI, METHOD_END_BLOCK_SUPPRESSION, (String) null, (Bundle) null);
        }

        public static int shouldSystemBlockNumber(Context context, String phoneNumber, Bundle extras) {
            int blockResult;
            try {
                String caller = context.getOpPackageName();
                Bundle res = context.getContentResolver().call(BlockedNumberContract.AUTHORITY_URI, METHOD_SHOULD_SYSTEM_BLOCK_NUMBER, phoneNumber, extras);
                if (res != null) {
                    blockResult = res.getInt(BlockedNumberContract.RES_BLOCK_STATUS, 0);
                } else {
                    blockResult = 0;
                }
                Log.d(BlockedNumberContract.LOG_TAG, "shouldSystemBlockNumber: number=%s, caller=%s, result=%s", Log.piiHandle(phoneNumber), caller, blockStatusToString(blockResult));
                return blockResult;
            } catch (IllegalArgumentException | NullPointerException e) {
                Log.w((String) null, "shouldSystemBlockNumber: provider not ready.", new Object[0]);
                return 0;
            }
        }

        public static BlockSuppressionStatus getBlockSuppressionStatus(Context context) {
            Bundle res = context.getContentResolver().call(BlockedNumberContract.AUTHORITY_URI, METHOD_GET_BLOCK_SUPPRESSION_STATUS, (String) null, (Bundle) null);
            BlockSuppressionStatus blockSuppressionStatus = new BlockSuppressionStatus(res.getBoolean(RES_IS_BLOCKING_SUPPRESSED, false), res.getLong(RES_BLOCKING_SUPPRESSED_UNTIL_TIMESTAMP, 0));
            Log.d(BlockedNumberContract.LOG_TAG, "getBlockSuppressionStatus: caller=%s, status=%s", context.getOpPackageName(), blockSuppressionStatus);
            return blockSuppressionStatus;
        }

        public static boolean shouldShowEmergencyCallNotification(Context context) {
            try {
                Bundle res = context.getContentResolver().call(BlockedNumberContract.AUTHORITY_URI, METHOD_SHOULD_SHOW_EMERGENCY_CALL_NOTIFICATION, (String) null, (Bundle) null);
                if (res == null || !res.getBoolean("show_emergency_call_notification", false)) {
                    return false;
                }
                return true;
            } catch (IllegalArgumentException | NullPointerException e) {
                Log.w((String) null, "shouldShowEmergencyCallNotification: provider not ready.", new Object[0]);
                return false;
            }
        }

        public static boolean getEnhancedBlockSetting(Context context, String key) {
            Bundle extras = new Bundle();
            extras.putString(BlockedNumberContract.EXTRA_ENHANCED_SETTING_KEY, key);
            try {
                Bundle res = context.getContentResolver().call(BlockedNumberContract.AUTHORITY_URI, METHOD_GET_ENHANCED_BLOCK_SETTING, (String) null, extras);
                if (res == null || !res.getBoolean(BlockedNumberContract.RES_ENHANCED_SETTING_IS_ENABLED, false)) {
                    return false;
                }
                return true;
            } catch (IllegalArgumentException | NullPointerException e) {
                Log.w((String) null, "getEnhancedBlockSetting: provider not ready.", new Object[0]);
                return false;
            }
        }

        public static void setEnhancedBlockSetting(Context context, String key, boolean value) {
            Bundle extras = new Bundle();
            extras.putString(BlockedNumberContract.EXTRA_ENHANCED_SETTING_KEY, key);
            extras.putBoolean(BlockedNumberContract.EXTRA_ENHANCED_SETTING_VALUE, value);
            context.getContentResolver().call(BlockedNumberContract.AUTHORITY_URI, METHOD_SET_ENHANCED_BLOCK_SETTING, (String) null, extras);
        }

        public static String blockStatusToString(int blockStatus) {
            if (blockStatus == 0) {
                return "not blocked";
            }
            if (blockStatus == 1) {
                return "blocked - in list";
            }
            if (blockStatus == 2) {
                return "blocked - restricted";
            }
            if (blockStatus == 3) {
                return "blocked - unknown";
            }
            if (blockStatus == 4) {
                return "blocked - payphone";
            }
            if (blockStatus != 5) {
                return "unknown";
            }
            return "blocked - not in contacts";
        }

        public static class BlockSuppressionStatus {
            public final boolean isSuppressed;
            public final long untilTimestampMillis;

            public BlockSuppressionStatus(boolean isSuppressed2, long untilTimestampMillis2) {
                this.isSuppressed = isSuppressed2;
                this.untilTimestampMillis = untilTimestampMillis2;
            }

            public String toString() {
                return "[BlockSuppressionStatus; isSuppressed=" + this.isSuppressed + ", until=" + this.untilTimestampMillis + "]";
            }
        }
    }
}
