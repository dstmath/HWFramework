package android.support.v4.app;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompatBase.Action;
import android.widget.RemoteViews;
import java.util.ArrayList;

@RequiresApi(26)
class NotificationCompatApi26 {

    public static class Builder implements NotificationBuilderWithBuilderAccessor, NotificationBuilderWithActions {
        private android.app.Notification.Builder mB;

        Builder(Context context, Notification n, CharSequence contentTitle, CharSequence contentText, CharSequence contentInfo, RemoteViews tickerView, int number, PendingIntent contentIntent, PendingIntent fullScreenIntent, Bitmap largeIcon, int progressMax, int progress, boolean progressIndeterminate, boolean showWhen, boolean useChronometer, int priority, CharSequence subText, boolean localOnly, String category, ArrayList<String> people, Bundle extras, int color, int visibility, Notification publicVersion, String groupKey, boolean groupSummary, String sortKey, CharSequence[] remoteInputHistory, RemoteViews contentView, RemoteViews bigContentView, RemoteViews headsUpContentView, String channelId, int badgeIcon, String shortcutId, long timeoutMs, boolean colorized, boolean colorizedSet, int groupAlertBehavior) {
            boolean z;
            android.app.Notification.Builder lights = new android.app.Notification.Builder(context, channelId).setWhen(n.when).setShowWhen(showWhen).setSmallIcon(n.icon, n.iconLevel).setContent(n.contentView).setTicker(n.tickerText, tickerView).setSound(n.sound, n.audioStreamType).setVibrate(n.vibrate).setLights(n.ledARGB, n.ledOnMS, n.ledOffMS);
            if ((n.flags & 2) != 0) {
                z = true;
            } else {
                z = false;
            }
            lights = lights.setOngoing(z);
            if ((n.flags & 8) != 0) {
                z = true;
            } else {
                z = false;
            }
            lights = lights.setOnlyAlertOnce(z);
            if ((n.flags & 16) != 0) {
                z = true;
            } else {
                z = false;
            }
            lights = lights.setAutoCancel(z).setDefaults(n.defaults).setContentTitle(contentTitle).setContentText(contentText).setSubText(subText).setContentInfo(contentInfo).setContentIntent(contentIntent).setDeleteIntent(n.deleteIntent);
            if ((n.flags & 128) != 0) {
                z = true;
            } else {
                z = false;
            }
            this.mB = lights.setFullScreenIntent(fullScreenIntent, z).setLargeIcon(largeIcon).setNumber(number).setUsesChronometer(useChronometer).setPriority(priority).setProgress(progressMax, progress, progressIndeterminate).setLocalOnly(localOnly).setExtras(extras).setGroup(groupKey).setGroupSummary(groupSummary).setSortKey(sortKey).setCategory(category).setColor(color).setVisibility(visibility).setPublicVersion(publicVersion).setRemoteInputHistory(remoteInputHistory).setChannelId(channelId).setBadgeIconType(badgeIcon).setShortcutId(shortcutId).setTimeoutAfter(timeoutMs).setGroupAlertBehavior(groupAlertBehavior);
            if (colorizedSet) {
                this.mB.setColorized(colorized);
            }
            if (contentView != null) {
                this.mB.setCustomContentView(contentView);
            }
            if (bigContentView != null) {
                this.mB.setCustomBigContentView(bigContentView);
            }
            if (headsUpContentView != null) {
                this.mB.setCustomHeadsUpContentView(headsUpContentView);
            }
            for (String person : people) {
                this.mB.addPerson(person);
            }
        }

        public void addAction(Action action) {
            NotificationCompatApi24.addAction(this.mB, action);
        }

        public android.app.Notification.Builder getBuilder() {
            return this.mB;
        }

        public Notification build() {
            return this.mB.build();
        }
    }

    NotificationCompatApi26() {
    }
}
