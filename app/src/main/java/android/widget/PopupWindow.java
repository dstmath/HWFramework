package android.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.IBinder;
import android.os.SystemProperties;
import android.transition.Transition;
import android.transition.Transition.EpicenterCallback;
import android.transition.Transition.TransitionListener;
import android.transition.Transition.TransitionListenerAdapter;
import android.transition.TransitionInflater;
import android.transition.TransitionManager;
import android.transition.TransitionSet;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.KeyEvent.DispatcherState;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnAttachStateChangeListener;
import android.view.View.OnTouchListener;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.ViewTreeObserver.OnScrollChangedListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.WindowManagerGlobal;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.inputmethod.EditorInfo;
import com.android.internal.R;
import com.android.internal.telephony.RILConstants;
import com.android.internal.util.Protocol;
import java.lang.ref.WeakReference;
import javax.microedition.khronos.opengles.GL10;

public class PopupWindow {
    private static final int[] ABOVE_ANCHOR_STATE_SET = null;
    private static final int ANIMATION_STYLE_DEFAULT = -1;
    private static final int DEFAULT_ANCHORED_GRAVITY = 8388659;
    public static final int INPUT_METHOD_FROM_FOCUSABLE = 0;
    public static final int INPUT_METHOD_NEEDED = 1;
    public static final int INPUT_METHOD_NOT_NEEDED = 2;
    private static final String TAG = "PopupWindow";
    private boolean mAboveAnchor;
    private Drawable mAboveAnchorBackgroundDrawable;
    private boolean mAllowScrollingAnchorParent;
    private WeakReference<View> mAnchor;
    private WeakReference<View> mAnchorRoot;
    private int mAnchorXoff;
    private int mAnchorYoff;
    private int mAnchoredGravity;
    private int mAnimationStyle;
    private boolean mAttachedInDecor;
    private boolean mAttachedInDecorSet;
    private Drawable mBackground;
    private View mBackgroundView;
    private Drawable mBelowAnchorBackgroundDrawable;
    private boolean mClipToScreen;
    private boolean mClippingEnabled;
    private View mContentView;
    private Context mContext;
    private PopupDecorView mDecorView;
    private float mDownX;
    private float mDownY;
    private float mElevation;
    private Transition mEnterTransition;
    private Rect mEpicenterBounds;
    private Transition mExitTransition;
    private boolean mFocusable;
    private int mHeight;
    private int mHeightMode;
    private boolean mIgnoreCheekPress;
    private int mInputMethodMode;
    private boolean mIsAnchorRootAttached;
    private boolean mIsDropdown;
    private boolean mIsForSpinner;
    private boolean mIsShowing;
    private boolean mIsTransitioningToDismiss;
    private int mLastHeight;
    private int mLastWidth;
    private boolean mLayoutInScreen;
    private boolean mLayoutInsetDecor;
    protected int mNewDisplayFrameLeft;
    protected int mNewDisplayFrameRight;
    private boolean mNotTouchModal;
    private final OnAttachStateChangeListener mOnAnchorRootDetachedListener;
    private OnDismissListener mOnDismissListener;
    private final OnScrollChangedListener mOnScrollChangedListener;
    private boolean mOutsideTouchable;
    private boolean mOverlapAnchor;
    private boolean mPopupViewInitialLayoutDirectionInherited;
    private int mSlop;
    private int mSoftInputMode;
    private int mSplitTouchEnabled;
    private final Rect mTempRect;
    private final int[] mTmpDrawingLocation;
    private final int[] mTmpScreenLocation;
    private OnTouchListener mTouchInterceptor;
    private boolean mTouchable;
    private int mWidth;
    private int mWidthMode;
    private int mWindowLayoutType;
    private WindowManager mWindowManager;

    public interface OnDismissListener {
        void onDismiss();
    }

    /* renamed from: android.widget.PopupWindow.3 */
    class AnonymousClass3 extends EpicenterCallback {
        final /* synthetic */ Rect val$epicenter;

        AnonymousClass3(Rect val$epicenter) {
            this.val$epicenter = val$epicenter;
        }

        public Rect onGetEpicenter(Transition transition) {
            return this.val$epicenter;
        }
    }

    /* renamed from: android.widget.PopupWindow.4 */
    class AnonymousClass4 extends TransitionListenerAdapter {
        final /* synthetic */ ViewGroup val$contentHolder;
        final /* synthetic */ View val$contentView;
        final /* synthetic */ PopupDecorView val$decorView;

        AnonymousClass4(PopupDecorView val$decorView, ViewGroup val$contentHolder, View val$contentView) {
            this.val$decorView = val$decorView;
            this.val$contentHolder = val$contentHolder;
            this.val$contentView = val$contentView;
        }

        public void onTransitionEnd(Transition transition) {
            PopupWindow.this.dismissImmediate(this.val$decorView, this.val$contentHolder, this.val$contentView);
        }
    }

    private class PopupBackgroundView extends FrameLayout {
        public PopupBackgroundView(Context context) {
            super(context);
        }

        protected int[] onCreateDrawableState(int extraSpace) {
            if (!PopupWindow.this.mAboveAnchor) {
                return super.onCreateDrawableState(extraSpace);
            }
            int[] drawableState = super.onCreateDrawableState(extraSpace + PopupWindow.INPUT_METHOD_NEEDED);
            View.mergeDrawableStates(drawableState, PopupWindow.ABOVE_ANCHOR_STATE_SET);
            return drawableState;
        }
    }

    private class PopupDecorView extends FrameLayout {
        private final OnAttachStateChangeListener mOnAnchorRootDetachedListener;
        private TransitionListenerAdapter mPendingExitListener;

        /* renamed from: android.widget.PopupWindow.PopupDecorView.2 */
        class AnonymousClass2 implements OnGlobalLayoutListener {
            final /* synthetic */ Transition val$enterTransition;

            /* renamed from: android.widget.PopupWindow.PopupDecorView.2.1 */
            class AnonymousClass1 extends EpicenterCallback {
                final /* synthetic */ Rect val$epicenter;

                AnonymousClass1(Rect val$epicenter) {
                    this.val$epicenter = val$epicenter;
                }

                public Rect onGetEpicenter(Transition transition) {
                    return this.val$epicenter;
                }
            }

            AnonymousClass2(Transition val$enterTransition) {
                this.val$enterTransition = val$enterTransition;
            }

            public void onGlobalLayout() {
                ViewTreeObserver observer = PopupDecorView.this.getViewTreeObserver();
                if (observer != null) {
                    observer.removeOnGlobalLayoutListener(this);
                }
                this.val$enterTransition.setEpicenterCallback(new AnonymousClass1(PopupWindow.this.getTransitionEpicenter()));
                PopupDecorView.this.startEnterTransition(this.val$enterTransition);
            }
        }

        /* renamed from: android.widget.PopupWindow.PopupDecorView.3 */
        class AnonymousClass3 extends TransitionListenerAdapter {
            final /* synthetic */ View val$anchorRoot;
            final /* synthetic */ TransitionListener val$listener;

            AnonymousClass3(View val$anchorRoot, TransitionListener val$listener) {
                this.val$anchorRoot = val$anchorRoot;
                this.val$listener = val$listener;
            }

