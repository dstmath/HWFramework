package com.android.server.audio;

public interface IHwAudioServiceInner {
    boolean checkAudioSettingsPermissionEx(String str);

    boolean isConnectedHeadPhoneEx();

    boolean isConnectedHeadSetEx();

    boolean isConnectedUsbInDeviceEx();

    boolean isConnectedUsbOutDeviceEx();
}
