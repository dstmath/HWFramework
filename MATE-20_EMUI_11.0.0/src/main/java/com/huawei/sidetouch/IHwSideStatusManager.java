package com.huawei.sidetouch;

import android.view.KeyEvent;

public interface IHwSideStatusManager {
    boolean isVolumeTriggered();

    void resetVolumeTriggerStatus();

    void updateVolumeTriggerStatus(KeyEvent keyEvent);
}
