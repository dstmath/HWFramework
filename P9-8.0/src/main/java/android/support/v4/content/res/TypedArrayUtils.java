package android.support.v4.content.res;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.annotation.AnyRes;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import android.support.annotation.StyleableRes;
import android.util.AttributeSet;
import android.util.TypedValue;
import org.xmlpull.v1.XmlPullParser;

@RestrictTo({Scope.LIBRARY_GROUP})
public class TypedArrayUtils {
    private static final String NAMESPACE = "http://schemas.android.com/apk/res/android";

    public static boolean hasAttribute(@NonNull XmlPullParser parser, @NonNull String attrName) {
        return parser.getAttributeValue(NAMESPACE, attrName) != null;
    }

    public static float getNamedFloat(@NonNull TypedArray a, @NonNull XmlPullParser parser, @NonNull String attrName, @StyleableRes int resId, float defaultValue) {
        if (hasAttribute(parser, attrName)) {
            return a.getFloat(resId, defaultValue);
        }
        return defaultValue;
    }

    public static boolean getNamedBoolean(@NonNull TypedArray a, @NonNull XmlPullParser parser, String attrName, @StyleableRes int resId, boolean defaultValue) {
        if (hasAttribute(parser, attrName)) {
            return a.getBoolean(resId, defaultValue);
        }
        return defaultValue;
    }

    public static int getNamedInt(@NonNull TypedArray a, @NonNull XmlPullParser parser, String attrName, @StyleableRes int resId, int defaultValue) {
        if (hasAttribute(parser, attrName)) {
            return a.getInt(resId, defaultValue);
        }
        return defaultValue;
    }

    @ColorInt
    public static int getNamedColor(@NonNull TypedArray a, @NonNull XmlPullParser parser, String attrName, @StyleableRes int resId, @ColorInt int defaultValue) {
        if (hasAttribute(parser, attrName)) {
            return a.getColor(resId, defaultValue);
        }
        return defaultValue;
    }

    @AnyRes
    public static int getNamedResourceId(@NonNull TypedArray a, @NonNull XmlPullParser parser, String attrName, @StyleableRes int resId, @AnyRes int defaultValue) {
        if (hasAttribute(parser, attrName)) {
            return a.getResourceId(resId, defaultValue);
        }
        return defaultValue;
    }

    public static String getNamedString(@NonNull TypedArray a, @NonNull XmlPullParser parser, String attrName, @StyleableRes int resId) {
        if (hasAttribute(parser, attrName)) {
            return a.getString(resId);
        }
        return null;
    }

    public static TypedValue peekNamedValue(TypedArray a, XmlPullParser parser, String attrName, int resId) {
        if (hasAttribute(parser, attrName)) {
            return a.peekValue(resId);
        }
        return null;
    }

    public static TypedArray obtainAttributes(Resources res, Theme theme, AttributeSet set, int[] attrs) {
        if (theme == null) {
            return res.obtainAttributes(set, attrs);
        }
        return theme.obtainStyledAttributes(set, attrs, 0, 0);
    }

    public static boolean getBoolean(TypedArray a, @StyleableRes int index, @StyleableRes int fallbackIndex, boolean defaultValue) {
        return a.getBoolean(index, a.getBoolean(fallbackIndex, defaultValue));
    }

    public static Drawable getDrawable(TypedArray a, @StyleableRes int index, @StyleableRes int fallbackIndex) {
        Drawable val = a.getDrawable(index);
        if (val == null) {
            return a.getDrawable(fallbackIndex);
        }
        return val;
    }

    public static int getInt(TypedArray a, @StyleableRes int index, @StyleableRes int fallbackIndex, int defaultValue) {
        return a.getInt(index, a.getInt(fallbackIndex, defaultValue));
    }

    @AnyRes
    public static int getResourceId(TypedArray a, @StyleableRes int index, @StyleableRes int fallbackIndex, @AnyRes int defaultValue) {
        return a.getResourceId(index, a.getResourceId(fallbackIndex, defaultValue));
    }

    public static String getString(TypedArray a, @StyleableRes int index, @StyleableRes int fallbackIndex) {
        String val = a.getString(index);
        if (val == null) {
            return a.getString(fallbackIndex);
        }
        return val;
    }

    public static CharSequence getText(TypedArray a, @StyleableRes int index, @StyleableRes int fallbackIndex) {
        CharSequence val = a.getText(index);
        if (val == null) {
            return a.getText(fallbackIndex);
        }
        return val;
    }

    public static CharSequence[] getTextArray(TypedArray a, @StyleableRes int index, @StyleableRes int fallbackIndex) {
        CharSequence[] val = a.getTextArray(index);
        if (val == null) {
            return a.getTextArray(fallbackIndex);
        }
        return val;
    }

    public static int getAttr(Context context, int attr, int fallbackAttr) {
        TypedValue value = new TypedValue();
        context.getTheme().resolveAttribute(attr, value, true);
        if (value.resourceId != 0) {
            return attr;
        }
        return fallbackAttr;
    }
}
