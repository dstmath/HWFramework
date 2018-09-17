package com.android.server.wifi.wifipro;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.os.UserHandle;
import android.provider.Settings.System;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;
import com.android.server.wifi.HwCHRWifiCPUUsage;

public class WifiProUIDisplayManager {
    public static final String ACTION_HIGH_MOBILE_DATA_DELETE = "com.android.server.wifi.wifipro.hmd.delete";
    public static final String ACTION_HIGH_MOBILE_DATA_ROVE_IN = "com.android.server.wifi.wifipro.hmd.rove.in";
    private static final String AUTO_CONNECT_WIFI = "auto_connect_wifi";
    private static final int AUTO_CONNECT_WIFI_AUTO = 1;
    private static final int AUTO_CONNECT_WIFI_NEVER = 2;
    private static final int AUTO_CONNECT_WIFI_NOTIFY = 0;
    private static final String BCM_PERMISSION = "huawei.permission.RECEIVE_WIFI_PRO_STATE";
    private static final boolean DBG = true;
    public static final int DIALOG_TYPE_AUTO_CONNECT_WIFI = 3;
    public static final int DIALOG_TYPE_CANCEL = 1;
    public static final int DIALOG_TYPE_CONNECT_INVALID_AP = 4;
    public static final int DIALOG_TYPE_CONNECT_PROTAL_AP_NO_LOGIN = 5;
    public static final int DIALOG_TYPE_OK = 2;
    public static final int DIALOG_TYPE_WIFI_HANDOVER_MOBILE_WIFI_NO_INTERNET = 2;
    public static final int DIALOG_TYPE_WIFI_HANDOVER_MOBILE_WIFI_POOR = 1;
    private static final String EXTRA_NETWORK_CHANGE_TYPE = "extra_network_change_type";
    private static final String EXTRA_WIFI_NO_INTERNET = "extra_wifipro_no_Internet";
    private static final int HMDF_NOTIFICATION_TAG = 188223;
    private static final String TAG = "WiFi_PRO_WifiProUIDisplayManager";
    public static final int TOAST_TYPE_RECOVER_WIFI_CONNECT = 4;
    public static final int TOAST_TYPE_WIFI_DISENABLE = 2;
    public static final int TOAST_TYPE_WIFI_HANDOVER_MOBILE = 1;
    public static final int TOAST_TYPE_WIFI_UNUSABLE = 3;
    private static final String WIFI_PRO_INTERNET_ACCESS_CHANGE_ACTION = "huawei.wifi.pro.INTERNET_ACCESS_CHANGE";
    private static final String WIFI_PRO_NETWORK_CHANGE_ACTION = "huawei.wifi.pro.NETWORK_CHANGE";
    private static final String WIFI_TO_MOBILE = "wifi_to_pdp";
    private static final int WIFI_TO_MOBILE_AUTO = 1;
    private static final int WIFI_TO_MOBILE_NEVER = 2;
    private static final int WIFI_TO_MOBILE_NOTIFY = 0;
    private static WifiProUIDisplayManager mWifiProUIDisplayManager;
    private int invalidAPUserChoice;
    private IWifiProUICallBack mCallBack;
    private Context mContext;
    private Notification mHmdNotification;
    private NotificationManager mHmdNotificationManager;
    private OnDismissListener mInvalidAPSingleChoiceListener;
    private AlertDialog mInvalidAPSingleChoiceWarningDialog;
    public volatile boolean mIsNotificationShown;
    private volatile boolean mIsRemindWifiHandoverMobile;
    private OnDismissListener mSwitchListener;
    private AlertDialog mWifiToMobileDialog;

