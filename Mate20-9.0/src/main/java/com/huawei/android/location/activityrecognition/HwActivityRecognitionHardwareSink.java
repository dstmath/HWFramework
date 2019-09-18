package com.huawei.android.location.activityrecognition;

public interface HwActivityRecognitionHardwareSink {
    void onActivityChanged(HwActivityChangedEvent hwActivityChangedEvent);

    void onActivityExtendChanged(HwActivityChangedExtendEvent hwActivityChangedExtendEvent);

    void onEnvironmentChanged(HwEnvironmentChangedEvent hwEnvironmentChangedEvent);
}
