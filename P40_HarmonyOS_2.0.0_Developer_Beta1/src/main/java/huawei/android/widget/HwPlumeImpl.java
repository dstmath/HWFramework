package huawei.android.widget;

import android.content.Context;
import android.view.View;
import android.widget.HwPlume;
import huawei.android.widget.plume.HwPlumeManager;

public class HwPlumeImpl implements HwPlume {
    private Context mContext;

    public HwPlumeImpl(Context context) {
        this.mContext = context;
    }

    public boolean getBoolean(View view, String attrName, boolean defaultValue) {
        if (!HwPlumeManager.isPlumeUsed(this.mContext)) {
            return defaultValue;
        }
        return HwPlumeManager.getInstance(this.mContext).getDefault(view, attrName, defaultValue);
    }
}
