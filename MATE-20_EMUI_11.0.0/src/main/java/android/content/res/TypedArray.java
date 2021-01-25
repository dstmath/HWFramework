package android.content.res;

import android.annotation.UnsupportedAppUsage;
import android.content.pm.ActivityInfo;
import android.content.res.AbsResourcesImpl;
import android.content.res.Resources;
import android.content.res.XmlBlock;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.hwtheme.HwThemeManager;
import android.os.StrictMode;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import com.android.internal.util.XmlUtils;
import dalvik.system.VMRuntime;
import java.util.Arrays;

public class TypedArray {
    static final int STYLE_ASSET_COOKIE = 2;
    static final int STYLE_CHANGING_CONFIGURATIONS = 4;
    static final int STYLE_DATA = 1;
    static final int STYLE_DENSITY = 5;
    static final int STYLE_NUM_ENTRIES = 7;
    static final int STYLE_RESOURCE_ID = 3;
    static final int STYLE_SOURCE_RESOURCE_ID = 6;
    static final int STYLE_TYPE = 0;
    @UnsupportedAppUsage
    private AssetManager mAssets;
    @UnsupportedAppUsage
    int[] mData;
    long mDataAddress;
    @UnsupportedAppUsage
    int[] mIndices;
    long mIndicesAddress;
    @UnsupportedAppUsage
    int mLength;
    @UnsupportedAppUsage
    private DisplayMetrics mMetrics;
    @UnsupportedAppUsage
    private boolean mRecycled;
    @UnsupportedAppUsage
    private final Resources mResources;
    @UnsupportedAppUsage
    Resources.Theme mTheme;
    @UnsupportedAppUsage
    TypedValue mValue = new TypedValue();
    @UnsupportedAppUsage
    XmlBlock.Parser mXml;

    static TypedArray obtain(Resources res, int len) {
        TypedArray attrs = res.mTypedArrayPool.acquire();
        if (attrs == null) {
            attrs = new TypedArray(res);
        }
        attrs.mRecycled = false;
        attrs.mAssets = res.getAssets();
        attrs.mMetrics = res.getDisplayMetrics();
        attrs.resize(len);
        return attrs;
    }

    private void resize(int len) {
        this.mLength = len;
        int dataLen = len * 7;
        int indicesLen = len + 1;
        VMRuntime runtime = VMRuntime.getRuntime();
        if (this.mDataAddress == 0 || this.mData.length < dataLen) {
            this.mData = (int[]) runtime.newNonMovableArray(Integer.TYPE, dataLen);
            this.mDataAddress = runtime.addressOf(this.mData);
            this.mIndices = (int[]) runtime.newNonMovableArray(Integer.TYPE, indicesLen);
            this.mIndicesAddress = runtime.addressOf(this.mIndices);
        }
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
        if (!this.mRecycled) {
            int index2 = index * 7;
            int type = this.mData[index2 + 0];
            if (type == 0) {
                return null;
            }
            if (type == 3) {
                Resources resources = this.mResources;
                if (resources == null || !resources.isSRLocale()) {
                    return loadStringValueAt(index2);
                }
                return this.mResources.serbianSyrillic2Latin(loadStringValueAt(index2));
            }
            TypedValue v = this.mValue;
            if (getValueAt(index2, v)) {
                return v.coerceToString();
            }
            throw new RuntimeException("getText of bad type: 0x" + Integer.toHexString(type));
        }
        throw new RuntimeException("Cannot make calls to a recycled instance!");
    }

    public String getString(int index) {
        if (!this.mRecycled) {
            int index2 = index * 7;
            int type = this.mData[index2 + 0];
            if (type == 0) {
                return null;
            }
            if (type == 3) {
                Resources resources = this.mResources;
                if (resources == null || !resources.isSRLocale()) {
                    return loadStringValueAt(index2).toString();
                }
                return this.mResources.serbianSyrillic2Latin(loadStringValueAt(index2).toString());
            }
            TypedValue v = this.mValue;
            if (getValueAt(index2, v)) {
                CharSequence cs = v.coerceToString();
                if (cs != null) {
                    return cs.toString();
                }
                return null;
            }
            throw new RuntimeException("getString of bad type: 0x" + Integer.toHexString(type));
        }
        throw new RuntimeException("Cannot make calls to a recycled instance!");
    }

