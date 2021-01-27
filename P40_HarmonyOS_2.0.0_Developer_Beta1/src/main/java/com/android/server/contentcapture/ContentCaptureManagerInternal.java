package com.android.server.contentcapture;

import android.content.ComponentName;
import android.content.ContentCaptureOptions;
import android.os.Bundle;
import android.os.IBinder;

public abstract class ContentCaptureManagerInternal {
    public abstract ContentCaptureOptions getOptionsForPackage(int i, String str);

    public abstract boolean isContentCaptureServiceForUser(int i, int i2);

    public abstract void notifyActivityEvent(int i, ComponentName componentName, int i2);

    public abstract boolean sendActivityAssistData(int i, IBinder iBinder, Bundle bundle);
}
