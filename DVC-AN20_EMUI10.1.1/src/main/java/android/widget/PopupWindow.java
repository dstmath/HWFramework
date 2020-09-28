package android.widget;

import android.annotation.UnsupportedAppUsage;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.hwcontrol.HwWidgetFactory;
import android.os.IBinder;
import android.os.SystemProperties;
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
    private static final int[] ABOVE_ANCHOR_STATE_SET = {16842922};
    private static final int ANIMATION_STYLE_DEFAULT = -1;
    private static final int DEFAULT_ANCHORED_GRAVITY = 8388659;
    private static final int DEVICE_TYPE_TELEVISION = 2;
    public static final int INPUT_METHOD_FROM_FOCUSABLE = 0;
    public static final int INPUT_METHOD_NEEDED = 1;
    public static final int INPUT_METHOD_NOT_NEEDED = 2;
    @UnsupportedAppUsage
    private boolean mAboveAnchor;
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    private Drawable mAboveAnchorBackgroundDrawable;
    private boolean mAllowScrollingAnchorParent;
    @UnsupportedAppUsage
    private WeakReference<View> mAnchor;
    private WeakReference<View> mAnchorRoot;
    private int mAnchorXoff;
    private int mAnchorYoff;
    private int mAnchoredGravity;
    @UnsupportedAppUsage
    private int mAnimationStyle;
    private boolean mAttachedInDecor;
    private boolean mAttachedInDecorSet;
    private Drawable mBackground;
    @UnsupportedAppUsage
    private View mBackgroundView;
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    private Drawable mBelowAnchorBackgroundDrawable;
    private boolean mClipToScreen;
    private boolean mClippingEnabled;
    @UnsupportedAppUsage
    private View mContentView;
    @UnsupportedAppUsage
    private Context mContext;
    @UnsupportedAppUsage
    private PopupDecorView mDecorView;
    private int mDropDownListPadding;
    private float mElevation;
    private Transition mEnterTransition;
    private Rect mEpicenterBounds;
    private Transition mExitTransition;
    private boolean mFocusable;
    private int mGravity;
    private int mHeight;
    @UnsupportedAppUsage
    private int mHeightMode;
    private boolean mIgnoreCheekPress;
    private int mInputMethodMode;
    private boolean mIsAnchorRootAttached;
    @UnsupportedAppUsage
    private boolean mIsDropdown;
    @UnsupportedAppUsage
    private boolean mIsShowing;
    private boolean mIsTransitioningToDismiss;
    private boolean mIsTvMode;
    @UnsupportedAppUsage
    private int mLastHeight;
    @UnsupportedAppUsage
    private int mLastWidth;
    @UnsupportedAppUsage
    private boolean mLayoutInScreen;
    private boolean mLayoutInsetDecor;
    protected int mNewDisplayFrameLeft;
    protected int mNewDisplayFrameRight;
    @UnsupportedAppUsage
    private boolean mNotTouchModal;
    private final View.OnAttachStateChangeListener mOnAnchorDetachedListener;
    private final View.OnAttachStateChangeListener mOnAnchorRootDetachedListener;
    @UnsupportedAppUsage
    private OnDismissListener mOnDismissListener;
    private final View.OnLayoutChangeListener mOnLayoutChangeListener;
    @UnsupportedAppUsage(maxTargetSdk = 28)
    private final ViewTreeObserver.OnScrollChangedListener mOnScrollChangedListener;
    private boolean mOutsideTouchable;
    @UnsupportedAppUsage(maxTargetSdk = 28)
    private boolean mOverlapAnchor;
    private int mPaddingToAnchor;
    private WeakReference<View> mParentRootView;
    private boolean mPopupViewInitialLayoutDirectionInherited;
    private int mSoftInputMode;
    private int mSplitTouchEnabled;
    private final Rect mTempRect;
    private CharSequence mTitle;
    private final int[] mTmpAppLocation;
    private final int[] mTmpDrawingLocation;
    private final int[] mTmpScreenLocation;
    @UnsupportedAppUsage
    private View.OnTouchListener mTouchInterceptor;
    private boolean mTouchable;
    private int mTvHorizontalMargin;
    private int mTvVerticalMargin;
    private int mWidth;
    @UnsupportedAppUsage
    private int mWidthMode;
    @UnsupportedAppUsage
    private int mWindowLayoutType;
    @UnsupportedAppUsage
    private WindowManager mWindowManager;

    public interface OnDismissListener {
        void onDismiss();
    }

    public /* synthetic */ void lambda$new$0$PopupWindow(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
        alignToAnchor();
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
        this.mTvHorizontalMargin = 0;
        this.mTvVerticalMargin = 0;
        this.mPaddingToAnchor = 0;
        this.mDropDownListPadding = 0;
        this.mIsTvMode = false;
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
        this.mOnAnchorDetachedListener = new View.OnAttachStateChangeListener() {
            /* class android.widget.PopupWindow.AnonymousClass1 */

            @Override // android.view.View.OnAttachStateChangeListener
            public void onViewAttachedToWindow(View v) {
                PopupWindow.this.alignToAnchor();
            }

            @Override // android.view.View.OnAttachStateChangeListener
            public void onViewDetachedFromWindow(View v) {
            }
        };
        this.mOnAnchorRootDetachedListener = new View.OnAttachStateChangeListener() {
            /* class android.widget.PopupWindow.AnonymousClass2 */

            @Override // android.view.View.OnAttachStateChangeListener
            public void onViewAttachedToWindow(View v) {
            }

            @Override // android.view.View.OnAttachStateChangeListener
            public void onViewDetachedFromWindow(View v) {
                PopupWindow.this.mIsAnchorRootAttached = false;
            }
        };
        this.mOnScrollChangedListener = new ViewTreeObserver.OnScrollChangedListener() {
            /* class android.widget.$$Lambda$PopupWindow$nV1HS3Nc6Ck5JRIbIHe3mkyHWzc */

            @Override // android.view.ViewTreeObserver.OnScrollChangedListener
            public final void onScrollChanged() {
                PopupWindow.this.alignToAnchor();
            }
        };
        this.mOnLayoutChangeListener = new View.OnLayoutChangeListener() {
            /* class android.widget.$$Lambda$PopupWindow$8Gc2stI5cSJZbuKX7X4Qr_vU2nI */

            @Override // android.view.View.OnLayoutChangeListener
            public final void onLayoutChange(View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
                PopupWindow.this.lambda$new$0$PopupWindow(view, i, i2, i3, i4, i5, i6, i7, i8);
            }
        };
        this.mContext = context;
        this.mWindowManager = (WindowManager) context.getSystemService("window");
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PopupWindow, defStyleAttr, defStyleRes);
        Drawable bg = a.getDrawable(0);
        this.mElevation = a.getDimension(3, 0.0f);
        this.mOverlapAnchor = a.getBoolean(2, false);
        if (a.hasValueOrEmpty(1)) {
            int animStyle = a.getResourceId(1, 0);
            if (animStyle == 16974594) {
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
        if (this.mContext.getResources().getInteger(com.android.hwext.internal.R.integer.emui_device_type) == 2) {
            this.mIsTvMode = true;
            this.mDropDownListPadding = this.mContext.getResources().getDimensionPixelSize(com.android.hwext.internal.R.dimen.popup_menu_drop_down_list_padding);
            this.mTvHorizontalMargin = this.mContext.getResources().getDimensionPixelSize(com.android.hwext.internal.R.dimen.popup_menu_horizontal_offset) - this.mDropDownListPadding;
            this.mTvVerticalMargin = this.mContext.getResources().getDimensionPixelSize(com.android.hwext.internal.R.dimen.popup_menu_vertical_offset) - this.mDropDownListPadding;
            this.mPaddingToAnchor = this.mContext.getResources().getDimensionPixelSize(com.android.hwext.internal.R.dimen.popup_menu_margin_from_anchor) - this.mDropDownListPadding;
        }
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
        this.mTvHorizontalMargin = 0;
        this.mTvVerticalMargin = 0;
        this.mPaddingToAnchor = 0;
        this.mDropDownListPadding = 0;
        this.mIsTvMode = false;
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
        this.mOnAnchorDetachedListener = new View.OnAttachStateChangeListener() {
            /* class android.widget.PopupWindow.AnonymousClass1 */

            @Override // android.view.View.OnAttachStateChangeListener
            public void onViewAttachedToWindow(View v) {
                PopupWindow.this.alignToAnchor();
            }

            @Override // android.view.View.OnAttachStateChangeListener
            public void onViewDetachedFromWindow(View v) {
            }
        };
        this.mOnAnchorRootDetachedListener = new View.OnAttachStateChangeListener() {
            /* class android.widget.PopupWindow.AnonymousClass2 */

            @Override // android.view.View.OnAttachStateChangeListener
            public void onViewAttachedToWindow(View v) {
            }

            @Override // android.view.View.OnAttachStateChangeListener
            public void onViewDetachedFromWindow(View v) {
                PopupWindow.this.mIsAnchorRootAttached = false;
            }
        };
        this.mOnScrollChangedListener = new ViewTreeObserver.OnScrollChangedListener() {
            /* class android.widget.$$Lambda$PopupWindow$nV1HS3Nc6Ck5JRIbIHe3mkyHWzc */

            @Override // android.view.ViewTreeObserver.OnScrollChangedListener
            public final void onScrollChanged() {
                PopupWindow.this.alignToAnchor();
            }
        };
        this.mOnLayoutChangeListener = new View.OnLayoutChangeListener() {
            /* class android.widget.$$Lambda$PopupWindow$8Gc2stI5cSJZbuKX7X4Qr_vU2nI */

            @Override // android.view.View.OnLayoutChangeListener
            public final void onLayoutChange(View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
                PopupWindow.this.lambda$new$0$PopupWindow(view, i, i2, i3, i4, i5, i6, i7, i8);
            }
        };
        if (contentView != null) {
            this.mContext = contentView.getContext();
            this.mWindowManager = (WindowManager) this.mContext.getSystemService("window");
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

    public Rect getEpicenterBounds() {
        Rect rect = this.mEpicenterBounds;
        if (rect != null) {
            return new Rect(rect);
        }
        return null;
    }

    public void setEpicenterBounds(Rect bounds) {
        this.mEpicenterBounds = bounds != null ? new Rect(bounds) : null;
    }

    private Transition getTransition(int resId) {
        Transition transition;
        if (resId == 0 || resId == 17760256 || (transition = TransitionInflater.from(this.mContext).inflateTransition(resId)) == null) {
            return null;
        }
        if (!((transition instanceof TransitionSet) && ((TransitionSet) transition).getTransitionCount() == 0)) {
            return transition;
        }
        return null;
    }

    public Drawable getBackground() {
        return this.mBackground;
    }

    public void setBackgroundDrawable(Drawable background) {
        this.mBackground = background;
        Drawable drawable = this.mBackground;
        if (drawable instanceof StateListDrawable) {
            StateListDrawable stateList = (StateListDrawable) drawable;
            int aboveAnchorStateIndex = stateList.findStateDrawableIndex(ABOVE_ANCHOR_STATE_SET);
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
        View view;
        if (!isShowing()) {
            this.mContentView = contentView;
            if (this.mContext == null && (view = this.mContentView) != null) {
                this.mContext = view.getContext();
            }
            if (this.mWindowManager == null && this.mContentView != null) {
                this.mWindowManager = (WindowManager) this.mContext.getSystemService("window");
            }
            Context context = this.mContext;
            if (context != null && !this.mAttachedInDecorSet) {
                setAttachedInDecor(context.getApplicationInfo().targetSdkVersion >= 22);
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

    @Deprecated
    public boolean isClipToScreenEnabled() {
        return this.mClipToScreen;
    }

    @Deprecated
    public void setClipToScreenEnabled(boolean enabled) {
        this.mClipToScreen = enabled;
    }

    public boolean isClippedToScreen() {
        return this.mClipToScreen;
    }

    public void setIsClippedToScreen(boolean enabled) {
        this.mClipToScreen = enabled;
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
    public void setAllowScrollingAnchorParent(boolean enabled) {
        this.mAllowScrollingAnchorParent = enabled;
    }

    /* access modifiers changed from: protected */
    public final boolean getAllowScrollingAnchorParent() {
        return this.mAllowScrollingAnchorParent;
    }

    public boolean isSplitTouchEnabled() {
        Context context;
        return (this.mSplitTouchEnabled >= 0 || (context = this.mContext) == null) ? this.mSplitTouchEnabled == 1 : context.getApplicationInfo().targetSdkVersion >= 11;
    }

    public void setSplitTouchEnabled(boolean enabled) {
        this.mSplitTouchEnabled = enabled ? 1 : 0;
    }

    @Deprecated
    public boolean isLayoutInScreenEnabled() {
        return this.mLayoutInScreen;
    }

    @Deprecated
    public void setLayoutInScreenEnabled(boolean enabled) {
        this.mLayoutInScreen = enabled;
    }

    public boolean isLaidOutInScreen() {
        return this.mLayoutInScreen;
    }

    public void setIsLaidOutInScreen(boolean enabled) {
        this.mLayoutInScreen = enabled;
    }

    public boolean isAttachedInDecor() {
        return this.mAttachedInDecor;
    }

    public void setAttachedInDecor(boolean enabled) {
        this.mAttachedInDecor = enabled;
        this.mAttachedInDecorSet = true;
    }

    @UnsupportedAppUsage
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

    public boolean isTouchModal() {
        return !this.mNotTouchModal;
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

    @UnsupportedAppUsage
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
            p.accessibilityIdOfAnchor = (long) anchor.getAccessibilityViewId();
            p.windowAnimations = computeAnimationResource();
            invokePopup(p);
        }
    }

    /* access modifiers changed from: protected */
    @UnsupportedAppUsage
    public final void updateAboveAnchor(boolean aboveAnchor) {
        View view;
        if (aboveAnchor != this.mAboveAnchor) {
            this.mAboveAnchor = aboveAnchor;
            if (this.mBackground != null && (view = this.mBackgroundView) != null) {
                Drawable drawable = this.mAboveAnchorBackgroundDrawable;
                if (drawable == null) {
                    view.refreshDrawableState();
                } else if (this.mAboveAnchor) {
                    view.setBackground(drawable);
                } else {
                    view.setBackground(this.mBelowAnchorBackgroundDrawable);
                }
            }
        }
    }

    public boolean isAboveAnchor() {
        return this.mAboveAnchor;
    }

    @UnsupportedAppUsage
    private void preparePopup(WindowManager.LayoutParams p) {
        if (this.mContentView == null || this.mContext == null || this.mWindowManager == null) {
            throw new IllegalStateException("You must specify a valid content view by calling setContentView() before attempting to show the popup.");
        }
        if (p.accessibilityTitle == null) {
            p.accessibilityTitle = this.mContext.getString(R.string.popup_window_default_title);
        }
        PopupDecorView popupDecorView = this.mDecorView;
        if (popupDecorView != null) {
            popupDecorView.cancelTransitions();
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
        if (this.mDecorView.getContext().getResources().getConfiguration().windowConfiguration.inHwMultiStackWindowingMode()) {
            p.format = -3;
        }
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
        backgroundView.addView(contentView, new FrameLayout.LayoutParams(-1, height));
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

    @UnsupportedAppUsage(maxTargetSdk = 28)
    private void invokePopup(WindowManager.LayoutParams p) {
        Context context = this.mContext;
        if (context != null) {
            p.packageName = context.getPackageName();
        }
        if (this.mDecorView == null) {
            Log.d("mDecorView", "invokePopup mDecorView == null");
            return;
        }
        PopupDecorView decorView = this.mDecorView;
        decorView.setFitsSystemWindows(this.mLayoutInsetDecor);
        setLayoutDirectionFromAnchor();
        this.mWindowManager.addView(decorView, p);
        Transition transition = this.mEnterTransition;
        if (transition != null) {
            decorView.requestEnterTransition(transition);
        }
    }

    private void setLayoutDirectionFromAnchor() {
        View anchor;
        WeakReference<View> weakReference = this.mAnchor;
        if (weakReference != null && (anchor = weakReference.get()) != null && this.mPopupViewInitialLayoutDirectionInherited) {
            this.mDecorView.setLayoutDirection(anchor.getLayoutDirection());
        }
    }

    private int computeGravity() {
        int gravity = this.mGravity;
        if (gravity == 0) {
            gravity = DEFAULT_ANCHORED_GRAVITY;
        }
        if (!this.mIsDropdown) {
            return gravity;
        }
        if (this.mClipToScreen || this.mClippingEnabled) {
            return gravity | 268435456;
        }
        return gravity;
    }

    /* access modifiers changed from: protected */
    @UnsupportedAppUsage
    public final WindowManager.LayoutParams createPopupLayoutParams(IBinder token) {
        WindowManager.LayoutParams p = new WindowManager.LayoutParams();
        p.gravity = computeGravity();
        p.flags = computeFlags(p.flags);
        p.type = this.mWindowLayoutType;
        p.token = token;
        p.softInputMode = this.mSoftInputMode;
        p.windowAnimations = computeAnimationResource();
        Drawable drawable = this.mBackground;
        if (drawable != null) {
            p.format = drawable.getOpacity();
        } else {
            p.format = -3;
        }
        int i = this.mHeightMode;
        if (i < 0) {
            this.mLastHeight = i;
            p.height = i;
        } else {
            int i2 = this.mHeight;
            this.mLastHeight = i2;
            p.height = i2;
        }
        int i3 = this.mWidthMode;
        if (i3 < 0) {
            this.mLastWidth = i3;
            p.width = i3;
        } else {
            int i4 = this.mWidth;
            this.mLastWidth = i4;
            p.width = i4;
        }
        p.privateFlags = 98304;
        CharSequence charSequence = this.mTitle;
        if (charSequence != null) {
            p.setTitle(charSequence);
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

    @UnsupportedAppUsage
    private int computeAnimationResource() {
        int i = this.mAnimationStyle;
        if (i != -1) {
            return i;
        }
        if (!this.mIsDropdown) {
            return 0;
        }
        Context context = this.mContext;
        if (context == null || !HwWidgetFactory.isHwTheme(context) || !HwWidgetFactory.isEmuiSuperLite()) {
            if (this.mAboveAnchor) {
                return R.style.Animation_DropDownUp;
            }
            return R.style.Animation_DropDownDown;
        } else if (this.mAboveAnchor) {
            return com.android.hwext.internal.R.style.HwAnimation_lite_DropDownUpLite;
        } else {
            return com.android.hwext.internal.R.style.HwAnimation_lite_DropDownDownLite;
        }
    }

    public void setPopupLocation(int start, int end) {
        this.mNewDisplayFrameLeft = start;
        this.mNewDisplayFrameRight = end;
    }

    /* JADX WARN: Type inference failed for: r19v1, types: [boolean] */
    /* JADX WARN: Type inference failed for: r19v6 */
    /* JADX WARN: Type inference failed for: r19v10 */
    /* JADX WARN: Type inference failed for: r19v11 */
    /* JADX WARN: Type inference failed for: r19v12 */
    /* access modifiers changed from: protected */
    public boolean findDropDownPosition(View anchor, WindowManager.LayoutParams outParams, int xOffset, int yOffset, int width, int height, int gravity, boolean allowScroll) {
        int yOffset2;
        int width2;
        int height2;
        ?? r19;
        WindowManager.LayoutParams layoutParams;
        char c;
        WindowManager.LayoutParams layoutParams2;
        int hgrav;
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
        anchor.getLocationOnScreen(screenLocation);
        int[] drawingLocation = this.mTmpDrawingLocation;
        drawingLocation[0] = screenLocation[0] - appScreenLocation[0];
        drawingLocation[1] = screenLocation[1] - appScreenLocation[1];
        if (this.mIsTvMode) {
            outParams.x = ((drawingLocation[0] - this.mWidth) - this.mPaddingToAnchor) + xOffset;
            outParams.y = (drawingLocation[1] - this.mDropDownListPadding) + yOffset2;
        } else {
            outParams.x = drawingLocation[0] + xOffset;
            outParams.y = drawingLocation[1] + anchorHeight + yOffset2;
        }
        Rect displayFrame = new Rect();
        appRootView.getWindowVisibleDisplayFrame(displayFrame);
        if (!(this.mNewDisplayFrameLeft == 0 && this.mNewDisplayFrameRight == 0)) {
            displayFrame.left = this.mNewDisplayFrameLeft;
            displayFrame.right = this.mNewDisplayFrameRight;
        }
        if (width == -1) {
            width2 = displayFrame.right - displayFrame.left;
        } else {
            width2 = width;
        }
        if (height == -1) {
            height2 = displayFrame.bottom - displayFrame.top;
        } else {
            height2 = height;
        }
        outParams.gravity = computeGravity();
        outParams.width = width2;
        outParams.height = height2;
        int hgrav2 = Gravity.getAbsoluteGravity(gravity, anchor.getLayoutDirection()) & 7;
        if (hgrav2 == 5 && !SystemProperties.getRTLFlag()) {
            outParams.x -= width2 - anchorWidth;
        }
        boolean fitsVertical = tryFitVertical(outParams, yOffset2, height2, anchorHeight, drawingLocation[1], screenLocation[1], displayFrame.top, displayFrame.bottom, false);
        boolean fitsHorizontal = tryFitHorizontal(outParams, xOffset, width2, anchorWidth, drawingLocation[0], screenLocation[0], displayFrame.left, displayFrame.right, false);
        if (!fitsVertical || !fitsHorizontal) {
            int scrollX = anchor.getScrollX();
            int scrollY = anchor.getScrollY();
            Rect r = new Rect(scrollX, scrollY, scrollX + width2 + xOffset, scrollY + height2 + anchorHeight + yOffset2);
            if (!allowScroll) {
                layoutParams2 = outParams;
                hgrav = hgrav2;
                c = 1;
            } else if (anchor.requestRectangleOnScreen(r, true)) {
                anchor.getLocationOnScreen(screenLocation);
                drawingLocation[0] = screenLocation[0] - appScreenLocation[0];
                drawingLocation[1] = screenLocation[1] - appScreenLocation[1];
                layoutParams2 = outParams;
                char c2 = 1;
                layoutParams2.x = drawingLocation[0] + xOffset;
                layoutParams2.y = drawingLocation[1] + anchorHeight + yOffset2;
                hgrav = hgrav2;
                c = c2;
                if (hgrav == 5) {
                    c = c2;
                    if (!SystemProperties.getRTLFlag()) {
                        layoutParams2.x -= width2 - anchorWidth;
                        c = c2;
                    }
                }
            } else {
                layoutParams2 = outParams;
                c = 1;
                hgrav = hgrav2;
            }
            layoutParams = layoutParams2;
            tryFitVertical(outParams, yOffset2, height2, anchorHeight, drawingLocation[c], screenLocation[c], displayFrame.top, displayFrame.bottom, this.mClipToScreen);
            tryFitHorizontal(outParams, xOffset, width2, anchorWidth, drawingLocation[0], screenLocation[0], displayFrame.left, displayFrame.right, this.mClipToScreen);
            r19 = c;
            if (this.mIsTvMode) {
                r19 = c;
                if (getHeight() - (this.mDropDownListPadding * 2) == displayFrame.bottom) {
                    layoutParams.height -= layoutParams.y + (this.mDropDownListPadding * 2);
                    r19 = c;
                }
            }
        } else {
            r19 = 1;
            layoutParams = outParams;
        }
        if (layoutParams.y < drawingLocation[r19]) {
            return r19;
        }
        return false;
    }

    private boolean isStatusBarShown(Context context) {
        if (!(context instanceof Activity) || (((Activity) context).getWindow().getAttributes().flags & 1024) != 1024) {
            return true;
        }
        return false;
    }

    private boolean tryFitVertical(WindowManager.LayoutParams outParams, int yOffset, int height, int anchorHeight, int drawingLocationY, int screenLocationY, int displayFrameTop, int displayFrameBottom, boolean allowResize) {
        int spaceBelow;
        int yOffset2;
        int anchorTopInScreen = outParams.y + (screenLocationY - drawingLocationY);
        if (this.mIsTvMode) {
            spaceBelow = (displayFrameBottom - anchorTopInScreen) - this.mTvVerticalMargin;
        } else {
            spaceBelow = displayFrameBottom - anchorTopInScreen;
        }
        if (anchorTopInScreen >= 0 && height <= spaceBelow) {
            return true;
        }
        if (height <= (anchorTopInScreen - anchorHeight) - displayFrameTop) {
            if (this.mOverlapAnchor) {
                yOffset2 = yOffset + anchorHeight;
            } else {
                yOffset2 = yOffset;
            }
            if (this.mIsTvMode) {
                outParams.y = (drawingLocationY - height) + anchorHeight + this.mDropDownListPadding + yOffset2;
            } else {
                outParams.y = (drawingLocationY - height) + yOffset2;
            }
            if (outParams.y + height <= displayFrameBottom) {
                return true;
            }
        }
        if (this.mIsTvMode) {
            if (isStatusBarShown(this.mContext)) {
                outParams.y = displayFrameTop;
                return false;
            }
            outParams.y = this.mTvVerticalMargin + displayFrameTop;
            return false;
        } else if (positionInDisplayVertical(outParams, height, drawingLocationY, screenLocationY, displayFrameTop, displayFrameBottom, allowResize)) {
            return true;
        } else {
            return false;
        }
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
        int anchorLeftInScreen;
        int winOffsetX = screenLocationX - drawingLocationX;
        if (this.mIsTvMode) {
            anchorLeftInScreen = (outParams.x + winOffsetX) - this.mTvHorizontalMargin;
        } else {
            anchorLeftInScreen = outParams.x + winOffsetX;
        }
        int spaceRight = displayFrameRight - anchorLeftInScreen;
        if (anchorLeftInScreen >= 0 && width <= spaceRight) {
            return true;
        }
        if (this.mIsTvMode) {
            int i = this.mPaddingToAnchor;
            int i2 = this.mTvHorizontalMargin;
            if (((displayFrameRight - drawingLocationX) - ((anchorWidth + i) + i2)) - xOffset >= width) {
                outParams.x = drawingLocationX + anchorWidth + i + xOffset;
            } else {
                outParams.x = ((displayFrameRight - width) - i2) - winOffsetX;
            }
            return true;
        } else if (positionInDisplayHorizontal(outParams, width, drawingLocationX, screenLocationX, displayFrameLeft, displayFrameRight, allowResize)) {
            return true;
        } else {
            return false;
        }
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
        if (this.mIsTvMode) {
            return visibleDisplayFrame.bottom;
        }
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
        Drawable drawable = this.mBackground;
        if (drawable == null) {
            return returnedHeight;
        }
        drawable.getPadding(this.mTempRect);
        return returnedHeight - (this.mTempRect.top + this.mTempRect.bottom);
    }

    public void dismiss() {
        final ViewGroup contentHolder;
        if (isShowing() && !isTransitioningToDismiss()) {
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
            if (exitTransition == null || !decorView.isLaidOut() || (!this.mIsAnchorRootAttached && this.mAnchorRoot != null)) {
                dismissImmediate(decorView, contentHolder, contentView);
            } else {
                WindowManager.LayoutParams p = (WindowManager.LayoutParams) decorView.getLayoutParams();
                p.flags |= 16;
                p.flags |= 8;
                p.flags &= -131073;
                this.mWindowManager.updateViewLayout(decorView, p);
                WeakReference<View> weakReference = this.mAnchorRoot;
                decorView.startExitTransition(exitTransition, weakReference != null ? weakReference.get() : null, getTransitionEpicenter(), new TransitionListenerAdapter() {
                    /* class android.widget.PopupWindow.AnonymousClass3 */

                    @Override // android.transition.TransitionListenerAdapter, android.transition.Transition.TransitionListener
                    public void onTransitionEnd(Transition transition) {
                        PopupWindow.this.dismissImmediate(decorView, contentHolder, contentView);
                    }
                });
            }
            detachFromAnchor();
            OnDismissListener onDismissListener = this.mOnDismissListener;
            if (onDismissListener != null) {
                onDismissListener.onDismiss();
            }
        }
    }

    /* access modifiers changed from: protected */
    public final Rect getTransitionEpicenter() {
        WeakReference<View> weakReference = this.mAnchor;
        View anchor = weakReference != null ? weakReference.get() : null;
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
    /* access modifiers changed from: public */
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
                WeakReference<View> weakReference = this.mAnchor;
                update(weakReference != null ? weakReference.get() : null, p);
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

    public void update(int x, int y, int width, int height, boolean force) {
        if (width >= 0) {
            this.mLastWidth = width;
            setWidth(width);
        }
        if (height >= 0) {
            this.mLastHeight = height;
            setHeight(height);
        }
        if (isShowing() && hasContentView()) {
            WindowManager.LayoutParams p = getDecorViewLayoutParams();
            boolean update = force;
            int finalWidth = this.mWidthMode;
            if (finalWidth >= 0) {
                finalWidth = this.mLastWidth;
            }
            if (!(width == -1 || p.width == finalWidth)) {
                this.mLastWidth = finalWidth;
                p.width = finalWidth;
                update = true;
            }
            int finalHeight = this.mHeightMode;
            if (finalHeight >= 0) {
                finalHeight = this.mLastHeight;
            }
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
            WeakReference<View> weakReference = this.mAnchor;
            if (!(weakReference == null || weakReference.get() == null)) {
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
        if (isShowing() && hasContentView()) {
            WeakReference<View> oldAnchor = this.mAnchor;
            int gravity = this.mAnchoredGravity;
            boolean needsUpdate = updateLocation && !(this.mAnchorXoff == xoff && this.mAnchorYoff == yoff);
            if (oldAnchor == null || oldAnchor.get() != anchor || (needsUpdate && !this.mIsDropdown)) {
                attachToAnchor(anchor, xoff, yoff, gravity);
            } else if (needsUpdate) {
                this.mAnchorXoff = xoff;
                this.mAnchorYoff = yoff;
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
            updateAboveAnchor(findDropDownPosition(anchor, p, this.mAnchorXoff, this.mAnchorYoff, width2, height2, gravity, this.mAllowScrollingAnchorParent));
            update(p.x, p.y, width2 < 0 ? width2 : p.width, height2 < 0 ? height2 : p.height, (oldGravity == p.gravity && oldX == p.x && oldY == p.y && oldWidth == p.width && oldHeight == p.height) ? false : true);
        }
    }

    /* access modifiers changed from: protected */
    public void detachFromAnchor() {
        View anchor = getAnchor();
        if (anchor != null) {
            anchor.getViewTreeObserver().removeOnScrollChangedListener(this.mOnScrollChangedListener);
            anchor.removeOnAttachStateChangeListener(this.mOnAnchorDetachedListener);
        }
        WeakReference<View> weakReference = this.mAnchorRoot;
        View anchorRoot = weakReference != null ? weakReference.get() : null;
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
        WeakReference<View> weakReference = this.mAnchor;
        if (weakReference != null) {
            return weakReference.get();
        }
        return null;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    public void alignToAnchor() {
        WeakReference<View> weakReference = this.mAnchor;
        View anchor = weakReference != null ? weakReference.get() : null;
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

    /* access modifiers changed from: private */
    public class PopupDecorView extends FrameLayout {
        private Runnable mCleanupAfterExit;
        private final View.OnAttachStateChangeListener mOnAnchorRootDetachedListener = new View.OnAttachStateChangeListener() {
            /* class android.widget.PopupWindow.PopupDecorView.AnonymousClass4 */

            @Override // android.view.View.OnAttachStateChangeListener
            public void onViewAttachedToWindow(View v) {
            }

            @Override // android.view.View.OnAttachStateChangeListener
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

        @Override // android.view.View, android.view.ViewGroup
        public boolean dispatchKeyEvent(KeyEvent event) {
            KeyEvent.DispatcherState state;
            if (event.getKeyCode() != 4) {
                return super.dispatchKeyEvent(event);
            }
            if (getKeyDispatcherState() == null) {
                return super.dispatchKeyEvent(event);
            }
            if (event.getAction() == 0 && event.getRepeatCount() == 0) {
                KeyEvent.DispatcherState state2 = getKeyDispatcherState();
                if (state2 != null) {
                    state2.startTracking(event, this);
                }
                return true;
            } else if (event.getAction() != 1 || (state = getKeyDispatcherState()) == null || !state.isTracking(event) || event.isCanceled()) {
                return super.dispatchKeyEvent(event);
            } else {
                PopupWindow.this.dismiss();
                return true;
            }
        }

        @Override // android.view.View, android.view.ViewGroup
        public boolean dispatchTouchEvent(MotionEvent ev) {
            if (PopupWindow.this.mTouchInterceptor == null || !PopupWindow.this.mTouchInterceptor.onTouch(this, ev)) {
                return super.dispatchTouchEvent(ev);
            }
            return true;
        }

        @Override // android.view.View
        public boolean onTouchEvent(MotionEvent event) {
            int x = (int) event.getX();
            int y = (int) event.getY();
            if (event.getAction() == 0 && (x < 0 || x >= getWidth() || y < 0 || y >= getHeight())) {
                PopupWindow.this.dismiss();
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
                    /* class android.widget.PopupWindow.PopupDecorView.AnonymousClass1 */

                    @Override // android.view.ViewTreeObserver.OnGlobalLayoutListener
                    public void onGlobalLayout() {
                        ViewTreeObserver observer = PopupDecorView.this.getViewTreeObserver();
                        if (observer != null) {
                            observer.removeOnGlobalLayoutListener(this);
                        }
                        final Rect epicenter = PopupWindow.this.getTransitionEpicenter();
                        enterTransition.setEpicenterCallback(new Transition.EpicenterCallback() {
                            /* class android.widget.PopupWindow.PopupDecorView.AnonymousClass1.AnonymousClass1 */

                            @Override // android.transition.Transition.EpicenterCallback
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
        /* access modifiers changed from: public */
        private void startEnterTransition(Transition enterTransition) {
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
                    /* class android.widget.$$Lambda$PopupWindow$PopupDecorView$T99WKEnQefOCXbbKvW95WY38p_I */
                    private final /* synthetic */ Transition.TransitionListener f$1;
                    private final /* synthetic */ Transition f$2;
                    private final /* synthetic */ View f$3;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                        this.f$3 = r4;
                    }

                    public final void run() {
                        PopupWindow.PopupDecorView.this.lambda$startExitTransition$0$PopupWindow$PopupDecorView(this.f$1, this.f$2, this.f$3);
                    }
                };
                Transition exitTransition = transition.clone();
                exitTransition.addListener(new TransitionListenerAdapter() {
                    /* class android.widget.PopupWindow.PopupDecorView.AnonymousClass2 */

                    @Override // android.transition.TransitionListenerAdapter, android.transition.Transition.TransitionListener
                    public void onTransitionEnd(Transition t) {
                        t.removeListener(this);
                        if (PopupDecorView.this.mCleanupAfterExit != null) {
                            PopupDecorView.this.mCleanupAfterExit.run();
                        }
                    }
                });
                exitTransition.setEpicenterCallback(new Transition.EpicenterCallback() {
                    /* class android.widget.PopupWindow.PopupDecorView.AnonymousClass3 */

                    @Override // android.transition.Transition.EpicenterCallback
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

        public /* synthetic */ void lambda$startExitTransition$0$PopupWindow$PopupDecorView(Transition.TransitionListener listener, Transition transition, View anchorRoot) {
            listener.onTransitionEnd(transition);
            if (anchorRoot != null) {
                anchorRoot.removeOnAttachStateChangeListener(this.mOnAnchorRootDetachedListener);
            }
            this.mCleanupAfterExit = null;
        }

        public void cancelTransitions() {
            TransitionManager.endTransitions(this);
            Runnable runnable = this.mCleanupAfterExit;
            if (runnable != null) {
                runnable.run();
            }
        }

        @Override // android.view.View
        public void requestKeyboardShortcuts(List<KeyboardShortcutGroup> list, int deviceId) {
            View parentRoot;
            if (PopupWindow.this.mParentRootView != null && (parentRoot = (View) PopupWindow.this.mParentRootView.get()) != null) {
                parentRoot.requestKeyboardShortcuts(list, deviceId);
            }
        }
    }

    /* access modifiers changed from: private */
    public class PopupBackgroundView extends FrameLayout {
        public PopupBackgroundView(Context context) {
            super(context);
        }

        /* access modifiers changed from: protected */
        @Override // android.view.View, android.view.ViewGroup
        public int[] onCreateDrawableState(int extraSpace) {
            if (!PopupWindow.this.mAboveAnchor) {
                return super.onCreateDrawableState(extraSpace);
            }
            int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
            View.mergeDrawableStates(drawableState, PopupWindow.ABOVE_ANCHOR_STATE_SET);
            return drawableState;
        }
    }

    /* access modifiers changed from: package-private */
    public int getDropDownListPadding() {
        return this.mDropDownListPadding;
    }
}