    public String getNonResourceString(int index) {
        if (!this.mRecycled) {
            int index2 = index * 7;
            int[] data = this.mData;
            if (data[index2 + 0] != 3 || data[index2 + 2] >= 0) {
                return null;
            }
            return this.mXml.getPooledString(data[index2 + 1]).toString();
        }
        throw new RuntimeException("Cannot make calls to a recycled instance!");
    }

    @UnsupportedAppUsage
    public String getNonConfigurationString(int index, int allowedChangingConfigs) {
        if (!this.mRecycled) {
            int index2 = index * 7;
            int[] data = this.mData;
            int type = data[index2 + 0];
            if (((~allowedChangingConfigs) & ActivityInfo.activityInfoConfigNativeToJava(data[index2 + 4])) != 0 || type == 0) {
                return null;
            }
            if (type == 3) {
                return loadStringValueAt(index2).toString();
            }
            TypedValue v = this.mValue;
            if (getValueAt(index2, v)) {
                CharSequence cs = v.coerceToString();
                if (cs != null) {
                    return cs.toString();
                }
                return null;
            }
            throw new RuntimeException("getNonConfigurationString of bad type: 0x" + Integer.toHexString(type));
        }
        throw new RuntimeException("Cannot make calls to a recycled instance!");
    }

    public boolean getBoolean(int index, boolean defValue) {
        if (!this.mRecycled) {
            int index2 = index * 7;
            int[] data = this.mData;
            int type = data[index2 + 0];
            if (type == 0) {
                return defValue;
            }
            if (type >= 16 && type <= 31) {
                return data[index2 + 1] != 0;
            }
            TypedValue v = this.mValue;
            if (getValueAt(index2, v)) {
                StrictMode.noteResourceMismatch(v);
                return XmlUtils.convertValueToBoolean(v.coerceToString(), defValue);
            }
            throw new RuntimeException("getBoolean of bad type: 0x" + Integer.toHexString(type));
        }
        throw new RuntimeException("Cannot make calls to a recycled instance!");
    }

    public int getInt(int index, int defValue) {
        int resourceId;
        if (!this.mRecycled) {
            int index2 = index * 7;
            int[] data = this.mData;
            int type = data[index2 + 0];
            if (type == 0) {
                return defValue;
            }
            if (type < 16 || type > 31) {
                TypedValue v = this.mValue;
                if (getValueAt(index2, v)) {
                    StrictMode.noteResourceMismatch(v);
                    return XmlUtils.convertValueToInt(v.coerceToString(), defValue);
                }
                throw new RuntimeException("getInt of bad type: 0x" + Integer.toHexString(type));
            }
            if (HwThemeManager.DEBUG_UI_PROGRAM && (resourceId = getResourceId(index2)) > 0) {
                try {
                    return this.mResources.getInteger(resourceId);
                } catch (Resources.NotFoundException e) {
                }
            }
            return data[index2 + 1];
        }
        throw new RuntimeException("Cannot make calls to a recycled instance!");
    }

    public float getFloat(int index, float defValue) {
        CharSequence str;
        int resourceId;
        int resourceId2;
        if (!this.mRecycled) {
            int index2 = index * 7;
            int[] data = this.mData;
            int type = data[index2 + 0];
            if (type == 0) {
                return defValue;
            }
            if (type == 4) {
                if (HwThemeManager.DEBUG_UI_PROGRAM && (resourceId2 = getResourceId(index2)) > 0) {
                    try {
                        return this.mResources.getFloat(resourceId2);
                    } catch (Resources.NotFoundException e) {
                    }
                }
                return Float.intBitsToFloat(data[index2 + 1]);
            } else if (type < 16 || type > 31) {
                TypedValue v = this.mValue;
                if (!getValueAt(index2, v) || (str = v.coerceToString()) == null) {
                    throw new RuntimeException("getFloat of bad type: 0x" + Integer.toHexString(type));
                }
                StrictMode.noteResourceMismatch(v);
                return Float.parseFloat(str.toString());
            } else {
                if (HwThemeManager.DEBUG_UI_PROGRAM && (resourceId = getResourceId(index2)) > 0) {
                    try {
                        return (float) this.mResources.getInteger(resourceId);
                    } catch (Resources.NotFoundException e2) {
                    }
                }
                return (float) data[index2 + 1];
            }
        } else {
            throw new RuntimeException("Cannot make calls to a recycled instance!");
        }
    }

