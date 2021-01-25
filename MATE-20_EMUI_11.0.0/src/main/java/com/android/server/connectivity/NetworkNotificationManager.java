package com.android.server.connectivity;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.NetworkSpecifier;
import android.net.StringNetworkSpecifier;
import android.net.wifi.WifiInfo;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.Slog;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.widget.Toast;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.notification.SystemNotificationChannels;
import com.android.server.ConnectivityService;

public class NetworkNotificationManager {
    private static final boolean DBG = true;
    private static final int PORTAL_NOTIFICATION_NOT_SHOWN = 0;
    private static final int PORTAL_NOTIFICATION_SHOWN = 1;
    private static final String TAG = NetworkNotificationManager.class.getSimpleName();
    private static final boolean VDBG = false;
    private ConnectivityService mConnectivityService;
    private final Context mContext;
    private final NotificationManager mNotificationManager;
    private final SparseIntArray mNotificationTypeMap = new SparseIntArray();
    private final TelephonyManager mTelephonyManager;

    public enum NotificationType {
        LOST_INTERNET(742),
        NETWORK_SWITCH(743),
        NO_INTERNET(741),
        LOGGED_IN(744),
        PARTIAL_CONNECTIVITY(745),
        SIGN_IN(740);
        
        public final int eventId;

        private NotificationType(int eventId2) {
            this.eventId = eventId2;
            Holder.sIdToTypeMap.put(eventId2, this);
        }

        /* access modifiers changed from: private */
        public static class Holder {
            private static SparseArray<NotificationType> sIdToTypeMap = new SparseArray<>();

            private Holder() {
            }
        }

        public static NotificationType getFromId(int id) {
            return (NotificationType) Holder.sIdToTypeMap.get(id);
        }
    }

    public NetworkNotificationManager(Context c, TelephonyManager t, NotificationManager n) {
        this.mContext = c;
        this.mTelephonyManager = t;
        this.mNotificationManager = n;
    }

    private static int getFirstTransportType(NetworkAgentInfo nai) {
        for (int i = 0; i < 64; i++) {
            if (nai.networkCapabilities.hasTransport(i)) {
                return i;
            }
        }
        return -1;
    }

    private static String getTransportName(int transportType) {
        Resources r = Resources.getSystem();
        try {
            return r.getStringArray(17236092)[transportType];
        } catch (IndexOutOfBoundsException e) {
            return r.getString(17040642);
        }
    }

    private static int getIcon(int transportType, NotificationType notifyType) {
        if (transportType != 1) {
            return 17303539;
        }
        if (notifyType == NotificationType.LOGGED_IN) {
            return 17302862;
        }
        return 17303543;
    }

