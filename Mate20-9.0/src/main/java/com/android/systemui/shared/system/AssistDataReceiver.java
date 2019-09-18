package com.android.systemui.shared.system;

import android.graphics.Bitmap;
import android.os.Bundle;

public abstract class AssistDataReceiver {
    public void onHandleAssistData(Bundle resultData) {
    }

    public void onHandleAssistScreenshot(Bitmap screenshot) {
    }
}
