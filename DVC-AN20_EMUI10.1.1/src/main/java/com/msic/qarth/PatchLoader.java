package com.msic.qarth;

import android.os.FileObserver;
import com.msic.qarth.Utils.SignatureUtil;
import dalvik.system.DexClassLoader;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

public class PatchLoader {
    private static final String ENTRY_CLASS_NAME = "QarthEntry";
    private static final PatchLoader PATCH_LOADER_INSTANCE = new PatchLoader();
    private static final String TAG = PatchLoader.class.getSimpleName();
    private DisPatchFileObserver mDisPatchFileObserver;

    public static PatchLoader getInstance() {
        return PATCH_LOADER_INSTANCE;
    }

    public boolean load(QarthContext qc) {
        if (qc == null || qc.context == null || qc.patchFile == null) {
            QarthLog.e(TAG, "qarth context or patch file is null");
            return false;
        }
        QarthLog.i(TAG, "load [" + qc.patchFile.getPath() + "] and update status file to downloaded");
        long start = System.currentTimeMillis();
        qc.recordProcessUtil = new RecordProcessUtil(qc);
        qc.recordProcessUtil.updateRecordFileDownloaded();
        if ("zygote".equals(qc.packageName) || "systemserver".equals(qc.packageName) || "android".equals(qc.context.getPackageName()) || SignatureUtil.checkApkSignatureIsPlatform(qc.context, qc.patchFile.getPath())) {
            if (Constants.DEBUG) {
                QarthLog.d(TAG, "\t==> elapse time 2.1: " + (System.currentTimeMillis() - start) + " ms");
            }
            qc.patchClassLoader = genQarthClassLoader(qc);
            if (qc.patchClassLoader == null) {
                QarthLog.e(TAG, "the patch class loader is null, load patch fail for: " + qc.context.getPackageName());
                return false;
            }
            try {
                String className = qc.patchFile.getFileName() + "." + ENTRY_CLASS_NAME;
                if (Constants.DEBUG) {
                    QarthLog.d(TAG, "class name = " + className + '\n');
                }
                Class<?> entryClass = qc.patchClassLoader.loadClass(className);
                if (Constants.DEBUG) {
                    QarthLog.d(TAG, "\t==> elapse time 2.2: " + (System.currentTimeMillis() - start) + " ms");
                }
                if (entryClass == null) {
                    QarthLog.e(TAG, "the patch entry class is null, load patch fail for: " + qc.context.getPackageName());
                    return false;
                }
                Method entryMethod = entryClass.getDeclaredMethod("init", QarthContext.class);
                if (entryMethod == null) {
                    QarthLog.e(TAG, "the patch init method is null, load patch fail for: " + qc.context.getPackageName());
                    return false;
                }
                entryMethod.setAccessible(true);
                entryMethod.invoke(null, qc);
                if (Constants.DEBUG) {
                    QarthLog.d(TAG, "\t==> elapse time 2.3: " + (System.currentTimeMillis() - start) + " ms");
                }
                Method disableMethod = entryClass.getDeclaredMethod("disable", new Class[0]);
                if (disableMethod != null) {
                    disableMethod.setAccessible(true);
                    if (Constants.COMMON_PATCH_PKG_NAME.equals(qc.packageName)) {
                        disPatchFileWatching(new File(qc.patchFile.getPath() + ".disable").getName(), disableMethod);
                    }
                }
                return true;
            } catch (ClassNotFoundException e) {
                QarthLog.e(TAG, "load fwk hot patch ClassNotFoundException");
                return false;
            } catch (NoSuchMethodException e2) {
                QarthLog.e(TAG, "load fwk hot patch NoSuchMethodException");
                return false;
            } catch (InvocationTargetException e3) {
                QarthLog.e(TAG, "load fwk hot patch InvocationTargetException");
                return false;
            } catch (IllegalAccessException e4) {
                QarthLog.e(TAG, "load fwk hot patch IllegalAccessException");
                return false;
            }
        } else {
            qc.recordProcessUtil.writeErrorCodeToFile(1);
            QarthLog.e(TAG, "load [" + qc.patchFile.getPath() + "] fail, signature can not be verified");
            return false;
        }
    }

    private ClassLoader genQarthClassLoader(QarthContext param) {
        return new DexClassLoader(param.patchFile.getPath(), null, null, param.qarthClassLoader);
    }

    private void disPatchFileWatching(String disFilename, Method disableMethod) {
        if (this.mDisPatchFileObserver == null) {
            this.mDisPatchFileObserver = new DisPatchFileObserver();
            this.mDisPatchFileObserver.startWatching();
        }
        this.mDisPatchFileObserver.addMap(disFilename, disableMethod);
    }

    /* access modifiers changed from: private */
    public class DisPatchFileObserver extends FileObserver {
        private HashMap<String, Method> hashMap = new HashMap<>();

        public DisPatchFileObserver() {
            super("/data/hotpatch/fwkpatchdir/system/all", 256);
        }

        public void onEvent(int event, String path) {
            int event2 = event & 4095;
            if (Constants.DEBUG) {
                String str = PatchLoader.TAG;
                QarthLog.d(str, "DisPatchFileObserver onEvent: " + Integer.toBinaryString(event2));
            }
            if ((event2 & 256) != 0 && path != null && this.hashMap.containsKey(path)) {
                try {
                    this.hashMap.get(path).invoke(null, new Object[0]);
                } catch (InvocationTargetException e) {
                    QarthLog.e(PatchLoader.TAG, "InvocationTargetException");
                } catch (IllegalAccessException e2) {
                    QarthLog.e(PatchLoader.TAG, "IllegalAccessException");
                }
                this.hashMap.remove(path);
                if (this.hashMap.isEmpty()) {
                    stopWatching();
                }
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void addMap(String filename, Method method) {
            this.hashMap.put(filename, method);
        }
    }
}
