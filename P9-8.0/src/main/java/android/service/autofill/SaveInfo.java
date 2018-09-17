package android.service.autofill;

import android.content.IntentSender;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.DebugUtils;
import android.view.autofill.AutofillId;
import android.view.autofill.Helper;
import com.android.internal.util.Preconditions;
import java.util.Arrays;

public final class SaveInfo implements Parcelable {
    public static final Creator<SaveInfo> CREATOR = new Creator<SaveInfo>() {
        public SaveInfo createFromParcel(Parcel parcel) {
            Builder builder = new Builder(parcel.readInt(), (AutofillId[]) parcel.readParcelableArray(null, AutofillId.class));
            builder.setNegativeAction(parcel.readInt(), (IntentSender) parcel.readParcelable(null));
            AutofillId[] optionalIds = (AutofillId[]) parcel.readParcelableArray(null, AutofillId.class);
            if (optionalIds != null) {
                builder.setOptionalIds(optionalIds);
            }
            builder.setDescription(parcel.readCharSequence());
            builder.setFlags(parcel.readInt());
            return builder.build();
        }

        public SaveInfo[] newArray(int size) {
            return new SaveInfo[size];
        }
    };
    public static final int FLAG_SAVE_ON_ALL_VIEWS_INVISIBLE = 1;
    public static final int NEGATIVE_BUTTON_STYLE_CANCEL = 0;
    public static final int NEGATIVE_BUTTON_STYLE_REJECT = 1;
    public static final int SAVE_DATA_TYPE_ADDRESS = 2;
    public static final int SAVE_DATA_TYPE_CREDIT_CARD = 4;
    public static final int SAVE_DATA_TYPE_EMAIL_ADDRESS = 16;
    public static final int SAVE_DATA_TYPE_GENERIC = 0;
    public static final int SAVE_DATA_TYPE_PASSWORD = 1;
    public static final int SAVE_DATA_TYPE_USERNAME = 8;
    private final CharSequence mDescription;
    private final int mFlags;
    private final IntentSender mNegativeActionListener;
    private final int mNegativeButtonStyle;
    private final AutofillId[] mOptionalIds;
    private final AutofillId[] mRequiredIds;
    private final int mType;

    public static final class Builder {
        private CharSequence mDescription;
        private boolean mDestroyed;
        private int mFlags;
        private IntentSender mNegativeActionListener;
        private int mNegativeButtonStyle = 0;
        private AutofillId[] mOptionalIds;
        private final AutofillId[] mRequiredIds;
        private final int mType;

        public Builder(int type, AutofillId[] requiredIds) {
            this.mType = type;
            this.mRequiredIds = assertValid(requiredIds);
        }

        private AutofillId[] assertValid(AutofillId[] ids) {
            boolean z;
            if (ids == null || ids.length <= 0) {
                z = false;
            } else {
                z = true;
            }
            Preconditions.checkArgument(z, "must have at least one id: " + Arrays.toString(ids));
            for (AutofillId id : ids) {
                if (id != null) {
                    z = true;
                } else {
                    z = false;
                }
                Preconditions.checkArgument(z, "cannot have null id: " + Arrays.toString(ids));
            }
            return ids;
        }

        public Builder setFlags(int flags) {
            throwIfDestroyed();
            this.mFlags = Preconditions.checkFlagsArgument(flags, 1);
            return this;
        }

        public Builder setOptionalIds(AutofillId[] ids) {
            throwIfDestroyed();
            this.mOptionalIds = assertValid(ids);
            return this;
        }

        public Builder setDescription(CharSequence description) {
            throwIfDestroyed();
            this.mDescription = description;
            return this;
        }

        public Builder setNegativeAction(int style, IntentSender listener) {
            throwIfDestroyed();
            if (style == 0 || style == 1) {
                this.mNegativeButtonStyle = style;
                this.mNegativeActionListener = listener;
                return this;
            }
            throw new IllegalArgumentException("Invalid style: " + style);
        }

        public SaveInfo build() {
            throwIfDestroyed();
            this.mDestroyed = true;
            return new SaveInfo(this, null);
        }

        private void throwIfDestroyed() {
            if (this.mDestroyed) {
                throw new IllegalStateException("Already called #build()");
            }
        }
    }

    /* synthetic */ SaveInfo(Builder builder, SaveInfo -this1) {
        this(builder);
    }

    private SaveInfo(Builder builder) {
        this.mType = builder.mType;
        this.mNegativeButtonStyle = builder.mNegativeButtonStyle;
        this.mNegativeActionListener = builder.mNegativeActionListener;
        this.mRequiredIds = builder.mRequiredIds;
        this.mOptionalIds = builder.mOptionalIds;
        this.mDescription = builder.mDescription;
        this.mFlags = builder.mFlags;
    }

    public int getNegativeActionStyle() {
        return this.mNegativeButtonStyle;
    }

    public IntentSender getNegativeActionListener() {
        return this.mNegativeActionListener;
    }

    public AutofillId[] getRequiredIds() {
        return this.mRequiredIds;
    }

    public AutofillId[] getOptionalIds() {
        return this.mOptionalIds;
    }

    public int getType() {
        return this.mType;
    }

    public int getFlags() {
        return this.mFlags;
    }

    public CharSequence getDescription() {
        return this.mDescription;
    }

    public String toString() {
        if (Helper.sDebug) {
            return "SaveInfo: [type=" + DebugUtils.flagsToString(SaveInfo.class, "SAVE_DATA_TYPE_", this.mType) + ", requiredIds=" + Arrays.toString(this.mRequiredIds) + ", optionalIds=" + Arrays.toString(this.mOptionalIds) + ", description=" + this.mDescription + DebugUtils.flagsToString(SaveInfo.class, "NEGATIVE_BUTTON_STYLE_", this.mNegativeButtonStyle) + ", mFlags=" + this.mFlags + "]";
        }
        return super.toString();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeInt(this.mType);
        parcel.writeParcelableArray(this.mRequiredIds, flags);
        parcel.writeInt(this.mNegativeButtonStyle);
        parcel.writeParcelable(this.mNegativeActionListener, flags);
        parcel.writeParcelableArray(this.mOptionalIds, flags);
        parcel.writeCharSequence(this.mDescription);
        parcel.writeInt(this.mFlags);
    }
}