            public void onTransitionEnd(Transition transition) {
                this.val$anchorRoot.removeOnAttachStateChangeListener(PopupDecorView.this.mOnAnchorRootDetachedListener);
                this.val$listener.onTransitionEnd(transition);
                PopupDecorView.this.mPendingExitListener = null;
            }
        }

        public PopupDecorView(Context context) {
            super(context);
            this.mOnAnchorRootDetachedListener = new OnAttachStateChangeListener() {
                public void onViewAttachedToWindow(View v) {
                }

                public void onViewDetachedFromWindow(View v) {
                    v.removeOnAttachStateChangeListener(this);
                    TransitionManager.endTransitions(PopupDecorView.this);
                }
            };
        }

        public boolean dispatchKeyEvent(KeyEvent event) {
            if (event.getKeyCode() != 4) {
                return super.dispatchKeyEvent(event);
            }
            if (getKeyDispatcherState() == null) {
                return super.dispatchKeyEvent(event);
            }
            DispatcherState state;
            if (event.getAction() == 0 && event.getRepeatCount() == 0) {
                state = getKeyDispatcherState();
                if (state != null) {
                    state.startTracking(event, this);
                }
                return true;
            }
            if (event.getAction() == PopupWindow.INPUT_METHOD_NEEDED) {
                state = getKeyDispatcherState();
                if (!(state == null || !state.isTracking(event) || event.isCanceled())) {
                    PopupWindow.this.dismiss();
                    return true;
                }
            }
            return super.dispatchKeyEvent(event);
        }

        public boolean dispatchTouchEvent(MotionEvent ev) {
            if (PopupWindow.this.mTouchInterceptor == null || !PopupWindow.this.mTouchInterceptor.onTouch(this, ev)) {
                return super.dispatchTouchEvent(ev);
            }
            return true;
        }

        public boolean onTouchEvent(MotionEvent event) {
            int x = (int) event.getX();
            int y = (int) event.getY();
            if (event.getAction() == 0) {
                PopupWindow.this.mDownX = (float) x;
                PopupWindow.this.mDownY = (float) y;
            }
            if (event.getAction() == 0 && (x < 0 || x >= getWidth() || y < 0 || y >= getHeight())) {
                return true;
            }
            if (event.getAction() == PopupWindow.INPUT_METHOD_NEEDED && (x < 0 || x >= getWidth() || y < 0 || y >= getHeight())) {
                if (Math.abs(((float) x) - PopupWindow.this.mDownX) <= ((float) PopupWindow.this.mSlop) && Math.abs(((float) y) - PopupWindow.this.mDownY) <= ((float) PopupWindow.this.mSlop)) {
                    PopupWindow.this.dismiss();
                }
                return true;
            } else if (event.getAction() != 4) {
                return super.onTouchEvent(event);
            } else {
                PopupWindow.this.dismiss();
                return true;
            }
        }

        public void requestEnterTransition(Transition transition) {
            ViewTreeObserver observer = getViewTreeObserver();
            if (observer != null && transition != null) {
                observer.addOnGlobalLayoutListener(new AnonymousClass2(transition.clone()));
            }
        }

        private void startEnterTransition(Transition enterTransition) {
            int i;
            int count = getChildCount();
            for (i = PopupWindow.INPUT_METHOD_FROM_FOCUSABLE; i < count; i += PopupWindow.INPUT_METHOD_NEEDED) {
                View child = getChildAt(i);
                enterTransition.addTarget(child);
                child.setVisibility(4);
            }
            TransitionManager.beginDelayedTransition(this, enterTransition);
            for (i = PopupWindow.INPUT_METHOD_FROM_FOCUSABLE; i < count; i += PopupWindow.INPUT_METHOD_NEEDED) {
                getChildAt(i).setVisibility(PopupWindow.INPUT_METHOD_FROM_FOCUSABLE);
            }
        }

        public void startExitTransition(Transition transition, View anchorRoot, TransitionListener listener) {
            if (transition != null) {
                int i;
                anchorRoot.addOnAttachStateChangeListener(this.mOnAnchorRootDetachedListener);
                this.mPendingExitListener = new AnonymousClass3(anchorRoot, listener);
                Transition exitTransition = transition.clone();
                exitTransition.addListener(this.mPendingExitListener);
                int count = getChildCount();
                for (i = PopupWindow.INPUT_METHOD_FROM_FOCUSABLE; i < count; i += PopupWindow.INPUT_METHOD_NEEDED) {
                    exitTransition.addTarget(getChildAt(i));
                }
                TransitionManager.beginDelayedTransition(this, exitTransition);
                for (i = PopupWindow.INPUT_METHOD_FROM_FOCUSABLE; i < count; i += PopupWindow.INPUT_METHOD_NEEDED) {
                    getChildAt(i).setVisibility(4);
                }
            }
        }

        public void cancelTransitions() {
            TransitionManager.endTransitions(this);
            if (this.mPendingExitListener != null) {
                this.mPendingExitListener.onTransitionEnd(null);
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.widget.PopupWindow.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.widget.PopupWindow.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.widget.PopupWindow.<clinit>():void");
    }

    public PopupWindow(Context context) {
        this(context, null);
    }

    public PopupWindow(Context context, AttributeSet attrs) {
        this(context, attrs, (int) R.attr.popupWindowStyle);
    }

    public PopupWindow(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, (int) INPUT_METHOD_FROM_FOCUSABLE);
    }

