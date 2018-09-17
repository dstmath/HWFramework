package android.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.IBinder;
import android.os.SystemProperties;
import android.rms.AppAssociate;
import android.transition.Transition;
import android.transition.Transition.EpicenterCallback;
import android.transition.Transition.TransitionListener;
import android.transition.TransitionInflater;
import android.transition.TransitionListenerAdapter;
import android.transition.TransitionManager;
import android.transition.TransitionSet;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.KeyEvent.DispatcherState;
import android.view.KeyboardShortcutGroup;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnAttachStateChangeListener;
import android.view.View.OnLayoutChangeListener;
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
import com.android.internal.R;
import java.lang.ref.WeakReference;
import java.util.List;

public class PopupWindow {
    private static final int[] ABOVE_ANCHOR_STATE_SET = new int[]{R.attr.state_above_anchor};
    private static final int ANIMATION_STYLE_DEFAULT = -1;
    private static final int DEFAULT_ANCHORED_GRAVITY = 8388659;
    public static final int INPUT_METHOD_FROM_FOCUSABLE = 0;
    public static final int INPUT_METHOD_NEEDED = 1;
    public static final int INPUT_METHOD_NOT_NEEDED = 2;
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
    private int mGravity;
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
    private final OnAttachStateChangeListener mOnAnchorDetachedListener;
    private final OnAttachStateChangeListener mOnAnchorRootDetachedListener;
    private OnDismissListener mOnDismissListener;
    private final OnLayoutChangeListener mOnLayoutChangeListener;
    private final OnScrollChangedListener mOnScrollChangedListener;
    private boolean mOutsideTouchable;
    private boolean mOverlapAnchor;
    private WeakReference<View> mParentRootView;
    private boolean mPopupViewInitialLayoutDirectionInherited;
    private int mSlop;
    private int mSoftInputMode;
    private int mSplitTouchEnabled;
    private final Rect mTempRect;
    private final int[] mTmpAppLocation;
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

    private class PopupBackgroundView extends FrameLayout {
        public PopupBackgroundView(Context context) {
            super(context);
        }

        protected int[] onCreateDrawableState(int extraSpace) {
            if (!PopupWindow.this.mAboveAnchor) {
                return super.onCreateDrawableState(extraSpace);
            }
            int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
            View.mergeDrawableStates(drawableState, PopupWindow.ABOVE_ANCHOR_STATE_SET);
            return drawableState;
        }
    }

    private class PopupDecorView extends FrameLayout {
        private Runnable mCleanupAfterExit;
        private final OnAttachStateChangeListener mOnAnchorRootDetachedListener = new OnAttachStateChangeListener() {
            public void onViewAttachedToWindow(View v) {
            }

            public void onViewDetachedFromWindow(View v) {
                v.removeOnAttachStateChangeListener(this);
                TransitionManager.endTransitions(PopupDecorView.this);
            }
        };

        public PopupDecorView(Context context) {
            super(context);
        }

