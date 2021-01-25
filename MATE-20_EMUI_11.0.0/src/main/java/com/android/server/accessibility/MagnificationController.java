package com.android.server.accessibility;

import android.animation.ValueAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Rect;
import android.graphics.Region;
import android.os.AsyncTask;
import android.os.Handler;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.MathUtils;
import android.util.Slog;
import android.util.SparseArray;
import android.view.MagnificationSpec;
import android.view.animation.DecelerateInterpolator;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.function.pooled.PooledLambda;
import com.android.server.LocalServices;
import com.android.server.wm.WindowManagerInternal;
import java.util.Locale;

public class MagnificationController {
    private static final boolean DEBUG = false;
    private static final boolean DEBUG_SET_MAGNIFICATION_SPEC = false;
    private static final float DEFAULT_MAGNIFICATION_SCALE = 2.0f;
    private static final String LOG_TAG = "MagnificationController";
    public static final float MAX_SCALE = 8.0f;
    public static final float MIN_SCALE = 1.0f;
    private final ControllerContext mControllerCtx;
    @GuardedBy({"mLock"})
    private final SparseArray<DisplayMagnification> mDisplays;
    private boolean mIsLastMagnifying;
    private final Object mLock;
    private final long mMainThreadId;
    private final ScreenStateObserver mScreenStateObserver;
    private int mUserId;

    /* access modifiers changed from: private */
    public final class DisplayMagnification implements WindowManagerInternal.MagnificationCallbacks {
        private static final int INVALID_ID = -1;
        private final MagnificationSpec mCurrentMagnificationSpec = MagnificationSpec.obtain();
        private boolean mDeleteAfterUnregister;
        private final int mDisplayId;
        private int mIdOfLastServiceToMagnify = -1;
        private final Rect mMagnificationBounds = new Rect();
        private final Region mMagnificationRegion = Region.obtain();
        private boolean mRegistered;
        private final SpecAnimationBridge mSpecAnimationBridge;
        private final Rect mTempRect = new Rect();
        private final Rect mTempRect1 = new Rect();
        private boolean mUnregisterPending;

        DisplayMagnification(int displayId) {
            this.mDisplayId = displayId;
            this.mSpecAnimationBridge = new SpecAnimationBridge(MagnificationController.this.mControllerCtx, MagnificationController.this.mLock, this.mDisplayId);
        }

        /* access modifiers changed from: package-private */
        @GuardedBy({"mLock"})
        public boolean register() {
            this.mRegistered = MagnificationController.this.mControllerCtx.getWindowManager().setMagnificationCallbacks(this.mDisplayId, this);
            if (!this.mRegistered) {
                Slog.w(MagnificationController.LOG_TAG, "set magnification callbacks fail, displayId:" + this.mDisplayId);
                return false;
            }
            this.mSpecAnimationBridge.setEnabled(true);
            MagnificationController.this.mControllerCtx.getWindowManager().getMagnificationRegion(this.mDisplayId, this.mMagnificationRegion);
            this.mMagnificationRegion.getBounds(this.mMagnificationBounds);
            return true;
        }

        /* access modifiers changed from: package-private */
        @GuardedBy({"mLock"})
        public void unregister(boolean delete) {
            if (this.mRegistered) {
                this.mSpecAnimationBridge.setEnabled(false);
                MagnificationController.this.mControllerCtx.getWindowManager().setMagnificationCallbacks(this.mDisplayId, (WindowManagerInternal.MagnificationCallbacks) null);
                this.mMagnificationRegion.setEmpty();
                this.mRegistered = false;
                MagnificationController.this.unregisterCallbackLocked(this.mDisplayId, delete);
            }
            this.mUnregisterPending = false;
        }

        /* access modifiers changed from: package-private */
        @GuardedBy({"mLock"})
        public void unregisterPending(boolean delete) {
            this.mDeleteAfterUnregister = delete;
            this.mUnregisterPending = true;
            reset(true);
        }

        /* access modifiers changed from: package-private */
        public boolean isRegistered() {
            return this.mRegistered;
        }

        /* access modifiers changed from: package-private */
        public boolean isMagnifying() {
            return this.mCurrentMagnificationSpec.scale > 1.0f;
        }

        /* access modifiers changed from: package-private */
        public float getScale() {
            return this.mCurrentMagnificationSpec.scale;
        }

        /* access modifiers changed from: package-private */
        public float getOffsetX() {
            return this.mCurrentMagnificationSpec.offsetX;
        }