    public PopupWindow(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        this.mTmpDrawingLocation = new int[INPUT_METHOD_NOT_NEEDED];
        this.mTmpScreenLocation = new int[INPUT_METHOD_NOT_NEEDED];
        this.mTempRect = new Rect();
        this.mInputMethodMode = INPUT_METHOD_FROM_FOCUSABLE;
        this.mSoftInputMode = INPUT_METHOD_NEEDED;
        this.mTouchable = true;
        this.mOutsideTouchable = false;
        this.mClippingEnabled = true;
        this.mSplitTouchEnabled = ANIMATION_STYLE_DEFAULT;
        this.mAllowScrollingAnchorParent = true;
        this.mLayoutInsetDecor = false;
        this.mAttachedInDecor = true;
        this.mAttachedInDecorSet = false;
        this.mWidth = -2;
        this.mHeight = -2;
        this.mWindowLayoutType = RILConstants.RIL_UNSOL_RESPONSE_RADIO_STATE_CHANGED;
        this.mIgnoreCheekPress = false;
        this.mAnimationStyle = ANIMATION_STYLE_DEFAULT;
        this.mSlop = 32;
        this.mOnAnchorRootDetachedListener = new OnAttachStateChangeListener() {
            public void onViewAttachedToWindow(View v) {
            }

            public void onViewDetachedFromWindow(View v) {
                PopupWindow.this.mIsAnchorRootAttached = false;
            }
        };
        this.mOnScrollChangedListener = new OnScrollChangedListener() {
            public void onScrollChanged() {
                View anchor = null;
                if (PopupWindow.this.mAnchor != null) {
                    anchor = (View) PopupWindow.this.mAnchor.get();
                }
                if (anchor != null && PopupWindow.this.mDecorView != null) {
                    LayoutParams p = (LayoutParams) PopupWindow.this.mDecorView.getLayoutParams();
                    PopupWindow.this.updateAboveAnchor(PopupWindow.this.findDropDownPosition(anchor, p, PopupWindow.this.mAnchorXoff, PopupWindow.this.mAnchorYoff, p.width, p.height, PopupWindow.this.mAnchoredGravity));
                    PopupWindow.this.update(p.x, p.y, PopupWindow.ANIMATION_STYLE_DEFAULT, PopupWindow.ANIMATION_STYLE_DEFAULT, true);
                }
            }
        };
        this.mIsForSpinner = false;
        this.mContext = context;
        this.mWindowManager = (WindowManager) context.getSystemService("window");
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PopupWindow, defStyleAttr, defStyleRes);
        Drawable bg = a.getDrawable(INPUT_METHOD_FROM_FOCUSABLE);
        this.mElevation = a.getDimension(3, 0.0f);
        this.mOverlapAnchor = a.getBoolean(INPUT_METHOD_NOT_NEEDED, false);
        if (a.hasValueOrEmpty(INPUT_METHOD_NEEDED)) {
            int animStyle = a.getResourceId(INPUT_METHOD_NEEDED, INPUT_METHOD_FROM_FOCUSABLE);
            if (animStyle == R.style.Animation_PopupWindow) {
                this.mAnimationStyle = ANIMATION_STYLE_DEFAULT;
            } else {
                this.mAnimationStyle = animStyle;
            }
        } else {
            this.mAnimationStyle = ANIMATION_STYLE_DEFAULT;
        }
        Transition enterTransition = getTransition(a.getResourceId(4, INPUT_METHOD_FROM_FOCUSABLE));
        Transition transition = a.hasValueOrEmpty(5) ? getTransition(a.getResourceId(5, INPUT_METHOD_FROM_FOCUSABLE)) : enterTransition == null ? null : enterTransition.clone();
        a.recycle();
        setEnterTransition(enterTransition);
        setExitTransition(transition);
        setBackgroundDrawable(bg);
        this.mSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    public PopupWindow() {
        this(null, (int) INPUT_METHOD_FROM_FOCUSABLE, (int) INPUT_METHOD_FROM_FOCUSABLE);
    }

    public PopupWindow(View contentView) {
        this(contentView, (int) INPUT_METHOD_FROM_FOCUSABLE, (int) INPUT_METHOD_FROM_FOCUSABLE);
    }

    public PopupWindow(int width, int height) {
        this(null, width, height);
    }

    public PopupWindow(View contentView, int width, int height) {
        this(contentView, width, height, false);
    }

    public PopupWindow(View contentView, int width, int height, boolean focusable) {
        this.mTmpDrawingLocation = new int[INPUT_METHOD_NOT_NEEDED];
        this.mTmpScreenLocation = new int[INPUT_METHOD_NOT_NEEDED];
        this.mTempRect = new Rect();
        this.mInputMethodMode = INPUT_METHOD_FROM_FOCUSABLE;
        this.mSoftInputMode = INPUT_METHOD_NEEDED;
        this.mTouchable = true;
        this.mOutsideTouchable = false;
        this.mClippingEnabled = true;
        this.mSplitTouchEnabled = ANIMATION_STYLE_DEFAULT;
        this.mAllowScrollingAnchorParent = true;
        this.mLayoutInsetDecor = false;
        this.mAttachedInDecor = true;
        this.mAttachedInDecorSet = false;
        this.mWidth = -2;
        this.mHeight = -2;
        this.mWindowLayoutType = RILConstants.RIL_UNSOL_RESPONSE_RADIO_STATE_CHANGED;
        this.mIgnoreCheekPress = false;
        this.mAnimationStyle = ANIMATION_STYLE_DEFAULT;
        this.mSlop = 32;
        this.mOnAnchorRootDetachedListener = new OnAttachStateChangeListener() {
            public void onViewAttachedToWindow(View v) {
            }

            public void onViewDetachedFromWindow(View v) {
                PopupWindow.this.mIsAnchorRootAttached = false;
            }
        };
        this.mOnScrollChangedListener = new OnScrollChangedListener() {
            public void onScrollChanged() {
                View anchor = null;
                if (PopupWindow.this.mAnchor != null) {
                    anchor = (View) PopupWindow.this.mAnchor.get();
                }
                if (anchor != null && PopupWindow.this.mDecorView != null) {
                    LayoutParams p = (LayoutParams) PopupWindow.this.mDecorView.getLayoutParams();
                    PopupWindow.this.updateAboveAnchor(PopupWindow.this.findDropDownPosition(anchor, p, PopupWindow.this.mAnchorXoff, PopupWindow.this.mAnchorYoff, p.width, p.height, PopupWindow.this.mAnchoredGravity));
                    PopupWindow.this.update(p.x, p.y, PopupWindow.ANIMATION_STYLE_DEFAULT, PopupWindow.ANIMATION_STYLE_DEFAULT, true);
                }
            }
        };
        this.mIsForSpinner = false;
        if (contentView != null) {
            this.mContext = contentView.getContext();
            this.mWindowManager = (WindowManager) this.mContext.getSystemService("window");
        }
        setContentView(contentView);
        setWidth(width);
        setHeight(height);
        setFocusable(focusable);
    }

    public void setSpinner(boolean IsForSpinner) {
        this.mIsForSpinner = IsForSpinner;
    }

    public void setEnterTransition(Transition enterTransition) {
        this.mEnterTransition = enterTransition;
    }

    public Transition getEnterTransition() {
        return this.mEnterTransition;
    }

    public void setExitTransition(Transition exitTransition) {
        this.mExitTransition = exitTransition;
    }

    public Transition getExitTransition() {
        return this.mExitTransition;
    }

    public void setEpicenterBounds(Rect bounds) {
        this.mEpicenterBounds = bounds;
    }

    private Transition getTransition(int resId) {
        boolean isEmpty = false;
        if (!(resId == 0 || resId == R.transition.no_transition)) {
            Transition transition = TransitionInflater.from(this.mContext).inflateTransition(resId);
            if (transition != null) {
                if ((transition instanceof TransitionSet) && ((TransitionSet) transition).getTransitionCount() == 0) {
                    isEmpty = true;
                }
                if (!isEmpty) {
                    return transition;
                }
            }
        }
        return null;
    }

    public Drawable getBackground() {
        return this.mBackground;
    }

    public void setBackgroundDrawable(Drawable background) {
        this.mBackground = background;
        if (this.mBackground instanceof StateListDrawable) {
            StateListDrawable stateList = this.mBackground;
            int aboveAnchorStateIndex = stateList.getStateDrawableIndex(ABOVE_ANCHOR_STATE_SET);
            int count = stateList.getStateCount();
            int belowAnchorStateIndex = ANIMATION_STYLE_DEFAULT;
            for (int i = INPUT_METHOD_FROM_FOCUSABLE; i < count; i += INPUT_METHOD_NEEDED) {
                if (i != aboveAnchorStateIndex) {
                    belowAnchorStateIndex = i;
                    break;
                }
            }
            if (aboveAnchorStateIndex == ANIMATION_STYLE_DEFAULT || belowAnchorStateIndex == ANIMATION_STYLE_DEFAULT) {
                this.mBelowAnchorBackgroundDrawable = null;
                this.mAboveAnchorBackgroundDrawable = null;
                return;
            }
            this.mAboveAnchorBackgroundDrawable = stateList.getStateDrawable(aboveAnchorStateIndex);
            this.mBelowAnchorBackgroundDrawable = stateList.getStateDrawable(belowAnchorStateIndex);
        }
    }

