package android.content.res;

import android.annotation.SuppressLint;
import android.content.res.AbsResourcesImpl;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.DisplayAdjustments;
import com.huawei.android.text.HwTextUtils;
import huawei.android.hwutil.AssetsFileCache;
import huawei.android.hwutil.ZipFileCache;
import java.io.File;
import java.util.Locale;

public class HwResources extends Resources {
    private static final boolean DEBUG_DRAWABLE = false;
    private static final String DEFAULT_RES_XX_DIR = "res/drawable-xxhdpi";
    private static final String DRAWABLE_FHD = "drawable-xxhdpi";
    private static final String FRAMEWORK_RES = "framework-res";
    private static final String FRAMEWORK_RES_EXT = "framework-res-hwext";
    static final String TAG = "HwResources";
    private static boolean sSerbiaLocale = false;
    protected String mPackageName = null;
    private boolean system = false;

    public ResourcesImpl getImpl() {
        return super.getImpl();
    }

    public void setPackageName(String name) {
        this.mPackageName = name;
    }

    public String getPackageName() {
        return this.mPackageName;
    }

    public Bitmap getThemeBitmap(TypedValue value, int id, Rect padding) throws Resources.NotFoundException {
        String deepThemeType;
        int i;
        Bitmap bmp;
        int index;
        TypedValue typedValue = value;
        Rect rect = padding;
        if (getImpl().mPreloading) {
            return null;
        }
        String packageName = getImpl().getHwResourcesImpl().getThemeResource(id, null).packageName;
        if (typedValue.string == null) {
            return null;
        }
        String file = typedValue.string.toString().replaceFirst("-v\\d+/", "/");
        if (file.isEmpty()) {
            return null;
        }
        Bitmap bmp2 = null;
        if (packageName.indexOf(FRAMEWORK_RES) >= 0 && file.indexOf("_holo") >= 0) {
            return null;
        }
        boolean isLand = file.indexOf("-land") >= 0;
        boolean isFramework = packageName.equals(FRAMEWORK_RES);
        boolean isHwFramework = packageName.equals(FRAMEWORK_RES_EXT);
        String deepThemeType2 = getImpl().getHwResourcesImpl().getDeepThemeType();
        if (!isFramework && !isHwFramework) {
            i = -1;
            deepThemeType = deepThemeType2;
        } else if (this.mPackageName == null || this.mPackageName.isEmpty()) {
            i = -1;
            deepThemeType = deepThemeType2;
        } else {
            ZipFileCache frameworkZipFileCache = ZipFileCache.getAndCheckCachedZipFile(getImpl().getHwResourcesImpl().getThemeDir(), this.mPackageName);
            String key = packageName + "/" + file;
            if (frameworkZipFileCache != null) {
                bmp2 = frameworkZipFileCache.getBitmapEntry(this, typedValue, key, rect);
                if (bmp2 == null && !file.contains(DRAWABLE_FHD)) {
                    frameworkZipFileCache.initResDirInfo();
                    if (isFramework) {
                        index = isLand ? 3 : 2;
                    } else {
                        index = isLand ? 5 : 4;
                    }
                    int themeDensity = frameworkZipFileCache.getDrawableDensity(index);
                    String dir = frameworkZipFileCache.getDrawableDir(index);
                    if (!(themeDensity == -1 || dir == null)) {
                        bmp2 = frameworkZipFileCache.getBitmapEntry(this, typedValue, dir + File.separator + file.substring(file.lastIndexOf(File.separator) + 1), rect);
                    }
                }
                if (bmp2 != null) {
                    return bmp2;
                }
            }
            Bitmap bmp3 = bmp2;
            if (!TextUtils.isEmpty(deepThemeType2) && bmp3 == null && frameworkZipFileCache == null) {
                ZipFileCache zipFileCache = frameworkZipFileCache;
                i = -1;
                deepThemeType = deepThemeType2;
                bmp = getFwkBitmapFromAsset(typedValue, this, packageName, file, key, deepThemeType2, rect);
            } else {
                ZipFileCache zipFileCache2 = frameworkZipFileCache;
                deepThemeType = deepThemeType2;
                i = -1;
                bmp = bmp3;
            }
            if (bmp2 != null) {
                return bmp2;
            }
        }
        ZipFileCache packageZipFileCache = ZipFileCache.getAndCheckCachedZipFile(getImpl().getHwResourcesImpl().getThemeDir(), packageName);
        if (packageZipFileCache != null) {
            bmp2 = packageZipFileCache.getBitmapEntry(this, typedValue, file, rect);
        }
        if (bmp2 == null && packageZipFileCache != null && !file.contains(DRAWABLE_FHD)) {
            packageZipFileCache.initResDirInfo();
            int index2 = file.indexOf("-land") >= 0 ? 1 : 0;
            int themeDensity2 = packageZipFileCache.getDrawableDensity(index2);
            String dir2 = packageZipFileCache.getDrawableDir(index2);
            if (!(themeDensity2 == i || dir2 == null)) {
                bmp2 = packageZipFileCache.getBitmapEntry(this, typedValue, dir2 + File.separator + file.substring(file.lastIndexOf(File.separator) + 1), rect);
            }
        }
        Bitmap bmp4 = bmp2;
        String deepThemeType3 = deepThemeType;
        if (!TextUtils.isEmpty(deepThemeType3) && bmp4 == null && packageZipFileCache == null && !isFramework && !isHwFramework) {
            bmp4 = getAppBitmapFromAsset(typedValue, this, file, deepThemeType3, rect);
        }
        return bmp4;
    }

