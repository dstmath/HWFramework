package com.android.server.rms.scene;

import android.content.Context;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import com.android.server.rms.IScene;

public class PhoneScene implements IScene {
    private TelephonyManager mTelephonyManager;

    public PhoneScene(Context context) {
        this.mTelephonyManager = (TelephonyManager) context.getSystemService("phone");
    }

    public boolean identify(Bundle extras) {
        if (this.mTelephonyManager != null) {
            return this.mTelephonyManager.isIdle() ^ 1;
        }
        return false;
    }
}
