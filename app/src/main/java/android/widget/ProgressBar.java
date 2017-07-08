package android.widget;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.graphics.drawable.shapes.Shape;
import android.hwcontrol.HwWidgetFactory;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.AttributeSet;
import android.util.FloatProperty;
import android.util.MathUtils;
import android.util.Pools.SynchronizedPool;
import android.view.RemotableViewMethod;
import android.view.View;
import android.view.View.BaseSavedState;
import android.view.ViewDebug.ExportedProperty;
import android.view.ViewHierarchyEncoder;
import android.view.WindowManager.LayoutParams;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityNodeInfo.RangeInfo;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.Transformation;
import android.widget.RemoteViews.RemoteView;
import com.android.internal.R;
import java.util.ArrayList;
import java.util.Locale;

@RemoteView
public class ProgressBar extends View {
    private static final int MAX_LEVEL = 10000;
    private static final int PROGRESS_ANIM_DURATION = 80;
    private static final DecelerateInterpolator PROGRESS_ANIM_INTERPOLATOR = null;
    private static final int TIMEOUT_SEND_ACCESSIBILITY_EVENT = 200;
    private final FloatProperty<ProgressBar> VISUAL_PROGRESS;
    private AccessibilityEventSender mAccessibilityEventSender;
    private boolean mAggregatedIsVisible;
    private AlphaAnimation mAnimation;
    private boolean mAttached;
    private int mBehavior;
    private Drawable mCurrentDrawable;
    private int mDuration;
    private boolean mForceRefresh;
    private boolean mHasAnimation;
    private boolean mInDrawing;
    private boolean mIndeterminate;
    private Drawable mIndeterminateDrawable;
    private Interpolator mInterpolator;
    protected int mMax;
    int mMaxHeight;
    int mMaxWidth;
    int mMinHeight;
    int mMinWidth;
    boolean mMirrorForRtl;
    private boolean mNoInvalidate;
    private boolean mOnlyIndeterminate;
    private int mProgress;
    private Drawable mProgressDrawable;
    private ProgressTintInfo mProgressTintInfo;
    private final ArrayList<RefreshData> mRefreshData;
    private boolean mRefreshIsPosted;
    private RefreshProgressRunnable mRefreshProgressRunnable;
    Bitmap mSampleTile;
    private int mSecondaryProgress;
    private boolean mShouldStartAnimationDrawable;
    private Transformation mTransformation;
    private long mUiThreadId;
    private float mVisualProgress;

    /* renamed from: android.widget.ProgressBar.1 */
    class AnonymousClass1 extends FloatProperty<ProgressBar> {
        AnonymousClass1(String $anonymous0) {
            super($anonymous0);
        }

        public void setValue(ProgressBar object, float value) {
            object.setVisualProgress(R.id.progress, value);
            object.mVisualProgress = value;
        }

        public Float get(ProgressBar object) {
            return Float.valueOf(object.mVisualProgress);
        }
    }

    private class AccessibilityEventSender implements Runnable {
        private AccessibilityEventSender() {
        }

        public void run() {
            ProgressBar.this.sendAccessibilityEvent(4);
        }
    }

    private static class ProgressTintInfo {
        boolean mHasIndeterminateTint;
        boolean mHasIndeterminateTintMode;
        boolean mHasProgressBackgroundTint;
        boolean mHasProgressBackgroundTintMode;
        boolean mHasProgressTint;
        boolean mHasProgressTintMode;
        boolean mHasSecondaryProgressTint;
        boolean mHasSecondaryProgressTintMode;
        ColorStateList mIndeterminateTintList;
        Mode mIndeterminateTintMode;
        ColorStateList mProgressBackgroundTintList;
        Mode mProgressBackgroundTintMode;
        ColorStateList mProgressTintList;
        Mode mProgressTintMode;
        ColorStateList mSecondaryProgressTintList;
        Mode mSecondaryProgressTintMode;

        private ProgressTintInfo() {
        }
    }

    private static class RefreshData {
        private static final int POOL_MAX = 24;
        private static final SynchronizedPool<RefreshData> sPool = null;
        public boolean animate;
        public boolean fromUser;
        public int id;
        public int progress;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.widget.ProgressBar.RefreshData.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.widget.ProgressBar.RefreshData.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.ProgressBar.RefreshData.<clinit>():void");
        }

        private RefreshData() {
        }

        public static RefreshData obtain(int id, int progress, boolean fromUser, boolean animate) {
            RefreshData rd = (RefreshData) sPool.acquire();
            if (rd == null) {
                rd = new RefreshData();
            }
            rd.id = id;
            rd.progress = progress;
            rd.fromUser = fromUser;
            rd.animate = animate;
            return rd;
        }

