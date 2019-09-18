package android.content.res;

import android.animation.Animator;
import android.animation.StateListAnimator;
import android.app.ActivityThread;
import android.app.slice.Slice;
import android.common.HwFrameworkFactory;
import android.content.pm.ActivityInfo;
import android.content.res.AssetManager;
import android.content.res.FontResourcesParser;
import android.content.res.Resources;
import android.content.res.XmlBlock;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.DrawableContainer;
import android.hwtheme.HwThemeManager;
import android.icu.text.PluralRules;
import android.os.Build;
import android.os.Bundle;
import android.os.LocaleList;
import android.os.Process;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.Trace;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.HwPCUtils;
import android.util.Log;
import android.util.LongSparseArray;
import android.util.Slog;
import android.util.TypedValue;
import android.util.Xml;
import android.view.DisplayAdjustments;
import com.android.internal.util.GrowingArrayUtils;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Locale;
import org.xmlpull.v1.XmlPullParserException;

public class ResourcesImpl extends AbsResourcesImpl {
    private static final int CONFIG_DENSITY = ActivityInfo.activityInfoConfigJavaToNative(4096);
    private static final int CONFIG_FONT_SCALE = ActivityInfo.activityInfoConfigJavaToNative(1073741824);
    private static final int CONFIG_ORIENTATION = ActivityInfo.activityInfoConfigJavaToNative(128);
    private static final int CONFIG_SCREEN_SIZE = 1024;
    private static final int CONFIG_SMALLEST_SCREEN_SIZE = ActivityInfo.activityInfoConfigJavaToNative(2048);
    private static final boolean DEBUG_CONFIG = false;
    private static final boolean DEBUG_LOAD = false;
    private static final int ID_OTHER = 16777220;
    private static final int SPEC_PUBLIC = 1073741824;
    static final String TAG = "Resources";
    static final String TAG_PRELOAD = "Resources.preload";
    public static final boolean TRACE_FOR_DETAILED_PRELOAD = SystemProperties.getBoolean("debug.trace_resource_preload", false);
    private static final boolean TRACE_FOR_MISS_PRELOAD = false;
    private static final boolean TRACE_FOR_PRELOAD = false;
    private static final int XML_BLOCK_CACHE_SIZE = 4;
    public static final boolean mIsLockResWhitelist = SystemProperties.getBoolean("ro.config.hw_lock_res_whitelist", false);
    private static int sPreloadTracingNumLoadedDrawables;
    private static boolean sPreloaded;
    private static final LongSparseArray<Drawable.ConstantState> sPreloadedColorDrawables = new LongSparseArray<>();
    private static final LongSparseArray<ConstantState<ComplexColor>> sPreloadedComplexColors = new LongSparseArray<>();
    private static int sPreloadedDensity;
    private static final LongSparseArray<Drawable.ConstantState>[] sPreloadedDrawables = new LongSparseArray[2];
    private static final Object sSync = new Object();
    private final Object mAccessLock = new Object();
    private final ConfigurationBoundResourceCache<Animator> mAnimatorCache = new ConfigurationBoundResourceCache<>();
    final AssetManager mAssets;
    private final int[] mCachedXmlBlockCookies = new int[4];
    private final String[] mCachedXmlBlockFiles = new String[4];
    private final XmlBlock[] mCachedXmlBlocks = new XmlBlock[4];
    private final DrawableCache mColorDrawableCache = new DrawableCache();
    private final ConfigurationBoundResourceCache<ComplexColor> mComplexColorCache = new ConfigurationBoundResourceCache<>();
    private final Configuration mConfiguration = new Configuration();
    private final DisplayAdjustments mDisplayAdjustments;
    private final DrawableCache mDrawableCache = new DrawableCache();
    private AbsResourcesImpl mHwResourcesImpl;
    private int mLastCachedXmlBlockIndex = -1;
    private boolean mLockDpi = false;
    private boolean mLockRes = true;
    private final ThreadLocal<LookupStack> mLookupStack = ThreadLocal.withInitial($$Lambda$ResourcesImpl$h3PTRX185BeQl8SVC2_w9arp5Og.INSTANCE);
    private final DisplayMetrics mMetrics = new DisplayMetrics();
    private PluralRules mPluralRule;
    private long mPreloadTracingPreloadStartTime;
    private long mPreloadTracingStartBitmapCount;
    private long mPreloadTracingStartBitmapSize;
    public boolean mPreloading;
    private final ConfigurationBoundResourceCache<StateListAnimator> mStateListAnimatorCache = new ConfigurationBoundResourceCache<>();
    private final Configuration mTmpConfig = new Configuration();

    private static class LookupStack {
        private int[] mIds;
        private int mSize;

        private LookupStack() {
            this.mIds = new int[4];
            this.mSize = 0;
        }

        public void push(int id) {
            this.mIds = GrowingArrayUtils.append(this.mIds, this.mSize, id);
            this.mSize++;
        }

        public boolean contains(int id) {
            for (int i = 0; i < this.mSize; i++) {
                if (this.mIds[i] == id) {
                    return true;
                }
            }
            return false;
        }

        public void pop() {
            this.mSize--;
        }
    }

    public class ThemeImpl {
        private final AssetManager mAssets;
        /* access modifiers changed from: private */
        public final Resources.ThemeKey mKey = new Resources.ThemeKey();
        private final long mTheme;
        private int mThemeResId = 0;

        ThemeImpl() {
            this.mAssets = ResourcesImpl.this.mAssets;
            this.mTheme = this.mAssets.createTheme();
        }

        /* access modifiers changed from: protected */
        public void finalize() throws Throwable {
            super.finalize();
            this.mAssets.releaseTheme(this.mTheme);
        }

        /* access modifiers changed from: package-private */
        public Resources.ThemeKey getKey() {
            return this.mKey;
        }

        /* access modifiers changed from: package-private */
        public long getNativeTheme() {
            return this.mTheme;
        }

        /* access modifiers changed from: package-private */
        public int getAppliedStyleResId() {
            return this.mThemeResId;
        }

        /* access modifiers changed from: package-private */
        public void applyStyle(int resId, boolean force) {
            synchronized (this.mKey) {
                this.mAssets.applyStyleToTheme(this.mTheme, resId, force);
                this.mThemeResId = resId;
                this.mKey.append(resId, force);
            }
        }

        /* access modifiers changed from: package-private */
        public void setTo(ThemeImpl other) {
            synchronized (this.mKey) {
                synchronized (other.mKey) {
                    AssetManager.nativeThemeCopy(this.mTheme, other.mTheme);
                    this.mThemeResId = other.mThemeResId;
                    this.mKey.setTo(other.getKey());
                }
            }
        }

        /* access modifiers changed from: package-private */
        public TypedArray obtainStyledAttributes(Resources.Theme wrapper, AttributeSet set, int[] attrs, int defStyleAttr, int defStyleRes) {
            synchronized (this.mKey) {
                int[] iArr = attrs;
                try {
                    int len = iArr.length;
                    TypedArray array = TypedArray.obtain(wrapper.getResources(), len);
                    XmlBlock.Parser parser = (XmlBlock.Parser) set;
                    int i = len;
                    XmlBlock.Parser parser2 = parser;
                    this.mAssets.applyStyle(this.mTheme, defStyleAttr, defStyleRes, parser, iArr, array.mDataAddress, array.mIndicesAddress);
                    array.mTheme = wrapper;
                    array.mXml = parser2;
                    return array;
                } catch (Throwable th) {
                    th = th;
                    throw th;
                }
            }
        }

