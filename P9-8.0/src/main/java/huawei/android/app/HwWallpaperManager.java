package huawei.android.app;

import android.app.AppGlobals;
import android.app.HwCustHwWallpaperManager;
import android.app.IWallpaperManager;
import android.app.WallpaperInfo;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.hwtheme.HwThemeManager;
import android.os.FreezeScreenScene;
import android.os.Handler;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import huawei.android.hwpicaveragenoises.HwPicAverageNoises;
import huawei.cust.HwCustUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

public class HwWallpaperManager extends WallpaperManager {
    private static boolean DEBUG = false;
    private static String TAG = "HwWallpaperManager";
    private static final Object sSync = new Object[0];
    public static final int x_land = 2;
    public static final int x_port = 0;
    public static final int y_land = 3;
    public static final int y_port = 1;
    private HwCustHwWallpaperManager mCustHwWallpaperManager = ((HwCustHwWallpaperManager) HwCustUtils.createObj(HwCustHwWallpaperManager.class, new Object[0]));

    public HwWallpaperManager(Context context, Handler handler) {
        super(context, handler);
    }

    public Bitmap getBlurBitmap(Rect rect) {
        return getBlurBitmap(rect, false);
    }

    public Bitmap getBitmap() {
        return super.getBitmap();
    }

    private Bitmap getBlurBitmap(Rect rect, boolean exclusiveIntersect) {
        Rect rect2 = new Rect(rect.left / 4, rect.top / 4, rect.right / 4, rect.bottom / 4);
        if (rect.width() > 0 && rect.width() < 4) {
            rect2.right = rect2.left + 1;
        }
        if (rect.height() > 0 && rect.height() < 4) {
            rect2.bottom = rect2.top + 1;
        }
        Rect thumbnailRect = cutForLiveWallpaper(rect2);
        Bitmap bp = peekBlurWallpaperBitmap(thumbnailRect);
        if (bp == null) {
            Log.w(TAG, "getBlurBitmap return default cause peek null blur wallpaper.");
            return BitmapFactory.decodeResource(getContext().getResources(), 33751582);
        }
        rect2 = new Rect(0, 0, bp.getWidth(), bp.getHeight());
        if (DEBUG) {
            Log.d(TAG, "getBlurBitmap wall=" + rect2 + ",rect=" + rect + ",thumbnailRect=" + thumbnailRect);
        }
        if (rect2.intersect(thumbnailRect)) {
            int height = rect2.height();
            int width = rect2.width();
            int[] inPixels = new int[(width * height)];
            try {
                Bitmap bitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
                if (bitmap == null) {
                    Log.e(TAG, "getBlurBitmap() bitmap = null");
                    return BitmapFactory.decodeResource(getContext().getResources(), 33751582);
                }
                bp.getPixels(inPixels, 0, width, rect2.left, rect2.top, width, height);
                bitmap.setPixels(inPixels, 0, width, 0, 0, width, height);
                if (HwPicAverageNoises.isAverageNoiseSupported()) {
                    return new HwPicAverageNoises().addNoiseWithBlackBoard(bitmap, 1275068416);
                }
                return bitmap;
            } catch (OutOfMemoryError e) {
                Log.e(TAG, "HwWallpaperManager can't create blur wallpaper for out of memory.");
                return BitmapFactory.decodeResource(getContext().getResources(), 33751582);
            }
        }
        Log.w(TAG, "getBlurBitmap return default cause rect intersect fail.");
        return BitmapFactory.decodeResource(getContext().getResources(), 33751582);
    }

    public void setCallback(Object callback) {
        int count = mCallbacks.size();
        int i = 0;
        while (i < count) {
            if (((WeakReference) mCallbacks.get(i)).get() != callback) {
                i++;
            } else {
                return;
            }
        }
        mCallbacks.add(new WeakReference(callback));
    }

    public void setStream(InputStream data) throws IOException {
        setStream(data, 3);
    }

