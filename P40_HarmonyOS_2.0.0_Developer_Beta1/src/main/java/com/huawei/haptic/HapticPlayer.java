package com.huawei.haptic;

import android.content.Context;
import android.os.Process;
import com.huawei.annotation.HwSystemApi;
import com.huawei.haptic.HwHapticPlayer;
import java.io.InputStream;

@HwSystemApi
public class HapticPlayer {
    public static final int HAPTIC_STATE_COMPLETED = 3;
    public static final int HAPTIC_STATE_ERROR = 16;
    public static final int HAPTIC_STATE_PLAYING = 1;
    public static final int HAPTIC_STATE_STOPPED = 2;
    private Context mContext;
    private HwHapticPlayer mHapticPlayer;
    private OnPlayStateListener mOnPlayStateListener;
    private final String mPackageName;
    private HwHapticPlayer.OnPlayStateListener mStateListener = new HwHapticPlayer.OnPlayStateListener() {
        /* class com.huawei.haptic.HapticPlayer.AnonymousClass1 */

        public void onState(int state, int errorCode) {
            if (HapticPlayer.this.mOnPlayStateListener != null) {
                HapticPlayer.this.mOnPlayStateListener.onState(state, errorCode);
            }
        }
    };

    public interface OnPlayStateListener {
        void onState(int i, int i2);
    }

    public HapticPlayer(Context context) {
        this.mContext = context;
        this.mPackageName = context.getOpPackageName();
        this.mHapticPlayer = new HwHapticPlayer();
        this.mHapticPlayer.setOnPlayStateListener(this.mStateListener);
    }

    public boolean setHapticWave(HapticAttributes attr, HapticWave wave) {
        if (attr == null || wave == null) {
            return false;
        }
        HwHapticWave hwWave = HapticWave.createHwHapticWave(wave);
        return this.mHapticPlayer.setHapticWave(Process.myUid(), this.mPackageName, HapticAttributes.createHwHapticAttributes(attr), hwWave);
    }

    public boolean setHapticWave(HapticAttributes attr, InputStream inputStream) {
        if (attr == null || inputStream == null) {
            return false;
        }
        return this.mHapticPlayer.setHapticWave(Process.myUid(), this.mPackageName, HapticAttributes.createHwHapticAttributes(attr), inputStream);
    }

    public void setLooping(boolean looping) {
        this.mHapticPlayer.setLooping(looping);
    }

    public boolean isLooping() {
        return this.mHapticPlayer.isLooping();
    }

    public void setSwapHapticPos(boolean isSwap) {
        this.mHapticPlayer.setSwapHapticPos(isSwap);
    }

    public boolean isSwapHapticPos() {
        return this.mHapticPlayer.isSwapHapticPos();
    }

    public boolean isPlaying() {
        return this.mHapticPlayer.isPlaying();
    }

    public int play() {
        return this.mHapticPlayer.play();
    }

    public void stop() {
        this.mHapticPlayer.stop();
    }

    public int getDuration() {
        return this.mHapticPlayer.getDuration();
    }

    public boolean setDynamicCurve(int type, int channelId, HapticCurve curve) {
        if (curve == null || curve.mAdjustPoints == null || curve.mAdjustPoints.isEmpty()) {
            return false;
        }
        return this.mHapticPlayer.setDynamicCurve(type, channelId, HapticCurve.createHwHapticCurve(curve));
    }

    public void setOnPlayStateListener(OnPlayStateListener listener) {
        this.mOnPlayStateListener = listener;
    }
}
