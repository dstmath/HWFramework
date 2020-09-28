package android.content.res;

import android.content.res.AbsResourcesImpl;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.hwtheme.HwThemeManager;
import android.os.Environment;
import android.os.Process;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.LongSparseArray;
import android.util.SparseArray;
import android.util.TypedValue;
import android.util.Xml;
import com.huawei.android.content.res.AssetManagerEx;
import com.huawei.android.content.res.ConfigurationAdapter;
import com.huawei.android.content.res.ResourcesImplAdapter;
import com.huawei.android.internal.util.XmlUtilsEx;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.os.UserHandleEx;
import huawei.android.hwutil.AssetsFileCache;
import huawei.android.hwutil.IconBitmapUtils;
import huawei.android.hwutil.IconCache;
import huawei.android.hwutil.ZipFileCache;
import huawei.android.view.HwMotionEvent;
import huawei.cust.HwCfgFilePolicy;
import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class HwResourcesImpl extends AbsResourcesImpl {
    private static final String ANDROID_RES = "android";
    private static final String ANDROID_RES_EXT = "androidhwext";
    private static final Object CACHE_LOCK = new Object();
    private static final String COLON = ":";
    private static final int COLOR_FOR_DARKER_ICON = -657931;
    private static final int CONFIG_APP_BIG_ICON_SIZE = SystemPropertiesEx.getInt("ro.config.app_big_icon_size", -1);
    private static final int CONFIG_APP_INNER_ICON_SIZE = SystemPropertiesEx.getInt("ro.config.app_inner_icon_size", -1);
    private static final String CUSTOM_DIFF_THEME_DIR = SystemPropertiesEx.get("ro.config.diff_themes");
    private static final String DEFAULT_CONFIG_NAME = "xml/hw_launcher_load_icon.xml";
    private static final int DEFAULT_EDGE_SIZE = 8;
    private static final int DEFAULT_INVALID_INTEGER = -1;
    private static final String DEFAULT_MULTI_DPI_WHITELIST = "/system/etc/xml/multidpi_whitelist.xml";
    private static final String DEFAULT_RES_XX_DIR = "res/drawable-xxhdpi";
    private static final String DIFF_THEME_ICON = "/themes/diff/icons";
    private static final Pattern DIMEN_PATTERN = Pattern.compile("[A-Za-z]");
    private static final String DPI_2K = "xxxhdpi";
    private static final String DPI_FHD = "xxhdpi";
    private static final String DRAWABLE_FHD = "drawable-xxhdpi";
    private static final String DYNAMIC_ICONS = "dynamic_icons";
    private static final String EMUI = "EMUI";
    private static final String FILE_DESCRIPTION = "description.xml";
    private static final String FILE_MULTI_DPI_WHITELIST = "xml/multidpi_whitelist.xml";
    private static final String FRAMEWORK_RES = "framework-res";
    private static final String FRAMEWORK_RES_EXT = "framework-res-hwext";
    private static final int FWK_EXT_FLAG = 2;
    private static final int FWK_FLAG = 1;
    private static final String HASH_TAG = "#";
    private static final String HONOR = "honor";
    private static final String ICONS_ZIP_FILE = "icons";
    private static final String ICON_BACKGROUND_PREFIX = "icon_background";
    private static final String ICON_BORDER_FILE = "icon_border.png";
    private static final String ICON_MASK_FILE = "icon_mask.png";
    private static final boolean IS_CUSTOM_ICON = SystemPropertiesEx.getBoolean("ro.config.custom_icon", false);
    private static final boolean IS_DEBUG = false;
    private static final boolean IS_DEBUG_DRAWABLE = false;
    private static final boolean IS_DEBUG_HWFLOW = ("1".equals(SystemPropertiesEx.get("ro.debuggable", "0")) || SystemPropertiesEx.getInt("ro.logsystem.usertype", 1) == 3 || SystemPropertiesEx.getInt("ro.logsystem.usertype", 1) == 5);
    private static final boolean IS_DEBUG_ICON = false;
    private static final boolean IS_ICON_SUPPORT_CUT = SystemPropertiesEx.getBoolean("ro.config.hw_icon_supprot_cut", true);
    private static final String LAND_SUFFIX = "-land";
    private static final int LEN_OF_ANDROID = 7;
    private static final int LEN_OF_ANDROID_EXT = 12;
    private static final int MOVE_STEP = 24;
    private static final String NOTIFICATION_ICON_BORDER = "ic_stat_notify_icon_border.png";
    private static final String NOTIFICATION_ICON_EXIST = "ic_stat_notify_bg_0.png";
    private static final String NOTIFICATION_ICON_MASK = "ic_stat_notify_icon_mask.png";
    private static final String NOTIFICATION_ICON_PREFIX = "ic_stat_notify_bg_";
    private static final String PNG_SUFFIX = ".png";
    private static final Set<String> PRELOADED_HW_THEME_ZIPS = new HashSet();
    private static final Object PRELOADED_HW_THEME_ZIP_LOCK = new Object();
    private static final String SLASH = "/";
    static final String TAG = "HwResourcesImpl";
    private static final String TAG_CONFIG = "launcher_config";
    private static final String THEME_ANIMATE_FOLDER_ICON_NAME = "portal_ring_inner_holo.png.animate";
    private static final Object THEME_COLOR_ARRAY_LOCK = new Object();
    private static final String THEME_DESIGNER = "designer";
    private static final Object THEME_DIMEN_LOCK = new Object();
    private static final String THEME_FOLDER_ICON_NAME = "portal_ring_inner_holo.png";
    private static final List<String> THEME_PACKAGE_LISTS = new ArrayList();
    private static final String TYPE_ACTIVITY_ICON = "activityIcon";
    private static final String TYPE_BACKGROUND = "background";
    private static final String TYPE_DIMEN = "dimen";
    private static final String TYPE_FRACTION = "fraction";
    private static final String TYPE_ICON = "icon";
    private static final String TYPE_INTEGER = "integer";
    private static final String XML_ATTRIBUTE_NAME = "name";
    private static final String XML_SUFFIX = ".xml";
    private static boolean isDefaultConfigHasRead = false;
    private static boolean isHwThemes = true;
    private static boolean isMultiDirHasRead = false;
    private static boolean isThemeDescriptionHasRead = false;
    private static Map<String, Integer> sAssetCacheColorInfoList = new HashMap();
    private static List<Bitmap> sBgList = new ArrayList();
    private static volatile Bitmap sBmpBorder = null;
    private static volatile Bitmap sBmpMask = null;
    private static Map<String, String> sCacheAssetsList = new HashMap();
    private static Map<String, Integer> sCacheColorInfoList = new HashMap();
    private static Map<AbsResourcesImpl.ThemedKey, AbsResourcesImpl.ThemedValue> sCacheKeyToValueLandMap = new HashMap();
    private static Map<AbsResourcesImpl.ThemedKey, AbsResourcesImpl.ThemedValue> sCacheKeyToValueMap = new HashMap();
    private static int sDefaultSizeWithoutEdge = -1;
    private static Set<String> sDisThemeActivityIcons = null;
    private static Set<String> sDisThemeBackgrounds = null;
    private static Set<String> sDisThemeIcons = null;
    private static Set<String> sLoadedThemeValueLists = new HashSet();
    private static int sMaskSizeWithoutEdge = -1;
    private static Set<String> sMultiDirPkgNames = new HashSet();
    private static Map<String, AbsResourcesImpl.ThemedValue> sNameToValueLandMap = new HashMap();
    private static Map<String, AbsResourcesImpl.ThemedValue> sNameToValueMap = new HashMap();
    private static Set<String> sNonThemedPackages = new HashSet();
    private static Pattern sPattern = Pattern.compile("-v\\d+/");
    private static Set<String> sPreloadedAssetsPackages = new HashSet();
    private static LongSparseArray<Drawable.ConstantState> sPreloadedColorDrawablesEx = new LongSparseArray<>();
    private static LongSparseArray<Drawable.ConstantState> sPreloadedDrawablesEx = new LongSparseArray<>();
    private static Set<String> sPreloadedThemeColorLists = new HashSet();
    private static int sStandardBgSize = -1;
    private static int sStandardIconSize = -1;
    private boolean isHasReadThemePackages = false;
    private boolean isNewPngFile = false;
    private boolean isThemeChanged = false;
    private final DrawableCacheEx mColorDrawableCacheEx = new DrawableCacheEx();
    protected int mConfigHwt = 0;
    private int mCurrentDeepTheme = 0;
    private String mDeepThemeType = null;
    private final DrawableCacheEx mDrawableCacheEx = new DrawableCacheEx();
    private final DrawableCacheEx mDynamicDrawableCache = new DrawableCacheEx();
    private List<Integer> mOutThemeColorIdArrays = new ArrayList();
    protected String mPackageName = null;
    private ResourcesImplEx mResourcesImpl;
    private SparseArray<AbsResourcesImpl.ThemeColor> mThemeColorArray = new SparseArray<>();
    private TypedValue mTmpValue = new TypedValue();
    private final Object mTmpValueLock = new Object();

    private void getAllBgImage() {
        ZipFileCache iconZipFileCache;
        if (sBmpBorder != null) {
            List<Bitmap> list = sBgList;
            if ((list == null || list.isEmpty()) && (iconZipFileCache = ZipFileCache.getAndCheckCachedZipFile(getThemeDir(), ICONS_ZIP_FILE)) != null) {
                sBgList = iconZipFileCache.getBitmapList(this.mResourcesImpl, ICON_BACKGROUND_PREFIX);
                List<Bitmap> list2 = sBgList;
                if (list2 != null) {
                    int bgListSize = list2.size();
                    for (int i = 0; i < bgListSize; i++) {
                        Bitmap bmp = sBgList.get(i);
                        if (bmp != null) {
                            sBgList.set(i, IconBitmapUtils.zoomIfNeed(bmp, sStandardBgSize, true));
                        }
                    }
                }
            }
        }
    }

    private Bitmap getRandomBgImage(String idAndPackageName) {
        List<Bitmap> list = sBgList;
        if (list == null || list.isEmpty()) {
            getAllBgImage();
        }
        int len = 0;
        List<Bitmap> list2 = sBgList;
        if (list2 != null) {
            len = list2.size();
        }
        if (len <= 0) {
            return null;
        }
        return sBgList.get(getCode(idAndPackageName) % len);
    }

    private int getCode(String idAndPackageName) {
        int code = 0;
        for (char ch : idAndPackageName.toCharArray()) {
            code += ch;
        }
        return code;
    }

    private void initMaskSizeWithoutEdge(boolean isUseDefault) {
        if (sMaskSizeWithoutEdge != -1) {
            return;
        }
        if (sBmpMask != null && !sBmpMask.isRecycled()) {
            Rect info = IconBitmapUtils.getIconInfo(sBmpMask);
            if (info != null) {
                sMaskSizeWithoutEdge = info.width() + 1;
            } else {
                sMaskSizeWithoutEdge = sStandardBgSize - 8;
            }
        } else if (isUseDefault) {
            sMaskSizeWithoutEdge = sStandardBgSize - 8;
        }
    }

    private void initMask(ZipFileCache iconZipFileCache) {
        if (sBmpMask == null) {
            synchronized (HwResourcesImpl.class) {
                sBmpMask = iconZipFileCache.getBitmapEntry(this.mResourcesImpl, ICON_MASK_FILE);
                if (sBmpMask != null) {
                    sBmpMask = IconBitmapUtils.zoomIfNeed(sBmpMask, sStandardBgSize, true);
                }
            }
        }
    }

    private void initBorder(ZipFileCache iconZipFileCache) {
        if (sBmpBorder == null && iconZipFileCache != null) {
            sBmpBorder = iconZipFileCache.getBitmapEntry(this.mResourcesImpl, ICON_BORDER_FILE);
            if (sBmpBorder != null) {
                sBmpBorder = IconBitmapUtils.zoomIfNeed(sBmpBorder, sStandardBgSize, true);
            }
        }
    }

    public Bitmap getThemeIconByName(String name) {
        if (name == null) {
            return null;
        }
        String imgFile = name;
        if (!imgFile.endsWith(PNG_SUFFIX)) {
            imgFile = imgFile + PNG_SUFFIX;
        }
        if (THEME_ANIMATE_FOLDER_ICON_NAME.equalsIgnoreCase(imgFile)) {
            imgFile = THEME_FOLDER_ICON_NAME;
        }
        Bitmap bmp = null;
        synchronized (HwResourcesImpl.class) {
            ZipFileCache iconZipFileCache = ZipFileCache.getAndCheckCachedZipFile(getThemeDir(), ICONS_ZIP_FILE);
            if (iconZipFileCache != null) {
                bmp = iconZipFileCache.getBitmapEntry(this.mResourcesImpl, imgFile);
            }
            if (bmp == null) {
                return bmp;
            }
            return IconBitmapUtils.zoomIfNeed(bmp, sStandardBgSize, true);
        }
    }

    public Bitmap addShortcutBackgroud(Bitmap bmpSrc) {
        int len;
        if (bmpSrc == null) {
            return null;
        }
        synchronized (HwResourcesImpl.class) {
            ZipFileCache iconZipFileCache = ZipFileCache.getAndCheckCachedZipFile(getThemeDir(), ICONS_ZIP_FILE);
            if (iconZipFileCache == null) {
                return null;
            }
            initBorder(iconZipFileCache);
            initMask(iconZipFileCache);
            Bitmap bmpBg = null;
            if (sBgList == null || sBgList.isEmpty()) {
                getAllBgImage();
            }
            if (sBgList != null && (len = sBgList.size()) > 0) {
                bmpBg = sBgList.get(new Random().nextInt(len));
            }
            initMaskSizeWithoutEdge(true);
            int width = bmpSrc.getWidth();
            int height = bmpSrc.getHeight();
            int iconSize = sDefaultSizeWithoutEdge;
            Rect rect = IconBitmapUtils.getIconInfo(bmpSrc);
            if (rect != null) {
                iconSize = computeBestIconSize(rect, sMaskSizeWithoutEdge, width, height);
            }
            return IconBitmapUtils.composeIcon(IconBitmapUtils.drawSource(bmpSrc, sStandardBgSize, iconSize), sBmpMask, bmpBg, sBmpBorder, false);
        }
    }

    private Bitmap addIconBackgroud(Bitmap bmpSrc, String idAndPackageName, boolean isComposeIcon) {
        synchronized (HwResourcesImpl.class) {
            ZipFileCache iconZipFileCache = ZipFileCache.getAndCheckCachedZipFile(getThemeDir(), ICONS_ZIP_FILE);
            if (iconZipFileCache == null) {
                return null;
            }
            initBorder(iconZipFileCache);
            initMask(iconZipFileCache);
            Bitmap bmpBg = getRandomBgImage(idAndPackageName);
            initMaskSizeWithoutEdge(true);
            int width = bmpSrc.getWidth();
            int height = bmpSrc.getHeight();
            int iconSize = sDefaultSizeWithoutEdge;
            Rect rect = IconBitmapUtils.getIconInfo(bmpSrc);
            if (rect != null) {
                iconSize = computeBestIconSize(rect, sMaskSizeWithoutEdge, width, height);
            }
            if (width == 1 && height == 1) {
                return IconBitmapUtils.overlap2Bitmap(bmpSrc, bmpBg);
            }
            Bitmap bmpSrcStd = IconBitmapUtils.drawSource(bmpSrc, sStandardBgSize, iconSize);
            if (!isComposeIcon) {
                return bmpSrcStd;
            }
            return IconBitmapUtils.composeIcon(bmpSrcStd, sBmpMask, bmpBg, sBmpBorder, false);
        }
    }

    /* access modifiers changed from: protected */
    public Drawable handleAddIconBackground(Resources res, int id, Drawable dr) {
        Drawable drawable = dr;
        if (id != 0) {
            String resourcesPackageName = this.mResourcesImpl.getResourcePackageName(id);
            String idAndPackageName = id + HASH_TAG + resourcesPackageName;
            if (IconCache.contains(idAndPackageName)) {
                boolean isAdaptiveIcon = drawable instanceof AdaptiveIconDrawable;
                String packageName = this.mPackageName;
                if (packageName == null) {
                    packageName = resourcesPackageName;
                }
                Bitmap bmp = IconBitmapUtils.drawableToBitmap(drawable);
                if (bmp == null) {
                    return drawable;
                }
                if (bmp.getWidth() > sDefaultSizeWithoutEdge * 3 || bmp.getHeight() > sDefaultSizeWithoutEdge * 3) {
                    Log.w(TAG, "icon in pkg " + packageName + " is too large." + "sDefaultSizeWithoutEdge = " + sDefaultSizeWithoutEdge + " bmp.getWidth() = " + bmp.getWidth());
                    bmp = IconBitmapUtils.zoomIfNeed(bmp, sDefaultSizeWithoutEdge, true);
                    drawable = new BitmapDrawable(res, bmp);
                    ((BitmapDrawable) drawable).setTargetDensity(this.mResourcesImpl.getDisplayMetrics());
                }
                ZipFileCache iconZipFileCache = ZipFileCache.getAndCheckCachedZipFile(getThemeDir(), ICONS_ZIP_FILE);
                if (iconZipFileCache == null) {
                    return drawable;
                }
                initMask(iconZipFileCache);
                Bitmap resultBitmap = null;
                if ((IS_ICON_SUPPORT_CUT || !isCurrentHwTheme(getThemeDir(), FILE_DESCRIPTION)) && !checkWhiteListApp(packageName)) {
                    Set<String> set = sDisThemeBackgrounds;
                    if (set == null || !set.contains(packageName)) {
                        resultBitmap = addIconBackgroud(bmp, idAndPackageName, true);
                    }
                } else {
                    resultBitmap = addIconBackgroud(bmp, idAndPackageName, isAdaptiveIcon);
                }
                if (resultBitmap == null) {
                    resultBitmap = IconBitmapUtils.zoomIfNeed(bmp, sStandardBgSize, false);
                }
                if (resultBitmap != null) {
                    Drawable drawable2 = new BitmapDrawable(res, getDarkerBitmap(resultBitmap));
                    ((BitmapDrawable) drawable2).setTargetDensity(this.mResourcesImpl.getDisplayMetrics());
                    return drawable2;
                }
            }
        }
        return drawable;
    }

    private boolean checkWhiteListApp(String packageName) {
        Set<String> set = sDisThemeIcons;
        return set != null && set.contains(packageName) && isCurrentHwTheme(getThemeDir(), FILE_DESCRIPTION);
    }

    private boolean checkWhiteImgFile(String imgFile) {
        Set<String> set = sDisThemeActivityIcons;
        if (set == null || !set.contains(imgFile)) {
            return false;
        }
        return true;
    }

    private Drawable getThemeIcon(Resources resources, String imgFile, String packageName) {
        Bitmap srcBitmap;
        ZipFileCache iconZipFileCache;
        if (checkWhiteListApp(packageName)) {
            return null;
        }
        Drawable dr = null;
        Bitmap bmp = null;
        if (checkWhiteImgFile(imgFile)) {
            return null;
        }
        synchronized (HwResourcesImpl.class) {
            if (IS_CUSTOM_ICON) {
                bmp = getIconsFromDiffTheme(this.mResourcesImpl, imgFile, packageName);
            }
            if (bmp == null && (iconZipFileCache = ZipFileCache.getAndCheckCachedZipFile(getThemeDir(), ICONS_ZIP_FILE)) != null) {
                bmp = iconZipFileCache.getBitmapEntry(this.mResourcesImpl, imgFile);
                if (!imgFile.equals(getIconFileName(packageName)) && bmp == null) {
                    bmp = iconZipFileCache.getBitmapEntry(this.mResourcesImpl, getIconFileName(packageName));
                }
            }
            if (!(bmp == null || (srcBitmap = IconBitmapUtils.zoomIfNeed(bmp, sStandardBgSize, true)) == null)) {
                dr = new BitmapDrawable(resources, getDarkerBitmap(srcBitmap));
                ((BitmapDrawable) dr).setTargetDensity(this.mResourcesImpl.getDisplayMetrics());
            }
        }
        return dr;
    }

    private AbsResourcesImpl.ThemeColor loadThemeColor(String packageName, String zipName, String resName, String themeXml, int defaultColor) {
        if (!isEmptyPreloadZip(zipName) && !sNonThemedPackages.contains(packageName) && !sPreloadedThemeColorLists.contains(packageName)) {
            ZipFileCache packageZipFileCache = ZipFileCache.getAndCheckCachedZipFile(getThemeDir(), zipName);
            if (packageZipFileCache != null) {
                parserColorInfoList(packageName, packageZipFileCache.getInputStreamEntry(themeXml), true);
                sPreloadedThemeColorLists.add(packageName);
            } else {
                sNonThemedPackages.add(packageName);
            }
        }
        Integer value = null;
        if (!sCacheColorInfoList.isEmpty()) {
            value = sCacheColorInfoList.get(resName);
        }
        if (!TextUtils.isEmpty(this.mDeepThemeType) && value == null && (value = sAssetCacheColorInfoList.get(resName)) == null && !sPreloadedThemeColorLists.contains(packageName) && !sPreloadedAssetsPackages.contains(packageName)) {
            loadThemeColorFromAssets(packageName, themeXml, this.mDeepThemeType);
            sPreloadedAssetsPackages.add(packageName);
            value = sAssetCacheColorInfoList.get(resName);
        }
        if (value != null) {
            return new AbsResourcesImpl.ThemeColor(value.intValue(), true);
        }
        return new AbsResourcesImpl.ThemeColor(defaultColor, false);
    }

    private static void closeStream(Closeable io) {
        if (io != null) {
            try {
                io.close();
            } catch (IOException e) {
                Log.w(TAG, "closeStream error");
            }
        }
    }

    private void parserColorInfoList(String packageName, InputStream is, boolean isZipFile) {
        if (is != null) {
            BufferedInputStream bufferedInput = new BufferedInputStream(is);
            try {
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(bufferedInput, null);
                inflateColorInfoList(parser, packageName, isZipFile);
            } catch (XmlPullParserException e) {
                Log.e(TAG, "loadThemeColor : XmlPullParserException");
            } catch (IOException e2) {
                Log.e(TAG, "loadThemeColor : IOException");
            } catch (RuntimeException e3) {
                Log.e(TAG, "loadThemeColor : RuntimeException");
            } catch (Throwable th) {
                closeStream(bufferedInput);
                closeStream(is);
                throw th;
            }
            closeStream(bufferedInput);
            closeStream(is);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x001e, code lost:
        if (r0 == false) goto L_0x0022;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0022, code lost:
        r3 = android.content.res.HwResourcesImpl.THEME_COLOR_ARRAY_LOCK;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0025, code lost:
        monitor-enter(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x002c, code lost:
        if (r17.mThemeColorArray.size() == 0) goto L_0x0038;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0036, code lost:
        r11 = r17.mThemeColorArray.get(r19);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0038, code lost:
        r11 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:?, code lost:
        monitor-exit(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x003a, code lost:
        if (r11 == null) goto L_0x003d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x003c, code lost:
        return r11;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x003d, code lost:
        r12 = getThemeResource(r19, null);
        r13 = r12.packageName;
        r14 = r12.resName;
        r1 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0049, code lost:
        if ((r19 >>> android.content.res.HwResourcesImpl.MOVE_STEP) == 1) goto L_0x0052;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x004e, code lost:
        if ((r19 >>> android.content.res.HwResourcesImpl.MOVE_STEP) != 2) goto L_0x0051;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0051, code lost:
        r1 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x0053, code lost:
        if (r1 == false) goto L_0x00c5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x0059, code lost:
        if (getPackageName() == null) goto L_0x00c5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x0063, code lost:
        if (getPackageName().isEmpty() != false) goto L_0x00c5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x0065, code lost:
        r1 = loadThemeColor(getPackageName() + android.content.res.HwResourcesImpl.SLASH + r13, getPackageName(), getPackageName() + android.content.res.HwResourcesImpl.SLASH + r14, r13 + "/theme.xml", r18.data);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x00b6, code lost:
        if (r1.mIsThemed == false) goto L_0x00c5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x00b8, code lost:
        r2 = android.content.res.HwResourcesImpl.THEME_COLOR_ARRAY_LOCK;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x00ba, code lost:
        monitor-enter(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:?, code lost:
        r17.mThemeColorArray.put(r19, r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x00c0, code lost:
        monitor-exit(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x00c1, code lost:
        return r1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x00c5, code lost:
        r1 = loadThemeColor(r13, r13, r14, "theme.xml", r18.data);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x00d4, code lost:
        if (r1.mIsThemed == false) goto L_0x00e3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:0x00d6, code lost:
        r2 = android.content.res.HwResourcesImpl.THEME_COLOR_ARRAY_LOCK;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:48:0x00d8, code lost:
        monitor-enter(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:?, code lost:
        r17.mThemeColorArray.put(r19, r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:51:0x00de, code lost:
        monitor-exit(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:0x00e3, code lost:
        r2 = android.content.res.HwResourcesImpl.THEME_COLOR_ARRAY_LOCK;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:57:0x00e5, code lost:
        monitor-enter(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:60:0x00f0, code lost:
        if (r17.mOutThemeColorIdArrays.contains(java.lang.Integer.valueOf(r19)) != false) goto L_0x00fb;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:61:0x00f2, code lost:
        r17.mOutThemeColorIdArrays.add(java.lang.Integer.valueOf(r19));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:62:0x00fb, code lost:
        monitor-exit(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:63:0x00fc, code lost:
        return r1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:67:0x0100, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:69:0x0103, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:70:0x0104, code lost:
        monitor-exit(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:71:0x0105, code lost:
        throw r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:73:0x010d, code lost:
        return new android.content.res.AbsResourcesImpl.ThemeColor(r18.data, false);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x001c, code lost:
        if (r17.mResourcesImpl.isPreloading() != false) goto L_0x0106;
     */
    public AbsResourcesImpl.ThemeColor getThemeColor(TypedValue value, int id) throws Resources.NotFoundException {
        synchronized (THEME_COLOR_ARRAY_LOCK) {
            try {
                boolean isOutThemeColorId = this.mOutThemeColorIdArrays.contains(Integer.valueOf(id));
                try {
                } catch (Throwable th) {
                    th = th;
                    throw th;
                }
            } catch (Throwable th2) {
                th = th2;
                throw th;
            }
        }
    }

    private void loadThemeColorFromAssets(String packageName, String themeXml, String dir) {
        if ((!packageName.equals(FRAMEWORK_RES) && !packageName.equals(FRAMEWORK_RES_EXT)) || themeXml.contains(packageName)) {
            parserColorInfoList(packageName, AssetsFileCache.getInputStreamEntry(this.mResourcesImpl.getAssets(), dir + File.separator + themeXml), false);
        }
    }

    public AbsResourcesImpl.ThemedValue getThemeDimension(TypedValue value, int id) throws Resources.NotFoundException {
        AbsResourcesImpl.ThemedValue themedValue;
        if (this.mResourcesImpl.isPreloading() || !this.mResourcesImpl.isPreloaded() || value == null || !isSupportedThemeType(value.type)) {
            return null;
        }
        AbsResourcesImpl.ThemedKey key = new AbsResourcesImpl.ThemedKey(id, value.type);
        boolean isLand = this.mResourcesImpl.getConfiguration().orientation == 2;
        synchronized (THEME_DIMEN_LOCK) {
            themedValue = getThemedValueLocked(key, isLand);
        }
        if (themedValue != null) {
            return themedValue;
        }
        AbsResourcesImpl.ThemeResource themeRes = getThemeResource(id, null);
        String packageName = themeRes.packageName;
        String resName = themeRes.resName;
        AbsResourcesImpl.ThemedValue themedValue2 = loadFrameworkResIfNeed(packageName, resName, key, isLand);
        if (themedValue2 != null) {
            return themedValue2;
        }
        return loadAndCacheThemeDimens(new String[]{packageName, null}, packageName, resName, key, isLand);
    }

    private boolean isSupportedThemeType(int type) {
        if ((type >= 16 && type <= 31) || type == 4 || type == 5 || type == 6) {
            return true;
        }
        return false;
    }

    private AbsResourcesImpl.ThemedValue getThemedValueLocked(AbsResourcesImpl.ThemedKey key, boolean isLand) {
        AbsResourcesImpl.ThemedValue themedValue = null;
        if (isLand && !sCacheKeyToValueLandMap.isEmpty()) {
            themedValue = sCacheKeyToValueLandMap.get(key);
        }
        if (themedValue != null || sCacheKeyToValueMap.isEmpty()) {
            return themedValue;
        }
        return sCacheKeyToValueMap.get(key);
    }

    private AbsResourcesImpl.ThemedValue loadFrameworkResIfNeed(String packageName, String resName, AbsResourcesImpl.ThemedKey key, boolean isLand) {
        String currentAppPackageName;
        if (!(FRAMEWORK_RES.equals(packageName) || FRAMEWORK_RES_EXT.equals(packageName)) || (currentAppPackageName = getPackageName()) == null || currentAppPackageName.isEmpty()) {
            return null;
        }
        String[] pathInfos = {currentAppPackageName + File.separator + packageName, packageName};
        AbsResourcesImpl.ThemedValue themedValue = loadAndCacheThemeDimens(pathInfos, currentAppPackageName, currentAppPackageName + File.separator + resName, key, isLand);
        if (themedValue != null) {
            return themedValue;
        }
        return null;
    }

    private AbsResourcesImpl.ThemedValue loadAndCacheThemeDimens(String[] pathInfo, String zipName, String resName, AbsResourcesImpl.ThemedKey key, boolean isLand) {
        AbsResourcesImpl.ThemedValue value;
        AbsResourcesImpl.ThemedValue tempValue;
        AbsResourcesImpl.ThemedValue tempValue2;
        if (isEmptyPreloadZip(zipName)) {
            return null;
        }
        loadDimensXmlInfoIfNeed(pathInfo[0], zipName, pathInfo[1]);
        synchronized (THEME_DIMEN_LOCK) {
            if (!sNameToValueLandMap.isEmpty() && (tempValue2 = sNameToValueLandMap.get(resName)) != null) {
                sCacheKeyToValueLandMap.put(key, tempValue2);
            }
            if (!sNameToValueMap.isEmpty() && (tempValue = sNameToValueMap.get(resName)) != null) {
                sCacheKeyToValueMap.put(key, tempValue);
            }
            value = getThemedValueLocked(key, isLand);
        }
        return value;
    }

    /* JADX WARNING: Removed duplicated region for block: B:20:0x0047  */
    private void loadDimensXmlInfoIfNeed(String packageName, String zipName, String prefixDir) {
        boolean isFwkRes;
        ZipFileCache packageZipFileCache;
        synchronized (THEME_DIMEN_LOCK) {
            if (!sLoadedThemeValueLists.contains(packageName)) {
                if (!FRAMEWORK_RES.equals(zipName)) {
                    if (!FRAMEWORK_RES_EXT.equals(zipName)) {
                        isFwkRes = false;
                        if (isFwkRes || UserHandleEx.getAppId(Process.myUid()) >= 1000) {
                            sLoadedThemeValueLists.add(packageName);
                            packageZipFileCache = ZipFileCache.getAndCheckCachedZipFile(getThemeDir(), zipName);
                            if (packageZipFileCache != null) {
                                String[] dimensFilePaths = {getDimensionFilePath(prefixDir, false), getDimensionFilePath(prefixDir, true)};
                                if (IS_DEBUG_HWFLOW) {
                                    Log.i(TAG, "packageName=" + packageName + ", dimensFilePaths[0]=" + dimensFilePaths[0] + ", dimensFilePaths[1]=" + dimensFilePaths[1] + ", zipName=" + zipName);
                                }
                                parseDimensXmlInfo(packageName, packageZipFileCache.getInputStreamEntry(dimensFilePaths[0]), false);
                                parseDimensXmlInfo(packageName, packageZipFileCache.getInputStreamEntry(dimensFilePaths[1]), true);
                            }
                        } else {
                            Log.w(TAG, "zygote skip loading framework-res and framework-res-hwext");
                            return;
                        }
                    }
                }
                isFwkRes = true;
                if (isFwkRes) {
                }
                sLoadedThemeValueLists.add(packageName);
                packageZipFileCache = ZipFileCache.getAndCheckCachedZipFile(getThemeDir(), zipName);
                if (packageZipFileCache != null) {
                }
            }
        }
    }

    private void parseDimensXmlInfo(String packageName, InputStream is, boolean isLand) {
        if (is != null) {
            BufferedInputStream bufferedInput = new BufferedInputStream(is);
            try {
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(bufferedInput, null);
                inflateDimensXmlInfo(parser, packageName, isLand);
            } catch (XmlPullParserException e) {
                Log.e(TAG, "parserDimenInfoList XmlPullParserException.");
            } catch (IOException e2) {
                Log.e(TAG, "parserDimenInfoList IOException.");
            } catch (RuntimeException e3) {
                Log.e(TAG, "parserDimenInfoList RuntimeException");
            } catch (Throwable th) {
                closeStream(bufferedInput);
                closeStream(is);
                throw th;
            }
            closeStream(bufferedInput);
            closeStream(is);
        }
    }

    private void inflateDimensXmlInfo(XmlPullParser parser, String packageName, boolean isLand) throws XmlPullParserException, IOException {
        String formatName;
        String typeName;
        int innerDepth = parser.getDepth();
        int type = parser.next();
        int depth = parser.getDepth();
        while (type != 1) {
            if (depth > innerDepth || type != 3) {
                if (type == 2) {
                    String resValue = null;
                    String tagName = parser.getName();
                    if (tagName != null) {
                        if ("item".equals(tagName)) {
                            typeName = parser.getAttributeValue(null, "type");
                            formatName = parser.getAttributeValue(null, "format");
                            if (formatName == null) {
                                formatName = typeName;
                            }
                        } else {
                            typeName = tagName;
                            formatName = typeName;
                        }
                        if (isSupportedTypeName(typeName)) {
                            String resName = parser.getAttributeValue(null, XML_ATTRIBUTE_NAME);
                            if (parser.next() == 4 && (resValue = parser.getText()) != null) {
                                resValue = resValue.trim();
                            }
                            if (!(typeName == null || resName == null || resValue == null)) {
                                fillDimensMap(packageName + COLON + typeName + SLASH + resName, typeName, formatName, resValue, isLand);
                            }
                        }
                    }
                }
                type = parser.next();
                depth = parser.getDepth();
            } else {
                return;
            }
        }
    }

    private boolean isSupportedTypeName(String typeName) {
        if (typeName == null) {
            return false;
        }
        char c = 65535;
        int hashCode = typeName.hashCode();
        if (hashCode != -1653751294) {
            if (hashCode != 95588145) {
                if (hashCode == 1958052158 && typeName.equals(TYPE_INTEGER)) {
                    c = 1;
                }
            } else if (typeName.equals(TYPE_DIMEN)) {
                c = 0;
            }
        } else if (typeName.equals(TYPE_FRACTION)) {
            c = 2;
        }
        if (c == 0 || c == 1 || c == 2) {
            return true;
        }
        return false;
    }

    /* JADX WARNING: Removed duplicated region for block: B:17:0x0039  */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x004b  */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x0055  */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x009b  */
    /* JADX WARNING: Removed duplicated region for block: B:31:? A[RETURN, SYNTHETIC] */
    private void fillDimensMap(String fullResName, String typeName, String formatName, String resValue, boolean isLand) {
        char c;
        int data = 0;
        int type = 0;
        int hashCode = typeName.hashCode();
        if (hashCode != -1653751294) {
            if (hashCode != 95588145) {
                if (hashCode == 1958052158 && typeName.equals(TYPE_INTEGER)) {
                    c = 1;
                    if (c != 0) {
                        data = dimenFormatItemToTypedValueData(formatName, resValue);
                        type = 5;
                    } else if (c == 1) {
                        data = integerToTypedValueData(resValue);
                        type = 16;
                    } else if (c == 2) {
                        data = fractionToTypedValueData(resValue);
                        type = 6;
                    }
                    if (IS_DEBUG_HWFLOW) {
                        Log.d(TAG, "fillDimensMap: isLand= " + isLand + ", fullResName= " + fullResName + ", format= " + formatName + ", type=" + typeName + ", resValue=" + resValue + ", data=" + data + ", type=" + type);
                    }
                    if (data == 0) {
                        AbsResourcesImpl.ThemedValue themedValue = new AbsResourcesImpl.ThemedValue(type, data);
                        if (isLand) {
                            sNameToValueLandMap.put(fullResName, themedValue);
                            return;
                        } else {
                            sNameToValueMap.put(fullResName, themedValue);
                            return;
                        }
                    } else {
                        return;
                    }
                }
            } else if (typeName.equals(TYPE_DIMEN)) {
                c = 0;
                if (c != 0) {
                }
                if (IS_DEBUG_HWFLOW) {
                }
                if (data == 0) {
                }
            }
        } else if (typeName.equals(TYPE_FRACTION)) {
            c = 2;
            if (c != 0) {
            }
            if (IS_DEBUG_HWFLOW) {
            }
            if (data == 0) {
            }
        }
        c = 65535;
        if (c != 0) {
        }
        if (IS_DEBUG_HWFLOW) {
        }
        if (data == 0) {
        }
    }

    private int dimenToTypedValueData(String resValue) {
        if (resValue == null) {
            return 0;
        }
        int length = resValue.length();
        int index = 0;
        Matcher matcher = DIMEN_PATTERN.matcher(resValue);
        if (matcher.find()) {
            index = matcher.start();
        }
        if (index >= length) {
            return 0;
        }
        float value = 0.0f;
        try {
            value = Float.parseFloat(resValue.substring(0, index));
        } catch (NumberFormatException e) {
            Log.e(TAG, "NumberFormatException for resValue: " + resValue + ", index: " + index);
        }
        return computeTypedValue(value) | transferToUnit(resValue.substring(index));
    }

    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0042, code lost:
        if (r6.equals("px") != false) goto L_0x006e;
     */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x0072 A[RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x0074 A[RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x0076 A[RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x0078 A[RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:43:0x007a A[RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x007c A[ORIG_RETURN, RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:45:? A[RETURN, SYNTHETIC] */
    private int transferToUnit(String unit) {
        char c = 0;
        if (unit == null) {
            return 0;
        }
        int hashCode = unit.hashCode();
        if (hashCode != 3212) {
            if (hashCode != 3365) {
                if (hashCode != 3488) {
                    if (hashCode != 3588) {
                        if (hashCode != 3592) {
                            if (hashCode != 3677) {
                                if (hashCode == 99467 && unit.equals("dip")) {
                                    c = 1;
                                    switch (c) {
                                        case 0:
                                            return 0;
                                        case 1:
                                        case 2:
                                            return 1;
                                        case 3:
                                            return 2;
                                        case 4:
                                            return 3;
                                        case 5:
                                            return 4;
                                        case HwMotionEvent.TOOL_TYPE_FINGER_NAIL:
                                            return 5;
                                        default:
                                            return 0;
                                    }
                                }
                            } else if (unit.equals("sp")) {
                                c = 3;
                                switch (c) {
                                }
                            }
                        }
                    } else if (unit.equals("pt")) {
                        c = 4;
                        switch (c) {
                        }
                    }
                } else if (unit.equals("mm")) {
                    c = 6;
                    switch (c) {
                    }
                }
            } else if (unit.equals("in")) {
                c = 5;
                switch (c) {
                }
            }
        } else if (unit.equals("dp")) {
            c = 2;
            switch (c) {
            }
        }
        c = 65535;
        switch (c) {
        }
    }

    private int dimenFormatItemToTypedValueData(String formatName, String resValue) {
        if (formatName == null || TYPE_DIMEN.equals(formatName)) {
            return dimenToTypedValueData(resValue);
        }
        char c = 65535;
        int hashCode = formatName.hashCode();
        if (hashCode != -1653751294) {
            if (hashCode != 97526364) {
                if (hashCode == 1958052158 && formatName.equals(TYPE_INTEGER)) {
                    c = 0;
                }
            } else if (formatName.equals("float")) {
                c = 1;
            }
        } else if (formatName.equals(TYPE_FRACTION)) {
            c = 2;
        }
        if (c == 0) {
            return integerToTypedValueData(resValue);
        }
        if (c == 1) {
            return floatToTypedValueData(resValue);
        }
        if (c != 2) {
            return 0;
        }
        return fractionToTypedValueData(resValue);
    }

    private int integerToTypedValueData(String resValue) {
        try {
            return Integer.parseInt(resValue);
        } catch (NumberFormatException e) {
            Log.e(TAG, "NumberFormatException: resValue = " + resValue);
            return 0;
        }
    }

    private int fractionToTypedValueData(String resValue) {
        int numIndex;
        int length = resValue.length();
        boolean isParent = false;
        if (resValue.endsWith("p")) {
            numIndex = length - 2;
            isParent = true;
        } else {
            numIndex = length - 1;
        }
        if (numIndex < 0) {
            return 0;
        }
        float value = 0.0f;
        try {
            value = Float.parseFloat(resValue.substring(0, numIndex));
        } catch (NumberFormatException e) {
            Log.e(TAG, "NumberFormatException for resValue: " + resValue + ", numIndex: " + numIndex);
        }
        int target = computeTypedValue(0.01f * value);
        if (isParent) {
            return target | 1;
        }
        return target & -2;
    }

    private int floatToTypedValueData(String resValue) {
        float floatValue = 0.0f;
        try {
            floatValue = Float.parseFloat(resValue);
        } catch (NumberFormatException e) {
            Log.e(TAG, "NumberFormatException: resValue = " + resValue);
        }
        return Float.floatToIntBits(floatValue);
    }

    private static int computeTypedValue(float value) {
        int shift;
        int radix;
        boolean isNeg = value < 0.0f;
        float dataValue = value;
        if (isNeg) {
            dataValue = -value;
        }
        long bits = (long) ((8388608.0f * dataValue) + 0.5f);
        if ((8388607 & bits) == 0) {
            radix = 0;
            shift = 23;
        } else if ((-8388608 & bits) == 0) {
            radix = 3;
            shift = 0;
        } else if ((-2147483648L & bits) == 0) {
            radix = 2;
            shift = 8;
        } else if ((-549755813888L & bits) == 0) {
            radix = 1;
            shift = 16;
        } else {
            radix = 0;
            shift = 23;
        }
        int mantissa = (int) ((bits >> shift) & 16777215);
        if (isNeg) {
            mantissa = (-mantissa) & 16777215;
        }
        return (radix << 4) | (mantissa << 8);
    }

    private String getDimensionFilePath(String prefixDir, boolean isLand) {
        String valueDir = isLand ? "values-land" : "values";
        StringBuffer sb = new StringBuffer();
        if (!TextUtils.isEmpty(prefixDir)) {
            sb.append(prefixDir);
            sb.append(File.separator);
        }
        sb.append("res");
        sb.append(File.separator);
        sb.append(valueDir);
        sb.append(File.separator);
        sb.append("dimens.xml");
        return sb.toString();
    }

    private void cacheDrawable(long key, Resources.Theme theme, Drawable.ConstantState cs) {
        synchronized (CACHE_LOCK) {
            if (this.mResourcesImpl.isPreloading()) {
                sPreloadedDrawablesEx.put(key, cs);
            } else {
                this.mDrawableCacheEx.put(key, theme, cs);
            }
        }
    }

    private Drawable getColorDrawableCacheEx(long key, Resources wrapper, Resources.Theme theme) {
        Drawable instance;
        synchronized (CACHE_LOCK) {
            instance = this.mColorDrawableCacheEx.getInstance(key, wrapper, theme);
        }
        return instance;
    }

    private Drawable loadColorDrawable(Resources wrapper, TypedValue value, int id, Resources.Theme theme, long key) {
        Drawable.ConstantState cs;
        Drawable.ConstantState cs2;
        AbsResourcesImpl.ThemeColor colorValue = null;
        if (id != 0) {
            AbsResourcesImpl.ThemeColor dr = getColorDrawableCacheEx((long) id, wrapper, theme);
            if (dr != null) {
                return dr;
            }
            synchronized (CACHE_LOCK) {
                if (!this.mResourcesImpl.isPreloading()) {
                    if (!this.isThemeChanged) {
                        cs = sPreloadedColorDrawablesEx.get((long) id);
                    }
                }
                cs = null;
            }
            if (cs != null) {
                colorValue = cs.newDrawable(wrapper);
            } else {
                AbsResourcesImpl.ThemeColor colorValue2 = getThemeColor(value, id);
                if (colorValue2.mIsThemed) {
                    colorValue = new ColorDrawable(colorValue2.mColor);
                } else {
                    colorValue = dr;
                }
            }
            if (!(colorValue == null || (cs2 = colorValue.getConstantState()) == null)) {
                cacheDrawable((long) id, theme, cs2);
            }
        }
        return colorValue;
    }

    private Drawable.ConstantState getDrawableconstantState(long key) {
        Drawable.ConstantState constantState;
        synchronized (CACHE_LOCK) {
            constantState = null;
            if (!this.mResourcesImpl.isPreloading()) {
                if (!this.isThemeChanged) {
                    if (this.mResourcesImpl.getConfiguration().densityDpi == DisplayMetrics.DENSITY_DEVICE_STABLE) {
                        constantState = sPreloadedDrawablesEx.get(key);
                    }
                }
            }
        }
        return constantState;
    }

    private Drawable getDrawable(Resources wrapper, TypedValue value, int id, Resources.Theme theme, long key) {
        if (value == null || value.string == null) {
            return null;
        }
        String packageName = getThemePackageName(id, null);
        String file = value.string.toString();
        if (file.isEmpty() || TextUtils.isEmpty(packageName)) {
            return null;
        }
        String packageName2 = getThemePackageName(id, packageName);
        if (isDrawableFileEmpty(id, packageName2, value)) {
            return null;
        }
        Drawable dr = getColorDrawableCacheEx(key, wrapper, theme);
        if (dr != null) {
            return dr;
        }
        Drawable.ConstantState cs = getDrawableconstantState(key);
        if (cs != null) {
            return cs.newDrawable(wrapper);
        }
        Drawable dr2 = getThemeDrawable(value, id, wrapper, packageName2, file);
        if (dr2 != null) {
            cacheDrawable(key, theme, dr2.getConstantState());
        }
        return dr2;
    }

    /* access modifiers changed from: protected */
    public Drawable loadDrawable(Resources wrapper, TypedValue value, int id, Resources.Theme theme, boolean isUseCache) throws Resources.NotFoundException {
        long key = (((long) value.assetCookie) << 32) | ((long) value.data);
        if (value.type >= 28 && value.type <= 31) {
            return loadColorDrawable(wrapper, value, id, theme, key);
        }
        if (id == 0) {
            return null;
        }
        String packageName = getThemePackageName(id, null);
        String iconKey = id + HASH_TAG + packageName;
        if (!IconCache.contains(iconKey)) {
            return getDrawable(wrapper, value, id, theme, key);
        }
        String imgFile = getIconFileName(packageName, IconCache.get(iconKey));
        if (isIconFileEmpty(packageName, imgFile)) {
            return null;
        }
        Drawable dr = getColorDrawableCacheEx(key, wrapper, theme);
        if (dr != null) {
            return dr;
        }
        Drawable.ConstantState cs = getDrawableconstantState(key);
        if (cs != null) {
            return cs.newDrawable(wrapper);
        }
        readDefaultConfig();
        Drawable drIcon = getThemeIcon(wrapper, imgFile, packageName);
        if (drIcon == null) {
            return dr;
        }
        cacheDrawable(key, theme, drIcon.getConstantState());
        return drIcon;
    }

    private Drawable getFrameworkResource(TypedValue value, int id, Resources res, String packageName, String file) {
        Drawable dr = null;
        ZipFileCache frameworkZipFileCache = ZipFileCache.getAndCheckCachedZipFile(getThemeDir(), this.mPackageName);
        String key = packageName + SLASH + file;
        if (frameworkZipFileCache != null) {
            dr = frameworkZipFileCache.getDrawableEntry(res, value, key, null);
            if (dr == null && !file.contains(DRAWABLE_FHD)) {
                frameworkZipFileCache.initResDirInfo();
                boolean isLand = file.contains(LAND_SUFFIX);
                int index = (id >>> MOVE_STEP) == 1 ? isLand ? 3 : 2 : isLand ? 5 : 4;
                int themeDensity = frameworkZipFileCache.getDrawableDensity(index);
                String dir = frameworkZipFileCache.getDrawableDir(index);
                if (!(themeDensity == -1 || dir == null)) {
                    BitmapFactory.Options opts = new BitmapFactory.Options();
                    opts.inDensity = themeDensity;
                    dr = frameworkZipFileCache.getDrawableEntry(res, value, dir + File.separator + file.substring(file.lastIndexOf(File.separator) + 1), opts);
                }
            }
            if (dr != null) {
                return dr;
            }
        }
        return dr;
    }

    /* JADX DEBUG: Multi-variable search result rejected for r11v0, resolved type: huawei.android.hwutil.ZipFileCache */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r2v5, types: [int, boolean] */
    private Drawable getThemeDrawableFromPkg(TypedValue value, int id, Resources res, String packageName, String file) {
        Drawable dr;
        boolean isFramework;
        Drawable dr2 = null;
        ZipFileCache packageZipFileCache = ZipFileCache.getAndCheckCachedZipFile(getThemeDir(), packageName);
        if (packageZipFileCache != 0) {
            dr2 = packageZipFileCache.getDrawableEntry(res, value, file, null);
        }
        boolean isDrAndCacheNull = true;
        if (packageZipFileCache != 0 && dr2 == null && !file.contains(DRAWABLE_FHD)) {
            packageZipFileCache.initResDirInfo();
            ?? contains = file.contains(LAND_SUFFIX);
            int themeDensity = packageZipFileCache.getDrawableDensity(contains == true ? 1 : 0);
            String dir = packageZipFileCache.getDrawableDir(contains);
            if (!(themeDensity == -1 || dir == null)) {
                BitmapFactory.Options opts = new BitmapFactory.Options();
                opts.inDensity = themeDensity;
                if (file.contains(DPI_2K)) {
                    dr = packageZipFileCache.getDrawableEntry(res, value, file.replace(DPI_2K, DPI_FHD), opts);
                } else {
                    dr = packageZipFileCache.getDrawableEntry(res, value, dir + File.separator + file.substring(file.lastIndexOf(File.separator) + 1), opts);
                }
                isFramework = (id >>> MOVE_STEP) != 1 || (id >>> MOVE_STEP) == 2;
                if (!(dr == null && packageZipFileCache == 0 && !TextUtils.isEmpty(this.mDeepThemeType))) {
                    isDrAndCacheNull = false;
                }
                if (!isFramework || !isDrAndCacheNull || this.isNewPngFile) {
                    return dr;
                }
                return getAppDrawableFromAsset(value, res, packageName, file, this.mDeepThemeType);
            }
        }
        dr = dr2;
        if ((id >>> MOVE_STEP) != 1) {
        }
        isDrAndCacheNull = false;
        return !isFramework ? dr : dr;
    }

    public Drawable getThemeDrawable(TypedValue value, int id, Resources res, String packageName, String filePath) throws Resources.NotFoundException {
        String str;
        Drawable dr;
        if (value == null || res == null || packageName == null || filePath == null || this.mResourcesImpl.isPreloading() || value.string == null) {
            return null;
        }
        boolean isHwFramework = false;
        this.isNewPngFile = false;
        String file = filePath;
        if (file.endsWith(XML_SUFFIX)) {
            file = file.substring(0, file.lastIndexOf(".")) + PNG_SUFFIX;
            this.isNewPngFile = true;
        }
        String file2 = sPattern.matcher(file).replaceFirst(SLASH);
        if (packageName.contains(FRAMEWORK_RES) && file2.contains("_holo")) {
            return null;
        }
        boolean isFramework = (id >>> MOVE_STEP) == 1;
        if ((id >>> MOVE_STEP) == 2) {
            isHwFramework = true;
        }
        if ((isFramework || isHwFramework) && (str = this.mPackageName) != null && !str.isEmpty() && (dr = getFrameworkResource(value, id, res, packageName, file2)) != null) {
            return dr;
        }
        return getThemeDrawableFromPkg(value, id, res, packageName, file2);
    }

    private String getThemeType() {
        if ((this.mCurrentDeepTheme & 16) == 16 && (this.mResourcesImpl.getConfiguration().uiMode & 48) == 16) {
            return HONOR;
        }
        return null;
    }

    private BitmapFactory.Options getDefaultOptions() {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inDensity = 480;
        return opts;
    }

    private Drawable getFwkDrawableFromAsset(TypedValue value, Resources res, String packageName, String file) {
        String key = packageName + SLASH + file;
        String dir = this.mDeepThemeType;
        String fullPath = this.mPackageName + COLON + dir + SLASH + key;
        Drawable dr = AssetsFileCache.getDrawableEntry(this.mResourcesImpl.getAssets(), res, value, dir + File.separator + key, null);
        if (dr == null && !file.contains(DRAWABLE_FHD)) {
            dr = AssetsFileCache.getDrawableEntry(this.mResourcesImpl.getAssets(), res, value, dir + File.separator + packageName + File.separator + DEFAULT_RES_XX_DIR + File.separator + file.substring(file.lastIndexOf(File.separator) + 1), getDefaultOptions());
        }
        if (dr == null) {
            sCacheAssetsList.put(fullPath, null);
        }
        return dr;
    }

    private Drawable getAppDrawableFromAsset(TypedValue value, Resources res, String packageName, String file, String dir) {
        String drawablePath = this.mPackageName + COLON + dir + SLASH + file;
        Drawable dr = AssetsFileCache.getDrawableEntry(this.mResourcesImpl.getAssets(), res, value, dir + File.separator + file, null);
        if (dr == null && !file.contains(DRAWABLE_FHD)) {
            dr = AssetsFileCache.getDrawableEntry(this.mResourcesImpl.getAssets(), res, value, dir + File.separator + DEFAULT_RES_XX_DIR + File.separator + file.substring(file.lastIndexOf(File.separator) + 1), getDefaultOptions());
        }
        if (dr == null) {
            sCacheAssetsList.put(drawablePath, null);
        }
        return dr;
    }

    /* JADX DEBUG: Multi-variable search result rejected for r1v2, resolved type: huawei.android.hwutil.ZipFileCache */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r3v5, types: [int, boolean] */
    private Bitmap getThemeBitmapFromPkg(Resources res, TypedValue value, String file, Rect padding, String packageName) {
        Bitmap bmp = null;
        ZipFileCache packageZipFileCache = ZipFileCache.getAndCheckCachedZipFile(ResourcesImplAdapter.getHwResourcesImpl(res).getThemeDir(), packageName);
        if (packageZipFileCache != 0) {
            bmp = packageZipFileCache.getBitmapEntry(res, value, file, padding);
        }
        boolean isFramework = true;
        isFramework = true;
        if (bmp == null && packageZipFileCache != 0 && !file.contains(DRAWABLE_FHD)) {
            packageZipFileCache.initResDirInfo();
            ?? contains = file.contains(LAND_SUFFIX);
            int themeDensity = packageZipFileCache.getDrawableDensity(contains == true ? 1 : 0);
            String dir = packageZipFileCache.getDrawableDir(contains);
            if (!(themeDensity == -1 || dir == null)) {
                bmp = packageZipFileCache.getBitmapEntry(res, value, dir + File.separator + file.substring(file.lastIndexOf(File.separator) + 1), padding);
            }
        }
        String deepThemeType = ResourcesImplAdapter.getHwResourcesImpl(res).getDeepThemeType();
        if (!packageName.equals(FRAMEWORK_RES) && !packageName.equals(FRAMEWORK_RES_EXT)) {
            isFramework = false;
        }
        if (TextUtils.isEmpty(deepThemeType) || bmp != null || packageZipFileCache != 0 || isFramework) {
            return bmp;
        }
        return getAppBitmapFromAsset(value, res, file, deepThemeType, padding);
    }

    private Bitmap getThemeBitmapFromFwkRes(Resources res, TypedValue value, String file, Rect padding, String packageName) {
        ZipFileCache frameworkZipFileCache = ZipFileCache.getAndCheckCachedZipFile(ResourcesImplAdapter.getHwResourcesImpl(res).getThemeDir(), this.mPackageName);
        if (frameworkZipFileCache == null) {
            return null;
        }
        boolean isFramework = packageName.equals(FRAMEWORK_RES);
        boolean isLand = file.contains(LAND_SUFFIX);
        Bitmap bmp = frameworkZipFileCache.getBitmapEntry(res, value, packageName + SLASH + file, padding);
        if (bmp != null || file.contains(DRAWABLE_FHD)) {
            return bmp;
        }
        frameworkZipFileCache.initResDirInfo();
        int index = isFramework ? isLand ? 3 : 2 : isLand ? 5 : 4;
        int themeDensity = frameworkZipFileCache.getDrawableDensity(index);
        String dir = frameworkZipFileCache.getDrawableDir(index);
        if (themeDensity == -1 || dir == null) {
            return bmp;
        }
        return frameworkZipFileCache.getBitmapEntry(res, value, dir + File.separator + file.substring(file.lastIndexOf(File.separator) + 1), padding);
    }

    public Bitmap getThemeBitmap(Resources res, TypedValue value, int id, Rect padding) throws Resources.NotFoundException {
        Bitmap bmp;
        if (!ResourcesImplAdapter.isResourcesImplPreloading(res)) {
            if (value.string != null) {
                String packageName = ResourcesImplAdapter.getHwResourcesImpl(res).getThemeResource(id, (String) null).packageName;
                String file = value.string.toString().replaceFirst("-v\\d+/", SLASH);
                if (!file.isEmpty()) {
                    if (!packageName.contains(FRAMEWORK_RES) || !file.contains("_holo")) {
                        boolean isFramework = packageName.equals(FRAMEWORK_RES);
                        boolean isHwFramework = packageName.equals(FRAMEWORK_RES_EXT);
                        if (isFramework || isHwFramework) {
                            String str = this.mPackageName;
                            if (!(str == null || str.isEmpty() || (bmp = getThemeBitmapFromFwkRes(res, value, file, padding, packageName)) == null)) {
                                return bmp;
                            }
                        }
                        return getThemeBitmapFromPkg(res, value, file, padding, packageName);
                    }
                }
                return null;
            }
        }
        return null;
    }

    private Bitmap getAppBitmapFromAsset(TypedValue value, Resources res, String file, String dir, Rect rect) {
        AssetManager assets = res.getAssets();
        Bitmap bmp = AssetsFileCache.getBitmapEntry(assets, res, value, dir + File.separator + file, null);
        if (bmp != null || file.contains(DRAWABLE_FHD)) {
            return bmp;
        }
        return AssetsFileCache.getBitmapEntry(res.getAssets(), res, value, dir + File.separator + DEFAULT_RES_XX_DIR + File.separator + file.substring(file.lastIndexOf(File.separator) + 1), rect);
    }

    private boolean isEmptyPreloadZip(String packageName) {
        return !emptyOrContainsPreloadedZip(packageName) && !emptyOrContainsPreloadedZip(this.mPackageName);
    }

    public AbsResourcesImpl.ThemeResource getThemeResource(int id, String name) {
        String packageName = name;
        if (packageName == null) {
            packageName = this.mResourcesImpl.getResourcePackageName(id);
        }
        String resName = this.mResourcesImpl.getResourceName(id);
        if (packageName.equals("android")) {
            packageName = FRAMEWORK_RES;
            resName = FRAMEWORK_RES + resName.substring(7);
        } else if (packageName.equals(ANDROID_RES_EXT)) {
            packageName = FRAMEWORK_RES_EXT;
            resName = FRAMEWORK_RES_EXT + resName.substring(LEN_OF_ANDROID_EXT);
        }
        return new AbsResourcesImpl.ThemeResource(packageName, resName);
    }

    public String getThemeResName(int id, String name) {
        String packageName = name;
        if (packageName == null) {
            packageName = this.mResourcesImpl.getResourcePackageName(id);
        }
        String resName = this.mResourcesImpl.getResourceName(id);
        if ("android".equals(packageName)) {
            return FRAMEWORK_RES + resName.substring(7);
        } else if (!ANDROID_RES_EXT.equals(packageName)) {
            return resName;
        } else {
            return FRAMEWORK_RES_EXT + resName.substring(LEN_OF_ANDROID_EXT);
        }
    }

    public String getThemePackageName(int id, String name) {
        String packageName = name;
        if (packageName == null) {
            packageName = this.mResourcesImpl.getResourcePackageName(id);
        }
        char c = 65535;
        int hashCode = packageName.hashCode();
        if (hashCode != -983259741) {
            if (hashCode == -861391249 && packageName.equals("android")) {
                c = 0;
            }
        } else if (packageName.equals(ANDROID_RES_EXT)) {
            c = 1;
        }
        if (c == 0) {
            return FRAMEWORK_RES;
        }
        if (c != 1) {
            return packageName;
        }
        return FRAMEWORK_RES_EXT;
    }

    private boolean isIconFileEmpty(String packageName, String imgFile) {
        if (IS_CUSTOM_ICON) {
            ZipFileCache checkIconZipFileCache = ZipFileCache.getAndCheckCachedZipFile(getDiffThemeIconPath(), ICONS_ZIP_FILE);
            if (checkIconZipFileCache != null && checkIconZipFileCache.checkIconEntry(imgFile)) {
                return false;
            }
            String name = getIconFileName(packageName);
            if (!imgFile.equals(name) && checkIconZipFileCache != null && checkIconZipFileCache.checkIconEntry(name)) {
                return false;
            }
        }
        ZipFileCache checkIconZipFileCache2 = ZipFileCache.getAndCheckCachedZipFile(getThemeDir(), ICONS_ZIP_FILE);
        if (checkIconZipFileCache2 == null) {
            return true;
        }
        if (checkIconZipFileCache2.checkIconEntry(imgFile)) {
            return false;
        }
        String name2 = getIconFileName(packageName);
        if (imgFile.equals(name2) || !checkIconZipFileCache2.checkIconEntry(name2)) {
            return true;
        }
        return false;
    }

    private boolean isColorFileEmpty(String pkgName) {
        if (isEmptyPreloadZip(pkgName)) {
            return true;
        }
        return false;
    }

    private boolean isDrawableFileEmpty(int id, String packageName, TypedValue value) {
        if (!isEmptyPreloadZip(packageName) || this.mCurrentDeepTheme != 0) {
            return false;
        }
        return true;
    }

    public void setResourcesImpl(ResourcesImplEx resourcesImpl) {
        this.mResourcesImpl = resourcesImpl;
    }

    public void initResource() {
        if (sStandardBgSize == -1) {
            int standardBgSize = CONFIG_APP_BIG_ICON_SIZE;
            if (standardBgSize == -1) {
                standardBgSize = getDimensionPixelSize(34472064);
            }
            int standardIconSize = CONFIG_APP_INNER_ICON_SIZE;
            if (standardIconSize == -1) {
                standardIconSize = getDimensionPixelSize(34472063);
            }
            setStandardSize(standardBgSize, standardIconSize);
        }
        HwResource.setIsSRLocale("sr".equals(Locale.getDefault().getLanguage()));
    }

    public int getDimensionPixelSize(int id) throws Resources.NotFoundException {
        TypedValue value = obtainTempTypedValue();
        try {
            this.mResourcesImpl.getValue(id, value, true);
            if (value.type == 5) {
                return TypedValue.complexToDimensionPixelSize(value.data, this.mResourcesImpl.getDisplayMetrics());
            }
            throw new Resources.NotFoundException("Resource ID #0x" + Integer.toHexString(id) + " type #0x" + Integer.toHexString(value.type) + " is not valid");
        } finally {
            releaseTempTypedValue(value);
        }
    }

    /* access modifiers changed from: protected */
    public void releaseTempTypedValue(TypedValue value) {
        synchronized (this.mTmpValueLock) {
            if (this.mTmpValue == null) {
                this.mTmpValue = value;
            }
        }
    }

    /* access modifiers changed from: protected */
    public TypedValue obtainTempTypedValue() {
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

    private static void setStandardSize(int standardBgSize, int standardIconSize) {
        sStandardBgSize = standardBgSize;
        sDefaultSizeWithoutEdge = sStandardBgSize + 8;
        sStandardIconSize = standardIconSize;
    }

    private void clearThemeCache() {
        sCacheColorInfoList.clear();
        sAssetCacheColorInfoList.clear();
        sPreloadedThemeColorLists.clear();
        sNonThemedPackages.clear();
        sPreloadedAssetsPackages.clear();
        sCacheAssetsList.clear();
        clearHwThemeZipsAndIconsCache();
        isThemeDescriptionHasRead = false;
        isHwThemes = true;
        this.isHasReadThemePackages = false;
        THEME_PACKAGE_LISTS.clear();
    }

    /* access modifiers changed from: protected */
    public void handleClearCache(int configChanges, boolean isThemeChange) {
        if (isThemeChange) {
            synchronized (THEME_COLOR_ARRAY_LOCK) {
                this.mThemeColorArray.clear();
                this.mOutThemeColorIdArrays.clear();
            }
            synchronized (THEME_DIMEN_LOCK) {
                sLoadedThemeValueLists.clear();
                sCacheKeyToValueMap.clear();
                sCacheKeyToValueLandMap.clear();
                sNameToValueMap.clear();
                sNameToValueLandMap.clear();
            }
        }
        synchronized (HwResourcesImpl.class) {
            if (isThemeChange) {
                clearThemeCache();
            }
            if (this.mDynamicDrawableCache != null) {
                this.mDynamicDrawableCache.onConfigurationChange(configChanges);
            }
        }
        synchronized (CACHE_LOCK) {
            if (this.mColorDrawableCacheEx != null) {
                this.mColorDrawableCacheEx.onConfigurationChange(configChanges);
            }
            if (this.mDrawableCacheEx != null) {
                this.mDrawableCacheEx.onConfigurationChange(configChanges);
            }
        }
        if (isThemeChange) {
            clearPreloadedHwThemeZipsCache();
        }
    }

    public void setResImplPackageName(String name) {
        this.mPackageName = name;
    }

    public String getPackageName() {
        return this.mPackageName;
    }

    /* access modifiers changed from: protected */
    public void setHwTheme(Configuration config) {
        if (config != null) {
            boolean z = true;
            this.mConfigHwt = ConfigurationAdapter.getExtraConfig(config).getConfigItem(1);
            if (ConfigurationAdapter.getExtraConfig(this.mResourcesImpl.getConfiguration()).getConfigItem(1) == 0) {
                z = false;
            }
            this.isThemeChanged = z;
        }
    }

    public void updateConfiguration(Configuration config, int configChanges) {
        if (config != null) {
            IHwConfiguration configEx = ConfigurationAdapter.getExtraConfig(this.mResourcesImpl.getConfiguration());
            if (this.mConfigHwt != configEx.getConfigItem(1)) {
                this.mConfigHwt = configEx.getConfigItem(1);
                synchronized (HwResourcesImpl.class) {
                    this.isThemeChanged = true;
                }
                handleClearCache(configChanges, this.isThemeChanged);
            }
            this.mDeepThemeType = getThemeType();
            if (this.mResourcesImpl.getConfiguration().locale != null) {
                HwResource.setIsSRLocale("sr".equals(this.mResourcesImpl.getConfiguration().locale.getLanguage()));
            }
        }
    }

    public Drawable getDrawableForDynamic(Resources res, String packageName, String iconName) throws Resources.NotFoundException {
        if (res == null || packageName == null || iconName == null) {
            return null;
        }
        String imgFile = "dynamic_icons/" + packageName + SLASH + iconName + PNG_SUFFIX;
        long key = (((long) imgFile.length()) << 32) | ((long) getCode(imgFile));
        Drawable dr = this.mDynamicDrawableCache.getInstance(key, res, (Resources.Theme) null);
        if (dr != null) {
            return dr;
        }
        ZipFileCache iconZipFileCache = ZipFileCache.getAndCheckCachedZipFile(getThemeDir(), ICONS_ZIP_FILE);
        if (iconZipFileCache == null) {
            return null;
        }
        Bitmap bmp = iconZipFileCache.getBitmapEntry(this.mResourcesImpl, imgFile);
        if (bmp == null) {
            return dr;
        }
        Drawable dr2 = new BitmapDrawable(res, getDarkerBitmap(bmp));
        ((BitmapDrawable) dr2).setTargetDensity(this.mResourcesImpl.getDisplayMetrics());
        this.mDynamicDrawableCache.put(key, (Resources.Theme) null, dr2.getConstantState(), true);
        return dr2;
    }

    public ArrayList<String> getDataThemePackages() {
        if (this.isHasReadThemePackages) {
            return (ArrayList) THEME_PACKAGE_LISTS;
        }
        List<String> lists = HwThemeManager.getDataSkinThemePackages();
        if (lists == null) {
            return null;
        }
        synchronized (HwResourcesImpl.class) {
            THEME_PACKAGE_LISTS.clear();
            THEME_PACKAGE_LISTS.addAll(lists);
        }
        this.isHasReadThemePackages = true;
        return (ArrayList) THEME_PACKAGE_LISTS;
    }

    private static void createThemeCaches() {
        sDisThemeIcons = new HashSet();
        sDisThemeBackgrounds = new HashSet();
        sDisThemeActivityIcons = new HashSet();
    }

    private static void addToThemeCaches(String drawableType, String packageName) {
        if (TYPE_ICON.equals(drawableType)) {
            sDisThemeIcons.add(packageName);
        } else if (TYPE_BACKGROUND.equals(drawableType)) {
            sDisThemeBackgrounds.add(packageName);
        } else if (TYPE_ACTIVITY_ICON.equals(drawableType)) {
            sDisThemeActivityIcons.add(packageName);
        } else {
            Log.w(TAG, "unknown drawable type : " + drawableType);
        }
    }

    private static void clearThemeCaches() {
        sDisThemeIcons = null;
        sDisThemeBackgrounds = null;
        sDisThemeActivityIcons = null;
    }

    private static synchronized void readDefaultConfig() {
        synchronized (HwResourcesImpl.class) {
            if (!isDefaultConfigHasRead) {
                File inputFile = null;
                try {
                    inputFile = HwCfgFilePolicy.getCfgFile(DEFAULT_CONFIG_NAME, 0);
                } catch (NoClassDefFoundError e) {
                    Log.d(TAG, "HwCfgFilePolicy NoClassDefFoundError");
                }
                if (inputFile == null) {
                    isDefaultConfigHasRead = true;
                    return;
                }
                InputStream in = null;
                try {
                    in = new FileInputStream(inputFile);
                    XmlPullParser parser = Xml.newPullParser();
                    parser.setInput(in, null);
                    XmlUtilsEx.beginDocument(parser, TAG_CONFIG);
                    createThemeCaches();
                    while (true) {
                        int type = parser.next();
                        if (type == 1) {
                            break;
                        } else if (type == 2) {
                            String packageName = parser.getAttributeValue(0);
                            String drawableType = null;
                            parser.next();
                            if (parser.getEventType() == 4) {
                                drawableType = String.valueOf(parser.getText());
                            }
                            addToThemeCaches(drawableType, packageName);
                        }
                    }
                } catch (IOException | XmlPullParserException e2) {
                    Log.e(TAG, "readDefaultConfig : XmlPullParserException | IOException");
                    clearThemeCaches();
                } finally {
                    closeStream(in);
                }
                isDefaultConfigHasRead = true;
            }
        }
    }

    private static synchronized void loadMultiDpiWhiteList() {
        synchronized (HwResourcesImpl.class) {
            isMultiDirHasRead = true;
            List<File> configFiles = new ArrayList<>();
            try {
                configFiles = HwCfgFilePolicy.getCfgFileList(FILE_MULTI_DPI_WHITELIST, 0);
            } catch (NoClassDefFoundError e) {
                Log.e(TAG, "HwCfgFilePolicy NoClassDefFoundError! ");
            }
            for (File configFile : configFiles) {
                setMultiDpiPkgNameFromXml(configFile);
            }
            setMultiDpiPkgNameFromXml(new File(DEFAULT_MULTI_DPI_WHITELIST));
        }
    }

    private static synchronized void setMultiDpiPkgNameFromXml(File configFile) {
        String packageName;
        synchronized (HwResourcesImpl.class) {
            if (configFile != null) {
                InputStream inputStream = null;
                try {
                    inputStream = new FileInputStream(configFile);
                    XmlPullParser xmlParser = Xml.newPullParser();
                    xmlParser.setInput(inputStream, null);
                    for (int xmlEventType = xmlParser.next(); xmlEventType != 1; xmlEventType = xmlParser.next()) {
                        if (xmlEventType == 2 && (packageName = xmlParser.getAttributeValue(null, XML_ATTRIBUTE_NAME)) != null) {
                            sMultiDirPkgNames.add(packageName);
                        }
                    }
                } catch (FileNotFoundException e) {
                    isMultiDirHasRead = false;
                    Log.e(TAG, "loadMultiDpiWhiteList : Load MultiDpiWhiteList Error");
                } catch (XmlPullParserException e2) {
                    isMultiDirHasRead = false;
                    Log.e(TAG, "loadMultiDpiWhiteList : XmlPullParserException");
                } catch (IOException e3) {
                    isMultiDirHasRead = false;
                    Log.e(TAG, "loadMultiDpiWhiteList : IOException");
                } finally {
                    closeStream(inputStream);
                }
            }
        }
    }

    public boolean isInMultiDpiWhiteList(String packageName) {
        if (!isMultiDirHasRead) {
            loadMultiDpiWhiteList();
        }
        if (sMultiDirPkgNames.size() == 0) {
            Log.i(TAG, "open lock res on whitelist, but not provide the whitelist, so lock resource");
            return true;
        } else if (packageName == null) {
            Log.i(TAG, "can't get the package name, so lock resource");
            return true;
        } else if (!sMultiDirPkgNames.contains(packageName)) {
            return false;
        } else {
            return true;
        }
    }

    private String getIconFileName(String packageName, IconCache.CacheEntry ce) {
        StringBuilder sb = new StringBuilder();
        sb.append(ce.type == 1 ? ce.name : packageName);
        sb.append(PNG_SUFFIX);
        return sb.toString();
    }

    private String getIconFileName(String packageName) {
        return packageName + PNG_SUFFIX;
    }

    /* JADX INFO: Multiple debug info for r0v1 int: [D('validSize' double), D('iconSize' int)] */
    private int computeBestIconSize(Rect validRect, int maskSize, int width, int height) {
        double validSize;
        if (validRect.height() > validRect.width()) {
            validSize = (((double) maskSize) / (((double) validRect.height()) + 1.0d)) * ((double) height);
        } else {
            validSize = (((double) maskSize) / (((double) validRect.width()) + 1.0d)) * ((double) width);
        }
        int iconSize = (int) validSize;
        if (iconSize % 2 != 0) {
            return (int) (0.5d + validSize);
        }
        return iconSize;
    }

    /* access modifiers changed from: package-private */
    public Bitmap createBitmap(Drawable srcDraw) {
        if (srcDraw == null) {
            Log.w(TAG, "notification icon is null!");
            return null;
        } else if (!(srcDraw instanceof BitmapDrawable)) {
            return null;
        } else {
            Bitmap temp = ((BitmapDrawable) srcDraw).getBitmap();
            int width = temp.getWidth();
            int height = temp.getHeight();
            int[] pixels = new int[(width * height)];
            temp.getPixels(pixels, 0, width, 0, 0, width, height);
            return Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888);
        }
    }

    public Drawable getJoinBitmap(Drawable srcDraw, int backgroundId) {
        BitmapDrawable bitmapDrawable = null;
        if (srcDraw == null) {
            return null;
        }
        synchronized (HwResourcesImpl.class) {
            Bitmap bmpSrc = createBitmap(srcDraw);
            if (bmpSrc == null) {
                Log.w(TAG, "getJoinBitmap : bmpSrc is null!");
                return null;
            }
            ZipFileCache iconZipFileCache = ZipFileCache.getAndCheckCachedZipFile(getThemeDir(), ICONS_ZIP_FILE);
            if (iconZipFileCache == null) {
                Log.w(TAG, "getJoinBitmap : iconZipFileCache == null");
                return null;
            }
            ResourcesImplEx resourcesImplEx = this.mResourcesImpl;
            Bitmap bmpBg = iconZipFileCache.getBitmapEntry(resourcesImplEx, NOTIFICATION_ICON_PREFIX + backgroundId + PNG_SUFFIX);
            if (bmpBg == null) {
                bmpBg = iconZipFileCache.getBitmapEntry(this.mResourcesImpl, NOTIFICATION_ICON_EXIST);
            }
            Bitmap bmpMask = iconZipFileCache.getBitmapEntry(this.mResourcesImpl, NOTIFICATION_ICON_MASK);
            Bitmap bmpBorder = iconZipFileCache.getBitmapEntry(this.mResourcesImpl, NOTIFICATION_ICON_BORDER);
            if (!(bmpBg == null || bmpMask == null)) {
                if (bmpBorder != null) {
                    int width = bmpSrc.getWidth();
                    int height = bmpSrc.getHeight();
                    if (width == 1 && height == 1) {
                        Bitmap overlap = IconBitmapUtils.overlap2Bitmap(bmpSrc, bmpBg);
                        if (overlap != null) {
                            bitmapDrawable = new BitmapDrawable(overlap);
                        }
                        return bitmapDrawable;
                    }
                    if (!(bmpMask.getWidth() == width || bmpMask.getHeight() == height)) {
                        bmpMask = IconBitmapUtils.drawSource(bmpMask, width, width);
                    }
                    if (!(bmpBorder.getWidth() == width || bmpBorder.getHeight() == height)) {
                        bmpBorder = IconBitmapUtils.drawSource(bmpBorder, width, width);
                    }
                    if (!(bmpBg.getWidth() == width || bmpBg.getHeight() == height)) {
                        bmpBg = IconBitmapUtils.drawSource(bmpBg, width, width);
                    }
                    Bitmap result = IconBitmapUtils.composeIcon(bmpSrc, bmpMask, bmpBg, bmpBorder, false);
                    if (result != null) {
                        return new BitmapDrawable(result);
                    }
                    Log.w(TAG, "getJoinBitmap is null!");
                    return null;
                }
            }
            StringBuilder sb = new StringBuilder();
            sb.append("getJoinBitmap :");
            sb.append(bmpBg == null ? " bmpBg == null, " : " bmpBg != null, ");
            sb.append(" id = ");
            sb.append(backgroundId);
            sb.append(bmpMask == null ? " bmpMask == null, " : "bmpMask != null, ");
            sb.append(bmpBorder == null ? " bmpBorder == null, " : " bmpBorder != null");
            Log.w(TAG, sb.toString());
            return null;
        }
    }

    public String getThemeDir() {
        return HwThemeManager.HWT_PATH_SKIN;
    }

    private static boolean emptyOrContainsPreloadedZip(String zipFile) {
        boolean z;
        synchronized (PRELOADED_HW_THEME_ZIP_LOCK) {
            if (!PRELOADED_HW_THEME_ZIPS.isEmpty()) {
                if (!PRELOADED_HW_THEME_ZIPS.contains(zipFile)) {
                    z = false;
                }
            }
            z = true;
        }
        return z;
    }

    private String getDiffThemeIconPath() {
        File iconZip;
        if (!TextUtils.isEmpty(CUSTOM_DIFF_THEME_DIR)) {
            if (new File(CUSTOM_DIFF_THEME_DIR + SLASH + ICONS_ZIP_FILE).exists()) {
                return CUSTOM_DIFF_THEME_DIR;
            }
            return null;
        }
        try {
            iconZip = HwCfgFilePolicy.getCfgFile(DIFF_THEME_ICON, 0);
        } catch (NoClassDefFoundError e) {
            iconZip = null;
            Log.e(TAG, "HwCfgFilePolicy NoClassDefFoundError");
        }
        if (iconZip != null) {
            return iconZip.getParent();
        }
        return null;
    }

    private Bitmap getIconsFromDiffTheme(ResourcesImplEx impl, String imgFile, String packageName) {
        ZipFileCache diffIconZipFileCache;
        String iconFilePath = getDiffThemeIconPath();
        if (iconFilePath == null || (diffIconZipFileCache = ZipFileCache.getAndCheckCachedZipFile(iconFilePath, ICONS_ZIP_FILE)) == null) {
            return null;
        }
        Bitmap bmp = diffIconZipFileCache.getBitmapEntry(impl, imgFile);
        if (bmp != null) {
            Log.i(TAG, "icon : " + imgFile + " found in custom diff theme");
            return bmp;
        } else if (imgFile.equals(getIconFileName(packageName))) {
            return bmp;
        } else {
            Bitmap bmp2 = diffIconZipFileCache.getBitmapEntry(impl, getIconFileName(packageName));
            Log.i(TAG, "icon : " + imgFile + " package name found in custom diff theme");
            return bmp2;
        }
    }

    public void preloadHwThemeZipsAndSomeIcons(int currentUserId) {
        String path = Environment.getDataDirectory() + "/themes/" + currentUserId;
        synchronized (PRELOADED_HW_THEME_ZIP_LOCK) {
            File themePath = new File(path);
            if (themePath.exists()) {
                File[] files = themePath.listFiles((FileFilter) new HwThemeManager.HwThemeManagerFileFilter());
                if (files != null) {
                    for (File file : files) {
                        PRELOADED_HW_THEME_ZIPS.add(file.getName());
                    }
                }
            }
        }
    }

    public void clearHwThemeZipsAndSomeIcons() {
        synchronized (HwResourcesImpl.class) {
            clearHwThemeZipsAndIconsCache();
        }
        clearPreloadedHwThemeZipsCache();
    }

    private void clearHwThemeZipsAndIconsCache() {
        List<Bitmap> list = sBgList;
        if (list != null) {
            for (Bitmap bm : list) {
                bm.recycle();
            }
            sBgList.clear();
        }
        if (sBmpBorder != null && !sBmpBorder.isRecycled()) {
            sBmpBorder.recycle();
            sBmpBorder = null;
        }
        if (sBmpMask != null && !sBmpMask.isRecycled()) {
            sBmpMask.recycle();
            Log.w(TAG, "sBmpMask recycle sBmpMask = " + sBmpMask);
            sBmpMask = null;
            sMaskSizeWithoutEdge = -1;
        }
        ZipFileCache.clear();
    }

    private void clearPreloadedHwThemeZipsCache() {
        synchronized (PRELOADED_HW_THEME_ZIP_LOCK) {
            PRELOADED_HW_THEME_ZIPS.clear();
        }
    }

    private void inflateColorInfoList(XmlPullParser parser, String packageName, boolean isZipFile) throws XmlPullParserException, IOException {
        int innerDepth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if (type == 1) {
                return;
            }
            if (parser.getDepth() <= innerDepth && type == 3) {
                return;
            }
            if (type == 2) {
                String labelName = parser.getName();
                if ("color".equals(labelName) || "drawable".equals(labelName)) {
                    String name = parser.getAttributeName(0);
                    String colorName = parser.getAttributeValue(0);
                    String text = null;
                    if (parser.next() == 4) {
                        text = parser.getText();
                    }
                    if (XML_ATTRIBUTE_NAME.equalsIgnoreCase(name)) {
                        int colorValue = getColorValueFromStr(text);
                        String fullColorName = packageName + COLON + labelName + SLASH + colorName;
                        if (isZipFile) {
                            sCacheColorInfoList.put(fullColorName, Integer.valueOf(colorValue));
                        } else {
                            sAssetCacheColorInfoList.put(fullColorName, Integer.valueOf(colorValue));
                        }
                    }
                }
            }
        }
    }

    private int getColorValueFromStr(String colorValue) {
        if (colorValue == null) {
            return 0;
        }
        if (!colorValue.startsWith(HASH_TAG)) {
            Log.e(TAG, "getColorValueFromStr " + String.format(Locale.ROOT, "Color value '%s' must start with #", colorValue));
            return 0;
        }
        String value = colorValue.substring(1);
        if (value.length() > 8) {
            Log.e(TAG, "getColorValueFromStr " + String.format(Locale.ROOT, "Color value '%s' is too long. Format is either#AARRGGBB, #RRGGBB, #RGB, or #ARGB", value));
            return 0;
        }
        if (value.length() == 3) {
            char[] color = new char[8];
            color[1] = 'F';
            color[0] = 'F';
            char charAt = value.charAt(0);
            color[3] = charAt;
            color[2] = charAt;
            char charAt2 = value.charAt(1);
            color[5] = charAt2;
            color[4] = charAt2;
            char charAt3 = value.charAt(2);
            color[7] = charAt3;
            color[6] = charAt3;
            value = new String(color);
        } else if (value.length() == 4) {
            char[] color2 = new char[8];
            char charAt4 = value.charAt(0);
            color2[1] = charAt4;
            color2[0] = charAt4;
            char charAt5 = value.charAt(1);
            color2[3] = charAt5;
            color2[2] = charAt5;
            char charAt6 = value.charAt(2);
            color2[5] = charAt6;
            color2[4] = charAt6;
            char charAt7 = value.charAt(3);
            color2[7] = charAt7;
            color2[6] = charAt7;
            value = new String(color2);
        } else if (value.length() == 6) {
            value = "FF" + value;
        }
        return (int) Long.parseLong(value, 16);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x001e, code lost:
        r2 = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new java.io.File(r11, r12));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0022, code lost:
        android.util.Log.e(android.content.res.HwResourcesImpl.TAG, "getLayoutXML Exception filename.");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x002b, code lost:
        android.util.Log.e(android.content.res.HwResourcesImpl.TAG, "getLayoutXML IOException filename.");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0034, code lost:
        android.util.Log.e(android.content.res.HwResourcesImpl.TAG, "getLayoutXML SAXException filename.");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x003d, code lost:
        android.util.Log.e(android.content.res.HwResourcesImpl.TAG, "getLayoutXML ParserConfigurationException.");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0045, code lost:
        r2 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0046, code lost:
        if (r2 != null) goto L_0x004b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x004a, code lost:
        return android.content.res.HwResourcesImpl.isHwThemes;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x004b, code lost:
        r3 = r2.getDocumentElement();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x004f, code lost:
        if (r3 == null) goto L_0x0051;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0053, code lost:
        return android.content.res.HwResourcesImpl.isHwThemes;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x0054, code lost:
        r4 = r3.getChildNodes();
        r5 = r4.getLength();
        r0 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x005e, code lost:
        if (r0 < r5) goto L_0x0060;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x0060, code lost:
        r7 = r4.item(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x0068, code lost:
        if (r7.getNodeType() != 1) goto L_0x009c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x0076, code lost:
        r8 = r7.getTextContent();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x0080, code lost:
        if (android.content.res.HwResourcesImpl.EMUI.equals(r8) != false) goto L_0x008d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x008b, code lost:
        r9 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x008d, code lost:
        r9 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x008e, code lost:
        android.content.res.HwResourcesImpl.isHwThemes = r9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x0092, code lost:
        monitor-enter(android.content.res.HwResourcesImpl.class);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:?, code lost:
        android.content.res.HwResourcesImpl.isThemeDescriptionHasRead = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:48:0x0098, code lost:
        return android.content.res.HwResourcesImpl.isHwThemes;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:0x009c, code lost:
        r0 = r0 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:54:0x00a1, code lost:
        monitor-enter(android.content.res.HwResourcesImpl.class);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:?, code lost:
        android.content.res.HwResourcesImpl.isThemeDescriptionHasRead = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:59:0x00a7, code lost:
        return android.content.res.HwResourcesImpl.isHwThemes;
     */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x0048  */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x004b  */
    private boolean isCurrentHwTheme(String dirpath, String filename) {
        synchronized (HwResourcesImpl.class) {
            if (isThemeDescriptionHasRead) {
                return isHwThemes;
            }
        }
    }

    private Bitmap getDarkerBitmap(Bitmap bmp) {
        Bitmap resultBmp = bmp;
        if ((this.mResourcesImpl.getConfiguration().uiMode & 48) == 32) {
            if (!resultBmp.isMutable()) {
                resultBmp = resultBmp.copy(Bitmap.Config.ARGB_8888, true);
            }
            new Canvas(resultBmp).drawColor(COLOR_FOR_DARKER_ICON, PorterDuff.Mode.MULTIPLY);
        }
        return resultBmp;
    }

    public DisplayMetrics hwGetDisplayMetrics() {
        return this.mResourcesImpl.getDisplayMetrics();
    }

    public void printErrorResource() {
        Log.i("Resource", "printErrorResource, maybe not an error because module has entative action to load resource.");
        ResourcesImplEx resourcesImplEx = this.mResourcesImpl;
        if (resourcesImplEx != null) {
            resourcesImplEx.dumpApkAssets();
        }
    }

    public void initDeepTheme() {
        if (HwThemeManager.isHonorProduct()) {
            if (UserHandleEx.getAppId(Process.myUid()) < 1000) {
                Log.w(TAG, "zygote not need to initDeepTheme");
                return;
            }
            this.mCurrentDeepTheme = AssetManagerEx.getDeepType(this.mResourcesImpl.getAssets());
            this.mDeepThemeType = getThemeType();
        }
    }

    public boolean removeIconCache(String packageName) {
        Log.i(TAG, "remove icon, packageName = " + packageName);
        return IconCache.removeByPackageName(packageName);
    }
}
