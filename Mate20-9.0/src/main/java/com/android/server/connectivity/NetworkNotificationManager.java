package com.android.server.connectivity;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.wifi.WifiInfo;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Slog;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.widget.Toast;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.notification.SystemNotificationChannels;
import com.android.server.ConnectivityService;

public class NetworkNotificationManager {
    private static final boolean DBG = true;
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
        SIGN_IN(740);
        
        public final int eventId;

        private static class Holder {
            /* access modifiers changed from: private */
            public static SparseArray<NotificationType> sIdToTypeMap;

            private Holder() {
            }

            static {
                sIdToTypeMap = new SparseArray<>();
            }
        }

        private NotificationType(int eventId2) {
            this.eventId = eventId2;
            Holder.sIdToTypeMap.put(eventId2, this);
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
            return r.getStringArray(17236064)[transportType];
        } catch (IndexOutOfBoundsException e) {
            return r.getString(17040553);
        }
    }

    private static int getIcon(int transportType) {
        if (transportType == 1) {
            return 17303481;
        }
        return 17303477;
    }

    /* JADX WARNING: Removed duplicated region for block: B:36:0x0195  */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x0198  */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x01b9  */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x01bb  */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x01ee  */
    /* JADX WARNING: Removed duplicated region for block: B:45:0x01fb  */
    /* JADX WARNING: Removed duplicated region for block: B:48:0x0202  */
    /* JADX WARNING: Removed duplicated region for block: B:51:0x0216  */
    public void showNotification(int id, NotificationType notifyType, NetworkAgentInfo nai, NetworkAgentInfo switchToNai, PendingIntent intent, boolean highPriority) {
        String name;
        String extraInfo;
        int transportType;
        boolean z;
        CharSequence details;
        CharSequence title;
        String channelId;
        int i = id;
        NotificationType notificationType = notifyType;
        NetworkAgentInfo networkAgentInfo = nai;
        String tag = tagFor(id);
        int eventId = notificationType.eventId;
        if (networkAgentInfo != null) {
            transportType = getFirstTransportType(nai);
            extraInfo = networkAgentInfo.networkInfo.getExtraInfo();
            name = TextUtils.isEmpty(extraInfo) ? networkAgentInfo.networkCapabilities.getSSID() : extraInfo;
            if (!networkAgentInfo.networkCapabilities.hasCapability(12)) {
                return;
            }
        } else {
            transportType = 0;
            name = null;
            extraInfo = null;
        }
        int transportType2 = transportType;
        int previousEventId = this.mNotificationTypeMap.get(i);
        NotificationType previousNotifyType = NotificationType.getFromId(previousEventId);
        if (priority(previousNotifyType) > priority(notifyType)) {
            Slog.d(TAG, String.format("ignoring notification %s for network %s with existing notification %s", new Object[]{notificationType, Integer.valueOf(id), previousNotifyType}));
            return;
        }
        clearNotification(id);
        Slog.d(TAG, String.format("showNotification tag=%s event=%s transport=%s name=%s highPriority=%s", new Object[]{tag, nameOf(eventId), getTransportName(transportType2), name, Boolean.valueOf(highPriority)}));
        Resources r = Resources.getSystem();
        int icon = getIcon(transportType2);
        if (notificationType == NotificationType.NO_INTERNET && transportType2 == 1) {
            title = r.getString(17041399, new Object[]{0});
            details = r.getString(17041400);
        } else if (notificationType == NotificationType.LOST_INTERNET && transportType2 == 1) {
            title = r.getString(17041399, new Object[]{0});
            details = r.getString(17041400);
        } else {
            if (notificationType == NotificationType.SIGN_IN) {
                switch (transportType2) {
                    case 0:
                        z = false;
                        title = r.getString(17040546, new Object[]{0});
                        details = this.mTelephonyManager.getNetworkOperatorName();
                        break;
                    case 1:
                        CharSequence title2 = r.getString(17041389, new Object[]{0});
                        details = r.getString(17040547, new Object[]{WifiInfo.removeDoubleQuotes(networkAgentInfo.networkCapabilities.getSSID())});
                        this.mConnectivityService = (ConnectivityService) ServiceManager.getService("connectivity");
                        this.mConnectivityService.startBrowserForWifiPortal(new Notification(), extraInfo);
                        title = title2;
                        break;
                    default:
                        title = r.getString(17040546, new Object[]{0});
                        details = r.getString(17040547, new Object[]{name});
                        break;
                }
            } else if (notificationType == NotificationType.NETWORK_SWITCH) {
                String fromTransport = getTransportName(transportType2);
                String toTransport = getTransportName(getFirstTransportType(switchToNai));
                z = false;
                CharSequence title3 = r.getString(17040550, new Object[]{toTransport});
                String str = fromTransport;
                details = r.getString(17040551, new Object[]{toTransport, fromTransport});
                title = title3;
            } else {
                String str2 = name;
                int i2 = previousEventId;
                NotificationType notificationType2 = previousNotifyType;
                PendingIntent pendingIntent = intent;
                Slog.wtf(TAG, "Unknown notification type " + notificationType + " on network transport " + getTransportName(transportType2));
                return;
            }
            CharSequence title4 = title;
            if (!highPriority) {
                channelId = SystemNotificationChannels.NETWORK_ALERTS;
            } else {
                channelId = SystemNotificationChannels.NETWORK_STATUS;
            }
            String str3 = extraInfo;
            String str4 = name;
            String name2 = channelId;
            int i3 = previousEventId;
            NotificationType notificationType3 = previousNotifyType;
            Notification.Builder builder = new Notification.Builder(this.mContext, name2).setWhen(System.currentTimeMillis()).setShowWhen(notificationType != NotificationType.NETWORK_SWITCH ? true : z).setSmallIcon(icon).setTicker(title4).setColor(this.mContext.getColor(17170784)).setContentTitle(title4).setContentIntent(intent).setLocalOnly(true).setOnlyAlertOnce(true);
            if (notificationType != NotificationType.NETWORK_SWITCH) {
                builder.setStyle(new Notification.BigTextStyle().bigText(details));
            } else {
                builder.setContentText(details);
            }
            if (notificationType == NotificationType.SIGN_IN) {
                builder.extend(new Notification.TvExtender().setChannelId(name2));
            }
            Notification notification = builder.build();
            if (notificationType == NotificationType.SIGN_IN) {
                notification.flags |= 32;
            }
            this.mNotificationTypeMap.put(i, eventId);
            this.mNotificationManager.notifyAsUser(tag, eventId, notification, UserHandle.ALL);
        }
        z = false;
        CharSequence title42 = title;
        if (!highPriority) {
        }
        String str32 = extraInfo;
        String str42 = name;
        String name22 = channelId;
        int i32 = previousEventId;
        NotificationType notificationType32 = previousNotifyType;
        Notification.Builder builder2 = new Notification.Builder(this.mContext, name22).setWhen(System.currentTimeMillis()).setShowWhen(notificationType != NotificationType.NETWORK_SWITCH ? true : z).setSmallIcon(icon).setTicker(title42).setColor(this.mContext.getColor(17170784)).setContentTitle(title42).setContentIntent(intent).setLocalOnly(true).setOnlyAlertOnce(true);
        if (notificationType != NotificationType.NETWORK_SWITCH) {
        }
        if (notificationType == NotificationType.SIGN_IN) {
        }
        Notification notification2 = builder2.build();
        if (notificationType == NotificationType.SIGN_IN) {
        }
        this.mNotificationTypeMap.put(i, eventId);
        try {
            this.mNotificationManager.notifyAsUser(tag, eventId, notification2, UserHandle.ALL);
        } catch (NullPointerException npe) {
            Slog.d(TAG, "setNotificationVisible: visible notificationManager error", npe);
        }
    }

    public void clearNotification(int id) {
        if (this.mNotificationTypeMap.indexOfKey(id) >= 0) {
            String tag = tagFor(id);
            int eventId = this.mNotificationTypeMap.get(id);
            Slog.d(TAG, String.format("clearing notification tag=%s event=%s", new Object[]{tag, nameOf(eventId)}));
            try {
                this.mNotificationManager.cancelAsUser(tag, eventId, UserHandle.ALL);
            } catch (NullPointerException npe) {
                Slog.d(TAG, String.format("failed to clear notification tag=%s event=%s", new Object[]{tag, nameOf(eventId)}), npe);
            }
            this.mNotificationTypeMap.delete(id);
        }
    }

    public void setProvNotificationVisible(boolean visible, int id, String action) {
        if (visible) {
            int i = id;
            showNotification(i, NotificationType.SIGN_IN, null, null, PendingIntent.getBroadcast(this.mContext, 0, new Intent(action), 0), false);
            return;
        }
        clearNotification(id);
    }

    public void showToast(NetworkAgentInfo fromNai, NetworkAgentInfo toNai) {
        String fromTransport = getTransportName(getFirstTransportType(fromNai));
        String toTransport = getTransportName(getFirstTransportType(toNai));
        Toast.makeText(this.mContext, this.mContext.getResources().getString(17040552, new Object[]{fromTransport, toTransport}), 1).show();
    }

    @VisibleForTesting
    static String tagFor(int id) {
        return String.format("ConnectivityNotification:%d", new Object[]{Integer.valueOf(id)});
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
                return 4;
            case NO_INTERNET:
                return 3;
            case NETWORK_SWITCH:
                return 2;
            case LOST_INTERNET:
                return 1;
            default:
                return 0;
        }
    }
}
