package com.huawei.rio;

import android.content.Context;
import android.os.RemoteException;
import android.pc.IHwPCManager;
import android.util.HwPCUtils;
import android.util.Log;
import android.view.Display;
import android.view.IHwRio;
import android.view.View;
import com.huawei.featurelayer.HwFeatureLoader;
import com.huawei.featurelayer.featureframework.IFeature;
import com.huawei.featurelayer.featureframework.IFeatureFramework;
import com.huawei.systemfeature.rio.IHwRioFeature;

public class HwRioImpl implements IHwRio {
    private static final String TAG = "HwRioImpl";
    private IHwRioFeature mHwRioFeature;

    private HwRioImpl() {
    }

    public static synchronized HwRioImpl getDefault() {
        HwRioImpl hwRioImpl;
        synchronized (HwRioImpl.class) {
            hwRioImpl = new HwRioImpl();
        }
        return hwRioImpl;
    }

    private static boolean isRioEnable(int displayId, String packageName) {
        IHwPCManager hwPCManager = HwPCUtils.getHwPCManager();
        if (hwPCManager == null) {
            return false;
        }
        try {
            return hwPCManager.isRioEnable(displayId, packageName);
        } catch (RemoteException e) {
            Log.e(TAG, "call isRioEnable error");
            return false;
        }
    }

    private void initFeature() {
        IFeatureFramework iFeatureFwk;
        if (this.mHwRioFeature == null && (iFeatureFwk = HwFeatureLoader.SystemFeature.getFeatureFramework()) != null) {
            IFeature feature = iFeatureFwk.loadFeature(IHwRioFeature.RIO_PACKAGE, IHwRioFeature.RIO_CLASS);
            if (feature instanceof IHwRioFeature) {
                this.mHwRioFeature = (IHwRioFeature) feature;
            }
        }
    }

    public void attachRio(Context context, View view, CharSequence charSequence, Display display) {
        if (isRioEnable(display.getDisplayId(), context.getPackageName())) {
            initFeature();
            IHwRioFeature iHwRioFeature = this.mHwRioFeature;
            if (iHwRioFeature != null) {
                iHwRioFeature.attachRio(context, view, charSequence, display);
            }
        }
    }

    public void detachRio() {
        IHwRioFeature iHwRioFeature = this.mHwRioFeature;
        if (iHwRioFeature != null) {
            iHwRioFeature.detachRio();
        }
    }

    public void hookAttribute() {
        IHwRioFeature iHwRioFeature = this.mHwRioFeature;
        if (iHwRioFeature != null) {
            iHwRioFeature.hookAttribute();
        }
    }
}
