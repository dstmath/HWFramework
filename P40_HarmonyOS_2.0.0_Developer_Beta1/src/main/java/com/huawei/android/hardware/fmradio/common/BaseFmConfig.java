package com.huawei.android.hardware.fmradio.common;

public interface BaseFmConfig {
    boolean fmConfigure(int i);

    int getChSpacing();

    int getEmphasis();

    int getLowerLimit();

    int getRadioBand();

    int getRdsStd();

    int getUpperLimit();

    void setChSpacing(int i);

    void setEmphasis(int i);

    void setLowerLimit(int i);

    void setRadioBand(int i);

    void setRdsStd(int i);

    void setUpperLimit(int i);
}
