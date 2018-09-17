package android.content.pm;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.graphics.drawable.Icon;
import android.net.ProxyInfo;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.PersistableBundle;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.Log;
import com.android.internal.util.Preconditions;
import java.util.Set;

public final class ShortcutInfo implements Parcelable {
    private static final String ANDROID_PACKAGE_NAME = "android";
    public static final int CLONE_REMOVE_FOR_CREATOR = 9;
    public static final int CLONE_REMOVE_FOR_LAUNCHER = 11;
    public static final int CLONE_REMOVE_FOR_LAUNCHER_APPROVAL = 10;
    private static final int CLONE_REMOVE_ICON = 1;
    private static final int CLONE_REMOVE_INTENT = 2;
    public static final int CLONE_REMOVE_NON_KEY_INFO = 4;
    public static final int CLONE_REMOVE_RES_NAMES = 8;
    public static final Creator<ShortcutInfo> CREATOR = new Creator<ShortcutInfo>() {
        public ShortcutInfo createFromParcel(Parcel source) {
            return new ShortcutInfo(source, null);
        }

        public ShortcutInfo[] newArray(int size) {
            return new ShortcutInfo[size];
        }
    };
    public static final int FLAG_ADAPTIVE_BITMAP = 512;
    public static final int FLAG_DISABLED = 64;
    public static final int FLAG_DYNAMIC = 1;
    public static final int FLAG_HAS_ICON_FILE = 8;
    public static final int FLAG_HAS_ICON_RES = 4;
    public static final int FLAG_ICON_FILE_PENDING_SAVE = 2048;
    public static final int FLAG_IMMUTABLE = 256;
    public static final int FLAG_KEY_FIELDS_ONLY = 16;
    public static final int FLAG_MANIFEST = 32;
    public static final int FLAG_PINNED = 2;
    public static final int FLAG_RETURNED_BY_SERVICE = 1024;
    public static final int FLAG_STRINGS_RESOLVED = 128;
    private static final int IMPLICIT_RANK_MASK = Integer.MAX_VALUE;
    private static final int RANK_CHANGED_BIT = Integer.MIN_VALUE;
    public static final int RANK_NOT_SET = Integer.MAX_VALUE;
    private static final String RES_TYPE_STRING = "string";
    public static final String SHORTCUT_CATEGORY_CONVERSATION = "android.shortcut.conversation";
    static final String TAG = "Shortcut";
    private ComponentName mActivity;
    private String mBitmapPath;
    private ArraySet<String> mCategories;
    private CharSequence mDisabledMessage;
    private int mDisabledMessageResId;
    private String mDisabledMessageResName;
    private PersistableBundle mExtras;
    private int mFlags;
    private Icon mIcon;
    private int mIconResId;
    private String mIconResName;
    private final String mId;
    private int mImplicitRank;
    private PersistableBundle[] mIntentPersistableExtrases;
    private Intent[] mIntents;
    private long mLastChangedTimestamp;
    private final String mPackageName;
    private int mRank;
    private CharSequence mText;
    private int mTextResId;
    private String mTextResName;
    private CharSequence mTitle;
    private int mTitleResId;
    private String mTitleResName;
    private final int mUserId;

    public static class Builder {
        private ComponentName mActivity;
        private Set<String> mCategories;
        private final Context mContext;
        private CharSequence mDisabledMessage;
        private int mDisabledMessageResId;
        private PersistableBundle mExtras;
        private Icon mIcon;
        private String mId;
        private Intent[] mIntents;
        private int mRank = Integer.MAX_VALUE;
        private CharSequence mText;
        private int mTextResId;
        private CharSequence mTitle;
        private int mTitleResId;

        @Deprecated
        public Builder(Context context) {
            this.mContext = context;
        }

        @Deprecated
        public Builder setId(String id) {
            this.mId = (String) Preconditions.checkStringNotEmpty(id, "id cannot be empty");
            return this;
        }

        public Builder(Context context, String id) {
            this.mContext = context;
            this.mId = (String) Preconditions.checkStringNotEmpty(id, "id cannot be empty");
        }

        public Builder setActivity(ComponentName activity) {
            this.mActivity = (ComponentName) Preconditions.checkNotNull(activity, "activity cannot be null");
            return this;
        }

        public Builder setIcon(Icon icon) {
            this.mIcon = ShortcutInfo.validateIcon(icon);
            return this;
        }

