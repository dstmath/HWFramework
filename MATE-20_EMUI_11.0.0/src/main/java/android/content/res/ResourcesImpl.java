package android.content.res;

import android.animation.Animator;
import android.animation.StateListAnimator;
import android.annotation.UnsupportedAppUsage;
import android.app.ActivityThread;
import android.app.Application;
import android.common.HwFrameworkFactory;
import android.content.pm.ActivityInfo;
import android.content.res.AbsResourcesImpl;
import android.content.res.AssetManager;
import android.content.res.FontResourcesParser;
import android.content.res.Resources;
import android.content.res.XmlBlock;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.graphics.Typeface;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.ColorStateListDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.DrawableContainer;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.VectorDrawable;
import android.hwtheme.HwThemeManager;
import android.icu.text.PluralRules;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.Build;
import android.os.Bundle;
import android.os.LocaleList;
import android.os.Process;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.Trace;
import android.util.AttributeSet;
import android.util.CoordinationModeUtils;
import android.util.DisplayMetrics;
import android.util.HwPCUtils;
import android.util.Log;
import android.util.LongSparseArray;
import android.util.Slog;
import android.util.TypedValue;
import android.util.Xml;
import android.view.DisplayAdjustments;
import com.android.internal.util.GrowingArrayUtils;
import com.huawei.android.fsm.HwFoldScreenManager;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import org.xmlpull.v1.XmlPullParserException;

public class ResourcesImpl {
    private static final String COORDINATION_PACKAGE_NAME = "com.huawei.camera";
    private static final boolean DEBUG_CONFIG = false;
    private static final boolean DEBUG_LOAD = false;
    private static final int ID_OTHER = 16777220;
    private static final int MIN_LANG_COUNT = 4;
    static final String TAG = "Resources";
    static final String TAG_PRELOAD = "Resources.preload";
    public static final boolean TRACE_FOR_DETAILED_PRELOAD = SystemProperties.getBoolean("debug.trace_resource_preload", false);
    @UnsupportedAppUsage
    private static final boolean TRACE_FOR_MISS_PRELOAD = false;
    @UnsupportedAppUsage
    private static final boolean TRACE_FOR_PRELOAD = false;
    private static final int XML_BLOCK_CACHE_SIZE = 4;
    public static final boolean mIsLockResWhitelist = SystemProperties.getBoolean("ro.config.hw_lock_res_whitelist", false);
    private static int sPreloadTracingNumLoadedDrawables;
    public static boolean sPreloaded;
    @UnsupportedAppUsage
    private static final LongSparseArray<Drawable.ConstantState> sPreloadedColorDrawables = new LongSparseArray<>();
    @UnsupportedAppUsage
    private static final LongSparseArray<ConstantState<ComplexColor>> sPreloadedComplexColors = new LongSparseArray<>();
    @UnsupportedAppUsage
    private static final LongSparseArray<Drawable.ConstantState>[] sPreloadedDrawables = new LongSparseArray[2];
    private static final Object sSync = new Object();
    @UnsupportedAppUsage
    private final Object mAccessLock = new Object();
    @UnsupportedAppUsage
    private final ConfigurationBoundResourceCache<Animator> mAnimatorCache = new ConfigurationBoundResourceCache<>();
    @UnsupportedAppUsage
    final AssetManager mAssets;
    private final int[] mCachedXmlBlockCookies = new int[4];
    private final String[] mCachedXmlBlockFiles = new String[4];
    private final XmlBlock[] mCachedXmlBlocks = new XmlBlock[4];
    @UnsupportedAppUsage
    private final DrawableCache mColorDrawableCache = new DrawableCache();
    private final ConfigurationBoundResourceCache<ComplexColor> mComplexColorCache = new ConfigurationBoundResourceCache<>();
    @UnsupportedAppUsage
    private final Configuration mConfiguration = new Configuration();
    private final DisplayAdjustments mDisplayAdjustments;
    @UnsupportedAppUsage
    private final DrawableCache mDrawableCache = new DrawableCache();
    private AbsResourcesImpl mHwResourcesImpl;
    private boolean mIsDpiLocked = false;
    private int mLastCachedXmlBlockIndex = -1;
    private final ThreadLocal<LookupStack> mLookupStack = ThreadLocal.withInitial($$Lambda$ResourcesImpl$h3PTRX185BeQl8SVC2_w9arp5Og.INSTANCE);
    private final DisplayMetrics mMetrics = new DisplayMetrics();
    private PluralRules mPluralRule;
    private long mPreloadTracingPreloadStartTime;
    private long mPreloadTracingStartBitmapCount;
    private long mPreloadTracingStartBitmapSize;
    @UnsupportedAppUsage
    public boolean mPreloading;
    @UnsupportedAppUsage
    private final ConfigurationBoundResourceCache<StateListAnimator> mStateListAnimatorCache = new ConfigurationBoundResourceCache<>();
    private final Configuration mTmpConfig = new Configuration();

    static {
        sPreloadedDrawables[0] = new LongSparseArray<>();
        sPreloadedDrawables[1] = new LongSparseArray<>();
    }

    static /* synthetic */ LookupStack lambda$new$0() {
        return new LookupStack();
    }

