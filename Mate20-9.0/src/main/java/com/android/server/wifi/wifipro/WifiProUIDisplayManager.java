package com.android.server.wifi.wifipro;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;
import com.android.internal.notification.SystemNotificationChannels;

public class WifiProUIDisplayManager {
    public static final String ACTION_HIGH_MOBILE_DATA_DELETE = "com.android.server.wifi.wifipro.hmd.delete";
    public static final String ACTION_HIGH_MOBILE_DATA_ROVE_IN = "com.android.server.wifi.wifipro.hmd.rove.in";
    private static final String AUTO_CONNECT_WIFI = "auto_connect_wifi";
    private static final int AUTO_CONNECT_WIFI_AUTO = 1;
    private static final int AUTO_CONNECT_WIFI_NEVER = 2;
    private static final int AUTO_CONNECT_WIFI_NOTIFY = 0;
    private static final String BCM_PERMISSION = "huawei.permission.RECEIVE_WIFI_PRO_STATE";
    private static final boolean DBG = true;
    public static final int DIALOG_STATUS_CANCEL = 1;
    public static final int DIALOG_STATUS_OK = 2;
    public static final int DIALOG_TYPE_AUTO_CONNECT_WIFI = 3;
    public static final int DIALOG_TYPE_CONNECT_INVALID_AP = 4;
    public static final int DIALOG_TYPE_CONNECT_PROTAL_AP_NO_LOGIN = 5;
    public static final int DIALOG_TYPE_CONNECT_WIFI_POOR = 6;
    public static final int DIALOG_TYPE_WIFI_HANDOVER_MOBILE = 102;
    public static final int DIALOG_TYPE_WIFI_HANDOVER_MOBILE_WIFI_NO_INTERNET = 2;
    public static final int DIALOG_TYPE_WIFI_HANDOVER_MOBILE_WIFI_POOR = 1;
    public static final int DIALOG_TYPE_WIFI_HANDOVER_WIFI = 101;
    private static final String EXTRA_NETWORK_CHANGE_TYPE = "extra_network_change_type";
    private static final String EXTRA_WIFI_NO_INTERNET = "extra_wifipro_no_Internet";
    private static final int HMDF_NOTIFICATION_TAG = 188223;
    private static final String PORTAL_STATUS_BAR_TAG = "wifipro_portal_expired_status_bar";
    private static final String TAG = "WiFi_PRO_WifiProUIDisplayManager";
    public static final int TOAST_TYPE_RECOVER_WIFI_CONNECT = 4;
    public static final int TOAST_TYPE_WIFI_DISENABLE = 2;
    public static final int TOAST_TYPE_WIFI_HANDOVER_MOBILE = 1;
    public static final int TOAST_TYPE_WIFI_UNUSABLE = 3;
    private static final String WIFI_PRO_INTERNET_ACCESS_CHANGE_ACTION = "huawei.wifi.pro.INTERNET_ACCESS_CHANGE";
    private static final String WIFI_PRO_NETWORK_CHANGE_ACTION = "huawei.wifi.pro.NETWORK_CHANGE";
    private static final String WIFI_PRO_NOTIFY_CHANNEL = "wifi_pro_notify_channel";
    private static final String WIFI_TO_MOBILE = "wifi_to_pdp";
    private static final int WIFI_TO_MOBILE_AUTO = 1;
    private static final int WIFI_TO_MOBILE_NEVER = 2;
    private static final int WIFI_TO_MOBILE_NOTIFY = 0;
    private static WifiProUIDisplayManager mWifiProUIDisplayManager;
    /* access modifiers changed from: private */
    public int invalidAPUserChoice = -1;
    private IWifiProUICallBack mCallBack;
    private Context mContext;
    private Notification mHmdNotification = null;
    private NotificationManager mHmdNotificationManager = null;
    private DialogInterface.OnDismissListener mInvalidAPSingleChoiceListener = new DialogInterface.OnDismissListener() {
        public void onDismiss(DialogInterface dialog) {
            int unused = WifiProUIDisplayManager.this.invalidAPUserChoice = 1;
        }
    };
    private AlertDialog mInvalidAPSingleChoiceWarningDialog = null;
    public volatile boolean mIsNotificationShown;
    /* access modifiers changed from: private */
    public volatile boolean mIsRemindWifiHandoverMobile;
    private DialogInterface.OnDismissListener mSwitchListener = new DialogInterface.OnDismissListener() {
        public void onDismiss(DialogInterface dialog) {
            AlertDialog unused = WifiProUIDisplayManager.this.mWifiToMobileDialog = null;
        }
    };
    /* access modifiers changed from: private */
    public AlertDialog mWifiToMobileDialog = null;

