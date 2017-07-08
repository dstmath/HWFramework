package com.android.server.accessibility;

import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
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
import android.provider.Settings.Secure;
import android.text.TextUtils;
import android.util.MathUtils;
import android.util.Property;
import android.view.MagnificationSpec;
import android.view.WindowManagerInternal;
import android.view.WindowManagerInternal.MagnificationCallbacks;
import android.view.animation.DecelerateInterpolator;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.os.SomeArgs;
import com.android.server.LocalServices;
import java.util.Locale;

class MagnificationController {
    private static final boolean DEBUG_SET_MAGNIFICATION_SPEC = false;
    private static final float DEFAULT_MAGNIFICATION_SCALE = 2.0f;
    private static final int DEFAULT_SCREEN_MAGNIFICATION_AUTO_UPDATE = 1;
    private static final int INVALID_ID = -1;
    private static final String LOG_TAG = "MagnificationController";
    private static final float MAX_SCALE = 5.0f;
    private static final float MIN_PERSISTED_SCALE = 2.0f;
    private static final float MIN_SCALE = 1.0f;
    private final AccessibilityManagerService mAms;
    private final ContentResolver mContentResolver;
    private final MagnificationSpec mCurrentMagnificationSpec;
    private int mIdOfLastServiceToMagnify;
    private final Object mLock;
    private final Rect mMagnificationBounds;
    private final Region mMagnificationRegion;
    private boolean mRegistered;
    private final ScreenStateObserver mScreenStateObserver;
    private final SpecAnimationBridge mSpecAnimationBridge;
    private final Rect mTempRect;
    private final Rect mTempRect1;
    private boolean mUnregisterPending;
    private int mUserId;
    private final WindowStateObserver mWindowStateObserver;

    /* renamed from: com.android.server.accessibility.MagnificationController.1 */
    class AnonymousClass1 extends AsyncTask<Void, Void, Void> {
        final /* synthetic */ float val$scale;
        final /* synthetic */ int val$userId;

        AnonymousClass1(float val$scale, int val$userId) {
            this.val$scale = val$scale;
            this.val$userId = val$userId;
        }

        protected Void doInBackground(Void... params) {
            Secure.putFloatForUser(MagnificationController.this.mContentResolver, "accessibility_display_magnification_scale", this.val$scale, this.val$userId);
            return null;
        }
    }

    private static class ScreenStateObserver extends BroadcastReceiver {
        private static final int MESSAGE_ON_SCREEN_STATE_CHANGE = 1;
        private final Context mContext;
        private final MagnificationController mController;
        private final Handler mHandler;

        private class StateChangeHandler extends Handler {
            public StateChangeHandler(Context context) {
                super(context.getMainLooper());
            }

            public void handleMessage(Message message) {
                switch (message.what) {
                    case ScreenStateObserver.MESSAGE_ON_SCREEN_STATE_CHANGE /*1*/:
                        ScreenStateObserver.this.handleOnScreenStateChange();
                    default:
                }
            }
        }

        public ScreenStateObserver(Context context, MagnificationController controller) {
            this.mContext = context;
            this.mController = controller;
            this.mHandler = new StateChangeHandler(context);
        }

        public void register() {
            this.mContext.registerReceiver(this, new IntentFilter("android.intent.action.SCREEN_OFF"));
        }

        public void unregister() {
            this.mContext.unregisterReceiver(this);
        }

        public void onReceive(Context context, Intent intent) {
            this.mHandler.obtainMessage(MESSAGE_ON_SCREEN_STATE_CHANGE, intent.getAction()).sendToTarget();
        }

        private void handleOnScreenStateChange() {
            this.mController.resetIfNeeded(MagnificationController.DEBUG_SET_MAGNIFICATION_SPEC);
        }
    }

    private static class SpecAnimationBridge {
        private static final int ACTION_UPDATE_SPEC = 1;
        @GuardedBy("mLock")
        private boolean mEnabled;
        private final Handler mHandler;
        private final Object mLock;
        private final long mMainThreadId;
        private final MagnificationSpec mSentMagnificationSpec;
        private final ValueAnimator mTransformationAnimator;
        private final WindowManagerInternal mWindowManager;

