package huawei.android.widget;

import android.content.res.ColorStateList;

public interface HwSmartColorListener {
    ColorStateList getSmartIconColor();

    ColorStateList getSmartTitleColor();

    void onSetSmartColor(ColorStateList colorStateList, ColorStateList colorStateList2);
}
