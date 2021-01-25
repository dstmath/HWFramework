package com.android.server.foldscreenview;

import android.content.Context;
import android.hardware.input.InputManager;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import com.android.server.lights.LightsManagerEx;

public class NavigationButton {
    private static final String TAG = "NavigationButton";
    private Context mContext;
    private long mDownTime = 0;
    private KeyButtonRipple mKeyButtonRipple;
    private int mKeyCode;
    private View mTargetView;

    public NavigationButton(Context context, View targetView, int keyCode) {
        this.mTargetView = targetView;
        this.mKeyCode = keyCode;
        this.mContext = context;
        this.mKeyButtonRipple = new KeyButtonRipple(this.mContext, this.mTargetView);
        View view = this.mTargetView;
        if (view != null) {
            view.setBackground(this.mKeyButtonRipple);
            this.mTargetView.setOnTouchListener(new View.OnTouchListener() {
                /* class com.android.server.foldscreenview.NavigationButton.AnonymousClass1 */

                @Override // android.view.View.OnTouchListener
                public boolean onTouch(View v, MotionEvent event) {
                    NavigationButton.this.onTouchEvent(event);
                    return true;
                }
            });
        }
    }

    private void sendEvent(int action, int flags) {
        InputManager.getInstance().injectInputEvent(new KeyEvent(this.mDownTime, SystemClock.uptimeMillis(), action, this.mKeyCode, 0, 0, -1, 0, flags | 8 | 64, LightsManagerEx.LIGHT_ID_SMARTBACKLIGHT), 0);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onTouchEvent(MotionEvent event) {
        if (event != null) {
            int action = event.getAction();
            if (action == 0) {
                this.mDownTime = SystemClock.uptimeMillis();
                sendEvent(0, 0);
                this.mKeyButtonRipple.setPressed(true);
            } else if (action == 1) {
                sendEvent(1, 0);
                this.mKeyButtonRipple.setPressed(false);
            } else if (action == 3) {
                sendEvent(1, 32);
                this.mKeyButtonRipple.setPressed(false);
            }
        }
    }
}
