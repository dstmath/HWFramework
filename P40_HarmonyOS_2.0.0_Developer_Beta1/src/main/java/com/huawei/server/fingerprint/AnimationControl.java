package com.huawei.server.fingerprint;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import java.math.BigDecimal;
import java.util.List;

public class AnimationControl {
    private static final int DEFAULT_SCREEN_SIZE_STRING = 2;
    private static final long DELAY_MILLIS_FOR_ANIMATION = 16;
    private static final int INITIAL_MESSAGE = -1;
    private static final int MAX_FPS = 1000;
    private static final int START = 0;
    private static final String TAG = AnimationControl.class.getSimpleName();
    private static AnimationControl sInstance;
    private boolean isAnimationRunning = false;
    private Context mContext;
    private long mFingerprintAnimDelay = DELAY_MILLIS_FOR_ANIMATION;
    private FpLayer mFpLayer;

    private AnimationControl() {
    }

    public static synchronized AnimationControl getInstance() {
        AnimationControl animationControl;
        synchronized (AnimationControl.class) {
            if (sInstance == null) {
                sInstance = new AnimationControl();
            }
            animationControl = sInstance;
        }
        return animationControl;
    }

    public void createLayer(Context context, int centerx, int centery, float scale, List<String> animationRes) {
        this.mContext = context;
        if (this.mFpLayer == null) {
            this.mFpLayer = new FpLayer(this.mContext, centerx, centery, scale, animationRes);
        }
    }

    public void setAnimDelayTime(int fpAnimFps) {
        if (fpAnimFps <= 0 || fpAnimFps > 1000) {
            this.mFingerprintAnimDelay = DELAY_MILLIS_FOR_ANIMATION;
        } else {
            this.mFingerprintAnimDelay = new BigDecimal(1000).divide(new BigDecimal(fpAnimFps), 0, 1).longValue();
            String str = TAG;
            Log.i(str, "setAnimDelayTime mFingerprintAnimDelay = " + this.mFingerprintAnimDelay);
        }
        String str2 = TAG;
        Log.i(str2, "setAnimDelayTime fpAnimFps=" + fpAnimFps + ",mFingerprintAnimDelay = " + this.mFingerprintAnimDelay);
    }

    private void drawAnimation(int index, Handler handler, int length) {
        if (handler == null || index >= length) {
            this.mFpLayer.hide();
            this.isAnimationRunning = false;
            Log.i(TAG, "drawAnimation hide");
        } else if (this.isAnimationRunning) {
            handler.sendEmptyMessageDelayed(index + 1, this.mFingerprintAnimDelay);
            String str = TAG;
            Log.i(str, "drawAnimation index=" + index);
            this.mFpLayer.draw(index);
            this.mFpLayer.show();
            String str2 = TAG;
            Log.i(str2, "drawAnimation sendEmptyMessageDelayed=" + index);
        }
    }

    public void handleMessage(Message msg, Handler handler, List<String> animFileNames) {
        if (this.mFpLayer == null || animFileNames == null || animFileNames.isEmpty()) {
            Log.i(TAG, "handleMessage null");
            return;
        }
        if (msg.what >= animFileNames.size() || msg.what < 0) {
            this.mFpLayer.hide();
            this.isAnimationRunning = false;
        } else {
            drawAnimation(msg.what, handler, animFileNames.size());
        }
        String str = TAG;
        Log.i(str, "handleMessage msg=" + msg);
    }

    public void startDrawAnimation(Handler handler) {
        if (handler == null) {
            Log.i(TAG, "sendDrawAnimation null");
        } else if (this.mFpLayer != null) {
            this.isAnimationRunning = true;
            handler.sendEmptyMessage(0);
            Log.i(TAG, "sendDrawAnimation");
        }
    }

    public void stopDrawAnimation(Handler handler) {
        if (handler == null) {
            Log.i(TAG, "stopDrawAnimation null");
            return;
        }
        Message msg = Message.obtain();
        msg.what = -1;
        if (!handler.hasMessages(-1)) {
            handler.sendMessageAtFrontOfQueue(msg);
        }
    }

    public void destroyFpLayer() {
        this.isAnimationRunning = false;
        FpLayer fpLayer = this.mFpLayer;
        if (fpLayer != null) {
            fpLayer.destroy();
            this.mFpLayer = null;
        }
    }
}