    public void setStream(InputStream data, int flag) throws IOException {
        setWallpaperStartingPoints(new int[]{-1, -1, -1, -1});
        super.setStream(data, null, true, flag);
    }

    protected Bitmap createDefaultWallpaperBitmap(Bitmap bm) {
        if (bm == null) {
            return null;
        }
        Display display = ((WindowManager) getContext().getSystemService(FreezeScreenScene.WINDOW_PARAM)).getDefaultDisplay();
        Point size = new Point();
        display.getRealSize(size);
        int max = size.x > size.y ? size.x : size.y;
        int min = size.x < size.y ? size.x : size.y;
        int width = bm.getWidth();
        int height = bm.getHeight();
        if (this.mCustHwWallpaperManager != null && this.mCustHwWallpaperManager.isScrollWallpaper(max, min, width, height)) {
            Log.w(TAG, "Default wallpaper is scrollable: width=" + width + ", height=" + height);
            return bm;
        } else if ((height == max || width == max) && (width == min * 2 || width == max)) {
            int[] offsets = new int[]{-1, -1, -1, -1};
            if (TextUtils.isEmpty(SystemProperties.get("ro.config.wallpaper")) && (new File("/data/cust/wallpaper/", "wallpaper1.jpg").exists() ^ 1) != 0) {
                String filePath = "/data/skin/description.xml";
                offsets = parseWallpaperOffsets("/data/skin/description.xml");
            }
            if (offsets[0] == -1) {
                offsets[0] = (width - min) / 2;
            } else if (offsets[0] < -1) {
                offsets[0] = 0;
            } else if (offsets[0] > width - min) {
                offsets[0] = width - min;
            }
            if (offsets[2] == -1) {
                offsets[2] = (width - max) / 2;
            } else if (offsets[2] < -1) {
                offsets[2] = 0;
            } else if (offsets[2] > width - max) {
                offsets[2] = width - max;
            }
            if (offsets[2] > offsets[0]) {
                offsets[2] = offsets[0];
            } else {
                if (offsets[2] < offsets[0] - (max - min)) {
                    offsets[2] = offsets[0] - (max - min);
                }
            }
            try {
                Bitmap newbm = Bitmap.createBitmap(bm, offsets[2], 0, max, max);
                offsets[0] = offsets[0] - offsets[2];
                offsets[1] = 0;
                offsets[2] = 0;
                if (offsets[3] == -1) {
                    offsets[3] = (max - min) / 2;
                } else if (offsets[3] < -1) {
                    offsets[3] = 0;
                } else if (offsets[3] > max - min) {
                    offsets[3] = max - min;
                }
                IWallpaperManager mService = getIWallpaperManager();
                if (mService != null) {
                    try {
                        mService.setCurrOffsets(offsets);
                    } catch (RemoteException e) {
                    } catch (SecurityException e2) {
                        Log.e(TAG, "Caller has no permission to set current wallpaper offsets!");
                    }
                }
                if (!(bm == null || bm == newbm)) {
                    if ((bm.isRecycled() ^ 1) != 0) {
                        bm.recycle();
                    }
                }
                return newbm;
            } catch (IllegalArgumentException e3) {
                Log.w(TAG, "Create default square bitmap error, run generateBitmap! ");
                return HwThemeManager.generateBitmap(getContext(), bm, min, max);
            }
        } else {
            Log.w(TAG, "Irregular default bitmap: width=" + width + ", height=" + height);
            return HwThemeManager.generateBitmap(getContext(), bm, min, max);
        }
    }

    private void setWallpaperStartingPoints(int[] offsets) {
        IWallpaperManager mService = getIWallpaperManager();
        if (mService != null) {
            try {
                mService.setNextOffsets(offsets);
            } catch (RemoteException e) {
            }
        }
    }

