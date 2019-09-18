package android.app;

import android.content.Context;
import android.graphics.drawable.Icon;
import android.os.SystemProperties;
import android.util.Log;
import android.widget.RemoteViews;
import com.android.internal.R;
import com.android.internal.util.HwNotificationColorUtil;

public class HwNotificationEx implements IHwNotificationEx {
    private static final String HW_SMALL_ICON_TINT_FOR_SYSTEMUI = "hw_small_icon_tint";
    private static final boolean IS_HONOR_PRODUCT = "HONOR".equals(SystemProperties.get("ro.product.brand"));
    private static final boolean IS_NOVA_PRODUCT = SystemProperties.getBoolean("ro.config.hw_novaThemeSupport", false);
    private static final int SMALL_ICON_CAN_BE_TINT = 1;
    private static final int SMALL_ICON_CAN_NOT_BE_TINT = 0;
    private static final String TAG = "HwNotificationEx";
    private Context mContext;
    private HwNotificationColorUtil mHWColorUtil;

    public HwNotificationEx(Context context) {
        this.mContext = context;
        Log.i(TAG, "new HwNotificationEx");
    }

    private HwNotificationColorUtil getHWColorUtil() {
        if (this.mHWColorUtil == null) {
            this.mHWColorUtil = HwNotificationColorUtil.getInstance(this.mContext);
        }
        return this.mHWColorUtil;
    }

    public boolean isPureColorIcon(Notification mN, Icon icon, boolean smallIcon) {
        Log.i(TAG, "isPureColorIcon");
        if (this.mHWColorUtil == null) {
            Log.i(TAG, "mHWColorUtil is null get it agin");
            this.mHWColorUtil = getHWColorUtil();
        }
        int type = this.mHWColorUtil.getSmallIconColorType(this.mContext, icon);
        if (type != 4) {
            switch (type) {
                case 0:
                case 1:
                    if (smallIcon) {
                        mN.extras.putInt(HW_SMALL_ICON_TINT_FOR_SYSTEMUI, 0);
                    }
                    return false;
                case 2:
                    if (smallIcon) {
                        mN.extras.putInt(HW_SMALL_ICON_TINT_FOR_SYSTEMUI, 1);
                    }
                    return true;
                default:
                    if (smallIcon) {
                        mN.extras.putInt(HW_SMALL_ICON_TINT_FOR_SYSTEMUI, 0);
                    }
                    return false;
            }
        } else {
            if (smallIcon) {
                mN.extras.putInt(HW_SMALL_ICON_TINT_FOR_SYSTEMUI, 1);
            }
            return false;
        }
    }

    public void preProcessLineView(RemoteViews contentView, Notification mN) {
        Log.i(TAG, "preProcessLineView");
        contentView.setOnClickPendingIntent(34603331, mN.contentIntent != null ? mN.contentIntent : mN.fullScreenIntent);
        contentView.setTextViewText(16908310, null);
        contentView.setTextViewText(R.id.text, null);
        contentView.setTextViewText(R.id.app_name_text, null);
        contentView.setImageViewResource(16908294, 0);
        contentView.setBoolean(R.id.notification_header, "setExpanded", false);
    }

    public void preProcessRemoteView(String nType, RemoteViews contentView, Notification mN) {
        Log.i(TAG, "preProcessRemoteView");
        PendingIntent intent = mN.contentIntent != null ? mN.contentIntent : mN.fullScreenIntent;
        if ("split_window".equals(nType)) {
            contentView.setViewVisibility(34603331, 0);
            contentView.setViewVisibility(34603061, 8);
            contentView.setOnClickPendingIntent(34603331, intent);
        } else if ("floating_window_notification".equals(nType)) {
            contentView.setViewVisibility(34603331, 8);
            contentView.setViewVisibility(34603061, 0);
            contentView.setOnClickPendingIntent(34603061, intent);
        } else {
            contentView.setViewVisibility(34603331, 8);
            contentView.setViewVisibility(34603061, 8);
        }
        if (IS_HONOR_PRODUCT) {
            contentView.setImageViewResource(34603062, 33752031);
        } else if (IS_NOVA_PRODUCT) {
            contentView.setImageViewResource(34603062, 33752032);
        } else {
            contentView.setImageViewResource(34603062, 33752030);
        }
        contentView.setTextViewText(16908310, null);
        contentView.setTextViewText(R.id.text, null);
        contentView.setTextViewText(R.id.app_name_text, null);
        contentView.setImageViewResource(16908294, 0);
    }
}
