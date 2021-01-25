package android.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.hardware.display.DisplayManagerGlobal;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.Surface;
import android.view.View;
import android.view.animation.PathInterpolator;
import com.huawei.android.view.IHwMultiDisplayDragStateListener;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;

public class HwViewEx {
    public static final int ACTION_CHECKVIEWDROPPABLE = 8;
    public static final int ACTION_RESTORE_SHADOW = 9;
    public static final int ACTION_SWITCH_ICON = 7;
    private static final String ADD = "+";
    private static final int ALPHA_TRANSPARENT = 0;
    private static final int ALPHA_UNTRANSPARENT = 255;
    private static final int CAL_VAL = 2;
    private static final boolean DEBUG_DRAG = false;
    private static final float EPSILON = 1.0E-6f;
    private static final float FINAL_SCALE_SIZE = 1.0f;
    private static final float FIRST_SHADOW_BLUR = 40.0f;
    private static final String FIRST_SHADOW_COLOR = "#4D000000";
    private static final int FIRST_SHADOW_OFFSET_Y_DP = 4;
    private static final int FLAG_TRANSPARENT = -255;
    public static final int FRAME_NUM = 5;
    protected static final String HW_VIEW_LOG_TAG = "HwViewEx";
    private static final float INITIAL_SCALE_SIZE = 0.6f;
    private static final int LEFT_BUBBLE_RADIUS_DP = 12;
    private static final int LEFT_BUBBLE_SHADOW_BLUR_DP = 2;
    private static final int LEFT_BUBBLE_SHADOW_X_DP = 1;
    private static final int LEFT_BUBBLE_SHADOW_Y_DP = 1;
    private static final int LEFT_ICON_ANIMATION_TIME = 400;
    private static final String[] LOCALE_FORMAT_NUM_LANGUAGE = {"ar"};
    private static final int MAX_CNT = 99;
    private static final float MAX_SCALE_SIZE = 1.2f;
    private static final int MAX_STACK_NUM = 3;
    private static final int MIN_DIS_BET_LEFT_AND_RIGHT_BUBBLES_DP = 4;
    private static final int MIN_STACK_NUM = 2;
    private static final float OVERLAY_SHADOW_BLUR = 8.0f;
    private static final String OVERLAY_SHADOW_COLOR = "#33000000";
    private static final int OVERLAY_SHADOW_OFFSET_DP = 6;
    private static final int PADDING_IN_RIGHT_BUBBLE_DP = 6;
    private static final int PADDING_OUT_BUBBLE_DP = 8;
    private static final String RIGHT_BUBBLE_COLOR_NIGHT_BAK = "#006CDE";
    private static final String RIGHT_BUBBLE_COLOR_NORMAL_BAK = "#007DFF";
    private static final int RIGHT_BUBBLE_RADIUS_DP = 12;
    private static final int RIGHT_BUBBLE_SHADOW_BLUR_DP = 2;
    private static final String RIGHT_BUBBLE_SHADOW_COLOR_NIGHT = "#00000000";
    private static final String RIGHT_BUBBLE_SHADOW_COLOR_NORMAL = "#4D000000";
    private static final int RIGHT_BUBBLE_SHADOW_X_DP = 1;
    private static final int RIGHT_BUBBLE_SHADOW_Y_DP = 1;
    private static final String RIGHT_BUBBLE_TEXT_COLOR_NIGHT_BAK = "#E5E5E5";
    private static final String RIGHT_BUBBLE_TEXT_COLOR_NORMAL_BAK = "#FFFFFF";
    private static final int RIGHT_BUBBLE_TEXT_SIZE_DP = 16;
    private static final float SCALE_IN = 0.2f;
    private static final float SCALE_IN_SUB = 0.0f;
    private static final float SCALE_OUT = 0.2f;
    private static final float SCALE_OUT_SUB = 1.0f;
    private static final int SECOND_BACKGROUND_ALPHA = 112;
    private static final float SECOND_BACKGROUND_SCALESIZE = 0.06f;
    private static final int SHADOW_SIZE_HEIGHT_ADD_DP = 100;
    private static final int SHADOW_SIZE_WIDTH_ADD_DP = 100;
    public static final int STATE_DRAG_STARTED = 0;
    public static final int STATE_DRAG_STOPED = 1;
    private static final int THIRD_BACKGROUND_ALPHA = 90;
    private static final float THIRD_BACKGROUND_SCALESIZE = 0.12f;
    private static float WIDTH_SCALE = 0.6f;
    static View.AttachInfo attachInfoForDrag = null;
    private static IHwMultiDisplayDragStateListener listener = new IHwMultiDisplayDragStateListener.Stub() {
        /* class android.view.HwViewEx.AnonymousClass1 */

        @Override // com.huawei.android.view.IHwMultiDisplayDragStateListener
        public void updateDragState(int dragState) {
            Log.d(HwViewEx.HW_VIEW_LOG_TAG, "update dragState:" + dragState);
            HwViewEx.sDragState = dragState;
        }
    };
    static Bitmap mBitmap = null;
    static Bitmap mBitmapAdd = null;
    static Bitmap mBitmapForbidden = null;
    static Bitmap mBitmapz = null;
    private static float mDp2PixAdd = 0.5f;
    static boolean mDrawableBool = true;
    static boolean mFirstDrag = false;
    private static int mHeight;
    static boolean mIsAddReceptableFlag = false;
    private static volatile boolean mIsScaled = false;
    static boolean mRestored = false;
    static View.DragShadowBuilder mShadow = null;
    private static int mSideDp = 8;
    private static int mStateSame = 0;
    static SurfaceControl mSurfaceControl;
    private static int mTouchPointY = 4;
    private static int mWidth;
    static int sAddX = 0;
    static int sAddY = 0;
    private static int sBubShadowColor = 0;
    private static int sBubbleColor = 0;
    private static ClipData sClipData;
    private static boolean sCntNeedTransFormat = false;
    private static volatile Bitmap sCountBitmap = null;
    static int sDragState = -1;
    private static float sHeightScale = 0.3f;
    private static int sLeftBubRadius = 0;
    private static int sLeftBubShadowBlur = 0;
    private static int sLeftBubShadowX = 0;
    private static int sLeftBubShadowY = 0;
    private static float sLeftIconPositionX = 0.0f;
    private static float sLeftIconPositionY = 0.0f;
    private static float sLeftStartPosition = 0.0f;
    private static int sMinDisBetLRBub = 0;
    static float sOriHeight = 0.0f;
    static float sOriWidth = 0.0f;
    private static int sPaddingOutBub = 0;
    private static int sPaddinginRBub = 0;
    private static int sRightBubRadius = 0;
    private static int sRightBubShadowBlur = 0;
    private static int sRightBubShadowX = 0;
    private static int sRightBubShadowY = 0;
    private static float sRightPositionX = 0.0f;
    private static float sRightPositionY = 0.0f;
    private static int sRoundCorner = 8;
    private static float sShortTextScale = 0.95f;
    private static volatile Bitmap sStatusBitmap = null;
    private static volatile boolean sStopAnimationRun = true;
    private static int sTextColor = 0;
    private static int sTextSize = 0;
    private static float sTopStartPosition = 0.0f;
    private static int sVaLongTextTime = 250;
    private static int sVaPictureTime = 300;
    private static int sVaShortTextTime = 150;
    static float scaleSize = 1.0f;

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

