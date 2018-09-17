package com.huawei.hwtransition;

import android.animation.TimeInterpolator;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import com.huawei.hwtransition.algorithm.BaseTransition;
import com.huawei.hwtransition.algorithm.BaseTransition.TransformationInfo;
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

public class HwTransition {
    private static final float ALPHA_OPAQUE = 0.996f;
    private static final float ALPHA_TRANSPARENT = 0.004f;
    public static final int BG_MODE_CENTER = 2;
    public static final int BG_MODE_TOP = 1;
    public static final int CAMERA_Z_DEF = -8;
    public static final boolean DBG = false;
    public static final int EDGE_MODE_CYCLE = 2;
    public static final int EDGE_MODE_ELASTIC = 1;
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
    ArrayList<View> mAlphaViews;
    ArrayList<Float> mAlphas;
    ArrayList<AnimateInfo> mAnimInfoEnds;
    ArrayList<AnimateInfo> mAnimInfos;
    private int mAnimationTargetFrameInterval;
    private Bitmap mBackground;
    Rect mBgDstRect;
    private int mBgMode;
    Paint mBgPaint;
    Rect mBgSrcRect;
    private boolean mBgStatic;
    private Display mDisplay;
    private Method mDisplayRealMethod;
    Point mDisplayRealSize;
    Point mDisplaySize;
    private Method mDrawMethod;
    private int mEdgeMode;
    Paint mErasePaint;
    private int mFirstOffset;
    boolean mForceDraw;
    private float mInitOffset;
    TimeInterpolator mInterpolator;
    boolean mIsTransparent;
    private int mLeftScreen;
    private int mMaxPage;
    private float mOffset;
    private int mPageSpacing;
    private Paint mPaint;
    private float mPreFrameElapse;
    private int mRightScreen;
    private boolean mTargetAnimating;
    private View mTargetView;
    private float mTotoalMissElapse;
    TransformationInfo mTransInfo;
    private HashMap<String, BaseTransition> mTransitionsMap;
    private float mTravelRatio;

    static class AnimateInfo {
        long drawingTime;
        long duration = 500;
        boolean isScrolling;
        boolean reverse = false;
        long startTime = -1;
        int type = 1;
        ArrayList<ViewInfo> views = new ArrayList();

        public AnimateInfo(int t) {
            this.type = t;
        }

        public void clear() {
            for (ViewInfo vi : this.views) {
                vi.clean();
            }
            this.views.clear();
            this.views = null;
        }
    }

    static class ShadowView extends View {
        Bitmap mBmp;
        Paint mPaint = new Paint();

        private ShadowView(View v) {
            super(v.getContext());
        }

        static ShadowView createShadow(View v) {
            ShadowView shadowView = new ShadowView(v);
            if (shadowView.copyView(v)) {
                return shadowView;
            }
            return null;
        }

        public void setAlpha(float alpha) {
            this.mPaint.setAlpha((int) (255.0f * alpha));
        }

        public void draw(Canvas canvas) {
            if (this.mBmp != null) {
                canvas.drawBitmap(this.mBmp, 0.0f, 0.0f, this.mPaint);
            } else {
                Log.e(HwTransition.TAG, "bitmap is null, should not come here!!! ");
            }
        }

        public void clearBitmap() {
            if (this.mBmp != null) {
                this.mBmp.recycle();
            }
        }

        public boolean copyView(View v) {
            boolean willNotCache = v.willNotCacheDrawing();
            int color = v.getDrawingCacheBackgroundColor();
            v.setWillNotCacheDrawing(false);
            v.setDrawingCacheBackgroundColor(0);
            if (color != 0) {
                v.destroyDrawingCache();
            }
            v.buildDrawingCache();
            Bitmap cacheBitmap = v.getDrawingCache();
            if (cacheBitmap == null) {
                Log.e(HwTransition.TAG, "copyView failed: " + v);
                return false;
            }
            this.mBmp = Bitmap.createBitmap(cacheBitmap);
            v.destroyDrawingCache();
            v.setWillNotCacheDrawing(willNotCache);
            v.setDrawingCacheBackgroundColor(color);
            return true;
        }
    }