    public int getColor(int index, int defValue) {
        if (this.mRecycled) {
            throw new RuntimeException("Cannot make calls to a recycled instance!");
        } else if (index >= this.mLength) {
            return defValue;
        } else {
            int index2 = index * 7;
            int[] data = this.mData;
            int type = data[index2 + 0];
            if (type == 0) {
                return defValue;
            }
            if (type >= 16 && type <= 31) {
                TypedValue value = this.mValue;
                return HwThemeManager.getThemeColor(data, index2, value, this.mResources, getValueAt(index2, value));
            } else if (type == 3) {
                TypedValue value2 = this.mValue;
                if (getValueAt(index2, value2)) {
                    return this.mResources.loadColorStateList(value2, value2.resourceId, this.mTheme).getDefaultColor();
                }
                return defValue;
            } else if (type == 2) {
                TypedValue value3 = this.mValue;
                getValueAt(index2, value3);
                throw new UnsupportedOperationException("Failed to resolve attribute at index " + index + ": " + value3);
            } else {
                throw new UnsupportedOperationException("Can't convert value at index " + index + " to color: type=0x" + Integer.toHexString(type));
            }
        }
    }

    public ComplexColor getComplexColor(int index) {
        if (!this.mRecycled) {
            TypedValue value = this.mValue;
            if (!getValueAt(index * 7, value)) {
                return null;
            }
            if (value.type == 2) {
                throw new UnsupportedOperationException("Failed to resolve attribute at index " + index + ": " + value);
            } else if (value.type < 28 || value.type > 31 || value.resourceId <= 0) {
                return this.mResources.loadComplexColor(value, value.resourceId, this.mTheme);
            } else {
                return ColorStateList.valueOf(this.mResources.getColor(value.resourceId));
            }
        } else {
            throw new RuntimeException("Cannot make calls to a recycled instance!");
        }
    }

    public ColorStateList getColorStateList(int index) {
        if (!this.mRecycled) {
            TypedValue value = this.mValue;
            if (!getValueAt(index * 7, value)) {
                return null;
            }
            if (value.type != 2) {
                return this.mResources.loadColorStateList(value, value.resourceId, this.mTheme);
            }
            throw new UnsupportedOperationException("Failed to resolve attribute at index " + index + ": " + value);
        }
        throw new RuntimeException("Cannot make calls to a recycled instance!");
    }

    public int getInteger(int index, int defValue) {
        int resourceId;
        if (this.mRecycled) {
            throw new RuntimeException("Cannot make calls to a recycled instance!");
        } else if (index >= this.mLength) {
            return defValue;
        } else {
            int index2 = index * 7;
            int[] data = this.mData;
            int type = data[index2 + 0];
            if (type == 0) {
                return defValue;
            }
            if (type >= 16 && type <= 31) {
                if (HwThemeManager.DEBUG_UI_PROGRAM && (resourceId = getResourceId(index2)) > 0) {
                    try {
                        return this.mResources.getInteger(resourceId);
                    } catch (Resources.NotFoundException e) {
                    }
                }
                return data[index2 + 1];
            } else if (type == 2) {
                TypedValue value = this.mValue;
                getValueAt(index2, value);
                throw new UnsupportedOperationException("Failed to resolve attribute at index " + index + ": " + value);
            } else {
                throw new UnsupportedOperationException("Can't convert value at index " + index + " to integer: type=0x" + Integer.toHexString(type));
            }
        }
    }

    public float getDimension(int index, float defValue) {
        int resourceId;
        if (!this.mRecycled) {
            int index2 = index * 7;
            int[] data = this.mData;
            int type = data[index2 + 0];
            if (type == 0) {
                return defValue;
            }
            if (type == 5) {
                if (HwThemeManager.DEBUG_UI_PROGRAM && (resourceId = getResourceId(index2)) > 0) {
                    try {
                        return this.mResources.getDimension(resourceId);
                    } catch (Resources.NotFoundException e) {
                    }
                }
                return TypedValue.complexToDimension(data[index2 + 1], this.mMetrics);
            } else if (type == 2) {
                TypedValue value = this.mValue;
                getValueAt(index2, value);
                throw new UnsupportedOperationException("Failed to resolve attribute at index " + index + ": " + value);
            } else {
                throw new UnsupportedOperationException("Can't convert value at index " + index + " to dimension: type=0x" + Integer.toHexString(type));
            }
        } else {
            throw new RuntimeException("Cannot make calls to a recycled instance!");
        }
    }