    public static void setDragAcceptableMimeType(String[] mimeTypes, View view) {
        if (mimeTypes == null || view == null) {
            Log.i(HW_VIEW_LOG_TAG, "view or mimeTypes is null in setDragAcceptableMimeType");
            return;
        }
        if (view.getListenerInfo().mAcceptableMimeType == null) {
            view.getListenerInfo().mAcceptableMimeType = new ArrayList<>();
        } else {
            view.getListenerInfo().mAcceptableMimeType.clear();
        }
        insertMimeTypeToArrayList(mimeTypes, view);
    }

    public static void verifyDragDataMimeType(DragEvent event, boolean isDropZone, View view) {
        String type;
        if (view != null) {
            if (event.mAction == 1) {
                if (!isDropZone) {
                    view.getListenerInfo().mMimeTypeSupportState = 255;
                } else if (view.getListenerInfo().mAcceptableMimeType == null) {
                    view.getListenerInfo().mMimeTypeSupportState = 0;
                } else {
                    view.getListenerInfo().mMimeTypeSupportState = 3;
                    view.getListenerInfo().mAcceptableItemCnt = 0;
                    if (view.getListenerInfo().mAcceptableMimeType.size() == 1 && (type = view.getListenerInfo().mAcceptableMimeType.get(0)) != null && type.equals("*/*")) {
                        view.getListenerInfo().mMimeTypeSupportState = 1;
                    }
                    if (view.getListenerInfo().mMimeTypeSupportState != 1) {
                        setAcceptableState(view, event);
                    }
                }
            } else if (event.mAction == 4) {
                view.getListenerInfo().mAcceptableItemCnt = 0;
                mIsScaled = false;
                Bitmap bitmap = mBitmap;
                if (bitmap != null) {
                    bitmap.recycle();
                }
                bitmapRecycle(sStatusBitmap);
                bitmapRecycle(sCountBitmap);
                sStatusBitmap = null;
                sCountBitmap = null;
                sStopAnimationRun = true;
            }
        }
    }

    public static void updateDragEnterExitState(boolean isEntered, int mimeTypeSupportState, int acceptableItemCnt, View view) {
        mShadow.setDragEnterExitState(isEntered, mimeTypeSupportState, acceptableItemCnt);
        if (mIsAddReceptableFlag) {
            updateHwDragShadow(mShadow, view.mContext);
        }
    }

