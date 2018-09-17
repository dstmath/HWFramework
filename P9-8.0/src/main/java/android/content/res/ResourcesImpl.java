package android.content.res;

import android.animation.Animator;
import android.animation.StateListAnimator;
import android.app.ActivityThread;
import android.content.pm.ActivityInfo;
import android.content.res.FontResourcesParser.FamilyResourceEntry;
import android.content.res.Resources.NotFoundException;
import android.content.res.Resources.Theme;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Drawable.ConstantState;
import android.graphics.drawable.DrawableContainer;
import android.hardware.camera2.params.TonemapCurve;
import android.hwcontrol.HwWidgetFactory;
import android.hwtheme.HwThemeManager;
import android.icu.text.PluralRules;
import android.net.ProxyInfo;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.LocaleList;
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
import huawei.android.animation.HwStateListAnimator;
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
    private static final boolean TRACE_FOR_MISS_PRELOAD = false;
    private static final boolean TRACE_FOR_PRELOAD = false;
    private static final int XML_BLOCK_CACHE_SIZE = 4;
    public static final boolean mIsLockResWhitelist = SystemProperties.getBoolean("ro.config.hw_lock_res_whitelist", false);
    public static final int mPadDpi2 = SystemProperties.getInt("ro.config.new_dpi_for_pad", 0);
    private static boolean sPreloaded;
    private static final LongSparseArray<ConstantState> sPreloadedColorDrawables = new LongSparseArray();
    private static final LongSparseArray<ConstantState<ComplexColor>> sPreloadedComplexColors = new LongSparseArray();
    private static int sPreloadedDensity;
    private static final LongSparseArray<ConstantState>[] sPreloadedDrawables = new LongSparseArray[2];
    private static final Object sSync = new Object();
    private final Object mAccessLock = new Object();
    private final ConfigurationBoundResourceCache<Animator> mAnimatorCache = new ConfigurationBoundResourceCache();
    final AssetManager mAssets;
    private final int[] mCachedXmlBlockCookies = new int[4];
    private final String[] mCachedXmlBlockFiles = new String[4];
    private final XmlBlock[] mCachedXmlBlocks = new XmlBlock[4];
    private final DrawableCache mColorDrawableCache = new DrawableCache();
    private final ConfigurationBoundResourceCache<ComplexColor> mComplexColorCache = new ConfigurationBoundResourceCache();
    private final Configuration mConfiguration = new Configuration();
    private final DisplayAdjustments mDisplayAdjustments;
    private final DrawableCache mDrawableCache = new DrawableCache();
    private AbsResourcesImpl mHwResourcesImpl = HwThemeManager.getHwResourcesImpl();
    private final ConfigurationBoundResourceCache<HwStateListAnimator> mHwStateListAnimatorCache = new ConfigurationBoundResourceCache();
    private int mLastCachedXmlBlockIndex = -1;
    private boolean mLockDpi = false;
    private boolean mLockRes = true;
    private final ThreadLocal<LookupStack> mLookupStack = ThreadLocal.withInitial(new -$Lambda$s0O-nf1GRGlu9U9Grxb4QL6yOfw());
    private final DisplayMetrics mMetrics = new DisplayMetrics();
    private PluralRules mPluralRule;
    public boolean mPreloading;
    private final ConfigurationBoundResourceCache<StateListAnimator> mStateListAnimatorCache = new ConfigurationBoundResourceCache();
    private final Configuration mTmpConfig = new Configuration();

    private static class LookupStack {
        private int[] mIds;
        private int mSize;

        /* synthetic */ LookupStack(LookupStack -this0) {
            this();
        }

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
        private final ThemeKey mKey = new ThemeKey();
        private final long mTheme;
        private int mThemeResId = 0;

        ThemeImpl() {
            this.mAssets = ResourcesImpl.this.mAssets;
            this.mTheme = this.mAssets.createTheme();
        }

        protected void finalize() throws Throwable {
            super.finalize();
            this.mAssets.releaseTheme(this.mTheme);
        }

        ThemeKey getKey() {
            return this.mKey;
        }

        long getNativeTheme() {
            return this.mTheme;
        }

        int getAppliedStyleResId() {
            return this.mThemeResId;
        }

        void applyStyle(int resId, boolean force) {
            synchronized (this.mKey) {
                AssetManager.applyThemeStyle(this.mTheme, HwWidgetFactory.getHuaweiRealTheme(resId), force);
                this.mThemeResId = resId;
                this.mKey.append(resId, force);
            }
        }

        void setTo(ThemeImpl other) {
            synchronized (this.mKey) {
                synchronized (other.mKey) {
                    AssetManager.copyTheme(this.mTheme, other.mTheme);
                    this.mThemeResId = other.mThemeResId;
                    this.mKey.setTo(other.getKey());
                }
            }
        }

        TypedArray obtainStyledAttributes(Theme wrapper, AttributeSet set, int[] attrs, int defStyleAttr, int defStyleRes) {
            TypedArray array;
            synchronized (this.mKey) {
                array = TypedArray.obtain(wrapper.getResources(), attrs.length);
                Parser parser = (Parser) set;
                AssetManager.applyStyle(this.mTheme, defStyleAttr, defStyleRes, parser != null ? parser.mParseState : 0, attrs, attrs.length, array.mDataAddress, array.mIndicesAddress);
                array.mTheme = wrapper;
                array.mXml = parser;
            }
            return array;
        }

        TypedArray resolveAttributes(Theme wrapper, int[] values, int[] attrs) {
            TypedArray array;
            synchronized (this.mKey) {
                int len = attrs.length;
                if (values == null || len != values.length) {
                    throw new IllegalArgumentException("Base attribute values must the same length as attrs");
                }
                array = TypedArray.obtain(wrapper.getResources(), len);
                AssetManager.resolveAttrs(this.mTheme, 0, 0, values, attrs, array.mData, array.mIndices);
                array.mTheme = wrapper;
                array.mXml = null;
            }
            return array;
        }

        boolean resolveAttribute(int resid, TypedValue outValue, boolean resolveRefs) {
            boolean themeValue;
            synchronized (this.mKey) {
                themeValue = this.mAssets.getThemeValue(this.mTheme, resid, outValue, resolveRefs);
            }
            return themeValue;
        }

        int[] getAllAttributes() {
            return this.mAssets.getStyleAttributes(getAppliedStyleResId());
        }

        int getChangingConfigurations() {
            int activityInfoConfigNativeToJava;
            synchronized (this.mKey) {
                activityInfoConfigNativeToJava = ActivityInfo.activityInfoConfigNativeToJava(AssetManager.getThemeChangingConfigurations(this.mTheme));
            }
            return activityInfoConfigNativeToJava;
        }

        public void dump(int priority, String tag, String prefix) {
            synchronized (this.mKey) {
                AssetManager.dumpTheme(this.mTheme, priority, tag, prefix);
            }
        }

        String[] getTheme() {
            String[] themes;
            synchronized (this.mKey) {
                int N = this.mKey.mCount;
                themes = new String[(N * 2)];
                int i = 0;
                int j = N - 1;
                while (i < themes.length) {
                    String str;
                    int resId = this.mKey.mResId[j];
                    boolean forced = this.mKey.mForce[j];
                    try {
                        themes[i] = ResourcesImpl.this.getResourceName(resId);
                    } catch (NotFoundException e) {
                        themes[i] = Integer.toHexString(i);
                    }
                    int i2 = i + 1;
                    if (forced) {
                        str = "forced";
                    } else {
                        str = "not forced";
                    }
                    themes[i2] = str;
                    i += 2;
                    j--;
                }
            }
            return themes;
        }

        void rebase() {
            synchronized (this.mKey) {
                AssetManager.clearTheme(this.mTheme);
                for (int i = 0; i < this.mKey.mCount; i++) {
                    AssetManager.applyThemeStyle(this.mTheme, this.mKey.mResId[i], this.mKey.mForce[i]);
                }
            }
        }
    }

    static {
        sPreloadedDrawables[0] = new LongSparseArray();
        sPreloadedDrawables[1] = new LongSparseArray();
    }

    public ResourcesImpl(AssetManager assets, DisplayMetrics metrics, Configuration config, DisplayAdjustments displayAdjustments) {
        boolean z = false;
        this.mHwResourcesImpl.setResourcesImpl(this);
        this.mHwResourcesImpl.setHwTheme(config);
        this.mAssets = assets;
        this.mMetrics.setToDefaults();
        this.mDisplayAdjustments = displayAdjustments;
        this.mConfiguration.setToDefaults();
        updateConfiguration(config, metrics, displayAdjustments.getCompatibilityInfo());
        this.mAssets.ensureStringBlocks();
        if (HwPCUtils.enabled() && ActivityThread.currentActivityThread() != null) {
            z = HwPCUtils.isValidExtDisplayId(ActivityThread.currentActivityThread().getDisplayId());
        }
        if (!z) {
            this.mHwResourcesImpl.initDeepTheme();
        }
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

    public DisplayMetrics getDisplayMetrics() {
        return this.mMetrics;
    }

    Configuration getConfiguration() {
        return this.mConfiguration;
    }

    Configuration[] getSizeConfigurations() {
        return this.mAssets.getSizeConfigurations();
    }

    CompatibilityInfo getCompatibilityInfo() {
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

    void getValue(int id, TypedValue outValue, boolean resolveRefs) throws NotFoundException {
        if (!this.mAssets.getResourceValue(id, 0, outValue, resolveRefs)) {
            throw new NotFoundException("Resource ID #0x" + Integer.toHexString(id));
        }
    }

    void getValueForDensity(int id, int density, TypedValue outValue, boolean resolveRefs) throws NotFoundException {
        if (!this.mAssets.getResourceValue(id, density, outValue, resolveRefs)) {
            throw new NotFoundException("Resource ID #0x" + Integer.toHexString(id));
        }
    }

    void getValue(String name, TypedValue outValue, boolean resolveRefs) throws NotFoundException {
        int id = getIdentifier(name, "string", null);
        if (id != 0) {
            getValue(id, outValue, resolveRefs);
            return;
        }
        throw new NotFoundException("String resource name " + name);
    }

    int getIdentifier(String name, String defType, String defPackage) {
        if (name == null) {
            throw new NullPointerException("name is null");
        }
        try {
            return Integer.parseInt(name);
        } catch (Exception e) {
            return this.mAssets.getResourceIdentifier(name, defType, defPackage);
        }
    }

    String getResourceName(int resid) throws NotFoundException {
        String str = this.mAssets.getResourceName(resid);
        if (str != null) {
            return str;
        }
        throw new NotFoundException("Unable to find resource ID #0x" + Integer.toHexString(resid));
    }

    String getResourcePackageName(int resid) throws NotFoundException {
        String str = this.mAssets.getResourcePackageName(resid);
        if (str != null) {
            return str;
        }
        throw new NotFoundException("Unable to find resource ID #0x" + Integer.toHexString(resid));
    }

    String getResourceTypeName(int resid) throws NotFoundException {
        String str = this.mAssets.getResourceTypeName(resid);
        if (str != null) {
            return str;
        }
        throw new NotFoundException("Unable to find resource ID #0x" + Integer.toHexString(resid));
    }

    String getResourceEntryName(int resid) throws NotFoundException {
        String str = this.mAssets.getResourceEntryName(resid);
        if (str != null) {
            return str;
        }
        throw new NotFoundException("Unable to find resource ID #0x" + Integer.toHexString(resid));
    }

    CharSequence getQuantityText(int id, int quantity) throws NotFoundException {
        PluralRules rule = getPluralRule();
        CharSequence res = this.mAssets.getResourceBagText(id, attrForQuantityCode(rule.select((double) quantity)));
        if (res != null) {
            return res;
        }
        res = this.mAssets.getResourceBagText(id, ID_OTHER);
        if (res != null) {
            return res;
        }
        throw new NotFoundException("Plural resource ID #0x" + Integer.toHexString(id) + " quantity=" + quantity + " item=" + rule.select((double) quantity));
    }

    private static int attrForQuantityCode(String quantityCode) {
        if (quantityCode.equals("zero")) {
            return 16777221;
        }
        if (quantityCode.equals("one")) {
            return 16777222;
        }
        if (quantityCode.equals("two")) {
            return 16777223;
        }
        if (quantityCode.equals("few")) {
            return 16777224;
        }
        if (quantityCode.equals("many")) {
            return 16777225;
        }
        return ID_OTHER;
    }

    AssetFileDescriptor openRawResourceFd(int id, TypedValue tempValue) throws NotFoundException {
        getValue(id, tempValue, true);
        try {
            return this.mAssets.openNonAssetFd(tempValue.assetCookie, tempValue.string.toString());
        } catch (Exception e) {
            throw new NotFoundException("File " + tempValue.string.toString() + " from drawable " + "resource ID #0x" + Integer.toHexString(id), e);
        }
    }

    InputStream openRawResource(int id, TypedValue value) throws NotFoundException {
        getValue(id, value, true);
        try {
            return this.mAssets.openNonAsset(value.assetCookie, value.string.toString(), 2);
        } catch (Exception e) {
            NotFoundException rnf = new NotFoundException("File " + (value.string == null ? "(null)" : value.string.toString()) + " from drawable resource ID #0x" + Integer.toHexString(id));
            rnf.initCause(e);
            throw rnf;
        }
    }

    ConfigurationBoundResourceCache<Animator> getAnimatorCache() {
        return this.mAnimatorCache;
    }

    ConfigurationBoundResourceCache<StateListAnimator> getStateListAnimatorCache() {
        return this.mStateListAnimatorCache;
    }

    /* JADX WARNING: Missing block: B:15:0x0054, code:
            if (((android.app.ActivityThread.currentActivityThread() != null ? android.util.HwPCUtils.isValidExtDisplayId(android.app.ActivityThread.currentActivityThread().getDisplayId()) : 0) ^ 1) != 0) goto L_0x0056;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void updateConfiguration(Configuration config, DisplayMetrics metrics, CompatibilityInfo compat) {
        Trace.traceBegin(8192, "ResourcesImpl#updateConfiguration");
        try {
            synchronized (this.mAccessLock) {
                if (compat != null) {
                    this.mDisplayAdjustments.setCompatibilityInfo(compat);
                }
                if (metrics != null) {
                    this.mMetrics.setTo(metrics);
                }
                this.mDisplayAdjustments.getCompatibilityInfo().applyToDisplayMetrics(this.mMetrics);
                if (!HwPCUtils.enabledInPad()) {
                }
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
                        if (LocaleList.isPseudoLocalesOnly(availableLocales) || availableLocales.length == 1) {
                            availableLocales = this.mAssets.getLocales();
                            if (LocaleList.isPseudoLocalesOnly(availableLocales)) {
                                availableLocales = null;
                            }
                        }
                        if (availableLocales != null) {
                            boolean hasDbid = false;
                            for (String oneLocale : availableLocales) {
                                if (oneLocale.equals("zz-ZX")) {
                                    hasDbid = true;
                                    break;
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
                if (!(this.mConfiguration.densityDpi == 0 || (this.mLockDpi ^ 1) == 0)) {
                    this.mMetrics.densityDpi = this.mConfiguration.densityDpi;
                    this.mMetrics.density = ((float) this.mConfiguration.densityDpi) * 0.00625f;
                }
                this.mMetrics.scaledDensity = (this.mConfiguration.fontScale != TonemapCurve.LEVEL_BLACK ? this.mConfiguration.fontScale : 1.0f) * this.mMetrics.density;
                updateDpiConfiguration();
                updateAssetConfiguration(ActivityThread.currentPackageName(), true, changeRes);
                this.mDrawableCache.onConfigurationChange(configChanges);
                this.mColorDrawableCache.onConfigurationChange(configChanges);
                this.mComplexColorCache.onConfigurationChange(configChanges);
                this.mAnimatorCache.onConfigurationChange(configChanges);
                this.mStateListAnimatorCache.onConfigurationChange(configChanges);
                this.mHwStateListAnimatorCache.onConfigurationChange(configChanges);
                flushLayoutCache();
                this.mHwResourcesImpl.updateConfiguration(config, configChanges);
            }
            synchronized (sSync) {
                if (this.mPluralRule != null) {
                    this.mPluralRule = PluralRules.forLocale(this.mConfiguration.getLocales().get(0));
                }
            }
            Trace.traceEnd(8192);
        } catch (Throwable th) {
            Trace.traceEnd(8192);
        }
    }

    public void updateAssetConfiguration(String packageName, boolean ResFlag, boolean changeRes) {
        int width;
        int height;
        int keyboardHidden;
        if (this.mMetrics.widthPixels >= this.mMetrics.heightPixels) {
            width = this.mMetrics.widthPixels;
            height = this.mMetrics.heightPixels;
        } else {
            width = this.mMetrics.heightPixels;
            height = this.mMetrics.widthPixels;
        }
        if (this.mConfiguration.keyboardHidden == 1 && this.mConfiguration.hardKeyboardHidden == 2) {
            keyboardHidden = 3;
        } else {
            keyboardHidden = this.mConfiguration.keyboardHidden;
        }
        int smallestScreenWidthDp = this.mConfiguration.smallestScreenWidthDp;
        int screenWidthDp = this.mConfiguration.screenWidthDp;
        int screenHeightDp = this.mConfiguration.screenHeightDp;
        int densityDpi = this.mConfiguration.densityDpi;
        if (!this.mLockDpi && ((mIsLockResWhitelist && getHwResourcesImpl().isInMultiDpiWhiteList(packageName)) || (!mIsLockResWhitelist && this.mLockRes))) {
            int srcDpi = SystemProperties.getInt("ro.sf.real_lcd_density", SystemProperties.getInt("ro.sf.lcd_density", 0));
            int realDpi = SystemProperties.getInt("persist.sys.dpi", srcDpi);
            if (realDpi <= 0) {
                realDpi = srcDpi;
            }
            if (!((realDpi == srcDpi || realDpi == 0 || srcDpi == 0) && (mPadDpi2 == 0 || mPadDpi2 == srcDpi))) {
                int tempdpi;
                if (mPadDpi2 > 0) {
                    tempdpi = mPadDpi2;
                } else {
                    tempdpi = srcDpi;
                }
                densityDpi = (((densityDpi * tempdpi) + realDpi) - 1) / realDpi;
                smallestScreenWidthDp = (((smallestScreenWidthDp * realDpi) + realDpi) - 1) / tempdpi;
                screenWidthDp = (((screenWidthDp * realDpi) + realDpi) - 1) / tempdpi;
                screenHeightDp = (((screenHeightDp * realDpi) + realDpi) - 1) / tempdpi;
            }
        }
        if (ResFlag) {
            this.mAssets.setConfiguration(this.mConfiguration.mcc, this.mConfiguration.mnc, adjustLanguageTag(this.mConfiguration.getLocales().get(0).toLanguageTag()), this.mConfiguration.orientation, this.mConfiguration.touchscreen, densityDpi, this.mConfiguration.keyboard, keyboardHidden, this.mConfiguration.navigation, width, height, smallestScreenWidthDp, screenWidthDp, screenHeightDp, this.mConfiguration.screenLayout, this.mConfiguration.uiMode, this.mConfiguration.colorMode, VERSION.RESOURCES_SDK_INT, changeRes);
        } else {
            this.mAssets.setConfiguration(this.mConfiguration.mcc, this.mConfiguration.mnc, adjustLanguageTag(this.mConfiguration.getLocales().get(0).toLanguageTag()), this.mConfiguration.orientation, this.mConfiguration.touchscreen, densityDpi, this.mConfiguration.keyboard, keyboardHidden, this.mConfiguration.navigation, width, height, smallestScreenWidthDp, screenWidthDp, screenHeightDp, this.mConfiguration.screenLayout, this.mConfiguration.uiMode, this.mConfiguration.colorMode, VERSION.RESOURCES_SDK_INT);
        }
    }

    private void updateDpiConfiguration() {
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
            if ((mPadDpi2 != 0 && mPadDpi2 != srcDpi && densityDpi != mPadDpi2 && densityDpi != ((int) (((((float) (mPadDpi2 * rogDpi)) * 1.0f) / ((float) dpi)) * 1.0f))) || (mPadDpi2 == 0 && dpi != srcDpi && dpi != 0 && srcDpi != 0 && densityDpi != srcDpi && densityDpi != ((int) (((((float) (srcDpi * rogDpi)) * 1.0f) / ((float) dpi)) * 1.0f)))) {
                int tempdpi;
                if (mPadDpi2 > 0) {
                    tempdpi = mPadDpi2;
                } else {
                    tempdpi = srcDpi;
                }
                Slog.i(TAG, "original config :" + this.mConfiguration);
                densityDpi = (((densityDpi * tempdpi) + dpi) - 1) / dpi;
                screenWidthDp = (((screenWidthDp * dpi) + dpi) - 1) / tempdpi;
                screenHeightDp = (((screenHeightDp * dpi) + dpi) - 1) / tempdpi;
                this.mConfiguration.smallestScreenWidthDp = (((smallestScreenWidthDp * dpi) + dpi) - 1) / tempdpi;
                this.mConfiguration.screenWidthDp = screenWidthDp;
                this.mConfiguration.screenHeightDp = screenHeightDp;
                this.mConfiguration.densityDpi = densityDpi;
                if ("com.android.browser".equals(ActivityThread.currentPackageName())) {
                    int longSizeDp = screenHeightDp > screenWidthDp ? screenHeightDp : screenWidthDp;
                    int shortSizeDp = screenHeightDp > screenWidthDp ? screenWidthDp : screenHeightDp;
                    int screenLayoutSize = screenLayout & 15;
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
        String language;
        String remainder;
        int separator = languageTag.indexOf(45);
        if (separator == -1) {
            language = languageTag;
            remainder = ProxyInfo.LOCAL_EXCL_LIST;
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

    protected Drawable loadDrawable(Resources wrapper, TypedValue value, int id, int density, Theme theme) throws NotFoundException {
        boolean useCache = density == 0 || value.density == this.mMetrics.densityDpi;
        if (density > 0 && value.density > 0 && value.density != 65535) {
            if (value.density == density) {
                value.density = this.mMetrics.densityDpi;
            } else {
                value.density = (value.density * this.mMetrics.densityDpi) / density;
            }
        }
        if (this.mHwResourcesImpl != null) {
            Drawable hwDrawable = this.mHwResourcesImpl.loadDrawable(wrapper, value, id, theme, useCache);
            if (hwDrawable != null) {
                return hwDrawable;
            }
        }
        try {
            boolean isColorDrawable;
            DrawableCache caches;
            long key;
            ConstantState cs;
            Drawable dr;
            if (value.type < 28 || value.type > 31) {
                isColorDrawable = false;
                caches = this.mDrawableCache;
                key = (((long) value.assetCookie) << 32) | ((long) value.data);
            } else {
                isColorDrawable = true;
                caches = this.mColorDrawableCache;
                key = (long) value.data;
            }
            if (!this.mPreloading && useCache) {
                Drawable cachedDrawable = caches.getInstance(key, wrapper, theme);
                if (cachedDrawable != null) {
                    cachedDrawable.setChangingConfigurations(value.changingConfigurations);
                    return cachedDrawable;
                }
            }
            if (isColorDrawable) {
                cs = (ConstantState) sPreloadedColorDrawables.get(key);
            } else {
                cs = (ConstantState) sPreloadedDrawables[this.mConfiguration.getLayoutDirection()].get(key);
            }
            boolean needsNewDrawableAfterCache = false;
            if (cs != null) {
                dr = cs.newDrawable(wrapper);
            } else if (isColorDrawable) {
                Drawable colorDrawable = new ColorDrawable(value.data);
            } else {
                dr = loadDrawableForCookie(wrapper, value, id, density, null);
            }
            if (dr instanceof DrawableContainer) {
                needsNewDrawableAfterCache = true;
            }
            boolean canApplyTheme = dr != null ? dr.canApplyTheme() : false;
            if (canApplyTheme && theme != null) {
                dr = dr.mutate();
                dr.applyTheme(theme);
                dr.clearMutated();
            }
            dr = this.mHwResourcesImpl.handleAddIconBackground(wrapper, id, dr);
            if (dr != null) {
                dr.setChangingConfigurations(value.changingConfigurations);
                if (useCache) {
                    cacheDrawable(value, isColorDrawable, caches, theme, canApplyTheme, key, dr);
                    if (needsNewDrawableAfterCache) {
                        ConstantState state = dr.getConstantState();
                        if (state != null) {
                            dr = state.newDrawable(wrapper);
                        }
                    }
                }
            }
            return dr;
        } catch (Exception e) {
            String name;
            try {
                name = getResourceName(id);
            } catch (NotFoundException e2) {
                name = "(missing name)";
            }
            NotFoundException notFoundException = new NotFoundException("Drawable " + name + " with resource ID #0x" + Integer.toHexString(id), e);
            notFoundException.setStackTrace(new StackTraceElement[0]);
            throw notFoundException;
        }
    }

    private void cacheDrawable(TypedValue value, boolean isColorDrawable, DrawableCache caches, Theme theme, boolean usesTheme, long key, Drawable dr) {
        ConstantState cs = dr.getConstantState();
        if (cs != null) {
            if (this.mPreloading) {
                int changingConfigs = cs.getChangingConfigurations();
                if (isColorDrawable) {
                    if (verifyPreloadConfig(changingConfigs, 0, value.resourceId, "drawable")) {
                        sPreloadedColorDrawables.put(key, cs);
                    }
                } else if (verifyPreloadConfig(changingConfigs, 8192, value.resourceId, "drawable")) {
                    if ((changingConfigs & 8192) == 0) {
                        sPreloadedDrawables[0].put(key, cs);
                        sPreloadedDrawables[1].put(key, cs);
                    } else {
                        sPreloadedDrawables[this.mConfiguration.getLayoutDirection()].put(key, cs);
                    }
                }
            } else {
                synchronized (this.mAccessLock) {
                    caches.put(key, theme, cs, usesTheme);
                }
            }
        }
    }

    private boolean verifyPreloadConfig(int changingConfigurations, int allowVarying, int resourceId, String name) {
        if ((((~(((((CONFIG_FONT_SCALE | CONFIG_DENSITY) | 1073741824) | CONFIG_SMALLEST_SCREEN_SIZE) | 1024) | CONFIG_ORIENTATION)) & changingConfigurations) & (~allowVarying)) == 0) {
            return true;
        }
        String resName;
        try {
            resName = getResourceName(resourceId);
        } catch (NotFoundException e) {
            resName = "?";
        }
        Log.w(TAG, "Preloaded " + name + " resource #0x" + Integer.toHexString(resourceId) + " (" + resName + ") that varies with configuration!!");
        return false;
    }

    private Drawable loadDrawableForCookie(Resources wrapper, TypedValue value, int id, int density, Theme theme) {
        if (value.string == null) {
            throw new NotFoundException("Resource \"" + getResourceName(id) + "\" (" + Integer.toHexString(id) + ") is not a Drawable (color or path): " + value);
        }
        String file = value.string.toString();
        Trace.traceBegin(8192, file);
        LookupStack stack = (LookupStack) this.mLookupStack.get();
        try {
            if (stack.contains(id)) {
                throw new Exception("Recursive reference in drawable");
            }
            Drawable dr;
            stack.push(id);
            if (file.endsWith(".xml")) {
                XmlResourceParser rp = loadXmlResourceParser(file, id, value.assetCookie, "drawable");
                dr = Drawable.createFromXmlForDensity(wrapper, rp, density, theme);
                rp.close();
            } else {
                InputStream is = this.mAssets.openNonAsset(value.assetCookie, file, 2);
                dr = Drawable.createFromResourceStream(wrapper, value, is, file, null);
                is.close();
            }
            stack.pop();
            Trace.traceEnd(8192);
            return dr;
        } catch (Exception e) {
            Trace.traceEnd(8192);
            NotFoundException rnf = new NotFoundException("File " + file + " from drawable resource ID #0x" + Integer.toHexString(id));
            rnf.initCause(e);
            throw rnf;
        } catch (Throwable th) {
            stack.pop();
        }
    }

    public Typeface loadFont(Resources wrapper, TypedValue value, int id) {
        if (value.string == null) {
            throw new NotFoundException("Resource \"" + getResourceName(id) + "\" (" + Integer.toHexString(id) + ") is not a Font: " + value);
        }
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
            Typeface createFromResources;
            if (file.endsWith("xml")) {
                FamilyResourceEntry familyEntry = FontResourcesParser.parse(loadXmlResourceParser(file, id, value.assetCookie, "font"), wrapper);
                if (familyEntry == null) {
                    return null;
                }
                createFromResources = Typeface.createFromResources(familyEntry, this.mAssets, file);
                Trace.traceEnd(8192);
                return createFromResources;
            }
            createFromResources = Typeface.createFromResources(this.mAssets, file, value.assetCookie);
            Trace.traceEnd(8192);
            return createFromResources;
        } catch (XmlPullParserException e) {
            Log.e(TAG, "Failed to parse xml resource " + file, e);
            return null;
        } catch (IOException e2) {
            Log.e(TAG, "Failed to read xml resource " + file, e2);
            return null;
        } finally {
            Trace.traceEnd(8192);
        }
    }

    private ComplexColor loadComplexColorFromName(Resources wrapper, Theme theme, TypedValue value, int id) {
        long key = (((long) value.assetCookie) << 32) | ((long) value.data);
        ConfigurationBoundResourceCache<ComplexColor> cache = this.mComplexColorCache;
        ComplexColor complexColor = (ComplexColor) cache.getInstance(key, wrapper, theme);
        if (complexColor != null) {
            return complexColor;
        }
        ConstantState<ComplexColor> factory = (ConstantState) sPreloadedComplexColors.get(key);
        if (factory != null) {
            complexColor = (ComplexColor) factory.newInstance(wrapper, theme);
        }
        if (complexColor == null) {
            complexColor = loadComplexColorForCookie(wrapper, value, id, theme);
        }
        if (complexColor != null) {
            complexColor.setBaseChangingConfigurations(value.changingConfigurations);
            if (!this.mPreloading) {
                cache.put(key, theme, complexColor.getConstantState());
            } else if (verifyPreloadConfig(complexColor.getChangingConfigurations(), 0, value.resourceId, "color")) {
                sPreloadedComplexColors.put(key, complexColor.getConstantState());
            }
        }
        return complexColor;
    }

    ComplexColor loadComplexColor(Resources wrapper, TypedValue value, int id, Theme theme) {
        long key = (((long) value.assetCookie) << 32) | ((long) value.data);
        if (value.type >= 28 && value.type <= 31) {
            return getColorStateListFromInt(value, key);
        }
        String file = value.string.toString();
        if (file.endsWith(".xml")) {
            try {
                return loadComplexColorFromName(wrapper, theme, value, id);
            } catch (Exception e) {
                NotFoundException rnf = new NotFoundException("File " + file + " from complex color resource ID #0x" + Integer.toHexString(id));
                rnf.initCause(e);
                throw rnf;
            }
        }
        throw new NotFoundException("File " + file + " from drawable resource ID #0x" + Integer.toHexString(id) + ": .xml extension required");
    }

    protected ColorStateList loadColorStateList(Resources wrapper, TypedValue value, int id, Theme theme) throws NotFoundException {
        long key = (((long) value.assetCookie) << 32) | ((long) value.data);
        if (value.type >= 28 && value.type <= 31) {
            return getColorStateListFromInt(value, key);
        }
        ComplexColor complexColor = loadComplexColorFromName(wrapper, theme, value, id);
        if (complexColor != null && (complexColor instanceof ColorStateList)) {
            return (ColorStateList) complexColor;
        }
        throw new NotFoundException("Can't find ColorStateList from drawable resource ID #0x" + Integer.toHexString(id));
    }

    private ColorStateList getColorStateListFromInt(TypedValue value, long key) {
        ConstantState<ComplexColor> factory = (ConstantState) sPreloadedComplexColors.get(key);
        if (factory != null) {
            return (ColorStateList) factory.newInstance();
        }
        ColorStateList csl = ColorStateList.valueOf(value.data);
        if (this.mPreloading && verifyPreloadConfig(value.changingConfigurations, 0, value.resourceId, "color")) {
            sPreloadedComplexColors.put(key, csl.getConstantState());
        }
        return csl;
    }

    private ComplexColor loadComplexColorForCookie(Resources wrapper, TypedValue value, int id, Theme theme) {
        if (value.string == null) {
            throw new UnsupportedOperationException("Can't convert to ComplexColor: type=0x" + value.type);
        }
        String file = value.string.toString();
        ComplexColor complexColor = null;
        Trace.traceBegin(8192, file);
        if (file.endsWith(".xml")) {
            try {
                int type;
                XmlResourceParser parser = loadXmlResourceParser(file, id, value.assetCookie, "ComplexColor");
                AttributeSet attrs = Xml.asAttributeSet(parser);
                do {
                    type = parser.next();
                    if (type == 2) {
                        break;
                    }
                } while (type != 1);
                if (type != 2) {
                    throw new XmlPullParserException("No start tag found");
                }
                String name = parser.getName();
                if (name.equals("gradient")) {
                    complexColor = GradientColor.createFromXmlInner(wrapper, parser, attrs, theme);
                } else if (name.equals("selector")) {
                    complexColor = ColorStateList.createFromXmlInner(wrapper, parser, attrs, theme);
                }
                parser.close();
                Trace.traceEnd(8192);
                return complexColor;
            } catch (Exception e) {
                Trace.traceEnd(8192);
                NotFoundException rnf = new NotFoundException("File " + file + " from ComplexColor resource ID #0x" + Integer.toHexString(id));
                rnf.initCause(e);
                throw rnf;
            }
        }
        Trace.traceEnd(8192);
        throw new NotFoundException("File " + file + " from drawable resource ID #0x" + Integer.toHexString(id) + ": .xml extension required");
    }

    XmlResourceParser loadXmlResourceParser(String file, int id, int assetCookie, String type) throws NotFoundException {
        if (id != 0) {
            try {
                synchronized (this.mCachedXmlBlocks) {
                    XmlResourceParser newParser;
                    int[] cachedXmlBlockCookies = this.mCachedXmlBlockCookies;
                    String[] cachedXmlBlockFiles = this.mCachedXmlBlockFiles;
                    XmlBlock[] cachedXmlBlocks = this.mCachedXmlBlocks;
                    int num = cachedXmlBlockFiles.length;
                    int i = 0;
                    while (i < num) {
                        if (cachedXmlBlockCookies[i] == assetCookie && cachedXmlBlockFiles[i] != null && cachedXmlBlockFiles[i].equals(file)) {
                            newParser = cachedXmlBlocks[i].newParser();
                            return newParser;
                        }
                        i++;
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
                        newParser = block.newParser();
                        return newParser;
                    }
                }
            } catch (Exception e) {
                NotFoundException rnf = new NotFoundException("File " + file + " from xml type " + type + " resource ID #0x" + Integer.toHexString(id));
                rnf.initCause(e);
                throw rnf;
            }
        }
        throw new NotFoundException("File " + file + " from xml type " + type + " resource ID #0x" + Integer.toHexString(id));
    }

    public final void startPreloading() {
        synchronized (sSync) {
            if (sPreloaded) {
                throw new IllegalStateException("Resources already preloaded");
            }
            sPreloaded = true;
            this.mPreloading = true;
            sPreloadedDensity = DisplayMetrics.DENSITY_DEVICE;
            this.mConfiguration.densityDpi = sPreloadedDensity;
            updateConfiguration(null, null, null);
        }
    }

    void finishPreloading() {
        if (this.mPreloading) {
            this.mPreloading = false;
            flushLayoutCache();
        }
    }

    LongSparseArray<ConstantState> getPreloadedDrawables() {
        return sPreloadedDrawables[0];
    }

    ThemeImpl newThemeImpl() {
        return new ThemeImpl();
    }

    ThemeImpl newThemeImpl(ThemeKey key) {
        ThemeImpl impl = new ThemeImpl();
        impl.mKey.setTo(key);
        impl.rebase();
        return impl;
    }

    public ConfigurationBoundResourceCache<HwStateListAnimator> getHwStateListAnimatorCache() {
        return this.mHwStateListAnimatorCache;
    }

    protected ConfigurationBoundResourceCache<ComplexColor> getComplexColorCache() {
        return this.mComplexColorCache;
    }
}