        public void recycle() {
            sPool.release(this);
        }
    }

    private class RefreshProgressRunnable implements Runnable {
        final /* synthetic */ ProgressBar this$0;

        /* synthetic */ RefreshProgressRunnable(ProgressBar this$0, RefreshProgressRunnable refreshProgressRunnable) {
            this(this$0);
        }

        private RefreshProgressRunnable(ProgressBar this$0) {
            this.this$0 = this$0;
        }

        public void run() {
            synchronized (this.this$0) {
                int count = this.this$0.mRefreshData.size();
                for (int i = 0; i < count; i++) {
                    RefreshData rd = (RefreshData) this.this$0.mRefreshData.get(i);
                    this.this$0.doRefreshProgress(rd.id, rd.progress, rd.fromUser, true, rd.animate);
                    rd.recycle();
                }
                this.this$0.mRefreshData.clear();
                this.this$0.mRefreshIsPosted = false;
            }
        }
    }

    static class SavedState extends BaseSavedState {
        public static final Creator<SavedState> CREATOR = null;
        int progress;
        int secondaryProgress;

        /* renamed from: android.widget.ProgressBar.SavedState.1 */
        static class AnonymousClass1 implements Creator<SavedState> {
            AnonymousClass1() {
            }

            public /* bridge */ /* synthetic */ Object m15createFromParcel(Parcel in) {
                return createFromParcel(in);
            }

            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in, null);
            }

            public /* bridge */ /* synthetic */ Object[] m16newArray(int size) {
                return newArray(size);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        }

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.widget.ProgressBar.SavedState.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.widget.ProgressBar.SavedState.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.ProgressBar.SavedState.<clinit>():void");
        }

        /* synthetic */ SavedState(Parcel in, SavedState savedState) {
            this(in);
        }

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            this.progress = in.readInt();
            this.secondaryProgress = in.readInt();
        }

        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(this.progress);
            out.writeInt(this.secondaryProgress);
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.widget.ProgressBar.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.widget.ProgressBar.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.widget.ProgressBar.<clinit>():void");
    }

    public ProgressBar(Context context) {
        this(context, null);
    }

    public ProgressBar(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.progressBarStyle);
    }

    public ProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ProgressBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        Drawable indeterminateDrawable;
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mMirrorForRtl = false;
        this.mRefreshData = new ArrayList();
        this.VISUAL_PROGRESS = new AnonymousClass1("visual_progress");
        this.mUiThreadId = Thread.currentThread().getId();
        initProgressBar();
        TypedArray ahwext = context.obtainStyledAttributes(attrs, androidhwext.R.styleable.ProgressBar, defStyleAttr, defStyleRes);
        boolean useLoading = ahwext.getBoolean(0, false);
        ahwext.recycle();
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ProgressBar, defStyleAttr, defStyleRes);
        this.mNoInvalidate = true;
        Drawable progressDrawable = a.getDrawable(8);
        if (progressDrawable != null) {
            if (needsTileify(progressDrawable)) {
                setProgressDrawableTiled(progressDrawable);
            } else {
                setProgressDrawable(progressDrawable);
            }
        }
        this.mDuration = a.getInt(9, this.mDuration);
        this.mMinWidth = a.getDimensionPixelSize(11, this.mMinWidth);
        this.mMaxWidth = a.getDimensionPixelSize(0, this.mMaxWidth);
        this.mMinHeight = a.getDimensionPixelSize(12, this.mMinHeight);
        this.mMaxHeight = a.getDimensionPixelSize(1, this.mMaxHeight);
        this.mBehavior = a.getInt(10, this.mBehavior);
        int resID = a.getResourceId(13, R.anim.linear_interpolator);
        if (resID > 0) {
            setInterpolator(context, resID);
        }
        setMax(a.getInt(2, this.mMax));
        int progress = a.getInt(3, this.mProgress);
        if (progress == 0) {
            this.mForceRefresh = true;
        }
        setProgress(progress);
        this.mForceRefresh = false;
        setSecondaryProgress(a.getInt(4, this.mSecondaryProgress));
        if (useLoading) {
            indeterminateDrawable = HwWidgetFactory.getHwLoadingDrawable(getResources(), Math.min(this.mMinWidth, this.mMaxHeight));
        } else {
            indeterminateDrawable = a.getDrawable(7);
        }
        if (indeterminateDrawable != null) {
            if (needsTileify(indeterminateDrawable)) {
                setIndeterminateDrawableTiled(indeterminateDrawable);
            } else {
                setIndeterminateDrawable(indeterminateDrawable);
            }
        }
        this.mOnlyIndeterminate = a.getBoolean(6, this.mOnlyIndeterminate);
        this.mNoInvalidate = false;
        setIndeterminate(!this.mOnlyIndeterminate ? a.getBoolean(5, this.mIndeterminate) : true);
        this.mMirrorForRtl = a.getBoolean(15, this.mMirrorForRtl);
        if (a.hasValue(17)) {
            if (this.mProgressTintInfo == null) {
                this.mProgressTintInfo = new ProgressTintInfo();
            }
            this.mProgressTintInfo.mProgressTintMode = Drawable.parseTintMode(a.getInt(17, -1), null);
            this.mProgressTintInfo.mHasProgressTintMode = true;
        }
        if (a.hasValue(16)) {
            if (this.mProgressTintInfo == null) {
                this.mProgressTintInfo = new ProgressTintInfo();
            }
            this.mProgressTintInfo.mProgressTintList = a.getColorStateList(16);
            this.mProgressTintInfo.mHasProgressTint = true;
        }
        if (a.hasValue(19)) {
            if (this.mProgressTintInfo == null) {
                this.mProgressTintInfo = new ProgressTintInfo();
            }
            this.mProgressTintInfo.mProgressBackgroundTintMode = Drawable.parseTintMode(a.getInt(19, -1), null);
            this.mProgressTintInfo.mHasProgressBackgroundTintMode = true;
        }
        if (a.hasValue(18)) {
            if (this.mProgressTintInfo == null) {
                this.mProgressTintInfo = new ProgressTintInfo();
            }
            this.mProgressTintInfo.mProgressBackgroundTintList = a.getColorStateList(18);
            this.mProgressTintInfo.mHasProgressBackgroundTint = true;
        }
        if (a.hasValue(21)) {
            if (this.mProgressTintInfo == null) {
                this.mProgressTintInfo = new ProgressTintInfo();
            }
            this.mProgressTintInfo.mSecondaryProgressTintMode = Drawable.parseTintMode(a.getInt(21, -1), null);
            this.mProgressTintInfo.mHasSecondaryProgressTintMode = true;
        }
        if (a.hasValue(20)) {
            if (this.mProgressTintInfo == null) {
                this.mProgressTintInfo = new ProgressTintInfo();
            }
            this.mProgressTintInfo.mSecondaryProgressTintList = a.getColorStateList(20);
            this.mProgressTintInfo.mHasSecondaryProgressTint = true;
        }
        if (a.hasValue(23)) {
            if (this.mProgressTintInfo == null) {
                this.mProgressTintInfo = new ProgressTintInfo();
            }
            this.mProgressTintInfo.mIndeterminateTintMode = Drawable.parseTintMode(a.getInt(23, -1), null);
            this.mProgressTintInfo.mHasIndeterminateTintMode = true;
        }
        if (a.hasValue(22)) {
            if (this.mProgressTintInfo == null) {
                this.mProgressTintInfo = new ProgressTintInfo();
            }
            this.mProgressTintInfo.mIndeterminateTintList = a.getColorStateList(22);
            this.mProgressTintInfo.mHasIndeterminateTint = true;
        }
        a.recycle();
        applyProgressTints();
        applyIndeterminateTint();
        if (getImportantForAccessibility() == 0) {
            setImportantForAccessibility(1);
        }
    }

    private static boolean needsTileify(Drawable dr) {
        int N;
        int i;
        if (dr instanceof LayerDrawable) {
            LayerDrawable orig = (LayerDrawable) dr;
            N = orig.getNumberOfLayers();
            for (i = 0; i < N; i++) {
                if (needsTileify(orig.getDrawable(i))) {
                    return true;
                }
            }
            return false;
        } else if (!(dr instanceof StateListDrawable)) {
            return dr instanceof BitmapDrawable;
        } else {
            StateListDrawable in = (StateListDrawable) dr;
            N = in.getStateCount();
            for (i = 0; i < N; i++) {
                if (needsTileify(in.getStateDrawable(i))) {
                    return true;
                }
            }
            return false;
        }
    }

    private Drawable tileify(Drawable drawable, boolean clip) {
        int N;
        int i;
        if (drawable instanceof LayerDrawable) {
            LayerDrawable orig = (LayerDrawable) drawable;
            N = orig.getNumberOfLayers();
            Drawable[] outDrawables = new Drawable[N];
            for (i = 0; i < N; i++) {
                int id = orig.getId(i);
                Drawable drawable2 = orig.getDrawable(i);
                boolean z = id == R.id.progress || id == R.id.secondaryProgress;
                outDrawables[i] = tileify(drawable2, z);
            }
            LayerDrawable clone = new LayerDrawable(outDrawables);
            for (i = 0; i < N; i++) {
                clone.setId(i, orig.getId(i));
                clone.setLayerGravity(i, orig.getLayerGravity(i));
                clone.setLayerWidth(i, orig.getLayerWidth(i));
                clone.setLayerHeight(i, orig.getLayerHeight(i));
                clone.setLayerInsetLeft(i, orig.getLayerInsetLeft(i));
                clone.setLayerInsetRight(i, orig.getLayerInsetRight(i));
                clone.setLayerInsetTop(i, orig.getLayerInsetTop(i));
                clone.setLayerInsetBottom(i, orig.getLayerInsetBottom(i));
                clone.setLayerInsetStart(i, orig.getLayerInsetStart(i));
                clone.setLayerInsetEnd(i, orig.getLayerInsetEnd(i));
            }
            return clone;
        } else if (drawable instanceof StateListDrawable) {
            StateListDrawable in = (StateListDrawable) drawable;
            StateListDrawable out = new StateListDrawable();
            N = in.getStateCount();
            for (i = 0; i < N; i++) {
                out.addState(in.getStateSet(i), tileify(in.getStateDrawable(i), clip));
            }
            return out;
        } else if (!(drawable instanceof BitmapDrawable)) {
            return drawable;
        } else {
            BitmapDrawable bitmap = (BitmapDrawable) drawable;
            Bitmap tileBitmap = bitmap.getBitmap();
            if (this.mSampleTile == null) {
                this.mSampleTile = tileBitmap;
            }
            BitmapDrawable clone2 = (BitmapDrawable) bitmap.getConstantState().newDrawable();
            clone2.setTileModeXY(TileMode.REPEAT, TileMode.CLAMP);
            if (clip) {
                return new ClipDrawable(clone2, 3, 1);
            }
            return clone2;
        }
    }

    Shape getDrawableShape() {
        return new RoundRectShape(new float[]{5.0f, 5.0f, 5.0f, 5.0f, 5.0f, 5.0f, 5.0f, 5.0f}, null, null);
    }

    private Drawable tileifyIndeterminate(Drawable drawable) {
        if (!(drawable instanceof AnimationDrawable)) {
            return drawable;
        }
        AnimationDrawable background = (AnimationDrawable) drawable;
        int N = background.getNumberOfFrames();
        Drawable newBg = new AnimationDrawable();
        newBg.setOneShot(background.isOneShot());
        for (int i = 0; i < N; i++) {
            Drawable frame = tileify(background.getFrame(i), true);
            frame.setLevel(MAX_LEVEL);
            newBg.addFrame(frame, background.getDuration(i));
        }
        newBg.setLevel(MAX_LEVEL);
        return newBg;
    }

    private void initProgressBar() {
        this.mMax = 100;
        this.mProgress = 0;
        this.mSecondaryProgress = 0;
        this.mIndeterminate = false;
        this.mOnlyIndeterminate = false;
        this.mDuration = 4000;
        this.mBehavior = 1;
        this.mMinWidth = 24;
        this.mMaxWidth = 48;
        this.mMinHeight = 24;
        this.mMaxHeight = 48;
    }

    @ExportedProperty(category = "progress")
    public synchronized boolean isIndeterminate() {
        return this.mIndeterminate;
    }

    @RemotableViewMethod
    public synchronized void setIndeterminate(boolean indeterminate) {
        if (!(this.mOnlyIndeterminate && this.mIndeterminate)) {
            if (indeterminate != this.mIndeterminate) {
                this.mIndeterminate = indeterminate;
                if (indeterminate) {
                    swapCurrentDrawable(this.mIndeterminateDrawable);
                    startAnimation();
                } else {
                    swapCurrentDrawable(this.mProgressDrawable);
                    stopAnimation();
                }
            }
        }
    }

    private void swapCurrentDrawable(Drawable newDrawable) {
        Drawable oldDrawable = this.mCurrentDrawable;
        this.mCurrentDrawable = newDrawable;
        if (oldDrawable != this.mCurrentDrawable) {
            if (oldDrawable != null) {
                oldDrawable.setVisible(false, false);
            }
            if (this.mCurrentDrawable != null) {
                this.mCurrentDrawable.setVisible(getWindowVisibility() == 0 ? isShown() : false, false);
            }
        }
    }

    public Drawable getIndeterminateDrawable() {
        return this.mIndeterminateDrawable;
    }

    public void setIndeterminateDrawable(Drawable d) {
        if (this.mIndeterminateDrawable != d) {
            if (this.mIndeterminateDrawable != null) {
                this.mIndeterminateDrawable.setCallback(null);
                unscheduleDrawable(this.mIndeterminateDrawable);
            }
            this.mIndeterminateDrawable = d;
            if (d != null) {
                d.setCallback(this);
                if ("ur".equals(Locale.getDefault().getLanguage())) {
                    d.setLayoutDirection(0);
                } else {
                    d.setLayoutDirection(getLayoutDirection());
                }
                if (d.isStateful()) {
                    d.setState(getDrawableState());
                }
                applyIndeterminateTint();
            }
            if (this.mIndeterminate) {
                swapCurrentDrawable(d);
                postInvalidate();
            }
        }
    }

    @RemotableViewMethod
    public void setIndeterminateTintList(ColorStateList tint) {
        if (this.mProgressTintInfo == null) {
            this.mProgressTintInfo = new ProgressTintInfo();
        }
        this.mProgressTintInfo.mIndeterminateTintList = tint;
        this.mProgressTintInfo.mHasIndeterminateTint = true;
        applyIndeterminateTint();
    }

    public ColorStateList getIndeterminateTintList() {
        return this.mProgressTintInfo != null ? this.mProgressTintInfo.mIndeterminateTintList : null;
    }

    public void setIndeterminateTintMode(Mode tintMode) {
        if (this.mProgressTintInfo == null) {
            this.mProgressTintInfo = new ProgressTintInfo();
        }
        this.mProgressTintInfo.mIndeterminateTintMode = tintMode;
        this.mProgressTintInfo.mHasIndeterminateTintMode = true;
        applyIndeterminateTint();
    }

    public Mode getIndeterminateTintMode() {
        return this.mProgressTintInfo != null ? this.mProgressTintInfo.mIndeterminateTintMode : null;
    }

    private void applyIndeterminateTint() {
        if (this.mIndeterminateDrawable != null && this.mProgressTintInfo != null) {
            ProgressTintInfo tintInfo = this.mProgressTintInfo;
            if (tintInfo.mHasIndeterminateTint || tintInfo.mHasIndeterminateTintMode) {
                this.mIndeterminateDrawable = this.mIndeterminateDrawable.mutate();
                if (tintInfo.mHasIndeterminateTint) {
                    this.mIndeterminateDrawable.setTintList(tintInfo.mIndeterminateTintList);
                }
                if (tintInfo.mHasIndeterminateTintMode) {
                    this.mIndeterminateDrawable.setTintMode(tintInfo.mIndeterminateTintMode);
                }
                if (this.mIndeterminateDrawable.isStateful()) {
                    this.mIndeterminateDrawable.setState(getDrawableState());
                }
            }
        }
    }

    public void setIndeterminateDrawableTiled(Drawable d) {
        if (d != null) {
            d = tileifyIndeterminate(d);
        }
        setIndeterminateDrawable(d);
    }

    public Drawable getProgressDrawable() {
        return this.mProgressDrawable;
    }

    public void setProgressDrawable(Drawable d) {
        if (this.mProgressDrawable != d) {
            if (this.mProgressDrawable != null) {
                this.mProgressDrawable.setCallback(null);
                unscheduleDrawable(this.mProgressDrawable);
            }
            this.mProgressDrawable = d;
            if (d != null) {
                d.setCallback(this);
                if ("ur".equals(Locale.getDefault().getLanguage())) {
                    d.setLayoutDirection(0);
                } else {
                    d.setLayoutDirection(getLayoutDirection());
                }
                if (d.isStateful()) {
                    d.setState(getDrawableState());
                }
                int drawableHeight = d.getMinimumHeight();
                if (this.mMaxHeight < drawableHeight) {
                    this.mMaxHeight = drawableHeight;
                    requestLayout();
                }
                applyProgressTints();
            }
            if (!this.mIndeterminate) {
                swapCurrentDrawable(d);
                postInvalidate();
            }
            updateDrawableBounds(getWidth(), getHeight());
            updateDrawableState();
            doRefreshProgress(R.id.progress, this.mProgress, false, false, false);
            doRefreshProgress(R.id.secondaryProgress, this.mSecondaryProgress, false, false, false);
        }
    }

    private void applyProgressTints() {
        if (this.mProgressDrawable != null && this.mProgressTintInfo != null) {
            applyPrimaryProgressTint();
            applyProgressBackgroundTint();
            applySecondaryProgressTint();
        }
    }

    private void applyPrimaryProgressTint() {
        if (this.mProgressTintInfo.mHasProgressTint || this.mProgressTintInfo.mHasProgressTintMode) {
            Drawable target = getTintTarget(R.id.progress, true);
            if (target != null) {
                if (this.mProgressTintInfo.mHasProgressTint) {
                    target.setTintList(this.mProgressTintInfo.mProgressTintList);
                }
                if (this.mProgressTintInfo.mHasProgressTintMode) {
                    target.setTintMode(this.mProgressTintInfo.mProgressTintMode);
                }
                if (target.isStateful()) {
                    target.setState(getDrawableState());
                }
            }
        }
    }

    private void applyProgressBackgroundTint() {
        if (this.mProgressTintInfo.mHasProgressBackgroundTint || this.mProgressTintInfo.mHasProgressBackgroundTintMode) {
            Drawable target = getTintTarget(R.id.background, false);
            if (target != null) {
                if (this.mProgressTintInfo.mHasProgressBackgroundTint) {
                    target.setTintList(this.mProgressTintInfo.mProgressBackgroundTintList);
                }
                if (this.mProgressTintInfo.mHasProgressBackgroundTintMode) {
                    target.setTintMode(this.mProgressTintInfo.mProgressBackgroundTintMode);
                }
                if (target.isStateful()) {
                    target.setState(getDrawableState());
                }
            }
        }
    }

    private void applySecondaryProgressTint() {
        if (this.mProgressTintInfo.mHasSecondaryProgressTint || this.mProgressTintInfo.mHasSecondaryProgressTintMode) {
            Drawable target = getTintTarget(R.id.secondaryProgress, false);
            if (target != null) {
                if (this.mProgressTintInfo.mHasSecondaryProgressTint) {
                    target.setTintList(this.mProgressTintInfo.mSecondaryProgressTintList);
                }
                if (this.mProgressTintInfo.mHasSecondaryProgressTintMode) {
                    target.setTintMode(this.mProgressTintInfo.mSecondaryProgressTintMode);
                }
                if (target.isStateful()) {
                    target.setState(getDrawableState());
                }
            }
        }
    }

    @RemotableViewMethod
    public void setProgressTintList(ColorStateList tint) {
        if (this.mProgressTintInfo == null) {
            this.mProgressTintInfo = new ProgressTintInfo();
        }
        this.mProgressTintInfo.mProgressTintList = tint;
        this.mProgressTintInfo.mHasProgressTint = true;
        if (this.mProgressDrawable != null) {
            applyPrimaryProgressTint();
        }
    }

    public ColorStateList getProgressTintList() {
        return this.mProgressTintInfo != null ? this.mProgressTintInfo.mProgressTintList : null;
    }

    public void setProgressTintMode(Mode tintMode) {
        if (this.mProgressTintInfo == null) {
            this.mProgressTintInfo = new ProgressTintInfo();
        }
        this.mProgressTintInfo.mProgressTintMode = tintMode;
        this.mProgressTintInfo.mHasProgressTintMode = true;
        if (this.mProgressDrawable != null) {
            applyPrimaryProgressTint();
        }
    }

    public Mode getProgressTintMode() {
        return this.mProgressTintInfo != null ? this.mProgressTintInfo.mProgressTintMode : null;
    }

    @RemotableViewMethod
    public void setProgressBackgroundTintList(ColorStateList tint) {
        if (this.mProgressTintInfo == null) {
            this.mProgressTintInfo = new ProgressTintInfo();
        }
        this.mProgressTintInfo.mProgressBackgroundTintList = tint;
        this.mProgressTintInfo.mHasProgressBackgroundTint = true;
        if (this.mProgressDrawable != null) {
            applyProgressBackgroundTint();
        }
    }

    public ColorStateList getProgressBackgroundTintList() {
        return this.mProgressTintInfo != null ? this.mProgressTintInfo.mProgressBackgroundTintList : null;
    }

    public void setProgressBackgroundTintMode(Mode tintMode) {
        if (this.mProgressTintInfo == null) {
            this.mProgressTintInfo = new ProgressTintInfo();
        }
        this.mProgressTintInfo.mProgressBackgroundTintMode = tintMode;
        this.mProgressTintInfo.mHasProgressBackgroundTintMode = true;
        if (this.mProgressDrawable != null) {
            applyProgressBackgroundTint();
        }
    }

    public Mode getProgressBackgroundTintMode() {
        return this.mProgressTintInfo != null ? this.mProgressTintInfo.mProgressBackgroundTintMode : null;
    }

    public void setSecondaryProgressTintList(ColorStateList tint) {
        if (this.mProgressTintInfo == null) {
            this.mProgressTintInfo = new ProgressTintInfo();
        }
        this.mProgressTintInfo.mSecondaryProgressTintList = tint;
        this.mProgressTintInfo.mHasSecondaryProgressTint = true;
        if (this.mProgressDrawable != null) {
            applySecondaryProgressTint();
        }
    }

    public ColorStateList getSecondaryProgressTintList() {
        return this.mProgressTintInfo != null ? this.mProgressTintInfo.mSecondaryProgressTintList : null;
    }

    public void setSecondaryProgressTintMode(Mode tintMode) {
        if (this.mProgressTintInfo == null) {
            this.mProgressTintInfo = new ProgressTintInfo();
        }
        this.mProgressTintInfo.mSecondaryProgressTintMode = tintMode;
        this.mProgressTintInfo.mHasSecondaryProgressTintMode = true;
        if (this.mProgressDrawable != null) {
            applySecondaryProgressTint();
        }
    }

    public Mode getSecondaryProgressTintMode() {
        return this.mProgressTintInfo != null ? this.mProgressTintInfo.mSecondaryProgressTintMode : null;
    }

    private Drawable getTintTarget(int layerId, boolean shouldFallback) {
        Drawable drawable = null;
        Drawable d = this.mProgressDrawable;
        if (d == null) {
            return null;
        }
        this.mProgressDrawable = d.mutate();
        if (d instanceof LayerDrawable) {
            drawable = ((LayerDrawable) d).findDrawableByLayerId(layerId);
        }
        if (shouldFallback && drawable == null) {
            return d;
        }
        return drawable;
    }

    public void setProgressDrawableTiled(Drawable d) {
        if (d != null) {
            d = tileify(d, false);
        }
        setProgressDrawable(d);
    }

    protected Drawable getCurrentDrawable() {
        return this.mCurrentDrawable;
    }

    protected boolean verifyDrawable(Drawable who) {
        if (who == this.mProgressDrawable || who == this.mIndeterminateDrawable) {
            return true;
        }
        return super.verifyDrawable(who);
    }

    public void jumpDrawablesToCurrentState() {
        super.jumpDrawablesToCurrentState();
        if (this.mProgressDrawable != null) {
            this.mProgressDrawable.jumpToCurrentState();
        }
        if (this.mIndeterminateDrawable != null) {
            this.mIndeterminateDrawable.jumpToCurrentState();
        }
    }

    public void onResolveDrawables(int layoutDirection) {
        Drawable d = this.mCurrentDrawable;
        if ("ur".equals(Locale.getDefault().getLanguage())) {
            layoutDirection = 0;
        }
        if (d != null) {
            d.setLayoutDirection(layoutDirection);
        }
        if (this.mIndeterminateDrawable != null) {
            this.mIndeterminateDrawable.setLayoutDirection(layoutDirection);
        }
        if (this.mProgressDrawable != null) {
            this.mProgressDrawable.setLayoutDirection(layoutDirection);
        }
    }

    public void postInvalidate() {
        if (!this.mNoInvalidate) {
            super.postInvalidate();
        }
    }

    private synchronized void doRefreshProgress(int id, int progress, boolean fromUser, boolean callBackToApp, boolean animate) {
        boolean isPrimary = true;
        synchronized (this) {
            float scale = this.mMax > 0 ? ((float) progress) / ((float) this.mMax) : 0.0f;
            if (id != R.id.progress) {
                isPrimary = false;
            }
            if (isPrimary && animate) {
                ObjectAnimator animator = ObjectAnimator.ofFloat(this, this.VISUAL_PROGRESS, new float[]{scale});
                animator.setAutoCancel(true);
                animator.setDuration(80);
                animator.setInterpolator(PROGRESS_ANIM_INTERPOLATOR);
                animator.start();
            } else {
                setVisualProgress(id, scale);
            }
            if (isPrimary && callBackToApp) {
                onProgressRefresh(scale, fromUser, progress);
            }
        }
    }

    void onProgressRefresh(float scale, boolean fromUser, int progress) {
        if (AccessibilityManager.getInstance(this.mContext).isEnabled()) {
            scheduleAccessibilityEventSender();
        }
    }

    private void setVisualProgress(int id, float progress) {
        this.mVisualProgress = progress;
        Drawable d = this.mCurrentDrawable;
        if (d instanceof LayerDrawable) {
            d = ((LayerDrawable) d).findDrawableByLayerId(id);
            if (d == null) {
                d = this.mCurrentDrawable;
            }
        }
        if (d != null) {
            int level = (int) (10000.0f * progress);
            if (this.mForceRefresh && progress == 0.0f) {
                d.setLevel(MAX_LEVEL);
            }
            d.setLevel(level);
        } else {
            invalidate();
        }
        onVisualProgressChanged(id, progress);
    }

    void onVisualProgressChanged(int id, float progress) {
    }

    private synchronized void refreshProgress(int id, int progress, boolean fromUser, boolean animate) {
        if (this.mUiThreadId == Thread.currentThread().getId()) {
            doRefreshProgress(id, progress, fromUser, true, animate);
        } else {
            if (this.mRefreshProgressRunnable == null) {
                this.mRefreshProgressRunnable = new RefreshProgressRunnable();
            }
            RefreshData rd = RefreshData.obtain(id, progress, fromUser, animate);
            if (this.mRefreshData != null) {
                this.mRefreshData.add(rd);
            }
            if (this.mAttached && !this.mRefreshIsPosted) {
                post(this.mRefreshProgressRunnable);
                this.mRefreshIsPosted = true;
            }
        }
    }

    @RemotableViewMethod
    public synchronized void setProgress(int progress) {
        setProgressInternal(progress, false, false);
    }

    public void setProgress(int progress, boolean animate) {
        setProgressInternal(progress, false, animate);
    }

    @RemotableViewMethod
    public synchronized boolean setProgressInternal(int progress, boolean fromUser, boolean animate) {
        if (this.mIndeterminate) {
            return false;
        }
        progress = MathUtils.constrain(progress, 0, this.mMax);
        if (progress == this.mProgress && !this.mForceRefresh) {
            return false;
        }
        this.mProgress = progress;
        refreshProgress(R.id.progress, this.mProgress, fromUser, animate);
        return true;
    }

    @RemotableViewMethod
    public synchronized void setSecondaryProgress(int secondaryProgress) {
        if (!this.mIndeterminate) {
            if (secondaryProgress < 0) {
                secondaryProgress = 0;
            }
            if (secondaryProgress > this.mMax) {
                secondaryProgress = this.mMax;
            }
            if (secondaryProgress != this.mSecondaryProgress) {
                this.mSecondaryProgress = secondaryProgress;
                refreshProgress(R.id.secondaryProgress, this.mSecondaryProgress, false, false);
            }
        }
    }

    @ExportedProperty(category = "progress")
    public synchronized int getProgress() {
        return this.mIndeterminate ? 0 : this.mProgress;
    }

    @ExportedProperty(category = "progress")
    public synchronized int getSecondaryProgress() {
        return this.mIndeterminate ? 0 : this.mSecondaryProgress;
    }

    @ExportedProperty(category = "progress")
    public synchronized int getMax() {
        return this.mMax;
    }

    @RemotableViewMethod
    public synchronized void setMax(int max) {
        if (max < 0) {
            max = 0;
        }
        if (max != this.mMax) {
            this.mMax = max;
            postInvalidate();
            if (this.mProgress > max) {
                this.mProgress = max;
            }
            refreshProgress(R.id.progress, this.mProgress, false, false);
        }
    }

    public final synchronized void incrementProgressBy(int diff) {
        setProgress(this.mProgress + diff);
    }

    public final synchronized void incrementSecondaryProgressBy(int diff) {
        setSecondaryProgress(this.mSecondaryProgress + diff);
    }

    void startAnimation() {
        if (getVisibility() == 0 && getWindowVisibility() == 0) {
            if (this.mIndeterminateDrawable instanceof Animatable) {
                this.mShouldStartAnimationDrawable = true;
                this.mHasAnimation = false;
            } else {
                this.mHasAnimation = true;
                if (this.mInterpolator == null) {
                    this.mInterpolator = new LinearInterpolator();
                }
                if (this.mTransformation == null) {
                    this.mTransformation = new Transformation();
                } else {
                    this.mTransformation.clear();
                }
                if (this.mAnimation == null) {
                    this.mAnimation = new AlphaAnimation(0.0f, (float) LayoutParams.BRIGHTNESS_OVERRIDE_FULL);
                } else {
                    this.mAnimation.reset();
                }
                this.mAnimation.setRepeatMode(this.mBehavior);
                this.mAnimation.setRepeatCount(-1);
                this.mAnimation.setDuration((long) this.mDuration);
                this.mAnimation.setInterpolator(this.mInterpolator);
                this.mAnimation.setStartTime(-1);
            }
            postInvalidate();
        }
    }

    void stopAnimation() {
        this.mHasAnimation = false;
        if (this.mIndeterminateDrawable instanceof Animatable) {
            ((Animatable) this.mIndeterminateDrawable).stop();
            this.mShouldStartAnimationDrawable = false;
        }
        postInvalidate();
    }

    public void setInterpolator(Context context, int resID) {
        setInterpolator(AnimationUtils.loadInterpolator(context, resID));
    }

    public void setInterpolator(Interpolator interpolator) {
        this.mInterpolator = interpolator;
    }

    public Interpolator getInterpolator() {
        return this.mInterpolator;
    }

    public void onVisibilityAggregated(boolean isVisible) {
        super.onVisibilityAggregated(isVisible);
        if (isVisible != this.mAggregatedIsVisible) {
            this.mAggregatedIsVisible = isVisible;
            if (this.mIndeterminate) {
                if (isVisible) {
                    startAnimation();
                } else {
                    stopAnimation();
                }
            }
            if (this.mCurrentDrawable != null) {
                this.mCurrentDrawable.setVisible(isVisible, false);
            }
        }
    }

    public void invalidateDrawable(Drawable dr) {
        if (!this.mInDrawing) {
            if (verifyDrawable(dr)) {
                Rect dirty = dr.getBounds();
                int scrollX = this.mScrollX + this.mPaddingLeft;
                int scrollY = this.mScrollY + this.mPaddingTop;
                invalidate(dirty.left + scrollX, dirty.top + scrollY, dirty.right + scrollX, dirty.bottom + scrollY);
                return;
            }
            super.invalidateDrawable(dr);
        }
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        updateDrawableBounds(w, h);
    }

    private void updateDrawableBounds(int w, int h) {
        w -= this.mPaddingRight + this.mPaddingLeft;
        h -= this.mPaddingTop + this.mPaddingBottom;
        int right = w;
        int bottom = h;
        int top = 0;
        int left = 0;
        if (this.mIndeterminateDrawable != null) {
            if (this.mOnlyIndeterminate && !(this.mIndeterminateDrawable instanceof AnimationDrawable)) {
                float intrinsicAspect = ((float) this.mIndeterminateDrawable.getIntrinsicWidth()) / ((float) this.mIndeterminateDrawable.getIntrinsicHeight());
                float boundAspect = ((float) w) / ((float) h);
                if (intrinsicAspect != boundAspect) {
                    if (boundAspect > intrinsicAspect) {
                        int width = (int) (((float) h) * intrinsicAspect);
                        left = (w - width) / 2;
                        right = left + width;
                    } else {
                        int height = (int) (((float) w) * (LayoutParams.BRIGHTNESS_OVERRIDE_FULL / intrinsicAspect));
                        top = (h - height) / 2;
                        bottom = top + height;
                    }
                }
            }
            if (isLayoutRtl() && this.mMirrorForRtl && !"ur".equals(Locale.getDefault().getLanguage())) {
                int tempLeft = left;
                left = w - right;
                right = w - tempLeft;
            }
            this.mIndeterminateDrawable.setBounds(left, top, right, bottom);
        }
        if (this.mProgressDrawable != null) {
            this.mProgressDrawable.setBounds(0, 0, right, bottom);
        }
    }

    protected synchronized void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawTrack(canvas);
    }

    void drawTrack(Canvas canvas) {
        Drawable d = this.mCurrentDrawable;
        if (d != null) {
            int saveCount = canvas.save();
            if (isLayoutRtl() && this.mMirrorForRtl && !"ur".equals(Locale.getDefault().getLanguage())) {
                canvas.translate((float) (getWidth() - this.mPaddingRight), (float) this.mPaddingTop);
                canvas.scale(LayoutParams.BRIGHTNESS_OVERRIDE_NONE, LayoutParams.BRIGHTNESS_OVERRIDE_FULL);
            } else {
                canvas.translate((float) this.mPaddingLeft, (float) this.mPaddingTop);
            }
            long time = getDrawingTime();
            if (this.mHasAnimation) {
                this.mAnimation.getTransformation(time, this.mTransformation);
                float scale = this.mTransformation.getAlpha();
                try {
                    this.mInDrawing = true;
                    d.setLevel((int) (10000.0f * scale));
                    postInvalidateOnAnimation();
                } finally {
                    this.mInDrawing = false;
                }
            }
            d.draw(canvas);
            canvas.restoreToCount(saveCount);
            if (this.mShouldStartAnimationDrawable && (d instanceof Animatable)) {
                ((Animatable) d).start();
                this.mShouldStartAnimationDrawable = false;
            }
        }
    }

    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int dw = 0;
        int dh = 0;
        Drawable d = this.mCurrentDrawable;
        if (d != null) {
            dw = Math.max(this.mMinWidth, Math.min(this.mMaxWidth, d.getIntrinsicWidth()));
            dh = Math.max(this.mMinHeight, Math.min(this.mMaxHeight, d.getIntrinsicHeight()));
        }
        updateDrawableState();
        setMeasuredDimension(View.resolveSizeAndState(dw + (this.mPaddingLeft + this.mPaddingRight), widthMeasureSpec, 0), View.resolveSizeAndState(dh + (this.mPaddingTop + this.mPaddingBottom), heightMeasureSpec, 0));
    }

    protected void drawableStateChanged() {
        super.drawableStateChanged();
        updateDrawableState();
    }

    private void updateDrawableState() {
        int[] state = getDrawableState();
        int changed = 0;
        Drawable progressDrawable = this.mProgressDrawable;
        if (progressDrawable != null && progressDrawable.isStateful()) {
            changed = progressDrawable.setState(state);
        }
        Drawable indeterminateDrawable = this.mIndeterminateDrawable;
        if (indeterminateDrawable != null && indeterminateDrawable.isStateful()) {
            changed |= indeterminateDrawable.setState(state);
        }
        if (changed != 0) {
            invalidate();
        }
    }

    public void drawableHotspotChanged(float x, float y) {
        super.drawableHotspotChanged(x, y);
        if (this.mProgressDrawable != null) {
            this.mProgressDrawable.setHotspot(x, y);
        }
        if (this.mIndeterminateDrawable != null) {
            this.mIndeterminateDrawable.setHotspot(x, y);
        }
    }

    public Parcelable onSaveInstanceState() {
        SavedState ss = new SavedState(super.onSaveInstanceState());
        ss.progress = this.mProgress;
        ss.secondaryProgress = this.mSecondaryProgress;
        return ss;
    }

    public void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        setProgress(ss.progress);
        setSecondaryProgress(ss.secondaryProgress);
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (this.mIndeterminate) {
            startAnimation();
        }
        if (this.mRefreshData != null) {
            synchronized (this) {
                int count = this.mRefreshData.size();
                for (int i = 0; i < count; i++) {
                    RefreshData rd = (RefreshData) this.mRefreshData.get(i);
                    doRefreshProgress(rd.id, rd.progress, rd.fromUser, true, rd.animate);
                    rd.recycle();
                }
                this.mRefreshData.clear();
            }
        }
        this.mAttached = true;
    }

    protected void onDetachedFromWindow() {
        if (this.mIndeterminate) {
            stopAnimation();
        }
        if (this.mRefreshProgressRunnable != null) {
            removeCallbacks(this.mRefreshProgressRunnable);
            this.mRefreshIsPosted = false;
        }
        if (this.mAccessibilityEventSender != null) {
            removeCallbacks(this.mAccessibilityEventSender);
        }
        super.onDetachedFromWindow();
        this.mAttached = false;
    }

    public CharSequence getAccessibilityClassName() {
        return ProgressBar.class.getName();
    }

    public synchronized void onInitializeAccessibilityEventInternal(AccessibilityEvent event) {
        super.onInitializeAccessibilityEventInternal(event);
        event.setItemCount(this.mMax);
        event.setCurrentItemIndex(this.mProgress);
    }

    public void onInitializeAccessibilityNodeInfoInternal(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfoInternal(info);
        if (!isIndeterminate()) {
            info.setRangeInfo(RangeInfo.obtain(0, 0.0f, (float) getMax(), (float) getProgress()));
        }
    }

    private void scheduleAccessibilityEventSender() {
        if (this.mAccessibilityEventSender == null) {
            this.mAccessibilityEventSender = new AccessibilityEventSender();
        } else {
            removeCallbacks(this.mAccessibilityEventSender);
        }
        postDelayed(this.mAccessibilityEventSender, 200);
    }

    protected void encodeProperties(ViewHierarchyEncoder stream) {
        super.encodeProperties(stream);
        stream.addProperty("progress:max", getMax());
        stream.addProperty("progress:progress", getProgress());
        stream.addProperty("progress:secondaryProgress", getSecondaryProgress());
        stream.addProperty("progress:indeterminate", isIndeterminate());
    }
}