        /* access modifiers changed from: package-private */
        public float getOffsetY() {
            return this.mCurrentMagnificationSpec.offsetY;
        }

        /* access modifiers changed from: package-private */
        @GuardedBy({"mLock"})
        public float getCenterX() {
            return (((((float) this.mMagnificationBounds.width()) / MagnificationController.DEFAULT_MAGNIFICATION_SCALE) + ((float) this.mMagnificationBounds.left)) - getOffsetX()) / getScale();
        }

        /* access modifiers changed from: package-private */
        @GuardedBy({"mLock"})
        public float getCenterY() {
            return (((((float) this.mMagnificationBounds.height()) / MagnificationController.DEFAULT_MAGNIFICATION_SCALE) + ((float) this.mMagnificationBounds.top)) - getOffsetY()) / getScale();
        }

        /* access modifiers changed from: package-private */
        public float getSentScale() {
            return this.mSpecAnimationBridge.mSentMagnificationSpec.scale;
        }

        /* access modifiers changed from: package-private */
        public float getSentOffsetX() {
            return this.mSpecAnimationBridge.mSentMagnificationSpec.offsetX;
        }

        /* access modifiers changed from: package-private */
        public float getSentOffsetY() {
            return this.mSpecAnimationBridge.mSentMagnificationSpec.offsetY;
        }

        public void onMagnificationRegionChanged(Region magnificationRegion) {
            MagnificationController.this.mControllerCtx.getHandler().sendMessage(PooledLambda.obtainMessage($$Lambda$SP6uGJNthzczgi990Xl2SJhDOMs.INSTANCE, this, Region.obtain(magnificationRegion)));
        }

        public void onRectangleOnScreenRequested(int left, int top, int right, int bottom) {
            MagnificationController.this.mControllerCtx.getHandler().sendMessage(PooledLambda.obtainMessage($$Lambda$iE9JplYHP8mrOjjadf_Oh8XKSE4.INSTANCE, this, Integer.valueOf(left), Integer.valueOf(top), Integer.valueOf(right), Integer.valueOf(bottom)));
        }

        public void onRotationChanged(int rotation) {
            MagnificationController.this.mControllerCtx.getHandler().sendMessage(PooledLambda.obtainMessage($$Lambda$AbiCM6mjSOPpIPMT9CFGL4UAcKY.INSTANCE, MagnificationController.this, Integer.valueOf(this.mDisplayId), true));
        }

        public void onUserContextChanged() {
            MagnificationController.this.mControllerCtx.getHandler().sendMessage(PooledLambda.obtainMessage($$Lambda$AbiCM6mjSOPpIPMT9CFGL4UAcKY.INSTANCE, MagnificationController.this, Integer.valueOf(this.mDisplayId), true));
        }

        /* access modifiers changed from: package-private */
        public void updateMagnificationRegion(Region magnified) {
            synchronized (MagnificationController.this.mLock) {
                if (this.mRegistered) {
                    if (!this.mMagnificationRegion.equals(magnified)) {
                        this.mMagnificationRegion.set(magnified);
                        this.mMagnificationRegion.getBounds(this.mMagnificationBounds);
                        if (updateCurrentSpecWithOffsetsLocked(this.mCurrentMagnificationSpec.offsetX, this.mCurrentMagnificationSpec.offsetY)) {
                            sendSpecToAnimation(this.mCurrentMagnificationSpec, false);
                        }
                        onMagnificationChangedLocked();
                    }
                    magnified.recycle();
                }
            }
        }

        /* access modifiers changed from: package-private */
        public void sendSpecToAnimation(MagnificationSpec spec, boolean animate) {
            if (Thread.currentThread().getId() == MagnificationController.this.mMainThreadId) {
                this.mSpecAnimationBridge.updateSentSpecMainThread(spec, animate);
                return;
            }
            MagnificationController.this.mControllerCtx.getHandler().sendMessage(PooledLambda.obtainMessage($$Lambda$CXn5BYHEDMuDgWNKCgknaVOAyJ8.INSTANCE, this.mSpecAnimationBridge, spec, Boolean.valueOf(animate)));
        }

        /* access modifiers changed from: package-private */
        public int getIdOfLastServiceToMagnify() {
            return this.mIdOfLastServiceToMagnify;
        }

