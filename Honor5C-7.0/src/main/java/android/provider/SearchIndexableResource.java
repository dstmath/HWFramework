package android.provider;

import android.content.Context;

public class SearchIndexableResource extends SearchIndexableData {
    public int xmlResId;

    public SearchIndexableResource(int rank, int xmlResId, String className, int iconResId) {
        this.rank = rank;
        this.xmlResId = xmlResId;
        this.className = className;
        this.iconResId = iconResId;
    }

    public SearchIndexableResource(Context context) {
        super(context);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("SearchIndexableResource[");
        sb.append(super.toString());
        sb.append(", ");
        sb.append("xmlResId: ");
        sb.append(this.xmlResId);
        sb.append("]");
        return sb.toString();
    }
}
