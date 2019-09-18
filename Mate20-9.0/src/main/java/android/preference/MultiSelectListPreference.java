package android.preference;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.Preference;
import android.util.AttributeSet;
import com.android.internal.R;
import java.util.HashSet;
import java.util.Set;

public class MultiSelectListPreference extends DialogPreference {
    private CharSequence[] mEntries;
    /* access modifiers changed from: private */
    public CharSequence[] mEntryValues;
    /* access modifiers changed from: private */
    public Set<String> mNewValues;
    private boolean mPreferenceChanged;
    private Set<String> mValues;

    private static class SavedState extends Preference.BaseSavedState {
        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
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
            for (String add : source.readStringArray()) {
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

    /* JADX WARNING: type inference failed for: r0v2, types: [boolean, byte] */
    static /* synthetic */ boolean access$076(MultiSelectListPreference x0, int x1) {
        ? r0 = (byte) (x0.mPreferenceChanged | x1);
        x0.mPreferenceChanged = r0;
        return r0;
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
        this(context, attrs, 16842897);
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

    /* access modifiers changed from: protected */
    public void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        super.onPrepareDialogBuilder(builder);
        if (this.mEntries == null || this.mEntryValues == null) {
            throw new IllegalStateException("MultiSelectListPreference requires an entries array and an entryValues array.");
        }
        builder.setMultiChoiceItems(this.mEntries, getSelectedItems(), new DialogInterface.OnMultiChoiceClickListener() {
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                if (isChecked) {
                    MultiSelectListPreference.access$076(MultiSelectListPreference.this, MultiSelectListPreference.this.mNewValues.add(MultiSelectListPreference.this.mEntryValues[which].toString()) ? 1 : 0);
                } else {
                    MultiSelectListPreference.access$076(MultiSelectListPreference.this, MultiSelectListPreference.this.mNewValues.remove(MultiSelectListPreference.this.mEntryValues[which].toString()) ? 1 : 0);
                }
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

    /* access modifiers changed from: protected */
    public void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        if (positiveResult && this.mPreferenceChanged) {
            Set<String> values = this.mNewValues;
            if (callChangeListener(values)) {
                setValues(values);
            }
        }
        this.mPreferenceChanged = false;
    }

    /* access modifiers changed from: protected */
    public Object onGetDefaultValue(TypedArray a, int index) {
        Set<String> result = new HashSet<>();
        for (CharSequence charSequence : a.getTextArray(index)) {
            result.add(charSequence.toString());
        }
        return result;
    }

    /* access modifiers changed from: protected */
    public void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        setValues(restoreValue ? getPersistedStringSet(this.mValues) : (Set) defaultValue);
    }

    /* access modifiers changed from: protected */
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        if (isPersistent()) {
            return superState;
        }
        SavedState myState = new SavedState(superState);
        myState.values = getValues();
        return myState;
    }
}
