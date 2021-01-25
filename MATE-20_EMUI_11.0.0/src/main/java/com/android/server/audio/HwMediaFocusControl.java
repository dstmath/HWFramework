package com.android.server.audio;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.media.AudioFocusInfo;
import android.media.AudioManager;
import android.media.AudioPlaybackConfiguration;
import android.media.AudioRecordingConfiguration;
import android.media.AudioSystem;
import android.media.IAudioFocusChangeDispatcher;
import android.media.IAudioFocusDispatcher;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.pc.IHwPCManager;
import android.text.TextUtils;
import android.util.HwPCUtils;
import android.util.Log;
import com.huawei.android.util.HwPCUtilsEx;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class HwMediaFocusControl extends MediaFocusControl {
    private static final boolean DEBUG = false;
    private static final String DESKTOP_AUDIO_MODE_KEY = "desktop_audio_mode";
    private static final String PHONE_AUDIO_MODE = "1";
    private static final String TAG = "HwMediaFocusControl";
    private final boolean ABANDON = false;
    private final boolean REQUEST = true;
    private final int SYSTEMUID = 1000;
    private final ArrayList<AudioFocusChangeClient> mClients = new ArrayList<>();
    protected volatile boolean mInDestopMode = false;

    public HwMediaFocusControl(Context context, PlayerFocusEnforcer playerFocusEnforcer) {
        super(context, playerFocusEnforcer);
        AudioFocusChangeClient.sHwMediaFocusControl = this;
    }

    /* access modifiers changed from: protected */
    public boolean isMediaForDPExternalDisplay(AudioAttributes audioAttributes, String clientId, String pkgName, int uid) {
        boolean isHiCarMode = HwPCUtilsEx.isHiCarCastMode();
        HwPCUtils.log(TAG, "isMediaForDPExternalDisplay audioAttributes = " + audioAttributes + ", clientId = " + clientId + ", mInDestopMode = " + this.mInDestopMode + ", uid = " + uid);
        if (!this.mInDestopMode || isHiCarMode || "AudioFocus_For_Phone_Ring_And_Calls".compareTo(clientId) == 0 || isDesktopMode(AudioSystem.getDevicesForStream(3))) {
            return false;
        }
        return isPackageRunningOnPc(audioAttributes, pkgName, uid);
    }

    private boolean isPackageRunningOnPc(AudioAttributes audioAttributes, String pkgName, int uid) {
        if (!isMedia(audioAttributes) || TextUtils.isEmpty(pkgName)) {
            return false;
        }
        try {
            IHwPCManager service = HwPCUtils.getHwPCManager();
            if (service == null) {
                return false;
            }
            long token = Binder.clearCallingIdentity();
            try {
                return service.isPackageRunningOnPCMode(pkgName, uid);
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "isMediaForDPExternalDisplay RemoteException");
            return false;
        }
    }

    private boolean isMedia(AudioAttributes audioAttributes) {
        return audioAttributes != null && audioAttributes.getUsage() == 1;
    }

    private boolean isDesktopMode(int device) {
        return (33792 & device) == 0 && "1".equals(AudioSystem.getParameters(DESKTOP_AUDIO_MODE_KEY));
    }

    public void desktopModeChanged(boolean desktopMode) {
        HwPCUtils.log(TAG, "changedToDestopMode desktopMode = " + desktopMode);
        if (desktopMode != this.mInDestopMode) {
            this.mInDestopMode = desktopMode;
        }
    }

    public boolean isPkgInExternalDisplay(String pkgName) {
        HwPCUtils.log(TAG, "isPkgInExternalDisplay pkgName = " + pkgName);
        if (pkgName == null) {
            return false;
        }
        synchronized (mAudioFocusLock) {
            Iterator it = this.mFocusStack.iterator();
            while (it.hasNext()) {
                FocusRequester fr = (FocusRequester) it.next();
                boolean isLargeDisplayApps = isLargeDisplayApp(pkgName, fr);
                if (fr.hasSamePackage(pkgName) && isLargeDisplayApps) {
                    return true;
                }
            }
            return false;
        }
    }

    private boolean isLargeDisplayApp(String pkgName, FocusRequester fr) {
        try {
            IHwPCManager service = HwPCUtils.getHwPCManager();
            if (service == null) {
                return false;
            }
            long token = Binder.clearCallingIdentity();
            try {
                return service.isPackageRunningOnPCMode(pkgName, fr.getClientUid());
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "isPkgInExternalDisplay RemoteException");
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isInDesktopMode() {
        return this.mInDestopMode;
    }

    /* access modifiers changed from: protected */
    public void travelsFocusedStack() {
        Iterator<FocusRequester> stackIterator = this.mFocusStack.iterator();
        while (stackIterator.hasNext()) {
            FocusRequester nextFr = stackIterator.next();
            boolean isInExternalDisplay = isMediaForDPExternalDisplay(nextFr.getAudioAttributes(), nextFr.getClientId(), nextFr.getPackageName(), nextFr.getClientUid());
            HwPCUtils.log(TAG, "travelsFocusedStack isInExternalDisplay = " + isInExternalDisplay);
            nextFr.setIsInExternal(isInExternalDisplay);
        }
    }

    /* access modifiers changed from: protected */
    public boolean isUsageAffectDesktopMedia(int usage) {
        HwPCUtils.log(TAG, " isUsageAffectDesktopMedia usage = " + usage);
        AudioManager audioManager = (AudioManager) this.mContext.getSystemService("audio");
        if (usage == 5 || usage == 1) {
            return true;
        }
        if (usage == 2 && audioManager.getMode() == 3) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    public static final class AudioFocusChangeClient implements IBinder.DeathRecipient {
        static HwMediaFocusControl sHwMediaFocusControl;
        final String mCallback;
        final IAudioFocusChangeDispatcher mDispatcherCb;
        final String mPkgName;

        AudioFocusChangeClient(IAudioFocusChangeDispatcher afc, String callback, String pkgName) {
            this.mDispatcherCb = afc;
            this.mCallback = callback;
            this.mPkgName = pkgName;
        }

        @Override // android.os.IBinder.DeathRecipient
        public void binderDied() {
            Log.i(HwMediaFocusControl.TAG, "AudioFocusChangeClient died");
            sHwMediaFocusControl.unregisterAudioFocusChangeCallback(this.mDispatcherCb, this.mCallback, this.mPkgName);
        }

        /* access modifiers changed from: package-private */
        public boolean init() {
            try {
                this.mDispatcherCb.asBinder().linkToDeath(this, 0);
                return true;
            } catch (RemoteException e) {
                Log.w(HwMediaFocusControl.TAG, "Could not link to client death");
                return false;
            }
        }

        /* access modifiers changed from: package-private */
        public void release() {
            this.mDispatcherCb.asBinder().unlinkToDeath(this, 0);
        }
    }

    public boolean registerAudioFocusChangeCallback(IAudioFocusChangeDispatcher afc, String callback, String pkgName) {
        if (!isSystemApp(pkgName)) {
            Log.e(TAG, "Permission Denial to registerAudioFocusChangeCallback.");
            return false;
        } else if (afc == null) {
            return false;
        } else {
            synchronized (this.mClients) {
                AudioFocusChangeClient afcClient = new AudioFocusChangeClient(afc, callback, pkgName);
                if (afcClient.init()) {
                    this.mClients.add(afcClient);
                    Log.i(TAG, "registerAudioFocusChangeCallback successfully.");
                    return true;
                }
                Log.w(TAG, "registerAudioFocusChangeCallback false");
                return false;
            }
        }
    }

    public boolean unregisterAudioFocusChangeCallback(IAudioFocusChangeDispatcher afc, String callback, String pkgName) {
        if (!isSystemApp(pkgName)) {
            Log.e(TAG, "Permission Denial to unregisterAudioFocusChangeCallback.");
            return false;
        } else if (afc == null) {
            return false;
        } else {
            synchronized (this.mClients) {
                Iterator<AudioFocusChangeClient> clientIterator = this.mClients.iterator();
                while (clientIterator.hasNext()) {
                    AudioFocusChangeClient afcClient = clientIterator.next();
                    if (afcClient != null && afcClient.mDispatcherCb != null && callback.equals(afcClient.mCallback)) {
                        afcClient.release();
                        clientIterator.remove();
                        Log.i(TAG, "unregisterAudioFocusChangeCallback successfully.");
                        return true;
                    }
                }
                Log.w(TAG, "unregisterAudioFocusChangeCallback false");
                return false;
            }
        }
    }

    public void dispatchAudioFocusChange(AudioAttributes attributes, String clientId, int focusType, boolean action) {
        synchronized (this.mClients) {
            if (!this.mClients.isEmpty()) {
                Iterator<AudioFocusChangeClient> clientIterator = this.mClients.iterator();
                while (clientIterator.hasNext()) {
                    AudioFocusChangeClient afcClient = clientIterator.next();
                    if (afcClient != null) {
                        try {
                            if (afcClient.mDispatcherCb != null) {
                                afcClient.mDispatcherCb.dispatchAudioFocusChange(attributes, clientId, focusType, action);
                            }
                        } catch (RemoteException e) {
                            Log.e(TAG, "failed to dispatch audio focus changes");
                        }
                    }
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public int requestAudioFocus(AudioAttributes audioAttributes, int focusChangeHint, IBinder cb, IAudioFocusDispatcher fd, String clientId, String callingPackageName, int flags, int sdk, boolean forceDuck) {
        int res = HwMediaFocusControl.super.requestAudioFocus(audioAttributes, focusChangeHint, cb, fd, clientId, callingPackageName, flags, sdk, forceDuck);
        AudioManager audioManager = (AudioManager) this.mContext.getSystemService("audio");
        if (res == 1 || audioManager.getMode() != 0) {
            dispatchAudioFocusChange(audioAttributes, clientId, focusChangeHint, true);
        }
        return res;
    }

    /* access modifiers changed from: protected */
    public int abandonAudioFocus(IAudioFocusDispatcher fl, String clientId, AudioAttributes audioAttributes, String callingPackageName) {
        int res = HwMediaFocusControl.super.abandonAudioFocus(fl, clientId, audioAttributes, callingPackageName);
        dispatchAudioFocusChange(audioAttributes, clientId, 0, false);
        return res;
    }

    public AudioFocusInfo getAudioFocusInfo(String pkgName) {
        if (!isSystemApp(pkgName)) {
            Log.e(TAG, "Permission Denial to getAudioFocusInfoEx.");
            return null;
        }
        synchronized (mAudioFocusLock) {
            if (this.mFocusStack.empty()) {
                Log.i(TAG, "no audio focus");
                return null;
            }
            FocusRequester fr = (FocusRequester) this.mFocusStack.peek();
            if (fr.getGainRequest() == 1) {
                if (!checkFocusActive(fr)) {
                    return null;
                }
            }
            return fr.toAudioFocusInfo();
        }
    }

    private boolean checkFocusActive(FocusRequester fr) {
        AudioManager audioManager = null;
        if (this.mContext.getSystemService("audio") instanceof AudioManager) {
            audioManager = (AudioManager) this.mContext.getSystemService("audio");
        }
        if (audioManager != null) {
            return isAudioPlayAndRecordFoucusRequester(fr, audioManager.getActivePlaybackConfigurations(), audioManager.getActiveRecordingConfigurations());
        }
        Log.w(TAG, "could not get AudioManager");
        return false;
    }

    private boolean isAudioPlayAndRecordFoucusRequester(FocusRequester fr, List<AudioPlaybackConfiguration> playbackList, List<AudioRecordingConfiguration> recordingList) {
        for (AudioPlaybackConfiguration apc : playbackList) {
            if (isFocusReQuester(fr, apc, null) && apc.isActive()) {
                return true;
            }
        }
        for (AudioRecordingConfiguration arc : recordingList) {
            if (isFocusReQuester(fr, null, arc)) {
                return true;
            }
        }
        Log.i(TAG, "no active audio focus");
        return false;
    }

    private boolean isFocusReQuester(FocusRequester fr, AudioPlaybackConfiguration apc, AudioRecordingConfiguration arc) {
        if (apc != null) {
            return fr != null && fr.getClientUid() == apc.getClientUid();
        }
        if (arc != null) {
            return fr != null && fr.getClientUid() == arc.getClientUid();
        }
        return false;
    }

    private boolean isSystemApp(String pkgName) {
        PackageManager pm = this.mContext.getPackageManager();
        if (pm == null) {
            Log.e(TAG, "could not get PackageManager");
            return false;
        }
        try {
            ApplicationInfo info = pm.getApplicationInfo(pkgName, 0);
            if (info != null && info.uid <= 1000) {
                return true;
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.i(TAG, "AudioException not found app");
        }
        Log.i(TAG, "the caller is not system app");
        return false;
    }
}
