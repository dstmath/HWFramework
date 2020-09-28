package android.view;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.util.Log;
import android.view.View;
import com.huawei.android.view.IHwMultiDisplayDragStateListener;

public class HwViewEx {
    public static final int ACTION_CHECKVIEWDROPPABLE = 8;
    public static final int ACTION_RESTORE_SHADOW = 9;
    public static final int ACTION_SWITCH_ICON = 7;
    private static final int ALPHA_TRANSPARENT = 0;
    private static final int ALPHA_UNTRANSPARENT = 255;
    private static final int FLAG_TRANSPARENT = -255;
    public static final int FRAME_NUM = 5;
    protected static final String HW_VIEW_LOG_TAG = "HwViewEx";
    public static final int STATE_DRAG_STARTED = 0;
    public static final int STATE_DRAG_STOPED = 1;
    private static IHwMultiDisplayDragStateListener listener = new IHwMultiDisplayDragStateListener.Stub() {
        /* class android.view.HwViewEx.AnonymousClass1 */

        @Override // com.huawei.android.view.IHwMultiDisplayDragStateListener
        public void updateDragState(int dragState) {
            Log.d(HwViewEx.HW_VIEW_LOG_TAG, "update dragState:" + dragState);
            int unused = HwViewEx.sDragState = dragState;
        }
    };
    static Bitmap mBitmapAdd = null;
    static Bitmap mBitmapForbidden = null;
    static Bitmap mBitmapz = null;
    static boolean mDrawableBool = true;
    static boolean mFirstDrag = false;
    private static int mHeight;
    static boolean mRestored = false;
    private static int mStateSame = 0;
    static SurfaceControl mSurfaceControl;
    private static int mWidth;
    private static ClipData sClipData;
    private static int sDragState = -1;

    public static void restoreShadow() {
        if (sDragState == 1) {
            Log.d(HW_VIEW_LOG_TAG, "give up to restoreShadow: drag state stoped already.");
            return;
        }
        Bitmap bitmap = mBitmapz;
        Log.d(HW_VIEW_LOG_TAG, "restoreShadow=======");
        Surface surface = new Surface();
        mRestored = true;
        mStateSame = 0;
        surface.copyFrom(mSurfaceControl);
        Paint tempPaint = new Paint();
        tempPaint.reset();
        Canvas canvas = surface.lockCanvas(null);
        try {
            canvas.drawColor(0, PorterDuff.Mode.CLEAR);
            canvas.drawBitmap(bitmap, 0.0f, (float) (mHeight / 2), tempPaint);
        } finally {
            surface.unlockCanvasAndPost(canvas);
        }
    }

    public static void switchDragShadow(boolean droppable) {
        Bitmap bitmap;
        if (sDragState == 1) {
            Log.d(HW_VIEW_LOG_TAG, "give up to switchDragShadow: drag state stoped already.");
        } else if (mFirstDrag || (!(mSurfaceControl == null || mDrawableBool == droppable) || mRestored || mStateSame <= 5)) {
            Log.d(HW_VIEW_LOG_TAG, "switchDragShadow droppable = " + droppable);
            mRestored = false;
            mFirstDrag = false;
            if (mDrawableBool == droppable) {
                mStateSame++;
            } else {
                mStateSame = 0;
            }
            mDrawableBool = droppable;
            if (droppable) {
                bitmap = mBitmapAdd;
            } else {
                bitmap = mBitmapForbidden;
            }
            Surface surface = new Surface();
            surface.copyFrom(mSurfaceControl);
            Paint tempPaint = new Paint();
            tempPaint.reset();
            Canvas canvas = surface.lockCanvas(null);
            try {
                canvas.drawColor(0, PorterDuff.Mode.CLEAR);
                canvas.drawBitmap(bitmap, 0.0f, 0.0f, tempPaint);
            } finally {
                surface.unlockCanvasAndPost(canvas);
            }
        } else {
            Log.d(HW_VIEW_LOG_TAG, "switchDragShadow return mFirstDrag = " + mFirstDrag + " mDrawableBool = " + mDrawableBool + " droppable = " + droppable + " mRestored = " + mRestored + " mStateSame = " + mStateSame);
        }
    }

