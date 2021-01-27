package com.huawei.server.hwmultidisplay.audio;

import android.content.Context;
import android.media.AudioManager;
import android.os.Binder;
import android.util.HwPCUtils;
import com.android.server.am.HwActivityManagerService;
import com.huawei.android.media.AudioManagerEx;
import com.huawei.android.media.DeviceSelectCallback;
import com.huawei.server.hwmultidisplay.HwMultiDisplayUtils;
import com.huawei.server.pc.HwPCManagerService;
import com.huawei.util.LogEx;

public class HwMultiDisplayAudioManager {
    private static final boolean DEBUG = LogEx.getLogHWInfo();
    private static final int DESKTOP_MODE = 0;
    private static final int MULTI_OUTPUT_AUTO_MODE = 2;
    private static final int PHONE_MODE = 1;
    private static final String SYS_PC_AUDIO_MODE = "desktop_audio_mode";
    private static final String TAG = "HwMultiDisplayAudioManager";
    private HwActivityManagerService mAMS;
    private AudioManager mAudioService = ((AudioManager) this.mContext.getSystemService("audio"));
    private Context mContext;
    private PcDeviceSelectCallback mDeviceSelectCallback = new PcDeviceSelectCallback();
    private HwPCManagerService mPCService;

    public HwMultiDisplayAudioManager(Context context, HwPCManagerService service, HwActivityManagerService ams) {
        this.mContext = context;
        this.mPCService = service;
        this.mAMS = ams;
    }

    public void registerAudioDeviceSelect() {
        if (this.mDeviceSelectCallback != null) {
            HwPCUtils.log(TAG, "register AudioDeviceSelect Callback");
            AudioManagerEx.registerAudioDeviceSelectCallback(this.mDeviceSelectCallback);
        }
    }

    public void unregisterAudioDeviceSelect() {
        if (this.mDeviceSelectCallback != null) {
            HwPCUtils.log(TAG, "unregister AudioDeviceSelect Callback");
            AudioManagerEx.unregisterAudioDeviceSelectCallback(this.mDeviceSelectCallback);
        }
    }

    /* access modifiers changed from: package-private */
    public class PcDeviceSelectCallback extends DeviceSelectCallback {
        PcDeviceSelectCallback() {
        }

        public int onSelectDevice(int pid, int uid, int content, int usage, int sessionId) {
            int audioMode = Integer.parseInt(HwMultiDisplayAudioManager.this.mAudioService.getParameters(HwMultiDisplayAudioManager.SYS_PC_AUDIO_MODE));
            HwPCUtils.log(HwMultiDisplayAudioManager.TAG, "AudioMode:" + audioMode);
            if (audioMode == 0) {
                HwPCUtils.log(HwMultiDisplayAudioManager.TAG, "Desktop mode: set OUTPUT_NONE");
                return 0;
            } else if (audioMode == 1) {
                HwPCUtils.log(HwMultiDisplayAudioManager.TAG, "Phone mode: set OUTPUT_NONE");
                return 0;
            } else if (audioMode != 2) {
                HwPCUtils.log(HwMultiDisplayAudioManager.TAG, "Audio mode not found: set OUTPUT_NONE");
                return 0;
            } else if (!HwMultiDisplayAudioManager.this.isPackageRunningOnPCMode(pid, uid)) {
                HwPCUtils.log(HwMultiDisplayAudioManager.TAG, "Multi output mode: app is running on phone display, set OUTPUT_NONE");
                return 0;
            } else if (HwPCUtils.getIsWifiMode()) {
                HwPCUtils.log(HwMultiDisplayAudioManager.TAG, "Multi output mode: app is running on PC Wifi display, set OUTPUT_REMOTE_SUBMIX");
                return 5;
            } else {
                HwPCUtils.log(HwMultiDisplayAudioManager.TAG, "Multi output mode: app is running on PC Wired display, set OUTPUT_HDMI");
                return 4;
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isPackageRunningOnPCMode(int pid, int uid) {
        long token = Binder.clearCallingIdentity();
        try {
            String pkg = HwMultiDisplayUtils.getPackageNameByPid(this.mContext, pid);
            if (DEBUG) {
                HwPCUtils.log(TAG, "app:" + pkg);
            }
            return this.mAMS.isPackageRunningOnPCMode(pkg, uid);
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }
}
