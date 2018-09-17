package android.view.animation;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.RectF;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.WindowManager.LayoutParams;
import com.android.internal.R;
import dalvik.system.CloseGuard;

public abstract class Animation implements Cloneable {
    public static final int ABSOLUTE = 0;
    public static final int INFINITE = -1;
    public static final int RELATIVE_TO_PARENT = 2;
    public static final int RELATIVE_TO_SELF = 1;
    public static final int RESTART = 1;
    public static final int REVERSE = 2;
    public static final int START_ON_FIRST_FRAME = -1;
    public static final int ZORDER_BOTTOM = -1;
    public static final int ZORDER_NORMAL = 0;
    public static final int ZORDER_TOP = 1;
    private final CloseGuard guard;
    private int mBackgroundColor;
    boolean mCycleFlip;
    private boolean mDetachWallpaper;
    long mDuration;
    boolean mEnded;
    boolean mFillAfter;
    boolean mFillBefore;
    boolean mFillEnabled;
    boolean mInitialized;
    Interpolator mInterpolator;
    AnimationListener mListener;
    private Handler mListenerHandler;
    private boolean mMore;
    private Runnable mOnEnd;
    private Runnable mOnRepeat;
    private Runnable mOnStart;
    private boolean mOneMoreTime;
    RectF mPreviousRegion;
    Transformation mPreviousTransformation;
    RectF mRegion;
    int mRepeatCount;
    int mRepeatMode;
    int mRepeated;
    private float mScaleFactor;
    long mStartOffset;
    long mStartTime;
    boolean mStarted;
    Transformation mTransformation;
    private int mZAdjustment;

    public interface AnimationListener {
        void onAnimationEnd(Animation animation);

        void onAnimationRepeat(Animation animation);

        void onAnimationStart(Animation animation);
    }

    protected static class Description {
        public int type;
        public float value;

        protected Description() {
        }

        static Description parseValue(TypedValue value) {
            int i = Animation.ZORDER_TOP;
            Description d = new Description();
            if (value == null) {
                d.type = Animation.ZORDER_NORMAL;
                d.value = 0.0f;
            } else if (value.type == 6) {
                if ((value.data & 15) == Animation.ZORDER_TOP) {
                    i = Animation.REVERSE;
                }
                d.type = i;
                d.value = TypedValue.complexToFloat(value.data);
                return d;
            } else if (value.type == 4) {
                d.type = Animation.ZORDER_NORMAL;
                d.value = value.getFloat();
                return d;
            } else if (value.type >= 16 && value.type <= 31) {
                d.type = Animation.ZORDER_NORMAL;
                d.value = (float) value.data;
                return d;
            }
            d.type = Animation.ZORDER_NORMAL;
            d.value = 0.0f;
            return d;
        }
    }

    private static class NoImagePreloadHolder {
        public static final boolean USE_CLOSEGUARD = false;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.view.animation.Animation.NoImagePreloadHolder.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.view.animation.Animation.NoImagePreloadHolder.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.view.animation.Animation.NoImagePreloadHolder.<clinit>():void");
        }