    private static void bitmapRecycle(Bitmap bitmap) {
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
        }
    }

    public static Point getAddAndForbiddenSize(Context context) {
        Bitmap bitmapForbidden = BitmapFactory.decodeResource(context.getResources(), 33751823);
        if (bitmapForbidden == null) {
            Log.e(HW_VIEW_LOG_TAG, "bitmapForbidden decode failed in function getAddAndForbiddenSize");
            return null;
        }
        Bitmap bitmapAdd = BitmapFactory.decodeResource(context.getResources(), 33751822);
        if (bitmapAdd == null) {
            bitmapRecycle(bitmapForbidden);
            Log.e(HW_VIEW_LOG_TAG, "bitmapAdd decode failed in function getAddAndForbiddenSize");
            return null;
        }
        int width = bitmapAdd.getWidth();
        int height = bitmapAdd.getHeight();
        int width_2 = bitmapForbidden.getWidth();
        int height_2 = bitmapForbidden.getHeight();
        Point size = new Point();
        size.x = width > width_2 ? width : width_2;
        size.y = height > height_2 ? height : height_2;
        bitmapRecycle(bitmapForbidden);
        bitmapRecycle(bitmapAdd);
        return size;
    }

    public static void setClipData(ClipData clipData) {
        sClipData = clipData;
    }

    public static void initBitmapAddAndForbidden(View view, Context context, SurfaceControl surfaceControl, Point shadowSize, View.DragShadowBuilder shadowBuilder) {
        Log.d(HW_VIEW_LOG_TAG, "initBitmapAddAndForbidden...");
        if (sDragState == 1) {
            Log.d(HW_VIEW_LOG_TAG, "give up to initBitmapAddAndForbidden: drag state stoped already.");
            return;
        }
        Bitmap bitmap = mBitmapAdd;
        if (bitmap != null) {
            bitmapRecycle(bitmap);
            bitmapRecycle(mBitmapForbidden);
            bitmapRecycle(mBitmapz);
            mBitmapAdd = null;
            mBitmapForbidden = null;
            mBitmapz = null;
        }
        mFirstDrag = true;
        if (surfaceControl != null) {
            Log.d(HW_VIEW_LOG_TAG, "surfaceControl is not empty, set surfaceControl.");
            mSurfaceControl = surfaceControl;
        }
        Point shadowSizeCopy = copyShadowSize(context, shadowSize, shadowBuilder);
        Bitmap bitmapForbidden = BitmapFactory.decodeResource(context.getResources(), 33751823);
        if (bitmapForbidden == null) {
            Log.e(HW_VIEW_LOG_TAG, "bitmapForbidden decode failed in function initBitmapAddAndForbidden");
            return;
        }
        Bitmap bitmapAdd = BitmapFactory.decodeResource(context.getResources(), 33751822);
        if (bitmapAdd == null) {
            bitmapRecycle(bitmapForbidden);
            Log.e(HW_VIEW_LOG_TAG, "bitmapAdd decode failed in function initBitmapAddAndForbidden");
            return;
        }
        initSrcBitmap(shadowSizeCopy, bitmapForbidden, bitmapAdd);
        drawBitmap(shadowBuilder, bitmapForbidden, bitmapAdd, getPaint(surfaceControl));
        restoreShadow();
        mRestored = false;
        bitmapRecycle(bitmapForbidden);
        bitmapRecycle(bitmapAdd);
    }

    private static Paint getPaint(SurfaceControl surfaceControl) {
        Paint tempPaint = new Paint();
        tempPaint.reset();
        if (surfaceControl == null) {
            tempPaint.setAlpha(255);
        } else if (isShadowTransparent()) {
            tempPaint.setAlpha(0);
        }
        return tempPaint;
    }

    private static void drawBitmap(View.DragShadowBuilder shadowBuilder, Bitmap bitmapForbidden, Bitmap bitmapAdd, Paint tempPaint) {
        shadowBuilder.onDrawShadow(new Canvas(mBitmapz));
        Canvas canvasForbidden = new Canvas(mBitmapForbidden);
        canvasForbidden.drawBitmap(mBitmapz, (float) (mWidth / 2), (float) (mHeight / 2), tempPaint);
        canvasForbidden.drawBitmap(bitmapForbidden, 0.0f, 0.0f, tempPaint);
        Canvas canvasAdd = new Canvas(mBitmapAdd);
        canvasAdd.drawBitmap(mBitmapz, (float) (mWidth / 2), (float) (mHeight / 2), tempPaint);
        canvasAdd.drawBitmap(bitmapAdd, 0.0f, 0.0f, tempPaint);
    }

    private static void initSrcBitmap(Point shadowSizeCopy, Bitmap bitmapForbidden, Bitmap bitmapAdd) {
        int width = bitmapAdd.getWidth();
        int height = bitmapAdd.getHeight();
        int width_2 = bitmapForbidden.getWidth();
        int height_2 = bitmapForbidden.getHeight();
        mWidth = width > width_2 ? width : width_2;
        mHeight = height > height_2 ? height : height_2;
        mBitmapForbidden = Bitmap.createBitmap(shadowSizeCopy.x, shadowSizeCopy.y, Bitmap.Config.ARGB_8888);
        mBitmapAdd = Bitmap.createBitmap(shadowSizeCopy.x, shadowSizeCopy.y, Bitmap.Config.ARGB_8888);
        mBitmapz = Bitmap.createBitmap(shadowSizeCopy.x - (mWidth / 2), shadowSizeCopy.y - (mHeight / 2), Bitmap.Config.ARGB_8888);
    }

    private static Point copyShadowSize(Context context, Point shadowSize, View.DragShadowBuilder shadowBuilder) {
        Point shadowSizeCopy = new Point();
        if (shadowSize != null) {
            shadowSizeCopy.set(shadowSize.x, shadowSize.y);
        } else {
            Log.d(HW_VIEW_LOG_TAG, "update drag shadow");
            shadowBuilder.onProvideShadowMetrics(shadowSizeCopy, new Point());
            Point forbiddenAdd = getAddAndForbiddenSize(context);
            if (forbiddenAdd != null) {
                shadowSizeCopy.x += forbiddenAdd.x / 2;
                shadowSizeCopy.y += forbiddenAdd.y / 2;
                shadowSizeCopy.set(shadowSizeCopy.x, shadowSizeCopy.y);
            }
        }
        return shadowSizeCopy;
    }

    private static boolean isShadowTransparent() {
        ClipData clipData = sClipData;
        if (clipData == null) {
            return false;
        }
        int count = clipData.getItemCount();
        for (int i = 0; i < count; i++) {
            Intent intent = sClipData.getItemAt(i).getIntent();
            if (intent != null && intent.getFlags() == -255) {
                Log.d(HW_VIEW_LOG_TAG, "find alphaFlag transparent.");
                return true;
            }
        }
        return false;
    }

    public static IHwMultiDisplayDragStateListener getListener() {
        return listener;
    }
}
