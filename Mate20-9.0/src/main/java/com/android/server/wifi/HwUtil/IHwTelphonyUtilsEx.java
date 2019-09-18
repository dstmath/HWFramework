package com.android.server.wifi.HwUtil;

public interface IHwTelphonyUtilsEx {
    String getCdmaGsmImsi();

    int getDefault4GSlotId();

    boolean isCDMASimCard(int i);

    boolean notifyDeviceState(String str, String str2, String str3);
}
