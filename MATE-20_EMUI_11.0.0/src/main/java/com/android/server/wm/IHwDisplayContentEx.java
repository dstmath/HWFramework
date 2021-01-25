package com.android.server.wm;

public interface IHwDisplayContentEx {
    void focusWinZrHung(WindowStateCommonEx windowStateCommonEx, AppWindowTokenEx appWindowTokenEx, int i);

    boolean isPointOutsideMagicWindow(WindowStateCommonEx windowStateCommonEx, int i, int i2);
}
