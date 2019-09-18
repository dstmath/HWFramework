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
import java.util.Arrays;

public class MultiCheckPreference extends DialogPreference {
    private CharSequence[] mEntries;
    private String[] mEntryValues;
    private boolean[] mOrigValues;
    /* access modifiers changed from: private */
    public boolean[] mSetValues;
    private String mSummary;

    private static class SavedState extends Preference.BaseSavedState {
        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        boolean[] values;

        public SavedState(Parcel source) {
            super(source);
            this.values = source.createBooleanArray();
        }

        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeBooleanArray(this.values);
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }
    }

    public MultiCheckPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ListPreference, defStyleAttr, defStyleRes);
        this.mEntries = a.getTextArray(0);
        if (this.mEntries != null) {
            setEntries(this.mEntries);
        }
        setEntryValuesCS(a.getTextArray(1));
        a.recycle();
        TypedArray a2 = context.obtainStyledAttributes(attrs, R.styleable.Preference, 0, 0);
        this.mSummary = a2.getString(7);
        a2.recycle();
    }

    public MultiCheckPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public MultiCheckPreference(Context context, AttributeSet attrs) {
        this(context, attrs, 16842897);
    }

    public MultiCheckPreference(Context context) {
        this(context, null);
    }

    public void setEntries(CharSequence[] entries) {
        this.mEntries = entries;
        this.mSetValues = new boolean[entries.length];
        this.mOrigValues = new boolean[entries.length];
    }

    public void setEntries(int entriesResId) {
        setEntries(getContext().getResources().getTextArray(entriesResId));
    }

    public CharSequence[] getEntries() {
        return this.mEntries;
    }

    public void setEntryValues(String[] entryValues) {
        this.mEntryValues = entryValues;
        Arrays.fill(this.mSetValues, false);
        Arrays.fill(this.mOrigValues, false);
    }

    public void setEntryValues(int entryValuesResId) {
        setEntryValuesCS(getContext().getResources().getTextArray(entryValuesResId));
    }

    private void setEntryValuesCS(CharSequence[] values) {
        setValues(null);
        if (values != null) {
            this.mEntryValues = new String[values.length];
            for (int i = 0; i < values.length; i++) {
                this.mEntryValues[i] = values[i].toString();
            }
        }
    }

    public String[] getEntryValues() {
        return this.mEntryValues;
    }

    public boolean getValue(int index) {
        return this.mSetValues[index];
    }

    public void setValue(int index, boolean state) {
        this.mSetValues[index] = state;
    }

    public void setValues(boolean[] values) {
        if (this.mSetValues != null) {
            Arrays.fill(this.mSetValues, false);
            Arrays.fill(this.mOrigValues, false);
            if (values != null) {
                System.arraycopy(values, 0, this.mSetValues, 0, values.length < this.mSetValues.length ? values.length : this.mSetValues.length);
            }
        }
    }

    public CharSequence getSummary() {
        if (this.mSummary == null) {
            return super.getSummary();
        }
        return this.mSummary;
    }

    public void setSummary(CharSequence summary) {
        super.setSummary(summary);
        if (summary == null && this.mSummary != null) {
            this.mSummary = null;
        } else if (summary != null && !summary.equals(this.mSummary)) {
            this.mSummary = summary.toString();
        }
    }

    public boolean[] getValues() {
        return this.mSetValues;
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
            throw new IllegalStateException("ListPreference requires an entries array and an entryValues array.");
        }
        this.mOrigValues = Arrays.copyOf(this.mSetValues, this.mSetValues.length);
        builder.setMultiChoiceItems(this.mEntries, this.mSetValues, new DialogInterface.OnMultiChoiceClickListener() {
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                MultiCheckPreference.this.mSetValues[which] = isChecked;
            }
        });
    }

    /* access modifiers changed from: protected */
    public void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        if (!positiveResult || !callChangeListener(getValues())) {
            System.arraycopy(this.mOrigValues, 0, this.mSetValues, 0, this.mSetValues.length);
        }
    }

    /* access modifiers changed from: protected */
    public Object onGetDefaultValue(TypedArray a, int index) {
        return a.getString(index);
    }

    /* access modifiers changed from: protected */
    public void onSetInitialValue(boolean restoreValue, Object defaultValue) {
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

    /* access modifiers changed from: protected */
    public void onRestoreInstanceState(Parcelable state) {
        if (state == null || !state.getClass().equals(SavedState.class)) {
            super.onRestoreInstanceState(state);
            return;
        }
        SavedState myState = (SavedState) state;
        super.onRestoreInstanceState(myState.getSuperState());
        setValues(myState.values);
    }
}
