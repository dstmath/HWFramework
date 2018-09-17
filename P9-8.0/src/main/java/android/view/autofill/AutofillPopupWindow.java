package android.view.autofill;

import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.IBinder;
import android.os.RemoteException;
import android.transition.Transition;
import android.util.Log;
import android.view.View;
import android.view.View.OnAttachStateChangeListener;
import android.view.View.OnTouchListener;
import android.view.ViewTreeObserver;
import android.view.WindowManager.LayoutParams;
import android.widget.PopupWindow;

public class AutofillPopupWindow extends PopupWindow {
    private static final String TAG = "AutofillPopupWindow";
    private LayoutParams mWindowLayoutParams;
    private final WindowPresenter mWindowPresenter;

    private class WindowPresenter {
        final IAutofillWindowPresenter mPresenter;

        WindowPresenter(IAutofillWindowPresenter presenter) {
            this.mPresenter = presenter;
        }

        void show(LayoutParams p, Rect transitionEpicenter, boolean fitsSystemWindows, int layoutDirection) {
            try {
                this.mPresenter.show(p, transitionEpicenter, fitsSystemWindows, layoutDirection);
            } catch (RemoteException e) {
                Log.w(AutofillPopupWindow.TAG, "Error showing fill window", e);
                e.rethrowFromSystemServer();
            }
        }

        void hide(Rect transitionEpicenter) {
            try {
                this.mPresenter.hide(transitionEpicenter);
            } catch (RemoteException e) {
                Log.w(AutofillPopupWindow.TAG, "Error hiding fill window", e);
                e.rethrowFromSystemServer();
            }
        }
    }

    public AutofillPopupWindow(IAutofillWindowPresenter presenter) {
        this.mWindowPresenter = new WindowPresenter(presenter);
        setOutsideTouchable(true);
        setInputMethodMode(1);
    }

    protected boolean hasContentView() {
        return true;
    }

    protected boolean hasDecorView() {
        return true;
    }

    protected LayoutParams getDecorViewLayoutParams() {
        return this.mWindowLayoutParams;
    }

    public void update(final View anchor, int offsetX, int offsetY, int width, int height, final Rect virtualBounds) {
        View actualAnchor;
        if (virtualBounds != null) {
            actualAnchor = new View(anchor.getContext()) {
                public void getLocationOnScreen(int[] location) {
                    location[0] = virtualBounds.left;
                    location[1] = virtualBounds.top;
                }

                public int getAccessibilityViewId() {
                    return anchor.getAccessibilityViewId();
                }

                public ViewTreeObserver getViewTreeObserver() {
                    return anchor.getViewTreeObserver();
                }

                public IBinder getApplicationWindowToken() {
                    return anchor.getApplicationWindowToken();
                }

                public View getRootView() {
                    return anchor.getRootView();
                }

                public int getLayoutDirection() {
                    return anchor.getLayoutDirection();
                }

                public void getWindowDisplayFrame(Rect outRect) {
                    anchor.getWindowDisplayFrame(outRect);
                }

                public void addOnAttachStateChangeListener(OnAttachStateChangeListener listener) {
                    anchor.addOnAttachStateChangeListener(listener);
                }

                public void removeOnAttachStateChangeListener(OnAttachStateChangeListener listener) {
                    anchor.removeOnAttachStateChangeListener(listener);
                }

                public boolean isAttachedToWindow() {
                    return anchor.isAttachedToWindow();
                }

                public boolean requestRectangleOnScreen(Rect rectangle, boolean immediate) {
                    return anchor.requestRectangleOnScreen(rectangle, immediate);
                }

                public IBinder getWindowToken() {
                    return anchor.getWindowToken();
                }
            };
            actualAnchor.setLeftTopRightBottom(virtualBounds.left, virtualBounds.top, virtualBounds.right, virtualBounds.bottom);
            actualAnchor.setScrollX(anchor.getScrollX());
            actualAnchor.setScrollY(anchor.getScrollY());
        } else {
            actualAnchor = anchor;
        }
        if (isShowing()) {
            update(actualAnchor, offsetX, offsetY, width, height);
            return;
        }
        setWidth(width);
        setHeight(height);
        showAsDropDown(actualAnchor, offsetX, offsetY);
    }

    protected void update(View anchor, LayoutParams params) {
        int layoutDirection;
        if (anchor != null) {
            layoutDirection = anchor.getLayoutDirection();
        } else {
            layoutDirection = 3;
        }
        this.mWindowPresenter.show(params, getTransitionEpicenter(), isLayoutInsetDecor(), layoutDirection);
    }

    public void showAsDropDown(View anchor, int xoff, int yoff, int gravity) {
        if (Helper.sVerbose) {
            Log.v(TAG, "showAsDropDown(): anchor=" + anchor + ", xoff=" + xoff + ", yoff=" + yoff + ", isShowing(): " + isShowing());
        }
        if (!isShowing()) {
            setShowing(true);
            setDropDown(true);
            attachToAnchor(anchor, xoff, yoff, gravity);
            LayoutParams p = createPopupLayoutParams(anchor.getWindowToken());
            this.mWindowLayoutParams = p;
            updateAboveAnchor(findDropDownPosition(anchor, p, xoff, yoff, p.width, p.height, gravity, getAllowScrollingAnchorParent()));
            p.accessibilityIdOfAnchor = anchor.getAccessibilityViewId();
            p.packageName = anchor.getContext().getPackageName();
            this.mWindowPresenter.show(p, getTransitionEpicenter(), isLayoutInsetDecor(), anchor.getLayoutDirection());
        }
    }

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

    public int getAnimationStyle() {
        throw new IllegalStateException("You can't call this!");
    }

    public Drawable getBackground() {
        throw new IllegalStateException("You can't call this!");
    }

    public View getContentView() {
        throw new IllegalStateException("You can't call this!");
    }

    public float getElevation() {
        throw new IllegalStateException("You can't call this!");
    }

    public Transition getEnterTransition() {
        throw new IllegalStateException("You can't call this!");
    }

    public Transition getExitTransition() {
        throw new IllegalStateException("You can't call this!");
    }

    public void setAnimationStyle(int animationStyle) {
        throw new IllegalStateException("You can't call this!");
    }

    public void setBackgroundDrawable(Drawable background) {
        throw new IllegalStateException("You can't call this!");
    }

    public void setContentView(View contentView) {
        if (contentView != null) {
            throw new IllegalStateException("You can't call this!");
        }
    }

    public void setElevation(float elevation) {
        throw new IllegalStateException("You can't call this!");
    }

    public void setEnterTransition(Transition enterTransition) {
        throw new IllegalStateException("You can't call this!");
    }

    public void setExitTransition(Transition exitTransition) {
        throw new IllegalStateException("You can't call this!");
    }

    public void setTouchInterceptor(OnTouchListener l) {
        throw new IllegalStateException("You can't call this!");
    }
}
