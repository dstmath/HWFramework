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
    private static String TAG = null;
    private static final Object sSync = null;
    public static final int x_land = 2;
    public static final int x_port = 0;
    public static final int y_land = 3;
    public static final int y_port = 1;
    private HwCustHwWallpaperManager mCustHwWallpaperManager;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: huawei.android.app.HwWallpaperManager.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: huawei.android.app.HwWallpaperManager.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: huawei.android.app.HwWallpaperManager.<clinit>():void");
    }

    public HwWallpaperManager(Context context, Handler handler) {
        super(context, handler);
        this.mCustHwWallpaperManager = (HwCustHwWallpaperManager) HwCustUtils.createObj(HwCustHwWallpaperManager.class, new Object[x_port]);
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
            rect2.right = rect2.left + y_port;
        }
        if (rect.height() > 0 && rect.height() < 4) {
            rect2.bottom = rect2.top + y_port;
        }
        Rect thumbnailRect = cutForLiveWallpaper(rect2);
        Bitmap bp = peekBlurWallpaperBitmap(thumbnailRect);
        if (bp == null) {
            Log.w(TAG, "getBlurBitmap return default cause peek null blur wallpaper.");
            return BitmapFactory.decodeResource(getContext().getResources(), 33751273);
        }
        rect2 = new Rect(x_port, x_port, bp.getWidth(), bp.getHeight());
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
                    return BitmapFactory.decodeResource(getContext().getResources(), 33751273);
                }
                bp.getPixels(inPixels, x_port, width, rect2.left, rect2.top, width, height);
                bitmap.setPixels(inPixels, x_port, width, x_port, x_port, width, height);
                if (!HwPicAverageNoises.isAverageNoiseSupported()) {
                    return bitmap;
                }
                return new HwPicAverageNoises().addNoiseWithBlackBoard(bitmap, 1275068416);
            } catch (OutOfMemoryError e) {
                Log.e(TAG, "HwWallpaperManager can't create blur wallpaper for out of memory.");
                return BitmapFactory.decodeResource(getContext().getResources(), 33751273);
            }
        }
        Log.w(TAG, "getBlurBitmap return default cause rect intersect fail.");
        return BitmapFactory.decodeResource(getContext().getResources(), 33751273);
    }

    public void setCallback(Object callback) {
        int count = mCallbacks.size();
        int i = x_port;
        while (i < count) {
            if (((WeakReference) mCallbacks.get(i)).get() != callback) {
                i += y_port;
            } else {
                return;
            }
        }
        mCallbacks.add(new WeakReference(callback));
    }

    public void setStream(InputStream data) throws IOException {
        setWallpaperStartingPoints(new int[]{-1, -1, -1, -1});
        super.setStream(data);
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
        if (this.mCustHwWallpaperManager != null) {
            if (this.mCustHwWallpaperManager.isScrollWallpaper(max, min, width, height)) {
                Log.w(TAG, "Default wallpaper is scrollable: width=" + width + ", height=" + height);
                return bm;
            }
        }
        if (height == max && (width == min * x_land || width == max)) {
            int[] offsets = new int[]{-1, -1, -1, -1};
            if (TextUtils.isEmpty(SystemProperties.get("ro.config.wallpaper")) && !new File("/data/cust/wallpaper/", "wallpaper1.jpg").exists()) {
                String filePath = "/data/skin/description.xml";
                offsets = parseWallpaperOffsets("/data/skin/description.xml");
            }
            if (offsets[x_port] == -1) {
                offsets[x_port] = (width - min) / x_land;
            } else if (offsets[x_port] < -1) {
                offsets[x_port] = x_port;
            } else if (offsets[x_port] > width - min) {
                offsets[x_port] = width - min;
            }
            if (offsets[x_land] == -1) {
                offsets[x_land] = (width - max) / x_land;
            } else if (offsets[x_land] < -1) {
                offsets[x_land] = x_port;
            } else if (offsets[x_land] > width - max) {
                offsets[x_land] = width - max;
            }
            if (offsets[x_land] > offsets[x_port]) {
                offsets[x_land] = offsets[x_port];
            } else {
                if (offsets[x_land] < offsets[x_port] - (max - min)) {
                    offsets[x_land] = offsets[x_port] - (max - min);
                }
            }
            try {
                Bitmap newbm = Bitmap.createBitmap(bm, offsets[x_land], x_port, max, max);
                offsets[x_port] = offsets[x_port] - offsets[x_land];
                offsets[y_port] = x_port;
                offsets[x_land] = x_port;
                if (offsets[y_land] == -1) {
                    offsets[y_land] = (max - min) / x_land;
                } else if (offsets[y_land] < -1) {
                    offsets[y_land] = x_port;
                } else if (offsets[y_land] > max - min) {
                    offsets[y_land] = max - min;
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
                    if (!bm.isRecycled()) {
                        bm.recycle();
                    }
                }
                return newbm;
            } catch (IllegalArgumentException e3) {
                Log.w(TAG, "Create default square bitmap error, run generateBitmap! ");
                return HwThemeManager.generateBitmap(getContext(), bm, min, max);
            }
        }
        Log.w(TAG, "Irregular default bitmap: width=" + width + ", height=" + height);
        return HwThemeManager.generateBitmap(getContext(), bm, min, max);
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
        setWallpaperStartingPoints(offsets);
        super.setStream(data);
    }

    public int[] parseWallpaperOffsets(String srcFile) {
        FileNotFoundException e;
        XmlPullParserException e2;
        IOException e3;
        Exception e4;
        Throwable th;
        int[] offsets = new int[]{-1, -1, -1, -1};
        if (srcFile == null) {
            return offsets;
        }
        File file = new File(srcFile);
        if (!file.exists()) {
            return offsets;
        }
        FileInputStream fileInputStream = null;
        try {
            FileInputStream is = new FileInputStream(file);
            try {
                XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
                parser.setInput(is, CharacterSets.DEFAULT_CHARSET_NAME);
                for (int eventType = parser.getEventType(); eventType != y_port; eventType = parser.next()) {
                    if (eventType == x_land) {
                        String tagName = parser.getName();
                        if (tagName.equals("WP_Xoffset_port")) {
                            offsets[x_port] = Integer.parseInt(parser.nextText());
                        } else if (tagName.equals("WP_Yoffset_port")) {
                            offsets[y_port] = Integer.parseInt(parser.nextText());
                        } else if (tagName.equals("WP_Xoffset_land")) {
                            offsets[x_land] = Integer.parseInt(parser.nextText());
                        } else if (tagName.equals("WP_Yoffset_land")) {
                            offsets[y_land] = Integer.parseInt(parser.nextText());
                        }
                    } else if (eventType == y_land) {
                    }
                }
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                    }
                }
            } catch (FileNotFoundException e5) {
                e = e5;
                fileInputStream = is;
            } catch (XmlPullParserException e6) {
                e2 = e6;
                fileInputStream = is;
            } catch (IOException e7) {
                e3 = e7;
                fileInputStream = is;
            } catch (Exception e8) {
                e4 = e8;
                fileInputStream = is;
            } catch (Throwable th2) {
                th = th2;
                fileInputStream = is;
            }
        } catch (FileNotFoundException e9) {
            e = e9;
            try {
                e.printStackTrace();
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (IOException ioe2) {
                        ioe2.printStackTrace();
                    }
                }
                Log.d(TAG, "Parse offsets:" + offsets[x_port] + "," + offsets[y_port] + "," + offsets[x_land] + "," + offsets[y_land]);
                return offsets;
            } catch (Throwable th3) {
                th = th3;
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (IOException ioe22) {
                        ioe22.printStackTrace();
                    }
                }
                throw th;
            }
        } catch (XmlPullParserException e10) {
            e2 = e10;
            e2.printStackTrace();
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException ioe222) {
                    ioe222.printStackTrace();
                }
            }
            Log.d(TAG, "Parse offsets:" + offsets[x_port] + "," + offsets[y_port] + "," + offsets[x_land] + "," + offsets[y_land]);
            return offsets;
        } catch (IOException e11) {
            e3 = e11;
            e3.printStackTrace();
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException ioe2222) {
                    ioe2222.printStackTrace();
                }
            }
            Log.d(TAG, "Parse offsets:" + offsets[x_port] + "," + offsets[y_port] + "," + offsets[x_land] + "," + offsets[y_land]);
            return offsets;
        } catch (Exception e12) {
            e4 = e12;
            e4.printStackTrace();
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException ioe22222) {
                    ioe22222.printStackTrace();
                }
            }
            Log.d(TAG, "Parse offsets:" + offsets[x_port] + "," + offsets[y_port] + "," + offsets[x_land] + "," + offsets[y_land]);
            return offsets;
        }
        Log.d(TAG, "Parse offsets:" + offsets[x_port] + "," + offsets[y_port] + "," + offsets[x_land] + "," + offsets[y_land]);
        return offsets;
    }

    private Rect cutForLiveWallpaper(Rect rect) {
        int flags = x_port;
        WallpaperInfo wallpaperInfo = getWallpaperInfo();
        int mCurrentUserId = x_port;
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
            ai = AppGlobals.getPackageManager().getApplicationInfo(packageName, x_port, mCurrentUserId);
            if (ai != null) {
                flags = ai.flags;
            }
            if ((flags & y_port) == 0 && rect.left == 0) {
                DisplayMetrics dm = new DisplayMetrics();
                ((WindowManager) getContext().getSystemService(FreezeScreenScene.WINDOW_PARAM)).getDefaultDisplay().getMetrics(dm);
                int disWidth = Math.min(dm.widthPixels, dm.heightPixels) / 4;
                rect.right = Math.min(rect.right, disWidth);
                rect.right = Math.max(rect.left + x_land, rect.right);
                if (rect.width() > 60) {
                    rect.left += 30;
                    rect.right -= 30;
                } else {
                    int dw = disWidth / x_land;
                    rect.left = dw - 2;
                    rect.right = dw + x_land;
                }
            }
        }
        return rect;
    }
}
