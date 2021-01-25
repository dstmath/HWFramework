package com.msic.qarth;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import java.io.IOException;

public class QarthResources {
    private static final String TAG = QarthResources.class.getSimpleName();
    private Context mContext;
    private Resources mOriResources;
    private QarthContext mQarthContext;
    private String mQarthPkgName;
    private Resources mQarthResources;

    public QarthResources(QarthContext qarthContext) {
        if (qarthContext != null) {
            this.mQarthContext = qarthContext;
            this.mContext = qarthContext.context;
            this.mQarthPkgName = getQarthPackageName(qarthContext.patchFile.getFile().getName());
            Context context = this.mContext;
            if (context != null) {
                this.mOriResources = context.getResources();
                try {
                    AssetManager assetManager = createAssetManager(qarthContext.patchFile.getFile().getCanonicalPath());
                    if (assetManager != null && this.mOriResources != null) {
                        this.mQarthResources = new Resources(assetManager, this.mOriResources.getDisplayMetrics(), this.mOriResources.getConfiguration());
                    }
                } catch (IOException e) {
                    QarthLog.e(TAG, "create qarth resource exception.");
                }
            }
        }
    }

    public Resources getQarthResources() {
        return this.mQarthResources;
    }

    public Resources getOriResources() {
        return this.mOriResources;
    }

    private AssetManager createAssetManager(String patchFilePath) {
        try {
            AssetManager assetManager = (AssetManager) AssetManager.class.newInstance();
            assetManager.addAssetPath(patchFilePath);
            return assetManager;
        } catch (InstantiationException e) {
            QarthLog.e(TAG, "create asset manager InstantiationException.");
            return null;
        } catch (IllegalAccessException e2) {
            QarthLog.e(TAG, "create asset manager IllegalAccessException.");
            return null;
        }
    }

    private String getQarthPackageName(String qarthFileName) {
        int index = qarthFileName.indexOf("_");
        if (index >= 0) {
            return qarthFileName.substring(0, index);
        }
        return null;
    }
}
