package com.android.internal.app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.media.MediaRouter;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.SeekBar;

public class MediaRouteControllerDialog extends AlertDialog {
    private static final int VOLUME_UPDATE_DELAY_MILLIS = 250;
    private boolean mAttachedToWindow;
    private final MediaRouterCallback mCallback;
    private View mControlView;
    private boolean mCreated;
    private Drawable mCurrentIconDrawable;
    private Drawable mMediaRouteButtonDrawable;
    private int[] mMediaRouteConnectingState = {16842912, 16842910};
    private int[] mMediaRouteOnState = {16843518, 16842910};
    /* access modifiers changed from: private */
    public final MediaRouter.RouteInfo mRoute;
    /* access modifiers changed from: private */
    public final MediaRouter mRouter;
    private boolean mVolumeControlEnabled = true;
    private LinearLayout mVolumeLayout;
    /* access modifiers changed from: private */
    public SeekBar mVolumeSlider;
    /* access modifiers changed from: private */
    public boolean mVolumeSliderTouched;

    private final class MediaRouterCallback extends MediaRouter.SimpleCallback {
        private MediaRouterCallback() {
        }

        public void onRouteUnselected(MediaRouter router, int type, MediaRouter.RouteInfo info) {
            boolean unused = MediaRouteControllerDialog.this.update();
        }

        public void onRouteChanged(MediaRouter router, MediaRouter.RouteInfo route) {
            boolean unused = MediaRouteControllerDialog.this.update();
        }

        public void onRouteVolumeChanged(MediaRouter router, MediaRouter.RouteInfo route) {
            if (route == MediaRouteControllerDialog.this.mRoute) {
                MediaRouteControllerDialog.this.updateVolume();
            }
        }

        public void onRouteGrouped(MediaRouter router, MediaRouter.RouteInfo info, MediaRouter.RouteGroup group, int index) {
            boolean unused = MediaRouteControllerDialog.this.update();
        }

        public void onRouteUngrouped(MediaRouter router, MediaRouter.RouteInfo info, MediaRouter.RouteGroup group) {
            boolean unused = MediaRouteControllerDialog.this.update();
        }
    }

    public MediaRouteControllerDialog(Context context, int theme) {
        super(context, theme);
        this.mRouter = (MediaRouter) context.getSystemService("media_router");
        this.mCallback = new MediaRouterCallback();
        this.mRoute = this.mRouter.getSelectedRoute();
    }

    public MediaRouter.RouteInfo getRoute() {
        return this.mRoute;
    }

    public View onCreateMediaControlView(Bundle savedInstanceState) {
        return null;
    }

    public View getMediaControlView() {
        return this.mControlView;
    }

    public void setVolumeControlEnabled(boolean enable) {
        if (this.mVolumeControlEnabled != enable) {
            this.mVolumeControlEnabled = enable;
            if (this.mCreated) {
                updateVolume();
            }
        }
    }

    public boolean isVolumeControlEnabled() {
        return this.mVolumeControlEnabled;
    }