    @UnsupportedAppUsage
    public ResourcesImpl(AssetManager assets, DisplayMetrics metrics, Configuration config, DisplayAdjustments displayAdjustments) {
        this.mAssets = assets;
        this.mHwResourcesImpl = HwThemeManager.getHwResourcesImpl();
        this.mHwResourcesImpl.setResourcesImpl(new ResourcesImplEx(this));
        this.mHwResourcesImpl.setResImplPackageName(this.mAssets.mMainApkPackageName);
        this.mHwResourcesImpl.setHwTheme(config);
        this.mHwResourcesImpl.initDeepTheme();
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

    @UnsupportedAppUsage
    public AssetManager getAssets() {
        return this.mAssets;
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
    public DisplayMetrics getDisplayMetrics() {
        Application appObj;
        String pkgName;
        if (HwFoldScreenManager.isFoldable() && (appObj = ActivityThread.currentApplication()) != null && appObj.getApplicationInfo() != null && (pkgName = appObj.getApplicationInfo().packageName) != null && pkgName.equals(COORDINATION_PACKAGE_NAME) && HwFoldScreenManager.getDisplayMode() == 4) {
            DisplayMetrics metrices = new DisplayMetrics();
            metrices.setTo(this.mMetrics);
            metrices.widthPixels = CoordinationModeUtils.getFoldScreenMainWidth();
            return metrices;
        } else if (!ActivityThread.isAdjustConfig(this.mConfiguration)) {
            return this.mMetrics;
        } else {
            DisplayMetrics metrics = new DisplayMetrics();
            metrics.setTo(this.mMetrics);
            int width = this.mConfiguration.windowConfiguration.getAppBounds().width();
            metrics.widthPixels = width;
            metrics.noncompatWidthPixels = width;
            int height = this.mConfiguration.windowConfiguration.getAppBounds().height();
            metrics.heightPixels = height;
            metrics.noncompatHeightPixels = height;
            CompatibilityInfo ci = getDisplayAdjustments().getCompatibilityInfo();
            if (!ci.supportsScreen()) {
                float ratio = ci.getSdrLowResolutionRatio();
                metrics.widthPixels = (int) ((((float) metrics.noncompatWidthPixels) * ratio) + 0.5f);
                metrics.heightPixels = (int) ((((float) metrics.noncompatHeightPixels) * ratio) + 0.5f);
            }
            return metrics;
        }
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
    @UnsupportedAppUsage
    public void getValue(int id, TypedValue outValue, boolean resolveRefs) throws Resources.NotFoundException {
        AbsResourcesImpl.ThemedValue themedValue;
        if (!this.mAssets.getResourceValue(id, 0, outValue, resolveRefs)) {
            getHwResourcesImpl().printErrorResource();
            throw new Resources.NotFoundException("Resource ID #0x" + Integer.toHexString(id));
        } else if (HwThemeManager.DEBUG_UI_PROGRAM && (themedValue = getHwResourcesImpl().getThemeDimension(outValue, id)) != null) {
            outValue.data = themedValue.data;
        }
    }

    /* access modifiers changed from: package-private */
    public void getValueForDensity(int id, int density, TypedValue outValue, boolean resolveRefs) throws Resources.NotFoundException {
        if (!this.mAssets.getResourceValue(id, density, outValue, resolveRefs)) {
            getHwResourcesImpl().printErrorResource();
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
        getHwResourcesImpl().printErrorResource();
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
        getHwResourcesImpl().printErrorResource();
        throw new Resources.NotFoundException("Unable to find resource ID #0x" + Integer.toHexString(resid));
    }

    /* access modifiers changed from: package-private */
    public String getResourcePackageName(int resid) throws Resources.NotFoundException {
        String str = this.mAssets.getResourcePackageName(resid);
        if (str != null) {
            return str;
        }
        getHwResourcesImpl().printErrorResource();
        throw new Resources.NotFoundException("Unable to find resource ID #0x" + Integer.toHexString(resid));
    }

    /* access modifiers changed from: package-private */
    public String getResourceTypeName(int resid) throws Resources.NotFoundException {
        String str = this.mAssets.getResourceTypeName(resid);
        if (str != null) {
            return str;
        }
        getHwResourcesImpl().printErrorResource();
        throw new Resources.NotFoundException("Unable to find resource ID #0x" + Integer.toHexString(resid));
    }

    /* access modifiers changed from: package-private */
    public String getResourceEntryName(int resid) throws Resources.NotFoundException {
        String str = this.mAssets.getResourceEntryName(resid);
        if (str != null) {
            return str;
        }
        getHwResourcesImpl().printErrorResource();
        throw new Resources.NotFoundException("Unable to find resource ID #0x" + Integer.toHexString(resid));
    }

    /* access modifiers changed from: package-private */
    public String getLastResourceResolution() throws Resources.NotFoundException {
        String str = this.mAssets.getLastResourceResolution();
        if (str != null) {
            return str;
        }
        throw new Resources.NotFoundException("Associated AssetManager hasn't resolved a resource");
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
        getHwResourcesImpl().printErrorResource();
        throw new Resources.NotFoundException("Plural resource ID #0x" + Integer.toHexString(id) + " quantity=" + quantity + " item=" + rule.select((double) quantity));
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    private static int attrForQuantityCode(String quantityCode) {
        char c;
        switch (quantityCode.hashCode()) {
            case 101272:
                if (quantityCode.equals("few")) {
                    c = 3;
                    break;
                }
                c = 65535;
                break;
            case 110182:
                if (quantityCode.equals("one")) {
                    c = 1;
                    break;
                }
                c = 65535;
                break;
            case 115276:
                if (quantityCode.equals("two")) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            case 3343967:
                if (quantityCode.equals("many")) {
                    c = 4;
                    break;
                }
                c = 65535;
                break;
            case 3735208:
                if (quantityCode.equals("zero")) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            default:
                c = 65535;
                break;
        }
        if (c == 0) {
            return 16777221;
        }
        if (c == 1) {
            return 16777222;
        }
        if (c == 2) {
            return 16777223;
        }
        if (c == 3) {
            return 16777224;
        }
        if (c != 4) {
            return ID_OTHER;
        }
        return 16777225;
    }

    /* access modifiers changed from: package-private */
    public AssetFileDescriptor openRawResourceFd(int id, TypedValue tempValue) throws Resources.NotFoundException {
        getValue(id, tempValue, true);
        try {
            return this.mAssets.openNonAssetFd(tempValue.assetCookie, tempValue.string.toString());
        } catch (Exception e) {
            getHwResourcesImpl().printErrorResource();
            throw new Resources.NotFoundException("File " + tempValue.string.toString() + " from drawable resource ID #0x" + Integer.toHexString(id), e);
        }
    }

    /* access modifiers changed from: package-private */
    public InputStream openRawResource(int id, TypedValue value) throws Resources.NotFoundException {
        getValue(id, value, true);
        try {
            return this.mAssets.openNonAsset(value.assetCookie, value.string.toString(), 2);
        } catch (Exception e) {
            getHwResourcesImpl().printErrorResource();
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

    public void updateConfiguration(Configuration config, DisplayMetrics metrics, CompatibilityInfo compat) {
        int height;
        int width;
        int keyboardHidden;
        boolean mLockDpi;
        boolean isSameDpi;
        String[] shared;
        Trace.traceBegin(8192, "ResourcesImpl#updateConfiguration");
        try {
            synchronized (this.mAccessLock) {
                if (compat != null) {
                    try {
                        this.mDisplayAdjustments.setCompatibilityInfo(compat);
                    } catch (Throwable th) {
                        throw th;
                    }
                }
                if (metrics != null) {
                    this.mMetrics.setTo(metrics);
                }
                this.mDisplayAdjustments.getCompatibilityInfo().applyToDisplayMetrics(this.mMetrics);
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
                        if (LocaleList.isPseudoLocalesOnly(availableLocales) || (availableLocales.length == 1 && HwAssetManagerEx.hasRes())) {
                            availableLocales = this.mAssets.getLocales();
                            if (LocaleList.isPseudoLocalesOnly(availableLocales)) {
                                availableLocales = null;
                            }
                        }
                        if (availableLocales != null) {
                            boolean hasDbid = false;
                            if (availableLocales.length >= 4) {
                                int length = availableLocales.length;
                                int i = 0;
                                while (true) {
                                    if (i >= length) {
                                        break;
                                    } else if ("zz-ZX".equals(availableLocales[i])) {
                                        hasDbid = true;
                                        break;
                                    } else {
                                        i++;
                                    }
                                }
                            }
                            if (hasDbid && (shared = HwAssetManagerEx.getSharedResList()) != null) {
                                String[] result = (String[]) Arrays.copyOf(availableLocales, availableLocales.length + shared.length);
                                System.arraycopy(shared, 0, result, availableLocales.length, shared.length);
                                availableLocales = result;
                                if (shared.length > 0) {
                                    changeRes = true;
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
                if (this.mConfiguration.densityDpi != 0) {
                    this.mMetrics.densityDpi = this.mConfiguration.densityDpi;
                    this.mMetrics.density = ((float) this.mConfiguration.densityDpi) * 0.00625f;
                }
                int srcDpi = SystemProperties.getInt("ro.sf.real_lcd_density", SystemProperties.getInt("ro.sf.lcd_density", 0));
                int dpi = SystemProperties.getInt("persist.sys.dpi", srcDpi);
                int rogDpi = SystemProperties.getInt("persist.sys.realdpi", srcDpi);
                if (dpi <= 0) {
                    dpi = srcDpi;
                }
                int screenHeightDp = this.mConfiguration.densityDpi;
                int smallestScreenWidthDp = this.mConfiguration.smallestScreenWidthDp;
                int screenWidthDp = this.mConfiguration.screenWidthDp;
                int screenHeightDp2 = this.mConfiguration.screenHeightDp;
                float f = 1.0f;
                if (dpi != srcDpi && srcDpi != 0 && screenHeightDp != srcDpi && screenHeightDp != ((int) (((((float) (srcDpi * rogDpi)) * 1.0f) / ((float) dpi)) * 1.0f))) {
                    boolean mLockRes = true;
                    Bundle multidpiInfo = HwThemeManager.getPreMultidpiInfo(ActivityThread.currentPackageName());
                    if (multidpiInfo != null) {
                        mLockDpi = multidpiInfo.getBoolean("LockDpi", false);
                        mLockRes = multidpiInfo.getBoolean("LockRes", false);
                        if (screenHeightDp != dpi) {
                            if (screenHeightDp != rogDpi) {
                                isSameDpi = false;
                                if (!mLockDpi && isSameDpi) {
                                    Slog.i(TAG, "LockDpi original config :" + this.mConfiguration + " mLockDpi = " + mLockDpi + " densityDpi = " + screenHeightDp + " dpi = " + dpi);
                                    if (this.mDisplayAdjustments.getCompatibilityInfo().getSdrLowResolutionRatio() <= 1.0f) {
                                        this.mMetrics.setToNonCompat();
                                    } else {
                                        float scale = (((float) dpi) * 1.0f) / ((float) srcDpi);
                                        this.mMetrics.density /= scale;
                                        this.mMetrics.scaledDensity /= scale;
                                        this.mMetrics.densityDpi = (int) ((((float) this.mMetrics.densityDpi) * 1.0f) / scale);
                                        this.mMetrics.xdpi /= scale;
                                        this.mMetrics.ydpi /= scale;
                                    }
                                    this.mConfiguration.densityDpi = (((screenHeightDp * srcDpi) + dpi) - 1) / dpi;
                                    this.mConfiguration.smallestScreenWidthDp = (((smallestScreenWidthDp * dpi) + dpi) - 1) / srcDpi;
                                    this.mConfiguration.screenWidthDp = (((screenWidthDp * dpi) + dpi) - 1) / srcDpi;
                                    this.mConfiguration.screenHeightDp = (((screenHeightDp2 * dpi) + dpi) - 1) / srcDpi;
                                    int densityDpi = this.mConfiguration.densityDpi;
                                    int smallestScreenWidthDp2 = this.mConfiguration.smallestScreenWidthDp;
                                    int screenWidthDp2 = this.mConfiguration.screenWidthDp;
                                    int screenHeightDp3 = this.mConfiguration.screenHeightDp;
                                    this.mIsDpiLocked = true;
                                    smallestScreenWidthDp = smallestScreenWidthDp2;
                                    screenWidthDp = screenWidthDp2;
                                    screenHeightDp2 = screenHeightDp3;
                                    screenHeightDp = densityDpi;
                                }
                            }
                        }
                        isSameDpi = true;
                        if (!mLockDpi) {
                        }
                    } else {
                        mLockDpi = false;
                    }
                    if (!mLockDpi && ((mIsLockResWhitelist && getHwResourcesImpl().isInMultiDpiWhiteList(ActivityThread.currentPackageName())) || (!mIsLockResWhitelist && mLockRes))) {
                        screenHeightDp = (((screenHeightDp * srcDpi) + dpi) - 1) / dpi;
                        smallestScreenWidthDp = (((smallestScreenWidthDp * dpi) + dpi) - 1) / srcDpi;
                        screenWidthDp = (((screenWidthDp * dpi) + dpi) - 1) / srcDpi;
                        screenHeightDp2 = (((screenHeightDp2 * dpi) + dpi) - 1) / srcDpi;
                    }
                }
                DisplayMetrics displayMetrics = this.mMetrics;
                float f2 = this.mMetrics.density;
                if (this.mConfiguration.fontScale != 0.0f) {
                    f = this.mConfiguration.fontScale;
                }
                displayMetrics.scaledDensity = f2 * f;
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
                this.mHwResourcesImpl.updateConfiguration(config, configChanges);
                if ((this.mConfiguration.uiMode & 48) == 32) {
                    ArrayList<String> themepackages = this.mHwResourcesImpl.getDataThemePackages();
                    if (themepackages != null && !themepackages.isEmpty()) {
                        if (themepackages.contains(ActivityThread.currentPackageName())) {
                            this.mConfiguration.uiMode = (config.uiMode & -49) | 16;
                        }
                    }
                }
                String locale = adjustLanguageTag(this.mConfiguration.getLocales().get(0).toLanguageTag());
                if (changeRes) {
                    HwAssetManagerEx.setDbidConfig(locale);
                }
                this.mAssets.setConfiguration(this.mConfiguration.mcc, this.mConfiguration.mnc, locale, this.mConfiguration.orientation, this.mConfiguration.touchscreen, screenHeightDp, this.mConfiguration.keyboard, keyboardHidden, this.mConfiguration.navigation, width, height, smallestScreenWidthDp, screenWidthDp, screenHeightDp2, this.mConfiguration.screenLayout, this.mConfiguration.uiMode, this.mConfiguration.colorMode, Build.VERSION.RESOURCES_SDK_INT);
                this.mDrawableCache.onConfigurationChange(configChanges);
                this.mColorDrawableCache.onConfigurationChange(configChanges);
                this.mComplexColorCache.onConfigurationChange(configChanges);
                this.mAnimatorCache.onConfigurationChange(configChanges);
                this.mStateListAnimatorCache.onConfigurationChange(configChanges);
                flushLayoutCache();
            }
            synchronized (sSync) {
                if (this.mPluralRule != null) {
                    this.mPluralRule = PluralRules.forLocale(this.mConfiguration.getLocales().get(0));
                }
            }
        } finally {
            Trace.traceEnd(8192);
        }
    }

    public int calcConfigChanges(Configuration config) {
        if (config == null) {
            return -1;
        }
        this.mTmpConfig.setTo(config);
        int density = config.densityDpi;
        if (density == 0) {
            if (this.mIsDpiLocked) {
                density = this.mConfiguration.densityDpi;
            } else {
                density = this.mMetrics.noncompatDensityDpi;
            }
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
            Arrays.fill(this.mCachedXmlBlockFiles, (Object) null);
            XmlBlock[] cachedXmlBlocks = this.mCachedXmlBlocks;
            for (int i = 0; i < 4; i++) {
                XmlBlock oldBlock = cachedXmlBlocks[i];
                if (oldBlock != null) {
                    oldBlock.close();
                }
            }
            Arrays.fill(cachedXmlBlocks, (Object) null);
        }
    }

    /* access modifiers changed from: protected */
    public Drawable loadDrawable(Resources wrapper, TypedValue value, int id, int density, Resources.Theme theme) throws Resources.NotFoundException {
        int i;
        Exception e;
        String name;
        boolean isColorDrawable;
        DrawableCache caches;
        long key;
        long key2;
        Drawable.ConstantState cs;
        long key3;
        Drawable dr;
        boolean needsNewDrawableAfterCache;
        Drawable.ConstantState state;
        String name2;
        boolean useCache = density == 0 || value.density == this.mMetrics.densityDpi;
        if (density > 0 && value.density > 0 && value.density != 65535) {
            if (value.density == density) {
                value.density = this.mMetrics.densityDpi;
            } else {
                value.density = (value.density * this.mMetrics.densityDpi) / density;
            }
        }
        try {
            if (value.type < 28 || value.type > 31) {
                DrawableCache caches2 = this.mDrawableCache;
                key = (((long) value.assetCookie) << 32) | ((long) value.data);
                isColorDrawable = false;
                caches = caches2;
            } else {
                DrawableCache caches3 = this.mColorDrawableCache;
                key = (long) value.data;
                isColorDrawable = true;
                caches = caches3;
            }
            if (this.mPreloading || !useCache) {
                key2 = key;
            } else {
                Drawable cachedDrawable = caches.getInstance(key, wrapper, theme);
                if (cachedDrawable != null) {
                    cachedDrawable.setChangingConfigurations(value.changingConfigurations);
                    if (HwFrameworkFactory.getHwActivityThread() != null) {
                        HwFrameworkFactory.getHwActivityThread().hitDrawableCache(id);
                    }
                    if (!(cachedDrawable instanceof VectorDrawable)) {
                        if (!(cachedDrawable instanceof GradientDrawable)) {
                            return cachedDrawable;
                        }
                    }
                    Drawable hwDrawable = this.mHwResourcesImpl.getThemeDrawable(value, id, wrapper, getResourcePackageName(id), value.string.toString());
                    if (hwDrawable != null) {
                        cachedDrawable.setShader(hwDrawable);
                    }
                    return cachedDrawable;
                }
                key2 = key;
            }
            Drawable hwDrawable2 = this.mHwResourcesImpl.loadDrawable(wrapper, value, id, theme, useCache);
            if (hwDrawable2 != null && !(hwDrawable2 instanceof AdaptiveIconDrawable)) {
                if (value.string == null || !value.string.toString().endsWith(".xml")) {
                    return hwDrawable2;
                }
            }
            if (isColorDrawable) {
                key3 = key2;
                cs = sPreloadedColorDrawables.get(key3);
            } else {
                key3 = key2;
                cs = sPreloadedDrawables[this.mConfiguration.getLayoutDirection()].get(key3);
            }
            if (cs != null) {
                if (TRACE_FOR_DETAILED_PRELOAD && (id >>> 24) == 1 && Process.myUid() != 0 && (name2 = getResourceName(id)) != null) {
                    Log.d(TAG_PRELOAD, "Hit preloaded FW drawable #" + Integer.toHexString(id) + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + name2);
                }
                dr = cs.newDrawable(wrapper);
            } else if (isColorDrawable) {
                dr = new ColorDrawable(value.data);
            } else {
                dr = loadDrawableForCookie(wrapper, value, id, density);
                if (hwDrawable2 != null) {
                    if (!(dr instanceof VectorDrawable)) {
                        if (!(dr instanceof GradientDrawable)) {
                            if (!(hwDrawable2 instanceof AdaptiveIconDrawable)) {
                                return hwDrawable2;
                            }
                            dr = hwDrawable2;
                        }
                    }
                    dr.setShader(hwDrawable2);
                }
            }
            if (dr instanceof DrawableContainer) {
                needsNewDrawableAfterCache = true;
            } else {
                needsNewDrawableAfterCache = false;
            }
            boolean canApplyTheme = dr != null && dr.canApplyTheme();
            if (canApplyTheme && theme != null) {
                dr = dr.mutate();
                dr.applyTheme(theme);
                dr.clearMutated();
            }
            Drawable dr2 = this.mHwResourcesImpl.handleAddIconBackground(wrapper, id, dr);
            if (dr2 == null) {
                return dr2;
            }
            dr2.setChangingConfigurations(value.changingConfigurations);
            if (!useCache) {
                return dr2;
            }
            i = 0;
            try {
                cacheDrawable(value, isColorDrawable, caches, theme, canApplyTheme, key3, dr2);
                if (!needsNewDrawableAfterCache || (state = dr2.getConstantState()) == null) {
                    return dr2;
                }
                return state.newDrawable(wrapper);
            } catch (Exception e2) {
                e = e2;
                try {
                    name = getResourceName(id);
                } catch (Resources.NotFoundException e3) {
                    name = "(missing name)";
                }
                getHwResourcesImpl().printErrorResource();
                Resources.NotFoundException nfe = new Resources.NotFoundException("Drawable " + name + " with resource ID #0x" + Integer.toHexString(id), e);
                nfe.setStackTrace(new StackTraceElement[i]);
                throw nfe;
            }
        } catch (Exception e4) {
            e = e4;
            i = 0;
            name = getResourceName(id);
            getHwResourcesImpl().printErrorResource();
            Resources.NotFoundException nfe2 = new Resources.NotFoundException("Drawable " + name + " with resource ID #0x" + Integer.toHexString(id), e);
            nfe2.setStackTrace(new StackTraceElement[i]);
            throw nfe2;
        }
    }

    private void cacheDrawable(TypedValue value, boolean isColorDrawable, DrawableCache caches, Resources.Theme theme, boolean usesTheme, long key, Drawable dr) {
        Drawable.ConstantState cs = dr.getConstantState();
        if (cs != null) {
            if (this.mPreloading) {
                int changingConfigs = cs.getChangingConfigurations();
                if (isColorDrawable) {
                    if (verifyPreloadConfig(changingConfigs, 0, value.resourceId, "drawable")) {
                        sPreloadedColorDrawables.put(key, cs);
                    }
                } else if (!verifyPreloadConfig(changingConfigs, 8192, value.resourceId, "drawable")) {
                } else {
                    if ((changingConfigs & 8192) == 0) {
                        sPreloadedDrawables[0].put(key, cs);
                        sPreloadedDrawables[1].put(key, cs);
                        return;
                    }
                    sPreloadedDrawables[this.mConfiguration.getLayoutDirection()].put(key, cs);
                }
            } else {
                synchronized (this.mAccessLock) {
                    caches.put(key, theme, cs, usesTheme);
                }
            }
        }
    }

    private boolean verifyPreloadConfig(int changingConfigurations, int allowVarying, int resourceId, String name) {
        String resName;
        if ((-1073745921 & changingConfigurations & (~allowVarying)) == 0) {
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
        long startTime;
        int startDrawableCount;
        int startBitmapCount;
        long startBitmapSize;
        String file;
        Throwable e;
        LookupStack stack;
        Throwable th;
        Drawable dr;
        String file2;
        int i;
        String str;
        Drawable drCache;
        if (value.string == null) {
            getHwResourcesImpl().printErrorResource();
            throw new Resources.NotFoundException("Resource \"" + getResourceName(id) + "\" (" + Integer.toHexString(id) + ") is not a Drawable (color or path): " + value);
        } else if (HwFrameworkFactory.getHwActivityThread() == null || (drCache = HwFrameworkFactory.getHwActivityThread().getCacheDrawableFromAware(id, wrapper, value.assetCookie, this.mAssets)) == null) {
            long beginTime = System.nanoTime();
            String file3 = value.string.toString();
            if (TRACE_FOR_DETAILED_PRELOAD) {
                startTime = System.nanoTime();
                startBitmapCount = Bitmap.sPreloadTracingNumInstantiatedBitmaps;
                startBitmapSize = Bitmap.sPreloadTracingTotalBitmapsSize;
                startDrawableCount = sPreloadTracingNumLoadedDrawables;
            } else {
                startTime = 0;
                startBitmapCount = 0;
                startBitmapSize = 0;
                startDrawableCount = 0;
            }
            Trace.traceBegin(8192, file3);
            LookupStack stack2 = this.mLookupStack.get();
            if (!stack2.contains(id)) {
                stack2.push(id);
                try {
                    if (file3.endsWith(".xml")) {
                        try {
                            if (file3.startsWith("res/color/")) {
                                dr = loadColorOrXmlDrawable(wrapper, value, id, density, file3);
                            } else {
                                dr = loadXmlDrawable(wrapper, value, id, density, file3);
                            }
                        } catch (Throwable th2) {
                            th = th2;
                            file = file3;
                            stack = stack2;
                            try {
                                stack.pop();
                                throw th;
                            } catch (Exception | StackOverflowError e2) {
                                e = e2;
                                Trace.traceEnd(8192);
                                getHwResourcesImpl().printErrorResource();
                                Resources.NotFoundException rnf = new Resources.NotFoundException("File " + file + " from drawable resource ID #0x" + Integer.toHexString(id));
                                rnf.initCause(e);
                                throw rnf;
                            }
                        }
                    } else {
                        InputStream is = this.mAssets.openNonAsset(value.assetCookie, file3, 2);
                        AssetManager.AssetInputStream ais = (AssetManager.AssetInputStream) is;
                        if (HwPCUtils.isValidExtDisplayId(HwPCUtils.getPCDisplayID())) {
                            dr = Drawable.createFromResourceStream(wrapper, value, is, file3);
                        } else {
                            dr = decodeImageDrawable(ais, wrapper, value);
                        }
                    }
                    try {
                        stack2.pop();
                        Trace.traceEnd(8192);
                        if (HwFrameworkFactory.getHwActivityThread() != null) {
                            file2 = file3;
                            i = id;
                            HwFrameworkFactory.getHwActivityThread().postCacheDrawableToAware(id, wrapper, System.nanoTime() - beginTime, value.assetCookie, this.mAssets);
                        } else {
                            file2 = file3;
                            i = id;
                        }
                        if (TRACE_FOR_DETAILED_PRELOAD) {
                            boolean isRoot = true;
                            if ((i >>> 24) == 1) {
                                String name = getResourceName(i);
                                if (name != null) {
                                    long time = System.nanoTime() - startTime;
                                    int loadedBitmapCount = Bitmap.sPreloadTracingNumInstantiatedBitmaps - startBitmapCount;
                                    long loadedBitmapSize = Bitmap.sPreloadTracingTotalBitmapsSize - startBitmapSize;
                                    int i2 = sPreloadTracingNumLoadedDrawables;
                                    int loadedDrawables = i2 - startDrawableCount;
                                    sPreloadTracingNumLoadedDrawables = i2 + 1;
                                    if (Process.myUid() != 0) {
                                        isRoot = false;
                                    }
                                    StringBuilder sb = new StringBuilder();
                                    if (isRoot) {
                                        str = "Preloaded FW drawable #";
                                    } else {
                                        str = "Loaded non-preloaded FW drawable #";
                                    }
                                    sb.append(str);
                                    sb.append(Integer.toHexString(id));
                                    sb.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
                                    sb.append(name);
                                    sb.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
                                    sb.append(file2);
                                    sb.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
                                    sb.append(dr.getClass().getCanonicalName());
                                    sb.append(" #nested_drawables= ");
                                    sb.append(loadedDrawables);
                                    sb.append(" #bitmaps= ");
                                    sb.append(loadedBitmapCount);
                                    sb.append(" total_bitmap_size= ");
                                    sb.append(loadedBitmapSize);
                                    sb.append(" in[us] ");
                                    sb.append(time / 1000);
                                    Log.d(TAG_PRELOAD, sb.toString());
                                }
                            }
                        }
                        return dr;
                    } catch (Exception | StackOverflowError e3) {
                        e = e3;
                        file = file3;
                        Trace.traceEnd(8192);
                        getHwResourcesImpl().printErrorResource();
                        Resources.NotFoundException rnf2 = new Resources.NotFoundException("File " + file + " from drawable resource ID #0x" + Integer.toHexString(id));
                        rnf2.initCause(e);
                        throw rnf2;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    file = file3;
                    stack = stack2;
                    stack.pop();
                    throw th;
                }
            } else {
                throw new Exception("Recursive reference in drawable");
            }
        } else {
            drCache.setChangingConfigurations(value.changingConfigurations);
            return drCache;
        }
    }

    private Drawable loadColorOrXmlDrawable(Resources wrapper, TypedValue value, int id, int density, String file) {
        try {
            return new ColorStateListDrawable(loadColorStateList(wrapper, value, id, null));
        } catch (Resources.NotFoundException originalException) {
            try {
                return loadXmlDrawable(wrapper, value, id, density, file);
            } catch (Exception e) {
                throw originalException;
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0017, code lost:
        if (r0 != null) goto L_0x0019;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:?, code lost:
        r0.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x001d, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x001e, code lost:
        r1.addSuppressed(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0021, code lost:
        throw r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0016, code lost:
        r2 = move-exception;
     */
    private Drawable loadXmlDrawable(Resources wrapper, TypedValue value, int id, int density, String file) throws IOException, XmlPullParserException {
        XmlResourceParser rp = loadXmlResourceParser(file, id, value.assetCookie, "drawable");
        Drawable createFromXmlForDensity = Drawable.createFromXmlForDensity(wrapper, rp, density, null);
        if (rp != null) {
            rp.close();
        }
        return createFromXmlForDensity;
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
                Typeface build = new Typeface.Builder(this.mAssets, file, false, value.assetCookie).build();
                Trace.traceEnd(8192);
                return build;
            } catch (XmlPullParserException e) {
                Log.e(TAG, "Failed to parse xml resource " + file, e);
            } catch (IOException e2) {
                Log.e(TAG, "Failed to read xml resource " + file, e2);
            } catch (Throwable th) {
                Trace.traceEnd(8192);
                throw th;
            }
        } else {
            getHwResourcesImpl().printErrorResource();
            throw new Resources.NotFoundException("Resource \"" + getResourceName(id) + "\" (" + Integer.toHexString(id) + ") is not a Font: " + value);
        }
        Trace.traceEnd(8192);
        return null;
    }

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
            } else if (verifyPreloadConfig(complexColor.getChangingConfigurations(), 0, value.resourceId, "color")) {
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
                getHwResourcesImpl().printErrorResource();
                Resources.NotFoundException rnf = new Resources.NotFoundException("File " + file + " from complex color resource ID #0x" + Integer.toHexString(id));
                rnf.initCause(e);
                throw rnf;
            }
        } else {
            getHwResourcesImpl().printErrorResource();
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
        getHwResourcesImpl().printErrorResource();
        throw new Resources.NotFoundException("Can't find ColorStateList from drawable resource ID #0x" + Integer.toHexString(id));
    }

    private ColorStateList getColorStateListFromInt(TypedValue value, long key) {
        ConstantState<ComplexColor> factory = sPreloadedComplexColors.get(key);
        if (factory != null) {
            return (ColorStateList) factory.newInstance();
        }
        ColorStateList csl = ColorStateList.valueOf(value.data);
        if (this.mPreloading && verifyPreloadConfig(value.changingConfigurations, 0, value.resourceId, "color")) {
            sPreloadedComplexColors.put(key, csl.getConstantState());
        }
        return csl;
    }

    private ComplexColor loadComplexColorForCookie(Resources wrapper, TypedValue value, int id, Resources.Theme theme) {
        XmlResourceParser parser;
        int type;
        if (value.string != null) {
            String file = value.string.toString();
            ComplexColor complexColor = null;
            Trace.traceBegin(8192, file);
            if (file.endsWith(".xml")) {
                try {
                    parser = loadXmlResourceParser(file, id, value.assetCookie, "ComplexColor");
                    AttributeSet attrs = Xml.asAttributeSet(parser);
                    if (type == 2) {
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
                } catch (Exception e) {
                    Trace.traceEnd(8192);
                    getHwResourcesImpl().printErrorResource();
                    Resources.NotFoundException rnf = new Resources.NotFoundException("File " + file + " from ComplexColor resource ID #0x" + Integer.toHexString(id));
                    rnf.initCause(e);
                    throw rnf;
                }
                while (true) {
                    type = parser.next();
                    if (type == 2 || type == 1) {
                        break;
                    }
                }
            } else {
                Trace.traceEnd(8192);
                getHwResourcesImpl().printErrorResource();
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
                    for (int i = 0; i < num; i++) {
                        if (cachedXmlBlockCookies[i] == assetCookie && cachedXmlBlockFiles[i] != null && cachedXmlBlockFiles[i].equals(file)) {
                            return cachedXmlBlocks[i].newParser(id);
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
                        return block.newParser(id);
                    }
                }
            } catch (Exception e) {
                getHwResourcesImpl().printErrorResource();
                Resources.NotFoundException rnf = new Resources.NotFoundException("File " + file + " from xml type " + type + " resource ID #0x" + Integer.toHexString(id));
                rnf.initCause(e);
                throw rnf;
            }
        }
        getHwResourcesImpl().printErrorResource();
        throw new Resources.NotFoundException("File " + file + " from xml type " + type + " resource ID #0x" + Integer.toHexString(id));
    }

    public final void startPreloading() {
        synchronized (sSync) {
            if (!sPreloaded) {
                sPreloaded = true;
                this.mPreloading = true;
                this.mConfiguration.densityDpi = DisplayMetrics.DENSITY_DEVICE;
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
                long count = ((long) Bitmap.sPreloadTracingNumInstantiatedBitmaps) - this.mPreloadTracingStartBitmapCount;
                Log.d(TAG_PRELOAD, "Preload finished, " + count + " bitmaps of " + size + " bytes in " + time + " ms");
            }
            this.mPreloading = false;
            flushLayoutCache();
        }
    }

    static int getAttributeSetSourceResId(AttributeSet set) {
        if (set == null || !(set instanceof XmlBlock.Parser)) {
            return 0;
        }
        return ((XmlBlock.Parser) set).getSourceResId();
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

    public class ThemeImpl {
        private final AssetManager mAssets;
        private final Resources.ThemeKey mKey = new Resources.ThemeKey();
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
                    this.mAssets.setThemeTo(this.mTheme, other.mAssets, other.mTheme);
                    this.mThemeResId = other.mThemeResId;
                    this.mKey.setTo(other.getKey());
                }
            }
        }

        /* access modifiers changed from: package-private */
        public TypedArray obtainStyledAttributes(Resources.Theme wrapper, AttributeSet set, int[] attrs, int defStyleAttr, int defStyleRes) {
            synchronized (this.mKey) {
                try {
                    TypedArray array = TypedArray.obtain(wrapper.getResources(), attrs.length);
                    XmlBlock.Parser parser = (XmlBlock.Parser) set;
                    this.mAssets.applyStyle(this.mTheme, defStyleAttr, defStyleRes, parser, attrs, array.mDataAddress, array.mIndicesAddress);
                    array.mTheme = wrapper;
                    array.mXml = parser;
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

        public int[] getAttributeResolutionStack(int defStyleAttr, int defStyleRes, int explicitStyleRes) {
            int[] attributeResolutionStack;
            synchronized (this.mKey) {
                attributeResolutionStack = this.mAssets.getAttributeResolutionStack(this.mTheme, defStyleAttr, defStyleRes, explicitStyleRes);
            }
            return attributeResolutionStack;
        }
    }

    /* access modifiers changed from: private */
    public static class LookupStack {
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
}
