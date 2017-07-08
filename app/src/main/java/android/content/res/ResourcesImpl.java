package android.content.res;

import android.animation.Animator;
import android.animation.StateListAnimator;
import android.app.ActivityThread;
import android.content.pm.ActivityInfo;
import android.content.res.Resources.NotFoundException;
import android.content.res.Resources.Theme;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Drawable.ConstantState;
import android.hwcontrol.HwWidgetFactory;
import android.hwtheme.HwThemeManager;
import android.icu.text.PluralRules;
import android.net.ProxyInfo;
import android.os.Build.VERSION;
import android.os.LocaleList;
import android.os.SystemProperties;
import android.os.Trace;
import android.rog.AppRogInfo;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.LongSparseArray;
import android.util.TypedValue;
import android.util.Xml;
import android.view.DisplayAdjustments;
import huawei.android.animation.HwStateListAnimator;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Locale;
import org.xmlpull.v1.XmlPullParserException;

public class ResourcesImpl extends AbsResourcesImpl {
    private static final int CONFIG_DENSITY = 0;
    private static final int CONFIG_FONT_SCALE = 0;
    private static final int CONFIG_ORIENTATION = 0;
    private static final int CONFIG_SCREEN_SIZE = 1024;
    private static final int CONFIG_SMALLEST_SCREEN_SIZE = 0;
    private static final boolean DEBUG_CONFIG = false;
    private static final boolean DEBUG_LOAD = false;
    private static final int ID_OTHER = 16777220;
    private static final int LAYOUT_DIR_CONFIG = 0;
    private static final int SPEC_PUBLIC = 1073741824;
    static final String TAG = "Resources";
    private static final boolean TRACE_FOR_MISS_PRELOAD = false;
    private static final boolean TRACE_FOR_PRELOAD = false;
    private static final int XML_BLOCK_CACHE_SIZE = 4;
    private static boolean sPreloaded;
    private static final LongSparseArray<ConstantState> sPreloadedColorDrawables = null;
    private static final LongSparseArray<ConstantState<ComplexColor>> sPreloadedComplexColors = null;
    private static int sPreloadedDensity;
    private static final LongSparseArray<ConstantState>[] sPreloadedDrawables = null;
    private static final Object sSync = null;
    private final Object mAccessLock;
    private final ConfigurationBoundResourceCache<Animator> mAnimatorCache;
    final AssetManager mAssets;
    private final int[] mCachedXmlBlockCookies;
    private final String[] mCachedXmlBlockFiles;
    private final XmlBlock[] mCachedXmlBlocks;
    private final DrawableCache mColorDrawableCache;
    private final ConfigurationBoundResourceCache<ComplexColor> mComplexColorCache;
    private final Configuration mConfiguration;
    private final DisplayAdjustments mDisplayAdjustments;
    private final DrawableCache mDrawableCache;
    private AbsResourcesImpl mHwResourcesImpl;
    private final ConfigurationBoundResourceCache<HwStateListAnimator> mHwStateListAnimatorCache;
    private int mLastCachedXmlBlockIndex;
    private final DisplayMetrics mMetrics;
    private PluralRules mPluralRule;
    private boolean mPreloading;
    private boolean mRogEnable;
    private AppRogInfo mRogInfo;
    private final ConfigurationBoundResourceCache<StateListAnimator> mStateListAnimatorCache;
    private final Configuration mTmpConfig;

    public class ThemeImpl {
        private final AssetManager mAssets;
        private final ThemeKey mKey;
        private final long mTheme;
        private int mThemeResId;