        private static class MagnificationSpecEvaluator implements TypeEvaluator<MagnificationSpec> {
            private final MagnificationSpec mTempSpec;

            private MagnificationSpecEvaluator() {
                this.mTempSpec = MagnificationSpec.obtain();
            }

            public MagnificationSpec evaluate(float fraction, MagnificationSpec fromSpec, MagnificationSpec toSpec) {
                MagnificationSpec result = this.mTempSpec;
                result.scale = fromSpec.scale + ((toSpec.scale - fromSpec.scale) * fraction);
                result.offsetX = fromSpec.offsetX + ((toSpec.offsetX - fromSpec.offsetX) * fraction);
                result.offsetY = fromSpec.offsetY + ((toSpec.offsetY - fromSpec.offsetY) * fraction);
                return result;
            }
        }

        private static class MagnificationSpecProperty extends Property<SpecAnimationBridge, MagnificationSpec> {
            public MagnificationSpecProperty() {
                super(MagnificationSpec.class, "spec");
            }

            public MagnificationSpec get(SpecAnimationBridge object) {
                MagnificationSpec -get1;
                synchronized (object.mLock) {
                    -get1 = object.mSentMagnificationSpec;
                }
                return -get1;
            }

            public void set(SpecAnimationBridge object, MagnificationSpec value) {
                synchronized (object.mLock) {
                    object.setMagnificationSpecLocked(value);
                }
            }
        }

