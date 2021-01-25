package com.android.server.display.color;

import android.util.SparseArray;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.annotations.VisibleForTesting;
import com.android.server.display.color.ColorDisplayService;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/* access modifiers changed from: package-private */
public class AppSaturationController {
    @VisibleForTesting
    static final float[] TRANSLATION_VECTOR = {0.0f, 0.0f, 0.0f};
    @GuardedBy({"mLock"})
    private final Map<String, SparseArray<SaturationController>> mAppsMap = new HashMap();
    private final Object mLock = new Object();

    AppSaturationController() {
    }

    /* access modifiers changed from: package-private */
    public boolean addColorTransformController(String packageName, int userId, WeakReference<ColorDisplayService.ColorTransformController> controller) {
        boolean addColorTransformController;
        synchronized (this.mLock) {
            addColorTransformController = getSaturationControllerLocked(packageName, userId).addColorTransformController(controller);
        }
        return addColorTransformController;
    }

    public boolean setSaturationLevel(String packageName, int userId, int saturationLevel) {
        boolean saturationLevel2;
        synchronized (this.mLock) {
            saturationLevel2 = getSaturationControllerLocked(packageName, userId).setSaturationLevel(saturationLevel);
        }
        return saturationLevel2;
    }

    public void dump(PrintWriter pw) {
        synchronized (this.mLock) {
            pw.println("App Saturation: ");
            if (this.mAppsMap.size() == 0) {
                pw.println("    No packages");
                return;
            }
            List<String> packageNames = new ArrayList<>(this.mAppsMap.keySet());
            Collections.sort(packageNames);
            for (String packageName : packageNames) {
                pw.println("    " + packageName + ":");
                SparseArray<SaturationController> appUserIdMap = this.mAppsMap.get(packageName);
                for (int i = 0; i < appUserIdMap.size(); i++) {
                    pw.println("        " + appUserIdMap.keyAt(i) + ":");
                    appUserIdMap.valueAt(i).dump(pw);
                }
            }
        }
    }

    private SaturationController getSaturationControllerLocked(String packageName, int userId) {
        return getOrCreateSaturationControllerLocked(getOrCreateUserIdMapLocked(packageName), userId);
    }

    private SparseArray<SaturationController> getOrCreateUserIdMapLocked(String packageName) {
        if (this.mAppsMap.get(packageName) != null) {
            return this.mAppsMap.get(packageName);
        }
        SparseArray<SaturationController> appUserIdMap = new SparseArray<>();
        this.mAppsMap.put(packageName, appUserIdMap);
        return appUserIdMap;
    }

    private SaturationController getOrCreateSaturationControllerLocked(SparseArray<SaturationController> appUserIdMap, int userId) {
        if (appUserIdMap.get(userId) != null) {
            return appUserIdMap.get(userId);
        }
        SaturationController saturationController = new SaturationController();
        appUserIdMap.put(userId, saturationController);
        return saturationController;
    }

    @VisibleForTesting
    static void computeGrayscaleTransformMatrix(float saturation, float[] matrix) {
        float desaturation = 1.0f - saturation;
        float[] luminance = {0.231f * desaturation, 0.715f * desaturation, 0.072f * desaturation};
        matrix[0] = luminance[0] + saturation;
        matrix[1] = luminance[0];
        matrix[2] = luminance[0];
        matrix[3] = luminance[1];
        matrix[4] = luminance[1] + saturation;
        matrix[5] = luminance[1];
        matrix[6] = luminance[2];
        matrix[7] = luminance[2];
        matrix[8] = luminance[2] + saturation;
    }

    /* access modifiers changed from: private */
    public static class SaturationController {
        private final List<WeakReference<ColorDisplayService.ColorTransformController>> mControllerRefs;
        private int mSaturationLevel;
        private float[] mTransformMatrix;

        private SaturationController() {
            this.mControllerRefs = new ArrayList();
            this.mSaturationLevel = 100;
            this.mTransformMatrix = new float[9];
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private boolean setSaturationLevel(int saturationLevel) {
            this.mSaturationLevel = saturationLevel;
            if (!this.mControllerRefs.isEmpty()) {
                return updateState();
            }
            return false;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private boolean addColorTransformController(WeakReference<ColorDisplayService.ColorTransformController> controller) {
            this.mControllerRefs.add(controller);
            if (this.mSaturationLevel != 100) {
                return updateState();
            }
            clearExpiredReferences();
            return false;
        }

        private boolean updateState() {
            AppSaturationController.computeGrayscaleTransformMatrix(((float) this.mSaturationLevel) / 100.0f, this.mTransformMatrix);
            boolean updated = false;
            Iterator<WeakReference<ColorDisplayService.ColorTransformController>> iterator = this.mControllerRefs.iterator();
            while (iterator.hasNext()) {
                ColorDisplayService.ColorTransformController controller = iterator.next().get();
                if (controller != null) {
                    controller.applyAppSaturation(this.mTransformMatrix, AppSaturationController.TRANSLATION_VECTOR);
                    updated = true;
                } else {
                    iterator.remove();
                }
            }
            return updated;
        }

        private void clearExpiredReferences() {
            Iterator<WeakReference<ColorDisplayService.ColorTransformController>> iterator = this.mControllerRefs.iterator();
            while (iterator.hasNext()) {
                if (iterator.next().get() == null) {
                    iterator.remove();
                }
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void dump(PrintWriter pw) {
            pw.println("            mSaturationLevel: " + this.mSaturationLevel);
            pw.println("            mControllerRefs count: " + this.mControllerRefs.size());
        }
    }
}
