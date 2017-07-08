package android.content.pm;

import android.app.Instrumentation;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.PersistableBundle;
import android.os.UserHandle;
import android.provider.Settings.Bookmarks;
import android.rms.iaware.DataContract.BaseAttr;
import android.util.ArraySet;
import com.android.internal.util.Preconditions;
import java.util.Set;

public final class ShortcutInfo implements Parcelable {
    public static final int CLONE_REMOVE_FOR_CREATOR = 1;
    public static final int CLONE_REMOVE_FOR_LAUNCHER = 3;
    private static final int CLONE_REMOVE_ICON = 1;
    private static final int CLONE_REMOVE_INTENT = 2;
    public static final int CLONE_REMOVE_NON_KEY_INFO = 4;
    public static final Creator<ShortcutInfo> CREATOR = null;
    public static final int FLAG_DYNAMIC = 1;
    public static final int FLAG_HAS_ICON_FILE = 8;
    public static final int FLAG_HAS_ICON_RES = 4;
    public static final int FLAG_KEY_FIELDS_ONLY = 16;
    public static final int FLAG_PINNED = 2;
    public static final String SHORTCUT_CATEGORY_CONVERSATION = "android.shortcut.conversation";
    private ComponentName mActivityComponent;
    private String mBitmapPath;
    private ArraySet<String> mCategories;
    private PersistableBundle mExtras;
    private int mFlags;
    private Icon mIcon;
    private int mIconResourceId;
    private final String mId;
    private Intent mIntent;
    private PersistableBundle mIntentPersistableExtras;
    private long mLastChangedTimestamp;
    private final String mPackageName;
    private String mText;
    private String mTitle;
    private final int mUserId;
    private int mWeight;

    public static class Builder {
        private ComponentName mActivityComponent;
        private Set<String> mCategories;
        private final Context mContext;
        private PersistableBundle mExtras;
        private Icon mIcon;
        private String mId;
        private Intent mIntent;
        private String mText;
        private String mTitle;
        private int mWeight;

        public Builder(Context context) {
            this.mContext = context;
        }

        public Builder setId(String id) {
            this.mId = (String) Preconditions.checkStringNotEmpty(id, Instrumentation.REPORT_KEY_IDENTIFIER);
            return this;
        }

        public Builder setActivityComponent(ComponentName activityComponent) {
            this.mActivityComponent = (ComponentName) Preconditions.checkNotNull(activityComponent, "activityComponent");
            return this;
        }

        public Builder setIcon(Icon icon) {
            this.mIcon = ShortcutInfo.validateIcon(icon);
            return this;
        }

        public Builder setTitle(String title) {
            this.mTitle = (String) Preconditions.checkStringNotEmpty(title, Bookmarks.TITLE);
            return this;
        }

        public Builder setText(String text) {
            this.mText = (String) Preconditions.checkStringNotEmpty(text, StreamItemsColumns.TEXT);
            return this;
        }

        public Builder setCategories(Set<String> categories) {
            this.mCategories = categories;
            return this;
        }

        public Builder setIntent(Intent intent) {
            this.mIntent = (Intent) Preconditions.checkNotNull(intent, BaseAttr.INTENT);
            return this;
        }

        public Builder setWeight(int weight) {
            this.mWeight = weight;
            return this;
        }

        public Builder setExtras(PersistableBundle extras) {
            this.mExtras = extras;
            return this;
        }

        public ShortcutInfo build() {
            return new ShortcutInfo();
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.content.pm.ShortcutInfo.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.content.pm.ShortcutInfo.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.content.pm.ShortcutInfo.<clinit>():void");
    }

