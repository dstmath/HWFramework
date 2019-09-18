package android.content.res;

import android.animation.Animator;
import android.animation.StateListAnimator;
import android.app.ActivityThread;
import android.app.AppGlobals;
import android.common.HwFrameworkMonitor;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import android.content.res.ResourcesImpl;
import android.content.res.XmlBlock;
import android.graphics.Bitmap;
import android.graphics.Movie;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.DrawableInflater;
import android.hwtheme.HwThemeManager;
import android.os.Bundle;
import android.os.Process;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.LongSparseArray;
import android.util.Pools;
import android.util.TypedValue;
import android.view.DisplayAdjustments;
import android.view.ViewDebug;
import android.view.ViewHierarchyEncoder;
import com.android.internal.R;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.GrowingArrayUtils;
import com.android.internal.util.XmlUtils;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.xmlpull.v1.XmlPullParserException;

public class Resources extends AbsResources {
    private static final int MIN_THEME_REFS_FLUSH_SIZE = 32;
    private static boolean SUPPORT_LOCK_DPI = SystemProperties.getBoolean("ro.config.auto_display_mode", true);
    static final String TAG = "Resources";
    static Resources mSystem = null;
    private static Map<String, Bundle> sMultidpiInfos = new HashMap();
    private static Object sMultidpiInfosLock = new Object();
    private static final Object sSync = new Object();
    final ClassLoader mClassLoader;
    private DrawableInflater mDrawableInflater;
    private ResourcesImpl mResourcesImpl;
    private boolean mScaleOpt;
    private final ArrayList<WeakReference<Theme>> mThemeRefs;
    private int mThemeRefsNextFlushSize;
    private TypedValue mTmpValue;
    private final Object mTmpValueLock;
    final Pools.SynchronizedPool<TypedArray> mTypedArrayPool;

    public static class NotFoundException extends RuntimeException {
        public NotFoundException() {
        }

        public NotFoundException(String name) {
            super(name);
        }

        public NotFoundException(String name, Exception cause) {
            super(name, cause);
        }
    }

    public final class Theme {
        private ResourcesImpl.ThemeImpl mThemeImpl;

        private Theme() {
        }

        /* access modifiers changed from: package-private */
        public void setImpl(ResourcesImpl.ThemeImpl impl) {
            this.mThemeImpl = impl;
        }

        public void applyStyle(int resId, boolean force) {
            this.mThemeImpl.applyStyle(resId, force);
        }

        public void setTo(Theme other) {
            this.mThemeImpl.setTo(other.mThemeImpl);
        }

        public TypedArray obtainStyledAttributes(int[] attrs) {
            return this.mThemeImpl.obtainStyledAttributes(this, null, attrs, 0, 0);
        }

        public TypedArray obtainStyledAttributes(int resId, int[] attrs) throws NotFoundException {
            return this.mThemeImpl.obtainStyledAttributes(this, null, attrs, 0, resId);
        }

        public TypedArray obtainStyledAttributes(AttributeSet set, int[] attrs, int defStyleAttr, int defStyleRes) {
            return this.mThemeImpl.obtainStyledAttributes(this, set, attrs, defStyleAttr, defStyleRes);
        }

        public TypedArray resolveAttributes(int[] values, int[] attrs) {
            return this.mThemeImpl.resolveAttributes(this, values, attrs);
        }

        public boolean resolveAttribute(int resid, TypedValue outValue, boolean resolveRefs) {
            return this.mThemeImpl.resolveAttribute(resid, outValue, resolveRefs);
        }

        public int[] getAllAttributes() {
            return this.mThemeImpl.getAllAttributes();
        }

        public Resources getResources() {
            return Resources.this;
        }

        public Drawable getDrawable(int id) throws NotFoundException {
            return Resources.this.getDrawable(id, this);
        }

        public int getChangingConfigurations() {
            return this.mThemeImpl.getChangingConfigurations();
        }

        public void dump(int priority, String tag, String prefix) {
            this.mThemeImpl.dump(priority, tag, prefix);
        }

        /* access modifiers changed from: package-private */
        public long getNativeTheme() {
            return this.mThemeImpl.getNativeTheme();
        }

        /* access modifiers changed from: package-private */
        public int getAppliedStyleResId() {
            return this.mThemeImpl.getAppliedStyleResId();
        }

        public ThemeKey getKey() {
            return this.mThemeImpl.getKey();
        }

        private String getResourceNameFromHexString(String hexString) {
            return Resources.this.getResourceName(Integer.parseInt(hexString, 16));
        }

        @ViewDebug.ExportedProperty(category = "theme", hasAdjacentMapping = true)
        public String[] getTheme() {
            return this.mThemeImpl.getTheme();
        }

