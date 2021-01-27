package android.view.autofill;

import android.common.HwFrameworkFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.IBinder;
import android.os.RemoteException;
import android.transition.Transition;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.PopupWindow;
import com.android.internal.R;

public class AutofillPopupWindow extends PopupWindow {
    private static final String TAG = "AutofillPopupWindow";
    private boolean mFullScreen;
    private IHwAutofillHelper mHwAutofillHelper = HwFrameworkFactory.getHwAutofillHelper();
    private final View.OnAttachStateChangeListener mOnAttachStateChangeListener = new View.OnAttachStateChangeListener() {
        /* class android.view.autofill.AutofillPopupWindow.AnonymousClass1 */

        @Override // android.view.View.OnAttachStateChangeListener
        public void onViewAttachedToWindow(View v) {
        }

        @Override // android.view.View.OnAttachStateChangeListener
        public void onViewDetachedFromWindow(View v) {
            AutofillPopupWindow.this.dismiss();
        }
    };
    private WindowManager.LayoutParams mWindowLayoutParams;
    private final WindowPresenter mWindowPresenter;

    public AutofillPopupWindow(IAutofillWindowPresenter presenter) {
        this.mWindowPresenter = new WindowPresenter(presenter);
        setTouchModal(false);
        setOutsideTouchable(true);
        setInputMethodMode(2);
        setFocusable(true);
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.PopupWindow
    public boolean hasContentView() {
        return true;
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.PopupWindow
    public boolean hasDecorView() {
        return true;
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.PopupWindow
    public WindowManager.LayoutParams getDecorViewLayoutParams() {
        return this.mWindowLayoutParams;
    }

    public void update(final View anchor, int offsetX, int offsetY, int width, int height, Rect virtualBounds) {
        int i;
        View actualAnchor;
        this.mFullScreen = width == -1;
        if (this.mFullScreen) {
            i = 2008;
        } else {
            i = 1005;
        }
        setWindowLayoutType(i);
        if (this.mFullScreen) {
            offsetX = 0;
            offsetY = 0;
            Point outPoint = new Point();
            anchor.getContext().getDisplay().getSize(outPoint);
            width = outPoint.x;
            if (height != -1) {
                offsetY = outPoint.y - height;
            }
            actualAnchor = anchor;
        } else if (virtualBounds != null) {
            final int[] mLocationOnScreen = {virtualBounds.left, virtualBounds.top};
            View r4 = new View(anchor.getContext()) {
                /* class android.view.autofill.AutofillPopupWindow.AnonymousClass2 */

                @Override // android.view.View
                public void getLocationOnScreen(int[] location) {
                    int[] iArr = mLocationOnScreen;
                    location[0] = iArr[0];
                    location[1] = iArr[1];
                }

                @Override // android.view.View
                public int getAccessibilityViewId() {
                    return anchor.getAccessibilityViewId();
                }

                @Override // android.view.View
                public ViewTreeObserver getViewTreeObserver() {
                    return anchor.getViewTreeObserver();
                }

                @Override // android.view.View
                public IBinder getApplicationWindowToken() {
                    return anchor.getApplicationWindowToken();
                }

                @Override // android.view.View
                public View getRootView() {
                    return anchor.getRootView();
                }

                @Override // android.view.View
                public int getLayoutDirection() {
                    return anchor.getLayoutDirection();
                }

                @Override // android.view.View
                public void getWindowDisplayFrame(Rect outRect) {
                    anchor.getWindowDisplayFrame(outRect);
                }

                @Override // android.view.View
                public void addOnAttachStateChangeListener(View.OnAttachStateChangeListener listener) {
                    anchor.addOnAttachStateChangeListener(listener);
                }

                @Override // android.view.View
                public void removeOnAttachStateChangeListener(View.OnAttachStateChangeListener listener) {
                    anchor.removeOnAttachStateChangeListener(listener);
                }

                @Override // android.view.View
                public boolean isAttachedToWindow() {
                    return anchor.isAttachedToWindow();
                }

                @Override // android.view.View
                public boolean requestRectangleOnScreen(Rect rectangle, boolean immediate) {
                    return anchor.requestRectangleOnScreen(rectangle, immediate);
                }

                @Override // android.view.View
                public IBinder getWindowToken() {
                    return anchor.getWindowToken();
                }
            };
            r4.setLeftTopRightBottom(virtualBounds.left, virtualBounds.top, virtualBounds.right, virtualBounds.bottom);
            r4.setScrollX(anchor.getScrollX());
            r4.setScrollY(anchor.getScrollY());
            anchor.setOnScrollChangeListener(new View.OnScrollChangeListener(mLocationOnScreen) {
                /* class android.view.autofill.$$Lambda$AutofillPopupWindow$DnLs9aVkSgQ89oSTe4P9EweBBks */
                private final /* synthetic */ int[] f$0;

                {
                    this.f$0 = r1;
                }

                @Override // android.view.View.OnScrollChangeListener
                public final void onScrollChange(View view, int i, int i2, int i3, int i4) {
                    AutofillPopupWindow.lambda$update$0(this.f$0, view, i, i2, i3, i4);
                }
            });
            r4.setWillNotDraw(true);
            actualAnchor = r4;
        } else {
            actualAnchor = anchor;
        }
        if (!this.mFullScreen) {
            setAnimationStyle(-1);
        } else if (height == -1) {
            setAnimationStyle(0);
        } else {
            setAnimationStyle(R.style.AutofillHalfScreenAnimation);
        }
        if (!isShowing()) {
            setWidth(width);
            setHeight(height);
            showAsDropDown(actualAnchor, offsetX, offsetY);
            return;
        }
        update(actualAnchor, offsetX, offsetY, width, height);
    }

    static /* synthetic */ void lambda$update$0(int[] mLocationOnScreen, View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
        mLocationOnScreen[0] = mLocationOnScreen[0] - (scrollX - oldScrollX);
        mLocationOnScreen[1] = mLocationOnScreen[1] - (scrollY - oldScrollY);
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.PopupWindow
    public void update(View anchor, WindowManager.LayoutParams params) {
        int layoutDirection;
        if (anchor != null) {
            layoutDirection = anchor.getLayoutDirection();
        } else {
            layoutDirection = 3;
        }
        IHwAutofillHelper iHwAutofillHelper = this.mHwAutofillHelper;
        if (iHwAutofillHelper != null) {
            iHwAutofillHelper.resizeLayoutForLowResolution(anchor, params);
        }
        this.mWindowPresenter.show(params, getTransitionEpicenter(), isLayoutInsetDecor(), layoutDirection);
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.PopupWindow
    public boolean findDropDownPosition(View anchor, WindowManager.LayoutParams outParams, int xOffset, int yOffset, int width, int height, int gravity, boolean allowScroll) {
        if (!this.mFullScreen) {
            return super.findDropDownPosition(anchor, outParams, xOffset, yOffset, width, height, gravity, allowScroll);
        }
        outParams.x = xOffset;
        outParams.y = yOffset;
        outParams.width = width;
        outParams.height = height;
        outParams.gravity = gravity;
        return false;
    }

    @Override // android.widget.PopupWindow
    public void showAsDropDown(View anchor, int xoff, int yoff, int gravity) {
        if (Helper.sVerbose) {
            Log.v(TAG, "showAsDropDown(): anchor=" + anchor + ", xoff=" + xoff + ", yoff=" + yoff + ", isShowing(): " + isShowing());
        }
        if (!isShowing()) {
            setShowing(true);
            setDropDown(true);
            attachToAnchor(anchor, xoff, yoff, gravity);
            WindowManager.LayoutParams p = createPopupLayoutParams(anchor.getWindowToken());
            this.mWindowLayoutParams = p;
            boolean aboveAnchor = findDropDownPosition(anchor, p, xoff, yoff, p.width, p.height, gravity, getAllowScrollingAnchorParent());
            IHwAutofillHelper iHwAutofillHelper = this.mHwAutofillHelper;
            if (iHwAutofillHelper != null) {
                iHwAutofillHelper.resizeLayoutForLowResolution(anchor, p);
            }
            updateAboveAnchor(aboveAnchor);
            p.accessibilityIdOfAnchor = (long) anchor.getAccessibilityViewId();
            p.packageName = anchor.getContext().getPackageName();
            this.mWindowPresenter.show(p, getTransitionEpicenter(), isLayoutInsetDecor(), anchor.getLayoutDirection());
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.PopupWindow
    public void attachToAnchor(View anchor, int xoff, int yoff, int gravity) {
        super.attachToAnchor(anchor, xoff, yoff, gravity);
        anchor.addOnAttachStateChangeListener(this.mOnAttachStateChangeListener);
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.PopupWindow
    public void detachFromAnchor() {
        View anchor = getAnchor();
        if (anchor != null) {
            anchor.removeOnAttachStateChangeListener(this.mOnAttachStateChangeListener);
        }
        super.detachFromAnchor();
    }

    @Override // android.widget.PopupWindow
    public void dismiss() {
        if (isShowing() && !isTransitioningToDismiss()) {
            setShowing(false);
            setTransitioningToDismiss(true);
            this.mWindowPresenter.hide(getTransitionEpicenter());
            detachFromAnchor();
            if (getOnDismissListener() != null) {
                getOnDismissListener().onDismiss();
            }
        }
    }

    @Override // android.widget.PopupWindow
    public int getAnimationStyle() {
        throw new IllegalStateException("You can't call this!");
    }

    @Override // android.widget.PopupWindow
    public Drawable getBackground() {
        throw new IllegalStateException("You can't call this!");
    }

    @Override // android.widget.PopupWindow
    public View getContentView() {
        throw new IllegalStateException("You can't call this!");
    }

    @Override // android.widget.PopupWindow
    public float getElevation() {
        throw new IllegalStateException("You can't call this!");
    }

    @Override // android.widget.PopupWindow
    public Transition getEnterTransition() {
        throw new IllegalStateException("You can't call this!");
    }

    @Override // android.widget.PopupWindow
    public Transition getExitTransition() {
        throw new IllegalStateException("You can't call this!");
    }

    @Override // android.widget.PopupWindow
    public void setBackgroundDrawable(Drawable background) {
        throw new IllegalStateException("You can't call this!");
    }

    @Override // android.widget.PopupWindow
    public void setContentView(View contentView) {
        if (contentView != null) {
            throw new IllegalStateException("You can't call this!");
        }
    }

    @Override // android.widget.PopupWindow
    public void setElevation(float elevation) {
        throw new IllegalStateException("You can't call this!");
    }

    @Override // android.widget.PopupWindow
    public void setEnterTransition(Transition enterTransition) {
        throw new IllegalStateException("You can't call this!");
    }

    @Override // android.widget.PopupWindow
    public void setExitTransition(Transition exitTransition) {
        throw new IllegalStateException("You can't call this!");
    }

    @Override // android.widget.PopupWindow
    public void setTouchInterceptor(View.OnTouchListener l) {
        throw new IllegalStateException("You can't call this!");
    }

    /* access modifiers changed from: private */
    public class WindowPresenter {
        final IAutofillWindowPresenter mPresenter;

        WindowPresenter(IAutofillWindowPresenter presenter) {
            this.mPresenter = presenter;
        }

        /* access modifiers changed from: package-private */
        public void show(WindowManager.LayoutParams p, Rect transitionEpicenter, boolean fitsSystemWindows, int layoutDirection) {
            try {
                this.mPresenter.show(p, transitionEpicenter, fitsSystemWindows, layoutDirection);
            } catch (RemoteException e) {
                Log.w(AutofillPopupWindow.TAG, "Error showing fill window", e);
                e.rethrowFromSystemServer();
            }
        }

        /* access modifiers changed from: package-private */
        public void hide(Rect transitionEpicenter) {
            try {
                this.mPresenter.hide(transitionEpicenter);
            } catch (RemoteException e) {
                Log.w(AutofillPopupWindow.TAG, "Error hiding fill window", e);
                e.rethrowFromSystemServer();
            }
        }
    }
}
