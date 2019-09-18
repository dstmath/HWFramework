package android.provider;

import android.annotation.SystemApi;
import android.content.Context;

@SystemApi
public class SearchIndexableResource extends SearchIndexableData {
    public int xmlResId;

    public SearchIndexableResource(int rank, int xmlResId2, String className, int iconResId) {
        this.rank = rank;
        this.xmlResId = xmlResId2;
        this.className = className;
        this.iconResId = iconResId;
    }

    public SearchIndexableResource(Context context) {
        super(context);
    }

    public String toString() {
        return "SearchIndexableResource[" + super.toString() + ", " + "xmlResId: " + this.xmlResId + "]";
    }
}
