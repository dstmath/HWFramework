package android.content.res;

import android.annotation.SuppressLint;
import android.app.ActivityThread;
import android.content.Context;
import android.content.res.AbsResourcesImpl;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.hwtheme.HwThemeManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Process;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.LongSparseArray;
import android.util.TypedValue;
import android.util.Xml;
import com.android.internal.util.XmlUtils;
import com.huawei.utils.reflect.EasyInvokeFactory;
import huawei.android.hwutil.AssetsFileCache;
import huawei.android.hwutil.IconBitmapUtils;
import huawei.android.hwutil.IconCache;
import huawei.android.hwutil.ZipFileCache;
import huawei.cust.HwCfgFilePolicy;
import huawei.cust.HwCustUtils;
import java.io.BufferedInputStream;
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
import java.util.Random;
import java.util.regex.Pattern;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class HwResourcesImpl extends AbsResourcesImpl {
    private static final String ANDROID_RES = "android";
    private static final String ANDROID_RES_EXT = "androidhwext";
    private static final String[] APP_WHILTLIST_ADD = {"com.android.chrome", "com.android.vending", "com.whatsapp", "com.facebook.orca"};
    private static final String CUSTOM_DIFF_THEME_DIR = SystemProperties.get("ro.config.diff_themes");
    private static final String DARK = "dark";
    private static final String DARK_OVERLAY_RES = "com.android.frameworkhwext.dark";
    private static final boolean DEBUG = false;
    private static final boolean DEBUG_DRAWABLE = false;
    private static final boolean DEBUG_ICON = false;
    private static final boolean DEBUG_VERBOSE_ICON = false;
    private static final String[] DEEPTHEME_WHILTLIST_ADD = {"/system/priv-app/Settings/Settings.apk", "/system/priv-app/Contacts/Contacts.apk"};
    private static final String DEFAULT_CONFIG_NAME = "xml/hw_launcher_load_icon.xml";
    private static final int DEFAULT_EDGE_SIZE = 8;
    private static final String DEFAULT_MULTI_DPI_WHITELIST = "/system/etc/xml/multidpi_whitelist.xml";
    private static final String DEFAULT_NO_DRAWABLE_FLAG = "null";
    private static final String DEFAULT_RES_DIR = "res/drawable-xxhdpi";
    private static final int DELTA_X_OF_BACKGROUND = 0;
    private static final int DELTA_Y_OF_BACKGROUND = 0;
    private static final String DIFF_THEME_ICON = "/themes/diff/icons";
    private static final String DPI_2K = "xxxhdpi";
    private static final String DPI_FHD = "xxhdpi";
    private static final String DRAWABLE_FHD = "drawable-xxhdpi";
    private static final String DYNAMIC_ICONS = "dynamic_icons";
    private static final String EMUI = "EMUI";
    private static final String EMUI_TAG = (File.separator + "emui_");
    private static final String FILE_DESCRIPTION = "description.xml";
    private static final String FILE_MULTI_DPI_WHITELIST = "xml/multidpi_whitelist.xml";
    private static final String FRAMEWORK_RES = "framework-res";
    private static final String FRAMEWORK_RES_EXT = "framework-res-hwext";
    private static final String GOOGLE_KEYWORD = "com.google";
    private static final String HONOR = "honor";
    private static final String HONOR_OVERLAY_RES = "com.android.frameworkhwext.honor";
    private static final String ICONS_ZIPFILE = "icons";
    private static final String ICON_BACKGROUND_PREFIX = "icon_background";
    private static final String ICON_BORDER_FILE = "icon_border.png";
    private static final String ICON_BORDER_UNFLAT_FILE = "icon_border_unflat.png";
    private static final String ICON_MASK_ALL_FILE = "icon_mask_all.png";
    private static final String ICON_MASK_FILE = "icon_mask.png";
    private static final boolean IS_CUSTOM_ICON = SystemProperties.getBoolean("ro.config.custom_icon", false);
    private static final int LEN_OF_ANDROID = 7;
    private static final int LEN_OF_ANDROID_EXT = 12;
    private static final int LEN_OF_ANDROID_EXT_DARK = 31;
    private static final int LEN_OF_ANDROID_EXT_HONOR = 32;
    private static final int LEN_OF_ANDROID_EXT_NOVA = 31;
    private static final int MASK_ABS_VALID_RANGE = 10;
    private static final String NOTIFICATION_ICON_BORDER = "ic_stat_notify_icon_border.png";
    private static final String NOTIFICATION_ICON_EXIST = "ic_stat_notify_bg_0.png";
    private static final String NOTIFICATION_ICON_MASK = "ic_stat_notify_icon_mask.png";
    private static final String NOTIFICATION_ICON_PREFIX = "ic_stat_notify_bg_";
    private static final String NOVA = "nova";
    private static final String NOVA_OVERLAY_RES = "com.android.frameworkhwext.nova";
    static final String TAG = "HwResourcesImpl";
    private static final String TAG_CONFIG = "launcher_config";
    private static final String THEMEDISIGNER = "designer";
    static final String THEME_ANIMATE_FOLDER_ICON_NAME = "portal_ring_inner_holo.png.animate";
    static final String THEME_FOLDER_ICON_NAME = "portal_ring_inner_holo.png";
    private static final String TYPT_ACTIVITY_ICON = "activityIcon";
    private static final String TYPT_BACKGROUND = "background";
    private static final String TYPT_ICON = "icon";
    private static final String XML_ATTRIBUTE_PACKAGE_NAME = "name";
    private static final String XML_SUFFIX = ".xml";
    private static final boolean isIconSupportCut = SystemProperties.getBoolean("ro.config.hw_icon_supprot_cut", true);
    private static HashMap<String, Integer> mAssetCacheColorInfoList = new HashMap<>();
    private static HashMap<String, String> mCacheAssetsList = new HashMap<>();
    private static HashMap<String, Integer> mCacheColorInfoList = new HashMap<>();
    private static int mCurrentUserId = 0;
    private static HwCustHwResources mCustHwResources = null;
    private static HashSet<String> mNonThemedPackage = new HashSet<>();
    private static HashSet<String> mPreloadedAssetsPackage = new HashSet<>();
    private static HashSet<String> mPreloadedThemeColorList = new HashSet<>();
    private static String mThemeAbsoluteDir = (Environment.getDataDirectory() + "/themes/0");
    private static ResourcesUtils resUtils = ((ResourcesUtils) EasyInvokeFactory.getInvokeUtils(ResourcesUtils.class));
    private static ArrayList<Bitmap> sBgList = new ArrayList<>();
    private static Bitmap sBmpBorder = null;
    private static Bitmap sBmpMask = null;
    private static final Object sCacheLock = new Object();
    private static final int sConfigAppBigIconSize = SystemProperties.getInt("ro.config.app_big_icon_size", -1);
    private static final int sConfigAppInnerIconSize = SystemProperties.getInt("ro.config.app_inner_icon_size", -1);
    private static boolean sDefaultConfigHasRead = false;
    private static int sDefaultSizeWithoutEdge = -1;
    private static HashSet<String> sDisThemeActivityIcon = null;
    private static HashSet<String> sDisThemeBackground = null;
    private static HashSet<String> sDisThemeIcon = null;
    private static boolean sIsHWThemes = true;
    private static int sMaskSizeWithoutEdge = -1;
    private static boolean sMultiDirHasRead = false;
    private static HashSet<String> sMultiDirPkgNames = new HashSet<>();
    private static Pattern sPattern = Pattern.compile("-v\\d+/");
    private static LongSparseArray<Drawable.ConstantState> sPreloadedColorDrawablesEx = new LongSparseArray<>();
    private static LongSparseArray<Drawable.ConstantState> sPreloadedDrawablesEx = new LongSparseArray<>();
    private static final Object sPreloadedHwThemeZipLock = new Object();
    private static final HashSet<String> sPreloadedHwThemeZips = new HashSet<>();
    private static boolean sSerbiaLocale = false;
    private static int sStandardBgSize = -1;
    private static int sStandardIconSize = -1;
    private static Object sThemeColorArrayLock = new Object();
    private static boolean sThemeDescripHasRead = false;
    private static int sUseAvgColor = -1;
    private final DrawableCache mColorDrawableCacheEx = new DrawableCache();
    protected int mConfigHwt = 0;
    private boolean mContainPackage = false;
    private int mCurrentDeepTheme = 0;
    private String mDarkThemeType = "";
    private String mDeepThemeType = "";
    private final DrawableCache mDrawableCacheEx = new DrawableCache();
    private final DrawableCache mDynamicDrawableCache = new DrawableCache();
    private List<Integer> mOutThemeColorIdArray = new ArrayList();
    protected String mPackageName = null;
    private ResourcesImpl mResourcesImpl;
    protected boolean mThemeChanged = false;
    private HashMap<Integer, AbsResourcesImpl.ThemeColor> mThemeColorArray = new HashMap<>();
    private TypedValue mTmpValue = new TypedValue();
    private final Object mTmpValueLock = new Object();
    private ArrayList<String> recPackageList = new ArrayList<>();

    static class HwThemeFileFilter implements FileFilter {
        HwThemeFileFilter() {
        }

        public boolean accept(File pathname) {
            if (!pathname.isFile() || pathname.getName().toLowerCase(Locale.getDefault()).endsWith(HwResourcesImpl.XML_SUFFIX)) {
                return false;
            }
            return true;
        }
    }

    private void getAllBgImage(String path, String zip) {
        if (sBmpBorder != null) {
            if (sBgList == null || sBgList.isEmpty()) {
                ZipFileCache iconZipFileCache = ZipFileCache.getAndCheckCachedZipFile(getThemeDir(), ICONS_ZIPFILE);
                if (iconZipFileCache != null) {
                    sBgList = iconZipFileCache.getBitmapList(this.mResourcesImpl, ICON_BACKGROUND_PREFIX);
                    if (sBgList != null) {
                        int bgListSize = sBgList.size();
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
    }

    private Bitmap getRandomBgImage(String idAndPackageName) {
        if (sBgList == null || sBgList.isEmpty()) {
            getAllBgImage(getThemeDir(), ICONS_ZIPFILE);
        }
        int len = 0;
        if (sBgList != null) {
            len = sBgList.size();
        }
        if (len <= 0) {
            return null;
        }
        return sBgList.get(getCode(idAndPackageName) % len);
    }

    public int getCode(String idAndPackageName) {
        int code = 0;
        int idPkgNameLength = idAndPackageName.length();
        for (int i = 0; i < idPkgNameLength; i++) {
            code += idAndPackageName.charAt(i);
        }
        return code;
    }

    private void initMaskSizeWithoutEdge(boolean useDefault) {
        if (-1 != sMaskSizeWithoutEdge) {
            return;
        }
        if (sBmpMask != null && !sBmpMask.isRecycled()) {
            Rect info = IconBitmapUtils.getIconInfo(sBmpMask);
            if (info != null) {
                sMaskSizeWithoutEdge = info.width() + 1;
            } else {
                sMaskSizeWithoutEdge = sStandardBgSize - 8;
            }
        } else if (useDefault) {
            sMaskSizeWithoutEdge = sStandardBgSize - 8;
        }
    }

    private void initMask(ZipFileCache iconZipFileCache) {
        if (sBmpMask == null && iconZipFileCache != null) {
            sBmpMask = iconZipFileCache.getBitmapEntry(this.mResourcesImpl, ICON_MASK_FILE);
            synchronized (HwResourcesImpl.class) {
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

    public static synchronized HwCustHwResources getHwCustHwResources() {
        HwCustHwResources hwCustHwResources;
        synchronized (HwResourcesImpl.class) {
            if (mCustHwResources == null) {
                mCustHwResources = (HwCustHwResources) HwCustUtils.createObj(HwCustHwResources.class, new Object[0]);
            }
            hwCustHwResources = mCustHwResources;
        }
        return hwCustHwResources;
    }

    public Bitmap getThemeIconByName(String name) {
        String imgFile = name;
        if (!imgFile.endsWith(".png")) {
            imgFile = imgFile + ".png";
        }
        if (THEME_ANIMATE_FOLDER_ICON_NAME.equalsIgnoreCase(imgFile)) {
            imgFile = THEME_FOLDER_ICON_NAME;
        }
        Bitmap bmp = null;
        synchronized (HwResourcesImpl.class) {
            ZipFileCache iconZipFileCache = ZipFileCache.getAndCheckCachedZipFile(getThemeDir(), ICONS_ZIPFILE);
            if (iconZipFileCache != null) {
                bmp = iconZipFileCache.getBitmapEntry(this.mResourcesImpl, imgFile);
            }
            if (bmp == null) {
                return bmp;
            }
            Bitmap srcBitmap = IconBitmapUtils.zoomIfNeed(bmp, sStandardBgSize, true);
            return srcBitmap;
        }
    }

    public Bitmap addShortcutBackgroud(Bitmap bmpSrc) {
        synchronized (HwResources.class) {
            ZipFileCache iconZipFileCache = ZipFileCache.getAndCheckCachedZipFile(getThemeDir(), ICONS_ZIPFILE);
            if (iconZipFileCache == null) {
                return null;
            }
            initBorder(iconZipFileCache);
            initMask(iconZipFileCache);
            Bitmap bmpBg = null;
            if (sBgList == null || sBgList.isEmpty()) {
                getAllBgImage(getThemeDir(), ICONS_ZIPFILE);
            }
            if (sBgList != null) {
                int len = sBgList.size();
                if (len > 0) {
                    bmpBg = sBgList.get(new Random().nextInt(len));
                }
            }
            initMaskSizeWithoutEdge(true);
            int w = bmpSrc.getWidth();
            int h = bmpSrc.getHeight();
            int iconSize = sDefaultSizeWithoutEdge;
            Rect r = IconBitmapUtils.getIconInfo(bmpSrc);
            if (r != null) {
                iconSize = computeDestIconSize(r, sMaskSizeWithoutEdge, w, h);
            }
            if (iconSize >= sDefaultSizeWithoutEdge) {
                boolean isZoomOutIcon = isZoomOutIcon(r, w, h);
                Log.w(TAG, "shortcut in Original image is too large.iconSize:" + iconSize + ",w*h: " + w + "*" + h + " . so cut,sDefaultSizeWithoutEdge=" + sDefaultSizeWithoutEdge + "; r=" + r + " ; isZoomOutIcon=" + isZoomOutIcon);
                iconSize = isZoomOutIcon ? sStandardBgSize - 16 : iconSize;
            }
            Bitmap composeIcon = IconBitmapUtils.composeIcon(IconBitmapUtils.drawSource(bmpSrc, sStandardBgSize, iconSize), sBmpMask, bmpBg, sBmpBorder, false);
            return composeIcon;
        }
    }

    private Bitmap addIconBackgroud(Bitmap bmpSrc, String idAndPackageName, boolean composeIcon) {
        synchronized (HwResourcesImpl.class) {
            ZipFileCache iconZipFileCache = ZipFileCache.getAndCheckCachedZipFile(getThemeDir(), ICONS_ZIPFILE);
            if (iconZipFileCache == null) {
                return null;
            }
            initBorder(iconZipFileCache);
            initMask(iconZipFileCache);
            Bitmap bmpBg = getRandomBgImage(idAndPackageName);
            initMaskSizeWithoutEdge(true);
            int w = bmpSrc.getWidth();
            int h = bmpSrc.getHeight();
            int iconSize = sDefaultSizeWithoutEdge;
            Rect r = IconBitmapUtils.getIconInfo(bmpSrc);
            if (r != null) {
                iconSize = computeDestIconSize(r, sMaskSizeWithoutEdge, w, h);
            }
            if (iconSize >= sDefaultSizeWithoutEdge) {
                boolean isZoomOutIcon = isZoomOutIcon(r, w, h);
                Log.w(TAG, "icon in Original image " + idAndPackageName + " is too large.iconSize:" + iconSize + ",w*h: " + w + "*" + h + " . so cut,sDefaultSizeWithoutEdge=" + sDefaultSizeWithoutEdge + "; r=" + r + " ; isZoomOutIcon=" + isZoomOutIcon);
                iconSize = isZoomOutIcon ? sStandardBgSize - 16 : iconSize;
            }
            if (w == 1 && h == 1) {
                Bitmap overlap2Bitmap = IconBitmapUtils.overlap2Bitmap(bmpSrc, bmpBg);
                return overlap2Bitmap;
            }
            Bitmap bmpSrcStd = IconBitmapUtils.drawSource(bmpSrc, sStandardBgSize, iconSize);
            if (!composeIcon) {
                return bmpSrcStd;
            }
            Bitmap composeIcon2 = IconBitmapUtils.composeIcon(bmpSrcStd, sBmpMask, bmpBg, sBmpBorder, false);
            return composeIcon2;
        }
    }

    private boolean isZoomOutIcon(Rect validRect, int w, int h) {
        boolean z = false;
        if (validRect == null) {
            return false;
        }
        if (w - validRect.width() < 10 || h - validRect.height() < 10) {
            z = true;
        }
        return z;
    }

    /* access modifiers changed from: protected */
    public Drawable handleAddIconBackground(Resources res, int id, Drawable dr) {
        String resourcesPackageName;
        if (id != 0) {
            String idAndPackageName = id + "#" + resourcesPackageName;
            if (IconCache.contains(idAndPackageName)) {
                boolean isAdaptiveIcon = dr instanceof AdaptiveIconDrawable;
                String packageName = this.mPackageName != null ? this.mPackageName : resourcesPackageName;
                Bitmap bmp = IconBitmapUtils.drawableToBitmap(dr);
                if (bmp == null) {
                    return dr;
                }
                if (bmp.getWidth() > sDefaultSizeWithoutEdge * 3 || bmp.getHeight() > 3 * sDefaultSizeWithoutEdge) {
                    Log.w(TAG, "icon in pkg " + packageName + " is too large.sDefaultSizeWithoutEdge = " + sDefaultSizeWithoutEdge + "bmp.getWidth() = " + bmp.getWidth());
                    bmp = IconBitmapUtils.zoomIfNeed(bmp, sDefaultSizeWithoutEdge, true);
                    dr = new BitmapDrawable(res, bmp);
                    ((BitmapDrawable) dr).setTargetDensity(this.mResourcesImpl.getDisplayMetrics());
                }
                ZipFileCache iconZipFileCache = ZipFileCache.getAndCheckCachedZipFile(getThemeDir(), ICONS_ZIPFILE);
                if (iconZipFileCache == null) {
                    return dr;
                }
                initMask(iconZipFileCache);
                Bitmap resultBitmap = null;
                if ((!isIconSupportCut && isCurrentHwTheme(getThemeDir(), FILE_DESCRIPTION)) || checkWhiteListApp(packageName)) {
                    resultBitmap = addIconBackgroud(bmp, idAndPackageName, isAdaptiveIcon);
                } else if ((getHwCustHwResources() == null || getHwCustHwResources().isUseThemeIconAndBackground()) && (sDisThemeBackground == null || !sDisThemeBackground.contains(packageName))) {
                    resultBitmap = addIconBackgroud(bmp, idAndPackageName, true);
                }
                if (resultBitmap == null) {
                    resultBitmap = IconBitmapUtils.zoomIfNeed(bmp, sStandardBgSize, false);
                }
                if (resultBitmap != null) {
                    Drawable dr2 = new BitmapDrawable(res, resultBitmap);
                    ((BitmapDrawable) dr2).setTargetDensity(this.mResourcesImpl.getDisplayMetrics());
                    return dr2;
                }
            }
        }
        return dr;
    }

    private boolean checkWhiteListApp(String packageName) {
        if (sDisThemeIcon == null || !sDisThemeIcon.contains(packageName) || !isCurrentHwTheme(getThemeDir(), FILE_DESCRIPTION)) {
            return false;
        }
        return true;
    }

    private boolean checkWhiteImgFile(String imgFile) {
        if (sDisThemeActivityIcon == null || !sDisThemeActivityIcon.contains(imgFile)) {
            return false;
        }
        return true;
    }

    private Drawable getThemeIcon(Resources resources, String iconKey, String packageName) {
        if (checkWhiteListApp(packageName)) {
            return null;
        }
        Drawable dr = null;
        Bitmap bmp = null;
        String imgFile = getIconFileName(packageName, IconCache.get(iconKey));
        if (checkWhiteImgFile(imgFile)) {
            return null;
        }
        synchronized (HwResourcesImpl.class) {
            if (IS_CUSTOM_ICON) {
                bmp = getIconsfromDiffTheme(this.mResourcesImpl, imgFile, packageName);
            }
            ZipFileCache iconZipFileCache = ZipFileCache.getAndCheckCachedZipFile(getThemeDir(), ICONS_ZIPFILE);
            if (bmp == null && iconZipFileCache != null) {
                bmp = iconZipFileCache.getBitmapEntry(this.mResourcesImpl, imgFile);
                if (!imgFile.equals(getIconFileName(packageName)) && bmp == null) {
                    bmp = iconZipFileCache.getBitmapEntry(this.mResourcesImpl, getIconFileName(packageName));
                }
            }
            if (bmp != null) {
                Bitmap srcBitmap = IconBitmapUtils.zoomIfNeed(bmp, sStandardBgSize, true);
                if (srcBitmap != null) {
                    dr = new BitmapDrawable(resources, srcBitmap);
                    ((BitmapDrawable) dr).setTargetDensity(this.mResourcesImpl.getDisplayMetrics());
                }
            }
        }
        return dr;
    }

    private AbsResourcesImpl.ThemeColor loadThemeColor(String packageName, String zipName, String resName, String themeXml, int defaultColor) {
        if (!isEmptyPreloadZip(zipName) && !mNonThemedPackage.contains(packageName) && !mPreloadedThemeColorList.contains(packageName)) {
            ZipFileCache packageZipFileCache = ZipFileCache.getAndCheckCachedZipFile(getThemeDir(), zipName);
            if (packageZipFileCache != null) {
                parserColorInfoList(packageName, packageZipFileCache.getInputStreamEntry(themeXml), true);
                mPreloadedThemeColorList.add(packageName);
            } else {
                mNonThemedPackage.add(packageName);
            }
        }
        Integer value = null;
        if (!mCacheColorInfoList.isEmpty()) {
            value = mCacheColorInfoList.get(resName);
        }
        String themeType = this.mDeepThemeType;
        if (!TextUtils.isEmpty(themeType) && value == null) {
            value = mAssetCacheColorInfoList.get(resName);
            if (value == null && !mPreloadedThemeColorList.contains(packageName) && !mPreloadedAssetsPackage.contains(packageName)) {
                loadThemeColorFromAssets(packageName, themeXml, themeType);
                mPreloadedAssetsPackage.add(packageName);
                value = mAssetCacheColorInfoList.get(resName);
            }
        }
        if (value != null) {
            return new AbsResourcesImpl.ThemeColor(value.intValue(), true);
        }
        return new AbsResourcesImpl.ThemeColor(defaultColor, false);
    }

    private void parserColorInfoList(String packageName, InputStream is, boolean isZipFile) {
        if (is != null) {
            BufferedInputStream bufferedInput = new BufferedInputStream(is);
            try {
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(bufferedInput, null);
                inflateColorInfoList(parser, packageName, isZipFile);
                try {
                    bufferedInput.close();
                    is.close();
                } catch (IOException e) {
                    Log.e(TAG, "loadThemeColor : IOException when close stream");
                }
            } catch (XmlPullParserException e2) {
                Log.e(TAG, "loadThemeColor : XmlPullParserException");
                bufferedInput.close();
                is.close();
            } catch (IOException e3) {
                Log.e(TAG, "loadThemeColor : IOException");
                bufferedInput.close();
                is.close();
            } catch (RuntimeException e4) {
                Log.e(TAG, "loadThemeColor : RuntimeException", e4);
                bufferedInput.close();
                is.close();
            } catch (Throwable th) {
                try {
                    bufferedInput.close();
                    is.close();
                } catch (IOException e5) {
                    Log.e(TAG, "loadThemeColor : IOException when close stream");
                }
                throw th;
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x001f, code lost:
        r3 = sThemeColorArrayLock;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0022, code lost:
        monitor-enter(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0029, code lost:
        if (r7.mThemeColorArray.isEmpty() != false) goto L_0x0039;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0037, code lost:
        r10 = r7.mThemeColorArray.get(java.lang.Integer.valueOf(r19));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0039, code lost:
        r10 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:?, code lost:
        monitor-exit(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x003b, code lost:
        if (r10 == null) goto L_0x003e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x003d, code lost:
        return r10;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x003e, code lost:
        r12 = getThemeResource(r19, null);
        r13 = r12.packageName;
        r14 = r12.resName;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x004f, code lost:
        if (r13.equals(FRAMEWORK_RES) != false) goto L_0x005b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0057, code lost:
        if (r13.equals(FRAMEWORK_RES_EXT) == false) goto L_0x005c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x005b, code lost:
        r1 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x005d, code lost:
        if (r1 == false) goto L_0x00d2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x0063, code lost:
        if (getPackageName() == null) goto L_0x00d2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x006d, code lost:
        if (getPackageName().isEmpty() != false) goto L_0x00d2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x006f, code lost:
        r1 = loadThemeColor(getPackageName() + "/" + r13, getPackageName(), getPackageName() + "/" + r14, r13 + "/theme.xml", r8.data);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x00bf, code lost:
        if (r1.mIsThemed == false) goto L_0x00d2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x00c1, code lost:
        r2 = sThemeColorArrayLock;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x00c3, code lost:
        monitor-enter(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:?, code lost:
        r7.mThemeColorArray.put(java.lang.Integer.valueOf(r19), r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x00cd, code lost:
        monitor-exit(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x00ce, code lost:
        return r1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x00d2, code lost:
        r1 = loadThemeColor(r13, r13, r14, "theme.xml", r8.data);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x00e0, code lost:
        if (r1.mIsThemed == false) goto L_0x00f3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x00e2, code lost:
        r2 = sThemeColorArrayLock;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:0x00e4, code lost:
        monitor-enter(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:49:?, code lost:
        r7.mThemeColorArray.put(java.lang.Integer.valueOf(r19), r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x00ee, code lost:
        monitor-exit(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:0x00f3, code lost:
        r2 = sThemeColorArrayLock;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:0x00f5, code lost:
        monitor-enter(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:59:0x0100, code lost:
        if (r7.mOutThemeColorIdArray.contains(java.lang.Integer.valueOf(r19)) != false) goto L_0x010b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:60:0x0102, code lost:
        r7.mOutThemeColorIdArray.add(java.lang.Integer.valueOf(r19));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:61:0x010b, code lost:
        monitor-exit(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:62:0x010c, code lost:
        return r1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:66:0x0110, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:67:0x0111, code lost:
        r11 = r19;
        r2 = r10;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:68:0x0115, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:69:0x0116, code lost:
        r11 = r19;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:71:?, code lost:
        monitor-exit(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:72:0x0119, code lost:
        throw r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:73:0x011a, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:74:0x011c, code lost:
        r11 = r19;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:75:0x0125, code lost:
        return new android.content.res.AbsResourcesImpl.ThemeColor(r8.data, false);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:7:0x0014, code lost:
        r1 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x0019, code lost:
        if (r7.mResourcesImpl.mPreloading != false) goto L_0x011c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x001b, code lost:
        if (r9 == false) goto L_0x001f;
     */
    public AbsResourcesImpl.ThemeColor getThemeColor(TypedValue value, int id) throws Resources.NotFoundException {
        TypedValue typedValue = value;
        synchronized (sThemeColorArrayLock) {
            try {
                boolean isOutThemeColorId = this.mOutThemeColorIdArray.contains(Integer.valueOf(id));
                try {
                } catch (Throwable th) {
                    th = th;
                    int i = id;
                    boolean z = isOutThemeColorId;
                    while (true) {
                        try {
                            break;
                        } catch (Throwable th2) {
                            th = th2;
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                int i2 = id;
                while (true) {
                    break;
                }
                throw th;
            }
        }
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:101:0x0170, code lost:
        r9 = getThemeDrawable(r3, r4, r2, r10);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:102:0x0174, code lost:
        if (r9 == null) goto L_0x01ba;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:103:0x0176, code lost:
        if (r12 == null) goto L_0x01ba;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:105:0x017e, code lost:
        if (r12.contains("ic_statusbar_battery") == false) goto L_0x01ba;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:106:0x0180, code lost:
        android.util.Log.d("battery_debug", "HwResourcesImpl#loadDrawable after getThemeDrawable  dr.getIntrinsicWidth() = " + r9.getIntrinsicWidth() + "dr.getIntrinsicHeight() = " + r9.getIntrinsicHeight() + " id = 0x" + java.lang.Integer.toHexString(r19) + " resName = " + r12);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:107:0x01ba, code lost:
        if (r9 == null) goto L_0x01dd;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:108:0x01bc, code lost:
        r13 = sCacheLock;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:109:0x01be, code lost:
        monitor-enter(r13);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:112:0x01c3, code lost:
        if (r1.mResourcesImpl.mPreloading == false) goto L_0x01cf;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:113:0x01c5, code lost:
        sPreloadedDrawablesEx.put(r6, r9.getConstantState());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:114:0x01cf, code lost:
        r1.mDrawableCacheEx.put(r6, r5, r9.getConstantState());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:115:0x01d8, code lost:
        monitor-exit(r13);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:116:0x01d9, code lost:
        return r9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:120:0x01dd, code lost:
        return r9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:80:0x00d6, code lost:
        if (r4 == 0) goto L_0x01dd;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:81:0x00d8, code lost:
        r10 = r1.mResourcesImpl.getResourcePackageName(r4);
        r11 = r4 + "#" + r10;
        r12 = r1.mResourcesImpl.getResourceName(r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:82:0x00fc, code lost:
        if (huawei.android.hwutil.IconCache.contains(r11) == false) goto L_0x0170;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:83:0x00fe, code lost:
        readDefaultConfig();
        r13 = getThemeIcon(r2, r11, r10);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:84:0x0106, code lost:
        if (r13 == null) goto L_0x01dd;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:85:0x0108, code lost:
        if (r12 == null) goto L_0x014e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:87:0x0110, code lost:
        if (r12.contains("ic_statusbar_battery") == false) goto L_0x014e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:88:0x0112, code lost:
        android.util.Log.d("battery_debug", "HwResourcesImpl#loadDrawable iconKey = " + r11 + " mPackageName = " + r1.mPackageName + " id = " + java.lang.Integer.toHexString(r19) + " packageName = " + r10 + " resName = " + r12);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:89:0x014e, code lost:
        r14 = sCacheLock;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:90:0x0150, code lost:
        monitor-enter(r14);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:93:0x0155, code lost:
        if (r1.mResourcesImpl.mPreloading == false) goto L_0x0161;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:94:0x0157, code lost:
        sPreloadedDrawablesEx.put(r6, r13.getConstantState());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:95:0x0161, code lost:
        r1.mDrawableCacheEx.put(r6, r5, r13.getConstantState());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:96:0x016a, code lost:
        monitor-exit(r14);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:97:0x016b, code lost:
        return r13;
     */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x006b  */
    public Drawable loadDrawable(Resources wrapper, TypedValue value, int id, Resources.Theme theme, boolean useCache) throws Resources.NotFoundException {
        Drawable dr;
        Drawable.ConstantState cs;
        Drawable colorValue;
        Resources resources = wrapper;
        TypedValue typedValue = value;
        int i = id;
        Resources.Theme theme2 = theme;
        long key = (((long) typedValue.assetCookie) << 32) | ((long) typedValue.data);
        Drawable dr2 = null;
        Drawable.ConstantState constantState = null;
        if (typedValue.type < 28 || typedValue.type > 31) {
            synchronized (sCacheLock) {
                dr = this.mDrawableCacheEx.getInstance(key, resources, theme2);
            }
            if (dr != null) {
                return dr;
            }
            synchronized (sCacheLock) {
                if (!this.mResourcesImpl.mPreloading) {
                    if (!this.mThemeChanged) {
                        if (resUtils.getPreloadedDensity(this.mResourcesImpl) == this.mResourcesImpl.getConfiguration().densityDpi) {
                            constantState = sPreloadedDrawablesEx.get(key);
                        }
                    }
                }
                Drawable.ConstantState cs2 = constantState;
                if (cs2 != null) {
                    Drawable dr3 = cs2.newDrawable(resources);
                    return dr3;
                }
            }
        } else {
            if (i != 0) {
                synchronized (sCacheLock) {
                    dr2 = this.mColorDrawableCacheEx.getInstance(key, resources, theme2);
                }
                if (dr2 != null) {
                    return dr2;
                }
                synchronized (sCacheLock) {
                    if (!this.mResourcesImpl.mPreloading) {
                        if (!this.mThemeChanged) {
                            constantState = sPreloadedColorDrawablesEx.get(key);
                        }
                    }
                    cs = constantState;
                }
                if (cs != null) {
                    colorValue = cs.newDrawable(resources);
                } else {
                    AbsResourcesImpl.ThemeColor colorValue2 = getThemeColor(typedValue, i);
                    if (colorValue2 != null && colorValue2.mIsThemed) {
                        colorValue = new ColorDrawable(colorValue2.mColor);
                    }
                    if (dr2 != null) {
                        Drawable.ConstantState cs3 = dr2.getConstantState();
                        if (cs3 != null) {
                            synchronized (sCacheLock) {
                                if (this.mResourcesImpl.mPreloading) {
                                    sPreloadedColorDrawablesEx.put(key, cs3);
                                } else {
                                    this.mColorDrawableCacheEx.put(key, theme2, cs3);
                                }
                            }
                        }
                    }
                }
                dr2 = colorValue;
                if (dr2 != null) {
                }
            }
            return dr2;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:76:0x0160 A[RETURN] */
    public Drawable getThemeDrawable(TypedValue value, int id, Resources res, String packageName) throws Resources.NotFoundException {
        String themeType;
        int i;
        Drawable dr;
        int index;
        TypedValue typedValue = value;
        Resources resources = res;
        if (this.mResourcesImpl.mPreloading) {
            return null;
        }
        String packageName2 = getThemeResource(id, packageName).packageName;
        if (typedValue.string == null) {
            return null;
        }
        if (this.mPackageName == null) {
            this.mPackageName = ActivityThread.currentPackageName();
        }
        if (isEmptyPreloadZip(packageName2) && this.mCurrentDeepTheme == 0) {
            return null;
        }
        String file = typedValue.string.toString();
        if (file == null || file.isEmpty() || file.endsWith(XML_SUFFIX)) {
            return null;
        }
        String file2 = sPattern.matcher(file).replaceFirst("/");
        Drawable dr2 = null;
        if (packageName2.indexOf(FRAMEWORK_RES) >= 0 && file2.indexOf("_holo") >= 0) {
            return null;
        }
        boolean isLand = file2.indexOf("-land") >= 0;
        boolean isFramework = packageName2.equals(FRAMEWORK_RES);
        boolean isHwFramework = packageName2.equals(FRAMEWORK_RES_EXT);
        String themeType2 = this.mDeepThemeType;
        if (!isFramework && !isHwFramework) {
            i = -1;
            themeType = themeType2;
        } else if (this.mPackageName == null || this.mPackageName.isEmpty()) {
            i = -1;
            themeType = themeType2;
        } else {
            ZipFileCache frameworkZipFileCache = isEmptyPreloadZip(packageName2) ? null : ZipFileCache.getAndCheckCachedZipFile(getThemeDir(), this.mPackageName);
            if (ANDROID_RES_EXT.equals(this.mPackageName) || "android".equals(this.mPackageName)) {
                ActivityThread activityThread = ActivityThread.currentActivityThread();
                if (activityThread != null) {
                    Context context = activityThread.getApplication();
                    if (context != null) {
                        this.mPackageName = context.getPackageName();
                    }
                }
                frameworkZipFileCache = isEmptyPreloadZip(packageName2) ? null : ZipFileCache.getAndCheckCachedZipFile(getThemeDir(), this.mPackageName);
            }
            ZipFileCache frameworkZipFileCache2 = frameworkZipFileCache;
            String key = packageName2 + "/" + file2;
            if (frameworkZipFileCache2 != null) {
                Drawable dr3 = frameworkZipFileCache2.getDrawableEntry(resources, typedValue, key, null);
                if (dr3 == null && !file2.contains(DRAWABLE_FHD)) {
                    frameworkZipFileCache2.initResDirInfo();
                    if (isFramework) {
                        index = isLand ? 3 : 2;
                    } else {
                        index = isLand ? 5 : 4;
                    }
                    int themeDensity = frameworkZipFileCache2.getDrawableDensity(index);
                    String dir = frameworkZipFileCache2.getDrawableDir(index);
                    if (!(themeDensity == -1 || dir == null)) {
                        BitmapFactory.Options opts = new BitmapFactory.Options();
                        opts.inDensity = themeDensity;
                        StringBuilder sb = new StringBuilder();
                        sb.append(dir);
                        Drawable drawable = dr3;
                        sb.append(File.separator);
                        sb.append(file2.substring(file2.lastIndexOf(File.separator) + 1));
                        dr2 = frameworkZipFileCache2.getDrawableEntry(resources, typedValue, sb.toString(), opts);
                        if (dr2 != null) {
                            return dr2;
                        }
                    }
                }
                dr2 = dr3;
                if (dr2 != null) {
                }
            }
            Drawable dr4 = dr2;
            if (!TextUtils.isEmpty(themeType2) && dr4 == null && frameworkZipFileCache2 == null) {
                ZipFileCache zipFileCache = frameworkZipFileCache2;
                Drawable drawable2 = dr4;
                i = -1;
                themeType = themeType2;
                dr = getFwkDrawableFromAsset(typedValue, resources, packageName2, file2, key, themeType2);
            } else {
                ZipFileCache zipFileCache2 = frameworkZipFileCache2;
                themeType = themeType2;
                Drawable dr5 = dr4;
                i = -1;
                dr = dr5;
            }
            if (dr2 != null) {
                return dr2;
            }
        }
        ZipFileCache packageZipFileCache = isEmptyPreloadZip(packageName2) ? null : ZipFileCache.getAndCheckCachedZipFile(getThemeDir(), packageName2);
        if (packageZipFileCache != null) {
            dr2 = packageZipFileCache.getDrawableEntry(resources, typedValue, file2, null);
        }
        if (packageZipFileCache != null && dr2 == null && !file2.contains(DRAWABLE_FHD)) {
            packageZipFileCache.initResDirInfo();
            int index2 = isLand ? 1 : 0;
            int themeDensity2 = packageZipFileCache.getDrawableDensity(index2);
            String dir2 = packageZipFileCache.getDrawableDir(index2);
            if (!(themeDensity2 == i || dir2 == null)) {
                BitmapFactory.Options opts2 = new BitmapFactory.Options();
                opts2.inDensity = themeDensity2;
                if (file2.contains(DPI_2K)) {
                    dr2 = packageZipFileCache.getDrawableEntry(resources, typedValue, file2.replace(DPI_2K, DPI_FHD), opts2);
                } else {
                    dr2 = packageZipFileCache.getDrawableEntry(resources, typedValue, dir2 + File.separator + file2.substring(file2.lastIndexOf(File.separator) + 1), opts2);
                }
            }
        }
        Drawable dr6 = dr2;
        String themeType3 = themeType;
        if (!TextUtils.isEmpty(themeType3) && dr6 == null && packageZipFileCache == null && !isFramework && !isHwFramework) {
            dr6 = getAppDrawableFromAsset(typedValue, resources, packageName2, file2, themeType3);
        }
        return dr6;
    }

    private boolean isEmptyPreloadZip(String packageName) {
        return !emptyOrContainsPreloadedZip(packageName) && !emptyOrContainsPreloadedZip(this.mPackageName);
    }

    public AbsResourcesImpl.ThemeResource getThemeResource(int id, String packageName) {
        if (packageName == null) {
            packageName = this.mResourcesImpl.getResourcePackageName(id);
        }
        String resName = this.mResourcesImpl.getResourceName(id);
        if (packageName.equals("android")) {
            packageName = FRAMEWORK_RES;
            resName = FRAMEWORK_RES + resName.substring(7);
        } else if (packageName.equals(ANDROID_RES_EXT)) {
            packageName = FRAMEWORK_RES_EXT;
            resName = FRAMEWORK_RES_EXT + resName.substring(12);
        } else if (packageName.equals(DARK_OVERLAY_RES)) {
            packageName = FRAMEWORK_RES_EXT;
            resName = FRAMEWORK_RES_EXT + resName.substring(31);
        } else if (packageName.equals(HONOR_OVERLAY_RES)) {
            packageName = FRAMEWORK_RES_EXT;
            resName = FRAMEWORK_RES_EXT + resName.substring(32);
        } else if (packageName.equals(NOVA_OVERLAY_RES)) {
            packageName = FRAMEWORK_RES_EXT;
            resName = FRAMEWORK_RES_EXT + resName.substring(31);
        }
        return new AbsResourcesImpl.ThemeResource(packageName, resName);
    }

    public void setResourcesImpl(ResourcesImpl resourcesImpl) {
        this.mResourcesImpl = resourcesImpl;
    }

    public void initResource() {
        if (-1 == sStandardBgSize) {
            setStandardSize(sConfigAppBigIconSize == -1 ? getDimensionPixelSize(34472064) : sConfigAppBigIconSize, sConfigAppInnerIconSize == -1 ? getDimensionPixelSize(34472063) : sConfigAppInnerIconSize);
        }
        HwResources.setIsSRLocale("sr".equals(Locale.getDefault().getLanguage()));
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

    /* access modifiers changed from: protected */
    public void handleClearCache(int configChanges, boolean themeChanged) {
        if (themeChanged) {
            synchronized (sThemeColorArrayLock) {
                this.mThemeColorArray.clear();
                this.mOutThemeColorIdArray.clear();
            }
        }
        synchronized (HwResourcesImpl.class) {
            if (themeChanged) {
                try {
                    mCacheColorInfoList.clear();
                    mAssetCacheColorInfoList.clear();
                    mPreloadedThemeColorList.clear();
                    mNonThemedPackage.clear();
                    mPreloadedAssetsPackage.clear();
                    mCacheAssetsList.clear();
                    clearHwThemeZipsAndIconsCache();
                    sThemeDescripHasRead = false;
                    sIsHWThemes = true;
                } catch (Throwable th) {
                    while (true) {
                        throw th;
                    }
                }
            }
            if (this.mDynamicDrawableCache != null) {
                this.mDynamicDrawableCache.onConfigurationChange(configChanges);
            }
        }
        synchronized (sCacheLock) {
            if (this.mColorDrawableCacheEx != null) {
                this.mColorDrawableCacheEx.onConfigurationChange(configChanges);
            }
            if (this.mDrawableCacheEx != null) {
                this.mDrawableCacheEx.onConfigurationChange(configChanges);
            }
        }
        if (themeChanged) {
            clearPreloadedHwThemeZipsCache();
        }
    }

    public void setPackageName(String name) {
        if (!ANDROID_RES_EXT.equals(name) && !"android".equals(name)) {
            this.mPackageName = name;
        }
    }

    public String getPackageName() {
        return this.mPackageName;
    }

    /* access modifiers changed from: protected */
    public void setHwTheme(Configuration config) {
        if (config != null) {
            boolean z = true;
            this.mConfigHwt = config.extraConfig.getConfigItem(1);
            if (this.mResourcesImpl.getConfiguration().extraConfig.getConfigItem(1) == 0) {
                z = false;
            }
            this.mThemeChanged = z;
        }
    }

    public void updateConfiguration(Configuration config, int configChanges) {
        int userId;
        IHwConfiguration configEx = this.mResourcesImpl.getConfiguration().extraConfig;
        if (this.mConfigHwt != configEx.getConfigItem(1)) {
            this.mConfigHwt = configEx.getConfigItem(1);
            synchronized (HwResourcesImpl.class) {
                this.mThemeChanged = true;
            }
            handleClearCache(configChanges, this.mThemeChanged);
            String path = Environment.getDataDirectory() + "/themes/" + userId;
            synchronized (HwResourcesImpl.class) {
                if (!mThemeAbsoluteDir.equals(path)) {
                    setHwThemeAbsoluteDir(path, userId);
                    Log.i(TAG, "updateConfiguration! the new theme absolute directory:" + mThemeAbsoluteDir);
                }
            }
            this.mDarkThemeType = SystemProperties.get("persist.deep.theme_" + userId);
            this.mDeepThemeType = getThemeType();
        }
        if (this.mResourcesImpl.getConfiguration().locale != null) {
            HwResources.setIsSRLocale("sr".equals(this.mResourcesImpl.getConfiguration().locale.getLanguage()));
        }
    }

    public Drawable getDrawableForDynamic(Resources res, String packageName, String iconName) throws Resources.NotFoundException {
        String imgFile = "dynamic_icons/" + packageName + "/" + iconName + ".png";
        long key = (((long) imgFile.length()) << 32) | ((long) getCode(imgFile));
        Drawable dr = this.mDynamicDrawableCache.getInstance(key, res, null);
        if (dr != null) {
            return dr;
        }
        ZipFileCache iconZipFileCache = ZipFileCache.getAndCheckCachedZipFile(getThemeDir(), ICONS_ZIPFILE);
        if (iconZipFileCache == null) {
            return null;
        }
        Bitmap bmp = iconZipFileCache.getBitmapEntry(this.mResourcesImpl, imgFile);
        if (bmp != null) {
            dr = new BitmapDrawable(res, bmp);
            ((BitmapDrawable) dr).setTargetDensity(this.mResourcesImpl.getDisplayMetrics());
            this.mDynamicDrawableCache.put(key, null, dr.getConstantState(), true);
        }
        return dr;
    }

    private static synchronized void readDefaultConfig() {
        InputStream in;
        String str;
        String str2;
        synchronized (HwResourcesImpl.class) {
            if (!sDefaultConfigHasRead) {
                File inputFile = null;
                try {
                    inputFile = HwCfgFilePolicy.getCfgFile(DEFAULT_CONFIG_NAME, 0);
                } catch (NoClassDefFoundError e) {
                    Log.d(TAG, "HwCfgFilePolicy NoClassDefFoundError");
                } catch (Throwable th) {
                    if (in != null) {
                        try {
                            in.close();
                        } catch (IOException e2) {
                            Log.e(TAG, "readDefaultConfig : IOException when close stream");
                        }
                    }
                    throw th;
                }
                if (inputFile == null) {
                    sDefaultConfigHasRead = true;
                    return;
                }
                in = null;
                try {
                    InputStream in2 = new FileInputStream(inputFile);
                    XmlPullParser parser = Xml.newPullParser();
                    parser.setInput(in2, null);
                    XmlUtils.beginDocument(parser, TAG_CONFIG);
                    sDisThemeIcon = new HashSet<>();
                    sDisThemeBackground = new HashSet<>();
                    sDisThemeActivityIcon = new HashSet<>();
                    for (int type = parser.next(); type != 1; type = parser.next()) {
                        if (type == 2) {
                            String packageName = parser.getAttributeValue(0);
                            String DrawableType = null;
                            parser.next();
                            if (parser.getEventType() == 4) {
                                DrawableType = String.valueOf(parser.getText());
                            }
                            if (TYPT_ICON.equals(DrawableType)) {
                                sDisThemeIcon.add(packageName);
                            } else if (TYPT_BACKGROUND.equals(DrawableType)) {
                                sDisThemeBackground.add(packageName);
                            } else if (TYPT_ACTIVITY_ICON.equals(DrawableType)) {
                                sDisThemeActivityIcon.add(packageName);
                            }
                        }
                    }
                    try {
                        in2.close();
                    } catch (IOException e3) {
                        str = TAG;
                        str2 = "readDefaultConfig : IOException when close stream";
                    }
                } catch (FileNotFoundException e4) {
                    Log.e(TAG, "readDefaultConfig : FileNotFoundException");
                    sDisThemeIcon = null;
                    sDisThemeBackground = null;
                    sDisThemeActivityIcon = null;
                    if (in != null) {
                        try {
                            in.close();
                        } catch (IOException e5) {
                            str = TAG;
                            str2 = "readDefaultConfig : IOException when close stream";
                        }
                    }
                } catch (IOException | XmlPullParserException e6) {
                    Log.e(TAG, "readDefaultConfig : XmlPullParserException | IOException");
                    sDisThemeIcon = null;
                    sDisThemeBackground = null;
                    sDisThemeActivityIcon = null;
                    if (in != null) {
                        try {
                            in.close();
                        } catch (IOException e7) {
                            str = TAG;
                            str2 = "readDefaultConfig : IOException when close stream";
                        }
                    }
                }
                sDefaultConfigHasRead = true;
            }
            return;
        }
        Log.e(str, str2);
        sDefaultConfigHasRead = true;
    }

    private static synchronized void loadMultiDpiWhiteList() {
        synchronized (HwResourcesImpl.class) {
            sMultiDirHasRead = true;
            ArrayList<File> configFiles = new ArrayList<>();
            try {
                configFiles = HwCfgFilePolicy.getCfgFileList(FILE_MULTI_DPI_WHITELIST, 0);
            } catch (NoClassDefFoundError e) {
                Log.e(TAG, "HwCfgFilePolicy NoClassDefFoundError! ");
            }
            int size = configFiles.size();
            for (int i = 0; i < size; i++) {
                setMultiDpiPkgNameFromXml(configFiles.get(i));
            }
            setMultiDpiPkgNameFromXml(new File(DEFAULT_MULTI_DPI_WHITELIST));
        }
    }

    private static synchronized void setMultiDpiPkgNameFromXml(File configFile) {
        String str;
        String str2;
        synchronized (HwResourcesImpl.class) {
            if (configFile != null) {
                InputStream inputStream = null;
                try {
                    InputStream inputStream2 = new FileInputStream(configFile);
                    XmlPullParser xmlParser = Xml.newPullParser();
                    xmlParser.setInput(inputStream2, null);
                    for (int xmlEventType = xmlParser.next(); xmlEventType != 1; xmlEventType = xmlParser.next()) {
                        if (xmlEventType == 2) {
                            String packageName = xmlParser.getAttributeValue(null, XML_ATTRIBUTE_PACKAGE_NAME);
                            if (packageName != null) {
                                sMultiDirPkgNames.add(packageName);
                            }
                        }
                    }
                    try {
                        inputStream2.close();
                    } catch (IOException e) {
                        str = TAG;
                        str2 = "loadMultiDpiWhiteList:- IOE while closing stream";
                    }
                } catch (FileNotFoundException e2) {
                    sMultiDirHasRead = false;
                    Log.e(TAG, "loadMultiDpiWhiteList : FileNotFoundException");
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e3) {
                            str = TAG;
                            str2 = "loadMultiDpiWhiteList:- IOE while closing stream";
                        }
                    }
                } catch (XmlPullParserException e4) {
                    sMultiDirHasRead = false;
                    Log.e(TAG, "loadMultiDpiWhiteList : XmlPullParserException");
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e5) {
                            str = TAG;
                            str2 = "loadMultiDpiWhiteList:- IOE while closing stream";
                        }
                    }
                } catch (IOException e6) {
                    try {
                        sMultiDirHasRead = false;
                        Log.e(TAG, "loadMultiDpiWhiteList : IOException");
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (IOException e7) {
                                str = TAG;
                                str2 = "loadMultiDpiWhiteList:- IOE while closing stream";
                            }
                        }
                    } catch (Throwable th) {
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (IOException e8) {
                                Log.e(TAG, "loadMultiDpiWhiteList:- IOE while closing stream");
                            }
                        }
                        throw th;
                    }
                }
            } else {
                return;
            }
        }
        Log.e(str, str2);
    }

    public boolean isInMultiDpiWhiteList(String packageName) {
        if (!sMultiDirHasRead) {
            loadMultiDpiWhiteList();
        }
        Boolean result = true;
        if (sMultiDirPkgNames.size() == 0) {
            Log.i(TAG, "open lock res on whitelist, but not provide the whitelist, so lock resource");
        } else if (packageName == null) {
            Log.i(TAG, "can't get the package name, so lock resource");
        } else if (!sMultiDirPkgNames.contains(packageName)) {
            result = false;
        }
        return result.booleanValue();
    }

    private String getIconFileName(String packageName, IconCache.CacheEntry ce) {
        StringBuilder sb = new StringBuilder();
        sb.append(ce.type == 1 ? ce.name : packageName);
        sb.append(".png");
        return sb.toString();
    }

    private String getIconFileName(String packageName) {
        return packageName + ".png";
    }

    private int computeDestIconSize(Rect validRect, int maskSize, int w, int h) {
        double validSize;
        if (validRect.height() > validRect.width()) {
            validSize = (((double) maskSize) / (((double) validRect.height()) + 1.0d)) * ((double) h);
        } else {
            validSize = (((double) maskSize) / (((double) validRect.width()) + 1.0d)) * ((double) w);
        }
        int iconSize = (int) validSize;
        if (1 == iconSize % 2) {
            return (int) (0.5d + validSize);
        }
        return iconSize;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:41:0x00c4, code lost:
        return r12;
     */
    public Drawable getJoinBitmap(Drawable srcDraw, int backgroundId) {
        Drawable drawable = srcDraw;
        int i = backgroundId;
        synchronized (HwResourcesImpl.class) {
            if (drawable == null) {
                try {
                    Log.w(TAG, "notification icon is null!");
                    return null;
                } catch (Throwable th) {
                    throw th;
                }
            } else {
                Bitmap bmpSrc = null;
                if (drawable instanceof BitmapDrawable) {
                    Bitmap temp = ((BitmapDrawable) drawable).getBitmap();
                    int width = temp.getWidth();
                    int height = temp.getHeight();
                    int[] pixels = new int[(width * height)];
                    temp.getPixels(pixels, 0, width, 0, 0, width, height);
                    bmpSrc = Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888);
                }
                if (bmpSrc == null) {
                    Log.w(TAG, "getJoinBitmap : bmpSrc is null!");
                    return null;
                }
                ZipFileCache iconZipFileCache = ZipFileCache.getAndCheckCachedZipFile(getThemeDir(), ICONS_ZIPFILE);
                if (iconZipFileCache == null) {
                    Log.w(TAG, "getJoinBitmap : iconZipFileCache == null");
                    return null;
                }
                ResourcesImpl resourcesImpl = this.mResourcesImpl;
                Bitmap bmpBg = iconZipFileCache.getBitmapEntry(resourcesImpl, NOTIFICATION_ICON_PREFIX + i + ".png");
                if (bmpBg == null) {
                    bmpBg = iconZipFileCache.getBitmapEntry(this.mResourcesImpl, NOTIFICATION_ICON_EXIST);
                }
                Bitmap bmpMask = iconZipFileCache.getBitmapEntry(this.mResourcesImpl, NOTIFICATION_ICON_MASK);
                Bitmap bmpBorder = iconZipFileCache.getBitmapEntry(this.mResourcesImpl, NOTIFICATION_ICON_BORDER);
                if (!(bmpBg == null || bmpMask == null)) {
                    if (bmpBorder != null) {
                        int w = bmpSrc.getWidth();
                        int h = bmpSrc.getHeight();
                        if (w == 1 && h == 1) {
                            Bitmap overlap = IconBitmapUtils.overlap2Bitmap(bmpSrc, bmpBg);
                            BitmapDrawable bitmapDrawable = overlap == null ? null : new BitmapDrawable(overlap);
                        } else {
                            if (!(bmpMask.getWidth() == w || bmpMask.getHeight() == h)) {
                                bmpMask = IconBitmapUtils.drawSource(bmpMask, w, w);
                            }
                            if (!(bmpBorder.getWidth() == w || bmpBorder.getHeight() == h)) {
                                bmpBorder = IconBitmapUtils.drawSource(bmpBorder, w, w);
                            }
                            if (!(bmpBg.getWidth() == w || bmpBg.getHeight() == h)) {
                                bmpBg = IconBitmapUtils.drawSource(bmpBg, w, w);
                            }
                            Bitmap result = IconBitmapUtils.composeIcon(bmpSrc, bmpMask, bmpBg, bmpBorder, false);
                            if (result != null) {
                                BitmapDrawable bitmapDrawable2 = new BitmapDrawable(result);
                                return bitmapDrawable2;
                            }
                            Log.w(TAG, "getJoinBitmap is null!");
                            return null;
                        }
                    }
                }
                StringBuilder sb = new StringBuilder();
                sb.append("getJoinBitmap :");
                sb.append(bmpBg == null ? " bmpBg == null, " : " bmpBg != null, ");
                sb.append(" id = ");
                sb.append(i);
                sb.append(bmpMask == null ? " bmpMask == null, " : "bmpMask != null, ");
                sb.append(bmpBorder == null ? " bmpBorder == null, " : " bmpBorder != null");
                Log.w(TAG, sb.toString());
                return null;
            }
        }
    }

    public String getThemeDir() {
        String str;
        synchronized (HwResourcesImpl.class) {
            str = mThemeAbsoluteDir;
        }
        return str;
    }

    public static boolean emptyOrContainsPreloadedZip(String zipFile) {
        boolean z;
        synchronized (sPreloadedHwThemeZipLock) {
            if (!sPreloadedHwThemeZips.isEmpty()) {
                if (!sPreloadedHwThemeZips.contains(zipFile)) {
                    z = false;
                }
            }
            z = true;
        }
        return z;
    }

    public String getDiffThemeIconPath() {
        File iconZip;
        if (!TextUtils.isEmpty(CUSTOM_DIFF_THEME_DIR)) {
            if (new File(CUSTOM_DIFF_THEME_DIR + "/" + ICONS_ZIPFILE).exists()) {
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

    public Bitmap getIconsfromDiffTheme(ResourcesImpl Impl, String imgFile, String packageName) {
        String iconFilePath = getDiffThemeIconPath();
        if (iconFilePath == null) {
            return null;
        }
        ZipFileCache diffIconZipFileCache = ZipFileCache.getAndCheckCachedZipFile(iconFilePath, ICONS_ZIPFILE);
        if (diffIconZipFileCache == null) {
            return null;
        }
        Bitmap bmp = diffIconZipFileCache.getBitmapEntry(Impl, imgFile);
        if (bmp != null) {
            Log.i(TAG, "icon : " + imgFile + " found in custom diff theme");
            return bmp;
        } else if (imgFile.equals(getIconFileName(packageName))) {
            return bmp;
        } else {
            Bitmap bmp2 = diffIconZipFileCache.getBitmapEntry(Impl, getIconFileName(packageName));
            Log.i(TAG, "icon : " + imgFile + " package name found in custom diff theme");
            return bmp2;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:13:0x004b, code lost:
        android.content.res.HwResources.setIsSRLocale("sr".equals(java.util.Locale.getDefault().getLanguage()));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x005c, code lost:
        return;
     */
    public void preloadHwThemeZipsAndSomeIcons(int currentUserId) {
        setHwThemeAbsoluteDir(Environment.getDataDirectory() + "/themes/" + currentUserId, currentUserId);
        synchronized (sPreloadedHwThemeZipLock) {
            File themePath = new File(mThemeAbsoluteDir);
            if (themePath.exists()) {
                File[] files = themePath.listFiles(new HwThemeFileFilter());
                if (files != null) {
                    for (File f : files) {
                        sPreloadedHwThemeZips.add(f.getName());
                    }
                }
            }
        }
    }

    private void setHwThemeAbsoluteDir(String path, int userId) {
        synchronized (HwResourcesImpl.class) {
            mThemeAbsoluteDir = path;
            mCurrentUserId = userId;
        }
    }

    public void clearHwThemeZipsAndSomeIcons() {
        synchronized (HwResourcesImpl.class) {
            clearHwThemeZipsAndIconsCache();
        }
        clearPreloadedHwThemeZipsCache();
    }

    private void clearHwThemeZipsAndIconsCache() {
        if (sBgList != null) {
            int listSize = sBgList.size();
            for (int i = 0; i < listSize; i++) {
                sBgList.get(i).recycle();
            }
            sBgList.clear();
        }
        if (sBmpBorder != null && !sBmpBorder.isRecycled()) {
            sBmpBorder.recycle();
            sBmpBorder = null;
        }
        if (sBmpMask != null && !sBmpMask.isRecycled()) {
            sBmpMask.recycle();
            Log.w(TAG, "sBmpMask  recycle  sBmpMask = " + sBmpMask);
            sBmpMask = null;
            sMaskSizeWithoutEdge = -1;
        }
        ZipFileCache.clear();
        sUseAvgColor = -1;
    }

    private void clearPreloadedHwThemeZipsCache() {
        synchronized (sPreloadedHwThemeZipLock) {
            sPreloadedHwThemeZips.clear();
        }
    }

    private void inflateColorInfoList(XmlPullParser parser, String packageName, boolean isZipFile) throws XmlPullParserException, IOException {
        int innerDepth = parser.getDepth();
        while (true) {
            int next = parser.next();
            int type = next;
            if (next == 1) {
                return;
            }
            if (parser.getDepth() <= innerDepth && type == 3) {
                return;
            }
            if (type == 2) {
                String lableName = parser.getName();
                if (lableName.equals("color") || lableName.equals("drawable")) {
                    String name = parser.getAttributeName(0);
                    String colorName = parser.getAttributeValue(0);
                    String text = null;
                    if (parser.next() == 4) {
                        text = parser.getText();
                    }
                    if (XML_ATTRIBUTE_PACKAGE_NAME.equalsIgnoreCase(name)) {
                        int colorVaue = getColorValueFromStr(text);
                        String fullColorName = packageName + ":" + lableName + "/" + colorName;
                        if (isZipFile) {
                            mCacheColorInfoList.put(fullColorName, Integer.valueOf(colorVaue));
                        } else {
                            mAssetCacheColorInfoList.put(fullColorName, Integer.valueOf(colorVaue));
                        }
                    }
                }
            }
        }
    }

    private int getColorValueFromStr(String value) throws IOException {
        if (value == null) {
            return 0;
        }
        if (!value.startsWith("#")) {
            Log.e(TAG, "getColorValueFromStr " + String.format("Color value '%s' must start with #", new Object[]{value}));
            return 0;
        }
        String value2 = value.substring(1);
        if (value2.length() > 8) {
            Log.e(TAG, "getColorValueFromStr " + String.format("Color value '%s' is too long. Format is either#AARRGGBB, #RRGGBB, #RGB, or #ARGB", new Object[]{value2}));
            return 0;
        }
        if (value2.length() == 3) {
            char[] color = new char[8];
            color[1] = 'F';
            color[0] = 'F';
            char charAt = value2.charAt(0);
            color[3] = charAt;
            color[2] = charAt;
            char charAt2 = value2.charAt(1);
            color[5] = charAt2;
            color[4] = charAt2;
            char charAt3 = value2.charAt(2);
            color[7] = charAt3;
            color[6] = charAt3;
            value2 = new String(color);
        } else if (value2.length() == 4) {
            char[] color2 = new char[8];
            char charAt4 = value2.charAt(0);
            color2[1] = charAt4;
            color2[0] = charAt4;
            char charAt5 = value2.charAt(1);
            color2[3] = charAt5;
            color2[2] = charAt5;
            char charAt6 = value2.charAt(2);
            color2[5] = charAt6;
            color2[4] = charAt6;
            char charAt7 = value2.charAt(3);
            color2[7] = charAt7;
            color2[6] = charAt7;
            value2 = new String(color2);
        } else if (value2.length() == 6) {
            value2 = "FF" + value2;
        }
        return (int) Long.parseLong(value2, 16);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x001e, code lost:
        r0 = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new java.io.File(r12, r13));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0021, code lost:
        android.util.Log.e(TAG, "getLayoutXML Exception filename =" + r13);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0039, code lost:
        android.util.Log.e(TAG, "getLayoutXML IOException filename = " + r13);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0051, code lost:
        android.util.Log.e(TAG, "getLayoutXML SAXException filename =" + r13);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0069, code lost:
        android.util.Log.e(TAG, "getLayoutXML ParserConfigurationException filename =" + r13);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x000c, code lost:
        r0 = null;
     */
    private boolean isCurrentHwTheme(String dirpath, String filename) {
        Document document;
        synchronized (HwResourcesImpl.class) {
            if (sThemeDescripHasRead) {
                boolean z = sIsHWThemes;
                return z;
            }
        }
        Document document2 = document;
        if (document2 == null) {
            return sIsHWThemes;
        }
        Element rootElement = document2.getDocumentElement();
        if (rootElement == null) {
            return sIsHWThemes;
        }
        NodeList itemNodes = rootElement.getChildNodes();
        int itemNodeLength = itemNodes.getLength();
        boolean z2 = false;
        int i = 0;
        while (i < itemNodeLength) {
            Node itemNode = itemNodes.item(i);
            if (itemNode.getNodeType() != 1 || !THEMEDISIGNER.equals(itemNode.getNodeName())) {
                i++;
            } else {
                String textContent = itemNode.getTextContent();
                if (EMUI.equals(textContent) || HONOR.equalsIgnoreCase(textContent)) {
                    z2 = true;
                }
                sIsHWThemes = z2;
                synchronized (HwResourcesImpl.class) {
                    sThemeDescripHasRead = true;
                }
                return sIsHWThemes;
            }
        }
        synchronized (HwResourcesImpl.class) {
            sThemeDescripHasRead = true;
        }
        return sIsHWThemes;
    }

    public void initDeepTheme() {
        if (UserHandle.getAppId(Process.myUid()) < 1000) {
            Log.w(TAG, "zygote not need to initDeepTheme");
            return;
        }
        this.mCurrentDeepTheme = this.mResourcesImpl.getAssets().getDeepType();
        this.mDarkThemeType = SystemProperties.get("persist.deep.theme_" + mCurrentUserId);
        if (isDeepDarkTheme() || HwThemeManager.isHonorProduct() || HwThemeManager.isNovaProduct()) {
            this.mDeepThemeType = getThemeType();
        }
    }

    private boolean checkWhiteDeepThemeAppList(String resDir) {
        int length = DEEPTHEME_WHILTLIST_ADD.length;
        for (int i = 0; i < length; i++) {
            if (resDir != null && resDir.equals(DEEPTHEME_WHILTLIST_ADD[i])) {
                return true;
            }
        }
        return false;
    }

    @SuppressLint({"AvoidInHardConnectInString"})
    private Drawable getAppDrawableFromAsset(TypedValue value, Resources res, String packageName, String file, String dir) {
        String drawablePath = this.mPackageName + ":" + dir + "/" + file;
        if (DEFAULT_NO_DRAWABLE_FLAG.equals(mCacheAssetsList.get(drawablePath))) {
            return null;
        }
        Drawable dr = AssetsFileCache.getDrawableEntry(this.mResourcesImpl.getAssets(), res, value, dir + File.separator + file, null);
        if (dr == null && !file.contains(DRAWABLE_FHD)) {
            dr = AssetsFileCache.getDrawableEntry(this.mResourcesImpl.getAssets(), res, value, dir + File.separator + getDefaultResDir() + File.separator + file.substring(file.lastIndexOf(File.separator) + 1), getDefaultOptions());
        }
        if (dr == null) {
            mCacheAssetsList.put(drawablePath, DEFAULT_NO_DRAWABLE_FLAG);
        }
        return dr;
    }

    @SuppressLint({"AvoidInHardConnectInString"})
    private Drawable getFwkDrawableFromAsset(TypedValue value, Resources res, String packageName, String file, String key, String dir) {
        String fullPath = this.mPackageName + ":" + dir + "/" + key;
        if (DEFAULT_NO_DRAWABLE_FLAG.equals(mCacheAssetsList.get(fullPath))) {
            return null;
        }
        Drawable dr = AssetsFileCache.getDrawableEntry(this.mResourcesImpl.getAssets(), res, value, dir + File.separator + key, null);
        if (dr == null && !file.contains(DRAWABLE_FHD)) {
            dr = AssetsFileCache.getDrawableEntry(this.mResourcesImpl.getAssets(), res, value, dir + File.separator + packageName + File.separator + getDefaultResDir() + File.separator + file.substring(file.lastIndexOf(File.separator) + 1), getDefaultOptions());
        }
        if (dr == null) {
            mCacheAssetsList.put(fullPath, DEFAULT_NO_DRAWABLE_FLAG);
        }
        return dr;
    }

    private BitmapFactory.Options getDefaultOptions() {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inDensity = 480;
        return opts;
    }

    private String getDefaultResDir() {
        return DEFAULT_RES_DIR;
    }

    private String getThemeType() {
        if (isDeepDarkTheme() && (this.mCurrentDeepTheme & 1) == 1) {
            return DARK;
        }
        if (HwThemeManager.isHonorProduct() && (this.mCurrentDeepTheme & 16) == 16) {
            return HONOR;
        }
        if (!HwThemeManager.isNovaProduct() || (this.mCurrentDeepTheme & 256) != 256) {
            return null;
        }
        return NOVA;
    }

    /* access modifiers changed from: protected */
    public String getDeepThemeType() {
        return this.mDeepThemeType;
    }

    private boolean isDeepDarkTheme() {
        return DARK.equals(this.mDarkThemeType);
    }

    private void loadThemeColorFromAssets(String packageName, String themeXml, String dir) {
        if ((!packageName.equals(FRAMEWORK_RES) && !packageName.equals(FRAMEWORK_RES_EXT)) || themeXml.contains(packageName)) {
            parserColorInfoList(packageName, AssetsFileCache.getInputStreamEntry(this.mResourcesImpl.getAssets(), dir + File.separator + themeXml), false);
        }
    }

    /* access modifiers changed from: protected */
    public Bundle getMultidpiInfo(String packageName) {
        return Resources.getPreMultidpiInfo(packageName);
    }

    public DisplayMetrics hwGetDisplayMetrics() {
        return this.mResourcesImpl.getDisplayMetrics();
    }
}
