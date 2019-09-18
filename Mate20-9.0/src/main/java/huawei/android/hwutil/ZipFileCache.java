package huawei.android.hwutil;

import android.content.res.Resources;
import android.content.res.ResourcesImpl;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
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
    private String HWT_PATH_SKIN = "/data/skin";
    private String HWT_PATH_TEMP_SKIN = "/data/skin.tmp";
    private boolean mFileNotExist = false;
    private boolean mInited = false;
    private String mPath;
    private String mZip;
    private ZipFile mZipFile;
    private ZipResDir[] mZipResDir = {new ZipResDir(-1, null), new ZipResDir(-1, null), new ZipResDir(-1, null), new ZipResDir(-1, null), new ZipResDir(-1, null), new ZipResDir(-1, null)};

    private static class ZipResDir {
        public int mDensity = -1;
        public String mDir = "";

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

    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0041, code lost:
        return null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0054, code lost:
        if (r1.mFileNotExist == false) goto L_0x0057;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0056, code lost:
        return null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0057, code lost:
        return r1;
     */
    public static ZipFileCache getAndCheckCachedZipFile(String path, String zip) {
        String key = path + "/" + zip;
        synchronized (ZipFileCache.class) {
            ZipFileCache zipFileCache = sZipFileCacheMaps.get(key);
            ZipFileCache zipFileCache2 = zipFileCache;
            if (zipFileCache == null) {
                ZipFileCache zipFileCache3 = new ZipFileCache(path, zip);
                if (zipFileCache3.mZipFile != null) {
                    ZipFileCache oldValue = sZipFileCacheMaps.putIfAbsent(key, zipFileCache3);
                    if (oldValue != null) {
                        return oldValue;
                    }
                    return zipFileCache3;
                } else if (zipFileCache3.mFileNotExist) {
                    ZipFileCache oldValue2 = sZipFileCacheMaps.putIfAbsent(key, zipFileCache3);
                    if (oldValue2 != null) {
                        return oldValue2;
                    }
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
        this.mPath = "";
        this.mZip = "";
        this.mZipFile = null;
        this.mFileNotExist = false;
    }

    /* JADX INFO: finally extract failed */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x00a0, code lost:
        return r2;
     */
    public synchronized Bitmap getBitmapEntry(ResourcesImpl impl, String fileName) {
        InputStream is = null;
        if (this.mZipFile != null) {
            int reTryCount = 3;
            Bitmap bmp = null;
            while (true) {
                if (reTryCount <= 0) {
                    break;
                }
                reTryCount--;
                try {
                    ZipEntry entry = this.mZipFile.getEntry(fileName);
                    if (entry != null) {
                        is = this.mZipFile.getInputStream(entry);
                        bmp = BitmapFactory.decodeStream(is);
                        try {
                            is.available();
                            if (bmp != null) {
                                bmp.setDensity(impl.getHwResourcesImpl().hwGetDisplayMetrics().densityDpi);
                                break;
                            }
                        } catch (IOException e) {
                            Log.e(TAG, "ZipFileCache#getBitmapEntry e = " + e.getMessage());
                        }
                    }
                } catch (Exception e2) {
                    try {
                        closeZipFile();
                        openZipFile();
                        Log.e(TAG, "getBitmapEntry occur exception fileName = " + fileName + " e = " + e2.getMessage());
                        closeInputStream(is);
                    } catch (Throwable th) {
                        closeInputStream(is);
                        throw th;
                    }
                }
            }
            closeInputStream(is);
            break;
        } else {
            Log.w(TAG, "Get bitmap entry from zip file failed fileName=" + fileName);
            return null;
        }
    }

    public synchronized Bitmap getBitmapEntry(Resources res, String fileName) {
        ResourcesImpl impl = res.getImpl();
        if (impl != null) {
            return getBitmapEntry(impl, fileName);
        }
        Log.w(TAG, "resourcesImpl is null");
        return null;
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
        opts.inScreenDensity = res != null ? res.getDisplayMetrics().noncompatDensityDpi : DisplayMetrics.DENSITY_DEVICE;
        try {
            ZipEntry entry = this.mZipFile.getEntry(fileName);
            if (entry != null) {
                is = this.mZipFile.getInputStream(entry);
                bmp = BitmapFactory.decodeResourceStream(res, value, is, padding, opts);
                if (bmp != null) {
                    bmp.setDensity(res != null ? res.getDisplayMetrics().densityDpi : DisplayMetrics.DENSITY_DEVICE);
                }
            }
            closeInputStream(is);
            return bmp;
        } catch (Exception e) {
            try {
                Log.e(TAG, "getBitmapEntry(res,value,filename) occur exception fileName = " + fileName + " e = " + e.getMessage());
                return null;
            } finally {
                closeInputStream(is);
            }
        }
    }

    public synchronized Bitmap getBitmapEntry(Resources res, TypedValue value, String fileName) {
        return getBitmapEntry(res, value, fileName, null);
    }

    public synchronized ArrayList<Bitmap> getBitmapList(ResourcesImpl impl, String filePattern) {
        ArrayList<Bitmap> bmpList = new ArrayList<>();
        if (this.mZipFile == null) {
            return bmpList;
        }
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inScreenDensity = impl != null ? impl.getHwResourcesImpl().hwGetDisplayMetrics().noncompatDensityDpi : DisplayMetrics.DENSITY_DEVICE;
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
                        bmp.setDensity(impl != null ? impl.getHwResourcesImpl().hwGetDisplayMetrics().densityDpi : DisplayMetrics.DENSITY_DEVICE);
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
        opts.inScreenDensity = res != null ? res.getDisplayMetrics().noncompatDensityDpi : DisplayMetrics.DENSITY_DEVICE;
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
                        bmp.setDensity(res != null ? res.getDisplayMetrics().densityDpi : DisplayMetrics.DENSITY_DEVICE);
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

    /* JADX INFO: finally extract failed */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0015, code lost:
        if (r3 == null) goto L_0x0023;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0017, code lost:
        r1 = r7.mZipFile.getInputStream(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0022, code lost:
        r2 = android.graphics.drawable.Drawable.createFromResourceStream(r8, r9, r1, r10, r11);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:?, code lost:
        closeInputStream(r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x005b, code lost:
        return r2;
     */
    public synchronized Drawable getDrawableEntry(Resources res, TypedValue value, String fileName, BitmapFactory.Options opts) {
        InputStream is = null;
        if (this.mZipFile != null) {
            int reTryCount = 3;
            Drawable dr = null;
            while (true) {
                if (reTryCount <= 0) {
                    break;
                }
                reTryCount--;
                try {
                    ZipEntry entry = this.mZipFile.getEntry(fileName);
                    break;
                } catch (Exception e) {
                    try {
                        closeZipFile();
                        openZipFile();
                        Log.e(TAG, "getDrawableEntry occur exception fileName = " + fileName + " e = " + e.getMessage());
                        closeInputStream(null);
                    } catch (Throwable th) {
                        closeInputStream(null);
                        throw th;
                    }
                }
            }
        } else {
            return null;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:29:0x0086, code lost:
        return;
     */
    public synchronized void initResDirInfo() {
        if (this.mZipFile != null && !this.mInited) {
            int i = 0;
            while (i < this.mZipResDir.length) {
                try {
                    Iterator it = getZipResDirMap(i).entrySet().iterator();
                    while (true) {
                        if (!it.hasNext()) {
                            break;
                        }
                        Map.Entry mapEntry = it.next();
                        if (this.mZipFile.getEntry(mapEntry.getKey().toString()) != null) {
                            this.mZipResDir[i].mDir = mapEntry.getKey().toString();
                            this.mZipResDir[i].mDensity = ((Integer) mapEntry.getValue()).intValue();
                            break;
                        }
                    }
                    i++;
                } catch (Exception e) {
                    Log.d(TAG, "initResDirInfo Exception = " + e.getMessage());
                }
            }
            this.mInited = true;
        }
        return;
    }

    private HashMap<String, Integer> getZipResDirMap(int index) {
        HashMap<String, Integer> map = new HashMap<>();
        switch (index) {
            case 0:
                map.put("res/drawable-xxhdpi", 480);
                map.put("res/drawable-sw360dp-xxhdpi", 480);
                break;
            case 1:
                map.put("res/drawable-land-xxhdpi", 480);
                map.put("res/drawable-sw360dp-land-xxhdpi", 480);
                break;
            case 2:
                map.put("framework-res/res/drawable-xxhdpi", 480);
                break;
            case 3:
                map.put("framework-res/res/drawable-land-xxhdpi", 480);
                break;
            case 4:
                map.put("framework-res-hwext/res/drawable-xxhdpi", 480);
                break;
            case 5:
                map.put("framework-res-hwext/res/drawable-land-xxhdpi", 480);
                break;
        }
        return map;
    }

    public int getDrawableDensity(int index) {
        if (index >= this.mZipResDir.length) {
            return -1;
        }
        return this.mZipResDir[index].mDensity;
    }

    public String getDrawableDir(int index) {
        if (index >= this.mZipResDir.length) {
            return null;
        }
        return this.mZipResDir[index].mDir;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:14:0x001a, code lost:
        return r0;
     */
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
        } catch (Exception e) {
            return null;
        }
    }
}
