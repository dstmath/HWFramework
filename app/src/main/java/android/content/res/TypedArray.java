package android.content.res;

import android.content.pm.ActivityInfo;
import android.content.res.Resources.Theme;
import android.graphics.drawable.Drawable;
import android.hwtheme.HwThemeManager;
import android.os.StrictMode;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import com.android.internal.util.XmlUtils;
import java.util.Arrays;

public class TypedArray {
    private AssetManager mAssets;
    int[] mData;
    int[] mIndices;
    int mLength;
    private final DisplayMetrics mMetrics;
    private boolean mRecycled;
    private final Resources mResources;
    Theme mTheme;
    TypedValue mValue;
    Parser mXml;

    public java.lang.String getNonConfigurationString(int r1, int r2) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.content.res.TypedArray.getNonConfigurationString(int, int):java.lang.String
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:568)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:99)
	... 7 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.content.res.TypedArray.getNonConfigurationString(int, int):java.lang.String");
    }

    static TypedArray obtain(Resources res, int len) {
        TypedArray attrs = (TypedArray) res.mTypedArrayPool.acquire();
        if (attrs == null) {
            return new TypedArray(res, new int[(len * 6)], new int[(len + 1)], len);
        }
        attrs.mLength = len;
        attrs.mRecycled = false;
        attrs.mAssets = res.getAssets();
        int fullLen = len * 6;
        if (attrs.mData.length >= fullLen) {
            res.clearTypedArray(attrs, len);
            return attrs;
        }
        attrs.mData = new int[fullLen];
        attrs.mIndices = new int[(len + 1)];
        return attrs;
    }

    public int length() {
        if (!this.mRecycled) {
            return this.mLength;
        }
        throw new RuntimeException("Cannot make calls to a recycled instance!");
    }

    public int getIndexCount() {
        if (!this.mRecycled) {
            return this.mIndices[0];
        }
        throw new RuntimeException("Cannot make calls to a recycled instance!");
    }

    public int getIndex(int at) {
        if (!this.mRecycled) {
            return this.mIndices[at + 1];
        }
        throw new RuntimeException("Cannot make calls to a recycled instance!");
    }

    public Resources getResources() {
        if (!this.mRecycled) {
            return this.mResources;
        }
        throw new RuntimeException("Cannot make calls to a recycled instance!");
    }

    public CharSequence getText(int index) {
        if (this.mRecycled) {
            throw new RuntimeException("Cannot make calls to a recycled instance!");
        }
        index *= 6;
        int type = this.mData[index + 0];
        if (type == 0) {
            return null;
        }
        if (type != 3) {
            TypedValue v = this.mValue;
            if (getValueAt(index, v)) {
                return v.coerceToString();
            }
            throw new RuntimeException("getText of bad type: 0x" + Integer.toHexString(type));
        } else if (this.mResources == null || !this.mResources.isSRLocale()) {
            return loadStringValueAt(index);
        } else {
            return this.mResources.serbianSyrillic2Latin(loadStringValueAt(index));
        }
    }

    public String getString(int index) {
        String str = null;
        if (this.mRecycled) {
            throw new RuntimeException("Cannot make calls to a recycled instance!");
        }
        index *= 6;
        int type = this.mData[index + 0];
        if (type == 0) {
            return null;
        }
        if (type != 3) {
            TypedValue v = this.mValue;
            if (getValueAt(index, v)) {
                CharSequence cs = v.coerceToString();
                if (cs != null) {
                    str = cs.toString();
                }
                return str;
            }
            throw new RuntimeException("getString of bad type: 0x" + Integer.toHexString(type));
        } else if (this.mResources == null || !this.mResources.isSRLocale()) {
            return loadStringValueAt(index).toString();
        } else {
            return this.mResources.serbianSyrillic2Latin(loadStringValueAt(index).toString());
        }
    }

    public String getNonResourceString(int index) {
        if (this.mRecycled) {
            throw new RuntimeException("Cannot make calls to a recycled instance!");
        }
        index *= 6;
        int[] data = this.mData;
        if (data[index + 0] != 3 || data[index + 2] >= 0) {
            return null;
        }
        return this.mXml.getPooledString(data[index + 1]).toString();
    }

    public boolean getBoolean(int index, boolean defValue) {
        boolean z = false;
        if (this.mRecycled) {
            throw new RuntimeException("Cannot make calls to a recycled instance!");
        }
        index *= 6;
        int[] data = this.mData;
        int type = data[index + 0];
        if (type == 0) {
            return defValue;
        }
        if (type < 16 || type > 31) {
            TypedValue v = this.mValue;
            if (getValueAt(index, v)) {
                StrictMode.noteResourceMismatch(v);
                return XmlUtils.convertValueToBoolean(v.coerceToString(), defValue);
            }
            throw new RuntimeException("getBoolean of bad type: 0x" + Integer.toHexString(type));
        }
        if (data[index + 1] != 0) {
            z = true;
        }
        return z;
    }

    public int getInt(int index, int defValue) {
        if (this.mRecycled) {
            throw new RuntimeException("Cannot make calls to a recycled instance!");
        }
        index *= 6;
        int[] data = this.mData;
        int type = data[index + 0];
        if (type == 0) {
            return defValue;
        }
        if (type >= 16 && type <= 31) {
            return data[index + 1];
        }
        TypedValue v = this.mValue;
        if (getValueAt(index, v)) {
            StrictMode.noteResourceMismatch(v);
            return XmlUtils.convertValueToInt(v.coerceToString(), defValue);
        }
        throw new RuntimeException("getInt of bad type: 0x" + Integer.toHexString(type));
    }

    public float getFloat(int index, float defValue) {
        if (this.mRecycled) {
            throw new RuntimeException("Cannot make calls to a recycled instance!");
        }
        index *= 6;
        int[] data = this.mData;
        int type = data[index + 0];
        if (type == 0) {
            return defValue;
        }
        if (type == 4) {
            return Float.intBitsToFloat(data[index + 1]);
        }
        if (type >= 16 && type <= 31) {
            return (float) data[index + 1];
        }
        TypedValue v = this.mValue;
        if (getValueAt(index, v)) {
            CharSequence str = v.coerceToString();
            if (str != null) {
                StrictMode.noteResourceMismatch(v);
                return Float.parseFloat(str.toString());
            }
        }
        throw new RuntimeException("getFloat of bad type: 0x" + Integer.toHexString(type));
    }

    public int getColor(int index, int defValue) {
        if (this.mRecycled) {
            throw new RuntimeException("Cannot make calls to a recycled instance!");
        }
        int attrIndex = index;
        if (index >= length()) {
            Log.w("Resources", "TypedArray.getColor() index=" + index + " out of bounds[" + length() + "] and return defValue=" + defValue);
            return defValue;
        }
        index *= 6;
        int[] data = this.mData;
        int type = data[index + 0];
        if (type == 0) {
            return defValue;
        }
        if (type >= 16 && type <= 31) {
            return HwThemeManager.getThemeColor(data, index, this.mValue, this.mResources, getValueAt(index, this.mValue));
        }
        TypedValue value;
        if (type == 3) {
            value = this.mValue;
            if (getValueAt(index, value)) {
                return this.mResources.loadColorStateList(value, value.resourceId, this.mTheme).getDefaultColor();
            }
            return defValue;
        } else if (type == 2) {
            value = this.mValue;
            getValueAt(index, value);
            throw new UnsupportedOperationException("Failed to resolve attribute at index " + attrIndex + ": " + value);
        } else {
            throw new UnsupportedOperationException("Can't convert value at index " + attrIndex + " to color: type=0x" + Integer.toHexString(type));
        }
    }

    public ComplexColor getComplexColor(int index) {
        if (this.mRecycled) {
            throw new RuntimeException("Cannot make calls to a recycled instance!");
        }
        TypedValue value = this.mValue;
        if (!getValueAt(index * 6, value)) {
            return null;
        }
        if (value.type == 2) {
            throw new UnsupportedOperationException("Failed to resolve attribute at index " + index + ": " + value);
        } else if (value.type < 28 || value.type > 31 || value.resourceId <= 0) {
            return this.mResources.loadComplexColor(value, value.resourceId, this.mTheme);
        } else {
            return ColorStateList.valueOf(this.mResources.getColor(value.resourceId));
        }
    }

    public ColorStateList getColorStateList(int index) {
        if (this.mRecycled) {
            throw new RuntimeException("Cannot make calls to a recycled instance!");
        }
        TypedValue value = this.mValue;
        if (!getValueAt(index * 6, value)) {
            return null;
        }
        if (value.type != 2) {
            return this.mResources.loadColorStateList(value, value.resourceId, this.mTheme);
        }
        throw new UnsupportedOperationException("Failed to resolve attribute at index " + index + ": " + value);
    }

    public int getInteger(int index, int defValue) {
        if (this.mRecycled) {
            throw new RuntimeException("Cannot make calls to a recycled instance!");
        }
        int attrIndex = index;
        if (index >= length()) {
            Log.w("Resources", "TypedArray.getInteger() index=" + index + " out of bounds[" + length() + "] and return defValue=" + defValue);
            return defValue;
        }
        index *= 6;
        int[] data = this.mData;
        int type = data[index + 0];
        if (type == 0) {
            return defValue;
        }
        if (type >= 16 && type <= 31) {
            return data[index + 1];
        }
        if (type == 2) {
            TypedValue value = this.mValue;
            getValueAt(index, value);
            throw new UnsupportedOperationException("Failed to resolve attribute at index " + attrIndex + ": " + value);
        }
        throw new UnsupportedOperationException("Can't convert value at index " + attrIndex + " to integer: type=0x" + Integer.toHexString(type));
    }

    public float getDimension(int index, float defValue) {
        if (this.mRecycled) {
            throw new RuntimeException("Cannot make calls to a recycled instance!");
        }
        int attrIndex = index;
        index *= 6;
        int[] data = this.mData;
        int type = data[index + 0];
        if (type == 0) {
            return defValue;
        }
        if (type == 5) {
            return TypedValue.complexToDimension(data[index + 1], this.mMetrics);
        }
        if (type == 2) {
            TypedValue value = this.mValue;
            getValueAt(index, value);
            throw new UnsupportedOperationException("Failed to resolve attribute at index " + attrIndex + ": " + value);
        }
        throw new UnsupportedOperationException("Can't convert value at index " + attrIndex + " to dimension: type=0x" + Integer.toHexString(type));
    }

    public int getDimensionPixelOffset(int index, int defValue) {
        if (this.mRecycled) {
            throw new RuntimeException("Cannot make calls to a recycled instance!");
        }
        int attrIndex = index;
        index *= 6;
        int[] data = this.mData;
        int type = data[index + 0];
        if (type == 0) {
            return defValue;
        }
        if (type == 5) {
            return TypedValue.complexToDimensionPixelOffset(data[index + 1], this.mMetrics);
        }
        if (type == 2) {
            TypedValue value = this.mValue;
            getValueAt(index, value);
            throw new UnsupportedOperationException("Failed to resolve attribute at index " + attrIndex + ": " + value);
        }
        throw new UnsupportedOperationException("Can't convert value at index " + attrIndex + " to dimension: type=0x" + Integer.toHexString(type));
    }

    public int getDimensionPixelSize(int index, int defValue) {
        if (this.mRecycled) {
            throw new RuntimeException("Cannot make calls to a recycled instance!");
        }
        int attrIndex = index;
        if (index >= length()) {
            Log.w("Resources", "TypedArray.getDimensionPixelSize() index=" + index + " out of bounds[" + length() + "] and return defValue=" + defValue);
            return defValue;
        }
        index *= 6;
        int[] data = this.mData;
        int type = data[index + 0];
        if (type == 0) {
            return defValue;
        }
        if (type == 5) {
            return TypedValue.complexToDimensionPixelSize(data[index + 1], this.mMetrics);
        }
        if (type == 2) {
            TypedValue value = this.mValue;
            getValueAt(index, value);
            throw new UnsupportedOperationException("Failed to resolve attribute at index " + attrIndex + ": " + value);
        }
        throw new UnsupportedOperationException("Can't convert value at index " + attrIndex + " to dimension: type=0x" + Integer.toHexString(type));
    }

    public int getLayoutDimension(int index, String name) {
        if (this.mRecycled) {
            throw new RuntimeException("Cannot make calls to a recycled instance!");
        }
        int attrIndex = index;
        index *= 6;
        int[] data = this.mData;
        int type = data[index + 0];
        if (type >= 16 && type <= 31) {
            return data[index + 1];
        }
        if (type == 5) {
            return TypedValue.complexToDimensionPixelSize(data[index + 1], this.mMetrics);
        }
        if (type == 2) {
            TypedValue value = this.mValue;
            getValueAt(index, value);
            throw new UnsupportedOperationException("Failed to resolve attribute at index " + attrIndex + ": " + value);
        }
        throw new UnsupportedOperationException(getPositionDescription() + ": You must supply a " + name + " attribute.");
    }

    public int getLayoutDimension(int index, int defValue) {
        if (this.mRecycled) {
            throw new RuntimeException("Cannot make calls to a recycled instance!");
        }
        index *= 6;
        int[] data = this.mData;
        int type = data[index + 0];
        if (type >= 16 && type <= 31) {
            return data[index + 1];
        }
        if (type == 5) {
            return TypedValue.complexToDimensionPixelSize(data[index + 1], this.mMetrics);
        }
        return defValue;
    }

    public float getFraction(int index, int base, int pbase, float defValue) {
        if (this.mRecycled) {
            throw new RuntimeException("Cannot make calls to a recycled instance!");
        }
        int attrIndex = index;
        if (index >= length()) {
            Log.w("Resources", "TypedArray.getFraction() index=" + index + " out of bounds[" + length() + "] and return defValue=" + defValue);
            return defValue;
        }
        index *= 6;
        int[] data = this.mData;
        int type = data[index + 0];
        if (type == 0) {
            return defValue;
        }
        if (type == 6) {
            return TypedValue.complexToFraction(data[index + 1], (float) base, (float) pbase);
        }
        if (type == 2) {
            TypedValue value = this.mValue;
            getValueAt(index, value);
            throw new UnsupportedOperationException("Failed to resolve attribute at index " + attrIndex + ": " + value);
        }
        throw new UnsupportedOperationException("Can't convert value at index " + attrIndex + " to fraction: type=0x" + Integer.toHexString(type));
    }

    public int getResourceId(int index, int defValue) {
        if (this.mRecycled) {
            throw new RuntimeException("Cannot make calls to a recycled instance!");
        }
        index *= 6;
        int[] data = this.mData;
        if (data[index + 0] != 0) {
            int resid = data[index + 3];
            if (resid != 0) {
                return resid;
            }
        }
        return defValue;
    }

    public int getThemeAttributeId(int index, int defValue) {
        if (this.mRecycled) {
            throw new RuntimeException("Cannot make calls to a recycled instance!");
        }
        index *= 6;
        int[] data = this.mData;
        if (data[index + 0] == 2) {
            return data[index + 1];
        }
        return defValue;
    }

    public Drawable getDrawable(int index) {
        if (this.mRecycled) {
            throw new RuntimeException("Cannot make calls to a recycled instance!");
        }
        TypedValue value = this.mValue;
        if (!getValueAt(index * 6, value)) {
            return null;
        }
        if (value.type != 2) {
            return this.mResources.loadDrawable(value, value.resourceId, this.mTheme);
        }
        throw new UnsupportedOperationException("Failed to resolve attribute at index " + index + ": " + value);
    }

    public CharSequence[] getTextArray(int index) {
        if (this.mRecycled) {
            throw new RuntimeException("Cannot make calls to a recycled instance!");
        }
        TypedValue value = this.mValue;
        if (getValueAt(index * 6, value)) {
            return this.mResources.getTextArray(value.resourceId);
        }
        return null;
    }

    public boolean getValue(int index, TypedValue outValue) {
        if (!this.mRecycled) {
            return getValueAt(index * 6, outValue);
        }
        throw new RuntimeException("Cannot make calls to a recycled instance!");
    }

    public int getType(int index) {
        if (this.mRecycled) {
            throw new RuntimeException("Cannot make calls to a recycled instance!");
        }
        return this.mData[(index * 6) + 0];
    }

    public boolean hasValue(int index) {
        if (this.mRecycled) {
            throw new RuntimeException("Cannot make calls to a recycled instance!");
        }
        if (this.mData[(index * 6) + 0] != 0) {
            return true;
        }
        return false;
    }

    public boolean hasValueOrEmpty(int index) {
        if (this.mRecycled) {
            throw new RuntimeException("Cannot make calls to a recycled instance!");
        }
        index *= 6;
        int[] data = this.mData;
        if (data[index + 0] != 0 || data[index + 1] == 1) {
            return true;
        }
        return false;
    }

    public TypedValue peekValue(int index) {
        if (this.mRecycled) {
            throw new RuntimeException("Cannot make calls to a recycled instance!");
        }
        TypedValue value = this.mValue;
        if (getValueAt(index * 6, value)) {
            return value;
        }
        return null;
    }

    public String getPositionDescription() {
        if (!this.mRecycled) {
            return this.mXml != null ? this.mXml.getPositionDescription() : "<internal>";
        } else {
            throw new RuntimeException("Cannot make calls to a recycled instance!");
        }
    }

    public void recycle() {
        if (this.mRecycled) {
            throw new RuntimeException(toString() + " recycled twice!");
        }
        this.mRecycled = true;
        this.mXml = null;
        this.mTheme = null;
        this.mAssets = null;
        this.mResources.mTypedArrayPool.release(this);
    }

    public int[] extractThemeAttrs() {
        return extractThemeAttrs(null);
    }

    public int[] extractThemeAttrs(int[] scrap) {
        if (this.mRecycled) {
            throw new RuntimeException("Cannot make calls to a recycled instance!");
        }
        int[] attrs = null;
        int[] data = this.mData;
        int N = length();
        for (int i = 0; i < N; i++) {
            int index = i * 6;
            if (data[index + 0] == 2) {
                data[index + 0] = 0;
                int attr = data[index + 1];
                if (attr != 0) {
                    if (attrs == null) {
                        if (scrap == null || scrap.length != N) {
                            attrs = new int[N];
                        } else {
                            attrs = scrap;
                            Arrays.fill(scrap, 0);
                        }
                    }
                    attrs[i] = attr;
                }
            }
        }
        return attrs;
    }

    public int getChangingConfigurations() {
        if (this.mRecycled) {
            throw new RuntimeException("Cannot make calls to a recycled instance!");
        }
        int changingConfig = 0;
        int[] data = this.mData;
        int N = length();
        for (int i = 0; i < N; i++) {
            int index = i * 6;
            if (data[index + 0] != 0) {
                changingConfig |= ActivityInfo.activityInfoConfigNativeToJava(data[index + 4]);
            }
        }
        return changingConfig;
    }

    private boolean getValueAt(int index, TypedValue outValue) {
        int[] data = this.mData;
        int type = data[index + 0];
        if (type == 0) {
            return false;
        }
        outValue.type = type;
        outValue.data = data[index + 1];
        outValue.assetCookie = data[index + 2];
        outValue.resourceId = data[index + 3];
        outValue.changingConfigurations = ActivityInfo.activityInfoConfigNativeToJava(data[index + 4]);
        outValue.density = data[index + 5];
        outValue.string = type == 3 ? loadStringValueAt(index) : null;
        return true;
    }

    private CharSequence loadStringValueAt(int index) {
        int[] data = this.mData;
        int cookie = data[index + 2];
        AssetManager assetManager = this.mAssets;
        if (AssetManager.hasRes()) {
            CharSequence result = this.mAssets.getResourceText(data[index + 3]);
            if (result != null) {
                return result;
            }
        }
        if (cookie >= 0) {
            return this.mAssets.getPooledStringForCookie(cookie, data[index + 1]);
        }
        if (this.mXml != null) {
            return this.mXml.getPooledString(data[index + 1]);
        }
        return null;
    }

    TypedArray(Resources resources, int[] data, int[] indices, int len) {
        this.mValue = new TypedValue();
        this.mResources = resources;
        this.mMetrics = this.mResources.getDisplayMetrics();
        this.mAssets = this.mResources.getAssets();
        this.mData = data;
        this.mIndices = indices;
        this.mLength = len;
    }

    public String toString() {
        return Arrays.toString(this.mData);
    }
}
