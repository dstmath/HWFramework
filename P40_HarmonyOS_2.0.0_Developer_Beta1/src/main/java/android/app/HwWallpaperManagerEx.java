package android.app;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.hwtheme.HwThemeManager;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import com.huawei.android.app.IWallpaperManagerEx;
import com.huawei.android.app.WallpaperManagerAdapter;
import com.huawei.android.content.pm.IPackageManagerEx;
import com.huawei.android.os.SystemPropertiesEx;
import huawei.android.hwpicaveragenoises.HwPicAverageNoises;
import huawei.cust.HwCustUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

public class HwWallpaperManagerEx implements IHwWallpaperManagerEx {
    private static final String TAG = "HwWallpaperManagerEx";
    public static final int x_land = 2;
    public static final int x_port = 0;
    public static final int y_land = 3;
    public static final int y_port = 1;
    private Context mContext;
    private HwCustHwWallpaperManager mCustHwWallpaperManager = ((HwCustHwWallpaperManager) HwCustUtils.createObj(HwCustHwWallpaperManager.class, new Object[0]));
    private IWallpaperManagerEx mService;
    private WallpaperManager mWallpaperManger;

    public HwWallpaperManagerEx(Context context, IWallpaperManagerEx service, WallpaperManager wm) {
        this.mContext = context;
        this.mService = service;
        this.mWallpaperManger = wm;
    }

    public void setWallpaperStartingPoints(int[] offsets) {
        try {
            this.mService.setNextOffsets(offsets);
        } catch (RemoteException e) {
        }
    }

    public Bitmap createDefaultWallpaperBitmap(Bitmap bm) {
        int[] offsets;
        if (bm == null) {
            return null;
        }
        Display display = ((WindowManager) this.mContext.getSystemService("window")).getDefaultDisplay();
        Point size = new Point();
        display.getRealSize(size);
        int max = size.x > size.y ? size.x : size.y;
        int min = size.x < size.y ? size.x : size.y;
        int width = bm.getWidth();
        int height = bm.getHeight();
        HwCustHwWallpaperManager hwCustHwWallpaperManager = this.mCustHwWallpaperManager;
        if (hwCustHwWallpaperManager != null && hwCustHwWallpaperManager.isScrollWallpaper(this.mContext)) {
            Bitmap scrollBitmap = this.mCustHwWallpaperManager.createScrollBitmap(width, height, bm);
            Log.i(TAG, "Default wallpaper is scrollable: width=" + scrollBitmap.getWidth() + ",height=" + scrollBitmap.getHeight());
            return scrollBitmap;
        } else if ((height == max || width == max) && (width == min * 2 || width == max)) {
            int[] offsets2 = {-1, -1, -1, -1};
            if (!TextUtils.isEmpty(SystemPropertiesEx.get("ro.config.wallpaper")) || new File("/data/cust/wallpaper/", "wallpaper1.jpg").exists()) {
                offsets = offsets2;
            } else {
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
            } else if (offsets[2] < offsets[0] - (max - min)) {
                offsets[2] = offsets[0] - (max - min);
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
                try {
                    this.mService.setCurrOffsets(offsets);
                } catch (RemoteException e) {
                } catch (SecurityException e2) {
                    Log.e(TAG, "Caller has no permission to set current wallpaper offsets!");
                }
                if (bm != newbm && !bm.isRecycled()) {
                    bm.recycle();
                }
                return newbm;
            } catch (IllegalArgumentException e3) {
                Log.w(TAG, "Create default square bitmap error, run generateBitmap! ");
                return HwThemeManager.generateBitmap(this.mContext, bm, min, max);
            }
        } else {
            Log.w(TAG, "Irregular default bitmap: width=" + width + ", height=" + height);
            return HwThemeManager.generateBitmap(this.mContext, bm, min, max);
        }
    }