        /* access modifiers changed from: package-private */
        public void onMagnificationChangedLocked() {
            MagnificationController.this.mControllerCtx.getAms().notifyMagnificationChanged(this.mDisplayId, this.mMagnificationRegion, getScale(), getCenterX(), getCenterY());
            if (this.mUnregisterPending && !isMagnifying()) {
                unregister(this.mDeleteAfterUnregister);
            }
            boolean isMagnifying = isMagnifying();
            Slog.i(MagnificationController.LOG_TAG, "magnification change,magnifying:" + isMagnifying + ",lastMagnifyt:" + MagnificationController.this.mIsLastMagnifying);
            if (isMagnifying != MagnificationController.this.mIsLastMagnifying) {
                MagnificationController.this.mControllerCtx.getWindowManager().setNotchRoundCornerVisibility(isMagnifying);
                MagnificationController.this.mIsLastMagnifying = isMagnifying;
            }
        }

        /* access modifiers changed from: package-private */
        @GuardedBy({"mLock"})
        public boolean magnificationRegionContains(float x, float y) {
            return this.mMagnificationRegion.contains((int) x, (int) y);
        }

        /* access modifiers changed from: package-private */
        @GuardedBy({"mLock"})
        public void getMagnificationBounds(Rect outBounds) {
            outBounds.set(this.mMagnificationBounds);
        }

        /* access modifiers changed from: package-private */
        @GuardedBy({"mLock"})
        public void getMagnificationRegion(Region outRegion) {
            outRegion.set(this.mMagnificationRegion);
        }

        /* access modifiers changed from: package-private */
        public void requestRectangleOnScreen(int left, int top, int right, int bottom) {
            float scrollX;
            float scrollY;
            synchronized (MagnificationController.this.mLock) {
                Rect magnifiedFrame = this.mTempRect;
                getMagnificationBounds(magnifiedFrame);
                if (magnifiedFrame.intersects(left, top, right, bottom)) {
                    Rect magnifFrameInScreenCoords = this.mTempRect1;
                    getMagnifiedFrameInContentCoordsLocked(magnifFrameInScreenCoords);
                    if (right - left > magnifFrameInScreenCoords.width()) {
                        if (TextUtils.getLayoutDirectionFromLocale(Locale.getDefault()) == 0) {
                            scrollX = (float) (left - magnifFrameInScreenCoords.left);
                        } else {
                            scrollX = (float) (right - magnifFrameInScreenCoords.right);
                        }
                    } else if (left < magnifFrameInScreenCoords.left) {
                        scrollX = (float) (left - magnifFrameInScreenCoords.left);
                    } else if (right > magnifFrameInScreenCoords.right) {
                        scrollX = (float) (right - magnifFrameInScreenCoords.right);
                    } else {
                        scrollX = 0.0f;
                    }
                    if (bottom - top > magnifFrameInScreenCoords.height()) {
                        scrollY = (float) (top - magnifFrameInScreenCoords.top);
                    } else if (top < magnifFrameInScreenCoords.top) {
                        scrollY = (float) (top - magnifFrameInScreenCoords.top);
                    } else if (bottom > magnifFrameInScreenCoords.bottom) {
                        scrollY = (float) (bottom - magnifFrameInScreenCoords.bottom);
                    } else {
                        scrollY = 0.0f;
                    }
                    float scale = getScale();
                    offsetMagnifiedRegion(scrollX * scale, scrollY * scale, -1);
                }
            }
        }

        /* access modifiers changed from: package-private */
        public void getMagnifiedFrameInContentCoordsLocked(Rect outFrame) {
            float scale = getSentScale();
            float offsetX = getSentOffsetX();
            float offsetY = getSentOffsetY();
            getMagnificationBounds(outFrame);
            outFrame.offset((int) (-offsetX), (int) (-offsetY));
            outFrame.scale(1.0f / scale);
        }

        /* access modifiers changed from: package-private */
        @GuardedBy({"mLock"})
        public void setForceShowMagnifiableBounds(boolean show) {
            if (this.mRegistered) {
                MagnificationController.this.mControllerCtx.getWindowManager().setForceShowMagnifiableBounds(this.mDisplayId, show);
            }
        }

        /* access modifiers changed from: package-private */
        @GuardedBy({"mLock"})
        public boolean reset(boolean animate) {
            if (!this.mRegistered) {
                return false;
            }
            MagnificationSpec spec = this.mCurrentMagnificationSpec;
            boolean changed = !spec.isNop();
            if (changed) {
                spec.clear();
                onMagnificationChangedLocked();
            }
            this.mIdOfLastServiceToMagnify = -1;
            sendSpecToAnimation(spec, animate);
            return changed;
        }

