package com.msic.qarth;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import java.lang.reflect.Field;

public class QarthResources {
    private static final String TAG = QarthResources.class.getSimpleName();
    private Context mContext;
    private Field mContextResourceField = reflectResourceField(this.mContext);
    private Resources mOriResources;
    private QarthContext mQarthContext;
    private String mQarthPkgName;
    private Resources mQarthResources;

    public QarthResources(QarthContext qarthContext) {
        this.mQarthContext = qarthContext;
        this.mContext = qarthContext.context;
        this.mQarthPkgName = getQarthPackageName(qarthContext.patchFile.getFile().getName());
        try {
            this.mOriResources = getContextResources(this.mContext);
        } catch (IllegalAccessException e) {
            String str = TAG;
            QarthLog.e(str, "getContextResources exception " + e.getMessage());
        }
        this.mQarthResources = new Resources(createAssetManager(qarthContext.patchFile.getFile().getAbsolutePath()), this.mContext.getResources().getDisplayMetrics(), this.mContext.getResources().getConfiguration());
    }

    public Resources getQarthResources() {
        return this.mQarthResources;
    }

    public Resources getOriResources() {
        return this.mOriResources;
    }

    private AssetManager createAssetManager(String patchFilePath) {
        try {
            AssetManager assetManager = AssetManager.class.newInstance();
            AssetManager.class.getDeclaredMethod("addAssetPath", new Class[]{String.class}).invoke(assetManager, new Object[]{patchFilePath});
            return assetManager;
        } catch (Exception e) {
            String str = TAG;
            QarthLog.e(str, "createAssetManager exception " + e.getMessage());
            return null;
        }
    }

    private Resources getContextResources(Context context) throws IllegalAccessException {
        if (this.mContextResourceField != null) {
            return (Resources) this.mContextResourceField.get(context);
        }
        return null;
    }

    private Field reflectResourceField(Context context) {
        try {
            Field field = context.getClass().getDeclaredField("mResources");
            field.setAccessible(true);
            return field;
        } catch (Exception e) {
            String str = TAG;
            QarthLog.e(str, "reflectResourceField exception " + e.getMessage());
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