    public void showNotification(int id, NotificationType notifyType, NetworkAgentInfo nai, NetworkAgentInfo switchToNai, PendingIntent intent, boolean highPriority) {
        String extraInfo;
        String name;
        int transportType;
        boolean z;
        CharSequence details;
        CharSequence title;
        CharSequence title2;
        int subId;
        String tag = tagFor(id);
        int eventId = notifyType.eventId;
        if (nai != null) {
            int transportType2 = getFirstTransportType(nai);
            String extraInfo2 = nai.networkInfo.getExtraInfo();
            name = TextUtils.isEmpty(extraInfo2) ? nai.networkCapabilities.getSSID() : extraInfo2;
            if (nai.networkCapabilities.hasCapability(12)) {
                extraInfo = extraInfo2;
                transportType = transportType2;
            } else {
                return;
            }
        } else {
            name = null;
            extraInfo = null;
            transportType = 0;
        }
        NotificationType previousNotifyType = NotificationType.getFromId(this.mNotificationTypeMap.get(id));
        if (priority(previousNotifyType) > priority(notifyType)) {
            Slog.d(TAG, String.format("ignoring notification %s for network %s with existing notification %s", notifyType, Integer.valueOf(id), previousNotifyType));
            return;
        }
        clearNotification(id);
        Slog.d(TAG, String.format("showNotification tag=%s event=%s transport=%s name=%s highPriority=%s", tag, nameOf(eventId), getTransportName(transportType), name, Boolean.valueOf(highPriority)));
        if (Settings.Global.getInt(this.mContext.getContentResolver(), "captive_portal_notification_shown", 0) == 1) {
            Log.d(TAG, "portal notification has been shown already, not show again.");
            return;
        }
        Settings.Global.putInt(this.mContext.getContentResolver(), "captive_portal_notification_shown", 1);
        Log.d(TAG, "portal notification is shown, change CAPTIVE_PORTAL_NOTIFICATION_SHOWN to 1");
        Resources r = Resources.getSystem();
        int icon = getIcon(transportType, notifyType);
        if (notifyType == NotificationType.NO_INTERNET && transportType == 1) {
            CharSequence title3 = r.getString(17041548, WifiInfo.removeDoubleQuotes(nai.networkCapabilities.getSSID()));
            details = r.getString(17041549);
            title = title3;
            z = false;
        } else if (notifyType == NotificationType.PARTIAL_CONNECTIVITY && transportType == 1) {
            CharSequence title4 = r.getString(17040637, WifiInfo.removeDoubleQuotes(nai.networkCapabilities.getSSID()));
            details = r.getString(17040638);
            title = title4;
            z = false;
        } else if (notifyType == NotificationType.LOST_INTERNET && transportType == 1) {
            CharSequence title5 = r.getString(17041548, WifiInfo.removeDoubleQuotes(nai.networkCapabilities.getSSID()));
            details = r.getString(17041549);
            title = title5;
            z = false;
        } else if (notifyType == NotificationType.SIGN_IN) {
            if (transportType == 0) {
                CharSequence title6 = r.getString(17040633, 0);
                NetworkSpecifier specifier = nai.networkCapabilities.getNetworkSpecifier();
                if (specifier instanceof StringNetworkSpecifier) {
                    try {
                        subId = Integer.parseInt(((StringNetworkSpecifier) specifier).specifier);
                        title2 = title6;
                    } catch (NumberFormatException e) {
                        title2 = title6;
                        Slog.e(TAG, "NumberFormatException on " + ((StringNetworkSpecifier) specifier).specifier);
                    }
                } else {
                    title2 = title6;
                    subId = Integer.MAX_VALUE;
                }
                details = this.mTelephonyManager.createForSubscriptionId(subId).getNetworkOperatorName();
                title = title2;
                z = false;
            } else if (transportType != 1) {
                z = false;
                CharSequence title7 = r.getString(17040633, 0);
                details = r.getString(17040634, name);
                title = title7;
            } else {
                CharSequence title8 = r.getString(17041538, 0);
                details = r.getString(17040634, WifiInfo.removeDoubleQuotes(nai.networkCapabilities.getSSID()));
                this.mConnectivityService = (ConnectivityService) ServiceManager.getService("connectivity");
                Notification notification = new Notification();
                notification.contentIntent = intent;
                this.mConnectivityService.startBrowserForWifiPortal(notification, extraInfo);
                title = title8;
                z = false;
            }
        } else if (notifyType == NotificationType.LOGGED_IN) {
            CharSequence title9 = WifiInfo.removeDoubleQuotes(nai.networkCapabilities.getSSID());
            details = r.getString(17039749);
            title = title9;
            z = false;
        } else if (notifyType == NotificationType.NETWORK_SWITCH) {
            String fromTransport = getTransportName(transportType);
            String toTransport = getTransportName(getFirstTransportType(switchToNai));
            z = false;
            title = r.getString(17040639, toTransport);
            details = r.getString(17040640, toTransport, fromTransport);
        } else if (notifyType != NotificationType.NO_INTERNET && notifyType != NotificationType.PARTIAL_CONNECTIVITY) {
            Slog.wtf(TAG, "Unknown notification type " + notifyType + " on network transport " + getTransportName(transportType));
            return;
        } else {
            return;
        }
        boolean z2 = previousNotifyType != null ? true : z;
        String channelId = SystemNotificationChannels.NETWORK_ALERTS;
        Notification.Builder builder = new Notification.Builder(this.mContext, channelId).setWhen(System.currentTimeMillis()).setShowWhen(notifyType == NotificationType.NETWORK_SWITCH ? true : z).setSmallIcon(icon).setTicker(title).setColor(this.mContext.getColor(17170460)).setContentTitle(title).setContentIntent(intent).setLocalOnly(true).setOnlyAlertOnce(true);
        if (notifyType == NotificationType.NETWORK_SWITCH) {
            builder.setStyle(new Notification.BigTextStyle().bigText(details));
        } else {
            builder.setContentText(details);
        }
        if (notifyType == NotificationType.SIGN_IN) {
            builder.extend(new Notification.TvExtender().setChannelId(channelId));
        }
        Notification notification2 = builder.build();
        if (notifyType == NotificationType.SIGN_IN) {
            notification2.flags |= 32;
        }
        this.mNotificationTypeMap.put(id, eventId);
        try {
            this.mNotificationManager.notifyAsUser(tag, eventId, notification2, UserHandle.ALL);
        } catch (NullPointerException npe) {
            Slog.d(TAG, "setNotificationVisible: visible notificationManager error", npe);
        }
    }