        /* access modifiers changed from: package-private */
        @GuardedBy({"mLock"})
        public boolean setScale(float scale, float pivotX, float pivotY, boolean animate, int id) {
            if (!this.mRegistered) {
                return false;
            }
            float scale2 = MathUtils.constrain(scale, 1.0f, 8.0f);
            Rect viewport = this.mTempRect;
            this.mMagnificationRegion.getBounds(viewport);
            MagnificationSpec spec = this.mCurrentMagnificationSpec;
            float oldScale = spec.scale;
            float oldCenterX = (((((float) viewport.width()) / MagnificationController.DEFAULT_MAGNIFICATION_SCALE) - spec.offsetX) + ((float) viewport.left)) / oldScale;
            float oldCenterY = (((((float) viewport.height()) / MagnificationController.DEFAULT_MAGNIFICATION_SCALE) - spec.offsetY) + ((float) viewport.top)) / oldScale;
            float normPivotX = (pivotX - spec.offsetX) / oldScale;
            float normPivotY = (pivotY - spec.offsetY) / oldScale;
            float offsetX = (oldCenterX - normPivotX) * (oldScale / scale2);
            this.mIdOfLastServiceToMagnify = id;
            return setScaleAndCenter(scale2, normPivotX + offsetX, normPivotY + ((oldCenterY - normPivotY) * (oldScale / scale2)), animate, id);
        }

        /* access modifiers changed from: package-private */
        @GuardedBy({"mLock"})
        public boolean setScaleAndCenter(float scale, float centerX, float centerY, boolean animate, int id) {
            if (!this.mRegistered) {
                return false;
            }
            boolean changed = updateMagnificationSpecLocked(scale, centerX, centerY);
            sendSpecToAnimation(this.mCurrentMagnificationSpec, animate);
            if (isMagnifying() && id != -1) {
                this.mIdOfLastServiceToMagnify = id;
            }
            return changed;
        }

        /* access modifiers changed from: package-private */
        public boolean updateMagnificationSpecLocked(float scale, float centerX, float centerY) {
            if (Float.isNaN(centerX)) {
                centerX = getCenterX();
            }
            if (Float.isNaN(centerY)) {
                centerY = getCenterY();
            }
            if (Float.isNaN(scale)) {
                scale = getScale();
            }
            boolean changed = false;
            float normScale = MathUtils.constrain(scale, 1.0f, 8.0f);
            if (Float.compare(this.mCurrentMagnificationSpec.scale, normScale) != 0) {
                this.mCurrentMagnificationSpec.scale = normScale;
                changed = true;
            }
            boolean changed2 = changed | updateCurrentSpecWithOffsetsLocked(((((float) this.mMagnificationBounds.width()) / MagnificationController.DEFAULT_MAGNIFICATION_SCALE) + ((float) this.mMagnificationBounds.left)) - (centerX * normScale), ((((float) this.mMagnificationBounds.height()) / MagnificationController.DEFAULT_MAGNIFICATION_SCALE) + ((float) this.mMagnificationBounds.top)) - (centerY * normScale));
            if (changed2) {
                onMagnificationChangedLocked();
            }
            return changed2;
        }

        /* access modifiers changed from: package-private */
        @GuardedBy({"mLock"})
        public void offsetMagnifiedRegion(float offsetX, float offsetY, int id) {
            if (this.mRegistered) {
                if (updateCurrentSpecWithOffsetsLocked(this.mCurrentMagnificationSpec.offsetX - offsetX, this.mCurrentMagnificationSpec.offsetY - offsetY)) {
                    onMagnificationChangedLocked();
                }
                if (id != -1) {
                    this.mIdOfLastServiceToMagnify = id;
                }
                sendSpecToAnimation(this.mCurrentMagnificationSpec, false);
            }
        }

        /* access modifiers changed from: package-private */
        public boolean updateCurrentSpecWithOffsetsLocked(float nonNormOffsetX, float nonNormOffsetY) {
            boolean changed = false;
            float offsetX = MathUtils.constrain(nonNormOffsetX, getMinOffsetXLocked(), getMaxOffsetXLocked());
            if (Float.compare(this.mCurrentMagnificationSpec.offsetX, offsetX) != 0) {
                this.mCurrentMagnificationSpec.offsetX = offsetX;
                changed = true;
            }
            float offsetY = MathUtils.constrain(nonNormOffsetY, getMinOffsetYLocked(), getMaxOffsetYLocked());
            if (Float.compare(this.mCurrentMagnificationSpec.offsetY, offsetY) == 0) {
                return changed;
            }
            this.mCurrentMagnificationSpec.offsetY = offsetY;
            return true;
        }

