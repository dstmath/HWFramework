package android.support.v4.app;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.RemoteInput;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompatBase.Action;
import android.support.v4.app.NotificationCompatBase.Action.Factory;
import android.widget.RemoteViews;
import java.util.ArrayList;

@RequiresApi(20)
class NotificationCompatApi20 {

    public static class Builder implements NotificationBuilderWithBuilderAccessor, NotificationBuilderWithActions {
        private android.app.Notification.Builder b;
        private RemoteViews mBigContentView;
        private RemoteViews mContentView;
        private Bundle mExtras;
        private int mGroupAlertBehavior;

        public Builder(Context context, Notification n, CharSequence contentTitle, CharSequence contentText, CharSequence contentInfo, RemoteViews tickerView, int number, PendingIntent contentIntent, PendingIntent fullScreenIntent, Bitmap largeIcon, int progressMax, int progress, boolean progressIndeterminate, boolean showWhen, boolean useChronometer, int priority, CharSequence subText, boolean localOnly, ArrayList<String> people, Bundle extras, String groupKey, boolean groupSummary, String sortKey, RemoteViews contentView, RemoteViews bigContentView, int groupAlertBehavior) {
            boolean z;
            android.app.Notification.Builder lights = new android.app.Notification.Builder(context).setWhen(n.when).setShowWhen(showWhen).setSmallIcon(n.icon, n.iconLevel).setContent(n.contentView).setTicker(n.tickerText, tickerView).setSound(n.sound, n.audioStreamType).setVibrate(n.vibrate).setLights(n.ledARGB, n.ledOnMS, n.ledOffMS);
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
            this.b = lights.setFullScreenIntent(fullScreenIntent, z).setLargeIcon(largeIcon).setNumber(number).setUsesChronometer(useChronometer).setPriority(priority).setProgress(progressMax, progress, progressIndeterminate).setLocalOnly(localOnly).setGroup(groupKey).setGroupSummary(groupSummary).setSortKey(sortKey);
            this.mExtras = new Bundle();
            if (extras != null) {
                this.mExtras.putAll(extras);
            }
            if (!(people == null || (people.isEmpty() ^ 1) == 0)) {
                this.mExtras.putStringArray(NotificationCompat.EXTRA_PEOPLE, (String[]) people.toArray(new String[people.size()]));
            }
            this.mContentView = contentView;
            this.mBigContentView = bigContentView;
            this.mGroupAlertBehavior = groupAlertBehavior;
        }

        public void addAction(Action action) {
            NotificationCompatApi20.addAction(this.b, action);
        }

        public android.app.Notification.Builder getBuilder() {
            return this.b;
        }

        public Notification build() {
            this.b.setExtras(this.mExtras);
            Notification notification = this.b.build();
            if (this.mContentView != null) {
                notification.contentView = this.mContentView;
            }
            if (this.mBigContentView != null) {
                notification.bigContentView = this.mBigContentView;
            }
            if (this.mGroupAlertBehavior != 0) {
                if (!(notification.getGroup() == null || (notification.flags & 512) == 0 || this.mGroupAlertBehavior != 2)) {
                    removeSoundAndVibration(notification);
                }
                if (notification.getGroup() != null && (notification.flags & 512) == 0 && this.mGroupAlertBehavior == 1) {
                    removeSoundAndVibration(notification);
                }
            }
            return notification;
        }

        private void removeSoundAndVibration(Notification notification) {
            notification.sound = null;
            notification.vibrate = null;
            notification.defaults &= -2;
            notification.defaults &= -3;
        }
    }

    NotificationCompatApi20() {
    }

    public static void addAction(android.app.Notification.Builder b, Action action) {
        Bundle actionExtras;
        android.app.Notification.Action.Builder actionBuilder = new android.app.Notification.Action.Builder(action.getIcon(), action.getTitle(), action.getActionIntent());
        if (action.getRemoteInputs() != null) {
            for (RemoteInput remoteInput : RemoteInputCompatApi20.fromCompat(action.getRemoteInputs())) {
                actionBuilder.addRemoteInput(remoteInput);
            }
        }
        if (action.getExtras() != null) {
            actionExtras = new Bundle(action.getExtras());
        } else {
            actionExtras = new Bundle();
        }
        actionExtras.putBoolean("android.support.allowGeneratedReplies", action.getAllowGeneratedReplies());
        actionBuilder.addExtras(actionExtras);
        b.addAction(actionBuilder.build());
    }

    public static Action getAction(Notification notif, int actionIndex, Factory actionFactory, RemoteInputCompatBase.RemoteInput.Factory remoteInputFactory) {
        return getActionCompatFromAction(notif.actions[actionIndex], actionFactory, remoteInputFactory);
    }

    private static Action getActionCompatFromAction(Notification.Action action, Factory actionFactory, RemoteInputCompatBase.RemoteInput.Factory remoteInputFactory) {
        return actionFactory.build(action.icon, action.title, action.actionIntent, action.getExtras(), RemoteInputCompatApi20.toCompat(action.getRemoteInputs(), remoteInputFactory), null, action.getExtras().getBoolean("android.support.allowGeneratedReplies"));
    }

    private static Notification.Action getActionFromActionCompat(Action actionCompat) {
        Bundle actionExtras;
        android.app.Notification.Action.Builder actionBuilder = new android.app.Notification.Action.Builder(actionCompat.getIcon(), actionCompat.getTitle(), actionCompat.getActionIntent());
        if (actionCompat.getExtras() != null) {
            actionExtras = new Bundle(actionCompat.getExtras());
        } else {
            actionExtras = new Bundle();
        }
        actionExtras.putBoolean("android.support.allowGeneratedReplies", actionCompat.getAllowGeneratedReplies());
        actionBuilder.addExtras(actionExtras);
        RemoteInputCompatBase.RemoteInput[] remoteInputCompats = actionCompat.getRemoteInputs();
        if (remoteInputCompats != null) {
            for (RemoteInput remoteInput : RemoteInputCompatApi20.fromCompat(remoteInputCompats)) {
                actionBuilder.addRemoteInput(remoteInput);
            }
        }
        return actionBuilder.build();
    }

    public static Action[] getActionsFromParcelableArrayList(ArrayList<Parcelable> parcelables, Factory actionFactory, RemoteInputCompatBase.RemoteInput.Factory remoteInputFactory) {
        if (parcelables == null) {
            return null;
        }
        Action[] actions = actionFactory.newArray(parcelables.size());
        for (int i = 0; i < actions.length; i++) {
            actions[i] = getActionCompatFromAction((Notification.Action) parcelables.get(i), actionFactory, remoteInputFactory);
        }
        return actions;
    }

    public static ArrayList<Parcelable> getParcelableArrayListForActions(Action[] actions) {
        if (actions == null) {
            return null;
        }
        ArrayList<Parcelable> parcelables = new ArrayList(actions.length);
        for (Action action : actions) {
            parcelables.add(getActionFromActionCompat(action));
        }
        return parcelables;
    }
}
