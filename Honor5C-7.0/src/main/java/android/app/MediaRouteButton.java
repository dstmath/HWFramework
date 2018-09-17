package android.app;

import android.R;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.media.MediaRouter;
import android.media.MediaRouter.RouteGroup;
import android.media.MediaRouter.RouteInfo;
import android.media.MediaRouter.SimpleCallback;
import android.security.keymaster.KeymasterDefs;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.widget.Toast;
import com.android.internal.app.MediaRouteDialogPresenter;

public class MediaRouteButton extends View {
    private static final int[] ACTIVATED_STATE_SET = null;
    private static final int[] CHECKED_STATE_SET = null;
    private boolean mAttachedToWindow;
    private final MediaRouterCallback mCallback;
    private boolean mCheatSheetEnabled;
    private OnClickListener mExtendedSettingsClickListener;
    private boolean mIsConnecting;
    private int mMinHeight;
    private int mMinWidth;
    private boolean mRemoteActive;
    private Drawable mRemoteIndicator;
    private int mRouteTypes;
    private final MediaRouter mRouter;

    private final class MediaRouterCallback extends SimpleCallback {
        private MediaRouterCallback() {
        }

        public void onRouteAdded(MediaRouter router, RouteInfo info) {
            MediaRouteButton.this.refreshRoute();
        }

        public void onRouteRemoved(MediaRouter router, RouteInfo info) {
            MediaRouteButton.this.refreshRoute();
        }

        public void onRouteChanged(MediaRouter router, RouteInfo info) {
            MediaRouteButton.this.refreshRoute();
        }

        public void onRouteSelected(MediaRouter router, int type, RouteInfo info) {
            MediaRouteButton.this.refreshRoute();
        }

        public void onRouteUnselected(MediaRouter router, int type, RouteInfo info) {
            MediaRouteButton.this.refreshRoute();
        }

        public void onRouteGrouped(MediaRouter router, RouteInfo info, RouteGroup group, int index) {
            MediaRouteButton.this.refreshRoute();
        }

        public void onRouteUngrouped(MediaRouter router, RouteInfo info, RouteGroup group) {
            MediaRouteButton.this.refreshRoute();
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.app.MediaRouteButton.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.app.MediaRouteButton.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.app.MediaRouteButton.<clinit>():void");
    }

    public MediaRouteButton(Context context) {
        this(context, null);
    }

    public MediaRouteButton(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.mediaRouteButtonStyle);
    }