        public void encode(ViewHierarchyEncoder encoder) {
            encoder.beginObject(this);
            String[] properties = getTheme();
            for (int i = 0; i < properties.length; i += 2) {
                encoder.addProperty(properties[i], properties[i + 1]);
            }
            encoder.endObject();
        }

        public void rebase() {
            this.mThemeImpl.rebase();
        }
    }

    static class ThemeKey implements Cloneable {
        int mCount;
        boolean[] mForce;
        private int mHashCode = 0;
        int[] mResId;

        ThemeKey() {
        }

        public void append(int resId, boolean force) {
            if (this.mResId == null) {
                this.mResId = new int[4];
            }
            if (this.mForce == null) {
                this.mForce = new boolean[4];
            }
            this.mResId = GrowingArrayUtils.append(this.mResId, this.mCount, resId);
            this.mForce = GrowingArrayUtils.append(this.mForce, this.mCount, force);
            this.mCount++;
            this.mHashCode = (31 * ((this.mHashCode * 31) + resId)) + (force);
        }

        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v5, resolved type: java.lang.Object} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v2, resolved type: boolean[]} */
        /* JADX WARNING: Multi-variable type inference failed */
        public void setTo(ThemeKey other) {
            boolean[] zArr = null;
            this.mResId = other.mResId == null ? null : (int[]) other.mResId.clone();
            if (other.mForce != null) {
                zArr = other.mForce.clone();
            }
            this.mForce = zArr;
            this.mCount = other.mCount;
        }

        public int hashCode() {
            return this.mHashCode;
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass() || hashCode() != o.hashCode()) {
                return false;
            }
            ThemeKey t = (ThemeKey) o;
            if (this.mCount != t.mCount) {
                return false;
            }
            int N = this.mCount;
            for (int i = 0; i < N; i++) {
                if (this.mResId[i] != t.mResId[i] || this.mForce[i] != t.mForce[i]) {
                    return false;
                }
            }
            return true;
        }

        public ThemeKey clone() {
            ThemeKey other = new ThemeKey();
            other.mResId = this.mResId;
            other.mForce = this.mForce;
            other.mCount = this.mCount;
            other.mHashCode = this.mHashCode;
            return other;
        }
    }

    public static int selectDefaultTheme(int curTheme, int targetSdkVersion) {
        return selectSystemTheme(curTheme, targetSdkVersion, 16973829, 16973931, 16974120, 16974143);
    }

    public static int selectSystemTheme(int curTheme, int targetSdkVersion, int orig, int holo, int dark, int deviceDefault) {
        if (curTheme != 0) {
            return curTheme;
        }
        if (targetSdkVersion < 11) {
            return orig;
        }
        if (targetSdkVersion < 14) {
            return holo;
        }
        if (targetSdkVersion < 24) {
            return dark;
        }
        return deviceDefault;
    }

    public static Resources getSystem() {
        Resources ret;
        synchronized (sSync) {
            ret = mSystem;
            if (ret == null) {
                ret = HwThemeManager.getResources();
                mSystem = ret;
            }
        }
        return ret;
    }

    public static Resources getSystem(boolean preloading) {
        Resources ret;
        synchronized (sSync) {
            ret = mSystem;
            if (ret == null) {
                ret = HwThemeManager.getResources(preloading);
                mSystem = ret;
            }
        }
        return ret;
    }

    public Resources(AssetManager assets, DisplayMetrics metrics, Configuration config, DisplayAdjustments displayAdjustments) {
        this(null);
        this.mResourcesImpl = new ResourcesImpl(assets, metrics, config, displayAdjustments);
    }

    @Deprecated
    public Resources(AssetManager assets, DisplayMetrics metrics, Configuration config) {
        this(null);
        DisplayAdjustments daj = new DisplayAdjustments();
        daj.setCompatibilityInfo((metrics == null || metrics.noncompatWidthPixels == 0 || Float.compare((float) metrics.widthPixels, (float) metrics.noncompatWidthPixels) == 0) ? CompatibilityInfo.DEFAULT_COMPATIBILITY_INFO : CompatibilityInfo.makeCompatibilityInfo((((float) metrics.widthPixels) * 1.0f) / ((float) metrics.noncompatWidthPixels)));
        this.mResourcesImpl = new ResourcesImpl(assets, metrics, config, daj);
    }

    public Resources(ClassLoader classLoader) {
        this.mTypedArrayPool = new Pools.SynchronizedPool<>(5);
        this.mTmpValueLock = new Object();
        this.mTmpValue = new TypedValue();
        this.mThemeRefs = new ArrayList<>();
        this.mThemeRefsNextFlushSize = 32;
        this.mScaleOpt = false;
        this.mClassLoader = classLoader == null ? ClassLoader.getSystemClassLoader() : classLoader;
    }

    public Resources() {
        this(null);
        DisplayMetrics metrics = new DisplayMetrics();
        metrics.setToDefaults();
        Configuration config = new Configuration();
        config.setToDefaults();
        this.mResourcesImpl = new ResourcesImpl(AssetManager.getSystem(), metrics, config, new DisplayAdjustments());
    }

    public void setImpl(ResourcesImpl impl) {
        if (impl != this.mResourcesImpl) {
            this.mResourcesImpl = impl;
            synchronized (this.mThemeRefs) {
                int count = this.mThemeRefs.size();
                for (int i = 0; i < count; i++) {
                    WeakReference<Theme> weakThemeRef = this.mThemeRefs.get(i);
                    Theme theme = weakThemeRef != null ? (Theme) weakThemeRef.get() : null;
                    if (theme != null) {
                        theme.setImpl(this.mResourcesImpl.newThemeImpl(theme.getKey()));
                    }
                }
            }
        }
    }

    public ResourcesImpl getImpl() {
        return this.mResourcesImpl;
    }

    public ClassLoader getClassLoader() {
        return this.mClassLoader;
    }

    public final DrawableInflater getDrawableInflater() {
        if (this.mDrawableInflater == null) {
            this.mDrawableInflater = new DrawableInflater(this, this.mClassLoader);
        }
        return this.mDrawableInflater;
    }

    public ConfigurationBoundResourceCache<Animator> getAnimatorCache() {
        return this.mResourcesImpl.getAnimatorCache();
    }

    public ConfigurationBoundResourceCache<StateListAnimator> getStateListAnimatorCache() {
        return this.mResourcesImpl.getStateListAnimatorCache();
    }

    public CharSequence getText(int id) throws NotFoundException {
        CharSequence res = this.mResourcesImpl.getAssets().getResourceText(id);
        if (res != null) {
            return res;
        }
        throw new NotFoundException("String resource ID #0x" + Integer.toHexString(id));
    }

    public Typeface getFont(int id) throws NotFoundException {
        TypedValue value = obtainTempTypedValue();
        try {
            ResourcesImpl impl = this.mResourcesImpl;
            impl.getValue(id, value, true);
            Typeface typeface = impl.loadFont(this, value, id);
            if (typeface != null) {
                return typeface;
            }
            releaseTempTypedValue(value);
            throw new NotFoundException("Font resource ID #0x" + Integer.toHexString(id));
        } finally {
            releaseTempTypedValue(value);
        }
    }

    /* access modifiers changed from: package-private */
    public Typeface getFont(TypedValue value, int id) throws NotFoundException {
        return this.mResourcesImpl.loadFont(this, value, id);
    }

    public void preloadFonts(int id) {
        TypedArray array = obtainTypedArray(id);
        try {
            int size = array.length();
            for (int i = 0; i < size; i++) {
                array.getFont(i);
            }
        } finally {
            array.recycle();
        }
    }

    public CharSequence getQuantityText(int id, int quantity) throws NotFoundException {
        return this.mResourcesImpl.getQuantityText(id, quantity);
    }

    public String getString(int id) throws NotFoundException {
        return getText(id).toString();
    }

    public String getString(int id, Object... formatArgs) throws NotFoundException {
        return String.format(this.mResourcesImpl.getConfiguration().getLocales().get(0), getString(id), formatArgs);
    }

    public String getQuantityString(int id, int quantity, Object... formatArgs) throws NotFoundException {
        return String.format(this.mResourcesImpl.getConfiguration().getLocales().get(0), getQuantityText(id, quantity).toString(), formatArgs);
    }

    public String getQuantityString(int id, int quantity) throws NotFoundException {
        return getQuantityText(id, quantity).toString();
    }

    public CharSequence getText(int id, CharSequence def) {
        CharSequence res = id != 0 ? this.mResourcesImpl.getAssets().getResourceText(id) : null;
        return res != null ? res : def;
    }

    public CharSequence[] getTextArray(int id) throws NotFoundException {
        CharSequence[] res = this.mResourcesImpl.getAssets().getResourceTextArray(id);
        if (res != null) {
            return res;
        }
        throw new NotFoundException("Text array resource ID #0x" + Integer.toHexString(id));
    }

    public String[] getStringArray(int id) throws NotFoundException {
        String[] res = this.mResourcesImpl.getAssets().getResourceStringArray(id);
        if (res != null) {
            return res;
        }
        throw new NotFoundException("String array resource ID #0x" + Integer.toHexString(id));
    }

    public int[] getIntArray(int id) throws NotFoundException {
        int[] res = this.mResourcesImpl.getAssets().getResourceIntArray(id);
        if (res != null) {
            return res;
        }
        throw new NotFoundException("Int array resource ID #0x" + Integer.toHexString(id));
    }

    public TypedArray obtainTypedArray(int id) throws NotFoundException {
        ResourcesImpl impl = this.mResourcesImpl;
        int len = impl.getAssets().getResourceArraySize(id);
        if (len >= 0) {
            TypedArray array = TypedArray.obtain(this, len);
            array.mLength = impl.getAssets().getResourceArray(id, array.mData);
            array.mIndices[0] = 0;
            return array;
        }
        throw new NotFoundException("Array resource ID #0x" + Integer.toHexString(id));
    }

    public float getDimension(int id) throws NotFoundException {
        TypedValue value = obtainTempTypedValue();
        try {
            ResourcesImpl impl = this.mResourcesImpl;
            impl.getValue(id, value, true);
            if (value.type == 5) {
                return TypedValue.complexToDimension(value.data, impl.getDisplayMetrics());
            }
            throw new NotFoundException("Resource ID #0x" + Integer.toHexString(id) + " type #0x" + Integer.toHexString(value.type) + " is not valid");
        } finally {
            releaseTempTypedValue(value);
        }
    }

    public int getDimensionPixelOffset(int id) throws NotFoundException {
        TypedValue value = obtainTempTypedValue();
        try {
            ResourcesImpl impl = this.mResourcesImpl;
            impl.getValue(id, value, true);
            if (value.type == 5) {
                int res = TypedValue.complexToDimensionPixelOffset(value.data, impl.getDisplayMetrics());
                if (isTypeMM(value.data) && isRogOn()) {
                    res = (int) ((((float) res) * getMMScale()) + 0.5f);
                }
                return rebuildSpecialDimens(id, value, res);
            }
            throw new NotFoundException("Resource ID #0x" + Integer.toHexString(id) + " type #0x" + Integer.toHexString(value.type) + " is not valid");
        } finally {
            releaseTempTypedValue(value);
        }
    }

    public int getDimensionPixelSize(int id) throws NotFoundException {
        TypedValue value = obtainTempTypedValue();
        try {
            ResourcesImpl impl = this.mResourcesImpl;
            impl.getValue(id, value, true);
            if (value.type == 5) {
                int res = TypedValue.complexToDimensionPixelSize(value.data, impl.getDisplayMetrics());
                if (isTypeMM(value.data) && isRogOn()) {
                    res = (int) ((((float) res) * getMMScale()) + 0.5f);
                }
                return rebuildSpecialDimens(id, value, res);
            }
            throw new NotFoundException("Resource ID #0x" + Integer.toHexString(id) + " type #0x" + Integer.toHexString(value.type) + " is not valid");
        } finally {
            releaseTempTypedValue(value);
        }
    }

    private int rebuildSpecialDimens(int id, TypedValue value, int res) {
        if ((id == 17105318 || id == 17105320 || id == 17105319) && !isLockDpi() && !isTypeMM(value.data)) {
            boolean isSupprotLockDpi = SystemProperties.getBoolean("ro.config.auto_display_mode", true);
            int srcDpi = SystemProperties.getInt("ro.sf.real_lcd_density", SystemProperties.getInt("ro.sf.lcd_density", 0));
            int dpi = SystemProperties.getInt("persist.sys.dpi", srcDpi);
            if (dpi <= 0 || dpi == srcDpi || !isSupprotLockDpi) {
                return res;
            }
            return (((res * srcDpi) + dpi) - 1) / dpi;
        } else if (id == 17105186 || id == 17105188) {
            if ((SystemProperties.getInt("persist.sys.navigationbar.mode", 0) & 2) != 0) {
                res = (int) (((double) res) * 0.42d);
                if (id == 17105188) {
                    res = 0;
                }
            }
            if (!isTypeMM(value.data) || (res & 1) != 1) {
                return res;
            }
            return res + 1;
        } else if (id != 17105191 || (SystemProperties.getInt("persist.sys.navigationbar.mode", 0) & 2) == 0) {
            return res;
        } else {
            return 0;
        }
    }

    private boolean isLockDpi() {
        String pkgName = ActivityThread.currentPackageName();
        Bundle multidpiInfo = null;
        if (!TextUtils.isEmpty(pkgName)) {
            multidpiInfo = this.mResourcesImpl.getHwResourcesImpl().getMultidpiInfo(pkgName);
        }
        if (multidpiInfo == null) {
            return false;
        }
        return multidpiInfo.getBoolean("LockDpi", false);
    }

    public boolean isTypeMM(int data) {
        if (((data >> 0) & 15) == 5) {
            return true;
        }
        return false;
    }

    public boolean isRogOn() {
        return SystemProperties.getInt("persist.sys.rog.configmode", 0) == 1;
    }

    public static float getMMScale() {
        int dpi = SystemProperties.getInt("persist.sys.dpi", SystemProperties.getInt("ro.sf.real_lcd_density", SystemProperties.getInt("ro.sf.lcd_density", 0)));
        int realdpi = SystemProperties.getInt("persist.sys.realdpi", dpi);
        if (dpi == 0 || realdpi == dpi) {
            return 1.0f;
        }
        return (((float) realdpi) * 1.0f) / ((float) dpi);
    }

    public float getFraction(int id, int base, int pbase) {
        TypedValue value = obtainTempTypedValue();
        try {
            this.mResourcesImpl.getValue(id, value, true);
            if (value.type == 6) {
                return TypedValue.complexToFraction(value.data, (float) base, (float) pbase);
            }
            throw new NotFoundException("Resource ID #0x" + Integer.toHexString(id) + " type #0x" + Integer.toHexString(value.type) + " is not valid");
        } finally {
            releaseTempTypedValue(value);
        }
    }

    @Deprecated
    public Drawable getDrawable(int id) throws NotFoundException {
        Drawable d = getDrawable(id, null);
        if (d != null && d.canApplyTheme()) {
            Log.w(TAG, "Drawable " + getResourceName(id) + " has unresolved theme attributes! Consider using Resources.getDrawable(int, Theme) or Context.getDrawable(int).", new RuntimeException());
        }
        return d;
    }

    public Drawable getDrawable(int id, Theme theme) throws NotFoundException {
        return getDrawableForDensity(id, 0, theme);
    }

    @Deprecated
    public Drawable getDrawableForDensity(int id, int density) throws NotFoundException {
        return getDrawableForDensity(id, density, null);
    }

    public Drawable getDrawableForDensity(int id, int density, Theme theme) {
        TypedValue value = obtainTempTypedValue();
        try {
            ResourcesImpl impl = this.mResourcesImpl;
            impl.getValueForDensity(id, density, value, true);
            return impl.loadDrawable(this, value, id, density, theme);
        } finally {
            releaseTempTypedValue(value);
        }
    }

    /* access modifiers changed from: package-private */
    public Drawable loadDrawable(TypedValue value, int id, int density, Theme theme) throws NotFoundException {
        return this.mResourcesImpl.loadDrawable(this, value, id, density, theme);
    }

    public Movie getMovie(int id) throws NotFoundException {
        InputStream is = openRawResource(id);
        Movie movie = Movie.decodeStream(is);
        try {
            is.close();
        } catch (IOException e) {
        }
        return movie;
    }

    @Deprecated
    public int getColor(int id) throws NotFoundException {
        return getColor(id, null);
    }

    public int getColor(int id, Theme theme) throws NotFoundException {
        TypedValue value = obtainTempTypedValue();
        try {
            ResourcesImpl impl = this.mResourcesImpl;
            impl.getValue(id, value, true);
            if (value.type >= 16 && value.type <= 31) {
                return value.data;
            }
            if (value.type == 3) {
                int defaultColor = impl.loadColorStateList(this, value, id, theme).getDefaultColor();
                releaseTempTypedValue(value);
                return defaultColor;
            }
            throw new NotFoundException("Resource ID #0x" + Integer.toHexString(id) + " type #0x" + Integer.toHexString(value.type) + " is not valid");
        } finally {
            releaseTempTypedValue(value);
        }
    }

    @Deprecated
    public ColorStateList getColorStateList(int id) throws NotFoundException {
        ColorStateList csl = getColorStateList(id, null);
        if (csl != null && csl.canApplyTheme()) {
            Log.w(TAG, "ColorStateList " + getResourceName(id) + " has unresolved theme attributes! Consider using Resources.getColorStateList(int, Theme) or Context.getColorStateList(int).", new RuntimeException());
        }
        return csl;
    }

    public ColorStateList getColorStateList(int id, Theme theme) throws NotFoundException {
        TypedValue value = obtainTempTypedValue();
        try {
            ResourcesImpl impl = this.mResourcesImpl;
            impl.getValue(id, value, true);
            return impl.loadColorStateList(this, value, id, theme);
        } finally {
            releaseTempTypedValue(value);
        }
    }

    /* access modifiers changed from: package-private */
    public ColorStateList loadColorStateList(TypedValue value, int id, Theme theme) throws NotFoundException {
        return this.mResourcesImpl.loadColorStateList(this, value, id, theme);
    }

    public ComplexColor loadComplexColor(TypedValue value, int id, Theme theme) {
        return this.mResourcesImpl.loadComplexColor(this, value, id, theme);
    }

    public boolean getBoolean(int id) throws NotFoundException {
        TypedValue value = obtainTempTypedValue();
        try {
            boolean z = true;
            this.mResourcesImpl.getValue(id, value, true);
            if (value.type < 16 || value.type > 31) {
                throw new NotFoundException("Resource ID #0x" + Integer.toHexString(id) + " type #0x" + Integer.toHexString(value.type) + " is not valid");
            }
            if (value.data == 0) {
                z = false;
            }
            return z;
        } finally {
            releaseTempTypedValue(value);
        }
    }

    public int getInteger(int id) throws NotFoundException {
        TypedValue value = obtainTempTypedValue();
        try {
            this.mResourcesImpl.getValue(id, value, true);
            if (value.type >= 16 && value.type <= 31) {
                return value.data;
            }
            throw new NotFoundException("Resource ID #0x" + Integer.toHexString(id) + " type #0x" + Integer.toHexString(value.type) + " is not valid");
        } finally {
            releaseTempTypedValue(value);
        }
    }

    public float getFloat(int id) {
        TypedValue value = obtainTempTypedValue();
        try {
            this.mResourcesImpl.getValue(id, value, true);
            if (value.type == 4) {
                return value.getFloat();
            }
            throw new NotFoundException("Resource ID #0x" + Integer.toHexString(id) + " type #0x" + Integer.toHexString(value.type) + " is not valid");
        } finally {
            releaseTempTypedValue(value);
        }
    }

    public XmlResourceParser getLayout(int id) throws NotFoundException {
        return loadXmlResourceParser(id, "layout");
    }

    public XmlResourceParser getAnimation(int id) throws NotFoundException {
        return loadXmlResourceParser(id, "anim");
    }

    public XmlResourceParser getXml(int id) throws NotFoundException {
        return loadXmlResourceParser(id, "xml");
    }

    public InputStream openRawResource(int id) throws NotFoundException {
        TypedValue value = obtainTempTypedValue();
        try {
            return openRawResource(id, value);
        } finally {
            releaseTempTypedValue(value);
        }
    }

    private TypedValue obtainTempTypedValue() {
        TypedValue tmpValue = null;
        synchronized (this.mTmpValueLock) {
            if (this.mTmpValue != null) {
                tmpValue = this.mTmpValue;
                this.mTmpValue = null;
            }
        }
        if (tmpValue == null) {
            return new TypedValue();
        }
        return tmpValue;
    }

    /* access modifiers changed from: protected */
    public TypedValue hwObtainTempTypedValue() {
        return obtainTempTypedValue();
    }

    /* access modifiers changed from: protected */
    public void hwReleaseTempTypedValue(TypedValue value) {
        releaseTempTypedValue(value);
    }

    private void releaseTempTypedValue(TypedValue value) {
        synchronized (this.mTmpValueLock) {
            if (this.mTmpValue == null) {
                this.mTmpValue = value;
            }
        }
    }

    public InputStream openRawResource(int id, TypedValue value) throws NotFoundException {
        return this.mResourcesImpl.openRawResource(id, value);
    }

    public AssetFileDescriptor openRawResourceFd(int id) throws NotFoundException {
        TypedValue value = obtainTempTypedValue();
        try {
            return this.mResourcesImpl.openRawResourceFd(id, value);
        } finally {
            releaseTempTypedValue(value);
        }
    }

    public void getValue(int id, TypedValue outValue, boolean resolveRefs) throws NotFoundException {
        this.mResourcesImpl.getValue(id, outValue, resolveRefs);
    }

    public void getValueForDensity(int id, int density, TypedValue outValue, boolean resolveRefs) throws NotFoundException {
        this.mResourcesImpl.getValueForDensity(id, density, outValue, resolveRefs);
    }

    public void getValue(String name, TypedValue outValue, boolean resolveRefs) throws NotFoundException {
        this.mResourcesImpl.getValue(name, outValue, resolveRefs);
    }

    public final Theme newTheme() {
        Theme theme = new Theme();
        theme.setImpl(this.mResourcesImpl.newThemeImpl());
        synchronized (this.mThemeRefs) {
            this.mThemeRefs.add(new WeakReference(theme));
            if (this.mThemeRefs.size() > this.mThemeRefsNextFlushSize) {
                this.mThemeRefs.removeIf($$Lambda$Resources$4msWUw7LKsgLexLZjIfWa4oguq4.INSTANCE);
                this.mThemeRefsNextFlushSize = Math.max(32, 2 * this.mThemeRefs.size());
            }
        }
        return theme;
    }

    static /* synthetic */ boolean lambda$newTheme$0(WeakReference ref) {
        return ref.get() == null;
    }

    public TypedArray obtainAttributes(AttributeSet set, int[] attrs) {
        TypedArray array = TypedArray.obtain(this, attrs.length);
        XmlBlock.Parser parser = (XmlBlock.Parser) set;
        this.mResourcesImpl.getAssets().retrieveAttributes(parser, attrs, array.mData, array.mIndices);
        array.mXml = parser;
        return array;
    }

    @Deprecated
    public void updateConfiguration(Configuration config, DisplayMetrics metrics) {
        updateConfiguration(config, metrics, null);
    }

    public void updateConfiguration(Configuration config, DisplayMetrics metrics, CompatibilityInfo compat) {
        this.mResourcesImpl.updateConfiguration(config, metrics, compat);
    }

    public static void updateSystemConfiguration(Configuration config, DisplayMetrics metrics, CompatibilityInfo compat) {
        if (mSystem != null) {
            mSystem.updateConfiguration(config, metrics, compat);
        }
    }

    public DisplayMetrics getDisplayMetrics() {
        return this.mResourcesImpl.getDisplayMetrics();
    }

    public DisplayAdjustments getDisplayAdjustments() {
        return this.mResourcesImpl.getDisplayAdjustments();
    }

    public Configuration getConfiguration() {
        return this.mResourcesImpl.getConfiguration();
    }

    public Configuration[] getSizeConfigurations() {
        return this.mResourcesImpl.getSizeConfigurations();
    }

    public CompatibilityInfo getCompatibilityInfo() {
        return this.mResourcesImpl.getCompatibilityInfo();
    }

    @VisibleForTesting
    public void setCompatibilityInfo(CompatibilityInfo ci) {
        if (ci != null) {
            this.mResourcesImpl.updateConfiguration(null, null, ci);
        }
    }

    public int getIdentifier(String name, String defType, String defPackage) {
        return this.mResourcesImpl.getIdentifier(name, defType, defPackage);
    }

    public static boolean resourceHasPackage(int resid) {
        return (resid >>> 24) != 0;
    }

    public String getResourceName(int resid) throws NotFoundException {
        return this.mResourcesImpl.getResourceName(resid);
    }

    public String getResourcePackageName(int resid) throws NotFoundException {
        return this.mResourcesImpl.getResourcePackageName(resid);
    }

    public String getResourceTypeName(int resid) throws NotFoundException {
        return this.mResourcesImpl.getResourceTypeName(resid);
    }

    public String getResourceEntryName(int resid) throws NotFoundException {
        return this.mResourcesImpl.getResourceEntryName(resid);
    }

    public void parseBundleExtras(XmlResourceParser parser, Bundle outBundle) throws XmlPullParserException, IOException {
        int outerDepth = parser.getDepth();
        while (true) {
            int next = parser.next();
            int type = next;
            if (next == 1) {
                return;
            }
            if (type == 3 && parser.getDepth() <= outerDepth) {
                return;
            }
            if (!(type == 3 || type == 4)) {
                if (parser.getName().equals(HwFrameworkMonitor.KEY_EXTRA)) {
                    parseBundleExtra(HwFrameworkMonitor.KEY_EXTRA, parser, outBundle);
                    XmlUtils.skipCurrentTag(parser);
                } else {
                    XmlUtils.skipCurrentTag(parser);
                }
            }
        }
    }

    public void parseBundleExtra(String tagName, AttributeSet attrs, Bundle outBundle) throws XmlPullParserException {
        TypedArray sa = obtainAttributes(attrs, R.styleable.Extra);
        boolean z = false;
        String name = sa.getString(0);
        if (name != null) {
            TypedValue v = sa.peekValue(1);
            if (v != null) {
                if (v.type == 3) {
                    outBundle.putCharSequence(name, v.coerceToString());
                } else if (v.type == 18) {
                    if (v.data != 0) {
                        z = true;
                    }
                    outBundle.putBoolean(name, z);
                } else if (v.type >= 16 && v.type <= 31) {
                    outBundle.putInt(name, v.data);
                } else if (v.type == 4) {
                    outBundle.putFloat(name, v.getFloat());
                } else {
                    sa.recycle();
                    throw new XmlPullParserException("<" + tagName + "> only supports string, integer, float, color, and boolean at " + attrs.getPositionDescription());
                }
                sa.recycle();
                return;
            }
            sa.recycle();
            throw new XmlPullParserException("<" + tagName + "> requires an android:value or android:resource attribute at " + attrs.getPositionDescription());
        }
        sa.recycle();
        throw new XmlPullParserException("<" + tagName + "> requires an android:name attribute at " + attrs.getPositionDescription());
    }

    public final AssetManager getAssets() {
        return this.mResourcesImpl.getAssets();
    }

    public final void flushLayoutCache() {
        this.mResourcesImpl.flushLayoutCache();
    }

    public final void startPreloading() {
        this.mResourcesImpl.startPreloading();
    }

    public final void finishPreloading() {
        this.mResourcesImpl.finishPreloading();
    }

    public LongSparseArray<Drawable.ConstantState> getPreloadedDrawables() {
        return this.mResourcesImpl.getPreloadedDrawables();
    }

    public Bitmap getOptimizationIcon(Bitmap bmpSrc) {
        if (bmpSrc == null || bmpSrc.isRecycled()) {
            return null;
        }
        return this.mResourcesImpl.getHwResourcesImpl().addShortcutBackgroud(bmpSrc);
    }

    public Drawable getThemeDrawableByName(String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }
        Bitmap themeIcon = this.mResourcesImpl.getHwResourcesImpl().getThemeIconByName(name);
        if (themeIcon != null) {
            return new BitmapDrawable(this, themeIcon);
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public XmlResourceParser loadXmlResourceParser(int id, String type) throws NotFoundException {
        TypedValue value = obtainTempTypedValue();
        try {
            ResourcesImpl impl = this.mResourcesImpl;
            impl.getValue(id, value, true);
            if (value.type == 3) {
                return impl.loadXmlResourceParser(value.string.toString(), id, value.assetCookie, type);
            }
            throw new NotFoundException("Resource ID #0x" + Integer.toHexString(id) + " type #0x" + Integer.toHexString(value.type) + " is not valid");
        } finally {
            releaseTempTypedValue(value);
        }
    }

    /* access modifiers changed from: package-private */
    public XmlResourceParser loadXmlResourceParser(String file, int id, int assetCookie, String type) throws NotFoundException {
        return this.mResourcesImpl.loadXmlResourceParser(file, id, assetCookie, type);
    }

    @VisibleForTesting
    public int calcConfigChanges(Configuration config) {
        return this.mResourcesImpl.calcConfigChanges(config);
    }

    public static TypedArray obtainAttributes(Resources res, Theme theme, AttributeSet set, int[] attrs) {
        if (theme == null) {
            return res.obtainAttributes(set, attrs);
        }
        return theme.obtainStyledAttributes(set, attrs, 0, 0);
    }

    /* access modifiers changed from: protected */
    public CharSequence serbianSyrillic2Latin(CharSequence res) {
        return res;
    }

    /* access modifiers changed from: protected */
    public CharSequence[] serbianSyrillic2Latin(CharSequence[] res) {
        return res;
    }

    /* access modifiers changed from: protected */
    public String serbianSyrillic2Latin(String res) {
        return res;
    }

    /* access modifiers changed from: protected */
    public String[] serbianSyrillic2Latin(String[] res) {
        return res;
    }

    /* access modifiers changed from: protected */
    public boolean isSRLocale() {
        return false;
    }

    public void setResourceScaleOpt(boolean scale) {
        this.mScaleOpt = scale;
    }

    public boolean getResourceScaleOpt() {
        return this.mScaleOpt;
    }

    public static Bundle getPreMultidpiInfo(String packageName) {
        Bundle dpiInfo;
        if (!SUPPORT_LOCK_DPI || packageName == null || "android".equals(packageName) || "androidhwext".equals(packageName)) {
            return null;
        }
        synchronized (sMultidpiInfosLock) {
            dpiInfo = sMultidpiInfos.get(packageName);
        }
        if (dpiInfo != null) {
            return dpiInfo;
        }
        Bundle dpiInfo2 = new Bundle();
        ApplicationInfo tmpInfo = null;
        try {
            IPackageManager pm = AppGlobals.getPackageManager();
            if (pm != null) {
                tmpInfo = pm.getApplicationInfo(packageName, 128, UserHandle.getUserId(Process.myUid()));
            }
        } catch (RemoteException e) {
            Log.w(TAG, "getApplicationInfo for " + packageName + ", Exception:" + e);
        }
        ApplicationInfo tmpInfo2 = tmpInfo;
        if (tmpInfo2 != null) {
            boolean needLockRes = (tmpInfo2.flags & 1) != 0;
            Bundle metaData = tmpInfo2.metaData;
            if (metaData != null) {
                if ("default".equalsIgnoreCase(metaData.getString("support_display_mode"))) {
                    dpiInfo2.putBoolean("LockDpi", true);
                }
                String msg = metaData.getString("huawei_support_lock_res");
                if (TextUtils.isEmpty(msg)) {
                    msg = metaData.getString("support_lock_res");
                }
                if ("lock".equalsIgnoreCase(msg)) {
                    dpiInfo2.putBoolean("LockRes", true);
                } else if ("no_lock".equalsIgnoreCase(msg)) {
                    dpiInfo2.putBoolean("LockRes", false);
                } else {
                    dpiInfo2.putBoolean("LockRes", needLockRes);
                }
            } else {
                dpiInfo2.putBoolean("LockRes", needLockRes);
            }
        }
        synchronized (sMultidpiInfosLock) {
            sMultidpiInfos.put(packageName, dpiInfo2);
        }
        return dpiInfo2;
    }
}
