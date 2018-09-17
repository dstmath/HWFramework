package android.app;

import android.content.Context;
import android.provider.Settings.System;
import android.provider.SettingsEx.Systemex;
import android.text.TextUtils;
import android.widget.RemoteViews;
import com.android.internal.util.NotificationColorUtil;

public class HwCustNotificationImpl extends HwCustNotification {
    public void setNotiActionButtonTextSize(Context context, RemoteViews button, int id) {
        boolean isRussianNotiActionButton = false;
        if (Systemex.getInt(context.getContentResolver(), "Russian_noti_action_button", 0) == 1) {
            isRussianNotiActionButton = true;
        }
        if (isRussianNotiActionButton) {
            button.setTextViewTextSize(id, 1, 9.0f);
        }
    }

    public boolean isInvertColorRequired(Context context) {
        String invertNotificationTxtClrPackages = System.getString(context.getContentResolver(), "hw_invert_txtclr_packages");
        String curPackageName = context.getApplicationInfo().packageName;
        if (!(TextUtils.isEmpty(invertNotificationTxtClrPackages) || TextUtils.isEmpty(curPackageName))) {
            for (String packageName : invertNotificationTxtClrPackages.split(";")) {
                if ((packageName.contains("*") && curPackageName.contains(packageName.substring(0, packageName.length() - 1))) || packageName.equals(curPackageName)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void setCustomizedInvertColorText(CharSequence text, Context context, int id, RemoteViews contentView) {
        contentView.setTextViewText(id, NotificationColorUtil.getInstance(context).invertCharSequenceColors(text));
    }
}
