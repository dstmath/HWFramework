package com.huawei.hwtransition;

import android.animation.TimeInterpolator;
import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import com.huawei.hwtransition.algorithm.BaseTransition;
import com.huawei.hwtransition.algorithm.BoxTransition;
import com.huawei.hwtransition.algorithm.CylinderTransition;
import com.huawei.hwtransition.algorithm.DepthTransition;
import com.huawei.hwtransition.algorithm.EditDepthTransition;
import com.huawei.hwtransition.algorithm.FlipOverTransition;
import com.huawei.hwtransition.algorithm.GoRotateTransition;
import com.huawei.hwtransition.algorithm.PageTransition;
import com.huawei.hwtransition.algorithm.Pendulum;
import com.huawei.hwtransition.algorithm.PushTransition;
import com.huawei.hwtransition.algorithm.TranlationTransition;
import com.huawei.hwtransition.algorithm.WindMillTransition;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HwTransition {
    private static final float ALPHA_OPAQUE = 0.996f;
    private static final float ALPHA_TRANSPARENT = 0.004f;
    public static final int BG_MODE_CENTER = 2;
    public static final int BG_MODE_TOP = 1;
    public static final int CAMERA_Z_DEF = -8;
    private static final int DEFAULT_ANIM_TARGET_FRAME_INTERVAL_MILLI = 1000;
    public static final int EDGE_MODE_CYCLE = 2;
    public static final int EDGE_MODE_ELASTIC = 1;
    public static final boolean IS_DBG = false;
    protected static final String TAG = "HwTransition";
    public static final String TRANS_TYPE_BOX = "Box";
    public static final String TRANS_TYPE_CYLINDER = "Cylinder";
    public static final String TRANS_TYPE_DEPTH = "Depth";
    public static final String TRANS_TYPE_EDIT_DEPTH = "EditDepth";
    public static final String TRANS_TYPE_FLIPOVER = "Flipover";
    public static final String TRANS_TYPE_NORMAL = "Normal";
    public static final String TRANS_TYPE_PAGE = "Page";
    public static final String TRANS_TYPE_PENDULUM = "Pendulum";
    public static final String TRANS_TYPE_PUSH = "Push";
    public static final String TRANS_TYPE_ROTATION = "Rotation";
    public static final String TRANS_TYPE_WINDMILL = "Windmill";
    public static final int TYPE_CHILD = 0;
    public static final int TYPE_CONTROL = 1;
    protected static final String VERSION = "0.3.01";
    private BaseTransition mActiveTransition;
    private String mActiveTransitionType;
    int mAlphaLeftIdx;
    List<View> mAlphaViews;
    List<Float> mAlphas;
    List<AnimateInfo> mAnimInfoEnds;
    List<AnimateInfo> mAnimInfos;
    private int mAnimationTargetFrameInterval;
    private Bitmap mBackground;
    Rect mBgDstRect;
    private int mBgMode;
    Paint mBgPaint;
    Rect mBgSrcRect;
    private Display mDisplay;
    private Method mDisplayRealMethod;
    Point mDisplayRealSize;
    Point mDisplaySize;
    private Method mDrawMethod;
    private int mEdgeMode;
    Paint mErasePaint;
    private int mFirstOffset;
    private float mInitOffset;
    TimeInterpolator mInterpolator;
    private boolean mIsBgStatic;
    boolean mIsForceDraw;
    private boolean mIsTargetAnimating;
    boolean mIsTransparent;
    private int mLeftIndex;
    private int mLeftScreen;
    private int mMaxPage;
    private float mOffset;
    private int mPageSpacing;
    private Paint mPaint;
    private float mPreFrameElapse;
    private int mRightScreen;
    private View mTargetView;
    private float mTotoalMissElapse;
    BaseTransition.TransformationInfo mTransInfo;
    private HashMap<String, BaseTransition> mTransitionsMap;
    private float mTravelRatio;

    public HwTransition(View targetView) {
        this(targetView, "Normal");
    }

    public HwTransition(View targetView, String type) {
        this(targetView, type, 0);
    }

    public HwTransition(View targetView, String type, int pageSpacing) {
        this.mAnimInfos = new ArrayList(0);
        this.mAnimInfoEnds = new ArrayList(0);
        this.mAlphaViews = new ArrayList(0);
        this.mAlphas = new ArrayList(0);
        this.mAlphaLeftIdx = -1;
        this.mIsForceDraw = false;
        this.mIsTransparent = false;
        this.mBgPaint = new Paint();
        this.mErasePaint = new Paint();
        this.mBgDstRect = new Rect();
        this.mBgSrcRect = new Rect();
        this.mInterpolator = new AccelerateDecelerateInterpolator();
        this.mIsBgStatic = false;
        this.mBgMode = 2;
        this.mOffset = 0.0f;
        this.mInitOffset = 0.0f;
        this.mTravelRatio = 1.0f;
        this.mFirstOffset = 0;
        this.mLeftIndex = 0;
        this.mTransitionsMap = new HashMap<>(0);
        this.mAnimationTargetFrameInterval = DEFAULT_ANIM_TARGET_FRAME_INTERVAL_MILLI;
        this.mTotoalMissElapse = 0.0f;
        this.mPreFrameElapse = -1.0f;
        this.mEdgeMode = 1;
        this.mLeftScreen = -1;
        this.mRightScreen = -1;
        this.mMaxPage = -1;
        this.mIsTargetAnimating = false;
        Log.d(TAG, "hwtransition version = 0.3.01, targetView = " + targetView);
        if (targetView != null) {
            this.mPaint = new Paint();
            this.mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
            this.mTargetView = targetView;
            this.mDrawMethod = getDrawChildMethod();
            this.mDisplayRealMethod = getDisplayRealMethod();
            initTransitions();
            setTransitionType(type);
            setPageSpacing(pageSpacing);
        }
    }

    /* access modifiers changed from: package-private */
    public static class ShadowView extends View {
        private static final int MAX_CHANNEL_VALUE = 255;
        Bitmap mBmp;
        Paint mPaint = new Paint();

        private ShadowView(View v) {
            super(v.getContext());
        }

        static ShadowView createShadow(View v) {
            ShadowView shadowView = new ShadowView(v);
            if (!shadowView.copyView(v)) {
                return null;
            }
            return shadowView;
        }

        @Override // android.view.View
        public void setAlpha(float alpha) {
            this.mPaint.setAlpha((int) (255.0f * alpha));
        }

        @Override // android.view.View
        public void draw(Canvas canvas) {
            super.draw(canvas);
            if (this.mBmp == null || canvas == null) {
                Log.e(HwTransition.TAG, "bitmap is null, should not come here!!! ");
            } else {
                canvas.drawBitmap(this.mBmp, 0.0f, 0.0f, this.mPaint);
            }
        }

        public void clearBitmap() {
            if (this.mBmp != null) {
                this.mBmp.recycle();
            }
        }

        public boolean copyView(View view) {
            if (view == null) {
                Log.e(HwTransition.TAG, "view is null");
                return false;
            }
            int color = view.getDrawingCacheBackgroundColor();
            view.setWillNotCacheDrawing(false);
            view.setDrawingCacheBackgroundColor(0);
            if (color != 0) {
                view.destroyDrawingCache();
            }
            view.buildDrawingCache();
            Bitmap cacheBitmap = view.getDrawingCache();
            if (cacheBitmap == null) {
                Log.e(HwTransition.TAG, "copyView failed: " + view);
                return false;
            }
            this.mBmp = Bitmap.createBitmap(cacheBitmap);
            view.destroyDrawingCache();
            view.setWillNotCacheDrawing(view.willNotCacheDrawing());
            view.setDrawingCacheBackgroundColor(color);
            return true;
        }
    }

    /* access modifiers changed from: package-private */
    public static class ViewInfo {
        float alpha = -1.0f;
        float fraction;
        int index;
        boolean isEdge;
        boolean isOverScrollFirst;
        boolean isOverScrollLast;
        float relativeTrans = 0.0f;
        ShadowView shadowView;
        View view;

        ViewInfo(View inputView) {
            this.view = inputView;
        }

        /* access modifiers changed from: package-private */
        public void clean() {
            if (this.shadowView != null) {
                this.shadowView.clearBitmap();
                this.shadowView = null;
            }
            this.view = null;
        }
    }

    /* access modifiers changed from: package-private */
    public static class AnimateInfo {
        private static final long DEFAULT_DURATION = 500;
        private static final long DEFAULT_START_TIME = -1;
        long drawingTime;
        long duration = DEFAULT_DURATION;
        boolean isReverse = false;
        boolean isScrolling;
        long startTime = DEFAULT_START_TIME;
        int type = 1;
        List<ViewInfo> views = new ArrayList(0);

        AnimateInfo(int typeValue) {
            this.type = typeValue;
        }

        public void clear() {
            for (ViewInfo vi : this.views) {
                vi.clean();
            }
            this.views.clear();
            this.views = null;
        }
    }

    private Method getDrawChildMethod() {
        Method method = null;
        try {
            method = ViewGroup.class.getDeclaredMethod("drawChild", Canvas.class, View.class, Long.TYPE);
            method.setAccessible(true);
            return method;
        } catch (NoSuchMethodException e) {
            Log.e(TAG, "getDrawChildMethod catch an error");
            return method;
        }
    }

    private Method getDisplayRealMethod() {
        try {
            return Display.class.getMethod("getRealSize", Point.class);
        } catch (NoSuchMethodException e) {
            Log.e(TAG, "getDisplayRealMethod catch an error");
            return null;
        }
    }

    public final void setTransitionType(String type) {
        BaseTransition transition = this.mTransitionsMap.get(type);
        if (transition != null) {
            this.mActiveTransition = transition;
            this.mActiveTransition.reset();
            TimeInterpolator inter = this.mActiveTransition.getDefInterpolator();
            if (inter != null) {
                setInterpolator(inter);
            }
            this.mActiveTransitionType = type;
            return;
        }
        Log.w(TAG, "setTransitionType failed, no such type : " + type);
    }

    public static float getTransitonScaleFactor() {
        return 0.844f;
    }

    public String getTransitionType() {
        return this.mActiveTransitionType;
    }

    public void setCameraDistance(float cameraDistance) {
        if (this.mActiveTransition != null) {
            this.mActiveTransition.setCameraDistance(cameraDistance);
        }
    }

    public void setBackground(Bitmap background) {
        this.mBackground = background;
        if (this.mActiveTransition.mIsUseBg) {
            this.mActiveTransition.setAlphaMode(this.mBackground == null);
        }
        if (background != null && this.mDisplay == null) {
            if (this.mTargetView.getContext().getSystemService("window") instanceof WindowManager) {
                this.mDisplay = ((WindowManager) this.mTargetView.getContext().getSystemService("window")).getDefaultDisplay();
            }
            this.mDisplaySize = new Point();
            this.mDisplayRealSize = new Point();
        }
    }

    public Bitmap getBackground() {
        return this.mBackground;
    }

    public void setAnimationFPS(int minfps) {
        if (minfps > 0) {
            this.mAnimationTargetFrameInterval = DEFAULT_ANIM_TARGET_FRAME_INTERVAL_MILLI / minfps;
        }
    }

    public void setViewDuration(View view, long duration) {
        for (AnimateInfo info : this.mAnimInfos) {
            if (info.views.get(0).view == view) {
                info.duration = duration;
                return;
            }
        }
    }

    public void setBackgroundOffset(boolean isBgStatic, float offset) {
        this.mIsBgStatic = isBgStatic;
        this.mOffset = offset;
    }

    public void setBackgroundMode(int bgMode) {
        this.mBgMode = bgMode;
    }

    public void setAlphaMode(boolean isUseAlpha) {
        this.mActiveTransition.setAlphaMode(isUseAlpha);
    }

    public void setLayerTransparent(boolean isTransparent) {
        this.mIsTransparent = isTransparent;
    }

    public boolean getLayerTransparent() {
        return this.mIsTransparent;
    }

    public static void setWindMillPageAngle(int pageAngle) {
        WindMillTransition.setPageAngle(pageAngle);
    }

    public void setEdgeMode(int edgeMode) {
        this.mEdgeMode = edgeMode;
    }

    public void setMaxPage(int maxPage) {
        this.mMaxPage = maxPage;
    }

    public float getLayerOffset(float offset, int stepValue) {
        float offsetInternal = offset;
        if (stepValue == 1) {
            if (Float.isNaN(offsetInternal)) {
                return 0.0f;
            }
        } else if (this.mBackground != null && this.mActiveTransition.mIsUseBg) {
            float step = this.mTravelRatio / ((float) (stepValue - 1));
            float overx = (offsetInternal - this.mInitOffset) % step;
            offsetInternal -= overx;
            if (overx / step > 0.5f) {
                offsetInternal += step;
            }
        }
        return offsetInternal;
    }

    public void setWallpaperTravel(int travelwidth, int width) {
        if (width != 0) {
            this.mInitOffset = ((float) (width - travelwidth)) / (2.0f * ((float) width));
            this.mTravelRatio = ((float) travelwidth) / ((float) width);
        }
    }

    public final void setPageSpacing(int pageSpacing) {
        this.mPageSpacing = pageSpacing;
    }

    private void initTransitions() {
        if (this.mTransitionsMap.size() == 0) {
            this.mTransitionsMap.put("Normal", new TranlationTransition());
            this.mTransitionsMap.put("Depth", new DepthTransition());
            this.mTransitionsMap.put("Windmill", new WindMillTransition());
            this.mTransitionsMap.put("Push", new PushTransition());
            this.mTransitionsMap.put("Box", new BoxTransition());
            this.mTransitionsMap.put("Flipover", new FlipOverTransition());
            this.mTransitionsMap.put("Rotation", new GoRotateTransition());
            this.mTransitionsMap.put("Page", new PageTransition());
            this.mTransitionsMap.put("Cylinder", new CylinderTransition());
            this.mTransitionsMap.put("Pendulum", new Pendulum());
            this.mTransitionsMap.put(TRANS_TYPE_EDIT_DEPTH, new EditDepthTransition());
        }
    }

    public boolean is3DAnimation() {
        if (this.mActiveTransition.getAnimationType().equals("3D")) {
            return true;
        }
        return false;
    }

    public HashMap<String, BaseTransition> getAvailableTransitions() {
        return this.mTransitionsMap;
    }

    public boolean startViewAnimation(View view) {
        if (view == null || this.mActiveTransition == null) {
            return false;
        }
        cancelPreviousAnimation(view);
        AnimateInfo info = new AnimateInfo(1);
        this.mActiveTransition.setLayoutType(1);
        this.mActiveTransition.setOrientation(1);
        info.views.add(new ViewInfo(view));
        this.mAnimInfos.add(info);
        view.invalidate();
        return true;
    }

    public boolean startAnimation(View view) {
        ShadowView shadowView;
        if (view == null || this.mActiveTransition == null || (shadowView = ShadowView.createShadow(view)) == null) {
            return false;
        }
        cancelPreviousAnimation(view);
        AnimateInfo info = new AnimateInfo(1);
        info.isReverse = true;
        this.mActiveTransition.setLayoutType(1);
        this.mActiveTransition.setOrientation(1);
        info.views.add(new ViewInfo(view));
        ViewInfo vi = new ViewInfo(view);
        vi.shadowView = shadowView;
        info.views.add(vi);
        this.mAnimInfos.add(info);
        view.invalidate();
        return true;
    }

    private void cancelPreviousAnimation(View view) {
        for (AnimateInfo info : this.mAnimInfos) {
            if (info.views.get(0).view == view) {
                onAnimationEnd(info);
                return;
            }
        }
    }

    public void setInterpolator(TimeInterpolator interpolator) {
        this.mInterpolator = interpolator;
    }

    public boolean animateDraw(Canvas canvas) {
        float normalizedTime;
        float nowTime;
        if (this.mAnimInfos.size() <= 0) {
            return false;
        }
        if (this.mTargetView == null || this.mActiveTransition == null || this.mIsForceDraw) {
            if (this.mIsForceDraw) {
                this.mIsForceDraw = false;
            }
            return false;
        }
        long currentTime = this.mTargetView.getDrawingTime();
        for (AnimateInfo info : this.mAnimInfos) {
            if (((float) info.startTime) == -1.0f) {
                info.startTime = currentTime;
                onAnimationStart(info);
            }
            float elapse = (float) (currentTime - info.startTime);
            if (this.mPreFrameElapse == -1.0f) {
                this.mPreFrameElapse = elapse;
            }
            if (elapse - this.mPreFrameElapse > ((float) this.mAnimationTargetFrameInterval)) {
                this.mTotoalMissElapse += (elapse - this.mPreFrameElapse) - ((float) this.mAnimationTargetFrameInterval);
            }
            this.mPreFrameElapse = elapse;
            float elapse2 = elapse - this.mTotoalMissElapse;
            if (info.duration != 0) {
                normalizedTime = elapse2 / ((float) info.duration);
            } else {
                normalizedTime = currentTime < info.startTime ? 0.0f : 1.0f;
            }
            float minValue = normalizedTime < 1.0f ? normalizedTime : 1.0f;
            float normalizedTime2 = minValue > 0.01f ? minValue : 0.01f;
            if (info.isReverse) {
                nowTime = 1.0f - normalizedTime2;
            } else {
                nowTime = normalizedTime2;
            }
            if (this.mInterpolator != null) {
                nowTime = this.mInterpolator.getInterpolation(nowTime);
            }
            float nowTime2 = ((float) ((int) (100000.0f * nowTime))) / 100000.0f;
            int length = info.views.size();
            for (int i = 0; i < length; i++) {
                ViewInfo vi = info.views.get(i);
                vi.fraction = nowTime2;
                if (i % 2 != 0) {
                    vi.fraction -= 4.0f;
                }
            }
            animView(canvas, info);
            if (normalizedTime2 >= 1.0f) {
                this.mAnimInfoEnds.add(info);
            }
        }
        for (AnimateInfo info2 : this.mAnimInfoEnds) {
            onAnimationEnd(info2);
        }
        this.mAnimInfoEnds.clear();
        this.mTargetView.invalidate();
        return true;
    }

    private void onAnimationStart(AnimateInfo info) {
        this.mTotoalMissElapse = 0.0f;
        this.mPreFrameElapse = -1.0f;
    }

    private void onAnimationEnd(AnimateInfo info) {
        this.mAnimInfos.remove(info);
        for (ViewInfo vi : info.views) {
            if (vi.alpha != -1.0f) {
                vi.view.setAlpha(vi.alpha);
            }
        }
        info.clear();
    }

    public boolean animateDispatchDraw(Canvas canvas, int transitonX, boolean isScrolling) {
        int index;
        if (canvas == null || this.mTargetView == null || this.mActiveTransition == null || !(this.mTargetView instanceof ViewGroup)) {
            return false;
        }
        int left = this.mLeftScreen;
        int right = this.mRightScreen;
        int transX = transitonX;
        if (right < left) {
            float[] values = new float[9];
            canvas.getMatrix().getValues(values);
            canvas.translate(-values[2], 0.0f);
            if (transitonX < 0) {
                transX = this.mTargetView.getWidth() + transitonX + (this.mTargetView.getWidth() * left);
            } else {
                transX = (transitonX % this.mTargetView.getWidth()) + (this.mTargetView.getWidth() * left);
            }
            canvas.translate((float) (-transX), 0.0f);
        }
        ViewGroup viewGroup = (ViewGroup) this.mTargetView;
        View child = null;
        float scrollProgress = 0.0f;
        AnimateInfo info = new AnimateInfo(0);
        info.drawingTime = viewGroup.getDrawingTime();
        info.isScrolling = isScrolling;
        int index2 = 0;
        int childCount = viewGroup.getChildCount();
        while (index2 < childCount) {
            child = viewGroup.getChildAt(index2);
            if (index2 == 0) {
                if (this.mActiveTransition.isHorizental()) {
                    this.mFirstOffset = child.getLeft();
                } else {
                    this.mFirstOffset = child.getTop();
                }
            }
            if (right >= left) {
                scrollProgress = TransitionUtil.getScrollProgress(viewGroup, transX, child, index2, this.mPageSpacing);
                if (scrollProgress < 1.0f) {
                    break;
                }
            }
            index2++;
        }
        if (right < left) {
            index2 = left;
            child = viewGroup.getChildAt(index2);
            scrollProgress = TransitionUtil.getScrollProgress(viewGroup, transX, child, index2, this.mPageSpacing);
        }
        float scrollProgress2 = ((float) ((int) (100000.0f * scrollProgress))) / 100000.0f;
        this.mLeftIndex = index2;
        if (index2 < childCount) {
            if ((scrollProgress2 == 0.0f && !isScrolling) || !(this.mAlphaLeftIdx == -1 || this.mAlphaLeftIdx == index2)) {
                int sz = this.mAlphaViews.size();
                for (int j = 0; j < sz; j++) {
                    this.mAlphaViews.get(j).setAlpha(this.mAlphas.get(j).floatValue());
                }
                this.mAlphaViews.clear();
                this.mAlphaLeftIdx = -1;
                if (scrollProgress2 == 0.0f && !isScrolling) {
                    return false;
                }
            }
            ViewInfo view = new ViewInfo(child);
            view.fraction = scrollProgress2;
            view.isOverScrollFirst = index2 == 0 && scrollProgress2 < 0.0f;
            view.isOverScrollLast = index2 == childCount + -1;
            view.index = index2;
            info.views.add(view);
            if (right < left) {
                view.isOverScrollFirst = false;
                view.isOverScrollLast = false;
            }
            if (scrollProgress2 > 0.0f && !view.isOverScrollLast) {
                if (right < left) {
                    index = right;
                } else {
                    index = index2 + 1;
                }
                ViewInfo view2 = new ViewInfo(viewGroup.getChildAt(index));
                view2.fraction = -1.0f + scrollProgress2;
                view2.index = index;
                if (right < left) {
                    view2.relativeTrans = (float) TransitionUtil.getChildOffset(viewGroup, childCount, this.mPageSpacing);
                }
                info.views.add(view2);
            }
            animView(canvas, info);
            for (ViewInfo vi : info.views) {
                if (vi.alpha != -1.0f && !this.mAlphaViews.contains(vi.view)) {
                    this.mAlphaLeftIdx = this.mLeftIndex;
                    this.mAlphaViews.add(vi.view);
                    this.mAlphas.add(Float.valueOf(vi.alpha));
                }
            }
            info.clear();
            return true;
        }
        Log.e(TAG, "error find progress, no view is visible");
        return false;
    }

    public boolean animateDispatchDraw(Canvas canvas, int transitonX, boolean isScrolling, int leftScreen, int rightScreen) {
        if (canvas == null) {
            return false;
        }
        if (this.mTargetView == null || this.mActiveTransition == null || !(this.mTargetView instanceof ViewGroup)) {
            Log.w(TAG, "animateDispatchDraw mTargetView = " + this.mTargetView + ", mActiveTransition " + this.mActiveTransition + ", mTargetView = " + this.mTargetView);
            return false;
        }
        ViewGroup viewGroupObject = (ViewGroup) this.mTargetView;
        int childCount = viewGroupObject.getChildCount();
        if (leftScreen < 0 || leftScreen >= childCount) {
            Log.w(TAG, "animateDispatchDraw leftScreen out of range " + leftScreen + " / " + childCount);
            return true;
        } else if (rightScreen < 0 || rightScreen >= childCount) {
            Log.w(TAG, "animateDispatchDraw rightScreen out of range " + rightScreen + " / " + childCount);
            return false;
        } else if (this.mEdgeMode == 2) {
            this.mLeftScreen = leftScreen;
            this.mRightScreen = rightScreen;
            return animateDispatchDraw(canvas, transitonX, isScrolling);
        } else {
            AnimateInfo info = new AnimateInfo(0);
            info.drawingTime = viewGroupObject.getDrawingTime();
            info.isScrolling = isScrolling;
            int index = leftScreen;
            while (index <= rightScreen) {
                View child = viewGroupObject.getChildAt(index);
                if (index == 0) {
                    if (this.mActiveTransition.isHorizental()) {
                        this.mFirstOffset = child.getLeft();
                    } else {
                        this.mFirstOffset = child.getTop();
                    }
                }
                float scrollProgress = TransitionUtil.getScrollProgress(viewGroupObject, transitonX, child, index, this.mPageSpacing);
                ViewInfo view = new ViewInfo(child);
                view.fraction = scrollProgress;
                if (this.mIsTargetAnimating && TRANS_TYPE_EDIT_DEPTH.equals(this.mActiveTransitionType)) {
                    int centerIdx = (int) (((float) (rightScreen + leftScreen)) / 2.0f);
                    if (rightScreen + 1 == this.mMaxPage && leftScreen + 1 == rightScreen) {
                        centerIdx = rightScreen;
                    }
                    if (centerIdx == leftScreen && leftScreen != 0) {
                        Log.w(TAG, "leftScreen is not 0 when only two screen shows ! centerIdx = " + centerIdx + ", " + leftScreen + " " + rightScreen);
                    }
                    if (centerIdx == index) {
                        view.fraction = 0.0f;
                    }
                }
                view.isOverScrollFirst = index == 0 && scrollProgress <= 0.0f;
                view.isEdge = view.isOverScrollFirst || view.isOverScrollLast;
                view.index = index;
                view.isOverScrollLast = index == childCount + -1 && scrollProgress >= 0.0f;
                info.views.add(view);
                index++;
            }
            if (leftScreen == 0 && info.views.get(leftScreen).isOverScrollFirst && leftScreen < rightScreen) {
                info.views.get(leftScreen + 1).isOverScrollFirst = true;
            }
            if (rightScreen == childCount - 1) {
                int last = info.views.size() - 1;
                if (info.views.get(last).isOverScrollLast && leftScreen < rightScreen) {
                    info.views.get(last - 1).isOverScrollLast = true;
                }
            }
            int size = this.mAlphaViews.size();
            for (int j = 0; j < size; j++) {
                this.mAlphaViews.get(j).setAlpha(this.mAlphas.get(j).floatValue());
            }
            this.mAlphaViews.clear();
            animView(canvas, info);
            return true;
        }
    }

    @SuppressLint({"WrongConstant"})
    private boolean animView(Canvas canvas, AnimateInfo info) {
        int num = this.mActiveTransition.getBreakTimes();
        if (this.mBackground != null) {
            try {
                this.mDisplayRealMethod.invoke(this.mDisplay, this.mDisplayRealSize);
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "Catch IllegalArgumentException");
            } catch (IllegalAccessException e2) {
                Log.e(TAG, "Catch IllegalAccessException");
            } catch (InvocationTargetException e3) {
                Log.e(TAG, "Catch InvocationTargetException");
            }
            this.mDisplay.getSize(this.mDisplaySize);
        }
        int targetViewWidth = this.mTargetView.getWidth();
        int targetViewHeight = this.mTargetView.getHeight();
        int n = info.views.size();
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < num; j++) {
                ViewInfo viewInfoObject = info.views.get(this.mActiveTransition.getDrawingOrder(n, i, j, info.views.get(i).fraction));
                View child = viewInfoObject.view;
                int childLeft = child.getLeft();
                int childTop = child.getTop();
                if (this.mTargetView == child) {
                    childLeft = 0;
                    childTop = 0;
                }
                int part = this.mActiveTransition.getBreakOrder(j, viewInfoObject.fraction);
                this.mActiveTransition.setState(info.isScrolling);
                this.mTransInfo = this.mActiveTransition.getTransformation(part, viewInfoObject.isOverScrollFirst, viewInfoObject.isOverScrollLast, viewInfoObject.fraction, viewInfoObject.isEdge, this.mTargetView, child, this.mPageSpacing);
                if (this.mTransInfo != null && (!this.mTransInfo.mIsAlphaDirty || this.mTransInfo.mAlpha >= ALPHA_TRANSPARENT)) {
                    int savedCount = canvas.save();
                    canvas.translate(viewInfoObject.relativeTrans, 0.0f);
                    if (!this.mTransInfo.mIsBoundsDirty) {
                        AlgorithmUtil.getTransformRect(child, this.mTransInfo.mBounds);
                    }
                    this.mTransInfo.mBounds.offset(childLeft - 0, childTop - 0);
                    if (this.mBackground != null && this.mTransInfo.mIsBackgroundDirty) {
                        this.mErasePaint.setColor(Constants.COLOR_BLACK);
                        this.mBgDstRect.set(this.mTransInfo.mBounds);
                        if (this.mActiveTransition.isHorizental()) {
                            this.mBgDstRect.top = 0;
                            this.mBgDstRect.bottom = targetViewHeight;
                            if (part == 0) {
                                this.mBgDstRect.left = childLeft - this.mFirstOffset;
                            } else if (part == num - 1) {
                                this.mBgDstRect.right = (childLeft + targetViewWidth) - this.mFirstOffset;
                            }
                        } else {
                            this.mBgDstRect.left = 0;
                            this.mBgDstRect.right = targetViewWidth;
                            if (part == 0) {
                                this.mBgDstRect.top = childTop - this.mFirstOffset;
                            } else if (part == num - 1) {
                                this.mBgDstRect.bottom = (childTop + targetViewHeight) - this.mFirstOffset;
                            }
                        }
                        if (viewInfoObject.isOverScrollFirst || viewInfoObject.isOverScrollLast) {
                            canvas.save();
                            canvas.clipRect(this.mBgDstRect);
                            canvas.drawPaint(this.mErasePaint);
                            canvas.restore();
                        }
                    }
                    if (this.mTransInfo.mIsMatrixDirty) {
                        canvas.translate((float) (childLeft - 0), (float) (childTop - 0));
                        canvas.concat(this.mTransInfo.mMatrix);
                        canvas.translate((float) (0 - childLeft), (float) (0 - childTop));
                    }
                    if (this.mBackground != null && this.mTransInfo.mIsBackgroundDirty) {
                        float distance = 0.0f;
                        int targetChildCount = ((ViewGroup) this.mTargetView).getChildCount();
                        if (this.mActiveTransition.isHorizental()) {
                            if (this.mIsBgStatic) {
                                distance = ((float) (this.mBackground.getWidth() - targetViewWidth)) * this.mOffset;
                            } else if (targetChildCount > 1) {
                                this.mOffset = (this.mTravelRatio * ((float) viewInfoObject.index)) / ((float) (targetChildCount - 1));
                                distance = ((float) (this.mBackground.getWidth() - targetViewWidth)) * (this.mInitOffset + this.mOffset);
                            }
                            this.mBgSrcRect.set((int) (((((float) this.mBgDstRect.left) + distance) - ((float) childLeft)) + ((float) this.mFirstOffset) + 0.5f), 0, (int) (((((float) this.mBgDstRect.right) + distance) - ((float) childLeft)) + ((float) this.mFirstOffset) + 0.5f), this.mBgDstRect.height());
                            switch (this.mBgMode) {
                                case 2:
                                    int dh = (int) (((float) (this.mBackground.getHeight() - this.mBgDstRect.height())) / 2.0f);
                                    if (dh <= 0) {
                                        dh = 0;
                                    }
                                    if (this.mBackground.getHeight() > this.mDisplayRealSize.y) {
                                        dh += (int) (((float) (this.mBackground.getHeight() - this.mDisplaySize.y)) / 2.0f);
                                    }
                                    this.mBgSrcRect.offset(0, dh);
                                    break;
                            }
                        } else {
                            if (this.mIsBgStatic) {
                                distance = ((float) (this.mBackground.getHeight() - targetViewHeight)) * this.mOffset;
                            } else if (targetChildCount > 1) {
                                this.mOffset = (this.mTravelRatio * ((float) viewInfoObject.index)) / ((float) (targetChildCount - 1));
                                distance = ((float) (this.mBackground.getHeight() - targetViewHeight)) * (this.mInitOffset + this.mOffset);
                            }
                            this.mBgSrcRect.set(0, (int) (((((float) this.mBgDstRect.top) + distance) - ((float) childTop)) + 0.5f), this.mBackground.getWidth(), (int) (((((float) this.mBgDstRect.bottom) + distance) - ((float) childTop)) + 0.5f));
                            switch (this.mBgMode) {
                                case 2:
                                    if (this.mBackground.getWidth() > this.mDisplayRealSize.x) {
                                        this.mBgSrcRect.offset((int) (((float) (this.mBackground.getWidth() - this.mDisplaySize.x)) / 2.0f), 0);
                                        break;
                                    }
                                    break;
                            }
                        }
                        canvas.drawBitmap(this.mBackground, this.mBgSrcRect, this.mBgDstRect, this.mBgPaint);
                    }
                    if (this.mTransInfo.mIsBoundsDirty) {
                        canvas.clipRect(this.mTransInfo.mBounds);
                    }
                    if (this.mTransInfo.mIsAlphaDirty && this.mTransInfo.mAlpha < ALPHA_OPAQUE && !this.mIsTransparent) {
                        if (num != 1) {
                            canvas.saveLayerAlpha((float) this.mTransInfo.mBounds.left, (float) this.mTransInfo.mBounds.top, (float) this.mTransInfo.mBounds.right, (float) this.mTransInfo.mBounds.bottom, (int) (255.0f * this.mTransInfo.mAlpha));
                        } else if (viewInfoObject.shadowView != null) {
                            viewInfoObject.shadowView.setAlpha(this.mTransInfo.mAlpha);
                        } else {
                            if (viewInfoObject.alpha == -1.0f) {
                                viewInfoObject.alpha = child.getAlpha();
                            }
                            child.setAlpha(this.mTransInfo.mAlpha);
                        }
                    }
                    if (info.type != 1) {
                        try {
                            this.mDrawMethod.invoke(this.mTargetView, canvas, child, Long.valueOf(info.drawingTime));
                        } catch (IllegalArgumentException e4) {
                            Log.e(TAG, "Catch IllegalArgumentException");
                        } catch (IllegalAccessException e5) {
                            Log.e(TAG, "Catch IllegalAccessException");
                        } catch (InvocationTargetException e6) {
                            Log.e(TAG, "Catch InvocationTargetException");
                        }
                    } else if (viewInfoObject.shadowView != null) {
                        viewInfoObject.shadowView.draw(canvas);
                    } else {
                        this.mIsForceDraw = true;
                        child.draw(canvas);
                    }
                    if (this.mTransInfo.mIsAlphaDirty && this.mTransInfo.mAlpha < ALPHA_OPAQUE) {
                        if (this.mIsTransparent) {
                            if (child.getAlpha() != 1.0f) {
                                Log.w(TAG, "set transparent when view's alpha is not 1");
                                child.setAlpha(1.0f);
                            }
                            this.mPaint.setAlpha((int) (this.mTransInfo.mAlpha * 255.0f));
                            this.mTransInfo.mBounds.inset(-1, 0);
                            canvas.drawRect(this.mTransInfo.mBounds, this.mPaint);
                        } else if (num != 1) {
                            canvas.restore();
                        }
                    }
                    canvas.restoreToCount(savedCount);
                }
            }
        }
        return true;
    }

    public void setIsTargetAnimating(boolean isTargetAnimting) {
        this.mIsTargetAnimating = isTargetAnimting;
    }
}
