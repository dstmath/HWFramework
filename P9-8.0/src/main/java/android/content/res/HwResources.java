package android.content.res;

import android.annotation.SuppressLint;
import android.content.res.AbsResourcesImpl.ThemeColor;
import android.content.res.Resources.NotFoundException;
import android.content.res.Resources.Theme;
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

    public Bitmap getThemeBitmap(TypedValue value, int id, Rect padding) throws NotFoundException {
        if (getImpl().mPreloading) {
            return null;
        }
        String packageName = getImpl().getHwResourcesImpl().getThemeResource(id, null).packageName;
        if (value.string == null) {
            return null;
        }
        String file = value.string.toString().replaceFirst("-v\\d+/", "/");
        if (file.isEmpty()) {
            return null;
        }
        Bitmap bmp = null;
        if (packageName.indexOf(FRAMEWORK_RES) >= 0 && file.indexOf("_holo") >= 0) {
            return null;
        }
        int index;
        int themeDensity;
        String dir;
        boolean isLand = file.indexOf("-land") >= 0;
        boolean isFramework = packageName.equals(FRAMEWORK_RES);
        boolean isHwFramework = packageName.equals(FRAMEWORK_RES_EXT);
        String deepThemeType = getImpl().getHwResourcesImpl().getDeepThemeType();
        if (!((!isFramework && !isHwFramework) || this.mPackageName == null || (this.mPackageName.isEmpty() ^ 1) == 0)) {
            ZipFileCache frameworkZipFileCache = ZipFileCache.getAndCheckCachedZipFile(getImpl().getHwResourcesImpl().getThemeDir(), this.mPackageName);
            String key = packageName + "/" + file;
            if (frameworkZipFileCache != null) {
                bmp = frameworkZipFileCache.getBitmapEntry(this, value, key, padding);
                if (bmp == null && (file.contains(DRAWABLE_FHD) ^ 1) != 0) {
                    frameworkZipFileCache.initResDirInfo();
                    index = isFramework ? isLand ? 3 : 2 : isLand ? 5 : 4;
                    themeDensity = frameworkZipFileCache.getDrawableDensity(index);
                    dir = frameworkZipFileCache.getDrawableDir(index);
                    if (!(themeDensity == -1 || dir == null)) {
                        bmp = frameworkZipFileCache.getBitmapEntry(this, value, dir + File.separator + file.substring(file.lastIndexOf(File.separator) + 1), padding);
                    }
                }
                if (bmp != null) {
                    return bmp;
                }
            }
            if (!TextUtils.isEmpty(deepThemeType) && bmp == null && frameworkZipFileCache == null) {
                bmp = getFwkBitmapFromAsset(value, this, packageName, file, key, deepThemeType, padding);
            }
            if (bmp != null) {
                return bmp;
            }
        }
        ZipFileCache packageZipFileCache = ZipFileCache.getAndCheckCachedZipFile(getImpl().getHwResourcesImpl().getThemeDir(), packageName);
        if (packageZipFileCache != null) {
            bmp = packageZipFileCache.getBitmapEntry(this, value, file, padding);
        }
        if (!(bmp != null || packageZipFileCache == null || (file.contains(DRAWABLE_FHD) ^ 1) == 0)) {
            packageZipFileCache.initResDirInfo();
            index = file.indexOf("-land") >= 0 ? 1 : 0;
            themeDensity = packageZipFileCache.getDrawableDensity(index);
            dir = packageZipFileCache.getDrawableDir(index);
            if (!(themeDensity == -1 || dir == null)) {
                bmp = packageZipFileCache.getBitmapEntry(this, value, dir + File.separator + file.substring(file.lastIndexOf(File.separator) + 1), padding);
            }
        }
        if (!TextUtils.isEmpty(deepThemeType) && bmp == null && packageZipFileCache == null) {
            if (isFramework) {
                isHwFramework = true;
            }
            if ((isHwFramework ^ 1) != 0) {
                bmp = getAppBitmapFromAsset(value, this, file, deepThemeType, padding);
            }
        }
        return bmp;
    }

    @SuppressLint({"AvoidInHardConnectInString"})
    private Bitmap getFwkBitmapFromAsset(TypedValue value, Resources res, String packageName, String file, String key, String dir, Rect padding) {
        Bitmap btm = AssetsFileCache.getBitmapEntry(getAssets(), res, value, dir + File.separator + key, padding);
        if (btm != null || (file.contains(DRAWABLE_FHD) ^ 1) == 0) {
            return btm;
        }
        StringBuilder name = new StringBuilder();
        name.append(dir);
        name.append(File.separator);
        name.append(packageName);
        name.append(File.separator);
        name.append(DEFAULT_RES_XX_DIR);
        name.append(File.separator);
        name.append(file.substring(file.lastIndexOf(File.separator) + 1));
        return AssetsFileCache.getBitmapEntry(getAssets(), res, value, name.toString(), padding);
    }

    @SuppressLint({"AvoidInHardConnectInString"})
    private Bitmap getAppBitmapFromAsset(TypedValue value, Resources res, String file, String dir, Rect rect) {
        Bitmap btmp = AssetsFileCache.getBitmapEntry(getAssets(), res, value, dir + File.separator + file, null);
        if (btmp != null || (file.contains(DRAWABLE_FHD) ^ 1) == 0) {
            return btmp;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(dir);
        sb.append(File.separator);
        sb.append(DEFAULT_RES_XX_DIR);
        sb.append(File.separator);
        sb.append(file.substring(file.lastIndexOf(File.separator) + 1));
        return AssetsFileCache.getBitmapEntry(getAssets(), res, value, sb.toString(), rect);
    }

    public Bitmap getThemeBitmap(TypedValue value, int id) throws NotFoundException {
        return getThemeBitmap(value, id, null);
    }

    protected ColorStateList loadColorStateList(TypedValue value, int id, Theme theme) throws NotFoundException {
        boolean isThemeColor = false;
        ColorStateList csl = null;
        if (id != 0) {
            ThemeColor colorValue = getThemeColor(value, id);
            if (colorValue != null) {
                isThemeColor = colorValue.mIsThemed;
                csl = ColorStateList.valueOf(colorValue.mColor);
            }
        }
        if (isThemeColor) {
            return csl;
        }
        return super.loadColorStateList(value, id, theme);
    }

    public int getColor(int id, Theme theme) throws NotFoundException {
        TypedValue value = obtainTempTypedValue();
        try {
            getValue(id, value, true);
            int color;
            if (value.type < 16 || value.type > 31) {
                color = super.getColor(id, theme);
                releaseTempTypedValue(value);
                return color;
            }
            ThemeColor themecolor = getThemeColor(value, id);
            if (themecolor != null) {
                int colorVaue = themecolor.mColor;
                return colorVaue;
            }
            color = value.data;
            releaseTempTypedValue(value);
            return color;
        } finally {
            releaseTempTypedValue(value);
        }
    }

    public ThemeColor getThemeColor(TypedValue value, int id) {
        ResourcesImpl impl = getImpl();
        if (impl == null) {
            return null;
        }
        impl.getHwResourcesImpl().setResourcesPackageName(getPackageName());
        return impl.getHwResourcesImpl().getThemeColor(value, id);
    }

    public HwResources(AssetManager assets, DisplayMetrics metrics, Configuration config) {
        super(assets, metrics, config);
        setIsSRLocale("sr".equals(Locale.getDefault().getLanguage()));
        getImpl().getHwResourcesImpl().checkChangedNameFile();
    }

    public HwResources(ClassLoader classLoader) {
        super(classLoader);
    }

    public HwResources(boolean system) {
        this.system = system;
        getImpl().getHwResourcesImpl().initResource();
    }

    public HwResources(AssetManager assets, DisplayMetrics metrics, Configuration config, DisplayAdjustments displayAdjustments, IBinder token) {
        super(assets, metrics, config, displayAdjustments);
        setIsSRLocale("sr".equals(Locale.getDefault().getLanguage()));
        getImpl().getHwResourcesImpl().checkChangedNameFile();
    }

    public HwResources() {
        getImpl().getHwResourcesImpl().initResource();
    }

    public Drawable getDrawableForDynamic(String packageName, String iconName) throws NotFoundException {
        return getImpl().getHwResourcesImpl().getDrawableForDynamic(this, packageName, iconName);
    }

    protected CharSequence serbianSyrillic2Latin(CharSequence res) {
        if (sSerbiaLocale) {
            return HwTextUtils.serbianSyrillic2Latin(res);
        }
        return res;
    }

    protected CharSequence[] serbianSyrillic2Latin(CharSequence[] res) {
        if (sSerbiaLocale) {
            for (int i = 0; i < res.length; i++) {
                res[i] = HwTextUtils.serbianSyrillic2Latin(res[i]);
            }
        }
        return res;
    }

    protected String serbianSyrillic2Latin(String res) {
        if (sSerbiaLocale) {
            return HwTextUtils.serbianSyrillic2Latin(res);
        }
        return res;
    }

    protected String[] serbianSyrillic2Latin(String[] res) {
        if (sSerbiaLocale) {
            for (int i = 0; i < res.length; i++) {
                res[i] = HwTextUtils.serbianSyrillic2Latin(res[i]);
            }
        }
        return res;
    }

    protected boolean isSRLocale() {
        return sSerbiaLocale;
    }

    protected static void setIsSRLocale(boolean isSerbia) {
        if (sSerbiaLocale != isSerbia) {
            sSerbiaLocale = isSerbia;
        }
    }

    public CharSequence getText(int id) throws NotFoundException {
        CharSequence res = super.getText(id);
        if (res != null) {
            return serbianSyrillic2Latin(res);
        }
        throw new NotFoundException("String resource ID #0x" + Integer.toHexString(id));
    }

    public CharSequence getText(int id, CharSequence def) {
        return serbianSyrillic2Latin(super.getText(id, def));
    }

    public CharSequence[] getTextArray(int id) throws NotFoundException {
        CharSequence[] res = super.getTextArray(id);
        if (res != null) {
            return serbianSyrillic2Latin(res);
        }
        throw new NotFoundException("Text array resource ID #0x" + Integer.toHexString(id));
    }

    public String[] getStringArray(int id) throws NotFoundException {
        String[] res = super.getStringArray(id);
        if (res != null) {
            return serbianSyrillic2Latin(res);
        }
        throw new NotFoundException("String array resource ID #0x" + Integer.toHexString(id));
    }
}
