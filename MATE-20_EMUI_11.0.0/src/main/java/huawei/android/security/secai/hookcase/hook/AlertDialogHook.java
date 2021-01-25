package huawei.android.security.secai.hookcase.hook;

import android.app.AlertDialog;
import android.util.Log;
import android.view.View;
import huawei.android.security.HwBehaviorCollectManagerImpl;
import huawei.android.security.secai.hookcase.annotations.BackupMethod;
import huawei.android.security.secai.hookcase.annotations.HookMethod;
import huawei.android.security.secai.hookcase.utils.BehaviorIdCast;

class AlertDialogHook {
    private static final String TAG = AlertDialogHook.class.getSimpleName();

    AlertDialogHook() {
    }

    @HookMethod(name = "setTitle", params = {CharSequence.class}, targetClass = AlertDialog.class)
    static void setTitleHook(Object obj, CharSequence title) {
        HwBehaviorCollectManagerImpl.getSubClassDefault().sendBehavior(BehaviorIdCast.BehaviorId.ALERTDIALOG_SETTITLE.getValue());
        Log.i(TAG, "Call System Hook Method: AlertDialog setTitleHook().");
        setTitleBackup(obj, title);
    }

    @BackupMethod(name = "setTitle", params = {CharSequence.class}, targetClass = AlertDialog.class)
    static void setTitleBackup(Object obj, CharSequence title) {
        for (int i = 0; i < 100; i++) {
        }
        Log.i(TAG, "Call System Backup Method:AlertDialog setTitleBackup().");
    }

    @HookMethod(name = "setCustomTitle", params = {View.class}, targetClass = AlertDialog.class)
    static void setCustomTitleHook(Object obj, View customTitleView) {
        HwBehaviorCollectManagerImpl.getSubClassDefault().sendBehavior(BehaviorIdCast.BehaviorId.ALERTDIALOG_SETCUSTOMTITLE.getValue());
        Log.i(TAG, "Call System Hook Method: AlertDialog setCustomTitleHook().");
        setCustomTitleBackup(obj, customTitleView);
    }

    @BackupMethod(name = "setCustomTitle", params = {View.class}, targetClass = AlertDialog.class)
    static void setCustomTitleBackup(Object obj, View customTitleView) {
        for (int i = 0; i < 100; i++) {
        }
        Log.i(TAG, "Call Backup Method: AlertDialog setCustomTitleBackup().");
    }

    @HookMethod(name = "setMessage", params = {CharSequence.class}, targetClass = AlertDialog.class)
    static void setMessageHook(Object obj, CharSequence message) {
        HwBehaviorCollectManagerImpl.getSubClassDefault().sendBehavior(BehaviorIdCast.BehaviorId.ALERTDIALOG_SETMESSAGE.getValue());
        Log.i(TAG, "Call System Hook Method:AlertDialog setMessageHook().");
        setMessageBackup(obj, message);
    }

    @BackupMethod(name = "setMessage", params = {CharSequence.class}, targetClass = AlertDialog.class)
    static void setMessageBackup(Object obj, CharSequence message) {
        for (int i = 0; i < 100; i++) {
        }
        Log.i(TAG, "Call Backup Method: AlertDialog setMessageBackup().");
    }

    @HookMethod(name = "setIcon", params = {int.class}, targetClass = AlertDialog.class)
    static void setIconHook(Object obj, int resId) {
        HwBehaviorCollectManagerImpl.getSubClassDefault().sendBehavior(BehaviorIdCast.BehaviorId.ALERTDIALOG_SETICON.getValue());
        Log.i(TAG, "Call System Hook Method: AlertDialog setIconHook().");
        setIconBackup(obj, resId);
    }

    @BackupMethod(name = "setIcon", params = {int.class}, targetClass = AlertDialog.class)
    static void setIconBackup(Object obj, int resId) {
        for (int i = 0; i < 100; i++) {
        }
        Log.i(TAG, "Call Backup Method:AlertDialog setIconBackup().");
    }
}
