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
    public static final int DEFAULT_DENSITY_VALUE = -1;
    private static final String ICONS = "icons";
    private static final boolean IS_DEBUG = false;
    private static final boolean IS_DEBUG_ICON = false;
    public static final int RES_INDEX_DEFAULT = 0;
    public static final int RES_INDEX_FRW = 2;
    public static final int RES_INDEX_HW_FRW = 4;
    public static final int RES_INDEX_LAND = 1;
    public static final int RES_INDEX_LAND_FRW = 3;
    public static final int RES_INDEX_LAND_HW_FRW = 5;
    private static final String TAG = "ZipFileCache";
    private static final int TRY_TIMES = 3;
    private static final ConcurrentHashMap<String, ZipFileCache> ZIP_FILE_CACHE_MAPS = new ConcurrentHashMap<>();
    private boolean mIsFileNotExist = false;
    private boolean mIsInitialized = false;
    private String mPath;
    private String mZip;
    private ZipFile mZipFile;
    private ZipResDir[] mZipResDir = {new ZipResDir(-1, null), new ZipResDir(-1, null), new ZipResDir(-1, null), new ZipResDir(-1, null), new ZipResDir(-1, null), new ZipResDir(-1, null)};

    private static class ZipResDir {
        private int mDensity = -1;
        private String mDir = BuildConfig.FLAVOR;

        ZipResDir(int density, String dir) {
            this.mDensity = density;
            this.mDir = dir;
        }

        public int getDensity() {
            return this.mDensity;
        }

        public String getDir() {
            return this.mDir;
        }

        public void setDir(String newDir) {
            this.mDir = newDir;
        }

        public void setDensity(int newDensity) {
            this.mDensity = newDensity;
        }
    }

    private ZipFileCache(String path, String zip) {
        this.mPath = path;
        this.mZip = zip;
        if (!openZipFile() && ICONS.equals(zip)) {
            Log.w(TAG, "init icons failed when open zip file. mPath=" + this.mPath + ",mZip=" + this.mZip + ",isFileNotExist=" + this.mIsFileNotExist);
        }
    }

    public static ZipFileCache getAndCheckCachedZipFile(String path, String zip) {
        ZipFileCache zipFileCache;
        ZipFileCache oldValue;
        String key = path + "/" + zip;
        synchronized (ZipFileCache.class) {
            zipFileCache = ZIP_FILE_CACHE_MAPS.get(key);
            if (zipFileCache == null) {
                ZipFileCache zipFileCache2 = new ZipFileCache(path, zip);
                if (zipFileCache2.mZipFile != null) {
                    ZipFileCache oldValue2 = ZIP_FILE_CACHE_MAPS.putIfAbsent(key, zipFileCache2);
                    if (oldValue2 != null) {
                        return oldValue2;
                    }
                    return zipFileCache2;
                } else if (!zipFileCache2.mIsFileNotExist || (oldValue = ZIP_FILE_CACHE_MAPS.putIfAbsent(key, zipFileCache2)) == null) {
                    return null;
                } else {
                    return oldValue;
                }
            }
        }
        if (zipFileCache.mIsFileNotExist) {
            return null;
        }
        return zipFileCache;
    }

    public static synchronized void clear() {
        synchronized (ZipFileCache.class) {
            for (ZipFileCache zip : ZIP_FILE_CACHE_MAPS.values()) {
                if (zip != null) {
                    zip.closeZipFile();
                }
            }
            ZIP_FILE_CACHE_MAPS.clear();
        }
    }

    public synchronized Bitmap getBitmapEntry(ResourcesImplEx impl, String fileName) {
        Throwable th;
        if (this.mZipFile == null || impl == null) {
            Log.w(TAG, "Null impl or get bitmap entry from zip file failed fileName=" + fileName);
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
                } catch (Exception e2) {
                    try {
                        closeZipFile();
                        Log.e(TAG, "getBitmapEntry occur exception fileName = " + fileName);
                        closeInputStream(is);
                        return bmp;
                    } catch (Throwable th2) {
                        th = th2;
                        closeInputStream(is);
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    closeInputStream(is);
                    throw th;
                }
                if (bmp == null) {
                    closeInputStream(is);
                    return null;
                }
                bmp.setDensity(impl.getHwResourcesImpl().hwGetDisplayMetrics().densityDpi);
            }
            closeInputStream(is);
        } catch (IOException e3) {
            closeZipFile();
            Log.e(TAG, "getBitmapEntry occur exception fileName = " + fileName);
            closeInputStream(null);
        } catch (Exception e4) {
            closeZipFile();
            Log.e(TAG, "getBitmapEntry occur exception fileName = " + fileName);
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
        Rect newPadding = padding == null ? new Rect() : padding;
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inScreenDensity = res != null ? DisplayMetricsEx.getNoncompatDensityDpi(res.getDisplayMetrics()) : DisplayMetrics.DENSITY_DEVICE_STABLE;
        try {
            ZipEntry entry = this.mZipFile.getEntry(fileName);
            if (!(entry == null || (bmp = BitmapFactory.decodeResourceStream(res, value, (is = this.mZipFile.getInputStream(entry)), newPadding, opts)) == null)) {
                bmp.setDensity(res != null ? res.getDisplayMetrics().densityDpi : DisplayMetrics.DENSITY_DEVICE_STABLE);
            }
            return bmp;
        } catch (IOException e) {
            Log.e(TAG, "getBitmapEntry(res,value,filename) occur exception fileName = " + fileName);
            return null;
        } catch (Exception e2) {
            Log.e(TAG, "getBitmapEntry(res,value,filename) occur exception fileName = " + fileName);
            return null;
        } finally {
            closeInputStream(is);
        }
    }

    public synchronized Bitmap getBitmapEntry(Resources res, TypedValue value, String fileName) {
        return getBitmapEntry(res, value, fileName, null);
    }

    public synchronized boolean isUseAdaptiveIcon(ResourcesImplEx impl, String fileName) {
        if (this.mZipFile == null || impl == null) {
            Log.w(TAG, "Null impl or get bitmap entry from zip file failed fileName= " + fileName);
            return false;
        }
        try {
            ZipEntry entry = this.mZipFile.getEntry(fileName);
            if (entry == null || (!entry.isDirectory() && !entry.getName().endsWith(File.separator))) {
                return false;
            }
            return true;
        } catch (IllegalStateException e) {
            Log.e(TAG, "checkIconEntry as adaptiveIcon occur exception.");
            return false;
        }
    }

    public synchronized ArrayList<Bitmap> getBitmapList(ResourcesImplEx impl, String filePattern) {
        Bitmap bmp;
        ArrayList<Bitmap> bmpList = new ArrayList<>();
        if (this.mZipFile == null) {
            return bmpList;
        }
        InputStream is = null;
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inScreenDensity = impl != null ? DisplayMetricsEx.getNoncompatDensityDpi(impl.getHwResourcesImpl().hwGetDisplayMetrics()) : DisplayMetrics.DENSITY_DEVICE_STABLE;
        try {
            Enumeration<? extends ZipEntry> enumeration = this.mZipFile.entries();
            while (enumeration.hasMoreElements()) {
                ZipEntry zipEntry = (ZipEntry) enumeration.nextElement();
                String name = zipEntry.getName();
                int indexOfFile = name.indexOf(filePattern);
                int indexOfPng = name.indexOf(".png");
                if (indexOfFile == 0 && indexOfPng > 0 && (bmp = BitmapFactory.decodeStream((is = this.mZipFile.getInputStream(zipEntry)), null, opts)) != null) {
                    bmp.setDensity(impl != null ? impl.getHwResourcesImpl().hwGetDisplayMetrics().densityDpi : DisplayMetrics.DENSITY_DEVICE_STABLE);
                    bmpList.add(bmp);
                }
            }
            return bmpList;
        } catch (IOException e) {
            bmpList.clear();
            return bmpList;
        } catch (Exception e2) {
            bmpList.clear();
            return bmpList;
        } finally {
            closeInputStream(is);
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
                int indexOfFile = name.indexOf(filePattern);
                int indexOfPng = name.indexOf(".png");
                if (indexOfFile == 0 && indexOfPng > 0) {
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
        } catch (IOException e) {
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
        Throwable th;
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
                } catch (RuntimeException e) {
                    closeZipFile();
                    Log.e(TAG, "getDrawableEntry occur exception fileName = " + fileName);
                    closeInputStream(is);
                    return dr;
                } catch (Exception e2) {
                    try {
                        closeZipFile();
                        Log.e(TAG, "getDrawableEntry occur exception fileName = " + fileName);
                        closeInputStream(is);
                        return dr;
                    } catch (Throwable th2) {
                        th = th2;
                        closeInputStream(is);
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    closeInputStream(is);
                    throw th;
                }
            }
            closeInputStream(is);
        } catch (RuntimeException e3) {
            closeZipFile();
            Log.e(TAG, "getDrawableEntry occur exception fileName = " + fileName);
            closeInputStream(is);
            return dr;
        } catch (Exception e4) {
            closeZipFile();
            Log.e(TAG, "getDrawableEntry occur exception fileName = " + fileName);
            closeInputStream(is);
            return dr;
        }
        return dr;
    }

    public synchronized void initResDirInfo() {
        Throwable th;
        String str;
        String str2;
        if (!(this.mZipFile == null || this.mIsInitialized)) {
            for (int i = 0; i < this.mZipResDir.length; i++) {
                try {
                    try {
                        Iterator it = getZipResDirMap(i).entrySet().iterator();
                        while (true) {
                            if (!it.hasNext()) {
                                break;
                            }
                            Map.Entry<String, Integer> mapEntry = it.next();
                            if (this.mZipFile.getEntry(mapEntry.getKey().toString()) != null) {
                                this.mZipResDir[i].setDir(mapEntry.getKey().toString());
                                this.mZipResDir[i].setDensity(mapEntry.getValue().intValue());
                                break;
                            }
                        }
                    } catch (IllegalArgumentException e) {
                        Log.d(TAG, "initResDirInfo Exception");
                        str2 = TAG;
                        str = "initResDirInfo Completed!";
                        Log.d(str2, str);
                    } catch (Exception e2) {
                        try {
                            Log.d(TAG, "initResDirInfo Exception");
                            str2 = TAG;
                            str = "initResDirInfo Completed!";
                            Log.d(str2, str);
                        } catch (Throwable th2) {
                            th = th2;
                            Log.d(TAG, "initResDirInfo Completed!");
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        Log.d(TAG, "initResDirInfo Completed!");
                        throw th;
                    }
                } catch (IllegalArgumentException e3) {
                    Log.d(TAG, "initResDirInfo Exception");
                    str2 = TAG;
                    str = "initResDirInfo Completed!";
                    Log.d(str2, str);
                } catch (Exception e4) {
                    Log.d(TAG, "initResDirInfo Exception");
                    str2 = TAG;
                    str = "initResDirInfo Completed!";
                    Log.d(str2, str);
                }
            }
            this.mIsInitialized = true;
            Log.d(TAG, "initResDirInfo Completed!");
        }
    }

    public int getDrawableDensity(int index) {
        ZipResDir[] zipResDirArr = this.mZipResDir;
        if (index >= zipResDirArr.length || index < 0) {
            return -1;
        }
        return zipResDirArr[index].getDensity();
    }

    public String getDrawableDir(int index) {
        ZipResDir[] zipResDirArr = this.mZipResDir;
        if (index >= zipResDirArr.length || index < 0) {
            return null;
        }
        return zipResDirArr[index].getDir();
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
        } catch (IOException e) {
            Log.e(TAG, "Exceptions occurred when opening file");
            return null;
        } catch (Exception e2) {
            Log.e(TAG, "Exceptions occurred when opening file");
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
            if (this.mZipFile.getEntry(fileName.substring(0, fileName.length() - 4)) != null) {
                return true;
            }
            return false;
        } catch (IllegalStateException e) {
            Log.e(TAG, "checkIconEntry(res,value,filename) occur exception");
            return false;
        }
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

    private synchronized boolean openZipFile() {
        try {
            File file = new File(this.mPath, this.mZip);
            if (!file.exists()) {
                this.mIsFileNotExist = true;
                return false;
            }
            this.mZipFile = new ZipFile(file, 1);
            this.mIsInitialized = false;
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
                Log.e(TAG, "Close ZipFile Error!");
            }
            this.mZipFile = null;
        }
    }

    private void closeInputStream(InputStream is) {
        if (is != null) {
            try {
                is.close();
            } catch (IOException e) {
                Log.e(TAG, "Close InputStream Error!");
            }
        }
    }

    private synchronized void setEmpty() {
        this.mPath = BuildConfig.FLAVOR;
        this.mZip = BuildConfig.FLAVOR;
        this.mZipFile = null;
        this.mIsFileNotExist = false;
    }
}
