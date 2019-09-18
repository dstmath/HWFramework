package com.android.server.input;

import android.os.IBinder;
import com.huawei.android.hardware.input.IHwTHPEventListener;

public interface IHwInputManagerServiceEx {
    void checkHasShowDismissSoftInputAlertDialog(boolean z);

    void registerListener(IHwTHPEventListener iHwTHPEventListener, IBinder iBinder);

    String runHwTHPCommand(String str, String str2);

    void unregisterListener(IHwTHPEventListener iHwTHPEventListener, IBinder iBinder);
}