        @Deprecated
        public Builder setShortLabelResId(int shortLabelResId) {
            Preconditions.checkState(this.mTitle == null, "shortLabel already set");
            this.mTitleResId = shortLabelResId;
            return this;
        }

        public Builder setShortLabel(CharSequence shortLabel) {
            boolean z = false;
            if (this.mTitleResId == 0) {
                z = true;
            }
            Preconditions.checkState(z, "shortLabelResId already set");
            this.mTitle = Preconditions.checkStringNotEmpty(shortLabel, "shortLabel cannot be empty");
            return this;
        }

        @Deprecated
        public Builder setLongLabelResId(int longLabelResId) {
            Preconditions.checkState(this.mText == null, "longLabel already set");
            this.mTextResId = longLabelResId;
            return this;
        }

        public Builder setLongLabel(CharSequence longLabel) {
            boolean z = false;
            if (this.mTextResId == 0) {
                z = true;
            }
            Preconditions.checkState(z, "longLabelResId already set");
            this.mText = Preconditions.checkStringNotEmpty(longLabel, "longLabel cannot be empty");
            return this;
        }

        @Deprecated
        public Builder setTitle(CharSequence value) {
            return setShortLabel(value);
        }

        @Deprecated
        public Builder setTitleResId(int value) {
            return setShortLabelResId(value);
        }

        @Deprecated
        public Builder setText(CharSequence value) {
            return setLongLabel(value);
        }

        @Deprecated
        public Builder setTextResId(int value) {
            return setLongLabelResId(value);
        }

        @Deprecated
        public Builder setDisabledMessageResId(int disabledMessageResId) {
            Preconditions.checkState(this.mDisabledMessage == null, "disabledMessage already set");
            this.mDisabledMessageResId = disabledMessageResId;
            return this;
        }

        public Builder setDisabledMessage(CharSequence disabledMessage) {
            boolean z = false;
            if (this.mDisabledMessageResId == 0) {
                z = true;
            }
            Preconditions.checkState(z, "disabledMessageResId already set");
            this.mDisabledMessage = Preconditions.checkStringNotEmpty(disabledMessage, "disabledMessage cannot be empty");
            return this;
        }

        public Builder setCategories(Set<String> categories) {
            this.mCategories = categories;
            return this;
        }

        public Builder setIntent(Intent intent) {
            return setIntents(new Intent[]{intent});
        }

        public Builder setIntents(Intent[] intents) {
            Preconditions.checkNotNull(intents, "intents cannot be null");
            Preconditions.checkNotNull(Integer.valueOf(intents.length), "intents cannot be empty");
            for (Intent intent : intents) {
                Preconditions.checkNotNull(intent, "intents cannot contain null");
                Preconditions.checkNotNull(intent.getAction(), "intent's action must be set");
            }
            this.mIntents = ShortcutInfo.cloneIntents(intents);
            return this;
        }

        public Builder setRank(int rank) {
            boolean z = false;
            if (rank >= 0) {
                z = true;
            }
            Preconditions.checkArgument(z, "Rank cannot be negative or bigger than MAX_RANK");
            this.mRank = rank;
            return this;
        }

        public Builder setExtras(PersistableBundle extras) {
            this.mExtras = extras;
            return this;
        }

        public ShortcutInfo build() {
            return new ShortcutInfo(this, null);
        }
    }

    private ShortcutInfo(Builder b) {
        this.mUserId = b.mContext.getUserId();
        this.mId = (String) Preconditions.checkStringNotEmpty(b.mId, "Shortcut ID must be provided");
        this.mPackageName = b.mContext.getPackageName();
        this.mActivity = b.mActivity;
        this.mIcon = b.mIcon;
        this.mTitle = b.mTitle;
        this.mTitleResId = b.mTitleResId;
        this.mText = b.mText;
        this.mTextResId = b.mTextResId;
        this.mDisabledMessage = b.mDisabledMessage;
        this.mDisabledMessageResId = b.mDisabledMessageResId;
        this.mCategories = cloneCategories(b.mCategories);
        this.mIntents = cloneIntents(b.mIntents);
        fixUpIntentExtras();
        this.mRank = b.mRank;
        this.mExtras = b.mExtras;
        updateTimestamp();
    }