        /* access modifiers changed from: package-private */
        public TypedArray resolveAttributes(Resources.Theme wrapper, int[] values, int[] attrs) {
            TypedArray array;
            synchronized (this.mKey) {
                int len = attrs.length;
                if (values == null || len != values.length) {
                    throw new IllegalArgumentException("Base attribute values must the same length as attrs");
                }
                array = TypedArray.obtain(wrapper.getResources(), len);
                this.mAssets.resolveAttrs(this.mTheme, 0, 0, values, attrs, array.mData, array.mIndices);
                array.mTheme = wrapper;
                array.mXml = null;
            }
            return array;
        }

        /* access modifiers changed from: package-private */
        public boolean resolveAttribute(int resid, TypedValue outValue, boolean resolveRefs) {
            boolean themeValue;
            synchronized (this.mKey) {
                themeValue = this.mAssets.getThemeValue(this.mTheme, resid, outValue, resolveRefs);
            }
            return themeValue;
        }

        /* access modifiers changed from: package-private */
        public int[] getAllAttributes() {
            return this.mAssets.getStyleAttributes(getAppliedStyleResId());
        }

        /* access modifiers changed from: package-private */
        public int getChangingConfigurations() {
            int activityInfoConfigNativeToJava;
            synchronized (this.mKey) {
                activityInfoConfigNativeToJava = ActivityInfo.activityInfoConfigNativeToJava(AssetManager.nativeThemeGetChangingConfigurations(this.mTheme));
            }
            return activityInfoConfigNativeToJava;
        }

        public void dump(int priority, String tag, String prefix) {
            synchronized (this.mKey) {
                this.mAssets.dumpTheme(this.mTheme, priority, tag, prefix);
            }
        }

        /* access modifiers changed from: package-private */
        public String[] getTheme() {
            String[] themes;
            synchronized (this.mKey) {
                int N = this.mKey.mCount;
                themes = new String[(N * 2)];
                int i = 0;
                int j = N - 1;
                while (i < themes.length) {
                    int resId = this.mKey.mResId[j];
                    boolean forced = this.mKey.mForce[j];
                    try {
                        themes[i] = ResourcesImpl.this.getResourceName(resId);
                    } catch (Resources.NotFoundException e) {
                        themes[i] = Integer.toHexString(i);
                    }
                    themes[i + 1] = forced ? "forced" : "not forced";
                    i += 2;
                    j--;
                }
            }
            return themes;
        }

        /* access modifiers changed from: package-private */
        public void rebase() {
            synchronized (this.mKey) {
                AssetManager.nativeThemeClear(this.mTheme);
                for (int i = 0; i < this.mKey.mCount; i++) {
                    this.mAssets.applyStyleToTheme(this.mTheme, this.mKey.mResId[i], this.mKey.mForce[i]);
                }
            }
        }
    }

    static {
        sPreloadedDrawables[0] = new LongSparseArray<>();
        sPreloadedDrawables[1] = new LongSparseArray<>();
    }

    static /* synthetic */ LookupStack lambda$new$0() {
        return new LookupStack();
    }

    public ResourcesImpl(AssetManager assets, DisplayMetrics metrics, Configuration config, DisplayAdjustments displayAdjustments) {
        this.mAssets = assets;
        this.mHwResourcesImpl = HwThemeManager.getHwResourcesImpl();
        this.mHwResourcesImpl.setResourcesImpl(this);
        this.mHwResourcesImpl.setHwTheme(config);
        if (!HwPCUtils.enabled() || ActivityThread.currentActivityThread() == null || !HwPCUtils.isValidExtDisplayId(ActivityThread.currentActivityThread().getDisplayId())) {
            this.mHwResourcesImpl.initDeepTheme();
        }
        this.mMetrics.setToDefaults();
        this.mDisplayAdjustments = displayAdjustments;
        this.mConfiguration.setToDefaults();
        updateConfiguration(config, metrics, displayAdjustments.getCompatibilityInfo());
    }

    public AbsResourcesImpl getHwResourcesImpl() {
        return this.mHwResourcesImpl;
    }

    public DisplayAdjustments getDisplayAdjustments() {
        return this.mDisplayAdjustments;
    }

    public AssetManager getAssets() {
        return this.mAssets;
    }

    /* access modifiers changed from: package-private */
    public DisplayMetrics getDisplayMetrics() {
        return this.mMetrics;
    }

    /* access modifiers changed from: package-private */
    public Configuration getConfiguration() {
        return this.mConfiguration;
    }

    /* access modifiers changed from: package-private */
    public Configuration[] getSizeConfigurations() {
        return this.mAssets.getSizeConfigurations();
    }

    /* access modifiers changed from: package-private */
    public CompatibilityInfo getCompatibilityInfo() {
        return this.mDisplayAdjustments.getCompatibilityInfo();
    }

    private PluralRules getPluralRule() {
        PluralRules pluralRules;
        synchronized (sSync) {
            if (this.mPluralRule == null) {
                this.mPluralRule = PluralRules.forLocale(this.mConfiguration.getLocales().get(0));
            }
            pluralRules = this.mPluralRule;
        }
        return pluralRules;
    }

    /* access modifiers changed from: package-private */
    public void getValue(int id, TypedValue outValue, boolean resolveRefs) throws Resources.NotFoundException {
        if (!this.mAssets.getResourceValue(id, 0, outValue, resolveRefs)) {
            throw new Resources.NotFoundException("Resource ID #0x" + Integer.toHexString(id));
        }
    }

    /* access modifiers changed from: package-private */
    public void getValueForDensity(int id, int density, TypedValue outValue, boolean resolveRefs) throws Resources.NotFoundException {
        if (!this.mAssets.getResourceValue(id, density, outValue, resolveRefs)) {
            throw new Resources.NotFoundException("Resource ID #0x" + Integer.toHexString(id));
        }
    }

    /* access modifiers changed from: package-private */
    public void getValue(String name, TypedValue outValue, boolean resolveRefs) throws Resources.NotFoundException {
        int id = getIdentifier(name, "string", null);
        if (id != 0) {
            getValue(id, outValue, resolveRefs);
            return;
        }
        throw new Resources.NotFoundException("String resource name " + name);
    }

    /* access modifiers changed from: package-private */
    public int getIdentifier(String name, String defType, String defPackage) {
        if (name != null) {
            try {
                return Integer.parseInt(name);
            } catch (Exception e) {
                return this.mAssets.getResourceIdentifier(name, defType, defPackage);
            }
        } else {
            throw new NullPointerException("name is null");
        }
    }

    /* access modifiers changed from: package-private */
    public String getResourceName(int resid) throws Resources.NotFoundException {
        String str = this.mAssets.getResourceName(resid);
        if (str != null) {
            return str;
        }
        throw new Resources.NotFoundException("Unable to find resource ID #0x" + Integer.toHexString(resid));
    }

    /* access modifiers changed from: package-private */
    public String getResourcePackageName(int resid) throws Resources.NotFoundException {
        String str = this.mAssets.getResourcePackageName(resid);
        if (str != null) {
            return str;
        }
        throw new Resources.NotFoundException("Unable to find resource ID #0x" + Integer.toHexString(resid));
    }

    /* access modifiers changed from: package-private */
    public String getResourceTypeName(int resid) throws Resources.NotFoundException {
        String str = this.mAssets.getResourceTypeName(resid);
        if (str != null) {
            return str;
        }
        throw new Resources.NotFoundException("Unable to find resource ID #0x" + Integer.toHexString(resid));
    }

