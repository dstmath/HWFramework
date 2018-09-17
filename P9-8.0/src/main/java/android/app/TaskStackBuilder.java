package android.app;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.UserHandle;
import android.util.Log;
import java.util.ArrayList;

public class TaskStackBuilder {
    private static final String TAG = "TaskStackBuilder";
    private final ArrayList<Intent> mIntents = new ArrayList();
    private final Context mSourceContext;

    private TaskStackBuilder(Context a) {
        this.mSourceContext = a;
    }

    public static TaskStackBuilder create(Context context) {
        return new TaskStackBuilder(context);
    }

    public TaskStackBuilder addNextIntent(Intent nextIntent) {
        this.mIntents.add(nextIntent);
        return this;
    }

    public TaskStackBuilder addNextIntentWithParentStack(Intent nextIntent) {
        ComponentName target = nextIntent.getComponent();
        if (target == null) {
            target = nextIntent.resolveActivity(this.mSourceContext.getPackageManager());
        }
        if (target != null) {
            addParentStack(target);
        }
        addNextIntent(nextIntent);
        return this;
    }

    public TaskStackBuilder addParentStack(Activity sourceActivity) {
        Intent parent = sourceActivity.getParentActivityIntent();
        if (parent != null) {
            ComponentName target = parent.getComponent();
            if (target == null) {
                target = parent.resolveActivity(this.mSourceContext.getPackageManager());
            }
            addParentStack(target);
            addNextIntent(parent);
        }
        return this;
    }

    public TaskStackBuilder addParentStack(Class<?> sourceActivityClass) {
        return addParentStack(new ComponentName(this.mSourceContext, (Class) sourceActivityClass));
    }

    public TaskStackBuilder addParentStack(ComponentName sourceActivityName) {
        int insertAt = this.mIntents.size();
        PackageManager pm = this.mSourceContext.getPackageManager();
        try {
            ActivityInfo info = pm.getActivityInfo(sourceActivityName, 0);
            String parentActivity = info.parentActivityName;
            while (parentActivity != null) {
                Intent parent;
                ComponentName target = new ComponentName(info.packageName, parentActivity);
                info = pm.getActivityInfo(target, 0);
                parentActivity = info.parentActivityName;
                if (parentActivity == null && insertAt == 0) {
                    parent = Intent.makeMainActivity(target);
                } else {
                    parent = new Intent().setComponent(target);
                }
                this.mIntents.add(insertAt, parent);
            }
            return this;
        } catch (NameNotFoundException e) {
            Log.e(TAG, "Bad ComponentName while traversing activity parent metadata");
            throw new IllegalArgumentException(e);
        }
    }

    public int getIntentCount() {
        return this.mIntents.size();
    }

    public Intent editIntentAt(int index) {
        return (Intent) this.mIntents.get(index);
    }

    public void startActivities() {
        startActivities(null);
    }

    public void startActivities(Bundle options, UserHandle userHandle) {
        if (this.mIntents.isEmpty()) {
            throw new IllegalStateException("No intents added to TaskStackBuilder; cannot startActivities");
        }
        this.mSourceContext.startActivitiesAsUser(getIntents(), options, userHandle);
    }

    public void startActivities(Bundle options) {
        startActivities(options, new UserHandle(UserHandle.myUserId()));
    }

    public PendingIntent getPendingIntent(int requestCode, int flags) {
        return getPendingIntent(requestCode, flags, null);
    }

    public PendingIntent getPendingIntent(int requestCode, int flags, Bundle options) {
        if (!this.mIntents.isEmpty()) {
            return PendingIntent.getActivities(this.mSourceContext, requestCode, getIntents(), flags, options);
        }
        throw new IllegalStateException("No intents added to TaskStackBuilder; cannot getPendingIntent");
    }

    public PendingIntent getPendingIntent(int requestCode, int flags, Bundle options, UserHandle user) {
        if (this.mIntents.isEmpty()) {
            throw new IllegalStateException("No intents added to TaskStackBuilder; cannot getPendingIntent");
        }
        return PendingIntent.getActivitiesAsUser(this.mSourceContext, requestCode, getIntents(), flags, options, user);
    }

    public Intent[] getIntents() {
        Intent[] intents = new Intent[this.mIntents.size()];
        if (intents.length == 0) {
            return intents;
        }
        intents[0] = new Intent((Intent) this.mIntents.get(0)).addFlags(268484608);
        for (int i = 1; i < intents.length; i++) {
            intents[i] = new Intent((Intent) this.mIntents.get(i));
        }
        return intents;
    }
}
