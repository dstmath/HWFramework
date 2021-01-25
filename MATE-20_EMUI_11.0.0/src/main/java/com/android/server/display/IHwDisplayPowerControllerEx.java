package com.android.server.display;

import android.animation.Animator;
import android.os.Bundle;
import android.os.PowerManager;

public interface IHwDisplayPowerControllerEx {

    public interface Callbacks {
        AutomaticBrightnessController getAutomaticBrightnessController();

        ManualBrightnessController getManualBrightnessController();

        void onTpKeepStateChanged(boolean z);
    }

    boolean checkDawnAnimationOnIsStarted();

    boolean checkDawnAnimationStarted();

    boolean getHwBrightnessData(String str, Bundle bundle, int[] iArr);

    boolean getTpKeep();

    void initTpKeepParamters();

    void initializeDawnAnimator(DisplayPowerState displayPowerState, Animator.AnimatorListener animatorListener);

    void inwardFoldDeviceDawnAnimation(PowerManager.WakeData wakeData, DisplayPowerState displayPowerState, int i);

    void sendProximityBroadcast(boolean z);

    boolean setHwBrightnessData(String str, Bundle bundle, int[] iArr);

    void setTPDozeMode(boolean z);

    void startInwardFoldDeviceDawnAnimation(PowerManager.WakeData wakeData, DisplayPowerState displayPowerState, int i);

    void updateColorFadeDawnAnimationAnimator(String str, boolean z);
}
