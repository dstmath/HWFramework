package com.android.server.audio;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.media.AudioRecordingConfiguration;
import android.media.HwMediaMonitorManager;
import android.media.IAudioService;
import android.os.Binder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.util.Slog;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class HwScoRecordService {
    private static final int RECORD_STATE_RECORDING = 3;
    private static final String TAG = "HwScoRecordService";
    private IAudioService mAudioService;
    private Binder mBinder = new Binder();
    private final Context mContext;
    private boolean mIsScoForRecordOn = false;
    private Set<Integer> mScoRecordSessionIdSet = new HashSet();
    private boolean mWaitForIntent = false;
    private boolean mWaitForIntentSecond = false;

    public HwScoRecordService(Context context) {
        this.mContext = context;
        this.mContext.registerReceiver(new BroadcastReceiver() {
            /* class com.android.server.audio.HwScoRecordService.AnonymousClass1 */

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                if (intent.getIntExtra("android.media.extra.SCO_AUDIO_STATE", -1) == 1 && (HwScoRecordService.this.mWaitForIntent || HwScoRecordService.this.mWaitForIntentSecond)) {
                    HwMediaMonitorManager.writeBigData(916600030, "", 1, 1);
                    Slog.i(HwScoRecordService.TAG, "receive SCO_AUDIO_STATE_CONNECTED");
                }
                HwScoRecordService.this.mWaitForIntent = false;
                HwScoRecordService.this.mWaitForIntentSecond = false;
            }
        }, new IntentFilter("android.media.SCO_AUDIO_STATE_CHANGED"));
    }

    private IAudioService getAudioService() {
        IAudioService iAudioService = this.mAudioService;
        if (iAudioService != null) {
            return iAudioService;
        }
        this.mAudioService = IAudioService.Stub.asInterface(ServiceManager.getService("audio"));
        return this.mAudioService;
    }

    public synchronized void setBluetoothScoState(int state, int sessionId) {
        if (state == 3) {
            this.mScoRecordSessionIdSet.add(Integer.valueOf(sessionId));
        } else {
            this.mScoRecordSessionIdSet.remove(Integer.valueOf(sessionId));
        }
        Slog.i(TAG, "setBluetoothScoState state: " + state + " sessionId: " + sessionId + " mIsScoForRecordOn: " + this.mIsScoForRecordOn + " sessionSize: " + this.mScoRecordSessionIdSet.size());
        if (this.mIsScoForRecordOn) {
            IAudioService audioService = getAudioService();
            if (audioService == null) {
                Slog.e(TAG, "setBluetoothScoState audioService is null");
                return;
            }
            ApplicationInfo applicationInfo = this.mContext.getApplicationInfo();
            if (applicationInfo == null) {
                Slog.e(TAG, "ApplicationInfo is null");
            } else if (this.mWaitForIntent) {
                Slog.w(TAG, "is waiting for intent SCO_AUDIO_STATE_CHANGED from bluetooth.");
            } else {
                if (state == 3) {
                    try {
                        if (!audioService.isBluetoothScoOn()) {
                            this.mWaitForIntent = true;
                        }
                        audioService.startBluetoothSco(this.mBinder, applicationInfo.targetSdkVersion);
                        Slog.i(TAG, "setBluetoothScoState on");
                    } catch (RemoteException e) {
                        Slog.e(TAG, "setBluetoothScoState change sco state fail");
                    }
                } else if (this.mScoRecordSessionIdSet.isEmpty()) {
                    audioService.stopBluetoothSco(this.mBinder);
                    Slog.i(TAG, "setBluetoothScoState off");
                }
            }
        }
    }

    public synchronized void setBtScoForRecord(boolean on) {
        int callerUid = Binder.getCallingUid();
        if (isSystemApp(callerUid)) {
            Slog.i(TAG, "setBtScoForRecord " + on + ", old state: " + this.mIsScoForRecordOn);
            this.mIsScoForRecordOn = on;
            if (!this.mScoRecordSessionIdSet.isEmpty()) {
                IAudioService audioService = getAudioService();
                if (audioService == null) {
                    Slog.e(TAG, "audioService is null");
                    return;
                }
                ApplicationInfo applicationInfo = this.mContext.getApplicationInfo();
                if (applicationInfo == null) {
                    Slog.e(TAG, "ApplicationInfo is null");
                    return;
                }
                if (on) {
                    try {
                        if (!audioService.isBluetoothScoOn()) {
                            this.mWaitForIntentSecond = true;
                        }
                        audioService.startBluetoothSco(this.mBinder, applicationInfo.targetSdkVersion);
                        Slog.i(TAG, "start sco when have session size: " + this.mScoRecordSessionIdSet.size());
                    } catch (RemoteException e) {
                        Slog.e(TAG, "setBtScoForRecord change sco fail");
                    }
                } else {
                    audioService.stopBluetoothSco(this.mBinder);
                    Slog.i(TAG, "stop sco when have session size: " + this.mScoRecordSessionIdSet.size());
                }
                return;
            }
            return;
        }
        Slog.e(TAG, "setBtScoForRecord have no permission of uid:" + callerUid);
        throw new SecurityException("Process with uid=" + callerUid + " cannot call function setBtScoForRecord.");
    }

    public synchronized void checkScoRecording(List<AudioRecordingConfiguration> configs) {
        if (!this.mScoRecordSessionIdSet.isEmpty()) {
            Set<Integer> sessionIds = new HashSet<>(configs.size());
            for (AudioRecordingConfiguration config : configs) {
                sessionIds.add(Integer.valueOf(config.getClientAudioSessionId()));
            }
            Iterator<Integer> iterator = this.mScoRecordSessionIdSet.iterator();
            while (iterator.hasNext()) {
                int sessionId = iterator.next().intValue();
                if (!sessionIds.contains(Integer.valueOf(sessionId))) {
                    iterator.remove();
                    Slog.i(TAG, "session maybe kill of " + sessionId);
                }
            }
            IAudioService audioService = getAudioService();
            if (audioService == null) {
                Slog.e(TAG, "checkScoRecording audioService is null");
                return;
            }
            if (this.mScoRecordSessionIdSet.isEmpty()) {
                Slog.i(TAG, "sessions is empty, stop sco");
                try {
                    audioService.stopBluetoothSco(this.mBinder);
                } catch (RemoteException e) {
                    Slog.e(TAG, "checkScoRecording stopBluetoothSco fail");
                }
            }
        }
    }

    private boolean isSystemApp(int uid) {
        if (UserHandle.getAppId(uid) == 1000) {
            return true;
        }
        PackageManager pm = this.mContext.getPackageManager();
        if (pm == null) {
            Slog.w(TAG, "PackageManager is null");
            return false;
        }
        try {
            String[] pkgNames = pm.getPackagesForUid(uid);
            if (pkgNames != null) {
                if (pkgNames.length > 0) {
                    ApplicationInfo info = pm.getApplicationInfo(pkgNames[0], 0);
                    if (info != null && (info.flags & 1) != 0) {
                        return true;
                    }
                    Slog.i(TAG, "Not system app for uid: " + uid);
                    return false;
                }
            }
            Slog.w(TAG, "pkgNames is empty for uid: " + uid);
            return false;
        } catch (PackageManager.NameNotFoundException e) {
            Slog.i(TAG, "AudioException not found for uid:" + uid);
        } catch (Exception e2) {
            Slog.i(TAG, "AudioException not found for uid:" + uid);
        }
    }
}
