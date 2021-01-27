package android.iawareperf;

import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.rms.iaware.AppTypeRecoManager;
import android.rms.iaware.DataContract;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.util.SlogEx;
import com.huawei.hwpartiaware.BuildConfig;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class RtgSchedVideo {
    private static final int DANMAKU_MIN_FPS = SystemPropertiesEx.getInt("persist.sys.rtg.danmaku.fps", 30);
    public static final int EVENT_SCENE_CHANGED = 5;
    private static final int MSG_FPS_SAMPLE = 1;
    private static final String RTG_VIDEO_TYPES = SystemPropertiesEx.get("persist.sys.rtg.video.type", BuildConfig.FLAVOR);
    public static final int SCENE_TYPE_INVALID = 0;
    public static final int SCENE_TYPE_SLIDE = 1;
    public static final int SCENE_TYPE_VIDEO = 2;
    private static final String TAG = "RtgSchedEvent";
    private int mAppType;
    private List<Integer> mAppTypes = new ArrayList();
    private int mAvgFps;
    private boolean mClicked;
    private boolean mEnableAll;
    private AtomicInteger mFps = new AtomicInteger();
    private RtgSchedEventHandle mHandler;
    private boolean mRtgVideoEnable;
    private int mScene;
    private boolean mSliding;

    RtgSchedVideo() {
        boolean z = false;
        if ("all".equals(RTG_VIDEO_TYPES)) {
            this.mEnableAll = true;
        } else {
            String[] types = RTG_VIDEO_TYPES.split(";");
            for (String type : types) {
                if (type.matches("\\d+")) {
                    this.mAppTypes.add(Integer.valueOf(Integer.parseInt(type)));
                }
            }
        }
        this.mRtgVideoEnable = (!this.mAppTypes.isEmpty() || this.mEnableAll) ? true : z;
    }

    private boolean isAppEnable(int type) {
        return this.mEnableAll || this.mAppTypes.contains(Integer.valueOf(type));
    }

    private int getValue(String str, String key) {
        String[] fields = str.split("=");
        if (fields.length != 2 || !key.equals(fields[0])) {
            return -1;
        }
        return Integer.parseInt(fields[1]);
    }

    private void appTypeUpdate() {
        int pid;
        String[] values = SystemPropertiesEx.get("sys.sched.rtg.current", BuildConfig.FLAVOR).split(";");
        if (values.length == 2 && (pid = getValue(values[0], DataContract.BaseProperty.PROCESS_ID)) == Process.myPid()) {
            this.mAppType = getValue(values[1], AppTypeRecoManager.APP_TYPE);
            SlogEx.i(TAG, "current pid:" + pid + " AppType:" + this.mAppType);
        }
    }

    private void doSceneChanged(int newScene) {
        if (this.mScene != newScene) {
            RtgSched.getInstance().sendMmEvent(5, newScene);
            this.mScene = newScene;
        }
    }

    public void setRtgEnable(int enable) {
        stopFpsSample();
        if (enable <= 0) {
            if (isVideoEnable()) {
                doSceneChanged(0);
            }
            this.mAppType = -1;
            return;
        }
        appTypeUpdate();
        if (this.mHandler == null) {
            this.mHandler = new RtgSchedEventHandle();
        }
        if (isAppEnable(this.mAppType)) {
            startFpsSample();
            SlogEx.i(TAG, "start fps sample");
        }
    }

    private void stopFpsSample() {
        if (this.mRtgVideoEnable) {
            RtgSchedEventHandle rtgSchedEventHandle = this.mHandler;
            if (rtgSchedEventHandle != null) {
                rtgSchedEventHandle.removeMessages(1);
            }
            this.mAvgFps = 0;
            this.mFps.set(0);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void startFpsSample() {
        if (this.mRtgVideoEnable) {
            this.mAvgFps = this.mFps.get();
            this.mFps.set(0);
            SlogEx.d(TAG, "current fps:" + this.mAvgFps);
            this.mHandler.sendEmptyMessageDelayed(1, 1000);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isVideoEnable() {
        return isAppEnable(this.mAppType) && this.mAvgFps > DANMAKU_MIN_FPS;
    }

    public void slideBegin() {
        this.mSliding = true;
        updateSceneType();
    }

    public void slideEnd() {
        this.mSliding = false;
        updateSceneType();
    }

    public void clickBegin() {
        this.mClicked = true;
        updateSceneType();
    }

    public void clickEnd() {
        this.mClicked = false;
        updateSceneType();
    }

    public void doFrameStart() {
        this.mFps.getAndIncrement();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateSceneType() {
        if (isAppEnable(this.mAppType)) {
            int scene = 0;
            if (isVideoEnable()) {
                scene = 2;
            }
            if (this.mClicked) {
                scene = 0;
            }
            if (this.mSliding) {
                scene = 1;
            }
            doSceneChanged(scene);
        }
    }

    /* access modifiers changed from: package-private */
    public class RtgSchedEventHandle extends Handler {
        RtgSchedEventHandle() {
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what != 1) {
                SlogEx.d(RtgSchedVideo.TAG, "handleMessage err msg:" + msg.what);
                return;
            }
            RtgSchedVideo.this.startFpsSample();
            RtgSchedVideo.this.updateSceneType();
        }
    }
}