    public float getElevation() {
        return this.mElevation;
    }

    public void setElevation(float elevation) {
        this.mElevation = elevation;
    }

    public int getAnimationStyle() {
        return this.mAnimationStyle;
    }

    public void setIgnoreCheekPress() {
        this.mIgnoreCheekPress = true;
    }

    public void setAnimationStyle(int animationStyle) {
        this.mAnimationStyle = animationStyle;
    }

    public View getContentView() {
        return this.mContentView;
    }

    public void setContentView(View contentView) {
        if (!isShowing()) {
            this.mContentView = contentView;
            if (this.mContext == null && this.mContentView != null) {
                this.mContext = this.mContentView.getContext();
            }
            if (this.mWindowManager == null && this.mContentView != null) {
                this.mWindowManager = (WindowManager) this.mContext.getSystemService("window");
            }
            if (!(this.mContext == null || this.mAttachedInDecorSet)) {
                setAttachedInDecor(this.mContext.getApplicationInfo().targetSdkVersion >= 22);
            }
        }
    }

    public void setTouchInterceptor(OnTouchListener l) {
        this.mTouchInterceptor = l;
    }

    public boolean isFocusable() {
        return this.mFocusable;
    }

    public void setFocusable(boolean focusable) {
        this.mFocusable = focusable;
    }

    public int getInputMethodMode() {
        return this.mInputMethodMode;
    }

    public void setInputMethodMode(int mode) {
        this.mInputMethodMode = mode;
    }

    public void setSoftInputMode(int mode) {
        this.mSoftInputMode = mode;
    }

    public int getSoftInputMode() {
        return this.mSoftInputMode;
    }

    public boolean isTouchable() {
        return this.mTouchable;
    }

    public void setTouchable(boolean touchable) {
        this.mTouchable = touchable;
    }

    public boolean isOutsideTouchable() {
        return this.mOutsideTouchable;
    }

    public void setOutsideTouchable(boolean touchable) {
        this.mOutsideTouchable = touchable;
    }

    public boolean isClippingEnabled() {
        return this.mClippingEnabled;
    }

    public void setClippingEnabled(boolean enabled) {
        this.mClippingEnabled = enabled;
    }

    public void setClipToScreenEnabled(boolean enabled) {
        this.mClipToScreen = enabled;
    }

    void setAllowScrollingAnchorParent(boolean enabled) {
        this.mAllowScrollingAnchorParent = enabled;
    }

    public boolean isSplitTouchEnabled() {
        boolean z = true;
        if (this.mSplitTouchEnabled >= 0 || this.mContext == null) {
            if (this.mSplitTouchEnabled != INPUT_METHOD_NEEDED) {
                z = false;
            }
            return z;
        }
        if (this.mContext.getApplicationInfo().targetSdkVersion < 11) {
            z = false;
        }
        return z;
    }

    public void setSplitTouchEnabled(boolean enabled) {
        this.mSplitTouchEnabled = enabled ? INPUT_METHOD_NEEDED : INPUT_METHOD_FROM_FOCUSABLE;
    }

    public boolean isLayoutInScreenEnabled() {
        return this.mLayoutInScreen;
    }

    public void setLayoutInScreenEnabled(boolean enabled) {
        this.mLayoutInScreen = enabled;
    }

    public boolean isAttachedInDecor() {
        return this.mAttachedInDecor;
    }

    public void setAttachedInDecor(boolean enabled) {
        this.mAttachedInDecor = enabled;
        this.mAttachedInDecorSet = true;
    }

    public void setLayoutInsetDecor(boolean enabled) {
        this.mLayoutInsetDecor = enabled;
    }

    public void setWindowLayoutType(int layoutType) {
        this.mWindowLayoutType = layoutType;
    }

    public int getWindowLayoutType() {
        return this.mWindowLayoutType;
    }

    public void setTouchModal(boolean touchModal) {
        this.mNotTouchModal = !touchModal;
    }

    @Deprecated
    public void setWindowLayoutMode(int widthSpec, int heightSpec) {
        this.mWidthMode = widthSpec;
        this.mHeightMode = heightSpec;
    }

    public int getHeight() {
        return this.mHeight;
    }

    public void setHeight(int height) {
        this.mHeight = height;
    }

    public int getWidth() {
        return this.mWidth;
    }

    public void setWidth(int width) {
        this.mWidth = width;
    }

    public void setOverlapAnchor(boolean overlapAnchor) {
        this.mOverlapAnchor = overlapAnchor;
    }

    public boolean getOverlapAnchor() {
        return this.mOverlapAnchor;
    }

    public boolean isShowing() {
        return this.mIsShowing;
    }

    public void showAtLocation(View parent, int gravity, int x, int y) {
        showAtLocation(parent.getWindowToken(), gravity, x, y);
    }

    public void showAtLocation(IBinder token, int gravity, int x, int y) {
        if (!isShowing() && this.mContentView != null) {
            TransitionManager.endTransitions(this.mDecorView);
            detachFromAnchor();
            this.mIsShowing = true;
            this.mIsDropdown = false;
            LayoutParams p = createPopupLayoutParams(token);
            preparePopup(p);
            if (gravity != 0) {
                p.gravity = gravity;
            }
            p.x = x;
            p.y = y;
            invokePopup(p);
        }
    }

    public void showAsDropDown(View anchor) {
        showAsDropDown(anchor, INPUT_METHOD_FROM_FOCUSABLE, INPUT_METHOD_FROM_FOCUSABLE);
    }

    public void showAsDropDown(View anchor, int xoff, int yoff) {
        showAsDropDown(anchor, xoff, yoff, DEFAULT_ANCHORED_GRAVITY);
    }

    public void showAsDropDown(View anchor, int xoff, int yoff, int gravity) {
        if (!isShowing() && this.mContentView != null) {
            TransitionManager.endTransitions(this.mDecorView);
            attachToAnchor(anchor, xoff, yoff, gravity);
            this.mIsShowing = true;
            this.mIsDropdown = true;
            LayoutParams p = createPopupLayoutParams(anchor.getWindowToken());
            preparePopup(p);
            updateAboveAnchor(findDropDownPosition(anchor, p, xoff, yoff, p.width, p.height, gravity));
            p.accessibilityIdOfAnchor = anchor != null ? anchor.getAccessibilityViewId() : ANIMATION_STYLE_DEFAULT;
            invokePopup(p);
        }
    }

