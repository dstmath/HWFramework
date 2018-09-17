package com.android.server.rms.scene;

import android.content.Context;
import android.os.Bundle;
import android.os.PowerManager;
import com.android.server.rms.IScene;

public class NonIdleScene implements IScene {
    private PowerManager mPowerManager;

    public NonIdleScene(Context context) {
        this.mPowerManager = (PowerManager) context.getSystemService("power");
    }

    public boolean identify(Bundle extras) {
        if (this.mPowerManager != null) {
            return this.mPowerManager.isInteractive();
        }
        return false;
    }
}
