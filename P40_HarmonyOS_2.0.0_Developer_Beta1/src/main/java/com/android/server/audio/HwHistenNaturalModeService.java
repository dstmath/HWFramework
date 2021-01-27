package com.android.server.audio;

import android.content.Context;
import android.media.AudioDeviceCallback;
import android.media.AudioDeviceInfo;
import android.media.AudioPlaybackConfiguration;
import android.media.AudioSystem;
import android.media.IPlaybackConfigDispatcher;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.util.Slog;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class HwHistenNaturalModeService {
    private static final int DEFAULT_MODE = 0;
    private static final int DEFAULT_SPLIT_LEN_ONE = 1;
    private static final int DEFAULT_SPLIT_LEN_TWO = 2;
    private static final int DEFAULT_SPLIT_LEN_ZERO = 0;
    private static final int DEVICE_OUT_BLUETOOTH_A2DP = 128;
    private static final int DEVICE_OUT_USB_DEVICE = 16384;
    private static final int DEVICE_OUT_USB_DEVICE_EXTENDED = 536870912;
    private static final int DEVICE_OUT_USB_HEADSET = 67108864;
    private static final int DEVICE_OUT_WIRED_HEADPHONE = 8;
    private static final int DEVICE_OUT_WIRED_HEADSET = 4;
    private static final String HW_MEDIACENTER_PKGNAME = "com.android.mediacenter";
    private static final String HW_MUSIC_PKGNAME = "com.huawei.music";
    private static final int NATURAL_SWS_MODE = 2;
    private static final String SWS_HP_MODE = "HP_MODE";
    private static final boolean SWS_SOUND_EFFECTS_SUPPORT = SystemProperties.getBoolean("ro.config.hw_sws", false);
    private static final String TAG = "HwHistenNaturalModeService";
    private final Context mContext;
    private boolean mIsDeviceSupportSws = false;
    private boolean mIsHwMusicOpenNaturalMode = false;
    private boolean mIsHwMusicStarted = true;
    private int mLastSelectSwsMode = -1;
    private final ArrayList<SetMusicDeathHandler> mSetMusicDeathHandler = new ArrayList<>();
    private final Object mSetParameterSwsLock = new Object();
    public AudioDeviceCallback sAudioDeviceCallback = new AudioDeviceCallback() {
        /* class com.android.server.audio.HwHistenNaturalModeService.AnonymousClass2 */

        @Override // android.media.AudioDeviceCallback
        public void onAudioDevicesAdded(AudioDeviceInfo[] addedDevices) {
            HwHistenNaturalModeService.this.getHeadSetStatus();
            super.onAudioDevicesAdded(addedDevices);
        }

        @Override // android.media.AudioDeviceCallback
        public void onAudioDevicesRemoved(AudioDeviceInfo[] removedDevices) {
            HwHistenNaturalModeService.this.getHeadSetStatus();
            super.onAudioDevicesRemoved(removedDevices);
        }
    };
    public final IPlaybackConfigDispatcher sPlaybackListener = new IPlaybackConfigDispatcher.Stub() {
        /* class com.android.server.audio.HwHistenNaturalModeService.AnonymousClass1 */

        public void dispatchPlaybackConfigChange(List<AudioPlaybackConfiguration> configs, boolean flush) {
            for (AudioPlaybackConfiguration apc : configs) {
                String pkgName = apc.getPkgName();
                int playerState = apc.getPlayerState();
                if (HwHistenNaturalModeService.HW_MEDIACENTER_PKGNAME.equals(pkgName) || HwHistenNaturalModeService.HW_MUSIC_PKGNAME.equals(pkgName)) {
                    if (playerState == 2 && HwHistenNaturalModeService.this.mIsDeviceSupportSws && HwHistenNaturalModeService.this.isSwsPermissionAvailable() && HwHistenNaturalModeService.this.mIsHwMusicOpenNaturalMode) {
                        HwHistenNaturalModeService.this.mIsHwMusicStarted = true;
                    }
                    if ((playerState == 3 || playerState == 4 || playerState == 1) && HwHistenNaturalModeService.this.mIsDeviceSupportSws && HwHistenNaturalModeService.this.isSwsPermissionAvailable() && HwHistenNaturalModeService.this.mIsHwMusicOpenNaturalMode) {
                        int swsMode = HwHistenNaturalModeService.this.getLastSelectedSwsMode(AudioSystem.getParameters(HwHistenNaturalModeService.SWS_HP_MODE));
                        if (!(swsMode == 2 || swsMode == HwHistenNaturalModeService.this.mLastSelectSwsMode)) {
                            Slog.i(HwHistenNaturalModeService.TAG, "Audio playback stopped, Sws last get Mode:" + swsMode);
                            HwHistenNaturalModeService.this.mLastSelectSwsMode = swsMode;
                        }
                        HwHistenNaturalModeService.this.mIsHwMusicStarted = false;
                        Slog.i(HwHistenNaturalModeService.TAG, "Audio playback stopped, Sws last set mode:" + HwHistenNaturalModeService.this.mLastSelectSwsMode);
                        HwHistenNaturalModeService.this.setHistenNaturalMode(false, null);
                        return;
                    }
                }
            }
        }
    };
    private int startUid = -1;

    public HwHistenNaturalModeService(Context context) {
        this.mContext = context;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isSwsPermissionAvailable() {
        if (this.mContext.checkCallingOrSelfPermission("sws.permission.START_SWSHEADPHONE") != 0) {
            return false;
        }
        return true;
    }

    private void devicesSupport() {
        int device = AudioSystem.getDevicesForStream(3);
        if (device == 4 || device == 8 || device == 128 || device == 16384 || device == 67108864 || device == DEVICE_OUT_USB_DEVICE_EXTENDED) {
            this.mIsDeviceSupportSws = true;
        } else {
            this.mIsDeviceSupportSws = false;
        }
        Slog.i(TAG, "getDevicesStatus mIsDeviceSupportSws: " + this.mIsDeviceSupportSws);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void getHeadSetStatus() {
        if (SWS_SOUND_EFFECTS_SUPPORT) {
            devicesSupport();
            if (this.mIsDeviceSupportSws && this.mIsHwMusicStarted) {
                if (this.mLastSelectSwsMode == -1) {
                    this.mLastSelectSwsMode = getLastSelectedSwsMode(AudioSystem.getParameters(SWS_HP_MODE));
                }
                setHistenNaturalMode(true, null);
            }
            if (!this.mIsDeviceSupportSws) {
                this.mIsHwMusicStarted = false;
                this.mLastSelectSwsMode = getLastSelectedSwsMode(AudioSystem.getParameters(SWS_HP_MODE));
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int getLastSelectedSwsMode(String keyValuePairs) {
        int mode = 0;
        String[] strSplit1 = keyValuePairs.split("\\=");
        if (strSplit1.length == 0 || strSplit1.length == 1) {
            mode = 0;
        }
        if (strSplit1.length != 2) {
            return mode;
        }
        String[] strSplit2 = strSplit1[1].split("\\.");
        if (strSplit2.length == 0) {
            return 0;
        }
        return Integer.parseInt(strSplit2[0]);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private String getKeyValuesPairs(int mode) {
        return "HP_MODE=" + mode;
    }

    /* access modifiers changed from: protected */
    public class SetMusicDeathHandler implements IBinder.DeathRecipient {
        private IBinder mCb;
        private int mPid;

        SetMusicDeathHandler(IBinder cb, int pid) {
            this.mCb = cb;
            this.mPid = pid;
        }

        @Override // android.os.IBinder.DeathRecipient
        public void binderDied() {
            synchronized (HwHistenNaturalModeService.this.mSetParameterSwsLock) {
                if (!HwHistenNaturalModeService.this.mIsHwMusicStarted) {
                    HwHistenNaturalModeService.this.mLastSelectSwsMode = HwHistenNaturalModeService.this.getLastSelectedSwsMode(AudioSystem.getParameters(HwHistenNaturalModeService.SWS_HP_MODE));
                }
                String keyValuePairs = HwHistenNaturalModeService.this.getKeyValuesPairs(HwHistenNaturalModeService.this.mLastSelectSwsMode);
                Slog.i(HwHistenNaturalModeService.TAG, "Audio playback client died, Sws last keyValuePairs:" + keyValuePairs);
                HwHistenNaturalModeService.this.setHistenNaturalMode(false, null);
            }
        }

        public int getPid() {
            return this.mPid;
        }

        public IBinder getBinder() {
            return this.mCb;
        }
    }

    public void setHistenNaturalMode(boolean isCloseHisten, IBinder cb) {
        if (!isSwsPermissionAvailable()) {
            Slog.w(TAG, "Sws permission is unavailable");
            return;
        }
        this.mIsHwMusicOpenNaturalMode = isCloseHisten;
        this.mIsHwMusicStarted = isCloseHisten;
        if (cb != null) {
            if (this.mIsDeviceSupportSws) {
                int swsMode = getLastSelectedSwsMode(AudioSystem.getParameters(SWS_HP_MODE));
                if (swsMode == 2) {
                    int i = this.mLastSelectSwsMode;
                    if (i == -1) {
                        i = swsMode;
                    }
                    this.mLastSelectSwsMode = i;
                } else {
                    this.mLastSelectSwsMode = swsMode;
                }
                Slog.i(TAG, "Audio playback started, Sws get mLastSelectSwsMode:" + this.mLastSelectSwsMode + ",swsMode:" + swsMode);
            }
            int pid = Binder.getCallingPid();
            SetMusicDeathHandler hdlr = null;
            Iterator iter = this.mSetMusicDeathHandler.iterator();
            while (true) {
                if (!iter.hasNext()) {
                    break;
                }
                Object object = iter.next();
                if (object instanceof SetMusicDeathHandler) {
                    SetMusicDeathHandler handler = (SetMusicDeathHandler) object;
                    if (handler.getPid() == pid) {
                        hdlr = handler;
                        iter.remove();
                        hdlr.getBinder().unlinkToDeath(hdlr, 0);
                        break;
                    }
                }
            }
            if (hdlr == null) {
                hdlr = new SetMusicDeathHandler(cb, pid);
            }
            try {
                cb.linkToDeath(hdlr, 0);
            } catch (RemoteException e) {
                Slog.w(TAG, "Sws could not link to " + cb + " binder death");
            }
            this.mSetMusicDeathHandler.add(0, hdlr);
        }
        setParameterForHisten(isCloseHisten);
    }

    private void setParameterForHisten(boolean isCloseHisten) {
        if (!this.mIsDeviceSupportSws) {
            Slog.w(TAG, "Sws device is not available");
        } else if (isCloseHisten) {
            AudioSystem.setParameters(getKeyValuesPairs(2));
        } else {
            AudioSystem.setParameters(getKeyValuesPairs(this.mLastSelectSwsMode));
        }
    }
}
