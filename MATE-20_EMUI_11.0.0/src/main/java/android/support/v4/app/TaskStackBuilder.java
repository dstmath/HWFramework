package android.support.v4.app;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import java.util.ArrayList;
import java.util.Iterator;

public final class TaskStackBuilder implements Iterable<Intent> {
    private static final String TAG = "TaskStackBuilder";
    private final ArrayList<Intent> mIntents = new ArrayList<>();
    private final Context mSourceContext;

    public interface SupportParentable {
        @Nullable
        Intent getSupportParentActivityIntent();
    }

    private TaskStackBuilder(Context a) {
        this.mSourceContext = a;
    }

    @NonNull
    public static TaskStackBuilder create(@NonNull Context context) {
        return new TaskStackBuilder(context);
    }

    @Deprecated
    public static TaskStackBuilder from(Context context) {
        return create(context);
    }

    @NonNull
    public TaskStackBuilder addNextIntent(@NonNull Intent nextIntent) {
        this.mIntents.add(nextIntent);
        return this;
    }

    @NonNull
    public TaskStackBuilder addNextIntentWithParentStack(@NonNull Intent nextIntent) {
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

    @NonNull
    public TaskStackBuilder addParentStack(@NonNull Activity sourceActivity) {
        Intent parent = null;
        if (sourceActivity instanceof SupportParentable) {
            parent = ((SupportParentable) sourceActivity).getSupportParentActivityIntent();
        }
        if (parent == null) {
            parent = NavUtils.getParentActivityIntent(sourceActivity);
        }
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

    @NonNull
    public TaskStackBuilder addParentStack(@NonNull Class<?> sourceActivityClass) {
        return addParentStack(new ComponentName(this.mSourceContext, sourceActivityClass));
    }

    public TaskStackBuilder addParentStack(ComponentName sourceActivityName) {
        int insertAt = this.mIntents.size();
        try {
            Intent parent = NavUtils.getParentActivityIntent(this.mSourceContext, sourceActivityName);
            while (parent != null) {
                this.mIntents.add(insertAt, parent);
                parent = NavUtils.getParentActivityIntent(this.mSourceContext, parent.getComponent());
            }
            return this;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Bad ComponentName while traversing activity parent metadata");
            throw new IllegalArgumentException(e);
        }
    }

    public int getIntentCount() {
        return this.mIntents.size();
    }

    @Deprecated
    public Intent getIntent(int index) {
        return editIntentAt(index);
    }

    @Nullable
    public Intent editIntentAt(int index) {
        return this.mIntents.get(index);
    }

    @Override // java.lang.Iterable
    @Deprecated
    public Iterator<Intent> iterator() {
        return this.mIntents.iterator();
    }

    public void startActivities() {
        startActivities(null);
    }

    public void startActivities(@Nullable Bundle options) {
        if (!this.mIntents.isEmpty()) {
            Intent[] intents = (Intent[]) this.mIntents.toArray(new Intent[this.mIntents.size()]);
            intents[0] = new Intent(intents[0]).addFlags(268484608);
            if (!ContextCompat.startActivities(this.mSourceContext, intents, options)) {
                Intent topIntent = new Intent(intents[intents.length - 1]);
                topIntent.addFlags(268435456);
                this.mSourceContext.startActivity(topIntent);
                return;
            }
            return;
        }
        throw new IllegalStateException("No intents added to TaskStackBuilder; cannot startActivities");
    }

    @Nullable
    public PendingIntent getPendingIntent(int requestCode, int flags) {
        return getPendingIntent(requestCode, flags, null);
    }

    @Nullable
    public PendingIntent getPendingIntent(int requestCode, int flags, @Nullable Bundle options) {
        if (!this.mIntents.isEmpty()) {
            Intent[] intents = (Intent[]) this.mIntents.toArray(new Intent[this.mIntents.size()]);
            intents[0] = new Intent(intents[0]).addFlags(268484608);
            if (Build.VERSION.SDK_INT >= 16) {
                return PendingIntent.getActivities(this.mSourceContext, requestCode, intents, flags, options);
            }
            return PendingIntent.getActivities(this.mSourceContext, requestCode, intents, flags);
        }
        throw new IllegalStateException("No intents added to TaskStackBuilder; cannot getPendingIntent");
    }

    @NonNull
    public Intent[] getIntents() {
        Intent[] intents = new Intent[this.mIntents.size()];
        if (intents.length == 0) {
            return intents;
        }
        intents[0] = new Intent(this.mIntents.get(0)).addFlags(268484608);
        for (int i = 1; i < intents.length; i++) {
            intents[i] = new Intent(this.mIntents.get(i));
        }
        return intents;
    }
}