    private void fixUpIntentExtras() {
        if (this.mIntents == null) {
            this.mIntentPersistableExtrases = null;
            return;
        }
        this.mIntentPersistableExtrases = new PersistableBundle[this.mIntents.length];
        for (int i = 0; i < this.mIntents.length; i++) {
            Intent intent = this.mIntents[i];
            Bundle extras = intent.getExtras();
            if (extras == null) {
                this.mIntentPersistableExtrases[i] = null;
            } else {
                this.mIntentPersistableExtrases[i] = new PersistableBundle(extras);
                intent.replaceExtras((Bundle) null);
            }
        }
    }

    private static ArraySet<String> cloneCategories(Set<String> source) {
        if (source == null) {
            return null;
        }
        ArraySet<String> ret = new ArraySet(source.size());
        for (String s : source) {
            if (!TextUtils.isEmpty(s)) {
                ret.add(s.toString().intern());
            }
        }
        return ret;
    }

    private static Intent[] cloneIntents(Intent[] intents) {
        if (intents == null) {
            return null;
        }
        Intent[] ret = new Intent[intents.length];
        for (int i = 0; i < ret.length; i++) {
            if (intents[i] != null) {
                ret[i] = new Intent(intents[i]);
            }
        }
        return ret;
    }

    private static PersistableBundle[] clonePersistableBundle(PersistableBundle[] bundle) {
        if (bundle == null) {
            return null;
        }
        PersistableBundle[] ret = new PersistableBundle[bundle.length];
        for (int i = 0; i < ret.length; i++) {
            if (bundle[i] != null) {
                ret[i] = new PersistableBundle(bundle[i]);
            }
        }
        return ret;
    }

    public void enforceMandatoryFields(boolean forPinned) {
        boolean z = false;
        Preconditions.checkStringNotEmpty(this.mId, "Shortcut ID must be provided");
        if (!forPinned) {
            Preconditions.checkNotNull(this.mActivity, "Activity must be provided");
        }
        if (this.mTitle == null && this.mTitleResId == 0) {
            throw new IllegalArgumentException("Short label must be provided");
        }
        Preconditions.checkNotNull(this.mIntents, "Shortcut Intent must be provided");
        if (this.mIntents.length > 0) {
            z = true;
        }
        Preconditions.checkArgument(z, "Shortcut Intent must be provided");
    }

    private ShortcutInfo(ShortcutInfo source, int cloneFlags) {
        this.mUserId = source.mUserId;
        this.mId = source.mId;
        this.mPackageName = source.mPackageName;
        this.mActivity = source.mActivity;
        this.mFlags = source.mFlags;
        this.mLastChangedTimestamp = source.mLastChangedTimestamp;
        this.mIconResId = source.mIconResId;
        if ((cloneFlags & 4) == 0) {
            if ((cloneFlags & 1) == 0) {
                this.mIcon = source.mIcon;
                this.mBitmapPath = source.mBitmapPath;
            }
            this.mTitle = source.mTitle;
            this.mTitleResId = source.mTitleResId;
            this.mText = source.mText;
            this.mTextResId = source.mTextResId;
            this.mDisabledMessage = source.mDisabledMessage;
            this.mDisabledMessageResId = source.mDisabledMessageResId;
            this.mCategories = cloneCategories(source.mCategories);
            if ((cloneFlags & 2) == 0) {
                this.mIntents = cloneIntents(source.mIntents);
                this.mIntentPersistableExtrases = clonePersistableBundle(source.mIntentPersistableExtrases);
            }
            this.mRank = source.mRank;
            this.mExtras = source.mExtras;
            if ((cloneFlags & 8) == 0) {
                this.mTitleResName = source.mTitleResName;
                this.mTextResName = source.mTextResName;
                this.mDisabledMessageResName = source.mDisabledMessageResName;
                this.mIconResName = source.mIconResName;
                return;
            }
            return;
        }
        this.mFlags |= 16;
    }

    private CharSequence getResourceString(Resources res, int resId, CharSequence defValue) {
        try {
            return res.getString(resId);
        } catch (NotFoundException e) {
            Log.e(TAG, "Resource for ID=" + resId + " not found in package " + this.mPackageName);
            return defValue;
        }
    }

