package huawei.android.security.secai.hookcase.hook;

import android.util.Log;
import dalvik.system.DexFile;
import huawei.android.security.HwBehaviorCollectManagerImpl;
import huawei.android.security.secai.hookcase.annotations.BackupMethod;
import huawei.android.security.secai.hookcase.annotations.HookMethod;
import huawei.android.security.secai.hookcase.utils.BehaviorIdCast;

class DexFileHook {
    private static final String TAG = DexFileHook.class.getSimpleName();

    DexFileHook() {
    }

    @HookMethod(name = "loadDex", params = {String.class, String.class, int.class}, targetClass = DexFile.class)
    static DexFile loadDexHook(String sourcePathName, String outputPathName, int flags) {
        HwBehaviorCollectManagerImpl.getSubClassDefault().sendBehavior(BehaviorIdCast.BehaviorId.DEXFILE_LOADDEX.getValue());
        Log.i(TAG, "Call System Hook Method: DexFile loadDex(String,String,int).");
        return loadDexBackup(sourcePathName, outputPathName, flags);
    }

    @BackupMethod(name = "loadDex", params = {String.class, String.class, int.class}, targetClass = DexFile.class)
    static DexFile loadDexBackup(String sourcePathName, String outputPathName, int flags) {
        for (int i = 0; i < 100; i++) {
        }
        Log.i(TAG, "Call Backup Method: DexFile loadDexBackup(String,String,int).");
        return null;
    }

    @HookMethod(params = {String.class}, targetClass = DexFile.class)
    static void dexFileHook(Object obj, String fileName) {
        HwBehaviorCollectManagerImpl.getSubClassDefault().sendBehavior(BehaviorIdCast.BehaviorId.DEXFILE_DEXFILE.getValue());
        Log.i(TAG, "Call System Hook Method: DexFile DexFile(fileName).");
        dexFileBackup(obj, fileName);
    }

    @BackupMethod(params = {String.class}, targetClass = DexFile.class)
    static void dexFileBackup(Object obj, String fileName) {
        for (int i = 0; i < 100; i++) {
        }
        Log.i(TAG, "Call System Backup Method: DexFile DexFile(fileName).");
    }
}