    @SuppressLint({"AvoidInHardConnectInString"})
    private Bitmap getFwkBitmapFromAsset(TypedValue value, Resources res, String packageName, String file, String key, String dir, Rect padding) {
        AssetManager assets = getAssets();
        Bitmap btm = AssetsFileCache.getBitmapEntry(assets, res, value, dir + File.separator + key, padding);
        if (btm != null || file.contains(DRAWABLE_FHD)) {
            return btm;
        }
        return AssetsFileCache.getBitmapEntry(getAssets(), res, value, dir + File.separator + packageName + File.separator + DEFAULT_RES_XX_DIR + File.separator + file.substring(file.lastIndexOf(File.separator) + 1), padding);
    }

    @SuppressLint({"AvoidInHardConnectInString"})
    private Bitmap getAppBitmapFromAsset(TypedValue value, Resources res, String file, String dir, Rect rect) {
        AssetManager assets = getAssets();
        Bitmap btmp = AssetsFileCache.getBitmapEntry(assets, res, value, dir + File.separator + file, null);
        if (btmp != null || file.contains(DRAWABLE_FHD)) {
            return btmp;
        }
        return AssetsFileCache.getBitmapEntry(getAssets(), res, value, dir + File.separator + DEFAULT_RES_XX_DIR + File.separator + file.substring(file.lastIndexOf(File.separator) + 1), rect);
    }

    public Bitmap getThemeBitmap(TypedValue value, int id) throws Resources.NotFoundException {
        return getThemeBitmap(value, id, null);
    }

    /* access modifiers changed from: protected */
    public ColorStateList loadColorStateList(TypedValue value, int id, Resources.Theme theme) throws Resources.NotFoundException {
        boolean isThemeColor = false;
        ColorStateList csl = null;
        if (id != 0) {
            AbsResourcesImpl.ThemeColor colorValue = getThemeColor(value, id);
            if (colorValue != null) {
                isThemeColor = colorValue.mIsThemed;
                if (isThemeColor) {
                    csl = ColorStateList.valueOf(colorValue.mColor);
                }
            }
        }
        if (!isThemeColor) {
            return super.loadColorStateList(value, id, theme);
        }
        return csl;
    }

    public int getColor(int id, Resources.Theme theme) throws Resources.NotFoundException {
        TypedValue value = hwObtainTempTypedValue();
        try {
            getValue(id, value, true);
            if (value.type < 16 || value.type > 31) {
                int color = super.getColor(id, theme);
                hwReleaseTempTypedValue(value);
                return color;
            }
            AbsResourcesImpl.ThemeColor themecolor = getThemeColor(value, id);
            if (themecolor != null) {
                return themecolor.mColor;
            }
            int colorVaue = value.data;
            hwReleaseTempTypedValue(value);
            return colorVaue;
        } finally {
            hwReleaseTempTypedValue(value);
        }
    }

