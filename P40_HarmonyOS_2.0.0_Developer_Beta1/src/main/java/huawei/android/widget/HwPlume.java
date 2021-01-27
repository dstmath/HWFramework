package huawei.android.widget;

import android.content.Context;
import android.view.View;
import huawei.android.widget.plume.HwPlumeManager;

public class HwPlume {
    private HwPlume() {
    }

    public static boolean getBoolean(Context context, View view, String attrName, boolean defaultValue) {
        if (!HwPlumeManager.isPlumeUsed(context)) {
            return defaultValue;
        }
        return HwPlumeManager.getInstance(context).getDefault(view, attrName, defaultValue);
    }
}