    private void updateAboveAnchor(boolean aboveAnchor) {
        if (aboveAnchor != this.mAboveAnchor) {
            this.mAboveAnchor = aboveAnchor;
            if (this.mBackground != null && this.mBackgroundView != null) {
                if (this.mAboveAnchorBackgroundDrawable == null) {
                    this.mBackgroundView.refreshDrawableState();
                } else if (this.mAboveAnchor) {
                    this.mBackgroundView.setBackground(this.mAboveAnchorBackgroundDrawable);
                } else {
                    this.mBackgroundView.setBackground(this.mBelowAnchorBackgroundDrawable);
                }
            }
        }
    }

    public boolean isAboveAnchor() {
        return this.mAboveAnchor;
    }

    private void preparePopup(LayoutParams p) {
        boolean z = true;
        if (this.mContentView == null || this.mContext == null || this.mWindowManager == null) {
            throw new IllegalStateException("You must specify a valid content view by calling setContentView() before attempting to show the popup.");
        }
        if (this.mDecorView != null) {
            this.mDecorView.cancelTransitions();
        }
        if (this.mBackground != null) {
            this.mBackgroundView = createBackgroundView(this.mContentView);
            this.mBackgroundView.setBackground(this.mBackground);
        } else {
            this.mBackgroundView = this.mContentView;
        }
        this.mDecorView = createDecorView(this.mBackgroundView);
        this.mBackgroundView.setElevation(this.mElevation);
        p.setSurfaceInsets(this.mBackgroundView, true, true);
        if (this.mContentView.getRawLayoutDirection() != INPUT_METHOD_NOT_NEEDED) {
            z = false;
        }
        this.mPopupViewInitialLayoutDirectionInherited = z;
    }

    private PopupBackgroundView createBackgroundView(View contentView) {
        int height;
        ViewGroup.LayoutParams layoutParams = this.mContentView.getLayoutParams();
        if (layoutParams == null || layoutParams.height != -2) {
            height = ANIMATION_STYLE_DEFAULT;
        } else {
            height = -2;
        }
        PopupBackgroundView backgroundView = new PopupBackgroundView(this.mContext);
        backgroundView.addView(contentView, (ViewGroup.LayoutParams) new FrameLayout.LayoutParams((int) ANIMATION_STYLE_DEFAULT, height));
        return backgroundView;
    }

    private PopupDecorView createDecorView(View contentView) {
        int height;
        ViewGroup.LayoutParams layoutParams = this.mContentView.getLayoutParams();
        if (layoutParams == null || layoutParams.height != -2) {
            height = ANIMATION_STYLE_DEFAULT;
        } else {
            height = -2;
        }
        PopupDecorView decorView = new PopupDecorView(this.mContext);
        decorView.addView(contentView, (int) ANIMATION_STYLE_DEFAULT, height);
        decorView.setClipChildren(false);
        decorView.setClipToPadding(false);
        return decorView;
    }

    private void invokePopup(LayoutParams p) {
        if (this.mContext != null) {
            p.packageName = this.mContext.getPackageName();
        }
        if (this.mDecorView == null) {
            Log.d("mDecorView", "invokePopup mDecorView == null");
            return;
        }
        PopupDecorView decorView = this.mDecorView;
        decorView.setFitsSystemWindows(this.mLayoutInsetDecor);
        setLayoutDirectionFromAnchor();
        this.mWindowManager.addView(decorView, p);
        if (this.mEnterTransition != null) {
            decorView.requestEnterTransition(this.mEnterTransition);
        }
    }

    private void setLayoutDirectionFromAnchor() {
        if (this.mAnchor != null) {
            View anchor = (View) this.mAnchor.get();
            if (anchor != null && this.mPopupViewInitialLayoutDirectionInherited) {
                this.mDecorView.setLayoutDirection(anchor.getLayoutDirection());
            }
        }
    }

    private int computeGravity() {
        if (this.mClipToScreen || this.mClippingEnabled) {
            return 276824115;
        }
        return DEFAULT_ANCHORED_GRAVITY;
    }

    private LayoutParams createPopupLayoutParams(IBinder token) {
        int i;
        LayoutParams p = new LayoutParams();
        p.gravity = computeGravity();
        p.flags = computeFlags(p.flags);
        p.type = this.mWindowLayoutType;
        p.token = token;
        p.softInputMode = this.mSoftInputMode;
        p.windowAnimations = computeAnimationResource();
        if (this.mBackground != null) {
            p.format = this.mBackground.getOpacity();
        } else {
            p.format = -3;
        }
        if (this.mHeightMode < 0) {
            i = this.mHeightMode;
            this.mLastHeight = i;
            p.height = i;
        } else {
            i = this.mHeight;
            this.mLastHeight = i;
            p.height = i;
        }
        if (this.mWidthMode < 0) {
            i = this.mWidthMode;
            this.mLastWidth = i;
            p.width = i;
        } else {
            i = this.mWidth;
            this.mLastWidth = i;
            p.width = i;
        }
        p.privateFlags = 98304;
        p.setTitle("PopupWindow:" + Integer.toHexString(hashCode()));
        return p;
    }

    private int computeFlags(int curFlags) {
        curFlags &= -8815129;
        if (this.mIgnoreCheekPress) {
            curFlags |= AccessibilityNodeInfo.ACTION_PASTE;
        }
        if (!this.mFocusable) {
            curFlags |= 8;
            if (this.mInputMethodMode == INPUT_METHOD_NEEDED) {
                curFlags |= Protocol.BASE_WIFI;
            }
        } else if (this.mInputMethodMode == INPUT_METHOD_NOT_NEEDED) {
            curFlags |= Protocol.BASE_WIFI;
        }
        if (!this.mTouchable) {
            curFlags |= 16;
        }
        if (this.mOutsideTouchable) {
            curFlags |= Protocol.BASE_DATA_CONNECTION;
        }
        if (!this.mClippingEnabled || this.mClipToScreen) {
            curFlags |= GL10.GL_NEVER;
        }
        if (isSplitTouchEnabled()) {
            curFlags |= AccessibilityEvent.TYPE_VIEW_CONTEXT_CLICKED;
        }
        if (this.mLayoutInScreen) {
            curFlags |= GL10.GL_DEPTH_BUFFER_BIT;
        }
        if (this.mLayoutInsetDecor) {
            curFlags |= Protocol.BASE_SYSTEM_RESERVED;
        }
        if (this.mNotTouchModal) {
            curFlags |= 32;
        }
        if (this.mAttachedInDecor) {
            return curFlags | EditorInfo.IME_FLAG_NO_ENTER_ACTION;
        }
        return curFlags;
    }

    private int computeAnimationResource() {
        if (this.mAnimationStyle != ANIMATION_STYLE_DEFAULT) {
            return this.mAnimationStyle;
        }
        if (!this.mIsDropdown) {
            return INPUT_METHOD_FROM_FOCUSABLE;
        }
        int i;
        if (this.mAboveAnchor) {
            i = R.style.Animation_DropDownUp;
        } else {
            i = R.style.Animation_DropDownDown;
        }
        return i;
    }

    public void setPopupLocation(int start, int end) {
        this.mNewDisplayFrameLeft = start;
        this.mNewDisplayFrameRight = end;
    }

