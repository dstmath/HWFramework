package huawei.android.security.secai.hookcase.hook;

import android.content.IntentSender;
import android.content.pm.PackageInstaller;
import android.util.Log;
import huawei.android.security.secai.hookcase.annotations.BackupMethod;
import huawei.android.security.secai.hookcase.annotations.HookMethod;

class PackageInstallerHook {
    private static final String PACKAGEINSTALLER_NAME = "android.content.pm.PackageInstaller$Session";
    private static final String TAG = PackageInstallerHook.class.getSimpleName();

    PackageInstallerHook() {
    }

    @HookMethod(name = "commit", params = {IntentSender.class}, targetClass = PackageInstaller.Session.class)
    static void commitHook(Object obj, IntentSender statusReceiver) {
        Log.i(TAG, "Call System Hook Method: PackageInstaller commitHook()");
        commitBackup(obj, statusReceiver);
    }

    @BackupMethod(name = "commit", params = {IntentSender.class}, targetClass = PackageInstaller.Session.class)
    static void commitBackup(Object obj, IntentSender statusReceiver) {
        for (int i = 0; i < 100; i++) {
        }
        Log.i(TAG, "Call System Backup Method: PackageInstaller commitBackup().");
    }
}
