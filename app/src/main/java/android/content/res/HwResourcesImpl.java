package android.content.res;

import android.app.ActivityThread;
import android.content.pm.ActivityInfoExInner;
import android.content.res.AbsResourcesImpl.ThemeColor;
import android.content.res.AbsResourcesImpl.ThemeResource;
import android.content.res.Resources.NotFoundException;
import android.content.res.Resources.Theme;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory.Options;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Drawable.ConstantState;
import android.hwtheme.HwThemeManager;
import android.os.Environment;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Log;
import android.util.LongSparseArray;
import android.util.TypedValue;
import android.util.Xml;
import com.android.internal.util.XmlUtils;
import com.huawei.hsm.permission.StubController;
import huawei.android.hwutil.IconBitmapUtils;
import huawei.android.hwutil.IconCache;
import huawei.android.hwutil.IconCache.CacheEntry;
import huawei.android.hwutil.ZipFileCache;
import huawei.cust.HwCfgFilePolicy;
import huawei.cust.HwCustUtils;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Random;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class HwResourcesImpl extends AbsResourcesImpl {
    private static final String ANDROID_RES = "android";
    private static final String ANDROID_RES_EXT = "androidhwext";
    private static final String[] APP_WHILTLIST_ADD = null;
    private static final String CHANGEPACKAGE_CONFIG_NAME = "xml/hw_change_package.xml";
    private static final String CUSTOM_DIFF_THEME_DIR = null;
    private static final boolean DEBUG = false;
    private static final boolean DEBUG_DRAWABLE = false;
    private static final boolean DEBUG_ICON = false;
    private static final boolean DEBUG_VERBOSE_ICON = false;
    private static final String DEFAULT_CONFIG_NAME = "xml/hw_launcher_load_icon.xml";
    private static final String DEFAULT_CONFIG_PATH = "/data/cust/xml/hw_launcher_load_icon.xml";
    private static final int DEFAULT_EDGE_SIZE = 8;
    private static final String DEFAULT_MULTI_DPI_WHITELIST = "/system/etc/xml/multidpi_whitelist.xml";
    private static final int DELTA_X_OF_BACKGROUND = 0;
    private static final int DELTA_Y_OF_BACKGROUND = 0;
    private static final String DIFF_THEME_ICON = "/themes/diff/icons";
    private static final String DPI_2K = "xxxhdpi";
    private static final String DPI_FHD = "xxhdpi";
    private static final String DRAWABLE_FHD = "drawable-xxhdpi";
    private static final String DYNAMIC_ICONS = "dynamic_icons";
    private static final String EMUI = "EMUI";
    private static final String FILE_DESCRIPTION = "description.xml";
    private static final String FILE_MULTI_DPI_WHITELIST = "xml/multidpi_whitelist.xml";
    private static final String FRAMEWORK_RES = "framework-res";
    private static final String FRAMEWORK_RES_EXT = "framework-res-hwext";
    private static final String GOOGLE_KEYWORD = "com.google";
    private static final String ICONS_ZIPFILE = "icons";
    private static final String ICON_BACKGROUND_PREFIX = "icon_background";
    private static final String ICON_BORDER_FILE = "icon_border.png";
    private static final String ICON_BORDER_UNFLAT_FILE = "icon_border_unflat.png";
    private static final String ICON_MASK_ALL_FILE = "icon_mask_all.png";
    private static final String ICON_MASK_FILE = "icon_mask.png";
    private static final int ICON_NOTUSE_AVGCOLOR = 1;
    private static final String ICON_SYS_APP_DEFAULT = "sym_def_app_icon";
    private static final int ICON_USE_AVGCOLOR = 0;
    private static final int LEN_OF_ANDROID = 7;
    private static final int LEN_OF_ANDROID_EXT = 12;
    private static final int MASK_ABS_VALID_RANGE = 10;
    private static final String NOTIFICATION_ICON_BORDER = "ic_stat_notify_icon_border.png";
    private static final String NOTIFICATION_ICON_EXIST = "ic_stat_notify_bg_0.png";
    private static final String NOTIFICATION_ICON_MASK = "ic_stat_notify_icon_mask.png";
    private static final String NOTIFICATION_ICON_PREFIX = "ic_stat_notify_bg_";
    static final String TAG = "HwResourcesImpl";
    private static final String TAG_CONFIG = "launcher_config";
    private static final String THEMEDISIGNER = "designer";
    static final String THEME_ANIMATE_FOLDER_ICON_NAME = "portal_ring_inner_holo.png.animate";
    static final String THEME_FOLDER_ICON_NAME = "portal_ring_inner_holo.png";
    private static final String TYPT_BACKGROUND = "background";
    private static final String TYPT_ICON = "icon";
    private static final String USE_AVGCOLOR_CONFIG_FILE = "config.xml";
    private static final String XML_ATTRIBUTE_PACKAGE_NAME = "name";
    private static boolean bCfgFileNotExist;
    private static final boolean isIconSupportCut = false;
    private static HashMap<String, Integer> mCacheColorInfoList;
    private static HwCustHwResources mCustHwResources;
    private static HashSet<String> mNonThemedPackage;
    private static HashSet<String> mPreloadedThemeColorList;
    private static String mThemeAbsoluteDir;
    private static HashMap<String, String> packageNameMap;
    private static ResourcesUtils resUtils;
    private static ArrayList<Bitmap> sBgList;
    private static Bitmap sBmpBorder;
    private static Bitmap sBmpMask;
    private static Bitmap sBmpSysAppDefault;
    private static final int sConfigAppBigIconSize = 0;
    private static final int sConfigAppInnerIconSize = 0;
    private static boolean sDefaultConfigHasRead;
    private static int sDefaultSizeWithoutEdge;
    private static HashSet<String> sDisThemeBackground;
    private static HashSet<String> sDisThemeIcon;
    private static boolean sIsHWThemes;
    private static int sMaskSizeWithoutEdge;
    private static boolean sMultiDirHasRead;
    private static HashSet<String> sMultiDirPkgNames;
    private static LongSparseArray<ConstantState> sPreloadedColorDrawablesEx;
    private static LongSparseArray<ConstantState> sPreloadedDrawablesEx;
    private static final HashSet<String> sPreloadedHwThemeZips = null;
    private static boolean sSerbiaLocale;
    private static int sStandardBgSize;
    private static int sStandardIconSize;
    private static boolean sThemeDescripHasRead;
    private static int sUseAvgColor;
    private final DrawableCache mColorDrawableCacheEx;
    protected int mConfigHwt;
    private boolean mContainPackage;
    private final DrawableCache mDrawableCacheEx;
    private final DrawableCache mDynamicDrawableCache;
    protected String mPackageName;
    private ResourcesImpl mResourcesImpl;
    protected boolean mThemeChanged;
    private TypedValue mTmpValue;
    private final Object mTmpValueLock;
    private ArrayList<String> recPackageList;
    private boolean system;

    static class HwThemeFileFilter implements FileFilter {
        HwThemeFileFilter() {
        }

        public boolean accept(File pathname) {
            if (!pathname.isFile() || pathname.getName().toLowerCase(Locale.getDefault()).endsWith(".xml")) {
                return HwResourcesImpl.DEBUG_VERBOSE_ICON;
            }
            return true;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.content.res.HwResourcesImpl.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.content.res.HwResourcesImpl.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.content.res.HwResourcesImpl.<clinit>():void");
    }

    public HwResourcesImpl() {
        this.mDrawableCacheEx = new DrawableCache();
        this.mDynamicDrawableCache = new DrawableCache();
        this.mColorDrawableCacheEx = new DrawableCache();
        this.recPackageList = new ArrayList();
        this.mContainPackage = DEBUG_VERBOSE_ICON;
        this.system = DEBUG_VERBOSE_ICON;
        this.mTmpValueLock = new Object();
        this.mTmpValue = new TypedValue();
        this.mConfigHwt = ICON_USE_AVGCOLOR;
        this.mThemeChanged = DEBUG_VERBOSE_ICON;
        this.mPackageName = null;
    }

    private void getAllBgImage(String path, String zip) {
        if (sBgList == null || sBgList.isEmpty()) {
            ZipFileCache iconZipFileCache = ZipFileCache.getAndCheckCachedZipFile(getThemeDir(), ICONS_ZIPFILE);
            if (iconZipFileCache != null) {
                sBgList = iconZipFileCache.getBitmapList(this.mResourcesImpl, ICON_BACKGROUND_PREFIX);
                if (sBgList != null) {
                    for (int i = ICON_USE_AVGCOLOR; i < sBgList.size(); i += ICON_NOTUSE_AVGCOLOR) {
                        Bitmap bmp = (Bitmap) sBgList.get(i);
                        if (bmp != null) {
                            sBgList.set(i, IconBitmapUtils.zoomIfNeed(bmp, sStandardBgSize, true));
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
        int len = ICON_USE_AVGCOLOR;
        if (sBgList != null) {
            len = sBgList.size();
        }
        if (len <= 0) {
            return null;
        }
        return (Bitmap) sBgList.get(getCode(idAndPackageName) % len);
    }

    public int getCode(String idAndPackageName) {
        int code = ICON_USE_AVGCOLOR;
        for (int i = ICON_USE_AVGCOLOR; i < idAndPackageName.length(); i += ICON_NOTUSE_AVGCOLOR) {
            code += idAndPackageName.charAt(i);
        }
        return code;
    }

    private void initMaskSizeWithoutEdge(boolean useDefault) {
        if (-1 != sMaskSizeWithoutEdge) {
            return;
        }
        if (sBmpMask != null) {
            Rect info = IconBitmapUtils.getIconInfo(sBmpMask);
            if (info != null) {
                sMaskSizeWithoutEdge = info.width() + ICON_NOTUSE_AVGCOLOR;
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
            if (sBmpMask != null) {
                sBmpMask = IconBitmapUtils.zoomIfNeed(sBmpMask, sStandardBgSize, true);
            } else {
                sBmpMask = iconZipFileCache.getBitmapEntry(this.mResourcesImpl, ICON_MASK_ALL_FILE);
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

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static void initUseAvgColor(ZipFileCache iconZipFileCache) {
        if (iconZipFileCache != null && sUseAvgColor == -1) {
            InputStream is = iconZipFileCache.getInputStreamEntry(USE_AVGCOLOR_CONFIG_FILE);
            if (is != null) {
                try {
                    XmlPullParser parser = Xml.newPullParser();
                    parser.setInput(is, null);
                    inflateConfigAttribute(parser);
                    try {
                        is.close();
                    } catch (IOException e) {
                        Log.e(TAG, "initUseAvgColor : IOException when close stream");
                    }
                } catch (XmlPullParserException e2) {
                    Log.e(TAG, "initUseAvgColor : XmlPullParserException");
                } catch (IOException e3) {
                    Log.e(TAG, "initUseAvgColor : IOException");
                    try {
                        is.close();
                    } catch (IOException e4) {
                        Log.e(TAG, "initUseAvgColor : IOException when close stream");
                    }
                } catch (Throwable th) {
                    try {
                        is.close();
                    } catch (IOException e5) {
                        Log.e(TAG, "initUseAvgColor : IOException when close stream");
                    }
                }
            } else {
                sUseAvgColor = ICON_USE_AVGCOLOR;
            }
        }
    }

    public static synchronized HwCustHwResources getHwCustHwResources() {
        HwCustHwResources hwCustHwResources;
        synchronized (HwResourcesImpl.class) {
            if (mCustHwResources == null) {
                mCustHwResources = (HwCustHwResources) HwCustUtils.createObj(HwCustHwResources.class, new Object[ICON_USE_AVGCOLOR]);
            }
            hwCustHwResources = mCustHwResources;
        }
        return hwCustHwResources;
    }

    private static void inflateConfigAttribute(XmlPullParser parser) throws XmlPullParserException, IOException {
        int innerDepth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if (type == ICON_NOTUSE_AVGCOLOR) {
                return;
            }
            if (parser.getDepth() <= innerDepth && type == 3) {
                return;
            }
            if (type == 2 && parser.getName().equals("useavgcolor") && parser.next() == 4) {
                String value = parser.getText();
                if (value != null) {
                    sUseAvgColor = value.toLowerCase().trim().equals("true") ? ICON_USE_AVGCOLOR : ICON_NOTUSE_AVGCOLOR;
                }
            }
        }
    }

    private void initSysDefault(ZipFileCache iconZipFileCache) {
        if (sBmpSysAppDefault == null) {
            ZipFileCache zip = iconZipFileCache;
            if (iconZipFileCache == null) {
                zip = ZipFileCache.getAndCheckCachedZipFile(getThemeDir(), ICONS_ZIPFILE);
                if (zip != null) {
                    sBmpSysAppDefault = zip.getBitmapEntry(this.mResourcesImpl, "sym_def_app_icon.png");
                    if (sBmpSysAppDefault != null) {
                        sBmpSysAppDefault = IconBitmapUtils.zoomIfNeed(sBmpSysAppDefault, sStandardBgSize, true);
                    }
                }
            }
        }
    }

    private Drawable checkSpecialIcons(Resources res, String entryName) {
        Throwable th;
        synchronized (HwResourcesImpl.class) {
            try {
                if (entryName.indexOf(ICON_SYS_APP_DEFAULT) >= 0) {
                    initSysDefault(null);
                    if (sBmpSysAppDefault != null) {
                        Drawable dr = new BitmapDrawable(res, sBmpSysAppDefault);
                        if (dr != null) {
                            try {
                                ((BitmapDrawable) dr).setTargetDensity(this.mResourcesImpl.getDisplayMetrics());
                            } catch (Throwable th2) {
                                th = th2;
                                throw th;
                            }
                        }
                        return dr;
                    }
                }
                return null;
            } catch (Throwable th3) {
                th = th3;
                throw th;
            }
        }
    }

    public Bitmap getThemeIconByName(String name) {
        String imgFile = name;
        if (!name.endsWith(".png")) {
            imgFile = name + ".png";
        }
        if (THEME_ANIMATE_FOLDER_ICON_NAME.equalsIgnoreCase(imgFile)) {
            imgFile = THEME_FOLDER_ICON_NAME;
        }
        Bitmap bitmap = null;
        synchronized (HwResourcesImpl.class) {
            ZipFileCache iconZipFileCache = ZipFileCache.getAndCheckCachedZipFile(getThemeDir(), ICONS_ZIPFILE);
            if (iconZipFileCache != null) {
                bitmap = iconZipFileCache.getBitmapEntry(this.mResourcesImpl, imgFile);
                if (!(this.mContainPackage || bitmap == null)) {
                    Log.i(TAG, "icon : " + name + " found in theme");
                }
            }
            if (bitmap != null) {
                Bitmap srcBitmap = IconBitmapUtils.zoomIfNeed(bitmap, sStandardBgSize, true);
                return srcBitmap;
            }
            return bitmap;
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
            Bitmap bitmap = null;
            if (sBgList == null || sBgList.isEmpty()) {
                getAllBgImage(getThemeDir(), ICONS_ZIPFILE);
            }
            if (sBgList != null) {
                int len = sBgList.size();
                if (len > 0) {
                    bitmap = (Bitmap) sBgList.get(new Random().nextInt(len));
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
            Bitmap composeIcon = IconBitmapUtils.composeIcon(IconBitmapUtils.drawSource(bmpSrc, sStandardBgSize, iconSize), sBmpMask, bitmap, sBmpBorder, DEBUG_VERBOSE_ICON);
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
            if (w == ICON_NOTUSE_AVGCOLOR && h == ICON_NOTUSE_AVGCOLOR) {
                Bitmap overlap2Bitmap = IconBitmapUtils.overlap2Bitmap(bmpSrc, bmpBg);
                return overlap2Bitmap;
            }
            Bitmap bmpSrcStd = IconBitmapUtils.drawSource(bmpSrc, sStandardBgSize, iconSize);
            if (composeIcon) {
                overlap2Bitmap = IconBitmapUtils.composeIcon(bmpSrcStd, sBmpMask, bmpBg, sBmpBorder, DEBUG_VERBOSE_ICON);
                return overlap2Bitmap;
            }
            return bmpSrcStd;
        }
    }

    protected Drawable handleAddIconBackground(Resources res, int id, Drawable dr) {
        if (HwThemeManager.isHwTheme() && id != 0) {
            String str;
            String resourcesPackageName = this.mResourcesImpl.getResourcePackageName(id);
            StringBuilder append = new StringBuilder().append(id).append("#");
            if (this.mPackageName != null) {
                str = this.mPackageName;
            } else {
                str = resourcesPackageName;
            }
            String idAndPackageName = append.append(str).toString();
            if (IconCache.contains(idAndPackageName)) {
                String packageName = this.mPackageName != null ? this.mPackageName : resourcesPackageName;
                Bitmap bmp = IconBitmapUtils.drawableToBitmap(dr);
                if (bmp == null || checkWhiteListApp(packageName)) {
                    return dr;
                }
                if (bmp.getWidth() > sDefaultSizeWithoutEdge * 3 || bmp.getHeight() > sDefaultSizeWithoutEdge * 3) {
                    Log.w(TAG, "icon in pkg " + packageName + " is too large." + "sDefaultSizeWithoutEdge = " + sDefaultSizeWithoutEdge + "bmp.getWidth() = " + bmp.getWidth());
                    bmp = IconBitmapUtils.zoomIfNeed(bmp, sDefaultSizeWithoutEdge, true);
                    dr = new BitmapDrawable(res, bmp);
                    if (dr != null) {
                        ((BitmapDrawable) dr).setTargetDensity(this.mResourcesImpl.getDisplayMetrics());
                    }
                }
                ZipFileCache iconZipFileCache = ZipFileCache.getAndCheckCachedZipFile(getThemeDir(), ICONS_ZIPFILE);
                if (iconZipFileCache == null) {
                    return dr;
                }
                initMask(iconZipFileCache);
                Bitmap resultBitmap = null;
                if (sBmpMask != null) {
                    if (isCurrentHwTheme(getThemeDir(), FILE_DESCRIPTION) && !isIconSupportCut) {
                        resultBitmap = addIconBackgroud(bmp, idAndPackageName, DEBUG_VERBOSE_ICON);
                    } else if ((getHwCustHwResources() == null || getHwCustHwResources().isUseThemeIconAndBackground()) && (sDisThemeBackground == null || !sDisThemeBackground.contains(packageName))) {
                        resultBitmap = addIconBackgroud(bmp, idAndPackageName, true);
                    }
                }
                if (resultBitmap == null) {
                    resultBitmap = IconBitmapUtils.zoomIfNeed(bmp, sStandardBgSize, DEBUG_VERBOSE_ICON);
                }
                if (resultBitmap != null) {
                    dr = new BitmapDrawable(res, resultBitmap);
                    if (dr != null) {
                        ((BitmapDrawable) dr).setTargetDensity(this.mResourcesImpl.getDisplayMetrics());
                    }
                    return dr;
                }
            }
        }
        return dr;
    }

    private boolean checkWhiteListApp(String packageName) {
        if (sDisThemeIcon != null && sDisThemeIcon.contains(packageName) && isCurrentHwTheme(getThemeDir(), FILE_DESCRIPTION)) {
            return true;
        }
        if (packageName != null && packageName.contains(GOOGLE_KEYWORD) && isCurrentHwTheme(getThemeDir(), FILE_DESCRIPTION)) {
            return true;
        }
        int i = ICON_USE_AVGCOLOR;
        while (i < APP_WHILTLIST_ADD.length) {
            if (packageName != null && packageName.equals(APP_WHILTLIST_ADD[i]) && isCurrentHwTheme(getThemeDir(), FILE_DESCRIPTION)) {
                return true;
            }
            i += ICON_NOTUSE_AVGCOLOR;
        }
        return DEBUG_VERBOSE_ICON;
    }

    private Drawable getThemeIcon(Resources resources, int id, String packageName) {
        Throwable th;
        if (checkWhiteListApp(packageName)) {
            return null;
        }
        Drawable drawable = null;
        CacheEntry ce = IconCache.get(id + "#" + packageName);
        if (packageNameMap.get(packageName) != null) {
            packageName = (String) packageNameMap.get(packageName);
        }
        String imgFile = getIconFileName(packageName, ce);
        synchronized (HwResourcesImpl.class) {
            try {
                Bitmap bmp = getIconsfromDiffTheme(this.mResourcesImpl, imgFile);
                ZipFileCache iconZipFileCache = ZipFileCache.getAndCheckCachedZipFile(getThemeDir(), ICONS_ZIPFILE);
                if (bmp == null && iconZipFileCache != null) {
                    bmp = iconZipFileCache.getBitmapEntry(this.mResourcesImpl, imgFile);
                    if (!(this.mContainPackage || bmp == null)) {
                        String str = TAG;
                        StringBuilder append = new StringBuilder().append("icon : ");
                        if (packageName == null) {
                            packageName = "null";
                        }
                        Log.i(str, append.append(packageName).append(" found in theme").toString());
                    }
                }
                if (bmp != null) {
                    Bitmap srcBitmap = IconBitmapUtils.zoomIfNeed(bmp, sStandardBgSize, true);
                    if (srcBitmap != null) {
                        Drawable dr = new BitmapDrawable(resources, srcBitmap);
                        if (dr != null) {
                            try {
                                ((BitmapDrawable) dr).setTargetDensity(this.mResourcesImpl.getDisplayMetrics());
                                drawable = dr;
                            } catch (Throwable th2) {
                                th = th2;
                                throw th;
                            }
                        }
                        drawable = dr;
                    }
                }
                return drawable;
            } catch (Throwable th3) {
                th = th3;
                throw th;
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private ThemeColor loadThemeColor(String packageName, String zipName, String resName, String themeXml, int defaultColor) {
        if (!(mPreloadedThemeColorList.contains(packageName) || mNonThemedPackage.contains(packageName))) {
            ZipFileCache packageZipFileCache = ZipFileCache.getAndCheckCachedZipFile(getThemeDir(), zipName);
            if (packageZipFileCache != null) {
                InputStream is = packageZipFileCache.getInputStreamEntry(themeXml);
                if (is != null) {
                    try {
                        XmlPullParser parser = Xml.newPullParser();
                        parser.setInput(is, null);
                        inflateColorInfoList(parser, packageName);
                        try {
                            is.close();
                        } catch (IOException e) {
                            Log.e(TAG, "loadThemeColor : IOException when close stream");
                        }
                    } catch (XmlPullParserException e2) {
                        Log.e(TAG, "loadThemeColor : XmlPullParserException");
                    } catch (IOException e3) {
                        Log.e(TAG, "loadThemeColor : IOException");
                        try {
                            is.close();
                        } catch (IOException e4) {
                            Log.e(TAG, "loadThemeColor : IOException when close stream");
                        }
                    } catch (RuntimeException e5) {
                        Log.e(TAG, "loadThemeColor : RuntimeException", e5);
                        try {
                            is.close();
                        } catch (IOException e6) {
                            Log.e(TAG, "loadThemeColor : IOException when close stream");
                        }
                    } catch (Throwable th) {
                        try {
                            is.close();
                        } catch (IOException e7) {
                            Log.e(TAG, "loadThemeColor : IOException when close stream");
                        }
                    }
                }
                mPreloadedThemeColorList.add(packageName);
            } else {
                mNonThemedPackage.add(packageName);
            }
        }
        Integer value = (Integer) mCacheColorInfoList.get(resName);
        if (value != null) {
            return new ThemeColor(value.intValue(), true);
        }
        return new ThemeColor(defaultColor, DEBUG_VERBOSE_ICON);
    }

    public ThemeColor getThemeColor(TypedValue value, int id) throws NotFoundException {
        if (resUtils.getPreloading(this.mResourcesImpl)) {
            return new ThemeColor(value.data, DEBUG_VERBOSE_ICON);
        }
        ThemeResource themeRes = getThemeResource(id);
        String packageName = themeRes.packageName;
        String resName = themeRes.resName;
        boolean isFramework = packageName.equals(FRAMEWORK_RES);
        boolean isHwFramework = packageName.equals(FRAMEWORK_RES_EXT);
        if (!((!isFramework && !isHwFramework) || getPackageName() == null || getPackageName().isEmpty())) {
            String fwDir = isFramework ? FRAMEWORK_RES : FRAMEWORK_RES_EXT;
            ThemeColor tc = loadThemeColor(getPackageName() + "/" + fwDir, getPackageName(), getPackageName() + "/" + resName, fwDir + "/theme.xml", value.data);
            if (tc.mIsThemed) {
                return tc;
            }
        }
        return loadThemeColor(packageName, packageName, resName, "theme.xml", value.data);
    }

    protected Drawable loadDrawable(Resources wrapper, TypedValue value, int id, Theme theme, boolean useCache) throws NotFoundException {
        long key = (((long) value.assetCookie) << 32) | ((long) value.data);
        Drawable dr = null;
        ConstantState constantState;
        if (value.type < 28 || value.type > 31) {
            synchronized (HwResourcesImpl.class) {
                dr = this.mDrawableCacheEx.getInstance(key, wrapper, theme);
            }
            if (dr != null) {
                return dr;
            }
            synchronized (HwResourcesImpl.class) {
                if (resUtils.getPreloading(this.mResourcesImpl) || this.mThemeChanged) {
                    constantState = null;
                } else {
                    int preloadedDensity = resUtils.getPreloadedDensity(this.mResourcesImpl);
                    int i = this.mResourcesImpl.getConfiguration().densityDpi;
                    constantState = preloadedDensity == r0 ? (ConstantState) sPreloadedDrawablesEx.get(key) : null;
                }
                if (constantState != null) {
                    dr = constantState.newDrawable(wrapper);
                    return dr;
                }
                if (id != 0 && HwThemeManager.isHwTheme()) {
                    String str;
                    String entryName = this.mResourcesImpl.getResourceEntryName(id);
                    StringBuilder append = new StringBuilder().append(id).append("#");
                    if (this.mPackageName != null) {
                        str = this.mPackageName;
                    } else {
                        str = this.mResourcesImpl.getResourcePackageName(id);
                    }
                    String idAndPackageName = append.append(str).toString();
                    dr = checkSpecialIcons(wrapper, entryName);
                    if (dr != null) {
                        synchronized (HwResourcesImpl.class) {
                            if (resUtils.getPreloading(this.mResourcesImpl)) {
                                sPreloadedDrawablesEx.put(key, dr.getConstantState());
                            } else {
                                this.mDrawableCacheEx.put(key, theme, dr.getConstantState());
                            }
                        }
                        return dr;
                    } else if (IconCache.contains(idAndPackageName)) {
                        String packageName = this.mPackageName != null ? this.mPackageName : this.mResourcesImpl.getResourcePackageName(id);
                        this.mContainPackage = this.recPackageList.contains(idAndPackageName);
                        if (!this.mContainPackage) {
                            String str2 = TAG;
                            StringBuilder append2 = new StringBuilder().append("load icon id : ").append(Integer.toHexString(id)).append(", pkgName : ");
                            if (packageName == null) {
                                str = "null";
                            } else {
                                str = packageName;
                            }
                            Log.i(str2, append2.append(str).toString());
                        }
                        readDefaultConfig();
                        Drawable drIcon = getThemeIcon(wrapper, id, packageName);
                        if (drIcon != null) {
                            synchronized (HwResourcesImpl.class) {
                                if (resUtils.getPreloading(this.mResourcesImpl)) {
                                    sPreloadedDrawablesEx.put(key, drIcon.getConstantState());
                                } else {
                                    this.mDrawableCacheEx.put(key, theme, drIcon.getConstantState());
                                }
                            }
                            return drIcon;
                        } else if (!this.mContainPackage) {
                            str = TAG;
                            append = new StringBuilder().append("icon : ");
                            if (packageName == null) {
                                packageName = "null";
                            }
                            Log.i(str, append.append(packageName).append(" found in app").toString());
                            this.recPackageList.add(idAndPackageName);
                        }
                    } else {
                        dr = getThemeDrawable(value, id, wrapper);
                        if (dr != null) {
                            synchronized (HwResourcesImpl.class) {
                                if (resUtils.getPreloading(this.mResourcesImpl)) {
                                    sPreloadedDrawablesEx.put(key, dr.getConstantState());
                                } else {
                                    this.mDrawableCacheEx.put(key, theme, dr.getConstantState());
                                }
                            }
                            return dr;
                        }
                    }
                }
                return dr;
            }
        }
        if (id != 0 && HwThemeManager.isHwTheme()) {
            synchronized (HwResourcesImpl.class) {
                dr = this.mColorDrawableCacheEx.getInstance(key, wrapper, theme);
            }
            if (dr != null) {
                return dr;
            }
            synchronized (HwResourcesImpl.class) {
                constantState = (resUtils.getPreloading(this.mResourcesImpl) || this.mThemeChanged) ? null : (ConstantState) sPreloadedColorDrawablesEx.get(key);
            }
            if (constantState != null) {
                dr = constantState.newDrawable(wrapper);
            } else {
                ThemeColor colorValue = getThemeColor(value, id);
                if (colorValue != null && colorValue.mIsThemed) {
                    dr = new ColorDrawable(colorValue.mColor);
                }
            }
            if (dr != null) {
                constantState = dr.getConstantState();
                if (constantState != null) {
                    synchronized (HwResourcesImpl.class) {
                        if (resUtils.getPreloading(this.mResourcesImpl)) {
                            sPreloadedColorDrawablesEx.put(key, constantState);
                        } else {
                            this.mColorDrawableCacheEx.put(key, theme, constantState);
                        }
                    }
                }
            }
        }
        return dr;
    }

    public Drawable getThemeDrawable(TypedValue value, int id, Resources res) throws NotFoundException {
        if (resUtils.getPreloading(this.mResourcesImpl)) {
            return null;
        }
        String packageName = getThemeResource(id).packageName;
        if (value.string == null) {
            return null;
        }
        if (!emptyOrContainsPreloadedZip(packageName)) {
            if (!emptyOrContainsPreloadedZip(this.mPackageName)) {
                return null;
            }
        }
        String file = value.string.toString();
        if (file == null || file.isEmpty()) {
            return null;
        }
        file = file.replaceFirst("-v\\d+/", "/");
        if (file == null || file.isEmpty()) {
            return null;
        }
        Drawable dr;
        int themeDensity;
        String dir;
        if (packageName.indexOf(FRAMEWORK_RES) >= 0) {
            if (file.indexOf("_holo") >= 0) {
                return null;
            }
        }
        boolean isLand = file.indexOf("-land") >= 0 ? true : DEBUG_VERBOSE_ICON;
        boolean isFramework = packageName.equals(FRAMEWORK_RES);
        boolean isHwFramework = packageName.equals(FRAMEWORK_RES_EXT);
        if ((isFramework || isHwFramework) && this.mPackageName != null) {
            if (!this.mPackageName.isEmpty()) {
                ZipFileCache frameworkZipFileCache = ZipFileCache.getAndCheckCachedZipFile(getThemeDir(), this.mPackageName);
                String key = packageName + "/" + file;
                if (frameworkZipFileCache != null) {
                    dr = frameworkZipFileCache.getDrawableEntry(res, value, key, null);
                    if (dr == null) {
                        if (!file.contains(DRAWABLE_FHD)) {
                            frameworkZipFileCache.initResDirInfo();
                            int index = isFramework ? isLand ? 3 : 2 : isLand ? 5 : 4;
                            themeDensity = frameworkZipFileCache.getDrawableDensity(index);
                            dir = frameworkZipFileCache.getDrawableDir(index);
                            if (!(themeDensity == -1 || dir == null)) {
                                Options opts = new Options();
                                opts.inDensity = themeDensity;
                                dr = frameworkZipFileCache.getDrawableEntry(res, value, dir + File.separator + file.substring(file.lastIndexOf(File.separator) + ICON_NOTUSE_AVGCOLOR), opts);
                            }
                        }
                    }
                    if (dr != null) {
                        return dr;
                    }
                }
            }
        }
        ZipFileCache packageZipFileCache = ZipFileCache.getAndCheckCachedZipFile(getThemeDir(), packageName);
        if (packageZipFileCache == null) {
            return null;
        }
        dr = packageZipFileCache.getDrawableEntry(res, value, file, null);
        if (dr == null) {
            if (!file.contains(DRAWABLE_FHD)) {
                packageZipFileCache.initResDirInfo();
                index = isLand ? ICON_NOTUSE_AVGCOLOR : ICON_USE_AVGCOLOR;
                themeDensity = packageZipFileCache.getDrawableDensity(index);
                dir = packageZipFileCache.getDrawableDir(index);
                if (!(themeDensity == -1 || dir == null)) {
                    opts = new Options();
                    opts.inDensity = themeDensity;
                    dr = file.contains(DPI_2K) ? packageZipFileCache.getDrawableEntry(res, value, file.replace(DPI_2K, DPI_FHD), opts) : packageZipFileCache.getDrawableEntry(res, value, dir + File.separator + file.substring(file.lastIndexOf(File.separator) + ICON_NOTUSE_AVGCOLOR), opts);
                }
            }
        }
        return dr;
    }

    public ThemeResource getThemeResource(int id) {
        String packageName = this.mResourcesImpl.getResourcePackageName(id);
        String resName = this.mResourcesImpl.getResourceName(id);
        if (packageName.equals(ANDROID_RES)) {
            packageName = FRAMEWORK_RES;
            resName = FRAMEWORK_RES + resName.substring(LEN_OF_ANDROID);
        } else if (packageName.equals(ANDROID_RES_EXT)) {
            packageName = FRAMEWORK_RES_EXT;
            resName = FRAMEWORK_RES_EXT + resName.substring(LEN_OF_ANDROID_EXT);
        } else if (this.mPackageName != null) {
            packageName = this.mPackageName;
        }
        return new ThemeResource(packageName, resName);
    }

    public void setResourcesImpl(ResourcesImpl resourcesImpl) {
        this.mResourcesImpl = resourcesImpl;
    }

    public void initResource() {
        if (-1 == sStandardBgSize) {
            setStandardSize(sConfigAppBigIconSize == -1 ? getDimensionPixelSize(34472073) : sConfigAppBigIconSize, sConfigAppInnerIconSize == -1 ? getDimensionPixelSize(34472072) : sConfigAppInnerIconSize);
        }
        HwResources.setIsSRLocale("sr".equals(Locale.getDefault().getLanguage()));
        checkChangedNameFile();
    }

    public int getDimensionPixelSize(int id) throws NotFoundException {
        TypedValue value = obtainTempTypedValue();
        try {
            this.mResourcesImpl.getValue(id, value, true);
            if (value.type == 5) {
                int complexToDimensionPixelSize = TypedValue.complexToDimensionPixelSize(value.data, this.mResourcesImpl.getDisplayMetrics());
                return complexToDimensionPixelSize;
            }
            throw new NotFoundException("Resource ID #0x" + Integer.toHexString(id) + " type #0x" + Integer.toHexString(value.type) + " is not valid");
        } finally {
            releaseTempTypedValue(value);
        }
    }

    protected void releaseTempTypedValue(TypedValue value) {
        synchronized (this.mTmpValueLock) {
            if (this.mTmpValue == null) {
                this.mTmpValue = value;
            }
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

    private static void setStandardSize(int standardBgSize, int standardIconSize) {
        sStandardBgSize = standardBgSize;
        sDefaultSizeWithoutEdge = sStandardBgSize + DEFAULT_EDGE_SIZE;
        sStandardIconSize = standardIconSize;
    }

    protected void handleClearCache(int configChanges, boolean themeChanged) {
        synchronized (HwResourcesImpl.class) {
            if (themeChanged) {
                mCacheColorInfoList.clear();
                mPreloadedThemeColorList.clear();
                mNonThemedPackage.clear();
                clearHwThemeZipsAndIconsCache();
                sThemeDescripHasRead = DEBUG_VERBOSE_ICON;
                sIsHWThemes = true;
            }
            if (this.mDrawableCacheEx != null) {
                this.mDrawableCacheEx.onConfigurationChange(configChanges);
            }
            if (this.mDynamicDrawableCache != null) {
                this.mDynamicDrawableCache.onConfigurationChange(configChanges);
            }
            if (this.mColorDrawableCacheEx != null) {
                this.mColorDrawableCacheEx.onConfigurationChange(configChanges);
            }
        }
    }

    public void setPackageName(String name) {
        this.mPackageName = name;
        if (SystemProperties.getBoolean("ro.config.hw_lock_res_whitelist", DEBUG_VERBOSE_ICON)) {
            String currentPackageName = ActivityThread.currentPackageName();
            if (this.mPackageName != null && !this.mPackageName.equals(currentPackageName)) {
                this.mResourcesImpl.updateAssetConfiguration(this.mPackageName, DEBUG_VERBOSE_ICON, DEBUG_VERBOSE_ICON);
            }
        }
    }

    public String getPackageName() {
        return this.mPackageName;
    }

    protected void setHwTheme(Configuration config) {
        boolean z = true;
        if (HwThemeManager.isHwTheme() && config != null) {
            this.mConfigHwt = config.extraConfig.getConfigItem(ICON_NOTUSE_AVGCOLOR);
            if (this.mResourcesImpl.getConfiguration().extraConfig.getConfigItem(ICON_NOTUSE_AVGCOLOR) == 0) {
                z = DEBUG_VERBOSE_ICON;
            }
            this.mThemeChanged = z;
        }
    }

    public void updateConfiguration(Configuration config, int configChanges) {
        boolean isInitConfigEmpty = this.mResourcesImpl.getConfiguration().equals(Configuration.EMPTY);
        configChanges = ActivityInfoExInner.activityInfoConfigToNative(configChanges);
        IHwConfiguration configEx = this.mResourcesImpl.getConfiguration().extraConfig;
        if (HwThemeManager.isHwTheme() && Configuration.needNewResources(configChanges, ICON_USE_AVGCOLOR)) {
            if (!(isInitConfigEmpty || (configChanges & StubController.PERMISSION_ACCESS_3G) == 0)) {
                handleClearCache(configChanges, DEBUG_VERBOSE_ICON);
            }
            if (this.mConfigHwt != configEx.getConfigItem(ICON_NOTUSE_AVGCOLOR)) {
                this.mConfigHwt = configEx.getConfigItem(ICON_NOTUSE_AVGCOLOR);
                synchronized (HwResourcesImpl.class) {
                    this.mThemeChanged = true;
                }
                if (configEx.getConfigItem(4) == ICON_NOTUSE_AVGCOLOR) {
                    handleClearCache(configChanges, true);
                } else {
                    handleClearCache(configChanges, DEBUG_VERBOSE_ICON);
                }
                String path = Environment.getDataDirectory() + "/themes/" + configEx.getConfigItem(3);
                synchronized (HwResourcesImpl.class) {
                    if (!mThemeAbsoluteDir.equals(path)) {
                        setHwThemeAbsoluteDir(path);
                    }
                }
            }
        }
        if (this.mResourcesImpl.getConfiguration().locale != null) {
            HwResources.setIsSRLocale("sr".equals(this.mResourcesImpl.getConfiguration().locale.getLanguage()));
        }
    }

    public Drawable getDrawableForDynamic(Resources res, String packageName, String iconName) throws NotFoundException {
        String imgFile = "";
        imgFile = "dynamic_icons/" + packageName + "/" + iconName + ".png";
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
            if (dr != null) {
                ((BitmapDrawable) dr).setTargetDensity(this.mResourcesImpl.getDisplayMetrics());
                this.mDynamicDrawableCache.put(key, null, dr.getConstantState(), true);
            }
        }
        return dr;
    }

    private static synchronized void readDefaultConfig() {
        Throwable th;
        synchronized (HwResourcesImpl.class) {
            if (sDefaultConfigHasRead) {
                return;
            }
            File inputFile;
            InputStream inputStream = null;
            try {
                inputFile = HwCfgFilePolicy.getCfgFile(DEFAULT_CONFIG_NAME, ICON_USE_AVGCOLOR);
                if (inputFile == null) {
                    inputFile = new File(DEFAULT_CONFIG_PATH);
                }
            } catch (NoClassDefFoundError e) {
                Log.d(TAG, "HwCfgFilePolicy NoClassDefFoundError");
                inputFile = new File(DEFAULT_CONFIG_PATH);
            } catch (Throwable th2) {
                inputFile = new File(DEFAULT_CONFIG_PATH);
            }
            try {
                InputStream in = new FileInputStream(inputFile);
                try {
                    XmlPullParser parser = Xml.newPullParser();
                    parser.setInput(in, null);
                    XmlUtils.beginDocument(parser, TAG_CONFIG);
                    sDisThemeIcon = new HashSet();
                    sDisThemeBackground = new HashSet();
                    for (int type = parser.next(); type != ICON_NOTUSE_AVGCOLOR; type = parser.next()) {
                        if (type == 2) {
                            String packageName = parser.getAttributeValue(ICON_USE_AVGCOLOR);
                            Object DrawableType = null;
                            parser.next();
                            if (parser.getEventType() == 4) {
                                DrawableType = String.valueOf(parser.getText());
                            }
                            if (TYPT_ICON.equals(DrawableType)) {
                                sDisThemeIcon.add(packageName);
                            } else if (TYPT_BACKGROUND.equals(DrawableType)) {
                                sDisThemeBackground.add(packageName);
                            }
                        }
                    }
                    if (in != null) {
                        try {
                            in.close();
                        } catch (IOException e2) {
                            Log.e(TAG, "readDefaultConfig : IOException when close stream");
                        }
                    }
                } catch (FileNotFoundException e3) {
                    inputStream = in;
                } catch (XmlPullParserException e4) {
                    inputStream = in;
                } catch (Throwable th3) {
                    th = th3;
                    inputStream = in;
                }
            } catch (FileNotFoundException e5) {
                try {
                    Log.e(TAG, "readDefaultConfig : FileNotFoundException");
                    sDisThemeIcon = null;
                    sDisThemeBackground = null;
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e6) {
                            Log.e(TAG, "readDefaultConfig : IOException when close stream");
                        }
                    }
                    sDefaultConfigHasRead = true;
                } catch (Throwable th4) {
                    th = th4;
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e7) {
                            Log.e(TAG, "readDefaultConfig : IOException when close stream");
                        }
                    }
                    throw th;
                }
            } catch (XmlPullParserException e8) {
                Log.e(TAG, "readDefaultConfig : XmlPullParserException | IOException");
                sDisThemeIcon = null;
                sDisThemeBackground = null;
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e9) {
                        Log.e(TAG, "readDefaultConfig : IOException when close stream");
                    }
                }
                sDefaultConfigHasRead = true;
            }
            sDefaultConfigHasRead = true;
        }
    }

    private static synchronized void loadMultiDpiWhiteList() {
        Throwable th;
        synchronized (HwResourcesImpl.class) {
            File configFile;
            sMultiDirHasRead = true;
            InputStream inputStream = null;
            try {
                configFile = HwCfgFilePolicy.getCfgFile(FILE_MULTI_DPI_WHITELIST, ICON_USE_AVGCOLOR);
                if (configFile == null) {
                    configFile = new File(DEFAULT_MULTI_DPI_WHITELIST);
                }
            } catch (NoClassDefFoundError e) {
                Log.e(TAG, "HwCfgFilePolicy NoClassDefFoundError! ");
                configFile = new File(DEFAULT_MULTI_DPI_WHITELIST);
            } catch (Throwable th2) {
                configFile = new File(DEFAULT_MULTI_DPI_WHITELIST);
            }
            try {
                InputStream inputStream2 = new FileInputStream(configFile);
                try {
                    XmlPullParser xmlParser = Xml.newPullParser();
                    xmlParser.setInput(inputStream2, null);
                    for (int xmlEventType = xmlParser.next(); xmlEventType != ICON_NOTUSE_AVGCOLOR; xmlEventType = xmlParser.next()) {
                        if (xmlEventType == 2) {
                            String packageName = xmlParser.getAttributeValue(null, XML_ATTRIBUTE_PACKAGE_NAME);
                            if (packageName != null) {
                                sMultiDirPkgNames.add(packageName);
                            }
                        }
                    }
                    if (inputStream2 != null) {
                        try {
                            inputStream2.close();
                        } catch (IOException e2) {
                            Log.e(TAG, "loadMultiDpiWhiteList:- IOE while closing stream");
                        }
                    }
                    inputStream = inputStream2;
                } catch (FileNotFoundException e3) {
                    inputStream = inputStream2;
                    sMultiDirHasRead = DEBUG_VERBOSE_ICON;
                    Log.e(TAG, "loadMultiDpiWhiteList : FileNotFoundException");
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e4) {
                            Log.e(TAG, "loadMultiDpiWhiteList:- IOE while closing stream");
                        }
                    }
                } catch (XmlPullParserException e5) {
                    inputStream = inputStream2;
                    sMultiDirHasRead = DEBUG_VERBOSE_ICON;
                    Log.e(TAG, "loadMultiDpiWhiteList : XmlPullParserException");
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e6) {
                            Log.e(TAG, "loadMultiDpiWhiteList:- IOE while closing stream");
                        }
                    }
                } catch (IOException e7) {
                    inputStream = inputStream2;
                    try {
                        sMultiDirHasRead = DEBUG_VERBOSE_ICON;
                        Log.e(TAG, "loadMultiDpiWhiteList : IOException");
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (IOException e8) {
                                Log.e(TAG, "loadMultiDpiWhiteList:- IOE while closing stream");
                            }
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (IOException e9) {
                                Log.e(TAG, "loadMultiDpiWhiteList:- IOE while closing stream");
                            }
                        }
                        throw th;
                    }
                } catch (Throwable th4) {
                    th = th4;
                    inputStream = inputStream2;
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    throw th;
                }
            } catch (FileNotFoundException e10) {
                sMultiDirHasRead = DEBUG_VERBOSE_ICON;
                Log.e(TAG, "loadMultiDpiWhiteList : FileNotFoundException");
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (XmlPullParserException e11) {
                sMultiDirHasRead = DEBUG_VERBOSE_ICON;
                Log.e(TAG, "loadMultiDpiWhiteList : XmlPullParserException");
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e12) {
                sMultiDirHasRead = DEBUG_VERBOSE_ICON;
                Log.e(TAG, "loadMultiDpiWhiteList : IOException");
                if (inputStream != null) {
                    inputStream.close();
                }
            }
        }
    }

    public boolean isInMultiDpiWhiteList(String packageName) {
        if (!sMultiDirHasRead) {
            loadMultiDpiWhiteList();
        }
        Boolean result = Boolean.valueOf(true);
        if (sMultiDirPkgNames.size() == 0) {
            Log.i(TAG, "open lock res on whitelist, but not provide the whitelist, so lock resource");
        } else if (packageName == null) {
            Log.i(TAG, "can't get the package name, so lock resource");
        } else if (!sMultiDirPkgNames.contains(packageName)) {
            result = Boolean.valueOf(DEBUG_VERBOSE_ICON);
        }
        return result.booleanValue();
    }

    private String getIconFileName(String packageName, CacheEntry ce) {
        StringBuilder stringBuilder = new StringBuilder();
        if (ce.type == ICON_NOTUSE_AVGCOLOR) {
            packageName = ce.name;
        }
        return stringBuilder.append(packageName).append(".png").toString();
    }

    private int computeDestIconSize(Rect validRect, int maskSize, int w, int h) {
        double validSize;
        if (validRect.height() > validRect.width()) {
            validSize = (((double) maskSize) / (((double) validRect.height()) + 1.0d)) * ((double) h);
        } else {
            validSize = (((double) maskSize) / (((double) validRect.width()) + 1.0d)) * ((double) w);
        }
        int iconSize = (int) validSize;
        if (ICON_NOTUSE_AVGCOLOR == iconSize % 2) {
            return (int) (0.5d + validSize);
        }
        return iconSize;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void checkChangedNameFile() {
        synchronized (HwResourcesImpl.class) {
            if (packageNameMap.size() > 0) {
            } else if (bCfgFileNotExist) {
            } else {
                File file = HwCfgFilePolicy.getCfgFile(CHANGEPACKAGE_CONFIG_NAME, ICON_USE_AVGCOLOR);
                if (file == null) {
                    bCfgFileNotExist = true;
                    return;
                }
                StringBuilder sb = new StringBuilder();
                try {
                    FileInputStream fis = new FileInputStream(file);
                    while (true) {
                        try {
                            int ch = fis.read();
                            if (ch != -1) {
                                sb.append((char) ch);
                            } else {
                                try {
                                    break;
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        } catch (IOException e2) {
                            e2.printStackTrace();
                        } catch (Throwable th) {
                            try {
                                fis.close();
                            } catch (IOException e22) {
                                e22.printStackTrace();
                            }
                        }
                    }
                    fis.close();
                } catch (FileNotFoundException e3) {
                    e3.printStackTrace();
                }
                if (sb.length() == 0) {
                    return;
                }
                String[] pkgs = sb.toString().trim().split(";");
                int length = pkgs.length;
                for (int i = ICON_USE_AVGCOLOR; i < length; i += ICON_NOTUSE_AVGCOLOR) {
                    String[] newPkgs = pkgs[i].split(",");
                    if (newPkgs.length == 2) {
                        packageNameMap.put(newPkgs[ICON_NOTUSE_AVGCOLOR], newPkgs[ICON_USE_AVGCOLOR]);
                    }
                }
            }
        }
    }

    public Drawable getJoinBitmap(Drawable srcDraw, int backgroundId) {
        synchronized (HwResourcesImpl.class) {
            if (srcDraw == null) {
                Log.w(TAG, "notification icon is null!");
                return null;
            }
            Bitmap bmpSrc = null;
            if (srcDraw instanceof BitmapDrawable) {
                Bitmap temp = ((BitmapDrawable) srcDraw).getBitmap();
                int width = temp.getWidth();
                int height = temp.getHeight();
                int[] pixels = new int[(width * height)];
                temp.getPixels(pixels, ICON_USE_AVGCOLOR, width, ICON_USE_AVGCOLOR, ICON_USE_AVGCOLOR, width, height);
                bmpSrc = Bitmap.createBitmap(pixels, width, height, Config.ARGB_8888);
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
            Bitmap bmpBg = iconZipFileCache.getBitmapEntry(this.mResourcesImpl, NOTIFICATION_ICON_PREFIX + backgroundId + ".png");
            if (bmpBg == null) {
                bmpBg = iconZipFileCache.getBitmapEntry(this.mResourcesImpl, NOTIFICATION_ICON_EXIST);
            }
            Bitmap bmpMask = iconZipFileCache.getBitmapEntry(this.mResourcesImpl, NOTIFICATION_ICON_MASK);
            Bitmap bmpBorder = iconZipFileCache.getBitmapEntry(this.mResourcesImpl, NOTIFICATION_ICON_BORDER);
            if (bmpBg == null || bmpMask == null || bmpBorder == null) {
                Log.w(TAG, "getJoinBitmap :" + (bmpBg == null ? " bmpBg == null, " : " bmpBg != null, ") + " id = " + backgroundId + (bmpMask == null ? " bmpMask == null, " : "bmpMask != null, ") + (bmpBorder == null ? " bmpBorder == null, " : " bmpBorder != null"));
                return null;
            }
            int w = bmpSrc.getWidth();
            int h = bmpSrc.getHeight();
            Drawable drawable;
            if (w == ICON_NOTUSE_AVGCOLOR && h == ICON_NOTUSE_AVGCOLOR) {
                Bitmap overlap = IconBitmapUtils.overlap2Bitmap(bmpSrc, bmpBg);
                if (overlap == null) {
                    drawable = null;
                } else {
                    drawable = new BitmapDrawable(overlap);
                }
                return drawable;
            }
            if (!(bmpMask.getWidth() == w || bmpMask.getHeight() == h)) {
                bmpMask = IconBitmapUtils.drawSource(bmpMask, w, w);
            }
            if (!(bmpBorder.getWidth() == w || bmpBorder.getHeight() == h)) {
                bmpBorder = IconBitmapUtils.drawSource(bmpBorder, w, w);
            }
            if (!(bmpBg.getWidth() == w || bmpBg.getHeight() == h)) {
                bmpBg = IconBitmapUtils.drawSource(bmpBg, w, w);
            }
            Bitmap result = IconBitmapUtils.composeIcon(bmpSrc, bmpMask, bmpBg, bmpBorder, DEBUG_VERBOSE_ICON);
            if (result != null) {
                drawable = new BitmapDrawable(result);
                return drawable;
            }
            Log.w(TAG, "getJoinBitmap is null!");
            return null;
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
        boolean contains;
        synchronized (HwResourcesImpl.class) {
            contains = !sPreloadedHwThemeZips.isEmpty() ? sPreloadedHwThemeZips.contains(zipFile) : true;
        }
        return contains;
    }

    public String getDiffThemeIconPath() {
        if (TextUtils.isEmpty(CUSTOM_DIFF_THEME_DIR)) {
            File iconZip;
            try {
                iconZip = HwCfgFilePolicy.getCfgFile(DIFF_THEME_ICON, ICON_USE_AVGCOLOR);
            } catch (NoClassDefFoundError e) {
                iconZip = null;
                Log.e(TAG, "HwCfgFilePolicy NoClassDefFoundError");
            }
            if (iconZip != null) {
                return iconZip.getParent();
            }
            return null;
        } else if (new File(CUSTOM_DIFF_THEME_DIR + "/" + ICONS_ZIPFILE).exists()) {
            return CUSTOM_DIFF_THEME_DIR;
        } else {
            return null;
        }
    }

    public Bitmap getIconsfromDiffTheme(ResourcesImpl Impl, String imgFile) {
        Bitmap bmp = null;
        String iconFilePath = getDiffThemeIconPath();
        if (iconFilePath != null) {
            ZipFileCache diffIconZipFileCache = ZipFileCache.getAndCheckCachedZipFile(iconFilePath, ICONS_ZIPFILE);
            if (diffIconZipFileCache != null) {
                bmp = diffIconZipFileCache.getBitmapEntry(Impl, imgFile);
                if (bmp != null) {
                    Log.i(TAG, "icon : " + imgFile + " found in custom diff theme");
                }
            }
        }
        return bmp;
    }

    public void preloadHwThemeZipsAndSomeIcons(int currentUserId) {
        setHwThemeAbsoluteDir(Environment.getDataDirectory() + "/themes/" + currentUserId);
        synchronized (HwResourcesImpl.class) {
            File themePath = new File(mThemeAbsoluteDir);
            if (themePath.exists()) {
                File[] files = themePath.listFiles(new HwThemeFileFilter());
                if (files == null) {
                    return;
                }
                int length = files.length;
                for (int i = ICON_USE_AVGCOLOR; i < length; i += ICON_NOTUSE_AVGCOLOR) {
                    sPreloadedHwThemeZips.add(files[i].getName());
                }
            }
            HwResources.setIsSRLocale("sr".equals(Locale.getDefault().getLanguage()));
        }
    }

    private void setHwThemeAbsoluteDir(String path) {
        synchronized (HwResourcesImpl.class) {
            mThemeAbsoluteDir = path;
        }
    }

    public void clearHwThemeZipsAndSomeIcons() {
        synchronized (HwResourcesImpl.class) {
            clearHwThemeZipsAndIconsCache();
        }
    }

    private void clearHwThemeZipsAndIconsCache() {
        if (sBgList != null) {
            int listSize = sBgList.size();
            for (int i = ICON_USE_AVGCOLOR; i < listSize; i += ICON_NOTUSE_AVGCOLOR) {
                ((Bitmap) sBgList.get(i)).recycle();
            }
            sBgList.clear();
        }
        if (!(sBmpBorder == null || sBmpBorder.isRecycled())) {
            sBmpBorder.recycle();
            sBmpBorder = null;
        }
        if (!(sBmpMask == null || sBmpMask.isRecycled())) {
            sBmpMask.recycle();
            sBmpMask = null;
            sMaskSizeWithoutEdge = -1;
        }
        if (!(sBmpSysAppDefault == null || sBmpSysAppDefault.isRecycled())) {
            sBmpSysAppDefault.recycle();
            sBmpSysAppDefault = null;
        }
        ZipFileCache.clear();
        sUseAvgColor = -1;
        sPreloadedHwThemeZips.clear();
    }

    private void inflateColorInfoList(XmlPullParser parser, String packageName) throws XmlPullParserException, IOException {
        int innerDepth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if (type == ICON_NOTUSE_AVGCOLOR) {
                return;
            }
            if (parser.getDepth() <= innerDepth && type == 3) {
                return;
            }
            if (type == 2) {
                String lableName = parser.getName();
                if (lableName.equals("color") || lableName.equals("drawable")) {
                    String name = parser.getAttributeName(ICON_USE_AVGCOLOR);
                    String colorName = parser.getAttributeValue(ICON_USE_AVGCOLOR);
                    String text = null;
                    if (parser.next() == 4) {
                        text = parser.getText();
                    }
                    if (XML_ATTRIBUTE_PACKAGE_NAME.equalsIgnoreCase(name)) {
                        int colorVaue = getColorValueFromStr(text);
                        StringBuilder sb = new StringBuilder();
                        sb.append(packageName);
                        sb.append(":");
                        sb.append(lableName);
                        sb.append("/");
                        sb.append(colorName);
                        mCacheColorInfoList.put(sb.toString(), Integer.valueOf(colorVaue));
                    }
                }
            }
        }
    }

    private int getColorValueFromStr(String value) throws IOException {
        if (value == null) {
            return ICON_USE_AVGCOLOR;
        }
        if (value.startsWith("#")) {
            value = value.substring(ICON_NOTUSE_AVGCOLOR);
            if (value.length() > DEFAULT_EDGE_SIZE) {
                String str = TAG;
                StringBuilder append = new StringBuilder().append("getColorValueFromStr ");
                Object[] objArr = new Object[ICON_NOTUSE_AVGCOLOR];
                objArr[ICON_USE_AVGCOLOR] = value;
                Log.e(str, append.append(String.format("Color value '%s' is too long. Format is either#AARRGGBB, #RRGGBB, #RGB, or #ARGB", objArr)).toString());
                return ICON_USE_AVGCOLOR;
            }
            char[] color;
            char charAt;
            if (value.length() == 3) {
                color = new char[DEFAULT_EDGE_SIZE];
                color[ICON_NOTUSE_AVGCOLOR] = 'F';
                color[ICON_USE_AVGCOLOR] = 'F';
                charAt = value.charAt(ICON_USE_AVGCOLOR);
                color[3] = charAt;
                color[2] = charAt;
                charAt = value.charAt(ICON_NOTUSE_AVGCOLOR);
                color[5] = charAt;
                color[4] = charAt;
                charAt = value.charAt(2);
                color[LEN_OF_ANDROID] = charAt;
                color[6] = charAt;
                value = new String(color);
            } else if (value.length() == 4) {
                color = new char[DEFAULT_EDGE_SIZE];
                charAt = value.charAt(ICON_USE_AVGCOLOR);
                color[ICON_NOTUSE_AVGCOLOR] = charAt;
                color[ICON_USE_AVGCOLOR] = charAt;
                charAt = value.charAt(ICON_NOTUSE_AVGCOLOR);
                color[3] = charAt;
                color[2] = charAt;
                charAt = value.charAt(2);
                color[5] = charAt;
                color[4] = charAt;
                charAt = value.charAt(3);
                color[LEN_OF_ANDROID] = charAt;
                color[6] = charAt;
                value = new String(color);
            } else if (value.length() == 6) {
                value = "FF" + value;
            }
            return (int) Long.parseLong(value, 16);
        }
        str = TAG;
        append = new StringBuilder().append("getColorValueFromStr ");
        objArr = new Object[ICON_NOTUSE_AVGCOLOR];
        objArr[ICON_USE_AVGCOLOR] = value;
        Log.e(str, append.append(String.format("Color value '%s' must start with #", objArr)).toString());
        return ICON_USE_AVGCOLOR;
    }

    private boolean isCurrentHwTheme(String dirpath, String filename) {
        synchronized (HwResourcesImpl.class) {
            if (sThemeDescripHasRead) {
                boolean z = sIsHWThemes;
                return z;
            }
            Document document = null;
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            try {
                document = builderFactory.newDocumentBuilder().parse(new File(dirpath, filename));
            } catch (ParserConfigurationException e) {
                Log.e(TAG, "getLayoutXML ParserConfigurationException filename =" + filename);
            } catch (SAXException e2) {
                Log.e(TAG, "getLayoutXML SAXException filename =" + filename);
            } catch (IOException e3) {
                Log.e(TAG, "getLayoutXML IOException filename = " + filename);
            } catch (Exception e4) {
                Log.e(TAG, "getLayoutXML Exception filename =" + filename);
            }
            if (document == null) {
                return sIsHWThemes;
            }
            Element rootElement = document.getDocumentElement();
            if (rootElement == null) {
                return sIsHWThemes;
            }
            NodeList itemNodes = rootElement.getChildNodes();
            for (int i = ICON_USE_AVGCOLOR; i < itemNodes.getLength(); i += ICON_NOTUSE_AVGCOLOR) {
                Node itemNode = itemNodes.item(i);
                if (itemNode.getNodeType() == (short) 1 && THEMEDISIGNER.equals(itemNode.getNodeName())) {
                    sIsHWThemes = EMUI.equals(itemNode.getTextContent());
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
    }
}