    private boolean findDropDownPosition(View anchor, LayoutParams outParams, int xOffset, int yOffset, int width, int height, int gravity) {
        String packageName = this.mContext.getPackageName();
        int anchorHeight = WindowManagerGlobal.getInstance().translateIntegerInAppToScreen(packageName, anchor.getHeight());
        int anchorWidth = WindowManagerGlobal.getInstance().translateIntegerInAppToScreen(packageName, anchor.getWidth());
        width = WindowManagerGlobal.getInstance().translateIntegerInAppToScreen(packageName, width);
        height = WindowManagerGlobal.getInstance().translateIntegerInAppToScreen(packageName, height);
        xOffset = WindowManagerGlobal.getInstance().translateIntegerInAppToScreen(packageName, xOffset);
        yOffset = WindowManagerGlobal.getInstance().translateIntegerInAppToScreen(packageName, yOffset);
        if (this.mOverlapAnchor) {
            yOffset -= anchorHeight;
        }
        int[] drawingLocation = this.mTmpDrawingLocation;
        anchor.getLocationInWindow(drawingLocation);
        translateLocationInAppToScreen(packageName, drawingLocation);
        outParams.x = drawingLocation[INPUT_METHOD_FROM_FOCUSABLE] + xOffset;
        outParams.y = (drawingLocation[INPUT_METHOD_NEEDED] + anchorHeight) + yOffset;
        Rect displayFrame = new Rect();
        anchor.getWindowVisibleDisplayFrame(displayFrame);
        if (!(this.mNewDisplayFrameLeft == 0 && this.mNewDisplayFrameRight == 0)) {
            displayFrame.left = this.mNewDisplayFrameLeft;
            displayFrame.right = this.mNewDisplayFrameRight;
        }
        if (width == ANIMATION_STYLE_DEFAULT) {
            width = displayFrame.right - displayFrame.left;
        }
        if (height == ANIMATION_STYLE_DEFAULT) {
            height = displayFrame.bottom - displayFrame.top;
        }
        outParams.gravity = 51;
        outParams.width = width;
        outParams.height = height;
        int hgrav = Gravity.getAbsoluteGravity(gravity, anchor.getLayoutDirection()) & 7;
        if (hgrav == 5 && !SystemProperties.getRTLFlag()) {
            outParams.x -= width - anchorWidth;
        }
        int[] screenLocation = this.mTmpScreenLocation;
        anchor.getLocationOnScreen(screenLocation);
        translateLocationInAppToScreen(packageName, screenLocation);
        boolean fitsVertical = tryFitVertical(outParams, yOffset, height, anchorHeight, drawingLocation[INPUT_METHOD_NEEDED], screenLocation[INPUT_METHOD_NEEDED], displayFrame.top, displayFrame.bottom, false);
        LayoutParams layoutParams = outParams;
        int i = xOffset;
        int i2 = width;
        int i3 = anchorWidth;
        boolean fitsHorizontal = tryFitHorizontal(layoutParams, i, i2, i3, drawingLocation[INPUT_METHOD_FROM_FOCUSABLE], screenLocation[INPUT_METHOD_FROM_FOCUSABLE], displayFrame.left, displayFrame.right, false);
        if (!(fitsVertical && fitsHorizontal)) {
            int scrollX = anchor.getScrollX();
            int scrollY = anchor.getScrollY();
            Rect rect = new Rect(scrollX, scrollY, (scrollX + width) + xOffset, ((scrollY + height) + anchorHeight) + yOffset);
            if (this.mAllowScrollingAnchorParent && anchor.requestRectangleOnScreen(rect, true)) {
                anchor.getLocationInWindow(drawingLocation);
                translateLocationInAppToScreen(packageName, drawingLocation);
                outParams.x = drawingLocation[INPUT_METHOD_FROM_FOCUSABLE] + xOffset;
                outParams.y = (drawingLocation[INPUT_METHOD_NEEDED] + anchorHeight) + yOffset;
                if (hgrav == 5 && !SystemProperties.getRTLFlag()) {
                    outParams.x -= width - anchorWidth;
                }
            }
            tryFitVertical(outParams, yOffset, height, anchorHeight, drawingLocation[INPUT_METHOD_NEEDED], screenLocation[INPUT_METHOD_NEEDED], displayFrame.top, displayFrame.bottom, this.mClipToScreen);
            layoutParams = outParams;
            i = xOffset;
            i2 = width;
            i3 = anchorWidth;
            tryFitHorizontal(layoutParams, i, i2, i3, drawingLocation[INPUT_METHOD_FROM_FOCUSABLE], screenLocation[INPUT_METHOD_FROM_FOCUSABLE], displayFrame.left, displayFrame.right, this.mClipToScreen);
        }
        outParams.width = WindowManagerGlobal.getInstance().translateIntegerInScreenToApp(packageName, outParams.width);
        outParams.height = WindowManagerGlobal.getInstance().translateIntegerInScreenToApp(packageName, outParams.height);
        outParams.x = WindowManagerGlobal.getInstance().translateIntegerInScreenToApp(packageName, outParams.x);
        outParams.y = WindowManagerGlobal.getInstance().translateIntegerInScreenToApp(packageName, outParams.y);
        if (outParams.y < drawingLocation[INPUT_METHOD_NEEDED]) {
            return true;
        }
        return false;
    }

    private boolean tryFitVertical(LayoutParams outParams, int yOffset, int height, int anchorHeight, int drawingLocationY, int screenLocationY, int displayFrameTop, int displayFrameBottom, boolean allowResize) {
        int anchorTopInScreen = outParams.y + (screenLocationY - drawingLocationY);
        int spaceBelow = displayFrameBottom - anchorTopInScreen;
        if (anchorTopInScreen >= 0 && height <= spaceBelow) {
            return true;
        }
        if (height <= (anchorTopInScreen - anchorHeight) - displayFrameTop) {
            if (this.mOverlapAnchor) {
                yOffset += anchorHeight;
            }
            outParams.y = (drawingLocationY - height) + yOffset;
            if (outParams.y + height <= displayFrameBottom) {
                return true;
            }
        }
        if (positionInDisplayVertical(outParams, height, drawingLocationY, screenLocationY, displayFrameTop, displayFrameBottom, allowResize)) {
            return true;
        }
        return false;
    }

    private boolean positionInDisplayVertical(LayoutParams outParams, int height, int drawingLocationY, int screenLocationY, int displayFrameTop, int displayFrameBottom, boolean canResize) {
        boolean fitsInDisplay = true;
        int winOffsetY = screenLocationY - drawingLocationY;
        outParams.y += winOffsetY;
        outParams.height = height;
        int bottom = outParams.y + height;
        if (bottom > displayFrameBottom) {
            outParams.y -= bottom - displayFrameBottom;
        }
        if (outParams.y < displayFrameTop) {
            outParams.y = displayFrameTop;
            int displayFrameHeight = displayFrameBottom - displayFrameTop;
            if (!canResize || height <= displayFrameHeight) {
                fitsInDisplay = false;
            } else {
                outParams.height = displayFrameHeight;
            }
        }
        outParams.y -= winOffsetY;
        return fitsInDisplay;
    }