    public void resolveResourceStrings(Resources res) {
        this.mFlags |= 128;
        if (this.mTitleResId != 0 || this.mTextResId != 0 || this.mDisabledMessageResId != 0) {
            if (this.mTitleResId != 0) {
                this.mTitle = getResourceString(res, this.mTitleResId, this.mTitle);
            }
            if (this.mTextResId != 0) {
                this.mText = getResourceString(res, this.mTextResId, this.mText);
            }
            if (this.mDisabledMessageResId != 0) {
                this.mDisabledMessage = getResourceString(res, this.mDisabledMessageResId, this.mDisabledMessage);
            }
        }
    }

    public static String lookUpResourceName(Resources res, int resId, boolean withType, String packageName) {
        if (resId == 0) {
            return null;
        }
        try {
            String fullName = res.getResourceName(resId);
            if (ANDROID_PACKAGE_NAME.equals(getResourcePackageName(fullName))) {
                return String.valueOf(resId);
            }
            String resourceTypeAndEntryName;
            if (withType) {
                resourceTypeAndEntryName = getResourceTypeAndEntryName(fullName);
            } else {
                resourceTypeAndEntryName = getResourceEntryName(fullName);
            }
            return resourceTypeAndEntryName;
        } catch (NotFoundException e) {
            Log.e(TAG, "Resource name for ID=" + resId + " not found in package " + packageName + ". Resource IDs may change when the application is upgraded, and the system" + " may not be able to find the correct resource.");
            return null;
        }
    }

    public static String getResourcePackageName(String fullResourceName) {
        int p1 = fullResourceName.indexOf(58);
        if (p1 < 0) {
            return null;
        }
        return fullResourceName.substring(0, p1);
    }

    public static String getResourceTypeName(String fullResourceName) {
        int p1 = fullResourceName.indexOf(58);
        if (p1 < 0) {
            return null;
        }
        int p2 = fullResourceName.indexOf(47, p1 + 1);
        if (p2 < 0) {
            return null;
        }
        return fullResourceName.substring(p1 + 1, p2);
    }

    public static String getResourceTypeAndEntryName(String fullResourceName) {
        int p1 = fullResourceName.indexOf(58);
        if (p1 < 0) {
            return null;
        }
        return fullResourceName.substring(p1 + 1);
    }

    public static String getResourceEntryName(String fullResourceName) {
        int p1 = fullResourceName.indexOf(47);
        if (p1 < 0) {
            return null;
        }
        return fullResourceName.substring(p1 + 1);
    }

    public static int lookUpResourceId(Resources res, String resourceName, String resourceType, String packageName) {
        if (resourceName == null) {
            return 0;
        }
        try {
            return Integer.parseInt(resourceName);
        } catch (NumberFormatException e) {
            try {
                Log.e(TAG, "lookUpResourceId()");
                return res.getIdentifier(resourceName, resourceType, packageName);
            } catch (NotFoundException e2) {
                Log.e(TAG, "Resource ID for name=" + resourceName + " not found in package " + packageName);
                return 0;
            }
        }
    }

    public void lookupAndFillInResourceNames(Resources res) {
        if (this.mTitleResId != 0 || this.mTextResId != 0 || this.mDisabledMessageResId != 0 || this.mIconResId != 0) {
            this.mTitleResName = lookUpResourceName(res, this.mTitleResId, false, this.mPackageName);
            this.mTextResName = lookUpResourceName(res, this.mTextResId, false, this.mPackageName);
            this.mDisabledMessageResName = lookUpResourceName(res, this.mDisabledMessageResId, false, this.mPackageName);
            this.mIconResName = lookUpResourceName(res, this.mIconResId, true, this.mPackageName);
        }
    }

    public void lookupAndFillInResourceIds(Resources res) {
        if (this.mTitleResName != null || this.mTextResName != null || this.mDisabledMessageResName != null || this.mIconResName != null) {
            this.mTitleResId = lookUpResourceId(res, this.mTitleResName, RES_TYPE_STRING, this.mPackageName);
            this.mTextResId = lookUpResourceId(res, this.mTextResName, RES_TYPE_STRING, this.mPackageName);
            this.mDisabledMessageResId = lookUpResourceId(res, this.mDisabledMessageResName, RES_TYPE_STRING, this.mPackageName);
            this.mIconResId = lookUpResourceId(res, this.mIconResName, null, this.mPackageName);
        }
    }

    public ShortcutInfo clone(int cloneFlags) {
        return new ShortcutInfo(this, cloneFlags);
    }