    private int[] parseWallpaperOffsets(String srcFile) {
        int[] offsets = {-1, -1, -1, -1};
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
            XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
            parser.setInput(is2, "utf-8");
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
            try {
                is2.close();
            } catch (IOException e) {
                Log.e(TAG, "parseWallpaperOffsets IOException is close error");
            }
        } catch (FileNotFoundException e2) {
            Log.e(TAG, "parseWallpaperOffsets FileNotFoundException error");
            if (0 != 0) {
                is.close();
            }
        } catch (XmlPullParserException e3) {
            Log.e(TAG, "parseWallpaperOffsets XmlPullParserException error");
            if (0 != 0) {
                is.close();
            }
        } catch (IOException e4) {
            Log.e(TAG, "parseWallpaperOffsets IOException error");
            if (0 != 0) {
                is.close();
            }
        } catch (Exception e5) {
            Log.e(TAG, "parseWallpaperOffsets Exception error");
            if (0 != 0) {
                is.close();
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    is.close();
                } catch (IOException e6) {
                    Log.e(TAG, "parseWallpaperOffsets IOException is close error");
                }
            }
            throw th;
        }
        Log.d(TAG, "Parse offsets:" + offsets[0] + "," + offsets[1] + "," + offsets[2] + "," + offsets[3]);
        return offsets;
    }

    public Bitmap getBlurBitmap(Rect rect) {
        Rect thumbnailRect = new Rect(rect.left / 4, rect.top / 4, rect.right / 4, rect.bottom / 4);
        if (rect.width() > 0 && rect.width() < 4) {
            thumbnailRect.right = thumbnailRect.left + 1;
        }
        if (rect.height() > 0 && rect.height() < 4) {
            thumbnailRect.bottom = thumbnailRect.top + 1;
        }
        Rect thumbnailRect2 = cutForLiveWallpaper(thumbnailRect);
        Bitmap bp = WallpaperManagerAdapter.peekBlurWallpaperBitmap(this.mWallpaperManger, thumbnailRect2);
        if (bp == null) {
            Log.w(TAG, "getBlurBitmap return default cause peek null blur wallpaper.");
            return null;
        }
        Rect wall = new Rect(0, 0, bp.getWidth(), bp.getHeight());
        if (wall.intersect(thumbnailRect2)) {
            int height = wall.height();
            int width = wall.width();
            int[] inPixels = new int[(width * height)];
            try {
                Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                if (bitmap == null) {
                    try {
                        Log.e(TAG, "getBlurBitmap() bitmap = null");
                        return null;
                    } catch (OutOfMemoryError e) {
                        Log.e(TAG, "HwWallpaperManager can't create blur wallpaper for out of memory.");
                        return null;
                    }
                } else {
                    try {
                        bp.getPixels(inPixels, 0, width, wall.left, wall.top, width, height);
                        bitmap.setPixels(inPixels, 0, width, 0, 0, width, height);
                        if (HwPicAverageNoises.isAverageNoiseSupported()) {
                            return new HwPicAverageNoises().addNoiseWithBlackBoard(bitmap, 1275068416);
                        }
                        return bitmap;
                    } catch (OutOfMemoryError e2) {
                        Log.e(TAG, "HwWallpaperManager can't create blur wallpaper for out of memory.");
                        return null;
                    }
                }
            } catch (OutOfMemoryError e3) {
                Log.e(TAG, "HwWallpaperManager can't create blur wallpaper for out of memory.");
                return null;
            }
        } else {
            Log.w(TAG, "getBlurBitmap return default cause rect intersect fail.");
            return null;
        }
    }

    private Rect cutForLiveWallpaper(Rect rect) {
        WallpaperInfo wallpaperInfo = this.mWallpaperManger.getWallpaperInfo();
        ApplicationInfo ai = null;
        if (wallpaperInfo != null) {
            int flags = 0;
            try {
                ai = IPackageManagerEx.getApplicationInfo(wallpaperInfo.getPackageName(), 0, this.mService.getWallpaperUserId());
            } catch (RemoteException e) {
                Log.e(TAG, "IWallpaperManagerEx or IPackageManager RemoteException error");
            }
            if (ai != null) {
                flags = ai.flags;
            }
            if ((flags & 1) == 0 && rect.left == 0) {
                DisplayMetrics dm = new DisplayMetrics();
                ((WindowManager) this.mContext.getSystemService("window")).getDefaultDisplay().getMetrics(dm);
                int disWidth = (dm.widthPixels > dm.heightPixels ? dm.heightPixels : dm.widthPixels) / 4;
                rect.right = rect.right > disWidth ? disWidth : rect.right;
                rect.right = rect.left + 2 > rect.right ? rect.left + 2 : rect.right;
                if (rect.width() > 30 * 2) {
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