    private boolean tryFitHorizontal(LayoutParams outParams, int xOffset, int width, int anchorWidth, int drawingLocationX, int screenLocationX, int displayFrameLeft, int displayFrameRight, boolean allowResize) {
        int anchorLeftInScreen = outParams.x + (screenLocationX - drawingLocationX);
        int spaceRight = displayFrameRight - anchorLeftInScreen;
        if (anchorLeftInScreen >= 0 && width <= spaceRight) {
            return true;
        }
        if (positionInDisplayHorizontal(outParams, width, drawingLocationX, screenLocationX, displayFrameLeft, displayFrameRight, allowResize)) {
            return true;
        }
        return false;
    }

    private boolean positionInDisplayHorizontal(LayoutParams outParams, int width, int drawingLocationX, int screenLocationX, int displayFrameLeft, int displayFrameRight, boolean canResize) {
        boolean fitsInDisplay = true;
        int winOffsetX = screenLocationX - drawingLocationX;
        outParams.x += winOffsetX;
        int right = outParams.x + width;
        if (right > displayFrameRight) {
            outParams.x -= right - displayFrameRight;
        }
        if (outParams.x < displayFrameLeft) {
            outParams.x = displayFrameLeft;
            int displayFrameWidth = displayFrameRight - displayFrameLeft;
            if (!canResize || width <= displayFrameWidth) {
                fitsInDisplay = false;
            } else {
                outParams.width = displayFrameWidth;
            }
        }
        outParams.x -= winOffsetX;
        return fitsInDisplay;
    }

    public int getMaxAvailableHeight(View anchor) {
        return getMaxAvailableHeight(anchor, INPUT_METHOD_FROM_FOCUSABLE);
    }

    public int getMaxAvailableHeight(View anchor, int yOffset) {
        return getMaxAvailableHeight(anchor, yOffset, false);
    }

    public int getMaxAvailableHeight(View anchor, int yOffset, boolean ignoreBottomDecorations) {
        int distanceToBottom;
        Rect displayFrame = new Rect();
        if (ignoreBottomDecorations) {
            anchor.getWindowDisplayFrame(displayFrame);
        } else {
            anchor.getWindowVisibleDisplayFrame(displayFrame);
        }
        int[] anchorPos = this.mTmpDrawingLocation;
        anchor.getLocationOnScreen(anchorPos);
        int bottomEdge = displayFrame.bottom;
        if (this.mOverlapAnchor) {
            distanceToBottom = (bottomEdge - anchorPos[INPUT_METHOD_NEEDED]) - yOffset;
        } else {
            distanceToBottom = (bottomEdge - (anchorPos[INPUT_METHOD_NEEDED] + anchor.getHeight())) - yOffset;
        }
        int returnedHeight = Math.max(distanceToBottom, (anchorPos[INPUT_METHOD_NEEDED] - displayFrame.top) + yOffset);
        if (this.mBackground == null) {
            return returnedHeight;
        }
        this.mBackground.getPadding(this.mTempRect);
        return returnedHeight - (this.mTempRect.top + this.mTempRect.bottom);
    }

    public void dismiss() {
        if (isShowing() && !this.mIsTransitioningToDismiss) {
            ViewGroup viewGroup;
            PopupDecorView decorView = this.mDecorView;
            View contentView = this.mContentView;
            ViewParent contentParent = contentView.getParent();
            if (contentParent instanceof ViewGroup) {
                viewGroup = (ViewGroup) contentParent;
            } else {
                viewGroup = null;
            }
            decorView.cancelTransitions();
            this.mIsShowing = false;
            this.mIsTransitioningToDismiss = true;
            Transition exitTransition = this.mExitTransition;
            if (this.mIsAnchorRootAttached && exitTransition != null && decorView.isLaidOut()) {
                LayoutParams p = (LayoutParams) decorView.getLayoutParams();
                p.flags |= 16;
                p.flags |= 8;
                p.flags &= -131073;
                this.mWindowManager.updateViewLayout(decorView, p);
                View view = this.mAnchorRoot != null ? (View) this.mAnchorRoot.get() : null;
                exitTransition.setEpicenterCallback(new AnonymousClass3(getTransitionEpicenter()));
                decorView.startExitTransition(exitTransition, view, new AnonymousClass4(decorView, viewGroup, contentView));
            } else {
                dismissImmediate(decorView, viewGroup, contentView);
            }
            detachFromAnchor();
            if (this.mOnDismissListener != null) {
                this.mOnDismissListener.onDismiss();
            }
        }
    }

    private Rect getTransitionEpicenterSpinner() {
        View view = this.mAnchor != null ? (View) this.mAnchor.get() : null;
        View decor = this.mDecorView;
        if (view == null || decor == null) {
            return null;
        }
        int[] anchorLocation = view.getLocationOnScreen();
        int[] popupLocation = this.mDecorView.getLocationOnScreen();
        Rect bounds = new Rect(INPUT_METHOD_FROM_FOCUSABLE, INPUT_METHOD_FROM_FOCUSABLE, this.mDecorView.getWidth(), view.getHeight());
        bounds.offset(INPUT_METHOD_FROM_FOCUSABLE, anchorLocation[INPUT_METHOD_NEEDED] - popupLocation[INPUT_METHOD_NEEDED]);
        if (this.mEpicenterBounds != null) {
            int offsetX = bounds.left;
            int offsetY = bounds.top;
            bounds.set(this.mEpicenterBounds);
            bounds.offset(offsetX, offsetY);
        }
        return bounds;
    }

    private Rect getTransitionEpicenter() {
        if (this.mIsForSpinner) {
            return getTransitionEpicenterSpinner();
        }
        View view = this.mAnchor != null ? (View) this.mAnchor.get() : null;
        View decor = this.mDecorView;
        if (view == null || decor == null) {
            return null;
        }
        int[] anchorLocation = view.getLocationOnScreen();
        int[] popupLocation = this.mDecorView.getLocationOnScreen();
        Rect bounds = new Rect(INPUT_METHOD_FROM_FOCUSABLE, INPUT_METHOD_FROM_FOCUSABLE, view.getWidth(), view.getHeight());
        bounds.offset(anchorLocation[INPUT_METHOD_FROM_FOCUSABLE] - popupLocation[INPUT_METHOD_FROM_FOCUSABLE], anchorLocation[INPUT_METHOD_NEEDED] - popupLocation[INPUT_METHOD_NEEDED]);
        if (this.mEpicenterBounds != null) {
            int offsetX = bounds.left;
            int offsetY = bounds.top;
            bounds.set(this.mEpicenterBounds);
            bounds.offset(offsetX, offsetY);
        }
        return bounds;
    }

    private void dismissImmediate(View decorView, ViewGroup contentHolder, View contentView) {
        if (decorView.getParent() != null) {
            this.mWindowManager.removeViewImmediate(decorView);
        }
        if (contentHolder != null) {
            contentHolder.removeView(contentView);
        }
        this.mDecorView = null;
        this.mBackgroundView = null;
        this.mIsTransitioningToDismiss = false;
    }

    public void setOnDismissListener(OnDismissListener onDismissListener) {
        this.mOnDismissListener = onDismissListener;
    }

