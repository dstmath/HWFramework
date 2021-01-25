package android.view;

import android.graphics.Outline;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.util.Log;
import android.view.WindowManager;
import java.util.ArrayList;
import java.util.Iterator;

public class HwBlurWindowManagerImpl implements IHwBlurWindowManager {
    private static final int CHANGE_CACHE_ENABLED = 2;
    private static final int CHANGE_ENABLED = 1;
    private static final int CHANGE_MODE = 8;
    private static final int CHANGE_NONE = 0;
    private static final int CHANGE_PROGRESS = 4;
    private static final int CHANGE_REGION = 16;
    private static final float OVAL_X_OFFSET = 0.2f;
    private static final float PROGRESS_MAX = 1.0f;
    private static final float PROGRESS_MIN = 0.0f;
    private static final String TAG = "HwBlurWindowManagerImpl";
    private boolean mCacheEnabled = false;
    private int mChanges = 0;
    private boolean mEnabled = true;
    private int mMode = 0;
    private float mProgress = 0.0f;
    private Region mRegion = new Region();
    private final Rect mTempBlurRect = new Rect();
    private final Rect mTmpBlurContentRect = new Rect();
    private final RectF mTmpBlurContentRectF = new RectF();
    private final Region mTmpBlurRegion = new Region();
    private final Rect mTmpBlurVisibleRect = new Rect();
    private final int[] mTmpLocation = new int[2];
    private final Outline mTmpOutline = new Outline();
    private final Region mTmpViewBlurRegion = new Region();
    private ArrayList<Float> mViewAlphaList = new ArrayList<>();
    private ArrayList<Rect> mViewRectList = new ArrayList<>();

    public boolean setBlurEnabled(boolean enabled) {
        if (this.mEnabled == enabled) {
            return false;
        }
        this.mEnabled = enabled;
        this.mChanges |= 1;
        return true;
    }

    public boolean getBlurEnabled() {
        return this.mEnabled;
    }

    public boolean setBlurCacheEnabled(boolean cacheEnabled) {
        if (!this.mEnabled || this.mCacheEnabled == cacheEnabled) {
            return false;
        }
        this.mCacheEnabled = cacheEnabled;
        this.mChanges |= 2;
        return true;
    }

    public boolean getBlurCacheEnabled() {
        return this.mCacheEnabled;
    }

    public boolean setBlurProgress(float progress) {
        if (!this.mEnabled) {
            return false;
        }
        float newProgress = Math.max(0.0f, Math.min(progress, 1.0f));
        if (this.mProgress == newProgress) {
            return false;
        }
        this.mProgress = newProgress;
        this.mChanges |= 4;
        return true;
    }

    public float getBlurProgress() {
        return this.mProgress;
    }

    public boolean setBlurMode(int mode) {
        if (!this.mEnabled || this.mMode == mode) {
            return false;
        }
        this.mMode = mode;
        this.mChanges |= 8;
        return true;
    }

    public int getBlurMode() {
        return this.mMode;
    }

    /* access modifiers changed from: package-private */
    public void updateWindowParams(SurfaceControl surfaceControl, boolean force) {
        SurfaceControl.openTransaction();
        if (!force) {
            try {
                if ((this.mChanges & 1) == 1) {
                }
                if (force || (this.mChanges & 2) == 2) {
                    Log.d(TAG, "BlurFeature: set blur cache enable:" + this.mCacheEnabled);
                    surfaceControl.setBlurCacheEnabled(this.mCacheEnabled);
                    this.mChanges = this.mChanges & -3;
                }
                if (force || (this.mChanges & 4) == 4) {
                    Log.d(TAG, "BlurFeature: set blur progress:" + this.mProgress);
                    surfaceControl.setBlurProgress(this.mProgress);
                    this.mChanges = this.mChanges & -5;
                }
                if (force || (this.mChanges & 8) == 8) {
                    Log.d(TAG, "BlurFeature: set blur mode:" + this.mMode);
                    surfaceControl.setBlurMode(this.mMode);
                    this.mChanges = this.mChanges & -9;
                }
                SurfaceControl.closeTransaction();
            } catch (Throwable th) {
                SurfaceControl.closeTransaction();
                throw th;
            }
        }
        Log.d(TAG, "BlurFeature: set blur enable:" + this.mEnabled);
        surfaceControl.setBlurEnabled(this.mEnabled);
        this.mChanges = this.mChanges & -2;
        Log.d(TAG, "BlurFeature: set blur cache enable:" + this.mCacheEnabled);
        surfaceControl.setBlurCacheEnabled(this.mCacheEnabled);
        this.mChanges = this.mChanges & -3;
        Log.d(TAG, "BlurFeature: set blur progress:" + this.mProgress);
        surfaceControl.setBlurProgress(this.mProgress);
        this.mChanges = this.mChanges & -5;
        Log.d(TAG, "BlurFeature: set blur mode:" + this.mMode);
        surfaceControl.setBlurMode(this.mMode);
        this.mChanges = this.mChanges & -9;
        SurfaceControl.closeTransaction();
    }

