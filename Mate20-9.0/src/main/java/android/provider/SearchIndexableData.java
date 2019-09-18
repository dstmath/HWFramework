package android.provider;

import android.annotation.SystemApi;
import android.content.Context;
import java.util.Locale;

@SystemApi
public abstract class SearchIndexableData {
    public String className;
    public Context context;
    public boolean enabled;
    public int iconResId;
    public String intentAction;
    public String intentTargetClass;
    public String intentTargetPackage;
    public String key;
    public Locale locale;
    public String packageName;
    public int rank;
    public int userId;

    public SearchIndexableData() {
        this.userId = -1;
        this.locale = Locale.getDefault();
        this.enabled = true;
    }

    public SearchIndexableData(Context ctx) {
        this();
        this.context = ctx;
    }

    public String toString() {
        return "SearchIndexableData[context: " + this.context + ", " + "locale: " + this.locale + ", " + "enabled: " + this.enabled + ", " + "rank: " + this.rank + ", " + "key: " + this.key + ", " + "userId: " + this.userId + ", " + "className: " + this.className + ", " + "packageName: " + this.packageName + ", " + "iconResId: " + this.iconResId + ", " + "intentAction: " + this.intentAction + ", " + "intentTargetPackage: " + this.intentTargetPackage + ", " + "intentTargetClass: " + this.intentTargetClass + "]";
    }
}