    /* access modifiers changed from: package-private */
    public String getResourceEntryName(int resid) throws Resources.NotFoundException {
        String str = this.mAssets.getResourceEntryName(resid);
        if (str != null) {
            return str;
        }
        throw new Resources.NotFoundException("Unable to find resource ID #0x" + Integer.toHexString(resid));
    }

    /* access modifiers changed from: package-private */
    public CharSequence getQuantityText(int id, int quantity) throws Resources.NotFoundException {
        PluralRules rule = getPluralRule();
        CharSequence res = this.mAssets.getResourceBagText(id, attrForQuantityCode(rule.select((double) quantity)));
        if (res != null) {
            return res;
        }
        CharSequence res2 = this.mAssets.getResourceBagText(id, ID_OTHER);
        if (res2 != null) {
            return res2;
        }
        throw new Resources.NotFoundException("Plural resource ID #0x" + Integer.toHexString(id) + " quantity=" + quantity + " item=" + rule.select((double) quantity));
    }

    /* JADX WARNING: Can't fix incorrect switch cases order */
    private static int attrForQuantityCode(String quantityCode) {
        char c;
        switch (quantityCode.hashCode()) {
            case 101272:
                if (quantityCode.equals("few")) {
                    c = 3;
                    break;
                }
            case 110182:
                if (quantityCode.equals("one")) {
                    c = 1;
                    break;
                }
            case 115276:
                if (quantityCode.equals("two")) {
                    c = 2;
                    break;
                }
            case 3343967:
                if (quantityCode.equals("many")) {
                    c = 4;
                    break;
                }
            case 3735208:
                if (quantityCode.equals("zero")) {
                    c = 0;
                    break;
                }
            default:
                c = 65535;
                break;
        }
        switch (c) {
            case 0:
                return 16777221;
            case 1:
                return 16777222;
            case 2:
                return 16777223;
            case 3:
                return 16777224;
            case 4:
                return 16777225;
            default:
                return ID_OTHER;
        }
    }

    /* access modifiers changed from: package-private */
    public AssetFileDescriptor openRawResourceFd(int id, TypedValue tempValue) throws Resources.NotFoundException {
        getValue(id, tempValue, true);
        try {
            return this.mAssets.openNonAssetFd(tempValue.assetCookie, tempValue.string.toString());
        } catch (Exception e) {
            throw new Resources.NotFoundException("File " + tempValue.string.toString() + " from drawable resource ID #0x" + Integer.toHexString(id), e);
        }
    }

    /* access modifiers changed from: package-private */
    public InputStream openRawResource(int id, TypedValue value) throws Resources.NotFoundException {
        getValue(id, value, true);
        try {
            return this.mAssets.openNonAsset(value.assetCookie, value.string.toString(), 2);
        } catch (Exception e) {
            StringBuilder sb = new StringBuilder();
            sb.append("File ");
            sb.append(value.string == null ? "(null)" : value.string.toString());
            sb.append(" from drawable resource ID #0x");
            sb.append(Integer.toHexString(id));
            Resources.NotFoundException rnf = new Resources.NotFoundException(sb.toString());
            rnf.initCause(e);
            throw rnf;
        }
    }

    /* access modifiers changed from: package-private */
    public ConfigurationBoundResourceCache<Animator> getAnimatorCache() {
        return this.mAnimatorCache;
    }

