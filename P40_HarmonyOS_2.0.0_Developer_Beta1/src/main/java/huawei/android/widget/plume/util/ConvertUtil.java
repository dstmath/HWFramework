package huawei.android.widget.plume.util;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.util.TypedValue;
import com.huawei.internal.widget.ConstantValues;
import java.util.Locale;

public class ConvertUtil {
    private static final int CONVERTER_ONE_BIT = 1;
    private static final int CONVERTER_THREE_BITS = 3;
    private static final int CONVERTER_TWO_BITS = 2;
    private static final int DEFAULT_PIXEL = 1;
    private static final float FLOAT_DEFAULT = 0.0f;
    private static final float FLOAT_DIVIDER = 100.0f;
    private static final String RESOURCE_ATTR = "?";
    private static final String RESOURCE_ID = "@";
    private static final int SPREAD_TYPE_DEFAULT = 0;
    private static final int SPREAD_TYPE_INSIDE = 1;
    private static final String TAG = ConvertUtil.class.getSimpleName();
    private static final int WRAP_DIRECTION_OBVERSE = 0;
    private static final int WRAP_DIRECTION_REVERSE = 1;

    private ConvertUtil() {
    }

    public static int convertConfigValue(Context context, String value, float defValue) {
        return roundFloatToInt(convertConfigValueInner(context, value, defValue));
    }

    private static float convertConfigValueInner(Context context, String value, float defValue) {
        if (value == null) {
            return defValue;
        }
        if (value.startsWith(RESOURCE_ID)) {
            return ResourceUtil.parseDimenResource(context, value, defValue);
        }
        if (value.startsWith(RESOURCE_ATTR)) {
            return ResourceUtil.parseDimenAttr(context, value, defValue);
        }
        if (value.endsWith("dp")) {
            return dpToPx(getFloatValue(value.substring(0, value.length() - 2), defValue));
        }
        if (value.endsWith("dip")) {
            return dpToPx(getFloatValue(value.substring(0, value.length() - 3), defValue));
        }
        if (value.endsWith("sp")) {
            return spToPx(getFloatValue(value.substring(0, value.length() - 2), defValue));
        }
        if (value.endsWith("px")) {
            return getFloatValue(value.substring(0, value.length() - 2), defValue);
        }
        String str = TAG;
        Log.w(str, "Plume: dimension value format error, value: " + value);
        return defValue;
    }

    public static float convertFractionToFloat(String value, float defValue) {
        if (value == null) {
            return defValue;
        }
        if (!value.endsWith("%")) {
            String str = TAG;
            Log.w(str, "Plume: fraction value format error, value: " + value);
            return defValue;
        }
        float floatValue = getFloatValue(value.substring(0, value.length() - 1), defValue);
        return Float.compare(floatValue, defValue) == 0 ? defValue : floatValue / FLOAT_DIVIDER;
    }

    public static int convertInteger(String value, int defValue) {
        if (value == null) {
            return defValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            String str = TAG;
            Log.w(str, "Plume: catch number format exception when parse int, value: " + value);
            return defValue;
        }
    }

    private static float getFloatValue(String value, float defValue) {
        if (value == null) {
            return defValue;
        }
        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException e) {
            String str = TAG;
            Log.w(str, "Plume: catch number format exception when parse float, value: " + value);
            return defValue;
        }
    }

    private static int roundFloatToInt(float value) {
        int intValue = Math.round(value);
        if (intValue != 0 || value == 0.0f) {
            return intValue;
        }
        return 1;
    }

    public static int convertSpreadType(String value) {
        if ("spread_inside".equalsIgnoreCase(value)) {
            return 1;
        }
        return 0;
    }

    public static int convertWrapDirection(String value) {
        if ("reverse".equalsIgnoreCase(value)) {
            return 1;
        }
        return 0;
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    public static int convertWrapGravity(String value) {
        char c;
        if (value == null) {
            return 0;
        }
        String lowerCase = value.toLowerCase(Locale.ENGLISH);
        switch (lowerCase.hashCode()) {
            case -1633016142:
                if (lowerCase.equals("fill_vertical")) {
                    c = 5;
                    break;
                }
                c = 65535;
                break;
            case -1383228885:
                if (lowerCase.equals("bottom")) {
                    c = 1;
                    break;
                }
                c = 65535;
                break;
            case -1364013995:
                if (lowerCase.equals("center")) {
                    c = '\b';
                    break;
                }
                c = 65535;
                break;
            case -831189901:
                if (lowerCase.equals("clip_horizontal")) {
                    c = 11;
                    break;
                }
                c = 65535;
                break;
            case -483365792:
                if (lowerCase.equals("fill_horizontal")) {
                    c = 7;
                    break;
                }
                c = 65535;
                break;
            case -348726240:
                if (lowerCase.equals("center_vertical")) {
                    c = 4;
                    break;
                }
                c = 65535;
                break;
            case -55726203:
                if (lowerCase.equals("clip_vertical")) {
                    c = '\n';
                    break;
                }
                c = 65535;
                break;
            case 100571:
                if (lowerCase.equals("end")) {
                    c = '\r';
                    break;
                }
                c = 65535;
                break;
            case 115029:
                if (lowerCase.equals("top")) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            case 3143043:
                if (lowerCase.equals("fill")) {
                    c = '\t';
                    break;
                }
                c = 65535;
                break;
            case 3317767:
                if (lowerCase.equals(ConstantValues.LEFT_HAND_LAZY_MODE_STR)) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            case 108511772:
                if (lowerCase.equals(ConstantValues.RIGHT_HAND_LAZY_MODE_STR)) {
                    c = 3;
                    break;
                }
                c = 65535;
                break;
            case 109757538:
                if (lowerCase.equals("start")) {
                    c = '\f';
                    break;
                }
                c = 65535;
                break;
            case 1063616078:
                if (lowerCase.equals("center_horizontal")) {
                    c = 6;
                    break;
                }
                c = 65535;
                break;
            default:
                c = 65535;
                break;
        }
        switch (c) {
            case 0:
                return 48;
            case 1:
                return 80;
            case 2:
                return 3;
            case 3:
                return 5;
            case 4:
                return 16;
            case 5:
                return 112;
            case 6:
                return 1;
            case 7:
                return 7;
            case '\b':
                return 17;
            case '\t':
                return 119;
            case '\n':
                return 128;
            case 11:
                return 8;
            case '\f':
                return 8388611;
            case '\r':
                return 8388613;
            default:
                return 0;
        }
    }

    public static float dpToPx(float dp) {
        return TypedValue.applyDimension(1, dp, Resources.getSystem().getDisplayMetrics());
    }

    private static float spToPx(float sp) {
        return TypedValue.applyDimension(2, sp, Resources.getSystem().getDisplayMetrics());
    }
}
