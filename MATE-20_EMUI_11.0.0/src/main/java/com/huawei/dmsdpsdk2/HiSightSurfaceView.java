package com.huawei.dmsdpsdk2;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class HiSightSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    private static final int KEY_CODE_85 = 85;
    private static final int KEY_CODE_86 = 86;
    private static final int KEY_CODE_87 = 87;
    private static final int KEY_CODE_88 = 88;
    private static final String TAG = "HiSightSurfaceView";
    SurfaceHolder surfaceHolder = getHolder();

    public HiSightSurfaceView(Context context) {
        super(context);
        this.surfaceHolder.addCallback(this);
    }

    public HiSightSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.surfaceHolder.addCallback(this);
    }

    @Override // android.view.SurfaceHolder.Callback
    public void surfaceCreated(SurfaceHolder surfaceHolder2) {
    }

    @Override // android.view.SurfaceHolder.Callback
    public void surfaceChanged(SurfaceHolder surfaceHolder2, int format, int width, int height) {
        Log.d(TAG, "surfaceChanged() called " + width + " " + height);
    }

    @Override // android.view.SurfaceHolder.Callback
    public void surfaceDestroyed(SurfaceHolder surfaceHolder2) {
    }

    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent event) {
        if (DMSDPAdapterAgent.sendHiSightMotionEvent(event) == 0) {
            return true;
        }
        return false;
    }

    @Override // android.view.KeyEvent.Callback, android.view.View
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        DMSDPAdapterAgent.sendHiSightKeyEvent(event);
        return dontHandleKeyFramework(keyCode);
    }

    @Override // android.view.KeyEvent.Callback, android.view.View
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        DMSDPAdapterAgent.sendHiSightKeyEvent(event);
        return dontHandleKeyFramework(keyCode);
    }

    private boolean dontHandleKeyFramework(int keyCode) {
        if (keyCode == KEY_CODE_85 || keyCode == KEY_CODE_87 || keyCode == KEY_CODE_88) {
            return true;
        }
        return false;
    }
}