    /* access modifiers changed from: protected */
    public void onCreate(Bundle savedInstanceState) {
        setTitle(this.mRoute.getName());
        setButton(-2, getContext().getResources().getString(17040416), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int id) {
                if (MediaRouteControllerDialog.this.mRoute.isSelected()) {
                    if (MediaRouteControllerDialog.this.mRoute.isBluetooth()) {
                        MediaRouteControllerDialog.this.mRouter.getDefaultRoute().select();
                    } else {
                        MediaRouteControllerDialog.this.mRouter.getFallbackRoute().select();
                    }
                }
                MediaRouteControllerDialog.this.dismiss();
            }
        });
        View customView = getLayoutInflater().inflate(17367177, null);
        setView(customView, 0, 0, 0, 0);
        super.onCreate(savedInstanceState);
        View customPanelView = getWindow().findViewById(16908839);
        if (customPanelView != null) {
            customPanelView.setMinimumHeight(0);
        }
        this.mVolumeLayout = (LinearLayout) customView.findViewById(16909078);
        this.mVolumeSlider = (SeekBar) customView.findViewById(16909079);
        this.mVolumeSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            private final Runnable mStopTrackingTouch = new Runnable() {
                public void run() {
                    if (MediaRouteControllerDialog.this.mVolumeSliderTouched) {
                        boolean unused = MediaRouteControllerDialog.this.mVolumeSliderTouched = false;
                        MediaRouteControllerDialog.this.updateVolume();
                    }
                }
            };

            public void onStartTrackingTouch(SeekBar seekBar) {
                if (MediaRouteControllerDialog.this.mVolumeSliderTouched) {
                    MediaRouteControllerDialog.this.mVolumeSlider.removeCallbacks(this.mStopTrackingTouch);
                } else {
                    boolean unused = MediaRouteControllerDialog.this.mVolumeSliderTouched = true;
                }
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                MediaRouteControllerDialog.this.mVolumeSlider.postDelayed(this.mStopTrackingTouch, 250);
            }

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    MediaRouteControllerDialog.this.mRoute.requestSetVolume(progress);
                }
            }
        });
        this.mMediaRouteButtonDrawable = obtainMediaRouteButtonDrawable();
        this.mCreated = true;
        if (update()) {
            this.mControlView = onCreateMediaControlView(savedInstanceState);
            FrameLayout controlFrame = (FrameLayout) customView.findViewById(16909074);
            if (this.mControlView != null) {
                controlFrame.addView(this.mControlView);
                controlFrame.setVisibility(0);
                return;
            }
            controlFrame.setVisibility(8);
        }
    }

    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.mAttachedToWindow = true;
        this.mRouter.addCallback(0, this.mCallback, 2);
        update();
    }

    public void onDetachedFromWindow() {
        this.mRouter.removeCallback(this.mCallback);
        this.mAttachedToWindow = false;
        super.onDetachedFromWindow();
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode != 25 && keyCode != 24) {
            return super.onKeyDown(keyCode, event);
        }
        this.mRoute.requestUpdateVolume(keyCode == 25 ? -1 : 1);
        return true;
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == 25 || keyCode == 24) {
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    /* access modifiers changed from: private */
    public boolean update() {
        if (!this.mRoute.isSelected() || this.mRoute.isDefault()) {
            dismiss();
            return false;
        }
        setTitle(this.mRoute.getName());
        updateVolume();
        Drawable icon = getIconDrawable();
        if (icon != this.mCurrentIconDrawable) {
            this.mCurrentIconDrawable = icon;
            if (icon instanceof AnimationDrawable) {
                AnimationDrawable animDrawable = (AnimationDrawable) icon;
                if (!this.mAttachedToWindow && !this.mRoute.isConnecting()) {
                    if (animDrawable.isRunning()) {
                        animDrawable.stop();
                    }
                    icon = animDrawable.getFrame(animDrawable.getNumberOfFrames() - 1);
                } else if (!animDrawable.isRunning()) {
                    animDrawable.start();
                }
            }
            setIcon(icon);
        }
        return true;
    }

    private Drawable obtainMediaRouteButtonDrawable() {
        Context context = getContext();
        TypedValue value = new TypedValue();
        if (!context.getTheme().resolveAttribute(16843693, value, true)) {
            return null;
        }
        TypedArray a = context.obtainStyledAttributes(value.data, new int[]{17891379});
        Drawable drawable = a.getDrawable(0);
        a.recycle();
        return drawable;
    }

    private Drawable getIconDrawable() {
        if (!(this.mMediaRouteButtonDrawable instanceof StateListDrawable)) {
            return this.mMediaRouteButtonDrawable;
        }
        if (this.mRoute.isConnecting()) {
            StateListDrawable stateListDrawable = (StateListDrawable) this.mMediaRouteButtonDrawable;
            stateListDrawable.setState(this.mMediaRouteConnectingState);
            return stateListDrawable.getCurrent();
        }
        StateListDrawable stateListDrawable2 = (StateListDrawable) this.mMediaRouteButtonDrawable;
        stateListDrawable2.setState(this.mMediaRouteOnState);
        return stateListDrawable2.getCurrent();
    }

    /* access modifiers changed from: private */
    public void updateVolume() {
        if (this.mVolumeSliderTouched) {
            return;
        }
        if (isVolumeControlAvailable()) {
            this.mVolumeLayout.setVisibility(0);
            this.mVolumeSlider.setMax(this.mRoute.getVolumeMax());
            this.mVolumeSlider.setProgress(this.mRoute.getVolume());
            return;
        }
        this.mVolumeLayout.setVisibility(8);
    }

    private boolean isVolumeControlAvailable() {
        return this.mVolumeControlEnabled && this.mRoute.getVolumeHandling() == 1;
    }
}