    public void clearNotification(int id, NotificationType notifyType) {
        if (notifyType == NotificationType.getFromId(this.mNotificationTypeMap.get(id))) {
            clearNotification(id);
        }
    }

    public void clearNotification(int id) {
        if (this.mNotificationTypeMap.indexOfKey(id) >= 0) {
            String tag = tagFor(id);
            int eventId = this.mNotificationTypeMap.get(id);
            Slog.d(TAG, String.format("clearing notification tag=%s event=%s", tag, nameOf(eventId)));
            try {
                this.mNotificationManager.cancelAsUser(tag, eventId, UserHandle.ALL);
                Settings.Global.putInt(this.mContext.getContentResolver(), "captive_portal_notification_shown", 0);
                Log.d(TAG, "portal notification is dismissed, change CAPTIVE_PORTAL_NOTIFICATION_SHOWN to 0");
            } catch (NullPointerException npe) {
                Slog.d(TAG, String.format("failed to clear notification tag=%s event=%s", tag, nameOf(eventId)), npe);
            }
            this.mNotificationTypeMap.delete(id);
        }
    }

    public void setProvNotificationVisible(boolean visible, int id, String action) {
        if (visible) {
            showNotification(id, NotificationType.SIGN_IN, null, null, PendingIntent.getBroadcast(this.mContext, 0, new Intent(action), 0), false);
            return;
        }
        clearNotification(id);
    }

    public void showToast(NetworkAgentInfo fromNai, NetworkAgentInfo toNai) {
        Toast.makeText(this.mContext, this.mContext.getResources().getString(17040641, getTransportName(getFirstTransportType(fromNai)), getTransportName(getFirstTransportType(toNai))), 1).show();
    }

    @VisibleForTesting
    static String tagFor(int id) {
        return String.format("ConnectivityNotification:%d", Integer.valueOf(id));
    }

    @VisibleForTesting
    static String nameOf(int eventId) {
        NotificationType t = NotificationType.getFromId(eventId);
        return t != null ? t.name() : "UNKNOWN";
    }

    private static int priority(NotificationType t) {
        if (t == null) {
            return 0;
        }
        switch (t) {
            case SIGN_IN:
                return 5;
            case PARTIAL_CONNECTIVITY:
                return 4;
            case NO_INTERNET:
                return 3;
            case NETWORK_SWITCH:
                return 2;
            case LOST_INTERNET:
            case LOGGED_IN:
                return 1;
            default:
                return 0;
        }
    }
}