    private WifiProUIDisplayManager(Context context, IWifiProUICallBack callBack) {
        this.mContext = context;
        this.mCallBack = callBack;
        this.mIsRemindWifiHandoverMobile = false;
        this.mHmdNotificationManager = (NotificationManager) this.mContext.getSystemService("notification");
        this.mHmdNotificationManager.createNotificationChannel(new NotificationChannel(WIFI_PRO_NOTIFY_CHANNEL, context.getText(33685835), 2));
    }

    public static WifiProUIDisplayManager createInstance(Context context, IWifiProUICallBack callBack) {
        if (mWifiProUIDisplayManager == null) {
            mWifiProUIDisplayManager = new WifiProUIDisplayManager(context, callBack);
        }
        return mWifiProUIDisplayManager;
    }

    public void registerCallBack(IWifiProUICallBack callBack) {
        if (this.mCallBack == null) {
            this.mCallBack = callBack;
        }
    }

    public boolean getIsRemindWifiHandoverMobile() {
        Log.i(TAG, "mIsRemindWifiHandoverMobile = " + this.mIsRemindWifiHandoverMobile);
        return this.mIsRemindWifiHandoverMobile;
    }

    public void unRegisterCallBack() {
        this.mCallBack = null;
    }

    /* access modifiers changed from: private */
    public void sendWifiProUICallBack(int type, int status) {
        if (this.mCallBack != null) {
            this.mCallBack.onUserConfirm(type, status);
        }
    }

    public void cancelAllDialog() {
        if (this.mInvalidAPSingleChoiceWarningDialog != null) {
            this.mInvalidAPSingleChoiceWarningDialog.cancel();
        }
        if (this.mWifiToMobileDialog != null) {
            this.mWifiToMobileDialog.cancel();
        }
    }

    public void showWifiProDialog(int key) {
        cancelAllDialog();
        switch (key) {
            case 1:
                switchToMobileNetwork(true);
                return;
            case 2:
                switchToMobileNetwork(false);
                return;
            case 4:
                showChoiceWarningDialog(false, true);
                return;
            case 5:
                showChoiceWarningDialog(true, true);
                return;
            case 6:
                showChoiceWarningDialog(false, false);
                return;
            default:
                return;
        }
    }

    public void showWifiProToast(int key) {
        switch (key) {
            case 1:
                showToast(getNetworkSwitchToMobileToast());
                return;
            case 2:
                showToast(getWlanDisconnectedToast());
                return;
            default:
                return;
        }
    }

    public void resetAlertDialog(int key) {
        if (key != 1) {
            switch (key) {
            }
        }
    }

    private void showToast(String info) {
        Log.i(TAG, "showToast +  " + info);
        Toast.makeText(this.mContext, info, 1).show();
    }

    public void showToastL(String info) {
        Log.i(TAG, "showToast +  " + info);
    }

