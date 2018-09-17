package android.view;

import android.graphics.drawable.Icon;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import com.android.internal.util.Preconditions;

public final class KeyboardShortcutInfo implements Parcelable {
    public static final Creator<KeyboardShortcutInfo> CREATOR = new Creator<KeyboardShortcutInfo>() {
        public KeyboardShortcutInfo createFromParcel(Parcel source) {
            return new KeyboardShortcutInfo(source, null);
        }

        public KeyboardShortcutInfo[] newArray(int size) {
            return new KeyboardShortcutInfo[size];
        }
    };
    private final char mBaseCharacter;
    private final Icon mIcon;
    private final int mKeycode;
    private final CharSequence mLabel;
    private final int mModifiers;

    /* synthetic */ KeyboardShortcutInfo(Parcel source, KeyboardShortcutInfo -this1) {
        this(source);
    }

    public KeyboardShortcutInfo(CharSequence label, Icon icon, int keycode, int modifiers) {
        boolean z = false;
        this.mLabel = label;
        this.mIcon = icon;
        this.mBaseCharacter = 0;
        if (keycode >= 0 && keycode <= KeyEvent.getMaxKeyCode()) {
            z = true;
        }
        Preconditions.checkArgument(z);
        this.mKeycode = keycode;
        this.mModifiers = modifiers;
    }

    public KeyboardShortcutInfo(CharSequence label, int keycode, int modifiers) {
        this(label, null, keycode, modifiers);
    }

    public KeyboardShortcutInfo(CharSequence label, char baseCharacter, int modifiers) {
        this.mLabel = label;
        Preconditions.checkArgument(baseCharacter != 0);
        this.mBaseCharacter = baseCharacter;
        this.mKeycode = 0;
        this.mModifiers = modifiers;
        this.mIcon = null;
    }

    private KeyboardShortcutInfo(Parcel source) {
        this.mLabel = source.readCharSequence();
        this.mIcon = (Icon) source.readParcelable(null);
        this.mBaseCharacter = (char) source.readInt();
        this.mKeycode = source.readInt();
        this.mModifiers = source.readInt();
    }

    public CharSequence getLabel() {
        return this.mLabel;
    }

    public Icon getIcon() {
        return this.mIcon;
    }

    public int getKeycode() {
        return this.mKeycode;
    }

    public char getBaseCharacter() {
        return this.mBaseCharacter;
    }

    public int getModifiers() {
        return this.mModifiers;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeCharSequence(this.mLabel);
        dest.writeParcelable(this.mIcon, 0);
        dest.writeInt(this.mBaseCharacter);
        dest.writeInt(this.mKeycode);
        dest.writeInt(this.mModifiers);
    }
}
