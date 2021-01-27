package com.android.server.display;

import android.content.Context;
import android.graphics.SurfaceTexture;
import com.android.server.display.OverlayDisplayWindow;

public class OverlayDisplayWindowEx {
    private OverlayDisplayWindow mOverlayDisplayWindow;

    public OverlayDisplayWindowEx(Context context, String name, int width, int height, int densityDpi, int gravity, boolean secure, ListenerEx listenerEx) {
        this.mOverlayDisplayWindow = new OverlayDisplayWindow(context, name, width, height, densityDpi, gravity, secure, listenerEx.getListener());
    }

    public void show() {
        OverlayDisplayWindow overlayDisplayWindow = this.mOverlayDisplayWindow;
        if (overlayDisplayWindow != null) {
            overlayDisplayWindow.show();
        }
    }

    public void dismiss() {
        OverlayDisplayWindow overlayDisplayWindow = this.mOverlayDisplayWindow;
        if (overlayDisplayWindow != null) {
            overlayDisplayWindow.dismiss();
        }
    }

    public void resize(int width, int height, int densityDpi) {
        OverlayDisplayWindow overlayDisplayWindow = this.mOverlayDisplayWindow;
        if (overlayDisplayWindow != null) {
            overlayDisplayWindow.resize(width, height, densityDpi);
        }
    }

    public static class ListenerEx {
        OverlayDisplayWindow.Listener mListener = new OverlayDisplayWindow.Listener() {
            /* class com.android.server.display.OverlayDisplayWindowEx.ListenerEx.AnonymousClass1 */

            public void onWindowCreated(SurfaceTexture surfaceTexture, float refreshRate, long presentationDeadlineNanos, int index) {
                ListenerEx.this.onWindowCreated(surfaceTexture, refreshRate, presentationDeadlineNanos, index);
            }

            public void onWindowDestroyed() {
                ListenerEx.this.onWindowDestroyed();
            }

            public void onStateChanged(int state) {
                ListenerEx.this.onStateChanged(state);
            }
        };

        public ListenerEx() {
        }

        public ListenerEx(OverlayDisplayWindow.Listener listener) {
            this.mListener = listener;
        }

        public OverlayDisplayWindow.Listener getListener() {
            return this.mListener;
        }

        public void onWindowCreated(SurfaceTexture surfaceTexture, float refreshRate, long presentationDeadlineNanos, int state) {
        }

        public void onWindowDestroyed() {
        }

        public void onStateChanged(int state) {
        }
    }
}