    private AlertDialog createSwitchWifiToMobileWarning(final boolean internet) {
        AlertDialog.Builder buider = new AlertDialog.Builder(this.mContext, 33947691);
        View view = LayoutInflater.from(buider.getContext()).inflate(34013281, null);
        final CheckBox checkBox = (CheckBox) view.findViewById(34603192);
        if (checkBox != null) {
            if (!internet) {
                checkBox.setVisibility(8);
            } else {
                checkBox.setVisibility(0);
            }
        }
        TextView textView = (TextView) view.findViewById(34603193);
        buider.setView(view);
        if (internet) {
            buider.setTitle(33685587);
        } else {
            buider.setTitle(33685588);
        }
        if (textView != null) {
            textView.setText(33685590);
        }
        buider.setIcon(17301543);
        buider.setPositiveButton(33685567, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialoginterface, int i) {
                Log.i(WifiProUIDisplayManager.TAG, "you have chose to switch to Mobile data service!");
                WifiProUIDisplayManager.this.sendWifiProUICallBack(102, 2);
                if (checkBox != null && internet) {
                    boolean unused = WifiProUIDisplayManager.this.mIsRemindWifiHandoverMobile = checkBox.isChecked();
                    Log.i(WifiProUIDisplayManager.TAG, "wifi poor, user choose swith to mobile! remeber choose : " + WifiProUIDisplayManager.this.mIsRemindWifiHandoverMobile);
                    WifiProUIDisplayManager.this.checkUserChoice(checkBox.isChecked(), true);
                }
            }
        });
        buider.setNegativeButton(33685568, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialoginterface, int i) {
                Log.i(WifiProUIDisplayManager.TAG, "you have chose to disconnect Mobile data service!");
                WifiProUIDisplayManager.this.sendWifiProUICallBack(102, 1);
            }
        });
        AlertDialog dialog = buider.create();
        dialog.setCancelable(false);
        dialog.getWindow().setType(2008);
        return dialog;
    }

    private void switchToMobileNetwork(boolean isInternet) {
        this.mWifiToMobileDialog = createSwitchWifiToMobileWarning(isInternet);
        this.mWifiToMobileDialog.setOnDismissListener(this.mSwitchListener);
        this.mWifiToMobileDialog.show();
    }

    private void showChoiceWarningDialog(boolean isNologin, boolean isNoInternet) {
        this.mInvalidAPSingleChoiceWarningDialog = createInvalidAPSingleChoiceWarning(isNologin, isNoInternet);
        this.mInvalidAPSingleChoiceWarningDialog.setOnDismissListener(this.mInvalidAPSingleChoiceListener);
        this.mInvalidAPSingleChoiceWarningDialog.show();
    }

    /* access modifiers changed from: private */
    public void checkUserChoice(boolean rememberChoice, boolean enableDataConnect) {
        int showPopState;
        if (!rememberChoice) {
            showPopState = 0;
        } else if (enableDataConnect) {
            showPopState = 1;
        } else {
            showPopState = 2;
        }
        Log.i(TAG, "checkUserChoice showPopState:" + showPopState + ", enableDataConnect:" + enableDataConnect);
        Settings.System.putInt(this.mContext.getContentResolver(), WIFI_TO_MOBILE, showPopState);
    }

    private void checkUserChoiceAutoWifi(boolean rememberChoice, boolean enableDataConnect) {
        int showPopState;
        if (!rememberChoice) {
            showPopState = 0;
        } else if (enableDataConnect) {
            showPopState = 1;
        } else {
            showPopState = 2;
        }
        Log.i(TAG, "checkUserChoiceAutoWifi showPopState:" + showPopState + ", enableDataConnect:" + enableDataConnect);
        Settings.System.putInt(this.mContext.getContentResolver(), AUTO_CONNECT_WIFI, showPopState);
    }

    private AlertDialog createInvalidAPSingleChoiceWarning(boolean isNologin, boolean isNoInternet) {
        AlertDialog.Builder buider = new AlertDialog.Builder(this.mContext, 33947691);
        if (!isNoInternet) {
            buider.setTitle(33685587);
        } else if (isNologin) {
            buider.setTitle(33685589);
        }
        buider.setMessage(33686260);
        buider.setPositiveButton(33685567, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialoginterface, int i) {
                Log.i(WifiProUIDisplayManager.TAG, "createInvalidAPSingleChoiceWarning choose " + WifiProUIDisplayManager.this.invalidAPUserChoice);
                WifiProUIDisplayManager.this.sendWifiProUICallBack(101, 2);
            }
        });
        buider.setNegativeButton(33685568, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialoginterface, int i) {
                Log.i(WifiProUIDisplayManager.TAG, "createInvalidAPSingleChoiceWarning choose cancel");
                WifiProUIDisplayManager.this.sendWifiProUICallBack(101, 1);
            }
        });
        AlertDialog dialog = buider.create();
        dialog.setCancelable(false);
        dialog.getWindow().setType(2008);
        return dialog;
    }

    public void showNotificationAutoOpenWLAN() {
        String title = this.mContext.getResources().getString(33685892);
        String text = this.mContext.getResources().getString(33685893);
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.android.settings", "com.android.settings.Settings$WifiSettingsActivity"));
        intent.setFlags(335544320);
        Notification.Builder builder = new Notification.Builder(this.mContext);
        builder.setWhen(System.currentTimeMillis()).setTicker(title).setAutoCancel(true).setContentTitle(title).setContentText(text).setContentIntent(PendingIntent.getActivity(this.mContext, 0, intent, 1073741824)).setShowWhen(true);
        ((NotificationManager) this.mContext.getSystemService("notification")).notify(33685578, builder.build());
    }

    public void showNotificationAutoDisableWLAN() {
        String title = this.mContext.getResources().getString(33685894);
        String text = this.mContext.getResources().getString(33685895);
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.android.settings", "com.android.settings.Settings$WifiSettingsActivity"));
        intent.setFlags(335544320);
        Notification.Builder builder = new Notification.Builder(this.mContext);
        builder.setWhen(System.currentTimeMillis()).setTicker(title).setAutoCancel(true).setContentTitle(title).setContentText(text).setContentIntent(PendingIntent.getActivity(this.mContext, 0, intent, 1073741824)).setShowWhen(true);
        ((NotificationManager) this.mContext.getSystemService("notification")).notify(33685581, builder.build());
    }

    public void removeNotificationAutoOpenWlan() {
        ((NotificationManager) this.mContext.getSystemService("notification")).cancel(33685578);
    }

    public void removeNotificationAutoDisableWlan() {
        ((NotificationManager) this.mContext.getSystemService("notification")).cancel(33685581);
    }

    public void showHMDNotification(int mobileDateSize, boolean isHeadsupDisplay) {
        String sitleSrc = this.mContext.getResources().getString(33685832);
        String title = sitleSrc.replace("%d", "" + mobileDateSize);
        String text = this.mContext.getResources().getString(33685833);
        Log.i(TAG, " title= " + title + ", text= " + text);
        String packageName = this.mContext.getPackageName();
        PendingIntent roveInPendingIntent = PendingIntent.getBroadcastAsUser(this.mContext, 0, new Intent(ACTION_HIGH_MOBILE_DATA_ROVE_IN).setPackage(packageName), 268435456, UserHandle.ALL);
        PendingIntent deletePendingIntent = PendingIntent.getBroadcastAsUser(this.mContext, 0, new Intent(ACTION_HIGH_MOBILE_DATA_DELETE).setPackage(packageName), 268435456, UserHandle.ALL);
        RemoteViews remoteViews = new RemoteViews(this.mContext.getPackageName(), 34013234);
        remoteViews.setImageViewResource(34603194, 33751240);
        remoteViews.setTextViewText(34603195, title);
        remoteViews.setTextViewText(34603196, text);
        Notification.Builder b = new Notification.Builder(this.mContext);
        b.setPriority(2);
        b.setContentIntent(roveInPendingIntent);
        b.setDeleteIntent(deletePendingIntent);
        b.setAutoCancel(true);
        b.setUsesChronometer(true);
        b.setTicker("");
        b.setContent(remoteViews);
        b.setWhen(System.currentTimeMillis());
        if (isHeadsupDisplay) {
            b.setDefaults(6);
        }
        b.setChannelId(WIFI_PRO_NOTIFY_CHANNEL);
        b.setSmallIcon(33751240);
        b.setVisibility(1);
        Bitmap icon = BitmapFactory.decodeResource(this.mContext.getResources(), 33751240);
        if (icon != null) {
            b.setLargeIcon(icon);
        }
        this.mHmdNotification = b.build();
        if (isHeadsupDisplay) {
            this.mHmdNotification.headsUpContentView = remoteViews;
        }
        this.mHmdNotificationManager.notify(HMDF_NOTIFICATION_TAG, this.mHmdNotification);
    }

    public void cleanUpheadNotificationHMD() {
        Log.i(TAG, "clean HMD Notification enter.");
        this.mHmdNotificationManager.cancel(HMDF_NOTIFICATION_TAG);
    }

    public Notification.Builder showPortalNotificationStatusBar(String ssid, String tag, int id, Notification.Builder builder) {
        String str = ssid;
        String str2 = tag;
        Notification.Builder builder2 = builder;
        String packageName = this.mContext.getPackageName();
        String titleText = this.mContext.getResources().getString(33685835);
        String contentText = this.mContext.getResources().getString(33685836);
        String contentText2 = contentText + " " + str;
        String mChannelId = SystemNotificationChannels.NETWORK_STATUS;
        boolean mAutoCancelFlag = true;
        if (PORTAL_STATUS_BAR_TAG.equals(str2)) {
            titleText = this.mContext.getResources().getString(33686259);
            contentText2 = this.mContext.getResources().getString(33686258);
            mChannelId = SystemNotificationChannels.NETWORK_ALERTS;
            mAutoCancelFlag = false;
            if (str != null) {
                contentText2 = contentText2.replace("%s", str.substring(1, ssid.length() - 1));
            }
        }
        PendingIntent usePendingIntent = PendingIntent.getBroadcastAsUser(this.mContext, 0, new Intent("com.huawei.wifipro.action.ACTION_PORTAL_USED_BY_USER").setPackage(packageName), 268435456, UserHandle.ALL);
        PendingIntent cancelPortalPendingIntent = PendingIntent.getBroadcastAsUser(this.mContext, 0, new Intent("com.huawei.wifipro.action.ACTION_PORTAL_CANCELED_BY_USER").setPackage(packageName), 268435456, UserHandle.ALL);
        if (builder2 == null) {
            builder2 = new Notification.Builder(this.mContext);
            builder2.setPriority(2);
            builder2.setContentIntent(usePendingIntent);
            builder2.setDeleteIntent(cancelPortalPendingIntent);
            builder2.setAutoCancel(mAutoCancelFlag);
            builder2.setChannelId(mChannelId);
            builder2.setTicker("");
            builder2.setGroup("wifipro_notify_group");
            builder2.setContentTitle(titleText);
            builder2.setContentText(contentText2);
            builder2.setSmallIcon(getBitampIcon(33751227));
            builder2.setVisibility(1);
        } else {
            builder2.setContentIntent(usePendingIntent);
            builder2.setDeleteIntent(cancelPortalPendingIntent);
            builder2.setContentText(contentText2);
        }
        Notification notification = builder2.build();
        notification.flags |= 32;
        this.mHmdNotificationManager.notify(str2, id, notification);
        return builder2;
    }

    public void cancelPortalNotificationStatusBar(String tag, int id) {
        this.mHmdNotificationManager.cancel(tag, id);
    }

    private String getNetworkSwitchToMobileToast() {
        return this.mContext.getResources().getString(33685576);
    }

    private String getWlanDisconnectedToast() {
        return this.mContext.getResources().getString(33685580);
    }

    public void notificateNetWorkHandover(int state) {
        Log.d(TAG, "notificateNetWorkHandover state = " + state);
        Intent intent = new Intent(WIFI_PRO_NETWORK_CHANGE_ACTION);
        intent.setFlags(67108864);
        intent.putExtra(EXTRA_NETWORK_CHANGE_TYPE, state);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL, BCM_PERMISSION);
    }

    public void notificateNetAccessChange(boolean no_internet) {
        Log.d(TAG, "notificateNetAccessChange no_internet = " + no_internet);
        Intent intent = new Intent(WIFI_PRO_INTERNET_ACCESS_CHANGE_ACTION);
        intent.setFlags(67108864);
        intent.putExtra(EXTRA_WIFI_NO_INTERNET, no_internet);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL, BCM_PERMISSION);
    }

    public void shownAccessNotification(boolean accessAPNotificationShown) {
        Log.d(TAG, "shownAccessNotification  accessAPNotificationShown = " + accessAPNotificationShown + ", mIsNotificationShown = " + this.mIsNotificationShown);
        Intent intent = new Intent();
        if (accessAPNotificationShown) {
            if (!this.mIsNotificationShown) {
                this.mIsNotificationShown = true;
                intent.setAction(WifiproUtils.ACTION_NOTIFY_INTERNET_ACCESS_AP_FOUND);
            } else {
                return;
            }
        } else if (this.mIsNotificationShown) {
            this.mIsNotificationShown = false;
            intent.setAction(WifiproUtils.ACTION_NOTIFY_INTERNET_ACCESS_AP_OUT_OF_RANGE);
        } else {
            return;
        }
        intent.setFlags(67108864);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
    }

    private Icon getBitampIcon(int resId) {
        Drawable drawable = this.mContext.getResources().getDrawable(resId);
        if (drawable == null) {
            Log.w(TAG, "drawable is null");
            return null;
        }
        int w = drawable.getIntrinsicWidth();
        int h = drawable.getIntrinsicHeight();
        Bitmap bitmap = Bitmap.createBitmap(w, h, drawable.getOpacity() != -1 ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, w, h);
        drawable.draw(canvas);
        return Icon.createWithBitmap(bitmap);
    }
}
