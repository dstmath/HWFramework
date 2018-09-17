package android.preference;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.preference.Preference.BaseSavedState;
import android.util.AttributeSet;
import com.android.internal.R;
import java.util.HashSet;
import java.util.Set;

public class MultiSelectListPreference extends DialogPreference {
    private CharSequence[] mEntries;
    private CharSequence[] mEntryValues;
    private Set<String> mNewValues;
    private boolean mPreferenceChanged;
    private Set<String> mValues;

    private static class SavedState extends BaseSavedState {
        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        Set<String> values;

        public SavedState(Parcel source) {
            super(source);
            this.values = new HashSet();
            for (Object add : source.readStringArray()) {
                this.values.add(add);
            }
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }

        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeStringArray((String[]) this.values.toArray(new String[0]));
        }
    }

    public MultiSelectListPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mValues = new HashSet();
        this.mNewValues = new HashSet();
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MultiSelectListPreference, defStyleAttr, defStyleRes);
        this.mEntries = a.getTextArray(0);
        this.mEntryValues = a.getTextArray(1);
        a.recycle();
    }

    public MultiSelectListPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public MultiSelectListPreference(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.dialogPreferenceStyle);
    }

    public MultiSelectListPreference(Context context) {
        this(context, null);
    }

    public void setEntries(CharSequence[] entries) {
        this.mEntries = entries;
    }

    public void setEntries(int entriesResId) {
        setEntries(getContext().getResources().getTextArray(entriesResId));
    }

    public CharSequence[] getEntries() {
        return this.mEntries;
    }

    public void setEntryValues(CharSequence[] entryValues) {
        this.mEntryValues = entryValues;
    }

    public void setEntryValues(int entryValuesResId) {
        setEntryValues(getContext().getResources().getTextArray(entryValuesResId));
    }

    public CharSequence[] getEntryValues() {
        return this.mEntryValues;
    }

    public void setValues(Set<String> values) {
        this.mValues.clear();
        this.mValues.addAll(values);
        persistStringSet(values);
    }

    public Set<String> getValues() {
        return this.mValues;
    }

    public int findIndexOfValue(String value) {
        if (!(value == null || this.mEntryValues == null)) {
            for (int i = this.mEntryValues.length - 1; i >= 0; i--) {
                if (this.mEntryValues[i].equals(value)) {
                    return i;
                }
            }
        }
        return -1;
    }

    protected void onPrepareDialogBuilder(Builder builder) {
        super.onPrepareDialogBuilder(builder);
        if (this.mEntries == null || this.mEntryValues == null) {
            throw new IllegalStateException("MultiSelectListPreference requires an entries array and an entryValues array.");
        }
        builder.setMultiChoiceItems(this.mEntries, getSelectedItems(), new OnMultiChoiceClickListener() {
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                MultiSelectListPreference multiSelectListPreference;
                if (isChecked) {
                    multiSelectListPreference = MultiSelectListPreference.this;
                    multiSelectListPreference.mPreferenceChanged = multiSelectListPreference.mPreferenceChanged | MultiSelectListPreference.this.mNewValues.add(MultiSelectListPreference.this.mEntryValues[which].toString());
                    return;
                }
                multiSelectListPreference = MultiSelectListPreference.this;
                multiSelectListPreference.mPreferenceChanged = multiSelectListPreference.mPreferenceChanged | MultiSelectListPreference.this.mNewValues.remove(MultiSelectListPreference.this.mEntryValues[which].toString());
            }
        });
        this.mNewValues.clear();
        this.mNewValues.addAll(this.mValues);
    }

    private boolean[] getSelectedItems() {
        CharSequence[] entries = this.mEntryValues;
        int entryCount = entries.length;
        Set<String> values = this.mValues;
        boolean[] result = new boolean[entryCount];
        for (int i = 0; i < entryCount; i++) {
            result[i] = values.contains(entries[i].toString());
        }
        return result;
    }

    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        if (positiveResult && this.mPreferenceChanged) {
            Set<String> values = this.mNewValues;
            if (callChangeListener(values)) {
                setValues(values);
            }
        }
        this.mPreferenceChanged = false;
    }

    protected Object onGetDefaultValue(TypedArray a, int index) {
        Set<String> result = new HashSet();
        for (CharSequence charSequence : a.getTextArray(index)) {
            result.add(charSequence.toString());
        }
        return result;
    }

    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        if (restoreValue) {
            defaultValue = getPersistedStringSet(this.mValues);
        } else {
            Set defaultValue2 = (Set) defaultValue;
        }
        setValues(defaultValue);
    }

    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        if (isPersistent()) {
            return superState;
        }
        SavedState myState = new SavedState(superState);
        myState.values = getValues();
        return myState;
    }
}
