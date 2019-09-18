package android.view;

import android.os.Parcel;
import android.os.Parcelable;
import com.android.internal.util.Preconditions;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public final class KeyboardShortcutGroup implements Parcelable {
    public static final Parcelable.Creator<KeyboardShortcutGroup> CREATOR = new Parcelable.Creator<KeyboardShortcutGroup>() {
        public KeyboardShortcutGroup createFromParcel(Parcel source) {
            return new KeyboardShortcutGroup(source);
        }

        public KeyboardShortcutGroup[] newArray(int size) {
            return new KeyboardShortcutGroup[size];
        }
    };
    private final List<KeyboardShortcutInfo> mItems;
    private final CharSequence mLabel;
    private boolean mSystemGroup;

    public KeyboardShortcutGroup(CharSequence label, List<KeyboardShortcutInfo> items) {
        this.mLabel = label;
        this.mItems = new ArrayList((Collection) Preconditions.checkNotNull(items));
    }

    public KeyboardShortcutGroup(CharSequence label) {
        this(label, (List<KeyboardShortcutInfo>) Collections.emptyList());
    }

    public KeyboardShortcutGroup(CharSequence label, List<KeyboardShortcutInfo> items, boolean isSystemGroup) {
        this.mLabel = label;
        this.mItems = new ArrayList((Collection) Preconditions.checkNotNull(items));
        this.mSystemGroup = isSystemGroup;
    }

    public KeyboardShortcutGroup(CharSequence label, boolean isSystemGroup) {
        this(label, Collections.emptyList(), isSystemGroup);
    }

    private KeyboardShortcutGroup(Parcel source) {
        this.mItems = new ArrayList();
        this.mLabel = source.readCharSequence();
        source.readTypedList(this.mItems, KeyboardShortcutInfo.CREATOR);
        this.mSystemGroup = source.readInt() != 1 ? false : true;
    }

    public CharSequence getLabel() {
        return this.mLabel;
    }

    public List<KeyboardShortcutInfo> getItems() {
        return this.mItems;
    }

    public boolean isSystemGroup() {
        return this.mSystemGroup;
    }

    public void addItem(KeyboardShortcutInfo item) {
        this.mItems.add(item);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeCharSequence(this.mLabel);
        dest.writeTypedList(this.mItems);
        dest.writeInt(this.mSystemGroup ? 1 : 0);
    }
}
