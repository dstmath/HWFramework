package com.android.internal.app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.res.TypedArray;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.media.MediaRouter;
import android.media.MediaRouter.RouteGroup;
import android.media.MediaRouter.RouteInfo;
import android.media.MediaRouter.SimpleCallback;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import com.android.internal.R;

public class MediaRouteControllerDialog extends AlertDialog {
    private static final int VOLUME_UPDATE_DELAY_MILLIS = 250;
    private final MediaRouterCallback mCallback;
    private View mControlView;
    private boolean mCreated;
    private Drawable mCurrentIconDrawable;
    private Drawable mMediaRouteButtonDrawable;
    private int[] mMediaRouteConnectingState = new int[]{R.attr.state_checked, R.attr.state_enabled};
    private int[] mMediaRouteOnState = new int[]{R.attr.state_activated, R.attr.state_enabled};
    private final RouteInfo mRoute;
    private final MediaRouter mRouter;
    private boolean mVolumeControlEnabled = true;
    private LinearLayout mVolumeLayout;
    private SeekBar mVolumeSlider;
    private boolean mVolumeSliderTouched;

    private final class MediaRouterCallback extends SimpleCallback {
        /* synthetic */ MediaRouterCallback(MediaRouteControllerDialog this$0, MediaRouterCallback -this1) {
            this();
        }

        private MediaRouterCallback() {
        }

        public void onRouteUnselected(MediaRouter router, int type, RouteInfo info) {
            MediaRouteControllerDialog.this.update();
        }

        public void onRouteChanged(MediaRouter router, RouteInfo route) {
            MediaRouteControllerDialog.this.update();
        }

        public void onRouteVolumeChanged(MediaRouter router, RouteInfo route) {
            if (route == MediaRouteControllerDialog.this.mRoute) {
                MediaRouteControllerDialog.this.updateVolume();
            }
        }

        public void onRouteGrouped(MediaRouter router, RouteInfo info, RouteGroup group, int index) {
            MediaRouteControllerDialog.this.update();
        }

        public void onRouteUngrouped(MediaRouter router, RouteInfo info, RouteGroup group) {
            MediaRouteControllerDialog.this.update();
        }
    }

    public MediaRouteControllerDialog(Context context, int theme) {
        super(context, theme);
        this.mRouter = (MediaRouter) context.getSystemService("media_router");
        this.mCallback = new MediaRouterCallback(this, null);
        this.mRoute = this.mRouter.getSelectedRoute();
    }

    public RouteInfo getRoute() {
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

    protected void onCreate(Bundle savedInstanceState) {
        setTitle(this.mRoute.getName());
        setButton(-2, getContext().getResources().getString(R.string.media_route_controller_disconnect), new OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int id) {
                if (MediaRouteControllerDialog.this.mRoute.isSelected()) {
                    MediaRouteControllerDialog.this.mRouter.getDefaultRoute().select();
                }
                MediaRouteControllerDialog.this.dismiss();
            }
        });
        View customView = getLayoutInflater().inflate((int) R.layout.media_route_controller_dialog, null);
        setView(customView, 0, 0, 0, 0);
        super.onCreate(savedInstanceState);
        View customPanelView = getWindow().findViewById(R.id.customPanel);
        if (customPanelView != null) {
            customPanelView.setMinimumHeight(0);
        }
        this.mVolumeLayout = (LinearLayout) customView.findViewById(R.id.media_route_volume_layout);
        this.mVolumeSlider = (SeekBar) customView.findViewById(R.id.media_route_volume_slider);
        this.mVolumeSlider.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            private final Runnable mStopTrackingTouch = new Runnable() {
                public void run() {
                    if (MediaRouteControllerDialog.this.mVolumeSliderTouched) {
                        MediaRouteControllerDialog.this.mVolumeSliderTouched = false;
                        MediaRouteControllerDialog.this.updateVolume();
                    }
                }
            };

            public void onStartTrackingTouch(SeekBar seekBar) {
                if (MediaRouteControllerDialog.this.mVolumeSliderTouched) {
                    MediaRouteControllerDialog.this.mVolumeSlider.removeCallbacks(this.mStopTrackingTouch);
                } else {
                    MediaRouteControllerDialog.this.mVolumeSliderTouched = true;
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
            FrameLayout controlFrame = (FrameLayout) customView.findViewById(R.id.media_route_control_frame);
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
        this.mRouter.addCallback(0, this.mCallback, 2);
        update();
    }

    public void onDetachedFromWindow() {
        this.mRouter.removeCallback(this.mCallback);
        super.onDetachedFromWindow();
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode != 25 && keyCode != 24) {
            return super.onKeyDown(keyCode, event);
        }
        int i;
        RouteInfo routeInfo = this.mRoute;
        if (keyCode == 25) {
            i = -1;
        } else {
            i = 1;
        }
        routeInfo.requestUpdateVolume(i);
        return true;
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == 25 || keyCode == 24) {
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    private boolean update() {
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
                if (!animDrawable.isRunning()) {
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
        if (!context.getTheme().resolveAttribute(R.attr.mediaRouteButtonStyle, value, true)) {
            return null;
        }
        TypedArray a = context.obtainStyledAttributes(value.data, new int[]{R.attr.externalRouteEnabledDrawable});
        Drawable drawable = a.getDrawable(0);
        a.recycle();
        return drawable;
    }

    private Drawable getIconDrawable() {
        if (!(this.mMediaRouteButtonDrawable instanceof StateListDrawable)) {
            return this.mMediaRouteButtonDrawable;
        }
        StateListDrawable stateListDrawable;
        if (this.mRoute.isConnecting()) {
            stateListDrawable = this.mMediaRouteButtonDrawable;
            stateListDrawable.setState(this.mMediaRouteConnectingState);
            return stateListDrawable.getCurrent();
        }
        stateListDrawable = (StateListDrawable) this.mMediaRouteButtonDrawable;
        stateListDrawable.setState(this.mMediaRouteOnState);
        return stateListDrawable.getCurrent();
    }

    private void updateVolume() {
        if (!this.mVolumeSliderTouched) {
            if (isVolumeControlAvailable()) {
                this.mVolumeLayout.setVisibility(0);
                this.mVolumeSlider.setMax(this.mRoute.getVolumeMax());
                this.mVolumeSlider.setProgress(this.mRoute.getVolume());
                return;
            }
            this.mVolumeLayout.setVisibility(8);
        }
    }

    private boolean isVolumeControlAvailable() {
        return this.mVolumeControlEnabled && this.mRoute.getVolumeHandling() == 1;
    }
}
