package com.msic.qarth;

import android.cover.CoverManager;
import com.msic.qarth.Utils.SignatureUtil;
import dalvik.system.DexClassLoader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class PatchLoader {
    private static final String ENTRY_CLASS_NAME = "QarthEntry";
    private static final String TAG = PatchLoader.class.getSimpleName();
    private static PatchLoader mInstance = null;

    public static PatchLoader getInstance() {
        if (mInstance == null) {
            mInstance = new PatchLoader();
        }
        return mInstance;
    }

    public boolean load(QarthContext qc) {
        if (qc == null || qc.context == null || qc.patchFile == null) {
            QarthLog.e(TAG, "qarth context or patch file is null");
            return false;
        }
        if (Constants.DEBUG) {
            QarthLog.d(TAG, "load [" + qc.patchFile.getPath() + "]");
        }
        long start = System.currentTimeMillis();
        qc.recordProcessUtil = new RecordProcessUtil(qc);
        qc.recordProcessUtil.updateRecordFileDownloaded();
        if ("zygote".equals(qc.packageName) || "systemserver".equals(qc.packageName) || CoverManager.HALL_STATE_RECEIVER_DEFINE.equals(qc.context.getPackageName()) || SignatureUtil.checkApkSignatureIsPlatform(qc.context, qc.patchFile.getPath())) {
            if (Constants.DEBUG) {
                QarthLog.d(TAG, "\t==> elapse time 2.1: " + (System.currentTimeMillis() - start) + " ms");
            }
            qc.patchClassLoader = genQarthClassLoader(qc);
            if (qc.patchClassLoader == null) {
                return false;
            }
            try {
                String className = qc.patchFile.getFileName() + "." + ENTRY_CLASS_NAME;
                if (Constants.DEBUG) {
                    QarthLog.d(TAG, "class name = " + className + 10);
                }
                Class<?> entryClass = qc.patchClassLoader.loadClass(className);
                if (Constants.DEBUG) {
                    QarthLog.d(TAG, "\t==> elapse time 2.2: " + (System.currentTimeMillis() - start) + " ms");
                }
                if (entryClass == null) {
                    return false;
                }
                Method entryMethod = entryClass.getDeclaredMethod("init", new Class[]{QarthContext.class});
                if (entryMethod == null) {
                    return false;
                }
                entryMethod.setAccessible(true);
                entryMethod.invoke(null, new Object[]{qc});
                if (Constants.DEBUG) {
                    QarthLog.d(TAG, "\t==> elapse time 2.3: " + (System.currentTimeMillis() - start) + " ms");
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
            QarthLog.e(TAG, "load [" + qc.patchFile.getPath() + "] fail, signature can not be verified");
            return false;
        }
    }

    private ClassLoader genQarthClassLoader(QarthContext param) {
        return new DexClassLoader(param.patchFile.getPath(), null, null, param.qarthClassLoader);
    }
}
