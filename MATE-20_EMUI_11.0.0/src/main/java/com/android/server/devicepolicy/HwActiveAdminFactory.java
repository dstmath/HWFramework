package com.android.server.devicepolicy;

import android.util.Slog;

public class HwActiveAdminFactory {
    private static final String ACTIVE_ADMIN_FACTORY_IMPL_NAME = "com.android.server.devicepolicy.HwActiveAdminFactoryImpl";
    private static final String LOG_TAG = "HwActiveAdminFactory";
    private static HwActiveAdminFactory sFactory;

    public static HwActiveAdminFactory loadFactory() {
        HwActiveAdminFactory hwActiveAdminFactory = sFactory;
        if (hwActiveAdminFactory != null) {
            return hwActiveAdminFactory;
        }
        Object object = null;
        try {
            object = Class.forName(ACTIVE_ADMIN_FACTORY_IMPL_NAME).newInstance();
        } catch (ClassNotFoundException e) {
            Slog.e(LOG_TAG, "loadFactory() ClassNotFoundException !");
        } catch (InstantiationException e2) {
            Slog.e(LOG_TAG, "loadFactory() InstantiationException !");
        } catch (IllegalAccessException e3) {
            Slog.e(LOG_TAG, "loadFactory() IllegalAccessException !");
        } catch (Exception e4) {
            Slog.e(LOG_TAG, "loadFactory() Exception !");
        }
        if (object == null || !(object instanceof HwActiveAdminFactory)) {
            Slog.i(LOG_TAG, "Create default factory for mdm part is not exist.");
            sFactory = new HwActiveAdminFactory();
        } else {
            Slog.i(LOG_TAG, "Create actual factory for mdm part.");
            sFactory = (HwActiveAdminFactory) object;
        }
        return sFactory;
    }

    public HwActiveAdmin getHwActiveAdmin() {
        return new HwActiveAdmin();
    }
}