    private static void setAcceptableState(View view, DragEvent event) {
        ClipDescription description = event.getClipDescription();
        int draggingMimeTypeCnt = description.getMimeTypeCount();
        boolean isIncludeNotSupport = false;
        int acceptableMimeTypeCnt = 0;
        for (int index = 0; index != draggingMimeTypeCnt; index++) {
            String dragOutMimeType = description.getMimeType(index);
            boolean isSupportFlag = false;
            Iterator<String> it = view.getListenerInfo().mAcceptableMimeType.iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                String acceptableMimeType = it.next();
                if (acceptableMimeType != null && ClipDescription.compareMimeTypes(dragOutMimeType, acceptableMimeType)) {
                    acceptableMimeTypeCnt++;
                    view.getListenerInfo().mAcceptableItemCnt += description.getMimeTypeItemCount(index);
                    isSupportFlag = true;
                    break;
                }
            }
            if (!isSupportFlag) {
                isIncludeNotSupport = true;
            }
        }
        if (view.getListenerInfo().mAcceptableItemCnt <= 0) {
            view.getListenerInfo().mMimeTypeSupportState = 3;
        } else if (isIncludeNotSupport) {
            view.getListenerInfo().mMimeTypeSupportState = 2;
        } else {
            view.getListenerInfo().mMimeTypeSupportState = 1;
        }
    }

    private static void updateHwDragShadow(View.DragShadowBuilder shadowBuilder, Context context) {
        if (context == null) {
            Log.e(HW_VIEW_LOG_TAG, "updateHwDragShadow context is null");
        } else if (!mIsScaled) {
            Log.i(HW_VIEW_LOG_TAG, "updateHwDragShadow need after scale.");
        } else {
            View.AttachInfo attachInfo = attachInfoForDrag;
            if (attachInfo == null) {
                Log.i(HW_VIEW_LOG_TAG, "updateDragShadow called on a detached view.");
            } else if (attachInfo.mDragToken != null) {
                setAcceptableStateBubble(attachInfoForDrag.mDragSurface, context, shadowBuilder);
            } else {
                Log.e(HW_VIEW_LOG_TAG, "No active drag");
            }
        }
    }

    /* access modifiers changed from: private */
    public static void setAcceptableStateBubble(Surface surface, Context context, View.DragShadowBuilder shadowBuilder) {
        if (!mIsAddReceptableFlag) {
            Log.i(HW_VIEW_LOG_TAG, "setAcceptableStateBubble return");
            return;
        }
        sStopAnimationRun = true;
        if (shadowBuilder.mIsEnteredDropZone) {
            bitmapRecycle(sStatusBitmap);
            sStatusBitmap = getStatusBitmap(shadowBuilder.mEnteredZoneMimeTypeSupportState, context);
            if (sStatusBitmap != null) {
                sStopAnimationRun = false;
                drawIconAnimation(surface, context, shadowBuilder);
                return;
            }
            setRightBubble(surface, context, shadowBuilder);
            return;
        }
        setRightBubble(surface, context, shadowBuilder);
    }

    private static void getCntBubBitMapAndPos(Surface surface, Context context, View.DragShadowBuilder shadowBuilder) {
        String cnt;
        if (shadowBuilder == null) {
            Log.e(HW_VIEW_LOG_TAG, "setClipTotalItemCntBubble shadowBuilder is null return");
        } else if (shadowBuilder.mClipTotalItemCnt > 1) {
            isCntNeedTransFormat();
            Paint bubblePaint = new Paint(1);
            Paint textPaint = new Paint(1);
            bubblePaint.setColor(sBubbleColor);
            bubblePaint.setShadowLayer((float) sRightBubShadowBlur, (float) sRightBubShadowX, (float) sRightBubShadowY, sBubShadowColor);
            textPaint.setColor(sTextColor);
            textPaint.setTextSize((float) sTextSize);
            textPaint.setTextAlign(Paint.Align.CENTER);
            String maxCntPlusStr = String.valueOf(99) + "+";
            if (shadowBuilder.mClipTotalItemCnt <= 99) {
                cnt = String.valueOf(shadowBuilder.mClipTotalItemCnt);
            } else {
                cnt = maxCntPlusStr;
            }
            if (sCntNeedTransFormat) {
                NumberFormat tr = NumberFormat.getInstance(Locale.getDefault());
                cnt = shadowBuilder.mClipTotalItemCnt <= 99 ? tr.format((long) shadowBuilder.mClipTotalItemCnt) : tr.format(99L) + "+";
            }
            drawCntBubBitMap(shadowBuilder, bubblePaint, textPaint, cnt);
            getCntBubBitMapPos(surface, shadowBuilder, bubblePaint, textPaint, cnt);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:23:0x00aa, code lost:
        android.util.Log.i(android.view.HwViewEx.HW_VIEW_LOG_TAG, "getCntBubBitMapPos exception");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:?, code lost:
        return;
     */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x00a9 A[ExcHandler: OutOfResourcesException | IllegalArgumentException | IllegalStateException (e java.lang.Throwable), Splitter:B:17:0x009f] */
    private static void getCntBubBitMapPos(Surface surface, View.DragShadowBuilder shadowBuilder, Paint bubblePaint, Paint textPaint, String cnt) {
        float bubLen;
        if (surface == null) {
            Log.e(HW_VIEW_LOG_TAG, "getCntBubBitMapPos surface is null");
            return;
        }
        Canvas canvas = surface.lockCanvas(null);
        if (canvas == null) {
            Log.e(HW_VIEW_LOG_TAG, "getCntBubBitMapPos canvas is null");
            return;
        }
        try {
            float textLength = textPaint.measureText(cnt);
            sRightPositionY = (((float) (canvas.getHeight() / 2)) - ((sOriHeight * scaleSize) / 2.0f)) - ((float) sPaddingOutBub);
            if (((float) (sPaddinginRBub * 2)) + textLength <= ((float) (sRightBubRadius * 2))) {
                bubLen = (float) (sRightBubRadius * 2);
            } else {
                bubLen = ((float) (sPaddinginRBub * 2)) + textLength;
            }
            if (((sOriWidth * scaleSize) - ((float) ((sLeftBubRadius * 2) - sPaddingOutBub))) - (bubLen - ((float) sPaddingOutBub)) < ((float) sMinDisBetLRBub)) {
                sRightPositionX = (((((float) canvas.getWidth()) * 1.0f) / 2.0f) - ((sOriWidth * scaleSize) / 2.0f)) + ((float) ((sLeftBubRadius * 2) - sPaddingOutBub)) + ((float) sMinDisBetLRBub);
            } else {
                sRightPositionX = ((((((float) canvas.getWidth()) * 1.0f) / 2.0f) + ((sOriWidth * scaleSize) / 2.0f)) + ((float) sPaddingOutBub)) - bubLen;
            }
            surface.unlockCanvasAndPost(canvas);
        } catch (Surface.OutOfResourcesException | IllegalArgumentException | IllegalStateException e) {
        } catch (Throwable th) {
            surface.unlockCanvasAndPost(canvas);
            throw th;
        }
    }

    private static boolean isCntNeedTransFormat() {
        String language = Locale.getDefault().getLanguage();
        for (String s : LOCALE_FORMAT_NUM_LANGUAGE) {
            if (s.equals(language)) {
                sCntNeedTransFormat = true;
                return true;
            }
        }
        sCntNeedTransFormat = false;
        return false;
    }

    public static void hwGetSurfaceSize(Context context, Point shadowSize) {
        if (context == null || shadowSize == null) {
            Log.e(HW_VIEW_LOG_TAG, "hwGetSurfaceSize null return");
            return;
        }
        getDragSpecification(context);
        sOriWidth = ((float) shadowSize.x) * 1.0f;
        sOriHeight = ((float) shadowSize.y) * 1.0f;
        shadowSize.x += sAddX;
        shadowSize.y += sAddY;
    }

    private static void getDragSpecification(Context context) {
        int i;
        int i2;
        int i3;
        sRightBubRadius = dipsToPixels(12, context);
        sTextSize = dipsToPixels(16, context);
        sRightBubShadowX = dipsToPixels(1, context);
        sRightBubShadowY = dipsToPixels(1, context);
        sRightBubShadowBlur = dipsToPixels(2, context);
        sLeftBubRadius = dipsToPixels(12, context);
        sLeftBubShadowX = dipsToPixels(1, context);
        sLeftBubShadowY = dipsToPixels(1, context);
        sLeftBubShadowBlur = dipsToPixels(2, context);
        sPaddinginRBub = dipsToPixels(6, context);
        sPaddingOutBub = dipsToPixels(8, context);
        sMinDisBetLRBub = dipsToPixels(4, context);
        sAddX = dipsToPixels(100, context);
        sAddY = dipsToPixels(100, context);
        int mode = context.getResources().getConfiguration().uiMode & 48;
        if (mode == 32) {
            i = Color.parseColor(RIGHT_BUBBLE_SHADOW_COLOR_NIGHT);
        } else {
            i = Color.parseColor("#4D000000");
        }
        sBubShadowColor = i;
        int bubbleColorId = context.getResources().getIdentifier("colorBadge", "attr", "androidhwext");
        TypedValue outValue = new TypedValue();
        context.getTheme().resolveAttribute(bubbleColorId, outValue, true);
        if (outValue.data != 0) {
            i2 = outValue.data;
        } else if (mode == 32) {
            i2 = Color.parseColor(RIGHT_BUBBLE_COLOR_NIGHT_BAK);
        } else {
            i2 = Color.parseColor(RIGHT_BUBBLE_COLOR_NORMAL_BAK);
        }
        sBubbleColor = i2;
        TypedArray typedArrayForTextColor = context.obtainStyledAttributes(new int[]{16843270});
        if (mode == 32) {
            i3 = Color.parseColor(RIGHT_BUBBLE_TEXT_COLOR_NIGHT_BAK);
        } else {
            i3 = Color.parseColor(RIGHT_BUBBLE_TEXT_COLOR_NORMAL_BAK);
        }
        sTextColor = typedArrayForTextColor.getColor(0, i3);
        typedArrayForTextColor.recycle();
    }

    private static void drawCntBubBitMap(View.DragShadowBuilder shadowBuilder, Paint bubblePaint, Paint textPaint, String cnt) {
        Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
        float textY = ((float) sRightBubRadius) + (((fontMetrics.bottom - fontMetrics.top) / 2.0f) - fontMetrics.bottom);
        if (((float) (sPaddinginRBub * 2)) + textPaint.measureText(cnt) <= ((float) (sRightBubRadius * 2))) {
            drawRoundnessBubBitMap(bubblePaint, textPaint, cnt, textY);
        } else {
            drawRoundedRectBubBitMap(bubblePaint, textPaint, cnt, textY);
        }
    }

    private static void drawRoundnessBubBitMap(Paint bubblePaint, Paint textPaint, String cnt, float textY) {
        int i = sRightBubRadius;
        int i2 = (i * 2) + sRightBubShadowX;
        int i3 = sRightBubShadowBlur;
        sCountBitmap = Bitmap.createBitmap(i2 + i3, (i * 2) + sRightBubShadowY + i3, Bitmap.Config.ARGB_8888);
        Canvas tmpCanvas = new Canvas(sCountBitmap);
        int i4 = sRightBubRadius;
        tmpCanvas.drawCircle((float) i4, (float) i4, (float) i4, bubblePaint);
        tmpCanvas.drawText(cnt, (float) sRightBubRadius, textY, textPaint);
    }

    private static void drawRoundedRectBubBitMap(Paint bubblePaint, Paint textPaint, String cnt, float textY) {
        float textLength = textPaint.measureText(cnt);
        int i = sRightBubShadowX;
        float f = ((float) (sPaddinginRBub * 2)) + textLength + ((float) i);
        int i2 = sRightBubShadowBlur;
        sCountBitmap = Bitmap.createBitmap((int) (f + ((float) i2)), (sRightBubRadius * 2) + i + i2, Bitmap.Config.ARGB_8888);
        Canvas tmpCanvas = new Canvas(sCountBitmap);
        float f2 = ((float) (sPaddinginRBub * 2)) + textLength;
        int i3 = sRightBubRadius;
        tmpCanvas.drawRoundRect(0.0f, 0.0f, f2, (float) (i3 * 2), (float) i3, (float) i3, bubblePaint);
        tmpCanvas.drawText(cnt, ((float) sPaddinginRBub) + (textLength / 2.0f), textY, textPaint);
    }

    private static Bitmap getStatusBitmap(int status, Context context) {
        Bitmap acceptableBmp;
        if (status == 0) {
            acceptableBmp = getBitmapFromVectorDrawable(context.getResources().getDrawable(33751846));
        } else if (status == 1 || status == 2) {
            acceptableBmp = getBitmapFromVectorDrawable(context.getResources().getDrawable(33751844));
        } else if (status != 3) {
            acceptableBmp = null;
        } else {
            acceptableBmp = getBitmapFromVectorDrawable(context.getResources().getDrawable(33751845));
        }
        if (acceptableBmp == null) {
            return acceptableBmp;
        }
        Bitmap tmpBmp = Bitmap.createBitmap(acceptableBmp.getWidth() + sLeftBubShadowX + mSideDp, acceptableBmp.getHeight() + sLeftBubShadowY + mSideDp, Bitmap.Config.ARGB_8888);
        Canvas tmpCanvas = new Canvas(tmpBmp);
        Paint paint = new Paint();
        paint.setShadowLayer((float) sLeftBubShadowBlur, (float) sLeftBubShadowX, (float) sLeftBubShadowY, sBubShadowColor);
        int i = sRightBubRadius;
        tmpCanvas.drawCircle((float) i, (float) i, (float) (i - 1), paint);
        tmpCanvas.drawBitmap(acceptableBmp, 0.0f, 0.0f, (Paint) null);
        if (!acceptableBmp.isRecycled()) {
            acceptableBmp.recycle();
        }
        return tmpBmp;
    }

    private static Bitmap getBitmapFromVectorDrawable(Drawable drawable) {
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    private static void insertMimeTypeToArrayList(String[] mimeTypes, View view) {
        if (view == null) {
            Log.i(HW_VIEW_LOG_TAG, "insertMimeTypeToArrayList error. view is null");
            return;
        }
        for (int i = 0; i != mimeTypes.length; i++) {
            String mimeType = mimeTypes[i];
            if (!view.getListenerInfo().mAcceptableMimeType.contains(mimeType)) {
                view.getListenerInfo().mAcceptableMimeType.add(mimeType);
            }
        }
    }

    public static void hwOnDrawShadow(Context context, ClipData data, View.DragShadowBuilder shadowBuilder, Bundle bundle, Surface surface) {
        if (bundle == null || data == null || context == null || shadowBuilder == null) {
            Log.e(HW_VIEW_LOG_TAG, "hwOnDrawShadow null return");
            return;
        }
        mBitmap = Bitmap.createBitmap((int) sOriWidth, (int) sOriHeight, Bitmap.Config.ARGB_8888);
        shadowBuilder.onDrawShadow(new Canvas(mBitmap));
        boolean hasDataText = false;
        if (data.getDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {
            hasDataText = true;
        }
        float scale = getScale(hasDataText);
        scaleSize = scale;
        decorateDragShadow(hasDataText, context);
        Point shadowSize = (Point) bundle.getParcelable("PointShadowSize");
        Point shadowTouchPoint = (Point) bundle.getParcelable("PointShadowTouch");
        if (shadowSize != null && shadowTouchPoint != null) {
            shadowTouchPoint.x = shadowSize.x / 2;
            shadowTouchPoint.y = (int) ((((((float) shadowSize.y) * 1.0f) / 2.0f) - (((((float) mBitmap.getHeight()) * 1.0f) * scale) / 2.0f)) + ((float) dipsToPixels(mTouchPointY, context)));
            sLeftIconPositionX = (((float) shadowTouchPoint.x) - ((((float) mBitmap.getWidth()) * scale) / 2.0f)) - ((float) sPaddingOutBub);
            sLeftIconPositionY = (((float) (shadowSize.y / 2)) - ((((float) mBitmap.getHeight()) * scale) / 2.0f)) - ((float) sPaddingOutBub);
            getCntBubBitMapAndPos(surface, context, shadowBuilder);
            drawProjectionShadow(context, shadowBuilder, hasDataText);
            if (scale < 1.0f || hasDataText) {
                sLeftStartPosition = ((float) shadowTouchPoint.x) - ((((float) mBitmap.getWidth()) * scale) / 2.0f);
                sTopStartPosition = (((float) (shadowSize.y / 2)) - ((((float) mBitmap.getHeight()) * scale) / 2.0f)) + ((float) dipsToPixels(mTouchPointY, context));
                mIsScaled = false;
                scaleCanvas(context, shadowBuilder, surface, hasDataText, scale);
                return;
            }
            sLeftStartPosition = 0.0f;
            sTopStartPosition = (float) dipsToPixels(mTouchPointY, context);
            scaleDragShadow(surface, scale, 1.0f, 1.0f);
            if (shadowBuilder.mClipTotalItemCnt >= 2) {
                setRightAnimation(surface, context, shadowBuilder);
                return;
            }
            mIsScaled = true;
            setAcceptableStateBubble(surface, context, shadowBuilder);
        }
    }

    private static void scaleCanvas(final Context context, final View.DragShadowBuilder shadowBuilder, final Surface surface, final boolean hasDataText, final float scale) {
        new Handler(context.getMainLooper()).post(new Runnable() {
            /* class android.view.HwViewEx.AnonymousClass2 */

            @Override // java.lang.Runnable
            public void run() {
                final float beginScale = (scale != 1.0f || !hasDataText) ? HwViewEx.sShortTextScale : 1.0f;
                final float endScale = scale;
                int duration = HwViewEx.sVaLongTextTime;
                if (scale == 1.0f && hasDataText) {
                    duration = HwViewEx.sVaShortTextTime;
                }
                if (!hasDataText) {
                    duration = HwViewEx.sVaPictureTime;
                }
                ValueAnimator va = ValueAnimator.ofFloat(beginScale, endScale);
                va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    /* class android.view.HwViewEx.AnonymousClass2.AnonymousClass1 */

                    @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                    public void onAnimationUpdate(ValueAnimator animation) {
                        HwViewEx.scaleDragShadow(surface, ((Float) animation.getAnimatedValue()).floatValue(), beginScale, endScale);
                    }
                });
                va.addListener(new AnimatorListenerAdapter() {
                    /* class android.view.HwViewEx.AnonymousClass2.AnonymousClass2 */

                    @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                    public void onAnimationEnd(Animator animation) {
                        HwViewEx.setBitmapSize();
                        if (shadowBuilder.mClipTotalItemCnt >= 2) {
                            HwViewEx.setRightAnimation(surface, context, shadowBuilder);
                            return;
                        }
                        boolean unused = HwViewEx.mIsScaled = true;
                        HwViewEx.setAcceptableStateBubble(surface, context, shadowBuilder);
                    }
                });
                va.setDuration((long) duration);
                va.start();
            }
        });
    }

    /* access modifiers changed from: private */
    public static void scaleDragShadow(Surface surface, float scale, float range1, float range2) {
        if (surface != null) {
            Canvas canvas = null;
            try {
                canvas = surface.lockCanvas(null);
                if (canvas != null) {
                    if (mBitmap != null) {
                        canvas.drawColor(0, PorterDuff.Mode.CLEAR);
                        if (scaleSize == 0.0f) {
                            Log.e(HW_VIEW_LOG_TAG, "scaleDragShadow return because scaleSize is zero");
                            surface.unlockCanvasAndPost(canvas);
                            return;
                        }
                        int width = mBitmap.getWidth();
                        int height = mBitmap.getHeight();
                        Matrix matrix = new Matrix();
                        matrix.preScale(scale, scale);
                        Bitmap newBitmap = Bitmap.createBitmap(mBitmap, 0, 0, width, height, matrix, false);
                        if (Math.abs(range1 - range2) < EPSILON) {
                            canvas.drawBitmap(newBitmap, sLeftStartPosition, sTopStartPosition, (Paint) null);
                        } else {
                            Paint paint = new Paint();
                            paint.setStyle(Paint.Style.STROKE);
                            paint.setAlpha(getPaintAlpha(scale, range1, range2));
                            canvas.drawBitmap(newBitmap, sLeftStartPosition, sLeftStartPosition, paint);
                        }
                        surface.unlockCanvasAndPost(canvas);
                        return;
                    }
                }
                Log.e(HW_VIEW_LOG_TAG, "scaleDragShadow null return");
                if (canvas != null) {
                    surface.unlockCanvasAndPost(canvas);
                }
            } catch (IllegalStateException e) {
                Log.e(HW_VIEW_LOG_TAG, "Unable to update drag shadow", e);
                if (0 == 0) {
                }
            } catch (IllegalArgumentException e2) {
                Log.e(HW_VIEW_LOG_TAG, "Surface was already locked", e2);
                if (0 == 0) {
                }
            } catch (Throwable th) {
                if (0 != 0) {
                    surface.unlockCanvasAndPost(null);
                }
                throw th;
            }
        } else {
            Log.e(HW_VIEW_LOG_TAG, "No active drag");
        }
    }

    private static int getPaintAlpha(float scale, float range1, float range2) {
        float percent;
        if (range1 - range2 > EPSILON) {
            percent = (range1 - scale) / (range1 - range2);
        } else if (range2 - range1 > EPSILON) {
            percent = (scale - range1) / (range2 - range1);
        } else {
            percent = 1.0f;
        }
        return (int) (255.0f * percent);
    }

    private static void decorateDragShadow(boolean hasDataText, Context context) {
        int backBitMapWidth;
        int backBitMapHeight;
        if (hasDataText) {
            int side = dipsToPixels(mSideDp, context) * 2;
            backBitMapWidth = ((int) sOriWidth) + side;
            backBitMapHeight = ((int) sOriHeight) + side;
        } else {
            backBitMapWidth = (int) sOriWidth;
            backBitMapHeight = (int) sOriHeight;
        }
        Bitmap backBitmap = Bitmap.createBitmap(backBitMapWidth, backBitMapHeight, Bitmap.Config.ARGB_8888, true);
        Canvas tempCanvas = new Canvas(backBitmap);
        Paint paint = new Paint();
        int color = getDialogColor(context);
        paint.setColor(color);
        tempCanvas.drawRect(new RectF(0.0f, 0.0f, (float) backBitMapWidth, (float) backBitMapHeight), paint);
        float roundCorner = getRoundCorner(context);
        Bitmap backBitmap2 = getRoundCornerBitmap(backBitmap, roundCorner, color);
        Bitmap contentBitmap = mBitmap;
        if (!hasDataText) {
            contentBitmap = getRoundCornerBitmap(contentBitmap, roundCorner, color);
        }
        mBitmap = mergeBitmap(backBitmap2, contentBitmap);
    }

    private static int getDialogColor(Context context) {
        TypedValue tv = new TypedValue();
        if (context.getTheme().resolveAttribute(context.getResources().getIdentifier("colorDialogBg", "attr", "androidhwext"), tv, true)) {
            return tv.data;
        }
        return -1;
    }

    private static float getRoundCorner(Context context) {
        TypedValue tv = new TypedValue();
        if (context.getTheme().resolveAttribute(context.getResources().getIdentifier("defaultCornerRadiusM", "attr", "androidhwext"), tv, true)) {
            return (float) TypedValue.complexToDimensionPixelSize(tv.data, context.getResources().getDisplayMetrics());
        }
        return (float) dipsToPixels(sRoundCorner, context);
    }

    private static Bitmap getRoundCornerBitmap(Bitmap bitmap, float roundPx, int color) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Bitmap bitmap2 = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap2);
        Rect rect = new Rect(0, 0, width, height);
        RectF rectF = new RectF(rect);
        Paint paint = new Paint();
        paint.setColor(color);
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return bitmap2;
    }

    private static float getScale(boolean hasDataText) {
        float scaleHeight;
        float scaleWidth;
        DisplayInfo displayInfo = new DisplayInfo();
        DisplayManagerGlobal.getInstance().getRealDisplay(0).getDisplayInfo(displayInfo);
        float shadowPercentH = (sOriHeight * 1.0f) / ((float) displayInfo.logicalHeight);
        if (shadowPercentH <= 0.0f || shadowPercentH > sHeightScale) {
            scaleHeight = ((((float) displayInfo.logicalHeight) * sHeightScale) * 1.0f) / sOriHeight;
        } else {
            scaleHeight = 1.0f;
        }
        if (hasDataText) {
            return scaleHeight;
        }
        float shadowPercentW = (sOriWidth * 1.0f) / ((float) displayInfo.logicalWidth);
        if (shadowPercentW <= 0.0f || shadowPercentW > WIDTH_SCALE) {
            scaleWidth = ((((float) displayInfo.logicalWidth) * WIDTH_SCALE) * 1.0f) / sOriWidth;
        } else {
            scaleWidth = 1.0f;
        }
        return Math.min(scaleWidth, scaleHeight);
    }

    private static int dipsToPixels(int dips, Context context) {
        return (int) ((((float) dips) * context.getResources().getDisplayMetrics().density) + mDp2PixAdd);
    }

    private static Bitmap mergeBitmap(Bitmap backBitmap, Bitmap frontBitmap) {
        Paint paint = new Paint();
        Canvas canvas = new Canvas(backBitmap);
        canvas.drawBitmap(frontBitmap, (((float) (backBitmap.getWidth() - frontBitmap.getWidth())) * 1.0f) / 2.0f, (((float) (backBitmap.getHeight() - frontBitmap.getHeight())) * 1.0f) / 2.0f, paint);
        canvas.save(31);
        canvas.restore();
        return backBitmap;
    }

    /* access modifiers changed from: private */
    public static void setBitmapSize() {
        int width = mBitmap.getWidth();
        int height = mBitmap.getHeight();
        Matrix matrix = new Matrix();
        float f = scaleSize;
        matrix.preScale(f, f);
        Bitmap newBitmap = Bitmap.createBitmap(mBitmap, 0, 0, width, height, matrix, false);
        mBitmap.recycle();
        mBitmap = newBitmap;
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0039, code lost:
        android.util.Log.i(android.view.HwViewEx.HW_VIEW_LOG_TAG, "setRightBubble exception");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:?, code lost:
        return;
     */
    /* JADX WARNING: Removed duplicated region for block: B:16:0x0038 A[ExcHandler: OutOfResourcesException | IllegalArgumentException | IllegalStateException (e java.lang.Throwable), Splitter:B:11:0x002e] */
    public static void setRightBubble(Surface surface, Context context, View.DragShadowBuilder shadowBuilder) {
        if (surface == null) {
            Log.e(HW_VIEW_LOG_TAG, "setRightBubble surface is null");
            return;
        }
        Canvas canvas = surface.lockCanvas(null);
        if (canvas == null) {
            Log.e(HW_VIEW_LOG_TAG, "setRightBubble canvas is null");
            return;
        }
        try {
            canvas.drawColor(0, PorterDuff.Mode.CLEAR);
            canvas.save();
            canvas.drawBitmap(mBitmap, sLeftStartPosition, sTopStartPosition, (Paint) null);
            canvas.restore();
            surface.unlockCanvasAndPost(canvas);
        } catch (Surface.OutOfResourcesException | IllegalArgumentException | IllegalStateException e) {
        } catch (Throwable th) {
            surface.unlockCanvasAndPost(canvas);
            throw th;
        }
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0052, code lost:
        android.util.Log.i(android.view.HwViewEx.HW_VIEW_LOG_TAG, "drawCurrentBubble execption");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:?, code lost:
        return;
     */
    /* JADX WARNING: Removed duplicated region for block: B:16:0x0051 A[ExcHandler: OutOfResourcesException | IllegalArgumentException | IllegalStateException (e java.lang.Throwable), Splitter:B:11:0x0047] */
    public static void drawCurrentBubble(Surface surface, Context context, View.DragShadowBuilder shadowBuilder, Bitmap statusBitmap) {
        if (surface == null) {
            Log.e(HW_VIEW_LOG_TAG, "drawCurrentBubble surface is null");
            return;
        }
        Canvas canvas = surface.lockCanvas(null);
        if (canvas == null) {
            Log.e(HW_VIEW_LOG_TAG, "setRightBubble canvas is null");
            return;
        }
        try {
            canvas.drawColor(0, PorterDuff.Mode.CLEAR);
            canvas.save();
            canvas.drawBitmap(mBitmap, sLeftStartPosition, sTopStartPosition, (Paint) null);
            Paint paint = new Paint();
            paint.setShadowLayer((float) sLeftBubShadowBlur, (float) sLeftBubShadowX, (float) sLeftBubShadowY, sBubShadowColor);
            canvas.drawBitmap(statusBitmap, sLeftIconPositionX, sLeftIconPositionY, paint);
            canvas.restore();
            surface.unlockCanvasAndPost(canvas);
        } catch (Surface.OutOfResourcesException | IllegalArgumentException | IllegalStateException e) {
        } catch (Throwable th) {
            surface.unlockCanvasAndPost(canvas);
            throw th;
        }
    }

    private static void drawIconAnimation(final Surface surface, final Context context, final View.DragShadowBuilder shadowBuilder) {
        if (context == null) {
            Log.i(HW_VIEW_LOG_TAG, "drawIconAnimation enter context is null");
        } else {
            new Handler(context.getMainLooper()).post(new Runnable() {
                /* class android.view.HwViewEx.AnonymousClass3 */

                @Override // java.lang.Runnable
                public void run() {
                    PathInterpolator pathInterpolator = new PathInterpolator(0.2f, 0.0f, 0.2f, 1.0f);
                    ValueAnimator va1 = ValueAnimator.ofFloat(0.6f, HwViewEx.MAX_SCALE_SIZE, 1.0f);
                    if (va1 == null) {
                        Log.i(HwViewEx.HW_VIEW_LOG_TAG, "drawIconAnimation va1 is null");
                        return;
                    }
                    va1.setInterpolator(pathInterpolator);
                    va1.setDuration(400L);
                    va1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        /* class android.view.HwViewEx.AnonymousClass3.AnonymousClass1 */

                        @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                        public void onAnimationUpdate(ValueAnimator animation) {
                            if (animation == null) {
                                Log.i(HwViewEx.HW_VIEW_LOG_TAG, "drawIconAnimation animation is null");
                            } else if (HwViewEx.sStopAnimationRun) {
                                HwViewEx.setRightBubble(Surface.this, context, shadowBuilder);
                            } else if (HwViewEx.sStatusBitmap == null || HwViewEx.sStatusBitmap.isRecycled()) {
                                Log.i(HwViewEx.HW_VIEW_LOG_TAG, "sStatusBitmap is recycled");
                            } else {
                                float val = ((Float) animation.getAnimatedValue()).floatValue();
                                Matrix matrix = new Matrix();
                                matrix.postScale(val, val);
                                HwViewEx.drawCurrentBubble(Surface.this, context, shadowBuilder, Bitmap.createBitmap(HwViewEx.sStatusBitmap, 0, 0, HwViewEx.sStatusBitmap.getWidth(), HwViewEx.sStatusBitmap.getHeight(), matrix, true));
                            }
                        }
                    });
                    va1.start();
                }
            });
        }
    }

    private static void drawSecondShadow(Canvas canvas, Context context, int colorId) {
        Paint shadowPaint = new Paint();
        shadowPaint.setAlpha(112);
        shadowPaint.setColor(colorId);
        shadowPaint.setShadowLayer(OVERLAY_SHADOW_BLUR, 0.0f, 0.0f, Color.parseColor(OVERLAY_SHADOW_COLOR));
        Matrix matrix = new Matrix();
        matrix.postScale(0.94f, 0.94f);
        Bitmap shadowBitmap = mBitmap.extractAlpha();
        int width = mBitmap.getWidth();
        int height = mBitmap.getHeight();
        canvas.drawBitmap(Bitmap.createBitmap(shadowBitmap, 0, 0, width, height, matrix, true), ((float) (sAddX / 2)) + ((((float) width) * SECOND_BACKGROUND_SCALESIZE) / 2.0f), ((float) (sAddY / 2)) + (((float) height) * SECOND_BACKGROUND_SCALESIZE) + ((float) dipsToPixels(6, context)), shadowPaint);
    }

    private static void drawThirdShadow(Canvas canvas, Context context, int colorId) {
        Paint shadowPaint = new Paint();
        shadowPaint.setAlpha(90);
        shadowPaint.setColor(colorId);
        shadowPaint.setShadowLayer(OVERLAY_SHADOW_BLUR, 0.0f, 0.0f, Color.parseColor(OVERLAY_SHADOW_COLOR));
        Matrix matrix = new Matrix();
        matrix.postScale(0.88f, 0.88f);
        Bitmap shadowBitmap = mBitmap.extractAlpha();
        int width = mBitmap.getWidth();
        int height = mBitmap.getHeight();
        canvas.drawBitmap(Bitmap.createBitmap(shadowBitmap, 0, 0, width, height, matrix, true), ((float) (sAddX / 2)) + ((((float) width) * THIRD_BACKGROUND_SCALESIZE) / 2.0f), ((float) (sAddY / 2)) + (((float) height) * THIRD_BACKGROUND_SCALESIZE) + ((float) dipsToPixels(12, context)), shadowPaint);
    }

    private static void drawProjectionShadow(Context context, View.DragShadowBuilder shadowBuilder, boolean hasDataText) {
        Bitmap tempBitmap = Bitmap.createBitmap(mBitmap.getWidth() + sAddX, mBitmap.getHeight() + sAddY, Bitmap.Config.ARGB_8888);
        Canvas tempCanvas = new Canvas(tempBitmap);
        if (!hasDataText) {
            int colorId = getDialogColor(context);
            if (shadowBuilder.mClipTotalItemCnt == 2) {
                drawSecondShadow(tempCanvas, context, colorId);
            } else if (shadowBuilder.mClipTotalItemCnt >= 3) {
                drawThirdShadow(tempCanvas, context, colorId);
                drawSecondShadow(tempCanvas, context, colorId);
            }
        }
        Paint shadowPaint = new Paint();
        shadowPaint.setAntiAlias(true);
        shadowPaint.setShadowLayer(FIRST_SHADOW_BLUR, 0.0f, (float) dipsToPixels(4, context), Color.parseColor("#4D000000"));
        tempCanvas.drawBitmap(mBitmap.extractAlpha(), (float) (sAddX / 2), (float) (sAddY / 2), shadowPaint);
        tempCanvas.drawBitmap(mBitmap, (float) (sAddX / 2), (float) (sAddY / 2), (Paint) null);
        bitmapRecycle(mBitmap);
        mBitmap = tempBitmap;
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x003e, code lost:
        android.util.Log.i(android.view.HwViewEx.HW_VIEW_LOG_TAG, "drawCurentRightIcon execption");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:?, code lost:
        return;
     */
    /* JADX WARNING: Removed duplicated region for block: B:16:0x003d A[ExcHandler: OutOfResourcesException | IllegalArgumentException | IllegalStateException (e java.lang.Throwable), Splitter:B:11:0x0033] */
    public static void drawCurentRightIcon(Surface surface, Bitmap countBitmap) {
        if (surface == null) {
            Log.e(HW_VIEW_LOG_TAG, "drawCurentRightIcon surface is null");
            return;
        }
        Canvas canvas = surface.lockCanvas(null);
        if (canvas == null) {
            Log.e(HW_VIEW_LOG_TAG, "drawCurentRightIcon canvas is null");
            return;
        }
        try {
            canvas.drawColor(0, PorterDuff.Mode.CLEAR);
            canvas.save();
            canvas.drawBitmap(mBitmap, sLeftStartPosition, sTopStartPosition, (Paint) null);
            canvas.drawBitmap(countBitmap, sRightPositionX, sRightPositionY, (Paint) null);
            canvas.restore();
            surface.unlockCanvasAndPost(canvas);
        } catch (Surface.OutOfResourcesException | IllegalArgumentException | IllegalStateException e) {
        } catch (Throwable th) {
            surface.unlockCanvasAndPost(canvas);
            throw th;
        }
    }

    /* access modifiers changed from: private */
    public static void mergeRightIconToBitmap() {
        float f = sOriWidth;
        Bitmap tempBitmap = Bitmap.createBitmap(((int) f) + sAddX, ((int) f) + sAddY, Bitmap.Config.ARGB_8888);
        Canvas tempCanvas = new Canvas(tempBitmap);
        tempCanvas.drawBitmap(mBitmap, sLeftStartPosition, sTopStartPosition, (Paint) null);
        tempCanvas.drawBitmap(sCountBitmap, sRightPositionX, sRightPositionY, (Paint) null);
        bitmapRecycle(mBitmap);
        bitmapRecycle(sCountBitmap);
        sCountBitmap = null;
        mBitmap = tempBitmap;
    }

    /* access modifiers changed from: private */
    public static void setRightAnimation(final Surface surface, final Context context, final View.DragShadowBuilder shadowBuilder) {
        if (shadowBuilder == null || shadowBuilder.mClipTotalItemCnt < 2) {
            Log.i(HW_VIEW_LOG_TAG, "setRightAnimation no need draw right Icon");
            bitmapRecycle(sCountBitmap);
            sCountBitmap = null;
            return;
        }
        new Handler(context.getMainLooper()).post(new Runnable() {
            /* class android.view.HwViewEx.AnonymousClass4 */

            @Override // java.lang.Runnable
            public void run() {
                PathInterpolator pathInterpolator = new PathInterpolator(0.2f, 0.0f, 0.2f, 1.0f);
                ValueAnimator animator = ValueAnimator.ofFloat(0.6f, HwViewEx.MAX_SCALE_SIZE, 1.0f);
                if (animator == null) {
                    Log.i(HwViewEx.HW_VIEW_LOG_TAG, "setRightAnimation va1 is null");
                    return;
                }
                animator.setInterpolator(pathInterpolator);
                animator.setDuration(400L);
                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    /* class android.view.HwViewEx.AnonymousClass4.AnonymousClass1 */

                    @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                    public void onAnimationUpdate(ValueAnimator animation) {
                        if (animation == null) {
                            Log.i(HwViewEx.HW_VIEW_LOG_TAG, "setRightAnimation animation is null");
                        } else if (HwViewEx.sCountBitmap == null) {
                            Log.i(HwViewEx.HW_VIEW_LOG_TAG, "setRightAnimation sCountBitmap is null");
                        } else {
                            float val = ((Float) animation.getAnimatedValue()).floatValue();
                            Matrix matrix = new Matrix();
                            matrix.postScale(val, val);
                            HwViewEx.drawCurentRightIcon(Surface.this, Bitmap.createBitmap(HwViewEx.sCountBitmap, 0, 0, HwViewEx.sCountBitmap.getWidth(), HwViewEx.sCountBitmap.getHeight(), matrix, true));
                        }
                    }
                });
                animator.addListener(new AnimatorListenerAdapter() {
                    /* class android.view.HwViewEx.AnonymousClass4.AnonymousClass2 */

                    @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                    public void onAnimationEnd(Animator animation) {
                        boolean unused = HwViewEx.mIsScaled = true;
                        HwViewEx.mergeRightIconToBitmap();
                        HwViewEx.setAcceptableStateBubble(Surface.this, context, shadowBuilder);
                    }
                });
                animator.start();
            }
        });
    }
}