    public int getDimensionPixelOffset(int index, int defValue) {
        int resourceId;
        if (!this.mRecycled) {
            int index2 = index * 7;
            int[] data = this.mData;
            int type = data[index2 + 0];
            if (type == 0) {
                return defValue;
            }
            if (type == 5) {
                if (HwThemeManager.DEBUG_UI_PROGRAM && (resourceId = getResourceId(index2)) > 0) {
                    try {
                        return this.mResources.getDimensionPixelOffset(resourceId);
                    } catch (Resources.NotFoundException e) {
                    }
                }
                return TypedValue.complexToDimensionPixelOffset(data[index2 + 1], this.mMetrics);
            } else if (type == 2) {
                TypedValue value = this.mValue;
                getValueAt(index2, value);
                throw new UnsupportedOperationException("Failed to resolve attribute at index " + index + ": " + value);
            } else {
                throw new UnsupportedOperationException("Can't convert value at index " + index + " to dimension: type=0x" + Integer.toHexString(type));
            }
        } else {
            throw new RuntimeException("Cannot make calls to a recycled instance!");
        }
    }

    public int getDimensionPixelSize(int index, int defValue) {
        int resourceId;
        if (this.mRecycled) {
            throw new RuntimeException("Cannot make calls to a recycled instance!");
        } else if (index >= this.mLength) {
            return defValue;
        } else {
            int index2 = index * 7;
            int[] data = this.mData;
            int type = data[index2 + 0];
            if (type == 0) {
                return defValue;
            }
            if (type == 5) {
                if (HwThemeManager.DEBUG_UI_PROGRAM && (resourceId = getResourceId(index2)) > 0) {
                    try {
                        return this.mResources.getDimensionPixelSize(resourceId);
                    } catch (Resources.NotFoundException e) {
                    }
                }
                return TypedValue.complexToDimensionPixelSize(data[index2 + 1], this.mMetrics);
            } else if (type == 2) {
                TypedValue value = this.mValue;
                getValueAt(index2, value);
                throw new UnsupportedOperationException("Failed to resolve attribute at index " + index + ": " + value);
            } else {
                throw new UnsupportedOperationException("Can't convert value at index " + index + " to dimension: type=0x" + Integer.toHexString(type));
            }
        }
    }

    public int getLayoutDimension(int index, String name) {
        int resourceId;
        int resourceId2;
        if (!this.mRecycled) {
            int index2 = index * 7;
            int[] data = this.mData;
            int type = data[index2 + 0];
            if (type >= 16 && type <= 31) {
                if (HwThemeManager.DEBUG_UI_PROGRAM && (resourceId2 = getResourceId(index2)) > 0) {
                    try {
                        return this.mResources.getInteger(resourceId2);
                    } catch (Resources.NotFoundException e) {
                    }
                }
                return data[index2 + 1];
            } else if (type == 5) {
                if (HwThemeManager.DEBUG_UI_PROGRAM && (resourceId = getResourceId(index2)) > 0) {
                    try {
                        return this.mResources.getDimensionPixelSize(resourceId);
                    } catch (Resources.NotFoundException e2) {
                    }
                }
                return TypedValue.complexToDimensionPixelSize(data[index2 + 1], this.mMetrics);
            } else if (type == 2) {
                TypedValue value = this.mValue;
                getValueAt(index2, value);
                throw new UnsupportedOperationException("Failed to resolve attribute at index " + index + ": " + value);
            } else {
                throw new UnsupportedOperationException(getPositionDescription() + ": You must supply a " + name + " attribute.");
            }
        } else {
            throw new RuntimeException("Cannot make calls to a recycled instance!");
        }
    }

    public int getLayoutDimension(int index, int defValue) {
        int resourceId;
        int resourceId2;
        if (!this.mRecycled) {
            int index2 = index * 7;
            int[] data = this.mData;
            int type = data[index2 + 0];
            if (type >= 16 && type <= 31) {
                if (HwThemeManager.DEBUG_UI_PROGRAM && (resourceId2 = getResourceId(index2)) > 0) {
                    try {
                        return this.mResources.getInteger(resourceId2);
                    } catch (Resources.NotFoundException e) {
                    }
                }
                return data[index2 + 1];
            } else if (type != 5) {
                return defValue;
            } else {
                if (HwThemeManager.DEBUG_UI_PROGRAM && (resourceId = getResourceId(index2)) > 0) {
                    try {
                        return this.mResources.getDimensionPixelSize(resourceId);
                    } catch (Resources.NotFoundException e2) {
                    }
                }
                return TypedValue.complexToDimensionPixelSize(data[index2 + 1], this.mMetrics);
            }
        } else {
            throw new RuntimeException("Cannot make calls to a recycled instance!");
        }
    }