    public void clearFlags(int r1) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.content.pm.ShortcutInfo.clearFlags(int):void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:568)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:99)
	... 5 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.content.pm.ShortcutInfo.clearFlags(int):void");
    }

    private ShortcutInfo(Builder b) {
        this.mUserId = b.mContext.getUserId();
        this.mId = (String) Preconditions.checkStringNotEmpty(b.mId, "Shortcut ID must be provided");
        this.mPackageName = b.mContext.getPackageName();
        this.mActivityComponent = b.mActivityComponent;
        this.mIcon = b.mIcon;
        this.mTitle = b.mTitle;
        this.mText = b.mText;
        this.mCategories = clone(b.mCategories);
        this.mIntent = b.mIntent;
        if (this.mIntent != null) {
            Bundle intentExtras = this.mIntent.getExtras();
            if (intentExtras != null) {
                this.mIntent.replaceExtras((Bundle) null);
                this.mIntentPersistableExtras = new PersistableBundle(intentExtras);
            }
        }
        this.mWeight = b.mWeight;
        this.mExtras = b.mExtras;
        updateTimestamp();
    }

    private <T> ArraySet<T> clone(Set<T> source) {
        return source == null ? null : new ArraySet(source);
    }

    public void enforceMandatoryFields() {
        Preconditions.checkStringNotEmpty(this.mId, "Shortcut ID must be provided");
        Preconditions.checkStringNotEmpty(this.mTitle, "Shortcut title must be provided");
        Preconditions.checkNotNull(this.mIntent, "Shortcut Intent must be provided");
    }

    private ShortcutInfo(ShortcutInfo source, int cloneFlags) {
        this.mUserId = source.mUserId;
        this.mId = source.mId;
        this.mPackageName = source.mPackageName;
        this.mFlags = source.mFlags;
        this.mLastChangedTimestamp = source.mLastChangedTimestamp;
        this.mIconResourceId = source.mIconResourceId;
        if ((cloneFlags & FLAG_HAS_ICON_RES) == 0) {
            this.mActivityComponent = source.mActivityComponent;
            if ((cloneFlags & FLAG_DYNAMIC) == 0) {
                this.mIcon = source.mIcon;
                this.mBitmapPath = source.mBitmapPath;
            }
            this.mTitle = source.mTitle;
            this.mText = source.mText;
            this.mCategories = clone(source.mCategories);
            if ((cloneFlags & FLAG_PINNED) == 0) {
                this.mIntent = source.mIntent;
                this.mIntentPersistableExtras = source.mIntentPersistableExtras;
            }
            this.mWeight = source.mWeight;
            this.mExtras = source.mExtras;
            return;
        }
        this.mFlags |= FLAG_KEY_FIELDS_ONLY;
    }

    public ShortcutInfo clone(int cloneFlags) {
        return new ShortcutInfo(this, cloneFlags);
    }

    public void copyNonNullFieldsFrom(ShortcutInfo source) {
        boolean z = false;
        if (this.mUserId == source.mUserId) {
            z = true;
        }
        Preconditions.checkState(z, "Owner User ID must match");
        Preconditions.checkState(this.mId.equals(source.mId), "ID must match");
        Preconditions.checkState(this.mPackageName.equals(source.mPackageName), "Package name must match");
        if (source.mActivityComponent != null) {
            this.mActivityComponent = source.mActivityComponent;
        }
        if (source.mIcon != null) {
            this.mIcon = source.mIcon;
        }
        if (source.mTitle != null) {
            this.mTitle = source.mTitle;
        }
        if (source.mText != null) {
            this.mText = source.mText;
        }
        if (source.mCategories != null) {
            this.mCategories = clone(source.mCategories);
        }
        if (source.mIntent != null) {
            this.mIntent = source.mIntent;
            this.mIntentPersistableExtras = source.mIntentPersistableExtras;
        }
        if (source.mWeight != 0) {
            this.mWeight = source.mWeight;
        }
        if (source.mExtras != null) {
            this.mExtras = source.mExtras;
        }
        updateTimestamp();
    }

    public static Icon validateIcon(Icon icon) {
        switch (icon.getType()) {
            case FLAG_DYNAMIC /*1*/:
            case FLAG_PINNED /*2*/:
                if (!icon.hasTint()) {
                    return icon;
                }
                throw new IllegalArgumentException("Icons with tints are not supported");
            default:
                throw getInvalidIconException();
        }
    }

    public static IllegalArgumentException getInvalidIconException() {
        return new IllegalArgumentException("Unsupported icon type: only bitmap, resource and content URI are supported");
    }

    public String getId() {
        return this.mId;
    }

    public String getPackageName() {
        return this.mPackageName;
    }

    public ComponentName getActivityComponent() {
        return this.mActivityComponent;
    }

    public Icon getIcon() {
        return this.mIcon;
    }

    public String getTitle() {
        return this.mTitle;
    }

    public String getText() {
        return this.mText;
    }

    public Set<String> getCategories() {
        return this.mCategories;
    }

    public Intent getIntent() {
        Bundle bundle = null;
        if (this.mIntent == null) {
            return null;
        }
        Intent intent = new Intent(this.mIntent);
        if (this.mIntentPersistableExtras != null) {
            bundle = new Bundle(this.mIntentPersistableExtras);
        }
        intent.replaceExtras(bundle);
        return intent;
    }

    public Intent getIntentNoExtras() {
        return this.mIntent;
    }

    public PersistableBundle getIntentPersistableExtras() {
        return this.mIntentPersistableExtras;
    }

    public int getWeight() {
        return this.mWeight;
    }

    public PersistableBundle getExtras() {
        return this.mExtras;
    }

    public int getUserId() {
        return this.mUserId;
    }

    public UserHandle getUserHandle() {
        return UserHandle.of(this.mUserId);
    }

    public long getLastChangedTimestamp() {
        return this.mLastChangedTimestamp;
    }

    public int getFlags() {
        return this.mFlags;
    }

    public void replaceFlags(int flags) {
        this.mFlags = flags;
    }

    public void addFlags(int flags) {
        this.mFlags |= flags;
    }

    public boolean hasFlags(int flags) {
        return (this.mFlags & flags) == flags;
    }

    public boolean isDynamic() {
        return hasFlags(FLAG_DYNAMIC);
    }

    public boolean isPinned() {
        return hasFlags(FLAG_PINNED);
    }

    public boolean hasIconResource() {
        return hasFlags(FLAG_HAS_ICON_RES);
    }

    public boolean hasIconFile() {
        return hasFlags(FLAG_HAS_ICON_FILE);
    }

    public boolean hasKeyFieldsOnly() {
        return hasFlags(FLAG_KEY_FIELDS_ONLY);
    }

    public void updateTimestamp() {
        this.mLastChangedTimestamp = System.currentTimeMillis();
    }

    public void setTimestamp(long value) {
        this.mLastChangedTimestamp = value;
    }

    public void clearIcon() {
        this.mIcon = null;
    }

    public void setIconResourceId(int iconResourceId) {
        this.mIconResourceId = iconResourceId;
    }

    public int getIconResourceId() {
        return this.mIconResourceId;
    }

    public String getBitmapPath() {
        return this.mBitmapPath;
    }

    public void setBitmapPath(String bitmapPath) {
        this.mBitmapPath = bitmapPath;
    }

    private ShortcutInfo(Parcel source) {
        ClassLoader cl = getClass().getClassLoader();
        this.mUserId = source.readInt();
        this.mId = source.readString();
        this.mPackageName = source.readString();
        this.mActivityComponent = (ComponentName) source.readParcelable(cl);
        this.mIcon = (Icon) source.readParcelable(cl);
        this.mTitle = source.readString();
        this.mText = source.readString();
        this.mIntent = (Intent) source.readParcelable(cl);
        this.mIntentPersistableExtras = (PersistableBundle) source.readParcelable(cl);
        this.mWeight = source.readInt();
        this.mExtras = (PersistableBundle) source.readParcelable(cl);
        this.mLastChangedTimestamp = source.readLong();
        this.mFlags = source.readInt();
        this.mIconResourceId = source.readInt();
        this.mBitmapPath = source.readString();
        int N = source.readInt();
        if (N == 0) {
            this.mCategories = null;
            return;
        }
        this.mCategories = new ArraySet(N);
        for (int i = 0; i < N; i += FLAG_DYNAMIC) {
            this.mCategories.add(source.readString().intern());
        }
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mUserId);
        dest.writeString(this.mId);
        dest.writeString(this.mPackageName);
        dest.writeParcelable(this.mActivityComponent, flags);
        dest.writeParcelable(this.mIcon, flags);
        dest.writeString(this.mTitle);
        dest.writeString(this.mText);
        dest.writeParcelable(this.mIntent, flags);
        dest.writeParcelable(this.mIntentPersistableExtras, flags);
        dest.writeInt(this.mWeight);
        dest.writeParcelable(this.mExtras, flags);
        dest.writeLong(this.mLastChangedTimestamp);
        dest.writeInt(this.mFlags);
        dest.writeInt(this.mIconResourceId);
        dest.writeString(this.mBitmapPath);
        if (this.mCategories != null) {
            int N = this.mCategories.size();
            dest.writeInt(N);
            for (int i = 0; i < N; i += FLAG_DYNAMIC) {
                dest.writeString((String) this.mCategories.valueAt(i));
            }
            return;
        }
        dest.writeInt(0);
    }

    public int describeContents() {
        return 0;
    }

    public String toString() {
        return toStringInner(true, false);
    }

    public String toInsecureString() {
        return toStringInner(false, true);
    }

    private String toStringInner(boolean secure, boolean includeInternalData) {
        StringBuilder sb = new StringBuilder();
        sb.append("ShortcutInfo {");
        sb.append("id=");
        sb.append(secure ? "***" : this.mId);
        sb.append(", packageName=");
        sb.append(this.mPackageName);
        if (isDynamic()) {
            sb.append(", dynamic");
        }
        if (isPinned()) {
            sb.append(", pinned");
        }
        sb.append(", activity=");
        sb.append(this.mActivityComponent);
        sb.append(", title=");
        sb.append(secure ? "***" : this.mTitle);
        sb.append(", text=");
        sb.append(secure ? "***" : this.mText);
        sb.append(", categories=");
        sb.append(this.mCategories);
        sb.append(", icon=");
        sb.append(this.mIcon);
        sb.append(", weight=");
        sb.append(this.mWeight);
        sb.append(", timestamp=");
        sb.append(this.mLastChangedTimestamp);
        sb.append(", intent=");
        sb.append(this.mIntent);
        sb.append(", intentExtras=");
        sb.append(secure ? "***" : this.mIntentPersistableExtras);
        sb.append(", extras=");
        sb.append(this.mExtras);
        sb.append(", flags=");
        sb.append(this.mFlags);
        if (includeInternalData) {
            sb.append(", iconRes=");
            sb.append(this.mIconResourceId);
            sb.append(", bitmapPath=");
            sb.append(this.mBitmapPath);
        }
        sb.append("}");
        return sb.toString();
    }

    public ShortcutInfo(int userId, String id, String packageName, ComponentName activityComponent, Icon icon, String title, String text, Set<String> categories, Intent intent, PersistableBundle intentPersistableExtras, int weight, PersistableBundle extras, long lastChangedTimestamp, int flags, int iconResId, String bitmapPath) {
        this.mUserId = userId;
        this.mId = id;
        this.mPackageName = packageName;
        this.mActivityComponent = activityComponent;
        this.mIcon = icon;
        this.mTitle = title;
        this.mText = text;
        this.mCategories = clone((Set) categories);
        this.mIntent = intent;
        this.mIntentPersistableExtras = intentPersistableExtras;
        this.mWeight = weight;
        this.mExtras = extras;
        this.mLastChangedTimestamp = lastChangedTimestamp;
        this.mFlags = flags;
        this.mIconResourceId = iconResId;
        this.mBitmapPath = bitmapPath;
    }
}