        ThemeImpl() {
            this.mKey = new ThemeKey();
            this.mThemeResId = ResourcesImpl.LAYOUT_DIR_CONFIG;
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
                AssetManager.applyStyle(this.mTheme, defStyleAttr, defStyleRes, parser != null ? parser.mParseState : 0, attrs, array.mData, array.mIndices);
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
                AssetManager.resolveAttrs(this.mTheme, ResourcesImpl.LAYOUT_DIR_CONFIG, ResourcesImpl.LAYOUT_DIR_CONFIG, values, attrs, array.mData, array.mIndices);
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
                int i = ResourcesImpl.LAYOUT_DIR_CONFIG;
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
                for (int i = ResourcesImpl.LAYOUT_DIR_CONFIG; i < this.mKey.mCount; i++) {
                    AssetManager.applyThemeStyle(this.mTheme, this.mKey.mResId[i], this.mKey.mForce[i]);
                }
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.content.res.ResourcesImpl.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.content.res.ResourcesImpl.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.content.res.ResourcesImpl.<clinit>():void");
    }

    private boolean verifyPreloadConfig(int r1, int r2, int r3, java.lang.String r4) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.content.res.ResourcesImpl.verifyPreloadConfig(int, int, int, java.lang.String):boolean
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
        throw new UnsupportedOperationException("Method not decompiled: android.content.res.ResourcesImpl.verifyPreloadConfig(int, int, int, java.lang.String):boolean");
    }