    /* access modifiers changed from: package-private */
    public ConfigurationBoundResourceCache<StateListAnimator> getStateListAnimatorCache() {
        return this.mStateListAnimatorCache;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:77:?, code lost:
        r4 = sSync;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:78:0x0174, code lost:
        monitor-enter(r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:81:0x0177, code lost:
        if (r1.mPluralRule == null) goto L_0x0189;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:82:0x0179, code lost:
        r1.mPluralRule = android.icu.text.PluralRules.forLocale(r1.mConfiguration.getLocales().get(0));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:83:0x0189, code lost:
        monitor-exit(r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:84:0x018a, code lost:
        android.os.Trace.traceEnd(8192);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:85:0x0190, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:95:0x0196, code lost:
        r0 = th;
     */
    public void updateConfiguration(Configuration config, DisplayMetrics metrics, CompatibilityInfo compat) {
        DisplayMetrics displayMetrics = metrics;
        CompatibilityInfo compatibilityInfo = compat;
        Trace.traceBegin(8192, "ResourcesImpl#updateConfiguration");
        try {
            synchronized (this.mAccessLock) {
                if (compatibilityInfo != null) {
                    try {
                        this.mDisplayAdjustments.setCompatibilityInfo(compatibilityInfo);
                    } catch (Throwable th) {
                        th = th;
                        Configuration configuration = config;
                        throw th;
                    }
                }
                if (displayMetrics != null) {
                    this.mMetrics.setTo(displayMetrics);
                }
                this.mDisplayAdjustments.getCompatibilityInfo().applyToDisplayMetrics(this.mMetrics);
                if (HwPCUtils.enabledInPad() || ActivityThread.currentActivityThread() == null || !HwPCUtils.isValidExtDisplayId(ActivityThread.currentActivityThread().getDisplayId())) {
                    String pkgName = ActivityThread.currentPackageName();
                    Bundle multidpiInfo = null;
                    if (!(pkgName == null || pkgName.length() == 0)) {
                        multidpiInfo = this.mHwResourcesImpl.getMultidpiInfo(pkgName);
                    }
                    if (multidpiInfo != null) {
                        this.mLockDpi = multidpiInfo.getBoolean("LockDpi", false);
                        this.mLockRes = multidpiInfo.getBoolean("LockRes", true);
                        if (this.mLockDpi) {
                            this.mMetrics.setToNonCompat();
                        }
                    }
                }
                int configChanges = calcConfigChanges(config);
                LocaleList locales = this.mConfiguration.getLocales();
                if (locales.isEmpty()) {
                    locales = LocaleList.getDefault();
                    this.mConfiguration.setLocales(locales);
                }
                boolean changeRes = false;
                if ((configChanges & 4) != 0) {
                    if (locales.size() > 1) {
                        String[] availableLocales = this.mAssets.getNonSystemLocales();
                        if (LocaleList.isPseudoLocalesOnly(availableLocales)) {
                            availableLocales = this.mAssets.getLocales();
                            if (LocaleList.isPseudoLocalesOnly(availableLocales)) {
                                availableLocales = null;
                            }
                        }
                        if (availableLocales != null && availableLocales.length == 1) {
                            changeRes = true;
                        }
                        if (availableLocales != null) {
                            boolean hasDbid = false;
                            int length = availableLocales.length;
                            int i = 0;
                            while (true) {
                                if (i >= length) {
                                    break;
                                } else if (availableLocales[i].equals("zz-ZX")) {
                                    hasDbid = true;
                                    break;
                                } else {
                                    i++;
                                }
                            }
                            if (hasDbid) {
                                String[] internal = availableLocales;
                                String[] shared = AssetManager.getSharedResList();
                                if (shared != null) {
                                    String[] result = (String[]) Arrays.copyOf(internal, internal.length + shared.length);
                                    System.arraycopy(shared, 0, result, internal.length, shared.length);
                                    availableLocales = result;
                                    if (shared.length > 0) {
                                        changeRes = true;
                                    }
                                }
                            }
                            Locale bestLocale = locales.getFirstMatchWithEnglishSupported(availableLocales);
                            if (!(bestLocale == null || bestLocale == locales.get(0))) {
                                this.mConfiguration.setLocales(new LocaleList(bestLocale, locales));
                            }
                        }
                    } else {
                        changeRes = true;
                    }
                }
                if (this.mConfiguration.densityDpi != 0 && !this.mLockDpi) {
                    this.mMetrics.densityDpi = this.mConfiguration.densityDpi;
                    this.mMetrics.density = ((float) this.mConfiguration.densityDpi) * 0.00625f;
                }
                this.mMetrics.scaledDensity = this.mMetrics.density * (this.mConfiguration.fontScale != 0.0f ? this.mConfiguration.fontScale : 1.0f);
                updateDpiConfiguration();
                updateAssetConfiguration(ActivityThread.currentPackageName(), true, changeRes);
                this.mDrawableCache.onConfigurationChange(configChanges);
                this.mColorDrawableCache.onConfigurationChange(configChanges);
                this.mComplexColorCache.onConfigurationChange(configChanges);
                this.mAnimatorCache.onConfigurationChange(configChanges);
                this.mStateListAnimatorCache.onConfigurationChange(configChanges);
                flushLayoutCache();
                try {
                    this.mHwResourcesImpl.updateConfiguration(config, configChanges);
                } catch (Throwable th2) {
                    th = th2;
                    throw th;
                }
            }
        } catch (Throwable th3) {
            th = th3;
            Configuration configuration2 = config;
            Trace.traceEnd(8192);
            throw th;
        }
    }

    public void updateAssetConfiguration(String packageName) {
        updateAssetConfiguration(packageName, false, false);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0057, code lost:
        if (getHwResourcesImpl().isInMultiDpiWhiteList(r39) == false) goto L_0x005c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0062, code lost:
        if (r0.mLockRes != false) goto L_0x0064;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0064, code lost:
        r7 = android.os.SystemProperties.getInt("ro.sf.real_lcd_density", android.os.SystemProperties.getInt("ro.sf.lcd_density", 0));
        r9 = android.os.SystemProperties.getInt("persist.sys.dpi", r7);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0079, code lost:
        if (r9 > 0) goto L_0x007c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x007b, code lost:
        r9 = r7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x007c, code lost:
        if (r9 == r7) goto L_0x00a0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x007e, code lost:
        if (r9 == 0) goto L_0x00a0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0080, code lost:
        if (r7 == 0) goto L_0x00a0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0082, code lost:
        r35 = (((r4 * r9) + r9) - 1) / r7;
        r36 = (((r5 * r9) + r9) - 1) / r7;
        r34 = (((r2 * r9) + r9) - 1) / r7;
        r2 = (((r6 * r7) + r9) - 1) / r9;
     */
    public void updateAssetConfiguration(String packageName, boolean resFlag, boolean changeRes) {
        int width;
        int i;
        int i2;
        if (this.mMetrics.widthPixels >= this.mMetrics.heightPixels) {
            width = this.mMetrics.widthPixels;
            i = this.mMetrics.heightPixels;
        } else {
            width = this.mMetrics.heightPixels;
            i = this.mMetrics.widthPixels;
        }
        int height = i;
        if (this.mConfiguration.keyboardHidden == 1 && this.mConfiguration.hardKeyboardHidden == 2) {
            i2 = 3;
        } else {
            i2 = this.mConfiguration.keyboardHidden;
        }
        int keyboardHidden = i2;
        int smallestScreenWidthDp = this.mConfiguration.smallestScreenWidthDp;
        int screenWidthDp = this.mConfiguration.screenWidthDp;
        int screenHeightDp = this.mConfiguration.screenHeightDp;
        int densityDpi = this.mConfiguration.densityDpi;
        if (!this.mLockDpi) {
            if (!mIsLockResWhitelist) {
                String str = packageName;
            }
            if (!mIsLockResWhitelist) {
            }
        } else {
            String str2 = packageName;
        }
        int smallestScreenWidthDp2 = smallestScreenWidthDp;
        int screenWidthDp2 = screenWidthDp;
        int screenHeightDp2 = screenHeightDp;
        int screenWidthDp3 = densityDpi;
        if (resFlag) {
            AssetManager assetManager = this.mAssets;
            int i3 = this.mConfiguration.mcc;
            int i4 = this.mConfiguration.mnc;
            String adjustLanguageTag = adjustLanguageTag(this.mConfiguration.getLocales().get(0).toLanguageTag());
            int i5 = this.mConfiguration.orientation;
            int i6 = this.mConfiguration.touchscreen;
            int i7 = this.mConfiguration.keyboard;
            int i8 = this.mConfiguration.navigation;
            int i9 = this.mConfiguration.screenLayout;
            int i10 = this.mConfiguration.uiMode;
            int i11 = this.mConfiguration.colorMode;
            int i12 = screenWidthDp3;
            int i13 = i9;
            int i14 = width;
            int i15 = smallestScreenWidthDp2;
            int i16 = screenWidthDp2;
            int i17 = screenHeightDp2;
            assetManager.setConfiguration(i3, i4, adjustLanguageTag, i5, i6, i12, i7, keyboardHidden, i8, i14, height, i15, i16, i17, i13, i10, i11, Build.VERSION.RESOURCES_SDK_INT, changeRes);
            return;
        }
        AssetManager assetManager2 = this.mAssets;
        int i18 = this.mConfiguration.mcc;
        int i19 = this.mConfiguration.mnc;
        String adjustLanguageTag2 = adjustLanguageTag(this.mConfiguration.getLocales().get(0).toLanguageTag());
        int i20 = this.mConfiguration.orientation;
        int i21 = this.mConfiguration.touchscreen;
        int i22 = this.mConfiguration.keyboard;
        int i23 = this.mConfiguration.navigation;
        int i24 = this.mConfiguration.screenLayout;
        int i25 = this.mConfiguration.uiMode;
        assetManager2.setConfiguration(i18, i19, adjustLanguageTag2, i20, i21, screenWidthDp3, i22, keyboardHidden, i23, width, height, smallestScreenWidthDp2, screenWidthDp2, screenHeightDp2, i24, i25, this.mConfiguration.colorMode, Build.VERSION.RESOURCES_SDK_INT);
    }

    private void updateDpiConfiguration() {
        int screenLayoutSize;
        if (this.mLockDpi) {
            int smallestScreenWidthDp = this.mConfiguration.smallestScreenWidthDp;
            int screenWidthDp = this.mConfiguration.screenWidthDp;
            int screenHeightDp = this.mConfiguration.screenHeightDp;
            int densityDpi = this.mConfiguration.densityDpi;
            int screenLayout = this.mConfiguration.screenLayout;
            int srcDpi = SystemProperties.getInt("ro.sf.real_lcd_density", SystemProperties.getInt("ro.sf.lcd_density", 0));
            int dpi = SystemProperties.getInt("persist.sys.dpi", srcDpi);
            int rogDpi = SystemProperties.getInt("persist.sys.realdpi", srcDpi);
            if (dpi <= 0) {
                dpi = srcDpi;
            }
            if (dpi != srcDpi && dpi != 0 && srcDpi != 0 && densityDpi != srcDpi && densityDpi != ((int) (((((float) (srcDpi * rogDpi)) * 1.0f) / ((float) dpi)) * 1.0f))) {
                Slog.i(TAG, "original config :" + this.mConfiguration);
                int screenWidthDp2 = (((screenWidthDp * dpi) + dpi) + -1) / srcDpi;
                int screenHeightDp2 = (((screenHeightDp * dpi) + dpi) + -1) / srcDpi;
                this.mConfiguration.smallestScreenWidthDp = (((smallestScreenWidthDp * dpi) + dpi) + -1) / srcDpi;
                this.mConfiguration.screenWidthDp = screenWidthDp2;
                this.mConfiguration.screenHeightDp = screenHeightDp2;
                this.mConfiguration.densityDpi = (((densityDpi * srcDpi) + dpi) - 1) / dpi;
                if ("com.android.browser".equals(ActivityThread.currentPackageName())) {
                    int longSizeDp = screenHeightDp2 > screenWidthDp2 ? screenHeightDp2 : screenWidthDp2;
                    int shortSizeDp = screenHeightDp2 > screenWidthDp2 ? screenWidthDp2 : screenHeightDp2;
                    int i = screenLayout & 15;
                    if (longSizeDp < 470) {
                        screenLayoutSize = 1;
                    } else if (longSizeDp >= 960 && shortSizeDp >= 720) {
                        screenLayoutSize = 4;
                    } else if (longSizeDp < 640 || shortSizeDp < 480) {
                        screenLayoutSize = 2;
                    } else {
                        screenLayoutSize = 3;
                    }
                    this.mConfiguration.screenLayout = (screenLayout & -16) | screenLayoutSize;
                }
                Slog.i(TAG, "set to no compat config  to:" + this.mConfiguration);
            }
        }
    }

    public int calcConfigChanges(Configuration config) {
        if (config == null) {
            return -1;
        }
        this.mTmpConfig.setTo(config);
        int density = config.densityDpi;
        if (density == 0) {
            density = this.mMetrics.noncompatDensityDpi;
        }
        this.mDisplayAdjustments.getCompatibilityInfo().applyToConfiguration(density, this.mTmpConfig);
        if (this.mTmpConfig.getLocales().isEmpty()) {
            this.mTmpConfig.setLocales(LocaleList.getDefault());
        }
        return this.mConfiguration.updateFrom(this.mTmpConfig);
    }

    private static String adjustLanguageTag(String languageTag) {
        String remainder;
        String language;
        int separator = languageTag.indexOf(45);
        if (separator == -1) {
            language = languageTag;
            remainder = "";
        } else {
            language = languageTag.substring(0, separator);
            remainder = languageTag.substring(separator);
        }
        return Locale.adjustLanguageCode(language) + remainder;
    }

    public void flushLayoutCache() {
        synchronized (this.mCachedXmlBlocks) {
            Arrays.fill(this.mCachedXmlBlockCookies, 0);
            Arrays.fill(this.mCachedXmlBlockFiles, null);
            XmlBlock[] cachedXmlBlocks = this.mCachedXmlBlocks;
            for (int i = 0; i < 4; i++) {
                XmlBlock oldBlock = cachedXmlBlocks[i];
                if (oldBlock != null) {
                    oldBlock.close();
                }
            }
            Arrays.fill(cachedXmlBlocks, null);
        }
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:69:0x0111  */
    /* JADX WARNING: Removed duplicated region for block: B:74:0x011a A[Catch:{ Exception -> 0x0116 }] */
    /* JADX WARNING: Removed duplicated region for block: B:77:0x011f A[Catch:{ Exception -> 0x0116 }] */
    /* JADX WARNING: Removed duplicated region for block: B:89:0x0144 A[Catch:{ Exception -> 0x0116 }] */
    public Drawable loadDrawable(Resources wrapper, TypedValue value, int id, int density, Resources.Theme theme) throws Resources.NotFoundException {
        int i;
        String name;
        long key;
        DrawableCache caches;
        boolean isColorDrawable;
        long key2;
        long key3;
        Drawable.ConstantState cs;
        Drawable dr;
        boolean z;
        Drawable dr2;
        Drawable dr3;
        ResourcesImpl resourcesImpl = this;
        Resources resources = wrapper;
        TypedValue typedValue = value;
        int i2 = id;
        int i3 = density;
        Resources.Theme theme2 = theme;
        boolean canApplyTheme = true;
        boolean useCache = i3 == 0 || typedValue.density == resourcesImpl.mMetrics.densityDpi;
        if (i3 > 0 && typedValue.density > 0 && typedValue.density != 65535) {
            if (typedValue.density == i3) {
                typedValue.density = resourcesImpl.mMetrics.densityDpi;
            } else {
                typedValue.density = (typedValue.density * resourcesImpl.mMetrics.densityDpi) / i3;
            }
        }
        try {
            if (typedValue.type < 28 || typedValue.type > 31) {
                isColorDrawable = false;
                caches = resourcesImpl.mDrawableCache;
                key = (((long) typedValue.assetCookie) << 32) | ((long) typedValue.data);
            } else {
                isColorDrawable = true;
                caches = resourcesImpl.mColorDrawableCache;
                key = (long) typedValue.data;
            }
            boolean isColorDrawable2 = isColorDrawable;
            DrawableCache caches2 = caches;
            long key4 = key;
            if (!resourcesImpl.mPreloading && useCache) {
                Drawable cachedDrawable = caches2.getInstance(key4, resources, theme2);
                if (cachedDrawable != null) {
                    cachedDrawable.setChangingConfigurations(typedValue.changingConfigurations);
                    if (HwFrameworkFactory.getHwActivityThread() != null) {
                        HwFrameworkFactory.getHwActivityThread().hitDrawableCache(i2);
                    }
                    return cachedDrawable;
                }
            }
            if (resourcesImpl.mHwResourcesImpl != null) {
                key2 = key4;
                try {
                    Drawable hwDrawable = resourcesImpl.mHwResourcesImpl.loadDrawable(resources, typedValue, i2, theme2, useCache);
                    if (hwDrawable != null) {
                        return hwDrawable;
                    }
                } catch (Exception e) {
                    e = e;
                    resourcesImpl = this;
                    i = 0;
                    Exception e2 = e;
                    try {
                        name = resourcesImpl.getResourceName(i2);
                    } catch (Resources.NotFoundException e3) {
                        Resources.NotFoundException notFoundException = e3;
                        name = "(missing name)";
                    }
                    Resources.NotFoundException nfe = new Resources.NotFoundException("Drawable " + name + " with resource ID #0x" + Integer.toHexString(id), e2);
                    nfe.setStackTrace(new StackTraceElement[i]);
                    throw nfe;
                }
            } else {
                key2 = key4;
            }
            if (isColorDrawable2) {
                cs = sPreloadedColorDrawables.get(key2);
                key3 = key2;
                resourcesImpl = this;
            } else {
                key3 = key2;
                resourcesImpl = this;
                cs = sPreloadedDrawables[resourcesImpl.mConfiguration.getLayoutDirection()].get(key3);
            }
            Drawable.ConstantState cs2 = cs;
            if (resources != null) {
                resources.setResourceScaleOpt(true);
            }
            boolean needsNewDrawableAfterCache = false;
            if (cs2 != null) {
                if (TRACE_FOR_DETAILED_PRELOAD && (i2 >>> 24) == 1 && Process.myUid() != 0) {
                    if (resourcesImpl.getResourceName(i2) != null) {
                        Log.d(TAG_PRELOAD, "Hit preloaded FW drawable #" + Integer.toHexString(id) + " " + name);
                    }
                }
                dr = cs2.newDrawable(resources);
            } else if (isColorDrawable2) {
                dr = new ColorDrawable(typedValue.data);
            } else {
                dr = loadDrawableForCookie(wrapper, value, id, density);
                if (resources == null) {
                    z = false;
                    try {
                        resources.setResourceScaleOpt(false);
                    } catch (Exception e4) {
                        e = e4;
                        i = 0;
                        Exception e22 = e;
                        name = resourcesImpl.getResourceName(i2);
                        Resources.NotFoundException nfe2 = new Resources.NotFoundException("Drawable " + name + " with resource ID #0x" + Integer.toHexString(id), e22);
                        nfe2.setStackTrace(new StackTraceElement[i]);
                        throw nfe2;
                    }
                } else {
                    z = false;
                }
                if (dr instanceof DrawableContainer) {
                    needsNewDrawableAfterCache = true;
                }
                boolean needsNewDrawableAfterCache2 = needsNewDrawableAfterCache;
                if (dr != null || !dr.canApplyTheme()) {
                    canApplyTheme = z;
                }
                if (canApplyTheme && theme2 != null) {
                    dr = dr.mutate();
                    dr.applyTheme(theme2);
                    dr.clearMutated();
                }
                dr2 = resourcesImpl.mHwResourcesImpl.handleAddIconBackground(resources, i2, dr);
                if (dr2 != null) {
                    dr2.setChangingConfigurations(typedValue.changingConfigurations);
                    if (useCache) {
                        TypedValue typedValue2 = typedValue;
                        Drawable dr4 = dr2;
                        Drawable.ConstantState constantState = cs2;
                        DrawableCache drawableCache = caches2;
                        i = z;
                        try {
                            resourcesImpl.cacheDrawable(typedValue2, isColorDrawable2, caches2, theme2, canApplyTheme, key3, dr4);
                            if (needsNewDrawableAfterCache2) {
                                dr3 = dr4;
                                Drawable.ConstantState state = dr3.getConstantState();
                                if (state != null) {
                                    dr3 = state.newDrawable(resources);
                                }
                            } else {
                                dr3 = dr4;
                            }
                            return dr3;
                        } catch (Exception e5) {
                            e = e5;
                            Exception e222 = e;
                            name = resourcesImpl.getResourceName(i2);
                            Resources.NotFoundException nfe22 = new Resources.NotFoundException("Drawable " + name + " with resource ID #0x" + Integer.toHexString(id), e222);
                            nfe22.setStackTrace(new StackTraceElement[i]);
                            throw nfe22;
                        }
                    }
                }
                dr3 = dr2;
                Drawable.ConstantState constantState2 = cs2;
                DrawableCache drawableCache2 = caches2;
                long j = key3;
                return dr3;
            }
            if (resources == null) {
            }
            if (dr instanceof DrawableContainer) {
            }
            boolean needsNewDrawableAfterCache22 = needsNewDrawableAfterCache;
            if (dr != null) {
            }
            canApplyTheme = z;
            dr = dr.mutate();
            dr.applyTheme(theme2);
            dr.clearMutated();
            dr2 = resourcesImpl.mHwResourcesImpl.handleAddIconBackground(resources, i2, dr);
            if (dr2 != null) {
            }
            dr3 = dr2;
            Drawable.ConstantState constantState22 = cs2;
            DrawableCache drawableCache22 = caches2;
            long j2 = key3;
            return dr3;
        } catch (Exception e6) {
            e = e6;
            i = 0;
            Exception e2222 = e;
            name = resourcesImpl.getResourceName(i2);
            Resources.NotFoundException nfe222 = new Resources.NotFoundException("Drawable " + name + " with resource ID #0x" + Integer.toHexString(id), e2222);
            nfe222.setStackTrace(new StackTraceElement[i]);
            throw nfe222;
        }
    }

    private void cacheDrawable(TypedValue value, boolean isColorDrawable, DrawableCache caches, Resources.Theme theme, boolean usesTheme, long key, Drawable dr) {
        TypedValue typedValue = value;
        long j = key;
        Drawable.ConstantState cs = dr.getConstantState();
        if (cs != null) {
            if (this.mPreloading) {
                int changingConfigs = cs.getChangingConfigurations();
                if (isColorDrawable) {
                    if (verifyPreloadConfig(changingConfigs, 0, typedValue.resourceId, "drawable")) {
                        sPreloadedColorDrawables.put(j, cs);
                    }
                } else if (verifyPreloadConfig(changingConfigs, 8192, typedValue.resourceId, "drawable")) {
                    if ((changingConfigs & 8192) == 0) {
                        sPreloadedDrawables[0].put(j, cs);
                        sPreloadedDrawables[1].put(j, cs);
                    } else {
                        sPreloadedDrawables[this.mConfiguration.getLayoutDirection()].put(j, cs);
                    }
                }
            } else {
                synchronized (this.mAccessLock) {
                    caches.put(j, theme, cs, usesTheme);
                }
            }
        }
    }

    private boolean verifyPreloadConfig(int changingConfigurations, int allowVarying, int resourceId, String name) {
        String resName;
        if (((~(CONFIG_FONT_SCALE | CONFIG_DENSITY | 1073741824 | CONFIG_SMALLEST_SCREEN_SIZE | 1024 | CONFIG_ORIENTATION)) & changingConfigurations & (~allowVarying)) == 0) {
            return true;
        }
        try {
            resName = getResourceName(resourceId);
        } catch (Resources.NotFoundException e) {
            resName = "?";
        }
        Log.w(TAG, "Preloaded " + name + " resource #0x" + Integer.toHexString(resourceId) + " (" + resName + ") that varies with configuration!!");
        return false;
    }

    private Drawable decodeImageDrawable(AssetManager.AssetInputStream ais, Resources wrapper, TypedValue value) {
        try {
            return ImageDecoder.decodeDrawable(new ImageDecoder.AssetInputStreamSource(ais, wrapper, value), $$Lambda$ResourcesImpl$99dm2ENnzo9b0SIUjUj2Kl3pi90.INSTANCE);
        } catch (IOException e) {
            return null;
        }
    }

    private Drawable loadDrawableForCookie(Resources wrapper, TypedValue value, int id, int density) {
        LookupStack stack;
        Drawable dr;
        String str;
        Resources resources = wrapper;
        TypedValue typedValue = value;
        int i = id;
        if (typedValue.string != null) {
            if (HwFrameworkFactory.getHwActivityThread() != null) {
                Drawable drCache = HwFrameworkFactory.getHwActivityThread().getCacheDrawableFromAware(i, resources, typedValue.assetCookie, this.mAssets);
                if (drCache != null) {
                    drCache.setChangingConfigurations(typedValue.changingConfigurations);
                    return drCache;
                }
            }
            String file = typedValue.string.toString();
            long startTime = System.nanoTime();
            int startBitmapCount = 0;
            long startBitmapSize = 0;
            int startDrawableCount = 0;
            if (TRACE_FOR_DETAILED_PRELOAD) {
                startBitmapCount = Bitmap.sPreloadTracingNumInstantiatedBitmaps;
                startBitmapSize = Bitmap.sPreloadTracingTotalBitmapsSize;
                startDrawableCount = sPreloadTracingNumLoadedDrawables;
            }
            int startBitmapCount2 = startBitmapCount;
            long startBitmapSize2 = startBitmapSize;
            int startDrawableCount2 = startDrawableCount;
            Trace.traceBegin(8192, file);
            LookupStack stack2 = this.mLookupStack.get();
            try {
                if (!stack2.contains(i)) {
                    stack2.push(i);
                    try {
                        if (file.endsWith(".xml")) {
                            try {
                                XmlResourceParser rp = loadXmlResourceParser(file, i, typedValue.assetCookie, "drawable");
                                try {
                                    Drawable dr2 = Drawable.createFromXmlForDensity(resources, rp, density, null);
                                    rp.close();
                                    dr = dr2;
                                } catch (Throwable th) {
                                    th = th;
                                    stack = stack2;
                                    try {
                                        stack.pop();
                                        throw th;
                                    } catch (Exception | StackOverflowError e) {
                                        e = e;
                                        Trace.traceEnd(8192);
                                        Resources.NotFoundException rnf = new Resources.NotFoundException("File " + file + " from drawable resource ID #0x" + Integer.toHexString(id));
                                        rnf.initCause(e);
                                        throw rnf;
                                    }
                                }
                            } catch (Throwable th2) {
                                th = th2;
                                int i2 = density;
                                stack = stack2;
                                stack.pop();
                                throw th;
                            }
                        } else {
                            int i3 = density;
                            InputStream is = this.mAssets.openNonAsset(typedValue.assetCookie, file, 2);
                            dr = HwPCUtils.isValidExtDisplayId(HwPCUtils.getPCDisplayID()) ? Drawable.createFromResourceStream(resources, typedValue, is, file) : decodeImageDrawable((AssetManager.AssetInputStream) is, resources, typedValue);
                        }
                        Drawable dr3 = dr;
                        stack2.pop();
                        Trace.traceEnd(8192);
                        long time = System.nanoTime() - startTime;
                        if (HwFrameworkFactory.getHwActivityThread() != null) {
                            LookupStack lookupStack = stack2;
                            HwFrameworkFactory.getHwActivityThread().postCacheDrawableToAware(i, resources, time, typedValue.assetCookie, this.mAssets);
                        }
                        if (TRACE_FOR_DETAILED_PRELOAD) {
                            boolean isRoot = true;
                            if ((i >>> 24) == 1) {
                                String name = getResourceName(i);
                                if (name != null) {
                                    int loadedBitmapCount = Bitmap.sPreloadTracingNumInstantiatedBitmaps - startBitmapCount2;
                                    long loadedBitmapSize = Bitmap.sPreloadTracingTotalBitmapsSize - startBitmapSize2;
                                    int loadedDrawables = sPreloadTracingNumLoadedDrawables - startDrawableCount2;
                                    sPreloadTracingNumLoadedDrawables++;
                                    if (Process.myUid() != 0) {
                                        isRoot = false;
                                    }
                                    long j = startTime;
                                    StringBuilder sb = new StringBuilder();
                                    if (isRoot) {
                                        str = "Preloaded FW drawable #";
                                    } else {
                                        str = "Loaded non-preloaded FW drawable #";
                                    }
                                    sb.append(str);
                                    sb.append(Integer.toHexString(id));
                                    sb.append(" ");
                                    sb.append(name);
                                    sb.append(" ");
                                    sb.append(file);
                                    sb.append(" ");
                                    sb.append(dr3.getClass().getCanonicalName());
                                    sb.append(" #nested_drawables= ");
                                    sb.append(loadedDrawables);
                                    sb.append(" #bitmaps= ");
                                    sb.append(loadedBitmapCount);
                                    sb.append(" total_bitmap_size= ");
                                    sb.append(loadedBitmapSize);
                                    sb.append(" in[us] ");
                                    String str2 = name;
                                    boolean z = isRoot;
                                    sb.append(time / 1000);
                                    Log.d(TAG_PRELOAD, sb.toString());
                                    return dr3;
                                }
                            }
                        }
                        return dr3;
                    } catch (Throwable th3) {
                        th = th3;
                        stack = stack2;
                        long j2 = startTime;
                        stack.pop();
                        throw th;
                    }
                } else {
                    long j3 = startTime;
                    throw new Exception("Recursive reference in drawable");
                }
            } catch (Exception | StackOverflowError e2) {
                e = e2;
                LookupStack lookupStack2 = stack2;
                long j4 = startTime;
                Trace.traceEnd(8192);
                Resources.NotFoundException rnf2 = new Resources.NotFoundException("File " + file + " from drawable resource ID #0x" + Integer.toHexString(id));
                rnf2.initCause(e);
                throw rnf2;
            }
        } else {
            throw new Resources.NotFoundException("Resource \"" + getResourceName(i) + "\" (" + Integer.toHexString(id) + ") is not a Drawable (color or path): " + typedValue);
        }
    }

    public Typeface loadFont(Resources wrapper, TypedValue value, int id) {
        if (value.string != null) {
            String file = value.string.toString();
            if (!file.startsWith("res/")) {
                return null;
            }
            Typeface cached = Typeface.findFromCache(this.mAssets, file);
            if (cached != null) {
                return cached;
            }
            Trace.traceBegin(8192, file);
            try {
                if (file.endsWith("xml")) {
                    FontResourcesParser.FamilyResourceEntry familyEntry = FontResourcesParser.parse(loadXmlResourceParser(file, id, value.assetCookie, "font"), wrapper);
                    if (familyEntry == null) {
                        Trace.traceEnd(8192);
                        return null;
                    }
                    Typeface createFromResources = Typeface.createFromResources(familyEntry, this.mAssets, file);
                    Trace.traceEnd(8192);
                    return createFromResources;
                }
                Typeface createFromResources2 = Typeface.createFromResources(this.mAssets, file, value.assetCookie);
                Trace.traceEnd(8192);
                return createFromResources2;
            } catch (XmlPullParserException e) {
                Log.e(TAG, "Failed to parse xml resource " + file, e);
            } catch (IOException e2) {
                Log.e(TAG, "Failed to read xml resource " + file, e2);
            } catch (Throwable th) {
                Trace.traceEnd(8192);
                throw th;
            }
        } else {
            throw new Resources.NotFoundException("Resource \"" + getResourceName(id) + "\" (" + Integer.toHexString(id) + ") is not a Font: " + value);
        }
        Trace.traceEnd(8192);
        return null;
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v6, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v5, resolved type: android.content.res.ComplexColor} */
    /* JADX WARNING: Multi-variable type inference failed */
    private ComplexColor loadComplexColorFromName(Resources wrapper, Resources.Theme theme, TypedValue value, int id) {
        long key = (((long) value.assetCookie) << 32) | ((long) value.data);
        ConfigurationBoundResourceCache<ComplexColor> cache = this.mComplexColorCache;
        ComplexColor complexColor = cache.getInstance(key, wrapper, theme);
        if (complexColor != null) {
            return complexColor;
        }
        ConstantState<ComplexColor> factory = sPreloadedComplexColors.get(key);
        if (factory != null) {
            complexColor = factory.newInstance(wrapper, theme);
        }
        if (complexColor == null) {
            complexColor = loadComplexColorForCookie(wrapper, value, id, theme);
        }
        if (complexColor != null) {
            complexColor.setBaseChangingConfigurations(value.changingConfigurations);
            if (!this.mPreloading) {
                cache.put(key, theme, complexColor.getConstantState());
            } else if (verifyPreloadConfig(complexColor.getChangingConfigurations(), 0, value.resourceId, Slice.SUBTYPE_COLOR)) {
                sPreloadedComplexColors.put(key, complexColor.getConstantState());
            }
        }
        return complexColor;
    }

    /* access modifiers changed from: package-private */
    public ComplexColor loadComplexColor(Resources wrapper, TypedValue value, int id, Resources.Theme theme) {
        long key = (((long) value.assetCookie) << 32) | ((long) value.data);
        if (value.type >= 28 && value.type <= 31) {
            return getColorStateListFromInt(value, key);
        }
        String file = value.string.toString();
        if (file.endsWith(".xml")) {
            try {
                return loadComplexColorFromName(wrapper, theme, value, id);
            } catch (Exception e) {
                Resources.NotFoundException rnf = new Resources.NotFoundException("File " + file + " from complex color resource ID #0x" + Integer.toHexString(id));
                rnf.initCause(e);
                throw rnf;
            }
        } else {
            throw new Resources.NotFoundException("File " + file + " from drawable resource ID #0x" + Integer.toHexString(id) + ": .xml extension required");
        }
    }

    /* access modifiers changed from: protected */
    public ColorStateList loadColorStateList(Resources wrapper, TypedValue value, int id, Resources.Theme theme) throws Resources.NotFoundException {
        long key = (((long) value.assetCookie) << 32) | ((long) value.data);
        if (value.type >= 28 && value.type <= 31) {
            return getColorStateListFromInt(value, key);
        }
        ComplexColor complexColor = loadComplexColorFromName(wrapper, theme, value, id);
        if (complexColor != null && (complexColor instanceof ColorStateList)) {
            return (ColorStateList) complexColor;
        }
        throw new Resources.NotFoundException("Can't find ColorStateList from drawable resource ID #0x" + Integer.toHexString(id));
    }

    private ColorStateList getColorStateListFromInt(TypedValue value, long key) {
        ConstantState<ComplexColor> factory = sPreloadedComplexColors.get(key);
        if (factory != null) {
            return (ColorStateList) factory.newInstance();
        }
        ColorStateList csl = ColorStateList.valueOf(value.data);
        if (this.mPreloading && verifyPreloadConfig(value.changingConfigurations, 0, value.resourceId, Slice.SUBTYPE_COLOR)) {
            sPreloadedComplexColors.put(key, csl.getConstantState());
        }
        return csl;
    }

    /* JADX WARNING: Removed duplicated region for block: B:12:0x0032 A[Catch:{ Exception -> 0x0062 }] */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x005a A[SYNTHETIC, Splitter:B:21:0x005a] */
    private ComplexColor loadComplexColorForCookie(Resources wrapper, TypedValue value, int id, Resources.Theme theme) {
        int type;
        if (value.string != null) {
            String file = value.string.toString();
            ComplexColor complexColor = null;
            Trace.traceBegin(8192, file);
            if (file.endsWith(".xml")) {
                try {
                    XmlResourceParser parser = loadXmlResourceParser(file, id, value.assetCookie, "ComplexColor");
                    AttributeSet attrs = Xml.asAttributeSet(parser);
                    while (true) {
                        int next = parser.next();
                        type = next;
                        if (next == 2 || type == 1) {
                            if (type != 2) {
                                String name = parser.getName();
                                if (name.equals("gradient")) {
                                    complexColor = GradientColor.createFromXmlInner(wrapper, parser, attrs, theme);
                                } else if (name.equals("selector")) {
                                    complexColor = ColorStateList.createFromXmlInner(wrapper, parser, attrs, theme);
                                }
                                parser.close();
                                Trace.traceEnd(8192);
                                return complexColor;
                            }
                            throw new XmlPullParserException("No start tag found");
                        }
                    }
                    if (type != 2) {
                    }
                } catch (Exception e) {
                    Trace.traceEnd(8192);
                    Resources.NotFoundException rnf = new Resources.NotFoundException("File " + file + " from ComplexColor resource ID #0x" + Integer.toHexString(id));
                    rnf.initCause(e);
                    throw rnf;
                }
            } else {
                Trace.traceEnd(8192);
                throw new Resources.NotFoundException("File " + file + " from drawable resource ID #0x" + Integer.toHexString(id) + ": .xml extension required");
            }
        } else {
            throw new UnsupportedOperationException("Can't convert to ComplexColor: type=0x" + value.type);
        }
    }

    /* access modifiers changed from: package-private */
    public XmlResourceParser loadXmlResourceParser(String file, int id, int assetCookie, String type) throws Resources.NotFoundException {
        if (id != 0) {
            try {
                synchronized (this.mCachedXmlBlocks) {
                    int[] cachedXmlBlockCookies = this.mCachedXmlBlockCookies;
                    String[] cachedXmlBlockFiles = this.mCachedXmlBlockFiles;
                    XmlBlock[] cachedXmlBlocks = this.mCachedXmlBlocks;
                    int num = cachedXmlBlockFiles.length;
                    int i = 0;
                    while (i < num) {
                        if (cachedXmlBlockCookies[i] != assetCookie || cachedXmlBlockFiles[i] == null || !cachedXmlBlockFiles[i].equals(file)) {
                            i++;
                        } else {
                            XmlResourceParser newParser = cachedXmlBlocks[i].newParser();
                            return newParser;
                        }
                    }
                    XmlBlock block = this.mAssets.openXmlBlockAsset(assetCookie, file);
                    if (block != null) {
                        int pos = (this.mLastCachedXmlBlockIndex + 1) % num;
                        this.mLastCachedXmlBlockIndex = pos;
                        XmlBlock oldBlock = cachedXmlBlocks[pos];
                        if (oldBlock != null) {
                            oldBlock.close();
                        }
                        cachedXmlBlockCookies[pos] = assetCookie;
                        cachedXmlBlockFiles[pos] = file;
                        cachedXmlBlocks[pos] = block;
                        XmlResourceParser newParser2 = block.newParser();
                        return newParser2;
                    }
                }
            } catch (Exception e) {
                Resources.NotFoundException rnf = new Resources.NotFoundException("File " + file + " from xml type " + type + " resource ID #0x" + Integer.toHexString(id));
                rnf.initCause(e);
                throw rnf;
            }
        }
        throw new Resources.NotFoundException("File " + file + " from xml type " + type + " resource ID #0x" + Integer.toHexString(id));
    }

    public final void startPreloading() {
        synchronized (sSync) {
            if (!sPreloaded) {
                sPreloaded = true;
                this.mPreloading = true;
                sPreloadedDensity = DisplayMetrics.DENSITY_DEVICE;
                this.mConfiguration.densityDpi = sPreloadedDensity;
                updateConfiguration(null, null, null);
                if (TRACE_FOR_DETAILED_PRELOAD) {
                    this.mPreloadTracingPreloadStartTime = SystemClock.uptimeMillis();
                    this.mPreloadTracingStartBitmapSize = Bitmap.sPreloadTracingTotalBitmapsSize;
                    this.mPreloadTracingStartBitmapCount = (long) Bitmap.sPreloadTracingNumInstantiatedBitmaps;
                    Log.d(TAG_PRELOAD, "Preload starting");
                }
            } else {
                throw new IllegalStateException("Resources already preloaded");
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void finishPreloading() {
        if (this.mPreloading) {
            if (TRACE_FOR_DETAILED_PRELOAD) {
                long time = SystemClock.uptimeMillis() - this.mPreloadTracingPreloadStartTime;
                long size = Bitmap.sPreloadTracingTotalBitmapsSize - this.mPreloadTracingStartBitmapSize;
                Log.d(TAG_PRELOAD, "Preload finished, " + (((long) Bitmap.sPreloadTracingNumInstantiatedBitmaps) - this.mPreloadTracingStartBitmapCount) + " bitmaps of " + size + " bytes in " + time + " ms");
            }
            this.mPreloading = false;
            flushLayoutCache();
        }
    }

    /* access modifiers changed from: package-private */
    public LongSparseArray<Drawable.ConstantState> getPreloadedDrawables() {
        return sPreloadedDrawables[0];
    }

    /* access modifiers changed from: package-private */
    public ThemeImpl newThemeImpl() {
        return new ThemeImpl();
    }

    /* access modifiers changed from: package-private */
    public ThemeImpl newThemeImpl(Resources.ThemeKey key) {
        ThemeImpl impl = new ThemeImpl();
        impl.mKey.setTo(key);
        impl.rebase();
        return impl;
    }
}
