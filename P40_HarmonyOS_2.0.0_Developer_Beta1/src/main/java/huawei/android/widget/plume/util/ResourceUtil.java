package huawei.android.widget.plume.util;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.Log;

public class ResourceUtil {
    private static final String COLON = ":";
    private static final String RESOURCE_ATTR = "?";
    private static final String RESOURCE_ID = "@";
    private static final String SLASH = "/";
    private static final String TAG = ResourceUtil.class.getSimpleName();

    private ResourceUtil() {
    }

    public static float parseDimenResource(Context context, String value, float defValue) {
        if (context == null) {
            return defValue;
        }
        int resId = getResId(context, value);
        if (resId == 0) {
            String str = TAG;
            Log.w(str, "Plume: parseDimenResource no such resource was found, value: " + value);
            return defValue;
        }
        try {
            return context.getResources().getDimension(resId);
        } catch (Resources.NotFoundException e) {
            String str2 = TAG;
            Log.w(str2, "Plume: parseDimenResource not found, value: " + value);
            return defValue;
        }
    }

    private static int getResId(Context context, String dimen) {
        if (dimen == null) {
            return 0;
        }
        return context.getResources().getIdentifier(dimen.substring(dimen.indexOf(RESOURCE_ID) + 1), null, context.getPackageName());
    }

    public static float parseDimenAttr(Context context, String value, float defValue) {
        if (context == null) {
            return defValue;
        }
        int resId = getAttrId(context, value);
        if (resId == 0) {
            String str = TAG;
            Log.w(str, "Plume: parseDimenAttr no such resource was found, value: " + value);
            return defValue;
        }
        TypedArray typedArray = context.obtainStyledAttributes(new int[]{resId});
        float out = typedArray.getDimension(0, defValue);
        typedArray.recycle();
        return out;
    }

    private static int getAttrId(Context context, String attr) {
        if (attr == null) {
            return 0;
        }
        String type = "attr";
        String packageName = "android";
        String curAttr = attr.substring(attr.indexOf(RESOURCE_ATTR) + 1);
        if (curAttr.contains(COLON)) {
            packageName = curAttr.substring(0, curAttr.indexOf(COLON));
            curAttr = curAttr.substring(curAttr.indexOf(COLON) + 1);
        }
        if (curAttr.contains(SLASH)) {
            type = curAttr.substring(0, curAttr.indexOf(SLASH));
            curAttr = curAttr.substring(curAttr.indexOf(SLASH) + 1);
        }
        return context.getResources().getIdentifier(curAttr, type, packageName);
    }
}