        public boolean dispatchKeyEvent(KeyEvent event) {
            if (event.getKeyCode() != 4) {
                return super.-wrap7(event);
            }
            if (getKeyDispatcherState() == null) {
                return super.-wrap7(event);
            }
            DispatcherState state;
            if (event.getAction() == 0 && event.getRepeatCount() == 0) {
                state = getKeyDispatcherState();
                if (state != null) {
                    state.startTracking(event, this);
                }
                return true;
            }
            if (event.getAction() == 1) {
                state = getKeyDispatcherState();
                if (!(state == null || !state.isTracking(event) || (event.isCanceled() ^ 1) == 0)) {
                    PopupWindow.this.dismiss();
                    return true;
                }
            }
            return super.-wrap7(event);
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
                observer.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
                    public void onGlobalLayout() {
                        ViewTreeObserver observer = PopupDecorView.this.getViewTreeObserver();
                        if (observer != null) {
                            observer.removeOnGlobalLayoutListener(this);
                        }
                        final Rect epicenter = PopupWindow.this.getTransitionEpicenter();
                        enterTransition.setEpicenterCallback(new EpicenterCallback() {
                            public Rect onGetEpicenter(Transition transition) {
                                return epicenter;
                            }
                        });
                        PopupDecorView.this.startEnterTransition(enterTransition);
                    }
                });
            }
        }

        private void startEnterTransition(Transition enterTransition) {
            int i;
            int count = getChildCount();
            for (i = 0; i < count; i++) {
                View child = getChildAt(i);
                enterTransition.addTarget(child);
                child.setVisibility(4);
            }
            TransitionManager.beginDelayedTransition(this, enterTransition);
            for (i = 0; i < count; i++) {
                getChildAt(i).setVisibility(0);
            }
        }

        public void startExitTransition(Transition transition, View anchorRoot, final Rect epicenter, TransitionListener listener) {
            if (transition != null) {
                int i;
                if (anchorRoot != null) {
                    anchorRoot.addOnAttachStateChangeListener(this.mOnAnchorRootDetachedListener);
                }
                this.mCleanupAfterExit = new android.widget.-$Lambda$ISuHLqeK-K4pmesAfzlFglc3xF4.AnonymousClass4(this, listener, transition, anchorRoot);
                Transition exitTransition = transition.clone();
                exitTransition.addListener(new TransitionListenerAdapter() {
                    public void onTransitionEnd(Transition t) {
                        t.removeListener(this);
                        if (PopupDecorView.this.mCleanupAfterExit != null) {
                            PopupDecorView.this.mCleanupAfterExit.run();
                        }
                    }
                });
                exitTransition.setEpicenterCallback(new EpicenterCallback() {
                    public Rect onGetEpicenter(Transition transition) {
                        return epicenter;
                    }
                });
                int count = getChildCount();
                for (i = 0; i < count; i++) {
                    exitTransition.addTarget(getChildAt(i));
                }
                TransitionManager.beginDelayedTransition(this, exitTransition);
                for (i = 0; i < count; i++) {
                    getChildAt(i).setVisibility(4);
                }
            }
        }

        /* synthetic */ void lambda$-android_widget_PopupWindow$PopupDecorView_97859(TransitionListener listener, Transition transition, View anchorRoot) {
            listener.onTransitionEnd(transition);
            if (anchorRoot != null) {
                anchorRoot.removeOnAttachStateChangeListener(this.mOnAnchorRootDetachedListener);
            }
            this.mCleanupAfterExit = null;
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
        this(context, null);
    }

    public PopupWindow(Context context, AttributeSet attrs) {
        this(context, attrs, (int) R.attr.popupWindowStyle);
    }

    public PopupWindow(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public PopupWindow(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
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
        this.mOnAnchorDetachedListener = new OnAttachStateChangeListener() {
            public void onViewAttachedToWindow(View v) {
                PopupWindow.this.alignToAnchor();
            }

            public void onViewDetachedFromWindow(View v) {
            }
        };
        this.mOnAnchorRootDetachedListener = new OnAttachStateChangeListener() {
            public void onViewAttachedToWindow(View v) {
            }

            public void onViewDetachedFromWindow(View v) {
                PopupWindow.this.mIsAnchorRootAttached = false;
            }
        };
        this.mOnScrollChangedListener = new android.widget.-$Lambda$ISuHLqeK-K4pmesAfzlFglc3xF4.AnonymousClass2(this);
        this.mOnLayoutChangeListener = new -$Lambda$ISuHLqeK-K4pmesAfzlFglc3xF4(this);
        this.mIsForSpinner = false;
        this.mContext = context;
        this.mWindowManager = (WindowManager) context.getSystemService(AppAssociate.ASSOC_WINDOW);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PopupWindow, defStyleAttr, defStyleRes);
        Drawable bg = a.getDrawable(0);
        this.mElevation = a.getDimension(3, 0.0f);
        this.mOverlapAnchor = a.getBoolean(2, false);
        if (a.hasValueOrEmpty(1)) {
            int animStyle = a.getResourceId(1, 0);
            if (animStyle == R.style.Animation_PopupWindow) {
                this.mAnimationStyle = -1;
            } else {
                this.mAnimationStyle = animStyle;
            }
        } else {
            this.mAnimationStyle = -1;
        }
        Transition enterTransition = getTransition(a.getResourceId(4, 0));
        Transition exitTransition = a.hasValueOrEmpty(5) ? getTransition(a.getResourceId(5, 0)) : enterTransition == null ? null : enterTransition.clone();
        a.recycle();
        setEnterTransition(enterTransition);
        setExitTransition(exitTransition);
        setBackgroundDrawable(bg);
        this.mSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    public PopupWindow() {
        this(null, 0, 0);
    }

    public PopupWindow(View contentView) {
        this(contentView, 0, 0);
    }

    public PopupWindow(int width, int height) {
        this(null, width, height);
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
        this.mOnAnchorDetachedListener = /* anonymous class already generated */;
        this.mOnAnchorRootDetachedListener = /* anonymous class already generated */;
        this.mOnScrollChangedListener = new android.widget.-$Lambda$ISuHLqeK-K4pmesAfzlFglc3xF4.AnonymousClass3(this);
        this.mOnLayoutChangeListener = new android.widget.-$Lambda$ISuHLqeK-K4pmesAfzlFglc3xF4.AnonymousClass1(this);
        this.mIsForSpinner = false;
        if (contentView != null) {
            this.mContext = contentView.getContext();
            this.mWindowManager = (WindowManager) this.mContext.getSystemService(AppAssociate.ASSOC_WINDOW);
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
        if (!(resId == 0 || resId == R.transition.no_transition)) {
            Transition transition = TransitionInflater.from(this.mContext).inflateTransition(resId);
            if (transition != null) {
                boolean isEmpty = transition instanceof TransitionSet ? ((TransitionSet) transition).getTransitionCount() == 0 : false;
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
            int belowAnchorStateIndex = -1;
            for (int i = 0; i < count; i++) {
                if (i != aboveAnchorStateIndex) {
                    belowAnchorStateIndex = i;
                    break;
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
            if (!(this.mContext == null || (this.mAttachedInDecorSet ^ 1) == 0)) {
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

    protected final boolean getAllowScrollingAnchorParent() {
        return this.mAllowScrollingAnchorParent;
    }

    public boolean isSplitTouchEnabled() {
        boolean z = true;
        if (this.mSplitTouchEnabled >= 0 || this.mContext == null) {
            if (this.mSplitTouchEnabled != 1) {
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
        this.mSplitTouchEnabled = enabled ? 1 : 0;
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

    protected final boolean isLayoutInsetDecor() {
        return this.mLayoutInsetDecor;
    }

    public void setWindowLayoutType(int layoutType) {
        this.mWindowLayoutType = layoutType;
    }

    public int getWindowLayoutType() {
        return this.mWindowLayoutType;
    }

    public void setTouchModal(boolean touchModal) {
        this.mNotTouchModal = touchModal ^ 1;
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

    protected final void setShowing(boolean isShowing) {
        this.mIsShowing = isShowing;
    }

    protected final void setDropDown(boolean isDropDown) {
        this.mIsDropdown = isDropDown;
    }

    protected final void setTransitioningToDismiss(boolean transitioningToDismiss) {
        this.mIsTransitioningToDismiss = transitioningToDismiss;
    }

    protected final boolean isTransitioningToDismiss() {
        return this.mIsTransitioningToDismiss;
    }

    public void showAtLocation(View parent, int gravity, int x, int y) {
        this.mParentRootView = new WeakReference(parent.getRootView());
        showAtLocation(parent.getWindowToken(), gravity, x, y);
    }

    public void showAtLocation(IBinder token, int gravity, int x, int y) {
        if (!isShowing() && this.mContentView != null) {
            TransitionManager.endTransitions(this.mDecorView);
            detachFromAnchor();
            this.mIsShowing = true;
            this.mIsDropdown = false;
            this.mGravity = gravity;
            LayoutParams p = createPopupLayoutParams(token);
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
        if (!isShowing() && (hasContentView() ^ 1) == 0) {
            TransitionManager.endTransitions(this.mDecorView);
            attachToAnchor(anchor, xoff, yoff, gravity);
            this.mIsShowing = true;
            this.mIsDropdown = true;
            LayoutParams p = createPopupLayoutParams(anchor.getApplicationWindowToken());
            preparePopup(p);
            updateAboveAnchor(findDropDownPosition(anchor, p, xoff, yoff, p.width, p.height, gravity, this.mAllowScrollingAnchorParent));
            p.accessibilityIdOfAnchor = anchor != null ? anchor.getAccessibilityViewId() : -1;
            invokePopup(p);
        }
    }

    protected final void updateAboveAnchor(boolean aboveAnchor) {
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
        int gravity = this.mGravity == 0 ? DEFAULT_ANCHORED_GRAVITY : this.mGravity;
        if (!this.mIsDropdown) {
            return gravity;
        }
        if (this.mClipToScreen || this.mClippingEnabled) {
            return gravity | 268435456;
        }
        return gravity;
    }

    protected final LayoutParams createPopupLayoutParams(IBinder token) {
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
            curFlags |= 32768;
        }
        if (!this.mFocusable) {
            curFlags |= 8;
            if (this.mInputMethodMode == 1) {
                curFlags |= 131072;
            }
        } else if (this.mInputMethodMode == 2) {
            curFlags |= 131072;
        }
        if (!this.mTouchable) {
            curFlags |= 16;
        }
        if (this.mOutsideTouchable) {
            curFlags |= 262144;
        }
        if (!this.mClippingEnabled || this.mClipToScreen) {
            curFlags |= 512;
        }
        if (isSplitTouchEnabled()) {
            curFlags |= 8388608;
        }
        if (this.mLayoutInScreen) {
            curFlags |= 256;
        }
        if (this.mLayoutInsetDecor) {
            curFlags |= 65536;
        }
        if (this.mNotTouchModal) {
            curFlags |= 32;
        }
        if (this.mAttachedInDecor) {
            return curFlags | 1073741824;
        }
        return curFlags;
    }

    private int computeAnimationResource() {
        if (this.mAnimationStyle != -1) {
            return this.mAnimationStyle;
        }
        if (!this.mIsDropdown) {
            return 0;
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

    protected final boolean findDropDownPosition(View anchor, LayoutParams outParams, int xOffset, int yOffset, int width, int height, int gravity, boolean allowScroll) {
        int anchorHeight = anchor.getHeight();
        int anchorWidth = anchor.getWidth();
        if (this.mOverlapAnchor) {
            yOffset -= anchorHeight;
        }
        int[] appScreenLocation = this.mTmpAppLocation;
        View appRootView = getAppRootView(anchor);
        appRootView.getLocationOnScreen(appScreenLocation);
        int[] screenLocation = this.mTmpScreenLocation;
        anchor.getLocationOnScreen(screenLocation);
        int[] drawingLocation = this.mTmpDrawingLocation;
        drawingLocation[0] = screenLocation[0] - appScreenLocation[0];
        drawingLocation[1] = screenLocation[1] - appScreenLocation[1];
        outParams.x = drawingLocation[0] + xOffset;
        outParams.y = (drawingLocation[1] + anchorHeight) + yOffset;
        Rect displayFrame = new Rect();
        appRootView.getWindowVisibleDisplayFrame(displayFrame);
        if (!(this.mNewDisplayFrameLeft == 0 && this.mNewDisplayFrameRight == 0)) {
            displayFrame.left = this.mNewDisplayFrameLeft;
            displayFrame.right = this.mNewDisplayFrameRight;
        }
        if (width == -1) {
            width = displayFrame.right - displayFrame.left;
        }
        if (height == -1) {
            height = displayFrame.bottom - displayFrame.top;
        }
        outParams.gravity = computeGravity();
        outParams.width = width;
        outParams.height = height;
        int hgrav = Gravity.getAbsoluteGravity(gravity, anchor.getLayoutDirection()) & 7;
        if (hgrav == 5 && (SystemProperties.getRTLFlag() ^ 1) != 0) {
            outParams.x -= width - anchorWidth;
        }
        boolean fitsVertical = tryFitVertical(outParams, yOffset, height, anchorHeight, drawingLocation[1], screenLocation[1], displayFrame.top, displayFrame.bottom, false);
        boolean fitsHorizontal = tryFitHorizontal(outParams, xOffset, width, anchorWidth, drawingLocation[0], screenLocation[0], displayFrame.left, displayFrame.right, false);
        if (!(fitsVertical && (fitsHorizontal ^ 1) == 0)) {
            int scrollX = anchor.getScrollX();
            int scrollY = anchor.getScrollY();
            Rect rect = new Rect(scrollX, scrollY, (scrollX + width) + xOffset, ((scrollY + height) + anchorHeight) + yOffset);
            if (allowScroll && anchor.requestRectangleOnScreen(rect, true)) {
                anchor.getLocationOnScreen(screenLocation);
                drawingLocation[0] = screenLocation[0] - appScreenLocation[0];
                drawingLocation[1] = screenLocation[1] - appScreenLocation[1];
                outParams.x = drawingLocation[0] + xOffset;
                outParams.y = (drawingLocation[1] + anchorHeight) + yOffset;
                if (hgrav == 5 && (SystemProperties.getRTLFlag() ^ 1) != 0) {
                    outParams.x -= width - anchorWidth;
                }
            }
            tryFitVertical(outParams, yOffset, height, anchorHeight, drawingLocation[1], screenLocation[1], displayFrame.top, displayFrame.bottom, this.mClipToScreen);
            tryFitHorizontal(outParams, xOffset, width, anchorWidth, drawingLocation[0], screenLocation[0], displayFrame.left, displayFrame.right, this.mClipToScreen);
        }
        return outParams.y < drawingLocation[1];
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
        if (isShowing() && !isTransitioningToDismiss()) {
            ViewGroup contentHolder;
            final PopupDecorView decorView = this.mDecorView;
            final View contentView = this.mContentView;
            ViewParent contentParent = contentView.getParent();
            if (contentParent instanceof ViewGroup) {
                contentHolder = (ViewGroup) contentParent;
            } else {
                contentHolder = null;
            }
            decorView.cancelTransitions();
            this.mIsShowing = false;
            this.mIsTransitioningToDismiss = true;
            Transition exitTransition = this.mExitTransition;
            if (exitTransition != null && decorView.isLaidOut() && (this.mIsAnchorRootAttached || this.mAnchorRoot == null)) {
                LayoutParams p = (LayoutParams) decorView.getLayoutParams();
                p.flags |= 16;
                p.flags |= 8;
                p.flags &= -131073;
                this.mWindowManager.updateViewLayout(decorView, p);
                decorView.startExitTransition(exitTransition, this.mAnchorRoot != null ? (View) this.mAnchorRoot.get() : null, getTransitionEpicenter(), new TransitionListenerAdapter() {
                    public void onTransitionEnd(Transition transition) {
                        PopupWindow.this.dismissImmediate(decorView, contentHolder, contentView);
                    }
                });
            } else {
                dismissImmediate(decorView, contentHolder, contentView);
            }
            detachFromAnchor();
            if (this.mOnDismissListener != null) {
                this.mOnDismissListener.onDismiss();
            }
        }
    }

    private Rect getTransitionEpicenterSpinner() {
        View anchor = this.mAnchor != null ? (View) this.mAnchor.get() : null;
        View decor = this.mDecorView;
        if (anchor == null || decor == null) {
            return null;
        }
        int[] anchorLocation = anchor.getLocationOnScreen();
        int[] popupLocation = this.mDecorView.getLocationOnScreen();
        Rect bounds = new Rect(0, 0, this.mDecorView.getWidth(), anchor.getHeight());
        bounds.offset(0, anchorLocation[1] - popupLocation[1]);
        if (this.mEpicenterBounds != null) {
            int offsetX = bounds.left;
            int offsetY = bounds.top;
            bounds.set(this.mEpicenterBounds);
            bounds.offset(offsetX, offsetY);
        }
        return bounds;
    }

    protected final Rect getTransitionEpicenter() {
        if (this.mIsForSpinner) {
            return getTransitionEpicenterSpinner();
        }
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

    protected final OnDismissListener getOnDismissListener() {
        return this.mOnDismissListener;
    }

    public void update() {
        View view = null;
        if (isShowing() && (hasContentView() ^ 1) == 0) {
            LayoutParams p = getDecorViewLayoutParams();
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
                if (this.mAnchor != null) {
                    view = (View) this.mAnchor.get();
                }
                update(view, p);
            }
        }
    }

    protected void update(View anchor, LayoutParams params) {
        setLayoutDirectionFromAnchor();
        this.mWindowManager.updateViewLayout(this.mDecorView, params);
    }

    public void update(int width, int height) {
        LayoutParams p = getDecorViewLayoutParams();
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
        if (isShowing() && (hasContentView() ^ 1) == 0) {
            LayoutParams p = getDecorViewLayoutParams();
            boolean update = force;
            int finalWidth = this.mWidthMode < 0 ? this.mWidthMode : this.mLastWidth;
            if (!(width == -1 || p.width == finalWidth)) {
                this.mLastWidth = finalWidth;
                p.width = finalWidth;
                update = true;
            }
            int finalHeight = this.mHeightMode < 0 ? this.mHeightMode : this.mLastHeight;
            if (!(height == -1 || p.height == finalHeight)) {
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
            View anchor = null;
            int newAccessibilityIdOfAnchor = -1;
            if (!(this.mAnchor == null || this.mAnchor.get() == null)) {
                anchor = (View) this.mAnchor.get();
                newAccessibilityIdOfAnchor = anchor.getAccessibilityViewId();
            }
            if (newAccessibilityIdOfAnchor != p.accessibilityIdOfAnchor) {
                p.accessibilityIdOfAnchor = newAccessibilityIdOfAnchor;
                update = true;
            }
            if (update) {
                update(anchor, p);
            }
        }
    }

    protected boolean hasContentView() {
        return this.mContentView != null;
    }

    protected boolean hasDecorView() {
        return this.mDecorView != null;
    }

    protected LayoutParams getDecorViewLayoutParams() {
        return (LayoutParams) this.mDecorView.getLayoutParams();
    }

    public void update(View anchor, int width, int height) {
        update(anchor, false, 0, 0, width, height);
    }

    public void update(View anchor, int xoff, int yoff, int width, int height) {
        update(anchor, true, xoff, yoff, width, height);
    }

    private void update(View anchor, boolean updateLocation, int xoff, int yoff, int width, int height) {
        if (isShowing() && (hasContentView() ^ 1) == 0) {
            WeakReference<View> oldAnchor = this.mAnchor;
            int gravity = this.mAnchoredGravity;
            boolean needsUpdate = updateLocation && !(this.mAnchorXoff == xoff && this.mAnchorYoff == yoff);
            if (oldAnchor == null || oldAnchor.get() != anchor || (needsUpdate && (this.mIsDropdown ^ 1) != 0)) {
                attachToAnchor(anchor, xoff, yoff, gravity);
            } else if (needsUpdate) {
                this.mAnchorXoff = xoff;
                this.mAnchorYoff = yoff;
            }
            LayoutParams p = getDecorViewLayoutParams();
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
            updateAboveAnchor(findDropDownPosition(anchor, p, this.mAnchorXoff, this.mAnchorYoff, width, height, gravity, this.mAllowScrollingAnchorParent));
            boolean paramsChanged = (oldGravity == p.gravity && oldX == p.x && oldY == p.y && oldWidth == p.width) ? oldHeight != p.height : true;
            update(p.x, p.y, width < 0 ? width : p.width, height < 0 ? height : p.height, paramsChanged);
        }
    }

    protected final void detachFromAnchor() {
        View anchor = this.mAnchor != null ? (View) this.mAnchor.get() : null;
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

    protected final void attachToAnchor(View anchor, int xoff, int yoff, int gravity) {
        detachFromAnchor();
        ViewTreeObserver vto = anchor.getViewTreeObserver();
        if (vto != null) {
            vto.addOnScrollChangedListener(this.mOnScrollChangedListener);
        }
        anchor.addOnAttachStateChangeListener(this.mOnAnchorDetachedListener);
        View anchorRoot = anchor.getRootView();
        anchorRoot.addOnAttachStateChangeListener(this.mOnAnchorRootDetachedListener);
        anchorRoot.addOnLayoutChangeListener(this.mOnLayoutChangeListener);
        this.mAnchor = new WeakReference(anchor);
        this.mAnchorRoot = new WeakReference(anchorRoot);
        this.mIsAnchorRootAttached = anchorRoot.isAttachedToWindow();
        this.mParentRootView = this.mAnchorRoot;
        this.mAnchorXoff = xoff;
        this.mAnchorYoff = yoff;
        this.mAnchoredGravity = gravity;
    }

    private void alignToAnchor() {
        View anchor = this.mAnchor != null ? (View) this.mAnchor.get() : null;
        if (anchor != null && anchor.isAttachedToWindow() && hasDecorView()) {
            LayoutParams p = getDecorViewLayoutParams();
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
}
