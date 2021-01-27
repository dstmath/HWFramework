package android.app;

import android.app.Notification;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Icon;
import android.os.SystemProperties;
import android.util.Log;
import android.util.SplitNotificationUtils;
import android.widget.RemoteViews;
import com.android.hwext.internal.R;
import com.android.internal.util.HwNotificationColorUtil;

public class HwNotificationEx implements IHwNotificationEx {
    public static final String EXTRA_SHOW_ACTION_ICON = "android.extraShowActionIcon";
    private static final String HW_SMALL_ICON_TINT_FOR_SYSTEMUI = "hw_small_icon_tint";
    private static final boolean IS_HONOR_PRODUCT = "HONOR".equals(SystemProperties.get("ro.product.brand"));
    private static final boolean IS_NOVA_PRODUCT = SystemProperties.getBoolean("ro.config.hw_novaThemeSupport", false);
    private static final Object LOCK_OBJECT = new Object();
    private static final int SMALL_ICON_CAN_BE_TINT = 1;
    private static final int SMALL_ICON_CAN_NOT_BE_TINT = 0;
    private static final int SMALL_ICON_SOLID_COLOR = 2;
    private static final String TAG = "HwNotificationEx";
    private Context mContext;
    private HwNotificationColorUtil mHwColorUtil;

    public HwNotificationEx(Context context) {
        this.mContext = context;
    }

    @Override // android.app.IHwNotificationEx
    public boolean isPureColorIcon(Notification notification, Icon icon, boolean isSmallIcon) {
        if (notification == null) {
            Log.e(TAG, "isPureColorIcon, notification is null!!!");
            return false;
        }
        if (this.mHwColorUtil == null) {
            this.mHwColorUtil = HwNotificationColorUtil.getInstance(this.mContext);
        }
        int type = this.mHwColorUtil.getSmallIconColorType(this.mContext, icon);
        synchronized (LOCK_OBJECT) {
            if (type == 0 || type == 1) {
                if (isSmallIcon) {
                    notification.extras.putInt(HW_SMALL_ICON_TINT_FOR_SYSTEMUI, 0);
                }
                return false;
            } else if (type == 2) {
                if (isSmallIcon) {
                    notification.extras.putInt(HW_SMALL_ICON_TINT_FOR_SYSTEMUI, 1);
                }
                return true;
            } else if (type != 4) {
                if (isSmallIcon) {
                    try {
                        notification.extras.putInt(HW_SMALL_ICON_TINT_FOR_SYSTEMUI, 0);
                    } catch (Throwable th) {
                        throw th;
                    }
                }
                return false;
            } else {
                if (isSmallIcon) {
                    notification.extras.putInt(HW_SMALL_ICON_TINT_FOR_SYSTEMUI, 2);
                }
                return false;
            }
        }
    }

    @Override // android.app.IHwNotificationEx
    public void preProcessLineView(RemoteViews contentView, Notification notification) {
        if (contentView == null || notification == null) {
            Log.e(TAG, "preProcessLineView, contentView or notification is null!!!");
            return;
        }
        Log.i(TAG, "preProcessLineView");
        contentView.setOnClickPendingIntent(R.id.line_img, notification.contentIntent != null ? notification.contentIntent : notification.fullScreenIntent);
        contentView.setTextViewText(16908310, null);
        contentView.setTextViewText(com.android.internal.R.id.text, null);
        contentView.setTextViewText(com.android.internal.R.id.app_name_text, null);
        contentView.setImageViewResource(16908294, 0);
        contentView.setBoolean(com.android.internal.R.id.notification_header, "setExpanded", false);
    }

    @Override // android.app.IHwNotificationEx
    public void preProcessRemoteView(String type, RemoteViews contentView, Notification notification) {
        if (contentView == null || notification == null) {
            Log.e(TAG, "preProcessRemoteView, contentView or notification is null!!!");
            return;
        }
        Log.i(TAG, "preProcessRemoteView");
        PendingIntent intent = notification.contentIntent != null ? notification.contentIntent : notification.fullScreenIntent;
        if (SplitNotificationUtils.SPLIT_WINDOW.equals(type)) {
            contentView.setViewVisibility(R.id.line_img, 0);
            contentView.setViewVisibility(R.id.float_window_img, 8);
            contentView.setOnClickPendingIntent(R.id.line_img, intent);
        } else if (SplitNotificationUtils.FLOATING_WINDOW_NOTIFICATION.equals(type)) {
            contentView.setViewVisibility(R.id.line_img, 8);
            contentView.setViewVisibility(R.id.float_window_img, 0);
            contentView.setOnClickPendingIntent(R.id.float_window_img, intent);
        } else {
            contentView.setViewVisibility(R.id.line_img, 8);
            contentView.setViewVisibility(R.id.float_window_img, 8);
        }
        if (IS_HONOR_PRODUCT) {
            contentView.setImageViewResource(R.id.float_window_img_sc, R.drawable.ic_notification_float_selector_honor);
        } else if (IS_NOVA_PRODUCT) {
            contentView.setImageViewResource(R.id.float_window_img_sc, R.drawable.ic_notification_float_selector_nova);
        } else {
            contentView.setImageViewResource(R.id.float_window_img_sc, R.drawable.ic_notification_float_selector);
        }
        contentView.setTextViewText(16908310, null);
        contentView.setTextViewText(com.android.internal.R.id.text, null);
        contentView.setTextViewText(com.android.internal.R.id.app_name_text, null);
        contentView.setImageViewResource(16908294, 0);
    }

    @Override // android.app.IHwNotificationEx
    public void preActionButton(Notification.Action action, RemoteViews button, Notification notification) {
        if (action == null || button == null || notification == null) {
            Log.e(TAG, "preActionButton, action or button or notification is null!!!");
            return;
        }
        Log.i(TAG, "preActionButton");
        if (notification.extras.getBoolean("android.extraShowActionIcon")) {
            int actionIconLength = this.mContext.getResources().getDimensionPixelSize(34472164);
            button.setTextViewCompoundDrawablesWithBounds(com.android.internal.R.id.action0, action.getIcon(), null, null, null, actionIconLength, actionIconLength, this.mContext.getResources().getDimensionPixelSize(34472165));
        }
        try {
            if (IS_HONOR_PRODUCT) {
                button.setTextColor(com.android.internal.R.id.action0, this.mContext.getResources().getColorStateList(33882454, null));
            } else if (IS_NOVA_PRODUCT) {
                button.setTextColor(com.android.internal.R.id.action0, this.mContext.getResources().getColorStateList(33882545, null));
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "preActionButton, catched Resources.NotFoundException!!!");
        }
    }
}
