package ohos.dmsdp.sdk;

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

    private boolean dontHandleKeyFramework(int i) {
        return i == 85 || i == 87 || i == 88;
    }

    @Override // android.view.SurfaceHolder.Callback
    public void surfaceCreated(SurfaceHolder surfaceHolder2) {
    }

    @Override // android.view.SurfaceHolder.Callback
    public void surfaceDestroyed(SurfaceHolder surfaceHolder2) {
    }

    public HiSightSurfaceView(Context context) {
        super(context);
        this.surfaceHolder.addCallback(this);
    }

    public HiSightSurfaceView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.surfaceHolder.addCallback(this);
    }

    @Override // android.view.SurfaceHolder.Callback
    public void surfaceChanged(SurfaceHolder surfaceHolder2, int i, int i2, int i3) {
        Log.d(TAG, "surfaceChanged() called " + i2 + " " + i3);
    }

    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent motionEvent) {
        return DMSDPAdapterAgent.sendHiSightMotionEvent(motionEvent) == 0;
    }

    @Override // android.view.KeyEvent.Callback, android.view.View
    public boolean onKeyUp(int i, KeyEvent keyEvent) {
        DMSDPAdapterAgent.sendHiSightKeyEvent(keyEvent);
        return dontHandleKeyFramework(i);
    }

    @Override // android.view.KeyEvent.Callback, android.view.View
    public boolean onKeyDown(int i, KeyEvent keyEvent) {
        DMSDPAdapterAgent.sendHiSightKeyEvent(keyEvent);
        return dontHandleKeyFramework(i);
    }
}
