package huawei.android.security.secai.hookcase.hook;

import android.util.Log;
import dalvik.system.BaseDexClassLoader;
import huawei.android.security.secai.hookcase.annotations.BackupMethod;
import huawei.android.security.secai.hookcase.annotations.HookMethod;
import java.nio.ByteBuffer;

class BaseDexClassLoaderHook {
    private static final String TAG = BaseDexClassLoaderHook.class.getSimpleName();

    BaseDexClassLoaderHook() {
    }

    @HookMethod(params = {String.class, String.class, ClassLoader.class, ClassLoader[].class, boolean.class}, targetClass = BaseDexClassLoader.class)
    static void baseDexClassLoaderHook(Object obj, String dexPath, String librarySearchPath, ClassLoader parent, ClassLoader[] sharedLibraryLoaders, boolean isTrusted) {
        Log.i(TAG, "Call System Hook Method: BaseDexClassLoader BaseDexClassLoaderHook(five params).");
        baseDexClassLoaderBackup(obj, dexPath, librarySearchPath, parent, sharedLibraryLoaders, isTrusted);
    }

    @BackupMethod(params = {String.class, String.class, ClassLoader.class, ClassLoader[].class, boolean.class}, targetClass = BaseDexClassLoader.class)
    static void baseDexClassLoaderBackup(Object obj, String dexPath, String librarySearchPath, ClassLoader parent, ClassLoader[] sharedLibraryLoaders, boolean isTrusted) {
        for (int i = 0; i < 100; i++) {
        }
        Log.i(TAG, "Call System Backup Method:BaseDexClassLoader BaseDexClassLoaderBackup(five params).");
    }

    @HookMethod(params = {ByteBuffer[].class, String.class, ClassLoader.class}, targetClass = BaseDexClassLoader.class)
    static void baseDexClassLoaderHook(Object obj, ByteBuffer[] dexFiles, String librarySearchPath, ClassLoader parent) {
        Log.i(TAG, "Call System Hook Method: BaseDexClassLoader BaseDexClassLoaderHook(three params).");
        baseDexClassLoaderBackup(obj, dexFiles, librarySearchPath, parent);
    }

    @BackupMethod(params = {ByteBuffer[].class, String.class, ClassLoader.class}, targetClass = BaseDexClassLoader.class)
    static void baseDexClassLoaderBackup(Object obj, ByteBuffer[] dexFiles, String librarySearchPath, ClassLoader parent) {
        for (int i = 0; i < 100; i++) {
        }
        Log.i(TAG, "Call System Hook Method: BaseDexClassLoader BaseDexClassLoaderHook(three params).");
    }
}
