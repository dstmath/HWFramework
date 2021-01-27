package huawei.android.security.secai.hookcase.hook;

import android.util.Log;
import huawei.android.security.secai.hookcase.annotations.BackupMethod;
import huawei.android.security.secai.hookcase.annotations.HookMethod;

class SmsMessageBaseHook {
    private static final String SMSMESSAGEBASE_CLASSNAME = "com.android.internal.telephony.SmsMessageBase";
    private static final String TAG = SmsMessageBaseHook.class.getSimpleName();

    SmsMessageBaseHook() {
    }

    @HookMethod(name = "getOriginatingAddress", params = {}, reflectionTargetClass = SMSMESSAGEBASE_CLASSNAME)
    static String getOriginatingAddressHook(Object obj) {
        Log.i(TAG, "Call System Hook Method: SmsMessageBase getOriginatingAddressHook().");
        return getOriginatingAddressBackup(obj);
    }

    @BackupMethod(name = "getOriginatingAddress", params = {}, reflectionTargetClass = SMSMESSAGEBASE_CLASSNAME)
    static String getOriginatingAddressBackup(Object obj) {
        for (int i = 0; i < 100; i++) {
        }
        Log.i(TAG, "Call Backup Method: SmsMessageBase getOriginatingAddressBackup().");
        return null;
    }

    @HookMethod(name = "getMessageBody", params = {}, reflectionTargetClass = SMSMESSAGEBASE_CLASSNAME)
    static String getMessageBodyHook(Object obj) {
        Log.i(TAG, "Call System Hook Method: SmsMessageBase getMessageBody().");
        return getMessageBodyBackup(obj);
    }

    @BackupMethod(name = "getMessageBody", params = {}, reflectionTargetClass = SMSMESSAGEBASE_CLASSNAME)
    static String getMessageBodyBackup(Object obj) {
        for (int i = 0; i < 100; i++) {
        }
        Log.i(TAG, "Call Backup Method: SmsMessageBase getMessageBody()");
        return null;
    }
}