    public void update() {
        if (isShowing() && this.mContentView != null) {
            LayoutParams p = (LayoutParams) this.mDecorView.getLayoutParams();
            boolean update = false;
            int newAnim = computeAnimationResource();
            if (newAnim != p.windowAnimations) {
                p.windowAnimations = newAnim;
                update = true;
            }
            int newFlags = computeFlags(p.flags);
            if (newFlags != p.flags) {
                p.flags = newFlags;
                update = true;
            }
            int newGravity = computeGravity();
            if (newGravity != p.gravity) {
                p.gravity = newGravity;
                update = true;
            }
            if (update) {
                setLayoutDirectionFromAnchor();
                this.mWindowManager.updateViewLayout(this.mDecorView, p);
            }
        }
    }

    public void update(int width, int height) {
        LayoutParams p = (LayoutParams) this.mDecorView.getLayoutParams();
        update(p.x, p.y, width, height, false);
    }

    public void update(int x, int y, int width, int height) {
        update(x, y, width, height, false);
    }

    public void update(int x, int y, int width, int height, boolean force) {
        if (width >= 0) {
            this.mLastWidth = width;
            setWidth(width);
        }
        if (height >= 0) {
            this.mLastHeight = height;
            setHeight(height);
        }
        if (isShowing() && this.mContentView != null) {
            LayoutParams p = (LayoutParams) this.mDecorView.getLayoutParams();
            boolean update = force;
            int finalWidth = this.mWidthMode < 0 ? this.mWidthMode : this.mLastWidth;
            if (!(width == ANIMATION_STYLE_DEFAULT || p.width == finalWidth)) {
                this.mLastWidth = finalWidth;
                p.width = finalWidth;
                update = true;
            }
            int finalHeight = this.mHeightMode < 0 ? this.mHeightMode : this.mLastHeight;
            if (!(height == ANIMATION_STYLE_DEFAULT || p.height == finalHeight)) {
                this.mLastHeight = finalHeight;
                p.height = finalHeight;
                update = true;
            }
            if (p.x != x) {
                p.x = x;
                update = true;
            }
            if (p.y != y) {
                p.y = y;
                update = true;
            }
            int newAnim = computeAnimationResource();
            if (newAnim != p.windowAnimations) {
                p.windowAnimations = newAnim;
                update = true;
            }
            int newFlags = computeFlags(p.flags);
            if (newFlags != p.flags) {
                p.flags = newFlags;
                update = true;
            }
            int newGravity = computeGravity();
            if (newGravity != p.gravity) {
                p.gravity = newGravity;
                update = true;
            }
            int newAccessibilityIdOfAnchor = this.mAnchor != null ? ((View) this.mAnchor.get()).getAccessibilityViewId() : ANIMATION_STYLE_DEFAULT;
            if (newAccessibilityIdOfAnchor != p.accessibilityIdOfAnchor) {
                p.accessibilityIdOfAnchor = newAccessibilityIdOfAnchor;
                update = true;
            }
            if (update) {
                setLayoutDirectionFromAnchor();
                this.mWindowManager.updateViewLayout(this.mDecorView, p);
            }
        }
    }

    public void update(View anchor, int width, int height) {
        update(anchor, false, INPUT_METHOD_FROM_FOCUSABLE, INPUT_METHOD_FROM_FOCUSABLE, width, height);
    }

    public void update(View anchor, int xoff, int yoff, int width, int height) {
        update(anchor, true, xoff, yoff, width, height);
    }

    private void update(View anchor, boolean updateLocation, int xoff, int yoff, int width, int height) {
        if (isShowing() && this.mContentView != null) {
            int i;
            int i2;
            WeakReference<View> oldAnchor = this.mAnchor;
            int gravity = this.mAnchoredGravity;
            boolean needsUpdate = updateLocation && !(this.mAnchorXoff == xoff && this.mAnchorYoff == yoff);
            if (oldAnchor == null || oldAnchor.get() != anchor || (needsUpdate && !this.mIsDropdown)) {
                attachToAnchor(anchor, xoff, yoff, gravity);
            } else if (needsUpdate) {
                this.mAnchorXoff = xoff;
                this.mAnchorYoff = yoff;
            }
            LayoutParams p = (LayoutParams) this.mDecorView.getLayoutParams();
            int oldGravity = p.gravity;
            int oldWidth = p.width;
            int oldHeight = p.height;
            int oldX = p.x;
            int oldY = p.y;
            if (width < 0) {
                width = this.mWidth;
            }
            if (height < 0) {
                height = this.mHeight;
            }
            updateAboveAnchor(findDropDownPosition(anchor, p, this.mAnchorXoff, this.mAnchorYoff, width, height, gravity));
            boolean paramsChanged = (oldGravity == p.gravity && oldX == p.x && oldY == p.y && oldWidth == p.width) ? oldHeight != p.height : true;
            int i3 = p.x;
            int i4 = p.y;
            if (width < 0) {
                i = width;
            } else {
                i = p.width;
            }
            if (height < 0) {
                i2 = height;
            } else {
                i2 = p.height;
            }
            update(i3, i4, i, i2, paramsChanged);
        }
    }

    private void detachFromAnchor() {
        View anchor;
        View anchorRoot;
        if (this.mAnchor != null) {
            anchor = (View) this.mAnchor.get();
        } else {
            anchor = null;
        }
        if (anchor != null) {
            anchor.getViewTreeObserver().removeOnScrollChangedListener(this.mOnScrollChangedListener);
        }
        if (this.mAnchorRoot != null) {
            anchorRoot = (View) this.mAnchorRoot.get();
        } else {
            anchorRoot = null;
        }
        if (anchorRoot != null) {
            anchorRoot.removeOnAttachStateChangeListener(this.mOnAnchorRootDetachedListener);
        }
        this.mAnchor = null;
        this.mAnchorRoot = null;
        this.mIsAnchorRootAttached = false;
    }

    private void attachToAnchor(View anchor, int xoff, int yoff, int gravity) {
        detachFromAnchor();
        ViewTreeObserver vto = anchor.getViewTreeObserver();
        if (vto != null) {
            vto.addOnScrollChangedListener(this.mOnScrollChangedListener);
        }
        View anchorRoot = anchor.getRootView();
        anchorRoot.addOnAttachStateChangeListener(this.mOnAnchorRootDetachedListener);
        this.mAnchor = new WeakReference(anchor);
        this.mAnchorRoot = new WeakReference(anchorRoot);
        this.mIsAnchorRootAttached = anchorRoot.isAttachedToWindow();
        this.mAnchorXoff = xoff;
        this.mAnchorYoff = yoff;
        this.mAnchoredGravity = gravity;
    }

    private void translateLocationInAppToScreen(String packageName, int[] drawingLocation) {
        drawingLocation[INPUT_METHOD_FROM_FOCUSABLE] = WindowManagerGlobal.getInstance().translateIntegerInAppToScreen(packageName, drawingLocation[INPUT_METHOD_FROM_FOCUSABLE]);
        drawingLocation[INPUT_METHOD_NEEDED] = WindowManagerGlobal.getInstance().translateIntegerInAppToScreen(packageName, drawingLocation[INPUT_METHOD_NEEDED]);
    }
}
