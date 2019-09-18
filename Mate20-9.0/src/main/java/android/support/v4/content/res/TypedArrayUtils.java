package android.support.v4.content.res;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.annotation.AnyRes;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.support.annotation.StyleableRes;
import android.util.AttributeSet;
import android.util.TypedValue;
import org.xmlpull.v1.XmlPullParser;

@RestrictTo({RestrictTo.Scope.LIBRARY_GROUP})
public class TypedArrayUtils {
    private static final String NAMESPACE = "http://schemas.android.com/apk/res/android";

    public static boolean hasAttribute(@NonNull XmlPullParser parser, @NonNull String attrName) {
        return parser.getAttributeValue(NAMESPACE, attrName) != null;
    }

    public static float getNamedFloat(@NonNull TypedArray a, @NonNull XmlPullParser parser, @NonNull String attrName, @StyleableRes int resId, float defaultValue) {
        if (!hasAttribute(parser, attrName)) {
            return defaultValue;
        }
        return a.getFloat(resId, defaultValue);
    }

    public static boolean getNamedBoolean(@NonNull TypedArray a, @NonNull XmlPullParser parser, @NonNull String attrName, @StyleableRes int resId, boolean defaultValue) {
        if (!hasAttribute(parser, attrName)) {
            return defaultValue;
        }
        return a.getBoolean(resId, defaultValue);
    }

    public static int getNamedInt(@NonNull TypedArray a, @NonNull XmlPullParser parser, @NonNull String attrName, @StyleableRes int resId, int defaultValue) {
        if (!hasAttribute(parser, attrName)) {
            return defaultValue;
        }
        return a.getInt(resId, defaultValue);
    }

    @ColorInt
    public static int getNamedColor(@NonNull TypedArray a, @NonNull XmlPullParser parser, @NonNull String attrName, @StyleableRes int resId, @ColorInt int defaultValue) {
        if (!hasAttribute(parser, attrName)) {
            return defaultValue;
        }
        return a.getColor(resId, defaultValue);
    }

    @AnyRes
    public static int getNamedResourceId(@NonNull TypedArray a, @NonNull XmlPullParser parser, @NonNull String attrName, @StyleableRes int resId, @AnyRes int defaultValue) {
        if (!hasAttribute(parser, attrName)) {
            return defaultValue;
        }
        return a.getResourceId(resId, defaultValue);
    }

    @Nullable
    public static String getNamedString(@NonNull TypedArray a, @NonNull XmlPullParser parser, @NonNull String attrName, @StyleableRes int resId) {
        if (!hasAttribute(parser, attrName)) {
            return null;
        }
        return a.getString(resId);
    }

    @Nullable
    public static TypedValue peekNamedValue(@NonNull TypedArray a, @NonNull XmlPullParser parser, @NonNull String attrName, int resId) {
        if (!hasAttribute(parser, attrName)) {
            return null;
        }
        return a.peekValue(resId);
    }

    @NonNull
    public static TypedArray obtainAttributes(@NonNull Resources res, @Nullable Resources.Theme theme, @NonNull AttributeSet set, @NonNull int[] attrs) {
        if (theme == null) {
            return res.obtainAttributes(set, attrs);
        }
        return theme.obtainStyledAttributes(set, attrs, 0, 0);
    }

    public static boolean getBoolean(@NonNull TypedArray a, @StyleableRes int index, @StyleableRes int fallbackIndex, boolean defaultValue) {
        return a.getBoolean(index, a.getBoolean(fallbackIndex, defaultValue));
    }

    @Nullable
    public static Drawable getDrawable(@NonNull TypedArray a, @StyleableRes int index, @StyleableRes int fallbackIndex) {
        Drawable val = a.getDrawable(index);
        if (val == null) {
            return a.getDrawable(fallbackIndex);
        }
        return val;
    }

    public static int getInt(@NonNull TypedArray a, @StyleableRes int index, @StyleableRes int fallbackIndex, int defaultValue) {
        return a.getInt(index, a.getInt(fallbackIndex, defaultValue));
    }

    @AnyRes
    public static int getResourceId(@NonNull TypedArray a, @StyleableRes int index, @StyleableRes int fallbackIndex, @AnyRes int defaultValue) {
        return a.getResourceId(index, a.getResourceId(fallbackIndex, defaultValue));
    }

    @Nullable
    public static String getString(@NonNull TypedArray a, @StyleableRes int index, @StyleableRes int fallbackIndex) {
        String val = a.getString(index);
        if (val == null) {
            return a.getString(fallbackIndex);
        }
        return val;
    }

    @Nullable
    public static CharSequence getText(@NonNull TypedArray a, @StyleableRes int index, @StyleableRes int fallbackIndex) {
        CharSequence val = a.getText(index);
        if (val == null) {
            return a.getText(fallbackIndex);
        }
        return val;
    }

    @Nullable
    public static CharSequence[] getTextArray(@NonNull TypedArray a, @StyleableRes int index, @StyleableRes int fallbackIndex) {
        CharSequence[] val = a.getTextArray(index);
        if (val == null) {
            return a.getTextArray(fallbackIndex);
        }
        return val;
    }

    public static int getAttr(@NonNull Context context, int attr, int fallbackAttr) {
        TypedValue value = new TypedValue();
        context.getTheme().resolveAttribute(attr, value, true);
        if (value.resourceId != 0) {
            return attr;
        }
        return fallbackAttr;
    }

    private TypedArrayUtils() {
    }
}
