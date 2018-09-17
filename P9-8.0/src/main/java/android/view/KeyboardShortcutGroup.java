package android.view;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import com.android.internal.util.Preconditions;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public final class KeyboardShortcutGroup implements Parcelable {
    public static final Creator<KeyboardShortcutGroup> CREATOR = new Creator<KeyboardShortcutGroup>() {
        public KeyboardShortcutGroup createFromParcel(Parcel source) {
            return new KeyboardShortcutGroup(source, null);
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
        this(label, Collections.emptyList());
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
        boolean z = true;
        this.mItems = new ArrayList();
        this.mLabel = source.readCharSequence();
        source.readTypedList(this.mItems, KeyboardShortcutInfo.CREATOR);
        if (source.readInt() != 1) {
            z = false;
        }
        this.mSystemGroup = z;
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
