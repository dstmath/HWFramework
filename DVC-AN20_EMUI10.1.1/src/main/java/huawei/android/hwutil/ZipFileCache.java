package huawei.android.hwutil;

import android.content.res.Resources;
import android.content.res.ResourcesImplEx;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import com.huawei.android.util.DisplayMetricsEx;
import com.huawei.hwpartbasicplatform.BuildConfig;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public final class ZipFileCache {
    private static final boolean DEBUG = false;
    private static final boolean DEBUG_ICON = false;
    private static String ICONS = "icons";
    public static final int RES_INDEX_DEFAULT = 0;
    public static final int RES_INDEX_FRW = 2;
    public static final int RES_INDEX_HW_FRW = 4;
    public static final int RES_INDEX_LAND = 1;
    public static final int RES_INDEX_LAND_FRW = 3;
    public static final int RES_INDEX_LAND_HW_FRW = 5;
    private static final String TAG = "ZipFileCache";
    private static final int TRY_TIMES = 3;
    private static final ConcurrentHashMap<String, ZipFileCache> sZipFileCacheMaps = new ConcurrentHashMap<>();
    private boolean mFileNotExist = false;
    private boolean mInited = false;
    private String mPath;
    private String mZip;
    private ZipFile mZipFile;
    private ZipResDir[] mZipResDir = {new ZipResDir(-1, null), new ZipResDir(-1, null), new ZipResDir(-1, null), new ZipResDir(-1, null), new ZipResDir(-1, null), new ZipResDir(-1, null)};

    private static class ZipResDir {
        public int mDensity = -1;
        public String mDir = BuildConfig.FLAVOR;

        public ZipResDir(int density, String dir) {
            this.mDensity = density;
            this.mDir = dir;
        }
    }

    private ZipFileCache(String path, String zip) {
        this.mPath = path;
        this.mZip = zip;
        if (!openZipFile() && ICONS.equals(zip)) {
            Log.w(TAG, "init icons failed when open zip file. mPath=" + this.mPath + ",mZip=" + this.mZip + ",mFileNotExist=" + this.mFileNotExist);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0054, code lost:
        if (r4.mFileNotExist == false) goto L_0x0057;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0056, code lost:
        return null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0057, code lost:
        return r4;
     */
    public static ZipFileCache getAndCheckCachedZipFile(String path, String zip) {
        ZipFileCache oldValue;
        String key = path + "/" + zip;
        synchronized (ZipFileCache.class) {
            ZipFileCache zipFileCache = sZipFileCacheMaps.get(key);
            if (zipFileCache == null) {
                ZipFileCache zipFileCache2 = new ZipFileCache(path, zip);
                if (zipFileCache2.mZipFile != null) {
                    ZipFileCache oldValue2 = sZipFileCacheMaps.putIfAbsent(key, zipFileCache2);
                    if (oldValue2 != null) {
                        return oldValue2;
                    }
                    return zipFileCache2;
                } else if (!zipFileCache2.mFileNotExist || (oldValue = sZipFileCacheMaps.putIfAbsent(key, zipFileCache2)) == null) {
                    return null;
                } else {
                    return oldValue;
                }
            }
        }
    }

    public static synchronized void clear() {
        synchronized (ZipFileCache.class) {
            for (ZipFileCache zip : sZipFileCacheMaps.values()) {
                if (zip != null) {
                    zip.closeZipFile();
                }
            }
            sZipFileCacheMaps.clear();
        }
    }

    private synchronized boolean openZipFile() {
        try {
            File file = new File(this.mPath, this.mZip);
            if (!file.exists()) {
                this.mFileNotExist = true;
                return false;
            }
            this.mZipFile = new ZipFile(file, 1);
            this.mInited = false;
            return true;
        } catch (IOException e) {
            closeZipFile();
            setEmpty();
            return false;
        }
    }

    private synchronized void closeZipFile() {
        if (this.mZipFile != null) {
            try {
                this.mZipFile.close();
            } catch (IOException e) {
            }
            this.mZipFile = null;
        }
    }

    private void closeInputStream(InputStream is) {
        if (is != null) {
            try {
                is.close();
            } catch (IOException e) {
            }
        }
    }

    private synchronized void setEmpty() {
        this.mPath = BuildConfig.FLAVOR;
        this.mZip = BuildConfig.FLAVOR;
        this.mZipFile = null;
        this.mFileNotExist = false;
    }

    public synchronized Bitmap getBitmapEntry(ResourcesImplEx impl, String fileName) {
        if (this.mZipFile == null) {
            Log.w(TAG, "Get bitmap entry from zip file failed fileName=" + fileName);
            return null;
        }
        Bitmap bmp = null;
        InputStream is = null;
        try {
            ZipEntry entry = this.mZipFile.getEntry(fileName);
            if (entry != null) {
                is = this.mZipFile.getInputStream(entry);
                bmp = BitmapFactory.decodeStream(is);
                try {
                    is.available();
                } catch (IOException e) {
                    Log.e(TAG, "ZipFileCache#getBitmapEntry e = " + e.getMessage());
                }
                if (bmp != null) {
                    bmp.setDensity(impl.getHwResourcesImpl().hwGetDisplayMetrics().densityDpi);
                }
            }
        } catch (Exception e2) {
            closeZipFile();
            Log.e(TAG, "getBitmapEntry occur exception fileName = " + fileName);
        } finally {
            closeInputStream(is);
        }
        return bmp;
    }

    public synchronized Bitmap getBitmapEntry(Resources res, String fileName) {
        return getBitmapEntry(new ResourcesImplEx(res), fileName);
    }

    public synchronized Bitmap getBitmapEntry(Resources res, TypedValue value, String fileName, Rect padding) {
        if (this.mZipFile == null) {
            return null;
        }
        Bitmap bmp = null;
        InputStream is = null;
        if (padding == null) {
            padding = new Rect();
        }
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inScreenDensity = res != null ? DisplayMetricsEx.getNoncompatDensityDpi(res.getDisplayMetrics()) : DisplayMetrics.DENSITY_DEVICE_STABLE;
        try {
            ZipEntry entry = this.mZipFile.getEntry(fileName);
            if (!(entry == null || (bmp = BitmapFactory.decodeResourceStream(res, value, (is = this.mZipFile.getInputStream(entry)), padding, opts)) == null)) {
                bmp.setDensity(res != null ? res.getDisplayMetrics().densityDpi : DisplayMetrics.DENSITY_DEVICE_STABLE);
            }
            return bmp;
        } catch (Exception e) {
            Log.e(TAG, "getBitmapEntry(res,value,filename) occur exception fileName = " + fileName);
            return null;
        } finally {
            closeInputStream(is);
        }
    }

    public synchronized Bitmap getBitmapEntry(Resources res, TypedValue value, String fileName) {
        return getBitmapEntry(res, value, fileName, null);
    }

    public synchronized ArrayList<Bitmap> getBitmapList(ResourcesImplEx impl, String filePattern) {
        ArrayList<Bitmap> bmpList = new ArrayList<>();
        if (this.mZipFile == null) {
            return bmpList;
        }
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inScreenDensity = impl != null ? DisplayMetricsEx.getNoncompatDensityDpi(impl.getHwResourcesImpl().hwGetDisplayMetrics()) : DisplayMetrics.DENSITY_DEVICE_STABLE;
        try {
            Enumeration<? extends ZipEntry> enumeration = this.mZipFile.entries();
            while (enumeration.hasMoreElements()) {
                ZipEntry zipEntry = (ZipEntry) enumeration.nextElement();
                String name = zipEntry.getName();
                int indexfile = name.indexOf(filePattern);
                int indexofpng = name.indexOf(".png");
                if (indexfile == 0 && indexofpng > 0) {
                    InputStream is = this.mZipFile.getInputStream(zipEntry);
                    Bitmap bmp = BitmapFactory.decodeStream(is, null, opts);
                    closeInputStream(is);
                    if (bmp != null) {
                        bmp.setDensity(impl != null ? impl.getHwResourcesImpl().hwGetDisplayMetrics().densityDpi : DisplayMetrics.DENSITY_DEVICE_STABLE);
                        bmpList.add(bmp);
                    }
                }
            }
            return bmpList;
        } catch (RuntimeException e) {
            closeInputStream(null);
            bmpList.clear();
            return bmpList;
        } catch (Exception e2) {
            closeInputStream(null);
            bmpList.clear();
            return bmpList;
        }
    }

    public synchronized ArrayList<Bitmap> getBitmapList(Resources res, String filePattern) {
        ArrayList<Bitmap> bmpList = new ArrayList<>();
        if (this.mZipFile == null) {
            return bmpList;
        }
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inScreenDensity = res != null ? DisplayMetricsEx.getNoncompatDensityDpi(res.getDisplayMetrics()) : DisplayMetrics.DENSITY_DEVICE_STABLE;
        try {
            Enumeration<? extends ZipEntry> enumeration = this.mZipFile.entries();
            while (enumeration.hasMoreElements()) {
                ZipEntry zipEntry = (ZipEntry) enumeration.nextElement();
                String name = zipEntry.getName();
                int indexfile = name.indexOf(filePattern);
                int indexofpng = name.indexOf(".png");
                if (indexfile == 0 && indexofpng > 0) {
                    InputStream is = this.mZipFile.getInputStream(zipEntry);
                    Bitmap bmp = BitmapFactory.decodeStream(is, null, opts);
                    closeInputStream(is);
                    if (bmp != null) {
                        bmp.setDensity(res != null ? res.getDisplayMetrics().densityDpi : DisplayMetrics.DENSITY_DEVICE_STABLE);
                        bmpList.add(bmp);
                    }
                }
            }
            return bmpList;
        } catch (RuntimeException e) {
            closeInputStream(null);
            bmpList.clear();
            return bmpList;
        } catch (Exception e2) {
            closeInputStream(null);
            bmpList.clear();
            return bmpList;
        }
    }

    public synchronized Drawable getDrawableEntry(Resources res, TypedValue value, String fileName, BitmapFactory.Options opts) {
        if (this.mZipFile == null) {
            return null;
        }
        Drawable dr = null;
        InputStream is = null;
        try {
            ZipEntry entry = this.mZipFile.getEntry(fileName);
            if (entry != null) {
                try {
                    is = this.mZipFile.getInputStream(entry);
                    dr = Drawable.createFromResourceStream(res, value, is, fileName, opts);
                } catch (Exception e) {
                    try {
                        closeZipFile();
                        Log.e(TAG, "getDrawableEntry occur exception fileName = " + fileName);
                        closeInputStream(is);
                        return dr;
                    } catch (Throwable th) {
                        th = th;
                        closeInputStream(is);
                        throw th;
                    }
                } catch (Throwable th2) {
                    th = th2;
                    closeInputStream(is);
                    throw th;
                }
            }
            closeInputStream(is);
        } catch (Exception e2) {
            closeZipFile();
            Log.e(TAG, "getDrawableEntry occur exception fileName = " + fileName);
            closeInputStream(is);
            return dr;
        }
        return dr;
    }

    public synchronized void initResDirInfo() {
        if (!(this.mZipFile == null || this.mInited)) {
            for (int i = 0; i < this.mZipResDir.length; i++) {
                try {
                    Iterator it = getZipResDirMap(i).entrySet().iterator();
                    while (true) {
                        if (!it.hasNext()) {
                            break;
                        }
                        Map.Entry<String, Integer> mapEntry = it.next();
                        if (this.mZipFile.getEntry(mapEntry.getKey().toString()) != null) {
                            this.mZipResDir[i].mDir = mapEntry.getKey().toString();
                            this.mZipResDir[i].mDensity = mapEntry.getValue().intValue();
                            break;
                        }
                    }
                } catch (Exception e) {
                    Log.d(TAG, "initResDirInfo Exception");
                }
            }
            this.mInited = true;
            return;
        }
        return;
    }

    private HashMap<String, Integer> getZipResDirMap(int index) {
        HashMap<String, Integer> map = new HashMap<>();
        if (index == 0) {
            map.put("res/drawable-xxhdpi", 480);
            map.put("res/drawable-sw360dp-xxhdpi", 480);
        } else if (index == 1) {
            map.put("res/drawable-land-xxhdpi", 480);
            map.put("res/drawable-sw360dp-land-xxhdpi", 480);
        } else if (index == 2) {
            map.put("framework-res/res/drawable-xxhdpi", 480);
        } else if (index == 3) {
            map.put("framework-res/res/drawable-land-xxhdpi", 480);
        } else if (index == 4) {
            map.put("framework-res-hwext/res/drawable-xxhdpi", 480);
        } else if (index == 5) {
            map.put("framework-res-hwext/res/drawable-land-xxhdpi", 480);
        }
        return map;
    }

    public int getDrawableDensity(int index) {
        ZipResDir[] zipResDirArr = this.mZipResDir;
        if (index >= zipResDirArr.length) {
            return -1;
        }
        return zipResDirArr[index].mDensity;
    }

    public String getDrawableDir(int index) {
        ZipResDir[] zipResDirArr = this.mZipResDir;
        if (index >= zipResDirArr.length) {
            return null;
        }
        return zipResDirArr[index].mDir;
    }

    public synchronized InputStream getInputStreamEntry(String fileName) {
        if (this.mZipFile == null) {
            return null;
        }
        InputStream is = null;
        try {
            ZipEntry entry = this.mZipFile.getEntry(fileName);
            if (entry != null) {
                is = this.mZipFile.getInputStream(entry);
            }
            return is;
        } catch (Exception e) {
            return null;
        }
    }

    public synchronized boolean checkIconEntry(String fileName) {
        if (this.mZipFile == null) {
            Log.w(TAG, "check bitmap entry from zip file failed");
            return false;
        }
        try {
            if (this.mZipFile.getEntry(fileName) != null) {
                return true;
            }
            return false;
        } catch (IllegalStateException e) {
            Log.e(TAG, "checkIconEntry(res,value,filename) occur exception");
            return false;
        }
    }
}