        /* access modifiers changed from: package-private */
        public float getMinOffsetXLocked() {
            float viewportWidth = (float) this.mMagnificationBounds.width();
            float viewportLeft = (float) this.mMagnificationBounds.left;
            return (viewportLeft + viewportWidth) - ((viewportLeft + viewportWidth) * this.mCurrentMagnificationSpec.scale);
        }

        /* access modifiers changed from: package-private */
        public float getMaxOffsetXLocked() {
            return ((float) this.mMagnificationBounds.left) - (((float) this.mMagnificationBounds.left) * this.mCurrentMagnificationSpec.scale);
        }

        /* access modifiers changed from: package-private */
        public float getMinOffsetYLocked() {
            float viewportHeight = (float) this.mMagnificationBounds.height();
            float viewportTop = (float) this.mMagnificationBounds.top;
            return (viewportTop + viewportHeight) - ((viewportTop + viewportHeight) * this.mCurrentMagnificationSpec.scale);
        }

        /* access modifiers changed from: package-private */
        public float getMaxOffsetYLocked() {
            return ((float) this.mMagnificationBounds.top) - (((float) this.mMagnificationBounds.top) * this.mCurrentMagnificationSpec.scale);
        }

        public String toString() {
            return "DisplayMagnification[mCurrentMagnificationSpec=" + this.mCurrentMagnificationSpec + ", mMagnificationRegion=" + this.mMagnificationRegion + ", mMagnificationBounds=" + this.mMagnificationBounds + ", mDisplayId=" + this.mDisplayId + ", mIdOfLastServiceToMagnify=" + this.mIdOfLastServiceToMagnify + ", mRegistered=" + this.mRegistered + ", mUnregisterPending=" + this.mUnregisterPending + ']';
        }
    }

    public MagnificationController(Context context, AccessibilityManagerService ams, Object lock) {
        this(new ControllerContext(context, ams, (WindowManagerInternal) LocalServices.getService(WindowManagerInternal.class), new Handler(context.getMainLooper()), (long) context.getResources().getInteger(17694722)), lock);
    }

    @VisibleForTesting
    public MagnificationController(ControllerContext ctx, Object lock) {
        this.mIsLastMagnifying = false;
        this.mDisplays = new SparseArray<>(0);
        this.mControllerCtx = ctx;
        this.mLock = lock;
        this.mMainThreadId = this.mControllerCtx.getContext().getMainLooper().getThread().getId();
        this.mScreenStateObserver = new ScreenStateObserver(this.mControllerCtx.getContext(), this);
    }

    public void register(int displayId) {
        synchronized (this.mLock) {
            DisplayMagnification display = this.mDisplays.get(displayId);
            if (display == null) {
                display = new DisplayMagnification(displayId);
            }
            if (!display.isRegistered()) {
                if (display.register()) {
                    this.mDisplays.put(displayId, display);
                    this.mScreenStateObserver.registerIfNecessary();
                }
            }
        }
    }

    public void unregister(int displayId) {
        synchronized (this.mLock) {
            unregisterLocked(displayId, false);
        }
    }

    public void unregisterAll() {
        synchronized (this.mLock) {
            SparseArray<DisplayMagnification> displays = this.mDisplays.clone();
            for (int i = 0; i < displays.size(); i++) {
                unregisterLocked(displays.keyAt(i), false);
            }
        }
    }

    public void onDisplayRemoved(int displayId) {
        synchronized (this.mLock) {
            unregisterLocked(displayId, true);
        }
    }

    public boolean isRegistered(int displayId) {
        synchronized (this.mLock) {
            DisplayMagnification display = this.mDisplays.get(displayId);
            if (display == null) {
                return false;
            }
            return display.isRegistered();
        }
    }

    public boolean isMagnifying(int displayId) {
        synchronized (this.mLock) {
            DisplayMagnification display = this.mDisplays.get(displayId);
            if (display == null) {
                return false;
            }
            return display.isMagnifying();
        }
    }

