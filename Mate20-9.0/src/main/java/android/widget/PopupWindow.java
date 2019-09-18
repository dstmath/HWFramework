package android.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.hwcontrol.HwWidgetFactory;
import android.os.IBinder;
import android.os.SystemProperties;
import android.rms.AppAssociate;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.transition.TransitionListenerAdapter;
import android.transition.TransitionManager;
import android.transition.TransitionSet;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.KeyboardShortcutGroup;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.WindowManagerGlobal;
import android.widget.FrameLayout;
import android.widget.PopupWindow;
import com.android.internal.R;
import java.lang.ref.WeakReference;
import java.util.List;

public class PopupWindow {
    /* access modifiers changed from: private */
    public static final int[] ABOVE_ANCHOR_STATE_SET = {16842922};
    private static final int ANIMATION_STYLE_DEFAULT = -1;
    private static final int DEFAULT_ANCHORED_GRAVITY = 8388659;
    public static final int INPUT_METHOD_FROM_FOCUSABLE = 0;
    public static final int INPUT_METHOD_NEEDED = 1;
    public static final int INPUT_METHOD_NOT_NEEDED = 2;
    /* access modifiers changed from: private */
    public boolean mAboveAnchor;
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
    /* access modifiers changed from: private */
    public float mDownX;
    /* access modifiers changed from: private */
    public float mDownY;
    private float mElevation;
    private Transition mEnterTransition;
    private Rect mEpicenterBounds;
    private Transition mExitTransition;
    private boolean mFocusable;
    private int mGravity;
    private int mHeight;
    private int mHeightMode;
    private boolean mIgnoreCheekPress;
    private int mInputMethodMode;
    /* access modifiers changed from: private */
    public boolean mIsAnchorRootAttached;
    private boolean mIsDropdown;
    private boolean mIsShowing;
    private boolean mIsTransitioningToDismiss;
    private int mLastHeight;
    private int mLastWidth;
    private boolean mLayoutInScreen;
    private boolean mLayoutInsetDecor;
    protected int mNewDisplayFrameLeft;
    protected int mNewDisplayFrameRight;
    private boolean mNotTouchModal;
    private final View.OnAttachStateChangeListener mOnAnchorDetachedListener;
    private final View.OnAttachStateChangeListener mOnAnchorRootDetachedListener;
    private OnDismissListener mOnDismissListener;
    private final View.OnLayoutChangeListener mOnLayoutChangeListener;
    private final ViewTreeObserver.OnScrollChangedListener mOnScrollChangedListener;
    private boolean mOutsideTouchable;
    private boolean mOverlapAnchor;
    /* access modifiers changed from: private */
    public WeakReference<View> mParentRootView;
    private boolean mPopupViewInitialLayoutDirectionInherited;
    /* access modifiers changed from: private */
    public int mSlop;
    private int mSoftInputMode;
    private int mSplitTouchEnabled;
    private final Rect mTempRect;
    private CharSequence mTitle;
    private final int[] mTmpAppLocation;
    private final int[] mTmpDrawingLocation;
    private final int[] mTmpScreenLocation;
    /* access modifiers changed from: private */
    public View.OnTouchListener mTouchInterceptor;
    private boolean mTouchable;
    private int mWidth;
    private int mWidthMode;
    private int mWindowLayoutType;
    private WindowManager mWindowManager;

    public interface OnDismissListener {
        void onDismiss();
    }

    private class PopupBackgroundView extends FrameLayout {
        public PopupBackgroundView(Context context) {
            super(context);
        }

        /* access modifiers changed from: protected */
        public int[] onCreateDrawableState(int extraSpace) {
            if (!PopupWindow.this.mAboveAnchor) {
                return super.onCreateDrawableState(extraSpace);
            }
            int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
            View.mergeDrawableStates(drawableState, PopupWindow.ABOVE_ANCHOR_STATE_SET);
            return drawableState;
        }
    }

    private class PopupDecorView extends FrameLayout {
        /* access modifiers changed from: private */
        public Runnable mCleanupAfterExit;
        private final View.OnAttachStateChangeListener mOnAnchorRootDetachedListener = new View.OnAttachStateChangeListener() {
            public void onViewAttachedToWindow(View v) {
            }

            public void onViewDetachedFromWindow(View v) {
                v.removeOnAttachStateChangeListener(this);
                if (PopupDecorView.this.isAttachedToWindow()) {
                    TransitionManager.endTransitions(PopupDecorView.this);
                }
            }
        };

        public PopupDecorView(Context context) {
            super(context);
        }

        public boolean dispatchKeyEvent(KeyEvent event) {
            if (event.getKeyCode() != 4) {
                return super.dispatchKeyEvent(event);
            }
            if (getKeyDispatcherState() == null) {
                return super.dispatchKeyEvent(event);
            }
            if (event.getAction() == 0 && event.getRepeatCount() == 0) {
                KeyEvent.DispatcherState state = getKeyDispatcherState();
                if (state != null) {
                    state.startTracking(event, this);
                }
                return true;
            }
            if (event.getAction() == 1) {
                KeyEvent.DispatcherState state2 = getKeyDispatcherState();
                if (state2 != null && state2.isTracking(event) && !event.isCanceled()) {
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
                float unused = PopupWindow.this.mDownX = (float) x;
                float unused2 = PopupWindow.this.mDownY = (float) y;
            }
            if (event.getAction() == 0 && (x < 0 || x >= getWidth() || y < 0 || y >= getHeight())) {
                return true;
            }
            if (event.getAction() == 1 && (x < 0 || x >= getWidth() || y < 0 || y >= getHeight())) {
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
                final Transition enterTransition = transition.clone();
                observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    public void onGlobalLayout() {
                        ViewTreeObserver observer = PopupDecorView.this.getViewTreeObserver();
                        if (observer != null) {
                            observer.removeOnGlobalLayoutListener(this);
                        }
                        final Rect epicenter = PopupWindow.this.getTransitionEpicenter();
                        enterTransition.setEpicenterCallback(new Transition.EpicenterCallback() {
                            public Rect onGetEpicenter(Transition transition) {
                                return epicenter;
                            }
                        });
                        PopupDecorView.this.startEnterTransition(enterTransition);
                    }
                });
            }
        }

