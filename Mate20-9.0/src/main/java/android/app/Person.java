package android.app;

import android.graphics.drawable.Icon;
import android.os.Parcel;
import android.os.Parcelable;

public final class Person implements Parcelable {
    public static final Parcelable.Creator<Person> CREATOR = new Parcelable.Creator<Person>() {
        public Person createFromParcel(Parcel in) {
            return new Person(in);
        }

        public Person[] newArray(int size) {
            return new Person[size];
        }
    };
    /* access modifiers changed from: private */
    public Icon mIcon;
    /* access modifiers changed from: private */
    public boolean mIsBot;
    /* access modifiers changed from: private */
    public boolean mIsImportant;
    /* access modifiers changed from: private */
    public String mKey;
    /* access modifiers changed from: private */
    public CharSequence mName;
    /* access modifiers changed from: private */
    public String mUri;

    public static class Builder {
        /* access modifiers changed from: private */
        public Icon mIcon;
        /* access modifiers changed from: private */
        public boolean mIsBot;
        /* access modifiers changed from: private */
        public boolean mIsImportant;
        /* access modifiers changed from: private */
        public String mKey;
        /* access modifiers changed from: private */
        public CharSequence mName;
        /* access modifiers changed from: private */
        public String mUri;

        public Builder() {
        }

        private Builder(Person person) {
            this.mName = person.mName;
            this.mIcon = person.mIcon;
            this.mUri = person.mUri;
            this.mKey = person.mKey;
            this.mIsBot = person.mIsBot;
            this.mIsImportant = person.mIsImportant;
        }

        public Builder setName(CharSequence name) {
            this.mName = name;
            return this;
        }

        public Builder setIcon(Icon icon) {
            this.mIcon = icon;
            return this;
        }

        public Builder setUri(String uri) {
            this.mUri = uri;
            return this;
        }

        public Builder setKey(String key) {
            this.mKey = key;
            return this;
        }

        public Builder setImportant(boolean isImportant) {
            this.mIsImportant = isImportant;
            return this;
        }

        public Builder setBot(boolean isBot) {
            this.mIsBot = isBot;
            return this;
        }

        public Person build() {
            return new Person(this);
        }
    }

    private Person(Parcel in) {
        this.mName = in.readCharSequence();
        if (in.readInt() != 0) {
            this.mIcon = Icon.CREATOR.createFromParcel(in);
        }
        this.mUri = in.readString();
        this.mKey = in.readString();
        this.mIsImportant = in.readBoolean();
        this.mIsBot = in.readBoolean();
    }

    private Person(Builder builder) {
        this.mName = builder.mName;
        this.mIcon = builder.mIcon;
        this.mUri = builder.mUri;
        this.mKey = builder.mKey;
        this.mIsBot = builder.mIsBot;
        this.mIsImportant = builder.mIsImportant;
    }

    public Builder toBuilder() {
        return new Builder();
    }

    public String getUri() {
        return this.mUri;
    }

    public CharSequence getName() {
        return this.mName;
    }

    public Icon getIcon() {
        return this.mIcon;
    }

    public String getKey() {
        return this.mKey;
    }

    public boolean isBot() {
        return this.mIsBot;
    }

    public boolean isImportant() {
        return this.mIsImportant;
    }

    public String resolveToLegacyUri() {
        if (this.mUri != null) {
            return this.mUri;
        }
        if (this.mName == null) {
            return "";
        }
        return "name:" + this.mName;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeCharSequence(this.mName);
        if (this.mIcon != null) {
            dest.writeInt(1);
            this.mIcon.writeToParcel(dest, 0);
        } else {
            dest.writeInt(0);
        }
        dest.writeString(this.mUri);
        dest.writeString(this.mKey);
        dest.writeBoolean(this.mIsImportant);
        dest.writeBoolean(this.mIsBot);
    }
}
