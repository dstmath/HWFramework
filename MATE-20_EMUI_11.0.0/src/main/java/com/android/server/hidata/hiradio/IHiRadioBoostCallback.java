package com.android.server.hidata.hiradio;

public interface IHiRadioBoostCallback {
    public static final int HIRADIO_4G_SWITCH_3G_FAILED = 1;
    public static final int HIRADIO_4G_SWITCH_3G_SUCCESS = 0;

    void LTEto3GResult(int i, int i2, int i3);
}
