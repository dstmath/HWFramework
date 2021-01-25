package android.common;

import android.magicwin.HwMagicWindow;
import android.magicwin.HwMagicWindowManagerImpl;

public class HwPartMagicWindowFactoryImpl extends HwPartMagicWindowFactory {
    private static final String TAG = "HWMW_HwPartMagicWindowFactoryImpl";

    public HwMagicWindow getHwMagicWindowManager() {
        return HwMagicWindowManagerImpl.getInstance();
    }
}