        private NoImagePreloadHolder() {
        }
    }

    public Animation() {
        this.mEnded = false;
        this.mStarted = false;
        this.mCycleFlip = false;
        this.mInitialized = false;
        this.mFillBefore = true;
        this.mFillAfter = false;
        this.mFillEnabled = false;
        this.mStartTime = -1;
        this.mRepeatCount = ZORDER_NORMAL;
        this.mRepeated = ZORDER_NORMAL;
        this.mRepeatMode = ZORDER_TOP;
        this.mScaleFactor = LayoutParams.BRIGHTNESS_OVERRIDE_FULL;
        this.mDetachWallpaper = false;
        this.mMore = true;
        this.mOneMoreTime = true;
        this.mPreviousRegion = new RectF();
        this.mRegion = new RectF();
        this.mTransformation = new Transformation();
        this.mPreviousTransformation = new Transformation();
        this.guard = CloseGuard.get();
        ensureInterpolator();
    }

    public Animation(Context context, AttributeSet attrs) {
        this.mEnded = false;
        this.mStarted = false;
        this.mCycleFlip = false;
        this.mInitialized = false;
        this.mFillBefore = true;
        this.mFillAfter = false;
        this.mFillEnabled = false;
        this.mStartTime = -1;
        this.mRepeatCount = ZORDER_NORMAL;
        this.mRepeated = ZORDER_NORMAL;
        this.mRepeatMode = ZORDER_TOP;
        this.mScaleFactor = LayoutParams.BRIGHTNESS_OVERRIDE_FULL;
        this.mDetachWallpaper = false;
        this.mMore = true;
        this.mOneMoreTime = true;
        this.mPreviousRegion = new RectF();
        this.mRegion = new RectF();
        this.mTransformation = new Transformation();
        this.mPreviousTransformation = new Transformation();
        this.guard = CloseGuard.get();
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Animation);
        setDuration((long) a.getInt(REVERSE, ZORDER_NORMAL));
        setStartOffset((long) a.getInt(5, ZORDER_NORMAL));
        setFillEnabled(a.getBoolean(9, this.mFillEnabled));
        setFillBefore(a.getBoolean(3, this.mFillBefore));
        setFillAfter(a.getBoolean(4, this.mFillAfter));
        setRepeatCount(a.getInt(6, this.mRepeatCount));
        setRepeatMode(a.getInt(7, ZORDER_TOP));
        setZAdjustment(a.getInt(8, ZORDER_NORMAL));
        setBackgroundColor(a.getInt(ZORDER_NORMAL, ZORDER_NORMAL));
        setDetachWallpaper(a.getBoolean(10, false));
        int resID = a.getResourceId(ZORDER_TOP, ZORDER_NORMAL);
        a.recycle();
        if (resID > 0) {
            setInterpolator(context, resID);
        }
        ensureInterpolator();
    }

    protected /* bridge */ /* synthetic */ Object m13clone() throws CloneNotSupportedException {
        return clone();
    }

    protected Animation clone() throws CloneNotSupportedException {
        Animation animation = (Animation) super.clone();
        animation.mPreviousRegion = new RectF();
        animation.mRegion = new RectF();
        animation.mTransformation = new Transformation();
        animation.mPreviousTransformation = new Transformation();
        return animation;
    }

    public void reset() {
        this.mPreviousRegion.setEmpty();
        this.mPreviousTransformation.clear();
        this.mInitialized = false;
        this.mCycleFlip = false;
        this.mRepeated = ZORDER_NORMAL;
        this.mMore = true;
        this.mOneMoreTime = true;
        this.mListenerHandler = null;
    }

    public void cancel() {
        if (this.mStarted && !this.mEnded) {
            fireAnimationEnd();
            this.mEnded = true;
            this.guard.close();
        }
        this.mStartTime = Long.MIN_VALUE;
        this.mOneMoreTime = false;
        this.mMore = false;
    }

    public void detach() {
        if (this.mStarted && !this.mEnded) {
            this.mEnded = true;
            this.guard.close();
            fireAnimationEnd();
        }
    }

    public boolean isInitialized() {
        return this.mInitialized;
    }

    public void initialize(int width, int height, int parentWidth, int parentHeight) {
        reset();
        this.mInitialized = true;
    }

    public void setListenerHandler(Handler handler) {
        if (this.mListenerHandler == null) {
            this.mOnStart = new Runnable() {
                public void run() {
                    if (Animation.this.mListener != null) {
                        Animation.this.mListener.onAnimationStart(Animation.this);
                    }
                }
            };
            this.mOnRepeat = new Runnable() {
                public void run() {
                    if (Animation.this.mListener != null) {
                        Animation.this.mListener.onAnimationRepeat(Animation.this);
                    }
                }
            };
            this.mOnEnd = new Runnable() {
                public void run() {
                    if (Animation.this.mListener != null) {
                        Animation.this.mListener.onAnimationEnd(Animation.this);
                    }
                }
            };
        }
        this.mListenerHandler = handler;
    }

    public void setInterpolator(Context context, int resID) {
        setInterpolator(AnimationUtils.loadInterpolator(context, resID));
    }

    public void setInterpolator(Interpolator i) {
        this.mInterpolator = i;
    }

    public void setStartOffset(long startOffset) {
        this.mStartOffset = startOffset;
    }

    public void setDuration(long durationMillis) {
        if (durationMillis < 0) {
            throw new IllegalArgumentException("Animation duration cannot be negative");
        }
        this.mDuration = durationMillis;
    }

    public void restrictDuration(long durationMillis) {
        if (this.mStartOffset > durationMillis) {
            this.mStartOffset = durationMillis;
            this.mDuration = 0;
            this.mRepeatCount = ZORDER_NORMAL;
            return;
        }
        long dur = this.mDuration + this.mStartOffset;
        if (dur > durationMillis) {
            this.mDuration = durationMillis - this.mStartOffset;
            dur = durationMillis;
        }
        if (this.mDuration <= 0) {
            this.mDuration = 0;
            this.mRepeatCount = ZORDER_NORMAL;
            return;
        }
        if (this.mRepeatCount >= 0 && ((long) this.mRepeatCount) <= durationMillis) {
            if (((long) this.mRepeatCount) * dur > durationMillis) {
            }
        }
        this.mRepeatCount = ((int) (durationMillis / dur)) + ZORDER_BOTTOM;
        if (this.mRepeatCount < 0) {
            this.mRepeatCount = ZORDER_NORMAL;
        }
    }

    public void scaleCurrentDuration(float scale) {
        this.mDuration = (long) (((float) this.mDuration) * scale);
        this.mStartOffset = (long) (((float) this.mStartOffset) * scale);
    }

    public void setStartTime(long startTimeMillis) {
        this.mStartTime = startTimeMillis;
        this.mEnded = false;
        this.mStarted = false;
        this.mCycleFlip = false;
        this.mRepeated = ZORDER_NORMAL;
        this.mMore = true;
    }

    public void start() {
        setStartTime(-1);
    }

    public void startNow() {
        setStartTime(AnimationUtils.currentAnimationTimeMillis());
    }

    public void setRepeatMode(int repeatMode) {
        this.mRepeatMode = repeatMode;
    }

    public void setRepeatCount(int repeatCount) {
        if (repeatCount < 0) {
            repeatCount = ZORDER_BOTTOM;
        }
        this.mRepeatCount = repeatCount;
    }

    public boolean isFillEnabled() {
        return this.mFillEnabled;
    }

    public void setFillEnabled(boolean fillEnabled) {
        this.mFillEnabled = fillEnabled;
    }

    public void setFillBefore(boolean fillBefore) {
        this.mFillBefore = fillBefore;
    }

    public void setFillAfter(boolean fillAfter) {
        this.mFillAfter = fillAfter;
    }

    public void setZAdjustment(int zAdjustment) {
        this.mZAdjustment = zAdjustment;
    }

    public void setBackgroundColor(int bg) {
        this.mBackgroundColor = bg;
    }

    protected float getScaleFactor() {
        return this.mScaleFactor;
    }

    public void setDetachWallpaper(boolean detachWallpaper) {
        this.mDetachWallpaper = detachWallpaper;
    }

    public Interpolator getInterpolator() {
        return this.mInterpolator;
    }

    public long getStartTime() {
        return this.mStartTime;
    }

    public long getDuration() {
        return this.mDuration;
    }

    public long getStartOffset() {
        return this.mStartOffset;
    }

    public int getRepeatMode() {
        return this.mRepeatMode;
    }

    public int getRepeatCount() {
        return this.mRepeatCount;
    }

    public boolean getFillBefore() {
        return this.mFillBefore;
    }

    public boolean getFillAfter() {
        return this.mFillAfter;
    }

    public int getZAdjustment() {
        return this.mZAdjustment;
    }

    public int getBackgroundColor() {
        return this.mBackgroundColor;
    }

    public boolean getDetachWallpaper() {
        return this.mDetachWallpaper;
    }

    public boolean willChangeTransformationMatrix() {
        return true;
    }

    public boolean willChangeBounds() {
        return true;
    }

    public void setAnimationListener(AnimationListener listener) {
        this.mListener = listener;
    }

    protected void ensureInterpolator() {
        if (this.mInterpolator == null) {
            this.mInterpolator = new AccelerateDecelerateInterpolator();
        }
    }

    public long computeDurationHint() {
        return (getStartOffset() + getDuration()) * ((long) (getRepeatCount() + ZORDER_TOP));
    }

    public boolean getTransformation(long currentTime, Transformation outTransformation) {
        if (this.mStartTime == -1) {
            this.mStartTime = currentTime;
        }
        long startOffset = getStartOffset();
        long duration = this.mDuration;
        float normalizedTime = duration != 0 ? ((float) (currentTime - (this.mStartTime + startOffset))) / ((float) duration) : currentTime < this.mStartTime ? 0.0f : LayoutParams.BRIGHTNESS_OVERRIDE_FULL;
        boolean expired = normalizedTime < LayoutParams.BRIGHTNESS_OVERRIDE_FULL ? isCanceled() : true;
        this.mMore = !expired;
        if (!this.mFillEnabled) {
            normalizedTime = Math.max(Math.min(normalizedTime, LayoutParams.BRIGHTNESS_OVERRIDE_FULL), 0.0f);
        }
        if ((normalizedTime >= 0.0f || this.mFillBefore) && (normalizedTime <= LayoutParams.BRIGHTNESS_OVERRIDE_FULL || this.mFillAfter)) {
            if (!this.mStarted) {
                fireAnimationStart();
                this.mStarted = true;
                if (NoImagePreloadHolder.USE_CLOSEGUARD) {
                    this.guard.open("cancel or detach or getTransformation");
                }
            }
            if (this.mFillEnabled) {
                normalizedTime = Math.max(Math.min(normalizedTime, LayoutParams.BRIGHTNESS_OVERRIDE_FULL), 0.0f);
            }
            if (this.mCycleFlip) {
                normalizedTime = LayoutParams.BRIGHTNESS_OVERRIDE_FULL - normalizedTime;
            }
            applyTransformation(this.mInterpolator.getInterpolation(normalizedTime), outTransformation);
        }
        if (expired) {
            if (this.mRepeatCount != this.mRepeated && !isCanceled()) {
                if (this.mRepeatCount > 0) {
                    this.mRepeated += ZORDER_TOP;
                }
                if (this.mRepeatMode == REVERSE) {
                    this.mCycleFlip = !this.mCycleFlip;
                }
                this.mStartTime = -1;
                this.mMore = true;
                fireAnimationRepeat();
            } else if (!this.mEnded) {
                this.mEnded = true;
                this.guard.close();
                fireAnimationEnd();
            }
        }
        if (this.mMore || !this.mOneMoreTime) {
            return this.mMore;
        }
        this.mOneMoreTime = false;
        return true;
    }

    private boolean isCanceled() {
        return this.mStartTime == Long.MIN_VALUE;
    }

    private void fireAnimationStart() {
        if (this.mListener == null) {
            return;
        }
        if (this.mListenerHandler == null) {
            this.mListener.onAnimationStart(this);
        } else {
            this.mListenerHandler.postAtFrontOfQueue(this.mOnStart);
        }
    }

    private void fireAnimationRepeat() {
        if (this.mListener == null) {
            return;
        }
        if (this.mListenerHandler == null) {
            this.mListener.onAnimationRepeat(this);
        } else {
            this.mListenerHandler.postAtFrontOfQueue(this.mOnRepeat);
        }
    }

    private void fireAnimationEnd() {
        if (this.mListener == null) {
            return;
        }
        if (this.mListenerHandler == null) {
            this.mListener.onAnimationEnd(this);
        } else {
            this.mListenerHandler.postAtFrontOfQueue(this.mOnEnd);
        }
    }

    public boolean getTransformation(long currentTime, Transformation outTransformation, float scale) {
        this.mScaleFactor = scale;
        return getTransformation(currentTime, outTransformation);
    }

    public boolean hasStarted() {
        return this.mStarted;
    }

    public boolean hasEnded() {
        return this.mEnded;
    }

    protected void applyTransformation(float interpolatedTime, Transformation t) {
    }

    protected float resolveSize(int type, float value, int size, int parentSize) {
        switch (type) {
            case ZORDER_NORMAL /*0*/:
                return value;
            case ZORDER_TOP /*1*/:
                return ((float) size) * value;
            case REVERSE /*2*/:
                return ((float) parentSize) * value;
            default:
                return value;
        }
    }

    public void getInvalidateRegion(int left, int top, int right, int bottom, RectF invalidate, Transformation transformation) {
        RectF tempRegion = this.mRegion;
        RectF previousRegion = this.mPreviousRegion;
        invalidate.set((float) left, (float) top, (float) right, (float) bottom);
        transformation.getMatrix().mapRect(invalidate);
        invalidate.inset(LayoutParams.BRIGHTNESS_OVERRIDE_NONE, LayoutParams.BRIGHTNESS_OVERRIDE_NONE);
        tempRegion.set(invalidate);
        invalidate.union(previousRegion);
        previousRegion.set(tempRegion);
        Transformation tempTransformation = this.mTransformation;
        Transformation previousTransformation = this.mPreviousTransformation;
        tempTransformation.set(transformation);
        transformation.set(previousTransformation);
        previousTransformation.set(tempTransformation);
    }

    public void initializeInvalidateRegion(int left, int top, int right, int bottom) {
        RectF region = this.mPreviousRegion;
        region.set((float) left, (float) top, (float) right, (float) bottom);
        region.inset(LayoutParams.BRIGHTNESS_OVERRIDE_NONE, LayoutParams.BRIGHTNESS_OVERRIDE_NONE);
        if (this.mFillBefore) {
            applyTransformation(this.mInterpolator.getInterpolation(0.0f), this.mPreviousTransformation);
        }
    }

    protected void finalize() throws Throwable {
        try {
            if (this.guard != null) {
                this.guard.warnIfOpen();
            }
            super.finalize();
        } catch (Throwable th) {
            super.finalize();
        }
    }

    public boolean hasAlpha() {
        return false;
    }
}
