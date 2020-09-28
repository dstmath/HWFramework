package com.huawei.android.provider;

import android.content.Context;
import android.net.Uri;
import android.os.UserManager;
import android.provider.BaseColumns;
import android.provider.CallLog;

public final class CallLogEx {
    public static final String SHADOW_AUTHORITY = "call_log_shadow";

    public static class Calls implements BaseColumns {
        public static final String ADD_FOR_ALL_USERS = "add_for_all_users";
        public static final String ALLOW_VOICEMAILS_PARAM_KEY = "allow_voicemails";
        public static final String PHONE_ACCOUNT_ADDRESS = "phone_account_address";
        public static final String PHONE_ACCOUNT_HIDDEN = "phone_account_hidden";
        public static final Uri SHADOW_CONTENT_URI = CallLog.Calls.SHADOW_CONTENT_URI;
        public static final String SUB_ID = "sub_id";

        public static boolean shouldHaveSharedCallLogEntries(Context context, UserManager userManager, int userId) {
            return CallLog.Calls.shouldHaveSharedCallLogEntries(context, userManager, userId);
        }
    }
}
