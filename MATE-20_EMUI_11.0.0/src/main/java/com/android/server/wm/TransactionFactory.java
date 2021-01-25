package com.android.server.wm;

import android.view.SurfaceControl;

public interface TransactionFactory {
    SurfaceControl.Transaction make();
}
