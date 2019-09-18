package com.android.server.accessibility;

import android.animation.ValueAnimator;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Rect;
import android.graphics.Region;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.MathUtils;
import android.view.MagnificationSpec;
import android.view.animation.DecelerateInterpolator;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.os.SomeArgs;
import com.android.server.LocalServices;
import com.android.server.wm.WindowManagerInternal;
import java.util.Locale;

public class MagnificationController implements Handler.Callback {
    private static final boolean DEBUG = false;
    private static final boolean DEBUG_SET_MAGNIFICATION_SPEC = false;
    private static final float DEFAULT_MAGNIFICATION_SCALE = 2.0f;
    private static final int INVALID_ID = -1;
    private static final String LOG_TAG = "MagnificationController";
    public static final float MAX_SCALE = 5.0f;
    public static final float MIN_SCALE = 1.0f;
    private static final int MSG_ON_MAGNIFIED_BOUNDS_CHANGED = 3;
    private static final int MSG_ON_RECTANGLE_ON_SCREEN_REQUESTED = 4;
    private static final int MSG_ON_USER_CONTEXT_CHANGED = 5;
    private static final int MSG_SCREEN_TURNED_OFF = 2;
    private static final int MSG_SEND_SPEC_TO_ANIMATION = 1;
    private final AccessibilityManagerService mAms;
    private final MagnificationSpec mCurrentMagnificationSpec;
    /* access modifiers changed from: private */
    public Handler mHandler;
    private int mIdOfLastServiceToMagnify;
    private final Object mLock;
    private final Rect mMagnificationBounds;
    private final Region mMagnificationRegion;
    private final long mMainThreadId;
    @VisibleForTesting
    boolean mRegistered;
    private final ScreenStateObserver mScreenStateObserver;
    /* access modifiers changed from: private */
    public final SettingsBridge mSettingsBridge;
    private final SpecAnimationBridge mSpecAnimationBridge;
    private final Rect mTempRect;
    private final Rect mTempRect1;
    private boolean mUnregisterPending;
    private int mUserId;
    private final WindowManagerInternal.MagnificationCallbacks mWMCallbacks;
    private final WindowManagerInternal mWindowManager;

    private static class ScreenStateObserver extends BroadcastReceiver {
        private final Context mContext;
        private final MagnificationController mController;

        public ScreenStateObserver(Context context, MagnificationController controller) {
            this.mContext = context;
            this.mController = controller;
        }

        public void register() {
            this.mContext.registerReceiver(this, new IntentFilter("android.intent.action.SCREEN_OFF"));
        }

        public void unregister() {
            this.mContext.unregisterReceiver(this);
        }

        public void onReceive(Context context, Intent intent) {
            this.mController.onScreenTurnedOff();
        }
    }

    public static class SettingsBridge {
        private final ContentResolver mContentResolver;

        public SettingsBridge(ContentResolver contentResolver) {
            this.mContentResolver = contentResolver;
        }

        public void putMagnificationScale(float value, int userId) {
            Settings.Secure.putFloatForUser(this.mContentResolver, "accessibility_display_magnification_scale", value, userId);
        }

        public float getMagnificationScale(int userId) {
            return Settings.Secure.getFloatForUser(this.mContentResolver, "accessibility_display_magnification_scale", MagnificationController.DEFAULT_MAGNIFICATION_SCALE, userId);
        }
    }

    private static class SpecAnimationBridge implements ValueAnimator.AnimatorUpdateListener {
        @GuardedBy("mLock")
        private boolean mEnabled;
        private final MagnificationSpec mEndMagnificationSpec;
        private final Object mLock;
        /* access modifiers changed from: private */
        public final MagnificationSpec mSentMagnificationSpec;
        private final MagnificationSpec mStartMagnificationSpec;
        private final MagnificationSpec mTmpMagnificationSpec;
        private final ValueAnimator mValueAnimator;
        private final WindowManagerInternal mWindowManager;