    public int[] getWallpaperStartingPoints() {
        int[] defaultOffsets = new int[]{-1, -1, -1, -1};
        int[] offsets = null;
        IWallpaperManager mService = getIWallpaperManager();
        if (mService != null) {
            try {
                offsets = mService.getCurrOffsets();
            } catch (RemoteException e) {
            }
        }
        if (offsets == null) {
            return defaultOffsets;
        }
        return offsets;
    }

    public void setBitmap(Bitmap bitmap) throws IOException {
        setWallpaperStartingPoints(new int[]{-1, -1, -1, -1});
        super.setBitmap(bitmap);
    }

    public void setBitmapWithOffsets(Bitmap bitmap, int[] offsets) throws IOException {
        setWallpaperStartingPoints(offsets);
        super.setBitmap(bitmap);
    }

    public void setStreamWithOffsets(InputStream data, int[] offsets) throws IOException {
        setStreamWithOffsets(data, offsets, 3);
    }

    public void setStreamWithOffsets(InputStream data, int[] offsets, int flag) throws IOException {
        setWallpaperStartingPoints(offsets);
        super.setStream(data, null, true, flag);
    }

    /* JADX WARNING: Removed duplicated region for block: B:78:0x0137 A:{SYNTHETIC, Splitter: B:78:0x0137} */
    /* JADX WARNING: Removed duplicated region for block: B:27:0x0073 A:{SYNTHETIC, Splitter: B:27:0x0073} */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x00d9 A:{SYNTHETIC, Splitter: B:40:0x00d9} */
    /* JADX WARNING: Removed duplicated region for block: B:53:0x00ff A:{SYNTHETIC, Splitter: B:53:0x00ff} */
    /* JADX WARNING: Removed duplicated region for block: B:70:0x0123 A:{SYNTHETIC, Splitter: B:70:0x0123} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int[] parseWallpaperOffsets(String srcFile) {
        FileNotFoundException e;
        Throwable th;
        XmlPullParserException e2;
        IOException e3;
        Exception e4;
        int[] offsets = new int[]{-1, -1, -1, -1};
        if (srcFile == null) {
            return offsets;
        }
        File file = new File(srcFile);
        if (!file.exists()) {
            return offsets;
        }
        FileInputStream is = null;
        try {
            FileInputStream is2 = new FileInputStream(file);
            try {
                XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
                parser.setInput(is2, CharacterSets.DEFAULT_CHARSET_NAME);
                for (int eventType = parser.getEventType(); eventType != 1; eventType = parser.next()) {
                    if (eventType == 2) {
                        String tagName = parser.getName();
                        if (tagName.equals("WP_Xoffset_port")) {
                            offsets[0] = Integer.parseInt(parser.nextText());
                        } else if (tagName.equals("WP_Yoffset_port")) {
                            offsets[1] = Integer.parseInt(parser.nextText());
                        } else if (tagName.equals("WP_Xoffset_land")) {
                            offsets[2] = Integer.parseInt(parser.nextText());
                        } else if (tagName.equals("WP_Yoffset_land")) {
                            offsets[3] = Integer.parseInt(parser.nextText());
                        }
                    } else if (eventType == 3) {
                    }
                }
                if (is2 != null) {
                    try {
                        is2.close();
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                    }
                }
            } catch (FileNotFoundException e5) {
                e = e5;
                is = is2;
                try {
                    e.printStackTrace();
                    if (is != null) {
                        try {
                            is.close();
                        } catch (IOException ioe2) {
                            ioe2.printStackTrace();
                        }
                    }
                    Log.d(TAG, "Parse offsets:" + offsets[0] + "," + offsets[1] + "," + offsets[2] + "," + offsets[3]);
                    return offsets;
                } catch (Throwable th2) {
                    th = th2;
                    if (is != null) {
                        try {
                            is.close();
                        } catch (IOException ioe22) {
                            ioe22.printStackTrace();
                        }
                    }
                    throw th;
                }
            } catch (XmlPullParserException e6) {
                e2 = e6;
                is = is2;
                e2.printStackTrace();
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException ioe222) {
                        ioe222.printStackTrace();
                    }
                }
                Log.d(TAG, "Parse offsets:" + offsets[0] + "," + offsets[1] + "," + offsets[2] + "," + offsets[3]);
                return offsets;
            } catch (IOException e7) {
                e3 = e7;
                is = is2;
                e3.printStackTrace();
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException ioe2222) {
                        ioe2222.printStackTrace();
                    }
                }
                Log.d(TAG, "Parse offsets:" + offsets[0] + "," + offsets[1] + "," + offsets[2] + "," + offsets[3]);
                return offsets;
            } catch (Exception e8) {
                e4 = e8;
                is = is2;
                e4.printStackTrace();
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException ioe22222) {
                        ioe22222.printStackTrace();
                    }
                }
                Log.d(TAG, "Parse offsets:" + offsets[0] + "," + offsets[1] + "," + offsets[2] + "," + offsets[3]);
                return offsets;
            } catch (Throwable th3) {
                th = th3;
                is = is2;
                if (is != null) {
                }
                throw th;
            }
        } catch (FileNotFoundException e9) {
            e = e9;
            e.printStackTrace();
            if (is != null) {
            }
            Log.d(TAG, "Parse offsets:" + offsets[0] + "," + offsets[1] + "," + offsets[2] + "," + offsets[3]);
            return offsets;
        } catch (XmlPullParserException e10) {
            e2 = e10;
            e2.printStackTrace();
            if (is != null) {
            }
            Log.d(TAG, "Parse offsets:" + offsets[0] + "," + offsets[1] + "," + offsets[2] + "," + offsets[3]);
            return offsets;
        } catch (IOException e11) {
            e3 = e11;
            e3.printStackTrace();
            if (is != null) {
            }
            Log.d(TAG, "Parse offsets:" + offsets[0] + "," + offsets[1] + "," + offsets[2] + "," + offsets[3]);
            return offsets;
        } catch (Exception e12) {
            e4 = e12;
            e4.printStackTrace();
            if (is != null) {
            }
            Log.d(TAG, "Parse offsets:" + offsets[0] + "," + offsets[1] + "," + offsets[2] + "," + offsets[3]);
            return offsets;
        }
        Log.d(TAG, "Parse offsets:" + offsets[0] + "," + offsets[1] + "," + offsets[2] + "," + offsets[3]);
        return offsets;
    }

    private Rect cutForLiveWallpaper(Rect rect) {
        WallpaperInfo wallpaperInfo = getWallpaperInfo();
        int mCurrentUserId = 0;
        ApplicationInfo ai = null;
        if (wallpaperInfo != null) {
            String packageName = wallpaperInfo.getPackageName();
            IWallpaperManager mService = getIWallpaperManager();
            if (mService != null) {
                try {
                    mCurrentUserId = mService.getWallpaperUserId();
                } catch (RemoteException e) {
                    Log.e(TAG, "IWallpaperManager or IPackageManager RemoteException error");
                }
            }
            ai = AppGlobals.getPackageManager().getApplicationInfo(packageName, 0, mCurrentUserId);
            if (((ai == null ? 0 : ai.flags) & 1) == 0 && rect.left == 0) {
                DisplayMetrics dm = new DisplayMetrics();
                ((WindowManager) getContext().getSystemService(FreezeScreenScene.WINDOW_PARAM)).getDefaultDisplay().getMetrics(dm);
                int disWidth = (dm.widthPixels > dm.heightPixels ? dm.heightPixels : dm.widthPixels) / 4;
                rect.right = rect.right > disWidth ? disWidth : rect.right;
                rect.right = rect.left + 2 > rect.right ? rect.left + 2 : rect.right;
                if (rect.width() > 60) {
                    rect.left += 30;
                    rect.right -= 30;
                } else {
                    int dw = disWidth / 2;
                    rect.left = dw - 2;
                    rect.right = dw + 2;
                }
            }
        }
        return rect;
    }
}