    public float getFraction(int index, int base, int pbase, float defValue) {
        int resourceId;
        if (this.mRecycled) {
            throw new RuntimeException("Cannot make calls to a recycled instance!");
        } else if (index >= this.mLength) {
            return defValue;
        } else {
            int index2 = index * 7;
            int[] data = this.mData;
            int type = data[index2 + 0];
            if (type == 0) {
                return defValue;
            }
            if (type == 6) {
                if (HwThemeManager.DEBUG_UI_PROGRAM && (resourceId = getResourceId(index2)) > 0) {
                    try {
                        return this.mResources.getFraction(resourceId, base, pbase);
                    } catch (Resources.NotFoundException e) {
                    }
                }
                return TypedValue.complexToFraction(data[index2 + 1], (float) base, (float) pbase);
            } else if (type == 2) {
                TypedValue value = this.mValue;
                getValueAt(index2, value);
                throw new UnsupportedOperationException("Failed to resolve attribute at index " + index + ": " + value);
            } else {
                throw new UnsupportedOperationException("Can't convert value at index " + index + " to fraction: type=0x" + Integer.toHexString(type));
            }
        }
    }

    public int getResourceId(int index, int defValue) {
        int resid;
        if (!this.mRecycled) {
            int index2 = index * 7;
            int[] data = this.mData;
            if (data[index2 + 0] == 0 || (resid = data[index2 + 3]) == 0) {
                return defValue;
            }
            return resid;
        }
        throw new RuntimeException("Cannot make calls to a recycled instance!");
    }

    public int getThemeAttributeId(int index, int defValue) {
        if (!this.mRecycled) {
            int index2 = index * 7;
            int[] data = this.mData;
            if (data[index2 + 0] == 2) {
                return data[index2 + 1];
            }
            return defValue;
        }
        throw new RuntimeException("Cannot make calls to a recycled instance!");
    }

    public Drawable getDrawable(int index) {
        return getDrawableForDensity(index, 0);
    }

    public Drawable getDrawableForDensity(int index, int density) {
        if (!this.mRecycled) {
            TypedValue value = this.mValue;
            if (!getValueAt(index * 7, value)) {
                return null;
            }
            if (value.type != 2) {
                if (density > 0) {
                    this.mResources.getValueForDensity(value.resourceId, density, value, true);
                }
                return this.mResources.loadDrawable(value, value.resourceId, density, this.mTheme);
            }
            throw new UnsupportedOperationException("Failed to resolve attribute at index " + index + ": " + value);
        }
        throw new RuntimeException("Cannot make calls to a recycled instance!");
    }

    public Typeface getFont(int index) {
        if (!this.mRecycled) {
            TypedValue value = this.mValue;
            if (!getValueAt(index * 7, value)) {
                return null;
            }
            if (value.type != 2) {
                return this.mResources.getFont(value, value.resourceId);
            }
            throw new UnsupportedOperationException("Failed to resolve attribute at index " + index + ": " + value);
        }
        throw new RuntimeException("Cannot make calls to a recycled instance!");
    }

    public CharSequence[] getTextArray(int index) {
        if (!this.mRecycled) {
            TypedValue value = this.mValue;
            if (getValueAt(index * 7, value)) {
                return this.mResources.getTextArray(value.resourceId);
            }
            return null;
        }
        throw new RuntimeException("Cannot make calls to a recycled instance!");
    }

    public boolean getValue(int index, TypedValue outValue) {
        AbsResourcesImpl.ThemedValue themedValue;
        if (!this.mRecycled) {
            boolean ret = getValueAt(index * 7, outValue);
            if (HwThemeManager.DEBUG_UI_PROGRAM && ret && outValue.resourceId > 0 && (themedValue = this.mResources.getImpl().getHwResourcesImpl().getThemeDimension(outValue, outValue.resourceId)) != null) {
                outValue.data = themedValue.data;
            }
            return ret;
        }
        throw new RuntimeException("Cannot make calls to a recycled instance!");
    }

    public int getType(int index) {
        if (!this.mRecycled) {
            return this.mData[(index * 7) + 0];
        }
        throw new RuntimeException("Cannot make calls to a recycled instance!");
    }

