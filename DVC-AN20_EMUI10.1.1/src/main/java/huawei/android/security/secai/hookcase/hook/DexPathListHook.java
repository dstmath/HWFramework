package huawei.android.security.secai.hookcase.hook;

import android.util.Log;
import huawei.android.security.HwBehaviorCollectManagerImpl;
import huawei.android.security.secai.hookcase.annotations.BackupMethod;
import huawei.android.security.secai.hookcase.annotations.HookMethod;
import huawei.android.security.secai.hookcase.utils.BehaviorIdCast;
import java.io.File;

class DexPathListHook {
    private static final String DEXPATHLIST_NAME = "dalvik.system.DexPathList";
    private static final String TAG = DexPathListHook.class.getSimpleName();

    DexPathListHook() {
    }

    @HookMethod(params = {ClassLoader.class, String.class}, reflectionTargetClass = DEXPATHLIST_NAME)
    static void dexPathListHook(Object obj, ClassLoader definingContext, String librarySearchPath) {
        HwBehaviorCollectManagerImpl.getSubClassDefault().sendBehavior(BehaviorIdCast.BehaviorId.DEXFILE_DEXPATHLIST.getValue());
        Log.i(TAG, "Call System Method: DexPathList DexPathListHook(classloader,librarySearchPath).");
        dexPathListBackup(obj, definingContext, librarySearchPath);
    }

    @BackupMethod(params = {ClassLoader.class, String.class}, reflectionTargetClass = DEXPATHLIST_NAME)
    static void dexPathListBackup(Object obj, ClassLoader definingContext, String librarySearchPath) {
        for (int i = 0; i < 100; i++) {
        }
        Log.i(TAG, "Call System Backup Method:DexPathList DexPathListBackup(classloader,librarySearchPath).");
    }

    @HookMethod(params = {ClassLoader.class, String.class, String.class, File.class}, reflectionTargetClass = DEXPATHLIST_NAME)
    static void dexPathListHook(Object obj, ClassLoader definingContext, String dexPath, String librarySearchPath, File optimizedDirectory) {
        HwBehaviorCollectManagerImpl.getSubClassDefault().sendBehavior(BehaviorIdCast.BehaviorId.DEXFILE_DEXPATHLIST.getValue());
        Log.i(TAG, "Call System Hook Method: DexPathList DexPathListHook(classLoader,string,string,file,isTrusted).");
        dexPathListBackup(obj, definingContext, dexPath, librarySearchPath, optimizedDirectory);
    }

    @BackupMethod(params = {ClassLoader.class, String.class, String.class, File.class}, reflectionTargetClass = DEXPATHLIST_NAME)
    static void dexPathListBackup(Object obj, ClassLoader definingContext, String dexPath, String librarySearchPath, File optimizedDirectory) {
        for (int i = 0; i < 100; i++) {
        }
        Log.i(TAG, "Call System backup Method: DexPathList DexPathListHook(classLoader,string,string,file,isTrusted).");
    }
}
