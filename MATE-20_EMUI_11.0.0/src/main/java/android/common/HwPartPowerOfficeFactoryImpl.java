package android.common;

import android.content.Context;
import android.pc.AbsHwDecorCaptionView;
import android.pc.HwPCManager;
import android.pc.HwPCManagerImpl;
import com.android.internal.widget.HwDecorCaptionView;

public class HwPartPowerOfficeFactoryImpl extends HwPartPowerOfficeFactory {
    private static final String TAG = "HwPartPowerOfficeFactoryImpl";

    public HwPCManager getHwPCManager() {
        return HwPCManagerImpl.getDefault();
    }

    public AbsHwDecorCaptionView getHwDecorCaptionView(Context context) {
        return new HwDecorCaptionView(context);
    }
}
