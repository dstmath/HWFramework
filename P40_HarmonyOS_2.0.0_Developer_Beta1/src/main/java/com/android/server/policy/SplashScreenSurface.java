package com.android.server.policy;

import android.os.IBinder;
import android.view.View;
import android.view.WindowManager;
import com.android.server.policy.WindowManagerPolicy;

class SplashScreenSurface implements WindowManagerPolicy.StartingSurface {
    private static final String TAG = "WindowManager";
    private final IBinder mAppToken;
    private final View mView;

    SplashScreenSurface(View view, IBinder appToken) {
        this.mView = view;
        this.mAppToken = appToken;
    }

    @Override // com.android.server.policy.WindowManagerPolicy.StartingSurface
    public void remove() {
        ((WindowManager) this.mView.getContext().getSystemService(WindowManager.class)).removeView(this.mView);
    }
}