    public AbsResourcesImpl.ThemeColor getThemeColor(TypedValue value, int id) {
        ResourcesImpl impl = getImpl();
        if (impl == null) {
            return null;
        }
        impl.getHwResourcesImpl().setPackageName(getPackageName());
        return impl.getHwResourcesImpl().getThemeColor(value, id);
    }

    public HwResources(AssetManager assets, DisplayMetrics metrics, Configuration config) {
        super(assets, metrics, config);
        setIsSRLocale("sr".equals(Locale.getDefault().getLanguage()));
    }

    public HwResources(ClassLoader classLoader) {
        super(classLoader);
    }

    public HwResources(boolean system2) {
        this.system = system2;
        getImpl().getHwResourcesImpl().initResource();
    }

    public HwResources(AssetManager assets, DisplayMetrics metrics, Configuration config, DisplayAdjustments displayAdjustments, IBinder token) {
        super(assets, metrics, config, displayAdjustments);
        setIsSRLocale("sr".equals(Locale.getDefault().getLanguage()));
    }

    public HwResources() {
        getImpl().getHwResourcesImpl().initResource();
    }

    public Drawable getDrawableForDynamic(String packageName, String iconName) throws Resources.NotFoundException {
        return getImpl().getHwResourcesImpl().getDrawableForDynamic(this, packageName, iconName);
    }

    /* access modifiers changed from: protected */
    public CharSequence serbianSyrillic2Latin(CharSequence res) {
        if (sSerbiaLocale) {
            return HwTextUtils.serbianSyrillic2Latin(res);
        }
        return res;
    }

    /* access modifiers changed from: protected */
    public CharSequence[] serbianSyrillic2Latin(CharSequence[] res) {
        if (sSerbiaLocale) {
            for (int i = 0; i < res.length; i++) {
                res[i] = HwTextUtils.serbianSyrillic2Latin(res[i]);
            }
        }
        return res;
    }

    /* access modifiers changed from: protected */
    public String serbianSyrillic2Latin(String res) {
        if (sSerbiaLocale) {
            return HwTextUtils.serbianSyrillic2Latin(res);
        }
        return res;
    }

    /* access modifiers changed from: protected */
    public String[] serbianSyrillic2Latin(String[] res) {
        if (sSerbiaLocale) {
            for (int i = 0; i < res.length; i++) {
                res[i] = HwTextUtils.serbianSyrillic2Latin(res[i]);
            }
        }
        return res;
    }

    /* access modifiers changed from: protected */
    public boolean isSRLocale() {
        return sSerbiaLocale;
    }

    protected static void setIsSRLocale(boolean isSerbia) {
        if (sSerbiaLocale != isSerbia) {
            sSerbiaLocale = isSerbia;
        }
    }

    public CharSequence getText(int id) throws Resources.NotFoundException {
        CharSequence res = super.getText(id);
        if (res != null) {
            return serbianSyrillic2Latin(res);
        }
        throw new Resources.NotFoundException("String resource ID #0x" + Integer.toHexString(id));
    }

    public CharSequence getText(int id, CharSequence def) {
        return serbianSyrillic2Latin(super.getText(id, def));
    }

    public CharSequence[] getTextArray(int id) throws Resources.NotFoundException {
        CharSequence[] res = super.getTextArray(id);
        if (res != null) {
            return serbianSyrillic2Latin(res);
        }
        throw new Resources.NotFoundException("Text array resource ID #0x" + Integer.toHexString(id));
    }

    public String[] getStringArray(int id) throws Resources.NotFoundException {
        String[] res = super.getStringArray(id);
        if (res != null) {
            return serbianSyrillic2Latin(res);
        }
        throw new Resources.NotFoundException("String array resource ID #0x" + Integer.toHexString(id));
    }
}
