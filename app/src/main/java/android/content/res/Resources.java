package android.content.res;

import android.R;
import android.animation.Animator;
import android.animation.StateListAnimator;
import android.common.HwFrameworkMonitor;
import android.content.res.ResourcesImpl.ThemeImpl;
import android.graphics.Bitmap;
import android.graphics.Movie;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Drawable.ConstantState;
import android.graphics.drawable.DrawableInflater;
import android.hwtheme.HwThemeManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.rog.AppRogInfo;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.LongSparseArray;
import android.util.Pools.SynchronizedPool;
import android.util.TypedValue;
import android.view.DisplayAdjustments;
import android.view.ViewDebug.ExportedProperty;
import android.view.ViewHierarchyEncoder;
import com.android.internal.util.GrowingArrayUtils;
import com.android.internal.util.XmlUtils;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import org.xmlpull.v1.XmlPullParserException;

public class Resources extends AbsResources {
    static final String TAG = "Resources";
    static Resources mSystem;
    private static final Object sSync = null;
    final ClassLoader mClassLoader;
    private DrawableInflater mDrawableInflater;
    private ResourcesImpl mResourcesImpl;
    private final ArrayList<WeakReference<Theme>> mThemeRefs;
    private TypedValue mTmpValue;
    private final Object mTmpValueLock;
    final SynchronizedPool<TypedArray> mTypedArrayPool;

    public static class NotFoundException extends RuntimeException {
        public NotFoundException(String name) {
            super(name);
        }

        public NotFoundException(String name, Exception cause) {
            super(name, cause);
        }
    }

    public final class Theme {
        private ThemeImpl mThemeImpl;

        private Theme() {
        }