        private class UpdateHandler extends Handler {
            public UpdateHandler(Context context) {
                super(context.getMainLooper());
            }

            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case SpecAnimationBridge.ACTION_UPDATE_SPEC /*1*/:
                        SpecAnimationBridge.this.updateSentSpecInternal(msg.obj, msg.arg1 == SpecAnimationBridge.ACTION_UPDATE_SPEC ? true : MagnificationController.DEBUG_SET_MAGNIFICATION_SPEC);
                    default:
                }
            }
        }

        private SpecAnimationBridge(Context context, Object lock) {
            this.mSentMagnificationSpec = MagnificationSpec.obtain();
            this.mEnabled = MagnificationController.DEBUG_SET_MAGNIFICATION_SPEC;
            this.mLock = lock;
            this.mMainThreadId = context.getMainLooper().getThread().getId();
            this.mHandler = new UpdateHandler(context);
            this.mWindowManager = (WindowManagerInternal) LocalServices.getService(WindowManagerInternal.class);
            MagnificationSpecProperty property = new MagnificationSpecProperty();
            MagnificationSpecEvaluator evaluator = new MagnificationSpecEvaluator();
            long animationDuration = (long) context.getResources().getInteger(17694722);
            MagnificationSpec[] magnificationSpecArr = new MagnificationSpec[ACTION_UPDATE_SPEC];
            magnificationSpecArr[0] = this.mSentMagnificationSpec;
            this.mTransformationAnimator = ObjectAnimator.ofObject(this, property, evaluator, magnificationSpecArr);
            this.mTransformationAnimator.setDuration(animationDuration);
            this.mTransformationAnimator.setInterpolator(new DecelerateInterpolator(2.5f));
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

        public void updateSentSpec(MagnificationSpec spec, boolean animate) {
            if (Thread.currentThread().getId() == this.mMainThreadId) {
                updateSentSpecInternal(spec, animate);
                return;
            }
            int i;
            Handler handler = this.mHandler;
            if (animate) {
                i = ACTION_UPDATE_SPEC;
            } else {
                i = 0;
            }
            handler.obtainMessage(ACTION_UPDATE_SPEC, i, 0, spec).sendToTarget();
        }

        private void updateSentSpecInternal(MagnificationSpec spec, boolean animate) {
            if (this.mTransformationAnimator.isRunning()) {
                this.mTransformationAnimator.cancel();
            }
            synchronized (this.mLock) {
                if (this.mSentMagnificationSpec.equals(spec) ? MagnificationController.DEBUG_SET_MAGNIFICATION_SPEC : true) {
                    if (animate) {
                        animateMagnificationSpecLocked(spec);
                    } else {
                        setMagnificationSpecLocked(spec);
                    }
                }
            }
        }

        private void animateMagnificationSpecLocked(MagnificationSpec toSpec) {
            this.mTransformationAnimator.setObjectValues(new Object[]{this.mSentMagnificationSpec, toSpec});
            this.mTransformationAnimator.start();
        }

        private void setMagnificationSpecLocked(MagnificationSpec spec) {
            if (this.mEnabled) {
                this.mSentMagnificationSpec.setTo(spec);
                this.mWindowManager.setMagnificationSpec(spec);
            }
        }
    }

    private static class WindowStateObserver implements MagnificationCallbacks {
        private static final int MESSAGE_ON_MAGNIFIED_BOUNDS_CHANGED = 1;
        private static final int MESSAGE_ON_RECTANGLE_ON_SCREEN_REQUESTED = 2;
        private static final int MESSAGE_ON_ROTATION_CHANGED = 4;
        private static final int MESSAGE_ON_USER_CONTEXT_CHANGED = 3;
        private final MagnificationController mController;
        private final Handler mHandler;
        private boolean mSpecIsDirty;
        private final WindowManagerInternal mWindowManager;

        private class CallbackHandler extends Handler {
            public CallbackHandler(Context context) {
                super(context.getMainLooper());
            }

            public void handleMessage(Message message) {
                switch (message.what) {
                    case WindowStateObserver.MESSAGE_ON_MAGNIFIED_BOUNDS_CHANGED /*1*/:
                        Region magnifiedBounds = message.obj.arg1;
                        WindowStateObserver.this.handleOnMagnifiedBoundsChanged(magnifiedBounds);
                        magnifiedBounds.recycle();
                    case WindowStateObserver.MESSAGE_ON_RECTANGLE_ON_SCREEN_REQUESTED /*2*/:
                        SomeArgs args = (SomeArgs) message.obj;
                        WindowStateObserver.this.handleOnRectangleOnScreenRequested(args.argi1, args.argi2, args.argi3, args.argi4);
                        args.recycle();
                    case WindowStateObserver.MESSAGE_ON_USER_CONTEXT_CHANGED /*3*/:
                        WindowStateObserver.this.handleOnUserContextChanged();
                    case WindowStateObserver.MESSAGE_ON_ROTATION_CHANGED /*4*/:
                        WindowStateObserver.this.handleOnRotationChanged();
                    default:
                }
            }
        }

        public WindowStateObserver(Context context, MagnificationController controller) {
            this.mController = controller;
            this.mWindowManager = (WindowManagerInternal) LocalServices.getService(WindowManagerInternal.class);
            this.mHandler = new CallbackHandler(context);
        }

        public void register() {
            this.mWindowManager.setMagnificationCallbacks(this);
        }

        public void unregister() {
            this.mWindowManager.setMagnificationCallbacks(null);
        }

        public void onMagnificationRegionChanged(Region magnificationRegion) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = Region.obtain(magnificationRegion);
            this.mHandler.obtainMessage(MESSAGE_ON_MAGNIFIED_BOUNDS_CHANGED, args).sendToTarget();
        }

        private void handleOnMagnifiedBoundsChanged(Region magnificationRegion) {
            this.mController.onMagnificationRegionChanged(magnificationRegion, this.mSpecIsDirty);
            this.mSpecIsDirty = MagnificationController.DEBUG_SET_MAGNIFICATION_SPEC;
        }

        public void onRectangleOnScreenRequested(int left, int top, int right, int bottom) {
            SomeArgs args = SomeArgs.obtain();
            args.argi1 = left;
            args.argi2 = top;
            args.argi3 = right;
            args.argi4 = bottom;
            this.mHandler.obtainMessage(MESSAGE_ON_RECTANGLE_ON_SCREEN_REQUESTED, args).sendToTarget();
        }

        private void handleOnRectangleOnScreenRequested(int left, int top, int right, int bottom) {
            this.mController.requestRectangleOnScreen(left, top, right, bottom);
        }

        public void onRotationChanged(int rotation) {
            this.mHandler.obtainMessage(MESSAGE_ON_ROTATION_CHANGED, rotation, 0).sendToTarget();
        }

        private void handleOnRotationChanged() {
            boolean z = true;
            if (this.mController.resetIfNeeded(true)) {
                z = MagnificationController.DEBUG_SET_MAGNIFICATION_SPEC;
            }
            this.mSpecIsDirty = z;
        }

        public void onUserContextChanged() {
            this.mHandler.sendEmptyMessage(MESSAGE_ON_USER_CONTEXT_CHANGED);
        }

        private void handleOnUserContextChanged() {
            this.mController.resetIfNeeded(true);
        }

        public void getMagnificationRegion(Region outMagnificationRegion) {
            this.mWindowManager.getMagnificationRegion(outMagnificationRegion);
        }
    }

    public MagnificationController(Context context, AccessibilityManagerService ams, Object lock) {
        this.mCurrentMagnificationSpec = MagnificationSpec.obtain();
        this.mMagnificationRegion = Region.obtain();
        this.mMagnificationBounds = new Rect();
        this.mTempRect = new Rect();
        this.mTempRect1 = new Rect();
        this.mIdOfLastServiceToMagnify = INVALID_ID;
        this.mAms = ams;
        this.mContentResolver = context.getContentResolver();
        this.mScreenStateObserver = new ScreenStateObserver(context, this);
        this.mWindowStateObserver = new WindowStateObserver(context, this);
        this.mLock = lock;
        this.mSpecAnimationBridge = new SpecAnimationBridge(this.mLock, null);
    }

    public void register() {
        synchronized (this.mLock) {
            if (!this.mRegistered) {
                this.mScreenStateObserver.register();
                this.mWindowStateObserver.register();
                this.mSpecAnimationBridge.setEnabled(true);
                this.mWindowStateObserver.getMagnificationRegion(this.mMagnificationRegion);
                this.mMagnificationRegion.getBounds(this.mMagnificationBounds);
                this.mRegistered = true;
            }
        }
    }

    public void unregister() {
        synchronized (this.mLock) {
            if (isMagnifying()) {
                this.mUnregisterPending = true;
                resetLocked(true);
            } else {
                unregisterInternalLocked();
            }
        }
    }

    public boolean isRegisteredLocked() {
        return this.mRegistered;
    }

    private void unregisterInternalLocked() {
        if (this.mRegistered) {
            this.mSpecAnimationBridge.setEnabled(DEBUG_SET_MAGNIFICATION_SPEC);
            this.mScreenStateObserver.unregister();
            this.mWindowStateObserver.unregister();
            this.mMagnificationRegion.setEmpty();
            this.mRegistered = DEBUG_SET_MAGNIFICATION_SPEC;
        }
        this.mUnregisterPending = DEBUG_SET_MAGNIFICATION_SPEC;
    }

    public boolean isMagnifying() {
        return this.mCurrentMagnificationSpec.scale > MIN_SCALE ? true : DEBUG_SET_MAGNIFICATION_SPEC;
    }

    private void onMagnificationRegionChanged(Region magnified, boolean updateSpec) {
        synchronized (this.mLock) {
            if (this.mRegistered) {
                boolean magnificationChanged = DEBUG_SET_MAGNIFICATION_SPEC;
                boolean boundsChanged = DEBUG_SET_MAGNIFICATION_SPEC;
                if (!this.mMagnificationRegion.equals(magnified)) {
                    this.mMagnificationRegion.set(magnified);
                    this.mMagnificationRegion.getBounds(this.mMagnificationBounds);
                    boundsChanged = true;
                }
                if (updateSpec) {
                    MagnificationSpec sentSpec = this.mSpecAnimationBridge.mSentMagnificationSpec;
                    float scale = sentSpec.scale;
                    float offsetX = sentSpec.offsetX;
                    magnificationChanged = setScaleAndCenterLocked(scale, (((((float) this.mMagnificationBounds.width()) / MIN_PERSISTED_SCALE) + ((float) this.mMagnificationBounds.left)) - offsetX) / scale, (((((float) this.mMagnificationBounds.height()) / MIN_PERSISTED_SCALE) + ((float) this.mMagnificationBounds.top)) - sentSpec.offsetY) / scale, DEBUG_SET_MAGNIFICATION_SPEC, INVALID_ID);
                }
                if (boundsChanged && updateSpec && !r7) {
                    onMagnificationChangedLocked();
                }
                return;
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
            width = (((((float) this.mMagnificationBounds.width()) / MIN_PERSISTED_SCALE) + ((float) this.mMagnificationBounds.left)) - getOffsetX()) / getScale();
        }
        return width;
    }

    public float getOffsetY() {
        return this.mCurrentMagnificationSpec.offsetY;
    }

    public float getCenterY() {
        float height;
        synchronized (this.mLock) {
            height = (((((float) this.mMagnificationBounds.height()) / MIN_PERSISTED_SCALE) + ((float) this.mMagnificationBounds.top)) - getOffsetY()) / getScale();
        }
        return height;
    }

    public float getSentScale() {
        return this.mSpecAnimationBridge.mSentMagnificationSpec.scale;
    }

    public float getSentOffsetX() {
        return this.mSpecAnimationBridge.mSentMagnificationSpec.offsetX;
    }

    public float getSentOffsetY() {
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
        boolean changed = DEBUG_SET_MAGNIFICATION_SPEC;
        if (!this.mRegistered) {
            return DEBUG_SET_MAGNIFICATION_SPEC;
        }
        MagnificationSpec spec = this.mCurrentMagnificationSpec;
        if (!spec.isNop()) {
            changed = true;
        }
        if (changed) {
            spec.clear();
            onMagnificationChangedLocked();
        }
        this.mIdOfLastServiceToMagnify = INVALID_ID;
        this.mSpecAnimationBridge.updateSentSpec(spec, animate);
        return changed;
    }

    public boolean setScale(float scale, float pivotX, float pivotY, boolean animate, int id) {
        synchronized (this.mLock) {
            if (this.mRegistered) {
                scale = MathUtils.constrain(scale, MIN_SCALE, MAX_SCALE);
                Rect viewport = this.mTempRect;
                this.mMagnificationRegion.getBounds(viewport);
                MagnificationSpec spec = this.mCurrentMagnificationSpec;
                float oldScale = spec.scale;
                float normPivotX = (pivotX - spec.offsetX) / oldScale;
                float normPivotY = (pivotY - spec.offsetY) / oldScale;
                float centerX = normPivotX + (((((((float) viewport.width()) / MIN_PERSISTED_SCALE) - spec.offsetX) / oldScale) - normPivotX) * (oldScale / scale));
                float centerY = normPivotY + (((((((float) viewport.height()) / MIN_PERSISTED_SCALE) - spec.offsetY) / oldScale) - normPivotY) * (oldScale / scale));
                this.mIdOfLastServiceToMagnify = id;
                boolean scaleAndCenterLocked = setScaleAndCenterLocked(scale, centerX, centerY, animate, id);
                return scaleAndCenterLocked;
            }
            return DEBUG_SET_MAGNIFICATION_SPEC;
        }
    }

    public boolean setCenter(float centerX, float centerY, boolean animate, int id) {
        synchronized (this.mLock) {
            if (this.mRegistered) {
                boolean scaleAndCenterLocked = setScaleAndCenterLocked(Float.NaN, centerX, centerY, animate, id);
                return scaleAndCenterLocked;
            }
            return DEBUG_SET_MAGNIFICATION_SPEC;
        }
    }

    public boolean setScaleAndCenter(float scale, float centerX, float centerY, boolean animate, int id) {
        synchronized (this.mLock) {
            if (this.mRegistered) {
                boolean scaleAndCenterLocked = setScaleAndCenterLocked(scale, centerX, centerY, animate, id);
                return scaleAndCenterLocked;
            }
            return DEBUG_SET_MAGNIFICATION_SPEC;
        }
    }

    private boolean setScaleAndCenterLocked(float scale, float centerX, float centerY, boolean animate, int id) {
        boolean changed = updateMagnificationSpecLocked(scale, centerX, centerY);
        this.mSpecAnimationBridge.updateSentSpec(this.mCurrentMagnificationSpec, animate);
        if (isMagnifying() && id != INVALID_ID) {
            this.mIdOfLastServiceToMagnify = id;
        }
        return changed;
    }

    public void offsetMagnifiedRegionCenter(float offsetX, float offsetY, int id) {
        synchronized (this.mLock) {
            if (this.mRegistered) {
                MagnificationSpec currSpec = this.mCurrentMagnificationSpec;
                currSpec.offsetX = MathUtils.constrain(currSpec.offsetX - offsetX, getMinOffsetXLocked(), 0.0f);
                currSpec.offsetY = MathUtils.constrain(currSpec.offsetY - offsetY, getMinOffsetYLocked(), 0.0f);
                if (id != INVALID_ID) {
                    this.mIdOfLastServiceToMagnify = id;
                }
                this.mSpecAnimationBridge.updateSentSpec(currSpec, DEBUG_SET_MAGNIFICATION_SPEC);
                return;
            }
        }
    }

    public int getIdOfLastServiceToMagnify() {
        return this.mIdOfLastServiceToMagnify;
    }

    private void onMagnificationChangedLocked() {
        this.mAms.onMagnificationStateChanged();
        this.mAms.notifyMagnificationChanged(this.mMagnificationRegion, getScale(), getCenterX(), getCenterY());
        if (this.mUnregisterPending && !isMagnifying()) {
            unregisterInternalLocked();
        }
    }

    public void persistScale() {
        new AnonymousClass1(this.mCurrentMagnificationSpec.scale, this.mUserId).execute(new Void[0]);
    }

    public float getPersistedScale() {
        return Secure.getFloatForUser(this.mContentResolver, "accessibility_display_magnification_scale", MIN_PERSISTED_SCALE, this.mUserId);
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
        if (!magnificationRegionContains(centerX, centerY)) {
            return DEBUG_SET_MAGNIFICATION_SPEC;
        }
        MagnificationSpec currSpec = this.mCurrentMagnificationSpec;
        boolean changed = DEBUG_SET_MAGNIFICATION_SPEC;
        float normScale = MathUtils.constrain(scale, MIN_SCALE, MAX_SCALE);
        if (Float.compare(currSpec.scale, normScale) != 0) {
            currSpec.scale = normScale;
            changed = true;
        }
        float offsetX = MathUtils.constrain(((((float) this.mMagnificationBounds.width()) / MIN_PERSISTED_SCALE) + ((float) this.mMagnificationBounds.left)) - (centerX * scale), getMinOffsetXLocked(), 0.0f);
        if (Float.compare(currSpec.offsetX, offsetX) != 0) {
            currSpec.offsetX = offsetX;
            changed = true;
        }
        float offsetY = MathUtils.constrain(((((float) this.mMagnificationBounds.height()) / MIN_PERSISTED_SCALE) + ((float) this.mMagnificationBounds.top)) - (centerY * scale), getMinOffsetYLocked(), 0.0f);
        if (Float.compare(currSpec.offsetY, offsetY) != 0) {
            currSpec.offsetY = offsetY;
            changed = true;
        }
        if (changed) {
            onMagnificationChangedLocked();
        }
        return changed;
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
                    reset(DEBUG_SET_MAGNIFICATION_SPEC);
                }
            }
        }
    }

    private boolean isScreenMagnificationAutoUpdateEnabled() {
        return Secure.getInt(this.mContentResolver, "accessibility_display_magnification_auto_update", DEFAULT_SCREEN_MAGNIFICATION_AUTO_UPDATE) == DEFAULT_SCREEN_MAGNIFICATION_AUTO_UPDATE ? true : DEBUG_SET_MAGNIFICATION_SPEC;
    }

    boolean resetIfNeeded(boolean animate) {
        synchronized (this.mLock) {
            if (isMagnifying() && isScreenMagnificationAutoUpdateEnabled()) {
                reset(animate);
                return true;
            }
            return DEBUG_SET_MAGNIFICATION_SPEC;
        }
    }

    private void getMagnifiedFrameInContentCoordsLocked(Rect outFrame) {
        float scale = getSentScale();
        float offsetX = getSentOffsetX();
        float offsetY = getSentOffsetY();
        getMagnificationBounds(outFrame);
        outFrame.offset((int) (-offsetX), (int) (-offsetY));
        outFrame.scale(MIN_SCALE / scale);
    }

    private void requestRectangleOnScreen(int left, int top, int right, int bottom) {
        synchronized (this.mLock) {
            Rect magnifiedFrame = this.mTempRect;
            getMagnificationBounds(magnifiedFrame);
            if (magnifiedFrame.intersects(left, top, right, bottom)) {
                float scrollX;
                float scrollY;
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
                offsetMagnifiedRegionCenter(scrollX * scale, scrollY * scale, INVALID_ID);
                return;
            }
        }
    }
}
