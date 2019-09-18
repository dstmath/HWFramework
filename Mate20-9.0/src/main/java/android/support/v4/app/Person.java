package android.support.v4.app;

import android.app.Person;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.annotation.RestrictTo;
import android.support.v4.graphics.drawable.IconCompat;

public class Person {
    private static final String ICON_KEY = "icon";
    private static final String IS_BOT_KEY = "isBot";
    private static final String IS_IMPORTANT_KEY = "isImportant";
    private static final String KEY_KEY = "key";
    private static final String NAME_KEY = "name";
    private static final String URI_KEY = "uri";
    /* access modifiers changed from: private */
    @Nullable
    public IconCompat mIcon;
    /* access modifiers changed from: private */
    public boolean mIsBot;
    /* access modifiers changed from: private */
    public boolean mIsImportant;
    /* access modifiers changed from: private */
    @Nullable
    public String mKey;
    /* access modifiers changed from: private */
    @Nullable
    public CharSequence mName;
    /* access modifiers changed from: private */
    @Nullable
    public String mUri;

    public static class Builder {
        /* access modifiers changed from: private */
        @Nullable
        public IconCompat mIcon;
        /* access modifiers changed from: private */
        public boolean mIsBot;
        /* access modifiers changed from: private */
        public boolean mIsImportant;
        /* access modifiers changed from: private */
        @Nullable
        public String mKey;
        /* access modifiers changed from: private */
        @Nullable
        public CharSequence mName;
        /* access modifiers changed from: private */
        @Nullable
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

        @NonNull
        public Builder setName(@Nullable CharSequence name) {
            this.mName = name;
            return this;
        }

        @NonNull
        public Builder setIcon(@Nullable IconCompat icon) {
            this.mIcon = icon;
            return this;
        }

        @NonNull
        public Builder setUri(@Nullable String uri) {
            this.mUri = uri;
            return this;
        }

        @NonNull
        public Builder setKey(@Nullable String key) {
            this.mKey = key;
            return this;
        }

        @NonNull
        public Builder setBot(boolean bot) {
            this.mIsBot = bot;
            return this;
        }

        @NonNull
        public Builder setImportant(boolean important) {
            this.mIsImportant = important;
            return this;
        }

        @NonNull
        public Person build() {
            return new Person(this);
        }
    }

    @NonNull
    public static Person fromBundle(@NonNull Bundle bundle) {
        Bundle iconBundle = bundle.getBundle(ICON_KEY);
        return new Builder().setName(bundle.getCharSequence(NAME_KEY)).setIcon(iconBundle != null ? IconCompat.createFromBundle(iconBundle) : null).setUri(bundle.getString(URI_KEY)).setKey(bundle.getString(KEY_KEY)).setBot(bundle.getBoolean(IS_BOT_KEY)).setImportant(bundle.getBoolean(IS_IMPORTANT_KEY)).build();
    }

    @RequiresApi(28)
    @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP})
    @NonNull
    public static Person fromAndroidPerson(@NonNull android.app.Person person) {
        return new Builder().setName(person.getName()).setIcon(person.getIcon() != null ? IconCompat.createFromIcon(person.getIcon()) : null).setUri(person.getUri()).setKey(person.getKey()).setBot(person.isBot()).setImportant(person.isImportant()).build();
    }

    private Person(Builder builder) {
        this.mName = builder.mName;
        this.mIcon = builder.mIcon;
        this.mUri = builder.mUri;
        this.mKey = builder.mKey;
        this.mIsBot = builder.mIsBot;
        this.mIsImportant = builder.mIsImportant;
    }

    @NonNull
    public Bundle toBundle() {
        Bundle result = new Bundle();
        result.putCharSequence(NAME_KEY, this.mName);
        result.putBundle(ICON_KEY, this.mIcon != null ? this.mIcon.toBundle() : null);
        result.putString(URI_KEY, this.mUri);
        result.putString(KEY_KEY, this.mKey);
        result.putBoolean(IS_BOT_KEY, this.mIsBot);
        result.putBoolean(IS_IMPORTANT_KEY, this.mIsImportant);
        return result;
    }

    @NonNull
    public Builder toBuilder() {
        return new Builder();
    }

    @RequiresApi(28)
    @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP})
    @NonNull
    public android.app.Person toAndroidPerson() {
        return new Person.Builder().setName(getName()).setIcon(getIcon() != null ? getIcon().toIcon() : null).setUri(getUri()).setKey(getKey()).setBot(isBot()).setImportant(isImportant()).build();
    }

    @Nullable
    public CharSequence getName() {
        return this.mName;
    }

    @Nullable
    public IconCompat getIcon() {
        return this.mIcon;
    }

    @Nullable
    public String getUri() {
        return this.mUri;
    }

    @Nullable
    public String getKey() {
        return this.mKey;
    }

    public boolean isBot() {
        return this.mIsBot;
    }

    public boolean isImportant() {
        return this.mIsImportant;
    }
}
