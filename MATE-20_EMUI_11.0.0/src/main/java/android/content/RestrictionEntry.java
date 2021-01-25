package android.content;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.format.DateFormat;
import java.util.Arrays;
import java.util.Objects;

public class RestrictionEntry implements Parcelable {
    public static final Parcelable.Creator<RestrictionEntry> CREATOR = new Parcelable.Creator<RestrictionEntry>() {
        /* class android.content.RestrictionEntry.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public RestrictionEntry createFromParcel(Parcel source) {
            return new RestrictionEntry(source);
        }

        @Override // android.os.Parcelable.Creator
        public RestrictionEntry[] newArray(int size) {
            return new RestrictionEntry[size];
        }
    };
    public static final int TYPE_BOOLEAN = 1;
    public static final int TYPE_BUNDLE = 7;
    public static final int TYPE_BUNDLE_ARRAY = 8;
    public static final int TYPE_CHOICE = 2;
    public static final int TYPE_CHOICE_LEVEL = 3;
    public static final int TYPE_INTEGER = 5;
    public static final int TYPE_MULTI_SELECT = 4;
    public static final int TYPE_NULL = 0;
    public static final int TYPE_STRING = 6;
    private String[] mChoiceEntries;
    private String[] mChoiceValues;
    private String mCurrentValue;
    private String[] mCurrentValues;
    private String mDescription;
    private String mKey;
    private RestrictionEntry[] mRestrictions;
    private String mTitle;
    private int mType;

    public RestrictionEntry(int type, String key) {
        this.mType = type;
        this.mKey = key;
    }

    public RestrictionEntry(String key, String selectedString) {
        this.mKey = key;
        this.mType = 2;
        this.mCurrentValue = selectedString;
    }

    public RestrictionEntry(String key, boolean selectedState) {
        this.mKey = key;
        this.mType = 1;
        setSelectedState(selectedState);
    }

    public RestrictionEntry(String key, String[] selectedStrings) {
        this.mKey = key;
        this.mType = 4;
        this.mCurrentValues = selectedStrings;
    }

    public RestrictionEntry(String key, int selectedInt) {
        this.mKey = key;
        this.mType = 5;
        setIntValue(selectedInt);
    }

    private RestrictionEntry(String key, RestrictionEntry[] restrictionEntries, boolean isBundleArray) {
        this.mKey = key;
        if (isBundleArray) {
            this.mType = 8;
            if (restrictionEntries != null) {
                for (RestrictionEntry restriction : restrictionEntries) {
                    if (restriction.getType() != 7) {
                        throw new IllegalArgumentException("bundle_array restriction can only have nested restriction entries of type bundle");
                    }
                }
            }
        } else {
            this.mType = 7;
        }
        setRestrictions(restrictionEntries);
    }

    public static RestrictionEntry createBundleEntry(String key, RestrictionEntry[] restrictionEntries) {
        return new RestrictionEntry(key, restrictionEntries, false);
    }

    public static RestrictionEntry createBundleArrayEntry(String key, RestrictionEntry[] restrictionEntries) {
        return new RestrictionEntry(key, restrictionEntries, true);
    }

    public void setType(int type) {
        this.mType = type;
    }

    public int getType() {
        return this.mType;
    }

    public String getSelectedString() {
        return this.mCurrentValue;
    }

    public String[] getAllSelectedStrings() {
        return this.mCurrentValues;
    }

    public boolean getSelectedState() {
        return Boolean.parseBoolean(this.mCurrentValue);
    }

    public int getIntValue() {
        return Integer.parseInt(this.mCurrentValue);
    }

    public void setIntValue(int value) {
        this.mCurrentValue = Integer.toString(value);
    }

    public void setSelectedString(String selectedString) {
        this.mCurrentValue = selectedString;
    }

    public void setSelectedState(boolean state) {
        this.mCurrentValue = Boolean.toString(state);
    }

    public void setAllSelectedStrings(String[] allSelectedStrings) {
        this.mCurrentValues = allSelectedStrings;
    }

    public void setChoiceValues(String[] choiceValues) {
        this.mChoiceValues = choiceValues;
    }

    public void setChoiceValues(Context context, int stringArrayResId) {
        this.mChoiceValues = context.getResources().getStringArray(stringArrayResId);
    }

    public RestrictionEntry[] getRestrictions() {
        return this.mRestrictions;
    }

    public void setRestrictions(RestrictionEntry[] restrictions) {
        this.mRestrictions = restrictions;
    }

    public String[] getChoiceValues() {
        return this.mChoiceValues;
    }

    public void setChoiceEntries(String[] choiceEntries) {
        this.mChoiceEntries = choiceEntries;
    }

    public void setChoiceEntries(Context context, int stringArrayResId) {
        this.mChoiceEntries = context.getResources().getStringArray(stringArrayResId);
    }

    public String[] getChoiceEntries() {
        return this.mChoiceEntries;
    }

    public String getDescription() {
        return this.mDescription;
    }

    public void setDescription(String description) {
        this.mDescription = description;
    }

    public String getKey() {
        return this.mKey;
    }

    public String getTitle() {
        return this.mTitle;
    }

    public void setTitle(String title) {
        this.mTitle = title;
    }

    public boolean equals(Object o) {
        String str;
        if (o == this) {
            return true;
        }
        if (!(o instanceof RestrictionEntry)) {
            return false;
        }
        RestrictionEntry other = (RestrictionEntry) o;
        if (this.mType != other.mType || !this.mKey.equals(other.mKey)) {
            return false;
        }
        if (this.mCurrentValues == null && other.mCurrentValues == null && this.mRestrictions == null && other.mRestrictions == null && Objects.equals(this.mCurrentValue, other.mCurrentValue)) {
            return true;
        }
        if (this.mCurrentValue == null && other.mCurrentValue == null && this.mRestrictions == null && other.mRestrictions == null && Arrays.equals(this.mCurrentValues, other.mCurrentValues)) {
            return true;
        }
        String str2 = this.mCurrentValue;
        if (str2 == null && (str = other.mCurrentValue) == null && str2 == null && str == null && Arrays.equals(this.mRestrictions, other.mRestrictions)) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        int result = (17 * 31) + this.mKey.hashCode();
        String str = this.mCurrentValue;
        if (str != null) {
            return (result * 31) + str.hashCode();
        }
        String[] strArr = this.mCurrentValues;
        if (strArr != null) {
            for (String value : strArr) {
                if (value != null) {
                    result = (result * 31) + value.hashCode();
                }
            }
            return result;
        }
        RestrictionEntry[] restrictionEntryArr = this.mRestrictions;
        if (restrictionEntryArr != null) {
            return (result * 31) + Arrays.hashCode(restrictionEntryArr);
        }
        return result;
    }

    public RestrictionEntry(Parcel in) {
        this.mType = in.readInt();
        this.mKey = in.readString();
        this.mTitle = in.readString();
        this.mDescription = in.readString();
        this.mChoiceEntries = in.readStringArray();
        this.mChoiceValues = in.readStringArray();
        this.mCurrentValue = in.readString();
        this.mCurrentValues = in.readStringArray();
        Parcelable[] parcelables = in.readParcelableArray(null);
        if (parcelables != null) {
            this.mRestrictions = new RestrictionEntry[parcelables.length];
            for (int i = 0; i < parcelables.length; i++) {
                this.mRestrictions[i] = (RestrictionEntry) parcelables[i];
            }
        }
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mType);
        dest.writeString(this.mKey);
        dest.writeString(this.mTitle);
        dest.writeString(this.mDescription);
        dest.writeStringArray(this.mChoiceEntries);
        dest.writeStringArray(this.mChoiceValues);
        dest.writeString(this.mCurrentValue);
        dest.writeStringArray(this.mCurrentValues);
        dest.writeParcelableArray(this.mRestrictions, 0);
    }

    public String toString() {
        return "RestrictionEntry{mType=" + this.mType + ", mKey='" + this.mKey + DateFormat.QUOTE + ", mTitle='" + this.mTitle + DateFormat.QUOTE + ", mDescription='" + this.mDescription + DateFormat.QUOTE + ", mChoiceEntries=" + Arrays.toString(this.mChoiceEntries) + ", mChoiceValues=" + Arrays.toString(this.mChoiceValues) + ", mCurrentValue='" + this.mCurrentValue + DateFormat.QUOTE + ", mCurrentValues=" + Arrays.toString(this.mCurrentValues) + ", mRestrictions=" + Arrays.toString(this.mRestrictions) + '}';
    }
}