    public void ensureUpdatableWith(ShortcutInfo source) {
        Preconditions.checkState(this.mUserId == source.mUserId, "Owner User ID must match");
        Preconditions.checkState(this.mId.equals(source.mId), "ID must match");
        Preconditions.checkState(this.mPackageName.equals(source.mPackageName), "Package name must match");
        Preconditions.checkState(isImmutable() ^ 1, "Target ShortcutInfo is immutable");
    }

    public void copyNonNullFieldsFrom(ShortcutInfo source) {
        ensureUpdatableWith(source);
        if (source.mActivity != null) {
            this.mActivity = source.mActivity;
        }
        if (source.mIcon != null) {
            this.mIcon = source.mIcon;
            this.mIconResId = 0;
            this.mIconResName = null;
            this.mBitmapPath = null;
        }
        if (source.mTitle != null) {
            this.mTitle = source.mTitle;
            this.mTitleResId = 0;
            this.mTitleResName = null;
        } else if (source.mTitleResId != 0) {
            this.mTitle = null;
            this.mTitleResId = source.mTitleResId;
            this.mTitleResName = null;
        }
        if (source.mText != null) {
            this.mText = source.mText;
            this.mTextResId = 0;
            this.mTextResName = null;
        } else if (source.mTextResId != 0) {
            this.mText = null;
            this.mTextResId = source.mTextResId;
            this.mTextResName = null;
        }
        if (source.mDisabledMessage != null) {
            this.mDisabledMessage = source.mDisabledMessage;
            this.mDisabledMessageResId = 0;
            this.mDisabledMessageResName = null;
        } else if (source.mDisabledMessageResId != 0) {
            this.mDisabledMessage = null;
            this.mDisabledMessageResId = source.mDisabledMessageResId;
            this.mDisabledMessageResName = null;
        }
        if (source.mCategories != null) {
            this.mCategories = cloneCategories(source.mCategories);
        }
        if (source.mIntents != null) {
            this.mIntents = cloneIntents(source.mIntents);
            this.mIntentPersistableExtrases = clonePersistableBundle(source.mIntentPersistableExtrases);
        }
        if (source.mRank != Integer.MAX_VALUE) {
            this.mRank = source.mRank;
        }
        if (source.mExtras != null) {
            this.mExtras = source.mExtras;
        }
    }

    public static Icon validateIcon(Icon icon) {
        switch (icon.getType()) {
            case 1:
            case 2:
            case 5:
                if (!icon.hasTint()) {
                    return icon;
                }
                throw new IllegalArgumentException("Icons with tints are not supported");
            default:
                throw getInvalidIconException();
        }
    }

    public static IllegalArgumentException getInvalidIconException() {
        return new IllegalArgumentException("Unsupported icon type: only the bitmap and resource types are supported");
    }

    public String getId() {
        return this.mId;
    }

    public String getPackage() {
        return this.mPackageName;
    }

    public ComponentName getActivity() {
        return this.mActivity;
    }

    public void setActivity(ComponentName activity) {
        this.mActivity = activity;
    }

    public Icon getIcon() {
        return this.mIcon;
    }

    @Deprecated
    public CharSequence getTitle() {
        return this.mTitle;
    }

    @Deprecated
    public int getTitleResId() {
        return this.mTitleResId;
    }

    @Deprecated
    public CharSequence getText() {
        return this.mText;
    }

    @Deprecated
    public int getTextResId() {
        return this.mTextResId;
    }

    public CharSequence getShortLabel() {
        return this.mTitle;
    }

    public int getShortLabelResourceId() {
        return this.mTitleResId;
    }

    public CharSequence getLongLabel() {
        return this.mText;
    }

    public int getLongLabelResourceId() {
        return this.mTextResId;
    }

    public CharSequence getDisabledMessage() {
        return this.mDisabledMessage;
    }

    public int getDisabledMessageResourceId() {
        return this.mDisabledMessageResId;
    }

    public Set<String> getCategories() {
        return this.mCategories;
    }

    public Intent getIntent() {
        if (this.mIntents == null || this.mIntents.length == 0) {
            return null;
        }
        int last = this.mIntents.length - 1;
        return setIntentExtras(new Intent(this.mIntents[last]), this.mIntentPersistableExtrases[last]);
    }

