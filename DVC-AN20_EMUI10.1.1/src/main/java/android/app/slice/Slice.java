package android.app.slice;

import android.app.PendingIntent;
import android.app.RemoteInput;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.Preconditions;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public final class Slice implements Parcelable {
    public static final Parcelable.Creator<Slice> CREATOR = new Parcelable.Creator<Slice>() {
        /* class android.app.slice.Slice.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public Slice createFromParcel(Parcel in) {
            return new Slice(in);
        }

        @Override // android.os.Parcelable.Creator
        public Slice[] newArray(int size) {
            return new Slice[size];
        }
    };
    public static final String EXTRA_RANGE_VALUE = "android.app.slice.extra.RANGE_VALUE";
    @Deprecated
    public static final String EXTRA_SLIDER_VALUE = "android.app.slice.extra.SLIDER_VALUE";
    public static final String EXTRA_TOGGLE_STATE = "android.app.slice.extra.TOGGLE_STATE";
    public static final String HINT_ACTIONS = "actions";
    public static final String HINT_CALLER_NEEDED = "caller_needed";
    public static final String HINT_ERROR = "error";
    public static final String HINT_HORIZONTAL = "horizontal";
    public static final String HINT_KEYWORDS = "keywords";
    public static final String HINT_LARGE = "large";
    public static final String HINT_LAST_UPDATED = "last_updated";
    public static final String HINT_LIST = "list";
    public static final String HINT_LIST_ITEM = "list_item";
    public static final String HINT_NO_TINT = "no_tint";
    public static final String HINT_PARTIAL = "partial";
    public static final String HINT_PERMISSION_REQUEST = "permission_request";
    public static final String HINT_SEE_MORE = "see_more";
    public static final String HINT_SELECTED = "selected";
    public static final String HINT_SHORTCUT = "shortcut";
    public static final String HINT_SUMMARY = "summary";
    public static final String HINT_TITLE = "title";
    public static final String HINT_TOGGLE = "toggle";
    public static final String HINT_TTL = "ttl";
    public static final String SUBTYPE_COLOR = "color";
    public static final String SUBTYPE_CONTENT_DESCRIPTION = "content_description";
    public static final String SUBTYPE_LAYOUT_DIRECTION = "layout_direction";
    public static final String SUBTYPE_MAX = "max";
    public static final String SUBTYPE_MESSAGE = "message";
    public static final String SUBTYPE_MILLIS = "millis";
    public static final String SUBTYPE_PRIORITY = "priority";
    public static final String SUBTYPE_RANGE = "range";
    @Deprecated
    public static final String SUBTYPE_SLIDER = "slider";
    public static final String SUBTYPE_SOURCE = "source";
    public static final String SUBTYPE_TOGGLE = "toggle";
    public static final String SUBTYPE_VALUE = "value";
    private final String[] mHints;
    private final SliceItem[] mItems;
    private SliceSpec mSpec;
    private Uri mUri;

    @Retention(RetentionPolicy.SOURCE)
    public @interface SliceHint {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface SliceSubtype {
    }

    Slice(ArrayList<SliceItem> items, String[] hints, Uri uri, SliceSpec spec) {
        this.mHints = hints;
        this.mItems = (SliceItem[]) items.toArray(new SliceItem[items.size()]);
        this.mUri = uri;
        this.mSpec = spec;
    }

    protected Slice(Parcel in) {
        this.mHints = in.readStringArray();
        int n = in.readInt();
        this.mItems = new SliceItem[n];
        for (int i = 0; i < n; i++) {
            this.mItems[i] = SliceItem.CREATOR.createFromParcel(in);
        }
        this.mUri = Uri.CREATOR.createFromParcel(in);
        this.mSpec = (SliceSpec) in.readTypedObject(SliceSpec.CREATOR);
    }

    public SliceSpec getSpec() {
        return this.mSpec;
    }

    public Uri getUri() {
        return this.mUri;
    }

    public List<SliceItem> getItems() {
        return Arrays.asList(this.mItems);
    }

    public List<String> getHints() {
        return Arrays.asList(this.mHints);
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(this.mHints);
        dest.writeInt(this.mItems.length);
        int i = 0;
        while (true) {
            SliceItem[] sliceItemArr = this.mItems;
            if (i < sliceItemArr.length) {
                sliceItemArr[i].writeToParcel(dest, flags);
                i++;
            } else {
                this.mUri.writeToParcel(dest, 0);
                dest.writeTypedObject(this.mSpec, flags);
                return;
            }
        }
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public boolean hasHint(String hint) {
        return ArrayUtils.contains(this.mHints, hint);
    }

    public boolean isCallerNeeded() {
        return hasHint(HINT_CALLER_NEEDED);
    }

    public static class Builder {
        private ArrayList<String> mHints = new ArrayList<>();
        private ArrayList<SliceItem> mItems = new ArrayList<>();
        private SliceSpec mSpec;
        private final Uri mUri;

        @Deprecated
        public Builder(Uri uri) {
            this.mUri = uri;
        }

        public Builder(Uri uri, SliceSpec spec) {
            this.mUri = uri;
            this.mSpec = spec;
        }

        public Builder(Builder parent) {
            this.mUri = parent.mUri.buildUpon().appendPath("_gen").appendPath(String.valueOf(this.mItems.size())).build();
        }

        public Builder setCallerNeeded(boolean callerNeeded) {
            if (callerNeeded) {
                this.mHints.add(Slice.HINT_CALLER_NEEDED);
            } else {
                this.mHints.remove(Slice.HINT_CALLER_NEEDED);
            }
            return this;
        }

        public Builder addHints(List<String> hints) {
            this.mHints.addAll(hints);
            return this;
        }

        public Builder setSpec(SliceSpec spec) {
            this.mSpec = spec;
            return this;
        }

        public Builder addSubSlice(Slice slice, String subType) {
            Preconditions.checkNotNull(slice);
            this.mItems.add(new SliceItem(slice, "slice", subType, (String[]) slice.getHints().toArray(new String[slice.getHints().size()])));
            return this;
        }

        public Builder addAction(PendingIntent action, Slice s, String subType) {
            Preconditions.checkNotNull(action);
            Preconditions.checkNotNull(s);
            List<String> hints = s.getHints();
            s.mSpec = null;
            this.mItems.add(new SliceItem(action, s, "action", subType, (String[]) hints.toArray(new String[hints.size()])));
            return this;
        }

        public Builder addText(CharSequence text, String subType, List<String> hints) {
            this.mItems.add(new SliceItem(text, "text", subType, hints));
            return this;
        }

        public Builder addIcon(Icon icon, String subType, List<String> hints) {
            Preconditions.checkNotNull(icon);
            this.mItems.add(new SliceItem(icon, SliceItem.FORMAT_IMAGE, subType, hints));
            return this;
        }

        public Builder addRemoteInput(RemoteInput remoteInput, String subType, List<String> hints) {
            Preconditions.checkNotNull(remoteInput);
            this.mItems.add(new SliceItem(remoteInput, "input", subType, hints));
            return this;
        }

        public Builder addInt(int value, String subType, List<String> hints) {
            this.mItems.add(new SliceItem(Integer.valueOf(value), SliceItem.FORMAT_INT, subType, hints));
            return this;
        }

        @Deprecated
        public Builder addTimestamp(long time, String subType, List<String> hints) {
            return addLong(time, subType, hints);
        }

        public Builder addLong(long value, String subType, List<String> hints) {
            this.mItems.add(new SliceItem(Long.valueOf(value), "long", subType, (String[]) hints.toArray(new String[hints.size()])));
            return this;
        }

        public Builder addBundle(Bundle bundle, String subType, List<String> hints) {
            Preconditions.checkNotNull(bundle);
            this.mItems.add(new SliceItem(bundle, SliceItem.FORMAT_BUNDLE, subType, hints));
            return this;
        }

        public Slice build() {
            ArrayList<SliceItem> arrayList = this.mItems;
            ArrayList<String> arrayList2 = this.mHints;
            return new Slice(arrayList, (String[]) arrayList2.toArray(new String[arrayList2.size()]), this.mUri, this.mSpec);
        }
    }

    public String toString() {
        return toString("");
    }

    private String toString(String indent) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < this.mItems.length; i++) {
            sb.append(indent);
            if (Objects.equals(this.mItems[i].getFormat(), "slice")) {
                sb.append("slice:\n");
                Slice slice = this.mItems[i].getSlice();
                sb.append(slice.toString(indent + "   "));
            } else if (Objects.equals(this.mItems[i].getFormat(), "text")) {
                sb.append("text: ");
                sb.append(this.mItems[i].getText());
                sb.append("\n");
            } else {
                sb.append(this.mItems[i].getFormat());
                sb.append("\n");
            }
        }
        return sb.toString();
    }
}