        private SpecAnimationBridge(Context context, Object lock, WindowManagerInternal wm, ValueAnimator animator) {
            this.mSentMagnificationSpec = MagnificationSpec.obtain();
            this.mStartMagnificationSpec = MagnificationSpec.obtain();
            this.mEndMagnificationSpec = MagnificationSpec.obtain();
            this.mTmpMagnificationSpec = MagnificationSpec.obtain();
            this.mEnabled = false;
            this.mLock = lock;
            this.mWindowManager = wm;
            this.mValueAnimator = animator;
            this.mValueAnimator.setDuration((long) context.getResources().getInteger(17694722));
            this.mValueAnimator.setInterpolator(new DecelerateInterpolator(2.5f));
            this.mValueAnimator.setFloatValues(new float[]{0.0f, 1.0f});
            this.mValueAnimator.addUpdateListener(this);
        }

        public void setEnabled(boolean enabled) {
            synchronized (this.mLock) {
                if (enabled != this.mEnabled) {
                    this.mEnabled = enabled;
                    if (!this.mEnabled) {
                        this.mSentMagnificationSpec.clear();
                        this.mWindowManager.setMagnificationSpec(this.mSentMagnificationSpec);
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

        @GuardedBy("mLock")
        private void setMagnificationSpecLocked(MagnificationSpec spec) {
            if (this.mEnabled) {
                this.mSentMagnificationSpec.setTo(spec);
                this.mWindowManager.setMagnificationSpec(spec);
            }
        }

        private void animateMagnificationSpecLocked(MagnificationSpec toSpec) {
            this.mEndMagnificationSpec.setTo(toSpec);
            this.mStartMagnificationSpec.setTo(this.mSentMagnificationSpec);
            this.mValueAnimator.start();
        }

        public void onAnimationUpdate(ValueAnimator animation) {
            synchronized (this.mLock) {
                if (this.mEnabled) {
                    float fract = animation.getAnimatedFraction();
                    this.mTmpMagnificationSpec.scale = this.mStartMagnificationSpec.scale + ((this.mEndMagnificationSpec.scale - this.mStartMagnificationSpec.scale) * fract);
                    this.mTmpMagnificationSpec.offsetX = this.mStartMagnificationSpec.offsetX + ((this.mEndMagnificationSpec.offsetX - this.mStartMagnificationSpec.offsetX) * fract);
                    this.mTmpMagnificationSpec.offsetY = this.mStartMagnificationSpec.offsetY + ((this.mEndMagnificationSpec.offsetY - this.mStartMagnificationSpec.offsetY) * fract);
                    synchronized (this.mLock) {
                        setMagnificationSpecLocked(this.mTmpMagnificationSpec);
                    }
                }
            }
        }
    }

    public MagnificationController(Context context, AccessibilityManagerService ams, Object lock) {
        this(context, ams, lock, null, (WindowManagerInternal) LocalServices.getService(WindowManagerInternal.class), new ValueAnimator(), new SettingsBridge(context.getContentResolver()));
        this.mHandler = new Handler(context.getMainLooper(), this);
    }

    public MagnificationController(Context context, AccessibilityManagerService ams, Object lock, Handler handler, WindowManagerInternal windowManagerInternal, ValueAnimator valueAnimator, SettingsBridge settingsBridge) {
        this.mCurrentMagnificationSpec = MagnificationSpec.obtain();
        this.mMagnificationRegion = Region.obtain();
        this.mMagnificationBounds = new Rect();
        this.mTempRect = new Rect();
        this.mTempRect1 = new Rect();
        this.mWMCallbacks = new WindowManagerInternal.MagnificationCallbacks() {
            public void onMagnificationRegionChanged(Region region) {
                SomeArgs args = SomeArgs.obtain();
                args.arg1 = Region.obtain(region);
                MagnificationController.this.mHandler.obtainMessage(3, args).sendToTarget();
            }

            public void onRectangleOnScreenRequested(int left, int top, int right, int bottom) {
                SomeArgs args = SomeArgs.obtain();
                args.argi1 = left;
                args.argi2 = top;
                args.argi3 = right;
                args.argi4 = bottom;
                MagnificationController.this.mHandler.obtainMessage(4, args).sendToTarget();
            }

            public void onRotationChanged(int rotation) {
                MagnificationController.this.mHandler.sendEmptyMessage(5);
            }

            public void onUserContextChanged() {
                MagnificationController.this.mHandler.sendEmptyMessage(5);
            }
        };
        this.mIdOfLastServiceToMagnify = -1;
        this.mHandler = handler;
        this.mWindowManager = windowManagerInternal;
        this.mMainThreadId = context.getMainLooper().getThread().getId();
        this.mAms = ams;
        this.mScreenStateObserver = new ScreenStateObserver(context, this);
        this.mLock = lock;
        SpecAnimationBridge specAnimationBridge = new SpecAnimationBridge(context, this.mLock, this.mWindowManager, valueAnimator);
        this.mSpecAnimationBridge = specAnimationBridge;
        this.mSettingsBridge = settingsBridge;
    }

    public void register() {
        synchronized (this.mLock) {
            if (!this.mRegistered) {
                this.mScreenStateObserver.register();
                this.mWindowManager.setMagnificationCallbacks(this.mWMCallbacks);
                this.mSpecAnimationBridge.setEnabled(true);
                this.mWindowManager.getMagnificationRegion(this.mMagnificationRegion);
                this.mMagnificationRegion.getBounds(this.mMagnificationBounds);
                this.mRegistered = true;
            }
        }
    }

    public void unregister() {
        synchronized (this.mLock) {
            if (!isMagnifying()) {
                unregisterInternalLocked();
            } else {
                this.mUnregisterPending = true;
                resetLocked(true);
            }
        }
    }

    public boolean isRegisteredLocked() {
        return this.mRegistered;
    }

    private void unregisterInternalLocked() {
        if (this.mRegistered) {
            this.mSpecAnimationBridge.setEnabled(false);
            this.mScreenStateObserver.unregister();
            this.mWindowManager.setMagnificationCallbacks(null);
            this.mMagnificationRegion.setEmpty();
            this.mRegistered = false;
        }
        this.mUnregisterPending = false;
    }

    public boolean isMagnifying() {
        return this.mCurrentMagnificationSpec.scale > 1.0f;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0035, code lost:
        return;
     */
    private void onMagnificationRegionChanged(Region magnified) {
        synchronized (this.mLock) {
            if (this.mRegistered) {
                if (!this.mMagnificationRegion.equals(magnified)) {
                    this.mMagnificationRegion.set(magnified);
                    this.mMagnificationRegion.getBounds(this.mMagnificationBounds);
                    if (updateCurrentSpecWithOffsetsLocked(this.mCurrentMagnificationSpec.offsetX, this.mCurrentMagnificationSpec.offsetY)) {
                        sendSpecToAnimation(this.mCurrentMagnificationSpec, false);
                    }
                    onMagnificationChangedLocked();
                }
            }
        }
    }

    public boolean magnificationRegionContains(float x, float y) {
        boolean contains;
        synchronized (this.mLock) {
            contains = this.mMagnificationRegion.contains((int) x, (int) y);
        }
        return contains;
    }

    public void getMagnificationBounds(Rect outBounds) {
        synchronized (this.mLock) {
            outBounds.set(this.mMagnificationBounds);
        }
    }

    public void getMagnificationRegion(Region outRegion) {
        synchronized (this.mLock) {
            outRegion.set(this.mMagnificationRegion);
        }
    }

    public float getScale() {
        return this.mCurrentMagnificationSpec.scale;
    }

    public float getOffsetX() {
        return this.mCurrentMagnificationSpec.offsetX;
    }

    public float getCenterX() {
        float width;
        synchronized (this.mLock) {
            width = (((((float) this.mMagnificationBounds.width()) / DEFAULT_MAGNIFICATION_SCALE) + ((float) this.mMagnificationBounds.left)) - getOffsetX()) / getScale();
        }
        return width;
    }

    public float getOffsetY() {
        return this.mCurrentMagnificationSpec.offsetY;
    }

    public float getCenterY() {
        float height;
        synchronized (this.mLock) {
            height = (((((float) this.mMagnificationBounds.height()) / DEFAULT_MAGNIFICATION_SCALE) + ((float) this.mMagnificationBounds.top)) - getOffsetY()) / getScale();
        }
        return height;
    }

    private float getSentScale() {
        return this.mSpecAnimationBridge.mSentMagnificationSpec.scale;
    }

    private float getSentOffsetX() {
        return this.mSpecAnimationBridge.mSentMagnificationSpec.offsetX;
    }

    private float getSentOffsetY() {
        return this.mSpecAnimationBridge.mSentMagnificationSpec.offsetY;
    }

    public boolean reset(boolean animate) {
        boolean resetLocked;
        synchronized (this.mLock) {
            resetLocked = resetLocked(animate);
        }
        return resetLocked;
    }

    private boolean resetLocked(boolean animate) {
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

    public boolean setScale(float scale, float pivotX, float pivotY, boolean animate, int id) {
        float f;
        synchronized (this.mLock) {
            try {
                if (!this.mRegistered) {
                    try {
                        return false;
                    } catch (Throwable th) {
                        th = th;
                        float f2 = scale;
                        throw th;
                    }
                } else {
                    f = scale;
                    try {
                        float scale2 = MathUtils.constrain(f, 1.0f, 5.0f);
                        Rect viewport = this.mTempRect;
                        this.mMagnificationRegion.getBounds(viewport);
                        MagnificationSpec spec = this.mCurrentMagnificationSpec;
                        float oldScale = spec.scale;
                        float oldCenterX = (((((float) viewport.width()) / DEFAULT_MAGNIFICATION_SCALE) - spec.offsetX) + ((float) viewport.left)) / oldScale;
                        float oldCenterY = (((((float) viewport.height()) / DEFAULT_MAGNIFICATION_SCALE) - spec.offsetY) + ((float) viewport.top)) / oldScale;
                        float normPivotX = (pivotX - spec.offsetX) / oldScale;
                        float normPivotY = (pivotY - spec.offsetY) / oldScale;
                        this.mIdOfLastServiceToMagnify = id;
                        boolean scaleAndCenterLocked = setScaleAndCenterLocked(scale2, normPivotX + ((oldCenterX - normPivotX) * (oldScale / scale2)), normPivotY + ((oldCenterY - normPivotY) * (oldScale / scale2)), animate, id);
                        return scaleAndCenterLocked;
                    } catch (Throwable th2) {
                        th = th2;
                        throw th;
                    }
                }
            } catch (Throwable th3) {
                th = th3;
                f = scale;
                float scale3 = f;
                throw th;
            }
        }
    }

    public boolean setCenter(float centerX, float centerY, boolean animate, int id) {
        synchronized (this.mLock) {
            if (!this.mRegistered) {
                return false;
            }
            boolean scaleAndCenterLocked = setScaleAndCenterLocked(Float.NaN, centerX, centerY, animate, id);
            return scaleAndCenterLocked;
        }
    }

    public boolean setScaleAndCenter(float scale, float centerX, float centerY, boolean animate, int id) {
        synchronized (this.mLock) {
            if (!this.mRegistered) {
                return false;
            }
            boolean scaleAndCenterLocked = setScaleAndCenterLocked(scale, centerX, centerY, animate, id);
            return scaleAndCenterLocked;
        }
    }

    private boolean setScaleAndCenterLocked(float scale, float centerX, float centerY, boolean animate, int id) {
        boolean changed = updateMagnificationSpecLocked(scale, centerX, centerY);
        sendSpecToAnimation(this.mCurrentMagnificationSpec, animate);
        if (isMagnifying() && id != -1) {
            this.mIdOfLastServiceToMagnify = id;
        }
        return changed;
    }

    public void offsetMagnifiedRegion(float offsetX, float offsetY, int id) {
        synchronized (this.mLock) {
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
    }

    public int getIdOfLastServiceToMagnify() {
        return this.mIdOfLastServiceToMagnify;
    }

    private void onMagnificationChangedLocked() {
        this.mAms.notifyMagnificationChanged(this.mMagnificationRegion, getScale(), getCenterX(), getCenterY());
        if (this.mUnregisterPending && !isMagnifying()) {
            unregisterInternalLocked();
        }
    }

    public void persistScale() {
        final float scale = this.mCurrentMagnificationSpec.scale;
        final int userId = this.mUserId;
        new AsyncTask<Void, Void, Void>() {
            /* access modifiers changed from: protected */
            public Void doInBackground(Void... params) {
                MagnificationController.this.mSettingsBridge.putMagnificationScale(scale, userId);
                return null;
            }
        }.execute(new Void[0]);
    }

    public float getPersistedScale() {
        return this.mSettingsBridge.getMagnificationScale(this.mUserId);
    }

    private boolean updateMagnificationSpecLocked(float scale, float centerX, float centerY) {
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
        float normScale = MathUtils.constrain(scale, 1.0f, 5.0f);
        if (Float.compare(this.mCurrentMagnificationSpec.scale, normScale) != 0) {
            this.mCurrentMagnificationSpec.scale = normScale;
            changed = true;
        }
        boolean changed2 = changed | updateCurrentSpecWithOffsetsLocked(((((float) this.mMagnificationBounds.width()) / DEFAULT_MAGNIFICATION_SCALE) + ((float) this.mMagnificationBounds.left)) - (centerX * normScale), ((((float) this.mMagnificationBounds.height()) / DEFAULT_MAGNIFICATION_SCALE) + ((float) this.mMagnificationBounds.top)) - (centerY * normScale));
        if (changed2) {
            onMagnificationChangedLocked();
        }
        return changed2;
    }

    private boolean updateCurrentSpecWithOffsetsLocked(float nonNormOffsetX, float nonNormOffsetY) {
        boolean changed = false;
        float offsetX = MathUtils.constrain(nonNormOffsetX, getMinOffsetXLocked(), 0.0f);
        if (Float.compare(this.mCurrentMagnificationSpec.offsetX, offsetX) != 0) {
            this.mCurrentMagnificationSpec.offsetX = offsetX;
            changed = true;
        }
        float offsetY = MathUtils.constrain(nonNormOffsetY, getMinOffsetYLocked(), 0.0f);
        if (Float.compare(this.mCurrentMagnificationSpec.offsetY, offsetY) == 0) {
            return changed;
        }
        this.mCurrentMagnificationSpec.offsetY = offsetY;
        return true;
    }

    private float getMinOffsetXLocked() {
        float viewportWidth = (float) this.mMagnificationBounds.width();
        return viewportWidth - (this.mCurrentMagnificationSpec.scale * viewportWidth);
    }

    private float getMinOffsetYLocked() {
        float viewportHeight = (float) this.mMagnificationBounds.height();
        return viewportHeight - (this.mCurrentMagnificationSpec.scale * viewportHeight);
    }

    public void setUserId(int userId) {
        if (this.mUserId != userId) {
            this.mUserId = userId;
            synchronized (this.mLock) {
                if (isMagnifying()) {
                    reset(false);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean resetIfNeeded(boolean animate) {
        synchronized (this.mLock) {
            if (!isMagnifying()) {
                return false;
            }
            reset(animate);
            return true;
        }
    }

    /* access modifiers changed from: package-private */
    public void setForceShowMagnifiableBounds(boolean show) {
        if (this.mRegistered) {
            this.mWindowManager.setForceShowMagnifiableBounds(show);
        }
    }

    private void getMagnifiedFrameInContentCoordsLocked(Rect outFrame) {
        float scale = getSentScale();
        float offsetX = getSentOffsetX();
        float offsetY = getSentOffsetY();
        getMagnificationBounds(outFrame);
        outFrame.offset((int) (-offsetX), (int) (-offsetY));
        outFrame.scale(1.0f / scale);
    }

    private void requestRectangleOnScreen(int left, int top, int right, int bottom) {
        float scrollX;
        float scrollX2;
        synchronized (this.mLock) {
            Rect magnifiedFrame = this.mTempRect;
            getMagnificationBounds(magnifiedFrame);
            if (magnifiedFrame.intersects(left, top, right, bottom)) {
                Rect magnifFrameInScreenCoords = this.mTempRect1;
                getMagnifiedFrameInContentCoordsLocked(magnifFrameInScreenCoords);
                float scrollY = 0.0f;
                if (right - left > magnifFrameInScreenCoords.width()) {
                    if (TextUtils.getLayoutDirectionFromLocale(Locale.getDefault()) == 0) {
                        scrollX2 = (float) (left - magnifFrameInScreenCoords.left);
                    } else {
                        scrollX2 = (float) (right - magnifFrameInScreenCoords.right);
                    }
                    scrollX = scrollX2;
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
                } else {
                    if (bottom > magnifFrameInScreenCoords.bottom) {
                        scrollY = (float) (bottom - magnifFrameInScreenCoords.bottom);
                    }
                    float scrollY2 = scrollY;
                    float scale = getScale();
                    offsetMagnifiedRegion(scrollX * scale, scrollY2 * scale, -1);
                }
                float scrollY22 = scrollY;
                float scale2 = getScale();
                offsetMagnifiedRegion(scrollX * scale2, scrollY22 * scale2, -1);
            }
        }
    }

    private void sendSpecToAnimation(MagnificationSpec spec, boolean animate) {
        if (Thread.currentThread().getId() == this.mMainThreadId) {
            this.mSpecAnimationBridge.updateSentSpecMainThread(spec, animate);
        } else {
            this.mHandler.obtainMessage(1, animate, 0, spec).sendToTarget();
        }
    }

    /* access modifiers changed from: private */
    public void onScreenTurnedOff() {
        this.mHandler.sendEmptyMessage(2);
    }

    public boolean handleMessage(Message msg) {
        boolean animate = false;
        switch (msg.what) {
            case 1:
                if (msg.arg1 == 1) {
                    animate = true;
                }
                this.mSpecAnimationBridge.updateSentSpecMainThread((MagnificationSpec) msg.obj, animate);
                break;
            case 2:
                resetIfNeeded(false);
                break;
            case 3:
                SomeArgs args = (SomeArgs) msg.obj;
                Region magnifiedBounds = (Region) args.arg1;
                onMagnificationRegionChanged(magnifiedBounds);
                magnifiedBounds.recycle();
                args.recycle();
                break;
            case 4:
                SomeArgs args2 = (SomeArgs) msg.obj;
                requestRectangleOnScreen(args2.argi1, args2.argi2, args2.argi3, args2.argi4);
                args2.recycle();
                break;
            case 5:
                resetIfNeeded(true);
                break;
        }
        return true;
    }

    public String toString() {
        return "MagnificationController{mCurrentMagnificationSpec=" + this.mCurrentMagnificationSpec + ", mMagnificationRegion=" + this.mMagnificationRegion + ", mMagnificationBounds=" + this.mMagnificationBounds + ", mUserId=" + this.mUserId + ", mIdOfLastServiceToMagnify=" + this.mIdOfLastServiceToMagnify + ", mRegistered=" + this.mRegistered + ", mUnregisterPending=" + this.mUnregisterPending + '}';
    }
}
