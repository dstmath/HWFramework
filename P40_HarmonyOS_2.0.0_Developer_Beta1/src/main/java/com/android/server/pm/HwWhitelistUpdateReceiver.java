package com.android.server.pm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.display.HwFoldScreenState;
import android.os.BadParcelableException;
import android.os.ServiceManager;
import android.util.Slog;
import com.android.server.wm.utils.HwDisplaySizeUtil;
import com.huawei.server.HwBasicPlatformFactory;

public class HwWhitelistUpdateReceiver extends BroadcastReceiver {
    private static final String ACTION_CFG_UPDATED = "huawei.android.hwouc.intent.action.CFG_UPDATED";
    private static final String TAG = "HwWhitelistUpdateReceiver";

    @Override // android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
        Slog.i(TAG, "HwWhitelistUpdateReceiver enter.");
        if (context == null || intent == null) {
            Slog.i(TAG, "HwWhitelistUpdateReceiver return null.");
        } else if (ACTION_CFG_UPDATED.equals(intent.getAction())) {
            String type = "";
            String subtype = "";
            try {
                type = intent.getStringExtra("type");
                subtype = intent.getStringExtra("subtype");
            } catch (BadParcelableException e) {
                Slog.e(TAG, "HwWhitelistUpdateReceiver bad parcel.");
            }
            Slog.i(TAG, "HwWhitelistUpdateReceiver type: " + type + ", subtype: " + subtype);
            if ("HwExtDisplay".equals(type)) {
                if ("displayside".equals(subtype)) {
                    if (HwDisplaySizeUtil.hasSideInScreen()) {
                        HwBasicPlatformFactory.loadFactory(HwBasicPlatformFactory.HW_SIDE_TOUCH_PART_FACTORY_IMPL).getHwDisplaySideRegionConfigInstance().updateWhitelistByOuc();
                    }
                } else if ("fold".equals(subtype) && HwFoldScreenState.isFoldScreenDevice()) {
                    ServiceManager.getService("package").getHwPMSEx().updateWhitelistByHot();
                }
            }
        }
    }
}