    static class ViewInfo {
        float alpha = -1.0f;
        float fraction;
        int index;
        boolean isEdge;
        boolean isOverScrollFirst;
        boolean isOverScrollLast;
        float relativeTrans = 0.0f;
        ShadowView shadowView;
        View view;

        public ViewInfo(View v) {
            this.view = v;
        }

        void clean() {
            if (this.shadowView != null) {
                this.shadowView.clearBitmap();
                this.shadowView = null;
            }
            this.view = null;
        }
    }

    private Method getDrawChildMethod() {
        Method method = null;
        try {
            method = ViewGroup.class.getDeclaredMethod("drawChild", new Class[]{Canvas.class, View.class, Long.TYPE});
            method.setAccessible(true);
            return method;
        } catch (NoSuchMethodException e) {
            Log.e(TAG, e.toString());
            return method;
        }
    }

    private Method getDisplayRealMethod() {
        Method method = null;
        try {
            return Display.class.getMethod("getRealSize", new Class[]{Point.class});
        } catch (NoSuchMethodException e) {
            Log.e(TAG, e.toString());
            return method;
        }
    }

    public HwTransition(View targetView) {
        this(targetView, "Normal");
    }

    public HwTransition(View targetView, String type) {
        this(targetView, type, 0);
    }

    public HwTransition(View targetView, String type, int pageSpacing) {
        this.mAnimInfos = new ArrayList();
        this.mAnimInfoEnds = new ArrayList();
        this.mAlphaViews = new ArrayList();
        this.mAlphas = new ArrayList();
        this.mAlphaLeftIdx = -1;
        this.mForceDraw = false;
        this.mBgStatic = false;
        this.mBgMode = 2;
        this.mOffset = 0.0f;
        this.mInitOffset = 0.0f;
        this.mTravelRatio = 1.0f;
        this.mIsTransparent = false;
        this.mFirstOffset = 0;
        this.mTransitionsMap = new HashMap();
        this.mBgPaint = new Paint();
        this.mErasePaint = new Paint();
        this.mBgDstRect = new Rect();
        this.mBgSrcRect = new Rect();
        this.mAnimationTargetFrameInterval = 1000;
        this.mTotoalMissElapse = 0.0f;
        this.mPreFrameElapse = -1.0f;
        this.mEdgeMode = 1;
        this.mLeftScreen = -1;
        this.mRightScreen = -1;
        this.mMaxPage = -1;
        this.mTargetAnimating = false;
        this.mInterpolator = new AccelerateDecelerateInterpolator();
        Log.d(TAG, "hwtransition version = 0.3.01, targetView = " + targetView);
        if (targetView != null) {
            this.mPaint = new Paint();
            this.mPaint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
            this.mTargetView = targetView;
            this.mDrawMethod = getDrawChildMethod();
            this.mDisplayRealMethod = getDisplayRealMethod();
            initTransitions();
            setTransitionType(type);
            setPageSpacing(pageSpacing);
        }
    }