    public Intent[] getIntents() {
        Intent[] ret = new Intent[this.mIntents.length];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = new Intent(this.mIntents[i]);
            setIntentExtras(ret[i], this.mIntentPersistableExtrases[i]);
        }
        return ret;
    }

    public Intent[] getIntentsNoExtras() {
        return this.mIntents;
    }

    public PersistableBundle[] getIntentPersistableExtrases() {
        return this.mIntentPersistableExtrases;
    }

    public int getRank() {
        return this.mRank;
    }

    public boolean hasRank() {
        return this.mRank != Integer.MAX_VALUE;
    }

    public void setRank(int rank) {
        this.mRank = rank;
    }

    public void clearImplicitRankAndRankChangedFlag() {
        this.mImplicitRank = 0;
    }

    public void setImplicitRank(int rank) {
        this.mImplicitRank = (this.mImplicitRank & Integer.MIN_VALUE) | (Integer.MAX_VALUE & rank);
    }

    public int getImplicitRank() {
        return this.mImplicitRank & Integer.MAX_VALUE;
    }

    public void setRankChanged() {
        this.mImplicitRank |= Integer.MIN_VALUE;
    }

    public boolean isRankChanged() {
        return (this.mImplicitRank & Integer.MIN_VALUE) != 0;
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

    public void clearFlags(int flags) {
        this.mFlags &= ~flags;
    }

    public boolean hasFlags(int flags) {
        return (this.mFlags & flags) == flags;
    }

    public boolean isReturnedByServer() {
        return hasFlags(1024);
    }

    public void setReturnedByServer() {
        addFlags(1024);
    }

    public boolean isDynamic() {
        return hasFlags(1);
    }

    public boolean isPinned() {
        return hasFlags(2);
    }

    public boolean isDeclaredInManifest() {
        return hasFlags(32);
    }

    @Deprecated
    public boolean isManifestShortcut() {
        return isDeclaredInManifest();
    }

    public boolean isFloating() {
        if (!isPinned()) {
            return false;
        }
        return (!isDynamic() ? isManifestShortcut() : 1) ^ 1;
    }

    public boolean isOriginallyFromManifest() {
        return hasFlags(256);
    }

    public boolean isImmutable() {
        return hasFlags(256);
    }

    public boolean isEnabled() {
        return hasFlags(64) ^ 1;
    }

    public boolean isAlive() {
        return (hasFlags(2) || hasFlags(1)) ? true : hasFlags(32);
    }

    public boolean usesQuota() {
        return !hasFlags(1) ? hasFlags(32) : true;
    }

    public boolean hasIconResource() {
        return hasFlags(4);
    }

    public boolean hasStringResources() {
        return (this.mTitleResId == 0 && this.mTextResId == 0 && this.mDisabledMessageResId == 0) ? false : true;
    }

    public boolean hasAnyResources() {
        return !hasIconResource() ? hasStringResources() : true;
    }

    public boolean hasIconFile() {
        return hasFlags(8);
    }

    public boolean hasAdaptiveBitmap() {
        return hasFlags(512);
    }

    public boolean isIconPendingSave() {
        return hasFlags(2048);
    }

    public void setIconPendingSave() {
        addFlags(2048);
    }

    public void clearIconPendingSave() {
        clearFlags(2048);
    }

    public boolean hasKeyFieldsOnly() {
        return hasFlags(16);
    }

    public boolean hasStringResourcesResolved() {
        return hasFlags(128);
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
        if (this.mIconResId != iconResourceId) {
            this.mIconResName = null;
        }
        this.mIconResId = iconResourceId;
    }

    public int getIconResourceId() {
        return this.mIconResId;
    }

    public String getBitmapPath() {
        return this.mBitmapPath;
    }

    public void setBitmapPath(String bitmapPath) {
        this.mBitmapPath = bitmapPath;
    }

    public void setDisabledMessageResId(int disabledMessageResId) {
        if (this.mDisabledMessageResId != disabledMessageResId) {
            this.mDisabledMessageResName = null;
        }
        this.mDisabledMessageResId = disabledMessageResId;
        this.mDisabledMessage = null;
    }

    public void setDisabledMessage(String disabledMessage) {
        this.mDisabledMessage = disabledMessage;
        this.mDisabledMessageResId = 0;
        this.mDisabledMessageResName = null;
    }

    public String getTitleResName() {
        return this.mTitleResName;
    }

    public void setTitleResName(String titleResName) {
        this.mTitleResName = titleResName;
    }

    public String getTextResName() {
        return this.mTextResName;
    }

    public void setTextResName(String textResName) {
        this.mTextResName = textResName;
    }

    public String getDisabledMessageResName() {
        return this.mDisabledMessageResName;
    }

    public void setDisabledMessageResName(String disabledMessageResName) {
        this.mDisabledMessageResName = disabledMessageResName;
    }

    public String getIconResName() {
        return this.mIconResName;
    }

    public void setIconResName(String iconResName) {
        this.mIconResName = iconResName;
    }

    public void setIntents(Intent[] intents) throws IllegalArgumentException {
        boolean z = false;
        Preconditions.checkNotNull(intents);
        if (intents.length > 0) {
            z = true;
        }
        Preconditions.checkArgument(z);
        this.mIntents = cloneIntents(intents);
        fixUpIntentExtras();
    }

    public static Intent setIntentExtras(Intent intent, PersistableBundle extras) {
        if (extras == null) {
            intent.replaceExtras((Bundle) null);
        } else {
            intent.replaceExtras(new Bundle(extras));
        }
        return intent;
    }

    public void setCategories(Set<String> categories) {
        this.mCategories = cloneCategories(categories);
    }

    private ShortcutInfo(Parcel source) {
        ClassLoader cl = getClass().getClassLoader();
        this.mUserId = source.readInt();
        this.mId = source.readString();
        this.mPackageName = source.readString();
        this.mActivity = (ComponentName) source.readParcelable(cl);
        this.mFlags = source.readInt();
        this.mIconResId = source.readInt();
        this.mLastChangedTimestamp = source.readLong();
        if (source.readInt() != 0) {
            this.mIcon = (Icon) source.readParcelable(cl);
            this.mTitle = source.readCharSequence();
            this.mTitleResId = source.readInt();
            this.mText = source.readCharSequence();
            this.mTextResId = source.readInt();
            this.mDisabledMessage = source.readCharSequence();
            this.mDisabledMessageResId = source.readInt();
            this.mIntents = (Intent[]) source.readParcelableArray(cl, Intent.class);
            this.mIntentPersistableExtrases = (PersistableBundle[]) source.readParcelableArray(cl, PersistableBundle.class);
            this.mRank = source.readInt();
            this.mExtras = (PersistableBundle) source.readParcelable(cl);
            this.mBitmapPath = source.readString();
            this.mIconResName = source.readString();
            this.mTitleResName = source.readString();
            this.mTextResName = source.readString();
            this.mDisabledMessageResName = source.readString();
            int N = source.readInt();
            if (N == 0) {
                this.mCategories = null;
            } else {
                this.mCategories = new ArraySet(N);
                for (int i = 0; i < N; i++) {
                    this.mCategories.add(source.readString().intern());
                }
            }
        }
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mUserId);
        dest.writeString(this.mId);
        dest.writeString(this.mPackageName);
        dest.writeParcelable(this.mActivity, flags);
        dest.writeInt(this.mFlags);
        dest.writeInt(this.mIconResId);
        dest.writeLong(this.mLastChangedTimestamp);
        if (hasKeyFieldsOnly()) {
            dest.writeInt(0);
            return;
        }
        dest.writeInt(1);
        dest.writeParcelable(this.mIcon, flags);
        dest.writeCharSequence(this.mTitle);
        dest.writeInt(this.mTitleResId);
        dest.writeCharSequence(this.mText);
        dest.writeInt(this.mTextResId);
        dest.writeCharSequence(this.mDisabledMessage);
        dest.writeInt(this.mDisabledMessageResId);
        dest.writeParcelableArray(this.mIntents, flags);
        dest.writeParcelableArray(this.mIntentPersistableExtrases, flags);
        dest.writeInt(this.mRank);
        dest.writeParcelable(this.mExtras, flags);
        dest.writeString(this.mBitmapPath);
        dest.writeString(this.mIconResName);
        dest.writeString(this.mTitleResName);
        dest.writeString(this.mTextResName);
        dest.writeString(this.mDisabledMessageResName);
        if (this.mCategories != null) {
            int N = this.mCategories.size();
            dest.writeInt(N);
            for (int i = 0; i < N; i++) {
                dest.writeString((String) this.mCategories.valueAt(i));
            }
        } else {
            dest.writeInt(0);
        }
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
        sb.append(", flags=0x");
        sb.append(Integer.toHexString(this.mFlags));
        sb.append(" [");
        if (!isEnabled()) {
            sb.append("X");
        }
        if (isImmutable()) {
            sb.append("Im");
        }
        if (isManifestShortcut()) {
            sb.append("M");
        }
        if (isDynamic()) {
            sb.append("D");
        }
        if (isPinned()) {
            sb.append("P");
        }
        if (hasIconFile()) {
            sb.append("If");
        }
        if (isIconPendingSave()) {
            sb.append("^");
        }
        if (hasIconResource()) {
            sb.append("Ir");
        }
        if (hasKeyFieldsOnly()) {
            sb.append("K");
        }
        if (hasStringResourcesResolved()) {
            sb.append("Sr");
        }
        if (isReturnedByServer()) {
            sb.append("V");
        }
        sb.append("]");
        sb.append(", packageName=");
        sb.append(this.mPackageName);
        sb.append(", activity=");
        sb.append(this.mActivity);
        sb.append(", shortLabel=");
        sb.append(secure ? "***" : this.mTitle);
        sb.append(", resId=");
        sb.append(this.mTitleResId);
        sb.append("[");
        sb.append(this.mTitleResName);
        sb.append("]");
        sb.append(", longLabel=");
        sb.append(secure ? "***" : this.mText);
        sb.append(", resId=");
        sb.append(this.mTextResId);
        sb.append("[");
        sb.append(this.mTextResName);
        sb.append("]");
        sb.append(", disabledMessage=");
        sb.append(secure ? "***" : this.mDisabledMessage);
        sb.append(", resId=");
        sb.append(this.mDisabledMessageResId);
        sb.append("[");
        sb.append(this.mDisabledMessageResName);
        sb.append("]");
        sb.append(", categories=");
        sb.append(this.mCategories);
        sb.append(", icon=");
        sb.append(this.mIcon);
        sb.append(", rank=");
        sb.append(this.mRank);
        sb.append(", timestamp=");
        sb.append(this.mLastChangedTimestamp);
        sb.append(", intents=");
        if (this.mIntents == null) {
            sb.append("null");
        } else if (secure) {
            sb.append("size:");
            sb.append(this.mIntents.length);
        } else {
            int size = this.mIntents.length;
            sb.append("[");
            String sep = ProxyInfo.LOCAL_EXCL_LIST;
            for (int i = 0; i < size; i++) {
                sb.append(sep);
                sep = ", ";
                sb.append(this.mIntents[i]);
                sb.append("/");
                sb.append(this.mIntentPersistableExtrases[i]);
            }
            sb.append("]");
        }
        sb.append(", extras=");
        sb.append(this.mExtras);
        if (includeInternalData) {
            sb.append(", iconRes=");
            sb.append(this.mIconResId);
            sb.append("[");
            sb.append(this.mIconResName);
            sb.append("]");
            sb.append(", bitmapPath=");
            sb.append(this.mBitmapPath);
        }
        sb.append("}");
        return sb.toString();
    }

    public ShortcutInfo(int userId, String id, String packageName, ComponentName activity, Icon icon, CharSequence title, int titleResId, String titleResName, CharSequence text, int textResId, String textResName, CharSequence disabledMessage, int disabledMessageResId, String disabledMessageResName, Set<String> categories, Intent[] intentsWithExtras, int rank, PersistableBundle extras, long lastChangedTimestamp, int flags, int iconResId, String iconResName, String bitmapPath) {
        this.mUserId = userId;
        this.mId = id;
        this.mPackageName = packageName;
        this.mActivity = activity;
        this.mIcon = icon;
        this.mTitle = title;
        this.mTitleResId = titleResId;
        this.mTitleResName = titleResName;
        this.mText = text;
        this.mTextResId = textResId;
        this.mTextResName = textResName;
        this.mDisabledMessage = disabledMessage;
        this.mDisabledMessageResId = disabledMessageResId;
        this.mDisabledMessageResName = disabledMessageResName;
        this.mCategories = cloneCategories(categories);
        this.mIntents = cloneIntents(intentsWithExtras);
        fixUpIntentExtras();
        this.mRank = rank;
        this.mExtras = extras;
        this.mLastChangedTimestamp = lastChangedTimestamp;
        this.mFlags = flags;
        this.mIconResId = iconResId;
        this.mIconResName = iconResName;
        this.mBitmapPath = bitmapPath;
    }
}
