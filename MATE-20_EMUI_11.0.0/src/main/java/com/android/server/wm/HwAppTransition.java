package com.android.server.wm;

import android.content.Context;

public class HwAppTransition extends AppTransition {
    public HwAppTransition(Context context, WindowManagerService w, DisplayContent displayContent) {
        super(context, w, displayContent);
    }
}
