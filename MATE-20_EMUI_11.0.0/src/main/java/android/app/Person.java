package android.app;

import android.graphics.drawable.Icon;
import android.os.Parcel;
import android.os.Parcelable;
import java.util.Objects;

public final class Person implements Parcelable {
    public static final Parcelable.Creator<Person> CREATOR = new Parcelable.Creator<Person>() {
        /* class android.app.Person.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public Person createFromParcel(Parcel in) {
            return new Person(in);
        }

        @Override // android.os.Parcelable.Creator
        public Person[] newArray(int size) {
            return new Person[size];
        }
    };
    private Icon mIcon;
    private boolean mIsBot;
    private boolean mIsImportant;
    private String mKey;
    private CharSequence mName;
    private String mUri;

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
        String str = this.mUri;
        if (str != null) {
            return str;
        }
        if (this.mName == null) {
            return "";
        }
        return "name:" + ((Object) this.mName);
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof Person)) {
            return false;
        }
        Person other = (Person) obj;
        if (!Objects.equals(this.mName, other.mName)) {
            return false;
        }
        Icon icon = this.mIcon;
        if (icon != null) {
            Icon icon2 = other.mIcon;
            if (icon2 == null || !icon.sameAs(icon2)) {
                return false;
            }
        } else if (other.mIcon != null) {
            return false;
        }
        if (!Objects.equals(this.mUri, other.mUri) || !Objects.equals(this.mKey, other.mKey) || this.mIsBot != other.mIsBot || this.mIsImportant != other.mIsImportant) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        return Objects.hash(this.mName, this.mIcon, this.mUri, this.mKey, Boolean.valueOf(this.mIsBot), Boolean.valueOf(this.mIsImportant));
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
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

    public static class Builder {
        private Icon mIcon;
        private boolean mIsBot;
        private boolean mIsImportant;
        private String mKey;
        private CharSequence mName;
        private String mUri;

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
}
