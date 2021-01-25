package com.android.server.multiwin.listener;

import com.android.server.multiwin.view.HwMultiWinHotAreaView;
import com.android.server.multiwin.view.HwMultiWinSwapAcceptView;

public interface HwMultiWinHotAreaConfigListener {
    HwMultiWinSwapAcceptView onAddHwFreeFormSwapRegion(int i, int i2, HwMultiWinHotAreaView hwMultiWinHotAreaView);
}
