package android.app;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaRouter;
import android.util.AttributeSet;
import android.view.View;
import com.android.internal.R;
import com.android.internal.app.MediaRouteDialogPresenter;

public class MediaRouteButton extends View {
    private static final int[] ACTIVATED_STATE_SET = {16843518};
    private static final int[] CHECKED_STATE_SET = {16842912};
    private boolean mAttachedToWindow;
    private final MediaRouterCallback mCallback;
    private View.OnClickListener mExtendedSettingsClickListener;
    private boolean mIsConnecting;
    private int mMinHeight;
    private int mMinWidth;
    private boolean mRemoteActive;
    private Drawable mRemoteIndicator;
    private int mRouteTypes;
    private final MediaRouter mRouter;

    private final class MediaRouterCallback extends MediaRouter.SimpleCallback {
        private MediaRouterCallback() {
        }

        public void onRouteAdded(MediaRouter router, MediaRouter.RouteInfo info) {
            MediaRouteButton.this.refreshRoute();
        }

        public void onRouteRemoved(MediaRouter router, MediaRouter.RouteInfo info) {
            MediaRouteButton.this.refreshRoute();
        }

        public void onRouteChanged(MediaRouter router, MediaRouter.RouteInfo info) {
            MediaRouteButton.this.refreshRoute();
        }

        public void onRouteSelected(MediaRouter router, int type, MediaRouter.RouteInfo info) {
            MediaRouteButton.this.refreshRoute();
        }

        public void onRouteUnselected(MediaRouter router, int type, MediaRouter.RouteInfo info) {
            MediaRouteButton.this.refreshRoute();
        }

        public void onRouteGrouped(MediaRouter router, MediaRouter.RouteInfo info, MediaRouter.RouteGroup group, int index) {
            MediaRouteButton.this.refreshRoute();
        }

        public void onRouteUngrouped(MediaRouter router, MediaRouter.RouteInfo info, MediaRouter.RouteGroup group) {
            MediaRouteButton.this.refreshRoute();
        }
    }

    public MediaRouteButton(Context context) {
        this(context, null);
    }

    public MediaRouteButton(Context context, AttributeSet attrs) {
        this(context, attrs, 16843693);
    }

