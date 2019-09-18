package android.provider;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.telecom.Log;

public class BlockedNumberContract {
    public static final String AUTHORITY = "com.android.blockednumber";
    public static final Uri AUTHORITY_URI = Uri.parse("content://com.android.blockednumber");
    public static final String EXTRA_CALL_PRESENTATION = "extra_call_presentation";
    public static final String EXTRA_CONTACT_EXIST = "extra_contact_exist";
    public static final String EXTRA_ENHANCED_SETTING_KEY = "extra_enhanced_setting_key";
    public static final String EXTRA_ENHANCED_SETTING_VALUE = "extra_enhanced_setting_value";
    public static final String METHOD_CAN_CURRENT_USER_BLOCK_NUMBERS = "can_current_user_block_numbers";
    public static final String METHOD_IS_BLOCKED = "is_blocked";
    public static final String METHOD_UNBLOCK = "unblock";
    public static final String RES_CAN_BLOCK_NUMBERS = "can_block";
    public static final String RES_ENHANCED_SETTING_IS_ENABLED = "enhanced_setting_enabled";
    public static final String RES_NUMBER_IS_BLOCKED = "blocked";
    public static final String RES_NUM_ROWS_DELETED = "num_deleted";
    public static final String RES_SHOW_EMERGENCY_CALL_NOTIFICATION = "show_emergency_call_notification";

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

        public static class BlockSuppressionStatus {
            public final boolean isSuppressed;
            public final long untilTimestampMillis;

            public BlockSuppressionStatus(boolean isSuppressed2, long untilTimestampMillis2) {
                this.isSuppressed = isSuppressed2;
                this.untilTimestampMillis = untilTimestampMillis2;
            }
        }

        public static void notifyEmergencyContact(Context context) {
            try {
                context.getContentResolver().call(BlockedNumberContract.AUTHORITY_URI, METHOD_NOTIFY_EMERGENCY_CONTACT, null, null);
            } catch (IllegalArgumentException | NullPointerException e) {
                Log.w((String) null, "notifyEmergencyContact: provider not ready.", new Object[0]);
            }
        }

        public static void endBlockSuppression(Context context) {
            context.getContentResolver().call(BlockedNumberContract.AUTHORITY_URI, METHOD_END_BLOCK_SUPPRESSION, null, null);
        }

        public static boolean shouldSystemBlockNumber(Context context, String phoneNumber, Bundle extras) {
            boolean z = false;
            try {
                Bundle res = context.getContentResolver().call(BlockedNumberContract.AUTHORITY_URI, METHOD_SHOULD_SYSTEM_BLOCK_NUMBER, phoneNumber, extras);
                if (res != null && res.getBoolean(BlockedNumberContract.RES_NUMBER_IS_BLOCKED, false)) {
                    z = true;
                }
                return z;
            } catch (IllegalArgumentException | NullPointerException e) {
                Log.w((String) null, "shouldSystemBlockNumber: provider not ready.", new Object[0]);
                return false;
            }
        }

        public static BlockSuppressionStatus getBlockSuppressionStatus(Context context) {
            Bundle res = context.getContentResolver().call(BlockedNumberContract.AUTHORITY_URI, METHOD_GET_BLOCK_SUPPRESSION_STATUS, null, null);
            boolean z = false;
            if (res != null && res.getBoolean(RES_IS_BLOCKING_SUPPRESSED, false)) {
                z = true;
            }
            long j = 0;
            if (res != null) {
                j = res.getLong(RES_BLOCKING_SUPPRESSED_UNTIL_TIMESTAMP, 0);
            }
            return new BlockSuppressionStatus(z, j);
        }

        public static boolean shouldShowEmergencyCallNotification(Context context) {
            boolean z = false;
            try {
                Bundle res = context.getContentResolver().call(BlockedNumberContract.AUTHORITY_URI, METHOD_SHOULD_SHOW_EMERGENCY_CALL_NOTIFICATION, null, null);
                if (res != null && res.getBoolean("show_emergency_call_notification", false)) {
                    z = true;
                }
                return z;
            } catch (IllegalArgumentException | NullPointerException e) {
                Log.w((String) null, "shouldShowEmergencyCallNotification: provider not ready.", new Object[0]);
                return false;
            }
        }

        public static boolean getEnhancedBlockSetting(Context context, String key) {
            Bundle extras = new Bundle();
            extras.putString(BlockedNumberContract.EXTRA_ENHANCED_SETTING_KEY, key);
            boolean z = false;
            try {
                Bundle res = context.getContentResolver().call(BlockedNumberContract.AUTHORITY_URI, METHOD_GET_ENHANCED_BLOCK_SETTING, null, extras);
                if (res != null && res.getBoolean(BlockedNumberContract.RES_ENHANCED_SETTING_IS_ENABLED, false)) {
                    z = true;
                }
                return z;
            } catch (IllegalArgumentException | NullPointerException e) {
                Log.w((String) null, "getEnhancedBlockSetting: provider not ready.", new Object[0]);
                return false;
            }
        }

        public static void setEnhancedBlockSetting(Context context, String key, boolean value) {
            Bundle extras = new Bundle();
            extras.putString(BlockedNumberContract.EXTRA_ENHANCED_SETTING_KEY, key);
            extras.putBoolean(BlockedNumberContract.EXTRA_ENHANCED_SETTING_VALUE, value);
            context.getContentResolver().call(BlockedNumberContract.AUTHORITY_URI, METHOD_SET_ENHANCED_BLOCK_SETTING, null, extras);
        }
    }

    private BlockedNumberContract() {
    }

    public static boolean isBlocked(Context context, String phoneNumber) {
        boolean z = false;
        try {
            Bundle res = context.getContentResolver().call(AUTHORITY_URI, METHOD_IS_BLOCKED, phoneNumber, null);
            if (res != null && res.getBoolean(RES_NUMBER_IS_BLOCKED, false)) {
                z = true;
            }
            return z;
        } catch (IllegalArgumentException | NullPointerException e) {
            Log.w((String) null, "isBlocked: provider not ready.", new Object[0]);
            return false;
        }
    }

    public static int unblock(Context context, String phoneNumber) {
        Bundle res = context.getContentResolver().call(AUTHORITY_URI, METHOD_UNBLOCK, phoneNumber, null);
        if (res != null) {
            return res.getInt(RES_NUM_ROWS_DELETED, 0);
        }
        return 0;
    }

    public static boolean canCurrentUserBlockNumbers(Context context) {
        boolean z = false;
        try {
            Bundle res = context.getContentResolver().call(AUTHORITY_URI, METHOD_CAN_CURRENT_USER_BLOCK_NUMBERS, null, null);
            if (res != null && res.getBoolean(RES_CAN_BLOCK_NUMBERS, false)) {
                z = true;
            }
            return z;
        } catch (IllegalArgumentException | NullPointerException e) {
            Log.w((String) null, "canCurrentUserBlockNumbers: provider not ready.", new Object[0]);
            return false;
        }
    }
}
