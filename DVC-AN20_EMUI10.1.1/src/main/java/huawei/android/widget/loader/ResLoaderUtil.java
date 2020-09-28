package huawei.android.widget.loader;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ResLoaderUtil {
    public static final String ARRAY = "array";
    public static final String COLOR = "color";
    public static final String DIMEN = "dimen";
    public static final String DRAWABLE = "drawable";
    public static final String ID = "id";
    public static final String LAYOUT = "layout";
    public static final String STAYLEABLE = "styleable";
    public static final String STRING = "string";
    public static final String TAG = "ResLoaderUtil";

    public static ResLoader getInstance() {
        return ResLoader.getInstance();
    }

    public static Resources getResources(Context context) {
        if (context != null) {
            return getInstance().getResources(context);
        }
        Log.w(TAG, "getResources: context can not be null!");
        return null;
    }

    public static int getColor(Context context, String colorResId) {
        if (context == null || colorResId == null) {
            Log.w(TAG, "getColor: context or colorResId can not be null! context: " + context + ", colorResId: " + colorResId);
            return 0;
        }
        return getResources(context).getColor(getInstance().getIdentifier(context, COLOR, colorResId));
    }

    public static int getDimensionPixelSize(Context context, String dimenResId) {
        if (context == null || dimenResId == null) {
            Log.w(TAG, "getDimensionPixelSize: context or dimenResId can not be null! context: " + context + ", dimenResId: " + dimenResId);
            return 0;
        }
        return getResources(context).getDimensionPixelSize(getInstance().getIdentifier(context, DIMEN, dimenResId));
    }

    public static View getLayout(Context context, String layoutResId, ViewGroup root, boolean isAttachToRoot) {
        if (context == null || layoutResId == null) {
            Log.w(TAG, "getLayout: context or layoutResId can not be null! context: " + context + ", layoutResId: " + layoutResId);
            return null;
        }
        return LayoutInflater.from(context).inflate(getInstance().getIdentifier(context, LAYOUT, layoutResId), root, isAttachToRoot);
    }

    public static int getLayoutId(Context context, String layoutResId) {
        if (context != null && layoutResId != null) {
            return getInstance().getIdentifier(context, LAYOUT, layoutResId);
        }
        Log.w(TAG, "getLayoutId: context or layoutResId can not be null! context: " + context + ", layoutResId: " + layoutResId);
        return 0;
    }

    public static int getViewId(Context context, String viewResId) {
        if (context != null && viewResId != null) {
            return getInstance().getIdentifier(context, ID, viewResId);
        }
        Log.w(TAG, "getViewId: context or viewResId can not be null! context: " + context + ", viewResId: " + viewResId);
        return 0;
    }

    public static int getDrawableId(Context context, String drawableResId) {
        if (context != null && drawableResId != null) {
            return getInstance().getIdentifier(context, DRAWABLE, drawableResId);
        }
        Log.w(TAG, "getDrawableId: context or drawableResId can not be null! context: " + context + ", drawableResId: " + drawableResId);
        return 0;
    }

    public static int getStyleableId(Context context, String styleableResId) {
        if (context != null && styleableResId != null) {
            return ResLoader.getInstance().getIdentifier(context, STAYLEABLE, styleableResId);
        }
        Log.w(TAG, "getStyleableId: context or styleableResId can not be null! context: " + context + ", styleableResId: " + styleableResId);
        return 0;
    }

    public static String getString(Context context, String stringResId) {
        if (context == null || stringResId == null) {
            Log.w(TAG, "getString: context or stringResId can not be null! context: " + context + ", stringResId: " + stringResId);
            return null;
        }
        return getResources(context).getString(getInstance().getIdentifier(context, STRING, stringResId));
    }

    public static String[] getStringArray(Context context, String stringArrayResId) {
        if (context == null || stringArrayResId == null) {
            Log.w(TAG, "getStringArray: context or stringArrayResId can not be null! context: " + context + ", stringArrayResId: " + stringArrayResId);
            return null;
        }
        return getResources(context).getStringArray(getInstance().getIdentifier(context, ARRAY, stringArrayResId));
    }

    public static int[] getIntArray(Context context, String intArrayResId) {
        if (context == null || intArrayResId == null) {
            Log.w(TAG, "getIntArray: context or intArrayResId can not be null! context: " + context + ", intArrayResId: " + intArrayResId);
            return null;
        }
        return getResources(context).getIntArray(getInstance().getIdentifier(context, ARRAY, intArrayResId));
    }
}