        void setImpl(ThemeImpl impl) {
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

        long getNativeTheme() {
            return this.mThemeImpl.getNativeTheme();
        }

        int getAppliedStyleResId() {
            return this.mThemeImpl.getAppliedStyleResId();
        }

        public ThemeKey getKey() {
            return this.mThemeImpl.getKey();
        }

        private String getResourceNameFromHexString(String hexString) {
            return Resources.this.getResourceName(Integer.parseInt(hexString, 16));
        }

        @ExportedProperty(category = "theme", hasAdjacentMapping = true)
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
        private int mHashCode;
        int[] mResId;

        ThemeKey() {
            this.mHashCode = 0;
        }

        public void append(int resId, boolean force) {
            int i;
            if (this.mResId == null) {
                this.mResId = new int[4];
            }
            if (this.mForce == null) {
                this.mForce = new boolean[4];
            }
            this.mResId = GrowingArrayUtils.append(this.mResId, this.mCount, resId);
            this.mForce = GrowingArrayUtils.append(this.mForce, this.mCount, force);
            this.mCount++;
            int i2 = ((this.mHashCode * 31) + resId) * 31;
            if (force) {
                i = 1;
            } else {
                i = 0;
            }
            this.mHashCode = i + i2;
        }

        public void setTo(ThemeKey other) {
            boolean[] zArr = null;
            this.mResId = other.mResId == null ? null : (int[]) other.mResId.clone();
            if (other.mForce != null) {
                zArr = (boolean[]) other.mForce.clone();
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
            int i = 0;
            while (i < N) {
                if (this.mResId[i] != t.mResId[i] || this.mForce[i] != t.mForce[i]) {
                    return false;
                }
                i++;
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

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.content.res.Resources.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.content.res.Resources.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.content.res.Resources.<clinit>():void");
    }

    public static int selectDefaultTheme(int curTheme, int targetSdkVersion) {
        return selectSystemTheme(curTheme, targetSdkVersion, R.style.Theme, R.style.Theme_Holo, R.style.Theme_DeviceDefault, R.style.Theme_DeviceDefault_Light_DarkActionBar);
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

    public Resources(AssetManager assets, DisplayMetrics metrics, Configuration config) {
        this(null);
        DisplayAdjustments daj = new DisplayAdjustments();
        CompatibilityInfo makeCompatibilityInfo = (metrics == null || Float.compare(metrics.density, metrics.noncompatDensity) == 0) ? CompatibilityInfo.DEFAULT_COMPATIBILITY_INFO : CompatibilityInfo.makeCompatibilityInfo(1);
        daj.setCompatibilityInfo(makeCompatibilityInfo);
        this.mResourcesImpl = new ResourcesImpl(assets, metrics, config, daj);
    }

    public Resources(ClassLoader classLoader) {
        this.mTypedArrayPool = new SynchronizedPool(5);
        this.mTmpValueLock = new Object();
        this.mTmpValue = new TypedValue();
        this.mThemeRefs = new ArrayList();
        if (classLoader == null) {
            classLoader = ClassLoader.getSystemClassLoader();
        }
        this.mClassLoader = classLoader;
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
                    WeakReference<Theme> weakThemeRef = (WeakReference) this.mThemeRefs.get(i);
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
        CharSequence res = null;
        if (id != 0) {
            res = this.mResourcesImpl.getAssets().getResourceText(id);
        }
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
        int[] res = this.mResourcesImpl.getAssets().getArrayIntResource(id);
        if (res != null) {
            return res;
        }
        throw new NotFoundException("Int array resource ID #0x" + Integer.toHexString(id));
    }

    public TypedArray obtainTypedArray(int id) throws NotFoundException {
        ResourcesImpl impl = this.mResourcesImpl;
        int len = impl.getAssets().getArraySize(id);
        if (len < 0) {
            throw new NotFoundException("Array resource ID #0x" + Integer.toHexString(id));
        }
        TypedArray array = TypedArray.obtain(this, len);
        array.mLength = impl.getAssets().retrieveArray(id, array.mData);
        array.mIndices[0] = 0;
        return array;
    }

    public float getDimension(int id) throws NotFoundException {
        TypedValue value = obtainTempTypedValue();
        try {
            ResourcesImpl impl = this.mResourcesImpl;
            impl.getValue(id, value, true);
            if (value.type == 5) {
                float complexToDimension = TypedValue.complexToDimension(value.data, impl.getDisplayMetrics());
                return complexToDimension;
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
                int complexToDimensionPixelOffset = TypedValue.complexToDimensionPixelOffset(value.data, impl.getDisplayMetrics());
                return complexToDimensionPixelOffset;
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
                int complexToDimensionPixelSize = TypedValue.complexToDimensionPixelSize(value.data, impl.getDisplayMetrics());
                return complexToDimensionPixelSize;
            }
            throw new NotFoundException("Resource ID #0x" + Integer.toHexString(id) + " type #0x" + Integer.toHexString(value.type) + " is not valid");
        } finally {
            releaseTempTypedValue(value);
        }
    }

    public float getFraction(int id, int base, int pbase) {
        TypedValue value = obtainTempTypedValue();
        try {
            this.mResourcesImpl.getValue(id, value, true);
            if (value.type == 6) {
                float complexToFraction = TypedValue.complexToFraction(value.data, (float) base, (float) pbase);
                return complexToFraction;
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
            Log.w(TAG, "Drawable " + getResourceName(id) + " has unresolved theme " + "attributes! Consider using Resources.getDrawable(int, Theme) or " + "Context.getDrawable(int).", new RuntimeException());
        }
        return d;
    }

    public Drawable getDrawable(int id, Theme theme) throws NotFoundException {
        TypedValue value = obtainTempTypedValue();
        try {
            ResourcesImpl impl = this.mResourcesImpl;
            impl.getValue(id, value, true);
            Drawable loadDrawable = impl.loadDrawable(this, value, id, theme, true);
            return loadDrawable;
        } finally {
            releaseTempTypedValue(value);
        }
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
            DisplayMetrics metrics = impl.getDisplayMetrics();
            boolean useCache = value.density == metrics.densityDpi;
            if (value.density > 0 && value.density != PowerManager.WAKE_LOCK_LEVEL_MASK) {
                if (value.density == density) {
                    value.density = metrics.densityDpi;
                } else {
                    value.density = (value.density * metrics.densityDpi) / density;
                }
            }
            Drawable loadDrawable = impl.loadDrawable(this, value, id, theme, useCache);
            return loadDrawable;
        } finally {
            releaseTempTypedValue(value);
        }
    }

    Drawable loadDrawable(TypedValue value, int id, Theme theme) throws NotFoundException {
        return this.mResourcesImpl.loadDrawable(this, value, id, theme, true);
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
            int i;
            if (value.type >= 16 && value.type <= 31) {
                i = value.data;
                return i;
            } else if (value.type != 3) {
                throw new NotFoundException("Resource ID #0x" + Integer.toHexString(id) + " type #0x" + Integer.toHexString(value.type) + " is not valid");
            } else {
                i = impl.loadColorStateList(this, value, id, theme).getDefaultColor();
                releaseTempTypedValue(value);
                return i;
            }
        } finally {
            releaseTempTypedValue(value);
        }
    }

    @Deprecated
    public ColorStateList getColorStateList(int id) throws NotFoundException {
        ColorStateList csl = getColorStateList(id, null);
        if (csl != null && csl.canApplyTheme()) {
            Log.w(TAG, "ColorStateList " + getResourceName(id) + " has " + "unresolved theme attributes! Consider using " + "Resources.getColorStateList(int, Theme) or " + "Context.getColorStateList(int).", new RuntimeException());
        }
        return csl;
    }

    public ColorStateList getColorStateList(int id, Theme theme) throws NotFoundException {
        TypedValue value = obtainTempTypedValue();
        try {
            ResourcesImpl impl = this.mResourcesImpl;
            impl.getValue(id, value, true);
            ColorStateList loadColorStateList = impl.loadColorStateList(this, value, id, theme);
            return loadColorStateList;
        } finally {
            releaseTempTypedValue(value);
        }
    }

    ColorStateList loadColorStateList(TypedValue value, int id, Theme theme) throws NotFoundException {
        return this.mResourcesImpl.loadColorStateList(this, value, id, theme);
    }

    public ComplexColor loadComplexColor(TypedValue value, int id, Theme theme) {
        return this.mResourcesImpl.loadComplexColor(this, value, id, theme);
    }

    public boolean getBoolean(int id) throws NotFoundException {
        boolean z = true;
        TypedValue value = obtainTempTypedValue();
        try {
            this.mResourcesImpl.getValue(id, value, true);
            if (value.type < 16 || value.type > 31) {
                throw new NotFoundException("Resource ID #0x" + Integer.toHexString(id) + " type #0x" + Integer.toHexString(value.type) + " is not valid");
            }
            if (value.data == 0) {
                z = false;
            }
            releaseTempTypedValue(value);
            return z;
        } catch (Throwable th) {
            releaseTempTypedValue(value);
        }
    }

    public int getInteger(int id) throws NotFoundException {
        TypedValue value = obtainTempTypedValue();
        try {
            this.mResourcesImpl.getValue(id, value, true);
            if (value.type < 16 || value.type > 31) {
                throw new NotFoundException("Resource ID #0x" + Integer.toHexString(id) + " type #0x" + Integer.toHexString(value.type) + " is not valid");
            }
            int i = value.data;
            return i;
        } finally {
            releaseTempTypedValue(value);
        }
    }

    public float getFloat(int id) {
        TypedValue value = obtainTempTypedValue();
        try {
            this.mResourcesImpl.getValue(id, value, true);
            if (value.type == 4) {
                float f = value.getFloat();
                return f;
            }
            throw new NotFoundException("Resource ID #0x" + Integer.toHexString(id) + " type #0x" + Integer.toHexString(value.type) + " is not valid");
        } finally {
            releaseTempTypedValue(value);
        }
    }

    public XmlResourceParser getLayout(int id) throws NotFoundException {
        return loadXmlResourceParser(id, TtmlUtils.TAG_LAYOUT);
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
            InputStream openRawResource = openRawResource(id, value);
            return openRawResource;
        } finally {
            releaseTempTypedValue(value);
        }
    }

    protected TypedValue obtainTempTypedValue() {
        TypedValue typedValue = null;
        synchronized (this.mTmpValueLock) {
            if (this.mTmpValue != null) {
                typedValue = this.mTmpValue;
                this.mTmpValue = null;
            }
        }
        if (typedValue == null) {
            return new TypedValue();
        }
        return typedValue;
    }

    protected void releaseTempTypedValue(TypedValue value) {
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
            AssetFileDescriptor openRawResourceFd = this.mResourcesImpl.openRawResourceFd(id, value);
            return openRawResourceFd;
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
        this.mThemeRefs.add(new WeakReference(theme));
        return theme;
    }

    public TypedArray obtainAttributes(AttributeSet set, int[] attrs) {
        TypedArray array = TypedArray.obtain(this, attrs.length);
        Parser parser = (Parser) set;
        this.mResourcesImpl.getAssets().retrieveAttributes(parser.mParseState, attrs, array.mData, array.mIndices);
        array.mXml = parser;
        return array;
    }

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
            int type = parser.next();
            if (type == 1) {
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
        boolean z = true;
        TypedArray sa = obtainAttributes(attrs, com.android.internal.R.styleable.Extra);
        String name = sa.getString(0);
        if (name == null) {
            sa.recycle();
            throw new XmlPullParserException("<" + tagName + "> requires an android:name attribute at " + attrs.getPositionDescription());
        }
        TypedValue v = sa.peekValue(1);
        if (v != null) {
            if (v.type == 3) {
                outBundle.putCharSequence(name, v.coerceToString());
            } else if (v.type == 18) {
                if (v.data == 0) {
                    z = false;
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

    public LongSparseArray<ConstantState> getPreloadedDrawables() {
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

    XmlResourceParser loadXmlResourceParser(int id, String type) throws NotFoundException {
        TypedValue value = obtainTempTypedValue();
        try {
            ResourcesImpl impl = this.mResourcesImpl;
            impl.getValue(id, value, true);
            if (value.type == 3) {
                XmlResourceParser loadXmlResourceParser = impl.loadXmlResourceParser(value.string.toString(), id, value.assetCookie, type);
                return loadXmlResourceParser;
            }
            throw new NotFoundException("Resource ID #0x" + Integer.toHexString(id) + " type #0x" + Integer.toHexString(value.type) + " is not valid");
        } finally {
            releaseTempTypedValue(value);
        }
    }

    XmlResourceParser loadXmlResourceParser(String file, int id, int assetCookie, String type) throws NotFoundException {
        return this.mResourcesImpl.loadXmlResourceParser(file, id, assetCookie, type);
    }

    public int calcConfigChanges(Configuration config) {
        return this.mResourcesImpl.calcConfigChanges(config);
    }

    public static TypedArray obtainAttributes(Resources res, Theme theme, AttributeSet set, int[] attrs) {
        if (theme == null) {
            return res.obtainAttributes(set, attrs);
        }
        return theme.obtainStyledAttributes(set, attrs, 0, 0);
    }

    protected CharSequence serbianSyrillic2Latin(CharSequence res) {
        return res;
    }

    protected CharSequence[] serbianSyrillic2Latin(CharSequence[] res) {
        return res;
    }

    protected String serbianSyrillic2Latin(String res) {
        return res;
    }

    protected String[] serbianSyrillic2Latin(String[] res) {
        return res;
    }

    protected boolean isSRLocale() {
        return false;
    }

    public void setRogInfo(AppRogInfo info, boolean enable) {
        this.mResourcesImpl.setRogInfo(info, enable);
    }
}