    public boolean magnificationRegionContains(int displayId, float x, float y) {
        synchronized (this.mLock) {
            DisplayMagnification display = this.mDisplays.get(displayId);
            if (display == null) {
                return false;
            }
            return display.magnificationRegionContains(x, y);
        }
    }

    public void getMagnificationBounds(int displayId, Rect outBounds) {
        synchronized (this.mLock) {
            DisplayMagnification display = this.mDisplays.get(displayId);
            if (display != null) {
                display.getMagnificationBounds(outBounds);
            }
        }
    }

    public void getMagnificationRegion(int displayId, Region outRegion) {
        synchronized (this.mLock) {
            DisplayMagnification display = this.mDisplays.get(displayId);
            if (display != null) {
                display.getMagnificationRegion(outRegion);
            }
        }
    }

    public float getScale(int displayId) {
        synchronized (this.mLock) {
            DisplayMagnification display = this.mDisplays.get(displayId);
            if (display == null) {
                return 1.0f;
            }
            return display.getScale();
        }
    }

    public float getOffsetX(int displayId) {
        synchronized (this.mLock) {
            DisplayMagnification display = this.mDisplays.get(displayId);
            if (display == null) {
                return 0.0f;
            }
            return display.getOffsetX();
        }
    }

    public float getCenterX(int displayId) {
        synchronized (this.mLock) {
            DisplayMagnification display = this.mDisplays.get(displayId);
            if (display == null) {
                return 0.0f;
            }
            return display.getCenterX();
        }
    }

    public float getOffsetY(int displayId) {
        synchronized (this.mLock) {
            DisplayMagnification display = this.mDisplays.get(displayId);
            if (display == null) {
                return 0.0f;
            }
            return display.getOffsetY();
        }
    }

    public float getCenterY(int displayId) {
        synchronized (this.mLock) {
            DisplayMagnification display = this.mDisplays.get(displayId);
            if (display == null) {
                return 0.0f;
            }
            return display.getCenterY();
        }
    }

    public boolean reset(int displayId, boolean animate) {
        synchronized (this.mLock) {
            DisplayMagnification display = this.mDisplays.get(displayId);
            if (display == null) {
                return false;
            }
            return display.reset(animate);
        }
    }

    public boolean setScale(int displayId, float scale, float pivotX, float pivotY, boolean animate, int id) {
        synchronized (this.mLock) {
            DisplayMagnification display = this.mDisplays.get(displayId);
            if (display == null) {
                return false;
            }
            return display.setScale(scale, pivotX, pivotY, animate, id);
        }
    }

    public boolean setCenter(int displayId, float centerX, float centerY, boolean animate, int id) {
        synchronized (this.mLock) {
            DisplayMagnification display = this.mDisplays.get(displayId);
            if (display == null) {
                return false;
            }
            return display.setScaleAndCenter(Float.NaN, centerX, centerY, animate, id);
        }
    }

    public boolean setScaleAndCenter(int displayId, float scale, float centerX, float centerY, boolean animate, int id) {
        synchronized (this.mLock) {
            DisplayMagnification display = this.mDisplays.get(displayId);
            if (display == null) {
                return false;
            }
            return display.setScaleAndCenter(scale, centerX, centerY, animate, id);
        }
    }

    public void offsetMagnifiedRegion(int displayId, float offsetX, float offsetY, int id) {
        synchronized (this.mLock) {
            DisplayMagnification display = this.mDisplays.get(displayId);
            if (display != null) {
                display.offsetMagnifiedRegion(offsetX, offsetY, id);
            }
        }
    }

    public int getIdOfLastServiceToMagnify(int displayId) {
        synchronized (this.mLock) {
            DisplayMagnification display = this.mDisplays.get(displayId);
            if (display == null) {
                return -1;
            }
            return display.getIdOfLastServiceToMagnify();
        }
    }

    public void persistScale() {
        final float scale = getScale(0);
        final int userId = this.mUserId;
        new AsyncTask<Void, Void, Void>() {
            /* class com.android.server.accessibility.MagnificationController.AnonymousClass1 */

            /* access modifiers changed from: protected */
            public Void doInBackground(Void... params) {
                MagnificationController.this.mControllerCtx.putMagnificationScale(scale, userId);
                return null;
            }
        }.execute(new Void[0]);
    }

    public float getPersistedScale() {
        return this.mControllerCtx.getMagnificationScale(this.mUserId);
    }

    public void setUserId(int userId) {
        if (this.mUserId != userId) {
            this.mUserId = userId;
            resetAllIfNeeded(false);
        }
    }