    private WifiProUIDisplayManager(Context context, IWifiProUICallBack callBack) {
        this.mHmdNotification = null;
        this.mHmdNotificationManager = null;
        this.mWifiToMobileDialog = null;
        this.mInvalidAPSingleChoiceWarningDialog = null;
        this.invalidAPUserChoice = -1;
        this.mSwitchListener = new OnDismissListener() {
            public void onDismiss(DialogInterface dialog) {
                WifiProUIDisplayManager.this.mWifiToMobileDialog = null;
            }
        };
        this.mInvalidAPSingleChoiceListener = new OnDismissListener() {
            public void onDismiss(DialogInterface dialog) {
                WifiProUIDisplayManager.this.invalidAPUserChoice = WifiProUIDisplayManager.WIFI_TO_MOBILE_AUTO;
            }
        };
        this.mContext = context;
        this.mCallBack = callBack;
        this.mIsRemindWifiHandoverMobile = false;
        this.mHmdNotificationManager = (NotificationManager) this.mContext.getSystemService("notification");
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

    private void sendWifiProUICallBack(int type) {
        if (this.mCallBack != null) {
            this.mCallBack.onUserConfirm(type);
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
            case WIFI_TO_MOBILE_AUTO /*1*/:
                switchToMobileNetwork(DBG);
            case WIFI_TO_MOBILE_NEVER /*2*/:
                switchToMobileNetwork(false);
            case TOAST_TYPE_RECOVER_WIFI_CONNECT /*4*/:
                showChoiceWarningDialog(false);
            case DIALOG_TYPE_CONNECT_PROTAL_AP_NO_LOGIN /*5*/:
                showChoiceWarningDialog(DBG);
            default:
        }
    }

    public void showWifiProToast(int key) {
        switch (key) {
            case WIFI_TO_MOBILE_AUTO /*1*/:
                showToast(getNetworkSwitchToMobileToast());
            case WIFI_TO_MOBILE_NEVER /*2*/:
                showToast(getWlanDisconnectedToast());
            default:
        }
    }

    public void resetAlertDialog(int key) {
        switch (key) {
        }
    }

    private void showToast(String info) {
        Log.i(TAG, "showToast +  " + info);
        Toast.makeText(this.mContext, info, WIFI_TO_MOBILE_AUTO).show();
    }

    public void showToastL(String info) {
        Log.i(TAG, "showToast +  " + info);
    }

    private AlertDialog createSwitchWifiToMobileWarning(boolean internet) {
        Builder buider = new Builder(this.mContext, 33947691);
        View view = LayoutInflater.from(buider.getContext()).inflate(34013281, null);
        CheckBox checkBox = (CheckBox) view.findViewById(34603228);
        if (checkBox != null) {
            checkBox.setVisibility(8);
        }
        TextView textView = (TextView) view.findViewById(34603227);
        buider.setView(view);
        if (internet) {
            buider.setTitle(33685579);
        } else {
            buider.setTitle(33685580);
        }
        if (textView != null) {
            textView.setText(33685582);
        }
        buider.setIcon(17301543);
        buider.setPositiveButton(33685559, new OnClickListener() {
            public void onClick(DialogInterface dialoginterface, int i) {
                Log.i(WifiProUIDisplayManager.TAG, "you have chose to switch to Mobile data service!");
                WifiProUIDisplayManager.this.sendWifiProUICallBack(WifiProUIDisplayManager.WIFI_TO_MOBILE_NEVER);
            }
        });
        buider.setNegativeButton(33685560, new OnClickListener() {
            public void onClick(DialogInterface dialoginterface, int i) {
                Log.i(WifiProUIDisplayManager.TAG, "you have chose to disconnect Mobile data service!");
                WifiProUIDisplayManager.this.sendWifiProUICallBack(WifiProUIDisplayManager.WIFI_TO_MOBILE_AUTO);
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

    private void showChoiceWarningDialog(boolean isNologin) {
        this.mInvalidAPSingleChoiceWarningDialog = createInvalidAPSingleChoiceWarning(isNologin);
        this.mInvalidAPSingleChoiceWarningDialog.setOnDismissListener(this.mInvalidAPSingleChoiceListener);
        this.mInvalidAPSingleChoiceWarningDialog.show();
    }

    private void checkUserChoice(boolean rememberChoice, boolean enableDataConnect) {
        int showPopState;
        if (!rememberChoice) {
            showPopState = AUTO_CONNECT_WIFI_NOTIFY;
        } else if (enableDataConnect) {
            showPopState = WIFI_TO_MOBILE_AUTO;
        } else {
            showPopState = WIFI_TO_MOBILE_NEVER;
        }
        Log.i(TAG, "checkUserChoice showPopState:" + showPopState + ", enableDataConnect:" + enableDataConnect);
        System.putInt(this.mContext.getContentResolver(), WIFI_TO_MOBILE, showPopState);
    }

    private void checkUserChoiceAutoWifi(boolean rememberChoice, boolean enableDataConnect) {
        int showPopState;
        if (!rememberChoice) {
            showPopState = AUTO_CONNECT_WIFI_NOTIFY;
        } else if (enableDataConnect) {
            showPopState = WIFI_TO_MOBILE_AUTO;
        } else {
            showPopState = WIFI_TO_MOBILE_NEVER;
        }
        Log.i(TAG, "checkUserChoiceAutoWifi showPopState:" + showPopState + ", enableDataConnect:" + enableDataConnect);
        System.putInt(this.mContext.getContentResolver(), AUTO_CONNECT_WIFI, showPopState);
    }

    private AlertDialog createInvalidAPSingleChoiceWarning(boolean isNologin) {
        Builder buider = new Builder(this.mContext, 33947691);
        if (isNologin) {
            buider.setTitle(33685581);
        } else {
            buider.setTitle(33685580);
        }
        buider.setMessage(33685583);
        buider.setPositiveButton(33685584, new OnClickListener() {
            public void onClick(DialogInterface dialoginterface, int i) {
                Log.i(WifiProUIDisplayManager.TAG, "createInvalidAPSingleChoiceWarning choose " + WifiProUIDisplayManager.this.invalidAPUserChoice);
                WifiProUIDisplayManager.this.sendWifiProUICallBack(WifiProUIDisplayManager.WIFI_TO_MOBILE_NEVER);
            }
        });
        buider.setNegativeButton(33685585, new OnClickListener() {
            public void onClick(DialogInterface dialoginterface, int i) {
                Log.i(WifiProUIDisplayManager.TAG, "createInvalidAPSingleChoiceWarning choose cancel");
                WifiProUIDisplayManager.this.sendWifiProUICallBack(WifiProUIDisplayManager.WIFI_TO_MOBILE_AUTO);
            }
        });
        AlertDialog dialog = buider.create();
        dialog.setCancelable(false);
        dialog.getWindow().setType(2008);
        return dialog;
    }

    public void showNotificationAutoOpenWLAN() {
        NotificationManager notificationManager = (NotificationManager) this.mContext.getSystemService("notification");
        String title = this.mContext.getResources().getString(33685883);
        String text = this.mContext.getResources().getString(33685884);
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.android.settings", "com.android.settings.Settings$WifiSettingsActivity"));
        intent.setFlags(335544320);
        Notification.Builder builder = new Notification.Builder(this.mContext);
        builder.setWhen(System.currentTimeMillis()).setTicker(title).setAutoCancel(DBG).setContentTitle(title).setContentText(text).setContentIntent(PendingIntent.getActivity(this.mContext, AUTO_CONNECT_WIFI_NOTIFY, intent, 1073741824)).setShowWhen(DBG);
        notificationManager.notify(33685570, builder.build());
    }

    public void showNotificationAutoDisableWLAN() {
        NotificationManager notificationManager = (NotificationManager) this.mContext.getSystemService("notification");
        String title = this.mContext.getResources().getString(33685885);
        String text = this.mContext.getResources().getString(33685886);
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.android.settings", "com.android.settings.Settings$WifiSettingsActivity"));
        intent.setFlags(335544320);
        Notification.Builder builder = new Notification.Builder(this.mContext);
        builder.setWhen(System.currentTimeMillis()).setTicker(title).setAutoCancel(DBG).setContentTitle(title).setContentText(text).setContentIntent(PendingIntent.getActivity(this.mContext, AUTO_CONNECT_WIFI_NOTIFY, intent, 1073741824)).setShowWhen(DBG);
        notificationManager.notify(33685573, builder.build());
    }

    public void removeNotificationAutoOpenWlan() {
        ((NotificationManager) this.mContext.getSystemService("notification")).cancel(33685570);
    }

    public void removeNotificationAutoDisableWlan() {
        ((NotificationManager) this.mContext.getSystemService("notification")).cancel(33685573);
    }

    public void showHMDNotification(int mobileDateSize, boolean isHeadsupDisplay) {
        String title = this.mContext.getResources().getString(33685825).replace("%d", "" + mobileDateSize);
        String text = this.mContext.getResources().getString(33685826);
        Log.i(TAG, " title= " + title + ", text= " + text);
        String packageName = this.mContext.getPackageName();
        PendingIntent roveInPendingIntent = PendingIntent.getBroadcastAsUser(this.mContext, AUTO_CONNECT_WIFI_NOTIFY, new Intent(ACTION_HIGH_MOBILE_DATA_ROVE_IN).setPackage(packageName), 268435456, UserHandle.ALL);
        PendingIntent deletePendingIntent = PendingIntent.getBroadcastAsUser(this.mContext, AUTO_CONNECT_WIFI_NOTIFY, new Intent(ACTION_HIGH_MOBILE_DATA_DELETE).setPackage(packageName), 268435456, UserHandle.ALL);
        RemoteViews remoteViews = new RemoteViews(this.mContext.getPackageName(), 34013234);
        remoteViews.setImageViewResource(34603142, 33751348);
        remoteViews.setTextViewText(34603144, title);
        remoteViews.setTextViewText(34603145, text);
        Notification.Builder b = new Notification.Builder(this.mContext);
        b.setPriority(WIFI_TO_MOBILE_NEVER);
        b.setContentIntent(roveInPendingIntent);
        b.setDeleteIntent(deletePendingIntent);
        b.setAutoCancel(DBG);
        b.setUsesChronometer(DBG);
        b.setTicker("");
        b.setContent(remoteViews);
        b.setWhen(System.currentTimeMillis());
        if (isHeadsupDisplay) {
            b.setDefaults(6);
        }
        b.setSmallIcon(33751348);
        b.setVisibility(WIFI_TO_MOBILE_AUTO);
        Bitmap icon = BitmapFactory.decodeResource(this.mContext.getResources(), 33751348);
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

    public void showPortalNotificationStatusBar(String ssid, String tag, int id) {
        String packageName = this.mContext.getPackageName();
        String titleText = this.mContext.getResources().getString(33685828);
        String contentText = this.mContext.getResources().getString(33685829) + HwCHRWifiCPUUsage.COL_SEP + ssid;
        PendingIntent usePendingIntent = PendingIntent.getBroadcastAsUser(this.mContext, AUTO_CONNECT_WIFI_NOTIFY, new Intent("com.huawei.wifipro.action.ACTION_PORTAL_USED_BY_USER").setPackage(packageName), 268435456, UserHandle.ALL);
        PendingIntent cancelPortalPendingIntent = PendingIntent.getBroadcastAsUser(this.mContext, AUTO_CONNECT_WIFI_NOTIFY, new Intent("com.huawei.wifipro.action.ACTION_PORTAL_CANCELED_BY_USER").setPackage(packageName), 268435456, UserHandle.ALL);
        Notification.Builder b = new Notification.Builder(this.mContext);
        b.setPriority(WIFI_TO_MOBILE_NEVER);
        b.setContentIntent(usePendingIntent);
        b.setDeleteIntent(cancelPortalPendingIntent);
        b.setAutoCancel(DBG);
        b.setTicker("");
        b.setWhen(System.currentTimeMillis());
        b.setShowWhen(DBG);
        b.setDefaults(WIFI_TO_MOBILE_NEVER);
        b.setContentTitle(titleText);
        b.setContentText(contentText);
        b.setSmallIcon(getBitampIcon(33751322));
        b.setVisibility(WIFI_TO_MOBILE_AUTO);
        this.mHmdNotificationManager.notify(tag, id, b.build());
    }

    public void cancelPortalNotificationStatusBar(String tag, int id) {
        this.mHmdNotificationManager.cancel(tag, id);
    }

    private String getNetworkSwitchToMobileToast() {
        return this.mContext.getResources().getString(33685568);
    }

    private String getWlanDisconnectedToast() {
        return this.mContext.getResources().getString(33685572);
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
                this.mIsNotificationShown = DBG;
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
        Bitmap bitmap = Bitmap.createBitmap(w, h, drawable.getOpacity() != -1 ? Config.ARGB_8888 : Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(AUTO_CONNECT_WIFI_NOTIFY, AUTO_CONNECT_WIFI_NOTIFY, w, h);
        drawable.draw(canvas);
        return Icon.createWithBitmap(bitmap);
    }
}
