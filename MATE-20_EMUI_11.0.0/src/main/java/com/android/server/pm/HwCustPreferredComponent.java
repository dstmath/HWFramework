package com.android.server.pm;

import android.content.pm.ActivityInfo;
import com.android.server.pm.PreferredComponent;

public class HwCustPreferredComponent {
    public boolean isSkipHwStarupGuide(PreferredComponent.Callbacks callbacks, ActivityInfo ai) {
        return false;
    }
}