        /* access modifiers changed from: private */
        public void startEnterTransition(Transition enterTransition) {
            int count = getChildCount();
            for (int i = 0; i < count; i++) {
                View child = getChildAt(i);
                enterTransition.addTarget(child);
                child.setTransitionVisibility(4);
            }
            TransitionManager.beginDelayedTransition(this, enterTransition);
            for (int i2 = 0; i2 < count; i2++) {
                getChildAt(i2).setTransitionVisibility(0);
            }
        }

        public void startExitTransition(Transition transition, View anchorRoot, final Rect epicenter, Transition.TransitionListener listener) {
            if (transition != null) {
                if (anchorRoot != null) {
                    anchorRoot.addOnAttachStateChangeListener(this.mOnAnchorRootDetachedListener);
                }
                this.mCleanupAfterExit = new Runnable(listener, transition, anchorRoot) {
                    private final /* synthetic */ Transition.TransitionListener f$1;
                    private final /* synthetic */ Transition f$2;
                    private final /* synthetic */ View f$3;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                        this.f$3 = r4;
                    }

                    public final void run() {
                        PopupWindow.PopupDecorView.lambda$startExitTransition$0(PopupWindow.PopupDecorView.this, this.f$1, this.f$2, this.f$3);
                    }
                };
                Transition exitTransition = transition.clone();
                exitTransition.addListener(new TransitionListenerAdapter() {
                    public void onTransitionEnd(Transition t) {
                        t.removeListener(this);
                        if (PopupDecorView.this.mCleanupAfterExit != null) {
                            PopupDecorView.this.mCleanupAfterExit.run();
                        }
                    }
                });
                exitTransition.setEpicenterCallback(new Transition.EpicenterCallback() {
                    public Rect onGetEpicenter(Transition transition) {
                        return epicenter;
                    }
                });
                int count = getChildCount();
                for (int i = 0; i < count; i++) {
                    exitTransition.addTarget(getChildAt(i));
                }
                TransitionManager.beginDelayedTransition(this, exitTransition);
                for (int i2 = 0; i2 < count; i2++) {
                    getChildAt(i2).setVisibility(4);
                }
            }
        }

        public static /* synthetic */ void lambda$startExitTransition$0(PopupDecorView popupDecorView, Transition.TransitionListener listener, Transition transition, View anchorRoot) {
            listener.onTransitionEnd(transition);
            if (anchorRoot != null) {
                anchorRoot.removeOnAttachStateChangeListener(popupDecorView.mOnAnchorRootDetachedListener);
            }
            popupDecorView.mCleanupAfterExit = null;
        }

        public void cancelTransitions() {
            TransitionManager.endTransitions(this);
            if (this.mCleanupAfterExit != null) {
                this.mCleanupAfterExit.run();
            }
        }

        public void requestKeyboardShortcuts(List<KeyboardShortcutGroup> list, int deviceId) {
            if (PopupWindow.this.mParentRootView != null) {
                View parentRoot = (View) PopupWindow.this.mParentRootView.get();
                if (parentRoot != null) {
                    parentRoot.requestKeyboardShortcuts(list, deviceId);
                }
            }
        }
    }

    public PopupWindow(Context context) {
        this(context, (AttributeSet) null);
    }

    public PopupWindow(Context context, AttributeSet attrs) {
        this(context, attrs, 16842870);
    }

    public PopupWindow(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public PopupWindow(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        Transition exitTransition;
        this.mTmpDrawingLocation = new int[2];
        this.mTmpScreenLocation = new int[2];
        this.mTmpAppLocation = new int[2];
        this.mTempRect = new Rect();
        this.mInputMethodMode = 0;
        this.mSoftInputMode = 1;
        this.mTouchable = true;
        this.mOutsideTouchable = false;
        this.mClippingEnabled = true;
        this.mSplitTouchEnabled = -1;
        this.mAllowScrollingAnchorParent = true;
        this.mLayoutInsetDecor = false;
        this.mAttachedInDecor = true;
        this.mAttachedInDecorSet = false;
        this.mWidth = -2;
        this.mHeight = -2;
        this.mWindowLayoutType = 1000;
        this.mIgnoreCheekPress = false;
        this.mAnimationStyle = -1;
        this.mGravity = 0;
        this.mSlop = 32;
        this.mOnAnchorDetachedListener = new View.OnAttachStateChangeListener() {
            public void onViewAttachedToWindow(View v) {
                PopupWindow.this.alignToAnchor();
            }

            public void onViewDetachedFromWindow(View v) {
            }
        };
        this.mOnAnchorRootDetachedListener = new View.OnAttachStateChangeListener() {
            public void onViewAttachedToWindow(View v) {
            }

            public void onViewDetachedFromWindow(View v) {
                boolean unused = PopupWindow.this.mIsAnchorRootAttached = false;
            }
        };
        this.mOnScrollChangedListener = new ViewTreeObserver.OnScrollChangedListener() {
            public final void onScrollChanged() {
                PopupWindow.this.alignToAnchor();
            }
        };
        this.mOnLayoutChangeListener = new View.OnLayoutChangeListener() {
            public final void onLayoutChange(View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
                PopupWindow.this.alignToAnchor();
            }
        };
        this.mContext = context;
        this.mWindowManager = (WindowManager) context.getSystemService(AppAssociate.ASSOC_WINDOW);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PopupWindow, defStyleAttr, defStyleRes);
        Drawable bg = a.getDrawable(0);
        this.mElevation = a.getDimension(3, 0.0f);
        this.mOverlapAnchor = a.getBoolean(2, false);
        if (a.hasValueOrEmpty(1)) {
            int animStyle = a.getResourceId(1, 0);
            if (animStyle == 16974588) {
                this.mAnimationStyle = -1;
            } else {
                this.mAnimationStyle = animStyle;
            }
        } else {
            this.mAnimationStyle = -1;
        }
        Transition enterTransition = getTransition(a.getResourceId(4, 0));
        if (a.hasValueOrEmpty(5)) {
            exitTransition = getTransition(a.getResourceId(5, 0));
        } else {
            exitTransition = enterTransition == null ? null : enterTransition.clone();
        }
        a.recycle();
        setEnterTransition(enterTransition);
        setExitTransition(exitTransition);
        setBackgroundDrawable(bg);
        this.mSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    public PopupWindow() {
        this((View) null, 0, 0);
    }

    public PopupWindow(View contentView) {
        this(contentView, 0, 0);
    }

    public PopupWindow(int width, int height) {
        this((View) null, width, height);
    }

    public PopupWindow(View contentView, int width, int height) {
        this(contentView, width, height, false);
    }

    public PopupWindow(View contentView, int width, int height, boolean focusable) {
        this.mTmpDrawingLocation = new int[2];
        this.mTmpScreenLocation = new int[2];
        this.mTmpAppLocation = new int[2];
        this.mTempRect = new Rect();
        this.mInputMethodMode = 0;
        this.mSoftInputMode = 1;
        this.mTouchable = true;
        this.mOutsideTouchable = false;
        this.mClippingEnabled = true;
        this.mSplitTouchEnabled = -1;
        this.mAllowScrollingAnchorParent = true;
        this.mLayoutInsetDecor = false;
        this.mAttachedInDecor = true;
        this.mAttachedInDecorSet = false;
        this.mWidth = -2;
        this.mHeight = -2;
        this.mWindowLayoutType = 1000;
        this.mIgnoreCheekPress = false;
        this.mAnimationStyle = -1;
        this.mGravity = 0;
        this.mSlop = 32;
        this.mOnAnchorDetachedListener = new View.OnAttachStateChangeListener() {
            public void onViewAttachedToWindow(View v) {
                PopupWindow.this.alignToAnchor();
            }

            public void onViewDetachedFromWindow(View v) {
            }
        };
        this.mOnAnchorRootDetachedListener = new View.OnAttachStateChangeListener() {
            public void onViewAttachedToWindow(View v) {
            }

            public void onViewDetachedFromWindow(View v) {
                boolean unused = PopupWindow.this.mIsAnchorRootAttached = false;
            }
        };
        this.mOnScrollChangedListener = new ViewTreeObserver.OnScrollChangedListener() {
            public final void onScrollChanged() {
                PopupWindow.this.alignToAnchor();
            }
        };
        this.mOnLayoutChangeListener = new View.OnLayoutChangeListener() {
            public final void onLayoutChange(View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
                PopupWindow.this.alignToAnchor();
            }
        };
        if (contentView != null) {
            this.mContext = contentView.getContext();
            this.mWindowManager = (WindowManager) this.mContext.getSystemService(AppAssociate.ASSOC_WINDOW);
        }
        setContentView(contentView);
        setWidth(width);
        setHeight(height);
        setFocusable(focusable);
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
        if (!(resId == 0 || resId == 17760256)) {
            Transition transition = TransitionInflater.from(this.mContext).inflateTransition(resId);
            if (transition != null) {
                if (!((transition instanceof TransitionSet) && ((TransitionSet) transition).getTransitionCount() == 0)) {
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
            StateListDrawable stateList = (StateListDrawable) this.mBackground;
            int aboveAnchorStateIndex = stateList.getStateDrawableIndex(ABOVE_ANCHOR_STATE_SET);
            int count = stateList.getStateCount();
            int belowAnchorStateIndex = -1;
            int i = 0;
            while (true) {
                if (i >= count) {
                    break;
                } else if (i != aboveAnchorStateIndex) {
                    belowAnchorStateIndex = i;
                    break;
                } else {
                    i++;
                }
            }
            if (aboveAnchorStateIndex == -1 || belowAnchorStateIndex == -1) {
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
                this.mWindowManager = (WindowManager) this.mContext.getSystemService(AppAssociate.ASSOC_WINDOW);
            }
            if (this.mContext != null && !this.mAttachedInDecorSet) {
                setAttachedInDecor(this.mContext.getApplicationInfo().targetSdkVersion >= 22);
            }
        }
    }

    public void setTouchInterceptor(View.OnTouchListener l) {
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

    /* access modifiers changed from: package-private */
    public void setAllowScrollingAnchorParent(boolean enabled) {
        this.mAllowScrollingAnchorParent = enabled;
    }

    /* access modifiers changed from: protected */
    public final boolean getAllowScrollingAnchorParent() {
        return this.mAllowScrollingAnchorParent;
    }

    public boolean isSplitTouchEnabled() {
        boolean z = false;
        if (this.mSplitTouchEnabled >= 0 || this.mContext == null) {
            if (this.mSplitTouchEnabled == 1) {
                z = true;
            }
            return z;
        }
        if (this.mContext.getApplicationInfo().targetSdkVersion >= 11) {
            z = true;
        }
        return z;
    }

    public void setSplitTouchEnabled(boolean enabled) {
        this.mSplitTouchEnabled = enabled;
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

    /* access modifiers changed from: protected */
    public final boolean isLayoutInsetDecor() {
        return this.mLayoutInsetDecor;
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

    /* access modifiers changed from: protected */
    public final void setShowing(boolean isShowing) {
        this.mIsShowing = isShowing;
    }

    /* access modifiers changed from: protected */
    public final void setDropDown(boolean isDropDown) {
        this.mIsDropdown = isDropDown;
    }

    /* access modifiers changed from: protected */
    public final void setTransitioningToDismiss(boolean transitioningToDismiss) {
        this.mIsTransitioningToDismiss = transitioningToDismiss;
    }

    /* access modifiers changed from: protected */
    public final boolean isTransitioningToDismiss() {
        return this.mIsTransitioningToDismiss;
    }

    public void showAtLocation(View parent, int gravity, int x, int y) {
        this.mParentRootView = new WeakReference<>(parent.getRootView());
        showAtLocation(parent.getWindowToken(), gravity, x, y);
    }

    public void showAtLocation(IBinder token, int gravity, int x, int y) {
        if (!isShowing() && this.mContentView != null) {
            TransitionManager.endTransitions(this.mDecorView);
            detachFromAnchor();
            this.mIsShowing = true;
            this.mIsDropdown = false;
            this.mGravity = gravity;
            WindowManager.LayoutParams p = createPopupLayoutParams(token);
            preparePopup(p);
            p.x = x;
            p.y = y;
            invokePopup(p);
        }
    }

    public void showAsDropDown(View anchor) {
        showAsDropDown(anchor, 0, 0);
    }

    public void showAsDropDown(View anchor, int xoff, int yoff) {
        showAsDropDown(anchor, xoff, yoff, DEFAULT_ANCHORED_GRAVITY);
    }

    public void showAsDropDown(View anchor, int xoff, int yoff, int gravity) {
        if (!isShowing() && hasContentView()) {
            TransitionManager.endTransitions(this.mDecorView);
            attachToAnchor(anchor, xoff, yoff, gravity);
            this.mIsShowing = true;
            this.mIsDropdown = true;
            WindowManager.LayoutParams p = createPopupLayoutParams(anchor.getApplicationWindowToken());
            preparePopup(p);
            updateAboveAnchor(findDropDownPosition(anchor, p, xoff, yoff, p.width, p.height, gravity, this.mAllowScrollingAnchorParent));
            p.accessibilityIdOfAnchor = anchor != null ? (long) anchor.getAccessibilityViewId() : -1;
            p.windowAnimations = computeAnimationResource();
            invokePopup(p);
        }
    }

    /* access modifiers changed from: protected */
    public final void updateAboveAnchor(boolean aboveAnchor) {
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

    private void preparePopup(WindowManager.LayoutParams p) {
        if (this.mContentView == null || this.mContext == null || this.mWindowManager == null) {
            throw new IllegalStateException("You must specify a valid content view by calling setContentView() before attempting to show the popup.");
        }
        if (p.accessibilityTitle == null) {
            p.accessibilityTitle = this.mContext.getString(17040943);
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
        boolean z = true;
        this.mDecorView.setIsRootNamespace(true);
        this.mBackgroundView.setElevation(this.mElevation);
        p.setSurfaceInsets(this.mBackgroundView, true, true);
        if (this.mContentView.getRawLayoutDirection() != 2) {
            z = false;
        }
        this.mPopupViewInitialLayoutDirectionInherited = z;
    }

    private PopupBackgroundView createBackgroundView(View contentView) {
        int height;
        ViewGroup.LayoutParams layoutParams = this.mContentView.getLayoutParams();
        if (layoutParams == null || layoutParams.height != -2) {
            height = -1;
        } else {
            height = -2;
        }
        PopupBackgroundView backgroundView = new PopupBackgroundView(this.mContext);
        backgroundView.addView(contentView, (ViewGroup.LayoutParams) new FrameLayout.LayoutParams(-1, height));
        return backgroundView;
    }

    private PopupDecorView createDecorView(View contentView) {
        int height;
        ViewGroup.LayoutParams layoutParams = this.mContentView.getLayoutParams();
        if (layoutParams == null || layoutParams.height != -2) {
            height = -1;
        } else {
            height = -2;
        }
        PopupDecorView decorView = new PopupDecorView(this.mContext);
        decorView.addView(contentView, -1, height);
        decorView.setClipChildren(false);
        decorView.setClipToPadding(false);
        return decorView;
    }

    private void invokePopup(WindowManager.LayoutParams p) {
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
        int gravity = this.mGravity == 0 ? DEFAULT_ANCHORED_GRAVITY : this.mGravity;
        if (!this.mIsDropdown) {
            return gravity;
        }
        if (this.mClipToScreen || this.mClippingEnabled) {
            return gravity | 268435456;
        }
        return gravity;
    }

    /* access modifiers changed from: protected */
    public final WindowManager.LayoutParams createPopupLayoutParams(IBinder token) {
        WindowManager.LayoutParams p = new WindowManager.LayoutParams();
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
            int i = this.mHeightMode;
            this.mLastHeight = i;
            p.height = i;
        } else {
            int i2 = this.mHeight;
            this.mLastHeight = i2;
            p.height = i2;
        }
        if (this.mWidthMode < 0) {
            int i3 = this.mWidthMode;
            this.mLastWidth = i3;
            p.width = i3;
        } else {
            int i4 = this.mWidth;
            this.mLastWidth = i4;
            p.width = i4;
        }
        p.privateFlags = 98304;
        if (this.mTitle != null) {
            p.setTitle(this.mTitle);
        } else {
            p.setTitle("PopupWindow:" + Integer.toHexString(hashCode()));
        }
        return p;
    }

    private int computeFlags(int curFlags) {
        int curFlags2 = curFlags & -8815129;
        if (this.mIgnoreCheekPress) {
            curFlags2 |= 32768;
        }
        if (!this.mFocusable) {
            curFlags2 |= 8;
            if (this.mInputMethodMode == 1) {
                curFlags2 |= 131072;
            }
        } else if (this.mInputMethodMode == 2) {
            curFlags2 |= 131072;
        }
        if (!this.mTouchable) {
            curFlags2 |= 16;
        }
        if (this.mOutsideTouchable) {
            curFlags2 |= 262144;
        }
        if (!this.mClippingEnabled || this.mClipToScreen) {
            curFlags2 |= 512;
        }
        if (isSplitTouchEnabled()) {
            curFlags2 |= 8388608;
        }
        if (this.mLayoutInScreen) {
            curFlags2 |= 256;
        }
        if (this.mLayoutInsetDecor) {
            curFlags2 |= 65536;
        }
        if (this.mNotTouchModal) {
            curFlags2 |= 32;
        }
        if (this.mAttachedInDecor) {
            return curFlags2 | 1073741824;
        }
        return curFlags2;
    }

    private int computeAnimationResource() {
        int i;
        if (this.mAnimationStyle != -1) {
            return this.mAnimationStyle;
        }
        if (!this.mIsDropdown) {
            return 0;
        }
        if (this.mContext == null || !HwWidgetFactory.isHwTheme(this.mContext) || !HwWidgetFactory.isEmuiSuperLite()) {
            if (this.mAboveAnchor) {
                i = 16974576;
            } else {
                i = 16974575;
            }
        } else if (this.mAboveAnchor) {
            i = com.android.hwext.internal.R.style.HwAnimation_lite_DropDownUpLite;
        } else {
            i = com.android.hwext.internal.R.style.HwAnimation_lite_DropDownDownLite;
        }
        return i;
    }

    public void setPopupLocation(int start, int end) {
        this.mNewDisplayFrameLeft = start;
        this.mNewDisplayFrameRight = end;
    }

    /* JADX WARNING: type inference failed for: r32v0, types: [boolean] */
    /* JADX WARNING: type inference failed for: r32v2 */
    /* JADX WARNING: type inference failed for: r32v3 */
    /* access modifiers changed from: protected */
    public boolean findDropDownPosition(View anchor, WindowManager.LayoutParams outParams, int xOffset, int yOffset, int width, int height, int gravity, boolean allowScroll) {
        int yOffset2;
        int height2;
        ? r32;
        char c;
        int hgrav;
        View view = anchor;
        WindowManager.LayoutParams layoutParams = outParams;
        int anchorHeight = anchor.getHeight();
        int anchorWidth = anchor.getWidth();
        if (this.mOverlapAnchor) {
            yOffset2 = yOffset - anchorHeight;
        } else {
            yOffset2 = yOffset;
        }
        int[] appScreenLocation = this.mTmpAppLocation;
        View appRootView = getAppRootView(anchor);
        appRootView.getLocationOnScreen(appScreenLocation);
        int[] screenLocation = this.mTmpScreenLocation;
        view.getLocationOnScreen(screenLocation);
        int[] drawingLocation = this.mTmpDrawingLocation;
        drawingLocation[0] = screenLocation[0] - appScreenLocation[0];
        drawingLocation[1] = screenLocation[1] - appScreenLocation[1];
        layoutParams.x = drawingLocation[0] + xOffset;
        layoutParams.y = drawingLocation[1] + anchorHeight + yOffset2;
        Rect displayFrame = new Rect();
        appRootView.getWindowVisibleDisplayFrame(displayFrame);
        if (!(this.mNewDisplayFrameLeft == 0 && this.mNewDisplayFrameRight == 0)) {
            displayFrame.left = this.mNewDisplayFrameLeft;
            displayFrame.right = this.mNewDisplayFrameRight;
        }
        int i = width;
        if (i == -1) {
            i = displayFrame.right - displayFrame.left;
        }
        int width2 = i;
        int i2 = height;
        if (i2 == -1) {
            height2 = displayFrame.bottom - displayFrame.top;
        } else {
            height2 = i2;
        }
        layoutParams.gravity = computeGravity();
        layoutParams.width = width2;
        layoutParams.height = height2;
        int hgrav2 = Gravity.getAbsoluteGravity(gravity, anchor.getLayoutDirection()) & 7;
        int[] appScreenLocation2 = appScreenLocation;
        if (hgrav2 == 5 && !SystemProperties.getRTLFlag()) {
            layoutParams.x -= width2 - anchorWidth;
        }
        int i3 = drawingLocation[1];
        int i4 = screenLocation[1];
        int[] screenLocation2 = screenLocation;
        View appRootView2 = appRootView;
        int hgrav3 = hgrav2;
        WindowManager.LayoutParams layoutParams2 = layoutParams;
        int height3 = height2;
        int width3 = width2;
        Rect displayFrame2 = displayFrame;
        int[] drawingLocation2 = drawingLocation;
        int i5 = i4;
        View view2 = appRootView2;
        int anchorHeight2 = anchorHeight;
        int[] screenLocation3 = screenLocation2;
        boolean fitsVertical = tryFitVertical(layoutParams2, yOffset2, height3, anchorHeight, i3, i5, displayFrame.top, displayFrame.bottom, false);
        boolean fitsHorizontal = tryFitHorizontal(layoutParams2, xOffset, width3, anchorWidth, drawingLocation2[0], screenLocation3[0], displayFrame2.left, displayFrame2.right, false);
        if (!fitsVertical || !fitsHorizontal) {
            Rect displayFrame3 = displayFrame2;
            View view3 = anchor;
            int scrollX = anchor.getScrollX();
            int scrollY = anchor.getScrollY();
            Rect r = new Rect(scrollX, scrollY, scrollX + width3 + xOffset, scrollY + height3 + anchorHeight2 + yOffset2);
            if (allowScroll) {
                c = 1;
                if (view3.requestRectangleOnScreen(r, true)) {
                    view3.getLocationOnScreen(screenLocation3);
                    drawingLocation2[0] = screenLocation3[0] - appScreenLocation2[0];
                    drawingLocation2[1] = screenLocation3[1] - appScreenLocation2[1];
                    layoutParams.x = drawingLocation2[0] + xOffset;
                    layoutParams.y = drawingLocation2[1] + anchorHeight2 + yOffset2;
                    hgrav = hgrav3;
                    if (hgrav == 5 && !SystemProperties.getRTLFlag()) {
                        layoutParams.x -= width3 - anchorWidth;
                    }
                } else {
                    hgrav = hgrav3;
                }
            } else {
                hgrav = hgrav3;
                c = 1;
            }
            int i6 = drawingLocation2[c];
            int i7 = screenLocation3[c];
            int i8 = displayFrame3.top;
            WindowManager.LayoutParams layoutParams3 = layoutParams;
            int i9 = hgrav;
            r32 = c;
            int i10 = i6;
            Rect rect = r;
            int i11 = i7;
            int i12 = scrollY;
            int scrollY2 = i8;
            int i13 = scrollX;
            Rect displayFrame4 = displayFrame3;
            tryFitVertical(layoutParams3, yOffset2, height3, anchorHeight2, i10, i11, scrollY2, displayFrame3.bottom, this.mClipToScreen);
            tryFitHorizontal(layoutParams3, xOffset, width3, anchorWidth, drawingLocation2[0], screenLocation3[0], displayFrame4.left, displayFrame4.right, this.mClipToScreen);
        } else {
            int i14 = hgrav3;
            r32 = 1;
        }
        if (layoutParams.y < drawingLocation2[r32]) {
            return r32;
        }
        return false;
    }

    private boolean tryFitVertical(WindowManager.LayoutParams outParams, int yOffset, int height, int anchorHeight, int drawingLocationY, int screenLocationY, int displayFrameTop, int displayFrameBottom, boolean allowResize) {
        PopupWindow popupWindow;
        int yOffset2;
        WindowManager.LayoutParams layoutParams = outParams;
        int i = height;
        int spaceAbove = displayFrameBottom;
        int anchorTopInScreen = layoutParams.y + (screenLocationY - drawingLocationY);
        int spaceBelow = spaceAbove - anchorTopInScreen;
        if (anchorTopInScreen >= 0 && i <= spaceBelow) {
            return true;
        }
        int spaceAbove2 = (anchorTopInScreen - anchorHeight) - displayFrameTop;
        if (i <= spaceAbove2) {
            popupWindow = this;
            if (popupWindow.mOverlapAnchor) {
                yOffset2 = yOffset + anchorHeight;
            } else {
                yOffset2 = yOffset;
            }
            layoutParams.y = (drawingLocationY - i) + yOffset2;
            if (layoutParams.y + i <= spaceAbove) {
                return true;
            }
            int i2 = yOffset2;
        } else {
            popupWindow = this;
            int i3 = yOffset;
        }
        int i4 = spaceAbove2;
        int i5 = spaceBelow;
        if (popupWindow.positionInDisplayVertical(layoutParams, i, drawingLocationY, screenLocationY, displayFrameTop, spaceAbove, allowResize)) {
            return true;
        }
        return false;
    }

    private boolean positionInDisplayVertical(WindowManager.LayoutParams outParams, int height, int drawingLocationY, int screenLocationY, int displayFrameTop, int displayFrameBottom, boolean canResize) {
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

    private boolean tryFitHorizontal(WindowManager.LayoutParams outParams, int xOffset, int width, int anchorWidth, int drawingLocationX, int screenLocationX, int displayFrameLeft, int displayFrameRight, boolean allowResize) {
        int i;
        WindowManager.LayoutParams layoutParams = outParams;
        int anchorLeftInScreen = layoutParams.x + (screenLocationX - drawingLocationX);
        int spaceRight = displayFrameRight - anchorLeftInScreen;
        if (anchorLeftInScreen >= 0) {
            i = width;
            if (i <= spaceRight) {
                return true;
            }
        } else {
            i = width;
        }
        if (positionInDisplayHorizontal(layoutParams, i, drawingLocationX, screenLocationX, displayFrameLeft, displayFrameRight, allowResize)) {
            return true;
        }
        return false;
    }

    private boolean positionInDisplayHorizontal(WindowManager.LayoutParams outParams, int width, int drawingLocationX, int screenLocationX, int displayFrameLeft, int displayFrameRight, boolean canResize) {
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
        return getMaxAvailableHeight(anchor, 0);
    }

    public int getMaxAvailableHeight(View anchor, int yOffset) {
        return getMaxAvailableHeight(anchor, yOffset, false);
    }

    public int getMaxAvailableHeight(View anchor, int yOffset, boolean ignoreBottomDecorations) {
        Rect displayFrame;
        int distanceToBottom;
        Rect visibleDisplayFrame = new Rect();
        getAppRootView(anchor).getWindowVisibleDisplayFrame(visibleDisplayFrame);
        if (ignoreBottomDecorations) {
            displayFrame = new Rect();
            anchor.getWindowDisplayFrame(displayFrame);
            displayFrame.top = visibleDisplayFrame.top;
            displayFrame.right = visibleDisplayFrame.right;
            displayFrame.left = visibleDisplayFrame.left;
        } else {
            displayFrame = visibleDisplayFrame;
        }
        int[] anchorPos = this.mTmpDrawingLocation;
        anchor.getLocationOnScreen(anchorPos);
        int bottomEdge = displayFrame.bottom;
        if (this.mOverlapAnchor) {
            distanceToBottom = (bottomEdge - anchorPos[1]) - yOffset;
        } else {
            distanceToBottom = (bottomEdge - (anchorPos[1] + anchor.getHeight())) - yOffset;
        }
        int returnedHeight = Math.max(distanceToBottom, (anchorPos[1] - displayFrame.top) + yOffset);
        if (this.mBackground == null) {
            return returnedHeight;
        }
        this.mBackground.getPadding(this.mTempRect);
        return returnedHeight - (this.mTempRect.top + this.mTempRect.bottom);
    }

    public void dismiss() {
        final ViewGroup contentHolder;
        if (isShowing() && !isTransitioningToDismiss()) {
            final PopupDecorView decorView = this.mDecorView;
            final View contentView = this.mContentView;
            ViewParent contentParent = contentView.getParent();
            View anchorRoot = null;
            if (contentParent instanceof ViewGroup) {
                contentHolder = (ViewGroup) contentParent;
            } else {
                contentHolder = null;
            }
            decorView.cancelTransitions();
            this.mIsShowing = false;
            this.mIsTransitioningToDismiss = true;
            Transition exitTransition = this.mExitTransition;
            if (exitTransition == null || !decorView.isLaidOut() || (!this.mIsAnchorRootAttached && this.mAnchorRoot != null)) {
                dismissImmediate(decorView, contentHolder, contentView);
            } else {
                WindowManager.LayoutParams p = (WindowManager.LayoutParams) decorView.getLayoutParams();
                p.flags |= 16;
                p.flags |= 8;
                p.flags &= -131073;
                this.mWindowManager.updateViewLayout(decorView, p);
                if (this.mAnchorRoot != null) {
                    anchorRoot = (View) this.mAnchorRoot.get();
                }
                decorView.startExitTransition(exitTransition, anchorRoot, getTransitionEpicenter(), new TransitionListenerAdapter() {
                    public void onTransitionEnd(Transition transition) {
                        PopupWindow.this.dismissImmediate(decorView, contentHolder, contentView);
                    }
                });
            }
            detachFromAnchor();
            if (this.mOnDismissListener != null) {
                this.mOnDismissListener.onDismiss();
            }
        }
    }

    /* access modifiers changed from: protected */
    public final Rect getTransitionEpicenter() {
        View anchor = this.mAnchor != null ? (View) this.mAnchor.get() : null;
        View decor = this.mDecorView;
        if (anchor == null || decor == null) {
            return null;
        }
        int[] anchorLocation = anchor.getLocationOnScreen();
        int[] popupLocation = this.mDecorView.getLocationOnScreen();
        Rect bounds = new Rect(0, 0, anchor.getWidth(), anchor.getHeight());
        bounds.offset(anchorLocation[0] - popupLocation[0], anchorLocation[1] - popupLocation[1]);
        if (this.mEpicenterBounds != null) {
            int offsetX = bounds.left;
            int offsetY = bounds.top;
            bounds.set(this.mEpicenterBounds);
            bounds.offset(offsetX, offsetY);
        }
        return bounds;
    }

    /* access modifiers changed from: private */
    public void dismissImmediate(View decorView, ViewGroup contentHolder, View contentView) {
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

    /* access modifiers changed from: protected */
    public final OnDismissListener getOnDismissListener() {
        return this.mOnDismissListener;
    }

    public void update() {
        if (isShowing() && hasContentView()) {
            WindowManager.LayoutParams p = getDecorViewLayoutParams();
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
                update(this.mAnchor != null ? (View) this.mAnchor.get() : null, p);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void update(View anchor, WindowManager.LayoutParams params) {
        setLayoutDirectionFromAnchor();
        this.mWindowManager.updateViewLayout(this.mDecorView, params);
    }

    public void update(int width, int height) {
        WindowManager.LayoutParams p = getDecorViewLayoutParams();
        update(p.x, p.y, width, height, false);
    }

    public void update(int x, int y, int width, int height) {
        update(x, y, width, height, false);
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r14v5, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r12v3, resolved type: android.view.View} */
    /* JADX WARNING: Multi-variable type inference failed */
    public void update(int x, int y, int width, int height, boolean force) {
        int i = x;
        int i2 = y;
        int i3 = width;
        int i4 = height;
        if (i3 >= 0) {
            this.mLastWidth = i3;
            setWidth(i3);
        }
        if (i4 >= 0) {
            this.mLastHeight = i4;
            setHeight(i4);
        }
        if (isShowing() && hasContentView()) {
            WindowManager.LayoutParams p = getDecorViewLayoutParams();
            boolean update = force;
            int finalWidth = this.mWidthMode < 0 ? this.mWidthMode : this.mLastWidth;
            if (!(i3 == -1 || p.width == finalWidth)) {
                this.mLastWidth = finalWidth;
                p.width = finalWidth;
                update = true;
            }
            int finalHeight = this.mHeightMode < 0 ? this.mHeightMode : this.mLastHeight;
            if (!(i4 == -1 || p.height == finalHeight)) {
                this.mLastHeight = finalHeight;
                p.height = finalHeight;
                update = true;
            }
            if (p.x != i) {
                p.x = i;
                update = true;
            }
            if (p.y != i2) {
                p.y = i2;
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
            View anchor = null;
            int newAccessibilityIdOfAnchor = -1;
            if (!(this.mAnchor == null || this.mAnchor.get() == null)) {
                anchor = this.mAnchor.get();
                newAccessibilityIdOfAnchor = anchor.getAccessibilityViewId();
            }
            if (((long) newAccessibilityIdOfAnchor) != p.accessibilityIdOfAnchor) {
                p.accessibilityIdOfAnchor = (long) newAccessibilityIdOfAnchor;
                update = true;
            }
            if (update) {
                update(anchor, p);
            }
        }
    }

    /* access modifiers changed from: protected */
    public boolean hasContentView() {
        return this.mContentView != null;
    }

    /* access modifiers changed from: protected */
    public boolean hasDecorView() {
        return this.mDecorView != null;
    }

    /* access modifiers changed from: protected */
    public WindowManager.LayoutParams getDecorViewLayoutParams() {
        return (WindowManager.LayoutParams) this.mDecorView.getLayoutParams();
    }

    public void update(View anchor, int width, int height) {
        update(anchor, false, 0, 0, width, height);
    }

    public void update(View anchor, int xoff, int yoff, int width, int height) {
        update(anchor, true, xoff, yoff, width, height);
    }

    private void update(View anchor, boolean updateLocation, int xoff, int yoff, int width, int height) {
        int width2;
        int height2;
        View view = anchor;
        int i = xoff;
        int i2 = yoff;
        if (isShowing() && hasContentView()) {
            WeakReference<View> oldAnchor = this.mAnchor;
            int gravity = this.mAnchoredGravity;
            boolean needsUpdate = updateLocation && !(this.mAnchorXoff == i && this.mAnchorYoff == i2);
            if (oldAnchor == null || oldAnchor.get() != view || (needsUpdate && !this.mIsDropdown)) {
                attachToAnchor(view, i, i2, gravity);
            } else if (needsUpdate) {
                this.mAnchorXoff = i;
                this.mAnchorYoff = i2;
            }
            WindowManager.LayoutParams p = getDecorViewLayoutParams();
            int oldGravity = p.gravity;
            int oldWidth = p.width;
            int oldHeight = p.height;
            int oldX = p.x;
            int oldY = p.y;
            if (width < 0) {
                width2 = this.mWidth;
            } else {
                width2 = width;
            }
            if (height < 0) {
                height2 = this.mHeight;
            } else {
                height2 = height;
            }
            int oldY2 = oldY;
            int oldX2 = oldX;
            int oldHeight2 = oldHeight;
            int oldWidth2 = oldWidth;
            WeakReference<View> weakReference = oldAnchor;
            int oldGravity2 = oldGravity;
            int oldGravity3 = gravity;
            int i3 = gravity;
            WindowManager.LayoutParams p2 = p;
            updateAboveAnchor(findDropDownPosition(view, p, this.mAnchorXoff, this.mAnchorYoff, width2, height2, oldGravity3, this.mAllowScrollingAnchorParent));
            update(p2.x, p2.y, width2 < 0 ? width2 : p2.width, height2 < 0 ? height2 : p2.height, (oldGravity2 == p2.gravity && oldX2 == p2.x && oldY2 == p2.y && oldWidth2 == p2.width && oldHeight2 == p2.height) ? false : true);
        }
    }

    /* access modifiers changed from: protected */
    public void detachFromAnchor() {
        View anchor = getAnchor();
        if (anchor != null) {
            anchor.getViewTreeObserver().removeOnScrollChangedListener(this.mOnScrollChangedListener);
            anchor.removeOnAttachStateChangeListener(this.mOnAnchorDetachedListener);
        }
        View anchorRoot = this.mAnchorRoot != null ? (View) this.mAnchorRoot.get() : null;
        if (anchorRoot != null) {
            anchorRoot.removeOnAttachStateChangeListener(this.mOnAnchorRootDetachedListener);
            anchorRoot.removeOnLayoutChangeListener(this.mOnLayoutChangeListener);
        }
        this.mAnchor = null;
        this.mAnchorRoot = null;
        this.mIsAnchorRootAttached = false;
    }

    /* access modifiers changed from: protected */
    public void attachToAnchor(View anchor, int xoff, int yoff, int gravity) {
        detachFromAnchor();
        ViewTreeObserver vto = anchor.getViewTreeObserver();
        if (vto != null) {
            vto.addOnScrollChangedListener(this.mOnScrollChangedListener);
        }
        anchor.addOnAttachStateChangeListener(this.mOnAnchorDetachedListener);
        View anchorRoot = anchor.getRootView();
        anchorRoot.addOnAttachStateChangeListener(this.mOnAnchorRootDetachedListener);
        anchorRoot.addOnLayoutChangeListener(this.mOnLayoutChangeListener);
        this.mAnchor = new WeakReference<>(anchor);
        this.mAnchorRoot = new WeakReference<>(anchorRoot);
        this.mIsAnchorRootAttached = anchorRoot.isAttachedToWindow();
        this.mParentRootView = this.mAnchorRoot;
        this.mAnchorXoff = xoff;
        this.mAnchorYoff = yoff;
        this.mAnchoredGravity = gravity;
    }

    /* access modifiers changed from: protected */
    public View getAnchor() {
        if (this.mAnchor != null) {
            return (View) this.mAnchor.get();
        }
        return null;
    }

    /* access modifiers changed from: private */
    public void alignToAnchor() {
        View anchor = this.mAnchor != null ? (View) this.mAnchor.get() : null;
        if (anchor != null && anchor.isAttachedToWindow() && hasDecorView()) {
            WindowManager.LayoutParams p = getDecorViewLayoutParams();
            updateAboveAnchor(findDropDownPosition(anchor, p, this.mAnchorXoff, this.mAnchorYoff, p.width, p.height, this.mAnchoredGravity, false));
            update(p.x, p.y, -1, -1, true);
        }
    }

    private View getAppRootView(View anchor) {
        View appWindowView = WindowManagerGlobal.getInstance().getWindowView(anchor.getApplicationWindowToken());
        if (appWindowView != null) {
            return appWindowView;
        }
        return anchor.getRootView();
    }

    public void setTitle(CharSequence title) {
        this.mTitle = title;
    }
}
