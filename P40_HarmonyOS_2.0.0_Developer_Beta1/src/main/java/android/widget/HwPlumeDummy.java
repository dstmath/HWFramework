package android.widget;

import android.view.View;

public class HwPlumeDummy implements HwPlume {
    @Override // android.widget.HwPlume
    public boolean getBoolean(View view, String attrName, boolean defaultValue) {
        return defaultValue;
    }
}