    public int getSourceResourceId(int index, int defaultValue) {
        if (!this.mRecycled) {
            int resid = this.mData[(index * 7) + 6];
            if (resid != 0) {
                return resid;
            }
            return defaultValue;
        }
        throw new RuntimeException("Cannot make calls to a recycled instance!");
    }

    public boolean hasValue(int index) {
        if (!this.mRecycled) {
            return this.mData[(index * 7) + 0] != 0;
        }
        throw new RuntimeException("Cannot make calls to a recycled instance!");
    }

    public boolean hasValueOrEmpty(int index) {
        if (!this.mRecycled) {
            int index2 = index * 7;
            int[] data = this.mData;
            if (data[index2 + 0] != 0 || data[index2 + 1] == 1) {
                return true;
            }
            return false;
        }
        throw new RuntimeException("Cannot make calls to a recycled instance!");
    }

    public TypedValue peekValue(int index) {
        AbsResourcesImpl.ThemedValue themedValue;
        if (!this.mRecycled) {
            TypedValue value = this.mValue;
            if (!getValueAt(index * 7, value)) {
                return null;
            }
            if (HwThemeManager.DEBUG_UI_PROGRAM && value.resourceId > 0 && (themedValue = this.mResources.getImpl().getHwResourcesImpl().getThemeDimension(value, value.resourceId)) != null) {
                value.data = themedValue.data;
            }
            return value;
        }
        throw new RuntimeException("Cannot make calls to a recycled instance!");
    }

    public String getPositionDescription() {
        if (!this.mRecycled) {
            XmlBlock.Parser parser = this.mXml;
            return parser != null ? parser.getPositionDescription() : "<internal>";
        }
        throw new RuntimeException("Cannot make calls to a recycled instance!");
    }

    public void recycle() {
        if (!this.mRecycled) {
            this.mRecycled = true;
            this.mXml = null;
            this.mTheme = null;
            this.mAssets = null;
            this.mResources.mTypedArrayPool.release(this);
            return;
        }
        throw new RuntimeException(toString() + " recycled twice!");
    }

    @UnsupportedAppUsage
    public int[] extractThemeAttrs() {
        return extractThemeAttrs(null);
    }

    @UnsupportedAppUsage
    public int[] extractThemeAttrs(int[] scrap) {
        if (!this.mRecycled) {
            int[] attrs = null;
            int[] data = this.mData;
            int N = length();
            for (int i = 0; i < N; i++) {
                int index = i * 7;
                if (data[index + 0] == 2) {
                    data[index + 0] = 0;
                    int attr = data[index + 1];
                    if (attr != 0) {
                        if (attrs == null) {
                            if (scrap == null || scrap.length != N) {
                                attrs = new int[N];
                            } else {
                                attrs = scrap;
                                Arrays.fill(attrs, 0);
                            }
                        }
                        attrs[i] = attr;
                    }
                }
            }
            return attrs;
        }
        throw new RuntimeException("Cannot make calls to a recycled instance!");
    }

    public int getChangingConfigurations() {
        if (!this.mRecycled) {
            int changingConfig = 0;
            int[] data = this.mData;
            int N = length();
            for (int i = 0; i < N; i++) {
                int index = i * 7;
                if (data[index + 0] != 0) {
                    changingConfig |= ActivityInfo.activityInfoConfigNativeToJava(data[index + 4]);
                }
            }
            return changingConfig;
        }
        throw new RuntimeException("Cannot make calls to a recycled instance!");
    }

    @UnsupportedAppUsage
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
        outValue.sourceResourceId = data[index + 6];
        return true;
    }

    private CharSequence loadStringValueAt(int index) {
        CharSequence result;
        int[] data = this.mData;
        int cookie = data[index + 2];
        if (HwAssetManagerEx.hasRes() && (result = this.mAssets.getResourceText(data[index + 3])) != null) {
            return result;
        }
        if (cookie >= 0) {
            return this.mAssets.getPooledStringForCookie(cookie, data[index + 1]);
        }
        XmlBlock.Parser parser = this.mXml;
        if (parser != null) {
            return parser.getPooledString(data[index + 1]);
        }
        return null;
    }

    protected TypedArray(Resources resources) {
        this.mResources = resources;
        this.mMetrics = this.mResources.getDisplayMetrics();
        this.mAssets = this.mResources.getAssets();
    }

    public String toString() {
        return Arrays.toString(this.mData);
    }

    private int getResourceId(int index) {
        TypedValue value = this.mValue;
        if (getValueAt(index, value)) {
            return value.resourceId;
        }
        return 0;
    }
}
