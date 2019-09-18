package com.android.server.wm;

import android.view.SurfaceControl;

interface TransactionFactory {
    SurfaceControl.Transaction make();
}