    public ResourcesImpl(AssetManager assets, DisplayMetrics metrics, Configuration config, DisplayAdjustments displayAdjustments) {
        this.mAccessLock = new Object();
        this.mTmpConfig = new Configuration();
        this.mDrawableCache = new DrawableCache();
        this.mColorDrawableCache = new DrawableCache();
        this.mComplexColorCache = new ConfigurationBoundResourceCache();
        this.mAnimatorCache = new ConfigurationBoundResourceCache();
        this.mStateListAnimatorCache = new ConfigurationBoundResourceCache();
        this.mLastCachedXmlBlockIndex = -1;
        this.mCachedXmlBlockCookies = new int[XML_BLOCK_CACHE_SIZE];
        this.mCachedXmlBlockFiles = new String[XML_BLOCK_CACHE_SIZE];
        this.mCachedXmlBlocks = new XmlBlock[XML_BLOCK_CACHE_SIZE];
        this.mMetrics = new DisplayMetrics();
        this.mConfiguration = new Configuration();
        this.mRogEnable = TRACE_FOR_PRELOAD;
        this.mHwStateListAnimatorCache = new ConfigurationBoundResourceCache();
        this.mHwResourcesImpl = HwThemeManager.getHwResourcesImpl();
        this.mHwResourcesImpl.setResourcesImpl(this);
        this.mHwResourcesImpl.setHwTheme(config);
        this.mAssets = assets;
        this.mMetrics.setToDefaults();
        this.mDisplayAdjustments = displayAdjustments;
        updateConfiguration(config, metrics, displayAdjustments.getCompatibilityInfo());
        this.mAssets.ensureStringBlocks();
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
        DisplayMetrics rogDm = new DisplayMetrics();
        rogDm.setTo(this.mMetrics);
        if (this.mRogInfo == null) {
            return this.mMetrics;
        }
        this.mRogInfo.getRealSizeDisplayMetrics(rogDm, this.mRogEnable);
        return rogDm;
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
                this.mPluralRule = PluralRules.forLocale(this.mConfiguration.getLocales().get(LAYOUT_DIR_CONFIG));
            }
            pluralRules = this.mPluralRule;
        }
        return pluralRules;
    }

    void getValue(int id, TypedValue outValue, boolean resolveRefs) throws NotFoundException {
        if (!this.mAssets.getResourceValue(id, LAYOUT_DIR_CONFIG, outValue, resolveRefs)) {
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

    public void updateConfiguration(Configuration config, DisplayMetrics metrics, CompatibilityInfo compat) {
        Trace.traceBegin(Trace.TRACE_TAG_RESOURCES, "ResourcesImpl#updateConfiguration");
        try {
            synchronized (this.mAccessLock) {
                if (compat != null) {
                    this.mDisplayAdjustments.setCompatibilityInfo(compat);
                }
                if (metrics != null) {
                    this.mMetrics.setTo(metrics);
                }
                if (this.mRogInfo != null) {
                    this.mRogInfo.applyToDisplayMetrics(this.mMetrics, this.mRogEnable);
                } else {
                    this.mDisplayAdjustments.getCompatibilityInfo().applyToDisplayMetrics(this.mMetrics);
                }
                int configChanges = calcConfigChanges(config);
                LocaleList locales = this.mConfiguration.getLocales();
                if (locales.isEmpty()) {
                    locales = LocaleList.getDefault();
                    this.mConfiguration.setLocales(locales);
                }
                boolean changeRes = TRACE_FOR_PRELOAD;
                if ((configChanges & XML_BLOCK_CACHE_SIZE) != 0) {
                    if (locales.size() > 1) {
                        String[] availableLocales = this.mAssets.getNonSystemLocales();
                        if (LocaleList.isPseudoLocalesOnly(availableLocales) || availableLocales.length == 1) {
                            availableLocales = this.mAssets.getLocales();
                            if (LocaleList.isPseudoLocalesOnly(availableLocales)) {
                                availableLocales = null;
                            }
                        }
                        if (availableLocales != null) {
                            boolean hasDbid = TRACE_FOR_PRELOAD;
                            int length = availableLocales.length;
                            for (int i = LAYOUT_DIR_CONFIG; i < length; i++) {
                                if (availableLocales[i].equals("zz-ZX")) {
                                    hasDbid = true;
                                    break;
                                }
                            }
                            if (hasDbid) {
                                String[] internal = availableLocales;
                                Resources.getSystem().getAssets();
                                String[] shared = AssetManager.getSharedResList();
                                if (shared != null) {
                                    String[] result = (String[]) Arrays.copyOf(internal, internal.length + shared.length);
                                    System.arraycopy(shared, LAYOUT_DIR_CONFIG, result, internal.length, shared.length);
                                    availableLocales = result;
                                    if (shared.length > 0) {
                                        changeRes = true;
                                    }
                                }
                            }
                            Locale bestLocale = locales.getFirstMatchWithEnglishSupported(availableLocales);
                            if (!(bestLocale == null || bestLocale == locales.get(LAYOUT_DIR_CONFIG))) {
                                this.mConfiguration.setLocales(new LocaleList(bestLocale, locales));
                            }
                        }
                    } else {
                        changeRes = true;
                    }
                }
                if (this.mConfiguration.densityDpi != 0) {
                    this.mMetrics.densityDpi = this.mConfiguration.densityDpi;
                    this.mMetrics.density = ((float) this.mConfiguration.densityDpi) * 0.00625f;
                }
                this.mMetrics.scaledDensity = this.mMetrics.density * this.mConfiguration.fontScale;
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
                    this.mPluralRule = PluralRules.forLocale(this.mConfiguration.getLocales().get(LAYOUT_DIR_CONFIG));
                }
            }
            Trace.traceEnd(Trace.TRACE_TAG_RESOURCES);
        } catch (Throwable th) {
            Trace.traceEnd(Trace.TRACE_TAG_RESOURCES);
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
        boolean isLockResWhitelist = SystemProperties.getBoolean("ro.config.hw_lock_res_whitelist", TRACE_FOR_PRELOAD);
        if ((isLockResWhitelist && getHwResourcesImpl().isInMultiDpiWhiteList(packageName)) || !isLockResWhitelist) {
            int srcDpi = SystemProperties.getInt("ro.sf.lcd_density", LAYOUT_DIR_CONFIG);
            int realDpi = SystemProperties.getInt("persist.sys.dpi", srcDpi);
            if (realDpi <= 0) {
                realDpi = srcDpi;
            }
            if (!(realDpi == srcDpi || realDpi == 0 || srcDpi == 0)) {
                densityDpi = (((densityDpi * srcDpi) + realDpi) - 1) / realDpi;
                smallestScreenWidthDp = (((smallestScreenWidthDp * realDpi) + srcDpi) - 1) / srcDpi;
                screenWidthDp = (((screenWidthDp * realDpi) + srcDpi) - 1) / srcDpi;
                screenHeightDp = (((screenHeightDp * realDpi) + srcDpi) - 1) / srcDpi;
            }
        }
        if (SystemProperties.getInt("persist.sys.rog.width", LAYOUT_DIR_CONFIG) > 0) {
            screenWidthDp = this.mConfiguration.screenWidthDp;
            screenHeightDp = this.mConfiguration.screenHeightDp;
            smallestScreenWidthDp = this.mConfiguration.smallestScreenWidthDp;
            densityDpi = this.mConfiguration.densityDpi;
        }
        if (ResFlag) {
            this.mAssets.setConfiguration(this.mConfiguration.mcc, this.mConfiguration.mnc, adjustLanguageTag(this.mConfiguration.getLocales().get(LAYOUT_DIR_CONFIG).toLanguageTag()), this.mConfiguration.orientation, this.mConfiguration.touchscreen, densityDpi, this.mConfiguration.keyboard, keyboardHidden, this.mConfiguration.navigation, width, height, smallestScreenWidthDp, screenWidthDp, screenHeightDp, this.mConfiguration.screenLayout, this.mConfiguration.uiMode, VERSION.RESOURCES_SDK_INT, changeRes);
            return;
        }
        this.mAssets.setConfiguration(this.mConfiguration.mcc, this.mConfiguration.mnc, adjustLanguageTag(this.mConfiguration.getLocales().get(LAYOUT_DIR_CONFIG).toLanguageTag()), this.mConfiguration.orientation, this.mConfiguration.touchscreen, densityDpi, this.mConfiguration.keyboard, keyboardHidden, this.mConfiguration.navigation, width, height, smallestScreenWidthDp, screenWidthDp, screenHeightDp, this.mConfiguration.screenLayout, this.mConfiguration.uiMode, VERSION.RESOURCES_SDK_INT);
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
        if (this.mRogInfo != null) {
            this.mRogInfo.applyToConfiguration(this.mMetrics, this.mTmpConfig);
        } else {
            this.mDisplayAdjustments.getCompatibilityInfo().applyToConfiguration(density, this.mTmpConfig);
            this.mDisplayAdjustments.getCompatibilityInfo().applyToConfigurationExt(this.mMetrics, density, this.mTmpConfig);
        }
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
            language = languageTag.substring(LAYOUT_DIR_CONFIG, separator);
            remainder = languageTag.substring(separator);
        }
        return Locale.adjustLanguageCode(language) + remainder;
    }

    public void flushLayoutCache() {
        synchronized (this.mCachedXmlBlocks) {
            Arrays.fill(this.mCachedXmlBlockCookies, LAYOUT_DIR_CONFIG);
            Arrays.fill(this.mCachedXmlBlockFiles, null);
            XmlBlock[] cachedXmlBlocks = this.mCachedXmlBlocks;
            for (int i = LAYOUT_DIR_CONFIG; i < XML_BLOCK_CACHE_SIZE; i++) {
                XmlBlock oldBlock = cachedXmlBlocks[i];
                if (oldBlock != null) {
                    oldBlock.close();
                }
            }
            Arrays.fill(cachedXmlBlocks, null);
        }
    }

    protected Drawable loadDrawable(Resources wrapper, TypedValue value, int id, Theme theme, boolean useCache) throws NotFoundException {
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
                isColorDrawable = TRACE_FOR_PRELOAD;
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
                    return cachedDrawable;
                }
            }
            if (isColorDrawable) {
                cs = (ConstantState) sPreloadedColorDrawables.get(key);
            } else {
                cs = (ConstantState) sPreloadedDrawables[this.mConfiguration.getLayoutDirection()].get(key);
            }
            if (cs != null) {
                dr = cs.newDrawable(wrapper);
            } else if (isColorDrawable) {
                dr = new ColorDrawable(value.data);
            } else {
                dr = loadDrawableForCookie(wrapper, value, id, null);
            }
            boolean canApplyTheme = dr != null ? dr.canApplyTheme() : TRACE_FOR_PRELOAD;
            if (canApplyTheme && theme != null) {
                dr = dr.mutate();
                dr.applyTheme(theme);
                dr.clearMutated();
            }
            dr = this.mHwResourcesImpl.handleAddIconBackground(wrapper, id, dr);
            if (dr != null && useCache) {
                dr.setChangingConfigurations(value.changingConfigurations);
                cacheDrawable(value, isColorDrawable, caches, theme, canApplyTheme, key, dr);
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
            notFoundException.setStackTrace(new StackTraceElement[LAYOUT_DIR_CONFIG]);
            throw notFoundException;
        }
    }

    private void cacheDrawable(TypedValue value, boolean isColorDrawable, DrawableCache caches, Theme theme, boolean usesTheme, long key, Drawable dr) {
        ConstantState cs = dr.getConstantState();
        if (cs != null) {
            if (this.mPreloading) {
                int changingConfigs = cs.getChangingConfigurations();
                if (isColorDrawable) {
                    if (verifyPreloadConfig(changingConfigs, LAYOUT_DIR_CONFIG, value.resourceId, "drawable")) {
                        sPreloadedColorDrawables.put(key, cs);
                    }
                } else if (verifyPreloadConfig(changingConfigs, LAYOUT_DIR_CONFIG, value.resourceId, "drawable")) {
                    if ((LAYOUT_DIR_CONFIG & changingConfigs) == 0) {
                        sPreloadedDrawables[LAYOUT_DIR_CONFIG].put(key, cs);
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

    private Drawable loadDrawableForCookie(Resources wrapper, TypedValue value, int id, Theme theme) {
        if (value.string == null) {
            throw new NotFoundException("Resource \"" + getResourceName(id) + "\" (" + Integer.toHexString(id) + ") is not a Drawable (color or path): " + value);
        }
        String file = value.string.toString();
        Trace.traceBegin(Trace.TRACE_TAG_RESOURCES, file);
        try {
            Drawable dr;
            if (file.endsWith(".xml")) {
                XmlResourceParser rp = loadXmlResourceParser(file, id, value.assetCookie, "drawable");
                dr = Drawable.createFromXml(wrapper, rp, theme);
                rp.close();
            } else {
                InputStream is = this.mAssets.openNonAsset(value.assetCookie, file, 2);
                dr = Drawable.createFromResourceStream(wrapper, value, is, file, null);
                is.close();
            }
            Trace.traceEnd(Trace.TRACE_TAG_RESOURCES);
            return dr;
        } catch (Exception e) {
            Trace.traceEnd(Trace.TRACE_TAG_RESOURCES);
            NotFoundException rnf = new NotFoundException("File " + file + " from drawable resource ID #0x" + Integer.toHexString(id));
            rnf.initCause(e);
            throw rnf;
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
            } else if (verifyPreloadConfig(complexColor.getChangingConfigurations(), LAYOUT_DIR_CONFIG, value.resourceId, ColorsColumns.COLOR)) {
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
        if (this.mPreloading && verifyPreloadConfig(value.changingConfigurations, LAYOUT_DIR_CONFIG, value.resourceId, ColorsColumns.COLOR)) {
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
        Trace.traceBegin(Trace.TRACE_TAG_RESOURCES, file);
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
                Trace.traceEnd(Trace.TRACE_TAG_RESOURCES);
                return complexColor;
            } catch (Exception e) {
                Trace.traceEnd(Trace.TRACE_TAG_RESOURCES);
                NotFoundException rnf = new NotFoundException("File " + file + " from ComplexColor resource ID #0x" + Integer.toHexString(id));
                rnf.initCause(e);
                throw rnf;
            }
        }
        Trace.traceEnd(Trace.TRACE_TAG_RESOURCES);
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
                    int i = LAYOUT_DIR_CONFIG;
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
            this.mPreloading = TRACE_FOR_PRELOAD;
            flushLayoutCache();
        }
    }

    LongSparseArray<ConstantState> getPreloadedDrawables() {
        return sPreloadedDrawables[LAYOUT_DIR_CONFIG];
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

    public void setRogInfo(AppRogInfo info, boolean enable) {
        this.mRogInfo = info;
        this.mRogEnable = enable;
        updateConfiguration(getConfiguration(), getDisplayMetrics(), null);
    }
}
