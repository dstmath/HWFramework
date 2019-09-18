package android.widget.sr;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.FreezeScreenScene;
import android.view.Display;
import android.view.WindowManager;

public class SRUtils {
    private static final String[] BLACK_LIST = {"com.tencent.mm.ui.chatting.ChattingImageBGView"};
    private static final int SR_MAX_LONGSIDE = 800;
    private static final int SR_MAX_SHORTSIDE = 600;
    private static final float SR_MIN_FULLSCREEN_FACTOR = 0.95f;
    private static final int SR_MIN_PIXELCOUNT = 90000;
    private static final int SR_MIN_SHORTSIDE = 150;
    private static final String SR_TAG = "SuperResolution";
    private static final String[] WHITE_LIST = {"com.tencent.mm", "com.ss.android.article.news", "jp.naver.line.android", "com.instagram.android"};

    public static boolean checkIsInSRWhiteList(Context context) {
        if (context == null) {
            return false;
        }
        String packageName = context.getPackageName();
        for (String name : WHITE_LIST) {
            if (name.equals(packageName)) {
                return true;
            }
        }
        return false;
    }

    public static boolean checkIsInSRBlackList(Object object) {
        if (object == null) {
            return false;
        }
        String className = object.getClass().getName();
        for (String name : BLACK_LIST) {
            if (name.equals(className)) {
                return true;
            }
        }
        return false;
    }

    public static boolean checkIsFullScreen(Context context, int w, int h) {
        boolean z = false;
        if (context == null) {
            return false;
        }
        Display display = ((WindowManager) context.getSystemService(FreezeScreenScene.WINDOW_PARAM)).getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int screenWidth = size.x;
        int screenHeight = size.y;
        int maxWidth = screenWidth;
        int minWidth = (int) (((float) screenWidth) * SR_MIN_FULLSCREEN_FACTOR);
        int maxHeight = screenHeight;
        int minHeight = (int) (((float) screenHeight) * SR_MIN_FULLSCREEN_FACTOR);
        if ((minWidth <= w && maxWidth >= w) || (minHeight <= h && maxHeight >= h)) {
            z = true;
        }
        return z;
    }

    public static boolean checkMatchResolution(Drawable drawable) {
        boolean z = false;
        if (drawable == null) {
            return false;
        }
        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();
        int longSide = width > height ? width : height;
        int shortSide = width > height ? height : width;
        if (longSide <= SR_MAX_LONGSIDE && shortSide >= 150 && shortSide <= SR_MAX_SHORTSIDE && width * height >= SR_MIN_PIXELCOUNT) {
            z = true;
        }
        return z;
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable == null) {
            return null;
        }
        if (!(drawable instanceof BitmapDrawable) || !drawable.getClass().equals(BitmapDrawable.class)) {
            int w = drawable.getIntrinsicWidth();
            int h = drawable.getIntrinsicHeight();
            if (w <= 0 || h <= 0) {
                return null;
            }
            Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            Rect rect = drawable.getBounds();
            int oldLeft = rect.left;
            int oldTop = rect.top;
            int oldRight = rect.right;
            int oldBottm = rect.bottom;
            drawable.setBounds(0, 0, w, h);
            drawable.draw(canvas);
            drawable.setBounds(oldLeft, oldTop, oldRight, oldBottm);
            return bitmap.createAshmemBitmap(Bitmap.Config.ARGB_8888);
        }
        Bitmap bitmap2 = ((BitmapDrawable) drawable).getBitmap();
        if (bitmap2 == null) {
            return null;
        }
        return bitmap2.createAshmemBitmap(Bitmap.Config.ARGB_8888);
    }
}
