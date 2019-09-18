package com.android.server.wifi;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.wifi.ScanResult;
import android.util.Log;
import com.android.internal.notification.SystemNotificationChannels;

public class ConnectToNetworkNotificationBuilder {
    public static final String ACTION_CONNECT_TO_NETWORK = "com.android.server.wifi.ConnectToNetworkNotification.CONNECT_TO_NETWORK";
    public static final String ACTION_PICK_WIFI_NETWORK = "com.android.server.wifi.ConnectToNetworkNotification.PICK_WIFI_NETWORK";
    public static final String ACTION_PICK_WIFI_NETWORK_AFTER_CONNECT_FAILURE = "com.android.server.wifi.ConnectToNetworkNotification.PICK_NETWORK_AFTER_FAILURE";
    public static final String ACTION_USER_DISMISSED_NOTIFICATION = "com.android.server.wifi.ConnectToNetworkNotification.USER_DISMISSED_NOTIFICATION";
    public static final String AVAILABLE_NETWORK_NOTIFIER_TAG = "com.android.server.wifi.ConnectToNetworkNotification.AVAILABLE_NETWORK_NOTIFIER_TAG";
    private Context mContext;
    private FrameworkFacade mFrameworkFacade;
    private Resources mResources;

    public ConnectToNetworkNotificationBuilder(Context context, FrameworkFacade framework) {
        this.mContext = context;
        this.mResources = context.getResources();
        this.mFrameworkFacade = framework;
    }

    /* JADX WARNING: Removed duplicated region for block: B:13:0x0028  */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x003f  */
    /* JADX WARNING: Removed duplicated region for block: B:16:0x0049  */
    public Notification createConnectToAvailableNetworkNotification(String notifierTag, ScanResult network) {
        char c;
        CharSequence title;
        int hashCode = notifierTag.hashCode();
        if (hashCode != 594918769) {
            if (hashCode == 2017428693 && notifierTag.equals(OpenNetworkNotifier.TAG)) {
                c = 0;
                switch (c) {
                    case 0:
                        title = this.mContext.getText(17041390);
                        break;
                    case 1:
                        title = this.mContext.getText(17041387);
                        break;
                    default:
                        Log.wtf("ConnectToNetworkNotificationBuilder", "Unknown network notifier." + notifierTag);
                        return null;
                }
                Notification.Action connectAction = new Notification.Action.Builder(null, this.mResources.getText(17041386), getPrivateBroadcast(ACTION_CONNECT_TO_NETWORK, notifierTag)).build();
                return createNotificationBuilder(title, network.SSID, notifierTag).setContentIntent(getPrivateBroadcast(ACTION_PICK_WIFI_NETWORK, notifierTag)).addAction(connectAction).addAction(new Notification.Action.Builder(null, this.mResources.getText(17041385), getPrivateBroadcast(ACTION_PICK_WIFI_NETWORK, notifierTag)).build()).build();
            }
        } else if (notifierTag.equals(CarrierNetworkNotifier.TAG)) {
            c = 1;
            switch (c) {
                case 0:
                    break;
                case 1:
                    break;
            }
            Notification.Action connectAction2 = new Notification.Action.Builder(null, this.mResources.getText(17041386), getPrivateBroadcast(ACTION_CONNECT_TO_NETWORK, notifierTag)).build();
            return createNotificationBuilder(title, network.SSID, notifierTag).setContentIntent(getPrivateBroadcast(ACTION_PICK_WIFI_NETWORK, notifierTag)).addAction(connectAction2).addAction(new Notification.Action.Builder(null, this.mResources.getText(17041385), getPrivateBroadcast(ACTION_PICK_WIFI_NETWORK, notifierTag)).build()).build();
        }
        c = 65535;
        switch (c) {
            case 0:
                break;
            case 1:
                break;
        }
        Notification.Action connectAction22 = new Notification.Action.Builder(null, this.mResources.getText(17041386), getPrivateBroadcast(ACTION_CONNECT_TO_NETWORK, notifierTag)).build();
        return createNotificationBuilder(title, network.SSID, notifierTag).setContentIntent(getPrivateBroadcast(ACTION_PICK_WIFI_NETWORK, notifierTag)).addAction(connectAction22).addAction(new Notification.Action.Builder(null, this.mResources.getText(17041385), getPrivateBroadcast(ACTION_PICK_WIFI_NETWORK, notifierTag)).build()).build();
    }

    public Notification createNetworkConnectingNotification(String notifierTag, ScanResult network) {
        return createNotificationBuilder(this.mContext.getText(17041392), network.SSID, notifierTag).setProgress(0, 0, true).build();
    }

    public Notification createNetworkConnectedNotification(String notifierTag, ScanResult network) {
        return createNotificationBuilder(this.mContext.getText(17041391), network.SSID, notifierTag).build();
    }

    public Notification createNetworkFailedNotification(String notifierTag) {
        return createNotificationBuilder(this.mContext.getText(17041393), this.mContext.getText(17041388), notifierTag).setContentIntent(getPrivateBroadcast(ACTION_PICK_WIFI_NETWORK_AFTER_CONNECT_FAILURE, notifierTag)).setAutoCancel(true).build();
    }

    /* JADX WARNING: Removed duplicated region for block: B:12:0x0029 A[RETURN] */
    /* JADX WARNING: Removed duplicated region for block: B:13:0x002a  */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x002c A[RETURN] */
    private int getNotifierRequestCode(String notifierTag) {
        char c;
        int hashCode = notifierTag.hashCode();
        if (hashCode == 594918769) {
            if (notifierTag.equals(CarrierNetworkNotifier.TAG)) {
                c = 1;
                switch (c) {
                    case 0:
                        break;
                    case 1:
                        break;
                }
            }
        } else if (hashCode == 2017428693 && notifierTag.equals(OpenNetworkNotifier.TAG)) {
            c = 0;
            switch (c) {
                case 0:
                    return 1;
                case 1:
                    return 2;
                default:
                    return 0;
            }
        }
        c = 65535;
        switch (c) {
            case 0:
                break;
            case 1:
                break;
        }
    }

    private Notification.Builder createNotificationBuilder(CharSequence title, CharSequence content, String extraData) {
        return this.mFrameworkFacade.makeNotificationBuilder(this.mContext, SystemNotificationChannels.NETWORK_AVAILABLE).setSmallIcon(17303481).setTicker(title).setContentTitle(title).setContentText(content).setDeleteIntent(getPrivateBroadcast(ACTION_USER_DISMISSED_NOTIFICATION, extraData)).setShowWhen(false).setLocalOnly(true).setColor(this.mResources.getColor(17170784, this.mContext.getTheme()));
    }

    private PendingIntent getPrivateBroadcast(String action, String extraData) {
        Intent intent = new Intent(action).setPackage("android");
        int requestCode = 0;
        if (extraData != null) {
            intent.putExtra(AVAILABLE_NETWORK_NOTIFIER_TAG, extraData);
            requestCode = getNotifierRequestCode(extraData);
        }
        return this.mFrameworkFacade.getBroadcast(this.mContext, requestCode, intent, 134217728);
    }
}
