package com.android.server.devicepolicy;

public class HwActiveAdminFactoryImpl extends HwActiveAdminFactory {
    public HwActiveAdmin getHwActiveAdmin() {
        return new HwActiveAdminImpl();
    }
}
