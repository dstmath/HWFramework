package com.android.server.audio;

import android.media.AudioAttributes;
import android.media.AudioFocusInfo;
import android.media.IAudioFocusDispatcher;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemProperties;
import android.rms.HwSysResManager;
import android.rms.iaware.AwareConstant;
import android.rms.iaware.CollectData;
import com.android.server.audio.MediaFocusControl;

public class HwFocusRequester extends FocusRequester {
    private static final String TAG = "HwFocusRequester";
    private static boolean sIsRecgOptEnable = SystemProperties.getBoolean("persist.sys.iaware.switch.recogOpt", true);
    private int mFocusLoss = 0;

    public boolean getIsInExternal() {
        return this.mIsInExternal;
    }

    public void setIsInExternal(boolean isInExternal) {
        this.mIsInExternal = isInExternal;
    }

    public String getPackageName() {
        return this.mPackageName;
    }

    public HwFocusRequester(AudioAttributes audioAttributes, int focusRequest, int grantFlags, IAudioFocusDispatcher afl, IBinder source, String id, MediaFocusControl.AudioFocusDeathHandler hdlr, String pn, int uid, MediaFocusControl ctlr, int sdk, boolean isInExternal) {
        super(audioAttributes, focusRequest, grantFlags, afl, source, id, hdlr, pn, uid, ctlr, sdk);
        this.mIsInExternal = isInExternal;
        reportFocusGain(focusRequest);
    }

    public HwFocusRequester(AudioFocusInfo afi, IAudioFocusDispatcher afl, IBinder source, MediaFocusControl.AudioFocusDeathHandler hdlr, MediaFocusControl ctlr, boolean isInExternal) {
        super(afi, afl, source, hdlr, ctlr);
        this.mIsInExternal = isInExternal;
        reportFocusGain(afi.getGainRequest());
    }

    /* access modifiers changed from: package-private */
    public void handleFocusGain(int focusGain) {
        reportFocusLoss(0, null);
        HwFocusRequester.super.handleFocusGain(focusGain);
    }

    /* access modifiers changed from: package-private */
    public void handleFocusLoss(int focusLoss, FocusRequester frWinner, boolean forceDuck) {
        reportFocusLoss(focusLoss, frWinner);
        HwFocusRequester.super.handleFocusLoss(focusLoss, frWinner, forceDuck);
    }

    /* access modifiers changed from: package-private */
    public void release() {
        reportFocusGain(0);
        HwFocusRequester.super.release();
    }

    private void reportFocusLoss(int focusLoss, FocusRequester frWinner) {
        if (sIsRecgOptEnable && focusLoss != this.mFocusLoss) {
            this.mFocusLoss = focusLoss;
            if (frWinner == null || frWinner.getClientUid() != getClientUid()) {
                reportAudioFocusForAware(36, focusLoss);
            }
        }
    }

    private void reportFocusGain(int focusRequest) {
        if (sIsRecgOptEnable) {
            reportAudioFocusForAware(35, focusRequest);
        }
    }

    private void reportAudioFocusForAware(int relationType, int stateType) {
        HwSysResManager resManager = HwSysResManager.getInstance();
        if (resManager != null && resManager.isResourceNeeded(AwareConstant.ResourceType.getReousrceId(AwareConstant.ResourceType.RESOURCE_APPASSOC))) {
            Bundle bundleArgs = new Bundle();
            bundleArgs.putInt("state_type", stateType);
            bundleArgs.putInt("callUid", getClientUid());
            bundleArgs.putString("request_name", toString());
            bundleArgs.putInt("relationType", relationType);
            CollectData data = new CollectData(AwareConstant.ResourceType.getReousrceId(AwareConstant.ResourceType.RESOURCE_APPASSOC), System.currentTimeMillis(), bundleArgs);
            long id = Binder.clearCallingIdentity();
            resManager.reportData(data);
            Binder.restoreCallingIdentity(id);
        }
    }
}
