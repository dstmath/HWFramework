package com.android.server.display;

import android.graphics.Rect;
import android.os.IBinder;
import android.view.SurfaceControl;

public class SurfaceControlExt {

    public static class TransactionEx {
        SurfaceControl.Transaction mTransaction;

        public TransactionEx() {
            this.mTransaction = new SurfaceControl.Transaction();
        }

        public TransactionEx(SurfaceControl.Transaction transaction) {
            this.mTransaction = transaction;
        }

        public SurfaceControl.Transaction getTransaction() {
            return this.mTransaction;
        }

        public void setDisplayProjection(IBinder displayToken, int orientation, Rect layerStackRect, Rect displayRect) {
            SurfaceControl.Transaction transaction = this.mTransaction;
            if (transaction != null) {
                transaction.setDisplayProjection(displayToken, orientation, layerStackRect, displayRect);
            }
        }

        public boolean isEmpty() {
            return this.mTransaction == null;
        }
    }

    public static void mergeToGlobalTransaction(TransactionEx transactEx) {
        if (transactEx != null) {
            SurfaceControl.mergeToGlobalTransaction(transactEx.getTransaction());
        }
    }

    public static class PhysicalDisplayInfoEx {
        SurfaceControl.PhysicalDisplayInfo mPhysicalDisplayInfo;

        public void setPhysicalDisplayInfo(SurfaceControl.PhysicalDisplayInfo physicalDisplayInfo) {
            this.mPhysicalDisplayInfo = physicalDisplayInfo;
        }

        public SurfaceControl.PhysicalDisplayInfo getPhysicalDisplayInfo() {
            return this.mPhysicalDisplayInfo;
        }

        public int getDisplayInfoWidth() {
            return this.mPhysicalDisplayInfo.width;
        }

        public int getDisplayInfoHeight() {
            return this.mPhysicalDisplayInfo.height;
        }
    }

    public static int getActiveConfig(IBinder displayToken) {
        return SurfaceControl.getActiveConfig(displayToken);
    }

    public static PhysicalDisplayInfoEx[] getDisplayConfigs(IBinder displayToken) {
        SurfaceControl.PhysicalDisplayInfo[] configs = SurfaceControl.getDisplayConfigs(displayToken);
        if (configs == null) {
            return null;
        }
        PhysicalDisplayInfoEx[] configsEx = new PhysicalDisplayInfoEx[configs.length];
        for (int i = 0; i < configs.length; i++) {
            PhysicalDisplayInfoEx displayInfoEx = new PhysicalDisplayInfoEx();
            displayInfoEx.setPhysicalDisplayInfo(configs[i]);
            configsEx[i] = displayInfoEx;
        }
        return configsEx;
    }

    public static void setDisplayStatus(IBinder displayToken, int region, int fold, Rect rectMain, Rect rectSub) {
        SurfaceControl.setDisplayStatus(displayToken, region, fold, rectMain, rectSub);
    }

    public static void setDisplaySize(IBinder displayToken, int width, int height) {
        SurfaceControl.setDisplaySize(displayToken, width, height);
    }

    public static int setRogSize(int width, int height) {
        return SurfaceControl.setRogSize(width, height);
    }
}
