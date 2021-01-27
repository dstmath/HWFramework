package huawei.android.widget.plume;

import android.app.ActivityThread;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import huawei.android.widget.plume.action.PlumeAction;
import huawei.android.widget.plume.action.PlumeActionProvider;
import huawei.android.widget.plume.initializer.AppAsyncInitializer;
import huawei.android.widget.plume.initializer.AppInitializer;
import huawei.android.widget.plume.initializer.LayoutAsyncInitializer;
import huawei.android.widget.plume.initializer.LayoutInitializer;
import huawei.android.widget.plume.initializer.MetaInfoAsyncInitializer;
import huawei.android.widget.plume.initializer.MetaInfoInitializer;
import huawei.android.widget.plume.model.AppData;
import huawei.android.widget.plume.model.LayoutData;
import huawei.android.widget.plume.model.PlumeData;
import huawei.android.widget.plume.model.WidgetData;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class HwPlumeManager {
    private static final int APP_ID = 1000;
    private static final boolean DEBUG = false;
    private static final int FAIL = -1;
    private static final String FILE_SUFFIX = ".json";
    private static final String HW_MAGIC_LAYOUT_NAME = "HwMagicLayout";
    private static final String HW_MAGIC_LAYOUT_PREFIX = "com.huawei.uikit.hwmagiclayout.widget.";
    private static final String LINEAR_LAYOUT_NAME = "LinearLayout";
    private static final int META_ID = 2000;
    private static final String META_INFO_FILE_NAME = "meta_info";
    private static final String MIN_VERSION = "version";
    private static final int NOT_USE_PLUME = -1;
    private static final String PATH_PLUME = "huawei_plume/";
    private static final float PLUME_VERSION = 1.0f;
    private static final int SUCCESS = 1;
    private static final String TAG = HwPlumeManager.class.getSimpleName();
    private static final int USE_PLUME = 1;
    private static HwPlumeManager sInstance = null;
    private static int sIsPlumeUsed = 0;
    private int mAppLoadStatus = 0;
    private Context mContext;
    private Map<Integer, PlumeData> mData = new ConcurrentHashMap();
    private boolean mIsAppLoadStarted = false;
    private boolean mIsLayoutLoadStarted = false;
    private boolean mIsMetaInfoLoadStarted = false;
    private ThreadLocal<Integer> mLayoutIdThreadLocal = new ThreadLocal<>();
    private int mLayoutLoadStatus = 0;
    private int mMetaInfoLoadStatus = 0;
    private Map<Integer, Set<Integer>> mPreData = new ConcurrentHashMap();
    private Float mVersion = null;
    private int mVersionStatus = 0;

    private HwPlumeManager(Context context) {
        this.mContext = context.getApplicationContext();
    }

    public static synchronized HwPlumeManager getInstance(Context context) {
        HwPlumeManager hwPlumeManager;
        synchronized (HwPlumeManager.class) {
            if (sInstance == null) {
                sInstance = new HwPlumeManager(context);
            }
            hwPlumeManager = sInstance;
        }
        return hwPlumeManager;
    }

    public static synchronized boolean isPlumeUsed(Context context) {
        synchronized (HwPlumeManager.class) {
            if (sIsPlumeUsed == 1) {
                return true;
            }
            if (sIsPlumeUsed == -1) {
                return false;
            }
            if (context == null || context.getApplicationContext() == null || ActivityThread.isSystem() || !hasFileInAssets(context, META_INFO_FILE_NAME)) {
                sIsPlumeUsed = -1;
                return false;
            }
            sIsPlumeUsed = 1;
            return true;
        }
    }

    private static boolean hasFileInAssets(Context context, String fileName) {
        if (context == null || fileName == null) {
            return false;
        }
        try {
            AssetManager assets = context.getAssets();
            InputStream is = assets.open(PATH_PLUME + fileName + FILE_SUFFIX);
            if (is != null) {
                is.close();
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public void setAppLoadStatus(int status) {
        this.mAppLoadStatus = status;
    }

    public void setMetaInfoLoadStatus(int status) {
        this.mMetaInfoLoadStatus = status;
    }

    public void setLayoutLoadStatus(int status) {
        this.mLayoutLoadStatus = status;
    }

    public void preInflate(int resource) {
        if (this.mMetaInfoLoadStatus == 0 && !this.mIsMetaInfoLoadStarted) {
            this.mIsMetaInfoLoadStarted = true;
            new MetaInfoAsyncInitializer(this.mContext, this.mData).execute(new Object[0]);
        }
        if (this.mAppLoadStatus == 0 && !this.mIsAppLoadStarted) {
            this.mIsAppLoadStarted = true;
            new AppAsyncInitializer(this.mContext, this.mData).execute(new Object[0]);
        }
        if (this.mLayoutLoadStatus == 0 && !this.mIsLayoutLoadStarted) {
            this.mIsLayoutLoadStarted = true;
            new LayoutAsyncInitializer(this.mContext, this.mData, this.mPreData).execute(new Object[0]);
        }
        if (this.mLayoutLoadStatus == 0) {
            initializeCurrentLayout(resource);
        }
        if (this.mLayoutIdThreadLocal.get() == null) {
            this.mLayoutIdThreadLocal.set(Integer.valueOf(resource));
        }
    }

    public void postInflate(int resource, View view) {
        if (this.mLayoutIdThreadLocal.get() != null && this.mLayoutIdThreadLocal.get().intValue() == resource) {
            this.mLayoutIdThreadLocal.set(null);
        }
        if (resource != 0 && view != null && this.mLayoutLoadStatus != -1 && this.mData.containsKey(Integer.valueOf(resource)) && isVersionValid()) {
            handleLayoutPlumeActions(resource, view);
        }
    }

    private void initializeCurrentLayout(int resource) {
        Context context = this.mContext;
        if (context != null) {
            try {
                String fileName = context.getResources().getResourceEntryName(resource);
                if (hasFileInAssets(this.mContext, fileName)) {
                    Context context2 = this.mContext;
                    Map<Integer, PlumeData> map = this.mData;
                    Map<Integer, Set<Integer>> map2 = this.mPreData;
                    new LayoutInitializer(context2, map, map2, fileName + FILE_SUFFIX).initialize();
                }
            } catch (Resources.NotFoundException e) {
                Log.e(TAG, "Plume: File name not found!");
            }
        }
    }

    private void handleLayoutPlumeActions(int resource, View view) {
        PlumeData plumeData = this.mData.get(Integer.valueOf(resource));
        if (plumeData instanceof LayoutData) {
            handleLayoutActions(view, (LayoutData) plumeData);
        }
    }

    private void handleLayoutActions(View view, LayoutData data) {
        if (!(view == null || data == null)) {
            for (Map.Entry<Integer, WidgetData> entry : data.getWidgetMap().entrySet()) {
                handleWidgetActions(view.findViewById(entry.getKey().intValue()), entry.getValue());
            }
        }
    }

    private void handleWidgetActions(View view, WidgetData data) {
        if (!(view == null || data == null)) {
            for (Map.Entry<String, String> entry : data.getAttributeMap().entrySet()) {
                PlumeAction action = PlumeActionProvider.getAction(this.mContext, view, entry.getKey());
                if (action == null) {
                    String str = TAG;
                    Log.w(str, "Plume: attribute " + entry.getKey() + " is not handled!");
                } else {
                    action.apply(entry.getKey(), entry.getValue());
                }
            }
        }
    }

    public View onCreateView(LayoutInflater layoutInflater, String name, AttributeSet attrs) {
        Integer layoutId;
        if (layoutInflater == null || !LINEAR_LAYOUT_NAME.equals(name) || (layoutId = this.mLayoutIdThreadLocal.get()) == null || !this.mPreData.containsKey(layoutId)) {
            return null;
        }
        Set<Integer> plumeSet = this.mPreData.get(layoutId);
        int id = getWidgetId(attrs);
        if (id == -1 || !plumeSet.contains(Integer.valueOf(id))) {
            return null;
        }
        try {
            return layoutInflater.createView(HW_MAGIC_LAYOUT_NAME, HW_MAGIC_LAYOUT_PREFIX, attrs);
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "Plume: HwMagicLayout ClassNotFoundException");
            return null;
        }
    }

    private int getWidgetId(AttributeSet attrs) {
        TypedArray typedArray = this.mContext.obtainStyledAttributes(attrs, new int[]{16842960});
        int id = typedArray.getResourceId(0, -1);
        typedArray.recycle();
        return id;
    }

    public boolean getDefault(View view, String attrName, boolean defaultValue) {
        if (view == null || attrName == null) {
            return defaultValue;
        }
        if (this.mAppLoadStatus == 0) {
            this.mAppLoadStatus = new AppInitializer(this.mContext, this.mData).initialize();
        }
        if (this.mAppLoadStatus == -1) {
            Log.e(TAG, "Plume: get default skip because loading app_plume.json failed!");
            return defaultValue;
        } else if (!isVersionValid()) {
            Log.e(TAG, "Plume: get default skip because of version error!");
            return defaultValue;
        } else if (!(this.mData.get(1000) instanceof AppData)) {
            return defaultValue;
        } else {
            Map<String, String> attrsMap = ((AppData) this.mData.get(1000)).getAttributeMap();
            if (!attrsMap.containsKey(attrName)) {
                return defaultValue;
            }
            return isAttrEnabled(attrsMap.get(attrName), view, defaultValue);
        }
    }

    private boolean isAttrEnabled(String value, View view, boolean defaultValue) {
        if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
            return Boolean.parseBoolean(value);
        }
        String[] enableWidgets = value.split("\\s*,\\s*");
        String targetName = view.getClass().getSimpleName();
        for (String widget : enableWidgets) {
            if (targetName.equals(widget)) {
                return true;
            }
        }
        return defaultValue;
    }

    private boolean isVersionValid() {
        if (this.mVersionStatus == 0) {
            this.mVersionStatus = getVersionStatus();
        }
        if (this.mVersionStatus == 1) {
            return true;
        }
        Log.e(TAG, "Plume: Version check failed!");
        return false;
    }

    private int getVersionStatus() {
        if (this.mMetaInfoLoadStatus == 0) {
            this.mMetaInfoLoadStatus = new MetaInfoInitializer(this.mContext, this.mData).initialize();
        }
        if (this.mMetaInfoLoadStatus == -1) {
            Log.e(TAG, "Plume: meta info initialize error!");
            return -1;
        }
        try {
            if (this.mVersion == null) {
                PlumeData data = this.mData.get(2000);
                if (data == null) {
                    Log.e(TAG, "Plume: meta data is null!");
                    return -1;
                }
                String versionString = data.getAttributeMap().get(MIN_VERSION);
                if (versionString == null) {
                    Log.e(TAG, "Plume: lack of declared version");
                    return -1;
                }
                this.mVersion = Float.valueOf(Float.parseFloat(versionString));
            }
            Float f = this.mVersion;
            if (f != null && f.floatValue() <= 1.0f) {
                return 1;
            }
            Log.e(TAG, "Plume: version claimed in plume is larger than current version!");
            return -1;
        } catch (NumberFormatException e) {
            Log.e(TAG, "Plume: incorrect version format!");
            return -1;
        }
    }
}