    public MediaRouteButton(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public MediaRouteButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mRouter = (MediaRouter) context.getSystemService(Context.MEDIA_ROUTER_SERVICE);
        this.mCallback = new MediaRouterCallback();
        TypedArray a = context.obtainStyledAttributes(attrs, com.android.internal.R.styleable.MediaRouteButton, defStyleAttr, defStyleRes);
        setRemoteIndicatorDrawable(a.getDrawable(3));
        this.mMinWidth = a.getDimensionPixelSize(0, 0);
        this.mMinHeight = a.getDimensionPixelSize(1, 0);
        int routeTypes = a.getInteger(2, 1);
        a.recycle();
        setClickable(true);
        setLongClickable(true);
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

    public void setExtendedSettingsClickListener(OnClickListener listener) {
        this.mExtendedSettingsClickListener = listener;
    }

    public void showDialog() {
        showDialogInternal();
    }

    boolean showDialogInternal() {
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

    void setCheatSheetEnabled(boolean enable) {
        this.mCheatSheetEnabled = enable;
    }

    public boolean performClick() {
        boolean handled = super.performClick();
        if (!handled) {
            playSoundEffect(0);
        }
        return !showDialogInternal() ? handled : true;
    }

    public boolean performLongClick() {
        if (super.performLongClick()) {
            return true;
        }
        if (!this.mCheatSheetEnabled) {
            return false;
        }
        CharSequence contentDesc = getContentDescription();
        if (TextUtils.isEmpty(contentDesc)) {
            return false;
        }
        int[] screenPos = new int[2];
        Rect displayFrame = new Rect();
        getLocationOnScreen(screenPos);
        getWindowVisibleDisplayFrame(displayFrame);
        Context context = getContext();
        int width = getWidth();
        int height = getHeight();
        int midy = screenPos[1] + (height / 2);
        int screenWidth = context.getResources().getDisplayMetrics().widthPixels;
        Toast cheatSheet = Toast.makeText(context, contentDesc, 0);
        if (midy < displayFrame.height()) {
            cheatSheet.setGravity(8388661, (screenWidth - screenPos[0]) - (width / 2), height);
        } else {
            cheatSheet.setGravity(81, 0, height);
        }
        cheatSheet.show();
        performHapticFeedback(0);
        return true;
    }

    protected int[] onCreateDrawableState(int extraSpace) {
        int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
        if (this.mIsConnecting) {
            mergeDrawableStates(drawableState, CHECKED_STATE_SET);
        } else if (this.mRemoteActive) {
            mergeDrawableStates(drawableState, ACTIVATED_STATE_SET);
        }
        return drawableState;
    }

    protected void drawableStateChanged() {
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
            boolean z;
            d.setCallback(this);
            d.setState(getDrawableState());
            if (getVisibility() == 0) {
                z = true;
            } else {
                z = false;
            }
            d.setVisible(z, false);
        }
        refreshDrawableState();
    }

    protected boolean verifyDrawable(Drawable who) {
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
            boolean z;
            Drawable drawable = this.mRemoteIndicator;
            if (getVisibility() == 0) {
                z = true;
            } else {
                z = false;
            }
            drawable.setVisible(z, false);
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

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int intrinsicWidth;
        int measuredWidth;
        int measuredHeight;
        int i = 0;
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int i2 = this.mMinWidth;
        if (this.mRemoteIndicator != null) {
            intrinsicWidth = (this.mRemoteIndicator.getIntrinsicWidth() + getPaddingLeft()) + getPaddingRight();
        } else {
            intrinsicWidth = 0;
        }
        int width = Math.max(i2, intrinsicWidth);
        intrinsicWidth = this.mMinHeight;
        if (this.mRemoteIndicator != null) {
            i = (this.mRemoteIndicator.getIntrinsicHeight() + getPaddingTop()) + getPaddingBottom();
        }
        int height = Math.max(intrinsicWidth, i);
        switch (widthMode) {
            case KeymasterDefs.KM_BIGNUM /*-2147483648*/:
                measuredWidth = Math.min(widthSize, width);
                break;
            case KeymasterDefs.KM_UINT_REP /*1073741824*/:
                measuredWidth = widthSize;
                break;
            default:
                measuredWidth = width;
                break;
        }
        switch (heightMode) {
            case KeymasterDefs.KM_BIGNUM /*-2147483648*/:
                measuredHeight = Math.min(heightSize, height);
                break;
            case KeymasterDefs.KM_UINT_REP /*1073741824*/:
                measuredHeight = heightSize;
                break;
            default:
                measuredHeight = height;
                break;
        }
        setMeasuredDimension(measuredWidth, measuredHeight);
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (this.mRemoteIndicator != null) {
            int left = getPaddingLeft();
            int right = getWidth() - getPaddingRight();
            int top = getPaddingTop();
            int bottom = getHeight() - getPaddingBottom();
            int drawWidth = this.mRemoteIndicator.getIntrinsicWidth();
            int drawHeight = this.mRemoteIndicator.getIntrinsicHeight();
            int drawLeft = left + (((right - left) - drawWidth) / 2);
            int drawTop = top + (((bottom - top) - drawHeight) / 2);
            this.mRemoteIndicator.setBounds(drawLeft, drawTop, drawLeft + drawWidth, drawTop + drawHeight);
            this.mRemoteIndicator.draw(canvas);
        }
    }

    private void refreshRoute() {
        if (this.mAttachedToWindow) {
            RouteInfo route = this.mRouter.getSelectedRoute();
            boolean isRemote = !route.isDefault() ? route.matchesTypes(this.mRouteTypes) : false;
            boolean isConnecting = isRemote ? route.isConnecting() : false;
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
            setEnabled(this.mRouter.isRouteAvailable(this.mRouteTypes, 1));
        }
    }
}