    public void resetAllIfNeeded(int connectionId) {
        synchronized (this.mLock) {
            for (int i = 0; i < this.mDisplays.size(); i++) {
                resetIfNeeded(this.mDisplays.keyAt(i), connectionId);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean resetIfNeeded(int displayId, boolean animate) {
        synchronized (this.mLock) {
            DisplayMagnification display = this.mDisplays.get(displayId);
            if (display != null) {
                if (display.isMagnifying()) {
                    display.reset(animate);
                    return true;
                }
            }
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean resetIfNeeded(int displayId, int connectionId) {
        synchronized (this.mLock) {
            DisplayMagnification display = this.mDisplays.get(displayId);
            if (display != null && display.isMagnifying()) {
                if (connectionId == display.getIdOfLastServiceToMagnify()) {
                    display.reset(true);
                    return true;
                }
            }
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    public void setForceShowMagnifiableBounds(int displayId, boolean show) {
        synchronized (this.mLock) {
            DisplayMagnification display = this.mDisplays.get(displayId);
            if (display != null) {
                display.setForceShowMagnifiableBounds(show);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onScreenTurnedOff() {
        this.mControllerCtx.getHandler().sendMessage(PooledLambda.obtainMessage($$Lambda$MagnificationController$UxSkaR2uzdX0ekJv4Wtodc8tuMY.INSTANCE, this, false));
    }

    /* access modifiers changed from: private */
    public void resetAllIfNeeded(boolean animate) {
        synchronized (this.mLock) {
            for (int i = 0; i < this.mDisplays.size(); i++) {
                resetIfNeeded(this.mDisplays.keyAt(i), animate);
            }
        }
    }

    private void unregisterLocked(int displayId, boolean delete) {
        DisplayMagnification display = this.mDisplays.get(displayId);
        if (display != null) {
            if (!display.isRegistered()) {
                if (delete) {
                    this.mDisplays.remove(displayId);
                }
            } else if (!display.isMagnifying()) {
                display.unregister(delete);
            } else {
                display.unregisterPending(delete);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void unregisterCallbackLocked(int displayId, boolean delete) {
        if (delete) {
            this.mDisplays.remove(displayId);
        }
        boolean hasRegister = false;
        int i = 0;
        while (i < this.mDisplays.size() && !(hasRegister = this.mDisplays.valueAt(i).isRegistered())) {
            i++;
        }
        if (!hasRegister) {
            this.mScreenStateObserver.unregister();
        }
    }

    public String toString() {
        return "MagnificationController[mUserId=" + this.mUserId + ", mDisplays=" + this.mDisplays + "]";
    }

    /* access modifiers changed from: private */
    public static class SpecAnimationBridge implements ValueAnimator.AnimatorUpdateListener {
        private final ControllerContext mControllerCtx;
        private final int mDisplayId;
        @GuardedBy({"mLock"})
        private boolean mEnabled;
        private final MagnificationSpec mEndMagnificationSpec;
        private final Object mLock;
        private final MagnificationSpec mSentMagnificationSpec;
        private final MagnificationSpec mStartMagnificationSpec;
        private final MagnificationSpec mTmpMagnificationSpec;
        private final ValueAnimator mValueAnimator;

        private SpecAnimationBridge(ControllerContext ctx, Object lock, int displayId) {
            this.mSentMagnificationSpec = MagnificationSpec.obtain();
            this.mStartMagnificationSpec = MagnificationSpec.obtain();
            this.mEndMagnificationSpec = MagnificationSpec.obtain();
            this.mTmpMagnificationSpec = MagnificationSpec.obtain();
            this.mEnabled = false;
            this.mControllerCtx = ctx;
            this.mLock = lock;
            this.mDisplayId = displayId;
            long animationDuration = this.mControllerCtx.getAnimationDuration();
            this.mValueAnimator = this.mControllerCtx.newValueAnimator();
            this.mValueAnimator.setDuration(animationDuration);
            this.mValueAnimator.setInterpolator(new DecelerateInterpolator(2.5f));
            this.mValueAnimator.setFloatValues(0.0f, 1.0f);
            this.mValueAnimator.addUpdateListener(this);
        }

        public void setEnabled(boolean enabled) {
            synchronized (this.mLock) {
                if (enabled != this.mEnabled) {
                    this.mEnabled = enabled;
                    if (!this.mEnabled) {
                        this.mSentMagnificationSpec.clear();
                        this.mControllerCtx.getWindowManager().setMagnificationSpec(this.mDisplayId, this.mSentMagnificationSpec);
                    }
                }
            }
        }

        public void updateSentSpecMainThread(MagnificationSpec spec, boolean animate) {
            if (this.mValueAnimator.isRunning()) {
                this.mValueAnimator.cancel();
            }
            synchronized (this.mLock) {
                if (!this.mSentMagnificationSpec.equals(spec)) {
                    if (animate) {
                        animateMagnificationSpecLocked(spec);
                    } else {
                        setMagnificationSpecLocked(spec);
                    }
                }
            }
        }

        @GuardedBy({"mLock"})
        private void setMagnificationSpecLocked(MagnificationSpec spec) {
            if (this.mEnabled) {
                this.mSentMagnificationSpec.setTo(spec);
                this.mControllerCtx.getWindowManager().setMagnificationSpec(this.mDisplayId, this.mSentMagnificationSpec);
            }
        }

        private void animateMagnificationSpecLocked(MagnificationSpec toSpec) {
            this.mEndMagnificationSpec.setTo(toSpec);
            this.mStartMagnificationSpec.setTo(this.mSentMagnificationSpec);
            this.mValueAnimator.start();
        }

        @Override // android.animation.ValueAnimator.AnimatorUpdateListener
        public void onAnimationUpdate(ValueAnimator animation) {
            synchronized (this.mLock) {
                if (this.mEnabled) {
                    float fract = animation.getAnimatedFraction();
                    this.mTmpMagnificationSpec.scale = this.mStartMagnificationSpec.scale + ((this.mEndMagnificationSpec.scale - this.mStartMagnificationSpec.scale) * fract);
                    this.mTmpMagnificationSpec.offsetX = this.mStartMagnificationSpec.offsetX + ((this.mEndMagnificationSpec.offsetX - this.mStartMagnificationSpec.offsetX) * fract);
                    this.mTmpMagnificationSpec.offsetY = this.mStartMagnificationSpec.offsetY + ((this.mEndMagnificationSpec.offsetY - this.mStartMagnificationSpec.offsetY) * fract);
                    setMagnificationSpecLocked(this.mTmpMagnificationSpec);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public static class ScreenStateObserver extends BroadcastReceiver {
        private final Context mContext;
        private final MagnificationController mController;
        private boolean mRegistered = false;

        public ScreenStateObserver(Context context, MagnificationController controller) {
            this.mContext = context;
            this.mController = controller;
        }

        public void registerIfNecessary() {
            if (!this.mRegistered) {
                this.mContext.registerReceiver(this, new IntentFilter("android.intent.action.SCREEN_OFF"));
                this.mRegistered = true;
            }
        }

        public void unregister() {
            if (this.mRegistered) {
                this.mContext.unregisterReceiver(this);
                this.mRegistered = false;
            }
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            this.mController.onScreenTurnedOff();
        }
    }

    @VisibleForTesting
    public static class ControllerContext {
        private final AccessibilityManagerService mAms;
        private final Long mAnimationDuration;
        private final Context mContext;
        private final Handler mHandler;
        private final WindowManagerInternal mWindowManager;

        public ControllerContext(Context context, AccessibilityManagerService ams, WindowManagerInternal windowManager, Handler handler, long animationDuration) {
            this.mContext = context;
            this.mAms = ams;
            this.mWindowManager = windowManager;
            this.mHandler = handler;
            this.mAnimationDuration = Long.valueOf(animationDuration);
        }

        public Context getContext() {
            return this.mContext;
        }

        public AccessibilityManagerService getAms() {
            return this.mAms;
        }

        public WindowManagerInternal getWindowManager() {
            return this.mWindowManager;
        }

        public Handler getHandler() {
            return this.mHandler;
        }

        public ValueAnimator newValueAnimator() {
            return new ValueAnimator();
        }

        public void putMagnificationScale(float value, int userId) {
            Settings.Secure.putFloatForUser(this.mContext.getContentResolver(), "accessibility_display_magnification_scale", value, userId);
        }

        public float getMagnificationScale(int userId) {
            return Settings.Secure.getFloatForUser(this.mContext.getContentResolver(), "accessibility_display_magnification_scale", MagnificationController.DEFAULT_MAGNIFICATION_SCALE, userId);
        }

        public long getAnimationDuration() {
            return this.mAnimationDuration.longValue();
        }
    }
}