    /* access modifiers changed from: package-private */
    public void updateWindowDrawOp(Surface surface, boolean force) {
        if (surface != null && surface.isValid()) {
            if (force || (this.mChanges & 16) == 16) {
                if (!this.mRegion.isEmpty()) {
                    int length = this.mViewAlphaList.size();
                    float[] viewAlphaArray = new float[length];
                    int idx = 0;
                    Iterator<Float> it = this.mViewAlphaList.iterator();
                    while (it.hasNext()) {
                        Float val = it.next();
                        int idx2 = idx + 1;
                        viewAlphaArray[idx] = val != null ? val.floatValue() : Float.NaN;
                        idx = idx2;
                    }
                    surface.setViewParam(this.mViewRectList, viewAlphaArray, length);
                }
                surface.setBlurRegion(this.mRegion);
                this.mChanges &= -17;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void getRoundRect(Region region, Rect rect, int roundX, int roundY) {
        int left = rect.left;
        int top = rect.top;
        int right = rect.right;
        int bottom = rect.bottom;
        int width = right - left;
        int height = bottom - top;
        int roundX2 = Math.min(width / 2, roundX);
        int roundY2 = Math.min(height / 2, roundY);
        if (roundX2 > 0) {
            if (roundY2 > 0) {
                region.set(left + roundX2, top, right - roundX2, bottom);
                Rect tempRect = this.mTempBlurRect;
                int i = 0;
                double rx = (double) roundX2;
                while (i <= roundX2) {
                    double rx2 = Math.max((double) (((float) (roundX2 - i)) - 0.2f), 0.0d);
                    double ry = Math.pow((1.0d - (Math.pow(rx2, 2.0d) / Math.pow((double) roundX2, 2.0d))) * Math.pow((double) roundY2, 2.0d), 0.5d);
                    int newLeft = left + i;
                    int newRight = left + i + 1;
                    int newTop = (top + roundY2) - ((int) Math.floor(ry));
                    int newBottom = (bottom - roundY2) + ((int) Math.floor(ry));
                    tempRect.set(newLeft, newTop, newRight, newBottom);
                    region.union(tempRect);
                    tempRect.set((left + right) - newRight, newTop, (left + right) - newLeft, newBottom);
                    region.union(tempRect);
                    i++;
                    width = width;
                    height = height;
                    roundX2 = roundX2;
                    roundY2 = roundY2;
                    rx = rx2;
                }
                return;
            }
        }
        region.set(rect);
    }

    /* access modifiers changed from: package-private */
    public float getGlobalScaleX(View view) {
        float scaleX = view.getScaleX();
        ViewParent parent = view.getParent();
        if (parent == null || !(parent instanceof View)) {
            return scaleX;
        }
        return getGlobalScaleX((View) parent) * scaleX;
    }

    /* access modifiers changed from: package-private */
    public float getGlobalScaleY(View view) {
        float scaleY = view.getScaleY();
        ViewParent parent = view.getParent();
        if (parent == null || !(parent instanceof View)) {
            return scaleY;
        }
        return getGlobalScaleY((View) parent) * scaleY;
    }

    private float getGlobalAlpha(View view) {
        float alpha = view.getAlpha();
        ViewParent parent = view.getParent();
        if (!(parent instanceof View)) {
            return alpha;
        }
        return getGlobalAlpha((View) parent) * alpha;
    }

    private void clipToGlobalOutline(View view, Region region, WindowManager.LayoutParams windowAttr) {
        if (view.getClipToOutline() && view.getOutlineProvider() != null) {
            Outline outline = this.mTmpOutline;
            Rect contentRect = this.mTmpBlurContentRect;
            view.getOutlineProvider().getOutline(view, outline);
            if (outline.getRect(contentRect)) {
                RectF position = this.mTmpBlurContentRectF;
                position.set(contentRect);
                mapRectFromViewToWindowCoords(view, position, false);
                contentRect.set(Math.round(position.left), Math.round(position.top), Math.round(position.right), Math.round(position.bottom));
                contentRect.offset(windowAttr.surfaceInsets.left, windowAttr.surfaceInsets.top);
                Region tempRegion = this.mTmpViewBlurRegion;
                int radius = (int) outline.getRadius();
                if (radius > 0) {
                    getRoundRect(tempRegion, contentRect, radius, radius);
                } else {
                    tempRegion.set(contentRect);
                }
                region.op(tempRegion, Region.Op.INTERSECT);
            }
        }
        ViewParent parent = view.getParent();
        if (parent instanceof View) {
            clipToGlobalOutline((View) parent, region, windowAttr);
        }
    }

    private void getViewBoundsOnSurface(View view, WindowManager.LayoutParams windowAttr, Rect outRect, boolean clipToParent) {
        RectF position = this.mTmpBlurContentRectF;
        position.set(0.0f, 0.0f, (float) view.getWidth(), (float) view.getHeight());
        mapRectFromViewToWindowCoords(view, position, clipToParent);
        outRect.set(Math.round(position.left), Math.round(position.top), Math.round(position.right), Math.round(position.bottom));
        outRect.offset(windowAttr.surfaceInsets.left, windowAttr.surfaceInsets.top);
    }

    private void mapRectFromViewToWindowCoords(View view, RectF rect, boolean clipToParent) {
        if (!view.hasIdentityMatrix()) {
            view.getMatrix().mapRect(rect);
        }
        rect.offset((float) view.getLeft(), (float) view.getTop());
        ViewParent parent = view.getParent();
        while (parent instanceof View) {
            View parentView = (View) parent;
            rect.offset((float) (-parentView.getScrollX()), (float) (-parentView.getScrollY()));
            if (clipToParent) {
                rect.left = Math.max(rect.left, 0.0f);
                rect.top = Math.max(rect.top, 0.0f);
                rect.right = Math.min(rect.right, (float) parentView.getWidth());
                rect.bottom = Math.min(rect.bottom, (float) parentView.getHeight());
            }
            if (!parentView.hasIdentityMatrix()) {
                parentView.getMatrix().mapRect(rect);
            }
            rect.offset((float) parentView.mLeft, (float) parentView.mTop);
            parent = parentView.mParent;
        }
        if (parent instanceof ViewRootImpl) {
            rect.offset(0.0f, (float) (-((ViewRootImpl) parent).mCurScrollY));
        }
    }

    /* access modifiers changed from: package-private */
    public boolean getGlobalVisibleBlurRegionInSurface(View view, Region region, WindowManager.LayoutParams windowAttr) {
        Rect visibleRect = this.mTmpBlurVisibleRect;
        if (!view.getGlobalVisibleRect(visibleRect)) {
            region.setEmpty();
            return false;
        }
        visibleRect.offset(windowAttr.surfaceInsets.left, windowAttr.surfaceInsets.top);
        Rect contentRect = this.mTmpBlurContentRect;
        getViewBoundsOnSurface(view, windowAttr, contentRect, false);
        Region tempRegion = this.mTmpViewBlurRegion;
        getRoundRect(tempRegion, contentRect, view.getBlurCornerRoundX(), view.getBlurCornerRoundY());
        tempRegion.op(visibleRect, Region.Op.INTERSECT);
        region.set(tempRegion);
        clipToGlobalOutline(view, region, windowAttr);
        return true;
    }

    /* access modifiers changed from: package-private */
    public void drawBlurRegion(View view, WindowManager.LayoutParams windowAttr) {
        if (!(view == null || windowAttr == null)) {
            if (view.isBlurEnabled() && view.isShown() && getGlobalVisibleBlurRegionInSurface(view, this.mTmpBlurRegion, windowAttr)) {
                Rect visibleRect = new Rect();
                view.getGlobalVisibleRect(visibleRect);
                visibleRect.offset(windowAttr.surfaceInsets.left, windowAttr.surfaceInsets.top);
                this.mViewRectList.add(visibleRect);
                this.mViewAlphaList.add(Float.valueOf(getGlobalAlpha(view)));
                this.mRegion.op(this.mTmpBlurRegion, Region.Op.UNION);
            }
            if (view instanceof ViewGroup) {
                ViewGroup parent = (ViewGroup) view;
                for (int i = 0; i < parent.getChildCount(); i++) {
                    drawBlurRegion(parent.getChildAt(i), windowAttr);
                }
                for (int i2 = 0; i2 < parent.getTransientViewCount(); i2++) {
                    drawBlurRegion(parent.getTransientView(i2), windowAttr);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isBlurWindow(ViewRootImpl viewRoot) {
        return AbsLayoutParams.BLUR_FEATURE_ENABLED && !((viewRoot.mWindowAttributes.hwFlags & 33554432) == 0 && (viewRoot.mWindowAttributes.hwFlags & 67108864) == 0);
    }

    public void performDrawBlurLayer(ViewRootImpl viewRoot, View view) {
        if (isBlurWindow(viewRoot) && this.mEnabled) {
            this.mRegion.setEmpty();
            this.mViewRectList.clear();
            this.mViewAlphaList.clear();
            drawBlurRegion(view, viewRoot.mWindowAttributes);
        }
    }

    public void updateWindowBlurParams(ViewRootImpl viewRoot, boolean force) {
        if (!isBlurWindow(viewRoot)) {
            return;
        }
        if (force || this.mChanges != 0) {
            SurfaceControl surfaceControl = viewRoot.getSurfaceControl();
            if (surfaceControl.isValid()) {
                updateWindowParams(surfaceControl, force);
            }
        }
    }

    public void updateWindowBlurDrawOp(ViewRootImpl viewRoot, boolean force) {
        if (isBlurWindow(viewRoot) && this.mEnabled) {
            Surface surface = viewRoot.mSurface;
            if (surface.isValid()) {
                updateWindowDrawOp(surface, force);
            }
        }
    }
}