    public void setTransitionType(String type) {
        BaseTransition transition = (BaseTransition) this.mTransitionsMap.get(type);
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

    public void setCameraDistance(float z) {
        if (this.mActiveTransition != null) {
            this.mActiveTransition.setCameraDistance(z);
        }
    }

    public void setBackground(Bitmap background) {
        this.mBackground = background;
        if (this.mActiveTransition.mUseBg) {
            this.mActiveTransition.setAlphaMode(this.mBackground == null);
        }
        if (background != null && this.mDisplay == null) {
            this.mDisplay = ((WindowManager) this.mTargetView.getContext().getSystemService("window")).getDefaultDisplay();
            this.mDisplaySize = new Point();
            this.mDisplayRealSize = new Point();
        }
    }

    public Bitmap getBackground() {
        return this.mBackground;
    }

    public void setAnimationFPS(int minfps) {
        if (minfps > 0) {
            this.mAnimationTargetFrameInterval = 1000 / minfps;
        }
    }

    public void setViewDuration(View view, long duration) {
        for (AnimateInfo info : this.mAnimInfos) {
            if (((ViewInfo) info.views.get(0)).view == view) {
                info.duration = duration;
                return;
            }
        }
    }

    public void setBackgroundOffset(boolean bgStatic, float offset) {
        this.mBgStatic = bgStatic;
        this.mOffset = offset;
    }

    public void setBackgroundMode(int bgMode) {
        this.mBgMode = bgMode;
    }

    public void setAlphaMode(boolean useAlpha) {
        this.mActiveTransition.setAlphaMode(useAlpha);
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

    public float getLayerOffset(float offset, int n) {
        if (n == 1) {
            if (Float.isNaN(offset)) {
                return 0.0f;
            }
        } else if (this.mBackground != null && this.mActiveTransition.mUseBg) {
            float step = this.mTravelRatio / ((float) (n - 1));
            float overx = (offset - this.mInitOffset) % step;
            offset -= overx;
            if (((double) (overx / step)) > 0.5d) {
                offset += step;
            }
        }
        return offset;
    }

    public void setWallpaperTravel(int Travelwidth, int width) {
        this.mInitOffset = ((float) (width - Travelwidth)) / (((float) width) * 2.0f);
        this.mTravelRatio = ((float) Travelwidth) / ((float) width);
    }

    public void setPageSpacing(int pageSpacing) {
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
        if (view == null || this.mActiveTransition == null) {
            return false;
        }
        ShadowView sv = ShadowView.createShadow(view);
        if (sv == null) {
            return false;
        }
        cancelPreviousAnimation(view);
        AnimateInfo info = new AnimateInfo(1);
        info.reverse = true;
        this.mActiveTransition.setLayoutType(1);
        this.mActiveTransition.setOrientation(1);
        info.views.add(new ViewInfo(view));
        ViewInfo vi = new ViewInfo(view);
        vi.shadowView = sv;
        info.views.add(vi);
        this.mAnimInfos.add(info);
        view.invalidate();
        return true;
    }

    private void cancelPreviousAnimation(View view) {
        for (AnimateInfo info : this.mAnimInfos) {
            if (((ViewInfo) info.views.get(0)).view == view) {
                onAnimationEnd(info);
                return;
            }
        }
    }

    public void setInterpolator(TimeInterpolator interpolator) {
        this.mInterpolator = interpolator;
    }

    public boolean animateDraw(Canvas canvas) {
        if (this.mAnimInfos.size() <= 0) {
            return false;
        }
        if (this.mTargetView == null || this.mActiveTransition == null || this.mForceDraw) {
            if (this.mForceDraw) {
                this.mForceDraw = false;
            }
            return false;
        }
        long currentTime = this.mTargetView.getDrawingTime();
        for (AnimateInfo info : this.mAnimInfos) {
            float nt;
            if (info.startTime == -1) {
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
            float normalizedTime = info.duration != 0 ? (elapse - this.mTotoalMissElapse) / ((float) info.duration) : currentTime < info.startTime ? 0.0f : 1.0f;
            normalizedTime = Math.max(Math.min(normalizedTime, 1.0f), 0.01f);
            if (info.reverse) {
                nt = 1.0f - normalizedTime;
            } else {
                nt = normalizedTime;
            }
            if (this.mInterpolator != null) {
                nt = this.mInterpolator.getInterpolation(nt);
            }
            nt = ((float) ((int) (100000.0f * nt))) / 100000.0f;
            int n = info.views.size();
            for (int i = 0; i < n; i++) {
                ViewInfo vi = (ViewInfo) info.views.get(i);
                vi.fraction = nt;
                if (i % 2 != 0) {
                    vi.fraction -= 4.0f;
                }
            }
            animView(canvas, info);
            if (normalizedTime >= 1.0f) {
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
        if (this.mTargetView == null || this.mActiveTransition == null || !(this.mTargetView instanceof ViewGroup)) {
            return false;
        }
        int left = this.mLeftScreen;
        int right = this.mRightScreen;
        if (right < left) {
            float[] values = new float[9];
            canvas.getMatrix().getValues(values);
            canvas.translate(-values[2], 0.0f);
            if (transitonX < 0) {
                transitonX = (this.mTargetView.getWidth() + transitonX) + (this.mTargetView.getWidth() * left);
            } else {
                transitonX = (transitonX % this.mTargetView.getWidth()) + (this.mTargetView.getWidth() * left);
            }
            canvas.translate((float) (-transitonX), 0.0f);
        }
        ViewGroup vg = (ViewGroup) this.mTargetView;
        View child = null;
        float scrollProgress = 0.0f;
        AnimateInfo info = new AnimateInfo(0);
        info.drawingTime = vg.getDrawingTime();
        info.isScrolling = isScrolling;
        int i = 0;
        int n = vg.getChildCount();
        while (i < n) {
            child = vg.getChildAt(i);
            if (i == 0) {
                if (this.mActiveTransition.isHorizental()) {
                    this.mFirstOffset = child.getLeft();
                } else {
                    this.mFirstOffset = child.getTop();
                }
            }
            if (right >= left) {
                scrollProgress = TransitionUtil.getScrollProgress(vg, transitonX, child, i, this.mPageSpacing);
                if (scrollProgress < 1.0f) {
                    break;
                }
            }
            i++;
        }
        if (right < left) {
            i = left;
            child = vg.getChildAt(left);
            scrollProgress = TransitionUtil.getScrollProgress(vg, transitonX, child, left, this.mPageSpacing);
        }
        scrollProgress = ((float) ((int) (100000.0f * scrollProgress))) / 100000.0f;
        int leftIdx = i;
        if (i < n) {
            if ((scrollProgress == 0.0f && !isScrolling) || !(this.mAlphaLeftIdx == -1 || this.mAlphaLeftIdx == i)) {
                int sz = this.mAlphaViews.size();
                for (int j = 0; j < sz; j++) {
                    ((View) this.mAlphaViews.get(j)).setAlpha(((Float) this.mAlphas.get(j)).floatValue());
                }
                this.mAlphaViews.clear();
                this.mAlphaLeftIdx = -1;
                if (scrollProgress == 0.0f && !isScrolling) {
                    return false;
                }
            }
            ViewInfo viewInfo = new ViewInfo(child);
            viewInfo.fraction = scrollProgress;
            boolean z = i == 0 && scrollProgress < 0.0f;
            viewInfo.isOverScrollFirst = z;
            viewInfo.isOverScrollLast = i == n + -1;
            viewInfo.index = i;
            info.views.add(viewInfo);
            if (right < left) {
                viewInfo.isOverScrollFirst = false;
                viewInfo.isOverScrollLast = false;
            }
            if (scrollProgress > 0.0f && !viewInfo.isOverScrollLast) {
                if (right < left) {
                    i = right;
                } else {
                    i++;
                }
                viewInfo = new ViewInfo(vg.getChildAt(i));
                viewInfo.fraction = -1.0f + scrollProgress;
                viewInfo.index = i;
                if (right < left) {
                    viewInfo.relativeTrans = (float) TransitionUtil.getChildOffset(vg, n, this.mPageSpacing);
                }
                info.views.add(viewInfo);
            }
            animView(canvas, info);
            for (ViewInfo vi : info.views) {
                if (!(vi.alpha == -1.0f || this.mAlphaViews.contains(vi.view))) {
                    this.mAlphaLeftIdx = leftIdx;
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
        if (this.mTargetView == null || this.mActiveTransition == null || !(this.mTargetView instanceof ViewGroup)) {
            Log.w(TAG, "animateDispatchDraw mTargetView = " + this.mTargetView + ", mActiveTransition " + this.mActiveTransition + ", mTargetView = " + this.mTargetView);
            return false;
        }
        ViewGroup vg = this.mTargetView;
        int n = vg.getChildCount();
        if (leftScreen < 0 || leftScreen >= n) {
            Log.w(TAG, "animateDispatchDraw leftScreen out of range " + leftScreen + " / " + n);
            return false;
        } else if (rightScreen < 0 || rightScreen >= n) {
            Log.w(TAG, "animateDispatchDraw rightScreen out of range " + rightScreen + " / " + n);
            return false;
        } else if (this.mEdgeMode == 2) {
            this.mLeftScreen = leftScreen;
            this.mRightScreen = rightScreen;
            return animateDispatchDraw(canvas, transitonX, isScrolling);
        } else {
            AnimateInfo info = new AnimateInfo(0);
            info.drawingTime = vg.getDrawingTime();
            info.isScrolling = isScrolling;
            int i = leftScreen;
            while (i <= rightScreen) {
                View child = vg.getChildAt(i);
                if (i == 0) {
                    if (this.mActiveTransition.isHorizental()) {
                        this.mFirstOffset = child.getLeft();
                    } else {
                        this.mFirstOffset = child.getTop();
                    }
                }
                float scrollProgress = TransitionUtil.getScrollProgress(vg, transitonX, child, i, this.mPageSpacing);
                ViewInfo vi = new ViewInfo(child);
                vi.fraction = scrollProgress;
                if (this.mTargetAnimating && TRANS_TYPE_EDIT_DEPTH.equals(this.mActiveTransitionType)) {
                    int centerIdx = (rightScreen + leftScreen) / 2;
                    if (rightScreen + 1 == this.mMaxPage && leftScreen + 1 == rightScreen) {
                        centerIdx = rightScreen;
                    }
                    if (centerIdx == leftScreen && leftScreen != 0) {
                        Log.w(TAG, "leftScreen is not 0 when only two screen shows ! centerIdx = " + centerIdx + ", " + leftScreen + " " + rightScreen);
                    }
                    if (centerIdx == i) {
                        vi.fraction = 0.0f;
                    }
                }
                boolean z = i == 0 && scrollProgress <= 0.0f;
                vi.isOverScrollFirst = z;
                z = i == n + -1 && scrollProgress >= 0.0f;
                vi.isOverScrollLast = z;
                vi.isEdge = !vi.isOverScrollFirst ? vi.isOverScrollLast : true;
                vi.index = i;
                info.views.add(vi);
                i++;
            }
            if (leftScreen == 0 && ((ViewInfo) info.views.get(leftScreen)).isOverScrollFirst && leftScreen < rightScreen) {
                ((ViewInfo) info.views.get(leftScreen + 1)).isOverScrollFirst = true;
            }
            if (rightScreen == n - 1) {
                int last = info.views.size() - 1;
                if (((ViewInfo) info.views.get(last)).isOverScrollLast && leftScreen < rightScreen) {
                    ((ViewInfo) info.views.get(last - 1)).isOverScrollLast = true;
                }
            }
            int sz = this.mAlphaViews.size();
            for (int j = 0; j < sz; j++) {
                ((View) this.mAlphaViews.get(j)).setAlpha(((Float) this.mAlphas.get(j)).floatValue());
            }
            this.mAlphaViews.clear();
            animView(canvas, info);
            return true;
        }
    }

    private boolean animView(Canvas canvas, AnimateInfo info) {
        int num = this.mActiveTransition.getBreakTimes();
        if (this.mBackground != null) {
            try {
                this.mDisplayRealMethod.invoke(this.mDisplay, new Object[]{this.mDisplayRealSize});
            } catch (IllegalArgumentException e) {
                Log.e(TAG, e.toString());
            } catch (IllegalAccessException e2) {
                Log.e(TAG, e2.toString());
            } catch (InvocationTargetException e3) {
                Log.e(TAG, e3.toString());
            }
            this.mDisplay.getSize(this.mDisplaySize);
        }
        int pw = this.mTargetView.getWidth();
        int ph = this.mTargetView.getHeight();
        int n = info.views.size();
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < num; j++) {
                ViewInfo vi = (ViewInfo) info.views.get(i);
                vi = (ViewInfo) info.views.get(this.mActiveTransition.getDrawingOrder(n, i, j, vi.fraction));
                View child = vi.view;
                int cl = child.getLeft();
                int ct = child.getTop();
                if (this.mTargetView == child) {
                    cl = 0;
                    ct = 0;
                }
                int part = this.mActiveTransition.getBreakOrder(j, vi.fraction);
                this.mActiveTransition.setState(info.isScrolling);
                this.mTransInfo = this.mActiveTransition.getTransformation(part, vi.isOverScrollFirst, vi.isOverScrollLast, vi.fraction, vi.isEdge, this.mTargetView, child, this.mPageSpacing);
                if (this.mTransInfo != null && (!this.mTransInfo.mAlphaDirty || this.mTransInfo.mAlpha >= ALPHA_TRANSPARENT)) {
                    int savedCount = canvas.save();
                    canvas.translate(vi.relativeTrans, 0.0f);
                    if (!this.mTransInfo.mBoundsDirty) {
                        AlgorithmUtil.getTransformRect(child, this.mTransInfo.mBounds);
                    }
                    this.mTransInfo.mBounds.offset(cl + 0, ct + 0);
                    if (this.mBackground != null && this.mTransInfo.mBackgroundDirty) {
                        this.mErasePaint.setColor(-16777216);
                        this.mBgDstRect.set(this.mTransInfo.mBounds);
                        if (this.mActiveTransition.isHorizental()) {
                            this.mBgDstRect.top = 0;
                            this.mBgDstRect.bottom = ph;
                            if (part == 0) {
                                this.mBgDstRect.left = cl - this.mFirstOffset;
                            } else if (part == num - 1) {
                                this.mBgDstRect.right = (cl + pw) - this.mFirstOffset;
                            }
                        } else {
                            this.mBgDstRect.left = 0;
                            this.mBgDstRect.right = pw;
                            if (part == 0) {
                                this.mBgDstRect.top = ct - this.mFirstOffset;
                            } else if (part == num - 1) {
                                this.mBgDstRect.bottom = (ct + ph) - this.mFirstOffset;
                            }
                        }
                        if (vi.isOverScrollFirst || vi.isOverScrollLast) {
                            canvas.save();
                            canvas.clipRect(this.mBgDstRect);
                            canvas.drawPaint(this.mErasePaint);
                            canvas.restore();
                        }
                    }
                    if (this.mTransInfo.mMatrixDirty) {
                        canvas.translate((float) (cl + 0), (float) (ct + 0));
                        canvas.concat(this.mTransInfo.mMatrix);
                        canvas.translate((float) (0 - cl), (float) (0 - ct));
                    }
                    if (this.mBackground != null && this.mTransInfo.mBackgroundDirty) {
                        float dxy = 0.0f;
                        int cn = ((ViewGroup) this.mTargetView).getChildCount();
                        if (!this.mActiveTransition.isHorizental()) {
                            if (this.mBgStatic) {
                                dxy = ((float) (this.mBackground.getHeight() - ph)) * this.mOffset;
                            } else if (cn > 1) {
                                this.mOffset = (this.mTravelRatio * ((float) vi.index)) / ((float) (cn - 1));
                                dxy = ((float) (this.mBackground.getHeight() - ph)) * (this.mInitOffset + this.mOffset);
                            }
                            this.mBgSrcRect.set(0, (int) (((((float) this.mBgDstRect.top) + dxy) - ((float) ct)) + 0.5f), this.mBackground.getWidth(), (int) (((((float) this.mBgDstRect.bottom) + dxy) - ((float) ct)) + 0.5f));
                            switch (this.mBgMode) {
                                case 2:
                                    if (this.mBackground.getWidth() > this.mDisplayRealSize.x) {
                                        this.mBgSrcRect.offset((this.mBackground.getWidth() - this.mDisplaySize.x) / 2, 0);
                                        break;
                                    }
                                    break;
                            }
                        }
                        if (this.mBgStatic) {
                            dxy = ((float) (this.mBackground.getWidth() - pw)) * this.mOffset;
                        } else if (cn > 1) {
                            this.mOffset = (this.mTravelRatio * ((float) vi.index)) / ((float) (cn - 1));
                            dxy = ((float) (this.mBackground.getWidth() - pw)) * (this.mInitOffset + this.mOffset);
                        }
                        this.mBgSrcRect.set((int) ((((((float) this.mBgDstRect.left) + dxy) - ((float) cl)) + ((float) this.mFirstOffset)) + 0.5f), 0, (int) ((((((float) this.mBgDstRect.right) + dxy) - ((float) cl)) + ((float) this.mFirstOffset)) + 0.5f), this.mBgDstRect.height());
                        switch (this.mBgMode) {
                            case 2:
                                int dh = (this.mBackground.getHeight() - this.mBgDstRect.height()) / 2;
                                if (dh <= 0) {
                                    dh = 0;
                                }
                                if (this.mBackground.getHeight() > this.mDisplayRealSize.y) {
                                    dh += (this.mBackground.getHeight() - this.mDisplaySize.y) / 2;
                                }
                                this.mBgSrcRect.offset(0, dh);
                                break;
                        }
                        canvas.drawBitmap(this.mBackground, this.mBgSrcRect, this.mBgDstRect, this.mBgPaint);
                    }
                    if (this.mTransInfo.mBoundsDirty) {
                        canvas.clipRect(this.mTransInfo.mBounds);
                    }
                    if (this.mTransInfo.mAlphaDirty && this.mTransInfo.mAlpha < ALPHA_OPAQUE && !this.mIsTransparent) {
                        if (num != 1) {
                            canvas.saveLayerAlpha((float) this.mTransInfo.mBounds.left, (float) this.mTransInfo.mBounds.top, (float) this.mTransInfo.mBounds.right, (float) this.mTransInfo.mBounds.bottom, (int) (this.mTransInfo.mAlpha * 255.0f), 16);
                        } else if (vi.shadowView != null) {
                            vi.shadowView.setAlpha(this.mTransInfo.mAlpha);
                        } else {
                            if (vi.alpha == -1.0f) {
                                vi.alpha = child.getAlpha();
                            }
                            child.setAlpha(this.mTransInfo.mAlpha);
                        }
                    }
                    if (info.type != 1) {
                        try {
                            this.mDrawMethod.invoke(this.mTargetView, new Object[]{canvas, child, Long.valueOf(info.drawingTime)});
                        } catch (IllegalArgumentException e4) {
                            Log.e(TAG, e4.toString());
                        } catch (IllegalAccessException e22) {
                            Log.e(TAG, e22.toString());
                        } catch (InvocationTargetException e32) {
                            Log.e(TAG, e32.toString());
                        }
                    } else if (vi.shadowView != null) {
                        vi.shadowView.draw(canvas);
                    } else {
                        this.mForceDraw = true;
                        child.draw(canvas);
                    }
                    if (this.mTransInfo.mAlphaDirty && this.mTransInfo.mAlpha < ALPHA_OPAQUE) {
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

    public void setIsTargetAnimating(boolean targetAnimting) {
        this.mTargetAnimating = targetAnimting;
    }
}