    public MediaRouteButton(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public MediaRouteButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mRouter = (MediaRouter) context.getSystemService(Context.MEDIA_ROUTER_SERVICE);
        this.mCallback = new MediaRouterCallback();
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MediaRouteButton, defStyleAttr, defStyleRes);
        setRemoteIndicatorDrawable(a.getDrawable(3));
        this.mMinWidth = a.getDimensionPixelSize(0, 0);
        this.mMinHeight = a.getDimensionPixelSize(1, 0);
        int routeTypes = a.getInteger(2, 1);
        a.recycle();
        setClickable(true);
        setRouteTypes(routeTypes);
    }

    public int getRouteTypes() {
        return this.mRouteTypes;
    }

    public void setRouteTypes(int types) {
        if (this.mRouteTypes != types) {
            if (this.mAttachedToWindow && this.mRouteTypes != 0) {
                this.mRouter.removeCallback(this.mCallback);
            }
            this.mRouteTypes = types;
            if (this.mAttachedToWindow && types != 0) {
                this.mRouter.addCallback(types, this.mCallback, 8);
            }
            refreshRoute();
        }
    }

    public void setExtendedSettingsClickListener(View.OnClickListener listener) {
        this.mExtendedSettingsClickListener = listener;
    }

    public void showDialog() {
        showDialogInternal();
    }

    /* access modifiers changed from: package-private */
    public boolean showDialogInternal() {
        boolean z = false;
        if (!this.mAttachedToWindow) {
            return false;
        }
        if (MediaRouteDialogPresenter.showDialogFragment(getActivity(), this.mRouteTypes, this.mExtendedSettingsClickListener) != null) {
            z = true;
        }
        return z;
    }

    private Activity getActivity() {
        for (Context context = getContext(); context instanceof ContextWrapper; context = ((ContextWrapper) context).getBaseContext()) {
            if (context instanceof Activity) {
                return (Activity) context;
            }
        }
        throw new IllegalStateException("The MediaRouteButton's Context is not an Activity.");
    }

    public void setContentDescription(CharSequence contentDescription) {
        super.setContentDescription(contentDescription);
        setTooltipText(contentDescription);
    }

    public boolean performClick() {
        boolean handled = super.performClick();
        if (!handled) {
            playSoundEffect(0);
        }
        if (showDialogInternal() || handled) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public int[] onCreateDrawableState(int extraSpace) {
        int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
        if (this.mIsConnecting) {
            mergeDrawableStates(drawableState, CHECKED_STATE_SET);
        } else if (this.mRemoteActive) {
            mergeDrawableStates(drawableState, ACTIVATED_STATE_SET);
        }
        return drawableState;
    }

    /* access modifiers changed from: protected */
    public void drawableStateChanged() {
        super.drawableStateChanged();
        Drawable remoteIndicator = this.mRemoteIndicator;
        if (remoteIndicator != null && remoteIndicator.isStateful() && remoteIndicator.setState(getDrawableState())) {
            invalidateDrawable(remoteIndicator);
        }
    }

    private void setRemoteIndicatorDrawable(Drawable d) {
        if (this.mRemoteIndicator != null) {
            this.mRemoteIndicator.setCallback(null);
            unscheduleDrawable(this.mRemoteIndicator);
        }
        this.mRemoteIndicator = d;
        if (d != null) {
            d.setCallback(this);
            d.setState(getDrawableState());
            d.setVisible(getVisibility() == 0, false);
        }
        refreshDrawableState();
    }

    /* access modifiers changed from: protected */
    public boolean verifyDrawable(Drawable who) {
        return super.verifyDrawable(who) || who == this.mRemoteIndicator;
    }

    public void jumpDrawablesToCurrentState() {
        super.jumpDrawablesToCurrentState();
        if (this.mRemoteIndicator != null) {
            this.mRemoteIndicator.jumpToCurrentState();
        }
    }

    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (this.mRemoteIndicator != null) {
            this.mRemoteIndicator.setVisible(getVisibility() == 0, false);
        }
    }

    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.mAttachedToWindow = true;
        if (this.mRouteTypes != 0) {
            this.mRouter.addCallback(this.mRouteTypes, this.mCallback, 8);
        }
        refreshRoute();
    }

    public void onDetachedFromWindow() {
        this.mAttachedToWindow = false;
        if (this.mRouteTypes != 0) {
            this.mRouter.removeCallback(this.mCallback);
        }
        super.onDetachedFromWindow();
    }

    /* access modifiers changed from: protected */
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int i;
        int measuredWidth;
        int measuredHeight;
        int widthSize = View.MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = View.MeasureSpec.getSize(heightMeasureSpec);
        int widthMode = View.MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = View.MeasureSpec.getMode(heightMeasureSpec);
        int i2 = this.mMinWidth;
        int i3 = 0;
        if (this.mRemoteIndicator != null) {
            i = this.mRemoteIndicator.getIntrinsicWidth() + getPaddingLeft() + getPaddingRight();
        } else {
            i = 0;
        }
        int width = Math.max(i2, i);
        int i4 = this.mMinHeight;
        if (this.mRemoteIndicator != null) {
            i3 = this.mRemoteIndicator.getIntrinsicHeight() + getPaddingTop() + getPaddingBottom();
        }
        int height = Math.max(i4, i3);
        if (widthMode == Integer.MIN_VALUE) {
            measuredWidth = Math.min(widthSize, width);
        } else if (widthMode != 1073741824) {
            measuredWidth = width;
        } else {
            measuredWidth = widthSize;
        }
        if (heightMode == Integer.MIN_VALUE) {
            measuredHeight = Math.min(heightSize, height);
        } else if (heightMode != 1073741824) {
            measuredHeight = height;
        } else {
            measuredHeight = heightSize;
        }
        setMeasuredDimension(measuredWidth, measuredHeight);
    }

    /* access modifiers changed from: protected */
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (this.mRemoteIndicator != null) {
            int left = getPaddingLeft();
            int right = getWidth() - getPaddingRight();
            int top = getPaddingTop();
            int bottom = getHeight() - getPaddingBottom();
            int drawWidth = this.mRemoteIndicator.getIntrinsicWidth();
            int drawHeight = this.mRemoteIndicator.getIntrinsicHeight();
            int drawLeft = (((right - left) - drawWidth) / 2) + left;
            int drawTop = (((bottom - top) - drawHeight) / 2) + top;
            this.mRemoteIndicator.setBounds(drawLeft, drawTop, drawLeft + drawWidth, drawTop + drawHeight);
            this.mRemoteIndicator.draw(canvas);
        }
    }

    /* access modifiers changed from: private */
    public void refreshRoute() {
        MediaRouter.RouteInfo route = this.mRouter.getSelectedRoute();
        boolean isConnecting = false;
        boolean isRemote = !route.isDefault() && route.matchesTypes(this.mRouteTypes);
        if (isRemote && route.isConnecting()) {
            isConnecting = true;
        }
        boolean needsRefresh = false;
        if (this.mRemoteActive != isRemote) {
            this.mRemoteActive = isRemote;
            needsRefresh = true;
        }
        if (this.mIsConnecting != isConnecting) {
            this.mIsConnecting = isConnecting;
            needsRefresh = true;
        }
        if (needsRefresh) {
            refreshDrawableState();
        }
        if (this.mAttachedToWindow) {
            setEnabled(this.mRouter.isRouteAvailable(this.mRouteTypes, 1));
        }
        if (this.mRemoteIndicator != null && (this.mRemoteIndicator.getCurrent() instanceof AnimationDrawable)) {
            AnimationDrawable curDrawable = (AnimationDrawable) this.mRemoteIndicator.getCurrent();
            if (this.mAttachedToWindow) {
                if ((needsRefresh || isConnecting) && !curDrawable.isRunning()) {
                    curDrawable.start();
                }
            } else if (isRemote && !isConnecting) {
                if (curDrawable.isRunning()) {
                    curDrawable.stop();
                }
                curDrawable.selectDrawable(curDrawable.getNumberOfFrames() - 1);
            }
        }
    }
}
