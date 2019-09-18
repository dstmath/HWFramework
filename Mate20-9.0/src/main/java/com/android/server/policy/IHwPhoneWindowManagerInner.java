package com.android.server.policy;

public interface IHwPhoneWindowManagerInner {
    Object getNavigationBarPolicy();

    int[] getNavigationBarValueForRotation(int i);

    int getRotationValueByType(int i);

    void setNavigationBarHeightDef(int[] iArr);

    void setNavigationBarValueForRotation(int i, int i2, int i3);

    void setNavigationBarWidthDef(int[] iArr);
}
